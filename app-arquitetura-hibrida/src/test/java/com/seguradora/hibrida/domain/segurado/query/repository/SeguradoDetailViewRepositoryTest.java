package com.seguradora.hibrida.domain.segurado.query.repository;

import com.seguradora.hibrida.domain.segurado.query.model.SeguradoDetailView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link SeguradoDetailViewRepository}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("SeguradoDetailViewRepository - Testes Unitários")
class SeguradoDetailViewRepositoryTest {

    private final SeguradoDetailViewRepository repository = mock(SeguradoDetailViewRepository.class);

    @Test
    @DisplayName("Deve buscar segurado por CPF")
    void shouldFindByCpf() {
        // Given
        String cpf = "12345678909";
        SeguradoDetailView segurado = new SeguradoDetailView();

        when(repository.findByCpf(cpf)).thenReturn(Optional.of(segurado));

        // When
        Optional<SeguradoDetailView> result = repository.findByCpf(cpf);

        // Then
        assertThat(result).isPresent();
        verify(repository).findByCpf(cpf);
    }

    @Test
    @DisplayName("Deve buscar segurado por email")
    void shouldFindByEmail() {
        // Given
        String email = "test@example.com";
        SeguradoDetailView segurado = new SeguradoDetailView();

        when(repository.findByEmail(email)).thenReturn(Optional.of(segurado));

        // When
        Optional<SeguradoDetailView> result = repository.findByEmail(email);

        // Then
        assertThat(result).isPresent();
        verify(repository).findByEmail(email);
    }

    @Test
    @DisplayName("Deve buscar segurados com apólices ativas")
    void shouldFindComApolicesAtivas() {
        // Given
        List<SeguradoDetailView> segurados = List.of(new SeguradoDetailView());

        when(repository.findComApolicesAtivas()).thenReturn(segurados);

        // When
        List<SeguradoDetailView> result = repository.findComApolicesAtivas();

        // Then
        assertThat(result).isNotEmpty();
        verify(repository).findComApolicesAtivas();
    }

    @Test
    @DisplayName("Deve buscar segurados por valor mínimo de apólices")
    void shouldFindByValorTotalApolicesGreaterThanEqual() {
        // Given
        java.math.BigDecimal valorMinimo = new java.math.BigDecimal("10000.00");
        List<SeguradoDetailView> segurados = List.of(new SeguradoDetailView());

        when(repository.findByValorTotalApolicesGreaterThanEqual(valorMinimo)).thenReturn(segurados);

        // When
        List<SeguradoDetailView> result = repository.findByValorTotalApolicesGreaterThanEqual(valorMinimo);

        // Then
        assertThat(result).isNotEmpty();
        verify(repository).findByValorTotalApolicesGreaterThanEqual(valorMinimo);
    }

    @Test
    @DisplayName("Deve buscar segurados mais ativos")
    void shouldFindMaisAtivos() {
        // Given
        List<SeguradoDetailView> segurados = List.of(new SeguradoDetailView());

        when(repository.findMaisAtivos()).thenReturn(segurados);

        // When
        List<SeguradoDetailView> result = repository.findMaisAtivos();

        // Then
        assertThat(result).isNotEmpty();
        verify(repository).findMaisAtivos();
    }

    @Test
    @DisplayName("Deve retornar vazio quando não há segurados")
    void shouldReturnEmptyWhenNoSegurados() {
        // Given
        when(repository.findComApolicesAtivas()).thenReturn(List.of());

        // When
        List<SeguradoDetailView> result = repository.findComApolicesAtivas();

        // Then
        assertThat(result).isEmpty();
        verify(repository).findComApolicesAtivas();
    }
}
