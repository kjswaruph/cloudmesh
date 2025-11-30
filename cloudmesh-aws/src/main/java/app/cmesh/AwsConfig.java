package app.cmesh;

public record AwsConfig (
    String accessKey,
    String secretKey,
    String region
){}
