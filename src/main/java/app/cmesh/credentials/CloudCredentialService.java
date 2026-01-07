package app.cmesh.credentials;

import app.cmesh.credentials.CredentialEncryptionService;
import app.cmesh.credentials.CredentialValidationService;
import app.cmesh.dashboard.CloudCredentials;
import app.cmesh.dashboard.CloudCredentials.CredentialStatus;
import app.cmesh.dashboard.enums.CloudProvider;
import app.cmesh.dashboard.repository.CloudCredentialsRepository;
import app.cmesh.user.User;
import app.cmesh.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudCredentialService {

    private final CloudCredentialsRepository credentialsRepository;
    private final UserRepository userRepository;
    private final CredentialEncryptionService encryptionService;
    private final CredentialValidationService validationService;

    @Transactional
    public CloudCredentialDTO createCredential(
            UUID userId,
            CloudProvider provider,
            String friendlyName,
            Map<String, String> config,
            boolean validateImmediately) {

        log.info("Creating new {} credential for user {}", provider, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        ValidationResult formatResult = validationService.validateFormat(provider, config);
        if (!formatResult.valid()) {
            throw new IllegalArgumentException("Invalid credential format: " + formatResult.message());
        }

        Map<String, Object> encryptedConfig = encryptSensitiveFields(provider, config);

        CloudCredentials credential = new CloudCredentials();
        credential.setUser(user);
        credential.setProvider(provider);
        credential.setFriendlyName(friendlyName);
        credential.setStatus(CredentialStatus.PENDING);
        credential.setProviderConfig(encryptedConfig);
        credential.setLastValidatedAt(null);

        CloudCredentials saved = credentialsRepository.save(credential);

        log.info("Created credential {} for user {}", saved.getCredentialId(), userId);

        if (validateImmediately) {
            ValidationResult validationResult = validateCredentialInternal(saved.getCredentialId(), userId);
            log.info("Immediate validation result for credential {}: {}",
                    saved.getCredentialId(), validationResult.valid() ? "SUCCESS" : "FAILED");
        }

        saved = credentialsRepository.findById(saved.getCredentialId()).orElse(saved);

        return toDTO(saved);
    }

    @Transactional
    public CloudCredentialDTO createCredential(
            UUID userId,
            CloudProvider provider,
            String friendlyName,
            Map<String, String> config) {
        return createCredential(userId, provider, friendlyName, config, true);
    }

    @Transactional(readOnly = true)
    public List<CloudCredentialDTO> listCredentials(UUID userId) {
        return credentialsRepository.findByUser_UserId(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CloudCredentialDTO> listCredentialsByProvider(UUID userId, CloudProvider provider) {
        return credentialsRepository.findByUser_UserIdAndProvider(userId, provider)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<CloudCredentialDTO> getCredential(UUID credentialId, UUID userId) {
        return credentialsRepository.findByCredentialIdAndUser_UserId(credentialId, userId)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Map<String, String> getDecryptedConfig(UUID credentialId, UUID userId) {
        CloudCredentials credential = credentialsRepository.findByCredentialIdAndUser_UserId(credentialId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Credential not found or access denied: " + credentialId));

        return decryptSensitiveFields(credential.getProvider(), credential.getProviderConfig());
    }

    @Transactional
    public void updateStatus(UUID credentialId, UUID userId, CredentialStatus status) {
        CloudCredentials credential = credentialsRepository.findByCredentialIdAndUser_UserId(credentialId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Credential not found or access denied: " + credentialId));

        credential.setStatus(status);
        credentialsRepository.save(credential);

        log.info("Updated credential {} status to {}", credentialId, status);
    }

    @Transactional
    public void markAsValidated(UUID credentialId, UUID userId) {
        CloudCredentials credential = credentialsRepository.findByCredentialIdAndUser_UserId(credentialId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Credential not found or access denied: " + credentialId));

        credential.setStatus(CredentialStatus.ACTIVE);
        credential.setLastValidatedAt(Instant.now());
        credentialsRepository.save(credential);

        log.info("Marked credential {} as validated", credentialId);
    }

    @Transactional
    public void markAsInvalid(UUID credentialId) {
        Optional<CloudCredentials> optCredential = credentialsRepository.findById(credentialId);
        if (optCredential.isPresent()) {
            CloudCredentials credential = optCredential.get();
            credential.setStatus(CredentialStatus.INVALID);
            credentialsRepository.save(credential);
            log.warn("Marked credential {} as invalid", credentialId);
        }
    }

    @Transactional
    public ValidationResult validateCredential(UUID credentialId, UUID userId) {
        return validateCredentialInternal(credentialId, userId);
    }

    private ValidationResult validateCredentialInternal(UUID credentialId, UUID userId) {
        CloudCredentials credential = credentialsRepository.findByCredentialIdAndUser_UserId(credentialId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Credential not found or access denied: " + credentialId));

        log.info("Validating credential {} for provider {}", credentialId, credential.getProvider());

        // Get decrypted config
        Map<String, String> decryptedConfig = decryptSensitiveFields(
                credential.getProvider(),
                credential.getProviderConfig());

        // Validate against cloud provider
        ValidationResult result = validationService.validate(credential.getProvider(), decryptedConfig);

        // Update credential status based on result
        if (result.valid()) {
            credential.setStatus(CredentialStatus.ACTIVE);
            credential.setLastValidatedAt(Instant.now());
            log.info("Credential {} validated successfully", credentialId);
        } else {
            credential.setStatus(CredentialStatus.INVALID);
            log.warn("Credential {} validation failed: {}", credentialId, result.message());
        }

        credentialsRepository.save(credential);

        return result;
    }

    @Transactional
    public void deleteCredential(UUID credentialId, UUID userId) {
        CloudCredentials credential = credentialsRepository.findByCredentialIdAndUser_UserId(credentialId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Credential not found or access denied: " + credentialId));

        // Secure deletion: overwrite sensitive data
        Map<String, Object> wiped = new HashMap<>();
        wiped.put("wiped", true);
        credential.setProviderConfig(wiped);
        credential.setStatus(CredentialStatus.INVALID);
        credentialsRepository.save(credential);

        // Now delete
        credentialsRepository.delete(credential);

        log.info("Deleted credential {} for user {}", credentialId, userId);
    }

    public boolean isOwner(UUID credentialId, UUID userId) {
        return credentialsRepository.existsByCredentialIdAndUser_UserId(credentialId, userId);
    }

    private Map<String, Object> encryptSensitiveFields(CloudProvider provider, Map<String, String> config) {
        Map<String, Object> encrypted = new HashMap<>(config);

        switch (provider) {
            case AWS:
                // AWS: External ID doesn't need encryption (it's not secret), but we encrypt it
                // anyway
                // RoleArn is public information
                if (config.containsKey("externalId")) {
                    encrypted.put("externalId", encryptionService.encrypt(config.get("externalId")));
                }
                break;

            case GCP:
                // GCP: Encrypt entire service account JSON
                if (config.containsKey("serviceAccountJson")) {
                    encrypted.put("serviceAccountJson",
                            encryptionService.encrypt(config.get("serviceAccountJson")));
                }
                break;

            case AZURE:
                // Azure: Encrypt client secret
                if (config.containsKey("clientSecret")) {
                    encrypted.put("clientSecret",
                            encryptionService.encrypt(config.get("clientSecret")));
                }
                break;

            case DIGITALOCEAN:
                // DigitalOcean: Encrypt API token
                if (config.containsKey("apiToken")) {
                    encrypted.put("apiToken",
                            encryptionService.encrypt(config.get("apiToken")));
                }
                break;
        }

        return encrypted;
    }

    private Map<String, String> decryptSensitiveFields(CloudProvider provider, Map<String, Object> config) {
        Map<String, String> decrypted = new HashMap<>();

        // Copy all non-encrypted fields
        config.forEach((key, value) -> {
            if (value instanceof String) {
                decrypted.put(key, (String) value);
            }
        });

        switch (provider) {
            case AWS:
                if (config.containsKey("externalId")) {
                    decrypted.put("externalId",
                            encryptionService.decrypt((String) config.get("externalId")));
                }
                break;

            case GCP:
                if (config.containsKey("serviceAccountJson")) {
                    decrypted.put("serviceAccountJson",
                            encryptionService.decrypt((String) config.get("serviceAccountJson")));
                }
                break;

            case AZURE:
                if (config.containsKey("clientSecret")) {
                    decrypted.put("clientSecret",
                            encryptionService.decrypt((String) config.get("clientSecret")));
                }
                break;

            case DIGITALOCEAN:
                if (config.containsKey("apiToken")) {
                    decrypted.put("apiToken",
                            encryptionService.decrypt((String) config.get("apiToken")));
                }
                break;
        }

        return decrypted;
    }

    private CloudCredentialDTO toDTO(CloudCredentials credential) {
        // Extract region from config (not sensitive)
        String region = null;
        if (credential.getProviderConfig() != null) {
            Object regionObj = credential.getProviderConfig().get("region");
            if (regionObj instanceof String) {
                region = (String) regionObj;
            }
        }

        return new CloudCredentialDTO(
                credential.getCredentialId(),
                credential.getProvider(),
                credential.getFriendlyName(),
                credential.getStatus(),
                region,
                credential.getLastValidatedAt(),
                credential.getCreatedAt(),
                credential.getUpdatedAt());
    }
}
