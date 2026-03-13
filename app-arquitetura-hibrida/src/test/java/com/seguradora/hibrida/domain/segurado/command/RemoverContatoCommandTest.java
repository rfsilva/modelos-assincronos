package com.seguradora.hibrida.domain.segurado.command;

import com.seguradora.hibrida.domain.segurado.model.TipoContato;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link RemoverContatoCommand}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("RemoverContatoCommand - Testes Unitários")
class RemoverContatoCommandTest {

    @Test
    @DisplayName("Deve criar command com construtor principal")
    void shouldCreateCommandWithMainConstructor() {
        // Given
        String seguradoId = "SEG-001";
        TipoContato tipo = TipoContato.EMAIL;
        String valor = "joao@example.com";
        String operadorId = "OP-001";

        // When
        RemoverContatoCommand command = new RemoverContatoCommand(
            seguradoId, tipo, valor, operadorId
        );

        // Then
        assertThat(command.getSeguradoId()).isEqualTo(seguradoId);
        assertThat(command.getTipo()).isEqualTo(tipo);
        assertThat(command.getValor()).isEqualTo(valor);
        assertThat(command.getOperadorId()).isEqualTo(operadorId);
        assertThat(command.getUserId()).isEqualTo(operadorId);
    }

    @Test
    @DisplayName("Deve criar command com construtor vazio")
    void shouldCreateCommandWithNoArgsConstructor() {
        // When
        RemoverContatoCommand command = new RemoverContatoCommand();

        // Then
        assertThat(command).isNotNull();
    }

    @Test
    @DisplayName("Deve gerar commandId automaticamente")
    void shouldGenerateCommandIdAutomatically() {
        // When
        RemoverContatoCommand command = new RemoverContatoCommand(
            "SEG-001", TipoContato.EMAIL, "joao@example.com", "OP-001"
        );

        // Then
        assertThat(command.getCommandId()).isNotNull();
    }

    @Test
    @DisplayName("Deve gerar timestamp automaticamente")
    void shouldGenerateTimestampAutomatically() {
        // When
        RemoverContatoCommand command = new RemoverContatoCommand(
            "SEG-001", TipoContato.EMAIL, "joao@example.com", "OP-001"
        );

        // Then
        assertThat(command.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve permitir versão esperada para controle de concorrência")
    void shouldAllowExpectedVersionForOptimisticLocking() {
        // Given
        Long versaoEsperada = 7L;
        RemoverContatoCommand command = new RemoverContatoCommand(
            "SEG-001", TipoContato.EMAIL, "joao@example.com", "OP-001"
        );

        // When
        command.setVersaoEsperada(versaoEsperada);

        // Then
        assertThat(command.getVersaoEsperada()).isEqualTo(versaoEsperada);
    }

    @Test
    @DisplayName("Deve aceitar diferentes tipos de contato")
    void shouldAcceptDifferentContactTypes() {
        assertThatNoException().isThrownBy(() ->
            new RemoverContatoCommand("SEG-001", TipoContato.EMAIL, "joao@example.com", "OP-001")
        );

        assertThatNoException().isThrownBy(() ->
            new RemoverContatoCommand("SEG-001", TipoContato.CELULAR, "11987654321", "OP-001")
        );

        assertThatNoException().isThrownBy(() ->
            new RemoverContatoCommand("SEG-001", TipoContato.TELEFONE, "1134567890", "OP-001")
        );

        assertThatNoException().isThrownBy(() ->
            new RemoverContatoCommand("SEG-001", TipoContato.WHATSAPP, "11987654321", "OP-001")
        );
    }

    @Test
    @DisplayName("Deve permitir configurar correlationId")
    void shouldAllowSettingCorrelationId() {
        // Given
        UUID correlationId = UUID.randomUUID();
        RemoverContatoCommand command = new RemoverContatoCommand(
            "SEG-001", TipoContato.EMAIL, "joao@example.com", "OP-001"
        );

        // When
        command.setCorrelationId(correlationId);

        // Then
        assertThat(command.getCorrelationId()).isEqualTo(correlationId);
    }

    @Test
    @DisplayName("Deve copiar operadorId para userId")
    void shouldCopyOperadorIdToUserId() {
        // Given
        String operadorId = "OP-999";

        // When
        RemoverContatoCommand command = new RemoverContatoCommand(
            "SEG-001", TipoContato.EMAIL, "joao@example.com", operadorId
        );

        // Then
        assertThat(command.getUserId()).isEqualTo(operadorId);
    }
}
