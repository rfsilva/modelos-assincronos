package com.seguradora.hibrida.domain.documento.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários para {@link AtualizarDocumentoCommand}.
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
@DisplayName("AtualizarDocumentoCommand - Testes Unitários")
class AtualizarDocumentoCommandTest {

    private byte[] novoConteudoValido;

    @BeforeEach
    void setUp() {
        novoConteudoValido = "Novo conteúdo do documento atualizado".getBytes();
    }

    @Nested
    @DisplayName("Criação e Validação")
    class CriacaoValidacao {

        @Test
        @DisplayName("Deve criar command com todos os campos válidos usando builder")
        void deveCriarCommandComTodosCamposValidosUsandoBuilder() {
            // Arrange & Act
            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(novoConteudoValido)
                    .motivo("Correção de informações")
                    .operadorId("OPR-456")
                    .operadorNome("Maria Santos")
                    .build();

            // Assert
            assertThat(command).isNotNull();
            assertThat(command.getDocumentoId()).isEqualTo("DOC-001");
            assertThat(command.getNovoConteudo()).isEqualTo(novoConteudoValido);
            assertThat(command.getMotivo()).isEqualTo("Correção de informações");
            assertThat(command.getOperadorId()).isEqualTo("OPR-456");
            assertThat(command.getOperadorNome()).isEqualTo("Maria Santos");
        }

        @Test
        @DisplayName("Deve validar command com todos os campos válidos sem lançar exceção")
        void deveValidarCommandComTodosCamposValidosSemLancarExcecao() {
            // Arrange
            AtualizarDocumentoCommand command = criarCommandValido();

            // Act & Assert
            command.validar();
        }

