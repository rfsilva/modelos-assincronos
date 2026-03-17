package com.seguradora.hibrida.domain.veiculo.query.repository;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import com.seguradora.hibrida.domain.veiculo.query.model.VeiculoQueryModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link VeiculoQueryRepositoryExtensions}.
 * Interface com queries customizadas de estatísticas e agregações.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("VeiculoQueryRepositoryExtensions - Testes Unitários")
class VeiculoQueryRepositoryExtensionsTest {

    private final VeiculoQueryRepositoryExtensions extensions = mock(VeiculoQueryRepositoryExtensions.class);

    @Nested
    @DisplayName("Consultas com Busca Parcial")
    class ConsultasBuscaParcial {

        @Test
        @DisplayName("Deve buscar veículos por nome do proprietário (case insensitive)")
        void shouldFindByProprietarioNomeContainingIgnoreCase() {
            // Arrange
            String nome = "joão";
            Pageable pageable = PageRequest.of(0, 20);
            VeiculoQueryModel veiculo = createVeiculoQueryModelWithProprietario("VEI-001", "João Silva");
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(veiculo));

            when(extensions.findByProprietarioNomeContainingIgnoreCase(nome, pageable)).thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = extensions.findByProprietarioNomeContainingIgnoreCase(nome, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getProprietarioNome()).containsIgnoringCase(nome);
            verify(extensions).findByProprietarioNomeContainingIgnoreCase(nome, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por cidade (case insensitive)")
        void shouldFindByCidadeContainingIgnoreCase() {
            // Arrange
            String cidade = "paulo";
            Pageable pageable = PageRequest.of(0, 20);
            VeiculoQueryModel veiculo = createVeiculoQueryModelWithCidade("VEI-001", "São Paulo");
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(veiculo));

            when(extensions.findByCidadeContainingIgnoreCase(cidade, pageable)).thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = extensions.findByCidadeContainingIgnoreCase(cidade, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCidade()).containsIgnoringCase(cidade);
            verify(extensions).findByCidadeContainingIgnoreCase(cidade, pageable);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando nome não encontrado")
        void shouldReturnEmptyPageWhenNomeNotFound() {
            // Arrange
            String nome = "NomeInexistente";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoQueryModel> emptyPage = Page.empty();

            when(extensions.findByProprietarioNomeContainingIgnoreCase(nome, pageable)).thenReturn(emptyPage);

            // Act
            Page<VeiculoQueryModel> result = extensions.findByProprietarioNomeContainingIgnoreCase(nome, pageable);

            // Assert
            assertThat(result).isEmpty();
            verify(extensions).findByProprietarioNomeContainingIgnoreCase(nome, pageable);
        }
    }

    @Nested
    @DisplayName("Contadores Simples")
    class ContadoresSimples {

        @Test
        @DisplayName("Deve contar veículos por status")
        void shouldCountByStatus() {
            // Arrange
            StatusVeiculo status = StatusVeiculo.ATIVO;
            when(extensions.countByStatus(status)).thenReturn(150L);

            // Act
            long count = extensions.countByStatus(status);

            // Assert
            assertThat(count).isEqualTo(150L);
            verify(extensions).countByStatus(status);
        }

        @Test
        @DisplayName("Deve contar veículos com apólice ativa")
        void shouldCountByApoliceAtivaTrue() {
            // Arrange
            when(extensions.countByApoliceAtivaTrue()).thenReturn(85L);

            // Act
            long count = extensions.countByApoliceAtivaTrue();

            // Assert
            assertThat(count).isEqualTo(85L);
            verify(extensions).countByApoliceAtivaTrue();
        }

        @Test
        @DisplayName("Deve retornar zero quando não há veículos com apólice ativa")
        void shouldReturnZeroWhenNoVeiculosWithApoliceAtiva() {
            // Arrange
            when(extensions.countByApoliceAtivaTrue()).thenReturn(0L);

            // Act
            long count = extensions.countByApoliceAtivaTrue();

            // Assert
            assertThat(count).isZero();
            verify(extensions).countByApoliceAtivaTrue();
        }

