package com.seguradora.hibrida.domain.veiculo.relationship.scheduler;

import com.seguradora.hibrida.domain.veiculo.relationship.model.StatusRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.model.VeiculoApoliceRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.repository.VeiculoApoliceRelacionamentoRepository;
import com.seguradora.hibrida.domain.veiculo.relationship.service.RelationshipAlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link RelationshipMonitorScheduler}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RelationshipMonitorScheduler - Testes Unitários")
class RelationshipMonitorSchedulerTest {

    @Mock
    private VeiculoApoliceRelacionamentoRepository relacionamentoRepository;

    @Mock
    private RelationshipAlertService alertService;

    @InjectMocks
    private RelationshipMonitorScheduler scheduler;

    private VeiculoApoliceRelacionamento relacionamentoMock;

    @BeforeEach
    void setUp() {
        relacionamentoMock = new VeiculoApoliceRelacionamento();
        relacionamentoMock.setId("REL-001");
        relacionamentoMock.setVeiculoId("VEI-001");
        relacionamentoMock.setApoliceId("APO-001");
        relacionamentoMock.setVeiculoPlaca("ABC1234");
        relacionamentoMock.setSeguradoCpf("12345678909");
        relacionamentoMock.setSeguradoNome("João Silva");
        relacionamentoMock.setApoliceNumero("2024-001");
        relacionamentoMock.setStatus(StatusRelacionamento.ATIVO);
        relacionamentoMock.setDataInicio(LocalDate.now().minusDays(90));
    }

    @Nested
    @DisplayName("Testes de monitorarVencimentos")
    class MonitorarVencimentosTests {

        @Test
        @DisplayName("Deve monitorar vencimentos com sucesso")
        void deveMonitorarVencimentosComSucesso() {
            when(relacionamentoRepository.findRelacionamentosVencendoAte(any()))
                .thenReturn(Collections.emptyList());

            assertThatCode(() -> scheduler.monitorarVencimentos())
                .doesNotThrowAnyException();

            verify(relacionamentoRepository).findRelacionamentosVencendoAte(any());
        }

        @Test
        @DisplayName("Deve alertar vencimento em 30 dias")
        void deveAlertarVencimentoEm30Dias() {
            relacionamentoMock.setDataFim(LocalDate.now().plusDays(30));

            when(relacionamentoRepository.findRelacionamentosVencendoAte(any()))
                .thenReturn(List.of(relacionamentoMock));

            scheduler.monitorarVencimentos();

            verify(alertService).alertarVencimentoProximo(
                eq("VEI-001"), eq("ABC1234"), eq("12345678909"), eq("João Silva"),
                eq("2024-001"), eq(30)
            );
        }

        @Test
        @DisplayName("Deve alertar vencimento em 15 dias")
        void deveAlertarVencimentoEm15Dias() {
            relacionamentoMock.setDataFim(LocalDate.now().plusDays(15));

            when(relacionamentoRepository.findRelacionamentosVencendoAte(any()))
                .thenReturn(List.of(relacionamentoMock));

            scheduler.monitorarVencimentos();

            verify(alertService).alertarVencimentoProximo(
                anyString(), anyString(), anyString(), anyString(), anyString(), eq(15)
            );
        }

        @Test
        @DisplayName("Deve alertar vencimento em 7 dias")
        void deveAlertarVencimentoEm7Dias() {
            relacionamentoMock.setDataFim(LocalDate.now().plusDays(7));

            when(relacionamentoRepository.findRelacionamentosVencendoAte(any()))
                .thenReturn(List.of(relacionamentoMock));

            scheduler.monitorarVencimentos();

            verify(alertService).alertarVencimentoProximo(
                anyString(), anyString(), anyString(), anyString(), anyString(), eq(7)
            );
        }

        @Test
        @DisplayName("Não deve alertar para dias que não são marcos")
        void naoDeveAlertarParaDiasQueNaoSaoMarcos() {
            relacionamentoMock.setDataFim(LocalDate.now().plusDays(20));

            when(relacionamentoRepository.findRelacionamentosVencendoAte(any()))
                .thenReturn(List.of(relacionamentoMock));

            scheduler.monitorarVencimentos();

            verify(alertService, never()).alertarVencimentoProximo(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyInt()
            );
        }

