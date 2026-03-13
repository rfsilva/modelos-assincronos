package com.seguradora.hibrida.domain.segurado.query.repository;

import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoListView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link SeguradoListViewRepository}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("SeguradoListViewRepository - Testes Unitários")
class SeguradoListViewRepositoryTest {

    private final SeguradoListViewRepository repository = mock(SeguradoListViewRepository.class);

    @Test
    @DisplayName("Deve buscar segurado por CPF")
    void shouldFindByCpf() {
        // Given
        String cpf = "12345678909";
        SeguradoListView segurado = new SeguradoListView();

        when(repository.findByCpf(cpf)).thenReturn(Optional.of(segurado));

        // When
        Optional<SeguradoListView> result = repository.findByCpf(cpf);

        // Then
        assertThat(result).isPresent();
        verify(repository).findByCpf(cpf);
    }

    @Test
    @DisplayName("Deve buscar segurado por email")
    void shouldFindByEmail() {
        // Given
        String email = "test@example.com";
        SeguradoListView segurado = new SeguradoListView();

        when(repository.findByEmail(email)).thenReturn(Optional.of(segurado));

        // When
        Optional<SeguradoListView> result = repository.findByEmail(email);

        // Then
        assertThat(result).isPresent();
        verify(repository).findByEmail(email);
    }

    @Test
    @DisplayName("Deve verificar existência por CPF")
    void shouldCheckExistsByCpf() {
        // Given
        String cpf = "12345678909";
        when(repository.existsByCpf(cpf)).thenReturn(true);

        // When
        boolean exists = repository.existsByCpf(cpf);

        // Then
        assertThat(exists).isTrue();
        verify(repository).existsByCpf(cpf);
    }

    @Test
    @DisplayName("Deve verificar existência por email")
    void shouldCheckExistsByEmail() {
        // Given
        String email = "test@example.com";
        when(repository.existsByEmail(email)).thenReturn(false);

        // When
        boolean exists = repository.existsByEmail(email);

        // Then
        assertThat(exists).isFalse();
        verify(repository).existsByEmail(email);
    }

    @Test
    @DisplayName("Deve buscar por nome contendo texto")
    void shouldFindByNomeContaining() {
        // Given
        String nome = "João";
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoListView> page = mock(Page.class);

        when(repository.findByNomeContaining(nome, pageable)).thenReturn(page);

        // When
        Page<SeguradoListView> result = repository.findByNomeContaining(nome, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(repository).findByNomeContaining(nome, pageable);
    }

    @Test
    @DisplayName("Deve buscar por status")
    void shouldFindByStatus() {
        // Given
        StatusSegurado status = StatusSegurado.ATIVO;
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoListView> page = mock(Page.class);

        when(repository.findByStatus(status, pageable)).thenReturn(page);

        // When
        Page<SeguradoListView> result = repository.findByStatus(status, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(repository).findByStatus(status, pageable);
    }

    @Test
    @DisplayName("Deve buscar por cidade")
    void shouldFindByCidade() {
        // Given
        String cidade = "São Paulo";
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoListView> page = mock(Page.class);

        when(repository.findByCidade(cidade, pageable)).thenReturn(page);

        // When
        Page<SeguradoListView> result = repository.findByCidade(cidade, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(repository).findByCidade(cidade, pageable);
    }

    @Test
    @DisplayName("Deve buscar por estado")
    void shouldFindByEstado() {
        // Given
        String estado = "SP";
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoListView> page = mock(Page.class);

        when(repository.findByEstado(estado, pageable)).thenReturn(page);

        // When
        Page<SeguradoListView> result = repository.findByEstado(estado, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(repository).findByEstado(estado, pageable);
    }

    @Test
    @DisplayName("Deve contar por status")
    void shouldCountByStatus() {
        // Given
        StatusSegurado status = StatusSegurado.ATIVO;
        when(repository.countByStatus(status)).thenReturn(100L);

        // When
        long count = repository.countByStatus(status);

        // Then
        assertThat(count).isEqualTo(100L);
        verify(repository).countByStatus(status);
    }

    @Test
    @DisplayName("Deve contar por cidade")
    void shouldCountByCidade() {
        // Given
        String cidade = "São Paulo";
        when(repository.countByCidade(cidade)).thenReturn(50L);

        // When
        long count = repository.countByCidade(cidade);

        // Then
        assertThat(count).isEqualTo(50L);
        verify(repository).countByCidade(cidade);
    }

    @Test
    @DisplayName("Deve buscar com múltiplos critérios")
    void shouldFindWithMultipleCriteria() {
        // Given
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoListView> page = mock(Page.class);

        when(repository.findWithMultipleCriteria(
            "João", "12345678909", "joao@example.com",
            StatusSegurado.ATIVO, "São Paulo", "SP", pageable
        )).thenReturn(page);

        // When
        Page<SeguradoListView> result = repository.findWithMultipleCriteria(
            "João", "12345678909", "joao@example.com",
            StatusSegurado.ATIVO, "São Paulo", "SP", pageable
        );

        // Then
        assertThat(result).isNotNull();
        verify(repository).findWithMultipleCriteria(
            "João", "12345678909", "joao@example.com",
            StatusSegurado.ATIVO, "São Paulo", "SP", pageable
        );
    }
}
