package app.cmesh.credentials.graphql;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AzureCredentialInput(
        @NotBlank(message = "Friendly name is required")
        String friendlyName,

        @NotBlank(message = "Client ID is required")
        @Pattern(regexp = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$",
                message = "Invalid Client ID format. Expected UUID format.")
        String clientId,

        @NotBlank(message = "Client Secret is required")
        String clientSecret,

        @NotBlank(message = "Tenant ID is required")
        @Pattern(regexp = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$",
                message = "Invalid Tenant ID format. Expected UUID format.")
        String tenantId,

        @NotBlank(message = "Subscription ID is required")
        @Pattern(regexp = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$",
                message = "Invalid Subscription ID format. Expected UUID format.")
        String subscriptionId,

        String region
) {
}

