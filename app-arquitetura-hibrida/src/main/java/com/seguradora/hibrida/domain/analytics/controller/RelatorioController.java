package com.seguradora.hibrida.domain.analytics.controller;

import com.seguradora.hibrida.domain.analytics.dto.*;
import com.seguradora.hibrida.domain.analytics.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller REST para relatórios analíticos.
 * 
 * <p>Fornece endpoints para geração de relatórios e dashboards
 * baseados nas projeções analíticas do sistema.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/relatorios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Relatórios", description = "APIs para geração de relatórios analíticos")
public class RelatorioController {
    
    private final RelatorioService relatorioService;
    
    /**
     * Obtém dashboard executivo com métricas principais.
     */
    @GetMapping("/dashboard")
    @Operation(
        summary = "Dashboard Executivo",
        description = "Obtém dashboard executivo com métricas principais do negócio para uma data específica"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dashboard obtido com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<DashboardExecutivoView> obterDashboardExecutivo(
            @Parameter(description = "Data de referência (padrão: hoje)", example = "2024-12-19")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        
        if (data == null) {
            data = LocalDate.now();
        }
        
        log.info("Obtendo dashboard executivo para {}", data);
        
        DashboardExecutivoView dashboard = relatorioService.obterDashboardExecutivo(data);
        return ResponseEntity.ok(dashboard);
    }
    
    /**
     * Obtém relatório de segurados por período.
     */
    @GetMapping("/segurados")
    @Operation(
        summary = "Relatório de Segurados",
        description = "Obtém relatório detalhado de segurados para um período específico"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Período inválido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<RelatorioSeguradosView> obterRelatorioSegurados(
            @Parameter(description = "Data de início do período", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            
            @Parameter(description = "Data de fim do período", example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        
        log.info("Obtendo relatório de segurados de {} a {}", inicio, fim);
        
        if (inicio.isAfter(fim)) {
            return ResponseEntity.badRequest().build();
        }
        
        RelatorioSeguradosView relatorio = relatorioService.obterRelatorioSegurados(inicio, fim);
        return ResponseEntity.ok(relatorio);
    }
    
    /**
     * Obtém relatório de apólices por período.
     */
    @GetMapping("/apolices")
    @Operation(
        summary = "Relatório de Apólices",
        description = "Obtém relatório detalhado de apólices para um período específico"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Período inválido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<RelatorioApolicesView> obterRelatorioApolices(
            @Parameter(description = "Data de início do período", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            
            @Parameter(description = "Data de fim do período", example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        
        log.info("Obtendo relatório de apólices de {} a {}", inicio, fim);
        
        if (inicio.isAfter(fim)) {
            return ResponseEntity.badRequest().build();
        }
        
        RelatorioApolicesView relatorio = relatorioService.obterRelatorioApolices(inicio, fim);
        return ResponseEntity.ok(relatorio);
    }
    
    /**
     * Obtém relatório de performance operacional.
     */
    @GetMapping("/performance")
    @Operation(
        summary = "Relatório de Performance",
        description = "Obtém relatório de performance operacional para um período específico"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Período inválido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<RelatorioPerformanceView> obterRelatorioPerformance(
            @Parameter(description = "Data de início do período", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            
            @Parameter(description = "Data de fim do período", example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        
        log.info("Obtendo relatório de performance de {} a {}", inicio, fim);
        
        if (inicio.isAfter(fim)) {
            return ResponseEntity.badRequest().build();
        }
        
        RelatorioPerformanceView relatorio = relatorioService.obterRelatorioPerformance(inicio, fim);
        return ResponseEntity.ok(relatorio);
    }
    
    /**
     * Obtém relatório de renovações.
     */
    @GetMapping("/renovacoes")
    @Operation(
        summary = "Relatório de Renovações",
        description = "Obtém relatório específico de renovações de apólices para um período"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Período inválido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<RelatorioRenovacoesView> obterRelatorioRenovacoes(
            @Parameter(description = "Data de início do período", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            
            @Parameter(description = "Data de fim do período", example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        
        log.info("Obtendo relatório de renovações de {} a {}", inicio, fim);
        
        if (inicio.isAfter(fim)) {
            return ResponseEntity.badRequest().build();
        }
        
        RelatorioRenovacoesView relatorio = relatorioService.obterRelatorioRenovacoes(inicio, fim);
        return ResponseEntity.ok(relatorio);
    }
    
    /**
     * Health check do sistema de relatórios.
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health Check",
        description = "Verifica a saúde do sistema de relatórios"
    )
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Sistema de relatórios operacional");
    }
}