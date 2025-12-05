package app.cmesh.cloud;

import app.cmesh.credentials.adapter.CloudCredentialAdapter;
import app.cmesh.dashboard.enums.CloudProvider;
import app.cmesh.docean.DigitalOceanClient;
import app.cmesh.docean.model.Droplet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalOceanService {

    private final CloudCredentialAdapter credentialAdapter;

    
    public List<String> listDropletNames(UUID credentialId, UUID userId) {
        log.info("Listing DigitalOcean droplets for user {} using credential {}", userId, credentialId);

        credentialAdapter.validateCredentialProvider(credentialId, userId, CloudProvider.DIGITALOCEAN);
        DigitalOceanClient client = credentialAdapter.toDigitalOceanClient(credentialId, userId);

        return client.droplets().list().stream()
                .map(Droplet::name)
                .toList();
    }

    
    public void powerOn(UUID credentialId, UUID userId, long dropletId) {
        log.info("Powering on droplet {} for user {} using credential {}", dropletId, userId, credentialId);

        credentialAdapter.validateCredentialProvider(credentialId, userId, CloudProvider.DIGITALOCEAN);
        DigitalOceanClient client = credentialAdapter.toDigitalOceanClient(credentialId, userId);

        client.droplets().powerOn(dropletId);
    }

    
    public DigitalOceanClient getClient(UUID credentialId, UUID userId) {
        log.debug("Creating DigitalOcean client for user {} using credential {}", userId, credentialId);

        credentialAdapter.validateCredentialProvider(credentialId, userId, CloudProvider.DIGITALOCEAN);
        return credentialAdapter.toDigitalOceanClient(credentialId, userId);
    }

    @Deprecated(since = "2.0", forRemoval = true)
    public List<String> listDropletNames() {
        log.warn("Using deprecated method without credential context - this will be removed");
        throw new UnsupportedOperationException(
                "Direct client access deprecated. Use listDropletNames(credentialId, userId) instead");
    }

    
    @Deprecated(since = "2.0", forRemoval = true)
    public void powerOn(long id) {
        log.warn("Using deprecated method without credential context - this will be removed");
        throw new UnsupportedOperationException(
                "Direct client access deprecated. Use powerOn(credentialId, userId, dropletId) instead");
    }

}
