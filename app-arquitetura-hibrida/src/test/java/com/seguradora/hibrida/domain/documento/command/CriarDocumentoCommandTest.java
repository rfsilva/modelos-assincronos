package com.seguradora.hibrida.domain.documento.command;

import com.seguradora.hibrida.domain.documento.model.TipoDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários para {@link CriarDocumentoCommand}.
 *
 * <p>Verifica:
 * <ul>
 *   <li>Criação usando builder</li>
 *   <li>Validações de campos obrigatórios</li>
 *   <li>Validações de campos vazios</li>
 *   <li>Métodos auxiliares (getTamanho, getTamanhoFormatado)</li>
 *   <li>toString (exclusão de conteúdo binário)</li>
 * </ul>
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("CriarDocumentoCommand - Testes Unitários")
class CriarDocumentoCommandTest {

    private byte[] conteudoValido;

    @BeforeEach
    void setUp() {
        conteudoValido = "Conteúdo do documento de teste".getBytes();
    }

    @Nested
    @DisplayName("Criação e Validação")
    class CriacaoValidacao {

        @Test
        @DisplayName("Deve criar command com todos os campos válidos usando builder")
        void deveCriarCommandComTodosCamposValidosUsandoBuilder() {
            // Arrange & Act
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("boletim_ocorrencia.pdf")
                    .tipo(TipoDocumento.BOLETIM_OCORRENCIA)
                    .conteudo(conteudoValido)
                    .formato("application/pdf")
                    .sinistroId("SIN-123")
                    .operadorId("OPR-456")
                    .operadorNome("João Silva")
                    .build();

            // Assert
            assertThat(command).isNotNull();
            assertThat(command.getDocumentoId()).isEqualTo("DOC-001");
            assertThat(command.getNome()).isEqualTo("boletim_ocorrencia.pdf");
            assertThat(command.getTipo()).isEqualTo(TipoDocumento.BOLETIM_OCORRENCIA);
            assertThat(command.getConteudo()).isEqualTo(conteudoValido);
            assertThat(command.getFormato()).isEqualTo("application/pdf");
            assertThat(command.getSinistroId()).isEqualTo("SIN-123");
            assertThat(command.getOperadorId()).isEqualTo("OPR-456");
            assertThat(command.getOperadorNome()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("Deve validar command com todos os campos válidos sem lançar exceção")
        void deveValidarCommandComTodosCamposValidosSemLancarExcecao() {
            // Arrange
            CriarDocumentoCommand command = criarCommandValido();

            // Act & Assert
            command.validar();
        }

        @Test
        @DisplayName("Deve lançar exceção quando documentoId é nulo")
        void deveLancarExcecaoQuandoDocumentoIdENulo() {
            // Arrange
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId(null)
                    .nome("arquivo.pdf")
                    .tipo(TipoDocumento.FOTO_DANOS)
                    .conteudo(conteudoValido)
                    .formato("application/pdf")
                    .sinistroId("SIN-123")
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Documento ID não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção quando nome é nulo")
        void deveLancarExcecaoQuandoNomeENulo() {
            // Arrange
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome(null)
                    .tipo(TipoDocumento.FOTO_DANOS)
                    .conteudo(conteudoValido)
                    .formato("application/pdf")
                    .sinistroId("SIN-123")
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Nome não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção quando tipo é nulo")
        void deveLancarExcecaoQuandoTipoENulo() {
            // Arrange
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("arquivo.pdf")
                    .tipo(null)
                    .conteudo(conteudoValido)
                    .formato("application/pdf")
                    .sinistroId("SIN-123")
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Tipo não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção quando conteúdo é nulo")
        void deveLancarExcecaoQuandoConteudoENulo() {
            // Arrange
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("arquivo.pdf")
                    .tipo(TipoDocumento.FOTO_DANOS)
                    .conteudo(null)
                    .formato("application/pdf")
                    .sinistroId("SIN-123")
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Conteúdo não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção quando formato é nulo")
        void deveLancarExcecaoQuandoFormatoENulo() {
            // Arrange
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("arquivo.pdf")
                    .tipo(TipoDocumento.FOTO_DANOS)
                    .conteudo(conteudoValido)
                    .formato(null)
                    .sinistroId("SIN-123")
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Formato não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção quando sinistroId é nulo")
        void deveLancarExcecaoQuandoSinistroIdENulo() {
            // Arrange
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("arquivo.pdf")
                    .tipo(TipoDocumento.FOTO_DANOS)
                    .conteudo(conteudoValido)
                    .formato("application/pdf")
                    .sinistroId(null)
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Sinistro ID não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção quando operadorId é nulo")
        void deveLancarExcecaoQuandoOperadorIdENulo() {
            // Arrange
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("arquivo.pdf")
                    .tipo(TipoDocumento.FOTO_DANOS)
                    .conteudo(conteudoValido)
                    .formato("application/pdf")
                    .sinistroId("SIN-123")
                    .operadorId(null)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Operador ID não pode ser nulo");
        }
    }

    @Nested
    @DisplayName("Validações de Campos Vazios")
    class ValidacoesCamposVazios {

        @Test
        @DisplayName("Deve lançar exceção quando nome é vazio")
        void deveLancarExcecaoQuandoNomeEVazio() {
            // Arrange
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("")
                    .tipo(TipoDocumento.FOTO_DANOS)
                    .conteudo(conteudoValido)
                    .formato("application/pdf")
                    .sinistroId("SIN-123")
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Nome não pode ser vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção quando nome contém apenas espaços")
        void deveLancarExcecaoQuandoNomeContemApenasEspacos() {
            // Arrange
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("   ")
                    .tipo(TipoDocumento.FOTO_DANOS)
                    .conteudo(conteudoValido)
                    .formato("application/pdf")
                    .sinistroId("SIN-123")
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Nome não pode ser vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção quando conteúdo é array vazio")
        void deveLancarExcecaoQuandoConteudoEArrayVazio() {
            // Arrange
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("arquivo.pdf")
                    .tipo(TipoDocumento.FOTO_DANOS)
                    .conteudo(new byte[0])
                    .formato("application/pdf")
                    .sinistroId("SIN-123")
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Conteúdo não pode ser vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção quando formato é vazio")
        void deveLancarExcecaoQuandoFormatoEVazio() {
            // Arrange
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("arquivo.pdf")
                    .tipo(TipoDocumento.FOTO_DANOS)
                    .conteudo(conteudoValido)
                    .formato("")
                    .sinistroId("SIN-123")
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Formato não pode ser vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção quando formato contém apenas espaços")
        void deveLancarExcecaoQuandoFormatoContemApenasEspacos() {
            // Arrange
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("arquivo.pdf")
                    .tipo(TipoDocumento.FOTO_DANOS)
                    .conteudo(conteudoValido)
                    .formato("   ")
                    .sinistroId("SIN-123")
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Formato não pode ser vazio");
        }
    }

    @Nested
    @DisplayName("Métodos Auxiliares")
    class MetodosAuxiliares {

        @Test
        @DisplayName("Deve retornar tamanho correto do conteúdo")
        void deveRetornarTamanhoCorretoDoConteudo() {
            // Arrange
            byte[] conteudo = new byte[1024]; // 1 KB
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("arquivo.pdf")
                    .tipo(TipoDocumento.FOTO_DANOS)
                    .conteudo(conteudo)
                    .formato("application/pdf")
                    .sinistroId("SIN-123")
                    .operadorId("OPR-456")
                    .build();

            // Act
            long tamanho = command.getTamanho();

            // Assert
            assertThat(tamanho).isEqualTo(1024);
        }

        @Test
        @DisplayName("Deve retornar zero quando conteúdo é nulo")
        void deveRetornarZeroQuandoConteudoENulo() {
            // Arrange
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("arquivo.pdf")
                    .tipo(TipoDocumento.FOTO_DANOS)
                    .conteudo(null)
                    .formato("application/pdf")
                    .sinistroId("SIN-123")
                    .operadorId("OPR-456")
                    .build();

            // Act
            long tamanho = command.getTamanho();

            // Assert
            assertThat(tamanho).isZero();
        }

        @Test
        @DisplayName("Deve retornar tamanho formatado corretamente em MB")
        void deveRetornarTamanhoFormatadoCorretamenteEmMB() {
            // Arrange
            byte[] conteudo = new byte[2 * 1024 * 1024]; // 2 MB
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("arquivo.pdf")
                    .tipo(TipoDocumento.FOTO_DANOS)
                    .conteudo(conteudo)
                    .formato("application/pdf")
                    .sinistroId("SIN-123")
                    .operadorId("OPR-456")
                    .build();

            // Act
            String tamanhoFormatado = command.getTamanhoFormatado();

            // Assert
            assertThat(tamanhoFormatado).matches("2[.,]00 MB");
        }

        @Test
        @DisplayName("Deve formatar tamanho com duas casas decimais")
        void deveFormatarTamanhoComDuasCasasDecimais() {
            // Arrange
            byte[] conteudo = new byte[1536 * 1024]; // 1.5 MB
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("arquivo.pdf")
                    .tipo(TipoDocumento.FOTO_DANOS)
                    .conteudo(conteudo)
                    .formato("application/pdf")
                    .sinistroId("SIN-123")
                    .operadorId("OPR-456")
                    .build();

            // Act
            String tamanhoFormatado = command.getTamanhoFormatado();

            // Assert
            assertThat(tamanhoFormatado).matches("1[.,]50 MB");
        }

        @Test
        @DisplayName("Deve retornar toString sem expor conteúdo binário")
        void deveRetornarToStringSemExporConteudoBinario() {
            // Arrange
            CriarDocumentoCommand command = criarCommandValido();

            // Act
            String toString = command.toString();

            // Assert
            assertThat(toString)
                    .contains("CriarDocumentoCommand")
                    .contains("DOC-001")
                    .contains("boletim.pdf")
                    .contains("BOLETIM_OCORRENCIA")
                    .contains("application/pdf")
                    .contains("SIN-123")
                    .contains("OPR-456")
                    .doesNotContain("conteudo=");
        }

        @Test
        @DisplayName("Deve aceitar todos os tipos de documento")
        void deveAceitarTodosTiposDeDocumento() {
            // Arrange & Act & Assert
            for (TipoDocumento tipo : TipoDocumento.values()) {
                CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                        .documentoId("DOC-" + tipo.name())
                        .nome("arquivo_" + tipo.name() + ".pdf")
                        .tipo(tipo)
                        .conteudo(conteudoValido)
                        .formato("application/pdf")
                        .sinistroId("SIN-123")
                        .operadorId("OPR-456")
                        .build();

                assertThat(command.getTipo()).isEqualTo(tipo);
                command.validar(); // Não deve lançar exceção
            }
        }

        @Test
        @DisplayName("Deve aceitar operadorNome nulo")
        void deveAceitarOperadorNomeNulo() {
            // Arrange
            CriarDocumentoCommand command = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("arquivo.pdf")
                    .tipo(TipoDocumento.FOTO_DANOS)
                    .conteudo(conteudoValido)
                    .formato("application/pdf")
                    .sinistroId("SIN-123")
                    .operadorId("OPR-456")
                    .operadorNome(null)
                    .build();

            // Act & Assert
            command.validar();
            assertThat(command.getOperadorNome()).isNull();
        }
    }

    /**
     * Helper method para criar um command válido para testes.
     *
     * @return Command válido
     */
    private CriarDocumentoCommand criarCommandValido() {
        return CriarDocumentoCommand.builder()
                .documentoId("DOC-001")
                .nome("boletim.pdf")
                .tipo(TipoDocumento.BOLETIM_OCORRENCIA)
                .conteudo(conteudoValido)
                .formato("application/pdf")
                .sinistroId("SIN-123")
                .operadorId("OPR-456")
                .operadorNome("João Silva")
                .build();
    }
}
