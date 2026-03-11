package com.seguradora.hibrida.domain.documento.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import java.util.List;
import java.util.Objects;

/**
 * Command para rejeitar um documento com justificativa.
 *
 * <p>Marca o documento como rejeitado incluindo:
 * <ul>
 *   <li>Motivo detalhado da rejeição</li>
 *   <li>Lista de problemas identificados</li>
 *   <li>Ações corretivas sugeridas</li>
 *   <li>Identificação do validador</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Builder
@ToString
public class RejeitarDocumentoCommand {

    /**
     * ID do documento a rejeitar.
     */
    private final String documentoId;

    /**
     * Motivo principal da rejeição.
     */
    private final String motivo;

    /**
     * Lista de problemas identificados.
     */
    @Singular
    private final List<String> problemasIdentificados;

    /**
     * ID do validador/operador que rejeitou.
     */
    private final String validadorId;

    /**
     * Nome do validador (para auditoria).
     */
    private final String validadorNome;

    /**
     * Ações corretivas sugeridas (opcional).
     */
    private final String acoesCorretivas;

    /**
     * Indica se permite reenvio do documento.
     */
    @Builder.Default
    private final boolean permiteReenvio = true;

    /**
     * Valida o command.
     *
     * @throws IllegalArgumentException se houver campos inválidos
     */
    public void validar() {
        Objects.requireNonNull(documentoId, "Documento ID não pode ser nulo");
        Objects.requireNonNull(motivo, "Motivo não pode ser nulo");
        Objects.requireNonNull(validadorId, "Validador ID não pode ser nulo");

        if (documentoId.trim().isEmpty()) {
            throw new IllegalArgumentException("Documento ID não pode ser vazio");
        }

        if (motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo não pode ser vazio");
        }

        if (validadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Validador ID não pode ser vazio");
        }
    }

    /**
     * Verifica se possui problemas detalhados.
     *
     * @return true se há problemas listados
     */
    public boolean possuiProblemasDetalhados() {
        return problemasIdentificados != null && !problemasIdentificados.isEmpty();
    }

    /**
     * Verifica se possui ações corretivas.
     *
     * @return true se há ações sugeridas
     */
    public boolean possuiAcoesCorretivas() {
        return acoesCorretivas != null && !acoesCorretivas.trim().isEmpty();
    }

    /**
     * Retorna o número de problemas identificados.
     *
     * @return Quantidade de problemas
     */
    public int getQuantidadeProblemas() {
        return problemasIdentificados != null ? problemasIdentificados.size() : 0;
    }
}
