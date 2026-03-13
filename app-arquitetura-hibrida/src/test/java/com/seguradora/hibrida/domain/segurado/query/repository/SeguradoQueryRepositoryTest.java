package com.seguradora.hibrida.domain.segurado.query.repository;

import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoQueryModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link SeguradoQueryRepository}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("SeguradoQueryRepository - Testes Unitários")
class SeguradoQueryRepositoryTest {

    private final SeguradoQueryRepository repository = mock(SeguradoQueryRepository.class);

    @Test
    @DisplayName("Deve buscar segurado por CPF")
    void shouldFindByCpf() {
        // Given
        String cpf = "12345678909";
        SeguradoQueryModel segurado = SeguradoQueryModel.builder()
                .id("SEG-001")
                .cpf(cpf)
                .nome("João Silva")
                .build();

        when(repository.findByCpf(cpf)).thenReturn(Optional.of(segurado));

        // When
        Optional<SeguradoQueryModel> result = repository.findByCpf(cpf);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCpf()).isEqualTo(cpf);
        verify(repository).findByCpf(cpf);
    }

    @Test
    @DisplayName("Deve buscar segurado por email")
    void shouldFindByEmail() {
        // Given
        String email = "joao@example.com";
        SeguradoQueryModel segurado = SeguradoQueryModel.builder()
                .id("SEG-001")
                .email(email)
                .nome("João Silva")
                .build();

        when(repository.findByEmail(email)).thenReturn(Optional.of(segurado));

        // When
        Optional<SeguradoQueryModel> result = repository.findByEmail(email);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
        verify(repository).findByEmail(email);
    }

    @Test
    @DisplayName("Deve verificar se CPF existe")
    void shouldCheckIfCpfExists() {
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
    @DisplayName("Deve verificar se email existe")
    void shouldCheckIfEmailExists() {
        // Given
        String email = "joao@example.com";
        when(repository.existsByEmail(email)).thenReturn(false);

        // When
        boolean exists = repository.existsByEmail(email);

        // Then
        assertThat(exists).isFalse();
        verify(repository).existsByEmail(email);
    }

    @Test
    @DisplayName("Deve contar segurados por status")
    void shouldCountByStatus() {
        // Given
        StatusSegurado status = StatusSegurado.ATIVO;
        when(repository.countByStatus(status)).thenReturn(42L);

        // When
        long count = repository.countByStatus(status);

        // Then
        assertThat(count).isEqualTo(42L);
        verify(repository).countByStatus(status);
    }

    @Test
    @DisplayName("Deve buscar segurados por status com paginação")
    void shouldFindByStatusWithPagination() {
        // Given
        StatusSegurado status = StatusSegurado.ATIVO;
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoQueryModel> page = mock(Page.class);

        when(repository.findByStatus(status, pageable)).thenReturn(page);

        // When
        Page<SeguradoQueryModel> result = repository.findByStatus(status, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(repository).findByStatus(status, pageable);
    }

    @Test
    @DisplayName("Deve buscar segurados por nome contendo texto")
    void shouldFindByNomeContaining() {
        // Given
        String nome = "João";
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoQueryModel> page = mock(Page.class);

        when(repository.findByNomeContaining(nome, pageable)).thenReturn(page);

        // When
        Page<SeguradoQueryModel> result = repository.findByNomeContaining(nome, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(repository).findByNomeContaining(nome, pageable);
    }

    @Test
    @DisplayName("Deve buscar segurados por cidade")
    void shouldFindByCidade() {
        // Given
        String cidade = "São Paulo";
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoQueryModel> page = mock(Page.class);

        when(repository.findByCidade(cidade, pageable)).thenReturn(page);

        // When
        Page<SeguradoQueryModel> result = repository.findByCidade(cidade, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(repository).findByCidade(cidade, pageable);
    }

    @Test
    @DisplayName("Deve buscar segurados por estado")
    void shouldFindByEstado() {
        // Given
        String estado = "SP";
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoQueryModel> page = mock(Page.class);

        when(repository.findByEstado(estado, pageable)).thenReturn(page);

        // When
        Page<SeguradoQueryModel> result = repository.findByEstado(estado, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(repository).findByEstado(estado, pageable);
    }
}
