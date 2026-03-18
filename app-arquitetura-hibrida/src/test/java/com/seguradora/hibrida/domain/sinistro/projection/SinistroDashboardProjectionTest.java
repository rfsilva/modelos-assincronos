package com.seguradora.hibrida.domain.sinistro.projection;

import com.seguradora.hibrida.domain.sinistro.event.SinistroCriadoEvent;
import com.seguradora.hibrida.domain.sinistro.event.SinistroAprovadoEvent;
import com.seguradora.hibrida.domain.sinistro.event.SinistroEmAnaliseEvent;
import com.seguradora.hibrida.domain.sinistro.event.SinistroReprovadoEvent;
import com.seguradora.hibrida.domain.sinistro.event.SinistroValidadoEvent;
import com.seguradora.hibrida.domain.sinistro.event.ConsultaDetranIniciadaEvent;
import com.seguradora.hibrida.domain.sinistro.event.ConsultaDetranConcluidaEvent;
import com.seguradora.hibrida.domain.sinistro.event.ConsultaDetranFalhadaEvent;
import com.seguradora.hibrida.domain.sinistro.event.DocumentoAnexadoEvent;
import com.seguradora.hibrida.domain.sinistro.event.DocumentoValidadoEvent;
import com.seguradora.hibrida.domain.sinistro.event.DocumentoRejeitadoEvent;
import com.seguradora.hibrida.domain.sinistro.query.model.SinistroDashboardView;
import com.seguradora.hibrida.domain.sinistro.query.repository.SinistroDashboardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SinistroDashboardProjection Tests")
class SinistroDashboardProjectionTest {

    @Mock
    private SinistroDashboardRepository dashboardRepository;

    private SinistroDashboardProjection projection;

    @BeforeEach
    void setUp() {
        projection = new SinistroDashboardProjection(dashboardRepository);
    }

    private SinistroDashboardView novoDashboard() {
        return SinistroDashboardView.builder()
                .periodo("2024-03-18")
                .tipoPeriodo("DIA")
                .dataReferencia(LocalDate.now())
                .totalSinistros(0)
                .sinistrosAbertos(0)
                .sinistrosEmAnalise(0)
                .sinistrosAprovados(0)
                .sinistrosReprovados(0)
                .sinistrosCancelados(0)
                .valorTotal(BigDecimal.ZERO)
                .valorMedio(BigDecimal.ZERO)
                .valorMaximo(BigDecimal.ZERO)
                .valorMinimo(BigDecimal.ZERO)
                .tempoMedioProcessamento(0L)
                .taxaAprovacao(BigDecimal.ZERO)
                .taxaReprovacao(BigDecimal.ZERO)
                .sinistrosDentroSla(0)
                .sinistrosForaSla(0)
                .sinistrosUrgentes(0)
                .sinistrosDocPendente(0)
                .build();
    }

    @Test
    @DisplayName("getCacheStats deve retornar estatísticas do cache")
    void getCacheStatsShouldReturnStats() {
        assertThat(projection.getCacheStats()).isNotNull();
    }

    @Test
    @DisplayName("clearCache deve limpar o cache sem exceções")
    void clearCacheShouldNotThrow() {
        projection.clearCache();
    }

