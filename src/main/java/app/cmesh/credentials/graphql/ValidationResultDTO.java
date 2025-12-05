package app.cmesh.credentials.graphql;


public record ValidationResultDTO(
        boolean valid,
        String message
) {
}

