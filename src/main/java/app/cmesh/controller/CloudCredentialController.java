package app.cmesh.controller;

import app.cmesh.credentials.CloudCredentialDTO;
import app.cmesh.credentials.CloudCredentialService;
import app.cmesh.credentials.ValidationResult;
import app.cmesh.credentials.graphql.AwsCredentialInput;
import app.cmesh.credentials.graphql.AzureCredentialInput;
import app.cmesh.credentials.graphql.DigitalOceanCredentialInput;
import app.cmesh.credentials.graphql.GcpCredentialInput;
import app.cmesh.credentials.graphql.ValidationResultDTO;
import app.cmesh.dashboard.enums.CloudProvider;
import app.cmesh.user.User;
import app.cmesh.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CloudCredentialController {

    private final CloudCredentialService credentialService;
    private final UserRepository userRepository;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<CloudCredentialDTO> cloudCredentials(
            @Argument CloudProvider provider,
            Authentication authentication) {

        UUID userId = getUserId(authentication);
        log.info("Query: cloudCredentials requested for user {} with provider filter: {}",
                userId, provider);

        if (provider != null) {
            return credentialService.listCredentialsByProvider(userId, provider);
        }
        return credentialService.listCredentials(userId);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public CloudCredentialDTO cloudCredential(
            @Argument String id,
            Authentication authentication) {

        UUID userId = getUserId(authentication);
        UUID credentialId = parseUUID(id, "credential ID");

        log.info("Query: cloudCredential requested for credential {} by user {}",
                credentialId, userId);

        Optional<CloudCredentialDTO> credential = credentialService.getCredential(credentialId, userId);

        if (credential.isEmpty()) {
            log.warn("Credential {} not found or access denied for user {}", credentialId, userId);
            throw new IllegalArgumentException("Credential not found or access denied");
        }

        return credential.get();
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CloudCredentialDTO connectAwsAccount(
            @Argument @Valid AwsCredentialInput input,
            Authentication authentication) {

        UUID userId = getUserId(authentication);
        log.info("Mutation: connectAwsAccount requested by user {} for role: {}",
                userId, input.roleArn());

        Map<String, String> config = new HashMap<>();
        config.put("roleArn", input.roleArn());
        config.put("externalId", input.externalId());
        if (input.region() != null) {
            config.put("region", input.region());
        }

        try {
            CloudCredentialDTO credential = credentialService.createCredential(
                    userId,
                    CloudProvider.AWS,
                    input.friendlyName(),
                    config,
                    true // validate immediately
            );

            log.info("AWS credential {} created successfully for user {}",
                    credential.credentialId(), userId);
            return credential;

        } catch (IllegalArgumentException e) {
            log.error("Failed to connect AWS account for user {}: {}", userId, e.getMessage());
            throw new IllegalArgumentException("Failed to connect AWS account: " + e.getMessage());
        }
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CloudCredentialDTO connectGcpAccount(
            @Argument @Valid GcpCredentialInput input,
            Authentication authentication) {

        UUID userId = getUserId(authentication);
        log.info("Mutation: connectGcpAccount requested by user {} for project: {}",
                userId, input.projectId());

        Map<String, String> config = new HashMap<>();
        config.put("serviceAccountJson", input.serviceAccountJson());
        config.put("projectId", input.projectId());
        if (input.region() != null) {
            config.put("region", input.region());
        }

        try {
            CloudCredentialDTO credential = credentialService.createCredential(
                    userId,
                    CloudProvider.GCP,
                    input.friendlyName(),
                    config,
                    true // validate immediately
            );

            log.info("GCP credential {} created successfully for user {}",
                    credential.credentialId(), userId);
            return credential;

        } catch (IllegalArgumentException e) {
            log.error("Failed to connect GCP account for user {}: {}", userId, e.getMessage());
            throw new IllegalArgumentException("Failed to connect GCP account: " + e.getMessage());
        }
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CloudCredentialDTO connectAzureAccount(
            @Argument @Valid AzureCredentialInput input,
            Authentication authentication) {

        UUID userId = getUserId(authentication);
        log.info("Mutation: connectAzureAccount requested by user {} for subscription: {}",
                userId, input.subscriptionId());

        Map<String, String> config = new HashMap<>();
        config.put("clientId", input.clientId());
        config.put("clientSecret", input.clientSecret());
        config.put("tenantId", input.tenantId());
        config.put("subscriptionId", input.subscriptionId());
        if (input.region() != null) {
            config.put("region", input.region());
        }

        try {
            CloudCredentialDTO credential = credentialService.createCredential(
                    userId,
                    CloudProvider.AZURE,
                    input.friendlyName(),
                    config,
                    true // validate immediately
            );

            log.info("Azure credential {} created successfully for user {}",
                    credential.credentialId(), userId);
            return credential;

        } catch (IllegalArgumentException e) {
            log.error("Failed to connect Azure account for user {}: {}", userId, e.getMessage());
            throw new IllegalArgumentException("Failed to connect Azure account: " + e.getMessage());
        }
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CloudCredentialDTO connectDigitalOceanAccount(
            @Argument @Valid DigitalOceanCredentialInput input,
            Authentication authentication) {

        UUID userId = getUserId(authentication);
        log.info("Mutation: connectDigitalOceanAccount requested by user {}", userId);

        Map<String, String> config = new HashMap<>();
        config.put("apiToken", input.apiToken());
        if (input.region() != null) {
            config.put("region", input.region());
        }

        try {
            CloudCredentialDTO credential = credentialService.createCredential(
                    userId,
                    CloudProvider.DIGITALOCEAN,
                    input.friendlyName(),
                    config,
                    true // validate immediately
            );

            log.info("DigitalOcean credential {} created successfully for user {}",
                    credential.credentialId(), userId);
            return credential;

        } catch (IllegalArgumentException e) {
            log.error("Failed to connect DigitalOcean account for user {}: {}", userId, e.getMessage());
            throw new IllegalArgumentException("Failed to connect DigitalOcean account: " + e.getMessage());
        }
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public ValidationResultDTO validateCredential(
            @Argument String id,
            Authentication authentication) {

        UUID userId = getUserId(authentication);
        UUID credentialId = parseUUID(id, "credential ID");

        log.info("Mutation: validateCredential requested for credential {} by user {}",
                credentialId, userId);

        try {
            ValidationResult result = credentialService.validateCredential(credentialId, userId);

            log.info("Credential {} validation result: {} - {}",
                    credentialId, result.valid() ? "SUCCESS" : "FAILED", result.message());

            return new ValidationResultDTO(result.valid(), result.message());

        } catch (IllegalArgumentException e) {
            log.error("Failed to validate credential {} for user {}: {}",
                    credentialId, userId, e.getMessage());
            throw new IllegalArgumentException("Failed to validate credential: " + e.getMessage());
        }
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public boolean deleteCredential(
            @Argument String id,
            Authentication authentication) {

        UUID userId = getUserId(authentication);
        UUID credentialId = parseUUID(id, "credential ID");

        log.info("Mutation: deleteCredential requested for credential {} by user {}",
                credentialId, userId);

        try {
            credentialService.deleteCredential(credentialId, userId);
            log.info("Credential {} deleted successfully by user {}", credentialId, userId);
            return true;

        } catch (IllegalArgumentException e) {
            log.error("Failed to delete credential {} for user {}: {}",
                    credentialId, userId, e.getMessage());
            return false;
        }
    }

    private UUID getUserId(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Authentication required");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            User user = userRepository.findUsersByUsername(username);
            if (user == null) {
                throw new IllegalStateException("User not found: " + username);
            }
            return user.getUserId();

        } else if (principal instanceof OAuth2User oauthUser) {
            String email = oauthUser.getAttribute("email");
            Optional<User> userOptional = userRepository.findUsersByEmail(email);
            if (userOptional.isEmpty()) {
                throw new IllegalStateException("User not found: " + email);
            }
            return userOptional.get().getUserId();
        }

        throw new IllegalStateException("Unknown principal type: " + principal.getClass().getName());
    }

    private UUID parseUUID(String id, String fieldName) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + id);
        }
    }
}

