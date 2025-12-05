package app.cmesh.credentials.graphql;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AwsCredentialInput(
        @NotBlank(message = "Friendly name is required")
        String friendlyName,

        @NotBlank(message = "Role ARN is required")
        @Pattern(regexp = "arn:aws:iam::\\d{12}:role/[a-zA-Z0-9+=,.@\\-_/]+",
                message = "Invalid AWS Role ARN format. Expected: arn:aws:iam::ACCOUNT_ID:role/ROLE_NAME")
        String roleArn,

        @NotBlank(message = "External ID is required")
        String externalId,

        String region
) {
}

