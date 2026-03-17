package com.seguradora.hibrida.domain.documento.event;

import com.seguradora.hibrida.domain.documento.model.TipoDocumento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link DocumentoCriadoEvent}.
 *
 * <p>Valida a criação e comportamento do evento de documento criado,
 * incluindo:
 * <ul>
 *   <li>Construção com todos os campos obrigatórios</li>
 *   <li>Métodos auxiliares de formatação e validação</li>
 *   <li>Propriedades herdadas do DomainEvent</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("DocumentoCriadoEvent - Testes Unitários")
class DocumentoCriadoEventTest {

    /**
     * Cria um evento de documento criado válido para testes.
     *
     * @return Evento criado com dados de exemplo
     */
    private DocumentoCriadoEvent criarEventoPadrao() {
        return new DocumentoCriadoEvent(
                "doc-123",
                "boletim_ocorrencia.pdf",
                TipoDocumento.BOLETIM_OCORRENCIA,
                5242880L, // 5 MB
                "abc123def456",
                "application/pdf",
                "/storage/docs/doc-123.pdf",
                "sin-456",
                "op-789"
        );
    }

    /**
     * Cria um evento usando o builder do Lombok.
     *
     * @return Evento criado via builder
     */
    private DocumentoCriadoEvent criarEventoComBuilder() {
        return DocumentoCriadoEvent.builder()
                .documentoId("doc-999")
                .nome("laudo_pericial.pdf")
                .tipo(TipoDocumento.LAUDO_PERICIAL)
                .tamanho(15728640L) // 15 MB
                .hash("hash999")
                .formato("application/pdf")
                .conteudoPath("/storage/docs/doc-999.pdf")
                .sinistroId("sin-999")
                .operadorId("op-999")
                .build();
    }

    @Nested
    @DisplayName("Criação e Validação")
    class CriacaoValidacao {

        @Test
        @DisplayName("Deve criar evento com construtor completo")
        void deveCriarEventoComConstrutorCompleto() {
            // Arrange & Act
            DocumentoCriadoEvent evento = new DocumentoCriadoEvent(
                    "doc-001",
                    "foto_danos.jpg",
                    TipoDocumento.FOTO_DANOS,
                    2097152L, // 2 MB
                    "hashABC",
                    "image/jpeg",
                    "/storage/images/foto_danos.jpg",
                    "sin-001",
                    "op-001"
            );

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.getDocumentoId()).isEqualTo("doc-001");
            assertThat(evento.getNome()).isEqualTo("foto_danos.jpg");
            assertThat(evento.getTipo()).isEqualTo(TipoDocumento.FOTO_DANOS);
            assertThat(evento.getTamanho()).isEqualTo(2097152L);
            assertThat(evento.getHash()).isEqualTo("hashABC");
            assertThat(evento.getFormato()).isEqualTo("image/jpeg");
            assertThat(evento.getConteudoPath()).isEqualTo("/storage/images/foto_danos.jpg");
            assertThat(evento.getSinistroId()).isEqualTo("sin-001");
            assertThat(evento.getOperadorId()).isEqualTo("op-001");
        }

