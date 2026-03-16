package com.seguradora.hibrida.domain.veiculo.relationship.handler;

import com.seguradora.hibrida.domain.apolice.event.ApoliceCanceladaEvent;
import com.seguradora.hibrida.domain.apolice.event.ApoliceVencidaEvent;
import com.seguradora.hibrida.domain.veiculo.event.VeiculoAssociadoEvent;
import com.seguradora.hibrida.domain.veiculo.event.VeiculoDesassociadoEvent;
import com.seguradora.hibrida.domain.veiculo.relationship.model.StatusRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.model.VeiculoApoliceRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.repository.VeiculoApoliceRelacionamentoRepository;
import com.seguradora.hibrida.domain.veiculo.relationship.service.RelationshipAlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link VeiculoApoliceRelationshipHandler}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VeiculoApoliceRelationshipHandler - Testes Unitários")
class VeiculoApoliceRelationshipHandlerTest {

    @Mock
    private VeiculoApoliceRelacionamentoRepository relacionamentoRepository;

    @Mock
    private RelationshipAlertService alertService;

    @InjectMocks
    private VeiculoApoliceRelationshipHandler handler;

    @Captor
    private ArgumentCaptor<VeiculoApoliceRelacionamento> relacionamentoCaptor;

    private VeiculoAssociadoEvent eventoAssociacao;
    private VeiculoDesassociadoEvent eventoDesassociacao;
    private VeiculoApoliceRelacionamento relacionamentoMock;

    @BeforeEach
    void setUp() {
        eventoAssociacao = new VeiculoAssociadoEvent(
            "VEI-001", 1L, "APO-001", LocalDate.now(), "OP-123"
        );

        eventoDesassociacao = new VeiculoDesassociadoEvent(
            "VEI-001", 2L, "APO-001", LocalDate.now(), "Cancelamento", "OP-456"
        );

        relacionamentoMock = new VeiculoApoliceRelacionamento();
        relacionamentoMock.setId("REL-001");
        relacionamentoMock.setVeiculoId("VEI-001");
        relacionamentoMock.setApoliceId("APO-001");
        relacionamentoMock.setVeiculoPlaca("ABC1234");
        relacionamentoMock.setSeguradoCpf("12345678909");
        relacionamentoMock.setSeguradoNome("João Silva");
        relacionamentoMock.setStatus(StatusRelacionamento.ATIVO);
    }

    @Nested
    @DisplayName("Testes de handle(VeiculoAssociadoEvent)")
    class HandleVeiculoAssociadoTests {

        @Test
        @DisplayName("Deve criar novo relacionamento")
        void deveCriarNovoRelacionamento() {
            when(relacionamentoRepository.existsRelacionamentoAtivo("VEI-001", "APO-001"))
                .thenReturn(false);

            handler.handle(eventoAssociacao);

            verify(relacionamentoRepository).save(relacionamentoCaptor.capture());

            VeiculoApoliceRelacionamento saved = relacionamentoCaptor.getValue();
            assertThat(saved.getVeiculoId()).isEqualTo("VEI-001");
            assertThat(saved.getApoliceId()).isEqualTo("APO-001");
            assertThat(saved.getStatus()).isEqualTo(StatusRelacionamento.ATIVO);
        }

