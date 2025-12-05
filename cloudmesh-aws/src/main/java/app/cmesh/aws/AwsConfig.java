package app.cmesh.aws;

public record AwsConfig (
    String accessKey,
    String secretKey,
    String region
){}
