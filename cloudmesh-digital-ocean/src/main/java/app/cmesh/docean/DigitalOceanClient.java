package app.cmesh.docean;

import app.cmesh.docean.api.DropletService;
import app.cmesh.docean.api.ImageService;
import app.cmesh.docean.api.RegionService;
import app.cmesh.docean.api.SizeService;
import app.cmesh.docean.http.DefaultHttpExecutor;
import app.cmesh.docean.http.HttpExecutor;
import app.cmesh.docean.impl.DropletServiceImpl;
import app.cmesh.docean.impl.ImageServiceImpl;
import app.cmesh.docean.impl.RegionServiceImpl;
import app.cmesh.docean.impl.SizeServiceImpl;
import java.net.http.HttpClient;

/// # DigitalOcean API Client
/// This is the main client class for interacting with the DigitalOcean API.
/// It provides access to various services such as Droplets, Regions, Sizes, and Images.
/// @author Swaruph
/// @version 1.0
public final class DigitalOceanClient {

    private final DropletService dropletService;
    private final RegionService regionService;
    private final SizeService sizeService;
    private final ImageService imageService;

    private DigitalOceanClient(Builder builder) {
        DigitalOceanConfig config = new DigitalOceanConfig(
                builder.baseUrl,
                builder.token
        );

        HttpExecutor httpExecutor = builder.httpExecutor;
        if(httpExecutor == null) {
            HttpClient httpClient = HttpClient.newBuilder()
                    .build();
            httpExecutor = new DefaultHttpExecutor(config, httpClient);
        }

        this.dropletService = new DropletServiceImpl(httpExecutor);
        this.regionService = new RegionServiceImpl(httpExecutor);
        this.sizeService = new SizeServiceImpl(httpExecutor);
        this.imageService = new ImageServiceImpl(httpExecutor);
    }

    public static Builder builder() {
        return new Builder();
    }

    public DropletService droplets() { return dropletService; }

    public RegionService regions() { return regionService; }

    public SizeService sizes() { return sizeService; }

    public ImageService images() { return imageService; }

    public static class Builder {
        private String baseUrl = "https://api.digitalocean.com/v2";
        private String token;
        private HttpExecutor httpExecutor;

        private Builder() {}

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder httpExecutor(HttpExecutor httpExecutor) {
            this.httpExecutor = httpExecutor;
            return this;
        }

        public DigitalOceanClient build() {
            return new DigitalOceanClient(this);
        }
    }
}
