package app.cmesh.sync;

import app.cmesh.aws.*;
import app.cmesh.azure.*;
import app.cmesh.dashboard.CloudCredentials;
import app.cmesh.dashboard.Resource;
import app.cmesh.dashboard.enums.CloudProvider;
import app.cmesh.dashboard.enums.ResourceStatus;
import app.cmesh.dashboard.enums.ResourceType;
import app.cmesh.dashboard.repository.CloudCredentialsRepository;
import app.cmesh.dashboard.repository.ResourceRepository;
import app.cmesh.docean.*;
import app.cmesh.docean.model.Droplet;
import app.cmesh.gcp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Background service that periodically syncs cloud resources from all
 * providers.
 * Runs on a scheduled interval to keep the resource inventory up-to-date.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceSyncService {

    private final CloudCredentialsRepository credentialsRepository;
    private final ResourceRepository resourceRepository;

    // AWS services
    private final AwsEc2Service awsEc2Service;
    private final AwsS3Service awsS3Service;
    private final StsService stsService;

    // GCP services
    private final GcpComputeService gcpComputeService;

    // Azure services
    private final AzureComputeService azureComputeService;

    // Assignment service
    private final app.cmesh.project.ResourceAssignmentService assignmentService;

    // Metrics
    private final app.cmesh.observability.MetricsService metricsService;

    /**
     * Scheduled sync job - runs every 15 minutes by default.
     * Can be configured via application.properties: cloudmesh.sync.interval
     */
    @Scheduled(fixedDelayString = "${cloudmesh.sync.interval:900000}")
    public void syncAllResources() {
        log.info("[ResourceSync] Starting scheduled resource sync");
        long startTime = System.currentTimeMillis();

        List<CloudCredentials> activeCredentials = credentialsRepository
                .findByStatus(CloudCredentials.CredentialStatus.ACTIVE);

        log.info("[ResourceSync] Found {} active credentials to sync", activeCredentials.size());

        int successCount = 0;
        int failureCount = 0;

        for (CloudCredentials credential : activeCredentials) {
            long credentialStartTime = System.currentTimeMillis();
            String provider = credential.getProvider().name();

            try {
                syncCredential(credential);
                successCount++;
                metricsService.recordResourceSyncSuccess(provider);
            } catch (Exception e) {
                failureCount++;
                metricsService.recordResourceSyncFailure(provider);
                log.error("[ResourceSync] Failed to sync credential {}: {}",
                        credential.getFriendlyName(), e.getMessage(), e);

                // Update credential with failure status
                credential.setLastSyncAt(LocalDateTime.now());
                credential.setLastSyncStatus("FAILED");
                credential.setLastSyncError(e.getMessage());
                credentialsRepository.save(credential);
            } finally {
                long credentialDuration = System.currentTimeMillis() - credentialStartTime;
                metricsService.recordSyncDuration(provider, credentialDuration);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("[ResourceSync] Completed in {}ms - {} success, {} failed",
                duration, successCount, failureCount);
    }

    /**
     * Syncs resources for a single credential.
     */
    @Transactional
    protected void syncCredential(CloudCredentials credential) {
        log.info("[ResourceSync] Syncing credential: {} ({})",
                credential.getFriendlyName(), credential.getProvider());

        credential.setLastSyncAt(LocalDateTime.now());
        credential.setLastSyncStatus("IN_PROGRESS");
        credentialsRepository.save(credential);

        try {
            switch (credential.getProvider()) {
                case AWS -> syncAwsResources(credential);
                case GCP -> syncGcpResources(credential);
                case AZURE -> syncAzureResources(credential);
                case DIGITALOCEAN -> syncDigitalOceanResources(credential);
            }

            credential.setLastSyncStatus("SUCCESS");
            credential.setLastSyncError(null);
        } catch (Exception e) {
            credential.setLastSyncStatus("FAILED");
            credential.setLastSyncError(e.getMessage());
            throw e;
        } finally {
            credentialsRepository.save(credential);
        }

        // Apply assignment rules after successful sync
        if ("SUCCESS".equals(credential.getLastSyncStatus())) {
            try {
                assignmentService.applyAssignmentRules();
            } catch (Exception e) {
                log.warn("[ResourceSync] Failed to apply assignment rules: {}", e.getMessage());
            }
        }
    }

    private void syncAwsResources(CloudCredentials credential) {
        Map<String, Object> config = credential.getProviderConfig();
        ConnectedAwsAccount account = new ConnectedAwsAccount(
                null, // id
                (String) config.get("roleArn"),
                (String) config.get("externalId"),
                (String) config.getOrDefault("region", "us-east-1"));

        int ec2Count = syncAwsEc2Instances(credential, account);
        int s3Count = syncAwsS3Buckets(credential, account);

        log.info("[ResourceSync] AWS sync complete: {} EC2, {} S3", ec2Count, s3Count);
    }

    private int syncAwsEc2Instances(CloudCredentials credential, ConnectedAwsAccount account) {
        // For now, just get instance IDs - we'll enhance this later with full details
        List<String> instanceIds = awsEc2Service.listInstanceIds(account);

        for (String instanceId : instanceIds) {
            Resource resource = resourceRepository
                    .findByProviderResourceId(instanceId)
                    .orElse(new Resource());

            resource.setProviderResourceId(instanceId);
            resource.setResourceName(instanceId);
            resource.setProvider(CloudProvider.AWS);
            resource.setResourceType(ResourceType.EC2_INSTANCE);
            resource.setResourceStatus(ResourceStatus.RUNNING);
            resource.setResourceRegion(account.region());
            resource.setResourceCost(0.0);
            resource.setLastSyncedAt(LocalDateTime.now());

            // TODO: Set project - for now skip if no project
            // resourceRepository.save(resource);
        }

        return instanceIds.size();
    }

    private int syncAwsS3Buckets(CloudCredentials credential, ConnectedAwsAccount account) {
        List<String> bucketNames = awsS3Service.listBuckets(account);

        for (String bucketName : bucketNames) {
            Resource resource = resourceRepository
                    .findByProviderResourceId(bucketName)
                    .orElse(new Resource());

            resource.setProviderResourceId(bucketName);
            resource.setResourceName(bucketName);
            resource.setProvider(CloudProvider.AWS);
            resource.setResourceType(ResourceType.S3_BUCKET);
            resource.setResourceStatus(ResourceStatus.RUNNING);
            resource.setResourceRegion(account.region());
            resource.setResourceCost(0.0);
            resource.setLastSyncedAt(LocalDateTime.now());

            // TODO: Set project
            // resourceRepository.save(resource);
        }

        return bucketNames.size();
    }

    private void syncGcpResources(CloudCredentials credential) {
        Map<String, Object> config = credential.getProviderConfig();
        String projectId = (String) config.get("projectId");
        ConnectedGcpAccount account = new ConnectedGcpAccount(
                projectId,
                null, // region
                null, // zone
                null // serviceAccountJson - using default credentials
        );

        try {
            List<String> instanceIds = gcpComputeService.listInstanceIds(account);
            log.info("[ResourceSync] GCP sync complete: {} instances", instanceIds.size());
            // TODO: Map to Resource entities
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to sync GCP resources: " + e.getMessage(), e);
        }
    }

    private void syncAzureResources(CloudCredentials credential) {
        Map<String, Object> config = credential.getProviderConfig();
        ConnectedAzureAccount account = new ConnectedAzureAccount(
                (String) config.get("subscriptionId"),
                (String) config.get("tenantId"),
                (String) config.get("clientId"),
                (String) config.get("clientSecret"),
                null, // region
                null // friendlyName
        );

        List<String> vmNames = azureComputeService.listVmNames(account);
        log.info("[ResourceSync] Azure sync complete: {} VMs", vmNames.size());

        // TODO: Map to Resource entities
    }

    private void syncDigitalOceanResources(CloudCredentials credential) {
        Map<String, Object> config = credential.getProviderConfig();
        String apiToken = (String) config.get("apiToken");
        DigitalOceanClient client = DigitalOceanClient.builder()
                .token(apiToken)
                .build();

        List<Droplet> droplets = client.droplets().list();
        log.info("[ResourceSync] DigitalOcean sync complete: {} droplets", droplets.size());

        // TODO: Map to Resource entities
    }
}
