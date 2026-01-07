package app.cmesh.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for recording application metrics using Micrometer.
 */
@Service
@Slf4j
public class MetricsService {

    private final MeterRegistry registry;

    // Gauges for current state
    private final AtomicInteger activeCredentialsGauge;
    private final AtomicInteger totalResourcesGauge;
    private final AtomicInteger totalProjectsGauge;

    public MetricsService(MeterRegistry registry) {
        this.registry = registry;

        // Initialize gauges
        this.activeCredentialsGauge = registry.gauge(
                "cloudmesh.credentials.active",
                new AtomicInteger(0));

        this.totalResourcesGauge = registry.gauge(
                "cloudmesh.resources.total",
                new AtomicInteger(0));

        this.totalProjectsGauge = registry.gauge(
                "cloudmesh.projects.total",
                new AtomicInteger(0));
    }

    /**
     * Record successful resource sync.
     */
    public void recordResourceSyncSuccess(String provider) {
        Counter.builder("cloudmesh.sync.success")
                .tag("provider", provider)
                .description("Successful resource syncs")
                .register(registry)
                .increment();
    }

    /**
     * Record failed resource sync.
     */
    public void recordResourceSyncFailure(String provider) {
        Counter.builder("cloudmesh.sync.failure")
                .tag("provider", provider)
                .description("Failed resource syncs")
                .register(registry)
                .increment();
    }

    /**
     * Record resource sync duration.
     */
    public void recordSyncDuration(String provider, long durationMs) {
        Timer.builder("cloudmesh.sync.duration")
                .tag("provider", provider)
                .description("Resource sync duration")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Record successful cost sync.
     */
    public void recordCostSyncSuccess() {
        Counter.builder("cloudmesh.cost.sync.success")
                .description("Successful cost syncs")
                .register(registry)
                .increment();
    }

    /**
     * Record failed cost sync.
     */
    public void recordCostSyncFailure() {
        Counter.builder("cloudmesh.cost.sync.failure")
                .description("Failed cost syncs")
                .register(registry)
                .increment();
    }

    /**
     * Record cost sync duration.
     */
    public void recordCostSyncDuration(long durationMs) {
        Timer.builder("cloudmesh.cost.sync.duration")
                .description("Cost sync duration")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Record credential creation.
     */
    public void recordCredentialCreation(String provider) {
        Counter.builder("cloudmesh.credentials.created")
                .tag("provider", provider)
                .description("Credentials created")
                .register(registry)
                .increment();
    }

    /**
     * Record audit event.
     */
    public void recordAuditEvent(String action, String status) {
        Counter.builder("cloudmesh.audit.events")
                .tag("action", action)
                .tag("status", status)
                .description("Audit events")
                .register(registry)
                .increment();
    }

    /**
     * Update active credentials count.
     */
    public void updateActiveCredentials(int count) {
        activeCredentialsGauge.set(count);
    }

    /**
     * Update total resources count.
     */
    public void updateTotalResources(int count) {
        totalResourcesGauge.set(count);
    }

    /**
     * Update total projects count.
     */
    public void updateTotalProjects(int count) {
        totalProjectsGauge.set(count);
    }
}
