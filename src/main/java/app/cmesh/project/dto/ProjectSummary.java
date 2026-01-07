package app.cmesh.project.dto;

import java.util.Map;
import java.util.UUID;

/**
 * DTO for project summary dashboard.
 */
public record ProjectSummary(
        UUID projectId,
        String projectName,
        String description,
        Integer resourceCount,
        Double totalCost,
        Map<String, Integer> resourcesByProvider,
        Map<String, Integer> resourcesByType) {
}
