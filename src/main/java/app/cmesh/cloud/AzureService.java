package app.cmesh.cloud;

import app.cmesh.azure.AzureBlobService;
import app.cmesh.azure.AzureComputeService;
import app.cmesh.azure.ConnectedAzureAccount;
import app.cmesh.credentials.adapter.CloudCredentialAdapter;
import app.cmesh.dashboard.enums.CloudProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AzureService {

    private final AzureBlobService azureBlobService;
    private final AzureComputeService azureComputeService;
    private final CloudCredentialAdapter credentialAdapter;

    
    public List<String> listVMs(UUID credentialId, UUID userId) {
        log.info("Listing Azure VMs for user {} using credential {}", userId, credentialId);

        credentialAdapter.validateCredentialProvider(credentialId, userId, CloudProvider.AZURE);
        ConnectedAzureAccount account = credentialAdapter.toAzureAccount(credentialId, userId);

        return azureComputeService.listVmNames(account);
    }

    
    public List<String> listBlobs(UUID credentialId, UUID userId) {
        log.info("Listing Azure blob containers for user {} using credential {}", userId, credentialId);

        credentialAdapter.validateCredentialProvider(credentialId, userId, CloudProvider.AZURE);
        ConnectedAzureAccount account = credentialAdapter.toAzureAccount(credentialId, userId);

        return azureBlobService.listContainers(account);
    }

    
    @Deprecated(since = "2.0", forRemoval = true)
    public List<String> listVMs(ConnectedAzureAccount account) {
        log.warn("Using deprecated method - migrate to credential-based approach");
        return azureComputeService.listVmNames(account);
    }

    
    @Deprecated(since = "2.0", forRemoval = true)
    public List<String> listBlobs(ConnectedAzureAccount account) {
        log.warn("Using deprecated method - migrate to credential-based approach");
        return azureBlobService.listContainers(account);
    }

}
