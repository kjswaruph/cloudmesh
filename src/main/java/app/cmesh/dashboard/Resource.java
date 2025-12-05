package app.cmesh.dashboard;

import app.cmesh.dashboard.enums.CloudProvider;
import app.cmesh.dashboard.enums.ResourceStatus;
import app.cmesh.dashboard.enums.ResourceType;
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
import java.util.UUID;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "resource_id", updatable = false, nullable = false)
    private UUID resourceId;
    @Column(name = "resource_name", nullable = false)
    private String resourceName;
    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CloudProvider provider;
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private ResourceType resourceType;
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_status", nullable = false)
    private ResourceStatus resourceStatus;
    @Column(name = "resource_region", nullable = false)
    private String resourceRegion;
    @Column(name = "resource_cost", nullable = false)
    private Double resourceCost;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
