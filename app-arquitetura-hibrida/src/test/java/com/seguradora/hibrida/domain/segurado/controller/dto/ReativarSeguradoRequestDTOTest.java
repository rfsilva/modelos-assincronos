package com.seguradora.hibrida.domain.segurado.controller.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link ReativarSeguradoRequestDTO}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("ReativarSeguradoRequestDTO - Testes Unitários")
class ReativarSeguradoRequestDTOTest {

    @Test
    @DisplayName("Deve criar DTO com motivo")
    void shouldCreateDTOWithReason() {
        // Given
        String motivo = "Regularização de pendências";

        // When
        ReativarSeguradoRequestDTO dto = new ReativarSeguradoRequestDTO(motivo);

        // Then
        assertThat(dto.getMotivo()).isEqualTo(motivo);
    }

    @Test
    @DisplayName("Deve criar com construtor vazio")
    void shouldCreateWithNoArgsConstructor() {
        // When
        ReativarSeguradoRequestDTO dto = new ReativarSeguradoRequestDTO();

        // Then
        assertThat(dto).isNotNull();
    }

    @Test
    @DisplayName("Deve permitir setter")
    void shouldAllowSetter() {
        // Given
        ReativarSeguradoRequestDTO dto = new ReativarSeguradoRequestDTO();

        // When
        dto.setMotivo("Quitação de débitos");

        // Then
        assertThat(dto.getMotivo()).isEqualTo("Quitação de débitos");
    }

    @Test
    @DisplayName("Deve aceitar diferentes motivos")
    void shouldAcceptDifferentReasons() {
        assertThatNoException().isThrownBy(() ->
            new ReativarSeguradoRequestDTO("Regularização de pendências")
        );

        assertThatNoException().isThrownBy(() ->
            new ReativarSeguradoRequestDTO("Solicitação do cliente")
        );

        assertThatNoException().isThrownBy(() ->
            new ReativarSeguradoRequestDTO("Esclarecimento de fraude")
        );
    }
}
