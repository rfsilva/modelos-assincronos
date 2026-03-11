package com.seguradora.hibrida.domain.documento.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Enum que representa os possíveis status de um documento.
 *
 * <p>Define o ciclo de vida de um documento e as transições válidas entre estados:
 * <ul>
 *   <li>PENDENTE: Documento enviado, aguardando validação</li>
 *   <li>VALIDADO: Documento aprovado e aceito</li>
 *   <li>REJEITADO: Documento recusado com justificativa</li>
 *   <li>ARQUIVADO: Documento arquivado (final)</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum StatusDocumento {

    /**
     * Documento pendente de validação.
     * Estado inicial após upload.
     */
    PENDENTE(false),

    /**
     * Documento validado e aceito.
     * Pode ser arquivado posteriormente.
     */
    VALIDADO(false),

    /**
     * Documento rejeitado.
     * Estado final que requer reenvio de novo documento.
     */
    REJEITADO(true),

    /**
     * Documento arquivado.
     * Estado final para documentos validados e processados.
     */
    ARQUIVADO(true);

    private final boolean statusFinal;

    StatusDocumento(boolean statusFinal) {
        this.statusFinal = statusFinal;
    }

    /**
     * Verifica se este é um status final (não permite mais transições).
     *
     * @return true se for status final
     */
    public boolean isFinal() {
        return statusFinal;
    }

    /**
     * Verifica se pode transicionar para o status especificado.
     *
     * @param novoStatus Status destino da transição
     * @return true se a transição é permitida
     */
    public boolean podeTransicionarPara(StatusDocumento novoStatus) {
        if (novoStatus == null) {
            return false;
        }

        // Definir transições válidas
        return switch (this) {
            case PENDENTE -> novoStatus == VALIDADO || novoStatus == REJEITADO;
            case VALIDADO -> novoStatus == ARQUIVADO;
            case REJEITADO, ARQUIVADO -> false; // Status finais
        };
    }

    /**
     * Retorna os status para os quais este status pode transicionar.
     *
     * @return Set de status permitidos
     */
    public Set<StatusDocumento> getTransicoesPermitidas() {
        return switch (this) {
            case PENDENTE -> EnumSet.of(VALIDADO, REJEITADO);
            case VALIDADO -> EnumSet.of(ARQUIVADO);
            case REJEITADO, ARQUIVADO -> EnumSet.noneOf(StatusDocumento.class);
        };
    }

    /**
     * Verifica se o documento pode ser atualizado neste status.
     *
     * @return true se pode ser atualizado
     */
    public boolean podeAtualizar() {
        return this == PENDENTE;
    }

    /**
     * Verifica se o documento pode ser validado neste status.
     *
     * @return true se pode ser validado
     */
    public boolean podeValidar() {
        return this == PENDENTE;
    }

    /**
     * Verifica se o documento pode ser rejeitado neste status.
     *
     * @return true se pode ser rejeitado
     */
    public boolean podeRejeitar() {
        return this == PENDENTE;
    }

    /**
     * Verifica se o documento está em processamento.
     *
     * @return true se está pendente
     */
    public boolean estaPendente() {
        return this == PENDENTE;
    }

    /**
     * Verifica se o documento foi aprovado.
     *
     * @return true se foi validado
     */
    public boolean estaValidado() {
        return this == VALIDADO || this == ARQUIVADO;
    }

    /**
     * Valida a transição para um novo status.
     *
     * @param novoStatus Status destino
     * @throws IllegalStateException se a transição não é permitida
     */
    public void validarTransicao(StatusDocumento novoStatus) {
        if (!podeTransicionarPara(novoStatus)) {
            throw new IllegalStateException(
                    String.format("Transição não permitida de %s para %s", this, novoStatus));
        }
    }
}
