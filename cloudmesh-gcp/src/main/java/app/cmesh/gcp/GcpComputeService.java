package app.cmesh.gcp;

import com.google.cloud.compute.v1.AggregatedListInstancesRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GcpComputeService {
    private final GcpCredentialsService credsService;

    public GcpComputeService(GcpCredentialsService credsService) {
        this.credsService = credsService;
    }

    private InstancesClient client() throws IOException {
        return InstancesClient.create();
    }

    public List<String> listInstanceIds(ConnectedGcpAccount account) throws IOException {
        try (InstancesClient client = client()) {
            var request = AggregatedListInstancesRequest.newBuilder()
                    .setProject(account.projectId())
                    .build();

            List<String> names = new ArrayList<>();
            // iterateAll() automatically handles pagination
            for (var entry : client.aggregatedList(request).iterateAll()) {
                if (entry.getValue() != null && entry.getValue().getInstancesList() != null) {
                    for (Instance instance : entry.getValue().getInstancesList()) {
                        names.add(instance.getName());
                    }
                }
            }
            return names;
        }
    }
}
