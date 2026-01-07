package app.cmesh.security.repository;

import app.cmesh.security.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for AuditLog entities.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Find audit logs for a user.
     */
    List<AuditLog> findByUserIdOrderByTimestampDesc(UUID userId);

    /**
     * Find recent audit logs.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentLogs(@Param("since") Instant since);

    /**
     * Find failed authentication attempts.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action = 'LOGIN' AND a.status = 'FAILED' " +
            "AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findFailedLogins(@Param("since") Instant since);
}
