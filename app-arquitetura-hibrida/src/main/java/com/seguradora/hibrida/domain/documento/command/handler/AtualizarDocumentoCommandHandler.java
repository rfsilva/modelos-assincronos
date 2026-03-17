package com.seguradora.hibrida.domain.documento.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.documento.aggregate.DocumentoAggregate;
import com.seguradora.hibrida.domain.documento.command.AtualizarDocumentoCommand;
import com.seguradora.hibrida.domain.documento.service.DocumentoStorageService;
import com.seguradora.hibrida.domain.documento.service.DocumentoValidatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler para processar comando de atualização de documento.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Validar comando e novo conteúdo</li>
 *   <li>Carregar aggregate existente</li>
 *   <li>Salvar nova versão no storage</li>
 *   <li>Preservar versão anterior (backup automático)</li>
 *   <li>Atualizar aggregate e persistir eventos</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AtualizarDocumentoCommandHandler {

    private final AggregateRepository<DocumentoAggregate> aggregateRepository;
    private final DocumentoStorageService storageService;
    private final DocumentoValidatorService validatorService;

    /**
     * Processa o comando de atualização de documento.
     *
     * @param command Comando de atualização
     * @return Nova versão do documento
     * @throws IllegalArgumentException se validações falharem
     * @throws IllegalStateException se documento não existir ou não puder ser atualizado
     * @throws RuntimeException se houver erro no processamento
     */
    @Transactional
    public int handle(AtualizarDocumentoCommand command) {
        log.info("Iniciando atualização de documento: {} (motivo: {}, tamanho: {})",
                command.getDocumentoId(), command.getMotivo(), command.getTamanhoFormatado());

        try {
            // 1. Validar comando
            command.validar();
            log.debug("Command validado com sucesso");

            // 2. Carregar aggregate existente
            DocumentoAggregate aggregate = aggregateRepository
                    .findById(command.getDocumentoId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Documento não encontrado: " + command.getDocumentoId()));

            log.debug("Aggregate carregado: versão atual {}", aggregate.getVersion());

            // 3. Validar novo conteúdo
            validarNovoConteudo(aggregate, command);
            log.debug("Novo conteúdo validado com sucesso");

            // 4. Fazer backup da versão anterior
            String pathAnterior = aggregate.getDocumento().getConteudoPath();
            if (pathAnterior != null && storageService.exists(pathAnterior)) {
                storageService.backup(pathAnterior);
                log.debug("Backup da versão anterior criado");
            }

            // 5. Salvar nova versão no storage
            int novaVersao = aggregate.getDocumento().getNumeroVersao() + 1;
            String novoPath = storageService.salvar(
                    command.getDocumentoId(),
                    novaVersao,
                    aggregate.getDocumento().getSinistroId(),
                    command.getNovoConteudo()
            );
            log.debug("Nova versão salva no storage: {}", novoPath);

            // 6. Atualizar aggregate
            aggregate.atualizar(
                    command.getNovoConteudo(),
                    command.getMotivo(),
                    novoPath,
                    command.getOperadorId()
            );

            // 7. Persistir eventos
            aggregateRepository.save(aggregate);
            log.info("Documento atualizado com sucesso: {} - Nova versão: {}",
                    command.getDocumentoId(), novaVersao);

            return novaVersao;

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Erro de validação ao atualizar documento: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("Erro ao atualizar documento {}: {}", command.getDocumentoId(), e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar documento: " + e.getMessage(), e);
        }
    }

    /**
     * Valida o novo conteúdo do documento.
     */
    private void validarNovoConteudo(DocumentoAggregate aggregate, AtualizarDocumentoCommand command) {
        // Validar tamanho - usar ArrayList mutável
        java.util.List<String> erros = new java.util.ArrayList<>(validatorService.validarTamanho(
                command.getTamanho(),
                aggregate.getTipo()
        ));

        // Validar conteúdo (magic bytes, estrutura)
        erros.addAll(validatorService.validarConteudo(
                command.getNovoConteudo(),
                aggregate.getTipo()
        ));

        // Verificar formato real
        String formato = aggregate.getDocumento().getFormato();
        if (!validatorService.verificarFormatoReal(command.getNovoConteudo(), formato)) {
            erros.add("Formato do novo arquivo não corresponde ao formato original");
        }

        if (!erros.isEmpty()) {
            String mensagemErro = "Validação do novo conteúdo falhou:\n- " +
                    String.join("\n- ", erros);
            log.warn("Nova versão rejeitada na validação: {}", mensagemErro);
            throw new IllegalArgumentException(mensagemErro);
        }
    }
}
