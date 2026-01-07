package app.cmesh.controller;

import app.cmesh.cost.Cost;
import app.cmesh.cost.repository.CostRepository;
import app.cmesh.dashboard.CloudCredentials;
import app.cmesh.dashboard.Project;
import app.cmesh.dashboard.repository.CloudCredentialsRepository;
import app.cmesh.dashboard.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for cost queries.
 */
@RestController
@RequestMapping("/api/costs")
@Slf4j
@RequiredArgsConstructor
public class CostController {

        private final CostRepository costRepository;
        private final CloudCredentialsRepository credentialsRepository;
        private final ProjectRepository projectRepository;

        /**
         * Get costs for a credential within a date range.
         */
        @GetMapping("/credentials/{credentialId}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<List<Cost>> credentialCosts(
                        @PathVariable UUID credentialId,
                        @RequestParam String startDate,
                        @RequestParam String endDate) {
                CloudCredentials credential = credentialsRepository.findById(credentialId)
                                .orElseThrow(() -> new RuntimeException("Credential not found"));

                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);

                return ResponseEntity.ok(costRepository.findByCredentialAndDateBetween(credential, start, end));
        }

        /**
         * Get costs for a project within a date range.
         */
        @GetMapping("/projects/{projectId}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<List<Cost>> projectCosts(
                        @PathVariable UUID projectId,
                        @RequestParam String startDate,
                        @RequestParam String endDate) {
                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Project not found"));

                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);

                return ResponseEntity.ok(costRepository.findByProjectAndDateBetween(project, start, end));
        }

        /**
         * Get total cost for a credential.
         */
        @GetMapping("/credentials/{credentialId}/total")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Double> totalCredentialCost(
                        @PathVariable UUID credentialId,
                        @RequestParam String startDate,
                        @RequestParam String endDate) {
                CloudCredentials credential = credentialsRepository.findById(credentialId)
                                .orElseThrow(() -> new RuntimeException("Credential not found"));

                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);

                Double total = costRepository.getTotalCostByCredential(credential, start, end);
                return ResponseEntity.ok(total != null ? total : 0.0);
        }

        /**
         * Get total cost for a project.
         */
        @GetMapping("/projects/{projectId}/total")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Double> totalProjectCost(
                        @PathVariable UUID projectId,
                        @RequestParam String startDate,
                        @RequestParam String endDate) {
                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Project not found"));

                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);

                Double total = costRepository.getTotalCostByProject(project, start, end);
                return ResponseEntity.ok(total != null ? total : 0.0);
        }

        /**
         * Get cost breakdown by service for a credential.
         */
        @GetMapping("/credentials/{credentialId}/service")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<List<ServiceCost>> costByService(
                        @PathVariable UUID credentialId,
                        @RequestParam String startDate,
                        @RequestParam String endDate) {
                CloudCredentials credential = credentialsRepository.findById(credentialId)
                                .orElseThrow(() -> new RuntimeException("Credential not found"));

                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);

                List<Object[]> results = costRepository.getCostByService(credential, start, end);

                List<ServiceCost> serviceCosts = results.stream()
                                .map(row -> new ServiceCost((String) row[0], (Double) row[1]))
                                .toList();
                return ResponseEntity.ok(serviceCosts);
        }

        /**
         * Get daily cost trend for a credential.
         */
        @GetMapping("/credentials/{credentialId}/trend")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<List<DailyCostTrend>> dailyCostTrend(
                        @PathVariable UUID credentialId,
                        @RequestParam String startDate,
                        @RequestParam String endDate) {
                CloudCredentials credential = credentialsRepository.findById(credentialId)
                                .orElseThrow(() -> new RuntimeException("Credential not found"));

                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);

                List<Object[]> results = costRepository.getDailyCostTrend(credential, start, end);

                List<DailyCostTrend> trends = results.stream()
                                .map(row -> new DailyCostTrend((LocalDate) row[0], (Double) row[1]))
                                .toList();
                return ResponseEntity.ok(trends);
        }

        /**
         * DTO for service cost breakdown.
         */
        public record ServiceCost(String service, Double totalCost) {
        }

        /**
         * DTO for daily cost trend.
         */
        public record DailyCostTrend(LocalDate date, Double totalCost) {
        }
}
