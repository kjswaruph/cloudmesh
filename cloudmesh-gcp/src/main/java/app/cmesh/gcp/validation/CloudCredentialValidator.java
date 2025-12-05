package app.cmesh.gcp.validation;
import app.cmesh.gcp.enums.CloudProvider;
import java.util.Map;
public interface CloudCredentialValidator {
    CloudProvider getProvider();
    ValidationResult validate(Map<String, String> config);
    ValidationResult validateFormat(Map<String, String> config);
}
