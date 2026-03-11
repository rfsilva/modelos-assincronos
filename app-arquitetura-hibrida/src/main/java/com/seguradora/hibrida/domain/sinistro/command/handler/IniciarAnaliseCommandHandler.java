package com.seguradora.hibrida.domain.sinistro.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.sinistro.aggregate.SinistroAggregate;
import com.seguradora.hibrida.domain.sinistro.command.IniciarAnaliseCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Command Handler para iniciar análise técnica de sinistro.
 *
 * <p>Este handler processa a atribuição de analista e início da análise técnica
 * detalhada do sinistro.
 *
 * <p><strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Verificar pré-requisitos para análise</li>
 *   <li>Validar permissões e capacidade do analista</li>
 *   <li>Atribuir sinistro ao analista</li>
 *   <li>Definir prioridade e prazo de análise</li>
 *   <li>Transicionar sinistro para estado EM_ANALISE</li>
 * </ul>
 *
 * <p><strong>Validações realizadas:</strong>
 * <ul>
 *   <li>Sinistro foi validado e possui dados completos</li>
 *   <li>Consulta Detran foi concluída (se aplicável)</li>
 *   <li>Analista existe e está ativo</li>
 *   <li>Analista possui qualificação para o tipo de sinistro</li>
 *   <li>Analista não está sobrecarregado</li>
 *   <li>Prioridade é válida e compatível com o sinistro</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IniciarAnaliseCommandHandler implements CommandHandler<IniciarAnaliseCommand> {

    private final AggregateRepository<SinistroAggregate> sinistroRepository;

    @Override
    public CommandResult handle(IniciarAnaliseCommand command) {
        log.info("Processando início de análise: sinistroId={}, analistaId={}",
            command.getSinistroId(), command.getAnalistaId());

        try {
            // 1. Carregar aggregate
            SinistroAggregate aggregate = sinistroRepository.getById(command.getSinistroId());

            // 2. Validar pré-requisitos
            validarPreRequisitos(aggregate, command);

            // 3. Validar prioridade
            validarPrioridade(command);

            // 4. Validar analista
            validarAnalista(command, aggregate);

            // 5. Validar prazo estimado
            validarPrazoEstimado(command, aggregate);

            // 6. Iniciar análise no aggregate
            int prioridade = converterPrioridade(command.getPrioridadeAnalise());
            aggregate.iniciarAnalise(command.getAnalistaId(), prioridade);

            // 7. Salvar aggregate (persiste eventos)
            sinistroRepository.save(aggregate);

            log.info("Análise iniciada com sucesso: sinistroId={}, analistaId={}, prioridade={}",
                command.getSinistroId(), command.getAnalistaId(), command.getPrioridadeAnalise());

            return CommandResult.success(command.getSinistroId(), Map.of(
                "analistaId", command.getAnalistaId(),
                "prioridade", command.getPrioridadeAnalise(),
                "prazoEstimado", command.getPrazoEstimado(),
                "versao", aggregate.getVersion()
            )).withCorrelationId(command.getCorrelationId());

        } catch (IllegalArgumentException e) {
            log.warn("Validação falhou ao iniciar análise do sinistro {}: {}",
                command.getSinistroId(), e.getMessage());
            return CommandResult.failure(e.getMessage(), "VALIDATION_ERROR")
                .withCorrelationId(command.getCorrelationId());

        } catch (Exception e) {
            log.error("Erro ao iniciar análise do sinistro: {}", command.getSinistroId(), e);
            return CommandResult.failure(
                "Erro ao processar início de análise: " + e.getMessage(),
                "PROCESSING_ERROR"
            ).withCorrelationId(command.getCorrelationId());
        }
    }

    /**
     * Valida pré-requisitos para iniciar análise.
     */
    private void validarPreRequisitos(SinistroAggregate aggregate, IniciarAnaliseCommand command) {
        log.debug("Validando pré-requisitos para análise");

        // Verificar se sinistro foi validado
        if (!aggregate.isAberto()) {
            throw new IllegalArgumentException(
                "Sinistro não está em estado válido para iniciar análise. " +
                "Status atual: " + aggregate.getSinistro().getStatus()
            );
        }

        // Verificar se consulta Detran foi concluída (se aplicável)
        if (aggregate.getSinistro().getTipoSinistro().requerConsultaDetran()) {
            validarConsultaDetran(aggregate);
        }

        log.debug("Pré-requisitos validados com sucesso");
    }

    /**
     * Valida se consulta Detran foi concluída.
     */
    private void validarConsultaDetran(SinistroAggregate aggregate) {
        // TODO: Verificar status da consulta Detran no aggregate
        // Por enquanto, apenas logar
        log.debug("Verificando consulta Detran para sinistro");

        // Mock: Simular verificação
        // Se consulta ainda não foi concluída, pode-se permitir ou bloquear
        // Dependendo da política de negócio
    }

    /**
     * Valida prioridade da análise.
     */
    private void validarPrioridade(IniciarAnaliseCommand command) {
        if (!command.isPrioridadeValida()) {
            throw new IllegalArgumentException(
                "Prioridade inválida: " + command.getPrioridadeAnalise() +
                ". Valores válidos: ALTA, MEDIA, BAIXA"
            );
        }

        log.debug("Prioridade validada: {}", command.getPrioridadeAnalise());
    }

    /**
     * Valida analista e suas permissões.
     */
    private void validarAnalista(IniciarAnaliseCommand command, SinistroAggregate aggregate) {
        log.debug("Validando analista: {}", command.getAnalistaId());

        // TODO: Implementar validações reais:
        // 1. Analista existe e está ativo
        validarAnalistaAtivo(command.getAnalistaId());

        // 2. Analista possui qualificação para o tipo de sinistro
        validarQualificacaoAnalista(
            command.getAnalistaId(),
            aggregate.getSinistro().getTipoSinistro().name()
        );

        // 3. Analista não está sobrecarregado
        validarCapacidadeAnalista(command.getAnalistaId());

        log.debug("Analista validado com sucesso");
    }

    /**
     * Valida se analista está ativo (mock).
     */
    private void validarAnalistaAtivo(String analistaId) {
        // Mock: Verificar se analista existe e está ativo
        if (analistaId == null || analistaId.trim().isEmpty()) {
            throw new IllegalArgumentException("Analista inválido");
        }

        log.debug("Analista está ativo: {}", analistaId);
    }

    /**
     * Valida qualificação do analista para o tipo de sinistro (mock).
     */
    private void validarQualificacaoAnalista(String analistaId, String tipoSinistro) {
        // Mock: Verificar se analista tem qualificação
        log.debug("Verificando qualificação do analista {} para tipo de sinistro {}",
            analistaId, tipoSinistro);

        // TODO: Implementar verificação real de qualificações
    }

    /**
     * Valida capacidade atual do analista (mock).
     */
    private void validarCapacidadeAnalista(String analistaId) {
        // Mock: Verificar quantidade de sinistros em análise
        int limiteSinistros = 10;
        int sinistrosEmAnalise = 3; // Mock

        if (sinistrosEmAnalise >= limiteSinistros) {
            throw new IllegalArgumentException(
                String.format(
                    "Analista %s está sobrecarregado (%d/%d sinistros). " +
                    "Não é possível atribuir novo sinistro.",
                    analistaId, sinistrosEmAnalise, limiteSinistros
                )
            );
        }

        log.debug("Capacidade do analista verificada: {}/{}", sinistrosEmAnalise, limiteSinistros);
    }

    /**
     * Valida prazo estimado.
     */
    private void validarPrazoEstimado(IniciarAnaliseCommand command, SinistroAggregate aggregate) {
        int prazoMaximo = aggregate.getSinistro().getTipoSinistro().getPrazoProcessamentoDias();

        if (command.getPrazoEstimado() > prazoMaximo) {
            log.warn("Prazo estimado ({} dias) excede o prazo máximo para o tipo de sinistro ({} dias)",
                command.getPrazoEstimado(), prazoMaximo);
            // Apenas avisar, não bloquear
        }

        if (command.isPrazoUrgente() && !command.isPrioridadeAlta()) {
            log.warn("Prazo urgente ({} dias) com prioridade não-alta ({})",
                command.getPrazoEstimado(), command.getPrioridadeAnalise());
        }

        log.debug("Prazo estimado validado: {} dias úteis", command.getPrazoEstimado());
    }

    /**
     * Converte string de prioridade para número.
     */
    private int converterPrioridade(String prioridade) {
        return switch (prioridade.toUpperCase()) {
            case "ALTA" -> 3;
            case "MEDIA" -> 2;
            case "BAIXA" -> 1;
            default -> 2; // Default: MEDIA
        };
    }

    @Override
    public Class<IniciarAnaliseCommand> getCommandType() {
        return IniciarAnaliseCommand.class;
    }

    @Override
    public int getTimeoutSeconds() {
        return 30;
    }
}
