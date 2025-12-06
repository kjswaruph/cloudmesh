package app.cmesh.aws;

import java.util.List;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

public class AwsS3Service {

    private final StsService service;

    public AwsS3Service(StsService service) {
        this.service = service;
    }

    private S3Client clientFor(ConnectedAwsAccount account) {
        AwsCredentialsProvider creds = service.credentialsFor(account);
        return S3Client.builder()
                .credentialsProvider(creds)
                .region(Region.of(account.region()))
                .build();
    }

    public List<String> listBuckets(ConnectedAwsAccount account) {
        try(S3Client s3Client = clientFor(account)) {
            ListBucketsResponse resp = s3Client.listBuckets();
            return resp.buckets()
                    .stream()
                    .map(Bucket::name)
                    .toList();
        }
    }

    public void createBucket(ConnectedAwsAccount account, String bucketName) {
        try(S3Client s3 = clientFor(account)) {
            s3.createBucket(CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
        }
    }
}
