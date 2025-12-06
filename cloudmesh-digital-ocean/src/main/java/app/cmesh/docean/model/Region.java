package app.cmesh.docean.model;

import java.util.List;

public record Region(
        String slug,
        String name,
        List<String> sizes,
        boolean available,
        List<String> features
) {
}
