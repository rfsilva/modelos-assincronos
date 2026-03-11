package com.seguradora.hibrida.domain.documento.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Evento disparado quando um documento é validado e aceito.
 *
 * <p>Marca a aprovação do documento após análise, incluindo:
 * <ul>
 *   <li>Identificação do validador</li>
 *   <li>Observações da validação</li>
 *   <li>Timestamp preciso da aprovação</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class DocumentoValidadoEvent extends DomainEvent {

    /**
     * ID do documento validado.
     */
    private String documentoId;

    /**
     * ID do operador/validador que aprovou o documento.
     */
    private String validadorId;

    /**
     * Nome do validador.
     */
    private String validadorNome;

    /**
     * Observações da validação (opcional).
     */
    private String observacoes;

    /**
     * Indica se a validação foi automática.
     */
    private boolean validacaoAutomatica;

    /**
     * Critérios de validação aplicados.
     */
    private String criteriosAplicados;

    /**
     * Construtor completo para criação do evento.
     */
    public DocumentoValidadoEvent(String documentoId, String validadorId, String validadorNome,
                                  String observacoes, boolean validacaoAutomatica,
                                  String criteriosAplicados) {
        super();
        this.documentoId = documentoId;
        this.validadorId = validadorId;
        this.validadorNome = validadorNome;
        this.observacoes = observacoes;
        this.validacaoAutomatica = validacaoAutomatica;
        this.criteriosAplicados = criteriosAplicados;
    }

    /**
     * Construtor simplificado para validação manual.
     */
    public DocumentoValidadoEvent(String documentoId, String validadorId, String observacoes) {
        this(documentoId, validadorId, null, observacoes, false, null);
    }

    /**
     * Verifica se possui observações.
     *
     * @return true se há observações
     */
    public boolean possuiObservacoes() {
        return observacoes != null && !observacoes.trim().isEmpty();
    }

    /**
     * Retorna o tipo de validação.
     *
     * @return "Automática" ou "Manual"
     */
    public String getTipoValidacao() {
        return validacaoAutomatica ? "Automática" : "Manual";
    }
}
