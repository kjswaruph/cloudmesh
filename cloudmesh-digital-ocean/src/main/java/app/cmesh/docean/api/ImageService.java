package app.cmesh.docean.api;

import app.cmesh.docean.model.Image;
import java.util.List;
import java.util.Optional;

public interface ImageService {
    List<Image> list(String type);
    List<Image> listAll();
    Optional<Image> get(long id);
    Optional<Image> get(String slug);
}
