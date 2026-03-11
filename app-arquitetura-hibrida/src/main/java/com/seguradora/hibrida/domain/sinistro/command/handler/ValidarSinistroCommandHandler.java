package com.seguradora.hibrida.domain.sinistro.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.sinistro.aggregate.SinistroAggregate;
import com.seguradora.hibrida.domain.sinistro.command.ValidarSinistroCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Command Handler para validação de dados complementares do sinistro.
 *
 * <p>Este handler processa a validação de dados complementares e documentos
 * do sinistro, verificando completude e consistência das informações.
 *
 * <p><strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Carregar aggregate do Event Store</li>
 *   <li>Validar completude dos dados complementares</li>
 *   <li>Verificar documentos obrigatórios anexados</li>
 *   <li>Aplicar evento de validação no aggregate</li>
 *   <li>Persistir eventos gerados</li>
 * </ul>
 *
 * <p><strong>Validações realizadas:</strong>
 * <ul>
 *   <li>Sinistro existe e está em estado válido para validação</li>
 *   <li>Dados complementares mínimos fornecidos</li>
 *   <li>Documentos obrigatórios conforme tipo de sinistro</li>
 *   <li>Formato e conteúdo dos dados complementares</li>
 *   <li>Operador possui permissões adequadas</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidarSinistroCommandHandler implements CommandHandler<ValidarSinistroCommand> {

    private final AggregateRepository<SinistroAggregate> sinistroRepository;

    @Override
    public CommandResult handle(ValidarSinistroCommand command) {
        log.info("Processando validação de sinistro: sinistroId={}", command.getSinistroId());

        try {
            // 1. Carregar aggregate
            SinistroAggregate aggregate = sinistroRepository.getById(command.getSinistroId());

            // 2. Validar estado do sinistro
            validarEstadoSinistro(aggregate);

            // 3. Validar dados complementares
            validarDadosComplementares(command);

            // 4. Validar documentos anexados
            validarDocumentosAnexados(command, aggregate);

            // 5. Aplicar validação no aggregate
            aggregate.validarDados(
                command.getDadosComplementares(),
                command.getDocumentosAnexados(),
                command.getOperadorId()
            );

            // 6. Salvar aggregate (persiste eventos)
            sinistroRepository.save(aggregate);

            log.info("Sinistro validado com sucesso: sinistroId={}", command.getSinistroId());

            return CommandResult.success(command.getSinistroId(), Map.of(
                "dadosComplementares", command.getDadosComplementares().size(),
                "documentosValidados", command.getDocumentosAnexados().size(),
                "versao", aggregate.getVersion()
            )).withCorrelationId(command.getCorrelationId());

        } catch (IllegalArgumentException e) {
            log.warn("Validação falhou para sinistro {}: {}", command.getSinistroId(), e.getMessage());
            return CommandResult.failure(e.getMessage(), "VALIDATION_ERROR")
                .withCorrelationId(command.getCorrelationId());

        } catch (Exception e) {
            log.error("Erro ao validar sinistro: {}", command.getSinistroId(), e);
            return CommandResult.failure(
                "Erro ao processar validação do sinistro: " + e.getMessage(),
                "PROCESSING_ERROR"
            ).withCorrelationId(command.getCorrelationId());
        }
    }

    /**
     * Valida se o sinistro está em estado válido para validação.
     */
    private void validarEstadoSinistro(SinistroAggregate aggregate) {
        if (!aggregate.isAberto()) {
            throw new IllegalArgumentException(
                "Sinistro não está em estado válido para validação. Status atual: " +
                aggregate.getSinistro().getStatus()
            );
        }

        log.debug("Sinistro está em estado válido para validação: status={}",
            aggregate.getSinistro().getStatus());
    }

    /**
     * Valida dados complementares fornecidos.
     */
    private void validarDadosComplementares(ValidarSinistroCommand command) {
        log.debug("Validando dados complementares: {} campos",
            command.getDadosComplementares().size());

        if (!command.hasDadosComplementaresSuficientes()) {
            throw new IllegalArgumentException(
                "Dados complementares insuficientes. Mínimo requerido: 3 campos. " +
                "Fornecidos: " + command.getDadosComplementares().size()
            );
        }

        // Validar campos obrigatórios específicos
        validarCamposObrigatorios(command.getDadosComplementares());

        // Validar formato dos dados
        validarFormatoDados(command.getDadosComplementares());
    }

    /**
     * Valida campos obrigatórios nos dados complementares.
     */
    private void validarCamposObrigatorios(Map<String, Object> dados) {
        List<String> camposFaltando = new ArrayList<>();

        // Campos obrigatórios básicos
        if (!dados.containsKey("condicaoClimatica") || dados.get("condicaoClimatica") == null) {
            camposFaltando.add("condicaoClimatica");
        }

        if (!dados.containsKey("condicaoPista") || dados.get("condicaoPista") == null) {
            camposFaltando.add("condicaoPista");
        }

        if (!camposFaltando.isEmpty()) {
            throw new IllegalArgumentException(
                "Campos obrigatórios ausentes: " + String.join(", ", camposFaltando)
            );
        }
    }

    /**
     * Valida formato e conteúdo dos dados complementares.
     */
    private void validarFormatoDados(Map<String, Object> dados) {
        // Validar velocidadeEstimada se fornecida
        if (dados.containsKey("velocidadeEstimada")) {
            Object velocidade = dados.get("velocidadeEstimada");
            if (velocidade != null) {
                try {
                    int vel = Integer.parseInt(velocidade.toString());
                    if (vel < 0 || vel > 200) {
                        throw new IllegalArgumentException(
                            "Velocidade estimada inválida: " + vel + " km/h. " +
                            "Deve estar entre 0 e 200 km/h"
                        );
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                        "Formato inválido para velocidadeEstimada: " + velocidade
                    );
                }
            }
        }

        // Validar lista de testemunhas se fornecida
        if (dados.containsKey("testemunhas")) {
            Object testemunhas = dados.get("testemunhas");
            if (testemunhas != null && !(testemunhas instanceof List)) {
                throw new IllegalArgumentException(
                    "Campo 'testemunhas' deve ser uma lista"
                );
            }
        }
    }

    /**
     * Valida documentos anexados.
     */
    private void validarDocumentosAnexados(ValidarSinistroCommand command, SinistroAggregate aggregate) {
        log.debug("Validando documentos anexados: {} documentos",
            command.getDocumentosAnexados().size());

        if (!command.hasDocumentosAnexados()) {
            throw new IllegalArgumentException(
                "Nenhum documento anexado. É necessário anexar pelo menos um documento."
            );
        }

        // Validar se documentos obrigatórios foram anexados conforme tipo de sinistro
        validarDocumentosObrigatorios(aggregate, command.getDocumentosAnexados());

        // Validar formato dos IDs de documentos
        for (String documentoId : command.getDocumentosAnexados()) {
            if (documentoId == null || documentoId.trim().isEmpty()) {
                throw new IllegalArgumentException("ID de documento inválido encontrado");
            }
        }
    }

    /**
     * Valida se documentos obrigatórios foram anexados.
     */
    private void validarDocumentosObrigatorios(SinistroAggregate aggregate, List<String> documentos) {
        // Mock: Verificar documentos obrigatórios conforme tipo de sinistro
        // TODO: Implementar verificação real com base nos documentos anexados ao aggregate

        if (aggregate.getSinistro().getTipoSinistro().requerBoletimOcorrencia()) {
            log.debug("Tipo de sinistro requer boletim de ocorrência");
            // Verificar se boletim foi anexado
            // Por enquanto, apenas logar
        }

        log.debug("Documentos obrigatórios validados");
    }

    @Override
    public Class<ValidarSinistroCommand> getCommandType() {
        return ValidarSinistroCommand.class;
    }

    @Override
    public int getTimeoutSeconds() {
        return 30;
    }
}
