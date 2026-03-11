package com.seguradora.hibrida.domain.documento.model;

/**
 * Enum que representa os tipos de assinatura disponíveis para documentos.
 *
 * <p>Define os diferentes níveis de assinatura e seus requisitos:
 * <ul>
 *   <li>DIGITAL: Assinatura com certificado digital ICP-Brasil (nível máximo)</li>
 *   <li>ELETRONICA: Assinatura eletrônica simples (e-mail, SMS)</li>
 *   <li>FISICA_DIGITALIZADA: Assinatura física escaneada</li>
 *   <li>SEM_ASSINATURA: Documento não requer assinatura</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum TipoAssinatura {

    /**
     * Assinatura digital com certificado ICP-Brasil.
     * Maior nível de segurança e validade jurídica.
     */
    DIGITAL(
            "Assinatura Digital ICP-Brasil",
            3,
            true,
            true
    ),

    /**
     * Assinatura eletrônica simples.
     * Validação por e-mail, SMS ou token.
     */
    ELETRONICA(
            "Assinatura Eletrônica",
            2,
            false,
            true
    ),

    /**
     * Assinatura física escaneada.
     * Documento assinado fisicamente e digitalizado.
     */
    FISICA_DIGITALIZADA(
            "Assinatura Física Digitalizada",
            1,
            false,
            false
    ),

    /**
     * Documento sem necessidade de assinatura.
     * Para documentos informativos ou não críticos.
     */
    SEM_ASSINATURA(
            "Sem Assinatura",
            0,
            false,
            false
    );

    private final String descricao;
    private final int nivelSeguranca;
    private final boolean requerCertificado;
    private final boolean validacaoAutomatica;

    TipoAssinatura(String descricao, int nivelSeguranca, boolean requerCertificado,
                   boolean validacaoAutomatica) {
        this.descricao = descricao;
        this.nivelSeguranca = nivelSeguranca;
        this.requerCertificado = requerCertificado;
        this.validacaoAutomatica = validacaoAutomatica;
    }

    /**
     * Retorna a descrição do tipo de assinatura.
     *
     * @return Descrição
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Retorna o nível de segurança (0-3, sendo 3 o mais seguro).
     *
     * @return Nível de segurança
     */
    public int getNivelSeguranca() {
        return nivelSeguranca;
    }

    /**
     * Verifica se requer certificado digital.
     *
     * @return true se requer certificado
     */
    public boolean requerCertificado() {
        return requerCertificado;
    }

    /**
     * Verifica se a validação pode ser automatizada.
     *
     * @return true se permite validação automática
     */
    public boolean permiteValidacaoAutomatica() {
        return validacaoAutomatica;
    }

    /**
     * Verifica se é uma assinatura válida (não é SEM_ASSINATURA).
     *
     * @return true se é uma assinatura válida
     */
    public boolean isAssinaturaValida() {
        return this != SEM_ASSINATURA;
    }

    /**
     * Verifica se atende ao nível de segurança mínimo requerido.
     *
     * @param nivelMinimo Nível mínimo de segurança requerido
     * @return true se atende ao nível mínimo
     */
    public boolean atendeNivelSeguranca(int nivelMinimo) {
        return this.nivelSeguranca >= nivelMinimo;
    }

    /**
     * Verifica se este tipo de assinatura é mais seguro que outro.
     *
     * @param outro Outro tipo de assinatura para comparação
     * @return true se este tipo é mais seguro
     */
    public boolean isMaisSeguroQue(TipoAssinatura outro) {
        if (outro == null) {
            return true;
        }
        return this.nivelSeguranca > outro.nivelSeguranca;
    }

    /**
     * Retorna o tipo de assinatura recomendado para documentos críticos.
     *
     * @return Tipo de assinatura recomendado
     */
    public static TipoAssinatura getRecomendadoParaDocumentosCriticos() {
        return DIGITAL;
    }

    /**
     * Retorna o tipo de assinatura mínimo aceitável.
     *
     * @return Tipo de assinatura mínimo
     */
    public static TipoAssinatura getMinimoAceitavel() {
        return ELETRONICA;
    }
}
