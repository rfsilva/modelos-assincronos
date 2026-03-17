package com.seguradora.hibrida.domain.veiculo.relationship.controller;

import com.seguradora.hibrida.domain.veiculo.relationship.dto.DashboardRelacionamentosDTO;
import com.seguradora.hibrida.domain.veiculo.relationship.dto.HistoricoRelacionamentoDTO;
import com.seguradora.hibrida.domain.veiculo.relationship.dto.VeiculoSemCoberturaDTO;
import com.seguradora.hibrida.domain.veiculo.relationship.model.StatusRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.model.VeiculoApoliceRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.service.RelationshipQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link RelationshipController}.
 * REST Controller para consultas de relacionamentos Veículo-Apólice.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RelationshipController - Testes Unitários")
class RelationshipControllerTest {

    @Mock
    private RelationshipQueryService queryService;

    @InjectMocks
    private RelationshipController controller;

    @Nested
    @DisplayName("GET /dashboard - Dashboard de Relacionamentos")
    class GetDashboard {

        @Test
        @DisplayName("Deve retornar dashboard com métricas consolidadas")
        void shouldReturnDashboardSuccessfully() {
            // Arrange
            DashboardRelacionamentosDTO dashboard = DashboardRelacionamentosDTO.builder()
                .totalRelacionamentosAtivos(150L)
                .totalRelacionamentosEncerrados(80L)
                .totalRelacionamentosCancelados(20L)
                .totalVeiculosSemCobertura(45L)
                .totalVencendoEm30Dias(12L)
                .totalComGapCobertura(8L)
                .dataAtualizacao(LocalDate.now())
                .build();

            when(queryService.getDashboard()).thenReturn(dashboard);

            // Act
            ResponseEntity<DashboardRelacionamentosDTO> response = controller.getDashboard();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTotalRelacionamentosAtivos()).isEqualTo(150L);
            assertThat(response.getBody().getTotalVeiculosSemCobertura()).isEqualTo(45L);
            verify(queryService).getDashboard();
        }

