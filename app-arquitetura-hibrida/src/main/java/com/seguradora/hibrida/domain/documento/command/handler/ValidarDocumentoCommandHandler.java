package com.seguradora.hibrida.domain.documento.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.documento.aggregate.DocumentoAggregate;
import com.seguradora.hibrida.domain.documento.command.ValidarDocumentoCommand;
import com.seguradora.hibrida.domain.documento.service.DocumentoStorageService;
import com.seguradora.hibrida.domain.documento.service.DocumentoValidatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler para processar comando de validação de documento.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Validar comando</li>
 *   <li>Carregar aggregate existente</li>
 *   <li>Verificar permissões do validador</li>
 *   <li>Validar documento completamente</li>
 *   <li>Marcar como validado e persistir</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidarDocumentoCommandHandler {

    private final AggregateRepository<DocumentoAggregate> aggregateRepository;
    private final DocumentoStorageService storageService;
    private final DocumentoValidatorService validatorService;

    /**
     * Processa o comando de validação de documento.
     *
     * @param command Comando de validação
     * @return ID do documento validado
     * @throws IllegalArgumentException se validações falharem
     * @throws IllegalStateException se documento não existir ou não puder ser validado
     * @throws RuntimeException se houver erro no processamento
     */
    @Transactional
    public String handle(ValidarDocumentoCommand command) {
        log.info("Iniciando validação de documento: {} por validador {}",
                command.getDocumentoId(), command.getValidadorNome());

        try {
            // 1. Validar comando
            command.validar();
            log.debug("Command validado com sucesso");

            // 2. Carregar aggregate existente
            DocumentoAggregate aggregate = aggregateRepository
                    .findById(command.getDocumentoId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Documento não encontrado: " + command.getDocumentoId()));

            log.debug("Aggregate carregado: status atual {}", aggregate.getStatus());

            // 3. Verificar se documento pode ser validado
            if (!aggregate.getDocumento().podeValidar()) {
                throw new IllegalStateException(
                        String.format("Documento no status %s não pode ser validado",
                                aggregate.getStatus()));
            }

            // 4. Validar integridade do documento
            validarIntegridadeDocumento(aggregate);
            log.debug("Integridade do documento verificada");

            // 5. Validar documento
            aggregate.validar(
                    command.getValidadorId(),
                    command.getValidadorNome(),
                    command.getObservacoes()
            );

            // 6. Persistir eventos
            aggregateRepository.save(aggregate);
            log.info("Documento validado com sucesso: {} por {}",
                    command.getDocumentoId(), command.getValidadorNome());

            return command.getDocumentoId();

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Erro ao validar documento: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("Erro ao validar documento {}: {}", command.getDocumentoId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao validar documento: " + e.getMessage(), e);
        }
    }

    /**
     * Valida a integridade completa do documento.
     */
    private void validarIntegridadeDocumento(DocumentoAggregate aggregate) {
        try {
            // Recuperar conteúdo do storage
            String path = aggregate.getDocumento().getConteudoPath();
            if (path == null || !storageService.exists(path)) {
                throw new IllegalStateException("Arquivo do documento não encontrado no storage");
            }

            byte[] conteudo = storageService.recuperar(path);

            // Validar hash (integridade)
            List<String> erros = validatorService.validarHash(
                    conteudo,
                    aggregate.getDocumento().getHash()
            );

            // Validar conteúdo
            erros.addAll(validatorService.validarConteudo(
                    conteudo,
                    aggregate.getTipo()
            ));

            // Validar documento completo
            List<String> errosDocumento = aggregate.getDocumento().validar();
            erros.addAll(errosDocumento);

            if (!erros.isEmpty()) {
                String mensagemErro = "Documento possui problemas que impedem validação:\n- " +
                        String.join("\n- ", erros);
                log.warn("Validação automática falhou: {}", mensagemErro);
                throw new IllegalStateException(mensagemErro);
            }

        } catch (Exception e) {
            log.error("Erro ao verificar integridade do documento: {}", e.getMessage(), e);
            throw new IllegalStateException("Erro ao verificar integridade: " + e.getMessage(), e);
        }
    }
}
