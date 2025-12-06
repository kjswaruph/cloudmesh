package app.cmesh.config;

import app.cmesh.aws.AwsConfig;
import app.cmesh.aws.AwsEc2Service;
import app.cmesh.aws.AwsS3Service;
import app.cmesh.aws.StsService;
import app.cmesh.azure.AzureBlobService;
import app.cmesh.azure.AzureComputeService;
import app.cmesh.azure.AzureCredentialsService;
import app.cmesh.cloud.AwsProperties;
import app.cmesh.cloud.DigitalOceanProperties;
import app.cmesh.docean.DigitalOceanClient;
import app.cmesh.docean.DigitalOceanConfig;
import app.cmesh.docean.http.DefaultHttpExecutor;
import app.cmesh.docean.http.HttpExecutor;
import app.cmesh.gcp.GcpComputeService;
import app.cmesh.gcp.GcpCredentialsService;
import app.cmesh.gcp.GcpStorageService;
import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AwsProperties.class, DigitalOceanProperties.class})
public class CloudConfig {

    // == AWS Beans ==
    @Bean
    public AwsConfig awsConfig(AwsProperties awsProperties) {
        return new AwsConfig(
                awsProperties.getAccessKey(),
                awsProperties.getSecretKey(),
                awsProperties.getRegion()
        );
    }

    @Bean
    public StsService stsService(AwsConfig config) {
        return StsService.create(config);
    }

    @Bean
    public AwsS3Service awsS3Service(StsService stsService) {
        return new AwsS3Service(stsService);
    }

    @Bean
    public AwsEc2Service awsEc2Service(StsService stsService) {
        return new AwsEc2Service(stsService);
    }

    // == Azure Beans ==
    @Bean
    public AzureCredentialsService azureCredentialsService() { return new AzureCredentialsService(); }

    @Bean
    public AzureBlobService azureBlobService(AzureCredentialsService creds) { return new AzureBlobService(creds); }

    @Bean
    public AzureComputeService azureComputeService(AzureCredentialsService creds) { return new AzureComputeService(creds); }

    // == DigitalOcean Beans ==
    @Bean
    public DigitalOceanClient digitalOceanClient(DigitalOceanProperties oceanProperties) {
        DigitalOceanClient.Builder builder = DigitalOceanClient.builder()
                .token(oceanProperties.token());
        if (oceanProperties.baseUrl() != null && !oceanProperties.baseUrl().isBlank()) {
            builder.baseUrl(oceanProperties.baseUrl());
        }
        return builder.build();
    }

    @Bean
    public HttpExecutor httpExecutor(DigitalOceanProperties oceanProperties) {
        HttpClient httpClient = HttpClient.newBuilder().build();
        return new DefaultHttpExecutor(
                new DigitalOceanConfig(oceanProperties.baseUrl(), oceanProperties.token()),
                httpClient
        );
    }

    // == GCP Beans ==
    @Bean
    public GcpCredentialsService gcpCredentialsService() { return  new GcpCredentialsService(); }

    @Bean
    public GcpComputeService gcpComputeService(GcpCredentialsService creds) { return new GcpComputeService(creds); }

    @Bean
    public GcpStorageService gcpStorageService(GcpCredentialsService creds) { return new GcpStorageService(creds); }

}
