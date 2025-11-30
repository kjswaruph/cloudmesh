package app.cmesh.cloud;

import app.cmesh.AwsEc2Service;
import app.cmesh.AwsS3Service;
import app.cmesh.ConnectedAwsAccount;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AwsService {

    private final AwsEc2Service awsEc2Service;
    private final AwsS3Service awsS3Service;

    public AwsService(AwsEc2Service awsEc2Service, AwsS3Service awsS3Service) {
        this.awsEc2Service = awsEc2Service;
        this.awsS3Service = awsS3Service;
    }

    public List<String> getInstances(ConnectedAwsAccount account) {
        return awsEc2Service.listInstanceIds(account);
    }

    public List<String> getBuckets(ConnectedAwsAccount account) {
        return awsS3Service.listBuckets(account);
    }

}
