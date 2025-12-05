package app.cmesh.docean;

public record DigitalOceanConfig(
        String baseUrl,
        String apiToken
) {
    public static DigitalOceanConfig getDigitalOceanConfig(String apiToken) {
        return new  DigitalOceanConfig(
                "https://api.digitalocean.com/v2/",
                apiToken
        );
    }
}
