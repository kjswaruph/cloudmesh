package app.cmesh.aws.validation;
import app.cmesh.aws.enums.CloudProvider;
import java.util.Map;
public interface CloudCredentialValidator {
    CloudProvider getProvider();
    ValidationResult validate(Map<String, String> config);
    ValidationResult validateFormat(Map<String, String> config);
}
