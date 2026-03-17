package com.seguradora.hibrida.cqrs.monitoring;

import com.seguradora.hibrida.eventstore.repository.EventStoreRepository;
import com.seguradora.hibrida.projection.tracking.ProjectionStatus;
import com.seguradora.hibrida.projection.tracking.ProjectionTracker;
import com.seguradora.hibrida.projection.tracking.ProjectionTrackerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para {@link CQRSHealthIndicator} (pacote monitoring).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CQRSHealthIndicator (monitoring) Tests")
class CQRSHealthIndicatorTest {

    @Mock
    private EventStoreRepository eventStoreRepository;

    @Mock
    private ProjectionTrackerRepository projectionTrackerRepository;

    private CQRSHealthIndicator indicator;

    @BeforeEach
    void setUp() {
        indicator = new CQRSHealthIndicator(eventStoreRepository, projectionTrackerRepository);
    }

    // =========================================================================
    // Anotações e hierarquia
    // =========================================================================

    @Nested
    @DisplayName("Anotações e hierarquia")
    class AnotacoesEHierarquia {

        @Test
        @DisplayName("Deve estar anotado com @Component(\"cqrsHealth\")")
        void shouldBeAnnotatedWithComponentNamedCqrsHealth() {
            Component component = CQRSHealthIndicator.class.getAnnotation(Component.class);
            assertThat(component).isNotNull();
            assertThat(component.value()).isEqualTo("cqrsHealth");
        }

        @Test
        @DisplayName("Deve implementar HealthIndicator")
        void shouldImplementHealthIndicator() {
            assertThat(indicator).isInstanceOf(HealthIndicator.class);
        }
    }

    // =========================================================================
    // health() — lag saudável (≤ 100)
    // =========================================================================

    @Nested
    @DisplayName("health() — lag saudável")
    class LagSaudavel {

        @Test
        @DisplayName("Deve retornar UP com lag zero")
        void shouldReturnUpWithZeroLag() {
            // Given
            when(eventStoreRepository.count()).thenReturn(100L);
            when(projectionTrackerRepository.findAll()).thenReturn(
                    List.of(buildTracker("p", 100L)));
            when(projectionTrackerRepository.count()).thenReturn(1L);

            // When
            Health health = indicator.health();

            // Then
            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails().get("lag")).isEqualTo(0L);
        }

        @Test
        @DisplayName("Deve reportar status HEALTHY nos detalhes")
        void shouldReportHealthyStatus() {
            when(eventStoreRepository.count()).thenReturn(50L);
            when(projectionTrackerRepository.findAll()).thenReturn(
                    List.of(buildTracker("p", 48L)));
            when(projectionTrackerRepository.count()).thenReturn(1L);

            Health health = indicator.health();

            assertThat(health.getDetails().get("status")).isEqualTo("HEALTHY");
        }

        @Test
        @DisplayName("Deve incluir command-side-events, query-side-events e lag nos detalhes")
        void shouldIncludeAllExpectedDetails() {
            when(eventStoreRepository.count()).thenReturn(30L);
            when(projectionTrackerRepository.findAll()).thenReturn(
                    List.of(buildTracker("p", 28L)));
            when(projectionTrackerRepository.count()).thenReturn(1L);

            Health health = indicator.health();

            assertThat(health.getDetails()).containsKeys(
                    "command-side-events", "query-side-events", "lag",
                    "lag-percentage", "status", "projections-count");
        }
    }

    // =========================================================================
    // health() — lag alto (100 < lag ≤ 1000 → WARNING)
    // =========================================================================

    @Nested
    @DisplayName("health() — lag alto (WARNING)")
    class LagAlto {

        @Test
        @DisplayName("Deve retornar WARNING quando lag está entre 100 e 1000")
        void shouldReturnWarningWhenLagIsHigh() {
            // Given – lag = 300
            when(eventStoreRepository.count()).thenReturn(400L);
            when(projectionTrackerRepository.findAll()).thenReturn(
                    List.of(buildTracker("p", 100L)));
            when(projectionTrackerRepository.count()).thenReturn(1L);

            Health health = indicator.health();

            assertThat(health.getStatus().getCode()).isEqualTo("WARNING");
            assertThat(health.getDetails().get("status")).isEqualTo("HIGH_LAG");
        }
    }

    // =========================================================================
    // health() — lag crítico (> 1000 → DOWN)
    // =========================================================================

    @Nested
    @DisplayName("health() — lag crítico (DOWN)")
    class LagCritico {

        @Test
        @DisplayName("Deve retornar DOWN quando lag ultrapassa 1000")
        void shouldReturnDownWhenLagIsCritical() {
            // Given – lag = 1500
            when(eventStoreRepository.count()).thenReturn(1600L);
            when(projectionTrackerRepository.findAll()).thenReturn(
                    List.of(buildTracker("p", 100L)));
            when(projectionTrackerRepository.count()).thenReturn(1L);

            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails().get("status")).isEqualTo("CRITICAL_LAG");
        }

        @Test
        @DisplayName("Deve reportar lag correto nos detalhes")
        void shouldReportCorrectLagValue() {
            when(eventStoreRepository.count()).thenReturn(2000L);
            when(projectionTrackerRepository.findAll()).thenReturn(
                    List.of(buildTracker("p", 500L)));
            when(projectionTrackerRepository.count()).thenReturn(1L);

            Health health = indicator.health();

            assertThat(health.getDetails().get("lag")).isEqualTo(1500L);
        }
    }

    // =========================================================================
    // health() — sem projeções
    // =========================================================================

    @Test
    @DisplayName("Deve retornar UP com lag igual ao total de eventos quando sem projeções")
    void shouldReturnUpWithTotalLagWhenNoProjections() {
        when(eventStoreRepository.count()).thenReturn(50L);
        when(projectionTrackerRepository.findAll()).thenReturn(List.of());
        when(projectionTrackerRepository.count()).thenReturn(0L);

        Health health = indicator.health();

        // lag = 50 - 0 = 50, abaixo do threshold de WARNING (100)
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    // =========================================================================
    // health() — exceção
    // =========================================================================

    @Test
    @DisplayName("Deve retornar DOWN e incluir mensagem de erro quando repositório lança exceção")
    void shouldReturnDownWhenRepositoryThrows() {
        when(eventStoreRepository.count()).thenThrow(new RuntimeException("Conexão perdida"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error");
    }

    // =========================================================================
    // lag-percentage
    // =========================================================================

    @Test
    @DisplayName("lag-percentage deve ser 0.0 quando total de eventos é zero")
    void lagPercentageShouldBeZeroWhenTotalEventsIsZero() {
        when(eventStoreRepository.count()).thenReturn(0L);
        when(projectionTrackerRepository.findAll()).thenReturn(List.of());
        when(projectionTrackerRepository.count()).thenReturn(0L);

        Health health = indicator.health();

        assertThat(health.getDetails().get("lag-percentage")).isEqualTo(0.0);
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private ProjectionTracker buildTracker(String name, Long lastProcessed) {
        ProjectionTracker tracker = new ProjectionTracker(name);
        tracker.setLastProcessedEventId(lastProcessed);
        tracker.setStatus(ProjectionStatus.ACTIVE);
        tracker.setEventsProcessed(0L);
        tracker.setEventsFailed(0L);
        tracker.setUpdatedAt(Instant.now());
        return tracker;
    }
}
