package com.seguradora.hibrida.domain.documento.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Propriedades de configuração para o módulo de documentos.
 *
 * <p>Agrupa todas as configurações relacionadas a:
 * <ul>
 *   <li>Armazenamento (paths, criptografia)</li>
 *   <li>Validação (tamanhos máximos, formatos)</li>
 *   <li>Performance (cache, threads)</li>
 * </ul>
 *
 * <p>Configurações via application.properties/yml com prefixo "documento".
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "documento")
public class DocumentoProperties {

    /**
     * Configurações de armazenamento.
     */
    private Storage storage = new Storage();

    /**
     * Configurações de validação.
     */
    private Validation validation = new Validation();

    /**
     * Configurações de performance.
     */
    private Performance performance = new Performance();

    @Data
    public static class Storage {
        /**
         * Configurações de path de armazenamento.
         */
        private Path path = new Path();

        /**
         * Configurações de criptografia.
         */
        private Encryption encryption = new Encryption();

        @Data
        public static class Path {
            /**
             * Path primário para armazenamento de documentos.
             * Default: ./data/documentos
             */
            private String primary = "./data/documentos";

            /**
             * Path secundário para backup de documentos.
             * Default: null (sem backup)
             */
            private String secondary;

            /**
             * Criar diretórios automaticamente se não existirem.
             * Default: true
             */
            private boolean autoCreate = true;
        }

        @Data
        public static class Encryption {
            /**
             * Habilitar criptografia de documentos em repouso.
             * Default: false
             */
            private boolean enabled = false;

            /**
             * Algoritmo de criptografia.
             * Suportado: AES-256
             * Default: AES-256
             */
            private String algorithm = "AES-256";

            /**
             * Path para arquivo de chaves (keystore).
             * Se não especificado, usa chave hardcoded (não recomendado para produção).
             */
            private String keystorePath;

            /**
             * Senha do keystore.
             */
            private String keystorePassword;
        }
    }

    @Data
    public static class Validation {
        /**
         * Tamanhos máximos em MB por tipo de arquivo.
         */
        private MaxSize maxSizeMB = new MaxSize();

        /**
         * Formatos permitidos por categoria.
         */
        private AllowedFormats allowedFormats = new AllowedFormats();

        /**
         * Validações estritas de conteúdo.
         */
        private Strict strict = new Strict();

        @Data
        public static class MaxSize {
            /**
             * Tamanho máximo para PDFs em MB.
             * Default: 15
             */
            private int pdf = 15;

            /**
             * Tamanho máximo para imagens em MB.
             * Default: 10
             */
            private int image = 10;

            /**
             * Tamanho máximo para documentos XML em MB.
             * Default: 5
             */
            private int xml = 5;

            /**
             * Tamanho máximo para planilhas em MB.
             * Default: 20
             */
            private int spreadsheet = 20;
        }

        @Data
        public static class AllowedFormats {
            /**
             * Formatos PDF permitidos.
             */
            private String[] pdf = {"application/pdf"};

            /**
             * Formatos de imagem permitidos.
             */
            private String[] image = {"image/jpeg", "image/png", "image/heic"};

            /**
             * Formatos XML permitidos.
             */
            private String[] xml = {"application/xml", "text/xml"};

            /**
             * Formatos de planilha permitidos.
             */
            private String[] spreadsheet = {
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            };
        }

        @Data
        public static class Strict {
            /**
             * Validar magic bytes dos arquivos.
             * Default: true
             */
            private boolean validateMagicBytes = true;

            /**
             * Validar estrutura interna de PDFs.
             * Default: false (performance)
             */
            private boolean validatePdfStructure = false;

            /**
             * Validar imagens carregando-as completamente.
             * Default: true
             */
            private boolean validateImageLoad = true;

            /**
             * Rejeitar arquivos com formato declarado diferente do detectado.
             * Default: true
             */
            private boolean rejectFormatMismatch = true;
        }
    }

    @Data
    public static class Performance {
        /**
         * Tamanho do buffer para leitura/escrita em KB.
         * Default: 64
         */
        private int bufferSizeKB = 64;

        /**
         * Timeout para operações de I/O em segundos.
         * Default: 30
         */
        private int ioTimeoutSeconds = 30;

        /**
         * Número de threads para processamento paralelo.
         * Default: 4
         */
        private int processingThreads = 4;

        /**
         * Habilitar cache de documentos em memória.
         * Default: false (documentos podem ser grandes)
         */
        private boolean enableCache = false;

        /**
         * Tamanho máximo do cache em MB.
         * Default: 100
         */
        private int cacheSizeMB = 100;
    }

    /**
     * Retorna o tamanho máximo em bytes para um tipo MIME.
     *
     * @param mimeType MIME type do arquivo
     * @return Tamanho máximo em bytes
     */
    public long getMaxSizeBytes(String mimeType) {
        int sizeMB;

        if (mimeType == null) {
            return 10 * 1024 * 1024L; // Default 10 MB
        }

        if (mimeType.startsWith("image/")) {
            sizeMB = validation.maxSizeMB.image;
        } else if (mimeType.equals("application/pdf")) {
            sizeMB = validation.maxSizeMB.pdf;
        } else if (mimeType.contains("xml")) {
            sizeMB = validation.maxSizeMB.xml;
        } else if (mimeType.contains("sheet") || mimeType.contains("excel")) {
            sizeMB = validation.maxSizeMB.spreadsheet;
        } else {
            sizeMB = 10; // Default
        }

        return sizeMB * 1024L * 1024L;
    }

    /**
     * Verifica se um formato MIME é permitido.
     *
     * @param mimeType MIME type a verificar
     * @return true se é permitido
     */
    public boolean isFormatoPermitido(String mimeType) {
        if (mimeType == null) {
            return false;
        }

        // Verificar em todas as categorias
        return contains(validation.allowedFormats.pdf, mimeType) ||
               contains(validation.allowedFormats.image, mimeType) ||
               contains(validation.allowedFormats.xml, mimeType) ||
               contains(validation.allowedFormats.spreadsheet, mimeType);
    }

    private boolean contains(String[] array, String value) {
        if (array == null) {
            return false;
        }

        for (String item : array) {
            if (item.equalsIgnoreCase(value)) {
                return true;
            }
        }

        return false;
    }
}
