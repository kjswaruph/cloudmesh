package app.cmesh.project;

import app.cmesh.dashboard.Project;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing a tag-based rule for automatically assigning resources to
 * projects.
 * Rules are evaluated in priority order (higher priority first).
 */
@Data
@Entity
@Table(name = "resource_assignment_rules", indexes = {
        @Index(name = "idx_rule_project", columnList = "project_id"),
        @Index(name = "idx_rule_priority", columnList = "priority DESC")
})
public class ResourceAssignmentRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rule_id", updatable = false, nullable = false)
    private UUID ruleId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tag_conditions", columnDefinition = "jsonb", nullable = false)
    private Map<String, String> tagConditions; // Tags that resource must have to match

    @Column(nullable = false)
    private Integer priority = 0; // Higher priority rules evaluated first

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
