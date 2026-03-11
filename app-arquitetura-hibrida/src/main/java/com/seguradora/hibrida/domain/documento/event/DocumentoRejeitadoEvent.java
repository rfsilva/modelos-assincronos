package com.seguradora.hibrida.domain.documento.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Evento disparado quando um documento é rejeitado.
 *
 * <p>Captura a rejeição do documento com justificativa completa:
 * <ul>
 *   <li>Motivo detalhado da rejeição</li>
 *   <li>Lista de problemas identificados</li>
 *   <li>Identificação do validador</li>
 *   <li>Ações corretivas sugeridas</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class DocumentoRejeitadoEvent extends DomainEvent {

    /**
     * ID do documento rejeitado.
     */
    private String documentoId;

    /**
     * Motivo principal da rejeição.
     */
    private String motivo;

    /**
     * Lista detalhada de problemas identificados.
     */
    private List<String> problemasIdentificados;

    /**
     * ID do operador/validador que rejeitou o documento.
     */
    private String validadorId;

    /**
     * Nome do validador.
     */
    private String validadorNome;

    /**
     * Ações corretivas sugeridas.
     */
    private String acoesCorretivas;

    /**
     * Indica se o documento pode ser reenviado.
     */
    private boolean permiteReenvio;

    /**
     * Construtor completo para criação do evento.
     */
    public DocumentoRejeitadoEvent(String documentoId, String motivo,
                                   List<String> problemasIdentificados,
                                   String validadorId, String validadorNome,
                                   String acoesCorretivas, boolean permiteReenvio) {
        super();
        this.documentoId = documentoId;
        this.motivo = motivo;
        this.problemasIdentificados = problemasIdentificados;
        this.validadorId = validadorId;
        this.validadorNome = validadorNome;
        this.acoesCorretivas = acoesCorretivas;
        this.permiteReenvio = permiteReenvio;
    }

    /**
     * Construtor simplificado para rejeição básica.
     */
    public DocumentoRejeitadoEvent(String documentoId, String motivo, String validadorId) {
        this(documentoId, motivo, null, validadorId, null, null, true);
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
     * Retorna o número de problemas identificados.
     *
     * @return Quantidade de problemas
     */
    public int getQuantidadeProblemas() {
        return problemasIdentificados != null ? problemasIdentificados.size() : 0;
    }

    /**
     * Verifica se possui ações corretivas sugeridas.
     *
     * @return true se há ações sugeridas
     */
    public boolean possuiAcoesCorretivas() {
        return acoesCorretivas != null && !acoesCorretivas.trim().isEmpty();
    }

    /**
     * Retorna mensagem completa da rejeição.
     *
     * @return Mensagem formatada
     */
    public String getMensagemCompleta() {
        StringBuilder sb = new StringBuilder();
        sb.append("DOCUMENTO REJEITADO\n");
        sb.append("Motivo: ").append(motivo).append("\n");

        if (possuiProblemasDetalhados()) {
            sb.append("\nProblemas Identificados:\n");
            for (int i = 0; i < problemasIdentificados.size(); i++) {
                sb.append(String.format("  %d. %s\n", i + 1, problemasIdentificados.get(i)));
            }
        }

        if (possuiAcoesCorretivas()) {
            sb.append("\nAções Corretivas:\n");
            sb.append(acoesCorretivas);
        }

        sb.append("\n\nReenvio: ").append(permiteReenvio ? "Permitido" : "Não permitido");

        return sb.toString();
    }
}
