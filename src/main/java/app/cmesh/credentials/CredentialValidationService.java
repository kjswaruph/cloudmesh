package app.cmesh.credentials;

import app.cmesh.credentials.ValidationResult;
import app.cmesh.dashboard.enums.CloudProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class CredentialValidationService {

    private final EnumMap<CloudProvider, CloudCredentialValidator> validators;

    public CredentialValidationService(List<CloudCredentialValidator> validatorList) {
        this.validators = new EnumMap<>(CloudProvider.class);

        // Register all validators by their provider
        for (CloudCredentialValidator validator : validatorList) {
            validators.put(validator.getProvider(), validator);
            log.info("Registered credential validator for provider: {}", validator.getProvider());
        }

        log.info("Credential validation service initialized with {} validators", validators.size());
    }

    
    public ValidationResult validate(CloudProvider provider, Map<String, String> config) {
        CloudCredentialValidator validator = validators.get(provider);

        if (validator == null) {
            log.error("No validator found for provider: {}", provider);
            return ValidationResult.failure(
                "Validator not found",
                "No validator configured for provider: " + provider
            );
        }

        try {
            log.info("Validating credentials for provider: {}", provider);
            return validator.validate(config);
        } catch (Exception e) {
            log.error("Validation failed for provider: " + provider, e);
            return ValidationResult.failure(
                "Validation error",
                "Unexpected validation error: " + e.getMessage()
            );
        }
    }

    
    public ValidationResult validateFormat(CloudProvider provider, Map<String, String> config) {
        CloudCredentialValidator validator = validators.get(provider);

        if (validator == null) {
            return ValidationResult.failure(
                "Validator not found",
                "No validator configured for provider: " + provider
            );
        }

        try {
            return validator.validateFormat(config);
        } catch (Exception e) {
            log.error("Format validation failed for provider: " + provider, e);
            return ValidationResult.failure(
                "Format validation error",
                "Unexpected error: " + e.getMessage()
            );
        }
    }

    
    public boolean hasValidator(CloudProvider provider) {
        return validators.containsKey(provider);
    }
}

