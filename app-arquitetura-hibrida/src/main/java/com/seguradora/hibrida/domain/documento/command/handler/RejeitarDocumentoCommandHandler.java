package com.seguradora.hibrida.domain.documento.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.documento.aggregate.DocumentoAggregate;
import com.seguradora.hibrida.domain.documento.command.RejeitarDocumentoCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler para processar comando de rejeição de documento.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Validar comando</li>
 *   <li>Carregar aggregate existente</li>
 *   <li>Verificar se justificativa é adequada</li>
 *   <li>Marcar como rejeitado com motivo detalhado</li>
 *   <li>Notificar operador sobre rejeição (futuro)</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RejeitarDocumentoCommandHandler {

    private final AggregateRepository<DocumentoAggregate> aggregateRepository;

    private static final int TAMANHO_MINIMO_MOTIVO = 10;

    /**
     * Processa o comando de rejeição de documento.
     *
     * @param command Comando de rejeição
     * @return ID do documento rejeitado
     * @throws IllegalArgumentException se validações falharem
     * @throws IllegalStateException se documento não existir ou não puder ser rejeitado
     * @throws RuntimeException se houver erro no processamento
     */
    @Transactional
    public String handle(RejeitarDocumentoCommand command) {
        log.info("Iniciando rejeição de documento: {} por validador {} (motivo: {})",
                command.getDocumentoId(), command.getValidadorNome(), command.getMotivo());

        try {
            // 1. Validar comando
            command.validar();
            validarJustificativa(command);
            log.debug("Command validado com sucesso");

            // 2. Carregar aggregate existente
            DocumentoAggregate aggregate = aggregateRepository
                    .findById(command.getDocumentoId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Documento não encontrado: " + command.getDocumentoId()));

            log.debug("Aggregate carregado: status atual {}", aggregate.getStatus());

            // 3. Verificar se documento pode ser rejeitado
            if (!aggregate.getDocumento().podeRejeitar()) {
                throw new IllegalStateException(
                        String.format("Documento no status %s não pode ser rejeitado",
                                aggregate.getStatus()));
            }

            // 4. Rejeitar documento
            aggregate.rejeitar(
                    command.getMotivo(),
                    command.getProblemasIdentificados(),
                    command.getValidadorId(),
                    command.getValidadorNome(),
                    command.getAcoesCorretivas()
            );

            // 5. Persistir eventos
            aggregateRepository.save(aggregate);
            log.info("Documento rejeitado com sucesso: {} por {} - {} problemas identificados",
                    command.getDocumentoId(), command.getValidadorNome(),
                    command.getQuantidadeProblemas());

            // TODO: Enviar notificação ao operador que criou o documento
            notificarOperador(aggregate.getDocumento().getCriadoPor(), command);

            return command.getDocumentoId();

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Erro ao rejeitar documento: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("Erro ao rejeitar documento {}: {}", command.getDocumentoId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao rejeitar documento: " + e.getMessage(), e);
        }
    }

    /**
     * Valida se a justificativa é adequada.
     */
    private void validarJustificativa(RejeitarDocumentoCommand command) {
        String motivo = command.getMotivo();

        if (motivo.length() < TAMANHO_MINIMO_MOTIVO) {
            throw new IllegalArgumentException(
                    String.format("Motivo da rejeição muito curto (mínimo %d caracteres)",
                            TAMANHO_MINIMO_MOTIVO));
        }

        // Verificar se motivo é genérico demais
        String motivoLower = motivo.toLowerCase().trim();
        if (motivoLower.equals("invalido") ||
            motivoLower.equals("errado") ||
            motivoLower.equals("incorreto") ||
            motivoLower.equals("documento invalido") ||
            motivoLower.equals("documento errado") ||
            motivoLower.equals("documento incorreto")) {
            throw new IllegalArgumentException(
                    "Motivo da rejeição muito genérico. Forneça detalhes específicos.");
        }

        // Recomendar ações corretivas se não fornecidas
        if (!command.possuiAcoesCorretivas() && !command.possuiProblemasDetalhados()) {
            log.warn("Rejeição sem ações corretivas ou problemas detalhados - " +
                    "recomenda-se fornecer orientação ao operador");
        }
    }

    /**
     * Notifica o operador sobre a rejeição do documento.
     * TODO: Implementar integração com sistema de notificações
     */
    private void notificarOperador(String operadorId, RejeitarDocumentoCommand command) {
        log.info("Notificação pendente para operador {} sobre rejeição de documento {}",
                operadorId, command.getDocumentoId());

        // Preparar mensagem de notificação
        StringBuilder mensagem = new StringBuilder();
        mensagem.append("Seu documento foi rejeitado.\n\n");
        mensagem.append("Motivo: ").append(command.getMotivo()).append("\n\n");

        if (command.possuiProblemasDetalhados()) {
            mensagem.append("Problemas identificados:\n");
            for (int i = 0; i < command.getProblemasIdentificados().size(); i++) {
                mensagem.append(String.format("%d. %s\n",
                        i + 1, command.getProblemasIdentificados().get(i)));
            }
            mensagem.append("\n");
        }

        if (command.possuiAcoesCorretivas()) {
            mensagem.append("Ações corretivas sugeridas:\n");
            mensagem.append(command.getAcoesCorretivas()).append("\n\n");
        }

        if (command.isPermiteReenvio()) {
            mensagem.append("Você pode reenviar o documento após as correções.");
        } else {
            mensagem.append("Entre em contato com o suporte para mais informações.");
        }

        log.debug("Mensagem de notificação preparada: {}", mensagem);

        // TODO: Integrar com sistema de notificações (e-mail, SMS, push)
        // notificationService.enviar(operadorId, "Documento Rejeitado", mensagem.toString());
    }
}
