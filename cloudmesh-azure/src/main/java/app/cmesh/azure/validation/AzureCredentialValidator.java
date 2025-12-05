package app.cmesh.azure.validation;
import app.cmesh.azure.enums.CloudProvider;
import com.azure.core.exception.HttpResponseException;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.UUID;


@Slf4j
public class AzureCredentialValidator implements CloudCredentialValidator {
    @Override
    public CloudProvider getProvider() {
        return CloudProvider.AZURE;
    }
    @Override
    public ValidationResult validateFormat(Map<String, String> config) {
        String tenantId = config.get("tenantId");
        String subscriptionId = config.get("subscriptionId");
        String clientId = config.get("clientId");
        String clientSecret = config.get("clientSecret");
        if (tenantId == null || tenantId.isBlank()) {
            return ValidationResult.failure("Missing tenantId", "tenantId is required");
        }
        if (subscriptionId == null || subscriptionId.isBlank()) {
            return ValidationResult.failure("Missing subscriptionId", "subscriptionId is required");
        }
        if (clientId == null || clientId.isBlank()) {
            return ValidationResult.failure("Missing clientId", "clientId is required");
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            return ValidationResult.failure("Missing clientSecret", "clientSecret is required");
        }
        try {
            UUID.fromString(tenantId);
        } catch (IllegalArgumentException e) {
            return ValidationResult.failure("Invalid tenantId format", "tenantId must be a valid UUID");
        }
        try {
            UUID.fromString(subscriptionId);
        } catch (IllegalArgumentException e) {
            return ValidationResult.failure("Invalid subscriptionId format", "subscriptionId must be a valid UUID");
        }
        try {
            UUID.fromString(clientId);
        } catch (IllegalArgumentException e) {
            return ValidationResult.failure("Invalid clientId format", "clientId must be a valid UUID");
        }
        return ValidationResult.success("Azure credential format is valid");
    }
    @Override
    public ValidationResult validate(Map<String, String> config) {
        ValidationResult formatResult = validateFormat(config);
        if (!formatResult.valid()) {
            return formatResult;
        }
        String tenantId = config.get("tenantId");
        String subscriptionId = config.get("subscriptionId");
        String clientId = config.get("clientId");
        String clientSecret = config.get("clientSecret");
        log.info("Validating Azure credentials - tenantId: {}, subscriptionId: {}, clientId: {}", 
            tenantId, subscriptionId, clientId);
        try {
            ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
            AzureResourceManager azureResourceManager;
            try {
                com.azure.core.management.profile.AzureProfile profile = 
                    new com.azure.core.management.profile.AzureProfile(
                        tenantId, 
                        subscriptionId, 
                        com.azure.core.management.AzureEnvironment.AZURE
                    );
                azureResourceManager = AzureResourceManager
                    .authenticate(credential, profile)
                    .withSubscription(subscriptionId);
            } catch (Exception e) {
                log.warn("Failed to authenticate with Azure: {}", e.getMessage());
                if (e.getMessage().contains("AADSTS")) {
                    if (e.getMessage().contains("AADSTS7000215") || e.getMessage().contains("invalid client secret")) {
                        return ValidationResult.failure("Invalid client secret",
                            "The provided client secret is invalid or expired");
                    } else if (e.getMessage().contains("AADSTS700016") || e.getMessage().contains("not found")) {
                        return ValidationResult.failure("Application not found",
                            "The application (client ID) was not found in tenant '" + tenantId + "'");
                    } else if (e.getMessage().contains("AADSTS50034")) {
                        return ValidationResult.failure("Tenant not found",
                            "Tenant '" + tenantId + "' does not exist");
                    } else {
                        return ValidationResult.failure("Authentication failed",
                            "Azure AD authentication error: " + e.getMessage());
                    }
                } else {
                    return ValidationResult.failure("Authentication failed",
                        "Failed to authenticate with Azure: " + e.getMessage());
                }
            }
            try {
                ResourceGroups resourceGroups = azureResourceManager.resourceGroups();
                resourceGroups.list().stream().limit(1).forEach(rg -> {
                    log.debug("Found resource group: {}", rg.name());
                });
                log.info("Successfully validated Azure credentials for subscription: {}", subscriptionId);
                return ValidationResult.success(
                    "Azure credentials validated successfully",
                    "Successfully authenticated and verified subscription access. Subscription: " + subscriptionId
                );
            } catch (HttpResponseException e) {
                log.warn("Azure Resource Manager API test failed: {}", e.getMessage());
                int statusCode = e.getResponse() != null ? e.getResponse().getStatusCode() : 0;
                if (statusCode == 403 || e.getMessage().contains("does not have authorization")) {
                    return ValidationResult.failure("Insufficient permissions",
                        "Service principal authenticated but lacks permissions to list resource groups");
                } else if (statusCode == 404 || (e.getMessage().contains("subscription") && e.getMessage().contains("not found"))) {
                    return ValidationResult.failure("Subscription not found",
                        "Subscription '" + subscriptionId + "' does not exist or is not accessible");
                } else {
                    return ValidationResult.failure("API call failed",
                        "Azure Resource Manager API failed: " + e.getMessage());
                }
            } catch (Exception e) {
                log.warn("Unexpected error during Azure API test", e);
                return ValidationResult.failure("API test failed",
                    "Unexpected error testing Azure API: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Unexpected error validating Azure credentials", e);
            return ValidationResult.failure("Validation error",
                "Unexpected error: " + e.getMessage());
        }
    }
}
