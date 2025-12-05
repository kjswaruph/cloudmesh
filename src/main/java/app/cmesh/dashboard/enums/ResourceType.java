package app.cmesh.dashboard.enums;

public enum ResourceType {
    // AWS
    EC2_INSTANCE,
    S3_BUCKET,
    RDS_DATABASE,
    VPC,
    LAMBDA_FUNCTION,

    // GCP
    COMPUTE_INSTANCE,
    STORAGE_BUCKET,
    CLOUD_SQL,

    // Azure
    VIRTUAL_MACHINE,
    STORAGE_ACCOUNT,
    SQL_DATABASE,

    // DigitalOcean
    DROPLET,
    SPACES_BUCKET,
    DATABASE_CLUSTER,
}
