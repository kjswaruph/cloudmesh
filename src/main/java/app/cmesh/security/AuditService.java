package app.cmesh.security;

import app.cmesh.security.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Service for logging security-relevant events.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log credential creation.
     */
    public void logCredentialCreation(UUID userId, UUID credentialId, String provider) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setAction("CREDENTIAL_CREATED");
        auditLog.setResourceType("CREDENTIAL");
        auditLog.setResourceId(credentialId);
        auditLog.setStatus("SUCCESS");
        auditLog.setDetails("Provider: " + provider);
        auditLog.setIpAddress(getClientIp());

        auditLogRepository.save(auditLog);
        log.info("[Audit] User {} created credential {} ({})", userId, credentialId, provider);
    }

    /**
     * Log credential deletion.
     */
    public void logCredentialDeletion(UUID userId, UUID credentialId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setAction("CREDENTIAL_DELETED");
        auditLog.setResourceType("CREDENTIAL");
        auditLog.setResourceId(credentialId);
        auditLog.setStatus("SUCCESS");
        auditLog.setIpAddress(getClientIp());

        auditLogRepository.save(auditLog);
        log.info("[Audit] User {} deleted credential {}", userId, credentialId);
    }

    /**
     * Log resource access.
     */
    public void logResourceAccess(UUID userId, UUID resourceId, String action) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setAction(action);
        auditLog.setResourceType("RESOURCE");
        auditLog.setResourceId(resourceId);
        auditLog.setStatus("SUCCESS");
        auditLog.setIpAddress(getClientIp());

        auditLogRepository.save(auditLog);
    }

    /**
     * Log authentication attempt.
     */
    public void logAuthenticationAttempt(String username, boolean success, String method) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("LOGIN");
        auditLog.setStatus(success ? "SUCCESS" : "FAILED");
        auditLog.setDetails("Method: " + method + ", Username: " + username);
        auditLog.setIpAddress(getClientIp());

        auditLogRepository.save(auditLog);
        log.info("[Audit] Login attempt for {} - {}", username, success ? "SUCCESS" : "FAILED");
    }

    /**
     * Log unauthorized access attempt.
     */
    public void logUnauthorizedAccess(UUID userId, String resource, String action) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setAction(action);
        auditLog.setStatus("FAILED");
        auditLog.setDetails("Unauthorized access to: " + resource);
        auditLog.setIpAddress(getClientIp());

        auditLogRepository.save(auditLog);
        log.warn("[Audit] Unauthorized access attempt by user {} to {}", userId, resource);
    }

    /**
     * Get client IP address from request.
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");

                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }

                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not get client IP", e);
        }

        return "unknown";
    }
}
