package app.cmesh.aws.validation;
import app.cmesh.aws.enums.CloudProvider;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeRegionsRequest;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import java.util.Map;
@Slf4j
public class AwsCredentialValidator implements CloudCredentialValidator {
    private final String cloudmeshAwsAccessKey;
    private final String cloudmeshAwsSecretKey;
    private final String defaultRegion;
    public AwsCredentialValidator(String cloudmeshAwsAccessKey, String cloudmeshAwsSecretKey, String defaultRegion) {
        this.cloudmeshAwsAccessKey = cloudmeshAwsAccessKey;
        this.cloudmeshAwsSecretKey = cloudmeshAwsSecretKey;
        this.defaultRegion = defaultRegion != null ? defaultRegion : "us-east-1";
    }
    @Override
    public CloudProvider getProvider() {
        return CloudProvider.AWS;
    }
    @Override
    public ValidationResult validateFormat(Map<String, String> config) {
        String roleArn = config.get("roleArn");
        String externalId = config.get("externalId");
        String region = config.get("region");
        if (roleArn == null || roleArn.isBlank()) {
            return ValidationResult.failure("Missing roleArn", "roleArn is required");
        }
        if (externalId == null || externalId.isBlank()) {
            return ValidationResult.failure("Missing externalId", "externalId is required");
        }
        if (!roleArn.startsWith("arn:aws:iam::")) {
            return ValidationResult.failure(
                "Invalid roleArn format",
                "roleArn must start with 'arn:aws:iam::'. Example: arn:aws:iam::123456789012:role/RoleName"
            );
        }
        if (!roleArn.contains(":role/")) {
            return ValidationResult.failure(
                "Invalid roleArn format",
                "roleArn must contain ':role/'. Example: arn:aws:iam::123456789012:role/RoleName"
            );
        }
        if (region != null && !region.isBlank()) {
            try {
                Region.of(region);
            } catch (Exception e) {
                return ValidationResult.failure(
                    "Invalid AWS region",
                    "Region '" + region + "' is not a valid AWS region"
                );
            }
        }
        return ValidationResult.success("AWS credential format is valid");
    }
    @Override
    public ValidationResult validate(Map<String, String> config) {
        ValidationResult formatResult = validateFormat(config);
        if (!formatResult.valid()) {
            return formatResult;
        }
        String roleArn = config.get("roleArn");
        String externalId = config.get("externalId");
        String region = config.getOrDefault("region", defaultRegion);
        log.info("Validating AWS credentials - roleArn: {}, region: {}", roleArn, region);
        if (cloudmeshAwsAccessKey == null || cloudmeshAwsSecretKey == null) {
            log.error("Cloudmesh AWS credentials not configured");
            return ValidationResult.failure(
                "Configuration error",
                "Cloudmesh AWS credentials are not configured"
            );
        }
        try {
            AwsBasicCredentials cloudmeshCreds = AwsBasicCredentials.create(
                cloudmeshAwsAccessKey,
                cloudmeshAwsSecretKey
            );
            StsClient stsClient = StsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(cloudmeshCreds))
                .build();
            AssumeRoleRequest assumeRoleRequest = AssumeRoleRequest.builder()
                .roleArn(roleArn)
                .externalId(externalId)
                .roleSessionName("cloudmesh-validation-" + System.currentTimeMillis())
                .durationSeconds(900)
                .build();
            AssumeRoleResponse assumeRoleResponse;
            try {
                assumeRoleResponse = stsClient.assumeRole(assumeRoleRequest);
            } catch (software.amazon.awssdk.services.sts.model.StsException e) {
                log.warn("Failed to assume role: {}", e.getMessage());
                if (e.getMessage().contains("AccessDenied")) {
                    return ValidationResult.failure(
                        "Access denied",
                        "Cannot assume role. Verify trust policy and External ID: " + externalId
                    );
                } else if (e.getMessage().contains("not found") || e.getMessage().contains("does not exist")) {
                    return ValidationResult.failure(
                        "Role not found",
                        "The IAM role '" + roleArn + "' does not exist or is not accessible"
                    );
                } else {
                    return ValidationResult.failure(
                        "Failed to assume role",
                        "STS AssumeRole failed: " + e.getMessage()
                    );
                }
            }
            Credentials credentials = assumeRoleResponse.credentials();
            AwsSessionCredentials sessionCreds = AwsSessionCredentials.create(
                credentials.accessKeyId(),
                credentials.secretAccessKey(),
                credentials.sessionToken()
            );
            Ec2Client ec2Client = Ec2Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(sessionCreds))
                .build();
            try {
                ec2Client.describeRegions(DescribeRegionsRequest.builder().build());
                log.info("Successfully validated AWS credentials for roleArn: {}", roleArn);
                return ValidationResult.success(
                    "AWS credentials validated successfully",
                    "Successfully assumed role and verified EC2 access. Role ARN: " + roleArn
                );
            } catch (software.amazon.awssdk.services.ec2.model.Ec2Exception e) {
                log.warn("EC2 API test failed: {}", e.getMessage());
                if (e.getMessage().contains("UnauthorizedOperation")) {
                    return ValidationResult.failure(
                        "Insufficient permissions",
                        "Role assumed successfully but lacks EC2 permissions"
                    );
                } else {
                    log.info("Role assumption successful, EC2 test inconclusive");
                    return ValidationResult.success(
                        "AWS credentials validated (partial)",
                        "Successfully assumed role, but EC2 test failed: " + e.getMessage()
                    );
                }
            } finally {
                ec2Client.close();
            }
        } catch (Exception e) {
            log.error("Unexpected error validating AWS credentials", e);
            return ValidationResult.failure(
                "Validation error",
                "Unexpected error: " + e.getMessage()
            );
        }
    }
}
