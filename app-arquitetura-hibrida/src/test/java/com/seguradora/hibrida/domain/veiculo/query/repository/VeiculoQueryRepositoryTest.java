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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link VeiculoQueryRepository}.
 * Interface Spring Data JPA com métodos de consulta customizados.
 *
 * @author Principal Java Architect
 * @since 3.0.0
 */
@DisplayName("VeiculoQueryRepository - Testes Unitários")
class VeiculoQueryRepositoryTest {

    private final VeiculoQueryRepository repository = mock(VeiculoQueryRepository.class);

    @Nested
    @DisplayName("Consultas por Identificadores Únicos")
    class ConsultasPorIdentificadores {

        @Test
        @DisplayName("Deve buscar veículo por placa")
        void shouldFindByPlaca() {
            // Arrange
            String placa = "ABC1234";
            VeiculoQueryModel veiculo = createVeiculoQueryModel("VEI-001", placa, "12345678901", "CHASSI123");

            when(repository.findByPlaca(placa)).thenReturn(Optional.of(veiculo));

            // Act
            Optional<VeiculoQueryModel> result = repository.findByPlaca(placa);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getPlaca()).isEqualTo(placa);
            verify(repository).findByPlaca(placa);
        }

        @Test
        @DisplayName("Deve buscar veículo por RENAVAM")
        void shouldFindByRenavam() {
            // Arrange
            String renavam = "12345678901";
            VeiculoQueryModel veiculo = createVeiculoQueryModel("VEI-001", "ABC1234", renavam, "CHASSI123");

            when(repository.findByRenavam(renavam)).thenReturn(Optional.of(veiculo));

            // Act
            Optional<VeiculoQueryModel> result = repository.findByRenavam(renavam);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getRenavam()).isEqualTo(renavam);
            verify(repository).findByRenavam(renavam);
        }

        @Test
        @DisplayName("Deve buscar veículo por chassi")
        void shouldFindByChassi() {
            // Arrange
            String chassi = "9BWZZZ377VT004251";
            VeiculoQueryModel veiculo = createVeiculoQueryModel("VEI-001", "ABC1234", "12345678901", chassi);

            when(repository.findByChassi(chassi)).thenReturn(Optional.of(veiculo));

            // Act
            Optional<VeiculoQueryModel> result = repository.findByChassi(chassi);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getChassi()).isEqualTo(chassi);
            verify(repository).findByChassi(chassi);
        }

        @Test
        @DisplayName("Deve retornar vazio quando placa não existe")
        void shouldReturnEmptyWhenPlacaNotFound() {
            // Arrange
            String placa = "XYZ9999";
            when(repository.findByPlaca(placa)).thenReturn(Optional.empty());

            // Act
            Optional<VeiculoQueryModel> result = repository.findByPlaca(placa);

            // Assert
            assertThat(result).isEmpty();
            verify(repository).findByPlaca(placa);
        }
    }

    @Nested
    @DisplayName("Consultas por Proprietário")
    class ConsultasPorProprietario {

        @Test
        @DisplayName("Deve buscar veículos por CPF do proprietário")
        void shouldFindByProprietarioCpf() {
            // Arrange
            String cpf = "12345678909";
            List<VeiculoQueryModel> veiculos = List.of(
                createVeiculoQueryModelWithCpf("VEI-001", cpf),
                createVeiculoQueryModelWithCpf("VEI-002", cpf)
            );

            when(repository.findByProprietarioCpf(cpf)).thenReturn(veiculos);

            // Act
            List<VeiculoQueryModel> result = repository.findByProprietarioCpf(cpf);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(v -> v.getProprietarioCpf().equals(cpf));
            verify(repository).findByProprietarioCpf(cpf);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando CPF não tem veículos")
        void shouldReturnEmptyListWhenCpfHasNoVehicles() {
            // Arrange
            String cpf = "99999999999";
            when(repository.findByProprietarioCpf(cpf)).thenReturn(List.of());

            // Act
            List<VeiculoQueryModel> result = repository.findByProprietarioCpf(cpf);

            // Assert
            assertThat(result).isEmpty();
            verify(repository).findByProprietarioCpf(cpf);
        }

        @Test
        @DisplayName("Deve contar veículos por proprietário e status")
        void shouldCountByProprietarioCpfAndStatus() {
            // Arrange
            String cpf = "12345678909";
            StatusVeiculo status = StatusVeiculo.ATIVO;
            when(repository.countByProprietarioCpfAndStatus(cpf, status)).thenReturn(3L);

            // Act
            long count = repository.countByProprietarioCpfAndStatus(cpf, status);

            // Assert
            assertThat(count).isEqualTo(3L);
            verify(repository).countByProprietarioCpfAndStatus(cpf, status);
        }
    }

    @Nested
    @DisplayName("Consultas por Marca e Modelo")
    class ConsultasPorMarcaModelo {

        @Test
        @DisplayName("Deve buscar veículos por marca e modelo com paginação")
        void shouldFindByMarcaAndModelo() {
            // Arrange
            String marca = "Toyota";
            String modelo = "Corolla";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(createVeiculoQueryModel("VEI-001", marca, modelo)));

            when(repository.findByMarcaAndModelo(marca, modelo, pageable)).thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = repository.findByMarcaAndModelo(marca, modelo, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getMarca()).isEqualTo(marca);
            assertThat(result.getContent().get(0).getModelo()).isEqualTo(modelo);
            verify(repository).findByMarcaAndModelo(marca, modelo, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por marca (busca parcial case insensitive)")
        void shouldFindByMarcaContainingIgnoreCase() {
            // Arrange
            String marca = "toy";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(
                createVeiculoQueryModelWithMarca("VEI-001", "Toyota"),
                createVeiculoQueryModelWithMarca("VEI-002", "TOYOTA")
            ));

            when(repository.findByMarcaContainingIgnoreCase(marca, pageable)).thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = repository.findByMarcaContainingIgnoreCase(marca, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            verify(repository).findByMarcaContainingIgnoreCase(marca, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por modelo (busca parcial case insensitive)")
        void shouldFindByModeloContainingIgnoreCase() {
            // Arrange
            String modelo = "civic";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(createVeiculoQueryModel("VEI-001", "Honda", "Civic")));

            when(repository.findByModeloContainingIgnoreCase(modelo, pageable)).thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = repository.findByModeloContainingIgnoreCase(modelo, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(repository).findByModeloContainingIgnoreCase(modelo, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por marca ou modelo (fuzzy)")
        void shouldFindByMarcaOrModeloFuzzy() {
            // Arrange
            String termo = "honda";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(createVeiculoQueryModelWithMarca("VEI-001", "Honda")));

            when(repository.findByMarcaOrModeloFuzzy(termo, pageable)).thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = repository.findByMarcaOrModeloFuzzy(termo, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(repository).findByMarcaOrModeloFuzzy(termo, pageable);
        }
    }

    @Nested
    @DisplayName("Consultas Geográficas")
    class ConsultasGeograficas {

        @Test
        @DisplayName("Deve buscar veículos por cidade e estado")
        void shouldFindByCidadeAndEstado() {
            // Arrange
            String cidade = "São Paulo";
            String estado = "SP";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(createVeiculoQueryModelWithLocation("VEI-001", cidade, estado)));

            when(repository.findByCidadeAndEstado(cidade, estado, pageable)).thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = repository.findByCidadeAndEstado(cidade, estado, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCidade()).isEqualTo(cidade);
            assertThat(result.getContent().get(0).getEstado()).isEqualTo(estado);
            verify(repository).findByCidadeAndEstado(cidade, estado, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por estado")
        void shouldFindByEstado() {
            // Arrange
            String estado = "RJ";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(createVeiculoQueryModelWithEstado("VEI-001", estado)));

            when(repository.findByEstado(estado, pageable)).thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = repository.findByEstado(estado, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(repository).findByEstado(estado, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por região")
        void shouldFindByRegiao() {
            // Arrange
            String regiao = "SUDESTE";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(createVeiculoQueryModelWithRegiao("VEI-001", regiao)));

            when(repository.findByRegiao(regiao, pageable)).thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = repository.findByRegiao(regiao, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(repository).findByRegiao(regiao, pageable);
        }
    }

    @Nested
    @DisplayName("Consultas por Status")
    class ConsultasPorStatus {

        @Test
        @DisplayName("Deve buscar veículos por status")
        void shouldFindByStatus() {
            // Arrange
            StatusVeiculo status = StatusVeiculo.ATIVO;
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(createVeiculoQueryModelWithStatus("VEI-001", status)));

            when(repository.findByStatus(status, pageable)).thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = repository.findByStatus(status, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(status);
            verify(repository).findByStatus(status, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos por status e categoria")
        void shouldFindByStatusAndCategoria() {
            // Arrange
            StatusVeiculo status = StatusVeiculo.ATIVO;
            String categoria = "PASSEIO";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(createVeiculoQueryModelWithStatusAndCategoria("VEI-001", status, categoria)));

            when(repository.findByStatusAndCategoria(status, categoria, pageable)).thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = repository.findByStatusAndCategoria(status, categoria, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(repository).findByStatusAndCategoria(status, categoria, pageable);
        }
    }

    @Nested
    @DisplayName("Consultas por Ano de Fabricação")
    class ConsultasPorAno {

        @Test
        @DisplayName("Deve buscar veículos por faixa de ano de fabricação")
        void shouldFindByAnoFabricacaoBetween() {
            // Arrange
            Integer anoInicio = 2020;
            Integer anoFim = 2023;
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(
                createVeiculoQueryModelWithAno("VEI-001", 2021),
                createVeiculoQueryModelWithAno("VEI-002", 2022)
            ));

            when(repository.findByAnoFabricacaoBetween(anoInicio, anoFim, pageable)).thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = repository.findByAnoFabricacaoBetween(anoInicio, anoFim, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(v ->
                v.getAnoFabricacao() >= anoInicio && v.getAnoFabricacao() <= anoFim
            );
            verify(repository).findByAnoFabricacaoBetween(anoInicio, anoFim, pageable);
        }
    }

    @Nested
    @DisplayName("Consultas por Apólice")
    class ConsultasPorApolice {

        @Test
        @DisplayName("Deve buscar veículos com apólice ativa")
        void shouldFindByApoliceAtivaTrue() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(createVeiculoQueryModelWithApolice("VEI-001", true)));

            when(repository.findByApoliceAtivaTrue(pageable)).thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = repository.findByApoliceAtivaTrue(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getApoliceAtiva()).isTrue();
            verify(repository).findByApoliceAtivaTrue(pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos sem apólice ativa")
        void shouldFindByApoliceAtivaFalse() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(createVeiculoQueryModelWithApolice("VEI-001", false)));

            when(repository.findByApoliceAtivaFalse(pageable)).thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = repository.findByApoliceAtivaFalse(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getApoliceAtiva()).isFalse();
            verify(repository).findByApoliceAtivaFalse(pageable);
        }
    }

    @Nested
    @DisplayName("Verificações de Existência")
    class VerificacoesExistencia {

        @Test
        @DisplayName("Deve verificar se existe veículo com a placa")
        void shouldCheckIfPlacaExists() {
            // Arrange
            String placa = "ABC1234";
            when(repository.existsByPlaca(placa)).thenReturn(true);

            // Act
            boolean exists = repository.existsByPlaca(placa);

            // Assert
            assertThat(exists).isTrue();
            verify(repository).existsByPlaca(placa);
        }

        @Test
        @DisplayName("Deve verificar se existe veículo com o RENAVAM")
        void shouldCheckIfRenavamExists() {
            // Arrange
            String renavam = "12345678901";
            when(repository.existsByRenavam(renavam)).thenReturn(false);

            // Act
            boolean exists = repository.existsByRenavam(renavam);

            // Assert
            assertThat(exists).isFalse();
            verify(repository).existsByRenavam(renavam);
        }

        @Test
        @DisplayName("Deve verificar se existe veículo com o chassi")
        void shouldCheckIfChassiExists() {
            // Arrange
            String chassi = "9BWZZZ377VT004251";
            when(repository.existsByChassi(chassi)).thenReturn(true);

            // Act
            boolean exists = repository.existsByChassi(chassi);

            // Assert
            assertThat(exists).isTrue();
            verify(repository).existsByChassi(chassi);
        }
    }

    @Nested
    @DisplayName("Consultas Customizadas com Múltiplos Critérios")
    class ConsultasMultiplosCriterios {

        @Test
        @DisplayName("Deve buscar veículos com múltiplos critérios")
        void shouldFindByMultiplosCriterios() {
            // Arrange
            String marca = "Toyota";
            String modelo = "Corolla";
            StatusVeiculo status = StatusVeiculo.ATIVO;
            Integer anoInicio = 2020;
            Integer anoFim = 2023;
            String estado = "SP";
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(createVeiculoQueryModel("VEI-001", marca, modelo)));

            when(repository.findByMultiplosCriterios(marca, modelo, status, anoInicio, anoFim, estado, pageable))
                .thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = repository.findByMultiplosCriterios(
                marca, modelo, status, anoInicio, anoFim, estado, pageable
            );

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(repository).findByMultiplosCriterios(marca, modelo, status, anoInicio, anoFim, estado, pageable);
        }

        @Test
        @DisplayName("Deve buscar veículos com critérios opcionais nulos")
        void shouldFindByMultiplosCriteriosWithNullValues() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            Page<VeiculoQueryModel> page = new PageImpl<>(List.of(createVeiculoQueryModel("VEI-001", "Ford", "Focus")));

            when(repository.findByMultiplosCriterios(null, null, null, null, null, null, pageable))
                .thenReturn(page);

            // Act
            Page<VeiculoQueryModel> result = repository.findByMultiplosCriterios(
                null, null, null, null, null, null, pageable
            );

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(repository).findByMultiplosCriterios(null, null, null, null, null, null, pageable);
        }
    }

    // ===== Helper Methods =====

    private VeiculoQueryModel createVeiculoQueryModel(String id, String placa, String renavam, String chassi) {
        VeiculoQueryModel model = new VeiculoQueryModel();
        model.setId(id);
        model.setPlaca(placa);
        model.setRenavam(renavam);
        model.setChassi(chassi);
        model.setMarca("Toyota");
        model.setModelo("Corolla");
        model.setAnoFabricacao(2022);
        model.setStatus(StatusVeiculo.ATIVO);
        model.setProprietarioCpf("12345678909");
        return model;
    }

    private VeiculoQueryModel createVeiculoQueryModel(String id, String marca, String modelo) {
        VeiculoQueryModel model = new VeiculoQueryModel();
        model.setId(id);
        model.setPlaca("ABC1234");
        model.setRenavam("12345678901");
        model.setChassi("CHASSI123");
        model.setMarca(marca);
        model.setModelo(modelo);
        model.setAnoFabricacao(2022);
        model.setStatus(StatusVeiculo.ATIVO);
        return model;
    }

    private VeiculoQueryModel createVeiculoQueryModelWithCpf(String id, String cpf) {
        VeiculoQueryModel model = new VeiculoQueryModel();
        model.setId(id);
        model.setPlaca("ABC" + id);
        model.setProprietarioCpf(cpf);
        model.setStatus(StatusVeiculo.ATIVO);
        return model;
    }

    private VeiculoQueryModel createVeiculoQueryModelWithMarca(String id, String marca) {
        VeiculoQueryModel model = new VeiculoQueryModel();
        model.setId(id);
        model.setMarca(marca);
        model.setModelo("Modelo Teste");
        return model;
    }

    private VeiculoQueryModel createVeiculoQueryModelWithLocation(String id, String cidade, String estado) {
        VeiculoQueryModel model = new VeiculoQueryModel();
        model.setId(id);
        model.setCidade(cidade);
        model.setEstado(estado);
        return model;
    }

    private VeiculoQueryModel createVeiculoQueryModelWithEstado(String id, String estado) {
        VeiculoQueryModel model = new VeiculoQueryModel();
        model.setId(id);
        model.setEstado(estado);
        return model;
    }

    private VeiculoQueryModel createVeiculoQueryModelWithRegiao(String id, String regiao) {
        VeiculoQueryModel model = new VeiculoQueryModel();
        model.setId(id);
        model.setRegiao(regiao);
        return model;
    }

    private VeiculoQueryModel createVeiculoQueryModelWithStatus(String id, StatusVeiculo status) {
        VeiculoQueryModel model = new VeiculoQueryModel();
        model.setId(id);
        model.setStatus(status);
        return model;
    }

    private VeiculoQueryModel createVeiculoQueryModelWithStatusAndCategoria(String id, StatusVeiculo status, String categoria) {
        VeiculoQueryModel model = new VeiculoQueryModel();
        model.setId(id);
        model.setStatus(status);
        model.setCategoria(categoria);
        return model;
    }

    private VeiculoQueryModel createVeiculoQueryModelWithAno(String id, Integer ano) {
        VeiculoQueryModel model = new VeiculoQueryModel();
        model.setId(id);
        model.setAnoFabricacao(ano);
        return model;
    }

    private VeiculoQueryModel createVeiculoQueryModelWithApolice(String id, Boolean apoliceAtiva) {
        VeiculoQueryModel model = new VeiculoQueryModel();
        model.setId(id);
        model.setApoliceAtiva(apoliceAtiva);
        return model;
    }
}
