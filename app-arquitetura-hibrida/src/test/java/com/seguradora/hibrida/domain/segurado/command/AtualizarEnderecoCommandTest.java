package com.seguradora.hibrida.domain.segurado.command;

import com.seguradora.hibrida.domain.segurado.model.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link AtualizarEnderecoCommand}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("AtualizarEnderecoCommand - Testes Unitários")
class AtualizarEnderecoCommandTest {

    @Test
    @DisplayName("Deve criar command com construtor principal")
    void shouldCreateCommandWithMainConstructor() {
        // Given
        String seguradoId = "SEG-001";
        Endereco endereco = createEnderecoValido();
        String operadorId = "OP-001";

        // When
        AtualizarEnderecoCommand command = new AtualizarEnderecoCommand(
            seguradoId, endereco, operadorId
        );

        // Then
        assertThat(command.getSeguradoId()).isEqualTo(seguradoId);
        assertThat(command.getEndereco()).isEqualTo(endereco);
        assertThat(command.getOperadorId()).isEqualTo(operadorId);
        assertThat(command.getUserId()).isEqualTo(operadorId);
    }

    @Test
    @DisplayName("Deve criar command com construtor incluindo versão esperada")
    void shouldCreateCommandWithExpectedVersion() {
        // Given
        String seguradoId = "SEG-001";
        Endereco endereco = createEnderecoValido();
        String operadorId = "OP-001";
        Long versaoEsperada = 5L;

        // When
        AtualizarEnderecoCommand command = new AtualizarEnderecoCommand(
            seguradoId, endereco, operadorId, versaoEsperada
        );

        // Then
        assertThat(command.getSeguradoId()).isEqualTo(seguradoId);
        assertThat(command.getEndereco()).isEqualTo(endereco);
        assertThat(command.getOperadorId()).isEqualTo(operadorId);
        assertThat(command.getVersaoEsperada()).isEqualTo(versaoEsperada);
    }

    @Test
    @DisplayName("Deve criar command com construtor vazio")
    void shouldCreateCommandWithNoArgsConstructor() {
        // When
        AtualizarEnderecoCommand command = new AtualizarEnderecoCommand();

        // Then
        assertThat(command).isNotNull();
    }

    @Test
    @DisplayName("Deve gerar commandId automaticamente")
    void shouldGenerateCommandIdAutomatically() {
        // When
        AtualizarEnderecoCommand command = new AtualizarEnderecoCommand(
            "SEG-001", createEnderecoValido(), "OP-001"
        );

        // Then
        assertThat(command.getCommandId()).isNotNull();
    }

    @Test
    @DisplayName("Deve gerar timestamp automaticamente")
    void shouldGenerateTimestampAutomatically() {
        // When
        AtualizarEnderecoCommand command = new AtualizarEnderecoCommand(
            "SEG-001", createEnderecoValido(), "OP-001"
        );

        // Then
        assertThat(command.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve aceitar endereço completo com complemento")
    void shouldAcceptCompleteAddressWithComplement() {
        // Given
        Endereco endereco = new Endereco(
            "Avenida Paulista",
            "1000",
            "Apto 505",
            "Bela Vista",
            "São Paulo",
            "SP",
            "01310100"
        );

        // When/Then
        assertThatNoException().isThrownBy(() ->
            new AtualizarEnderecoCommand("SEG-001", endereco, "OP-001")
        );
    }

    @Test
    @DisplayName("Deve aceitar endereço sem complemento")
    void shouldAcceptAddressWithoutComplement() {
        // Given
        Endereco endereco = new Endereco(
            "Rua Augusta",
            "500",
            null,
            "Consolação",
            "São Paulo",
            "SP",
            "01305000"
        );

        // When/Then
        assertThatNoException().isThrownBy(() ->
            new AtualizarEnderecoCommand("SEG-001", endereco, "OP-001")
        );
    }

    @Test
    @DisplayName("Deve permitir configurar correlationId")
    void shouldAllowSettingCorrelationId() {
        // Given
        UUID correlationId = UUID.randomUUID();
        AtualizarEnderecoCommand command = new AtualizarEnderecoCommand(
            "SEG-001", createEnderecoValido(), "OP-001"
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
        String operadorId = "OP-777";

        // When
        AtualizarEnderecoCommand command = new AtualizarEnderecoCommand(
            "SEG-001", createEnderecoValido(), operadorId
        );

        // Then
        assertThat(command.getUserId()).isEqualTo(operadorId);
    }

    private Endereco createEnderecoValido() {
        return new Endereco(
            "Rua Teste",
            "100",
            "Apto 101",
            "Centro",
            "São Paulo",
            "SP",
            "01310100"
        );
    }
}
