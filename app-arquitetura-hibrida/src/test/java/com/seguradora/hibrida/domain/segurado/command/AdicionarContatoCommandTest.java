package com.seguradora.hibrida.domain.segurado.command;

import com.seguradora.hibrida.domain.segurado.model.TipoContato;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link AdicionarContatoCommand}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("AdicionarContatoCommand - Testes Unitários")
class AdicionarContatoCommandTest {

    @Test
    @DisplayName("Deve criar command com dados válidos")
    void shouldCreateCommandWithValidData() {
        // Given
        String seguradoId = "SEG-001";
        TipoContato tipo = TipoContato.EMAIL;
        String valor = "joao@example.com";
        boolean principal = true;
        String operadorId = "OP-001";
        Long versaoEsperada = 3L;

        // When
        AdicionarContatoCommand command = AdicionarContatoCommand.builder()
            .seguradoId(seguradoId)
            .tipo(tipo)
            .valor(valor)
            .principal(principal)
            .operadorId(operadorId)
            .versaoEsperada(versaoEsperada)
            .build();

        // Then
        assertThat(command.getSeguradoId()).isEqualTo(seguradoId);
        assertThat(command.getTipo()).isEqualTo(tipo);
        assertThat(command.getValor()).isEqualTo(valor);
        assertThat(command.isPrincipal()).isEqualTo(principal);
        assertThat(command.getOperadorId()).isEqualTo(operadorId);
        assertThat(command.getVersaoEsperada()).isEqualTo(versaoEsperada);
    }

    @Test
    @DisplayName("Deve criar command com principal false por padrão")
    void shouldCreateCommandWithPrincipalFalseByDefault() {
        // When
        AdicionarContatoCommand command = AdicionarContatoCommand.builder()
            .seguradoId("SEG-001")
            .tipo(TipoContato.EMAIL)
            .valor("joao@example.com")
            .operadorId("OP-001")
            .build();

        // Then
        assertThat(command.isPrincipal()).isFalse();
    }

    @Test
    @DisplayName("Deve gerar commandId automaticamente")
    void shouldGenerateCommandIdAutomatically() {
        // When
        AdicionarContatoCommand command = AdicionarContatoCommand.builder()
            .seguradoId("SEG-001")
            .tipo(TipoContato.EMAIL)
            .valor("joao@example.com")
            .operadorId("OP-001")
            .build();

        // Then
        assertThat(command.getCommandId()).isNotNull();
    }

    @Test
    @DisplayName("Deve gerar timestamp automaticamente")
    void shouldGenerateTimestampAutomatically() {
        // When
        AdicionarContatoCommand command = AdicionarContatoCommand.builder()
            .seguradoId("SEG-001")
            .tipo(TipoContato.EMAIL)
            .valor("joao@example.com")
            .operadorId("OP-001")
            .build();

        // Then
        assertThat(command.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve aceitar diferentes tipos de contato")
    void shouldAcceptDifferentContactTypes() {
        // When/Then
        assertThatNoException().isThrownBy(() ->
            AdicionarContatoCommand.builder()
                .seguradoId("SEG-001")
                .tipo(TipoContato.EMAIL)
                .valor("joao@example.com")
                .operadorId("OP-001")
                .build()
        );

        assertThatNoException().isThrownBy(() ->
            AdicionarContatoCommand.builder()
                .seguradoId("SEG-001")
                .tipo(TipoContato.CELULAR)
                .valor("11987654321")
                .operadorId("OP-001")
                .build()
        );

        assertThatNoException().isThrownBy(() ->
            AdicionarContatoCommand.builder()
                .seguradoId("SEG-001")
                .tipo(TipoContato.TELEFONE)
                .valor("1134567890")
                .operadorId("OP-001")
                .build()
        );

        assertThatNoException().isThrownBy(() ->
            AdicionarContatoCommand.builder()
                .seguradoId("SEG-001")
                .tipo(TipoContato.WHATSAPP)
                .valor("11987654321")
                .operadorId("OP-001")
                .build()
        );
    }

    @Test
    @DisplayName("Deve permitir configurar correlationId")
    void shouldAllowSettingCorrelationId() {
        // Given
        UUID correlationId = UUID.randomUUID();

        // When
        AdicionarContatoCommand command = AdicionarContatoCommand.builder()
            .seguradoId("SEG-001")
            .tipo(TipoContato.EMAIL)
            .valor("joao@example.com")
            .operadorId("OP-001")
            .correlationId(correlationId)
            .build();

        // Then
        assertThat(command.getCorrelationId()).isEqualTo(correlationId);
    }

    @Test
    @DisplayName("Deve permitir configurar userId")
    void shouldAllowSettingUserId() {
        // Given
        String userId = "user-789";

        // When
        AdicionarContatoCommand command = AdicionarContatoCommand.builder()
            .seguradoId("SEG-001")
            .tipo(TipoContato.EMAIL)
            .valor("joao@example.com")
            .operadorId("OP-001")
            .userId(userId)
            .build();

        // Then
        assertThat(command.getUserId()).isEqualTo(userId);
    }
}
