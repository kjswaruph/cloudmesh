package app.cmesh.project.repository;

import app.cmesh.dashboard.Project;
import app.cmesh.project.ResourceAssignmentRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ResourceAssignmentRule entities.
 */
@Repository
public interface ResourceAssignmentRuleRepository extends JpaRepository<ResourceAssignmentRule, UUID> {

    /**
     * Find all enabled rules ordered by priority (descending).
     */
    List<ResourceAssignmentRule> findByEnabledTrueOrderByPriorityDesc();

    /**
     * Find all rules for a project.
     */
    List<ResourceAssignmentRule> findByProject(Project project);

    /**
     * Find all enabled rules for a project.
     */
    List<ResourceAssignmentRule> findByProjectAndEnabledTrue(Project project);
}
