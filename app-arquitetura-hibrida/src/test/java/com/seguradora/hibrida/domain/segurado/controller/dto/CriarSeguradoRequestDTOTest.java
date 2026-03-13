package com.seguradora.hibrida.domain.segurado.controller.dto;

import com.seguradora.hibrida.domain.segurado.model.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link CriarSeguradoRequestDTO}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("CriarSeguradoRequestDTO - Testes Unitários")
class CriarSeguradoRequestDTOTest {

    @Test
    @DisplayName("Deve criar DTO com dados válidos")
    void shouldCreateDTOWithValidData() {
        // Given
        String cpf = "12345678909";
        String nome = "João Silva";
        String email = "joao@example.com";
        String telefone = "11987654321";
        LocalDate dataNascimento = LocalDate.of(1990, 1, 15);
        Endereco endereco = createEnderecoValido();

        // When
        CriarSeguradoRequestDTO dto = new CriarSeguradoRequestDTO(
            cpf, nome, email, telefone, dataNascimento, endereco
        );

        // Then
        assertThat(dto.getCpf()).isEqualTo(cpf);
        assertThat(dto.getNome()).isEqualTo(nome);
        assertThat(dto.getEmail()).isEqualTo(email);
        assertThat(dto.getTelefone()).isEqualTo(telefone);
        assertThat(dto.getDataNascimento()).isEqualTo(dataNascimento);
        assertThat(dto.getEndereco()).isEqualTo(endereco);
    }

    @Test
    @DisplayName("Deve criar com construtor vazio")
    void shouldCreateWithNoArgsConstructor() {
        // When
        CriarSeguradoRequestDTO dto = new CriarSeguradoRequestDTO();

        // Then
        assertThat(dto).isNotNull();
    }

    @Test
    @DisplayName("Deve permitir setters")
    void shouldAllowSetters() {
        // Given
        CriarSeguradoRequestDTO dto = new CriarSeguradoRequestDTO();

        // When
        dto.setCpf("12345678909");
        dto.setNome("Maria Santos");
        dto.setEmail("maria@example.com");
        dto.setTelefone("11987654321");
        dto.setDataNascimento(LocalDate.of(1985, 5, 20));
        dto.setEndereco(createEnderecoValido());

        // Then
        assertThat(dto.getCpf()).isEqualTo("12345678909");
        assertThat(dto.getNome()).isEqualTo("Maria Santos");
        assertThat(dto.getEmail()).isEqualTo("maria@example.com");
        assertThat(dto.getTelefone()).isEqualTo("11987654321");
        assertThat(dto.getDataNascimento()).isEqualTo(LocalDate.of(1985, 5, 20));
        assertThat(dto.getEndereco()).isNotNull();
    }

    @Test
    @DisplayName("Deve aceitar telefone com 10 dígitos")
    void shouldAcceptPhoneWith10Digits() {
        // When
        CriarSeguradoRequestDTO dto = new CriarSeguradoRequestDTO();
        dto.setTelefone("1134567890");

        // Then
        assertThat(dto.getTelefone()).hasSize(10);
    }

    @Test
    @DisplayName("Deve aceitar telefone com 11 dígitos")
    void shouldAcceptPhoneWith11Digits() {
        // When
        CriarSeguradoRequestDTO dto = new CriarSeguradoRequestDTO();
        dto.setTelefone("11987654321");

        // Then
        assertThat(dto.getTelefone()).hasSize(11);
    }

    @Test
    @DisplayName("Deve aceitar CPF com 11 dígitos")
    void shouldAcceptCPFWith11Digits() {
        // When
        CriarSeguradoRequestDTO dto = new CriarSeguradoRequestDTO();
        dto.setCpf("12345678909");

        // Then
        assertThat(dto.getCpf()).hasSize(11);
    }

    @Test
    @DisplayName("Deve aceitar nome com tamanho mínimo")
    void shouldAcceptNameWithMinimumSize() {
        // When
        CriarSeguradoRequestDTO dto = new CriarSeguradoRequestDTO();
        dto.setNome("Ana");

        // Then
        assertThat(dto.getNome()).hasSize(3);
    }

    @Test
    @DisplayName("Deve aceitar nome com tamanho máximo")
    void shouldAcceptNameWithMaximumSize() {
        // Given
        String nomeCompleto = "A".repeat(100);

        // When
        CriarSeguradoRequestDTO dto = new CriarSeguradoRequestDTO();
        dto.setNome(nomeCompleto);

        // Then
        assertThat(dto.getNome()).hasSize(100);
    }

    @Test
    @DisplayName("Deve aceitar data de nascimento no passado")
    void shouldAcceptPastBirthDate() {
        // When
        CriarSeguradoRequestDTO dto = new CriarSeguradoRequestDTO();
        dto.setDataNascimento(LocalDate.of(1990, 1, 1));

        // Then
        assertThat(dto.getDataNascimento()).isBefore(LocalDate.now());
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
