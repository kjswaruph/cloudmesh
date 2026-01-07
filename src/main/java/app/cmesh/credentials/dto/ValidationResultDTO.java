package app.cmesh.credentials.dto;


public record ValidationResultDTO(
        boolean valid,
        String message
) {
}

