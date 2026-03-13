package com.seguradora.hibrida.domain.segurado.command;

import com.seguradora.hibrida.domain.segurado.model.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link CriarSeguradoCommand}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("CriarSeguradoCommand - Testes Unitários")
class CriarSeguradoCommandTest {

    @Test
    @DisplayName("Deve criar command com dados válidos")
    void shouldCreateCommandWithValidData() {
        // Given
        String cpf = "12345678909";
        String nome = "João Silva";
        String email = "joao@example.com";
        String telefone = "11987654321";
        LocalDate dataNascimento = LocalDate.of(1990, 1, 15);
        Endereco endereco = createEnderecoValido();

        // When
        CriarSeguradoCommand command = CriarSeguradoCommand.builder()
            .cpf(cpf)
            .nome(nome)
            .email(email)
            .telefone(telefone)
            .dataNascimento(dataNascimento)
            .endereco(endereco)
            .build();

        // Then
        assertThat(command.getCpf()).isEqualTo(cpf);
        assertThat(command.getNome()).isEqualTo(nome);
        assertThat(command.getEmail()).isEqualTo(email);
        assertThat(command.getTelefone()).isEqualTo(telefone);
        assertThat(command.getDataNascimento()).isEqualTo(dataNascimento);
        assertThat(command.getEndereco()).isEqualTo(endereco);
    }

    @Test
    @DisplayName("Deve gerar commandId automaticamente")
    void shouldGenerateCommandIdAutomatically() {
        // When
        CriarSeguradoCommand command = CriarSeguradoCommand.builder()
            .cpf("12345678909")
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
        CriarSeguradoCommand command = CriarSeguradoCommand.builder()
            .cpf("12345678909")
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
    @DisplayName("Deve permitir configurar correlationId")
    void shouldAllowSettingCorrelationId() {
        // Given
        UUID correlationId = UUID.randomUUID();

        // When
        CriarSeguradoCommand command = CriarSeguradoCommand.builder()
            .cpf("12345678909")
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
        String userId = "user-123";

        // When
        CriarSeguradoCommand command = CriarSeguradoCommand.builder()
            .cpf("12345678909")
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

    @Test
    @DisplayName("Deve aceitar telefone com 10 dígitos")
    void shouldAcceptPhoneWith10Digits() {
        // When/Then
        assertThatNoException().isThrownBy(() ->
            CriarSeguradoCommand.builder()
                .cpf("12345678909")
                .nome("João Silva")
                .email("joao@example.com")
                .telefone("1134567890")
                .dataNascimento(LocalDate.of(1990, 1, 15))
                .endereco(createEnderecoValido())
                .build()
        );
    }

    @Test
    @DisplayName("Deve aceitar telefone com 11 dígitos")
    void shouldAcceptPhoneWith11Digits() {
        // When/Then
        assertThatNoException().isThrownBy(() ->
            CriarSeguradoCommand.builder()
                .cpf("12345678909")
                .nome("João Silva")
                .email("joao@example.com")
                .telefone("11987654321")
                .dataNascimento(LocalDate.of(1990, 1, 15))
                .endereco(createEnderecoValido())
                .build()
        );
    }

    @Test
    @DisplayName("Deve aceitar CPF com 11 dígitos")
    void shouldAcceptCPFWith11Digits() {
        // When/Then
        assertThatNoException().isThrownBy(() ->
            CriarSeguradoCommand.builder()
                .cpf("12345678909")
                .nome("João Silva")
                .email("joao@example.com")
                .telefone("11987654321")
                .dataNascimento(LocalDate.of(1990, 1, 15))
                .endereco(createEnderecoValido())
                .build()
        );
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
