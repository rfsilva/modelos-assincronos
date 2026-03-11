package com.seguradora.hibrida.domain.documento.service;

import com.seguradora.hibrida.domain.documento.config.DocumentoProperties;
import com.seguradora.hibrida.domain.documento.model.AssinaturaDigital;
import com.seguradora.hibrida.domain.documento.model.HashDocumento;
import com.seguradora.hibrida.domain.documento.model.TipoDocumento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Serviço de validação de documentos.
 *
 * <p>Realiza validações completas incluindo:
 * <ul>
 *   <li>Validação de tipo e formato (MIME type vs extensão)</li>
 *   <li>Validação de tamanho (limites por tipo)</li>
 *   <li>Validação de conteúdo (magic bytes, estrutura)</li>
 *   <li>Validação de integridade (hash)</li>
 *   <li>Validação de assinaturas digitais</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentoValidatorService {

    private final DocumentoProperties properties;

    // Magic bytes para identificação de formatos
    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46}; // %PDF
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47};

    /**
     * Valida se o tipo e formato são compatíveis.
     *
     * @param tipo Tipo do documento
     * @param formato MIME type
     * @return Lista de erros (vazia se válido)
     */
    public List<String> validarTipo(TipoDocumento tipo, String formato) {
        List<String> erros = new ArrayList<>();

        if (tipo == null) {
            erros.add("Tipo de documento não especificado");
            return erros;
        }

        if (formato == null || formato.isEmpty()) {
            erros.add("Formato não especificado");
            return erros;
        }

        if (!tipo.aceitaFormato(formato)) {
            erros.add(String.format("Formato '%s' não é aceito para documentos do tipo '%s'. " +
                            "Formatos aceitos: %s",
                    formato, tipo.getDescricao(), String.join(", ", tipo.getFormatosAceitos())));
        }

        return erros;
    }

    /**
     * Valida se o tamanho está dentro dos limites.
     *
     * @param tamanho Tamanho em bytes
     * @param tipo Tipo do documento
     * @return Lista de erros (vazia se válido)
     */
    public List<String> validarTamanho(long tamanho, TipoDocumento tipo) {
        List<String> erros = new ArrayList<>();

        if (tamanho <= 0) {
            erros.add("Tamanho inválido: arquivo vazio");
            return erros;
        }

        if (tipo == null) {
            erros.add("Tipo de documento não especificado");
            return erros;
        }

        long tamanhoMaximoBytes = tipo.getTamanhoMaximoMB() * 1024L * 1024L;

        if (tamanho > tamanhoMaximoBytes) {
            erros.add(String.format("Tamanho do arquivo (%.2f MB) excede o limite de %d MB para %s",
                    tamanho / (1024.0 * 1024.0), tipo.getTamanhoMaximoMB(), tipo.getDescricao()));
        }

        return erros;
    }

    /**
     * Valida o conteúdo do documento (estrutura e magic bytes).
     *
     * @param conteudo Conteúdo em bytes
     * @param tipo Tipo do documento
     * @return Lista de erros (vazia se válido)
     */
    public List<String> validarConteudo(byte[] conteudo, TipoDocumento tipo) {
        List<String> erros = new ArrayList<>();

        if (conteudo == null || conteudo.length == 0) {
            erros.add("Conteúdo vazio ou nulo");
            return erros;
        }

        if (tipo == null) {
            erros.add("Tipo de documento não especificado");
            return erros;
        }

        // Validar baseado nos formatos aceitos
        for (String formato : tipo.getFormatosAceitos()) {
            if (formato.equals("application/pdf")) {
                if (!validarPDF(conteudo)) {
                    erros.add("Arquivo não é um PDF válido");
                }
            } else if (formato.equals("image/jpeg")) {
                if (!validarJPEG(conteudo)) {
                    erros.add("Arquivo não é um JPEG válido");
                }
            } else if (formato.equals("image/png")) {
                if (!validarPNG(conteudo)) {
                    erros.add("Arquivo não é um PNG válido");
                }
            }
        }

        return erros;
    }

    /**
     * Valida a integridade do conteúdo através do hash.
     *
     * @param conteudo Conteúdo em bytes
     * @param hash Hash esperado
     * @return Lista de erros (vazia se válido)
     */
    public List<String> validarHash(byte[] conteudo, HashDocumento hash) {
        List<String> erros = new ArrayList<>();

        if (conteudo == null || conteudo.length == 0) {
            erros.add("Conteúdo vazio ou nulo");
            return erros;
        }

        if (hash == null || !hash.isValido()) {
            erros.add("Hash inválido ou não especificado");
            return erros;
        }

        try {
            if (!hash.validar(conteudo)) {
                erros.add("Integridade comprometida: hash não corresponde ao conteúdo");
                log.warn("Validação de hash falhou - possível corrupção de dados");
            }
        } catch (Exception e) {
            erros.add("Erro ao validar hash: " + e.getMessage());
            log.error("Erro na validação de hash", e);
        }

        return erros;
    }

    /**
     * Valida uma assinatura digital.
     *
     * @param assinatura Assinatura a validar
     * @return Lista de erros (vazia se válida)
     */
    public List<String> validarAssinatura(AssinaturaDigital assinatura) {
        List<String> erros = new ArrayList<>();

        if (assinatura == null) {
            erros.add("Assinatura não especificada");
            return erros;
        }

        if (!assinatura.isValida()) {
            if (assinatura.expirada()) {
                erros.add("Assinatura expirada em " + assinatura.getValidadeFim());
            } else if (assinatura.naoIniciada()) {
                erros.add("Assinatura ainda não iniciou sua validade");
            } else {
                erros.add("Assinatura inválida");
            }
        }

        if (assinatura.getTipo().requerCertificado() && !assinatura.possuiCertificado()) {
            erros.add("Certificado digital é obrigatório para este tipo de assinatura");
        }

        if (assinatura.proximaDeExpirar()) {
            long dias = assinatura.diasParaExpirar();
            erros.add(String.format("AVISO: Assinatura próxima de expirar (%d dias)", dias));
        }

        return erros;
    }

    /**
     * Validação completa de documento.
     *
     * @param conteudo Conteúdo em bytes
     * @param tipo Tipo do documento
     * @param formato MIME type
     * @param hash Hash esperado
     * @return Lista de erros (vazia se válido)
     */
    public List<String> validarCompleto(byte[] conteudo, TipoDocumento tipo,
                                        String formato, HashDocumento hash) {
        List<String> erros = new ArrayList<>();

        erros.addAll(validarTipo(tipo, formato));
        erros.addAll(validarTamanho(conteudo != null ? conteudo.length : 0, tipo));
        erros.addAll(validarConteudo(conteudo, tipo));
        erros.addAll(validarHash(conteudo, hash));

        return erros;
    }

    // ==================== Validações Específicas de Formato ====================

    /**
     * Valida se o conteúdo é um PDF válido.
     */
    private boolean validarPDF(byte[] conteudo) {
        if (conteudo.length < PDF_MAGIC.length) {
            return false;
        }

        // Verificar magic bytes
        for (int i = 0; i < PDF_MAGIC.length; i++) {
            if (conteudo[i] != PDF_MAGIC[i]) {
                return false;
            }
        }

        // Verificar se termina com %%EOF (opcional, mas recomendado)
        String fim = new String(conteudo, Math.max(0, conteudo.length - 10), Math.min(10, conteudo.length));
        boolean temEOF = fim.contains("%%EOF");

        if (!temEOF) {
            log.warn("PDF não termina com %%EOF - pode estar corrompido");
        }

        return true;
    }

    /**
     * Valida se o conteúdo é um JPEG válido.
     */
    private boolean validarJPEG(byte[] conteudo) {
        if (conteudo.length < JPEG_MAGIC.length) {
            return false;
        }

        // Verificar magic bytes
        for (int i = 0; i < JPEG_MAGIC.length; i++) {
            if (conteudo[i] != JPEG_MAGIC[i]) {
                return false;
            }
        }

        // Tentar carregar como imagem
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(conteudo);
            return ImageIO.read(bais) != null;
        } catch (IOException e) {
            log.warn("Falha ao validar JPEG: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Valida se o conteúdo é um PNG válido.
     */
    private boolean validarPNG(byte[] conteudo) {
        if (conteudo.length < PNG_MAGIC.length) {
            return false;
        }

        // Verificar magic bytes
        for (int i = 0; i < PNG_MAGIC.length; i++) {
            if (conteudo[i] != PNG_MAGIC[i]) {
                return false;
            }
        }

        // Tentar carregar como imagem
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(conteudo);
            return ImageIO.read(bais) != null;
        } catch (IOException e) {
            log.warn("Falha ao validar PNG: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Detecta o formato real do arquivo baseado em magic bytes.
     *
     * @param conteudo Conteúdo em bytes
     * @return MIME type detectado ou "application/octet-stream"
     */
    public String detectarFormato(byte[] conteudo) {
        if (conteudo == null || conteudo.length < 4) {
            return "application/octet-stream";
        }

        if (validarPDF(conteudo)) {
            return "application/pdf";
        }

        if (validarJPEG(conteudo)) {
            return "image/jpeg";
        }

        if (validarPNG(conteudo)) {
            return "image/png";
        }

        return "application/octet-stream";
    }

    /**
     * Verifica se o formato detectado corresponde ao declarado.
     *
     * @param conteudo Conteúdo em bytes
     * @param formatoDeclarado MIME type declarado
     * @return true se corresponder
     */
    public boolean verificarFormatoReal(byte[] conteudo, String formatoDeclarado) {
        String formatoDetectado = detectarFormato(conteudo);
        boolean corresponde = formatoDetectado.equals(formatoDeclarado);

        if (!corresponde) {
            log.warn("Formato declarado ({}) difere do detectado ({})",
                    formatoDeclarado, formatoDetectado);
        }

        return corresponde;
    }
}
