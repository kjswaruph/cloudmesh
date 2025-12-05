package app.cmesh.docean.model;

import java.util.List;
import java.util.Map;

public record Droplet(
        long id,
        String name,
        String status,
        String region,
        String sizeSlug,
        List<String> tags,
        Map<String, Object> raw // optional for extra fields
) {
}
