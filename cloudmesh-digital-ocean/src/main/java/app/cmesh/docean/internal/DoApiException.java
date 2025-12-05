package app.cmesh.docean.internal;

import tools.jackson.databind.ObjectMapper;

public class DoApiException extends RuntimeException {

    private final int statusCode;
    private final String errorId;

    public DoApiException(int statusCode, String errorId, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorId = errorId;
    }

    public  int statusCode() {
        return statusCode;
    }
    public String errorId() {
        return errorId;
    }

    public static DoApiException from(int status, String body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            DoApiError error = mapper.readValue(body, DoApiError.class);
            return new DoApiException(status, error.id(), error.message());
        } catch (Exception e) {
            return new DoApiException(status, null, "HTTP " + status + ": " + body);
        }
    }
}
