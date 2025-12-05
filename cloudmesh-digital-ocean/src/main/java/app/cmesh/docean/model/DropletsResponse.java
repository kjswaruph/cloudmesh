package app.cmesh.docean.model;

import java.util.List;

public record DropletsResponse(
        Droplet droplet,
        List<Droplet> droplets
) {
}
