package com.seguradora.hibrida.domain.segurado.command;

import com.seguradora.hibrida.domain.segurado.model.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link AtualizarSeguradoCommand}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("AtualizarSeguradoCommand - Testes Unitários")
class AtualizarSeguradoCommandTest {

    @Test
    @DisplayName("Deve criar command com dados válidos")
    void shouldCreateCommandWithValidData() {
        // Given
        String seguradoId = "SEG-001";
        String nome = "João Silva";
        String email = "joao@example.com";
        String telefone = "11987654321";
        LocalDate dataNascimento = LocalDate.of(1990, 1, 15);
        Endereco endereco = createEnderecoValido();
        Long versaoEsperada = 5L;

        // When
        AtualizarSeguradoCommand command = AtualizarSeguradoCommand.builder()
            .seguradoId(seguradoId)
            .nome(nome)
            .email(email)
            .telefone(telefone)
            .dataNascimento(dataNascimento)
            .endereco(endereco)
            .versaoEsperada(versaoEsperada)
            .build();

        // Then
        assertThat(command.getSeguradoId()).isEqualTo(seguradoId);
        assertThat(command.getNome()).isEqualTo(nome);
        assertThat(command.getEmail()).isEqualTo(email);
        assertThat(command.getTelefone()).isEqualTo(telefone);
        assertThat(command.getDataNascimento()).isEqualTo(dataNascimento);
        assertThat(command.getEndereco()).isEqualTo(endereco);
        assertThat(command.getVersaoEsperada()).isEqualTo(versaoEsperada);
    }

    @Test
    @DisplayName("Deve gerar commandId automaticamente")
    void shouldGenerateCommandIdAutomatically() {
        // When
        AtualizarSeguradoCommand command = AtualizarSeguradoCommand.builder()
            .seguradoId("SEG-001")
            .nome("João Silva")
            .email("joao@example.com")
            .telefone("11987654321")
            .dataNascimento(LocalDate.of(1990, 1, 15))
            .endereco(createEnderecoValido())
            .build();

        // Then
        assertThat(command.getCommandId()).isNotNull();
    }

    @Test
    @DisplayName("Deve gerar timestamp automaticamente")
    void shouldGenerateTimestampAutomatically() {
        // When
        AtualizarSeguradoCommand command = AtualizarSeguradoCommand.builder()
            .seguradoId("SEG-001")
            .nome("João Silva")
            .email("joao@example.com")
            .telefone("11987654321")
            .dataNascimento(LocalDate.of(1990, 1, 15))
            .endereco(createEnderecoValido())
            .build();

        // Then
        assertThat(command.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve permitir versão esperada para controle de concorrência")
    void shouldAllowExpectedVersionForOptimisticLocking() {
        // Given
        Long versaoEsperada = 10L;

        // When
        AtualizarSeguradoCommand command = AtualizarSeguradoCommand.builder()
            .seguradoId("SEG-001")
            .nome("João Silva")
            .email("joao@example.com")
            .telefone("11987654321")
            .dataNascimento(LocalDate.of(1990, 1, 15))
            .endereco(createEnderecoValido())
            .versaoEsperada(versaoEsperada)
            .build();

        // Then
        assertThat(command.getVersaoEsperada()).isEqualTo(versaoEsperada);
    }

    @Test
    @DisplayName("Deve permitir configurar correlationId")
    void shouldAllowSettingCorrelationId() {
        // Given
        UUID correlationId = UUID.randomUUID();

        // When
        AtualizarSeguradoCommand command = AtualizarSeguradoCommand.builder()
            .seguradoId("SEG-001")
            .nome("João Silva")
            .email("joao@example.com")
            .telefone("11987654321")
            .dataNascimento(LocalDate.of(1990, 1, 15))
            .endereco(createEnderecoValido())
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
        AtualizarSeguradoCommand command = AtualizarSeguradoCommand.builder()
            .seguradoId("SEG-001")
            .nome("João Silva")
            .email("joao@example.com")
            .telefone("11987654321")
            .dataNascimento(LocalDate.of(1990, 1, 15))
            .endereco(createEnderecoValido())
            .userId(userId)
            .build();

        // Then
        assertThat(command.getUserId()).isEqualTo(userId);
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
