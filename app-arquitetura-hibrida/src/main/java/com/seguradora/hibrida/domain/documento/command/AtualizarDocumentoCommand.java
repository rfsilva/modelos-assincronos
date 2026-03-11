package com.seguradora.hibrida.domain.documento.command;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * Command para atualizar um documento existente (criar nova versão).
 *
 * <p>Gera automaticamente uma nova versão do documento com:
 * <ul>
 *   <li>Novo conteúdo</li>
 *   <li>Motivo da atualização</li>
 *   <li>Operador responsável</li>
 *   <li>Preservação do histórico</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Builder
@ToString(exclude = "novoConteudo") // Não logar conteúdo binário
public class AtualizarDocumentoCommand {

    /**
     * ID do documento a atualizar.
     */
    private final String documentoId;

    /**
     * Novo conteúdo do arquivo em bytes.
     */
    private final byte[] novoConteudo;

    /**
     * Motivo/descrição da atualização.
     */
    private final String motivo;

    /**
     * ID do operador que está atualizando.
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
        Objects.requireNonNull(novoConteudo, "Novo conteúdo não pode ser nulo");
        Objects.requireNonNull(motivo, "Motivo não pode ser nulo");
        Objects.requireNonNull(operadorId, "Operador ID não pode ser nulo");

        if (documentoId.trim().isEmpty()) {
            throw new IllegalArgumentException("Documento ID não pode ser vazio");
        }

        if (novoConteudo.length == 0) {
            throw new IllegalArgumentException("Novo conteúdo não pode ser vazio");
        }

        if (motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo não pode ser vazio");
        }
    }

    /**
     * Retorna o tamanho do novo conteúdo em bytes.
     *
     * @return Tamanho em bytes
     */
    public long getTamanho() {
        return novoConteudo != null ? novoConteudo.length : 0;
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
