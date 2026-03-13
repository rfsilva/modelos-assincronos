package com.seguradora.hibrida.domain.segurado.query.model;

import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link SeguradoQueryModel}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("SeguradoQueryModel - Testes Unitários")
class SeguradoQueryModelTest {

    @Test
    @DisplayName("Deve criar query model com builder")
    void shouldCreateQueryModelWithBuilder() {
        // Given
        String id = "SEG-001";
        String cpf = "12345678909";
        String nome = "João Silva";
        String email = "joao@example.com";
        String telefone = "11987654321";
        LocalDate dataNascimento = LocalDate.of(1990, 1, 15);
        StatusSegurado status = StatusSegurado.ATIVO;

        // When
        SeguradoQueryModel model = SeguradoQueryModel.builder()
            .id(id)
            .cpf(cpf)
            .nome(nome)
            .email(email)
            .telefone(telefone)
            .dataNascimento(dataNascimento)
            .status(status)
            .build();

        // Then
        assertThat(model.getId()).isEqualTo(id);
        assertThat(model.getCpf()).isEqualTo(cpf);
        assertThat(model.getNome()).isEqualTo(nome);
        assertThat(model.getEmail()).isEqualTo(email);
        assertThat(model.getTelefone()).isEqualTo(telefone);
        assertThat(model.getDataNascimento()).isEqualTo(dataNascimento);
        assertThat(model.getStatus()).isEqualTo(status);
    }

    @Test
    @DisplayName("Deve armazenar endereço desnormalizado")
    void shouldStoreDenormalizedAddress() {
        // When
        SeguradoQueryModel model = SeguradoQueryModel.builder()
            .id("SEG-001")
            .cpf("12345678909")
            .nome("João Silva")
            .cep("01310100")
            .logradouro("Avenida Paulista")
            .numero("1000")
            .complemento("Apto 505")
            .bairro("Bela Vista")
            .cidade("São Paulo")
            .estado("SP")
            .build();

        // Then
        assertThat(model.getCep()).isEqualTo("01310100");
        assertThat(model.getLogradouro()).isEqualTo("Avenida Paulista");
        assertThat(model.getNumero()).isEqualTo("1000");
        assertThat(model.getComplemento()).isEqualTo("Apto 505");
        assertThat(model.getBairro()).isEqualTo("Bela Vista");
        assertThat(model.getCidade()).isEqualTo("São Paulo");
        assertThat(model.getEstado()).isEqualTo("SP");
    }

    @Test
    @DisplayName("Deve definir timestamps automaticamente no onCreate")
    void shouldSetTimestampsAutomaticallyOnCreate() {
        // Given
        SeguradoQueryModel model = SeguradoQueryModel.builder()
            .id("SEG-001")
            .cpf("12345678909")
            .nome("João Silva")
            .build();

        // When
        model.onCreate();

        // Then
        assertThat(model.getCreatedAt()).isNotNull();
        assertThat(model.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve atualizar updatedAt no onUpdate")
    void shouldUpdateUpdatedAtOnUpdate() throws InterruptedException {
        // Given
        SeguradoQueryModel model = SeguradoQueryModel.builder()
            .id("SEG-001")
            .cpf("12345678909")
            .nome("João Silva")
            .build();
        model.onCreate();
        Instant createdAt = model.getCreatedAt();

        Thread.sleep(10); // Pequeno delay para garantir timestamps diferentes

        // When
        model.onUpdate();

        // Then
        assertThat(model.getUpdatedAt()).isAfter(createdAt);
        assertThat(model.getCreatedAt()).isEqualTo(createdAt); // createdAt não muda
    }

    @Test
    @DisplayName("Deve armazenar metadados de versionamento")
    void shouldStoreVersioningMetadata() {
        // When
        SeguradoQueryModel model = SeguradoQueryModel.builder()
            .id("SEG-001")
            .cpf("12345678909")
            .nome("João Silva")
            .version(5L)
            .build();

        // Then
        assertThat(model.getVersion()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Deve permitir criação com construtor completo")
    void shouldAllowCreationWithAllArgsConstructor() {
        // When
        SeguradoQueryModel model = new SeguradoQueryModel(
            "SEG-001",
            "12345678909",
            "João Silva",
            "joao@example.com",
            "11987654321",
            LocalDate.of(1990, 1, 15),
            StatusSegurado.ATIVO,
            "01310100",
            "Rua Teste",
            "100",
            "Apto 101",
            "Centro",
            "São Paulo",
            "SP",
            Instant.now(),
            Instant.now(),
            1L
        );

        // Then
        assertThat(model.getId()).isEqualTo("SEG-001");
        assertThat(model.getCpf()).isEqualTo("12345678909");
    }

    @Test
    @DisplayName("Deve permitir criação com construtor vazio")
    void shouldAllowCreationWithNoArgsConstructor() {
        // When
        SeguradoQueryModel model = new SeguradoQueryModel();

        // Then
        assertThat(model).isNotNull();
    }

    @Test
    @DisplayName("Deve não sobrescrever createdAt existente no onCreate")
    void shouldNotOverwriteExistingCreatedAtOnCreate() {
        // Given
        Instant fixedCreatedAt = Instant.parse("2024-01-01T10:00:00Z");
        SeguradoQueryModel model = SeguradoQueryModel.builder()
            .id("SEG-001")
            .cpf("12345678909")
            .nome("João Silva")
            .createdAt(fixedCreatedAt)
            .build();

        // When
        model.onCreate();

        // Then
        assertThat(model.getCreatedAt()).isEqualTo(fixedCreatedAt);
    }
}
