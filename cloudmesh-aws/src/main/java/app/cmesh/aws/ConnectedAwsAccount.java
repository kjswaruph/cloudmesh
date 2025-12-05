package app.cmesh.aws;

public record ConnectedAwsAccount(
        String id,
        String roleArn,
        String externalId,
        String region
) { }
