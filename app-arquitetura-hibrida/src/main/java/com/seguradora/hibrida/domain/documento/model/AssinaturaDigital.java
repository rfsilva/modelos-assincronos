package com.seguradora.hibrida.domain.documento.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Value Object que representa uma assinatura digital em um documento.
 *
 * <p>Contém informações completas sobre a assinatura:
 * <ul>
 *   <li>Algoritmo utilizado</li>
 *   <li>Certificado digital (opcional)</li>
 *   <li>Timestamp da assinatura</li>
 *   <li>Dados do assinante (nome e CPF)</li>
 *   <li>Validade do certificado</li>
 * </ul>
 *
 * <p>Imutável e thread-safe.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@ToString(exclude = {"certificado"}) // Certificado pode ser grande
@EqualsAndHashCode
public final class AssinaturaDigital {

    private final TipoAssinatura tipo;
    private final String algoritmo;
    private final String certificado;
    private final Instant timestamp;
    private final String assinanteNome;
    private final String assinanteCpf;
    private final LocalDate validadeInicio;
    private final LocalDate validadeFim;

    /**
     * Construtor privado - use o builder.
     */
    private AssinaturaDigital(TipoAssinatura tipo, String algoritmo, String certificado,
                              Instant timestamp, String assinanteNome, String assinanteCpf,
                              LocalDate validadeInicio, LocalDate validadeFim) {
        this.tipo = tipo;
        this.algoritmo = algoritmo;
        this.certificado = certificado;
        this.timestamp = timestamp;
        this.assinanteNome = assinanteNome;
        this.assinanteCpf = assinanteCpf;
        this.validadeInicio = validadeInicio;
        this.validadeFim = validadeFim;
    }

    /**
     * Cria uma assinatura digital completa com certificado.
     *
     * @param algoritmo Algoritmo de assinatura (ex: RSA, ECDSA)
     * @param certificado Certificado digital em Base64
     * @param assinanteNome Nome do assinante
     * @param assinanteCpf CPF do assinante
     * @param validadeInicio Data de início da validade
     * @param validadeFim Data de fim da validade
     * @return Assinatura digital
     */
    public static AssinaturaDigital digital(String algoritmo, String certificado,
                                            String assinanteNome, String assinanteCpf,
                                            LocalDate validadeInicio, LocalDate validadeFim) {
        validarCamposObrigatorios(algoritmo, assinanteNome, assinanteCpf);
        validarCertificado(certificado);
        validarPeriodoValidade(validadeInicio, validadeFim);

        return new AssinaturaDigital(
                TipoAssinatura.DIGITAL,
                algoritmo,
                certificado,
                Instant.now(),
                assinanteNome,
                assinanteCpf,
                validadeInicio,
                validadeFim
        );
    }

    /**
     * Cria uma assinatura eletrônica simples.
     *
     * @param assinanteNome Nome do assinante
     * @param assinanteCpf CPF do assinante
     * @return Assinatura eletrônica
     */
    public static AssinaturaDigital eletronica(String assinanteNome, String assinanteCpf) {
        validarCamposObrigatorios("ELETRONICA", assinanteNome, assinanteCpf);

        return new AssinaturaDigital(
                TipoAssinatura.ELETRONICA,
                "ELETRONICA",
                null,
                Instant.now(),
                assinanteNome,
                assinanteCpf,
                LocalDate.now(),
                LocalDate.now().plusYears(1)
        );
    }

    /**
     * Cria uma assinatura física digitalizada.
     *
     * @param assinanteNome Nome do assinante
     * @param assinanteCpf CPF do assinante
     * @return Assinatura física digitalizada
     */
    public static AssinaturaDigital fisicaDigitalizada(String assinanteNome, String assinanteCpf) {
        validarCamposObrigatorios("FISICA", assinanteNome, assinanteCpf);

        return new AssinaturaDigital(
                TipoAssinatura.FISICA_DIGITALIZADA,
                "FISICA",
                null,
                Instant.now(),
                assinanteNome,
                assinanteCpf,
                LocalDate.now(),
                null // Sem data de expiração
        );
    }

    /**
     * Verifica se a assinatura é válida no momento atual.
     *
     * @return true se válida
     */
    public boolean isValida() {
        if (tipo == TipoAssinatura.SEM_ASSINATURA) {
            return false;
        }

        if (tipo == TipoAssinatura.FISICA_DIGITALIZADA) {
            return true; // Assinatura física não expira
        }

        LocalDate hoje = LocalDate.now();

        // Verificar período de validade
        if (validadeInicio != null && hoje.isBefore(validadeInicio)) {
            return false;
        }

        if (validadeFim != null && hoje.isAfter(validadeFim)) {
            return false;
        }

        return true;
    }

