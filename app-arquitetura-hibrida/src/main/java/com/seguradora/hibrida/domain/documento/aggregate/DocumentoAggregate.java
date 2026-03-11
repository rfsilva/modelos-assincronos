package com.seguradora.hibrida.domain.documento.aggregate;

import com.seguradora.hibrida.aggregate.AggregateRoot;
import com.seguradora.hibrida.aggregate.EventSourcingHandler;
import com.seguradora.hibrida.domain.documento.event.*;
import com.seguradora.hibrida.domain.documento.model.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;

/**
 * Aggregate Root para o domínio de Documentos.
 *
 * <p>Gerencia o ciclo de vida completo de documentos com versionamento:
 * <ul>
 *   <li>Criação e atualização de documentos</li>
 *   <li>Versionamento automático</li>
 *   <li>Validação e rejeição</li>
 *   <li>Assinaturas digitais</li>
 *   <li>Controle de integridade via hash</li>
 * </ul>
 *
 * <p>Implementa Event Sourcing completo para rastreabilidade total.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Getter
public class DocumentoAggregate extends AggregateRoot {

    private Documento documento;

    /**
     * Construtor padrão para reconstrução do histórico.
     */
    public DocumentoAggregate() {
        super();
    }

    /**
     * Construtor para criação de novo documento.
     *
     * @param id ID único do documento
     * @param nome Nome do arquivo
     * @param tipo Tipo do documento
     * @param conteudo Conteúdo em bytes
     * @param formato MIME type do arquivo
     * @param conteudoPath Path de armazenamento
     * @param sinistroId ID do sinistro vinculado
     * @param operadorId ID do operador criador
     */
    public DocumentoAggregate(String id, String nome, TipoDocumento tipo, byte[] conteudo,
                              String formato, String conteudoPath, String sinistroId,
                              String operadorId) {
        super(id);

        // Validações de criação
        validarCamposObrigatorios(nome, tipo, conteudo, formato, sinistroId, operadorId);
        validarTamanho(conteudo.length, tipo);
        validarFormato(formato, tipo);

        // Calcular hash para integridade
        HashDocumento hash = HashDocumento.calcular(conteudo);

        // Criar versão inicial
        VersaoDocumento versaoInicial = VersaoDocumento.versaoInicial(hash.getValor(), operadorId);

        // Criar e aplicar evento
        DocumentoCriadoEvent event = new DocumentoCriadoEvent(
                id, nome, tipo, conteudo.length, hash.getValor(),
                formato, conteudoPath, sinistroId, operadorId
        );

        applyEvent(event);

        log.info("Documento criado: {} (tipo: {}, tamanho: {} bytes)", nome, tipo, conteudo.length);
    }

    /**
     * Atualiza o documento com novo conteúdo (gera nova versão).
     *
     * @param novoConteudo Novo conteúdo em bytes
     * @param motivo Descrição da alteração
     * @param novoPath Novo path de armazenamento
     * @param operadorId ID do operador responsável
     */
    public void atualizar(byte[] novoConteudo, String motivo, String novoPath, String operadorId) {
        // Validações
        if (documento == null) {
            throw new IllegalStateException("Documento não inicializado");
        }

        if (!documento.podeAtualizar()) {
            throw new IllegalStateException(
                    String.format("Documento no status %s não pode ser atualizado", documento.getStatus()));
        }

        Objects.requireNonNull(novoConteudo, "Novo conteúdo não pode ser nulo");
        Objects.requireNonNull(motivo, "Motivo da atualização é obrigatório");
        Objects.requireNonNull(operadorId, "Operador ID não pode ser nulo");

        if (motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo da atualização não pode ser vazio");
        }

        validarTamanho(novoConteudo.length, documento.getTipo());

        // Calcular novo hash
        HashDocumento novoHash = HashDocumento.calcular(novoConteudo);

        // Verificar se houve mudança real no conteúdo
        if (novoHash.getValor().equals(documento.getHashValor())) {
            throw new IllegalArgumentException("Conteúdo não foi modificado");
        }

        // Criar nova versão
        VersaoDocumento novaVersao = documento.getVersao().proximaVersao(
                novoHash.getValor(), motivo, operadorId);

        // Criar e aplicar evento
        DocumentoAtualizadoEvent event = new DocumentoAtualizadoEvent(
                getId(), novaVersao.getNumero(), motivo,
                documento.getHashValor(), novoHash.getValor(),
                novoConteudo.length, novoPath, operadorId
        );

        applyEvent(event);

        log.info("Documento atualizado: {} - Nova versão: {} (motivo: {})",
                documento.getNome(), novaVersao.getNumero(), motivo);
    }

    /**
     * Valida o documento após análise.
     *
     * @param validadorId ID do validador
     * @param validadorNome Nome do validador
     * @param observacoes Observações da validação (opcional)
     */
    public void validar(String validadorId, String validadorNome, String observacoes) {
        // Validações
        if (documento == null) {
            throw new IllegalStateException("Documento não inicializado");
        }

        if (!documento.podeValidar()) {
            throw new IllegalStateException(
                    String.format("Documento no status %s não pode ser validado", documento.getStatus()));
        }

        Objects.requireNonNull(validadorId, "Validador ID não pode ser nulo");

        // Verificar se atende requisitos
        List<String> erros = documento.validar();
        if (!erros.isEmpty()) {
            throw new IllegalStateException(
                    "Documento possui erros de validação: " + String.join(", ", erros));
        }

        // Criar e aplicar evento
        DocumentoValidadoEvent event = new DocumentoValidadoEvent(
                getId(), validadorId, validadorNome, observacoes, false, "Validação Manual"
        );

        applyEvent(event);

        log.info("Documento validado: {} por {}", documento.getNome(), validadorNome);
    }

    /**
     * Rejeita o documento com justificativa.
     *
     * @param motivo Motivo da rejeição
     * @param problemasIdentificados Lista de problemas
     * @param validadorId ID do validador
     * @param validadorNome Nome do validador
     * @param acoesCorretivas Ações sugeridas
     */
    public void rejeitar(String motivo, List<String> problemasIdentificados,
                        String validadorId, String validadorNome, String acoesCorretivas) {
        // Validações
        if (documento == null) {
            throw new IllegalStateException("Documento não inicializado");
        }

        if (!documento.podeRejeitar()) {
            throw new IllegalStateException(
                    String.format("Documento no status %s não pode ser rejeitado", documento.getStatus()));
        }

        Objects.requireNonNull(motivo, "Motivo da rejeição é obrigatório");
        Objects.requireNonNull(validadorId, "Validador ID não pode ser nulo");

        if (motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo da rejeição não pode ser vazio");
        }

        // Criar e aplicar evento
        DocumentoRejeitadoEvent event = new DocumentoRejeitadoEvent(
                getId(), motivo, problemasIdentificados, validadorId,
                validadorNome, acoesCorretivas, true
        );

        applyEvent(event);

        log.warn("Documento rejeitado: {} - Motivo: {}", documento.getNome(), motivo);
    }

    /**
     * Adiciona assinatura digital ao documento.
     *
     * @param assinatura Assinatura digital a ser aplicada
     */
    public void assinarDigitalmente(AssinaturaDigital assinatura) {
        // Validações
        if (documento == null) {
            throw new IllegalStateException("Documento não inicializado");
        }

        Objects.requireNonNull(assinatura, "Assinatura não pode ser nula");

        if (!assinatura.isValida()) {
            throw new IllegalArgumentException("Assinatura inválida ou expirada");
        }

        // Verificar se o tipo de documento requer assinatura
        if (!documento.getTipo().requerAssinatura()) {
            log.warn("Documento do tipo {} não requer assinatura, mas será aceita",
                    documento.getTipo());
        }

        // Criar e aplicar evento
        DocumentoAssinadoEvent event = new DocumentoAssinadoEvent(
                getId(),
                assinatura.getTipo(),
                assinatura.getAlgoritmo(),
                assinatura.getCertificado(),
                assinatura.getAssinanteNome(),
                assinatura.getAssinanteCpf(),
                assinatura.getValidadeInicio(),
                assinatura.getValidadeFim(),
                documento.getHashValor()
        );

        applyEvent(event);

        log.info("Assinatura {} aplicada ao documento {} por {}",
                assinatura.getTipo(), documento.getNome(), assinatura.getAssinanteNome());
    }

    // ==================== Event Handlers ====================

    @EventSourcingHandler
    protected void on(DocumentoCriadoEvent event) {
        HashDocumento hash = HashDocumento.fromHex(event.getHash());
        VersaoDocumento versao = VersaoDocumento.versaoInicial(event.getHash(), event.getOperadorId());

        this.documento = Documento.builder()
                .id(event.getDocumentoId())
                .nome(event.getNome())
                .tipo(event.getTipo())
                .versao(versao)
                .tamanho(event.getTamanho())
                .hash(hash)
                .formato(event.getFormato())
                .conteudoPath(event.getConteudoPath())
                .sinistroId(event.getSinistroId())
                .status(StatusDocumento.PENDENTE)
                .criadoEm(event.getTimestamp())
                .atualizadoEm(event.getTimestamp())
                .criadoPor(event.getOperadorId())
                .atualizadoPor(event.getOperadorId())
                .build();
    }

    @EventSourcingHandler
    protected void on(DocumentoAtualizadoEvent event) {
        HashDocumento novoHash = HashDocumento.fromHex(event.getHashNovo());
        VersaoDocumento novaVersao = documento.getVersao().proximaVersao(
                event.getHashNovo(), event.getMotivo(), event.getOperadorId());

        this.documento = documento.toBuilder()
                .versao(novaVersao)
                .hash(novoHash)
                .tamanho(event.getNovoTamanho())
                .conteudoPath(event.getNovoConteudoPath())
                .atualizadoEm(event.getTimestamp())
                .atualizadoPor(event.getOperadorId())
                .build();
    }

    @EventSourcingHandler
    protected void on(DocumentoValidadoEvent event) {
        this.documento = documento.toBuilder()
                .status(StatusDocumento.VALIDADO)
                .atualizadoEm(event.getTimestamp())
                .atualizadoPor(event.getValidadorId())
                .metadado("validadorNome", event.getValidadorNome())
                .metadado("observacoesValidacao", event.getObservacoes())
                .build();
    }

    @EventSourcingHandler
    protected void on(DocumentoRejeitadoEvent event) {
        this.documento = documento.toBuilder()
                .status(StatusDocumento.REJEITADO)
                .atualizadoEm(event.getTimestamp())
                .atualizadoPor(event.getValidadorId())
                .metadado("motivoRejeicao", event.getMotivo())
                .metadado("validadorNome", event.getValidadorNome())
                .metadado("acoesCorretivas", event.getAcoesCorretivas())
                .build();
    }

    @EventSourcingHandler
    protected void on(DocumentoAssinadoEvent event) {
        AssinaturaDigital assinatura = AssinaturaDigital.digital(
                event.getAlgoritmo(),
                event.getCertificado(),
                event.getAssinanteNome(),
                event.getAssinanteCpf(),
                event.getValidadeInicio(),
                event.getValidadeFim()
        );

        this.documento = documento.toBuilder()
                .assinatura(assinatura)
                .atualizadoEm(event.getTimestamp())
                .build();
    }

    // ==================== Métodos de Validação ====================

    private void validarCamposObrigatorios(String nome, TipoDocumento tipo, byte[] conteudo,
                                          String formato, String sinistroId, String operadorId) {
        Objects.requireNonNull(nome, "Nome do documento não pode ser nulo");
        Objects.requireNonNull(tipo, "Tipo do documento não pode ser nulo");
        Objects.requireNonNull(conteudo, "Conteúdo não pode ser nulo");
        Objects.requireNonNull(formato, "Formato não pode ser nulo");
        Objects.requireNonNull(sinistroId, "Sinistro ID não pode ser nulo");
        Objects.requireNonNull(operadorId, "Operador ID não pode ser nulo");

        if (nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do documento não pode ser vazio");
        }

        if (conteudo.length == 0) {
            throw new IllegalArgumentException("Conteúdo do documento não pode ser vazio");
        }
    }

    private void validarTamanho(long tamanho, TipoDocumento tipo) {
        long tamanhoMaximoBytes = tipo.getTamanhoMaximoMB() * 1024L * 1024L;

        if (tamanho > tamanhoMaximoBytes) {
            throw new IllegalArgumentException(
                    String.format("Tamanho do arquivo (%.2f MB) excede o limite de %d MB para o tipo %s",
                            tamanho / (1024.0 * 1024.0), tipo.getTamanhoMaximoMB(), tipo));
        }

        if (tamanho == 0) {
            throw new IllegalArgumentException("Arquivo vazio não é permitido");
        }
    }

    private void validarFormato(String formato, TipoDocumento tipo) {
        if (!tipo.aceitaFormato(formato)) {
            throw new IllegalArgumentException(
                    String.format("Formato %s não é aceito para documentos do tipo %s. " +
                                    "Formatos aceitos: %s",
                            formato, tipo, String.join(", ", tipo.getFormatosAceitos())));
        }
    }

    // ==================== Métodos Abstratos ====================

    @Override
    protected void restoreFromSnapshot(Object snapshotData) {
        if (snapshotData instanceof Documento) {
            this.documento = (Documento) snapshotData;
            log.debug("Documento restaurado do snapshot: {}", documento.getId());
        } else {
            throw new IllegalArgumentException("Snapshot inválido para DocumentoAggregate");
        }
    }

    @Override
    protected void clearState() {
        this.documento = null;
    }

    @Override
    public Object createSnapshot() {
        return this.documento;
    }

    // ==================== Getters Convenientes ====================

    public String getDocumentoId() {
        return documento != null ? documento.getId() : null;
    }

    public StatusDocumento getStatus() {
        return documento != null ? documento.getStatus() : null;
    }

    public TipoDocumento getTipo() {
        return documento != null ? documento.getTipo() : null;
    }
}
