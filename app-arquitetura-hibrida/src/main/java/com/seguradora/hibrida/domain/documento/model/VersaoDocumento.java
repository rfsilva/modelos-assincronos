package com.seguradora.hibrida.domain.documento.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.Objects;

/**
 * Value Object que representa uma versão de documento.
 *
 * <p>Mantém o histórico completo de alterações, incluindo:
 * <ul>
 *   <li>Número da versão</li>
 *   <li>Descrição das alterações</li>
 *   <li>Identificação do operador responsável</li>
 *   <li>Timestamp da modificação</li>
 *   <li>Hashes para rastreabilidade e integridade</li>
 * </ul>
 *
 * <p>Imutável e thread-safe.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@ToString
@EqualsAndHashCode
public final class VersaoDocumento {

    private final int numero;
    private final String descricaoAlteracao;
    private final String operadorId;
    private final Instant timestamp;
    private final String hashAnterior;
    private final String hashAtual;

    /**
     * Construtor privado - use os métodos factory.
     */
    private VersaoDocumento(int numero, String descricaoAlteracao, String operadorId,
                            Instant timestamp, String hashAnterior, String hashAtual) {
        this.numero = numero;
        this.descricaoAlteracao = descricaoAlteracao;
        this.operadorId = operadorId;
        this.timestamp = timestamp;
        this.hashAnterior = hashAnterior;
        this.hashAtual = hashAtual;
    }

    /**
     * Cria a versão inicial de um documento.
     *
     * @param hashInicial Hash do conteúdo inicial
     * @param operadorId ID do operador que criou
     * @return Versão inicial
     */
    public static VersaoDocumento versaoInicial(String hashInicial, String operadorId) {
        Objects.requireNonNull(hashInicial, "Hash inicial não pode ser nulo");
        Objects.requireNonNull(operadorId, "Operador ID não pode ser nulo");

        return new VersaoDocumento(
                1,
                "Versão inicial",
                operadorId,
                Instant.now(),
                null,
                hashInicial
        );
    }

    /**
     * Cria a próxima versão a partir desta.
     *
     * @param novoHash Hash do novo conteúdo
     * @param descricao Descrição das alterações
     * @param operadorId ID do operador responsável
     * @return Nova versão
     */
    public VersaoDocumento proximaVersao(String novoHash, String descricao, String operadorId) {
        Objects.requireNonNull(novoHash, "Hash não pode ser nulo");
        Objects.requireNonNull(descricao, "Descrição não pode ser nula");
        Objects.requireNonNull(operadorId, "Operador ID não pode ser nulo");

        if (descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição das alterações é obrigatória");
        }

        if (novoHash.equals(this.hashAtual)) {
            throw new IllegalArgumentException("Conteúdo não foi modificado (hash idêntico)");
        }

        return new VersaoDocumento(
                this.numero + 1,
                descricao,
                operadorId,
                Instant.now(),
                this.hashAtual,
                novoHash
        );
    }

    /**
     * Verifica se esta é a versão inicial.
     *
     * @return true se for a versão 1
     */
    public boolean isVersaoInicial() {
        return numero == 1;
    }

    /**
     * Retorna uma descrição completa da versão.
     *
     * @return Descrição formatada
     */
    public String getDescricaoCompleta() {
        if (isVersaoInicial()) {
            return String.format("v%d: %s (por %s em %s)",
                    numero, descricaoAlteracao, operadorId, timestamp);
        } else {
            return String.format("v%d: %s (por %s em %s) [hash anterior: %s]",
                    numero, descricaoAlteracao, operadorId, timestamp,
                    hashAnterior.substring(0, Math.min(8, hashAnterior.length())));
        }
    }

    /**
     * Verifica se o hash anterior corresponde ao fornecido.
     *
     * @param hash Hash para comparação
     * @return true se corresponder
     */
    public boolean hashAnteriorCorresponde(String hash) {
        if (isVersaoInicial()) {
            return true; // Versão inicial não tem hash anterior
        }
        return Objects.equals(hashAnterior, hash);
    }

    /**
     * Verifica se a versão foi criada pelo operador especificado.
     *
     * @param operadorId ID do operador
     * @return true se foi criado por este operador
     */
    public boolean foiCriadaPor(String operadorId) {
        return Objects.equals(this.operadorId, operadorId);
    }

    /**
     * Verifica se a versão é mais recente que outra.
     *
     * @param outra Outra versão para comparação
     * @return true se esta versão é mais recente
     */
    public boolean isMaisRecenteQue(VersaoDocumento outra) {
        if (outra == null) {
            return true;
        }
        return this.numero > outra.numero;
    }

    /**
     * Retorna o número da versão formatado.
     *
     * @return String no formato "v1.0", "v2.0", etc.
     */
    public String getNumeroFormatado() {
        return String.format("v%d.0", numero);
    }

    /**
     * Calcula a diferença de versões.
     *
     * @param outra Outra versão
     * @return Número de versões de diferença
     */
    public int diferencaVersoes(VersaoDocumento outra) {
        if (outra == null) {
            return this.numero;
        }
        return Math.abs(this.numero - outra.numero);
    }
}
