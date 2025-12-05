package app.cmesh.azure;

public record ConnectedAzureAccount (
        String subscriptionId,
        String tenantId,
        String clientId,
        String clientSecret,
        String region,
        String friendlyName
){
}