        @Test
        @DisplayName("Deve lançar exceção quando documentoId é nulo")
        void deveLancarExcecaoQuandoDocumentoIdENulo() {
            // Arrange
            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId(null)
                    .novoConteudo(novoConteudoValido)
                    .motivo("Atualização necessária")
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Documento ID não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção quando novoConteudo é nulo")
        void deveLancarExcecaoQuandoNovoConteudoENulo() {
            // Arrange
            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(null)
                    .motivo("Atualização necessária")
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Novo conteúdo não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção quando motivo é nulo")
        void deveLancarExcecaoQuandoMotivoENulo() {
            // Arrange
            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(novoConteudoValido)
                    .motivo(null)
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Motivo não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção quando operadorId é nulo")
        void deveLancarExcecaoQuandoOperadorIdENulo() {
            // Arrange
            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(novoConteudoValido)
                    .motivo("Atualização necessária")
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
        @DisplayName("Deve lançar exceção quando documentoId é vazio")
        void deveLancarExcecaoQuandoDocumentoIdEVazio() {
            // Arrange
            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId("")
                    .novoConteudo(novoConteudoValido)
                    .motivo("Atualização necessária")
                    .operadorId("OPR-456")
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
            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId("   ")
                    .novoConteudo(novoConteudoValido)
                    .motivo("Atualização necessária")
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Documento ID não pode ser vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção quando novoConteudo é array vazio")
        void deveLancarExcecaoQuandoNovoConteudoEArrayVazio() {
            // Arrange
            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(new byte[0])
                    .motivo("Atualização necessária")
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Novo conteúdo não pode ser vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção quando motivo é vazio")
        void deveLancarExcecaoQuandoMotivoEVazio() {
            // Arrange
            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(novoConteudoValido)
                    .motivo("")
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Motivo não pode ser vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção quando motivo contém apenas espaços")
        void deveLancarExcecaoQuandoMotivoContemApenasEspacos() {
            // Arrange
            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(novoConteudoValido)
                    .motivo("   ")
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Motivo não pode ser vazio");
        }
    }

    @Nested
    @DisplayName("Métodos Auxiliares")
    class MetodosAuxiliares {

        @Test
        @DisplayName("Deve retornar tamanho correto do novo conteúdo")
        void deveRetornarTamanhoCorretoDoNovoConteudo() {
            // Arrange
            byte[] conteudo = new byte[2048]; // 2 KB
            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(conteudo)
                    .motivo("Atualização necessária")
                    .operadorId("OPR-456")
                    .build();

            // Act
            long tamanho = command.getTamanho();

            // Assert
            assertThat(tamanho).isEqualTo(2048);
        }

        @Test
        @DisplayName("Deve retornar zero quando novoConteudo é nulo")
        void deveRetornarZeroQuandoNovoConteudoENulo() {
            // Arrange
            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(null)
                    .motivo("Atualização necessária")
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
            byte[] conteudo = new byte[3 * 1024 * 1024]; // 3 MB
            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(conteudo)
                    .motivo("Atualização necessária")
                    .operadorId("OPR-456")
                    .build();

            // Act
            String tamanhoFormatado = command.getTamanhoFormatado();

            // Assert
            assertThat(tamanhoFormatado).matches("3[.,]00 MB");
        }

        @Test
        @DisplayName("Deve formatar tamanho com duas casas decimais")
        void deveFormatarTamanhoComDuasCasasDecimais() {
            // Arrange
            byte[] conteudo = new byte[2560 * 1024]; // 2.5 MB
            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(conteudo)
                    .motivo("Atualização necessária")
                    .operadorId("OPR-456")
                    .build();

            // Act
            String tamanhoFormatado = command.getTamanhoFormatado();

            // Assert
            assertThat(tamanhoFormatado).matches("2[.,]50 MB");
        }

        @Test
        @DisplayName("Deve retornar toString sem expor conteúdo binário")
        void deveRetornarToStringSemExporConteudoBinario() {
            // Arrange
            AtualizarDocumentoCommand command = criarCommandValido();

            // Act
            String toString = command.toString();

            // Assert
            assertThat(toString)
                    .contains("AtualizarDocumentoCommand")
                    .contains("DOC-001")
                    .contains("Correção de dados")
                    .contains("OPR-456")
                    .doesNotContain("novoConteudo=");
        }

        @Test
        @DisplayName("Deve aceitar operadorNome nulo")
        void deveAceitarOperadorNomeNulo() {
            // Arrange
            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(novoConteudoValido)
                    .motivo("Atualização necessária")
                    .operadorId("OPR-456")
                    .operadorNome(null)
                    .build();

            // Act & Assert
            command.validar();
            assertThat(command.getOperadorNome()).isNull();
        }

        @Test
        @DisplayName("Deve aceitar motivos descritivos longos")
        void deveAceitarMotivosDescritivosLongos() {
            // Arrange
            String motivoLongo = "Atualização necessária devido a identificação de " +
                    "inconsistências nos dados originais que requerem correção imediata " +
                    "para garantir a conformidade com os requisitos regulatórios";

            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(novoConteudoValido)
                    .motivo(motivoLongo)
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            command.validar();
            assertThat(command.getMotivo()).isEqualTo(motivoLongo);
        }
    }

    @Nested
    @DisplayName("Regras de Negócio")
    class RegrasNegocio {

        @Test
        @DisplayName("Deve aceitar diferentes tamanhos de conteúdo")
        void deveAceitarDiferentesTamanhosDeConteudo() {
            // Arrange & Act & Assert
            int[] tamanhos = {1024, 10240, 102400, 1024000}; // 1KB, 10KB, 100KB, 1MB

            for (int tamanho : tamanhos) {
                byte[] conteudo = new byte[tamanho];
                AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                        .documentoId("DOC-" + tamanho)
                        .novoConteudo(conteudo)
                        .motivo("Teste com " + tamanho + " bytes")
                        .operadorId("OPR-456")
                        .build();

                command.validar();
                assertThat(command.getTamanho()).isEqualTo(tamanho);
            }
        }

        @Test
        @DisplayName("Deve preservar dados do operador para auditoria")
        void devePreservarDadosDoOperadorParaAuditoria() {
            // Arrange
            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(novoConteudoValido)
                    .motivo("Correção necessária")
                    .operadorId("OPR-789")
                    .operadorNome("Carlos Oliveira")
                    .build();

            // Act
            command.validar();

            // Assert
            assertThat(command.getOperadorId()).isEqualTo("OPR-789");
            assertThat(command.getOperadorNome()).isEqualTo("Carlos Oliveira");
        }

        @Test
        @DisplayName("Deve aceitar motivos com caracteres especiais")
        void deveAceitarMotivosComCaracteresEspeciais() {
            // Arrange
            String motivoComEspeciais = "Correção: atualização de dados (v2.0) - seção §3.1";

            AtualizarDocumentoCommand command = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(novoConteudoValido)
                    .motivo(motivoComEspeciais)
                    .operadorId("OPR-456")
                    .build();

            // Act & Assert
            command.validar();
            assertThat(command.getMotivo()).isEqualTo(motivoComEspeciais);
        }
    }

    /**
     * Helper method para criar um command válido para testes.
     *
     * @return Command válido
     */
    private AtualizarDocumentoCommand criarCommandValido() {
        return AtualizarDocumentoCommand.builder()
                .documentoId("DOC-001")
                .novoConteudo(novoConteudoValido)
                .motivo("Correção de dados")
                .operadorId("OPR-456")
                .operadorNome("Maria Santos")
                .build();
    }
}
