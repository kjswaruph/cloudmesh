package app.cmesh.gcp;

import com.google.auth.Credentials;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.StorageOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GcpCredentialsService {

    public Credentials credentialsFor(ConnectedGcpAccount account) throws IOException {
        String json = account.serviceAccountJson();
        if(json != null && !json.isBlank()) {
            try(var stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
                return GoogleCredentials.fromStream(stream);
            }
        }
        return GoogleCredentials.getApplicationDefault();
    }

    public StorageOptions storageOptionsFor(ConnectedGcpAccount account) throws IOException {
        Credentials creds = credentialsFor(account);
        return StorageOptions.newBuilder()
                .setProjectId(account.projectId())
                .setCredentials(creds)
                .build();
    }
}
