package com.seguradora.hibrida.cqrs.monitoring;

import com.seguradora.hibrida.eventstore.repository.EventStoreRepository;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator para monitorar o lag entre Command e Query sides do CQRS.
 *
 * <p>Calcula e reporta:
 * <ul>
 *   <li>Total de eventos no Command Side (Event Store)</li>
 *   <li>Total de eventos processados no Query Side (Projections)</li>
 *   <li>Lag atual entre os lados</li>
 *   <li>Status de saúde baseado no lag</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component("cqrsHealth")
@RequiredArgsConstructor
@Slf4j
public class CQRSHealthIndicator implements HealthIndicator {

    private final EventStoreRepository eventStoreRepository;
    private final ProjectionTrackerRepository projectionTrackerRepository;

    // Thresholds para determinar saúde
    private static final long LAG_WARNING_THRESHOLD = 100;
    private static final long LAG_ERROR_THRESHOLD = 1000;

    @Override
    public Health health() {
        try {
            // Obter total de eventos no Command Side
            long commandSideEvents = eventStoreRepository.count();

            // Obter máximo de eventos processados nas projeções (Query Side)
            Long querySideEvents = projectionTrackerRepository.findAll()
                .stream()
                .mapToLong(tracker -> tracker.getLastProcessedEventId() != null ? tracker.getLastProcessedEventId() : 0L)
                .max()
                .orElse(0L);

            // Calcular lag
            long lag = commandSideEvents - querySideEvents;

            // Determinar status baseado no lag
            Health.Builder builder;
            String status;

            if (lag > LAG_ERROR_THRESHOLD) {
                builder = Health.down();
                status = "CRITICAL_LAG";
            } else if (lag > LAG_WARNING_THRESHOLD) {
                builder = Health.status("WARNING");
                status = "HIGH_LAG";
            } else {
                builder = Health.up();
                status = "HEALTHY";
            }

            return builder
                .withDetail("command-side-events", commandSideEvents)
                .withDetail("query-side-events", querySideEvents)
                .withDetail("lag", lag)
                .withDetail("lag-percentage", calculateLagPercentage(lag, commandSideEvents))
                .withDetail("status", status)
                .withDetail("lag-warning-threshold", LAG_WARNING_THRESHOLD)
                .withDetail("lag-error-threshold", LAG_ERROR_THRESHOLD)
                .withDetail("projections-count", projectionTrackerRepository.count())
                .build();

        } catch (Exception e) {
            log.error("Failed to check CQRS health", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("status", "HEALTH_CHECK_FAILED")
                .build();
        }
    }

    /**
     * Calcula a porcentagem de lag em relação ao total de eventos.
     */
    private double calculateLagPercentage(long lag, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round((lag * 100.0 / total) * 100.0) / 100.0;
    }
}
