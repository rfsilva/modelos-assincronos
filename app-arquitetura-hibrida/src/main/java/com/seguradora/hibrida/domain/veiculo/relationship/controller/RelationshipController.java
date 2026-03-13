package com.seguradora.hibrida.domain.veiculo.relationship.controller;

import com.seguradora.hibrida.domain.veiculo.relationship.dto.DashboardRelacionamentosDTO;
import com.seguradora.hibrida.domain.veiculo.relationship.dto.HistoricoRelacionamentoDTO;
import com.seguradora.hibrida.domain.veiculo.relationship.dto.VeiculoSemCoberturaDTO;
import com.seguradora.hibrida.domain.veiculo.relationship.model.VeiculoApoliceRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.service.RelationshipQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller REST para consultas de relacionamentos Veículo-Apólice.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/relacionamentos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Relacionamentos", description = "APIs para gerenciar relacionamentos Veículo-Apólice")
public class RelationshipController {

    private final RelationshipQueryService queryService;

    /**
     * Retorna dashboard consolidado de relacionamentos.
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard de relacionamentos",
               description = "Retorna métricas consolidadas de relacionamentos Veículo-Apólice")
    public ResponseEntity<DashboardRelacionamentosDTO> getDashboard() {
        log.debug("GET /api/v1/relacionamentos/dashboard");

        DashboardRelacionamentosDTO dashboard = queryService.getDashboard();
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Lista veículos sem cobertura ativa.
     */
    @GetMapping("/sem-cobertura")
    @Operation(summary = "Veículos sem cobertura",
               description = "Lista todos os veículos que não possuem cobertura ativa")
    public ResponseEntity<List<VeiculoSemCoberturaDTO>> getVeiculosSemCobertura() {
        log.debug("GET /api/v1/relacionamentos/sem-cobertura");

        List<VeiculoSemCoberturaDTO> veiculos = queryService.getVeiculosSemCobertura();
        return ResponseEntity.ok(veiculos);
    }

    /**
     * Retorna histórico de relacionamentos de um veículo.
     */
    @GetMapping("/veiculo/{veiculoId}/historico")
    @Operation(summary = "Histórico de relacionamentos",
               description = "Retorna histórico completo de coberturas de um veículo")
    public ResponseEntity<List<HistoricoRelacionamentoDTO>> getHistoricoVeiculo(
            @PathVariable String veiculoId) {

        log.debug("GET /api/v1/relacionamentos/veiculo/{}/historico", veiculoId);

        List<HistoricoRelacionamentoDTO> historico = queryService.getHistoricoVeiculo(veiculoId);
        return ResponseEntity.ok(historico);
    }

    /**
     * Retorna relacionamentos ativos de um veículo.
     */
    @GetMapping("/veiculo/{veiculoId}/ativos")
    @Operation(summary = "Relacionamentos ativos de um veículo",
               description = "Retorna todas as coberturas ativas de um veículo")
    public ResponseEntity<List<VeiculoApoliceRelacionamento>> getRelacionamentosAtivos(
            @PathVariable String veiculoId) {

        log.debug("GET /api/v1/relacionamentos/veiculo/{}/ativos", veiculoId);

        List<VeiculoApoliceRelacionamento> relacionamentos =
            queryService.getRelacionamentosAtivosVeiculo(veiculoId);

        return ResponseEntity.ok(relacionamentos);
    }

    /**
     * Verifica se um veículo tem cobertura ativa.
     */
    @GetMapping("/veiculo/{veiculoId}/tem-cobertura")
    @Operation(summary = "Verificar cobertura ativa",
               description = "Verifica se um veículo possui cobertura ativa")
    public ResponseEntity<Boolean> temCoberturaAtiva(@PathVariable String veiculoId) {
        log.debug("GET /api/v1/relacionamentos/veiculo/{}/tem-cobertura", veiculoId);

        boolean temCobertura = queryService.temCoberturaAtiva(veiculoId);
        return ResponseEntity.ok(temCobertura);
    }

    /**
     * Verifica se um veículo estava coberto em uma data específica.
     */
    @GetMapping("/veiculo/{veiculoId}/coberto-em")
    @Operation(summary = "Verificar cobertura em data específica",
               description = "Verifica se um veículo possuía cobertura em uma data específica")
    public ResponseEntity<Boolean> estavaCobertoEm(
            @PathVariable String veiculoId,
            @RequestParam LocalDate data) {

        log.debug("GET /api/v1/relacionamentos/veiculo/{}/coberto-em?data={}", veiculoId, data);

        boolean estaCoberto = queryService.estaCoberto(veiculoId, data);
        return ResponseEntity.ok(estaCoberto);
    }
}
