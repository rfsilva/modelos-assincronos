package com.seguradora.hibrida.domain.segurado.query.model;

import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link SeguradoListView}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("SeguradoListView - Testes Unitários")
class SeguradoListViewTest {

    @Test
    @DisplayName("Deve criar view com construtor principal")
    void shouldCreateViewWithMainConstructor() {
        // Given
        String id = "SEG-001";
        String cpf = "12345678909";
        String nome = "João Silva";
        String email = "joao@example.com";
        StatusSegurado status = StatusSegurado.ATIVO;
        String cidade = "São Paulo";
        String estado = "SP";
        Instant dataCriacao = Instant.now();
        Integer idade = 35;

        // When
        SeguradoListView view = new SeguradoListView(
            id, cpf, nome, email, status, cidade, estado, dataCriacao, idade
        );

        // Then
        assertThat(view.getId()).isEqualTo(id);
        assertThat(view.getCpf()).isEqualTo(cpf);
        assertThat(view.getNome()).isEqualTo(nome);
        assertThat(view.getEmail()).isEqualTo(email);
        assertThat(view.getStatus()).isEqualTo(status);
        assertThat(view.getCidade()).isEqualTo(cidade);
        assertThat(view.getEstado()).isEqualTo(estado);
        assertThat(view.getDataCriacao()).isEqualTo(dataCriacao);
        assertThat(view.getIdade()).isEqualTo(idade);
    }

    @Test
    @DisplayName("Deve inicializar valores padrão no construtor")
    void shouldInitializeDefaultValuesInConstructor() {
        // Given
        Instant dataCriacao = Instant.now();

        // When
        SeguradoListView view = new SeguradoListView(
            "SEG-001", "12345678909", "João Silva", "joao@example.com",
            StatusSegurado.ATIVO, "São Paulo", "SP", dataCriacao, 35
        );

        // Then
        assertThat(view.getDataUltimaAtualizacao()).isEqualTo(dataCriacao);
        assertThat(view.getTotalContatos()).isEqualTo(2); // Email + telefone iniciais
        assertThat(view.getTemWhatsapp()).isFalse();
    }

    @Test
    @DisplayName("Deve permitir criação com construtor vazio")
    void shouldAllowCreationWithNoArgsConstructor() {
        // When
        SeguradoListView view = new SeguradoListView();

        // Then
        assertThat(view).isNotNull();
    }

    @Test
    @DisplayName("Deve permitir setters")
    void shouldAllowSetters() {
        // Given
        SeguradoListView view = new SeguradoListView();

        // When
        view.setId("SEG-002");
        view.setCpf("98765432100");
        view.setNome("Maria Santos");
        view.setEmail("maria@example.com");
        view.setStatus(StatusSegurado.SUSPENSO);
        view.setCidade("Rio de Janeiro");
        view.setEstado("RJ");
        view.setIdade(28);
        view.setTotalContatos(5);
        view.setTemWhatsapp(true);

        // Then
        assertThat(view.getId()).isEqualTo("SEG-002");
        assertThat(view.getCpf()).isEqualTo("98765432100");
        assertThat(view.getNome()).isEqualTo("Maria Santos");
        assertThat(view.getEmail()).isEqualTo("maria@example.com");
        assertThat(view.getStatus()).isEqualTo(StatusSegurado.SUSPENSO);
        assertThat(view.getCidade()).isEqualTo("Rio de Janeiro");
        assertThat(view.getEstado()).isEqualTo("RJ");
        assertThat(view.getIdade()).isEqualTo(28);
        assertThat(view.getTotalContatos()).isEqualTo(5);
        assertThat(view.getTemWhatsapp()).isTrue();
    }

    @Test
    @DisplayName("Deve armazenar data de última atualização")
    void shouldStoreLastUpdateDate() {
        // Given
        SeguradoListView view = new SeguradoListView();
        Instant dataAtualizacao = Instant.now();

        // When
        view.setDataUltimaAtualizacao(dataAtualizacao);

        // Then
        assertThat(view.getDataUltimaAtualizacao()).isEqualTo(dataAtualizacao);
    }

    @Test
    @DisplayName("Deve aceitar diferentes status")
    void shouldAcceptDifferentStatuses() {
        assertThatNoException().isThrownBy(() -> {
            SeguradoListView view = new SeguradoListView();
            view.setStatus(StatusSegurado.ATIVO);
        });

        assertThatNoException().isThrownBy(() -> {
            SeguradoListView view = new SeguradoListView();
            view.setStatus(StatusSegurado.SUSPENSO);
        });

        assertThatNoException().isThrownBy(() -> {
            SeguradoListView view = new SeguradoListView();
            view.setStatus(StatusSegurado.INATIVO);
        });

        assertThatNoException().isThrownBy(() -> {
            SeguradoListView view = new SeguradoListView();
            view.setStatus(StatusSegurado.BLOQUEADO);
        });
    }

    @Test
    @DisplayName("Deve rastrear flag de WhatsApp")
    void shouldTrackWhatsAppFlag() {
        // Given
        SeguradoListView view = new SeguradoListView();

        // When/Then - Inicialmente sem WhatsApp
        view.setTemWhatsapp(false);
        assertThat(view.getTemWhatsapp()).isFalse();

        // When/Then - Com WhatsApp
        view.setTemWhatsapp(true);
        assertThat(view.getTemWhatsapp()).isTrue();
    }
}
