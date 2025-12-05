package app.cmesh.docean.validation;
import app.cmesh.docean.enums.CloudProvider;
import java.util.Map;
public interface CloudCredentialValidator {
    CloudProvider getProvider();
    ValidationResult validate(Map<String, String> config);
    ValidationResult validateFormat(Map<String, String> config);
}
