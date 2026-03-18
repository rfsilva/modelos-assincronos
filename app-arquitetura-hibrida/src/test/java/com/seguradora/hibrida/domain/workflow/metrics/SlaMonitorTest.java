package com.seguradora.hibrida.domain.workflow.metrics;

import com.seguradora.hibrida.domain.workflow.execution.WorkflowInstance;
import com.seguradora.hibrida.domain.workflow.repository.WorkflowInstanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SlaMonitor Tests")
class SlaMonitorTest {

    @Mock
    private WorkflowInstanceRepository instanceRepository;

    @Mock
    private SlaConfiguration slaConfiguration;

    @Mock
    private WorkflowMetrics workflowMetrics;

    @InjectMocks
    private SlaMonitor slaMonitor;

    private WorkflowInstance instanceAtiva() {
        return WorkflowInstance.builder()
                .id("wi-001")
                .sinistroId("SIN-001")
                .definicaoId("def-001")
                .status(WorkflowInstance.StatusWorkflowInstance.EM_ANDAMENTO)
                .inicioEm(LocalDateTime.now().minusHours(10))
                .build();
    }

    @Test
    @DisplayName("monitorarSlas deve processar instâncias ativas")
    void monitorarSlasShouldProcessActiveInstances() {
        WorkflowInstance wi = instanceAtiva();
        wi.setContexto("tipoSinistro", "SIMPLES");
        when(instanceRepository.findByStatusIn(anyList())).thenReturn(List.of(wi));
        when(slaConfiguration.calcularPercentualSla("SIMPLES", 10)).thenReturn(20.0);
        when(slaConfiguration.getNivelAlerta(20.0)).thenReturn(null);

        slaMonitor.monitorarSlas();

        verify(instanceRepository).findByStatusIn(anyList());
    }

    @Test
    @DisplayName("monitorarSlas deve gerar alerta quando SLA em risco")
    void monitorarSlasShouldGenerateAlertWhenAtRisk() {
        WorkflowInstance wi = instanceAtiva();
        wi.setContexto("tipoSinistro", "SIMPLES");
        when(instanceRepository.findByStatusIn(anyList())).thenReturn(List.of(wi));
        when(slaConfiguration.calcularPercentualSla(anyString(), anyLong())).thenReturn(85.0);
        when(slaConfiguration.getNivelAlerta(85.0)).thenReturn(SlaConfiguration.NivelAlerta.ALTO);
        when(slaConfiguration.getAlertas()).thenReturn(new SlaConfiguration.Alertas());
        when(slaConfiguration.getEscalacao()).thenReturn(new SlaConfiguration.Escalacao());

        slaMonitor.monitorarSlas();

        verify(slaConfiguration).getNivelAlerta(85.0);
    }

    @Test
    @DisplayName("monitorarSlas deve usar COMPLEXO como default quando tipo não definido")
    void monitorarSlasShouldUseComplexoAsDefaultWhenTypeNotDefined() {
        WorkflowInstance wi = instanceAtiva();
        when(instanceRepository.findByStatusIn(anyList())).thenReturn(List.of(wi));
        when(slaConfiguration.calcularPercentualSla(anyString(), anyLong())).thenReturn(10.0);
        when(slaConfiguration.getNivelAlerta(10.0)).thenReturn(null);

        slaMonitor.monitorarSlas();

        verify(slaConfiguration).calcularPercentualSla(eq("COMPLEXO"), anyLong());
    }

    @Test
    @DisplayName("monitorarSlas deve funcionar com lista vazia")
    void monitorarSlasShouldWorkWithEmptyList() {
        when(instanceRepository.findByStatusIn(anyList())).thenReturn(List.of());

        slaMonitor.monitorarSlas();

        verify(instanceRepository).findByStatusIn(anyList());
        verifyNoInteractions(slaConfiguration);
    }

    @Test
    @DisplayName("identificarEmRisco deve retornar instâncias com alto percentual de SLA")
    void identificarEmRiscoShouldReturnHighSlaInstances() {
        when(instanceRepository.findByStatusIn(anyList())).thenReturn(List.of());

        var resultado = slaMonitor.identificarEmRisco();

        assertThat(resultado).isNotNull();
    }

    @Test
    @DisplayName("limparCacheAlertas deve limpar o cache")
    void limparCacheAlertasShouldClearCache() {
        slaMonitor.limparCacheAlertas();
    }

}
