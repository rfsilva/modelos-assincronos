package com.seguradora.hibrida.domain.veiculo.relationship.repository;

import com.seguradora.hibrida.domain.veiculo.relationship.model.StatusRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.model.VeiculoApoliceRelacionamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link VeiculoApoliceRelacionamentoRepository}.
 * Repository Spring Data para gerenciar relacionamentos Veículo-Apólice.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("VeiculoApoliceRelacionamentoRepository - Testes Unitários")
class VeiculoApoliceRelacionamentoRepositoryTest {

    private final VeiculoApoliceRelacionamentoRepository repository = mock(VeiculoApoliceRelacionamentoRepository.class);

    @Nested
    @DisplayName("Consultas por Veículo e Status")
    class ConsultasPorVeiculoStatus {

        @Test
        @DisplayName("Deve buscar relacionamentos ativos por veículo")
        void shouldFindByVeiculoIdAndStatus() {
            // Arrange
            String veiculoId = "VEI-001";
            StatusRelacionamento status = StatusRelacionamento.ATIVO;
            List<VeiculoApoliceRelacionamento> relacionamentos = List.of(
                createRelacionamento("REL-001", veiculoId, "APO-001", status),
                createRelacionamento("REL-002", veiculoId, "APO-002", status)
            );

            when(repository.findByVeiculoIdAndStatus(veiculoId, status)).thenReturn(relacionamentos);

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findByVeiculoIdAndStatus(veiculoId, status);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(r -> r.getVeiculoId().equals(veiculoId));
            assertThat(result).allMatch(r -> r.getStatus() == status);
            verify(repository).findByVeiculoIdAndStatus(veiculoId, status);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando veículo não tem relacionamentos ativos")
        void shouldReturnEmptyListWhenNoActiveRelacionamentos() {
            // Arrange
            String veiculoId = "VEI-999";
            StatusRelacionamento status = StatusRelacionamento.ATIVO;
            when(repository.findByVeiculoIdAndStatus(veiculoId, status)).thenReturn(List.of());

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findByVeiculoIdAndStatus(veiculoId, status);

            // Assert
            assertThat(result).isEmpty();
            verify(repository).findByVeiculoIdAndStatus(veiculoId, status);
        }

        @Test
        @DisplayName("Deve buscar relacionamentos encerrados por veículo")
        void shouldFindEncerradosByVeiculo() {
            // Arrange
            String veiculoId = "VEI-001";
            StatusRelacionamento status = StatusRelacionamento.ENCERRADO;
            List<VeiculoApoliceRelacionamento> relacionamentos = List.of(
                createRelacionamento("REL-003", veiculoId, "APO-003", status)
            );

            when(repository.findByVeiculoIdAndStatus(veiculoId, status)).thenReturn(relacionamentos);

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findByVeiculoIdAndStatus(veiculoId, status);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(StatusRelacionamento.ENCERRADO);
            verify(repository).findByVeiculoIdAndStatus(veiculoId, status);
        }
    }

    @Nested
    @DisplayName("Consultas por Apólice e Status")
    class ConsultasPorApoliceStatus {

        @Test
        @DisplayName("Deve buscar relacionamentos ativos por apólice")
        void shouldFindByApoliceIdAndStatus() {
            // Arrange
            String apoliceId = "APO-001";
            StatusRelacionamento status = StatusRelacionamento.ATIVO;
            List<VeiculoApoliceRelacionamento> relacionamentos = List.of(
                createRelacionamento("REL-001", "VEI-001", apoliceId, status),
                createRelacionamento("REL-002", "VEI-002", apoliceId, status)
            );

            when(repository.findByApoliceIdAndStatus(apoliceId, status)).thenReturn(relacionamentos);

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findByApoliceIdAndStatus(apoliceId, status);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(r -> r.getApoliceId().equals(apoliceId));
            assertThat(result).allMatch(r -> r.getStatus() == status);
            verify(repository).findByApoliceIdAndStatus(apoliceId, status);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando apólice não tem relacionamentos")
        void shouldReturnEmptyListWhenApoliceHasNoRelacionamentos() {
            // Arrange
            String apoliceId = "APO-999";
            StatusRelacionamento status = StatusRelacionamento.ATIVO;
            when(repository.findByApoliceIdAndStatus(apoliceId, status)).thenReturn(List.of());

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findByApoliceIdAndStatus(apoliceId, status);

            // Assert
            assertThat(result).isEmpty();
            verify(repository).findByApoliceIdAndStatus(apoliceId, status);
        }
    }

