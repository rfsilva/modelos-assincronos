package com.seguradora.hibrida.domain.segurado.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link DesativarSeguradoCommand}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("DesativarSeguradoCommand - Testes Unitários")
class DesativarSeguradoCommandTest {

    @Test
    @DisplayName("Deve criar command com dados válidos")
    void shouldCreateCommandWithValidData() {
        // Given
        String seguradoId = "SEG-001";
        String motivo = "Solicitação do cliente";

        // When
        DesativarSeguradoCommand command = DesativarSeguradoCommand.builder()
            .seguradoId(seguradoId)
            .motivo(motivo)
            .build();

        // Then
        assertThat(command.getSeguradoId()).isEqualTo(seguradoId);
        assertThat(command.getMotivo()).isEqualTo(motivo);
    }

    @Test
    @DisplayName("Deve gerar commandId automaticamente")
    void shouldGenerateCommandIdAutomatically() {
        // When
        DesativarSeguradoCommand command = DesativarSeguradoCommand.builder()
            .seguradoId("SEG-001")
            .motivo("Solicitação do cliente")
            .build();

        // Then
        assertThat(command.getCommandId()).isNotNull();
    }

    @Test
    @DisplayName("Deve gerar timestamp automaticamente")
    void shouldGenerateTimestampAutomatically() {
        // When
        DesativarSeguradoCommand command = DesativarSeguradoCommand.builder()
            .seguradoId("SEG-001")
            .motivo("Solicitação do cliente")
            .build();

        // Then
        assertThat(command.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve permitir configurar correlationId")
    void shouldAllowSettingCorrelationId() {
        // Given
        UUID correlationId = UUID.randomUUID();

        // When
        DesativarSeguradoCommand command = DesativarSeguradoCommand.builder()
            .seguradoId("SEG-001")
            .motivo("Solicitação do cliente")
            .correlationId(correlationId)
            .build();

        // Then
        assertThat(command.getCorrelationId()).isEqualTo(correlationId);
    }

    @Test
    @DisplayName("Deve permitir configurar userId")
    void shouldAllowSettingUserId() {
        // Given
        String userId = "user-123";

        // When
        DesativarSeguradoCommand command = DesativarSeguradoCommand.builder()
            .seguradoId("SEG-001")
            .motivo("Solicitação do cliente")
            .userId(userId)
            .build();

        // Then
        assertThat(command.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Deve aceitar diferentes motivos")
    void shouldAcceptDifferentReasons() {
        assertThatNoException().isThrownBy(() ->
            DesativarSeguradoCommand.builder()
                .seguradoId("SEG-001")
                .motivo("Solicitação do cliente")
                .build()
        );

        assertThatNoException().isThrownBy(() ->
            DesativarSeguradoCommand.builder()
                .seguradoId("SEG-001")
                .motivo("Inatividade prolongada")
                .build()
        );

        assertThatNoException().isThrownBy(() ->
            DesativarSeguradoCommand.builder()
                .seguradoId("SEG-001")
                .motivo("Inadimplência")
                .build()
        );
    }
}
