package com.seguradora.hibrida.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do CommandResult")
class CommandResultTest {

    @Test
    @DisplayName("Deve criar resultado de sucesso simples")
    void shouldCreateSimpleSuccessResult() {
        // Act
        CommandResult result = CommandResult.success();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isFailure()).isFalse();
        assertThat(result.getData()).isNull();
        assertThat(result.getErrorMessage()).isNull();
        assertThat(result.getExecutedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar resultado de sucesso com dados")
    void shouldCreateSuccessResultWithData() {
        // Arrange
        UUID aggregateId = UUID.randomUUID();

        // Act
        CommandResult result = CommandResult.success(aggregateId);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(aggregateId);
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Deve criar resultado de sucesso com dados e metadados")
    void shouldCreateSuccessResultWithDataAndMetadata() {
        // Arrange
        String data = "test-id";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("version", 1L);
        metadata.put("timestamp", Instant.now());

        // Act
        CommandResult result = CommandResult.success(data, metadata);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(data);
        assertThat(result.getMetadata()).isEqualTo(metadata);
        assertThat(result.getMetadata()).containsKey("version");
        assertThat(result.getMetadata()).containsKey("timestamp");
    }

    @Test
    @DisplayName("Deve criar resultado de falha com mensagem")
    void shouldCreateFailureResultWithMessage() {
        // Arrange
        String errorMessage = "CPF já cadastrado no sistema";

        // Act
        CommandResult result = CommandResult.failure(errorMessage);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(result.getErrorCode()).isNull();
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("Deve criar resultado de falha com mensagem e código")
    void shouldCreateFailureResultWithMessageAndCode() {
        // Arrange
        String errorMessage = "Recurso não encontrado";
        String errorCode = "NOT_FOUND";

        // Act
        CommandResult result = CommandResult.failure(errorMessage, errorCode);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(result.getErrorCode()).isEqualTo(errorCode);
    }

    @Test
    @DisplayName("Deve criar resultado de falha a partir de exceção")
    void shouldCreateFailureResultFromException() {
        // Arrange
        Exception exception = new IllegalArgumentException("Valor inválido");

        // Act
        CommandResult result = CommandResult.failure(exception);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Valor inválido");
        assertThat(result.getErrorCode()).isEqualTo("IllegalArgumentException");
    }

    @Test
    @DisplayName("Deve criar resultado de falha com exceção e metadados")
    void shouldCreateFailureResultWithExceptionAndMetadata() {
        // Arrange
        Exception exception = new RuntimeException("Erro de processamento");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("attemptCount", 3);
        metadata.put("lastAttemptTime", Instant.now());

        // Act
        CommandResult result = CommandResult.failure(exception, metadata);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Erro de processamento");
        assertThat(result.getErrorCode()).isEqualTo("RuntimeException");
        assertThat(result.getMetadata()).isEqualTo(metadata);
    }

    @Test
    @DisplayName("Deve adicionar correlation ID via method chaining")
    void shouldAddCorrelationIdViaMethodChaining() {
        // Arrange
        UUID correlationId = UUID.randomUUID();

        // Act
        CommandResult result = CommandResult.success()
                .withCorrelationId(correlationId);

        // Assert
        assertThat(result.getCorrelationId()).isEqualTo(correlationId);
    }

    @Test
    @DisplayName("Deve adicionar tempo de execução via method chaining")
    void shouldAddExecutionTimeViaMethodChaining() {
        // Arrange
        Long executionTime = 150L;

        // Act
        CommandResult result = CommandResult.success()
                .withExecutionTime(executionTime);

        // Assert
        assertThat(result.getExecutionTimeMs()).isEqualTo(executionTime);
    }

    @Test
    @DisplayName("Deve adicionar metadados via method chaining")
    void shouldAddMetadataViaMethodChaining() {
        // Act
        CommandResult result = CommandResult.success()
                .withMetadata("key1", "value1")
                .withMetadata("key2", 42)
                .withMetadata("key3", true);

        // Assert
        assertThat(result.getMetadata()).isNotNull();
        assertThat(result.getMetadata()).containsEntry("key1", "value1");
        assertThat(result.getMetadata()).containsEntry("key2", 42);
        assertThat(result.getMetadata()).containsEntry("key3", true);
    }

    @Test
    @DisplayName("Deve permitir encadeamento completo de métodos")
    void shouldAllowCompleteMethodChaining() {
        // Arrange
        UUID correlationId = UUID.randomUUID();
        Long executionTime = 200L;

        // Act
        CommandResult result = CommandResult.success("aggregate-id")
                .withCorrelationId(correlationId)
                .withExecutionTime(executionTime)
                .withMetadata("version", 1L)
                .withMetadata("source", "API");

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo("aggregate-id");
        assertThat(result.getCorrelationId()).isEqualTo(correlationId);
        assertThat(result.getExecutionTimeMs()).isEqualTo(executionTime);
        assertThat(result.getMetadata()).containsEntry("version", 1L);
        assertThat(result.getMetadata()).containsEntry("source", "API");
    }

    @Test
    @DisplayName("Deve criar metadados quando adicionar primeira entrada")
    void shouldCreateMetadataWhenAddingFirstEntry() {
        // Act
        CommandResult result = CommandResult.success();
        assertThat(result.getMetadata()).isNull();

        result.withMetadata("first", "value");

        // Assert
        assertThat(result.getMetadata()).isNotNull();
        assertThat(result.getMetadata()).containsEntry("first", "value");
    }

    @Test
    @DisplayName("Deve incluir timestamp automaticamente")
    void shouldIncludeTimestampAutomatically() {
        // Arrange
        Instant before = Instant.now();

        // Act
        CommandResult result = CommandResult.success();

        // Assert
        Instant after = Instant.now();
        assertThat(result.getExecutedAt()).isNotNull();
        assertThat(result.getExecutedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("Deve funcionar com builder")
    void shouldWorkWithBuilder() {
        // Arrange
        UUID correlationId = UUID.randomUUID();
        Map<String, Object> metadata = Map.of("key", "value");

        // Act
        CommandResult result = CommandResult.builder()
                .success(true)
                .data("test-data")
                .correlationId(correlationId)
                .executionTimeMs(100L)
                .metadata(metadata)
                .build();

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo("test-data");
        assertThat(result.getCorrelationId()).isEqualTo(correlationId);
        assertThat(result.getExecutionTimeMs()).isEqualTo(100L);
        assertThat(result.getMetadata()).isEqualTo(metadata);
        assertThat(result.getExecutedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve funcionar com construtor vazio")
    void shouldWorkWithNoArgsConstructor() {
        // Act
        CommandResult result = new CommandResult();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        // executedAt pode ou não ser null dependendo de como o Lombok configura o @Builder.Default
    }

    @Test
    @DisplayName("Deve permitir configuração via setters")
    void shouldAllowConfigurationViaSetters() {
        // Arrange
        CommandResult result = new CommandResult();
        UUID correlationId = UUID.randomUUID();
        Instant timestamp = Instant.now();

        // Act
        result.setSuccess(true);
        result.setData("data");
        result.setErrorMessage("error");
        result.setErrorCode("CODE");
        result.setExecutedAt(timestamp);
        result.setExecutionTimeMs(150L);
        result.setCorrelationId(correlationId);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");
        result.setMetadata(metadata);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo("data");
        assertThat(result.getErrorMessage()).isEqualTo("error");
        assertThat(result.getErrorCode()).isEqualTo("CODE");
        assertThat(result.getExecutedAt()).isEqualTo(timestamp);
        assertThat(result.getExecutionTimeMs()).isEqualTo(150L);
        assertThat(result.getCorrelationId()).isEqualTo(correlationId);
        assertThat(result.getMetadata()).isEqualTo(metadata);
    }

    @Test
    @DisplayName("isFailure deve retornar negação de isSuccess")
    void isFailureShouldReturnNegationOfIsSuccess() {
        // Arrange
        CommandResult successResult = CommandResult.success();
        CommandResult failureResult = CommandResult.failure("error");

        // Assert
        assertThat(successResult.isSuccess()).isTrue();
        assertThat(successResult.isFailure()).isFalse();

        assertThat(failureResult.isSuccess()).isFalse();
        assertThat(failureResult.isFailure()).isTrue();
    }

    @Test
    @DisplayName("Deve suportar tipos complexos como dados")
    void shouldSupportComplexTypesAsData() {
        // Arrange
        Map<String, Object> complexData = new HashMap<>();
        complexData.put("id", UUID.randomUUID());
        complexData.put("version", 5L);
        complexData.put("items", java.util.Arrays.asList("item1", "item2"));

        // Act
        CommandResult result = CommandResult.success(complexData);

        // Assert
        assertThat(result.getData()).isEqualTo(complexData);

        @SuppressWarnings("unchecked")
        Map<String, Object> retrievedData = (Map<String, Object>) result.getData();
        assertThat(retrievedData).containsKey("id");
        assertThat(retrievedData).containsKey("version");
        assertThat(retrievedData).containsKey("items");
    }

    @Test
    @DisplayName("Deve permitir metadados nulos")
    void shouldAllowNullMetadata() {
        // Act
        CommandResult result = CommandResult.success("data", null);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo("data");
        assertThat(result.getMetadata()).isNull();
    }

    @Test
    @DisplayName("Deve manter referência ao adicionar múltiplos metadados")
    void shouldMaintainReferenceWhenAddingMultipleMetadata() {
        // Act
        CommandResult result = CommandResult.success();
        result.withMetadata("first", "value1");
        result.withMetadata("second", "value2");
        result.withMetadata("third", "value3");

        // Assert
        assertThat(result.getMetadata()).hasSize(3);
        assertThat(result.getMetadata()).containsKeys("first", "second", "third");
    }

    @Test
    @DisplayName("Deve retornar mesma instância no method chaining")
    void shouldReturnSameInstanceInMethodChaining() {
        // Act
        CommandResult original = CommandResult.success();
        CommandResult withCorrelation = original.withCorrelationId(UUID.randomUUID());
        CommandResult withExecutionTime = withCorrelation.withExecutionTime(100L);
        CommandResult withMetadata = withExecutionTime.withMetadata("key", "value");

        // Assert
        assertThat(withCorrelation).isSameAs(original);
        assertThat(withExecutionTime).isSameAs(original);
        assertThat(withMetadata).isSameAs(original);
    }
}
