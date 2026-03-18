package com.seguradora.hibrida.domain.sinistro.projection;

import com.seguradora.hibrida.domain.sinistro.query.repository.*;
import com.seguradora.hibrida.eventstore.EventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SinistroProjectionRebuilder Tests")
class SinistroProjectionRebuilderTest {

    @Mock
    private EventStore eventStore;

    @Mock
    private SinistroQueryRepository sinistroQueryRepository;

    @Mock
    private SinistroDashboardRepository dashboardRepository;

    @Mock
    private SinistroListRepository listRepository;

    @Mock
    private SinistroDetailRepository detailRepository;

    @Mock
    private SinistroAnalyticsRepository analyticsRepository;

    @Mock
    private SinistroDashboardProjection dashboardProjection;

    @Mock
    private SinistroProjectionHandler mainProjectionHandler;

    private SinistroProjectionRebuilder rebuilder;

    @BeforeEach
    void setUp() {
        rebuilder = new SinistroProjectionRebuilder(
                eventStore,
                sinistroQueryRepository,
                dashboardRepository,
                listRepository,
                detailRepository,
                analyticsRepository,
                dashboardProjection,
                mainProjectionHandler
        );
    }

    @Test
    @DisplayName("rebuildAll deve retornar resultado com sucesso quando sem eventos")
    void rebuildAllShouldReturnSuccessWithNoEvents() {
        when(sinistroQueryRepository.count()).thenReturn(0L);
        when(listRepository.count()).thenReturn(0L);
        when(detailRepository.count()).thenReturn(0L);
        when(dashboardRepository.count()).thenReturn(0L);
        when(analyticsRepository.count()).thenReturn(0L);

        SinistroProjectionRebuilder.RebuildResult result = rebuilder.rebuildAll();

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalEventos()).isEqualTo(0);
        assertThat(result.getEventosProcessados()).isEqualTo(0);
        assertThat(result.getEventosComErro()).isEqualTo(0);
    }

    @Test
    @DisplayName("rebuildAll deve definir startTime e endTime")
    void rebuildAllShouldSetTimestamps() {
        when(sinistroQueryRepository.count()).thenReturn(0L);
        when(listRepository.count()).thenReturn(0L);
        when(detailRepository.count()).thenReturn(0L);
        when(dashboardRepository.count()).thenReturn(0L);
        when(analyticsRepository.count()).thenReturn(0L);

        SinistroProjectionRebuilder.RebuildResult result = rebuilder.rebuildAll();

        assertThat(result.getStartTime()).isNotNull();
        assertThat(result.getEndTime()).isNotNull();
        assertThat(result.getEndTime()).isAfterOrEqualTo(result.getStartTime());
    }

    @Test
    @DisplayName("rebuildAll deve incluir resultado de validação")
    void rebuildAllShouldIncludeValidation() {
        when(sinistroQueryRepository.count()).thenReturn(5L);
        when(listRepository.count()).thenReturn(5L);
        when(detailRepository.count()).thenReturn(5L);
        when(dashboardRepository.count()).thenReturn(3L);
        when(analyticsRepository.count()).thenReturn(2L);

        SinistroProjectionRebuilder.RebuildResult result = rebuilder.rebuildAll();

        assertThat(result.getValidation()).isNotNull();
        assertThat(result.getValidation().getQueryModelCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("rebuildIncremental deve retornar resultado com sucesso")
    void rebuildIncrementalShouldReturnSuccess() {
        SinistroProjectionRebuilder.RebuildResult result = rebuilder.rebuildIncremental(100L);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isIncremental()).isTrue();
        assertThat(result.getFromEventId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("rebuildIncremental deve definir fromEventId correto")
    void rebuildIncrementalShouldSetFromEventId() {
        SinistroProjectionRebuilder.RebuildResult result = rebuilder.rebuildIncremental(500L);

        assertThat(result.getFromEventId()).isEqualTo(500L);
    }

    @Test
    @DisplayName("validarConsistencia deve retornar contagens corretas")
    void validarConsistenciaShouldReturnCorrectCounts() {
        when(sinistroQueryRepository.count()).thenReturn(10L);
        when(listRepository.count()).thenReturn(10L);
        when(detailRepository.count()).thenReturn(10L);
        when(dashboardRepository.count()).thenReturn(3L);
        when(analyticsRepository.count()).thenReturn(1L);

        SinistroProjectionRebuilder.ValidationResult result = rebuilder.validarConsistencia();

        assertThat(result.getQueryModelCount()).isEqualTo(10L);
        assertThat(result.getListViewCount()).isEqualTo(10L);
        assertThat(result.getDetailViewCount()).isEqualTo(10L);
        assertThat(result.getDashboardViewCount()).isEqualTo(3L);
        assertThat(result.getAnalyticsViewCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("validarConsistencia deve gerar warning quando contagens inconsistentes")
    void validarConsistenciaShouldWarnWhenInconsistent() {
        when(sinistroQueryRepository.count()).thenReturn(10L);
        when(listRepository.count()).thenReturn(8L); // diferente de query
        when(detailRepository.count()).thenReturn(9L);
        when(dashboardRepository.count()).thenReturn(3L);
        when(analyticsRepository.count()).thenReturn(1L);

        SinistroProjectionRebuilder.ValidationResult result = rebuilder.validarConsistencia();

        assertThat(result.getWarnings()).isNotEmpty();
    }

    @Test
    @DisplayName("validarConsistencia deve ser válido quando contagens consistentes")
    void validarConsistenciaShouldBeValidWhenConsistent() {
        when(sinistroQueryRepository.count()).thenReturn(10L);
        when(listRepository.count()).thenReturn(10L);
        when(detailRepository.count()).thenReturn(10L);
        when(dashboardRepository.count()).thenReturn(3L);
        when(analyticsRepository.count()).thenReturn(1L);

        SinistroProjectionRebuilder.ValidationResult result = rebuilder.validarConsistencia();

        assertThat(result.isValid()).isTrue();
        assertThat(result.getWarnings()).isEmpty();
    }

    // ==================== RebuildResult inner class ====================

    @Test
    @DisplayName("RebuildResult.getDurationMs deve retornar milissegundos corretos")
    void rebuildResultDurationMsShouldBeCorrect() {
        SinistroProjectionRebuilder.RebuildResult result = new SinistroProjectionRebuilder.RebuildResult();
        Instant start = Instant.ofEpochMilli(1000L);
        Instant end = Instant.ofEpochMilli(2500L);

        result.setStartTime(start);
        result.setEndTime(end);

        assertThat(result.getDurationMs()).isEqualTo(1500L);
    }

    @Test
    @DisplayName("RebuildResult.getDurationMs deve retornar 0 quando startTime nulo")
    void rebuildResultDurationMsShouldReturn0WhenNullStart() {
        SinistroProjectionRebuilder.RebuildResult result = new SinistroProjectionRebuilder.RebuildResult();
        assertThat(result.getDurationMs()).isEqualTo(0L);
    }

    @Test
    @DisplayName("RebuildResult.addError deve adicionar detalhe de erro")
    void rebuildResultAddErrorShouldAddDetail() {
        SinistroProjectionRebuilder.RebuildResult result = new SinistroProjectionRebuilder.RebuildResult();
        result.addError("event-123", "Falha no processamento");

        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getEventId()).isEqualTo("event-123");
        assertThat(result.getErrors().get(0).getMessage()).isEqualTo("Falha no processamento");
    }

    @Test
    @DisplayName("RebuildResult.toString deve conter informações essenciais")
    void rebuildResultToStringShouldContainInfo() {
        SinistroProjectionRebuilder.RebuildResult result = new SinistroProjectionRebuilder.RebuildResult();
        result.setSuccess(true);
        result.setTotalEventos(100);
        result.setEventosProcessados(100);
        result.setEventosComErro(0);
        result.setStartTime(Instant.now());
        result.setEndTime(Instant.now());

        String str = result.toString();

        assertThat(str).contains("success=true");
        assertThat(str).contains("total=100");
    }

    @Test
    @DisplayName("ValidationResult.addWarning deve adicionar aviso")
    void validationResultAddWarningShouldAddWarning() {
        SinistroProjectionRebuilder.ValidationResult result = new SinistroProjectionRebuilder.ValidationResult();
        result.addWarning("Inconsistência detectada");

        assertThat(result.getWarnings()).containsExactly("Inconsistência detectada");
    }

    @Test
    @DisplayName("ValidationResult.addError deve adicionar erro")
    void validationResultAddErrorShouldAddError() {
        SinistroProjectionRebuilder.ValidationResult result = new SinistroProjectionRebuilder.ValidationResult();
        result.addError("Erro crítico de validação");

        assertThat(result.getErrors()).containsExactly("Erro crítico de validação");
    }

    @Test
    @DisplayName("ErrorDetail deve ter eventId, message e timestamp")
    void errorDetailShouldHaveFields() {
        SinistroProjectionRebuilder.ErrorDetail detail =
                new SinistroProjectionRebuilder.ErrorDetail("ev-001", "Mensagem de erro");

        assertThat(detail.getEventId()).isEqualTo("ev-001");
        assertThat(detail.getMessage()).isEqualTo("Mensagem de erro");
        assertThat(detail.getTimestamp()).isNotNull();
    }
}