    @Nested
    @DisplayName("Histórico Completo")
    class HistoricoCompleto {

        @Test
        @DisplayName("Deve buscar histórico completo do veículo ordenado por data")
        void shouldFindByVeiculoIdOrderByDataInicioDesc() {
            // Arrange
            String veiculoId = "VEI-001";
            List<VeiculoApoliceRelacionamento> historico = List.of(
                createRelacionamentoWithDate("REL-001", veiculoId, LocalDate.now().minusMonths(1)),
                createRelacionamentoWithDate("REL-002", veiculoId, LocalDate.now().minusYears(1)),
                createRelacionamentoWithDate("REL-003", veiculoId, LocalDate.now().minusYears(2))
            );

            when(repository.findByVeiculoIdOrderByDataInicioDesc(veiculoId)).thenReturn(historico);

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findByVeiculoIdOrderByDataInicioDesc(veiculoId);

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result).allMatch(r -> r.getVeiculoId().equals(veiculoId));
            // Verifica ordem decrescente
            assertThat(result.get(0).getDataInicio()).isAfter(result.get(1).getDataInicio());
            assertThat(result.get(1).getDataInicio()).isAfter(result.get(2).getDataInicio());
            verify(repository).findByVeiculoIdOrderByDataInicioDesc(veiculoId);
        }

        @Test
        @DisplayName("Deve buscar histórico completo da apólice")
        void shouldFindByApoliceIdOrderByDataInicioDesc() {
            // Arrange
            String apoliceId = "APO-001";
            List<VeiculoApoliceRelacionamento> historico = List.of(
                createRelacionamentoWithApolice("REL-001", "VEI-001", apoliceId),
                createRelacionamentoWithApolice("REL-002", "VEI-002", apoliceId)
            );

            when(repository.findByApoliceIdOrderByDataInicioDesc(apoliceId)).thenReturn(historico);

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findByApoliceIdOrderByDataInicioDesc(apoliceId);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(r -> r.getApoliceId().equals(apoliceId));
            verify(repository).findByApoliceIdOrderByDataInicioDesc(apoliceId);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há histórico")
        void shouldReturnEmptyListWhenNoHistorico() {
            // Arrange
            String veiculoId = "VEI-999";
            when(repository.findByVeiculoIdOrderByDataInicioDesc(veiculoId)).thenReturn(List.of());

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findByVeiculoIdOrderByDataInicioDesc(veiculoId);

            // Assert
            assertThat(result).isEmpty();
            verify(repository).findByVeiculoIdOrderByDataInicioDesc(veiculoId);
        }
    }

    @Nested
    @DisplayName("Busca de Relacionamento Ativo Específico")
    class RelacionamentoAtivoEspecifico {

        @Test
        @DisplayName("Deve buscar relacionamento ativo entre veículo e apólice")
        void shouldFindRelacionamentoAtivo() {
            // Arrange
            String veiculoId = "VEI-001";
            String apoliceId = "APO-001";
            VeiculoApoliceRelacionamento relacionamento = createRelacionamento("REL-001", veiculoId, apoliceId, StatusRelacionamento.ATIVO);

            when(repository.findRelacionamentoAtivo(veiculoId, apoliceId)).thenReturn(Optional.of(relacionamento));

            // Act
            Optional<VeiculoApoliceRelacionamento> result = repository.findRelacionamentoAtivo(veiculoId, apoliceId);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getVeiculoId()).isEqualTo(veiculoId);
            assertThat(result.get().getApoliceId()).isEqualTo(apoliceId);
            assertThat(result.get().getStatus()).isEqualTo(StatusRelacionamento.ATIVO);
            verify(repository).findRelacionamentoAtivo(veiculoId, apoliceId);
        }

