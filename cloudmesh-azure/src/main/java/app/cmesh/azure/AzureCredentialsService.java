package app.cmesh.azure;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class AzureCredentialsService {

    public TokenCredential credentialFor(ConnectedAzureAccount account) {
        if (account.clientId() != null && account.clientSecret() != null && account.tenantId() != null) {
            return new ClientSecretCredentialBuilder()
                    .clientId(account.clientId())
                    .clientSecret(account.clientSecret())
                    .tenantId(account.tenantId())
                    .build();
        }

        return new DefaultAzureCredentialBuilder().build();
    }
}
