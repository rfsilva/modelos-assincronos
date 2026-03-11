package com.seguradora.hibrida.domain.documento.event;

import com.seguradora.hibrida.domain.documento.model.TipoAssinatura;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Evento disparado quando um documento recebe uma assinatura digital.
 *
 * <p>Registra a aplicação de assinatura ao documento:
 * <ul>
 *   <li>Tipo de assinatura aplicada</li>
 *   <li>Dados completos da assinatura</li>
 *   <li>Informações do assinante</li>
 *   <li>Validade do certificado</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true, exclude = {"certificado"})
public class DocumentoAssinadoEvent extends DomainEvent {

    /**
     * ID do documento assinado.
     */
    private String documentoId;

    /**
     * Tipo de assinatura aplicada.
     */
    private TipoAssinatura tipoAssinatura;

    /**
     * Algoritmo utilizado na assinatura.
     */
    private String algoritmo;

    /**
     * Certificado digital em Base64 (para assinatura digital).
     */
    private String certificado;

    /**
     * Nome do assinante.
     */
    private String assinanteNome;

    /**
     * CPF do assinante.
     */
    private String assinanteCpf;

    /**
     * Data de início da validade do certificado.
     */
    private LocalDate validadeInicio;

    /**
     * Data de fim da validade do certificado.
     */
    private LocalDate validadeFim;

    /**
     * Hash do documento no momento da assinatura.
     */
    private String hashDocumento;

    /**
     * Construtor completo para assinatura digital.
     */
    public DocumentoAssinadoEvent(String documentoId, TipoAssinatura tipoAssinatura,
                                  String algoritmo, String certificado,
                                  String assinanteNome, String assinanteCpf,
                                  LocalDate validadeInicio, LocalDate validadeFim,
                                  String hashDocumento) {
        super();
        this.documentoId = documentoId;
        this.tipoAssinatura = tipoAssinatura;
        this.algoritmo = algoritmo;
        this.certificado = certificado;
        this.assinanteNome = assinanteNome;
        this.assinanteCpf = assinanteCpf;
        this.validadeInicio = validadeInicio;
        this.validadeFim = validadeFim;
        this.hashDocumento = hashDocumento;
    }

    /**
     * Construtor simplificado para assinatura eletrônica.
     */
    public DocumentoAssinadoEvent(String documentoId, String assinanteNome,
                                  String assinanteCpf, String hashDocumento) {
        this(documentoId, TipoAssinatura.ELETRONICA, "ELETRONICA", null,
                assinanteNome, assinanteCpf, LocalDate.now(),
                LocalDate.now().plusYears(1), hashDocumento);
    }

    /**
     * Verifica se possui certificado digital.
     *
     * @return true se possui certificado
     */
    public boolean possuiCertificado() {
        return certificado != null && !certificado.isEmpty();
    }

    /**
     * Verifica se a assinatura está válida na data atual.
     *
     * @return true se válida
     */
    public boolean isAssinaturaValida() {
        LocalDate hoje = LocalDate.now();

        if (validadeInicio != null && hoje.isBefore(validadeInicio)) {
            return false;
        }

        if (validadeFim != null && hoje.isAfter(validadeFim)) {
            return false;
        }

        return true;
    }

    /**
     * Retorna o CPF formatado.
     *
     * @return CPF no formato XXX.XXX.XXX-XX
     */
    public String getCpfFormatado() {
        if (assinanteCpf == null || assinanteCpf.length() != 11) {
            return assinanteCpf;
        }

        return String.format("%s.%s.%s-%s",
                assinanteCpf.substring(0, 3),
                assinanteCpf.substring(3, 6),
                assinanteCpf.substring(6, 9),
                assinanteCpf.substring(9, 11)
        );
    }

    /**
     * Calcula quantos dias faltam para expirar.
     *
     * @return Dias até expiração, ou -1 se não aplicável
     */
    public long diasParaExpirar() {
        if (validadeFim == null) {
            return -1;
        }

        LocalDate hoje = LocalDate.now();
        if (hoje.isAfter(validadeFim)) {
            return 0; // Já expirou
        }

        return java.time.temporal.ChronoUnit.DAYS.between(hoje, validadeFim);
    }

    /**
     * Verifica se requer certificado digital.
     *
     * @return true se requer
     */
    public boolean requerCertificado() {
        return tipoAssinatura != null && tipoAssinatura.requerCertificado();
    }
}