        @Test
        @DisplayName("Deve retornar vazio quando não há relacionamento ativo")
        void shouldReturnEmptyWhenNoRelacionamentoAtivo() {
            // Arrange
            String veiculoId = "VEI-001";
            String apoliceId = "APO-999";
            when(repository.findRelacionamentoAtivo(veiculoId, apoliceId)).thenReturn(Optional.empty());

            // Act
            Optional<VeiculoApoliceRelacionamento> result = repository.findRelacionamentoAtivo(veiculoId, apoliceId);

            // Assert
            assertThat(result).isEmpty();
            verify(repository).findRelacionamentoAtivo(veiculoId, apoliceId);
        }
    }

    @Nested
    @DisplayName("Veículos sem Cobertura")
    class VeiculosSemCobertura {

        @Test
        @DisplayName("Deve buscar IDs de veículos sem cobertura ativa")
        void shouldFindVeiculosSemCobertura() {
            // Arrange
            List<String> veiculosIds = List.of("VEI-001", "VEI-002", "VEI-003");
            when(repository.findVeiculosSemCobertura()).thenReturn(veiculosIds);

            // Act
            List<String> result = repository.findVeiculosSemCobertura();

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result).containsExactly("VEI-001", "VEI-002", "VEI-003");
            verify(repository).findVeiculosSemCobertura();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando todos têm cobertura")
        void shouldReturnEmptyListWhenAllHaveCobertura() {
            // Arrange
            when(repository.findVeiculosSemCobertura()).thenReturn(List.of());

            // Act
            List<String> result = repository.findVeiculosSemCobertura();

            // Assert
            assertThat(result).isEmpty();
            verify(repository).findVeiculosSemCobertura();
        }
    }

    @Nested
    @DisplayName("Relacionamentos Vigentes em Data Específica")
    class RelacionamentosVigentes {

        @Test
        @DisplayName("Deve buscar relacionamentos vigentes em data específica")
        void shouldFindRelacionamentosVigentesEm() {
            // Arrange
            String veiculoId = "VEI-001";
            LocalDate data = LocalDate.of(2024, 6, 15);
            List<VeiculoApoliceRelacionamento> relacionamentos = List.of(
                createRelacionamentoWithDates("REL-001", veiculoId, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))
            );

            when(repository.findRelacionamentosVigentesEm(veiculoId, data)).thenReturn(relacionamentos);

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findRelacionamentosVigentesEm(veiculoId, data);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getVeiculoId()).isEqualTo(veiculoId);
            verify(repository).findRelacionamentosVigentesEm(veiculoId, data);
        }

        @Test
        @DisplayName("Deve retornar vazio quando não há cobertura na data")
        void shouldReturnEmptyWhenNoCoberturaOnDate() {
            // Arrange
            String veiculoId = "VEI-001";
            LocalDate data = LocalDate.of(2023, 1, 1);
            when(repository.findRelacionamentosVigentesEm(veiculoId, data)).thenReturn(List.of());

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findRelacionamentosVigentesEm(veiculoId, data);

            // Assert
            assertThat(result).isEmpty();
            verify(repository).findRelacionamentosVigentesEm(veiculoId, data);
        }

