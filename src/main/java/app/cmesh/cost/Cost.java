package app.cmesh.cost;

import app.cmesh.dashboard.CloudCredentials;
import app.cmesh.dashboard.Project;
import app.cmesh.dashboard.Resource;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing a daily cost entry from a cloud provider.
 * Costs can be associated with specific resources and projects.
 */
@Data
@Entity
@Table(name = "costs", indexes = {
        @Index(name = "idx_cost_credential_date", columnList = "credential_id,date"),
        @Index(name = "idx_cost_resource", columnList = "resource_id"),
        @Index(name = "idx_cost_project", columnList = "project_id")
})
public class Cost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cost_id", updatable = false, nullable = false)
    private UUID costId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "credential_id", nullable = false)
    private CloudCredentials credential;

    @ManyToOne
    @JoinColumn(name = "resource_id")
    private Resource resource; // Nullable - some costs can't be mapped to specific resources

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project; // Nullable - derived from resource or unassigned

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency; // USD, EUR, etc.

    @Column(nullable = false, length = 50)
    private String service; // EC2, S3, RDS, etc.

    @Column(length = 100)
    private String usageType; // BoxUsage:t2.micro, etc.

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    private Map<String, String> tags; // AWS tags from cost data

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
