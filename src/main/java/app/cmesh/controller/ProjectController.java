package app.cmesh.controller;

import app.cmesh.cost.repository.CostRepository;
import app.cmesh.dashboard.Project;
import app.cmesh.dashboard.Resource;
import app.cmesh.dashboard.repository.ProjectRepository;
import app.cmesh.dashboard.repository.ResourceRepository;
import app.cmesh.project.ResourceAssignmentRule;
import app.cmesh.project.ResourceAssignmentService;
import app.cmesh.project.dto.ProjectSummary;
import app.cmesh.project.repository.ResourceAssignmentRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@Slf4j
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceAssignmentRuleRepository ruleRepository;
    private final ResourceAssignmentService assignmentService;
    private final CostRepository costRepository;

    @GetMapping("/{projectId}/resources")
    @PreAuthorize("isAuthenticated()")
    public List<Resource> projectResources(@PathVariable UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        return resourceRepository.findAll().stream()
                .filter(r -> r.getProject() != null && r.getProject().getProjectId().equals(projectId))
                .toList();
    }

    /**
     * Get project summary with aggregated stats.
     */
    @GetMapping("/{projectId}/summary")
    @PreAuthorize("isAuthenticated()")
    public ProjectSummary projectSummary(@PathVariable UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        List<Resource> resources = resourceRepository.findAll().stream()
                .filter(r -> r.getProject() != null && r.getProject().getProjectId().equals(projectId))
                .toList();

        // Count by provider
        Map<String, Integer> byProvider = resources.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getProvider().name(),
                        Collectors.summingInt(r -> 1)));

        // Count by type
        Map<String, Integer> byType = resources.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getResourceType().name(),
                        Collectors.summingInt(r -> 1)));

        // Get total cost (last 30 days)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        Double totalCost = costRepository.getTotalCostByProject(project, startDate, endDate);

        return new ProjectSummary(
                project.getProjectId(),
                project.getProjectName(),
                project.getDescription(),
                resources.size(),
                totalCost,
                byProvider,
                byType);
    }

    /**
     * Get all assignment rules for a project.
     */
    @GetMapping("/{projectId}/rules")
    @PreAuthorize("isAuthenticated()")
    public List<ResourceAssignmentRule> projectAssignmentRules(@PathVariable UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        return ruleRepository.findByProject(project);
    }

    /**
     * Manually assign a resource to a project.
     */
    @PostMapping("/{projectId}/assign/{resourceId}")
    @PreAuthorize("isAuthenticated()")
    public Resource assignResourceToProject(
            @PathVariable UUID resourceId,
            @PathVariable UUID projectId) {
        return assignmentService.manuallyAssign(resourceId, projectId);
    }

    /**
     * Remove manual assignment from a resource.
     */
    @PostMapping("/resources/{resourceId}/unassign")
    @PreAuthorize("isAuthenticated()")
    public Resource unassignResource(@PathVariable UUID resourceId) {
        return assignmentService.removeManualAssignment(resourceId);
    }

    public record CreateRuleRequest(Map<String, String> tagConditions, Integer priority, String description) {
    }

    /**
     * Create a new assignment rule.
     */
    @PostMapping("/{projectId}/rules")
    @PreAuthorize("isAuthenticated()")
    public ResourceAssignmentRule createAssignmentRule(
            @PathVariable UUID projectId,
            @RequestBody CreateRuleRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        ResourceAssignmentRule rule = new ResourceAssignmentRule();
        rule.setProject(project);
        rule.setTagConditions(request.tagConditions());
        rule.setPriority(request.priority() != null ? request.priority() : 0);
        rule.setDescription(request.description());
        rule.setEnabled(true);

        ResourceAssignmentRule saved = ruleRepository.save(rule);

        log.info("Created assignment rule {} for project {}", saved.getRuleId(), project.getProjectName());

        return saved;
    }

    /**
     * Delete an assignment rule.
     */
    @DeleteMapping("/rules/{ruleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAssignmentRule(@PathVariable UUID ruleId) {
        if (!ruleRepository.existsById(ruleId)) {
            return ResponseEntity.notFound().build();
        }

        ruleRepository.deleteById(ruleId);
        log.info("Deleted assignment rule {}", ruleId);

        return ResponseEntity.ok().build();
    }

    /**
     * Trigger manual application of assignment rules.
     */
    @PostMapping("/rules/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> applyAssignmentRules() {
        try {
            assignmentService.applyAssignmentRules();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to apply assignment rules", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