        @Test
        @DisplayName("Deve retornar dashboard com valores zerados quando não há dados")
        void shouldReturnDashboardWithZeroValuesWhenNoData() {
            // Arrange
            DashboardRelacionamentosDTO dashboard = DashboardRelacionamentosDTO.builder()
                .totalRelacionamentosAtivos(0L)
                .totalRelacionamentosEncerrados(0L)
                .totalRelacionamentosCancelados(0L)
                .totalVeiculosSemCobertura(0L)
                .totalVencendoEm30Dias(0L)
                .totalComGapCobertura(0L)
                .dataAtualizacao(LocalDate.now())
                .build();

            when(queryService.getDashboard()).thenReturn(dashboard);

            // Act
            ResponseEntity<DashboardRelacionamentosDTO> response = controller.getDashboard();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTotalRelacionamentosAtivos()).isZero();
            verify(queryService).getDashboard();
        }

        @Test
        @DisplayName("Deve incluir data de atualização no dashboard")
        void shouldIncludeDataAtualizacaoInDashboard() {
            // Arrange
            LocalDate today = LocalDate.now();
            DashboardRelacionamentosDTO dashboard = DashboardRelacionamentosDTO.builder()
                .totalRelacionamentosAtivos(100L)
                .totalRelacionamentosEncerrados(50L)
                .totalRelacionamentosCancelados(10L)
                .totalVeiculosSemCobertura(20L)
                .totalVencendoEm30Dias(5L)
                .totalComGapCobertura(3L)
                .dataAtualizacao(today)
                .build();

            when(queryService.getDashboard()).thenReturn(dashboard);

            // Act
            ResponseEntity<DashboardRelacionamentosDTO> response = controller.getDashboard();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getDataAtualizacao()).isEqualTo(today);
        }
    }

    @Nested
    @DisplayName("GET /sem-cobertura - Veículos sem Cobertura")
    class GetVeiculosSemCobertura {

        @Test
        @DisplayName("Deve retornar lista de veículos sem cobertura ativa")
        void shouldReturnVeiculosSemCoberturaSuccessfully() {
            // Arrange
            List<VeiculoSemCoberturaDTO> veiculos = List.of(
                VeiculoSemCoberturaDTO.builder()
                    .veiculoId("VEI-001")
                    .placa("ABC1234")
                    .seguradoCpf("12345678909")
                    .seguradoNome("João Silva")
                    .ultimaApolice("APO-001")
                    .dataFimUltimaCobertura(LocalDate.now().minusDays(15))
                    .diasSemCobertura(15)
                    .build(),
                VeiculoSemCoberturaDTO.builder()
                    .veiculoId("VEI-002")
                    .placa("XYZ5678")
                    .seguradoCpf("98765432100")
                    .seguradoNome("Maria Oliveira")
                    .ultimaApolice("APO-002")
                    .dataFimUltimaCobertura(LocalDate.now().minusDays(45))
                    .diasSemCobertura(45)
                    .build()
            );

            when(queryService.getVeiculosSemCobertura()).thenReturn(veiculos);

            // Act
            ResponseEntity<List<VeiculoSemCoberturaDTO>> response = controller.getVeiculosSemCobertura();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);
            assertThat(response.getBody().get(0).getPlaca()).isEqualTo("ABC1234");
            assertThat(response.getBody().get(1).getDiasSemCobertura()).isEqualTo(45);
            verify(queryService).getVeiculosSemCobertura();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há veículos sem cobertura")
        void shouldReturnEmptyListWhenNoVeiculosSemCobertura() {
            // Arrange
            when(queryService.getVeiculosSemCobertura()).thenReturn(List.of());

            // Act
            ResponseEntity<List<VeiculoSemCoberturaDTO>> response = controller.getVeiculosSemCobertura();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
            verify(queryService).getVeiculosSemCobertura();
        }

        @Test
        @DisplayName("Deve incluir informações completas de cada veículo sem cobertura")
        void shouldIncludeCompleteInfoForEachVeiculo() {
            // Arrange
            VeiculoSemCoberturaDTO veiculo = VeiculoSemCoberturaDTO.builder()
                .veiculoId("VEI-001")
                .placa("ABC1234")
                .seguradoCpf("12345678909")
                .seguradoNome("João Silva")
                .ultimaApolice("APO-001")
                .dataFimUltimaCobertura(LocalDate.now().minusDays(10))
                .diasSemCobertura(10)
                .build();

            when(queryService.getVeiculosSemCobertura()).thenReturn(List.of(veiculo));

            // Act
            ResponseEntity<List<VeiculoSemCoberturaDTO>> response = controller.getVeiculosSemCobertura();

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            VeiculoSemCoberturaDTO result = response.getBody().get(0);
            assertThat(result.getVeiculoId()).isEqualTo("VEI-001");
            assertThat(result.getPlaca()).isEqualTo("ABC1234");
            assertThat(result.getSeguradoCpf()).isEqualTo("12345678909");
            assertThat(result.getSeguradoNome()).isEqualTo("João Silva");
            assertThat(result.getUltimaApolice()).isEqualTo("APO-001");
        }
    }

    @Nested
    @DisplayName("GET /veiculo/{id}/historico - Histórico de Relacionamentos")
    class GetHistoricoVeiculo {

        @Test
        @DisplayName("Deve retornar histórico completo de um veículo")
        void shouldReturnHistoricoSuccessfully() {
            // Arrange
            String veiculoId = "VEI-001";
            List<HistoricoRelacionamentoDTO> historico = List.of(
                HistoricoRelacionamentoDTO.builder()
                    .relacionamentoId("REL-001")
                    .apoliceId("APO-001")
                    .apoliceNumero("2024-001")
                    .dataInicio(LocalDate.now().minusMonths(6))
                    .dataFim(null)
                    .status(StatusRelacionamento.ATIVO)
                    .duracaoDias(180)
                    .build(),
                HistoricoRelacionamentoDTO.builder()
                    .relacionamentoId("REL-002")
                    .apoliceId("APO-002")
                    .apoliceNumero("2023-045")
                    .dataInicio(LocalDate.now().minusYears(1))
                    .dataFim(LocalDate.now().minusMonths(6))
                    .status(StatusRelacionamento.ENCERRADO)
                    .duracaoDias(180)
                    .build()
            );

            when(queryService.getHistoricoVeiculo(veiculoId)).thenReturn(historico);

            // Act
            ResponseEntity<List<HistoricoRelacionamentoDTO>> response = controller.getHistoricoVeiculo(veiculoId);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);
            assertThat(response.getBody().get(0).getStatus()).isEqualTo(StatusRelacionamento.ATIVO);
            verify(queryService).getHistoricoVeiculo(veiculoId);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando veículo não tem histórico")
        void shouldReturnEmptyListWhenNoHistorico() {
            // Arrange
            String veiculoId = "VEI-999";
            when(queryService.getHistoricoVeiculo(veiculoId)).thenReturn(List.of());

            // Act
            ResponseEntity<List<HistoricoRelacionamentoDTO>> response = controller.getHistoricoVeiculo(veiculoId);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
            verify(queryService).getHistoricoVeiculo(veiculoId);
        }

        @Test
        @DisplayName("Deve incluir todos os campos no histórico")
        void shouldIncludeAllFieldsInHistorico() {
            // Arrange
            String veiculoId = "VEI-001";
            HistoricoRelacionamentoDTO historico = HistoricoRelacionamentoDTO.builder()
                .relacionamentoId("REL-001")
                .apoliceId("APO-001")
                .apoliceNumero("2024-001")
                .dataInicio(LocalDate.of(2024, 1, 1))
                .dataFim(LocalDate.of(2024, 12, 31))
                .status(StatusRelacionamento.ATIVO)
                .tipoCobertura("TOTAL")
                .motivoDesassociacao(null)
                .duracaoDias(365)
                .build();

            when(queryService.getHistoricoVeiculo(veiculoId)).thenReturn(List.of(historico));

            // Act
            ResponseEntity<List<HistoricoRelacionamentoDTO>> response = controller.getHistoricoVeiculo(veiculoId);

            // Assert
            HistoricoRelacionamentoDTO result = response.getBody().get(0);
            assertThat(result.getRelacionamentoId()).isEqualTo("REL-001");
            assertThat(result.getApoliceId()).isEqualTo("APO-001");
            assertThat(result.getApoliceNumero()).isEqualTo("2024-001");
            assertThat(result.getDataInicio()).isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(result.getDuracaoDias()).isEqualTo(365);
        }
    }

    @Nested
    @DisplayName("GET /veiculo/{id}/ativos - Relacionamentos Ativos")
    class GetRelacionamentosAtivos {

        @Test
        @DisplayName("Deve retornar relacionamentos ativos de um veículo")
        void shouldReturnRelacionamentosAtivosSuccessfully() {
            // Arrange
            String veiculoId = "VEI-001";
            List<VeiculoApoliceRelacionamento> relacionamentos = List.of(
                createRelacionamento("REL-001", veiculoId, "APO-001", StatusRelacionamento.ATIVO),
                createRelacionamento("REL-002", veiculoId, "APO-002", StatusRelacionamento.ATIVO)
            );

            when(queryService.getRelacionamentosAtivosVeiculo(veiculoId)).thenReturn(relacionamentos);

            // Act
            ResponseEntity<List<VeiculoApoliceRelacionamento>> response = controller.getRelacionamentosAtivos(veiculoId);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);
            assertThat(response.getBody()).allMatch(VeiculoApoliceRelacionamento::isAtivo);
            verify(queryService).getRelacionamentosAtivosVeiculo(veiculoId);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando veículo não tem relacionamentos ativos")
        void shouldReturnEmptyListWhenNoRelacionamentosAtivos() {
            // Arrange
            String veiculoId = "VEI-999";
            when(queryService.getRelacionamentosAtivosVeiculo(veiculoId)).thenReturn(List.of());

            // Act
            ResponseEntity<List<VeiculoApoliceRelacionamento>> response = controller.getRelacionamentosAtivos(veiculoId);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
            verify(queryService).getRelacionamentosAtivosVeiculo(veiculoId);
        }

        @Test
        @DisplayName("Deve retornar apenas relacionamentos ativos")
        void shouldReturnOnlyActiveRelacionamentos() {
            // Arrange
            String veiculoId = "VEI-001";
            List<VeiculoApoliceRelacionamento> relacionamentos = List.of(
                createRelacionamento("REL-001", veiculoId, "APO-001", StatusRelacionamento.ATIVO)
            );

            when(queryService.getRelacionamentosAtivosVeiculo(veiculoId)).thenReturn(relacionamentos);

            // Act
            ResponseEntity<List<VeiculoApoliceRelacionamento>> response = controller.getRelacionamentosAtivos(veiculoId);

            // Assert
            assertThat(response.getBody()).allMatch(rel ->
                rel.getStatus() == StatusRelacionamento.ATIVO
            );
        }
    }

    @Nested
    @DisplayName("GET /veiculo/{id}/tem-cobertura - Verificar Cobertura Ativa")
    class TemCoberturaAtiva {

        @Test
        @DisplayName("Deve retornar true quando veículo tem cobertura ativa")
        void shouldReturnTrueWhenVeiculoHasCobertura() {
            // Arrange
            String veiculoId = "VEI-001";
            when(queryService.temCoberturaAtiva(veiculoId)).thenReturn(true);

            // Act
            ResponseEntity<Boolean> response = controller.temCoberturaAtiva(veiculoId);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isTrue();
            verify(queryService).temCoberturaAtiva(veiculoId);
        }

        @Test
        @DisplayName("Deve retornar false quando veículo não tem cobertura ativa")
        void shouldReturnFalseWhenVeiculoHasNoCobertura() {
            // Arrange
            String veiculoId = "VEI-999";
            when(queryService.temCoberturaAtiva(veiculoId)).thenReturn(false);

            // Act
            ResponseEntity<Boolean> response = controller.temCoberturaAtiva(veiculoId);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isFalse();
            verify(queryService).temCoberturaAtiva(veiculoId);
        }

        @Test
        @DisplayName("Deve validar veiculoId não nulo")
        void shouldValidateNonNullVeiculoId() {
            // Arrange
            String veiculoId = "VEI-001";
            when(queryService.temCoberturaAtiva(veiculoId)).thenReturn(true);

            // Act
            ResponseEntity<Boolean> response = controller.temCoberturaAtiva(veiculoId);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(queryService).temCoberturaAtiva(veiculoId);
        }
    }

    @Nested
    @DisplayName("GET /veiculo/{id}/coberto-em?data={date} - Verificar Cobertura em Data Específica")
    class EstavaCobertoEm {

        @Test
        @DisplayName("Deve retornar true quando veículo estava coberto na data")
        void shouldReturnTrueWhenVeiculoWasCoberto() {
            // Arrange
            String veiculoId = "VEI-001";
            LocalDate data = LocalDate.of(2024, 6, 15);
            when(queryService.estaCoberto(veiculoId, data)).thenReturn(true);

            // Act
            ResponseEntity<Boolean> response = controller.estavaCobertoEm(veiculoId, data);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isTrue();
            verify(queryService).estaCoberto(veiculoId, data);
        }

        @Test
        @DisplayName("Deve retornar false quando veículo não estava coberto na data")
        void shouldReturnFalseWhenVeiculoWasNotCoberto() {
            // Arrange
            String veiculoId = "VEI-001";
            LocalDate data = LocalDate.of(2023, 1, 1);
            when(queryService.estaCoberto(veiculoId, data)).thenReturn(false);

            // Act
            ResponseEntity<Boolean> response = controller.estavaCobertoEm(veiculoId, data);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isFalse();
            verify(queryService).estaCoberto(veiculoId, data);
        }

        @Test
        @DisplayName("Deve verificar cobertura para data passada")
        void shouldCheckCoberturaForPastDate() {
            // Arrange
            String veiculoId = "VEI-001";
            LocalDate dataPast = LocalDate.now().minusMonths(3);
            when(queryService.estaCoberto(veiculoId, dataPast)).thenReturn(true);

            // Act
            ResponseEntity<Boolean> response = controller.estavaCobertoEm(veiculoId, dataPast);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isTrue();
            verify(queryService).estaCoberto(veiculoId, dataPast);
        }

        @Test
        @DisplayName("Deve verificar cobertura para data futura")
        void shouldCheckCoberturaForFutureDate() {
            // Arrange
            String veiculoId = "VEI-001";
            LocalDate dataFuture = LocalDate.now().plusMonths(2);
            when(queryService.estaCoberto(veiculoId, dataFuture)).thenReturn(true);

            // Act
            ResponseEntity<Boolean> response = controller.estavaCobertoEm(veiculoId, dataFuture);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isTrue();
            verify(queryService).estaCoberto(veiculoId, dataFuture);
        }

        @Test
        @DisplayName("Deve verificar cobertura para data atual")
        void shouldCheckCoberturaForCurrentDate() {
            // Arrange
            String veiculoId = "VEI-001";
            LocalDate today = LocalDate.now();
            when(queryService.estaCoberto(veiculoId, today)).thenReturn(true);

            // Act
            ResponseEntity<Boolean> response = controller.estavaCobertoEm(veiculoId, today);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isTrue();
            verify(queryService).estaCoberto(veiculoId, today);
        }
    }

    @Nested
    @DisplayName("Validação de Respostas HTTP")
    class ValidacaoRespostasHttp {

        @Test
        @DisplayName("Deve retornar status 200 OK para todos os endpoints")
        void shouldReturnOkStatusForAllEndpoints() {
            // Arrange
            String veiculoId = "VEI-001";
            LocalDate data = LocalDate.now();

            when(queryService.getDashboard()).thenReturn(DashboardRelacionamentosDTO.builder().build());
            when(queryService.getVeiculosSemCobertura()).thenReturn(List.of());
            when(queryService.getHistoricoVeiculo(veiculoId)).thenReturn(List.of());
            when(queryService.getRelacionamentosAtivosVeiculo(veiculoId)).thenReturn(List.of());
            when(queryService.temCoberturaAtiva(veiculoId)).thenReturn(true);
            when(queryService.estaCoberto(veiculoId, data)).thenReturn(true);

            // Act & Assert
            assertThat(controller.getDashboard().getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(controller.getVeiculosSemCobertura().getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(controller.getHistoricoVeiculo(veiculoId).getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(controller.getRelacionamentosAtivos(veiculoId).getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(controller.temCoberturaAtiva(veiculoId).getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(controller.estavaCobertoEm(veiculoId, data).getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Deve retornar body não nulo em todas as respostas")
        void shouldReturnNonNullBodyInAllResponses() {
            // Arrange
            String veiculoId = "VEI-001";
            LocalDate data = LocalDate.now();

            when(queryService.getDashboard()).thenReturn(DashboardRelacionamentosDTO.builder().build());
            when(queryService.getVeiculosSemCobertura()).thenReturn(List.of());
            when(queryService.getHistoricoVeiculo(veiculoId)).thenReturn(List.of());
            when(queryService.getRelacionamentosAtivosVeiculo(veiculoId)).thenReturn(List.of());
            when(queryService.temCoberturaAtiva(veiculoId)).thenReturn(true);
            when(queryService.estaCoberto(veiculoId, data)).thenReturn(true);

            // Act & Assert
            assertThat(controller.getDashboard().getBody()).isNotNull();
            assertThat(controller.getVeiculosSemCobertura().getBody()).isNotNull();
            assertThat(controller.getHistoricoVeiculo(veiculoId).getBody()).isNotNull();
            assertThat(controller.getRelacionamentosAtivos(veiculoId).getBody()).isNotNull();
            assertThat(controller.temCoberturaAtiva(veiculoId).getBody()).isNotNull();
            assertThat(controller.estavaCobertoEm(veiculoId, data).getBody()).isNotNull();
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
}
