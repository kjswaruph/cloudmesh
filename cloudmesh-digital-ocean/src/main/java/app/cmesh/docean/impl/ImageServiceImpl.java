package app.cmesh.docean.impl;

import app.cmesh.docean.api.ImageService;
import app.cmesh.docean.http.HttpExecutor;
import app.cmesh.docean.model.Image;
import app.cmesh.docean.model.ImagesResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ImageServiceImpl implements ImageService {

    private final HttpExecutor http;

    public ImageServiceImpl(HttpExecutor http) {
        this.http = http;
    }

    @Override
    public List<Image> list(String type) {
        return http.get("/images", Map.of("type", type), ImagesResponse.class).images();
    }

    @Override
    public List<Image> listAll() {
        return http.get("/images", Map.of("per_page", "200"), ImagesResponse.class).images();
    }

    @Override
    public Optional<Image> get(long id) {
        return get(String.valueOf(id));
    }

    @Override
    public Optional<Image> get(String slug) {
        try {
            return Optional.ofNullable(
                    http.get("/images/" + slug, Map.of(), ImageResponse.class).image()
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    record  ImageResponse(Image image) { }
}
