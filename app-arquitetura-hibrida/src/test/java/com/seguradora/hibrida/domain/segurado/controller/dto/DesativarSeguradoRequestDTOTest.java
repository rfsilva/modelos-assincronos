package com.seguradora.hibrida.domain.segurado.controller.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link DesativarSeguradoRequestDTO}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("DesativarSeguradoRequestDTO - Testes Unitários")
class DesativarSeguradoRequestDTOTest {

    @Test
    @DisplayName("Deve criar DTO com motivo")
    void shouldCreateDTOWithReason() {
        // Given
        String motivo = "Solicitação do cliente";

        // When
        DesativarSeguradoRequestDTO dto = new DesativarSeguradoRequestDTO(motivo);

        // Then
        assertThat(dto.getMotivo()).isEqualTo(motivo);
    }

    @Test
    @DisplayName("Deve criar com construtor vazio")
    void shouldCreateWithNoArgsConstructor() {
        // When
        DesativarSeguradoRequestDTO dto = new DesativarSeguradoRequestDTO();

        // Then
        assertThat(dto).isNotNull();
    }

    @Test
    @DisplayName("Deve permitir setter")
    void shouldAllowSetter() {
        // Given
        DesativarSeguradoRequestDTO dto = new DesativarSeguradoRequestDTO();

        // When
        dto.setMotivo("Inatividade prolongada");

        // Then
        assertThat(dto.getMotivo()).isEqualTo("Inatividade prolongada");
    }

    @Test
    @DisplayName("Deve aceitar diferentes motivos")
    void shouldAcceptDifferentReasons() {
        assertThatNoException().isThrownBy(() ->
            new DesativarSeguradoRequestDTO("Solicitação do cliente")
        );

        assertThatNoException().isThrownBy(() ->
            new DesativarSeguradoRequestDTO("Inadimplência")
        );

        assertThatNoException().isThrownBy(() ->
            new DesativarSeguradoRequestDTO("Fraude detectada")
        );
    }
}