        @Test
        @DisplayName("Deve contar veículos por diferentes status")
        void shouldCountByDifferentStatus() {
            // Arrange
            when(extensions.countByStatus(StatusVeiculo.ATIVO)).thenReturn(100L);
            when(extensions.countByStatus(StatusVeiculo.INATIVO)).thenReturn(25L);
            when(extensions.countByStatus(StatusVeiculo.BLOQUEADO)).thenReturn(5L);

            // Act & Assert
            assertThat(extensions.countByStatus(StatusVeiculo.ATIVO)).isEqualTo(100L);
            assertThat(extensions.countByStatus(StatusVeiculo.INATIVO)).isEqualTo(25L);
            assertThat(extensions.countByStatus(StatusVeiculo.BLOQUEADO)).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("Estatísticas por Agrupamento")
    class EstatisticasAgrupamento {

        @Test
        @DisplayName("Deve retornar estatísticas por estado")
        void shouldCountByEstado() {
            // Arrange
            List<Object[]> stats = List.of(
                new Object[]{"SP", 120L},
                new Object[]{"RJ", 80L},
                new Object[]{"MG", 50L}
            );

            when(extensions.countByEstado()).thenReturn(stats);

            // Act
            List<Object[]> result = extensions.countByEstado();

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result.get(0)[0]).isEqualTo("SP");
            assertThat(result.get(0)[1]).isEqualTo(120L);
            assertThat(result.get(1)[0]).isEqualTo("RJ");
            assertThat(result.get(1)[1]).isEqualTo(80L);
            verify(extensions).countByEstado();
        }

        @Test
        @DisplayName("Deve retornar estatísticas por marca")
        void shouldCountByMarca() {
            // Arrange
            List<Object[]> stats = List.of(
                new Object[]{"Toyota", 45L},
                new Object[]{"Honda", 38L},
                new Object[]{"Ford", 32L}
            );

            when(extensions.countByMarca()).thenReturn(stats);

            // Act
            List<Object[]> result = extensions.countByMarca();

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result.get(0)[0]).isEqualTo("Toyota");
            assertThat(result.get(0)[1]).isEqualTo(45L);
            verify(extensions).countByMarca();
        }

        @Test
        @DisplayName("Deve retornar estatísticas por categoria")
        void shouldCountByCategoria() {
            // Arrange
            List<Object[]> stats = List.of(
                new Object[]{"PASSEIO", 150L},
                new Object[]{"UTILITARIO", 45L},
                new Object[]{"CAMINHAO", 20L}
            );

            when(extensions.countByCategoria()).thenReturn(stats);

            // Act
            List<Object[]> result = extensions.countByCategoria();

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result.get(0)[0]).isEqualTo("PASSEIO");
            assertThat(result.get(0)[1]).isEqualTo(150L);
            verify(extensions).countByCategoria();
        }

        @Test
        @DisplayName("Deve retornar estatísticas por tipo de combustível")
        void shouldCountByTipoCombustivel() {
            // Arrange
            List<Object[]> stats = List.of(
                new Object[]{"GASOLINA", 80L},
                new Object[]{"FLEX", 70L},
                new Object[]{"DIESEL", 30L},
                new Object[]{"ELETRICO", 5L}
            );

            when(extensions.countByTipoCombustivel()).thenReturn(stats);

            // Act
            List<Object[]> result = extensions.countByTipoCombustivel();

            // Assert
            assertThat(result).hasSize(4);
            assertThat(result.get(0)[0]).isEqualTo("GASOLINA");
            assertThat(result.get(0)[1]).isEqualTo(80L);
            assertThat(result.get(3)[0]).isEqualTo("ELETRICO");
            assertThat(result.get(3)[1]).isEqualTo(5L);
            verify(extensions).countByTipoCombustivel();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há dados para agrupar")
        void shouldReturnEmptyListWhenNoDataToGroup() {
            // Arrange
            when(extensions.countByEstado()).thenReturn(List.of());

            // Act
            List<Object[]> result = extensions.countByEstado();

            // Assert
            assertThat(result).isEmpty();
            verify(extensions).countByEstado();
        }
    }

    @Nested
    @DisplayName("Estatísticas Avançadas")
    class EstatisticasAvancadas {

