package com.seguradora.hibrida.domain.veiculo.relationship.service;

import com.seguradora.hibrida.domain.veiculo.relationship.dto.DashboardRelacionamentosDTO;
import com.seguradora.hibrida.domain.veiculo.relationship.dto.HistoricoRelacionamentoDTO;
import com.seguradora.hibrida.domain.veiculo.relationship.dto.VeiculoSemCoberturaDTO;
import com.seguradora.hibrida.domain.veiculo.relationship.model.StatusRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.model.TipoRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.model.VeiculoApoliceRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.repository.VeiculoApoliceRelacionamentoRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link RelationshipQueryService}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RelationshipQueryService - Testes Unitários")
class RelationshipQueryServiceTest {

    @Mock
    private VeiculoApoliceRelacionamentoRepository relacionamentoRepository;

    @InjectMocks
    private RelationshipQueryService service;

    private VeiculoApoliceRelacionamento relacionamentoMock;

    @BeforeEach
    void setUp() {
        relacionamentoMock = new VeiculoApoliceRelacionamento();
        relacionamentoMock.setId("REL-001");
        relacionamentoMock.setVeiculoId("VEI-001");
        relacionamentoMock.setApoliceId("APO-001");
        relacionamentoMock.setVeiculoPlaca("ABC1234");
        relacionamentoMock.setApoliceNumero("2024-001");
        relacionamentoMock.setSeguradoCpf("12345678909");
        relacionamentoMock.setSeguradoNome("João Silva");
        relacionamentoMock.setDataInicio(LocalDate.now().minusDays(90));
        relacionamentoMock.setDataFim(null);
        relacionamentoMock.setStatus(StatusRelacionamento.ATIVO);
        relacionamentoMock.setTipoRelacionamento(TipoRelacionamento.PRINCIPAL);
        relacionamentoMock.setTipoCobertura("COMPREENSIVA");
    }

    @Nested
    @DisplayName("Testes de getDashboard")
    class GetDashboardTests {

