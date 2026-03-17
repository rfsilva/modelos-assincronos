package com.seguradora.hibrida.domain.documento.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários para {@link ValidarDocumentoCommand}.
 *
 * <p>Verifica:
 * <ul>
 *   <li>Criação usando builder</li>
 *   <li>Validações de campos obrigatórios</li>
 *   <li>Validações de campos vazios</li>
 *   <li>Método possuiObservacoes()</li>
 *   <li>toString e campos opcionais</li>
 * </ul>
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("ValidarDocumentoCommand - Testes Unitários")
class ValidarDocumentoCommandTest {

    @Nested
    @DisplayName("Criação e Validação")
    class CriacaoValidacao {

        @Test
        @DisplayName("Deve criar command com todos os campos válidos usando builder")
        void deveCriarCommandComTodosCamposValidosUsandoBuilder() {
            // Arrange & Act
            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .validadorId("VAL-123")
                    .validadorNome("Ana Silva")
                    .observacoes("Documento validado conforme especificações")
                    .build();

            // Assert
            assertThat(command).isNotNull();
            assertThat(command.getDocumentoId()).isEqualTo("DOC-001");
            assertThat(command.getValidadorId()).isEqualTo("VAL-123");
            assertThat(command.getValidadorNome()).isEqualTo("Ana Silva");
            assertThat(command.getObservacoes()).isEqualTo("Documento validado conforme especificações");
        }

        @Test
        @DisplayName("Deve criar command sem observações")
        void deveCriarCommandSemObservacoes() {
            // Arrange & Act
            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-002")
                    .validadorId("VAL-456")
                    .validadorNome("Pedro Santos")
                    .build();

            // Assert
            assertThat(command).isNotNull();
            assertThat(command.getDocumentoId()).isEqualTo("DOC-002");
            assertThat(command.getValidadorId()).isEqualTo("VAL-456");
            assertThat(command.getValidadorNome()).isEqualTo("Pedro Santos");
            assertThat(command.getObservacoes()).isNull();
        }

        @Test
        @DisplayName("Deve validar command com todos os campos válidos sem lançar exceção")
        void deveValidarCommandComTodosCamposValidosSemLancarExcecao() {
            // Arrange
            ValidarDocumentoCommand command = criarCommandValido();

            // Act & Assert
            command.validar();
        }

        @Test
        @DisplayName("Deve lançar exceção quando documentoId é nulo")
        void deveLancarExcecaoQuandoDocumentoIdENulo() {
            // Arrange
            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId(null)
                    .validadorId("VAL-123")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Documento ID não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção quando validadorId é nulo")
        void deveLancarExcecaoQuandoValidadorIdENulo() {
            // Arrange
            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .validadorId(null)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Validador ID não pode ser nulo");
        }
    }

    @Nested
    @DisplayName("Validações de Campos Vazios")
    class ValidacoesCamposVazios {

