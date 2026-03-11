package com.seguradora.hibrida.domain.workflow.approval;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Serviço de notificações relacionadas a aprovações.
 * Gerencia comunicação com aprovadores sobre solicitações e decisões.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AprovacaoNotificationService {

    /**
     * Notifica os aprovadores sobre uma nova solicitação.
     *
     * @param aprovacao aprovação solicitada
     */
    public void notificarAprovadores(Aprovacao aprovacao) {
        log.info("Notificando {} aprovadores sobre aprovação {}",
                aprovacao.getAprovadores().size(), aprovacao.getId());

        for (String aprovadorId : aprovacao.getAprovadores()) {
            try {
                enviarNotificacao(aprovadorId,
                        "Nova Aprovação Requerida",
                        String.format(
                                "Sinistro: %s\nNível: %s\nValor: R$ %,.2f\nPrazo: %s",
                                aprovacao.getSinistroId(),
                                aprovacao.getNivel().getDescricao(),
                                aprovacao.getValorSinistro(),
                                aprovacao.getDataLimite()
                        )
                );
            } catch (Exception e) {
                log.error("Erro ao notificar aprovador {}: {}", aprovadorId, e.getMessage(), e);
            }
        }
    }

    /**
     * Notifica sobre a decisão de aprovação.
     *
     * @param aprovacao aprovação decidida
     * @param decisao decisão tomada
     */
    public void notificarDecisao(Aprovacao aprovacao, Aprovacao.DecisaoAprovacao decisao) {
        log.info("Notificando sobre decisão {} da aprovação {}", decisao, aprovacao.getId());

        String assunto = decisao == Aprovacao.DecisaoAprovacao.APROVADO
                ? "Aprovação Concedida"
                : "Aprovação Rejeitada";

        String mensagem = String.format(
                "Sinistro: %s\nDecisão: %s\nAprovador: %s\nJustificativa: %s",
                aprovacao.getSinistroId(),
                decisao,
                aprovacao.getAprovadorDecisaoNome(),
                aprovacao.getJustificativa()
        );

        // Notifica o solicitante original
        try {
            enviarNotificacao("SISTEMA_WORKFLOW", assunto, mensagem);
        } catch (Exception e) {
            log.error("Erro ao notificar sobre decisão: {}", e.getMessage(), e);
        }
    }

    /**
     * Envia lembretes para aprovações pendentes próximas do prazo.
     *
     * @param aprovacoesPendentes lista de aprovações pendentes
     */
    public void enviarLembretes(List<Aprovacao> aprovacoesPendentes) {
        log.info("Enviando lembretes para {} aprovações pendentes", aprovacoesPendentes.size());

        for (Aprovacao aprovacao : aprovacoesPendentes) {
            if (!aprovacao.isAtiva()) {
                continue;
            }

            for (String aprovadorId : aprovacao.getAprovadores()) {
                try {
                    long horasRestantes = java.time.Duration.between(
                            java.time.LocalDateTime.now(),
                            aprovacao.getDataLimite()
                    ).toHours();

                    enviarNotificacao(aprovadorId,
                            "Lembrete: Aprovação Pendente",
                            String.format(
                                    "Sinistro: %s\nValor: R$ %,.2f\nTempo restante: %d horas\n\nPor favor, tome uma decisão.",
                                    aprovacao.getSinistroId(),
                                    aprovacao.getValorSinistro(),
                                    horasRestantes
                            )
                    );
                } catch (Exception e) {
                    log.error("Erro ao enviar lembrete para aprovador {}: {}",
                            aprovadorId, e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Escala a aprovação por timeout para o próximo nível.
     *
     * @param aprovacao aprovação que expirou
     */
    public void escalarPorTimeout(Aprovacao aprovacao) {
        log.warn("Escalando aprovação {} por timeout", aprovacao.getId());

        // Notifica os aprovadores originais sobre o timeout
        for (String aprovadorId : aprovacao.getAprovadores()) {
            try {
                enviarNotificacao(aprovadorId,
                        "Aprovação Expirada - Escalada",
                        String.format(
                                "A aprovação do sinistro %s foi escalada para o próximo nível por exceder o prazo limite.",
                                aprovacao.getSinistroId()
                        )
                );
            } catch (Exception e) {
                log.error("Erro ao notificar escalação para {}: {}", aprovadorId, e.getMessage(), e);
            }
        }

        // Notifica supervisores sobre a escalação
        try {
            enviarNotificacao("SUPERVISORES",
                    "Aprovação Escalada por Timeout",
                    String.format(
                            "Sinistro: %s\nNível original: %s\nValor: R$ %,.2f\n\nRequer atenção imediata.",
                            aprovacao.getSinistroId(),
                            aprovacao.getNivel().getDescricao(),
                            aprovacao.getValorSinistro()
                    )
            );
        } catch (Exception e) {
            log.error("Erro ao notificar supervisores: {}", e.getMessage(), e);
        }
    }

    /**
     * Envia uma notificação (mock - em produção usaria serviço real).
     *
     * @param destinatario ID do destinatário
     * @param assunto assunto da notificação
     * @param mensagem corpo da mensagem
     */
    private void enviarNotificacao(String destinatario, String assunto, String mensagem) {
        // Mock de envio de notificação
        // Em produção, integraria com:
        // - Serviço de e-mail
        // - Sistema de mensageria (RabbitMQ, Kafka)
        // - Notificações push
        // - SMS para casos urgentes

        log.debug("NOTIFICAÇÃO enviada para {}: {} - {}", destinatario, assunto, mensagem);
    }

    /**
     * Notifica sobre delegação de aprovação.
     *
     * @param aprovacao aprovação delegada
     * @param novosAprovadores novos aprovadores
     */
    public void notificarDelegacao(Aprovacao aprovacao, List<String> novosAprovadores) {
        log.info("Notificando delegação de aprovação {} para {} aprovadores",
                aprovacao.getId(), novosAprovadores.size());

        String mensagem = String.format(
                "Você foi designado como aprovador do sinistro %s\nValor: R$ %,.2f\nNível: %s",
                aprovacao.getSinistroId(),
                aprovacao.getValorSinistro(),
                aprovacao.getNivel().getDescricao()
        );

        for (String aprovadorId : novosAprovadores) {
            try {
                enviarNotificacao(aprovadorId, "Aprovação Delegada", mensagem);
            } catch (Exception e) {
                log.error("Erro ao notificar delegação para {}: {}", aprovadorId, e.getMessage(), e);
            }
        }
    }
}
