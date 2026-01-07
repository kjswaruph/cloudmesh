package app.cmesh.cost;

import app.cmesh.aws.AwsCostService;
import app.cmesh.aws.ConnectedAwsAccount;
import app.cmesh.aws.StsService;
import app.cmesh.cost.repository.CostRepository;
import app.cmesh.dashboard.CloudCredentials;
import app.cmesh.dashboard.repository.CloudCredentialsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Spring service that schedules daily cost syncs from AWS Cost Explorer.
 * Runs at 2 AM daily to minimize API costs.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CostSyncService {

    private final CloudCredentialsRepository credentialsRepository;
    private final CostRepository costRepository;
    private final StsService stsService;
    private final app.cmesh.observability.MetricsService metricsService;

    /**
     * Scheduled cost sync - runs daily at 2 AM.
     * Fetches last 30 days of cost data.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void syncAllCosts() {
        log.info("[CostSync] Starting daily cost sync");
        long startTime = System.currentTimeMillis();

        List<CloudCredentials> awsCredentials = credentialsRepository
                .findByProviderAndStatus(
                        app.cmesh.dashboard.enums.CloudProvider.AWS,
                        CloudCredentials.CredentialStatus.ACTIVE);

        log.info("[CostSync] Found {} AWS credentials to sync", awsCredentials.size());

        int successCount = 0;
        int failureCount = 0;

        for (CloudCredentials credential : awsCredentials) {
            long credentialStartTime = System.currentTimeMillis();

            try {
                syncAwsCosts(credential);
                successCount++;
                metricsService.recordCostSyncSuccess();
            } catch (Exception e) {
                failureCount++;
                metricsService.recordCostSyncFailure();
                log.error("[CostSync] Failed to sync costs for credential {}: {}",
                        credential.getFriendlyName(), e.getMessage(), e);
            } finally {
                long credentialDuration = System.currentTimeMillis() - credentialStartTime;
                metricsService.recordCostSyncDuration(credentialDuration);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("[CostSync] Completed in {}ms - {} success, {} failed",
                duration, successCount, failureCount);
    }

    /**
     * Sync costs for a single AWS credential.
     */
    @Transactional
    protected void syncAwsCosts(CloudCredentials credential) {
        log.info("[CostSync] Syncing costs for: {}", credential.getFriendlyName());

        Map<String, Object> config = credential.getProviderConfig();
        ConnectedAwsAccount account = new ConnectedAwsAccount(
                null, // id
                (String) config.get("roleArn"),
                (String) config.get("externalId"),
                (String) config.getOrDefault("region", "us-east-1"));

        // Fetch last 30 days of costs
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        AwsCostService costService = new AwsCostService(stsService);
        List<AwsCostService.DailyCostEntry> costEntries = costService.getCostAndUsage(
                account,
                startDate,
                endDate);

        log.info("[CostSync] Fetched {} cost entries", costEntries.size());

        // Save costs to database
        int savedCount = 0;
        for (AwsCostService.DailyCostEntry entry : costEntries) {
            // Skip if already exists
            if (costRepository.existsByCredentialAndDate(credential, entry.date())) {
                continue;
            }

            Cost cost = new Cost();
            cost.setCredential(credential);
            cost.setDate(entry.date());
            cost.setAmount(entry.amount());
            cost.setCurrency(entry.currency());
            cost.setService(entry.service());
            cost.setTags(entry.tags());
            // Resource and Project mapping would be done here based on tags
            // For now, leaving them null

            costRepository.save(cost);
            savedCount++;
        }

        log.info("[CostSync] Saved {} new cost entries for {}",
                savedCount, credential.getFriendlyName());
    }
}