        @Test
        @DisplayName("Deve retornar dashboard com dados")
        void deveRetornarDashboardComDados() {
            when(relacionamentoRepository.findAll()).thenReturn(List.of(relacionamentoMock));
            when(relacionamentoRepository.findVeiculosSemCobertura()).thenReturn(Collections.emptyList());
            when(relacionamentoRepository.findRelacionamentosVencendoAte(any())).thenReturn(Collections.emptyList());
            when(relacionamentoRepository.findRelacionamentosComGap()).thenReturn(Collections.emptyList());

            DashboardRelacionamentosDTO result = service.getDashboard();

            assertThat(result).isNotNull();
            assertThat(result.getTotalRelacionamentosAtivos()).isEqualTo(1L);
            assertThat(result.getDataAtualizacao()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("Deve contar relacionamentos por status")
        void deveContarRelacionamentosPorStatus() {
            VeiculoApoliceRelacionamento encerrado = new VeiculoApoliceRelacionamento();
            encerrado.setStatus(StatusRelacionamento.ENCERRADO);

            VeiculoApoliceRelacionamento cancelado = new VeiculoApoliceRelacionamento();
            cancelado.setStatus(StatusRelacionamento.CANCELADO);

            when(relacionamentoRepository.findAll()).thenReturn(
                List.of(relacionamentoMock, encerrado, cancelado)
            );
            when(relacionamentoRepository.findVeiculosSemCobertura()).thenReturn(Collections.emptyList());
            when(relacionamentoRepository.findRelacionamentosVencendoAte(any())).thenReturn(Collections.emptyList());
            when(relacionamentoRepository.findRelacionamentosComGap()).thenReturn(Collections.emptyList());

            DashboardRelacionamentosDTO result = service.getDashboard();

            assertThat(result.getTotalRelacionamentosAtivos()).isEqualTo(1L);
            assertThat(result.getTotalRelacionamentosEncerrados()).isEqualTo(1L);
            assertThat(result.getTotalRelacionamentosCancelados()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("Testes de getVeiculosSemCobertura")
    class GetVeiculosSemCoberturaTests {

        @Test
        @DisplayName("Deve retornar lista de veículos sem cobertura")
        void deveRetornarListaVeiculosSemCobertura() {
            relacionamentoMock.setDataFim(LocalDate.now().minusDays(10));

            when(relacionamentoRepository.findVeiculosSemCobertura()).thenReturn(List.of("VEI-001"));
            when(relacionamentoRepository.findByVeiculoIdOrderByDataInicioDesc("VEI-001"))
                .thenReturn(List.of(relacionamentoMock));

            List<VeiculoSemCoberturaDTO> result = service.getVeiculosSemCobertura();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getVeiculoId()).isEqualTo("VEI-001");
            assertThat(result.get(0).getDiasSemCobertura()).isEqualTo(10);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há veículos sem cobertura")
        void deveRetornarListaVaziaQuandoNaoHaVeiculosSemCobertura() {
            when(relacionamentoRepository.findVeiculosSemCobertura()).thenReturn(Collections.emptyList());

            List<VeiculoSemCoberturaDTO> result = service.getVeiculosSemCobertura();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de getHistoricoVeiculo")
    class GetHistoricoVeiculoTests {

        @Test
        @DisplayName("Deve retornar histórico de relacionamentos")
        void deveRetornarHistoricoRelacionamentos() {
            when(relacionamentoRepository.findByVeiculoIdOrderByDataInicioDesc("VEI-001"))
                .thenReturn(List.of(relacionamentoMock));

            List<HistoricoRelacionamentoDTO> result = service.getHistoricoVeiculo("VEI-001");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getApoliceId()).isEqualTo("APO-001");
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há histórico")
        void deveRetornarListaVaziaQuandoNaoHaHistorico() {
            when(relacionamentoRepository.findByVeiculoIdOrderByDataInicioDesc("VEI-999"))
                .thenReturn(Collections.emptyList());

            List<HistoricoRelacionamentoDTO> result = service.getHistoricoVeiculo("VEI-999");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de getRelacionamentosAtivosVeiculo")
    class GetRelacionamentosAtivosVeiculoTests {

        @Test
        @DisplayName("Deve retornar relacionamentos ativos do veículo")
        void deveRetornarRelacionamentosAtivosVeiculo() {
            when(relacionamentoRepository.findByVeiculoIdAndStatus("VEI-001", StatusRelacionamento.ATIVO))
                .thenReturn(List.of(relacionamentoMock));

            List<VeiculoApoliceRelacionamento> result = service.getRelacionamentosAtivosVeiculo("VEI-001");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(StatusRelacionamento.ATIVO);
        }
    }

    @Nested
    @DisplayName("Testes de temCoberturaAtiva")
    class TemCoberturaAtivaTests {

        @Test
        @DisplayName("Deve retornar true quando tem cobertura ativa")
        void deveRetornarTrueQuandoTemCoberturaAtiva() {
            when(relacionamentoRepository.countRelacionamentosAtivos("VEI-001")).thenReturn(1L);

            boolean result = service.temCoberturaAtiva("VEI-001");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando não tem cobertura ativa")
        void deveRetornarFalseQuandoNaoTemCoberturaAtiva() {
            when(relacionamentoRepository.countRelacionamentosAtivos("VEI-001")).thenReturn(0L);

            boolean result = service.temCoberturaAtiva("VEI-001");

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de estaCoberto")
    class EstaCobertoTests {

        @Test
        @DisplayName("Deve retornar true quando está coberto na data")
        void deveRetornarTrueQuandoEstaCobertoNaData() {
            LocalDate data = LocalDate.now();
            when(relacionamentoRepository.findRelacionamentosVigentesEm("VEI-001", data))
                .thenReturn(List.of(relacionamentoMock));

            boolean result = service.estaCoberto("VEI-001", data);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando não está coberto na data")
        void deveRetornarFalseQuandoNaoEstaCobertoNaData() {
            LocalDate data = LocalDate.now();
            when(relacionamentoRepository.findRelacionamentosVigentesEm("VEI-001", data))
                .thenReturn(Collections.emptyList());

            boolean result = service.estaCoberto("VEI-001", data);

            assertThat(result).isFalse();
        }
    }
}
