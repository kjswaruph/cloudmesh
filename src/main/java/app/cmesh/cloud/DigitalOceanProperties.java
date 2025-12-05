package app.cmesh.cloud;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "digital.ocean")
public record DigitalOceanProperties(
        String token,
        String baseUrl
) {}