        @Test
        @DisplayName("Deve retornar estatísticas por faixa etária")
        void shouldCountByFaixaEtaria() {
            // Arrange
            List<Object[]> stats = List.of(
                new Object[]{"NOVO", 35L},
                new Object[]{"SEMINOVO", 85L},
                new Object[]{"USADO", 65L}
            );

            when(extensions.countByFaixaEtaria()).thenReturn(stats);

            // Act
            List<Object[]> result = extensions.countByFaixaEtaria();

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result.get(0)[0]).isEqualTo("NOVO");
            assertThat(result.get(0)[1]).isEqualTo(35L);
            assertThat(result.get(1)[0]).isEqualTo("SEMINOVO");
            assertThat(result.get(1)[1]).isEqualTo(85L);
            assertThat(result.get(2)[0]).isEqualTo("USADO");
            assertThat(result.get(2)[1]).isEqualTo(65L);
            verify(extensions).countByFaixaEtaria();
        }

        @Test
        @DisplayName("Deve retornar top marcas por região")
        void shouldGetTopMarcasPorRegiao() {
            // Arrange
            List<Object[]> stats = List.of(
                new Object[]{"SUDESTE", "Toyota", 45L},
                new Object[]{"SUDESTE", "Honda", 38L},
                new Object[]{"SUL", "Volkswagen", 52L},
                new Object[]{"NORDESTE", "Fiat", 41L}
            );

            when(extensions.getTopMarcasPorRegiao()).thenReturn(stats);

            // Act
            List<Object[]> result = extensions.getTopMarcasPorRegiao();

            // Assert
            assertThat(result).hasSize(4);
            assertThat(result.get(0)[0]).isEqualTo("SUDESTE");
            assertThat(result.get(0)[1]).isEqualTo("Toyota");
            assertThat(result.get(0)[2]).isEqualTo(45L);
            assertThat(result.get(2)[0]).isEqualTo("SUL");
            assertThat(result.get(2)[1]).isEqualTo("Volkswagen");
            verify(extensions).getTopMarcasPorRegiao();
        }

        @Test
        @DisplayName("Deve retornar veículos sem apólice por estado")
        void shouldCountVeiculosSemApolicePorEstado() {
            // Arrange
            List<Object[]> stats = List.of(
                new Object[]{"SP", 25L},
                new Object[]{"RJ", 18L},
                new Object[]{"MG", 12L}
            );

            when(extensions.countVeiculosSemApolicePorEstado()).thenReturn(stats);

            // Act
            List<Object[]> result = extensions.countVeiculosSemApolicePorEstado();

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result.get(0)[0]).isEqualTo("SP");
            assertThat(result.get(0)[1]).isEqualTo(25L);
            verify(extensions).countVeiculosSemApolicePorEstado();
        }

