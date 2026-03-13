package com.seguradora.hibrida.domain.segurado.controller.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link CommandResponseDTO}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("CommandResponseDTO - Testes Unitários")
class CommandResponseDTOTest {

    @Test
    @DisplayName("Deve criar resposta de sucesso")
    void shouldCreateSuccessResponse() {
        // Given
        String message = "Segurado criado com sucesso";
        Object data = "SEG-001";

        // When
        CommandResponseDTO response = new CommandResponseDTO(true, message, data);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getData()).isEqualTo(data);
    }

    @Test
    @DisplayName("Deve criar resposta de erro")
    void shouldCreateErrorResponse() {
        // Given
        String message = "Erro ao criar segurado";

        // When
        CommandResponseDTO response = new CommandResponseDTO(false, message, null);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getData()).isNull();
    }

    @Test
    @DisplayName("Deve criar com construtor vazio")
    void shouldCreateWithNoArgsConstructor() {
        // When
        CommandResponseDTO response = new CommandResponseDTO();

        // Then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Deve permitir setters")
    void shouldAllowSetters() {
        // Given
        CommandResponseDTO response = new CommandResponseDTO();

        // When
        response.setSuccess(true);
        response.setMessage("Operação realizada");
        response.setData("12345");

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Operação realizada");
        assertThat(response.getData()).isEqualTo("12345");
    }

    @Test
    @DisplayName("Deve aceitar diferentes tipos de dados")
    void shouldAcceptDifferentDataTypes() {
        // String
        CommandResponseDTO response1 = new CommandResponseDTO(true, "OK", "text");
        assertThat(response1.getData()).isInstanceOf(String.class);

        // Number
        CommandResponseDTO response2 = new CommandResponseDTO(true, "OK", 123);
        assertThat(response2.getData()).isInstanceOf(Integer.class);

        // Map
        Map<String, String> map = new HashMap<>();
        map.put("id", "SEG-001");
        CommandResponseDTO response3 = new CommandResponseDTO(true, "OK", map);
        assertThat(response3.getData()).isInstanceOf(Map.class);
    }
}
