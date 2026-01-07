package app.cmesh.project;

import app.cmesh.dashboard.Project;
import app.cmesh.dashboard.Resource;
import app.cmesh.dashboard.repository.ProjectRepository;
import app.cmesh.dashboard.repository.ResourceRepository;
import app.cmesh.project.repository.ResourceAssignmentRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing resource-to-project assignments.
 * Handles both automatic (rule-based) and manual assignments.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceAssignmentService {

    private final ResourceRepository resourceRepository;
    private final ProjectRepository projectRepository;
    private final ResourceAssignmentRuleRepository ruleRepository;

    /**
     * Apply all assignment rules to resources.
     * Only affects resources that are not manually assigned.
     */
    @Transactional
    public void applyAssignmentRules() {
        log.info("[ResourceAssignment] Starting rule application");
        long startTime = System.currentTimeMillis();

        List<ResourceAssignmentRule> rules = ruleRepository.findByEnabledTrueOrderByPriorityDesc();
        log.info("[ResourceAssignment] Found {} enabled rules", rules.size());

        // Get all resources that are not manually assigned
        List<Resource> resources = resourceRepository.findAll().stream()
                .filter(r -> r.getManuallyAssigned() == null || !r.getManuallyAssigned())
                .toList();

        log.info("[ResourceAssignment] Processing {} auto-assignable resources", resources.size());

        int assignedCount = 0;
        for (Resource resource : resources) {
            // Try to match against rules (highest priority first)
            for (ResourceAssignmentRule rule : rules) {
                if (matchesRule(resource, rule)) {
                    resource.setProject(rule.getProject());
                    resourceRepository.save(resource);
                    assignedCount++;
                    log.debug("[ResourceAssignment] Assigned resource {} to project {} via rule {}",
                            resource.getResourceName(), rule.getProject().getProjectName(), rule.getRuleId());
                    break; // Stop at first matching rule
                }
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("[ResourceAssignment] Completed in {}ms - {} resources assigned", duration, assignedCount);
    }

    /**
     * Manually assign a resource to a project.
     * Sets manuallyAssigned flag to prevent auto-assignment rules from changing it.
     */
    @Transactional
    public Resource manuallyAssign(UUID resourceId, UUID projectId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        resource.setProject(project);
        resource.setManuallyAssigned(true);

        log.info("[ResourceAssignment] Manually assigned resource {} to project {}",
                resource.getResourceName(), project.getProjectName());

        return resourceRepository.save(resource);
    }

    /**
     * Remove manual assignment from a resource.
     * Resource becomes eligible for auto-assignment again.
     */
    @Transactional
    public Resource removeManualAssignment(UUID resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));

        resource.setManuallyAssigned(false);

        log.info("[ResourceAssignment] Removed manual assignment from resource {}",
                resource.getResourceName());

        return resourceRepository.save(resource);
    }

    /**
     * Check if a resource matches a rule's tag conditions.
     * Resource must have ALL tags specified in the rule.
     */
    private boolean matchesRule(Resource resource, ResourceAssignmentRule rule) {
        Map<String, String> resourceTags = resource.getTags();
        Map<String, String> ruleConditions = rule.getTagConditions();

        if (resourceTags == null || resourceTags.isEmpty()) {
            return false;
        }

        if (ruleConditions == null || ruleConditions.isEmpty()) {
            return false;
        }

        // Resource must have ALL tags from rule with exact values
        for (Map.Entry<String, String> condition : ruleConditions.entrySet()) {
            String resourceValue = resourceTags.get(condition.getKey());
            if (resourceValue == null || !resourceValue.equals(condition.getValue())) {
                return false;
            }
        }

        return true;
    }
}
