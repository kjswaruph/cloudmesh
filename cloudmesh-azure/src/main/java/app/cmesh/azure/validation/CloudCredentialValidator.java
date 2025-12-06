package app.cmesh.azure.validation;
import app.cmesh.azure.enums.CloudProvider;
import java.util.Map;
public interface CloudCredentialValidator {
    CloudProvider getProvider();
    ValidationResult validate(Map<String, String> config);
    ValidationResult validateFormat(Map<String, String> config);
}
