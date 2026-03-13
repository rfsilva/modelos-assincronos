package com.seguradora.hibrida.domain.segurado.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link ReativarSeguradoCommand}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("ReativarSeguradoCommand - Testes Unitários")
class ReativarSeguradoCommandTest {

    @Test
    @DisplayName("Deve criar command com dados válidos")
    void shouldCreateCommandWithValidData() {
        // Given
        String seguradoId = "SEG-001";
        String motivo = "Regularização de pendências";

        // When
        ReativarSeguradoCommand command = ReativarSeguradoCommand.builder()
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
        ReativarSeguradoCommand command = ReativarSeguradoCommand.builder()
            .seguradoId("SEG-001")
            .motivo("Regularização de pendências")
            .build();

        // Then
        assertThat(command.getCommandId()).isNotNull();
    }

    @Test
    @DisplayName("Deve gerar timestamp automaticamente")
    void shouldGenerateTimestampAutomatically() {
        // When
        ReativarSeguradoCommand command = ReativarSeguradoCommand.builder()
            .seguradoId("SEG-001")
            .motivo("Regularização de pendências")
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
        ReativarSeguradoCommand command = ReativarSeguradoCommand.builder()
            .seguradoId("SEG-001")
            .motivo("Regularização de pendências")
            .correlationId(correlationId)
            .build();

        // Then
        assertThat(command.getCorrelationId()).isEqualTo(correlationId);
    }

    @Test
    @DisplayName("Deve permitir configurar userId")
    void shouldAllowSettingUserId() {
        // Given
        String userId = "user-456";

        // When
        ReativarSeguradoCommand command = ReativarSeguradoCommand.builder()
            .seguradoId("SEG-001")
            .motivo("Regularização de pendências")
            .userId(userId)
            .build();

        // Then
        assertThat(command.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Deve aceitar diferentes motivos")
    void shouldAcceptDifferentReasons() {
        assertThatNoException().isThrownBy(() ->
            ReativarSeguradoCommand.builder()
                .seguradoId("SEG-001")
                .motivo("Regularização de pendências")
                .build()
        );

        assertThatNoException().isThrownBy(() ->
            ReativarSeguradoCommand.builder()
                .seguradoId("SEG-001")
                .motivo("Solicitação do cliente")
                .build()
        );

        assertThatNoException().isThrownBy(() ->
            ReativarSeguradoCommand.builder()
                .seguradoId("SEG-001")
                .motivo("Quitação de débitos")
                .build()
        );
    }
}
