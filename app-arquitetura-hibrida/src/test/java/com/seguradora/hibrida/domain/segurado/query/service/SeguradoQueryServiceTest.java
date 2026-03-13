package com.seguradora.hibrida.domain.segurado.query.service;

import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoDetailView;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoListView;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoQueryModel;
import com.seguradora.hibrida.domain.segurado.query.repository.SeguradoDetailViewRepository;
import com.seguradora.hibrida.domain.segurado.query.repository.SeguradoListViewRepository;
import com.seguradora.hibrida.domain.segurado.query.repository.SeguradoQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link SeguradoQueryService}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SeguradoQueryService - Testes Unitários")
class SeguradoQueryServiceTest {

    @Mock
    private SeguradoQueryRepository repository;

    @Mock
    private SeguradoListViewRepository listViewRepository;

    @Mock
    private SeguradoDetailViewRepository detailViewRepository;

    @InjectMocks
    private SeguradoQueryService service;

    @Test
    @DisplayName("Deve buscar segurado por ID")
    void shouldFindById() {
        // Given
        String id = "SEG-001";
        SeguradoQueryModel segurado = SeguradoQueryModel.builder()
                .id(id)
                .nome("João Silva")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(segurado));

        // When
        Optional<SeguradoQueryModel> result = service.findById(id);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Deve buscar segurado por CPF")
    void shouldFindByCpf() {
        // Given
        String cpf = "12345678909";
        SeguradoQueryModel segurado = SeguradoQueryModel.builder()
                .cpf(cpf)
                .nome("João Silva")
                .build();

        when(repository.findByCpf(cpf)).thenReturn(Optional.of(segurado));

        // When
        Optional<SeguradoQueryModel> result = service.findByCpf(cpf);

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
                .email(email)
                .nome("João Silva")
                .build();

        when(repository.findByEmail(email)).thenReturn(Optional.of(segurado));

        // When
        Optional<SeguradoQueryModel> result = service.findByEmail(email);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
        verify(repository).findByEmail(email);
    }

    @Test
    @DisplayName("Deve buscar view detalhada por ID")
    void shouldFindDetailById() {
        // Given
        String id = "SEG-001";
        SeguradoDetailView detailView = new SeguradoDetailView();

        when(detailViewRepository.findById(id)).thenReturn(Optional.of(detailView));

        // When
        Optional<SeguradoDetailView> result = service.findDetailById(id);

        // Then
        assertThat(result).isPresent();
        verify(detailViewRepository).findById(id);
    }

