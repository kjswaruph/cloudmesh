package app.cmesh.gcp;

public record ConnectedGcpAccount(
        String projectId,
        String region,
        String zone,
        String serviceAccountJson
) {
}
