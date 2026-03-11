package com.seguradora.hibrida.domain.documento.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.time.Instant;
import java.util.*;

/**
 * Entidade de domínio que representa um documento com versionamento.
 *
 * <p>Características principais:
 * <ul>
 *   <li>Versionamento completo com histórico</li>
 *   <li>Integridade garantida por hash SHA-256</li>
 *   <li>Suporte a múltiplas assinaturas digitais</li>
 *   <li>Metadata extensível</li>
 *   <li>Controle de status e validações</li>
 * </ul>
 *
 * <p>Usa Builder pattern para construção imutável.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Builder(toBuilder = true)
public class Documento {

    private final String id;
    private final String nome;
    private final TipoDocumento tipo;
    private final VersaoDocumento versao;
    private final long tamanho;
    private final HashDocumento hash;
    private final String formato;
    private final String sinistroId;
    private final StatusDocumento status;

    /**
     * Path ou referência ao conteúdo armazenado.
     * Não armazena o conteúdo diretamente por questões de memória.
     */
    private final String conteudoPath;

    /**
     * Lista de assinaturas digitais aplicadas ao documento.
     */
    @Singular
    private final List<AssinaturaDigital> assinaturas;

    /**
     * Metadata adicional do documento (extensível).
     */
    @Singular
    private final Map<String, String> metadados;

    /**
     * Data de criação do documento.
     */
    private final Instant criadoEm;

    /**
     * Data da última atualização.
     */
    private final Instant atualizadoEm;

    /**
     * ID do operador que criou o documento.
     */
    private final String criadoPor;

    /**
     * ID do operador que fez a última atualização.
     */
    private final String atualizadoPor;

    /**
     * Verifica se o documento pode ser atualizado no status atual.
     *
     * @return true se pode atualizar
     */
    public boolean podeAtualizar() {
        return status != null && status.podeAtualizar();
    }

    /**
     * Verifica se o documento pode ser validado no status atual.
     *
     * @return true se pode validar
     */
    public boolean podeValidar() {
        return status != null && status.podeValidar();
    }

    /**
     * Verifica se o documento pode ser rejeitado no status atual.
     *
     * @return true se pode rejeitar
     */
    public boolean podeRejeitar() {
        return status != null && status.podeRejeitar();
    }

    /**
     * Calcula o hash do conteúdo fornecido.
     *
     * @param conteudo Conteúdo em bytes
     * @return Hash calculado
     */
    public static HashDocumento calcularHash(byte[] conteudo) {
        return HashDocumento.calcular(conteudo);
    }

    /**
     * Verifica se o documento mantém sua integridade.
     *
     * @param conteudo Conteúdo atual para verificação
     * @return true se o hash corresponder
     */
    public boolean isIntegro(byte[] conteudo) {
        if (hash == null || conteudo == null) {
            return false;
        }
        return hash.validar(conteudo);
    }

    /**
     * Verifica se o documento possui assinaturas válidas.
     *
     * @return true se possui pelo menos uma assinatura válida
     */
    public boolean possuiAssinaturasValidas() {
        if (assinaturas == null || assinaturas.isEmpty()) {
            return false;
        }

        return assinaturas.stream().anyMatch(AssinaturaDigital::isValida);
    }

    /**
     * Verifica se o documento requer assinatura.
     *
     * @return true se requer assinatura
     */
    public boolean requerAssinatura() {
        return tipo != null && tipo.requerAssinatura();
    }

    /**
     * Verifica se o documento atende aos requisitos de assinatura.
     *
     * @return true se atende (ou não requer)
     */
    public boolean atendeRequisitosAssinatura() {
        if (!requerAssinatura()) {
            return true;
        }

        return possuiAssinaturasValidas();
    }

    /**
     * Verifica se o tamanho está dentro do limite permitido.
     *
     * @return true se está dentro do limite
     */
    public boolean tamanhoValido() {
        if (tipo == null) {
            return false;
        }

        long tamanhoMaximoBytes = tipo.getTamanhoMaximoMB() * 1024L * 1024L;
        return tamanho <= tamanhoMaximoBytes;
    }

    /**
     * Verifica se o formato é aceito pelo tipo de documento.
     *
     * @return true se o formato é aceito
     */
    public boolean formatoValido() {
        if (tipo == null || formato == null) {
            return false;
        }

        return tipo.aceitaFormato(formato);
    }

    /**
     * Retorna o número da versão atual.
     *
     * @return Número da versão
     */
    public int getNumeroVersao() {
        return versao != null ? versao.getNumero() : 0;
    }

    /**
     * Retorna o valor do hash em formato string.
     *
     * @return Hash em string ou null
     */
    public String getHashValor() {
        return hash != null ? hash.getValor() : null;
    }

    /**
     * Retorna o tamanho formatado em MB.
     *
     * @return Tamanho formatado (ex: "2.5 MB")
     */
    public String getTamanhoFormatado() {
        double tamanhoMB = tamanho / (1024.0 * 1024.0);
        return String.format("%.2f MB", tamanhoMB);
    }

    /**
     * Verifica se o documento possui metadata específica.
     *
     * @param chave Chave da metadata
     * @return true se possui
     */
    public boolean possuiMetadata(String chave) {
        return metadados != null && metadados.containsKey(chave);
    }

    /**
     * Retorna valor de metadata específica.
     *
     * @param chave Chave da metadata
     * @return Valor ou null
     */
    public String getMetadata(String chave) {
        return metadados != null ? metadados.get(chave) : null;
    }

    /**
     * Retorna lista imutável de assinaturas.
     *
     * @return Lista de assinaturas
     */
    public List<AssinaturaDigital> getAssinaturas() {
        return assinaturas != null ? Collections.unmodifiableList(assinaturas) : Collections.emptyList();
    }

    /**
     * Retorna mapa imutável de metadados.
     *
     * @return Mapa de metadados
     */
    public Map<String, String> getMetadados() {
        return metadados != null ? Collections.unmodifiableMap(metadados) : Collections.emptyMap();
    }

    /**
     * Conta quantas assinaturas válidas o documento possui.
     *
     * @return Número de assinaturas válidas
     */
    public long contarAssinaturasValidas() {
        if (assinaturas == null) {
            return 0;
        }

        return assinaturas.stream()
                .filter(AssinaturaDigital::isValida)
                .count();
    }

    /**
     * Verifica se foi criado pelo operador especificado.
     *
     * @param operadorId ID do operador
     * @return true se foi criado por este operador
     */
    public boolean foiCriadoPor(String operadorId) {
        return Objects.equals(this.criadoPor, operadorId);
    }

    /**
     * Verifica se foi atualizado pelo operador especificado.
     *
     * @param operadorId ID do operador
     * @return true se foi atualizado por este operador
     */
    public boolean foiAtualizadoPor(String operadorId) {
        return Objects.equals(this.atualizadoPor, operadorId);
    }

    /**
     * Verifica se o documento está em status final.
     *
     * @return true se está em status final
     */
    public boolean isStatusFinal() {
        return status != null && status.isFinal();
    }

    /**
     * Verifica se o documento está validado.
     *
     * @return true se está validado
     */
    public boolean isValidado() {
        return status != null && status.estaValidado();
    }

    /**
     * Verifica se o documento está pendente.
     *
     * @return true se está pendente
     */
    public boolean isPendente() {
        return status != null && status.estaPendente();
    }

    /**
     * Retorna informações resumidas do documento.
     *
     * @return String com resumo
     */
    public String getResumo() {
        return String.format("Documento[id=%s, nome=%s, tipo=%s, versao=%s, status=%s, tamanho=%s]",
                id, nome, tipo, versao != null ? versao.getNumeroFormatado() : "?",
                status, getTamanhoFormatado());
    }

    /**
     * Valida o documento de forma completa.
     *
     * @return Lista de erros de validação (vazia se válido)
     */
    public List<String> validar() {
        List<String> erros = new ArrayList<>();

        if (id == null || id.isEmpty()) {
            erros.add("ID do documento é obrigatório");
        }

        if (nome == null || nome.isEmpty()) {
            erros.add("Nome do documento é obrigatório");
        }

        if (tipo == null) {
            erros.add("Tipo do documento é obrigatório");
        }

        if (hash == null || !hash.isValido()) {
            erros.add("Hash do documento é inválido");
        }

        if (!tamanhoValido()) {
            erros.add(String.format("Tamanho excede o limite de %d MB", tipo.getTamanhoMaximoMB()));
        }

        if (!formatoValido()) {
            erros.add(String.format("Formato %s não é aceito para %s", formato, tipo));
        }

        if (requerAssinatura() && !possuiAssinaturasValidas()) {
            erros.add("Documento requer assinatura válida");
        }

        return erros;
    }

    /**
     * Verifica se o documento é válido.
     *
     * @return true se não há erros de validação
     */
    public boolean isValido() {
        return validar().isEmpty();
    }
}
