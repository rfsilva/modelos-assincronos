package com.seguradora.hibrida.domain.segurado.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link Segurado}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("Segurado - Testes Unitários")
class SeguradoTest {

    @Test
    @DisplayName("Deve criar segurado com dados completos")
    void shouldCreateSeguradoWithCompleteData() {
        // Given
        String cpf = "12345678909";
        String nome = "João da Silva";
        String email = "joao@example.com";
        String telefone = "11987654321";
        LocalDate dataNascimento = LocalDate.now().minusYears(30);
        Endereco endereco = createEnderecoValido();

        // When
        Segurado segurado = new Segurado(cpf, nome, email, telefone, dataNascimento, endereco);

        // Then
        assertThat(segurado.getCpf()).isEqualTo(cpf);
        assertThat(segurado.getNome()).isEqualTo(nome);
        assertThat(segurado.getEmail()).isEqualTo(email);
        assertThat(segurado.getTelefone()).isEqualTo(telefone);
        assertThat(segurado.getDataNascimento()).isEqualTo(dataNascimento);
        assertThat(segurado.getEndereco()).isEqualTo(endereco);
        assertThat(segurado.getStatus()).isEqualTo(StatusSegurado.ATIVO);
        assertThat(segurado.getDataCadastro()).isNotNull();
        assertThat(segurado.getDataUltimaAtualizacao()).isNotNull();
    }

    @Test
    @DisplayName("Deve iniciar segurado com status ATIVO")
    void shouldStartSeguradoWithActiveStatus() {
        // When
        Segurado segurado = createSeguradoValido();

        // Then
        assertThat(segurado.getStatus()).isEqualTo(StatusSegurado.ATIVO);
        assertThat(segurado.isAtivo()).isTrue();
    }

    @Test
    @DisplayName("Deve identificar segurado ativo corretamente")
    void shouldIdentifyActiveSeguradoCorrectly() {
        // Given
        Segurado segurado = createSeguradoValido();

        // When/Then
        assertThat(segurado.isAtivo()).isTrue();

        segurado.setStatus(StatusSegurado.INATIVO);
        assertThat(segurado.isAtivo()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se é maior de idade")
    void shouldCheckIfIsAdult() {
        // Given
        Segurado maior = createSeguradoValido();
        maior.setDataNascimento(LocalDate.now().minusYears(25));

        Segurado menor = createSeguradoValido();
        menor.setDataNascimento(LocalDate.now().minusYears(15));

        // Then
        assertThat(maior.isMaiorIdade()).isTrue();
        assertThat(menor.isMaiorIdade()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar limite de 18 anos corretamente")
    void shouldCheckAdultAgeThresholdCorrectly() {
        // Given
        Segurado exatos18 = createSeguradoValido();
        exatos18.setDataNascimento(LocalDate.now().minusYears(18).minusDays(1));

        Segurado quase18 = createSeguradoValido();
        quase18.setDataNascimento(LocalDate.now().minusYears(18).plusDays(1));

        // Then
        assertThat(exatos18.isMaiorIdade()).isTrue();
        assertThat(quase18.isMaiorIdade()).isFalse();
    }

    @Test
    @DisplayName("Deve permitir atualizar nome")
    void shouldAllowUpdateName() {
        // Given
        Segurado segurado = createSeguradoValido();
        String novoNome = "João Santos Silva";

        // When
        segurado.setNome(novoNome);

        // Then
        assertThat(segurado.getNome()).isEqualTo(novoNome);
    }

    @Test
    @DisplayName("Deve permitir atualizar email")
    void shouldAllowUpdateEmail() {
        // Given
        Segurado segurado = createSeguradoValido();
        String novoEmail = "joao.novo@example.com";

        // When
        segurado.setEmail(novoEmail);

        // Then
        assertThat(segurado.getEmail()).isEqualTo(novoEmail);
    }

    @Test
    @DisplayName("Deve permitir atualizar telefone")
    void shouldAllowUpdateTelefone() {
        // Given
        Segurado segurado = createSeguradoValido();
        String novoTelefone = "11999887766";

        // When
        segurado.setTelefone(novoTelefone);

        // Then
        assertThat(segurado.getTelefone()).isEqualTo(novoTelefone);
    }

    @Test
    @DisplayName("Deve permitir atualizar data de nascimento")
    void shouldAllowUpdateDataNascimento() {
        // Given
        Segurado segurado = createSeguradoValido();
        LocalDate novaData = LocalDate.now().minusYears(35);

        // When
        segurado.setDataNascimento(novaData);

        // Then
        assertThat(segurado.getDataNascimento()).isEqualTo(novaData);
    }

    @Test
    @DisplayName("Deve permitir atualizar endereço")
    void shouldAllowUpdateEndereco() {
        // Given
        Segurado segurado = createSeguradoValido();
        Endereco novoEndereco = new Endereco(
            "Avenida Brasil", "500", "Apto 202", "Jardins", "São Paulo", "SP", "01310100"
        );

        // When
        segurado.setEndereco(novoEndereco);

        // Then
        assertThat(segurado.getEndereco()).isEqualTo(novoEndereco);
    }

    @Test
    @DisplayName("Deve permitir alterar status")
    void shouldAllowChangeStatus() {
        // Given
        Segurado segurado = createSeguradoValido();

        // When
        segurado.setStatus(StatusSegurado.INATIVO);

        // Then
        assertThat(segurado.getStatus()).isEqualTo(StatusSegurado.INATIVO);
        assertThat(segurado.isAtivo()).isFalse();
    }

    @Test
    @DisplayName("Deve ter equals e hashCode corretos via Lombok")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        Segurado segurado1 = createSeguradoValido();
        Segurado segurado2 = createSeguradoValido();
        Segurado segurado3 = createSeguradoValido();
        segurado3.setCpf("98765432100");

        // Then
        assertThat(segurado1).isEqualTo(segurado2);
        assertThat(segurado1).isNotEqualTo(segurado3);
        assertThat(segurado1.hashCode()).isEqualTo(segurado2.hashCode());
    }

    @Test
    @DisplayName("Deve ter toString útil via Lombok")
    void shouldHaveUsefulToString() {
        // Given
        Segurado segurado = createSeguradoValido();

        // When
        String toString = segurado.toString();

        // Then
        assertThat(toString).contains("Segurado");
        assertThat(toString).contains(segurado.getCpf());
        assertThat(toString).contains(segurado.getNome());
    }

    @Test
    @DisplayName("Deve criar segurado com construtor vazio via Lombok")
    void shouldCreateSeguradoWithNoArgsConstructor() {
        // When
        Segurado segurado = new Segurado();

        // Then
        assertThat(segurado).isNotNull();
        assertThat(segurado.getCpf()).isNull();
        assertThat(segurado.getNome()).isNull();
    }

    // ==================== HELPERS ====================

    private Segurado createSeguradoValido() {
        return new Segurado(
            "12345678909",
            "João da Silva",
            "joao@example.com",
            "11987654321",
            LocalDate.now().minusYears(30),
            createEnderecoValido()
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
