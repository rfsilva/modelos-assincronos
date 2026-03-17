package com.seguradora.hibrida.domain.veiculo.controller.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link CommandResponseDTO}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("CommandResponseDTO - Testes Unitários")
class CommandResponseDTOTest {

    @Nested
    @DisplayName("Testes de Criação")
    class CriacaoTests {

        @Test
        @DisplayName("Deve criar DTO com todos os campos")
        void deveCriarDtoComTodosCampos() {
            // Arrange
            Instant timestamp = Instant.now();
            Map<String, Object> details = Map.of("key", "value");

            // Act
            CommandResponseDTO dto = new CommandResponseDTO(
                "AGG-123",
                "Operação realizada com sucesso",
                5L,
                timestamp,
                details
            );

            // Assert
            assertThat(dto).isNotNull();
            assertThat(dto.aggregateId()).isEqualTo("AGG-123");
            assertThat(dto.message()).isEqualTo("Operação realizada com sucesso");
            assertThat(dto.version()).isEqualTo(5L);
            assertThat(dto.timestamp()).isEqualTo(timestamp);
            assertThat(dto.details()).isEqualTo(details);
        }

        @Test
        @DisplayName("Deve criar DTO sem versão")
        void deveCriarDtoSemVersao() {
            // Act
            CommandResponseDTO dto = new CommandResponseDTO(
                "AGG-123",
                "Sucesso",
                null,
                Instant.now(),
                null
            );

            // Assert
            assertThat(dto.version()).isNull();
        }

