package app.cmesh.aws;

import java.util.List;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.StartInstancesRequest;
import software.amazon.awssdk.services.ec2.model.StopInstancesRequest;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;

public class AwsEc2Service {

    private final StsService stsService;
    public AwsEc2Service(StsService stsService) {
        this.stsService = stsService;
    }

    private Ec2Client clientFor(ConnectedAwsAccount account) {
        return Ec2Client.builder()
                .credentialsProvider(stsService.credentialsFor(account))
                .region(software.amazon.awssdk.regions.Region.of(account.region()))
                .build();
    }

    public List<String> listInstanceIds(ConnectedAwsAccount account) {
        try(Ec2Client ec2 = clientFor(account)) {
            return ec2.describeInstances()
                    .reservations()
                    .stream()
                    .flatMap(reservation -> reservation.instances().stream())
                    .map(Instance::instanceId)
                    .toList();
        }
    }

    public String createInstance(ConnectedAwsAccount account, String amiId, String instanceType, String keyname) {
        try(Ec2Client ec2 = clientFor(account)) {
            RunInstancesRequest request = RunInstancesRequest.builder()
                    .imageId(amiId)
                    .instanceType(instanceType)
                    .keyName(keyname)
                    .minCount(1)
                    .maxCount(1)
                    .build();

            RunInstancesResponse response = ec2.runInstances(request);
            return response.instances().get(0).instanceId();
        }
    }

    public void startInstance(ConnectedAwsAccount account, String instanceId) {
        try(Ec2Client ec2 = clientFor(account)) {
            ec2.startInstances(StartInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build());
        }
    }

    public void stopInstance(ConnectedAwsAccount account, String instanceId) {
        try(Ec2Client ec2 = clientFor(account)) {
            ec2.stopInstances(StopInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build());
        }
    }

    public void terminateInstance(ConnectedAwsAccount account, String instanceId) {
        try(Ec2Client ec2 = clientFor(account)) {
            ec2.terminateInstances(TerminateInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build());
        }
    }

}
