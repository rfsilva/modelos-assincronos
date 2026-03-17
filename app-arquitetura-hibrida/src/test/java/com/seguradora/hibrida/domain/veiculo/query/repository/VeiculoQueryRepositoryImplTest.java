package com.seguradora.hibrida.domain.veiculo.query.repository;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import com.seguradora.hibrida.domain.veiculo.query.model.VeiculoQueryModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link VeiculoQueryRepositoryImpl}.
 * Implementação customizada com EntityManager.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VeiculoQueryRepositoryImpl - Testes Unitários")
class VeiculoQueryRepositoryImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<VeiculoQueryModel> typedQuery;

    @Mock
    private TypedQuery<Long> countQuery;

    @InjectMocks
    private VeiculoQueryRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(repository, "entityManager", entityManager);
    }

    @Nested
    @DisplayName("Busca por Nome do Proprietário")
    class BuscaPorProprietarioNome {

        @Test
        @DisplayName("Deve buscar veículos por nome do proprietário com paginação")
        void shouldFindByProprietarioNomeContainingIgnoreCase() {
            // Arrange
            String nome = "João";
            Pageable pageable = PageRequest.of(0, 20);

            List<VeiculoQueryModel> veiculos = List.of(
                createVeiculoQueryModel("VEI-001", "João Silva"),
                createVeiculoQueryModel("VEI-002", "João Oliveira")
            );

            when(entityManager.createQuery(contains("SELECT v FROM VeiculoQueryModel v"), eq(VeiculoQueryModel.class)))
                .thenReturn(typedQuery);
            when(typedQuery.setParameter("nome", nome)).thenReturn(typedQuery);
            when(typedQuery.setFirstResult(0)).thenReturn(typedQuery);
            when(typedQuery.setMaxResults(20)).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(veiculos);

            when(entityManager.createQuery(contains("SELECT COUNT(v)"), eq(Long.class)))
                .thenReturn(countQuery);
            when(countQuery.setParameter("nome", nome)).thenReturn(countQuery);
            when(countQuery.getSingleResult()).thenReturn(2L);

            // Act
            Page<VeiculoQueryModel> result = repository.findByProprietarioNomeContainingIgnoreCase(nome, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2L);
            assertThat(result.getContent()).allMatch(v -> v.getProprietarioNome().contains("João"));

            verify(entityManager).createQuery(contains("SELECT v FROM VeiculoQueryModel v"), eq(VeiculoQueryModel.class));
            verify(entityManager).createQuery(contains("SELECT COUNT(v)"), eq(Long.class));
            verify(typedQuery).setParameter("nome", nome);
            verify(countQuery).setParameter("nome", nome);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando nome não encontrado")
        void shouldReturnEmptyPageWhenNomeNotFound() {
            // Arrange
            String nome = "NomeInexistente";
            Pageable pageable = PageRequest.of(0, 20);

            when(entityManager.createQuery(contains("SELECT v FROM VeiculoQueryModel v"), eq(VeiculoQueryModel.class)))
                .thenReturn(typedQuery);
            when(typedQuery.setParameter("nome", nome)).thenReturn(typedQuery);
            when(typedQuery.setFirstResult(0)).thenReturn(typedQuery);
            when(typedQuery.setMaxResults(20)).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(List.of());

            when(entityManager.createQuery(contains("SELECT COUNT(v)"), eq(Long.class)))
                .thenReturn(countQuery);
            when(countQuery.setParameter("nome", nome)).thenReturn(countQuery);
            when(countQuery.getSingleResult()).thenReturn(0L);

            // Act
            Page<VeiculoQueryModel> result = repository.findByProprietarioNomeContainingIgnoreCase(nome, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("Deve aplicar paginação corretamente na segunda página")
        void shouldApplyPaginationCorrectlyOnSecondPage() {
            // Arrange
            String nome = "Silva";
            Pageable pageable = PageRequest.of(1, 10);

            when(entityManager.createQuery(contains("SELECT v FROM VeiculoQueryModel v"), eq(VeiculoQueryModel.class)))
                .thenReturn(typedQuery);
            when(typedQuery.setParameter("nome", nome)).thenReturn(typedQuery);
            when(typedQuery.setFirstResult(10)).thenReturn(typedQuery);
            when(typedQuery.setMaxResults(10)).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(List.of(createVeiculoQueryModel("VEI-011", "Silva")));

            when(entityManager.createQuery(contains("SELECT COUNT(v)"), eq(Long.class)))
                .thenReturn(countQuery);
            when(countQuery.setParameter("nome", nome)).thenReturn(countQuery);
            when(countQuery.getSingleResult()).thenReturn(11L);

            // Act
            Page<VeiculoQueryModel> result = repository.findByProprietarioNomeContainingIgnoreCase(nome, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getTotalElements()).isEqualTo(11L);
            verify(typedQuery).setFirstResult(10);
            verify(typedQuery).setMaxResults(10);
        }
    }

    @Nested
    @DisplayName("Busca por Cidade")
    class BuscaPorCidade {

        @Test
        @DisplayName("Deve buscar veículos por cidade com paginação")
        void shouldFindByCidadeContainingIgnoreCase() {
            // Arrange
            String cidade = "São Paulo";
            Pageable pageable = PageRequest.of(0, 20);

            List<VeiculoQueryModel> veiculos = List.of(
                createVeiculoQueryModelWithCidade("VEI-001", "São Paulo"),
                createVeiculoQueryModelWithCidade("VEI-002", "São Paulo")
            );

            when(entityManager.createQuery(contains("SELECT v FROM VeiculoQueryModel v"), eq(VeiculoQueryModel.class)))
                .thenReturn(typedQuery);
            when(typedQuery.setParameter("cidade", cidade)).thenReturn(typedQuery);
            when(typedQuery.setFirstResult(0)).thenReturn(typedQuery);
            when(typedQuery.setMaxResults(20)).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(veiculos);

            when(entityManager.createQuery(contains("SELECT COUNT(v)"), eq(Long.class)))
                .thenReturn(countQuery);
            when(countQuery.setParameter("cidade", cidade)).thenReturn(countQuery);
            when(countQuery.getSingleResult()).thenReturn(2L);

            // Act
            Page<VeiculoQueryModel> result = repository.findByCidadeContainingIgnoreCase(cidade, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2L);
            assertThat(result.getContent()).allMatch(v -> v.getCidade().equals("São Paulo"));

            verify(entityManager).createQuery(contains("SELECT v FROM VeiculoQueryModel v"), eq(VeiculoQueryModel.class));
            verify(entityManager).createQuery(contains("SELECT COUNT(v)"), eq(Long.class));
        }

        @Test
        @DisplayName("Deve buscar cidade com busca parcial")
        void shouldFindCidadeWithPartialMatch() {
            // Arrange
            String cidade = "Paulo";
            Pageable pageable = PageRequest.of(0, 20);

            when(entityManager.createQuery(contains("SELECT v FROM VeiculoQueryModel v"), eq(VeiculoQueryModel.class)))
                .thenReturn(typedQuery);
            when(typedQuery.setParameter("cidade", cidade)).thenReturn(typedQuery);
            when(typedQuery.setFirstResult(0)).thenReturn(typedQuery);
            when(typedQuery.setMaxResults(20)).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(List.of(createVeiculoQueryModelWithCidade("VEI-001", "São Paulo")));

            when(entityManager.createQuery(contains("SELECT COUNT(v)"), eq(Long.class)))
                .thenReturn(countQuery);
            when(countQuery.setParameter("cidade", cidade)).thenReturn(countQuery);
            when(countQuery.getSingleResult()).thenReturn(1L);

            // Act
            Page<VeiculoQueryModel> result = repository.findByCidadeContainingIgnoreCase(cidade, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCidade()).contains("Paulo");
        }
    }

    @Nested
    @DisplayName("Contadores por Status")
    class ContadoresPorStatus {

        @Test
        @DisplayName("Deve contar veículos por status")
        void shouldCountByStatus() {
            // Arrange
            StatusVeiculo status = StatusVeiculo.ATIVO;

            when(entityManager.createQuery(contains("SELECT COUNT(v)"), eq(Long.class)))
                .thenReturn(countQuery);
            when(countQuery.setParameter("status", status)).thenReturn(countQuery);
            when(countQuery.getSingleResult()).thenReturn(150L);

            // Act
            long count = repository.countByStatus(status);

            // Assert
            assertThat(count).isEqualTo(150L);
            verify(entityManager).createQuery(contains("SELECT COUNT(v)"), eq(Long.class));
            verify(countQuery).setParameter("status", status);
        }

        @Test
        @DisplayName("Deve contar veículos inativos")
        void shouldCountInactiveVehicles() {
            // Arrange
            StatusVeiculo status = StatusVeiculo.INATIVO;

            when(entityManager.createQuery(contains("SELECT COUNT(v)"), eq(Long.class)))
                .thenReturn(countQuery);
            when(countQuery.setParameter("status", status)).thenReturn(countQuery);
            when(countQuery.getSingleResult()).thenReturn(25L);

            // Act
            long count = repository.countByStatus(status);

            // Assert
            assertThat(count).isEqualTo(25L);
        }

        @Test
        @DisplayName("Deve retornar zero quando não há veículos com status")
        void shouldReturnZeroWhenNoVehiclesWithStatus() {
            // Arrange
            StatusVeiculo status = StatusVeiculo.BLOQUEADO;

            when(entityManager.createQuery(contains("SELECT COUNT(v)"), eq(Long.class)))
                .thenReturn(countQuery);
            when(countQuery.setParameter("status", status)).thenReturn(countQuery);
            when(countQuery.getSingleResult()).thenReturn(0L);

            // Act
            long count = repository.countByStatus(status);

            // Assert
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Contador de Apólice Ativa")
    class ContadorApoliceAtiva {

        @Test
        @DisplayName("Deve contar veículos com apólice ativa")
        void shouldCountByApoliceAtivaTrue() {
            // Arrange
            when(entityManager.createQuery(contains("SELECT COUNT(v)"), eq(Long.class)))
                .thenReturn(countQuery);
            when(countQuery.getSingleResult()).thenReturn(85L);

            // Act
            long count = repository.countByApoliceAtivaTrue();

            // Assert
            assertThat(count).isEqualTo(85L);
            verify(entityManager).createQuery(contains("SELECT COUNT(v)"), eq(Long.class));
        }

        @Test
        @DisplayName("Deve retornar zero quando não há veículos com apólice ativa")
        void shouldReturnZeroWhenNoVehiclesWithApoliceAtiva() {
            // Arrange
            when(entityManager.createQuery(contains("SELECT COUNT(v)"), eq(Long.class)))
                .thenReturn(countQuery);
            when(countQuery.getSingleResult()).thenReturn(0L);

            // Act
            long count = repository.countByApoliceAtivaTrue();

            // Assert
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Estatísticas por Estado")
    class EstatisticasPorEstado {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Deve retornar estatísticas por estado")
        void shouldCountByEstado() {
            // Arrange
            List<Object[]> stats = List.of(
                new Object[]{"SP", 120L},
                new Object[]{"RJ", 80L},
                new Object[]{"MG", 50L}
            );

            var query = mock(jakarta.persistence.Query.class);
            when(entityManager.createQuery(contains("SELECT v.estado, COUNT(v)"))).thenReturn(query);
            when(query.getResultList()).thenReturn(stats);

            // Act
            List<Object[]> result = repository.countByEstado();

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result.get(0)[0]).isEqualTo("SP");
            assertThat(result.get(0)[1]).isEqualTo(120L);
            verify(entityManager).createQuery(contains("SELECT v.estado, COUNT(v)"));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Deve retornar lista vazia quando não há dados")
        void shouldReturnEmptyListWhenNoData() {
            // Arrange
            var query = mock(jakarta.persistence.Query.class);
            when(entityManager.createQuery(contains("SELECT v.estado, COUNT(v)"))).thenReturn(query);
            when(query.getResultList()).thenReturn(List.of());

            // Act
            List<Object[]> result = repository.countByEstado();

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Estatísticas por Marca")
    class EstatisticasPorMarca {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Deve retornar estatísticas por marca")
        void shouldCountByMarca() {
            // Arrange
            List<Object[]> stats = List.of(
                new Object[]{"Toyota", 45L},
                new Object[]{"Honda", 38L},
                new Object[]{"Ford", 32L}
            );

            var query = mock(jakarta.persistence.Query.class);
            when(entityManager.createQuery(contains("SELECT v.marca, COUNT(v)"))).thenReturn(query);
            when(query.getResultList()).thenReturn(stats);

            // Act
            List<Object[]> result = repository.countByMarca();

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result.get(0)[0]).isEqualTo("Toyota");
            assertThat(result.get(0)[1]).isEqualTo(45L);
            verify(entityManager).createQuery(contains("SELECT v.marca, COUNT(v)"));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Deve retornar marcas ordenadas por quantidade")
        void shouldReturnMarcasOrderedByQuantity() {
            // Arrange
            List<Object[]> stats = List.of(
                new Object[]{"Toyota", 45L},
                new Object[]{"Honda", 38L},
                new Object[]{"Ford", 32L}
            );

            var query = mock(jakarta.persistence.Query.class);
            when(entityManager.createQuery(contains("ORDER BY COUNT(v) DESC"))).thenReturn(query);
            when(query.getResultList()).thenReturn(stats);

            // Act
            List<Object[]> result = repository.countByMarca();

            // Assert
            assertThat(result).hasSize(3);
            // Verifica ordem decrescente
            Long previousCount = Long.MAX_VALUE;
            for (Object[] row : result) {
                Long currentCount = (Long) row[1];
                assertThat(currentCount).isLessThanOrEqualTo(previousCount);
                previousCount = currentCount;
            }
        }
    }

    @Nested
    @DisplayName("Estatísticas por Categoria")
    class EstatisticasPorCategoria {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Deve retornar estatísticas por categoria")
        void shouldCountByCategoria() {
            // Arrange
            List<Object[]> stats = List.of(
                new Object[]{"PASSEIO", 150L},
                new Object[]{"UTILITARIO", 45L},
                new Object[]{"CAMINHAO", 20L}
            );

            var query = mock(jakarta.persistence.Query.class);
            when(entityManager.createQuery(contains("SELECT v.categoria, COUNT(v)"))).thenReturn(query);
            when(query.getResultList()).thenReturn(stats);

            // Act
            List<Object[]> result = repository.countByCategoria();

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result.get(0)[0]).isEqualTo("PASSEIO");
            assertThat(result.get(0)[1]).isEqualTo(150L);
            verify(entityManager).createQuery(contains("SELECT v.categoria, COUNT(v)"));
        }
    }

    @Nested
    @DisplayName("Estatísticas por Tipo de Combustível")
    class EstatisticasPorTipoCombustivel {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Deve retornar estatísticas por tipo de combustível")
        void shouldCountByTipoCombustivel() {
            // Arrange
            List<Object[]> stats = List.of(
                new Object[]{"GASOLINA", 80L},
                new Object[]{"FLEX", 70L},
                new Object[]{"DIESEL", 30L}
            );

            var query = mock(jakarta.persistence.Query.class);
            when(entityManager.createQuery(contains("SELECT v.tipoCombustivel, COUNT(v)"))).thenReturn(query);
            when(query.getResultList()).thenReturn(stats);

            // Act
            List<Object[]> result = repository.countByTipoCombustivel();

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result.get(0)[0]).isEqualTo("GASOLINA");
            assertThat(result.get(0)[1]).isEqualTo(80L);
            verify(entityManager).createQuery(contains("SELECT v.tipoCombustivel, COUNT(v)"));
        }
    }

    // ===== Helper Methods =====

    private VeiculoQueryModel createVeiculoQueryModel(String id, String proprietarioNome) {
        VeiculoQueryModel model = new VeiculoQueryModel();
        model.setId(id);
        model.setProprietarioNome(proprietarioNome);
        model.setPlaca("ABC1234");
        model.setStatus(StatusVeiculo.ATIVO);
        return model;
    }

    private VeiculoQueryModel createVeiculoQueryModelWithCidade(String id, String cidade) {
        VeiculoQueryModel model = new VeiculoQueryModel();
        model.setId(id);
        model.setCidade(cidade);
        model.setPlaca("ABC" + id);
        model.setStatus(StatusVeiculo.ATIVO);
        return model;
    }
}