        @Test
        @DisplayName("Deve retornar estatísticas ordenadas por quantidade decrescente")
        void shouldReturnStatsOrderedByQuantityDesc() {
            // Arrange
            List<Object[]> stats = List.of(
                new Object[]{"SP", 100L},
                new Object[]{"RJ", 75L},
                new Object[]{"MG", 50L},
                new Object[]{"RS", 30L}
            );

            when(extensions.countByEstado()).thenReturn(stats);

            // Act
            List<Object[]> result = extensions.countByEstado();

            // Assert
            assertThat(result).hasSize(4);
            // Verifica que está ordenado decrescente
            Long previousCount = Long.MAX_VALUE;
            for (Object[] row : result) {
                Long currentCount = (Long) row[1];
                assertThat(currentCount).isLessThanOrEqualTo(previousCount);
                previousCount = currentCount;
            }
            verify(extensions).countByEstado();
        }
    }

    @Nested
    @DisplayName("Casos de Borda")
    class CasosDeBorda {

        @Test
        @DisplayName("Deve lidar com contagem zero por status")
        void shouldHandleZeroCountByStatus() {
            // Arrange
            StatusVeiculo status = StatusVeiculo.BLOQUEADO;
            when(extensions.countByStatus(status)).thenReturn(0L);

            // Act
            long count = extensions.countByStatus(status);

            // Assert
            assertThat(count).isZero();
            verify(extensions).countByStatus(status);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há veículos sem apólice")
        void shouldReturnEmptyListWhenNoVeiculosSemApolice() {
            // Arrange
            when(extensions.countVeiculosSemApolicePorEstado()).thenReturn(List.of());

            // Act
            List<Object[]> result = extensions.countVeiculosSemApolicePorEstado();

            // Assert
            assertThat(result).isEmpty();
            verify(extensions).countVeiculosSemApolicePorEstado();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há top marcas por região")
        void shouldReturnEmptyListWhenNoTopMarcasPorRegiao() {
            // Arrange
            when(extensions.getTopMarcasPorRegiao()).thenReturn(List.of());

            // Act
            List<Object[]> result = extensions.getTopMarcasPorRegiao();

            // Assert
            assertThat(result).isEmpty();
            verify(extensions).getTopMarcasPorRegiao();
        }

        @Test
        @DisplayName("Deve processar nome com caracteres especiais")
        void shouldHandleSpecialCharactersInNome() {
            // Arrange
            String nome = "José María";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoQueryModel> page = Page.empty();

            when(extensions.findByProprietarioNomeContainingIgnoreCase(nome, pageable)).thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = extensions.findByProprietarioNomeContainingIgnoreCase(nome, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(extensions).findByProprietarioNomeContainingIgnoreCase(nome, pageable);
        }

        @Test
        @DisplayName("Deve processar cidade com espaços e acentos")
        void shouldHandleCidadeWithSpacesAndAccents() {
            // Arrange
            String cidade = "São José dos Campos";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoQueryModel> page = Page.empty();

            when(extensions.findByCidadeContainingIgnoreCase(cidade, pageable)).thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = extensions.findByCidadeContainingIgnoreCase(cidade, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(extensions).findByCidadeContainingIgnoreCase(cidade, pageable);
        }
    }

    @Nested
    @DisplayName("Validação de Resultados de Agregação")
    class ValidacaoResultadosAgregacao {

        @Test
        @DisplayName("Deve validar estrutura de resultado de countByEstado")
        void shouldValidateCountByEstadoResultStructure() {
            // Arrange
            Object[] row1 = new Object[]{"SP", 100L};
            List<Object[]> stats = java.util.Collections.singletonList(row1);

            when(extensions.countByEstado()).thenReturn(stats);

            // Act
            List<Object[]> result = extensions.countByEstado();

            // Assert
            assertThat(result).isNotEmpty();
            Object[] row = result.get(0);
            assertThat(row).hasSize(2);
            assertThat(row[0]).isInstanceOf(String.class);
            assertThat(row[1]).isInstanceOf(Long.class);
            verify(extensions).countByEstado();
        }

        @Test
        @DisplayName("Deve validar estrutura de resultado de getTopMarcasPorRegiao")
        void shouldValidateGetTopMarcasPorRegiaoResultStructure() {
            // Arrange
            Object[] row1 = new Object[]{"SUDESTE", "Toyota", 45L};
            List<Object[]> stats = java.util.Collections.singletonList(row1);

            when(extensions.getTopMarcasPorRegiao()).thenReturn(stats);

            // Act
            List<Object[]> result = extensions.getTopMarcasPorRegiao();

            // Assert
            assertThat(result).isNotEmpty();
            Object[] row = result.get(0);
            assertThat(row).hasSize(3);
            assertThat(row[0]).isInstanceOf(String.class); // regiao
            assertThat(row[1]).isInstanceOf(String.class); // marca
            assertThat(row[2]).isInstanceOf(Long.class);   // quantidade
            verify(extensions).getTopMarcasPorRegiao();
        }

        @Test
        @DisplayName("Deve validar estrutura de resultado de countByFaixaEtaria")
        void shouldValidateCountByFaixaEtariaResultStructure() {
            // Arrange
            Object[] row1 = new Object[]{"NOVO", 35L};
            List<Object[]> stats = java.util.Collections.singletonList(row1);

            when(extensions.countByFaixaEtaria()).thenReturn(stats);

            // Act
            List<Object[]> result = extensions.countByFaixaEtaria();

            // Assert
            assertThat(result).isNotEmpty();
            Object[] row = result.get(0);
            assertThat(row).hasSize(2);
            assertThat(row[0]).isInstanceOf(String.class); // faixa_etaria
            assertThat(row[1]).isInstanceOf(Long.class);   // quantidade
            verify(extensions).countByFaixaEtaria();
        }
    }

    // ===== Helper Methods =====

    private VeiculoQueryModel createVeiculoQueryModelWithProprietario(String id, String proprietarioNome) {
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
        model.setPlaca("ABC1234");
        model.setStatus(StatusVeiculo.ATIVO);
        return model;
    }
}
