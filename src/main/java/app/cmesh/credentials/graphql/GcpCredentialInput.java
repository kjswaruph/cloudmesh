package app.cmesh.credentials.graphql;

import jakarta.validation.constraints.NotBlank;


public record GcpCredentialInput(
        @NotBlank(message = "Friendly name is required")
        String friendlyName,

        @NotBlank(message = "Service account JSON is required")
        String serviceAccountJson,

        @NotBlank(message = "Project ID is required")
        String projectId,

        String region
) {
}

