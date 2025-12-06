package app.cmesh.gcp.validation;

import app.cmesh.gcp.enums.CloudProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class GcpCredentialValidator implements CloudCredentialValidator {
    @Override
    public CloudProvider getProvider() {
        return CloudProvider.GCP;
    }
    @Override
    public ValidationResult validateFormat(Map<String, String> config) {
        String projectId = config.get("projectId");
        String serviceAccountJson = config.get("serviceAccountJson");
        if (projectId == null || projectId.isBlank()) {
            return ValidationResult.failure("Missing projectId", "projectId is required");
        }
        if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
            return ValidationResult.failure("Missing serviceAccountJson", "serviceAccountJson is required");
        }
        if (!projectId.matches("^[a-z][a-z0-9-]{4,28}[a-z0-9]$")) {
            return ValidationResult.failure("Invalid projectId format",
                "GCP project ID must be 6-30 characters, start with a letter");
        }
        if (!serviceAccountJson.trim().startsWith("{")) {
            return ValidationResult.failure("Invalid JSON format",
                "serviceAccountJson must be valid JSON starting with '{'");
        }
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(
                serviceAccountJson.getBytes(StandardCharsets.UTF_8)
            );
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(stream);
            if (credentials.getServiceAccountUser() == null) {
                return ValidationResult.failure("Invalid service account",
                    "JSON key does not contain a valid service account email");
            }
            stream.close();
        } catch (Exception e) {
            return ValidationResult.failure("Invalid service account JSON",
                "Failed to parse service account JSON: " + e.getMessage());
        }
        return ValidationResult.success("GCP credential format is valid");
    }
    @Override
    public ValidationResult validate(Map<String, String> config) {
        ValidationResult formatResult = validateFormat(config);
        if (!formatResult.valid()) {
            return formatResult;
        }
        String projectId = config.get("projectId");
        String serviceAccountJson = config.get("serviceAccountJson");
        log.info("Validating GCP credentials - projectId: {}", projectId);
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(
                serviceAccountJson.getBytes(StandardCharsets.UTF_8)
            );
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
            stream.close();
            Storage storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();
            try {
                storage.list(Storage.BucketListOption.pageSize(1));
                log.info("Successfully validated GCP credentials for project: {}", projectId);
                return ValidationResult.success(
                    "GCP credentials validated successfully",
                    "Successfully authenticated with Google Cloud Storage. Project: " + projectId
                );
            } catch (com.google.cloud.storage.StorageException e) {
                log.warn("GCP Storage API test failed: {}", e.getMessage());
                if (e.getCode() == 403) {
                    return ValidationResult.failure("Insufficient permissions",
                        "Service account authenticated but lacks Storage permissions");
                } else if (e.getCode() == 404) {
                    return ValidationResult.failure("Project not found",
                        "Project '" + projectId + "' does not exist or is not accessible");
                } else if (e.getCode() == 401) {
                    return ValidationResult.failure("Authentication failed",
                        "Service account credentials are invalid or expired");
                } else {
                    return ValidationResult.failure("API call failed",
                        "GCP Storage API call failed with code " + e.getCode());
                }
            }
        } catch (java.io.IOException e) {
            log.error("Failed to create GCP credentials from JSON", e);
            return ValidationResult.failure("Invalid credentials",
                "Failed to create credentials from service account JSON: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error validating GCP credentials", e);
            return ValidationResult.failure("Validation error",
                "Unexpected error: " + e.getMessage());
        }
    }
}