        @Test
        @DisplayName("Não deve criar relacionamento duplicado")
        void naoDeveCriarRelacionamentoDuplicado() {
            when(relacionamentoRepository.existsRelacionamentoAtivo("VEI-001", "APO-001"))
                .thenReturn(true);

            handler.handle(eventoAssociacao);

            verify(relacionamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve notificar cobertura restaurada")
        void deveNotificarCoberturaRestaurada() {
            when(relacionamentoRepository.existsRelacionamentoAtivo("VEI-001", "APO-001"))
                .thenReturn(false);

            handler.handle(eventoAssociacao);

            verify(alertService).notificarCoberturaRestaurada("VEI-001", "APO-001");
        }

        @Test
        @DisplayName("Deve lançar exceção em caso de erro")
        void deveLancarExcecaoEmCasoErro() {
            when(relacionamentoRepository.existsRelacionamentoAtivo(anyString(), anyString()))
                .thenThrow(new RuntimeException("Erro de conexão"));

            assertThatThrownBy(() -> handler.handle(eventoAssociacao))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao processar associação de veículo");
        }
    }

    @Nested
    @DisplayName("Testes de handle(VeiculoDesassociadoEvent)")
    class HandleVeiculoDesassociadoTests {

        @Test
        @DisplayName("Deve encerrar relacionamento existente")
        void deveEncerrarRelacionamentoExistente() {
            when(relacionamentoRepository.findRelacionamentoAtivo("VEI-001", "APO-001"))
                .thenReturn(Optional.of(relacionamentoMock));
            when(relacionamentoRepository.countRelacionamentosAtivos("VEI-001"))
                .thenReturn(0L);

            handler.handle(eventoDesassociacao);

            verify(relacionamentoRepository).save(relacionamentoMock);
            assertThat(relacionamentoMock.getStatus()).isEqualTo(StatusRelacionamento.ENCERRADO);
            assertThat(relacionamentoMock.getMotivoDesassociacao()).isEqualTo("Cancelamento");
        }

        @Test
        @DisplayName("Deve alertar quando veículo fica sem cobertura")
        void deveAlertarQuandoVeiculoFicaSemCobertura() {
            when(relacionamentoRepository.findRelacionamentoAtivo("VEI-001", "APO-001"))
                .thenReturn(Optional.of(relacionamentoMock));
            when(relacionamentoRepository.countRelacionamentosAtivos("VEI-001"))
                .thenReturn(0L);

            handler.handle(eventoDesassociacao);

            verify(alertService).alertarVeiculoSemCobertura(
                eq("VEI-001"), eq("ABC1234"), eq("12345678909"), eq("João Silva")
            );
        }

        @Test
        @DisplayName("Não deve alertar quando veículo tem outras coberturas")
        void naoDeveAlertarQuandoVeiculoTemOutrasCoberturas() {
            when(relacionamentoRepository.findRelacionamentoAtivo("VEI-001", "APO-001"))
                .thenReturn(Optional.of(relacionamentoMock));
            when(relacionamentoRepository.countRelacionamentosAtivos("VEI-001"))
                .thenReturn(1L);

            handler.handle(eventoDesassociacao);

            verify(alertService, never()).alertarVeiculoSemCobertura(
                anyString(), anyString(), anyString(), anyString()
            );
        }

        @Test
        @DisplayName("Não deve fazer nada se relacionamento não existe")
        void naoDeveFazerNadaSeRelacionamentoNaoExiste() {
            when(relacionamentoRepository.findRelacionamentoAtivo("VEI-001", "APO-001"))
                .thenReturn(Optional.empty());

            handler.handle(eventoDesassociacao);

            verify(relacionamentoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de handle(ApoliceCanceladaEvent)")
    class HandleApoliceCanceladaTests {

        @Test
        @DisplayName("Deve cancelar todos relacionamentos da apólice")
        void deveCancelarTodosRelacionamentosApolice() {
            ApoliceCanceladaEvent evento = new ApoliceCanceladaEvent(
                "APO-001", 1L, "2024-001", "SEG-001", "100000.00", "Inadimplência",
                LocalDate.now().toString(), "0.00", "OP-123", null, "INADIMPLENCIA"
            );

            when(relacionamentoRepository.findByApoliceIdAndStatus("APO-001", StatusRelacionamento.ATIVO))
                .thenReturn(List.of(relacionamentoMock));

            handler.handle(evento);

            verify(relacionamentoRepository).save(relacionamentoMock);
            assertThat(relacionamentoMock.getStatus()).isEqualTo(StatusRelacionamento.CANCELADO);
            assertThat(relacionamentoMock.getMotivoDesassociacao()).contains("Inadimplência");
        }

        @Test
        @DisplayName("Deve alertar veículos afetados por cancelamento")
        void deveAlertarVeiculosAfetadosPorCancelamento() {
            ApoliceCanceladaEvent evento = new ApoliceCanceladaEvent(
                "APO-001", 1L, "2024-001", "SEG-001", "100000.00", "Inadimplência",
                LocalDate.now().toString(), "0.00", "OP-123", null, "INADIMPLENCIA"
            );

            relacionamentoMock.setApoliceNumero("2024-001");
            when(relacionamentoRepository.findByApoliceIdAndStatus("APO-001", StatusRelacionamento.ATIVO))
                .thenReturn(List.of(relacionamentoMock));

            handler.handle(evento);

            verify(alertService).alertarVeiculoSemCoberturaPorCancelamento(
                eq("VEI-001"), eq("ABC1234"), eq("12345678909"), eq("João Silva"),
                eq("2024-001"), eq("Inadimplência")
            );
        }

        @Test
        @DisplayName("Não deve fazer nada se não há relacionamentos ativos")
        void naoDeveFazerNadaSeNaoHaRelacionamentosAtivos() {
            ApoliceCanceladaEvent evento = new ApoliceCanceladaEvent(
                "APO-001", 1L, "2024-001", "SEG-001", "100000.00", "Inadimplência",
                LocalDate.now().toString(), "0.00", "OP-123", null, "INADIMPLENCIA"
            );

            when(relacionamentoRepository.findByApoliceIdAndStatus("APO-001", StatusRelacionamento.ATIVO))
                .thenReturn(Collections.emptyList());

            handler.handle(evento);

            verify(relacionamentoRepository, never()).save(any());
            verify(alertService, never()).alertarVeiculoSemCoberturaPorCancelamento(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
            );
        }
    }

    @Nested
    @DisplayName("Testes de handle(ApoliceVencidaEvent)")
    class HandleApoliceVencidaTests {

        @Test
        @DisplayName("Deve encerrar relacionamentos da apólice vencida")
        void deveEncerrarRelacionamentosApoliceVencida() {
            ApoliceVencidaEvent evento = new ApoliceVencidaEvent(
                "APO-001", 1L, "2024-001", "SEG-001", LocalDate.now().toString(), "100000.00"
            );

            when(relacionamentoRepository.findByApoliceIdAndStatus("APO-001", StatusRelacionamento.ATIVO))
                .thenReturn(List.of(relacionamentoMock));

            handler.handle(evento);

            verify(relacionamentoRepository).save(relacionamentoMock);
            assertThat(relacionamentoMock.getStatus()).isEqualTo(StatusRelacionamento.ENCERRADO);
            assertThat(relacionamentoMock.getMotivoDesassociacao()).contains("vencida");
        }

        @Test
        @DisplayName("Deve alertar sobre vencimento")
        void deveAlertarSobreVencimento() {
            ApoliceVencidaEvent evento = new ApoliceVencidaEvent(
                "APO-001", 1L, "2024-001", "SEG-001", LocalDate.now().toString(), "100000.00"
            );

            relacionamentoMock.setApoliceNumero("2024-001");
            when(relacionamentoRepository.findByApoliceIdAndStatus("APO-001", StatusRelacionamento.ATIVO))
                .thenReturn(List.of(relacionamentoMock));

            handler.handle(evento);

            verify(alertService).alertarVeiculoSemCoberturaPorVencimento(
                eq("VEI-001"), eq("ABC1234"), eq("12345678909"), eq("João Silva"), eq("2024-001")
            );
        }

        @Test
        @DisplayName("Não deve lançar exceção em caso de erro")
        void naoDeveLancarExcecaoEmCasoErro() {
            ApoliceVencidaEvent evento = new ApoliceVencidaEvent(
                "APO-001", 1L, "2024-001", "SEG-001", LocalDate.now().toString(), "100000.00"
            );

            when(relacionamentoRepository.findByApoliceIdAndStatus(anyString(), any()))
                .thenThrow(new RuntimeException("Erro de conexão"));

            assertThatCode(() -> handler.handle(evento))
                .doesNotThrowAnyException();
        }
    }
}