        @Test
        @DisplayName("Deve lançar exceção quando documentoId é vazio")
        void deveLancarExcecaoQuandoDocumentoIdEVazio() {
            // Arrange
            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId("")
                    .validadorId("VAL-123")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Documento ID não pode ser vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção quando documentoId contém apenas espaços")
        void deveLancarExcecaoQuandoDocumentoIdContemApenasEspacos() {
            // Arrange
            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId("   ")
                    .validadorId("VAL-123")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Documento ID não pode ser vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção quando validadorId é vazio")
        void deveLancarExcecaoQuandoValidadorIdEVazio() {
            // Arrange
            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .validadorId("")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Validador ID não pode ser vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção quando validadorId contém apenas espaços")
        void deveLancarExcecaoQuandoValidadorIdContemApenasEspacos() {
            // Arrange
            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .validadorId("   ")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Validador ID não pode ser vazio");
        }
    }

    @Nested
    @DisplayName("Regras de Negócio")
    class RegrasNegocio {

        @Test
        @DisplayName("Deve retornar true quando possui observações preenchidas")
        void deveRetornarTrueQuandoPossuiObservacoesPreenchidas() {
            // Arrange
            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .validadorId("VAL-123")
                    .observacoes("Observações importantes")
                    .build();

            // Act
            boolean possuiObservacoes = command.possuiObservacoes();

            // Assert
            assertThat(possuiObservacoes).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando observações é nulo")
        void deveRetornarFalseQuandoObservacoesENulo() {
            // Arrange
            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .validadorId("VAL-123")
                    .observacoes(null)
                    .build();

            // Act
            boolean possuiObservacoes = command.possuiObservacoes();

            // Assert
            assertThat(possuiObservacoes).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando observações é vazio")
        void deveRetornarFalseQuandoObservacoesEVazio() {
            // Arrange
            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .validadorId("VAL-123")
                    .observacoes("")
                    .build();

            // Act
            boolean possuiObservacoes = command.possuiObservacoes();

            // Assert
            assertThat(possuiObservacoes).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando observações contém apenas espaços")
        void deveRetornarFalseQuandoObservacoesContemApenasEspacos() {
            // Arrange
            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .validadorId("VAL-123")
                    .observacoes("   ")
                    .build();

            // Act
            boolean possuiObservacoes = command.possuiObservacoes();

            // Assert
            assertThat(possuiObservacoes).isFalse();
        }

        @Test
        @DisplayName("Deve aceitar validação sem nome do validador")
        void deveAceitarValidacaoSemNomeDoValidador() {
            // Arrange
            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .validadorId("VAL-123")
                    .validadorNome(null)
                    .build();

            // Act & Assert
            command.validar();
            assertThat(command.getValidadorNome()).isNull();
        }

        @Test
        @DisplayName("Deve preservar dados do validador para auditoria")
        void devePreservarDadosDoValidadorParaAuditoria() {
            // Arrange
            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .validadorId("VAL-789")
                    .validadorNome("Roberto Lima")
                    .observacoes("Validado após verificação detalhada")
                    .build();

            // Act
            command.validar();

            // Assert
            assertThat(command.getValidadorId()).isEqualTo("VAL-789");
            assertThat(command.getValidadorNome()).isEqualTo("Roberto Lima");
            assertThat(command.getObservacoes()).isEqualTo("Validado após verificação detalhada");
        }

        @Test
        @DisplayName("Deve aceitar observações longas")
        void deveAceitarObservacoesLongas() {
            // Arrange
            String observacoesLongas = "Documento analisado minuciosamente. " +
                    "Todas as informações estão corretas e de acordo com os requisitos. " +
                    "Aprovado para prosseguimento do processo.";

            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .validadorId("VAL-123")
                    .observacoes(observacoesLongas)
                    .build();

            // Act & Assert
            command.validar();
            assertThat(command.getObservacoes()).isEqualTo(observacoesLongas);
            assertThat(command.possuiObservacoes()).isTrue();
        }

        @Test
        @DisplayName("Deve aceitar observações com caracteres especiais")
        void deveAceitarObservacoesComCaracteresEspeciais() {
            // Arrange
            String observacoesEspeciais = "Validado: conforme § 2º (art. 5º) - aprovado!";

            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .validadorId("VAL-123")
                    .observacoes(observacoesEspeciais)
                    .build();

            // Act & Assert
            command.validar();
            assertThat(command.getObservacoes()).isEqualTo(observacoesEspeciais);
        }
    }

    @Nested
    @DisplayName("Métodos Auxiliares")
    class MetodosAuxiliares {

        @Test
        @DisplayName("Deve retornar toString formatado corretamente")
        void deveRetornarToStringFormatadoCorretamente() {
            // Arrange
            ValidarDocumentoCommand command = criarCommandValido();

            // Act
            String toString = command.toString();

            // Assert
            assertThat(toString)
                    .contains("ValidarDocumentoCommand")
                    .contains("DOC-001")
                    .contains("VAL-123")
                    .contains("Ana Silva");
        }

        @Test
        @DisplayName("Deve incluir observações no toString quando presente")
        void deveIncluirObservacoesNoToStringQuandoPresente() {
            // Arrange
            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .validadorId("VAL-123")
                    .validadorNome("Ana Silva")
                    .observacoes("Teste de observações")
                    .build();

            // Act
            String toString = command.toString();

            // Assert
            assertThat(toString).contains("Teste de observações");
        }

        @Test
        @DisplayName("Deve aceitar diferentes formatos de IDs")
        void deveAceitarDiferentesFormatosDeIDs() {
            // Arrange & Act & Assert
            String[] idsDocumento = {"DOC-001", "DOC_2024_001", "doc.123", "DOCUMENTO-XYZ"};
            String[] idsValidador = {"VAL-001", "VAL_2024_001", "val.123", "VALIDADOR-XYZ"};

            for (int i = 0; i < idsDocumento.length; i++) {
                ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                        .documentoId(idsDocumento[i])
                        .validadorId(idsValidador[i])
                        .build();

                command.validar();
                assertThat(command.getDocumentoId()).isEqualTo(idsDocumento[i]);
                assertThat(command.getValidadorId()).isEqualTo(idsValidador[i]);
            }
        }
    }

    /**
     * Helper method para criar um command válido para testes.
     *
     * @return Command válido
     */
    private ValidarDocumentoCommand criarCommandValido() {
        return ValidarDocumentoCommand.builder()
                .documentoId("DOC-001")
                .validadorId("VAL-123")
                .validadorNome("Ana Silva")
                .observacoes("Documento validado conforme padrões")
                .build();
    }
}
