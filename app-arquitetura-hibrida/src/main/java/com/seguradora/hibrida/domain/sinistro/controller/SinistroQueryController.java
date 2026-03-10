package com.seguradora.hibrida.domain.sinistro.controller;

import com.seguradora.hibrida.domain.sinistro.query.dto.DashboardView;
import com.seguradora.hibrida.domain.sinistro.query.dto.SinistroDetailView;
import com.seguradora.hibrida.domain.sinistro.query.dto.SinistroFilter;
import com.seguradora.hibrida.domain.sinistro.query.dto.SinistroListView;
import com.seguradora.hibrida.domain.sinistro.query.service.SinistroQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Controller para consultas de sinistros (Query Side do CQRS).
 */
@RestController
@RequestMapping("/api/v1/query/sinistros")
@Tag(name = "🔍 Queries - Sinistros", description = "APIs para consulta de sinistros (Query Side CQRS)")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SinistroQueryController {
    
    private static final Logger log = LoggerFactory.getLogger(SinistroQueryController.class);
    
    private final SinistroQueryService queryService;
    
    public SinistroQueryController(SinistroQueryService queryService) {
        this.queryService = queryService;
    }
    
    /**
     * Busca sinistro por ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar sinistro por ID", description = "Retorna os detalhes completos de um sinistro específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sinistro encontrado"),
        @ApiResponse(responseCode = "404", description = "Sinistro não encontrado"),
        @ApiResponse(responseCode = "400", description = "ID inválido")
    })
    public ResponseEntity<SinistroDetailView> buscarPorId(
            @Parameter(description = "ID único do sinistro", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        
        log.debug("GET /api/v1/query/sinistros/{}", id);
        
        return queryService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Busca sinistro por protocolo.
     */
    @GetMapping("/protocolo/{protocolo}")
    @Operation(summary = "Buscar sinistro por protocolo", description = "Retorna os detalhes completos de um sinistro pelo seu protocolo único")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sinistro encontrado"),
        @ApiResponse(responseCode = "404", description = "Sinistro não encontrado")
    })
    public ResponseEntity<SinistroDetailView> buscarPorProtocolo(
            @Parameter(description = "Protocolo único do sinistro", example = "SIN-2024-001234")
            @PathVariable String protocolo) {
        
        log.debug("GET /api/v1/query/sinistros/protocolo/{}", protocolo);
        
        return queryService.buscarPorProtocolo(protocolo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Lista sinistros com filtros e paginação.
     */
    @GetMapping
    @Operation(summary = "Listar sinistros", description = "Lista sinistros com filtros opcionais e paginação")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de sinistros retornada com sucesso")
    })
    public ResponseEntity<Page<SinistroListView>> listar(
            @Parameter(description = "Filtros de consulta")
            @ModelAttribute SinistroFilter filter,
            
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "dataAbertura") Pageable pageable) {
        
        log.debug("GET /api/v1/query/sinistros - Filtros: {}, Página: {}", filter, pageable);
        
        Page<SinistroListView> resultado = queryService.listar(filter, pageable);
        
        return ResponseEntity.ok(resultado);
    }
    
    /**
     * Busca sinistros por CPF do segurado.
     */
    @GetMapping("/segurado/{cpf}")
    @Operation(summary = "Buscar sinistros por CPF do segurado", description = "Retorna todos os sinistros de um segurado específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sinistros do segurado retornados")
    })
    public ResponseEntity<Page<SinistroListView>> buscarPorCpfSegurado(
            @Parameter(description = "CPF do segurado", example = "12345678901")
            @PathVariable String cpf,
            
            @PageableDefault(size = 10, sort = "dataAbertura") Pageable pageable) {
        
        log.debug("GET /api/v1/query/sinistros/segurado/{}", cpf);
        
        Page<SinistroListView> resultado = queryService.buscarPorCpfSegurado(cpf, pageable);
        
        return ResponseEntity.ok(resultado);
    }
    
    /**
     * Busca sinistros por placa do veículo.
     */
    @GetMapping("/veiculo/{placa}")
    @Operation(summary = "Buscar sinistros por placa do veículo", description = "Retorna todos os sinistros de um veículo específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sinistros do veículo retornados")
    })
    public ResponseEntity<Page<SinistroListView>> buscarPorPlaca(
            @Parameter(description = "Placa do veículo", example = "ABC1234")
            @PathVariable String placa,
            
            @PageableDefault(size = 10, sort = "dataAbertura") Pageable pageable) {
        
        log.debug("GET /api/v1/query/sinistros/veiculo/{}", placa);
        
        Page<SinistroListView> resultado = queryService.buscarPorPlaca(placa, pageable);
        
        return ResponseEntity.ok(resultado);
    }
    
    /**
     * Busca textual em sinistros.
     */
    @GetMapping("/buscar")
    @Operation(summary = "Busca textual em sinistros", description = "Realiza busca full-text em protocolo, nome do segurado e descrição")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resultados da busca retornados"),
        @ApiResponse(responseCode = "400", description = "Termo de busca inválido")
    })
    public ResponseEntity<Page<SinistroListView>> buscarPorTexto(
            @Parameter(description = "Termo de busca", example = "acidente avenida")
            @RequestParam String termo,
            
            @PageableDefault(size = 20, sort = "dataAbertura") Pageable pageable) {
        
        log.debug("GET /api/v1/query/sinistros/buscar?termo={}", termo);
        
        if (termo == null || termo.trim().length() < 3) {
            return ResponseEntity.badRequest().build();
        }
        
        Page<SinistroListView> resultado = queryService.buscarPorTexto(termo.trim(), pageable);
        
        return ResponseEntity.ok(resultado);
    }
    
    /**
     * Busca sinistros por tag.
     */
    @GetMapping("/tag/{tag}")
    @Operation(summary = "Buscar sinistros por tag", description = "Retorna sinistros que possuem uma tag específica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sinistros com a tag retornados")
    })
    public ResponseEntity<Page<SinistroListView>> buscarPorTag(
            @Parameter(description = "Tag a ser buscada", example = "URGENTE")
            @PathVariable String tag,
            
            @PageableDefault(size = 20, sort = "dataAbertura") Pageable pageable) {
        
        log.debug("GET /api/v1/query/sinistros/tag/{}", tag);
        
        Page<SinistroListView> resultado = queryService.buscarPorTag(tag, pageable);
        
        return ResponseEntity.ok(resultado);
    }
    
    /**
     * Obtém dados para dashboard.
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Obter dados do dashboard", description = "Retorna métricas agregadas para exibição em dashboard")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dados do dashboard retornados")
    })
    public ResponseEntity<DashboardView> obterDashboard() {
        
        log.debug("GET /api/v1/query/sinistros/dashboard");
        
        DashboardView dashboard = queryService.obterDashboard();
        
        return ResponseEntity.ok(dashboard);
    }
    
    /**
     * Health check específico para queries.
     */
    @GetMapping("/health")
    @Operation(summary = "Health check das consultas", description = "Verifica a saúde do sistema de consultas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sistema de consultas saudável"),
        @ApiResponse(responseCode = "503", description = "Sistema de consultas com problemas")
    })
    public ResponseEntity<Map<String, Object>> healthCheck() {
        
        log.debug("GET /api/v1/query/sinistros/health");
        
        try {
            // Teste básico de conectividade
            DashboardView dashboard = queryService.obterDashboard();
            
            Map<String, Object> health = Map.of(
                "status", "UP",
                "queryService", "OPERATIONAL",
                "totalSinistros", dashboard.totalSinistros() != null ? dashboard.totalSinistros() : 0,
                "timestamp", Instant.now()
            );
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("Health check failed", e);
            
            Map<String, Object> health = Map.of(
                "status", "DOWN",
                "queryService", "ERROR",
                "error", e.getMessage(),
                "timestamp", Instant.now()
            );
            
            return ResponseEntity.status(503).body(health);
        }
    }
}