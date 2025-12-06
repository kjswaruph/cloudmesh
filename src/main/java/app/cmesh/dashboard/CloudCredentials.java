package app.cmesh.dashboard;

import app.cmesh.dashboard.enums.CloudProvider;
import app.cmesh.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Data
@Table(name = "cloud_credentials")
public class CloudCredentials {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID credentialId;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Enumerated(EnumType.STRING)
    private CloudProvider provider;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CredentialStatus status;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "provider_config", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> providerConfig;
    @Column(name = "friendly_name", nullable = false)
    private String friendlyName;
    @Column(name = "last_validated_at")
    @UpdateTimestamp
    private Instant lastValidatedAt;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    public enum CredentialStatus{
        ACTIVE, INVALID, EXPIRED, PENDING
    }
}


