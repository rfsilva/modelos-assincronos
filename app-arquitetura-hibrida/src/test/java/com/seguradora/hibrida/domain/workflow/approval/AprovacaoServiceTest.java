package com.seguradora.hibrida.domain.workflow.approval;

import com.seguradora.hibrida.domain.workflow.model.NivelAprovacao;
import com.seguradora.hibrida.domain.workflow.repository.AprovacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AprovacaoService Tests")
class AprovacaoServiceTest {

    @Mock
    private AprovacaoRepository aprovacaoRepository;

    @Mock
    private AprovadorPolicy aprovadorPolicy;

    @Mock
    private AprovacaoNotificationService notificationService;

    @InjectMocks
    private AprovacaoService service;

    private Aprovacao aprovacaoPendente() {
        return Aprovacao.builder()
                .id("aprov-001")
                .workflowInstanceId("wi-001")
                .sinistroId("SIN-001")
                .nivel(NivelAprovacao.NIVEL_1_ANALISTA)
                .status(Aprovacao.StatusAprovacao.PENDENTE)
                .aprovadores(List.of("ANALISTA_001", "ANALISTA_002"))
                .valorSinistro(new BigDecimal("5000.00"))
                .dataSolicitacao(LocalDateTime.now())
                .dataLimite(LocalDateTime.now().plusHours(72))
                .build();
    }

    // =========================================================================
    // solicitarAprovacao
    // =========================================================================

    @Nested
    @DisplayName("solicitarAprovacao()")
    class SolicitarAprovacao {

        @Test
        @DisplayName("Deve criar aprovação quando não há pendente")
        void shouldCreateAprovacaoWhenNoPendingExists() {
            when(aprovacaoRepository.findBySinistroIdAndStatusIn(anyString(), anyList()))
                    .thenReturn(List.of());
            when(aprovadorPolicy.obterAprovadores(any(), anyString()))
                    .thenReturn(List.of("ANALISTA_001"));
            Aprovacao saved = aprovacaoPendente();
            when(aprovacaoRepository.save(any())).thenReturn(saved);

            Aprovacao result = service.solicitarAprovacao("wi-001", "SIN-001",
                    NivelAprovacao.NIVEL_1_ANALISTA, new BigDecimal("5000.00"));

            assertThat(result).isNotNull();
            verify(aprovacaoRepository).save(any());
            verify(notificationService).notificarAprovadores(any());
        }

