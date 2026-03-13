package com.seguradora.hibrida.domain.segurado.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link Endereco}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("Endereco - Testes Unitários")
class EnderecoTest {

    @Test
    @DisplayName("Deve criar endereço completo")
    void shouldCreateCompleteAddress() {
        // When
        Endereco endereco = new Endereco(
            "Rua das Flores",
            "123",
            "Apto 45",
            "Centro",
            "São Paulo",
            "SP",
            "01310100"
        );

        // Then
        assertThat(endereco.getLogradouro()).isEqualTo("Rua das Flores");
        assertThat(endereco.getNumero()).isEqualTo("123");
        assertThat(endereco.getComplemento()).isEqualTo("Apto 45");
        assertThat(endereco.getBairro()).isEqualTo("Centro");
        assertThat(endereco.getCidade()).isEqualTo("São Paulo");
        assertThat(endereco.getEstado()).isEqualTo("SP");
        assertThat(endereco.getCep()).isEqualTo("01310100");
    }

    @Test
    @DisplayName("Deve criar endereço sem complemento")
    void shouldCreateAddressWithoutComplement() {
        // When
        Endereco endereco = new Endereco(
            "Avenida Paulista",
            "1000",
            null,
            "Bela Vista",
            "São Paulo",
            "SP",
            "01310100"
        );

        // Then
        assertThat(endereco.getComplemento()).isNull();
    }

    @Test
    @DisplayName("Deve retornar endereço completo formatado")
    void shouldReturnFormattedCompleteAddress() {
        // Given
        Endereco endereco = new Endereco(
            "Rua das Flores",
            "123",
            "Apto 45",
            "Centro",
            "São Paulo",
            "SP",
            "01310100"
        );

        // When
        String enderecoCompleto = endereco.getEnderecoCompleto();

        // Then
        assertThat(enderecoCompleto).isEqualTo(
            "Rua das Flores, 123 - Apto 45 - Centro, São Paulo/SP - CEP: 01310100"
        );
    }

    @Test
    @DisplayName("Deve retornar endereço formatado sem complemento")
    void shouldReturnFormattedAddressWithoutComplement() {
        // Given
        Endereco endereco = new Endereco(
            "Avenida Paulista",
            "1000",
            null,
            "Bela Vista",
            "São Paulo",
            "SP",
            "01310100"
        );

        // When
        String enderecoCompleto = endereco.getEnderecoCompleto();

        // Then
        assertThat(enderecoCompleto).isEqualTo(
            "Avenida Paulista, 1000 - Bela Vista, São Paulo/SP - CEP: 01310100"
        );
        assertThat(enderecoCompleto).doesNotContain(" -  - ");
    }

    @Test
    @DisplayName("Deve retornar endereço formatado com complemento vazio")
    void shouldReturnFormattedAddressWithEmptyComplement() {
        // Given
        Endereco endereco = new Endereco(
            "Rua Teste",
            "100",
            "",
            "Jardins",
            "São Paulo",
            "SP",
            "01310100"
        );

        // When
        String enderecoCompleto = endereco.getEnderecoCompleto();

        // Then
        assertThat(enderecoCompleto).doesNotContain(" -  - ");
    }

    @Test
    @DisplayName("Deve ter equals e hashCode corretos via Lombok")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        Endereco endereco1 = new Endereco(
            "Rua das Flores", "123", "Apto 45", "Centro", "São Paulo", "SP", "01310100"
        );
        Endereco endereco2 = new Endereco(
            "Rua das Flores", "123", "Apto 45", "Centro", "São Paulo", "SP", "01310100"
        );
        Endereco endereco3 = new Endereco(
            "Rua Diferente", "456", null, "Outro Bairro", "Rio de Janeiro", "RJ", "20000000"
        );

        // Then
        assertThat(endereco1).isEqualTo(endereco2);
        assertThat(endereco1).isNotEqualTo(endereco3);
        assertThat(endereco1.hashCode()).isEqualTo(endereco2.hashCode());
    }

    @Test
    @DisplayName("Deve ter toString útil via Lombok")
    void shouldHaveUsefulToString() {
        // Given
        Endereco endereco = new Endereco(
            "Rua das Flores", "123", "Apto 45", "Centro", "São Paulo", "SP", "01310100"
        );

        // When
        String toString = endereco.toString();

        // Then
        assertThat(toString).contains("Endereco");
        assertThat(toString).contains("Rua das Flores");
        assertThat(toString).contains("123");
        assertThat(toString).contains("01310100");
    }

    @Test
    @DisplayName("Deve suportar todos os estados brasileiros")
    void shouldSupportAllBrazilianStates() {
        String[] estados = {"AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA",
                           "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN",
                           "RS", "RO", "RR", "SC", "SP", "SE", "TO"};

        for (String estado : estados) {
            Endereco endereco = new Endereco(
                "Rua Teste", "100", null, "Centro", "Cidade", estado, "01310100"
            );
            assertThat(endereco.getEstado()).isEqualTo(estado);
        }
    }
}