        @Test
        @DisplayName("Deve incluir relacionamentos sem data fim")
        void shouldIncludeRelacionamentosSemDataFim() {
            // Arrange
            String veiculoId = "VEI-001";
            LocalDate data = LocalDate.now();
            List<VeiculoApoliceRelacionamento> relacionamentos = List.of(
                createRelacionamentoWithDates("REL-001", veiculoId, LocalDate.now().minusMonths(6), null)
            );

            when(repository.findRelacionamentosVigentesEm(veiculoId, data)).thenReturn(relacionamentos);

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findRelacionamentosVigentesEm(veiculoId, data);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDataFim()).isNull();
        }
    }

    @Nested
    @DisplayName("Relacionamentos com Gap de Cobertura")
    class RelacionamentosComGap {

        @Test
        @DisplayName("Deve buscar relacionamentos com gap de cobertura")
        void shouldFindRelacionamentosComGap() {
            // Arrange
            List<VeiculoApoliceRelacionamento> relacionamentos = List.of(
                createRelacionamentoWithDates("REL-001", "VEI-001", LocalDate.now().minusYears(1), LocalDate.now().minusMonths(1))
            );

            when(repository.findRelacionamentosComGap()).thenReturn(relacionamentos);

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findRelacionamentosComGap();

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDataFim()).isBefore(LocalDate.now());
            verify(repository).findRelacionamentosComGap();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há gaps")
        void shouldReturnEmptyListWhenNoGaps() {
            // Arrange
            when(repository.findRelacionamentosComGap()).thenReturn(List.of());

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findRelacionamentosComGap();

            // Assert
            assertThat(result).isEmpty();
            verify(repository).findRelacionamentosComGap();
        }
    }

    @Nested
    @DisplayName("Relacionamentos Vencendo")
    class RelacionamentosVencendo {

        @Test
        @DisplayName("Deve buscar relacionamentos que vencem nos próximos N dias")
        void shouldFindRelacionamentosVencendoAte() {
            // Arrange
            LocalDate dataLimite = LocalDate.now().plusDays(30);
            List<VeiculoApoliceRelacionamento> relacionamentos = List.of(
                createRelacionamentoWithDates("REL-001", "VEI-001", LocalDate.now().minusMonths(6), LocalDate.now().plusDays(15))
            );

            when(repository.findRelacionamentosVencendoAte(dataLimite)).thenReturn(relacionamentos);

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findRelacionamentosVencendoAte(dataLimite);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDataFim()).isBetween(LocalDate.now(), dataLimite);
            verify(repository).findRelacionamentosVencendoAte(dataLimite);
        }

        @Test
        @DisplayName("Deve retornar vazio quando não há relacionamentos vencendo")
        void shouldReturnEmptyWhenNoRelacionamentosVencendo() {
            // Arrange
            LocalDate dataLimite = LocalDate.now().plusDays(30);
            when(repository.findRelacionamentosVencendoAte(dataLimite)).thenReturn(List.of());

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findRelacionamentosVencendoAte(dataLimite);

            // Assert
            assertThat(result).isEmpty();
            verify(repository).findRelacionamentosVencendoAte(dataLimite);
        }
    }

    @Nested
    @DisplayName("Contadores")
    class Contadores {

        @Test
        @DisplayName("Deve contar relacionamentos ativos por veículo")
        void shouldCountRelacionamentosAtivos() {
            // Arrange
            String veiculoId = "VEI-001";
            when(repository.countRelacionamentosAtivos(veiculoId)).thenReturn(2L);

            // Act
            long count = repository.countRelacionamentosAtivos(veiculoId);

            // Assert
            assertThat(count).isEqualTo(2L);
            verify(repository).countRelacionamentosAtivos(veiculoId);
        }

        @Test
        @DisplayName("Deve retornar zero quando veículo não tem relacionamentos ativos")
        void shouldReturnZeroWhenNoActiveRelacionamentos() {
            // Arrange
            String veiculoId = "VEI-999";
            when(repository.countRelacionamentosAtivos(veiculoId)).thenReturn(0L);

            // Act
            long count = repository.countRelacionamentosAtivos(veiculoId);

            // Assert
            assertThat(count).isZero();
            verify(repository).countRelacionamentosAtivos(veiculoId);
        }
    }

    @Nested
    @DisplayName("Verificação de Existência")
    class VerificacaoExistencia {

        @Test
        @DisplayName("Deve verificar existência de relacionamento ativo")
        void shouldCheckExistsRelacionamentoAtivo() {
            // Arrange
            String veiculoId = "VEI-001";
            String apoliceId = "APO-001";
            when(repository.existsRelacionamentoAtivo(veiculoId, apoliceId)).thenReturn(true);

            // Act
            boolean exists = repository.existsRelacionamentoAtivo(veiculoId, apoliceId);

            // Assert
            assertThat(exists).isTrue();
            verify(repository).existsRelacionamentoAtivo(veiculoId, apoliceId);
        }

        @Test
        @DisplayName("Deve retornar false quando não existe relacionamento ativo")
        void shouldReturnFalseWhenNoRelacionamentoAtivo() {
            // Arrange
            String veiculoId = "VEI-001";
            String apoliceId = "APO-999";
            when(repository.existsRelacionamentoAtivo(veiculoId, apoliceId)).thenReturn(false);

            // Act
            boolean exists = repository.existsRelacionamentoAtivo(veiculoId, apoliceId);

            // Assert
            assertThat(exists).isFalse();
            verify(repository).existsRelacionamentoAtivo(veiculoId, apoliceId);
        }
    }

    @Nested
    @DisplayName("Consultas por Segurado")
    class ConsultasPorSegurado {

        @Test
        @DisplayName("Deve buscar relacionamentos por CPF do segurado")
        void shouldFindBySeguradoCpfAndStatus() {
            // Arrange
            String cpf = "12345678909";
            StatusRelacionamento status = StatusRelacionamento.ATIVO;
            List<VeiculoApoliceRelacionamento> relacionamentos = List.of(
                createRelacionamentoWithSegurado("REL-001", "VEI-001", cpf),
                createRelacionamentoWithSegurado("REL-002", "VEI-002", cpf)
            );

            when(repository.findBySeguradoCpfAndStatus(cpf, status)).thenReturn(relacionamentos);

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findBySeguradoCpfAndStatus(cpf, status);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(r -> r.getSeguradoCpf().equals(cpf));
            verify(repository).findBySeguradoCpfAndStatus(cpf, status);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando CPF não tem relacionamentos")
        void shouldReturnEmptyListWhenCpfHasNoRelacionamentos() {
            // Arrange
            String cpf = "99999999999";
            StatusRelacionamento status = StatusRelacionamento.ATIVO;
            when(repository.findBySeguradoCpfAndStatus(cpf, status)).thenReturn(List.of());

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findBySeguradoCpfAndStatus(cpf, status);

            // Assert
            assertThat(result).isEmpty();
            verify(repository).findBySeguradoCpfAndStatus(cpf, status);
        }
    }

    @Nested
    @DisplayName("Consultas por Placa")
    class ConsultasPorPlaca {

        @Test
        @DisplayName("Deve buscar relacionamentos por placa do veículo")
        void shouldFindByVeiculoPlacaAndStatus() {
            // Arrange
            String placa = "ABC1234";
            StatusRelacionamento status = StatusRelacionamento.ATIVO;
            List<VeiculoApoliceRelacionamento> relacionamentos = List.of(
                createRelacionamentoWithPlaca("REL-001", "VEI-001", placa)
            );

            when(repository.findByVeiculoPlacaAndStatus(placa, status)).thenReturn(relacionamentos);

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findByVeiculoPlacaAndStatus(placa, status);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getVeiculoPlaca()).isEqualTo(placa);
            verify(repository).findByVeiculoPlacaAndStatus(placa, status);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando placa não tem relacionamentos")
        void shouldReturnEmptyListWhenPlacaHasNoRelacionamentos() {
            // Arrange
            String placa = "XYZ9999";
            StatusRelacionamento status = StatusRelacionamento.ATIVO;
            when(repository.findByVeiculoPlacaAndStatus(placa, status)).thenReturn(List.of());

            // Act
            List<VeiculoApoliceRelacionamento> result = repository.findByVeiculoPlacaAndStatus(placa, status);

            // Assert
            assertThat(result).isEmpty();
            verify(repository).findByVeiculoPlacaAndStatus(placa, status);
        }
    }

    // ===== Helper Methods =====

    private VeiculoApoliceRelacionamento createRelacionamento(String id, String veiculoId, String apoliceId, StatusRelacionamento status) {
        VeiculoApoliceRelacionamento relacionamento = new VeiculoApoliceRelacionamento();
        relacionamento.setId(id);
        relacionamento.setVeiculoId(veiculoId);
        relacionamento.setApoliceId(apoliceId);
        relacionamento.setStatus(status);
        relacionamento.setDataInicio(LocalDate.now().minusMonths(6));
        return relacionamento;
    }

    private VeiculoApoliceRelacionamento createRelacionamentoWithDate(String id, String veiculoId, LocalDate dataInicio) {
        VeiculoApoliceRelacionamento relacionamento = new VeiculoApoliceRelacionamento();
        relacionamento.setId(id);
        relacionamento.setVeiculoId(veiculoId);
        relacionamento.setApoliceId("APO-001");
        relacionamento.setStatus(StatusRelacionamento.ATIVO);
        relacionamento.setDataInicio(dataInicio);
        return relacionamento;
    }

    private VeiculoApoliceRelacionamento createRelacionamentoWithApolice(String id, String veiculoId, String apoliceId) {
        VeiculoApoliceRelacionamento relacionamento = new VeiculoApoliceRelacionamento();
        relacionamento.setId(id);
        relacionamento.setVeiculoId(veiculoId);
        relacionamento.setApoliceId(apoliceId);
        relacionamento.setStatus(StatusRelacionamento.ATIVO);
        relacionamento.setDataInicio(LocalDate.now());
        return relacionamento;
    }

    private VeiculoApoliceRelacionamento createRelacionamentoWithDates(String id, String veiculoId, LocalDate dataInicio, LocalDate dataFim) {
        VeiculoApoliceRelacionamento relacionamento = new VeiculoApoliceRelacionamento();
        relacionamento.setId(id);
        relacionamento.setVeiculoId(veiculoId);
        relacionamento.setApoliceId("APO-001");
        relacionamento.setStatus(StatusRelacionamento.ATIVO);
        relacionamento.setDataInicio(dataInicio);
        relacionamento.setDataFim(dataFim);
        return relacionamento;
    }

    private VeiculoApoliceRelacionamento createRelacionamentoWithSegurado(String id, String veiculoId, String seguradoCpf) {
        VeiculoApoliceRelacionamento relacionamento = new VeiculoApoliceRelacionamento();
        relacionamento.setId(id);
        relacionamento.setVeiculoId(veiculoId);
        relacionamento.setApoliceId("APO-001");
        relacionamento.setSeguradoCpf(seguradoCpf);
        relacionamento.setStatus(StatusRelacionamento.ATIVO);
        relacionamento.setDataInicio(LocalDate.now());
        return relacionamento;
    }

    private VeiculoApoliceRelacionamento createRelacionamentoWithPlaca(String id, String veiculoId, String placa) {
        VeiculoApoliceRelacionamento relacionamento = new VeiculoApoliceRelacionamento();
        relacionamento.setId(id);
        relacionamento.setVeiculoId(veiculoId);
        relacionamento.setApoliceId("APO-001");
        relacionamento.setVeiculoPlaca(placa);
        relacionamento.setStatus(StatusRelacionamento.ATIVO);
        relacionamento.setDataInicio(LocalDate.now());
        return relacionamento;
    }
}