    @Test
    @DisplayName("Deve listar todos os segurados com paginação")
    void shouldFindAll() {
        // Given
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoListView> page = new PageImpl<>(List.of(new SeguradoListView()));

        when(listViewRepository.findAll(pageable)).thenReturn(page);

        // When
        Page<SeguradoListView> result = service.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        verify(listViewRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar segurados por status")
    void shouldFindByStatus() {
        // Given
        StatusSegurado status = StatusSegurado.ATIVO;
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoListView> page = new PageImpl<>(List.of(new SeguradoListView()));

        when(listViewRepository.findByStatus(status, pageable)).thenReturn(page);

        // When
        Page<SeguradoListView> result = service.findByStatus(status, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(listViewRepository).findByStatus(status, pageable);
    }

    @Test
    @DisplayName("Deve buscar segurados por nome")
    void shouldFindByNome() {
        // Given
        String nome = "João";
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoListView> page = new PageImpl<>(List.of(new SeguradoListView()));

        when(listViewRepository.findByNomeContaining(nome, pageable)).thenReturn(page);

        // When
        Page<SeguradoListView> result = service.findByNome(nome, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(listViewRepository).findByNomeContaining(nome, pageable);
    }

    @Test
    @DisplayName("Deve buscar segurados por cidade")
    void shouldFindByCidade() {
        // Given
        String cidade = "São Paulo";
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoListView> page = new PageImpl<>(List.of(new SeguradoListView()));

        when(listViewRepository.findByCidade(cidade, pageable)).thenReturn(page);

        // When
        Page<SeguradoListView> result = service.findByCidade(cidade, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(listViewRepository).findByCidade(cidade, pageable);
    }

    @Test
    @DisplayName("Deve buscar segurados por estado")
    void shouldFindByEstado() {
        // Given
        String estado = "SP";
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoListView> page = new PageImpl<>(List.of(new SeguradoListView()));

        when(listViewRepository.findByEstado(estado, pageable)).thenReturn(page);

        // When
        Page<SeguradoListView> result = service.findByEstado(estado, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(listViewRepository).findByEstado(estado, pageable);
    }

    @Test
    @DisplayName("Deve verificar se CPF existe")
    void shouldCheckIfCpfExists() {
        // Given
        String cpf = "12345678909";
        when(repository.existsByCpf(cpf)).thenReturn(true);

        // When
        boolean exists = service.existsByCpf(cpf);

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
        boolean exists = service.existsByEmail(email);

        // Then
        assertThat(exists).isFalse();
        verify(repository).existsByEmail(email);
    }

    @Test
    @DisplayName("Deve contar segurados por status")
    void shouldCountByStatus() {
        // Given
        StatusSegurado status = StatusSegurado.ATIVO;
        when(repository.countByStatus(status)).thenReturn(100L);

        // When
        long count = service.countByStatus(status);

        // Then
        assertThat(count).isEqualTo(100L);
        verify(repository).countByStatus(status);
    }

    @Test
    @DisplayName("Deve retornar estatísticas gerais")
    void shouldGetStatistics() {
        // Given
        when(repository.countByStatus(StatusSegurado.ATIVO)).thenReturn(100L);
        when(repository.countByStatus(StatusSegurado.INATIVO)).thenReturn(20L);
        when(repository.countByStatus(StatusSegurado.SUSPENSO)).thenReturn(5L);
        when(repository.countByStatus(StatusSegurado.BLOQUEADO)).thenReturn(3L);

        // When
        SeguradoQueryService.SeguradoStatistics stats = service.getStatistics();

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalAtivos()).isEqualTo(100L);
        assertThat(stats.getTotalInativos()).isEqualTo(20L);
        assertThat(stats.getTotalSuspensos()).isEqualTo(5L);
        assertThat(stats.getTotalBloqueados()).isEqualTo(3L);
        assertThat(stats.getTotal()).isEqualTo(128L);
    }

    @Test
    @DisplayName("Deve calcular percentuais nas estatísticas")
    void shouldCalculatePercentagesInStatistics() {
        // Given
        when(repository.countByStatus(StatusSegurado.ATIVO)).thenReturn(80L);
        when(repository.countByStatus(StatusSegurado.INATIVO)).thenReturn(20L);
        when(repository.countByStatus(StatusSegurado.SUSPENSO)).thenReturn(0L);
        when(repository.countByStatus(StatusSegurado.BLOQUEADO)).thenReturn(0L);

        // When
        SeguradoQueryService.SeguradoStatistics stats = service.getStatistics();

        // Then
        assertThat(stats.getPercentualAtivos()).isEqualTo(80.0);
        assertThat(stats.getPercentualInativos()).isEqualTo(20.0);
    }

    @Test
    @DisplayName("Deve buscar com múltiplos critérios")
    void shouldFindWithMultipleCriteria() {
        // Given
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoListView> page = new PageImpl<>(List.of(new SeguradoListView()));

        when(listViewRepository.findWithMultipleCriteria(
            "João", "12345678909", "joao@example.com",
            StatusSegurado.ATIVO, "São Paulo", "SP", pageable
        )).thenReturn(page);

        // When
        Page<SeguradoListView> result = service.findWithMultipleCriteria(
            "João", "12345678909", "joao@example.com",
            StatusSegurado.ATIVO, "São Paulo", "SP", pageable
        );

        // Then
        assertThat(result).isNotNull();
        verify(listViewRepository).findWithMultipleCriteria(
            "João", "12345678909", "joao@example.com",
            StatusSegurado.ATIVO, "São Paulo", "SP", pageable
        );
    }

    @Test
    @DisplayName("Deve buscar fuzzy por nome")
    void shouldFindByNomeFuzzy() {
        // Given
        String termo = "João";
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoListView> page = new PageImpl<>(List.of(new SeguradoListView()));

        when(listViewRepository.findByNomeFuzzy(termo, pageable)).thenReturn(page);

        // When
        Page<SeguradoListView> result = service.findByNomeFuzzy(termo, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(listViewRepository).findByNomeFuzzy(termo, pageable);
    }
}
