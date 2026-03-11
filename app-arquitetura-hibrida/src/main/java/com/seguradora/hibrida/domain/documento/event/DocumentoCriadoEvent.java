package com.seguradora.hibrida.domain.documento.event;

import com.seguradora.hibrida.domain.documento.model.TipoDocumento;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Evento disparado quando um novo documento é criado no sistema.
 *
 * <p>Contém todas as informações necessárias para reconstruir o estado inicial
 * do documento, incluindo:
 * <ul>
 *   <li>Identificação completa do documento</li>
 *   <li>Tipo e características do arquivo</li>
 *   <li>Hash para integridade</li>
 *   <li>Vinculação com sinistro e operador</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class DocumentoCriadoEvent extends DomainEvent {

    /**
     * ID único do documento.
     */
    private String documentoId;

    /**
     * Nome do arquivo do documento.
     */
    private String nome;

    /**
     * Tipo do documento.
     */
    private TipoDocumento tipo;

    /**
     * Tamanho do arquivo em bytes.
     */
    private long tamanho;

    /**
     * Hash SHA-256 do conteúdo para integridade.
     */
    private String hash;

    /**
     * Formato/MIME type do arquivo.
     */
    private String formato;

    /**
     * Path onde o conteúdo está armazenado.
     */
    private String conteudoPath;

    /**
     * ID do sinistro ao qual o documento está vinculado.
     */
    private String sinistroId;

    /**
     * ID do operador que criou o documento.
     */
    private String operadorId;

    /**
     * Construtor completo para criação do evento.
     */
    public DocumentoCriadoEvent(String documentoId, String nome, TipoDocumento tipo,
                                long tamanho, String hash, String formato,
                                String conteudoPath, String sinistroId, String operadorId) {
        super();
        this.documentoId = documentoId;
        this.nome = nome;
        this.tipo = tipo;
        this.tamanho = tamanho;
        this.hash = hash;
        this.formato = formato;
        this.conteudoPath = conteudoPath;
        this.sinistroId = sinistroId;
        this.operadorId = operadorId;
    }

    /**
     * Retorna o tamanho formatado em MB.
     *
     * @return Tamanho formatado
     */
    public String getTamanhoFormatado() {
        double tamanhoMB = tamanho / (1024.0 * 1024.0);
        return String.format("%.2f MB", tamanhoMB);
    }

    /**
     * Verifica se o documento é grande (maior que 10 MB).
     *
     * @return true se for grande
     */
    public boolean isDocumentoGrande() {
        return tamanho > (10 * 1024 * 1024);
    }
}
