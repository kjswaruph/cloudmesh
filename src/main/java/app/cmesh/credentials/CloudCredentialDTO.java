package app.cmesh.credentials;
import app.cmesh.dashboard.CloudCredentials.CredentialStatus;
import app.cmesh.dashboard.enums.CloudProvider;
import java.time.Instant;
import java.util.UUID;

public record CloudCredentialDTO(
        UUID credentialId,
        CloudProvider provider,
        String friendlyName,
        CredentialStatus status,
        String region,
        Instant lastValidatedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
