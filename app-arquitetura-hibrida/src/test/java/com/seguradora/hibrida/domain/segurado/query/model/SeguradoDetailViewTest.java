package com.seguradora.hibrida.domain.segurado.query.model;

import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link SeguradoDetailView}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("SeguradoDetailView - Testes Unitários")
class SeguradoDetailViewTest {

    @Test
    @DisplayName("Deve criar view detalhada com construtor principal")
    void shouldCreateDetailViewWithMainConstructor() {
        // Given
        String id = "SEG-001";
        String cpf = "12345678909";
        String nome = "João Silva";
        String email = "joao@example.com";
        String telefone = "11987654321";
        LocalDate dataNascimento = LocalDate.of(1990, 1, 15);
        StatusSegurado status = StatusSegurado.ATIVO;
        String enderecoCompleto = "Rua Teste, 100 - Centro - São Paulo/SP";
        Instant dataCriacao = Instant.now();
        Integer idade = 35;

        // When
        SeguradoDetailView view = new SeguradoDetailView(
            id, cpf, nome, email, telefone, dataNascimento,
            status, enderecoCompleto, dataCriacao, idade
        );

        // Then
        assertThat(view.getId()).isEqualTo(id);
        assertThat(view.getCpf()).isEqualTo(cpf);
        assertThat(view.getNome()).isEqualTo(nome);
        assertThat(view.getEmail()).isEqualTo(email);
        assertThat(view.getTelefone()).isEqualTo(telefone);
        assertThat(view.getDataNascimento()).isEqualTo(dataNascimento);
        assertThat(view.getStatus()).isEqualTo(status);
        assertThat(view.getEnderecoCompleto()).isEqualTo(enderecoCompleto);
        assertThat(view.getDataCriacao()).isEqualTo(dataCriacao);
        assertThat(view.getIdade()).isEqualTo(idade);
    }

