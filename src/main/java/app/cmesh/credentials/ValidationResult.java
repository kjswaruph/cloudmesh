package app.cmesh.credentials;
public record ValidationResult(
        boolean valid,
        String message,
        String details
) {
    public static ValidationResult success(String message) {
        return new ValidationResult(true, message, null);
    }
    public static ValidationResult success(String message, String details) {
        return new ValidationResult(true, message, details);
    }
    public static ValidationResult failure(String message) {
        return new ValidationResult(false, message, null);
    }
    public static ValidationResult failure(String message, String details) {
        return new ValidationResult(false, message, details);
    }
}
