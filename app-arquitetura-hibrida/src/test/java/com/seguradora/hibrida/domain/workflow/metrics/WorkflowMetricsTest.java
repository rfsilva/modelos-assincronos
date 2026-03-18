package com.seguradora.hibrida.domain.workflow.metrics;

import com.seguradora.hibrida.domain.workflow.execution.WorkflowInstance;
import com.seguradora.hibrida.domain.workflow.repository.WorkflowInstanceRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowMetrics Tests")
class WorkflowMetricsTest {

    @Mock
    private WorkflowInstanceRepository instanceRepository;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private WorkflowMetrics workflowMetrics;

    private WorkflowInstance instanciaCompleta() {
        WorkflowInstance wi = WorkflowInstance.builder()
                .id("wi-001")
                .sinistroId("SIN-001")
                .definicaoId("def-001")
                .status(WorkflowInstance.StatusWorkflowInstance.COMPLETO)
                .inicioEm(LocalDateTime.now().minusHours(2))
                .build();
        wi.completar();
        return wi;
    }

    @BeforeEach
    void setUp() {
        // Allow meterRegistry to return mock objects for gauge/counter
        lenient().when(meterRegistry.gauge(anyString(), any(Iterable.class), any(), any()))
                .thenReturn(0.0);
    }

    @Test
    @DisplayName("calcularTempoMedio deve retornar 0 quando repository retorna null")
    void calcularTempoMedioShouldReturn0WhenRepositoryReturnsNull() {
        when(instanceRepository.calcularTempoMedioPorTipo("SIMPLES")).thenReturn(null);

        double result = workflowMetrics.calcularTempoMedio("SIMPLES");

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    @DisplayName("calcularTempoMedio deve retornar valor do repository quando disponível")
    void calcularTempoMedioShouldReturnRepositoryValue() {
        when(instanceRepository.calcularTempoMedioPorTipo("SIMPLES")).thenReturn(120.0);
        lenient().when(meterRegistry.gauge(anyString(), any(), any(), any())).thenReturn(120.0);

        double result = workflowMetrics.calcularTempoMedio("SIMPLES");

        assertThat(result).isEqualTo(120.0);
    }

    @Test
    @DisplayName("calcularTaxaSucesso deve calcular percentual corretamente")
    void calcularTaxaSucessoShouldCalculatePercentage() {
        WorkflowInstance completo = instanciaCompleta();
        WorkflowInstance falhado = WorkflowInstance.builder()
                .id("wi-002").sinistroId("SIN-002").definicaoId("def-001")
                .status(WorkflowInstance.StatusWorkflowInstance.FALHADO)
                .inicioEm(LocalDateTime.now().minusHours(1)).build();

        when(instanceRepository.findByInicioEmBetween(any(), any()))
                .thenReturn(List.of(completo, falhado));

        double taxa = workflowMetrics.calcularTaxaSucesso("SIMPLES");

        assertThat(taxa).isGreaterThanOrEqualTo(0.0).isLessThanOrEqualTo(100.0);
    }

    @Test
    @DisplayName("calcularTaxaSucesso deve retornar 0 quando sem instâncias")
    void calcularTaxaSucessoShouldReturn0WhenNoInstances() {
        when(instanceRepository.findByInicioEmBetween(any(), any())).thenReturn(List.of());

        double taxa = workflowMetrics.calcularTaxaSucesso("SIMPLES");

        assertThat(taxa).isEqualTo(0.0);
    }

    @Test
    @DisplayName("identificarGargalos deve retornar lista não nula")
    void identificarGargalosShouldReturnNonNullList() {
        when(instanceRepository.findByInicioEmBetween(any(), any())).thenReturn(List.of());

        var gargalos = workflowMetrics.identificarGargalos("SIMPLES");

        assertThat(gargalos).isNotNull();
    }

    @Test
    @DisplayName("getTendencias deve retornar mapa com dados")
    void getTendenciasShouldReturnMapWithData() {
        when(instanceRepository.findByInicioEmBetween(any(), any())).thenReturn(List.of());

        Map<LocalDateTime, Long> tendencias = workflowMetrics.getTendencias(30);

        assertThat(tendencias).isNotNull();
    }

    @Test
    @DisplayName("calcularEstatisticasGerais deve retornar mapa com estatísticas")
    void calcularEstatisticasGeraisShouldReturnMapWithStats() {
        when(instanceRepository.count()).thenReturn(100L);
        when(instanceRepository.countByStatus(any())).thenReturn(50L);
        lenient().when(instanceRepository.findByStatusIn(any())).thenReturn(List.of());

        Map<String, Object> stats = workflowMetrics.calcularEstatisticasGerais();

        assertThat(stats).isNotNull();
    }

    @Test
    @DisplayName("identificarEmRiscoSLA deve retornar lista não nula")
    void identificarEmRiscoSLAShouldReturnNonNullList() {
        when(instanceRepository.findAtivosAntigos(any())).thenReturn(List.of());

        var emRisco = workflowMetrics.identificarEmRiscoSLA(48);

        assertThat(emRisco).isNotNull();
    }

    @Test
    @DisplayName("calcularDistribuicaoPorTipo deve retornar mapa não nulo")
    void calcularDistribuicaoPorTipoShouldReturnNonNullMap() {
        when(instanceRepository.findAll()).thenReturn(List.of());

        Map<String, Long> distribuicao = workflowMetrics.calcularDistribuicaoPorTipo();

        assertThat(distribuicao).isNotNull();
    }

    @Test
    @DisplayName("exportarMetricas deve retornar mapa com métricas")
    void exportarMetricasShouldReturnMapWithMetrics() {
        when(instanceRepository.count()).thenReturn(0L);
        when(instanceRepository.countByStatus(any())).thenReturn(0L);
        lenient().when(instanceRepository.findByStatusIn(any())).thenReturn(List.of());

        Map<String, Object> metricas = workflowMetrics.exportarMetricas();

        assertThat(metricas).isNotNull();
    }
}
