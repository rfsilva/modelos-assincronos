package com.seguradora.hibrida.domain.documento.model;

import com.seguradora.hibrida.domain.sinistro.model.TipoSinistro;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.EnumSet;

/**
 * Enum que representa os tipos de documentos do sistema.
 *
 * <p>Cada tipo possui características específicas como:
 * <ul>
 *   <li>Formatos aceitos (MIME types)</li>
 *   <li>Tamanho máximo em MB</li>
 *   <li>Obrigatoriedade por tipo de sinistro</li>
 *   <li>Necessidade de assinatura digital</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum TipoDocumento {

    /**
     * Boletim de Ocorrência policial.
     */
    BOLETIM_OCORRENCIA(
            "Boletim de Ocorrência",
            Arrays.asList("application/pdf", "image/jpeg", "image/png"),
            10,
            true,
            EnumSet.of(TipoSinistro.ROUBO_FURTO, TipoSinistro.VANDALISMO, TipoSinistro.INCENDIO)
    ),

    /**
     * Laudo pericial técnico.
     */
    LAUDO_PERICIAL(
            "Laudo Pericial",
            Arrays.asList("application/pdf"),
            15,
            true,
            EnumSet.of(TipoSinistro.COLISAO, TipoSinistro.INCENDIO, TipoSinistro.ENCHENTE)
    ),

    /**
     * Fotos dos danos ao veículo.
     */
    FOTO_DANOS(
            "Foto dos Danos",
            Arrays.asList("image/jpeg", "image/png", "image/heic"),
            5,
            false,
            EnumSet.allOf(TipoSinistro.class)
    ),

    /**
     * Orçamento de reparo.
     */
    ORCAMENTO(
            "Orçamento",
            Arrays.asList("application/pdf", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            10,
            false,
            EnumSet.of(TipoSinistro.COLISAO, TipoSinistro.INCENDIO, TipoSinistro.ENCHENTE, TipoSinistro.VANDALISMO)
    ),

    /**
     * Nota fiscal de serviços/peças.
     */
    NOTA_FISCAL(
            "Nota Fiscal",
            Arrays.asList("application/pdf", "application/xml"),
            10,
            true,
            EnumSet.of(TipoSinistro.COLISAO, TipoSinistro.INCENDIO, TipoSinistro.ENCHENTE, TipoSinistro.VANDALISMO)
    ),

    /**
     * Comprovante de residência do segurado.
     */
    COMPROVANTE_RESIDENCIA(
            "Comprovante de Residência",
            Arrays.asList("application/pdf", "image/jpeg", "image/png"),
            5,
            false,
            EnumSet.allOf(TipoSinistro.class)
    ),

    /**
     * Carteira Nacional de Habilitação.
     */
    CNH(
            "CNH",
            Arrays.asList("application/pdf", "image/jpeg", "image/png"),
            5,
            false,
            EnumSet.allOf(TipoSinistro.class)
    ),

    /**
     * Certificado de Registro e Licenciamento de Veículo.
     */
    CRLV(
            "CRLV",
            Arrays.asList("application/pdf", "image/jpeg", "image/png"),
            5,
            false,
            EnumSet.allOf(TipoSinistro.class)
    ),

    /**
     * Declaração de testemunha ocular.
     */
    DECLARACAO_TESTEMUNHA(
            "Declaração de Testemunha",
            Arrays.asList("application/pdf"),
            10,
            true,
            EnumSet.of(TipoSinistro.COLISAO, TipoSinistro.TERCEIROS)
    ),

    /**
     * Carta de franquia assinada.
     */
    CARTA_FRANQUIA(
            "Carta de Franquia",
            Arrays.asList("application/pdf"),
            5,
            true,
            EnumSet.of(TipoSinistro.COLISAO, TipoSinistro.TERCEIROS)
    );

    private final String descricao;
    private final List<String> formatosAceitos;
    private final int tamanhoMaximoMB;
    private final boolean requerAssinatura;
    private final Set<TipoSinistro> tiposSinistroObrigatorio;

    TipoDocumento(String descricao, List<String> formatosAceitos, int tamanhoMaximoMB,
                  boolean requerAssinatura, Set<TipoSinistro> tiposSinistroObrigatorio) {
        this.descricao = descricao;
        this.formatosAceitos = formatosAceitos;
        this.tamanhoMaximoMB = tamanhoMaximoMB;
        this.requerAssinatura = requerAssinatura;
        this.tiposSinistroObrigatorio = tiposSinistroObrigatorio;
    }

    /**
     * Verifica se o documento é obrigatório para o tipo de sinistro especificado.
     *
     * @param tipoSinistro Tipo de sinistro a verificar
     * @return true se for obrigatório
     */
    public boolean isObrigatorio(TipoSinistro tipoSinistro) {
        return tiposSinistroObrigatorio.contains(tipoSinistro);
    }

    /**
     * Retorna os formatos aceitos para este tipo de documento.
     *
     * @return Lista de MIME types aceitos
     */
    public List<String> getFormatosAceitos() {
        return formatosAceitos;
    }

    /**
     * Retorna o tamanho máximo em MB para este tipo de documento.
     *
     * @return Tamanho máximo em MB
     */
    public int getTamanhoMaximoMB() {
        return tamanhoMaximoMB;
    }

    /**
     * Verifica se este tipo de documento requer assinatura digital.
     *
     * @return true se requer assinatura
     */
    public boolean requerAssinatura() {
        return requerAssinatura;
    }

    /**
     * Retorna a descrição do tipo de documento.
     *
     * @return Descrição
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Verifica se o formato é aceito para este tipo de documento.
     *
     * @param mimeType MIME type a verificar
     * @return true se o formato é aceito
     */
    public boolean aceitaFormato(String mimeType) {
        return formatosAceitos.stream()
                .anyMatch(formato -> formato.equalsIgnoreCase(mimeType));
    }

    /**
     * Retorna os tipos de sinistro para os quais este documento é obrigatório.
     *
     * @return Set de tipos de sinistro
     */
    public Set<TipoSinistro> getTiposSinistroObrigatorio() {
        return EnumSet.copyOf(tiposSinistroObrigatorio);
    }
}
