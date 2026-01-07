package app.cmesh.cost.repository;

import app.cmesh.cost.Cost;
import app.cmesh.dashboard.CloudCredentials;
import app.cmesh.dashboard.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Cost entities with aggregation queries.
 */
@Repository
public interface CostRepository extends JpaRepository<Cost, UUID> {

    /**
     * Find all costs for a credential within a date range.
     */
    List<Cost> findByCredentialAndDateBetween(
            CloudCredentials credential,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * Find all costs for a project within a date range.
     */
    List<Cost> findByProjectAndDateBetween(
            Project project,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * Get total cost for a project within a date range.
     */
    @Query("SELECT COALESCE(SUM(c.amount), 0.0) FROM Cost c " +
            "WHERE c.project = :project AND c.date BETWEEN :startDate AND :endDate")
    Double getTotalCostByProject(
            @Param("project") Project project,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get total cost for a credential within a date range.
     */
    @Query("SELECT COALESCE(SUM(c.amount), 0.0) FROM Cost c " +
            "WHERE c.credential = :credential AND c.date BETWEEN :startDate AND :endDate")
    Double getTotalCostByCredential(
            @Param("credential") CloudCredentials credential,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get cost breakdown by service for a credential.
     * Returns list of [service, totalCost] arrays.
     */
    @Query("SELECT c.service, SUM(c.amount) FROM Cost c " +
            "WHERE c.credential = :credential AND c.date BETWEEN :startDate AND :endDate " +
            "GROUP BY c.service ORDER BY SUM(c.amount) DESC")
    List<Object[]> getCostByService(
            @Param("credential") CloudCredentials credential,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get daily cost trend for a credential.
     * Returns list of [date, totalCost] arrays.
     */
    @Query("SELECT c.date, SUM(c.amount) FROM Cost c " +
            "WHERE c.credential = :credential AND c.date BETWEEN :startDate AND :endDate " +
            "GROUP BY c.date ORDER BY c.date")
    List<Object[]> getDailyCostTrend(
            @Param("credential") CloudCredentials credential,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Check if costs exist for a credential and date.
     */
    boolean existsByCredentialAndDate(CloudCredentials credential, LocalDate date);
}
