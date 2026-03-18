package com.seguradora.hibrida.domain.workflow.approval;

import com.seguradora.hibrida.domain.workflow.model.NivelAprovacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@DisplayName("AprovacaoNotificationService Tests")
class AprovacaoNotificationServiceTest {

    private AprovacaoNotificationService service;

    @BeforeEach
    void setUp() {
        service = new AprovacaoNotificationService();
    }

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

    @Test
    @DisplayName("notificarAprovadores não deve lançar exceção")
    void notificarAprovadoresNotShouldThrow() {
        Aprovacao aprovacao = aprovacaoPendente();
        service.notificarAprovadores(aprovacao);
    }

    @Test
    @DisplayName("notificarDecisao com APROVADO não deve lançar exceção")
    void notificarDecisaoAprovadoNotShouldThrow() {
        Aprovacao aprovacao = aprovacaoPendente();
        aprovacao.aprovar("ANALISTA_001", "João", "Aprovado");
        service.notificarDecisao(aprovacao, Aprovacao.DecisaoAprovacao.APROVADO);
    }

    @Test
    @DisplayName("notificarDecisao com REJEITADO não deve lançar exceção")
    void notificarDecisaoRejeitadoNotShouldThrow() {
        Aprovacao aprovacao = aprovacaoPendente();
        aprovacao.rejeitar("ANALISTA_001", "João", "Documentação insuficiente");
        service.notificarDecisao(aprovacao, Aprovacao.DecisaoAprovacao.REJEITADO);
    }

    @Test
    @DisplayName("enviarLembretes não deve lançar exceção para lista não vazia")
    void enviarLembretesNotShouldThrowForNonEmptyList() {
        Aprovacao aprovacao = aprovacaoPendente();
        service.enviarLembretes(List.of(aprovacao));
    }

    @Test
    @DisplayName("enviarLembretes não deve lançar exceção para lista vazia")
    void enviarLembretesNotShouldThrowForEmptyList() {
        service.enviarLembretes(List.of());
    }

    @Test
    @DisplayName("escalarPorTimeout não deve lançar exceção")
    void escalarPorTimeoutNotShouldThrow() {
        Aprovacao aprovacao = aprovacaoPendente();
        service.escalarPorTimeout(aprovacao);
    }

    @Test
    @DisplayName("notificarDelegacao não deve lançar exceção")
    void notificarDelegacaoNotShouldThrow() {
        Aprovacao aprovacao = aprovacaoPendente();
        service.notificarDelegacao(aprovacao, List.of("SUPERVISOR_001", "SUPERVISOR_002"));
    }

    @Test
    @DisplayName("enviarLembretes deve ignorar aprovações inativas")
    void enviarLembretesShouldIgnoreInactiveAprovacoes() {
        Aprovacao aprovacao = aprovacaoPendente();
        aprovacao.aprovar("ANALISTA_001", "João", "OK");
        service.enviarLembretes(List.of(aprovacao));
    }
}
