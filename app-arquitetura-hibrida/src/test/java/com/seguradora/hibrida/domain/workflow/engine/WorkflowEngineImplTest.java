package com.seguradora.hibrida.domain.workflow.engine;

import com.seguradora.hibrida.domain.workflow.execution.WorkflowInstance;
import com.seguradora.hibrida.domain.workflow.model.*;
import com.seguradora.hibrida.domain.workflow.repository.WorkflowDefinitionRepository;
import com.seguradora.hibrida.domain.workflow.repository.WorkflowInstanceRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowEngineImpl Tests")
class WorkflowEngineImplTest {

    @Mock
    private WorkflowDefinitionRepository definitionRepository;

    @Mock
    private WorkflowInstanceRepository instanceRepository;

    @Mock
    private WorkflowExecutor executor;

    private WorkflowEngineImpl engine;

    private WorkflowDefinition definicaoSimples() {
        return WorkflowDefinition.builder()
                .id("def-001")
                .nome("Workflow Simples")
                .versao(1)
                .tipoSinistro("SIMPLES")
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .etapas(List.of(
                        EtapaWorkflow.builder()
                                .id("et-01").nome("Validação").tipo(TipoEtapa.AUTOMATICA).ordem(1).build()
                ))
                .build();
    }

    private WorkflowInstance instanceEmAndamento() {
        return WorkflowInstance.builder()
                .id("wi-001")
                .definicaoId("def-001")
                .sinistroId("SIN-001")
                .status(WorkflowInstance.StatusWorkflowInstance.EM_ANDAMENTO)
                .inicioEm(LocalDateTime.now())
                .build();
    }

    @BeforeEach
    void setUp() {
        engine = new WorkflowEngineImpl(definitionRepository, instanceRepository, executor,
                new SimpleMeterRegistry());
    }

    // =========================================================================
    // iniciar
    // =========================================================================

    @Nested
    @DisplayName("iniciar()")
    class Iniciar {

        @Test
        @DisplayName("Deve lançar exceção quando não há definição ativa")
        void shouldThrowWhenNoActiveDefinition() {
            when(definitionRepository.findByTipoSinistroAndAtivoTrue("SIMPLES"))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> engine.iniciar("SIN-001", "SIMPLES"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Nenhuma definição");
        }

        @Test
        @DisplayName("Deve criar instância e salvar quando definição encontrada")
        void shouldCreateAndSaveInstanceWhenDefinitionFound() {
            WorkflowDefinition def = definicaoSimples();
            when(definitionRepository.findByTipoSinistroAndAtivoTrue("SIMPLES"))
                    .thenReturn(List.of(def));

            WorkflowInstance saved = instanceEmAndamento();
            when(instanceRepository.save(any())).thenReturn(saved);

            WorkflowInstance result = engine.iniciar("SIN-001", "SIMPLES");

            assertThat(result).isNotNull();
            verify(instanceRepository, atLeastOnce()).save(any());
        }
    }

    // =========================================================================
    // cancelar
    // =========================================================================

    @Nested
    @DisplayName("cancelar()")
    class Cancelar {

        @Test
        @DisplayName("Deve lançar exceção quando instância não encontrada")
        void shouldThrowWhenInstanceNotFound() {
            when(instanceRepository.findById("inexistente")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> engine.cancelar("inexistente", "Motivo"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve cancelar instância existente")
        void shouldCancelExistingInstance() {
            WorkflowInstance wi = instanceEmAndamento();
            when(instanceRepository.findById("wi-001")).thenReturn(Optional.of(wi));
            when(instanceRepository.save(any())).thenReturn(wi);

            engine.cancelar("wi-001", "Cancelamento solicitado");

            assertThat(wi.isCancelado()).isTrue();
            verify(instanceRepository).save(wi);
        }
    }

    // =========================================================================
    // pausar
    // =========================================================================

    @Nested
    @DisplayName("pausar()")
    class Pausar {

        @Test
        @DisplayName("Deve pausar instância em andamento")
        void shouldPauseInProgressInstance() {
            WorkflowInstance wi = instanceEmAndamento();
            when(instanceRepository.findById("wi-001")).thenReturn(Optional.of(wi));
            when(instanceRepository.save(any())).thenReturn(wi);

            engine.pausar("wi-001");

            assertThat(wi.isPausado()).isTrue();
        }
    }

    // =========================================================================
    // retomar
    // =========================================================================

    @Nested
    @DisplayName("retomar()")
    class Retomar {

        @Test
        @DisplayName("Deve retomar instância pausada")
        void shouldResumeFromPaused() {
            WorkflowInstance wi = instanceEmAndamento();
            wi.pausar();
            WorkflowDefinition def = definicaoSimples();
            when(instanceRepository.findById("wi-001")).thenReturn(Optional.of(wi));
            when(instanceRepository.save(any())).thenReturn(wi);
            when(definitionRepository.findById("def-001")).thenReturn(Optional.of(def));

            engine.retomar("wi-001");

            assertThat(wi.isEmAndamento()).isTrue();
        }
    }

    // =========================================================================
    // obterStatus
    // =========================================================================

    @Nested
    @DisplayName("obterStatus()")
    class ObterStatus {

        @Test
        @DisplayName("Deve lançar exceção quando instância não encontrada")
        void shouldThrowWhenNotFound() {
            when(instanceRepository.findById("inexistente")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> engine.obterStatus("inexistente"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve retornar instância com status EM_ANDAMENTO")
        void shouldReturnInstanceStatus() {
            WorkflowInstance wi = instanceEmAndamento();
            when(instanceRepository.findById("wi-001")).thenReturn(Optional.of(wi));

            WorkflowInstance result = engine.obterStatus("wi-001");

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(WorkflowInstance.StatusWorkflowInstance.EM_ANDAMENTO);
        }
    }

    // =========================================================================
    // obterHistorico
    // =========================================================================

    @Test
    @DisplayName("obterHistorico deve retornar histórico da instância")
    void obterHistoricoShouldReturnHistory() {
        WorkflowInstance wi = instanceEmAndamento();
        when(instanceRepository.findById("wi-001")).thenReturn(Optional.of(wi));

        var historico = engine.obterHistorico("wi-001");
        assertThat(historico).isNotNull();
    }

    // =========================================================================
    // listarPorSinistro
    // =========================================================================

    @Test
    @DisplayName("listarPorSinistro deve delegar ao repository")
    void listarPorSinistroShouldDelegateToRepository() {
        when(instanceRepository.findBySinistroId("SIN-001")).thenReturn(List.of());

        var result = engine.listarPorSinistro("SIN-001");

        assertThat(result).isEmpty();
        verify(instanceRepository).findBySinistroId("SIN-001");
    }
}