        @Test
        @DisplayName("Deve criar DTO sem detalhes")
        void deveCriarDtoSemDetalhes() {
            // Act
            CommandResponseDTO dto = new CommandResponseDTO(
                "AGG-123",
                "Sucesso",
                1L,
                Instant.now(),
                null
            );

            // Assert
            assertThat(dto.details()).isNull();
            assertThat(dto.hasDetails()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Verificação de Sucesso")
    class VerificacaoSucessoTests {

        @Test
        @DisplayName("Deve identificar sucesso quando tem aggregateId e mensagem positiva")
        void deveIdentificarSucessoComAggregateIdEMensagemPositiva() {
            // Arrange
            CommandResponseDTO dto = new CommandResponseDTO(
                "AGG-123",
                "Operação concluída com sucesso",
                1L,
                Instant.now(),
                null
            );

            // Act & Assert
            assertThat(dto.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Deve identificar falha quando aggregateId é null")
        void deveIdentificarFalhaQuandoAggregateIdNull() {
            // Arrange
            CommandResponseDTO dto = new CommandResponseDTO(
                null,
                "Operação realizada",
                null,
                Instant.now(),
                null
            );

            // Act & Assert
            assertThat(dto.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Deve identificar falha quando mensagem contém 'erro'")
        void deveIdentificarFalhaQuandoMensagemContemErro() {
            // Arrange
            CommandResponseDTO dto = new CommandResponseDTO(
                "AGG-123",
                "Erro ao processar comando",
                null,
                Instant.now(),
                null
            );

            // Act & Assert
            assertThat(dto.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Deve identificar falha quando mensagem contém 'falha'")
        void deveIdentificarFalhaQuandoMensagemContemFalha() {
            // Arrange
            CommandResponseDTO dto = new CommandResponseDTO(
                "AGG-123",
                "Falha na execução do comando",
                null,
                Instant.now(),
                null
            );

            // Act & Assert
            assertThat(dto.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Verificação deve ser case-insensitive")
        void verificacaoDeveSerCaseInsensitive() {
            assertThat(criarDtoComMensagem("ERRO NA OPERAÇÃO").isSuccess()).isFalse();
            assertThat(criarDtoComMensagem("erro na operação").isSuccess()).isFalse();
            assertThat(criarDtoComMensagem("Erro na operação").isSuccess()).isFalse();

            assertThat(criarDtoComMensagem("FALHA TOTAL").isSuccess()).isFalse();
            assertThat(criarDtoComMensagem("falha total").isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Verificação de Detalhes")
    class VerificacaoDetalhesTests {

        @Test
        @DisplayName("Deve identificar que tem detalhes quando map não é nulo e não vazio")
        void deveIdentificarQueTemDetalhesQuandoMapNaoNuloENaoVazio() {
            // Arrange
            Map<String, Object> details = Map.of("campo", "valor");
            CommandResponseDTO dto = criarDtoComDetalhes(details);

            // Act & Assert
            assertThat(dto.hasDetails()).isTrue();
        }

        @Test
        @DisplayName("Não deve ter detalhes quando map é null")
        void naoDeveTermDetalhesQuandoMapNull() {
            // Arrange
            CommandResponseDTO dto = criarDtoComDetalhes(null);

            // Act & Assert
            assertThat(dto.hasDetails()).isFalse();
        }

        @Test
        @DisplayName("Não deve ter detalhes quando map está vazio")
        void naoDeveTermDetalhesQuandoMapVazio() {
            // Arrange
            CommandResponseDTO dto = criarDtoComDetalhes(Map.of());

            // Act & Assert
            assertThat(dto.hasDetails()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Factory Methods - Success")
    class FactoryMethodsSuccessTests {

        @Test
        @DisplayName("Deve criar resposta de sucesso simples")
        void deveCriarRespostaDeSucessoSimples() {
            // Act
            CommandResponseDTO dto = CommandResponseDTO.success(
                "AGG-123",
                "Comando executado",
                5L
            );

            // Assert
            assertThat(dto.aggregateId()).isEqualTo("AGG-123");
            assertThat(dto.message()).isEqualTo("Comando executado");
            assertThat(dto.version()).isEqualTo(5L);
            assertThat(dto.timestamp()).isNotNull();
            assertThat(dto.details()).isNull();
            assertThat(dto.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Deve criar resposta de sucesso com detalhes")
        void deveCriarRespostaDeSucessoComDetalhes() {
            // Arrange
            Map<String, Object> details = Map.of(
                "campo1", "valor1",
                "campo2", 123
            );

            // Act
            CommandResponseDTO dto = CommandResponseDTO.success(
                "AGG-456",
                "Operação concluída",
                3L,
                details
            );

            // Assert
            assertThat(dto.aggregateId()).isEqualTo("AGG-456");
            assertThat(dto.message()).isEqualTo("Operação concluída");
            assertThat(dto.version()).isEqualTo(3L);
            assertThat(dto.timestamp()).isNotNull();
            assertThat(dto.details()).isEqualTo(details);
            assertThat(dto.hasDetails()).isTrue();
            assertThat(dto.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Timestamp deve ser recente em resposta de sucesso")
        void timestampDeveSerRecenteEmRespostaDeSucesso() {
            // Arrange
            Instant antes = Instant.now();

            // Act
            CommandResponseDTO dto = CommandResponseDTO.success("AGG-1", "OK", 1L);
            Instant depois = Instant.now();

            // Assert
            assertThat(dto.timestamp()).isBetween(antes, depois);
        }
    }

    @Nested
    @DisplayName("Testes de Factory Methods - Error")
    class FactoryMethodsErrorTests {

        @Test
        @DisplayName("Deve criar resposta de erro simples")
        void deveCriarRespostaDeErroSimples() {
            // Act
            CommandResponseDTO dto = CommandResponseDTO.error("Erro ao processar comando");

            // Assert
            assertThat(dto.aggregateId()).isNull();
            assertThat(dto.message()).isEqualTo("Erro ao processar comando");
            assertThat(dto.version()).isNull();
            assertThat(dto.timestamp()).isNotNull();
            assertThat(dto.details()).isNull();
            assertThat(dto.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Deve criar resposta de erro com detalhes")
        void deveCriarRespostaDeErroComDetalhes() {
            // Arrange
            Map<String, Object> details = Map.of(
                "exception", "IllegalArgumentException",
                "cause", "Invalid input"
            );

            // Act
            CommandResponseDTO dto = CommandResponseDTO.error(
                "Falha na validação",
                details
            );

            // Assert
            assertThat(dto.aggregateId()).isNull();
            assertThat(dto.message()).isEqualTo("Falha na validação");
            assertThat(dto.version()).isNull();
            assertThat(dto.timestamp()).isNotNull();
            assertThat(dto.details()).isEqualTo(details);
            assertThat(dto.hasDetails()).isTrue();
            assertThat(dto.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Timestamp deve ser recente em resposta de erro")
        void timestampDeveSerRecenteEmRespostaDeErro() {
            // Arrange
            Instant antes = Instant.now();

            // Act
            CommandResponseDTO dto = CommandResponseDTO.error("Erro");
            Instant depois = Instant.now();

            // Assert
            assertThat(dto.timestamp()).isBetween(antes, depois);
        }
    }

    @Nested
    @DisplayName("Testes de Records")
    class RecordsTests {

        @Test
        @DisplayName("Deve ter equals correto")
        void deveTermEqualsCorreto() {
            // Arrange
            Instant timestamp = Instant.parse("2024-06-15T10:00:00Z");
            CommandResponseDTO dto1 = new CommandResponseDTO(
                "AGG-1", "Mensagem", 1L, timestamp, null
            );
            CommandResponseDTO dto2 = new CommandResponseDTO(
                "AGG-1", "Mensagem", 1L, timestamp, null
            );

            // Act & Assert
            assertThat(dto1).isEqualTo(dto2);
            assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        }

        @Test
        @DisplayName("Deve ter toString legível")
        void deveTermToStringLegivel() {
            // Arrange
            CommandResponseDTO dto = new CommandResponseDTO(
                "AGG-789",
                "Operação bem-sucedida",
                10L,
                Instant.now(),
                Map.of("key", "value")
            );

            // Act
            String toString = dto.toString();

            // Assert
            assertThat(toString).contains("AGG-789");
            assertThat(toString).contains("Operação bem-sucedida");
            assertThat(toString).contains("10");
        }
    }

    @Nested
    @DisplayName("Testes de Cenários Completos")
    class CenariosCompletosTests {

        @Test
        @DisplayName("Deve representar resposta de criação bem-sucedida")
        void deveRepresentarRespostaDeCriacaoBemSucedida() {
            // Act
            CommandResponseDTO dto = CommandResponseDTO.success(
                "VEI-001",
                "Veículo criado com sucesso",
                1L,
                Map.of("placa", "ABC1234", "marca", "Honda")
            );

            // Assert
            assertThat(dto.isSuccess()).isTrue();
            assertThat(dto.hasDetails()).isTrue();
            assertThat(dto.version()).isEqualTo(1L);
            assertThat(dto.details().get("placa")).isEqualTo("ABC1234");
        }

        @Test
        @DisplayName("Deve representar resposta de atualização bem-sucedida")
        void deveRepresentarRespostaDeAtualizacaoBemSucedida() {
            // Act
            CommandResponseDTO dto = CommandResponseDTO.success(
                "VEI-002",
                "Veículo atualizado com sucesso",
                8L,
                Map.of("novaVersao", 8L)
            );

            // Assert
            assertThat(dto.isSuccess()).isTrue();
            assertThat(dto.version()).isEqualTo(8L);
            assertThat(dto.details().get("novaVersao")).isEqualTo(8L);
        }

        @Test
        @DisplayName("Deve representar resposta de erro de validação")
        void deveRepresentarRespostaDeErroDeValidacao() {
            // Act
            CommandResponseDTO dto = CommandResponseDTO.error(
                "Erro de validação: Placa inválida",
                Map.of(
                    "field", "placa",
                    "value", "INVALID",
                    "error", "Formato incorreto"
                )
            );

            // Assert
            assertThat(dto.isSuccess()).isFalse();
            assertThat(dto.aggregateId()).isNull();
            assertThat(dto.version()).isNull();
            assertThat(dto.hasDetails()).isTrue();
            assertThat(dto.message()).contains("Erro de validação");
        }

        @Test
        @DisplayName("Deve representar resposta de erro de concorrência")
        void deveRepresentarRespostaDeErroDeConcorrencia() {
            // Act
            CommandResponseDTO dto = CommandResponseDTO.error(
                "Falha: Versão do aggregate desatualizada",
                Map.of(
                    "expectedVersion", 5L,
                    "actualVersion", 7L,
                    "errorType", "ConcurrencyException"
                )
            );

            // Assert
            assertThat(dto.isSuccess()).isFalse();
            assertThat(dto.message()).contains("Falha");
            assertThat(dto.details().get("errorType")).isEqualTo("ConcurrencyException");
        }
    }

    // === Métodos auxiliares ===

    private CommandResponseDTO criarDtoComMensagem(String mensagem) {
        return new CommandResponseDTO("AGG-TEST", mensagem, 1L, Instant.now(), null);
    }

    private CommandResponseDTO criarDtoComDetalhes(Map<String, Object> details) {
        return new CommandResponseDTO("AGG-TEST", "Teste", 1L, Instant.now(), details);
    }
}
