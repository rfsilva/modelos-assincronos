package com.seguradora.hibrida.domain.sinistro.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.sinistro.aggregate.SinistroAggregate;
import com.seguradora.hibrida.domain.sinistro.command.AprovarSinistroCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Command Handler para aprovação de sinistros.
 *
 * <p>Este handler processa a aprovação final do sinistro após análise técnica,
 * validando permissões, alçadas e valores de indenização.
 *
 * <p><strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Validar permissões e alçada do analista</li>
 *   <li>Verificar limites de indenização da apólice</li>
 *   <li>Validar justificativa e documentação</li>
 *   <li>Aprovar sinistro no aggregate</li>
 *   <li>Iniciar processo de pagamento</li>
 * </ul>
 *
 * <p><strong>Validações realizadas:</strong>
 * <ul>
 *   <li>Sinistro está em análise e completo</li>
 *   <li>Analista possui alçada para o valor aprovado</li>
 *   <li>Valor de indenização é válido e calculado corretamente</li>
 *   <li>Valor não excede limites da apólice</li>
 *   <li>Justificativa é adequada e completa</li>
 *   <li>Documentos comprobatórios foram anexados</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AprovarSinistroCommandHandler implements CommandHandler<AprovarSinistroCommand> {

    private final AggregateRepository<SinistroAggregate> sinistroRepository;

    // Limites de alçada (mock - devem vir de configuração ou domínio)
    private static final BigDecimal ALCADA_ANALISTA_JR = new BigDecimal("10000.00");
    private static final BigDecimal ALCADA_ANALISTA_PL = new BigDecimal("50000.00");
    private static final BigDecimal ALCADA_ANALISTA_SR = new BigDecimal("100000.00");

    @Override
    public CommandResult handle(AprovarSinistroCommand command) {
        log.info("Processando aprovação de sinistro: sinistroId={}, valorIndenizacao={}",
            command.getSinistroId(),
            command.getValorIndenizacao() != null ? command.getValorIndenizacao().getValorLiquido() : "null");

        try {
            // 1. Carregar aggregate
            SinistroAggregate aggregate = sinistroRepository.getById(command.getSinistroId());

            // 2. Validar estado do sinistro
            validarEstadoSinistro(aggregate);

            // 3. Validar valor de indenização
            validarValorIndenizacao(command, aggregate);

            // 4. Validar alçada do analista
            validarAlcadaAnalista(command);

            // 5. Validar justificativa
            validarJustificativa(command);

            // 6. Validar documentos comprobatórios
            validarDocumentosComprobatorios(command);

            // 7. Aprovar no aggregate
            aggregate.aprovar(
                command.getValorIndenizacao(),
                command.getJustificativa(),
                command.getAnalistaId(),
                command.getDocumentosComprobatorios()
            );

            // 8. Salvar aggregate (persiste eventos)
            sinistroRepository.save(aggregate);

            log.info("Sinistro aprovado com sucesso: sinistroId={}, valorLiquido={}",
                command.getSinistroId(), command.getValorIndenizacao().getValorLiquido());

            return CommandResult.success(command.getSinistroId(), Map.of(
                "valorBruto", command.getValorIndenizacao().getValorBruto(),
                "valorLiquido", command.getValorIndenizacao().getValorLiquido(),
                "analistaId", command.getAnalistaId(),
                "versao", aggregate.getVersion()
            )).withCorrelationId(command.getCorrelationId());

        } catch (IllegalArgumentException e) {
            log.warn("Validação falhou ao aprovar sinistro {}: {}", command.getSinistroId(), e.getMessage());
            return CommandResult.failure(e.getMessage(), "VALIDATION_ERROR")
                .withCorrelationId(command.getCorrelationId());

        } catch (Exception e) {
            log.error("Erro ao aprovar sinistro: {}", command.getSinistroId(), e);
            return CommandResult.failure(
                "Erro ao processar aprovação do sinistro: " + e.getMessage(),
                "PROCESSING_ERROR"
            ).withCorrelationId(command.getCorrelationId());
        }
    }

    /**
     * Valida se o sinistro está em estado válido para aprovação.
     */
    private void validarEstadoSinistro(SinistroAggregate aggregate) {
        if (!aggregate.isAberto()) {
            throw new IllegalArgumentException(
                "Sinistro não está em estado válido para aprovação. " +
                "Status atual: " + aggregate.getSinistro().getStatus()
            );
        }

        log.debug("Estado do sinistro validado: status={}", aggregate.getSinistro().getStatus());
    }

    /**
     * Valida valor de indenização.
     */
    private void validarValorIndenizacao(AprovarSinistroCommand command, SinistroAggregate aggregate) {
        log.debug("Validando valor de indenização");

        // Validar se valor é válido
        if (!command.isValorIndenizacaoValido()) {
            throw new IllegalArgumentException(
                "Valor de indenização inválido ou negativo após deduções"
            );
        }

        BigDecimal valorLiquido = command.getValorIndenizacao().getValorLiquido();

        // Validar valor mínimo
        BigDecimal valorMinimo = new BigDecimal("100.00");
        if (valorLiquido.compareTo(valorMinimo) < 0) {
            throw new IllegalArgumentException(
                String.format(
                    "Valor de indenização muito baixo: R$ %.2f. Mínimo: R$ %.2f",
                    valorLiquido, valorMinimo
                )
            );
        }

        // Validar se valor não excede limite da apólice
        validarLimiteApolice(valorLiquido, aggregate);

        log.debug("Valor de indenização validado: valorBruto={}, valorLiquido={}",
            command.getValorIndenizacao().getValorBruto(), valorLiquido);
    }

    /**
     * Valida se valor não excede limite da apólice (mock).
     */
    private void validarLimiteApolice(BigDecimal valorIndenizacao, SinistroAggregate aggregate) {
        // TODO: Consultar limite real da apólice
        BigDecimal limiteApolice = new BigDecimal("500000.00"); // Mock

        if (valorIndenizacao.compareTo(limiteApolice) > 0) {
            throw new IllegalArgumentException(
                String.format(
                    "Valor de indenização (R$ %.2f) excede o limite da apólice (R$ %.2f)",
                    valorIndenizacao, limiteApolice
                )
            );
        }

        log.debug("Valor dentro do limite da apólice: {} / {}", valorIndenizacao, limiteApolice);
    }

    /**
     * Valida alçada do analista para o valor aprovado.
     */
    private void validarAlcadaAnalista(AprovarSinistroCommand command) {
        log.debug("Validando alçada do analista: {}", command.getAnalistaId());

        BigDecimal valorLiquido = command.getValorIndenizacao().getValorLiquido();

        // TODO: Buscar alçada real do analista do sistema
        // Mock: Assumir que analista é Senior
        BigDecimal alcadaAnalista = obterAlcadaAnalista(command.getAnalistaId());

        if (valorLiquido.compareTo(alcadaAnalista) > 0) {
            throw new IllegalArgumentException(
                String.format(
                    "Analista %s não possui alçada para aprovar valor de R$ %.2f. " +
                    "Alçada máxima: R$ %.2f. É necessária aprovação de nível superior.",
                    command.getAnalistaId(), valorLiquido, alcadaAnalista
                )
            );
        }

        log.debug("Alçada do analista validada: {} >= {}", alcadaAnalista, valorLiquido);
    }

    /**
     * Obtém alçada do analista (mock).
     */
    private BigDecimal obterAlcadaAnalista(String analistaId) {
        // Mock: Determinar alçada baseado em alguma lógica
        // TODO: Implementar consulta real ao sistema de RH/Permissões

        // Por enquanto, retornar alçada Senior
        return ALCADA_ANALISTA_SR;
    }

    /**
     * Valida justificativa da aprovação.
     */
    private void validarJustificativa(AprovarSinistroCommand command) {
        if (!command.isJustificativaAdequada()) {
            throw new IllegalArgumentException(
                "Justificativa inadequada. Deve conter pelo menos 20 palavras e " +
                "explicar detalhadamente os critérios técnicos da aprovação."
            );
        }

        log.debug("Justificativa validada: {} caracteres", command.getJustificativa().length());
    }

    /**
     * Valida documentos comprobatórios.
     */
    private void validarDocumentosComprobatorios(AprovarSinistroCommand command) {
        if (!command.hasDocumentosComprobatoriosSuficientes()) {
            throw new IllegalArgumentException(
                "Documentos comprobatórios insuficientes. " +
                "São necessários pelo menos 2 documentos para aprovação."
            );
        }

        log.debug("Documentos comprobatórios validados: {} documentos",
            command.getDocumentosComprobatorios().size());
    }

    @Override
    public Class<AprovarSinistroCommand> getCommandType() {
        return AprovarSinistroCommand.class;
    }

    @Override
    public int getTimeoutSeconds() {
        return 30;
    }
}
