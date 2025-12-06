package app.cmesh.credentials.adapter;

import app.cmesh.aws.ConnectedAwsAccount;
import app.cmesh.azure.ConnectedAzureAccount;
import app.cmesh.credentials.CloudCredentialService;
import app.cmesh.dashboard.enums.CloudProvider;
import app.cmesh.docean.DigitalOceanClient;
import app.cmesh.gcp.ConnectedGcpAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class CloudCredentialAdapter {

    private final CloudCredentialService credentialService;

    
    public ConnectedAwsAccount toAwsAccount(UUID credentialId, UUID userId) {
        Map<String, String> config = credentialService.getDecryptedConfig(credentialId, userId);

        String roleArn = config.get("roleArn");
        String externalId = config.get("externalId");
        String region = config.getOrDefault("region", "us-east-1");

        if (roleArn == null || externalId == null) {
            throw new IllegalArgumentException("Invalid AWS credential configuration: missing roleArn or externalId");
        }

        log.debug("Converted credential {} to ConnectedAwsAccount for region {}", credentialId, region);

        return new ConnectedAwsAccount(
                credentialId.toString(),
                roleArn,
                externalId,
                region
        );
    }

    
    public ConnectedGcpAccount toGcpAccount(UUID credentialId, UUID userId) {
        Map<String, String> config = credentialService.getDecryptedConfig(credentialId, userId);

        String projectId = config.get("projectId");
        String serviceAccountJson = config.get("serviceAccountJson");
        String region = config.getOrDefault("region", "us-central1");
        String zone = config.getOrDefault("zone", region + "-a");

        if (projectId == null || serviceAccountJson == null) {
            throw new IllegalArgumentException("Invalid GCP credential configuration: missing projectId or serviceAccountJson");
        }

        log.debug("Converted credential {} to ConnectedGcpAccount for project {}", credentialId, projectId);

        return new ConnectedGcpAccount(
                projectId,
                region,
                zone,
                serviceAccountJson
        );
    }

    
    public ConnectedAzureAccount toAzureAccount(UUID credentialId, UUID userId) {
        Map<String, String> config = credentialService.getDecryptedConfig(credentialId, userId);

        String subscriptionId = config.get("subscriptionId");
        String tenantId = config.get("tenantId");
        String clientId = config.get("clientId");
        String clientSecret = config.get("clientSecret");
        String region = config.getOrDefault("region", "eastus");
        String friendlyName = config.getOrDefault("friendlyName", "Azure Account");

        if (subscriptionId == null || tenantId == null || clientId == null || clientSecret == null) {
            throw new IllegalArgumentException("Invalid Azure credential configuration: missing required fields");
        }

        log.debug("Converted credential {} to ConnectedAzureAccount for subscription {}", credentialId, subscriptionId);

        return new ConnectedAzureAccount(
                subscriptionId,
                tenantId,
                clientId,
                clientSecret,
                region,
                friendlyName
        );
    }

    
    public DigitalOceanClient toDigitalOceanClient(UUID credentialId, UUID userId) {
        Map<String, String> config = credentialService.getDecryptedConfig(credentialId, userId);

        String apiToken = config.get("apiToken");

        if (apiToken == null) {
            throw new IllegalArgumentException("Invalid DigitalOcean credential configuration: missing apiToken");
        }

        log.debug("Created DigitalOceanClient for credential {}", credentialId);

        return DigitalOceanClient.builder()
                .token(apiToken)
                .build();
    }

    public void validateCredentialProvider(UUID credentialId, UUID userId, CloudProvider expectedProvider) {
        var credential = credentialService.getCredential(credentialId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Credential not found: " + credentialId));

        if (credential.provider() != expectedProvider) {
            throw new IllegalArgumentException(
                    String.format("Credential %s is for %s, expected %s",
                            credentialId, credential.provider(), expectedProvider));
        }
    }
}

