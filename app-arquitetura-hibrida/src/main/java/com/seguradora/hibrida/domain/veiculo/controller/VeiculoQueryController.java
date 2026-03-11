package com.seguradora.hibrida.domain.veiculo.controller;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import com.seguradora.hibrida.domain.veiculo.query.dto.VeiculoDetailView;
import com.seguradora.hibrida.domain.veiculo.query.dto.VeiculoListView;
import com.seguradora.hibrida.domain.veiculo.query.service.VeiculoQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST para consultas de veículos.
 * 
 * <p>Fornece endpoints otimizados para consulta de dados de veículos,
 * incluindo busca por identificadores únicos, filtros avançados,
 * consultas geográficas e estatísticas.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/veiculos")
@Tag(name = "Veículos - Consultas", description = "APIs para consulta de dados de veículos")
public class VeiculoQueryController {
    
    private static final Logger log = LoggerFactory.getLogger(VeiculoQueryController.class);
    
    private final VeiculoQueryService veiculoQueryService;
    
    public VeiculoQueryController(VeiculoQueryService veiculoQueryService) {
        this.veiculoQueryService = veiculoQueryService;
    }
    
    // === CONSULTAS BÁSICAS ===
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar veículo por ID", description = "Retorna dados completos de um veículo específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Veículo encontrado"),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    public ResponseEntity<VeiculoDetailView> buscarPorId(
            @Parameter(description = "ID único do veículo", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String id) {
        
        log.debug("Buscando veículo por ID: {}", id);
        
        return veiculoQueryService.buscarPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/placa/{placa}")
    @Operation(summary = "Buscar veículo por placa", description = "Retorna dados completos de um veículo pela placa")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Veículo encontrado"),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    public ResponseEntity<VeiculoDetailView> buscarPorPlaca(
            @Parameter(description = "Placa do veículo", example = "ABC1234")
            @PathVariable String placa) {
        
        log.debug("Buscando veículo por placa: {}", placa);
        
        return veiculoQueryService.buscarPorPlaca(placa)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/renavam/{renavam}")
    @Operation(summary = "Buscar veículo por RENAVAM", description = "Retorna dados completos de um veículo pelo RENAVAM")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Veículo encontrado"),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    public ResponseEntity<VeiculoDetailView> buscarPorRenavam(
            @Parameter(description = "RENAVAM do veículo", example = "12345678901")
            @PathVariable String renavam) {
        
        log.debug("Buscando veículo por RENAVAM: {}", renavam);
        
        return veiculoQueryService.buscarPorRenavam(renavam)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/chassi/{chassi}")
    @Operation(summary = "Buscar veículo por chassi", description = "Retorna dados completos de um veículo pelo chassi")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Veículo encontrado"),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    public ResponseEntity<VeiculoDetailView> buscarPorChassi(
            @Parameter(description = "Chassi do veículo", example = "1HGBH41JXMN109186")
            @PathVariable String chassi) {
        
        log.debug("Buscando veículo por chassi: {}", chassi);
        
        return veiculoQueryService.buscarPorChassi(chassi)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // === LISTAGENS ===
    
    @GetMapping
    @Operation(summary = "Listar todos os veículos", description = "Retorna lista paginada de todos os veículos")
    public ResponseEntity<Page<VeiculoListView>> listarTodos(
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "marca", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.debug("Listando todos os veículos - página: {}, tamanho: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<VeiculoListView> veiculos = veiculoQueryService.listarTodos(pageable);
        return ResponseEntity.ok(veiculos);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Listar veículos por status", description = "Retorna lista paginada de veículos com status específico")
    public ResponseEntity<Page<VeiculoListView>> listarPorStatus(
            @Parameter(description = "Status do veículo")
            @PathVariable StatusVeiculo status,
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "marca", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.debug("Listando veículos por status: {}", status);
        
        Page<VeiculoListView> veiculos = veiculoQueryService.buscarPorStatus(status, pageable);
        return ResponseEntity.ok(veiculos);
    }
    
    @GetMapping("/ativos")
    @Operation(summary = "Listar veículos ativos", description = "Retorna lista paginada de veículos ativos")
    public ResponseEntity<Page<VeiculoListView>> listarAtivos(
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "marca", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.debug("Listando veículos ativos");
        
        Page<VeiculoListView> veiculos = veiculoQueryService.buscarAtivos(pageable);
        return ResponseEntity.ok(veiculos);
    }
    
    // === CONSULTAS POR PROPRIETÁRIO ===
    
    @GetMapping("/proprietario/cpf/{cpf}")
    @Operation(summary = "Buscar veículos por CPF do proprietário", description = "Retorna lista de veículos de um proprietário específico")
    public ResponseEntity<List<VeiculoListView>> buscarPorProprietarioCpf(
            @Parameter(description = "CPF do proprietário", example = "12345678901")
            @PathVariable String cpf) {
        
        log.debug("Buscando veículos por CPF do proprietário: {}", cpf);
        
        List<VeiculoListView> veiculos = veiculoQueryService.buscarPorProprietarioCpf(cpf);
        return ResponseEntity.ok(veiculos);
    }
    
    @GetMapping("/proprietario/nome")
    @Operation(summary = "Buscar veículos por nome do proprietário", description = "Retorna lista paginada de veículos por nome do proprietário")
    public ResponseEntity<Page<VeiculoListView>> buscarPorProprietarioNome(
            @Parameter(description = "Nome do proprietário", example = "João Silva")
            @RequestParam String nome,
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "proprietarioNome", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.debug("Buscando veículos por nome do proprietário: {}", nome);
        
        Page<VeiculoListView> veiculos = veiculoQueryService.buscarPorProprietarioNome(nome, pageable);
        return ResponseEntity.ok(veiculos);
    }
    
    // === CONSULTAS POR MARCA/MODELO ===
    
    @GetMapping("/marca/{marca}")
    @Operation(summary = "Buscar veículos por marca", description = "Retorna lista paginada de veículos de uma marca específica")
    public ResponseEntity<Page<VeiculoListView>> buscarPorMarca(
            @Parameter(description = "Marca do veículo", example = "Honda")
            @PathVariable String marca,
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "modelo", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.debug("Buscando veículos por marca: {}", marca);
        
        Page<VeiculoListView> veiculos = veiculoQueryService.buscarPorMarca(marca, pageable);
        return ResponseEntity.ok(veiculos);
    }
    
    @GetMapping("/marca/{marca}/modelo/{modelo}")
    @Operation(summary = "Buscar veículos por marca e modelo", description = "Retorna lista paginada de veículos de marca e modelo específicos")
    public ResponseEntity<Page<VeiculoListView>> buscarPorMarcaEModelo(
            @Parameter(description = "Marca do veículo", example = "Honda")
            @PathVariable String marca,
            @Parameter(description = "Modelo do veículo", example = "Civic")
            @PathVariable String modelo,
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "anoFabricacao", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.debug("Buscando veículos por marca: {} e modelo: {}", marca, modelo);
        
        Page<VeiculoListView> veiculos = veiculoQueryService.buscarPorMarcaEModelo(marca, modelo, pageable);
        return ResponseEntity.ok(veiculos);
    }
    
    @GetMapping("/buscar")
    @Operation(summary = "Busca fuzzy por marca ou modelo", description = "Busca veículos por termo livre em marca ou modelo")
    public ResponseEntity<Page<VeiculoListView>> buscarPorMarcaOuModelo(
            @Parameter(description = "Termo de busca", example = "civic")
            @RequestParam String termo,
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "marca", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.debug("Buscando veículos por termo: {}", termo);
        
        Page<VeiculoListView> veiculos = veiculoQueryService.buscarPorMarcaOuModelo(termo, pageable);
        return ResponseEntity.ok(veiculos);
    }
    
    // === CONSULTAS POR ANO ===
    
    @GetMapping("/ano/{ano}")
    @Operation(summary = "Buscar veículos por ano", description = "Retorna lista paginada de veículos de um ano específico")
    public ResponseEntity<Page<VeiculoListView>> buscarPorAno(
            @Parameter(description = "Ano de fabricação", example = "2020")
            @PathVariable Integer ano,
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "marca", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.debug("Buscando veículos por ano: {}", ano);
        
        Page<VeiculoListView> veiculos = veiculoQueryService.buscarPorAno(ano, pageable);
        return ResponseEntity.ok(veiculos);
    }
    
    @GetMapping("/ano-faixa")
    @Operation(summary = "Buscar veículos por faixa de ano", description = "Retorna lista paginada de veículos em uma faixa de anos")
    public ResponseEntity<Page<VeiculoListView>> buscarPorFaixaAno(
            @Parameter(description = "Ano inicial", example = "2018")
            @RequestParam Integer anoInicio,
            @Parameter(description = "Ano final", example = "2022")
            @RequestParam Integer anoFim,
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "anoFabricacao", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.debug("Buscando veículos por faixa de ano: {} a {}", anoInicio, anoFim);
        
        Page<VeiculoListView> veiculos = veiculoQueryService.buscarPorFaixaAno(anoInicio, anoFim, pageable);
        return ResponseEntity.ok(veiculos);
    }
    
    // === CONSULTAS GEOGRÁFICAS ===
    
    @GetMapping("/cidade/{cidade}")
    @Operation(summary = "Buscar veículos por cidade", description = "Retorna lista paginada de veículos de uma cidade específica")
    public ResponseEntity<Page<VeiculoListView>> buscarPorCidade(
            @Parameter(description = "Cidade", example = "São Paulo")
            @PathVariable String cidade,
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "proprietarioNome", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.debug("Buscando veículos por cidade: {}", cidade);
        
        Page<VeiculoListView> veiculos = veiculoQueryService.buscarPorCidade(cidade, pageable);
        return ResponseEntity.ok(veiculos);
    }
    
    @GetMapping("/estado/{estado}")
    @Operation(summary = "Buscar veículos por estado", description = "Retorna lista paginada de veículos de um estado específico")
    public ResponseEntity<Page<VeiculoListView>> buscarPorEstado(
            @Parameter(description = "Estado (UF)", example = "SP")
            @PathVariable String estado,
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "cidade", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.debug("Buscando veículos por estado: {}", estado);
        
        Page<VeiculoListView> veiculos = veiculoQueryService.buscarPorEstado(estado, pageable);
        return ResponseEntity.ok(veiculos);
    }
    
    @GetMapping("/regiao/{regiao}")
    @Operation(summary = "Buscar veículos por região", description = "Retorna lista paginada de veículos de uma região específica")
    public ResponseEntity<Page<VeiculoListView>> buscarPorRegiao(
            @Parameter(description = "Região", example = "SUDESTE")
            @PathVariable String regiao,
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "estado", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.debug("Buscando veículos por região: {}", regiao);
        
        Page<VeiculoListView> veiculos = veiculoQueryService.buscarPorRegiao(regiao, pageable);
        return ResponseEntity.ok(veiculos);
    }
    
    // === CONSULTAS DE APÓLICE ===
    
    @GetMapping("/com-apolice")
    @Operation(summary = "Listar veículos com apólice ativa", description = "Retorna lista paginada de veículos que possuem apólice ativa")
    public ResponseEntity<Page<VeiculoListView>> listarComApoliceAtiva(
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "marca", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.debug("Listando veículos com apólice ativa");
        
        Page<VeiculoListView> veiculos = veiculoQueryService.buscarComApoliceAtiva(pageable);
        return ResponseEntity.ok(veiculos);
    }
    
    @GetMapping("/sem-apolice")
    @Operation(summary = "Listar veículos sem apólice ativa", description = "Retorna lista paginada de veículos que não possuem apólice ativa")
    public ResponseEntity<Page<VeiculoListView>> listarSemApoliceAtiva(
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "marca", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.debug("Listando veículos sem apólice ativa");
        
        Page<VeiculoListView> veiculos = veiculoQueryService.buscarSemApoliceAtiva(pageable);
        return ResponseEntity.ok(veiculos);
    }
    
    // === CONSULTAS AVANÇADAS ===
    
    @GetMapping("/filtros")
    @Operation(summary = "Buscar com múltiplos filtros", description = "Busca avançada com múltiplos critérios")
    public ResponseEntity<Page<VeiculoListView>> buscarComFiltros(
            @Parameter(description = "Marca do veículo") @RequestParam(required = false) String marca,
            @Parameter(description = "Modelo do veículo") @RequestParam(required = false) String modelo,
            @Parameter(description = "Status do veículo") @RequestParam(required = false) StatusVeiculo status,
            @Parameter(description = "Ano inicial") @RequestParam(required = false) Integer anoInicio,
            @Parameter(description = "Ano final") @RequestParam(required = false) Integer anoFim,
            @Parameter(description = "Estado") @RequestParam(required = false) String estado,
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "marca", direction = Sort.Direction.ASC) Pageable pageable) {
        
        log.debug("Buscando veículos com filtros - marca: {}, modelo: {}, status: {}, anos: {}-{}, estado: {}",
                marca, modelo, status, anoInicio, anoFim, estado);
        
        Page<VeiculoListView> veiculos = veiculoQueryService.buscarComFiltros(
            marca, modelo, status, anoInicio, anoFim, estado, pageable);
        return ResponseEntity.ok(veiculos);
    }
    
    // === VERIFICAÇÕES ===
    
    @GetMapping("/existe/placa/{placa}")
    @Operation(summary = "Verificar se placa existe", description = "Verifica se já existe um veículo com a placa informada")
    public ResponseEntity<Boolean> existeComPlaca(
            @Parameter(description = "Placa do veículo", example = "ABC1234")
            @PathVariable String placa) {
        
        boolean existe = veiculoQueryService.existeComPlaca(placa);
        return ResponseEntity.ok(existe);
    }
    
    @GetMapping("/existe/renavam/{renavam}")
    @Operation(summary = "Verificar se RENAVAM existe", description = "Verifica se já existe um veículo com o RENAVAM informado")
    public ResponseEntity<Boolean> existeComRenavam(
            @Parameter(description = "RENAVAM do veículo", example = "12345678901")
            @PathVariable String renavam) {
        
        boolean existe = veiculoQueryService.existeComRenavam(renavam);
        return ResponseEntity.ok(existe);
    }
    
    @GetMapping("/existe/chassi/{chassi}")
    @Operation(summary = "Verificar se chassi existe", description = "Verifica se já existe um veículo com o chassi informado")
    public ResponseEntity<Boolean> existeComChassi(
            @Parameter(description = "Chassi do veículo", example = "1HGBH41JXMN109186")
            @PathVariable String chassi) {
        
        boolean existe = veiculoQueryService.existeComChassi(chassi);
        return ResponseEntity.ok(existe);
    }
    
    // === ESTATÍSTICAS ===
    
    @GetMapping("/estatisticas")
    @Operation(summary = "Obter estatísticas gerais", description = "Retorna estatísticas gerais dos veículos cadastrados")
    public ResponseEntity<VeiculoQueryService.VeiculoStatistics> obterEstatisticas() {
        log.debug("Obtendo estatísticas gerais de veículos");
        
        VeiculoQueryService.VeiculoStatistics stats = veiculoQueryService.obterEstatisticas();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/estatisticas/estados")
    @Operation(summary = "Obter estatísticas por estado", description = "Retorna estatísticas de veículos agrupadas por estado")
    public ResponseEntity<List<Object[]>> obterEstatisticasPorEstado() {
        log.debug("Obtendo estatísticas por estado");
        
        List<Object[]> stats = veiculoQueryService.obterEstatisticasPorEstado();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/estatisticas/marcas")
    @Operation(summary = "Obter estatísticas por marca", description = "Retorna estatísticas de veículos agrupadas por marca")
    public ResponseEntity<List<Object[]>> obterEstatisticasPorMarca() {
        log.debug("Obtendo estatísticas por marca");
        
        List<Object[]> stats = veiculoQueryService.obterEstatisticasPorMarca();
        return ResponseEntity.ok(stats);
    }
    
    // === HEALTH CHECK ===
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verifica a saúde do serviço de consulta de veículos")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.debug("Executando health check do serviço de veículos");
        
        try {
            VeiculoQueryService.VeiculoStatistics stats = veiculoQueryService.obterEstatisticas();
            
            Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "VeiculoQueryService",
                "timestamp", java.time.Instant.now(),
                "totalVeiculos", stats.totalVeiculos(),
                "veiculosAtivos", stats.veiculosAtivos(),
                "percentualComApolice", String.format("%.2f%%", stats.percentualComApolice())
            );
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("Erro no health check do serviço de veículos", e);
            
            Map<String, Object> health = Map.of(
                "status", "DOWN",
                "service", "VeiculoQueryService",
                "timestamp", java.time.Instant.now(),
                "error", e.getMessage()
            );
            
            return ResponseEntity.status(503).body(health);
        }
    }
}