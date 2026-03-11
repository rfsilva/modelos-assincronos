package com.seguradora.hibrida.domain.documento.command;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * Command para validar um documento após análise.
 *
 * <p>Marca o documento como validado e aceito:
 * <ul>
 *   <li>Identificação do validador</li>
 *   <li>Observações da validação</li>
 *   <li>Timestamp de aprovação</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Builder
@ToString
public class ValidarDocumentoCommand {

    /**
     * ID do documento a validar.
     */
    private final String documentoId;

    /**
     * ID do validador/operador.
     */
    private final String validadorId;

    /**
     * Nome do validador (para auditoria).
     */
    private final String validadorNome;

    /**
     * Observações da validação (opcional).
     */
    private final String observacoes;

    /**
     * Valida o command.
     *
     * @throws IllegalArgumentException se houver campos inválidos
     */
    public void validar() {
        Objects.requireNonNull(documentoId, "Documento ID não pode ser nulo");
        Objects.requireNonNull(validadorId, "Validador ID não pode ser nulo");

        if (documentoId.trim().isEmpty()) {
            throw new IllegalArgumentException("Documento ID não pode ser vazio");
        }

        if (validadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Validador ID não pode ser vazio");
        }
    }

    /**
     * Verifica se possui observações.
     *
     * @return true se há observações
     */
    public boolean possuiObservacoes() {
        return observacoes != null && !observacoes.trim().isEmpty();
    }
}
