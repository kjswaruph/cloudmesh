package app.cmesh.docean.validation;
import app.cmesh.docean.enums.CloudProvider;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Validator for DigitalOcean credentials using Personal Access Token.
 * Pure Java library - no Spring dependencies.
 */
@Slf4j
public class DigitalOceanCredentialValidator implements CloudCredentialValidator {

    private static final String DO_API_BASE = "https://api.digitalocean.com/v2";
    private static final String ACCOUNT_ENDPOINT = "/account";

    private final HttpClient httpClient;

    public DigitalOceanCredentialValidator() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    @Override
    public CloudProvider getProvider() {
        return CloudProvider.DIGITALOCEAN;
    }

    @Override
    public ValidationResult validateFormat(Map<String, String> config) {
        String apiToken = config.get("apiToken");

        if (apiToken == null || apiToken.isBlank()) {
            return ValidationResult.failure(
                "Missing apiToken",
                "apiToken is required"
            );
        }

        // DigitalOcean tokens are typically 64 characters (hexadecimal)
        if (apiToken.length() < 20) {
            return ValidationResult.failure(
                "Invalid token format",
                "API token appears too short. DigitalOcean tokens are typically 64 characters long."
            );
        }

        // Check if token contains only valid characters (alphanumeric)
        if (!apiToken.matches("^[a-zA-Z0-9_-]+$")) {
            return ValidationResult.failure(
                "Invalid token format",
                "API token contains invalid characters. Should only contain alphanumeric characters, hyphens, and underscores."
            );
        }

        return ValidationResult.success("DigitalOcean credential format is valid");
    }

    @Override
    public ValidationResult validate(Map<String, String> config) {
        // First validate format
        ValidationResult formatResult = validateFormat(config);
        if (!formatResult.valid()) {
            return formatResult;
        }

        String apiToken = config.get("apiToken");

        log.info("Validating DigitalOcean credentials");

        try {
            // Make request to /v2/account endpoint
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DO_API_BASE + ACCOUNT_ENDPOINT))
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            log.debug("DigitalOcean API response status: {}", response.statusCode());

            if (response.statusCode() == 200) {
                // Success - token is valid
                log.info("Successfully validated DigitalOcean credentials");

                // Try to extract account email from response for additional detail
                String responseBody = response.body();
                String accountInfo = "";
                if (responseBody.contains("\"email\"")) {
                    try {
                        // Simple extraction - in production you'd use a JSON library
                        int emailStart = responseBody.indexOf("\"email\":\"") + 9;
                        int emailEnd = responseBody.indexOf("\"", emailStart);
                        if (emailStart > 8 && emailEnd > emailStart) {
                            String email = responseBody.substring(emailStart, emailEnd);
                            accountInfo = " Account: " + email;
                        }
                    } catch (Exception e) {
                        // Ignore parsing errors
                    }
                }

                return ValidationResult.success(
                    "DigitalOcean credentials validated successfully",
                    "Successfully authenticated with DigitalOcean API." + accountInfo
                );

            } else if (response.statusCode() == 401) {
                // Unauthorized - invalid token
                log.warn("DigitalOcean authentication failed - invalid token");
                return ValidationResult.failure(
                    "Invalid API token",
                    "The provided API token is invalid or has been revoked. " +
                    "Please generate a new Personal Access Token in the DigitalOcean control panel."
                );

            } else if (response.statusCode() == 403) {
                // Forbidden - token lacks required scopes
                log.warn("DigitalOcean authentication failed - insufficient scopes");
                return ValidationResult.failure(
                    "Insufficient token scopes",
                    "The API token is valid but lacks required scopes. " +
                    "Please ensure the token has 'read' scope at minimum, or 'read+write' for full functionality."
                );

            } else if (response.statusCode() == 429) {
                // Rate limited
                log.warn("DigitalOcean rate limit exceeded");
                return ValidationResult.failure(
                    "Rate limit exceeded",
                    "DigitalOcean API rate limit exceeded. Please try again later."
                );

            } else {
                log.warn("DigitalOcean API returned unexpected status: {}", response.statusCode());
                return ValidationResult.failure(
                    "Unexpected API response",
                    "DigitalOcean API returned status " + response.statusCode() + ": " + response.body()
                );
            }

        } catch (java.net.http.HttpTimeoutException e) {
            log.error("Timeout connecting to DigitalOcean API", e);
            return ValidationResult.failure(
                "Connection timeout",
                "Timeout connecting to DigitalOcean API. Please check network connectivity and try again."
            );

        } catch (java.net.ConnectException | java.net.UnknownHostException e) {
            log.error("Cannot connect to DigitalOcean API", e);
            return ValidationResult.failure(
                "Connection failed",
                "Cannot connect to DigitalOcean API. Please check network connectivity."
            );

        } catch (Exception e) {
            log.error("Unexpected error validating DigitalOcean credentials", e);
            return ValidationResult.failure(
                "Validation error",
                "Unexpected error: " + e.getMessage()
            );
        }
    }
}


