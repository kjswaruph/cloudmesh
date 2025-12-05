package app.cmesh.docean.model;

public record Image(
        long id,
        String name,
        String type,
        String distribution,
        String slug,
        boolean available,
        String[] regions,
        String status
) {
}
