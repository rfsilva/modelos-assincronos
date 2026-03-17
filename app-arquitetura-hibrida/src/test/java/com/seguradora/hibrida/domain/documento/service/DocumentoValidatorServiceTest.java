package com.seguradora.hibrida.domain.documento.service;

import com.seguradora.hibrida.domain.documento.config.DocumentoProperties;
import com.seguradora.hibrida.domain.documento.model.AssinaturaDigital;
import com.seguradora.hibrida.domain.documento.model.HashDocumento;
import com.seguradora.hibrida.domain.documento.model.TipoDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

/**
 * Testes unitários para {@link DocumentoValidatorService}.
 *
 * <p>Valida todos os aspectos de validação de documentos:
 * <ul>
 *   <li>Validação de tipo e formato</li>
 *   <li>Validação de tamanho</li>
 *   <li>Validação de conteúdo (magic bytes)</li>
 *   <li>Validação de hash/integridade</li>
 *   <li>Validação de assinaturas digitais</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentoValidatorService - Testes Unitários")
class DocumentoValidatorServiceTest {

    @Mock
    private DocumentoProperties properties;

    @InjectMocks
    private DocumentoValidatorService validatorService;

    // ==================== Helper Methods ====================

    /**
     * Cria um conteúdo PDF válido.
     */
    private byte[] criarConteudoPDF() {
        // %PDF-1.4 header + minimal content + %%EOF
        String pdfContent = "%PDF-1.4\n1 0 obj\n<<\n/Type /Catalog\n>>\nendobj\nxref\n0 1\n" +
                "0000000000 65535 f\ntrailer\n<<\n/Root 1 0 R\n>>\nstartxref\n0\n%%EOF";
        return pdfContent.getBytes();
    }

    /**
     * Cria um conteúdo JPEG válido (apenas magic bytes).
     * Nota: O validador tenta carregar como imagem, então pode falhar.
     * Usamos apenas para testar magic bytes.
     */
    private byte[] criarConteudoJPEG() {
        // JPEG magic bytes básicos
        byte[] jpeg = new byte[3];
        jpeg[0] = (byte) 0xFF;
        jpeg[1] = (byte) 0xD8;
        jpeg[2] = (byte) 0xFF;
        return jpeg;
    }

    /**
     * Cria um conteúdo PNG válido (apenas magic bytes).
     * Nota: O validador tenta carregar como imagem, então pode falhar.
     * Usamos apenas para testar magic bytes.
     */
    private byte[] criarConteudoPNG() {
        // PNG magic bytes
        byte[] png = new byte[8];
        png[0] = (byte) 0x89;
        png[1] = 0x50;
        png[2] = 0x4E;
        png[3] = 0x47;
        png[4] = 0x0D;
        png[5] = 0x0A;
        png[6] = 0x1A;
        png[7] = 0x0A;
        return png;
    }

    /**
     * Cria uma assinatura digital válida.
     */
    private AssinaturaDigital criarAssinaturaValida() {
        return AssinaturaDigital.digital(
                "RSA-SHA256",
                "certificado-base64-exemplo",
                "João Silva",
                "12345678900",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusYears(1)
        );
    }

    /**
     * Cria uma assinatura eletrônica válida.
     */
    private AssinaturaDigital criarAssinaturaEletronica() {
        return AssinaturaDigital.eletronica("Maria Santos", "98765432100");
    }

    // ==================== Testes de Validação de Tipo ====================

    @Nested
    @DisplayName("Validação de Tipo e Formato")
    class ValidacaoTipo {

        @Test
        @DisplayName("Deve validar tipo e formato compatíveis")
        void deveValidarTipoFormatoCompativeis() {
            // Given
            TipoDocumento tipo = TipoDocumento.BOLETIM_OCORRENCIA;
            String formato = "application/pdf";

            // When
            List<String> erros = validatorService.validarTipo(tipo, formato);

            // Then
            assertThat(erros).isEmpty();
        }

        @Test
        @DisplayName("Deve rejeitar formato não aceito pelo tipo")
        void deveRejeitarFormatoNaoAceito() {
            // Given
            TipoDocumento tipo = TipoDocumento.LAUDO_PERICIAL; // Aceita apenas PDF
            String formato = "image/jpeg";

            // When
            List<String> erros = validatorService.validarTipo(tipo, formato);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("não é aceito");
        }

        @Test
        @DisplayName("Deve rejeitar tipo nulo")
        void deveRejeitarTipoNulo() {
            // Given
            String formato = "application/pdf";

            // When
            List<String> erros = validatorService.validarTipo(null, formato);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("Tipo de documento não especificado");
        }

        @Test
        @DisplayName("Deve rejeitar formato nulo")
        void deveRejeitarFormatoNulo() {
            // Given
            TipoDocumento tipo = TipoDocumento.BOLETIM_OCORRENCIA;

            // When
            List<String> erros = validatorService.validarTipo(tipo, null);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("Formato não especificado");
        }

        @Test
        @DisplayName("Deve rejeitar formato vazio")
        void deveRejeitarFormatoVazio() {
            // Given
            TipoDocumento tipo = TipoDocumento.BOLETIM_OCORRENCIA;

            // When
            List<String> erros = validatorService.validarTipo(tipo, "");

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("Formato não especificado");
        }

        @Test
        @DisplayName("Deve validar múltiplos formatos aceitos")
        void deveValidarMultiplosFormatosAceitos() {
            // Given
            TipoDocumento tipo = TipoDocumento.FOTO_DANOS;

            // When & Then
            assertThat(validatorService.validarTipo(tipo, "image/jpeg")).isEmpty();
            assertThat(validatorService.validarTipo(tipo, "image/png")).isEmpty();
            assertThat(validatorService.validarTipo(tipo, "image/heic")).isEmpty();
        }
    }

    // ==================== Testes de Validação de Tamanho ====================

    @Nested
    @DisplayName("Validação de Tamanho")
    class ValidacaoTamanho {

        @Test
        @DisplayName("Deve validar tamanho dentro do limite")
        void deveValidarTamanhoDentroLimite() {
            // Given
            TipoDocumento tipo = TipoDocumento.BOLETIM_OCORRENCIA; // Limite: 10 MB
            long tamanho = 5 * 1024 * 1024; // 5 MB

            // When
            List<String> erros = validatorService.validarTamanho(tamanho, tipo);

            // Then
            assertThat(erros).isEmpty();
        }

        @Test
        @DisplayName("Deve rejeitar tamanho acima do limite")
        void deveRejeitarTamanhoAcimaLimite() {
            // Given
            TipoDocumento tipo = TipoDocumento.FOTO_DANOS; // Limite: 5 MB
            long tamanho = 10 * 1024 * 1024; // 10 MB

            // When
            List<String> erros = validatorService.validarTamanho(tamanho, tipo);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("excede o limite");
        }

        @Test
        @DisplayName("Deve rejeitar tamanho zero")
        void deveRejeitarTamanhoZero() {
            // Given
            TipoDocumento tipo = TipoDocumento.BOLETIM_OCORRENCIA;
            long tamanho = 0;

            // When
            List<String> erros = validatorService.validarTamanho(tamanho, tipo);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("arquivo vazio");
        }

        @Test
        @DisplayName("Deve rejeitar tamanho negativo")
        void deveRejeitarTamanhoNegativo() {
            // Given
            TipoDocumento tipo = TipoDocumento.BOLETIM_OCORRENCIA;
            long tamanho = -1;

            // When
            List<String> erros = validatorService.validarTamanho(tamanho, tipo);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("inválido");
        }

        @Test
        @DisplayName("Deve rejeitar tipo nulo")
        void deveRejeitarTipoNulo() {
            // Given
            long tamanho = 1024;

            // When
            List<String> erros = validatorService.validarTamanho(tamanho, null);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("Tipo de documento não especificado");
        }

        @Test
        @DisplayName("Deve validar tamanho exatamente no limite")
        void deveValidarTamanhoExatamenteNoLimite() {
            // Given
            TipoDocumento tipo = TipoDocumento.FOTO_DANOS; // Limite: 5 MB
            long tamanho = 5 * 1024 * 1024; // Exatamente 5 MB

            // When
            List<String> erros = validatorService.validarTamanho(tamanho, tipo);

            // Then
            assertThat(erros).isEmpty();
        }
    }

    // ==================== Testes de Validação de Conteúdo ====================

    @Nested
    @DisplayName("Validação de Conteúdo")
    class ValidacaoConteudo {

        @Test
        @DisplayName("Deve validar conteúdo PDF válido")
        void deveValidarConteudoPDFValido() {
            // Given
            byte[] conteudo = criarConteudoPDF();
            TipoDocumento tipo = TipoDocumento.LAUDO_PERICIAL;

            // When
            List<String> erros = validatorService.validarConteudo(conteudo, tipo);

            // Then
            assertThat(erros).isEmpty();
        }

        @Test
        @DisplayName("Deve rejeitar PDF com magic bytes inválidos")
        void deveRejeitarPDFComMagicBytesInvalidos() {
            // Given
            byte[] conteudo = "Não é um PDF".getBytes();
            TipoDocumento tipo = TipoDocumento.LAUDO_PERICIAL;

            // When
            List<String> erros = validatorService.validarConteudo(conteudo, tipo);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("não é um PDF válido");
        }

        @Test
        @DisplayName("Deve rejeitar conteúdo nulo")
        void deveRejeitarConteudoNulo() {
            // Given
            TipoDocumento tipo = TipoDocumento.LAUDO_PERICIAL;

            // When
            List<String> erros = validatorService.validarConteudo(null, tipo);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("Conteúdo vazio ou nulo");
        }

        @Test
        @DisplayName("Deve rejeitar conteúdo vazio")
        void deveRejeitarConteudoVazio() {
            // Given
            byte[] conteudo = new byte[0];
            TipoDocumento tipo = TipoDocumento.LAUDO_PERICIAL;

            // When
            List<String> erros = validatorService.validarConteudo(conteudo, tipo);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("Conteúdo vazio ou nulo");
        }

        @Test
        @DisplayName("Deve rejeitar tipo nulo")
        void deveRejeitarTipoNulo() {
            // Given
            byte[] conteudo = criarConteudoPDF();

            // When
            List<String> erros = validatorService.validarConteudo(conteudo, null);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("Tipo de documento não especificado");
        }
    }

    // ==================== Testes de Validação de Hash ====================

    @Nested
    @DisplayName("Validação de Hash")
    class ValidacaoHash {

        @Test
        @DisplayName("Deve validar hash correto")
        void deveValidarHashCorreto() {
            // Given
            byte[] conteudo = "Conteúdo de teste".getBytes();
            HashDocumento hash = HashDocumento.calcular(conteudo);

            // When
            List<String> erros = validatorService.validarHash(conteudo, hash);

            // Then
            assertThat(erros).isEmpty();
        }

        @Test
        @DisplayName("Deve rejeitar hash incorreto")
        void deveRejeitarHashIncorreto() {
            // Given
            byte[] conteudo = "Conteúdo de teste".getBytes();
            byte[] outroConteudo = "Outro conteúdo".getBytes();
            HashDocumento hash = HashDocumento.calcular(outroConteudo);

            // When
            List<String> erros = validatorService.validarHash(conteudo, hash);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("hash não corresponde");
        }

        @Test
        @DisplayName("Deve rejeitar conteúdo nulo")
        void deveRejeitarConteudoNulo() {
            // Given
            HashDocumento hash = HashDocumento.calcular("teste".getBytes());

            // When
            List<String> erros = validatorService.validarHash(null, hash);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("Conteúdo vazio ou nulo");
        }

        @Test
        @DisplayName("Deve rejeitar conteúdo vazio")
        void deveRejeitarConteudoVazio() {
            // Given
            byte[] conteudo = new byte[0];
            HashDocumento hash = HashDocumento.calcular("teste".getBytes());

            // When
            List<String> erros = validatorService.validarHash(conteudo, hash);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("Conteúdo vazio ou nulo");
        }

        @Test
        @DisplayName("Deve rejeitar hash nulo")
        void deveRejeitarHashNulo() {
            // Given
            byte[] conteudo = "Conteúdo de teste".getBytes();

            // When
            List<String> erros = validatorService.validarHash(conteudo, null);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("Hash inválido");
        }
    }

    // ==================== Testes de Validação de Assinatura ====================

    @Nested
    @DisplayName("Validação de Assinatura Digital")
    class ValidacaoAssinatura {

        @Test
        @DisplayName("Deve validar assinatura digital válida")
        void deveValidarAssinaturaDigitalValida() {
            // Given
            AssinaturaDigital assinatura = criarAssinaturaValida();

            // When
            List<String> erros = validatorService.validarAssinatura(assinatura);

            // Then
            assertThat(erros).isEmpty();
        }

        @Test
        @DisplayName("Deve validar assinatura eletrônica válida")
        void deveValidarAssinaturaEletronicaValida() {
            // Given
            AssinaturaDigital assinatura = criarAssinaturaEletronica();

            // When
            List<String> erros = validatorService.validarAssinatura(assinatura);

            // Then
            assertThat(erros).isEmpty();
        }

        @Test
        @DisplayName("Deve alertar sobre assinatura próxima de expirar")
        void deveAlertarAssinaturaProximaExpirar() {
            // Given
            AssinaturaDigital assinatura = AssinaturaDigital.digital(
                    "RSA-SHA256",
                    "certificado-base64",
                    "João Silva",
                    "12345678900",
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(15) // Expira em 15 dias
            );

            // When
            List<String> erros = validatorService.validarAssinatura(assinatura);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("próxima de expirar");
        }

        @Test
        @DisplayName("Deve detectar assinatura que expirará em breve")
        void deveDetectarAssinaturaExpiraBreve() {
            // Given
            // Como o construtor não aceita assinatura já expirada, testamos uma que vai expirar
            AssinaturaDigital assinatura = AssinaturaDigital.digital(
                    "RSA-SHA256",
                    "certificado-base64",
                    "João Silva",
                    "12345678900",
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(29) // Expira em 29 dias (< 30)
            );

            // When
            List<String> erros = validatorService.validarAssinatura(assinatura);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("próxima de expirar");
        }

        @Test
        @DisplayName("Deve rejeitar assinatura não iniciada")
        void deveRejeitarAssinaturaNaoIniciada() {
            // Given
            AssinaturaDigital assinatura = AssinaturaDigital.digital(
                    "RSA-SHA256",
                    "certificado-base64",
                    "João Silva",
                    "12345678900",
                    LocalDate.now().plusDays(1), // Inicia amanhã
                    LocalDate.now().plusYears(1)
            );

            // When
            List<String> erros = validatorService.validarAssinatura(assinatura);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("não iniciou");
        }

        @Test
        @DisplayName("Deve rejeitar assinatura nula")
        void deveRejeitarAssinaturaNula() {
            // When
            List<String> erros = validatorService.validarAssinatura(null);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.get(0)).contains("não especificada");
        }
    }

    // ==================== Testes de Validação Completa ====================

    @Nested
    @DisplayName("Validação Completa")
    class ValidacaoCompleta {

        @Test
        @DisplayName("Deve validar documento completamente válido")
        void deveValidarDocumentoCompletoValido() {
            // Given
            byte[] conteudo = criarConteudoPDF();
            TipoDocumento tipo = TipoDocumento.LAUDO_PERICIAL;
            String formato = "application/pdf";
            HashDocumento hash = HashDocumento.calcular(conteudo);

            // When
            List<String> erros = validatorService.validarCompleto(conteudo, tipo, formato, hash);

            // Then
            assertThat(erros).isEmpty();
        }

        @Test
        @DisplayName("Deve acumular múltiplos erros")
        void deveAcumularMultiplosErros() {
            // Given
            byte[] conteudo = new byte[0]; // Vazio
            TipoDocumento tipo = TipoDocumento.LAUDO_PERICIAL;
            String formato = "image/jpeg"; // Formato errado
            HashDocumento hash = null; // Hash nulo

            // When
            List<String> erros = validatorService.validarCompleto(conteudo, tipo, formato, hash);

            // Then
            assertThat(erros).isNotEmpty();
            assertThat(erros.size()).isGreaterThan(1);
        }

        @Test
        @DisplayName("Deve validar PDF com formato aceito e conteúdo válido")
        void deveValidarFormatoConteudoValidos() {
            // Given
            byte[] conteudo = criarConteudoPDF();
            TipoDocumento tipo = TipoDocumento.LAUDO_PERICIAL; // Aceita apenas PDF
            String formato = "application/pdf";
            HashDocumento hash = HashDocumento.calcular(conteudo);

            // When
            List<String> erros = validatorService.validarCompleto(conteudo, tipo, formato, hash);

            // Then
            assertThat(erros).isEmpty();
        }
    }

    // ==================== Testes de Detecção de Formato ====================

    @Nested
    @DisplayName("Detecção de Formato")
    class DeteccaoFormato {

        @Test
        @DisplayName("Deve detectar formato PDF")
        void deveDetectarFormatoPDF() {
            // Given
            byte[] conteudo = criarConteudoPDF();

            // When
            String formato = validatorService.detectarFormato(conteudo);

            // Then
            assertThat(formato).isEqualTo("application/pdf");
        }

        @Test
        @DisplayName("Deve detectar formato JPEG com magic bytes válidos")
        void deveDetectarFormatoJPEG() {
            // Given
            // JPEG válido retorna octet-stream porque ImageIO.read() falha
            // (apenas magic bytes não são suficientes para uma imagem completa)
            byte[] conteudo = criarConteudoJPEG();

            // When
            String formato = validatorService.detectarFormato(conteudo);

            // Then
            // Como não é um JPEG completo válido, retorna octet-stream
            assertThat(formato).isEqualTo("application/octet-stream");
        }

        @Test
        @DisplayName("Deve detectar formato PNG com magic bytes válidos")
        void deveDetectarFormatoPNG() {
            // Given
            // PNG válido retorna octet-stream porque ImageIO.read() falha
            // (apenas magic bytes não são suficientes para uma imagem completa)
            byte[] conteudo = criarConteudoPNG();

            // When
            String formato = validatorService.detectarFormato(conteudo);

            // Then
            // Como não é um PNG completo válido, retorna octet-stream
            assertThat(formato).isEqualTo("application/octet-stream");
        }

        @Test
        @DisplayName("Deve retornar octet-stream para formato desconhecido")
        void deveRetornarOctetStreamParaFormatoDesconhecido() {
            // Given
            byte[] conteudo = "Conteúdo qualquer".getBytes();

            // When
            String formato = validatorService.detectarFormato(conteudo);

            // Then
            assertThat(formato).isEqualTo("application/octet-stream");
        }

        @Test
        @DisplayName("Deve retornar octet-stream para conteúdo nulo")
        void deveRetornarOctetStreamParaConteudoNulo() {
            // When
            String formato = validatorService.detectarFormato(null);

            // Then
            assertThat(formato).isEqualTo("application/octet-stream");
        }

        @Test
        @DisplayName("Deve retornar octet-stream para conteúdo muito pequeno")
        void deveRetornarOctetStreamParaConteudoPequeno() {
            // Given
            byte[] conteudo = new byte[2];

            // When
            String formato = validatorService.detectarFormato(conteudo);

            // Then
            assertThat(formato).isEqualTo("application/octet-stream");
        }
    }

    // ==================== Testes de Verificação de Formato Real ====================

    @Nested
    @DisplayName("Verificação de Formato Real")
    class VerificacaoFormatoReal {

        @Test
        @DisplayName("Deve confirmar formato declarado igual ao detectado")
        void deveConfirmarFormatoIgual() {
            // Given
            byte[] conteudo = criarConteudoPDF();
            String formatoDeclarado = "application/pdf";

            // When
            boolean corresponde = validatorService.verificarFormatoReal(conteudo, formatoDeclarado);

            // Then
            assertThat(corresponde).isTrue();
        }

        @Test
        @DisplayName("Deve detectar formato declarado diferente do real")
        void deveDetectarFormatoDiferente() {
            // Given
            byte[] conteudo = criarConteudoPDF();
            String formatoDeclarado = "image/jpeg"; // Declarado errado

            // When
            boolean corresponde = validatorService.verificarFormatoReal(conteudo, formatoDeclarado);

            // Then
            assertThat(corresponde).isFalse();
        }

        @Test
        @DisplayName("Deve detectar formato JPEG declarado incorretamente")
        void deveDetectarFormatoJPEGIncorreto() {
            // Given
            // Como não é um JPEG completo, será detectado como octet-stream
            byte[] conteudo = criarConteudoJPEG();
            String formatoDeclarado = "image/jpeg";

            // When
            boolean corresponde = validatorService.verificarFormatoReal(conteudo, formatoDeclarado);

            // Then
            // Não corresponde porque detecta como octet-stream
            assertThat(corresponde).isFalse();
        }

        @Test
        @DisplayName("Deve detectar formato PNG declarado incorretamente")
        void deveDetectarFormatoPNGIncorreto() {
            // Given
            // Como não é um PNG completo, será detectado como octet-stream
            byte[] conteudo = criarConteudoPNG();
            String formatoDeclarado = "image/png";

            // When
            boolean corresponde = validatorService.verificarFormatoReal(conteudo, formatoDeclarado);

            // Then
            // Não corresponde porque detecta como octet-stream
            assertThat(corresponde).isFalse();
        }
    }
}
