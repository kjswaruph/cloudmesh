package app.cmesh.credentials;

import app.cmesh.credentials.ValidationResult;
import app.cmesh.dashboard.enums.CloudProvider;
import java.util.Map;

public interface CloudCredentialValidator {
    CloudProvider getProvider();

    ValidationResult validate(Map<String, String> config);

    ValidationResult validateFormat(Map<String, String> config);
}
