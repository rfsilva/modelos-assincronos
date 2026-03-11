package com.seguradora.hibrida.domain.workflow.model;

import lombok.Getter;

/**
 * Define os tipos de etapas possíveis em um workflow de sinistro.
 * Cada tipo possui características específicas sobre processamento e intervenção humana.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Getter
public enum TipoEtapa {

    /**
     * Etapa executada automaticamente pelo sistema, sem intervenção humana.
     * Exemplos: validações automáticas, cálculos, integrações síncronas.
     */
    AUTOMATICA(false, true),

    /**
     * Etapa que requer intervenção humana para execução manual.
     * Exemplos: análise de documentos, validação de informações complexas.
     */
    MANUAL(true, false),

    /**
     * Etapa que requer aprovação formal de um responsável com alçada adequada.
     * Exemplos: aprovação de indenização, aprovação de exceções.
     */
    APROVACAO(true, false),

    /**
     * Etapa que realiza integração com sistemas externos de forma assíncrona.
     * Exemplos: consulta a bureaus, validação de terceiros, processamento externo.
     */
    INTEGRACAO(false, true);

    private final boolean requerIntervencaoHumana;
    private final boolean assincrona;

    /**
     * Construtor do enum.
     *
     * @param requerIntervencaoHumana indica se a etapa precisa de ação humana
     * @param assincrona indica se a etapa é processada de forma assíncrona
     */
    TipoEtapa(boolean requerIntervencaoHumana, boolean assincrona) {
        this.requerIntervencaoHumana = requerIntervencaoHumana;
        this.assincrona = assincrona;
    }

    /**
     * Verifica se esta etapa requer intervenção humana para ser completada.
     *
     * @return true se requer intervenção humana, false caso contrário
     */
    public boolean requerIntervencaoHumana() {
        return requerIntervencaoHumana;
    }

    /**
     * Verifica se esta etapa é executada de forma assíncrona.
     *
     * @return true se é assíncrona, false caso contrário
     */
    public boolean isAssincrona() {
        return assincrona;
    }

    /**
     * Verifica se a etapa pode ser executada automaticamente sem espera.
     *
     * @return true se pode executar automaticamente
     */
    public boolean isPodeExecutarAutomaticamente() {
        return !requerIntervencaoHumana;
    }

    /**
     * Verifica se a etapa é do tipo aprovação.
     *
     * @return true se é etapa de aprovação
     */
    public boolean isAprovacao() {
        return this == APROVACAO;
    }
}
