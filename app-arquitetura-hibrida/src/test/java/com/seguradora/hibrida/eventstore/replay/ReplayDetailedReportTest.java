package com.seguradora.hibrida.eventstore.replay;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link ReplayDetailedReport}.
 */
@DisplayName("ReplayDetailedReport Tests")
class ReplayDetailedReportTest {

    @Test
    @DisplayName("Builder deve criar instância com defaults")
    void builderShouldCreateInstanceWithDefaults() {
        ReplayDetailedReport report = ReplayDetailedReport.builder().build();

        assertThat(report.getGeneratedAt()).isNotNull();
        assertThat(report.getBatchDetails()).isEmpty();
        assertThat(report.getEventTypeAnalysis()).isEmpty();
        assertThat(report.getHandlerAnalysis()).isEmpty();
        assertThat(report.getFailedEvents()).isEmpty();
        assertThat(report.getSkippedEvents()).isEmpty();
        assertThat(report.getRecommendations()).isEmpty();
    }

    @Test
    @DisplayName("Builder deve aceitar executiveSummary")
    void builderShouldAcceptExecutiveSummary() {
        ReplayDetailedReport report = ReplayDetailedReport.builder()
                .executiveSummary("Replay concluído com sucesso")
                .build();

        assertThat(report.getExecutiveSummary()).isEqualTo("Replay concluído com sucesso");
    }

    @Test
    @DisplayName("Builder deve aceitar recommendations")
    void builderShouldAcceptRecommendations() {
        ReplayDetailedReport report = ReplayDetailedReport.builder()
                .recommendations(List.of("Rec1", "Rec2"))
                .build();

        assertThat(report.getRecommendations()).containsExactly("Rec1", "Rec2");
    }

    @Test
    @DisplayName("BatchProcessingDetail deve ter classe interna pública")
    void batchProcessingDetailShouldBePublicInnerClass() {
        assertThat(ReplayDetailedReport.BatchProcessingDetail.class.getEnclosingClass())
                .isEqualTo(ReplayDetailedReport.class);
    }

    @Test
    @DisplayName("BatchProcessingDetail builder deve funcionar")
    void batchProcessingDetailBuilderShouldWork() {
        ReplayDetailedReport.BatchProcessingDetail detail =
                ReplayDetailedReport.BatchProcessingDetail.builder()
                        .batchNumber(1)
                        .eventsInBatch(50)
                        .successfulEvents(48)
                        .failedEvents(2)
                        .build();

        assertThat(detail.getBatchNumber()).isEqualTo(1);
        assertThat(detail.getEventsInBatch()).isEqualTo(50);
        assertThat(detail.getSuccessfulEvents()).isEqualTo(48);
        assertThat(detail.getFailedEvents()).isEqualTo(2);
    }

    @Test
    @DisplayName("EventTypeAnalysis deve ter classe interna pública")
    void eventTypeAnalysisShouldBePublicInnerClass() {
        assertThat(ReplayDetailedReport.EventTypeAnalysis.class.getEnclosingClass())
                .isEqualTo(ReplayDetailedReport.class);
    }

    @Test
    @DisplayName("HandlerAnalysis deve ter classe interna pública")
    void handlerAnalysisShouldBePublicInnerClass() {
        assertThat(ReplayDetailedReport.HandlerAnalysis.class.getEnclosingClass())
                .isEqualTo(ReplayDetailedReport.class);
    }

    @Test
    @DisplayName("FailedEventDetail builder deve funcionar")
    void failedEventDetailBuilderShouldWork() {
        ReplayDetailedReport.FailedEventDetail detail =
                ReplayDetailedReport.FailedEventDetail.builder()
                        .eventId("evt-1")
                        .eventType("SinistroCriado")
                        .errorMessage("erro de teste")
                        .attemptNumber(1)
                        .build();

        assertThat(detail.getEventId()).isEqualTo("evt-1");
        assertThat(detail.getEventType()).isEqualTo("SinistroCriado");
        assertThat(detail.getErrorMessage()).isEqualTo("erro de teste");
    }

    @Test
    @DisplayName("SkippedEventDetail builder deve funcionar")
    void skippedEventDetailBuilderShouldWork() {
        ReplayDetailedReport.SkippedEventDetail detail =
                ReplayDetailedReport.SkippedEventDetail.builder()
                        .eventId("evt-2")
                        .skipReason("filtro aplicado")
                        .build();

        assertThat(detail.getEventId()).isEqualTo("evt-2");
        assertThat(detail.getSkipReason()).isEqualTo("filtro aplicado");
    }

    @Test
    @DisplayName("StateComparisonReport deve ter classe interna pública")
    void stateComparisonReportShouldBePublicInnerClass() {
        assertThat(ReplayDetailedReport.StateComparisonReport.class.getEnclosingClass())
                .isEqualTo(ReplayDetailedReport.class);
    }
}
