package app.cmesh.gcp;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GcpStorageService {

    private final GcpCredentialsService credsService;

    public GcpStorageService(GcpCredentialsService credsService) {
        this.credsService = credsService;
    }

    private Storage clientFor(ConnectedGcpAccount account) throws IOException {
        StorageOptions options = credsService.storageOptionsFor(account);
        return options.getService();
    }

    public List<String> listBuckets(ConnectedGcpAccount account) throws Exception {
        try(Storage storage = clientFor(account)) {
            List<String> names = new ArrayList<>();
            for (Bucket bucket : storage.list().iterateAll()) {
                names.add(bucket.getName());
            }
            return names;
        }
    }

    public void createBucket(ConnectedGcpAccount account, String bucketName) throws Exception {
        try(Storage storage = clientFor(account)) {
            storage.create(Bucket.newBuilder(bucketName).setLocation(account.region()).build());
        }
    }
}