    @Test
    @DisplayName("Deve inicializar valores padrão no construtor")
    void shouldInitializeDefaultValuesInConstructor() {
        // Given
        Instant dataCriacao = Instant.now();

        // When
        SeguradoDetailView view = new SeguradoDetailView(
            "SEG-001", "12345678909", "João Silva", "joao@example.com",
            "11987654321", LocalDate.of(1990, 1, 15),
            StatusSegurado.ATIVO, "Endereço completo", dataCriacao, 35
        );

        // Then
        assertThat(view.getDataUltimaAtualizacao()).isEqualTo(dataCriacao);
        assertThat(view.getTotalContatos()).isEqualTo(2); // Email + telefone iniciais
        assertThat(view.getTotalAlteracoes()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve incrementar contador de alterações")
    void shouldIncrementChangesCounter() {
        // Given
        SeguradoDetailView view = new SeguradoDetailView(
            "SEG-001", "12345678909", "João Silva", "joao@example.com",
            "11987654321", LocalDate.of(1990, 1, 15),
            StatusSegurado.ATIVO, "Endereço completo", Instant.now(), 35
        );
        Instant dataAntes = view.getDataUltimaAtualizacao();

        // When
        view.incrementarAlteracoes();

        // Then
        assertThat(view.getTotalAlteracoes()).isEqualTo(1);
        assertThat(view.getDataUltimaAlteracao()).isNotNull();
        assertThat(view.getDataUltimaAtualizacao()).isAfterOrEqualTo(dataAntes);
    }

    @Test
    @DisplayName("Deve incrementar múltiplas vezes")
    void shouldIncrementMultipleTimes() {
        // Given
        SeguradoDetailView view = new SeguradoDetailView(
            "SEG-001", "12345678909", "João Silva", "joao@example.com",
            "11987654321", LocalDate.of(1990, 1, 15),
            StatusSegurado.ATIVO, "Endereço completo", Instant.now(), 35
        );

        // When
        view.incrementarAlteracoes();
        view.incrementarAlteracoes();
        view.incrementarAlteracoes();

        // Then
        assertThat(view.getTotalAlteracoes()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve permitir criação com construtor vazio")
    void shouldAllowCreationWithNoArgsConstructor() {
        // When
        SeguradoDetailView view = new SeguradoDetailView();

        // Then
        assertThat(view).isNotNull();
    }

    @Test
    @DisplayName("Deve armazenar endereço desnormalizado completo")
    void shouldStoreCompleteDenormalizedAddress() {
        // Given
        SeguradoDetailView view = new SeguradoDetailView();

        // When
        view.setEnderecoLogradouro("Avenida Paulista");
        view.setEnderecoNumero("1000");
        view.setEnderecoComplemento("Apto 505");
        view.setEnderecoBairro("Bela Vista");
        view.setEnderecoCidade("São Paulo");
        view.setEnderecoEstado("SP");
        view.setEnderecoCep("01310100");

        // Then
        assertThat(view.getEnderecoLogradouro()).isEqualTo("Avenida Paulista");
        assertThat(view.getEnderecoNumero()).isEqualTo("1000");
        assertThat(view.getEnderecoComplemento()).isEqualTo("Apto 505");
        assertThat(view.getEnderecoBairro()).isEqualTo("Bela Vista");
        assertThat(view.getEnderecoCidade()).isEqualTo("São Paulo");
        assertThat(view.getEnderecoEstado()).isEqualTo("SP");
        assertThat(view.getEnderecoCep()).isEqualTo("01310100");
    }

    @Test
    @DisplayName("Deve armazenar dados de apólices")
    void shouldStorePolicyData() {
        // Given
        SeguradoDetailView view = new SeguradoDetailView();

        // When
        view.setTotalApolicesAtivas(3);
        view.setValorTotalApolices(new BigDecimal("150000.00"));
        view.setDataUltimaApolice(Instant.now());

        // Then
        assertThat(view.getTotalApolicesAtivas()).isEqualTo(3);
        assertThat(view.getValorTotalApolices()).isEqualByComparingTo("150000.00");
        assertThat(view.getDataUltimaApolice()).isNotNull();
    }

    @Test
    @DisplayName("Deve inicializar valores padrão de apólices")
    void shouldInitializeDefaultPolicyValues() {
        // When
        SeguradoDetailView view = new SeguradoDetailView();

        // Then
        assertThat(view.getTotalApolicesAtivas()).isEqualTo(0);
        assertThat(view.getValorTotalApolices()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Deve armazenar operador responsável")
    void shouldStoreResponsibleOperator() {
        // Given
        SeguradoDetailView view = new SeguradoDetailView();

        // When
        view.setOperadorResponsavel("OP-123");

        // Then
        assertThat(view.getOperadorResponsavel()).isEqualTo("OP-123");
    }

    @Test
    @DisplayName("Deve armazenar contatos principais como JSON")
    void shouldStoreMainContactsAsJson() {
        // Given
        SeguradoDetailView view = new SeguradoDetailView();
        String contatosJson = "{\"email\":\"joao@example.com\",\"telefone\":\"11987654321\"}";

        // When
        view.setContatosPrincipais(contatosJson);

        // Then
        assertThat(view.getContatosPrincipais()).isEqualTo(contatosJson);
    }

    @Test
    @DisplayName("Deve armazenar total de contatos")
    void shouldStoreTotalContacts() {
        // Given
        SeguradoDetailView view = new SeguradoDetailView();

        // When
        view.setTotalContatos(5);

        // Then
        assertThat(view.getTotalContatos()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deve aceitar diferentes status")
    void shouldAcceptDifferentStatuses() {
        // Given
        SeguradoDetailView view = new SeguradoDetailView();

        // When/Then
        assertThatNoException().isThrownBy(() -> view.setStatus(StatusSegurado.ATIVO));
        assertThatNoException().isThrownBy(() -> view.setStatus(StatusSegurado.SUSPENSO));
        assertThatNoException().isThrownBy(() -> view.setStatus(StatusSegurado.INATIVO));
        assertThatNoException().isThrownBy(() -> view.setStatus(StatusSegurado.BLOQUEADO));
    }
}
