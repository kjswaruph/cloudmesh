package app.cmesh;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;

@RequiredArgsConstructor
public class StsService {

    private final AwsConfig config;
    private final StsClient stsClient;

    public static StsService create(AwsConfig config) {
        StsClient sts = StsClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .region(Region.of(config.region()))
                .build();
        return new StsService(config, sts);
    }

    public AwsCredentialsProvider credentialsFor(ConnectedAwsAccount account) {
        AssumeRoleRequest request = AssumeRoleRequest.builder()
                .roleArn(account.roleArn())
                .externalId(account.externalId())
                .roleSessionName("cmesh-"+account.id())
                .build();

        AssumeRoleResponse response = stsClient.assumeRole(request);
        Credentials cred = response.credentials();
        AwsSessionCredentials sessionCreds = AwsSessionCredentials.create(
                cred.accessKeyId(),
                cred.secretAccessKey(),
                cred.sessionToken()
        );

        return StaticCredentialsProvider.create(sessionCreds);

    }
}
