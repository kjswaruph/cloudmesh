package app.cmesh.cloud;

import app.cmesh.credentials.adapter.CloudCredentialAdapter;
import app.cmesh.dashboard.enums.CloudProvider;
import app.cmesh.gcp.ConnectedGcpAccount;
import app.cmesh.gcp.GcpComputeService;
import app.cmesh.gcp.GcpStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class GcpService {

    private final GcpComputeService gcpComputeService;
    private final GcpStorageService gcpStorageService;
    private final CloudCredentialAdapter credentialAdapter;

    
    public List<String> getInstances(UUID credentialId, UUID userId) throws IOException {
        log.info("Listing GCP Compute instances for user {} using credential {}", userId, credentialId);

        credentialAdapter.validateCredentialProvider(credentialId, userId, CloudProvider.GCP);
        ConnectedGcpAccount account = credentialAdapter.toGcpAccount(credentialId, userId);

        return gcpComputeService.listInstanceIds(account);
    }

    
    public List<String> getBuckets(UUID credentialId, UUID userId) throws Exception {
        log.info("Listing GCP Storage buckets for user {} using credential {}", userId, credentialId);

        credentialAdapter.validateCredentialProvider(credentialId, userId, CloudProvider.GCP);
        ConnectedGcpAccount account = credentialAdapter.toGcpAccount(credentialId, userId);

        return gcpStorageService.listBuckets(account);
    }

    
    @Deprecated(since = "2.0", forRemoval = true)
    public List<String> getInstances(ConnectedGcpAccount account) throws IOException {
        log.warn("Using deprecated method - migrate to credential-based approach");
        return gcpComputeService.listInstanceIds(account);
    }

    
    @Deprecated(since = "2.0", forRemoval = true)
    public List<String> getBuckets(ConnectedGcpAccount account) throws Exception {
        log.warn("Using deprecated method - migrate to credential-based approach");
        return gcpStorageService.listBuckets(account);
    }

}