        @Test
        @DisplayName("Deve continuar em caso de exceção")
        void deveContinuarEmCasoExcecao() {
            when(relacionamentoRepository.findRelacionamentosVencendoAte(any()))
                .thenThrow(new RuntimeException("Erro de conexão"));

            assertThatCode(() -> scheduler.monitorarVencimentos())
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de detectarGapsCobertura")
    class DetectarGapsCoberturaTests {

        @Test
        @DisplayName("Deve detectar gaps com sucesso")
        void deveDetectarGapsComSucesso() {
            when(relacionamentoRepository.findRelacionamentosComGap())
                .thenReturn(Collections.emptyList());

            assertThatCode(() -> scheduler.detectarGapsCobertura())
                .doesNotThrowAnyException();

            verify(relacionamentoRepository).findRelacionamentosComGap();
        }

        @Test
        @DisplayName("Deve alertar sobre gap detectado")
        void deveAlertarSobreGapDetectado() {
            relacionamentoMock.setDataFim(LocalDate.now().minusDays(10));

            when(relacionamentoRepository.findRelacionamentosComGap())
                .thenReturn(List.of(relacionamentoMock));

            scheduler.detectarGapsCobertura();

            verify(alertService).alertarGapCobertura(
                eq("VEI-001"), eq("ABC1234"), eq("12345678909"), eq(10)
            );
        }

        @Test
        @DisplayName("Deve processar múltiplos gaps")
        void deveProcessarMultiplosGaps() {
            VeiculoApoliceRelacionamento gap1 = new VeiculoApoliceRelacionamento();
            gap1.setVeiculoId("VEI-001");
            gap1.setVeiculoPlaca("ABC1234");
            gap1.setSeguradoCpf("12345678909");
            gap1.setDataFim(LocalDate.now().minusDays(5));

            VeiculoApoliceRelacionamento gap2 = new VeiculoApoliceRelacionamento();
            gap2.setVeiculoId("VEI-002");
            gap2.setVeiculoPlaca("XYZ9876");
            gap2.setSeguradoCpf("98765432100");
            gap2.setDataFim(LocalDate.now().minusDays(15));

            when(relacionamentoRepository.findRelacionamentosComGap())
                .thenReturn(List.of(gap1, gap2));

            scheduler.detectarGapsCobertura();

            verify(alertService, times(2)).alertarGapCobertura(
                anyString(), anyString(), anyString(), anyInt()
            );
        }

        @Test
        @DisplayName("Deve continuar em caso de exceção")
        void deveContinuarEmCasoExcecao() {
            when(relacionamentoRepository.findRelacionamentosComGap())
                .thenThrow(new RuntimeException("Erro de conexão"));

            assertThatCode(() -> scheduler.detectarGapsCobertura())
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de monitorarVeiculosSemCobertura")
    class MonitorarVeiculosSemCoberturaTests {

        @Test
        @DisplayName("Deve monitorar veículos sem cobertura com sucesso")
        void deveMonitorarVeiculosSemCoberturaComSucesso() {
            when(relacionamentoRepository.findVeiculosSemCobertura())
                .thenReturn(Collections.emptyList());

            assertThatCode(() -> scheduler.monitorarVeiculosSemCobertura())
                .doesNotThrowAnyException();

            verify(relacionamentoRepository).findVeiculosSemCobertura();
        }

        @Test
        @DisplayName("Deve detectar veículos sem cobertura")
        void deveDetectarVeiculosSemCobertura() {
            when(relacionamentoRepository.findVeiculosSemCobertura())
                .thenReturn(List.of("VEI-001", "VEI-002", "VEI-003"));

            scheduler.monitorarVeiculosSemCobertura();

            verify(relacionamentoRepository).findVeiculosSemCobertura();
        }

        @Test
        @DisplayName("Não deve fazer nada quando não há veículos sem cobertura")
        void naoDeveFazerNadaQuandoNaoHaVeiculosSemCobertura() {
            when(relacionamentoRepository.findVeiculosSemCobertura())
                .thenReturn(Collections.emptyList());

            scheduler.monitorarVeiculosSemCobertura();

            verify(relacionamentoRepository).findVeiculosSemCobertura();
        }

        @Test
        @DisplayName("Deve continuar em caso de exceção")
        void deveContinuarEmCasoExcecao() {
            when(relacionamentoRepository.findVeiculosSemCobertura())
                .thenThrow(new RuntimeException("Erro de conexão"));

            assertThatCode(() -> scheduler.monitorarVeiculosSemCobertura())
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de Robustez")
    class RobustezTests {

        @Test
        @DisplayName("Todos os métodos devem ser resilientes a exceções")
        void todosMetodosDevemSerResilientesAExcecoes() {
            when(relacionamentoRepository.findRelacionamentosVencendoAte(any()))
                .thenThrow(new RuntimeException());
            when(relacionamentoRepository.findRelacionamentosComGap())
                .thenThrow(new RuntimeException());
            when(relacionamentoRepository.findVeiculosSemCobertura())
                .thenThrow(new RuntimeException());

            assertThatCode(() -> {
                scheduler.monitorarVencimentos();
                scheduler.detectarGapsCobertura();
                scheduler.monitorarVeiculosSemCobertura();
            }).doesNotThrowAnyException();
        }
    }
}
