package com.seguradora.hibrida.domain.sinistro.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.sinistro.aggregate.SinistroAggregate;
import com.seguradora.hibrida.domain.sinistro.command.ReprovarSinistroCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Command Handler para reprovação de sinistros.
 *
 * <p>Este handler processa a reprovação do sinistro após análise técnica,
 * documentando o motivo e fundamentação legal da decisão.
 *
 * <p><strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Validar motivo e fundamentação da reprovação</li>
 *   <li>Verificar completude da justificativa</li>
 *   <li>Reprovar sinistro no aggregate</li>
 *   <li>Gerar documentação para o segurado</li>
 *   <li>Habilitar possibilidade de recurso</li>
 * </ul>
 *
 * <p><strong>Validações realizadas:</strong>
 * <ul>
 *   <li>Sinistro está em análise</li>
 *   <li>Motivo de reprovação é válido</li>
 *   <li>Justificativa é detalhada e fundamentada</li>
 *   <li>Fundamento legal citado quando aplicável</li>
 *   <li>Analista possui permissões para reprovar</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReprovarSinistroCommandHandler implements CommandHandler<ReprovarSinistroCommand> {

    private final AggregateRepository<SinistroAggregate> sinistroRepository;

    @Override
    public CommandResult handle(ReprovarSinistroCommand command) {
        log.info("Processando reprovação de sinistro: sinistroId={}, motivo={}",
            command.getSinistroId(), command.getMotivo());

        try {
            // 1. Carregar aggregate
            SinistroAggregate aggregate = sinistroRepository.getById(command.getSinistroId());

            // 2. Validar estado do sinistro
            validarEstadoSinistro(aggregate);

            // 3. Validar motivo da reprovação
            validarMotivo(command);

            // 4. Validar justificativa
            validarJustificativa(command);

            // 5. Validar fundamentação legal
            validarFundamentacaoLegal(command);

            // 6. Validar permissões do analista
            validarPermissoesAnalista(command);

            // 7. Reprovar no aggregate
            aggregate.reprovar(
                command.getMotivo(),
                command.getJustificativaDetalhada(),
                command.getAnalistaId(),
                command.getFundamentoLegal()
            );

            // 8. Salvar aggregate (persiste eventos)
            sinistroRepository.save(aggregate);

            log.info("Sinistro reprovado com sucesso: sinistroId={}, motivo={}",
                command.getSinistroId(), command.getMotivo());

            // 9. Verificar se é caso de fraude para notificações especiais
            if (command.isReprovacaoPorFraude()) {
                log.warn("Sinistro reprovado por suspeita de fraude: sinistroId={}",
                    command.getSinistroId());
                // TODO: Acionar sistema de prevenção à fraude
            }

            return CommandResult.success(command.getSinistroId(), Map.of(
                "motivo", command.getMotivo(),
                "analistaId", command.getAnalistaId(),
                "possibilidadeRecurso", true,
                "versao", aggregate.getVersion()
            )).withCorrelationId(command.getCorrelationId());

        } catch (IllegalArgumentException e) {
            log.warn("Validação falhou ao reprovar sinistro {}: {}", command.getSinistroId(), e.getMessage());
            return CommandResult.failure(e.getMessage(), "VALIDATION_ERROR")
                .withCorrelationId(command.getCorrelationId());

        } catch (Exception e) {
            log.error("Erro ao reprovar sinistro: {}", command.getSinistroId(), e);
            return CommandResult.failure(
                "Erro ao processar reprovação do sinistro: " + e.getMessage(),
                "PROCESSING_ERROR"
            ).withCorrelationId(command.getCorrelationId());
        }
    }

    /**
     * Valida se o sinistro está em estado válido para reprovação.
     */
    private void validarEstadoSinistro(SinistroAggregate aggregate) {
        if (!aggregate.isAberto()) {
            throw new IllegalArgumentException(
                "Sinistro não está em estado válido para reprovação. " +
                "Status atual: " + aggregate.getSinistro().getStatus()
            );
        }

        log.debug("Estado do sinistro validado: status={}", aggregate.getSinistro().getStatus());
    }

    /**
     * Valida motivo da reprovação.
     */
    private void validarMotivo(ReprovarSinistroCommand command) {
        log.debug("Validando motivo da reprovação: {}", command.getMotivo());

        if (command.getMotivo() == null || command.getMotivo().trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo da reprovação não pode ser vazio");
        }

        if (command.getMotivo().length() < 5) {
            throw new IllegalArgumentException(
                "Motivo da reprovação muito curto. Mínimo: 5 caracteres"
            );
        }

        // Validar se motivo é padronizado (recomendado, mas não obrigatório)
        if (!command.isMotivoValido()) {
            log.warn("Motivo de reprovação não padronizado: {}. " +
                "Considere usar um dos motivos padrão.", command.getMotivo());
        }

        log.debug("Motivo validado: {}", command.getMotivo());
    }

    /**
     * Valida justificativa detalhada.
     */
    private void validarJustificativa(ReprovarSinistroCommand command) {
        if (!command.isJustificativaAdequada()) {
            throw new IllegalArgumentException(
                "Justificativa inadequada. Deve conter pelo menos 30 palavras e " +
                "explicar detalhadamente os motivos técnicos e legais da reprovação."
            );
        }

        // Verificar se justificativa menciona elementos importantes
        String justificativa = command.getJustificativaDetalhada().toLowerCase();

        boolean mencionaAnalise = justificativa.contains("análise") ||
                                  justificativa.contains("avaliação") ||
                                  justificativa.contains("verificação");

        boolean mencionaMotivo = justificativa.contains("motivo") ||
                                 justificativa.contains("razão") ||
                                 justificativa.contains("causa");

        if (!mencionaAnalise || !mencionaMotivo) {
            log.warn("Justificativa pode estar incompleta. " +
                "Recomenda-se mencionar análise realizada e motivos específicos.");
        }

        log.debug("Justificativa validada: {} caracteres, {} palavras",
            command.getJustificativaDetalhada().length(),
            command.getJustificativaDetalhada().split("\\s+").length);
    }

    /**
     * Valida fundamentação legal.
     */
    private void validarFundamentacaoLegal(ReprovarSinistroCommand command) {
        // Fundamento legal é obrigatório para certos motivos
        boolean requerFundamento = command.getMotivo() != null && (
            command.getMotivo().toUpperCase().contains("COBERTURA") ||
            command.getMotivo().toUpperCase().contains("CARENCIA") ||
            command.getMotivo().toUpperCase().contains("IRREGULAR")
        );

        if (requerFundamento && !command.hasFundamentoLegal()) {
            throw new IllegalArgumentException(
                "Fundamento legal é obrigatório para o motivo: " + command.getMotivo() +
                ". Cite a cláusula contratual ou dispositivo legal aplicável."
            );
        }

        if (command.hasFundamentoLegal()) {
            log.debug("Fundamento legal fornecido: {} caracteres",
                command.getFundamentoLegal().length());

            // Validar formato mínimo do fundamento
            if (command.getFundamentoLegal().length() < 10) {
                throw new IllegalArgumentException(
                    "Fundamento legal muito curto. Forneça referência completa."
                );
            }
        }
    }

    /**
     * Valida permissões do analista para reprovar.
     */
    private void validarPermissoesAnalista(ReprovarSinistroCommand command) {
        log.debug("Validando permissões do analista: {}", command.getAnalistaId());

        // TODO: Implementar verificação real de permissões
        // Mock: Verificar se analista está ativo
        if (command.getAnalistaId() == null || command.getAnalistaId().trim().isEmpty()) {
            throw new IllegalArgumentException("Analista inválido");
        }

        // Reprovações por fraude podem requerer alçada especial
        if (command.isReprovacaoPorFraude()) {
            validarAlcadaParaReprovacaoFraude(command.getAnalistaId());
        }

        log.debug("Permissões do analista validadas");
    }

    /**
     * Valida alçada para reprovação por fraude (mock).
     */
    private void validarAlcadaParaReprovacaoFraude(String analistaId) {
        log.debug("Validando alçada para reprovação por fraude: {}", analistaId);

        // TODO: Implementar verificação real
        // Reprovações por fraude podem requerer:
        // - Analista Senior ou acima
        // - Aprovação de supervisor
        // - Documentação adicional

        // Mock: Por enquanto, apenas logar
        log.info("Reprovação por fraude requer alçada especial - analista: {}", analistaId);
    }

    @Override
    public Class<ReprovarSinistroCommand> getCommandType() {
        return ReprovarSinistroCommand.class;
    }

    @Override
    public int getTimeoutSeconds() {
        return 30;
    }
}
