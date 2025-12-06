package app.cmesh.azure;

import com.azure.core.credential.TokenCredential;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AzureBlobService {
    private final AzureCredentialsService creds;

    public AzureBlobService(AzureCredentialsService creds) {
        this.creds = creds;
    }

    private BlobServiceClient clientFor(ConnectedAzureAccount account) {
        TokenCredential credential = creds.credentialFor(account);
        String endpoint = System.getenv("AZURE_STORAGE_BLOB_ENDPOINT");
        return new BlobServiceClientBuilder()
                .credential(credential)
                .endpoint(endpoint)
                .buildClient();
    }

    public List<String> listContainers(ConnectedAzureAccount account) {
        BlobServiceClient client = clientFor(account);
        return StreamSupport.stream(client.listBlobContainers().spliterator(), false)
                .map(BlobContainerItem::getName)
                .collect(Collectors.toList());
    }

    public void createContainer(ConnectedAzureAccount account, String containerName) {
        BlobServiceClient client = clientFor(account);
        client.createBlobContainer(containerName);
    }
}
