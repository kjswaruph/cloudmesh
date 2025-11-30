package app.cmesh.cloud;

import app.cmesh.AwsConfig;
import app.cmesh.AwsEc2Service;
import app.cmesh.AwsS3Service;
import app.cmesh.StsService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AwsProperties.class)
public class AwsConfiguration {

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
}