    /**
     * Verifica se a assinatura está expirada.
     *
     * @return true se expirada
     */
    public boolean expirada() {
        if (tipo == TipoAssinatura.FISICA_DIGITALIZADA || tipo == TipoAssinatura.SEM_ASSINATURA) {
            return false; // Estes tipos não expiram
        }

        if (validadeFim == null) {
            return false;
        }

        return LocalDate.now().isAfter(validadeFim);
    }

    /**
     * Verifica se a assinatura ainda não iniciou sua validade.
     *
     * @return true se ainda não iniciou
     */
    public boolean naoIniciada() {
        if (validadeInicio == null) {
            return false;
        }

        return LocalDate.now().isBefore(validadeInicio);
    }

    /**
     * Retorna os detalhes da assinatura em formato legível.
     *
     * @return String com detalhes
     */
    public String getDetalhes() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tipo: ").append(tipo.getDescricao()).append("\n");
        sb.append("Assinante: ").append(assinanteNome).append(" (CPF: ").append(formatarCpf()).append(")\n");
        sb.append("Algoritmo: ").append(algoritmo).append("\n");
        sb.append("Data: ").append(timestamp).append("\n");

        if (validadeInicio != null) {
            sb.append("Válido de: ").append(validadeInicio);
        }

        if (validadeFim != null) {
            sb.append(" até: ").append(validadeFim);
        }

        sb.append("\n");
        sb.append("Status: ").append(isValida() ? "VÁLIDA" : "INVÁLIDA");

        if (tipo.requerCertificado() && certificado != null) {
            sb.append("\nCertificado: Presente (").append(certificado.length()).append(" bytes)");
        }

        return sb.toString();
    }

    /**
     * Formata o CPF para exibição (XXX.XXX.XXX-XX).
     *
     * @return CPF formatado
     */
    private String formatarCpf() {
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
        if (validadeFim == null || tipo == TipoAssinatura.FISICA_DIGITALIZADA) {
            return -1;
        }

        LocalDate hoje = LocalDate.now();
        if (hoje.isAfter(validadeFim)) {
            return 0; // Já expirou
        }

        return java.time.temporal.ChronoUnit.DAYS.between(hoje, validadeFim);
    }

    /**
     * Verifica se a assinatura está próxima de expirar (30 dias).
     *
     * @return true se está próxima de expirar
     */
    public boolean proximaDeExpirar() {
        long dias = diasParaExpirar();
        return dias >= 0 && dias <= 30;
    }

    /**
     * Verifica se o CPF corresponde ao fornecido.
     *
     * @param cpf CPF para comparação
     * @return true se corresponder
     */
    public boolean cpfCorresponde(String cpf) {
        if (cpf == null || assinanteCpf == null) {
            return false;
        }

        // Remover formatação para comparar
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        String assinanteCpfLimpo = assinanteCpf.replaceAll("[^0-9]", "");

        return cpfLimpo.equals(assinanteCpfLimpo);
    }

    /**
     * Verifica se possui certificado digital anexado.
     *
     * @return true se possui certificado
     */
    public boolean possuiCertificado() {
        return certificado != null && !certificado.isEmpty();
    }

    // Métodos de validação privados

    private static void validarCamposObrigatorios(String algoritmo, String nome, String cpf) {
        Objects.requireNonNull(algoritmo, "Algoritmo não pode ser nulo");
        Objects.requireNonNull(nome, "Nome do assinante não pode ser nulo");
        Objects.requireNonNull(cpf, "CPF do assinante não pode ser nulo");

        if (nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do assinante não pode ser vazio");
        }

        if (!cpf.matches("\\d{11}")) {
            throw new IllegalArgumentException("CPF inválido: " + cpf);
        }
    }

    private static void validarCertificado(String certificado) {
        if (certificado == null || certificado.isEmpty()) {
            throw new IllegalArgumentException("Certificado digital é obrigatório para assinatura digital");
        }
    }

    private static void validarPeriodoValidade(LocalDate inicio, LocalDate fim) {
        Objects.requireNonNull(inicio, "Data de início da validade não pode ser nula");
        Objects.requireNonNull(fim, "Data de fim da validade não pode ser nula");

        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("Data de fim não pode ser anterior à data de início");
        }

        if (fim.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Certificado já expirado");
        }
    }
}