        @Test
        @DisplayName("Deve retornar aprovação existente quando há pendente")
        void shouldReturnExistingWhenPendingExists() {
            Aprovacao existente = aprovacaoPendente();
            when(aprovacaoRepository.findBySinistroIdAndStatusIn(anyString(), anyList()))
                    .thenReturn(List.of(existente));

            Aprovacao result = service.solicitarAprovacao("wi-001", "SIN-001",
                    NivelAprovacao.NIVEL_1_ANALISTA, new BigDecimal("5000.00"));

            assertThat(result).isEqualTo(existente);
            verify(aprovacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando não há aprovadores")
        void shouldThrowWhenNoAprovadores() {
            when(aprovacaoRepository.findBySinistroIdAndStatusIn(anyString(), anyList()))
                    .thenReturn(List.of());
            when(aprovadorPolicy.obterAprovadores(any(), anyString()))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> service.solicitarAprovacao("wi-001", "SIN-001",
                    NivelAprovacao.NIVEL_1_ANALISTA, new BigDecimal("5000.00")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Nenhum aprovador");
        }
    }

    // =========================================================================
    // aprovar
    // =========================================================================

    @Nested
    @DisplayName("aprovar()")
    class Aprovar {

        @Test
        @DisplayName("Deve aprovar quando aprovação existe")
        void shouldApproveWhenExists() {
            Aprovacao aprovacao = aprovacaoPendente();
            when(aprovacaoRepository.findById("aprov-001")).thenReturn(Optional.of(aprovacao));
            doNothing().when(aprovadorPolicy).validarPermissao(anyString(), any(), any());
            when(aprovacaoRepository.save(any())).thenReturn(aprovacao);

            service.aprovar("aprov-001", "ANALISTA_001", "João", "Aprovado");

            assertThat(aprovacao.isAprovada()).isTrue();
            verify(notificationService).notificarDecisao(any(), eq(Aprovacao.DecisaoAprovacao.APROVADO));
        }

        @Test
        @DisplayName("Deve lançar exceção quando aprovação não encontrada")
        void shouldThrowWhenNotFound() {
            when(aprovacaoRepository.findById("inexistente")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.aprovar("inexistente", "ANALISTA_001", "João", "OK"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não encontrada");
        }
    }

    // =========================================================================
    // rejeitar
    // =========================================================================

    @Nested
    @DisplayName("rejeitar()")
    class Rejeitar {

        @Test
        @DisplayName("Deve rejeitar quando aprovação existe")
        void shouldRejectWhenExists() {
            Aprovacao aprovacao = aprovacaoPendente();
            when(aprovacaoRepository.findById("aprov-001")).thenReturn(Optional.of(aprovacao));
            doNothing().when(aprovadorPolicy).validarPermissao(anyString(), any(), any());
            when(aprovacaoRepository.save(any())).thenReturn(aprovacao);

            service.rejeitar("aprov-001", "ANALISTA_001", "João", "Documentação inválida");

            assertThat(aprovacao.isRejeitada()).isTrue();
            verify(notificationService).notificarDecisao(any(), eq(Aprovacao.DecisaoAprovacao.REJEITADO));
        }
    }

    // =========================================================================
    // delegar
    // =========================================================================

    @Nested
    @DisplayName("delegar()")
    class Delegar {

        @Test
        @DisplayName("Deve delegar quando aprovação existe")
        void shouldDelegateWhenExists() {
            Aprovacao aprovacao = aprovacaoPendente();
            when(aprovacaoRepository.findById("aprov-001")).thenReturn(Optional.of(aprovacao));
            when(aprovacaoRepository.save(any())).thenReturn(aprovacao);

            service.delegar("aprov-001", List.of("SUPERVISOR_001"));

            assertThat(aprovacao.getAprovadores()).containsExactly("SUPERVISOR_001");
            verify(notificationService).notificarAprovadores(any());
        }
    }

    // =========================================================================
    // verificarTimeout
    // =========================================================================

    @Nested
    @DisplayName("verificarTimeout()")
    class VerificarTimeout {

        @Test
        @DisplayName("Deve expirar aprovações que excederam o prazo")
        void shouldExpireAprovacoesThatExceededDeadline() {
            Aprovacao vencida = aprovacaoPendente();
            vencida.setDataLimite(LocalDateTime.now().minusHours(1));
            when(aprovacaoRepository.findByStatusIn(anyList())).thenReturn(List.of(vencida));
            when(aprovacaoRepository.save(any())).thenReturn(vencida);

            service.verificarTimeout();

            assertThat(vencida.isExpirada()).isTrue();
            verify(notificationService).escalarPorTimeout(any());
        }

        @Test
        @DisplayName("Deve enviar lembretes para aprovações próximas do prazo")
        void shouldSendRemindersForAprovacoesByDeadline() {
            Aprovacao proxima = aprovacaoPendente();
            proxima.setDataLimite(LocalDateTime.now().plusHours(6));
            when(aprovacaoRepository.findByStatusIn(anyList())).thenReturn(List.of(proxima));

            service.verificarTimeout();

            verify(notificationService).enviarLembretes(anyList());
        }

        @Test
        @DisplayName("Não deve fazer nada para lista vazia")
        void shouldDoNothingForEmptyList() {
            when(aprovacaoRepository.findByStatusIn(anyList())).thenReturn(List.of());

            service.verificarTimeout();

            verify(aprovacaoRepository, never()).save(any());
        }
    }

    // =========================================================================
    // escalar
    // =========================================================================

    @Nested
    @DisplayName("escalar()")
    class Escalar {

        @Test
        @DisplayName("Deve escalar para próximo nível")
        void shouldEscalateToNextLevel() {
            Aprovacao aprovacao = aprovacaoPendente();
            when(aprovacaoRepository.findById("aprov-001")).thenReturn(Optional.of(aprovacao));
            when(aprovadorPolicy.obterProximoNivel(any())).thenReturn(NivelAprovacao.NIVEL_2_SUPERVISOR);
            when(aprovadorPolicy.obterAprovadores(any(), anyString())).thenReturn(List.of("SUPERVISOR_001"));
            when(aprovacaoRepository.save(any())).thenReturn(aprovacao);

            service.escalar("aprov-001", "Prazo excedido");

            assertThat(aprovacao.getNivel()).isEqualTo(NivelAprovacao.NIVEL_2_SUPERVISOR);
            verify(notificationService).notificarAprovadores(any());
        }

        @Test
        @DisplayName("Deve lançar exceção se no nível máximo")
        void shouldThrowIfAtMaxLevel() {
            Aprovacao aprovacao = aprovacaoPendente();
            when(aprovacaoRepository.findById("aprov-001")).thenReturn(Optional.of(aprovacao));
            when(aprovadorPolicy.obterProximoNivel(any())).thenReturn(null);

            assertThatThrownBy(() -> service.escalar("aprov-001", "Motivo"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("máximo");
        }
    }

    // =========================================================================
    // buscarPorSinistro / buscarPendentesPorAprovador
    // =========================================================================

    @Test
    @DisplayName("buscarPorSinistro deve delegar ao repository")
    void buscarPorSinistroShouldDelegateToRepository() {
        Aprovacao aprovacao = aprovacaoPendente();
        when(aprovacaoRepository.findBySinistroId("SIN-001")).thenReturn(List.of(aprovacao));

        List<Aprovacao> result = service.buscarPorSinistro("SIN-001");

        assertThat(result).hasSize(1);
        verify(aprovacaoRepository).findBySinistroId("SIN-001");
    }

    @Test
    @DisplayName("buscarPendentesPorAprovador deve delegar ao repository")
    void buscarPendentesPorAprovadorShouldDelegateToRepository() {
        when(aprovacaoRepository.findByAprovadoresContainingAndStatusIn(anyString(), anyList()))
                .thenReturn(List.of());

        List<Aprovacao> result = service.buscarPendentesPorAprovador("ANALISTA_001");

        assertThat(result).isEmpty();
        verify(aprovacaoRepository).findByAprovadoresContainingAndStatusIn(anyString(), anyList());
    }
}
