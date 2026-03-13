package com.seguradora.hibrida.domain.segurado.controller.dto;

import com.seguradora.hibrida.domain.segurado.model.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link AtualizarSeguradoRequestDTO}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("AtualizarSeguradoRequestDTO - Testes Unitários")
class AtualizarSeguradoRequestDTOTest {

    @Test
    @DisplayName("Deve criar DTO com dados válidos")
    void shouldCreateDTOWithValidData() {
        // Given
        String nome = "João Silva Atualizado";
        String email = "joao.novo@example.com";
        String telefone = "11987654321";
        LocalDate dataNascimento = LocalDate.of(1990, 1, 15);
        Endereco endereco = createEnderecoValido();

        // When
        AtualizarSeguradoRequestDTO dto = new AtualizarSeguradoRequestDTO(
            nome, email, telefone, dataNascimento, endereco
        );

        // Then
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
        AtualizarSeguradoRequestDTO dto = new AtualizarSeguradoRequestDTO();

        // Then
        assertThat(dto).isNotNull();
    }

    @Test
    @DisplayName("Deve permitir setters")
    void shouldAllowSetters() {
        // Given
        AtualizarSeguradoRequestDTO dto = new AtualizarSeguradoRequestDTO();

        // When
        dto.setNome("Pedro Oliveira");
        dto.setEmail("pedro@example.com");
        dto.setTelefone("11987654321");
        dto.setDataNascimento(LocalDate.of(1992, 3, 10));
        dto.setEndereco(createEnderecoValido());

        // Then
        assertThat(dto.getNome()).isEqualTo("Pedro Oliveira");
        assertThat(dto.getEmail()).isEqualTo("pedro@example.com");
        assertThat(dto.getTelefone()).isEqualTo("11987654321");
        assertThat(dto.getDataNascimento()).isEqualTo(LocalDate.of(1992, 3, 10));
        assertThat(dto.getEndereco()).isNotNull();
    }

    @Test
    @DisplayName("Deve aceitar nome com tamanho mínimo")
    void shouldAcceptNameWithMinimumSize() {
        // When
        AtualizarSeguradoRequestDTO dto = new AtualizarSeguradoRequestDTO();
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
        AtualizarSeguradoRequestDTO dto = new AtualizarSeguradoRequestDTO();
        dto.setNome(nomeCompleto);

        // Then
        assertThat(dto.getNome()).hasSize(100);
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
