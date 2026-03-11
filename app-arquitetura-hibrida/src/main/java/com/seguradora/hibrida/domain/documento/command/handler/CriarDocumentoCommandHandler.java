package com.seguradora.hibrida.domain.documento.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.documento.aggregate.DocumentoAggregate;
import com.seguradora.hibrida.domain.documento.command.CriarDocumentoCommand;
import com.seguradora.hibrida.domain.documento.service.DocumentoStorageService;
import com.seguradora.hibrida.domain.documento.service.DocumentoValidatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler para processar comando de criação de documento.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Validar comando e conteúdo do documento</li>
 *   <li>Salvar conteúdo no storage</li>
 *   <li>Criar aggregate e persistir eventos</li>
 *   <li>Tratamento completo de erros</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CriarDocumentoCommandHandler {

    private final AggregateRepository<DocumentoAggregate> aggregateRepository;
    private final DocumentoStorageService storageService;
    private final DocumentoValidatorService validatorService;

    /**
     * Processa o comando de criação de documento.
     *
     * @param command Comando de criação
     * @return ID do documento criado
     * @throws IllegalArgumentException se validações falharem
     * @throws RuntimeException se houver erro no processamento
     */
    @Transactional
    public String handle(CriarDocumentoCommand command) {
        log.info("Iniciando criação de documento: {} (tipo: {}, tamanho: {})",
                command.getNome(), command.getTipo(), command.getTamanhoFormatado());

        try {
            // 1. Validar comando
            command.validar();
            log.debug("Command validado com sucesso");

            // 2. Validar conteúdo do documento
            validarConteudo(command);
            log.debug("Conteúdo validado com sucesso");

            // 3. Salvar no storage
            String conteudoPath = storageService.salvar(
                    command.getDocumentoId(),
                    1, // Versão inicial
                    command.getSinistroId(),
                    command.getConteudo()
            );
            log.debug("Documento salvo no storage: {}", conteudoPath);

            // 4. Criar aggregate
            DocumentoAggregate aggregate = new DocumentoAggregate(
                    command.getDocumentoId(),
                    command.getNome(),
                    command.getTipo(),
                    command.getConteudo(),
                    command.getFormato(),
                    conteudoPath,
                    command.getSinistroId(),
                    command.getOperadorId()
            );

            // 5. Persistir eventos
            aggregateRepository.save(aggregate);
            log.info("Documento criado com sucesso: {} (ID: {})",
                    command.getNome(), command.getDocumentoId());

            return command.getDocumentoId();

        } catch (IllegalArgumentException e) {
            log.error("Erro de validação ao criar documento: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("Erro ao criar documento {}: {}", command.getNome(), e.getMessage(), e);

            // Tentar fazer rollback do storage
            tentarRemoverDoStorage(command.getDocumentoId(), command.getSinistroId());

            throw new RuntimeException("Erro ao criar documento: " + e.getMessage(), e);
        }
    }

    /**
     * Valida o conteúdo do documento.
     */
    private void validarConteudo(CriarDocumentoCommand command) {
        // Validar tipo e formato
        List<String> erros = validatorService.validarTipo(command.getTipo(), command.getFormato());

        // Validar tamanho
        erros.addAll(validatorService.validarTamanho(command.getTamanho(), command.getTipo()));

        // Validar conteúdo (magic bytes, estrutura)
        erros.addAll(validatorService.validarConteudo(command.getConteudo(), command.getTipo()));

        // Verificar se formato real corresponde ao declarado
        if (!validatorService.verificarFormatoReal(command.getConteudo(), command.getFormato())) {
            erros.add("Formato declarado não corresponde ao formato real do arquivo");
        }

        if (!erros.isEmpty()) {
            String mensagemErro = "Validação de documento falhou:\n- " +
                    String.join("\n- ", erros);
            log.warn("Documento rejeitado na validação: {}", mensagemErro);
            throw new IllegalArgumentException(mensagemErro);
        }
    }

    /**
     * Tenta remover arquivo do storage em caso de erro.
     */
    private void tentarRemoverDoStorage(String documentoId, String sinistroId) {
        try {
            String[] arquivos = storageService.listarDocumentosSinistro(sinistroId);
            for (String arquivo : arquivos) {
                if (arquivo.contains(documentoId)) {
                    storageService.deletar(arquivo);
                    log.debug("Arquivo removido do storage após erro: {}", arquivo);
                }
            }
        } catch (Exception e) {
            log.warn("Não foi possível remover arquivo do storage após erro: {}", e.getMessage());
        }
    }
}
