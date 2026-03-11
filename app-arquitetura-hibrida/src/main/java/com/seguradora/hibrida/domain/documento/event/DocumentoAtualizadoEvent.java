package com.seguradora.hibrida.domain.documento.event;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Evento disparado quando um documento é atualizado (nova versão).
 *
 * <p>Captura as alterações realizadas no documento, incluindo:
 * <ul>
 *   <li>Nova versão gerada</li>
 *   <li>Motivo da atualização</li>
 *   <li>Novo hash do conteúdo</li>
 *   <li>Operador responsável</li>
 * </ul>
 *
 * <p>Este evento preserva o histórico de versionamento completo.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class DocumentoAtualizadoEvent extends DomainEvent {

    /**
     * ID do documento atualizado.
     */
    private String documentoId;

    /**
     * Número da nova versão gerada.
     */
    private int novaVersao;

    /**
     * Motivo/descrição da atualização.
     */
    private String motivo;

    /**
     * Hash do conteúdo anterior.
     */
    private String hashAnterior;

    /**
     * Novo hash SHA-256 do conteúdo.
     */
    private String hashNovo;

    /**
     * Novo tamanho do arquivo em bytes.
     */
    private long novoTamanho;

    /**
     * Novo path de armazenamento.
     */
    private String novoConteudoPath;

    /**
     * ID do operador que realizou a atualização.
     */
    private String operadorId;

    /**
     * Construtor completo para criação do evento.
     */
    public DocumentoAtualizadoEvent(String documentoId, int novaVersao, String motivo,
                                    String hashAnterior, String hashNovo, long novoTamanho,
                                    String novoConteudoPath, String operadorId) {
        super();
        this.documentoId = documentoId;
        this.novaVersao = novaVersao;
        this.motivo = motivo;
        this.hashAnterior = hashAnterior;
        this.hashNovo = hashNovo;
        this.novoTamanho = novoTamanho;
        this.novoConteudoPath = novoConteudoPath;
        this.operadorId = operadorId;
    }

    /**
     * Verifica se o conteúdo foi significativamente alterado.
     *
     * @return true se o hash mudou
     */
    public boolean conteudoAlterado() {
        return !hashAnterior.equals(hashNovo);
    }

    /**
     * Calcula a diferença de tamanho em relação à versão anterior.
     *
     * @param tamanhoAnterior Tamanho da versão anterior
     * @return Diferença em bytes (positivo se aumentou)
     */
    public long calcularDiferencaTamanho(long tamanhoAnterior) {
        return novoTamanho - tamanhoAnterior;
    }

    /**
     * Retorna o número da versão formatado.
     *
     * @return String no formato "v2.0", "v3.0", etc.
     */
    public String getVersaoFormatada() {
        return String.format("v%d.0", novaVersao);
    }
}