        @Test
        @DisplayName("Deve criar evento com builder do Lombok")
        void deveCriarEventoComBuilder() {
            // Arrange & Act
            DocumentoCriadoEvent evento = DocumentoCriadoEvent.builder()
                    .documentoId("doc-002")
                    .nome("orcamento.pdf")
                    .tipo(TipoDocumento.ORCAMENTO)
                    .tamanho(3145728L) // 3 MB
                    .hash("hashXYZ")
                    .formato("application/pdf")
                    .conteudoPath("/storage/docs/orcamento.pdf")
                    .sinistroId("sin-002")
                    .operadorId("op-002")
                    .build();

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.getDocumentoId()).isEqualTo("doc-002");
            assertThat(evento.getNome()).isEqualTo("orcamento.pdf");
            assertThat(evento.getTipo()).isEqualTo(TipoDocumento.ORCAMENTO);
            assertThat(evento.getTamanho()).isEqualTo(3145728L);
        }

        @Test
        @DisplayName("Deve criar evento sem chamar super() explicitamente")
        void deveCriarEventoComNoArgsConstructor() {
            // Arrange & Act
            DocumentoCriadoEvent evento = new DocumentoCriadoEvent();

            // Assert - Verifica que o objeto foi criado
            assertThat(evento).isNotNull();
        }

        @Test
        @DisplayName("Deve preservar todos os campos após criação")
        void devePreservarTodosOsCampos() {
            // Arrange
            String documentoId = "doc-preserve-001";
            String nome = "teste_preservacao.pdf";
            TipoDocumento tipo = TipoDocumento.CNH;
            long tamanho = 1048576L; // 1 MB
            String hash = "hashPreserve123";
            String formato = "application/pdf";
            String conteudoPath = "/storage/preserve/teste.pdf";
            String sinistroId = "sin-preserve-001";
            String operadorId = "op-preserve-001";

            // Act
            DocumentoCriadoEvent evento = new DocumentoCriadoEvent(
                    documentoId, nome, tipo, tamanho, hash, formato,
                    conteudoPath, sinistroId, operadorId
            );

            // Assert - Verifica que todos os campos foram preservados
            assertThat(evento.getDocumentoId()).isEqualTo(documentoId);
            assertThat(evento.getNome()).isEqualTo(nome);
            assertThat(evento.getTipo()).isEqualTo(tipo);
            assertThat(evento.getTamanho()).isEqualTo(tamanho);
            assertThat(evento.getHash()).isEqualTo(hash);
            assertThat(evento.getFormato()).isEqualTo(formato);
            assertThat(evento.getConteudoPath()).isEqualTo(conteudoPath);
            assertThat(evento.getSinistroId()).isEqualTo(sinistroId);
            assertThat(evento.getOperadorId()).isEqualTo(operadorId);
        }
    }

    @Nested
    @DisplayName("Propriedades do Evento")
    class PropriedadesEvento {

        @Test
        @DisplayName("Deve ter eventType correto")
        void deveTerEventTypeCorreto() {
            // Arrange
            DocumentoCriadoEvent evento = criarEventoPadrao();

            // Act
            String eventType = evento.getEventType();

            // Assert
            assertThat(eventType).isEqualTo("DocumentoCriadoEvent");
        }

        @Test
        @DisplayName("Deve permitir acesso a todos os getters")
        void devePermitirAcessoATodosOsGetters() {
            // Arrange
            DocumentoCriadoEvent evento = criarEventoPadrao();

            // Act & Assert
            assertThat(evento.getDocumentoId()).isNotNull();
            assertThat(evento.getNome()).isNotNull();
            assertThat(evento.getTipo()).isNotNull();
            assertThat(evento.getTamanho()).isPositive();
            assertThat(evento.getHash()).isNotNull();
            assertThat(evento.getFormato()).isNotNull();
            assertThat(evento.getConteudoPath()).isNotNull();
            assertThat(evento.getSinistroId()).isNotNull();
            assertThat(evento.getOperadorId()).isNotNull();
        }

        @Test
        @DisplayName("Deve ter toString implementado")
        void deveTerToStringImplementado() {
            // Arrange
            DocumentoCriadoEvent evento = criarEventoPadrao();

            // Act
            String toString = evento.toString();

            // Assert
            assertThat(toString).isNotNull();
            assertThat(toString).contains("DocumentoCriadoEvent");
        }
    }

    @Nested
    @DisplayName("Métodos Auxiliares")
    class MetodosAuxiliares {

        @Test
        @DisplayName("Deve formatar tamanho em MB corretamente")
        void deveFormatarTamanhoEmMBCorretamente() {
            // Arrange
            DocumentoCriadoEvent evento = criarEventoPadrao(); // 5 MB

            // Act
            String tamanhoFormatado = evento.getTamanhoFormatado();

            // Assert
            assertThat(tamanhoFormatado).isEqualTo("5,00 MB");
        }

        @Test
        @DisplayName("Deve formatar tamanho pequeno em MB")
        void deveFormatarTamanhoPequenoEmMB() {
            // Arrange
            DocumentoCriadoEvent evento = new DocumentoCriadoEvent(
                    "doc-small",
                    "small.txt",
                    TipoDocumento.COMPROVANTE_RESIDENCIA,
                    512000L, // 0.488 MB
                    "hashSmall",
                    "text/plain",
                    "/storage/small.txt",
                    "sin-001",
                    "op-001"
            );

            // Act
            String tamanhoFormatado = evento.getTamanhoFormatado();

            // Assert
            assertThat(tamanhoFormatado).isEqualTo("0,49 MB");
        }

        @Test
        @DisplayName("Deve formatar tamanho grande em MB")
        void deveFormatarTamanhoGrandeEmMB() {
            // Arrange
            DocumentoCriadoEvent evento = new DocumentoCriadoEvent(
                    "doc-large",
                    "large.pdf",
                    TipoDocumento.LAUDO_PERICIAL,
                    15728640L, // 15 MB
                    "hashLarge",
                    "application/pdf",
                    "/storage/large.pdf",
                    "sin-002",
                    "op-002"
            );

            // Act
            String tamanhoFormatado = evento.getTamanhoFormatado();

            // Assert
            assertThat(tamanhoFormatado).isEqualTo("15,00 MB");
        }

        @Test
        @DisplayName("Deve identificar documento grande corretamente")
        void deveIdentificarDocumentoGrandeCorretamente() {
            // Arrange
            DocumentoCriadoEvent eventoGrande = new DocumentoCriadoEvent(
                    "doc-big",
                    "big.pdf",
                    TipoDocumento.LAUDO_PERICIAL,
                    11534336L, // 11 MB (> 10 MB)
                    "hashBig",
                    "application/pdf",
                    "/storage/big.pdf",
                    "sin-003",
                    "op-003"
            );

            // Act
            boolean isGrande = eventoGrande.isDocumentoGrande();

            // Assert
            assertThat(isGrande).isTrue();
        }

        @Test
        @DisplayName("Deve identificar documento pequeno corretamente")
        void deveIdentificarDocumentoPequenoCorretamente() {
            // Arrange
            DocumentoCriadoEvent eventoPequeno = new DocumentoCriadoEvent(
                    "doc-small",
                    "small.pdf",
                    TipoDocumento.FOTO_DANOS,
                    5242880L, // 5 MB (< 10 MB)
                    "hashSmall",
                    "image/jpeg",
                    "/storage/small.jpg",
                    "sin-004",
                    "op-004"
            );

            // Act
            boolean isGrande = eventoPequeno.isDocumentoGrande();

            // Assert
            assertThat(isGrande).isFalse();
        }

        @Test
        @DisplayName("Deve identificar documento no limite de 10 MB")
        void deveIdentificarDocumentoNoLimite() {
            // Arrange
            DocumentoCriadoEvent eventoLimite = new DocumentoCriadoEvent(
                    "doc-limit",
                    "limit.pdf",
                    TipoDocumento.ORCAMENTO,
                    10485760L, // Exatamente 10 MB
                    "hashLimit",
                    "application/pdf",
                    "/storage/limit.pdf",
                    "sin-005",
                    "op-005"
            );

            // Act
            boolean isGrande = eventoLimite.isDocumentoGrande();

            // Assert
            assertThat(isGrande).isFalse(); // Exatamente 10 MB não é considerado grande
        }

        @Test
        @DisplayName("Deve identificar documento imediatamente acima do limite")
        void deveIdentificarDocumentoAcimaDoLimite() {
            // Arrange
            DocumentoCriadoEvent eventoAcimaLimite = new DocumentoCriadoEvent(
                    "doc-above",
                    "above.pdf",
                    TipoDocumento.LAUDO_PERICIAL,
                    10485761L, // 10 MB + 1 byte
                    "hashAbove",
                    "application/pdf",
                    "/storage/above.pdf",
                    "sin-006",
                    "op-006"
            );

            // Act
            boolean isGrande = eventoAcimaLimite.isDocumentoGrande();

            // Assert
            assertThat(isGrande).isTrue();
        }
    }

    @Nested
    @DisplayName("Cenários Específicos")
    class CenariosEspecificos {

        @Test
        @DisplayName("Deve criar evento para diferentes tipos de documento")
        void deveCriarEventoParaDiferentesTiposDeDocumento() {
            // Arrange & Act
            DocumentoCriadoEvent eventoBO = new DocumentoCriadoEvent(
                    "doc-bo", "bo.pdf", TipoDocumento.BOLETIM_OCORRENCIA,
                    5242880L, "hash1", "application/pdf", "/storage/bo.pdf",
                    "sin-1", "op-1"
            );

            DocumentoCriadoEvent eventoLaudo = new DocumentoCriadoEvent(
                    "doc-laudo", "laudo.pdf", TipoDocumento.LAUDO_PERICIAL,
                    15728640L, "hash2", "application/pdf", "/storage/laudo.pdf",
                    "sin-2", "op-2"
            );

            DocumentoCriadoEvent eventoFoto = new DocumentoCriadoEvent(
                    "doc-foto", "foto.jpg", TipoDocumento.FOTO_DANOS,
                    2097152L, "hash3", "image/jpeg", "/storage/foto.jpg",
                    "sin-3", "op-3"
            );

            // Assert
            assertThat(eventoBO.getTipo()).isEqualTo(TipoDocumento.BOLETIM_OCORRENCIA);
            assertThat(eventoLaudo.getTipo()).isEqualTo(TipoDocumento.LAUDO_PERICIAL);
            assertThat(eventoFoto.getTipo()).isEqualTo(TipoDocumento.FOTO_DANOS);
        }

        @Test
        @DisplayName("Deve criar evento com diferentes formatos de arquivo")
        void deveCriarEventoComDiferentesFormatos() {
            // Arrange & Act
            DocumentoCriadoEvent eventoPDF = new DocumentoCriadoEvent(
                    "doc-pdf", "doc.pdf", TipoDocumento.ORCAMENTO,
                    1048576L, "hashPDF", "application/pdf", "/storage/doc.pdf",
                    "sin-1", "op-1"
            );

            DocumentoCriadoEvent eventoJPEG = new DocumentoCriadoEvent(
                    "doc-jpeg", "doc.jpg", TipoDocumento.FOTO_DANOS,
                    2097152L, "hashJPEG", "image/jpeg", "/storage/doc.jpg",
                    "sin-2", "op-2"
            );

            DocumentoCriadoEvent eventoPNG = new DocumentoCriadoEvent(
                    "doc-png", "doc.png", TipoDocumento.CNH,
                    3145728L, "hashPNG", "image/png", "/storage/doc.png",
                    "sin-3", "op-3"
            );

            // Assert
            assertThat(eventoPDF.getFormato()).isEqualTo("application/pdf");
            assertThat(eventoJPEG.getFormato()).isEqualTo("image/jpeg");
            assertThat(eventoPNG.getFormato()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("Deve criar evento com hash único")
        void deveCriarEventoComHashUnico() {
            // Arrange & Act
            DocumentoCriadoEvent evento1 = new DocumentoCriadoEvent(
                    "doc-1", "file1.pdf", TipoDocumento.NOTA_FISCAL,
                    1048576L, "hashUnico123", "application/pdf", "/storage/file1.pdf",
                    "sin-1", "op-1"
            );

            DocumentoCriadoEvent evento2 = new DocumentoCriadoEvent(
                    "doc-2", "file2.pdf", TipoDocumento.NOTA_FISCAL,
                    1048576L, "hashUnico456", "application/pdf", "/storage/file2.pdf",
                    "sin-2", "op-2"
            );

            // Assert
            assertThat(evento1.getHash()).isNotEqualTo(evento2.getHash());
            assertThat(evento1.getHash()).isEqualTo("hashUnico123");
            assertThat(evento2.getHash()).isEqualTo("hashUnico456");
        }
    }
}