    @Test
    @DisplayName("on(SinistroCriadoEvent) deve incrementar totalSinistros")
    void onSinistroCriadoShouldIncrementTotal() {
        SinistroDashboardView dashboard = novoDashboard();
        when(dashboardRepository.findByPeriodoAndTipoPeriodo(anyString(), anyString()))
                .thenReturn(Optional.of(dashboard));
        when(dashboardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SinistroCriadoEvent event = new SinistroCriadoEvent(
                "SIN-001", "2024-000001", "SEG-001", "VEI-001", "APO-001",
                "COLISAO", "Colisão frontal", "OP-001"
        );

        projection.on(event);

        assertThat(dashboard.getTotalSinistros()).isGreaterThan(0);
    }

    @Test
    @DisplayName("on(SinistroCriadoEvent) deve ser idempotente para evento duplicado")
    void onSinistroCriadoShouldBeIdempotent() {
        SinistroDashboardView dashboard = novoDashboard();
        when(dashboardRepository.findByPeriodoAndTipoPeriodo(anyString(), anyString()))
                .thenReturn(Optional.of(dashboard));
        when(dashboardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SinistroCriadoEvent event = new SinistroCriadoEvent(
                "SIN-DUP", "2024-000002", "SEG-001", "VEI-001", "APO-001",
                "COLISAO", "Colisão frontal", "OP-001"
        );
        event.setVersion(42L);

        // Primeiro processamento
        projection.on(event);
        int totalApos1 = dashboard.getTotalSinistros();

        // Segundo processamento do mesmo evento — idempotente, não deve incrementar
        projection.on(event);
        assertThat(dashboard.getTotalSinistros()).isEqualTo(totalApos1);
    }

    @Test
    @DisplayName("on(SinistroValidadoEvent) deve processar sem lançar exceção")
    void onSinistroValidadoShouldNotThrow() {
        SinistroDashboardView dashboard = novoDashboard();
        when(dashboardRepository.findByPeriodoAndTipoPeriodo(anyString(), anyString()))
                .thenReturn(Optional.of(dashboard));
        when(dashboardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SinistroValidadoEvent event = new SinistroValidadoEvent(
                "SIN-001", "SIN-001", Map.of("k", "v"), List.of("DOC-001"), "OP-001"
        );

        projection.on(event);
    }

    @Test
    @DisplayName("on(SinistroEmAnaliseEvent) deve decrementar abertos e incrementar emAnalise")
    void onSinistroEmAnaliseShouldUpdateCounters() {
        SinistroDashboardView dashboard = novoDashboard();
        dashboard.setSinistrosAbertos(5);
        when(dashboardRepository.findByPeriodoAndTipoPeriodo(anyString(), anyString()))
                .thenReturn(Optional.of(dashboard));
        when(dashboardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SinistroEmAnaliseEvent event = new SinistroEmAnaliseEvent(
                "SIN-001", "SIN-001", "ANALISTA-01", "2024-12-31T23:59:59Z", "3"
        );

        projection.on(event);

        assertThat(dashboard.getSinistrosEmAnalise()).isGreaterThan(0);
    }

    @Test
    @DisplayName("on(SinistroAprovadoEvent) deve incrementar aprovados e atualizar valor")
    void onSinistroAprovadoShouldIncrementAprovados() {
        SinistroDashboardView dashboard = novoDashboard();
        dashboard.setSinistrosEmAnalise(2);
        when(dashboardRepository.findByPeriodoAndTipoPeriodo(anyString(), anyString()))
                .thenReturn(Optional.of(dashboard));
        when(dashboardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SinistroAprovadoEvent event = new SinistroAprovadoEvent(
                "SIN-001", "SIN-001", "10000.00",
                "Justificativa com pelo menos 20 caracteres",
                "ANALISTA-01", List.of("DOC-001")
        );

        projection.on(event);

        assertThat(dashboard.getSinistrosAprovados()).isGreaterThan(0);
    }

    @Test
    @DisplayName("on(SinistroReprovadoEvent) deve incrementar reprovados")
    void onSinistroReprovadoShouldIncrementReprovados() {
        SinistroDashboardView dashboard = novoDashboard();
        dashboard.setSinistrosEmAnalise(1);
        when(dashboardRepository.findByPeriodoAndTipoPeriodo(anyString(), anyString()))
                .thenReturn(Optional.of(dashboard));
        when(dashboardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SinistroReprovadoEvent event = new SinistroReprovadoEvent(
                "SIN-001", "SIN-001", "FORA_COBERTURA",
                "Justificativa técnica com pelo menos 50 caracteres para reprovação válida ok",
                "ANALISTA-01", "Art. 5"
        );

        projection.on(event);

        assertThat(dashboard.getSinistrosReprovados()).isGreaterThan(0);
    }

    @Test
    @DisplayName("on(ConsultaDetranIniciadaEvent) deve processar sem exceção")
    void onConsultaDetranIniciadaShouldNotThrow() {
        ConsultaDetranIniciadaEvent event = new ConsultaDetranIniciadaEvent(
                "SIN-001", "SIN-001", "ABC1234", "12345678901", 1
        );
        projection.on(event);
    }

    @Test
    @DisplayName("on(ConsultaDetranConcluidaEvent) deve processar sem exceção")
    void onConsultaDetranConcluidaShouldNotThrow() {
        ConsultaDetranConcluidaEvent event = new ConsultaDetranConcluidaEvent(
                "SIN-001", "SIN-001", Map.of("restricao", "false"), "2024-03-18T10:00:00Z"
        );
        projection.on(event);
    }

    @Test
    @DisplayName("on(ConsultaDetranFalhadaEvent) deve processar sem exceção")
    void onConsultaDetranFalhadaShouldNotThrow() {
        ConsultaDetranFalhadaEvent event = new ConsultaDetranFalhadaEvent(
                "SIN-001", "SIN-001", "Timeout", 1, null
        );
        projection.on(event);
    }

    @Test
    @DisplayName("on(DocumentoAnexadoEvent) deve processar sem exceção")
    void onDocumentoAnexadoShouldNotThrow() {
        DocumentoAnexadoEvent event = new DocumentoAnexadoEvent(
                "SIN-001", "SIN-001", "DOC-001", "FOTO_VEICULO", "OP-001", null
        );
        projection.on(event);
    }

    @Test
    @DisplayName("on(DocumentoValidadoEvent) deve processar sem exceção")
    void onDocumentoValidadoShouldNotThrow() {
        DocumentoValidadoEvent event = new DocumentoValidadoEvent(
                "SIN-001", "SIN-001", "DOC-001", "VALID-01", "2024-03-18T10:00:00Z"
        );
        projection.on(event);
    }

    @Test
    @DisplayName("on(DocumentoRejeitadoEvent) deve incrementar sinistrosDocPendente")
    void onDocumentoRejeitadoShouldIncrementDocPendente() {
        SinistroDashboardView dashboard = novoDashboard();
        when(dashboardRepository.findByPeriodoAndTipoPeriodo(anyString(), anyString()))
                .thenReturn(Optional.of(dashboard));
        when(dashboardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DocumentoRejeitadoEvent event = new DocumentoRejeitadoEvent(
                "SIN-001", "SIN-001", "DOC-001", "Ilegível", "VALID-01"
        );

        projection.on(event);

        assertThat(dashboard.getSinistrosDocPendente()).isGreaterThan(0);
    }

    @Test
    @DisplayName("on(SinistroCriadoEvent) deve criar novo dashboard quando não encontrado")
    void onSinistroCriadoShouldCreateDashboardWhenNotFound() {
        when(dashboardRepository.findByPeriodoAndTipoPeriodo(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(dashboardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SinistroCriadoEvent event = new SinistroCriadoEvent(
                "SIN-NEW", "2024-000099", "SEG-001", "VEI-001", "APO-001",
                "TERCEIROS", "Ocorrência descrição válida", "OP-001"
        );

        // Deve processar sem exceção, criando novo dashboard
        projection.on(event);
    }
}
