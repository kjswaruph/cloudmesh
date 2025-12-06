package app.cmesh.credentials.graphql;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record DigitalOceanCredentialInput(
        @NotBlank(message = "Friendly name is required")
        String friendlyName,

        @NotBlank(message = "API token is required")
        @Size(min = 64, max = 256, message = "Invalid API token format")
        String apiToken,

        String region
) {
}

