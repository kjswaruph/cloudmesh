package app.cmesh.azure;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import java.util.ArrayList;
import java.util.List;

public class AzureComputeService {
    private final AzureCredentialsService creds;

    public AzureComputeService(AzureCredentialsService creds) {
        this.creds = creds;
    }

    private AzureResourceManager clientFor(ConnectedAzureAccount account) {
        TokenCredential credential = creds.credentialFor(account);
        AzureProfile profile = new AzureProfile(account.tenantId(), account.subscriptionId(), AzureEnvironment.AZURE);
        return AzureResourceManager
                .authenticate(credential, profile)
                .withSubscription(account.subscriptionId());
    }

    public List<String> listVmNames(ConnectedAzureAccount account) {
        AzureResourceManager arm = clientFor(account);
        List<String> vmNames = new ArrayList<>();
        for (VirtualMachine vm : arm.virtualMachines().list()) {
            vmNames.add(vm.name());
        }
        return vmNames;
    }
}
