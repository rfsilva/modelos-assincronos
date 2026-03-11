package com.seguradora.hibrida.domain.documento.command;

import com.seguradora.hibrida.domain.documento.model.TipoDocumento;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * Command para criar um novo documento no sistema.
 *
 * <p>Contém todas as informações necessárias para criação:
 * <ul>
 *   <li>Identificação do documento</li>
 *   <li>Tipo e características</li>
 *   <li>Conteúdo do arquivo</li>
 *   <li>Vinculação com sinistro</li>
 *   <li>Operador responsável</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Builder
@ToString(exclude = "conteudo") // Não logar conteúdo binário
public class CriarDocumentoCommand {

    /**
     * ID único do documento.
     */
    private final String documentoId;

    /**
     * Nome do arquivo.
     */
    private final String nome;

    /**
     * Tipo do documento.
     */
    private final TipoDocumento tipo;

    /**
     * Conteúdo do arquivo em bytes.
     */
    private final byte[] conteudo;

    /**
     * Formato/MIME type do arquivo.
     */
    private final String formato;

    /**
     * ID do sinistro vinculado.
     */
    private final String sinistroId;

    /**
     * ID do operador que está criando o documento.
     */
    private final String operadorId;

    /**
     * Nome do operador (para auditoria).
     */
    private final String operadorNome;

    /**
     * Valida o command.
     *
     * @throws IllegalArgumentException se houver campos inválidos
     */
    public void validar() {
        Objects.requireNonNull(documentoId, "Documento ID não pode ser nulo");
        Objects.requireNonNull(nome, "Nome não pode ser nulo");
        Objects.requireNonNull(tipo, "Tipo não pode ser nulo");
        Objects.requireNonNull(conteudo, "Conteúdo não pode ser nulo");
        Objects.requireNonNull(formato, "Formato não pode ser nulo");
        Objects.requireNonNull(sinistroId, "Sinistro ID não pode ser nulo");
        Objects.requireNonNull(operadorId, "Operador ID não pode ser nulo");

        if (nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser vazio");
        }

        if (conteudo.length == 0) {
            throw new IllegalArgumentException("Conteúdo não pode ser vazio");
        }

        if (formato.trim().isEmpty()) {
            throw new IllegalArgumentException("Formato não pode ser vazio");
        }
    }

    /**
     * Retorna o tamanho do conteúdo em bytes.
     *
     * @return Tamanho em bytes
     */
    public long getTamanho() {
        return conteudo != null ? conteudo.length : 0;
    }

    /**
     * Retorna o tamanho formatado em MB.
     *
     * @return Tamanho formatado
     */
    public String getTamanhoFormatado() {
        double tamanhoMB = getTamanho() / (1024.0 * 1024.0);
        return String.format("%.2f MB", tamanhoMB);
    }
}
