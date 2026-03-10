package com.seguradora.hibrida.domain.apolice.query.controller;

import com.seguradora.hibrida.domain.apolice.model.StatusApolice;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceDetailView;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceListView;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceVencimentoView;
import com.seguradora.hibrida.domain.apolice.query.service.ApoliceQueryService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para consultas de apólices.
 * 
 * <p>Fornece endpoints otimizados para diferentes cenários
 * de consulta de apólices com cache e paginação.</p>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/apolices")
@Tag(name = "Consultas de Apólices", description = "Endpoints para consulta de apólices")
public class ApoliceQueryController {
    
    private static final Logger log = LoggerFactory.getLogger(ApoliceQueryController.class);
    
    private final ApoliceQueryService queryService;
    
    public ApoliceQueryController(ApoliceQueryService queryService) {
        this.queryService = queryService;
    }
    
    // === CONSULTAS BÁSICAS ===
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar apólice por ID", description = "Retorna detalhes completos da apólice")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Apólice encontrada"),
        @ApiResponse(responseCode = "404", description = "Apólice não encontrada")
    })
    public ResponseEntity<ApoliceDetailView> buscarPorId(
            @Parameter(description = "ID único da apólice", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String id) {
        
        log.info("Buscando apólice por ID: {}", id);
        
        return queryService.buscarPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/numero/{numero}")
    @Operation(summary = "Buscar apólice por número", description = "Retorna detalhes completos da apólice pelo número")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Apólice encontrada"),
        @ApiResponse(responseCode = "404", description = "Apólice não encontrada")
    })
    public ResponseEntity<ApoliceDetailView> buscarPorNumero(
            @Parameter(description = "Número da apólice", example = "AP-2024-001234")
            @PathVariable String numero) {
        
        log.info("Buscando apólice por número: {}", numero);
        
        return queryService.buscarPorNumero(numero)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "Listar todas as apólices", description = "Lista apólices com paginação")
    public ResponseEntity<Page<ApoliceListView>> listarTodas(
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "vigenciaFim") Pageable pageable) {
        
        log.info("Listando todas as apólices - página: {}", pageable.getPageNumber());
        
        Page<ApoliceListView> apolices = queryService.listarTodas(pageable);
        return ResponseEntity.ok(apolices);
    }
    
    // === CONSULTAS POR SEGURADO ===
    
    @GetMapping("/segurado/cpf/{cpf}")
    @Operation(summary = "Buscar apólices por CPF do segurado", description = "Lista todas as apólices do segurado")
    public ResponseEntity<List<ApoliceListView>> buscarPorCpfSegurado(
            @Parameter(description = "CPF do segurado", example = "12345678901")
            @PathVariable String cpf) {
        
        log.info("Buscando apólices por CPF: {}", cpf);
        
        List<ApoliceListView> apolices = queryService.buscarPorCpfSegurado(cpf);
        return ResponseEntity.ok(apolices);
    }
    
    @GetMapping("/segurado/cpf/{cpf}/ativas")
    @Operation(summary = "Buscar apólices ativas por CPF", description = "Lista apenas apólices ativas do segurado")
    public ResponseEntity<List<ApoliceListView>> buscarAtivasPorCpf(
            @Parameter(description = "CPF do segurado", example = "12345678901")
            @PathVariable String cpf) {
        
        log.info("Buscando apólices ativas por CPF: {}", cpf);
        
        List<ApoliceListView> apolices = queryService.buscarAtivasPorCpfSegurado(cpf);
        return ResponseEntity.ok(apolices);
    }
    
    @GetMapping("/segurado/nome")
    @Operation(summary = "Buscar apólices por nome do segurado", description = "Busca parcial por nome")
    public ResponseEntity<Page<ApoliceListView>> buscarPorNomeSegurado(
            @Parameter(description = "Nome ou parte do nome do segurado", example = "João Silva")
            @RequestParam String nome,
            
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "seguradoNome") Pageable pageable) {
        
        log.info("Buscando apólices por nome do segurado: {}", nome);
        
        Page<ApoliceListView> apolices = queryService.buscarPorNomeSegurado(nome, pageable);
        return ResponseEntity.ok(apolices);
    }
    
    // === CONSULTAS POR STATUS ===
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Buscar apólices por status", description = "Lista apólices com status específico")
    public ResponseEntity<Page<ApoliceListView>> buscarPorStatus(
            @Parameter(description = "Status da apólice", example = "ATIVA")
            @PathVariable StatusApolice status,
            
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "vigenciaFim") Pageable pageable) {
        
        log.info("Buscando apólices por status: {}", status);
        
        Page<ApoliceListView> apolices = queryService.buscarPorStatus(status, pageable);
        return ResponseEntity.ok(apolices);
    }
    
    @GetMapping("/ativas")
    @Operation(summary = "Buscar apólices ativas", description = "Lista todas as apólices ativas")
    public ResponseEntity<Page<ApoliceListView>> buscarAtivas(
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "vigenciaFim") Pageable pageable) {
        
        log.info("Buscando apólices ativas");
        
        Page<ApoliceListView> apolices = queryService.buscarAtivas(pageable);
        return ResponseEntity.ok(apolices);
    }
    
    @GetMapping("/vencidas")
    @Operation(summary = "Buscar apólices vencidas", description = "Lista todas as apólices vencidas")
    public ResponseEntity<Page<ApoliceListView>> buscarVencidas(
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "vigenciaFim") Pageable pageable) {
        
        log.info("Buscando apólices vencidas");
        
        Page<ApoliceListView> apolices = queryService.buscarVencidas(pageable);
        return ResponseEntity.ok(apolices);
    }
    
    // === CONSULTAS POR VENCIMENTO ===
    
    @GetMapping("/vencimento/periodo")
    @Operation(summary = "Buscar apólices por período de vencimento", description = "Lista apólices que vencem no período")
    public ResponseEntity<List<ApoliceVencimentoView>> buscarVencendoEntre(
            @Parameter(description = "Data de início do período", example = "2024-01-01")
            @RequestParam LocalDate inicio,
            
            @Parameter(description = "Data de fim do período", example = "2024-01-31")
            @RequestParam LocalDate fim) {
        
        log.info("Buscando apólices vencendo entre {} e {}", inicio, fim);
        
        List<ApoliceVencimentoView> apolices = queryService.buscarVencendoEntre(inicio, fim);
        return ResponseEntity.ok(apolices);
    }
    
    @GetMapping("/vencimento/dias/{dias}")
    @Operation(summary = "Buscar apólices vencendo em X dias", description = "Lista apólices que vencem nos próximos dias")
    public ResponseEntity<List<ApoliceVencimentoView>> buscarVencendoEm(
            @Parameter(description = "Número de dias", example = "30")
            @PathVariable int dias) {
        
        log.info("Buscando apólices vencendo em {} dias", dias);
        
        List<ApoliceVencimentoView> apolices = queryService.buscarVencendoEm(dias);
        return ResponseEntity.ok(apolices);
    }
    
    @GetMapping("/vencimento/proximo")
    @Operation(summary = "Buscar apólices com vencimento próximo", description = "Lista apólices com vencimento nos próximos 30 dias")
    public ResponseEntity<List<ApoliceVencimentoView>> buscarComVencimentoProximo() {
        
        log.info("Buscando apólices com vencimento próximo");
        
        List<ApoliceVencimentoView> apolices = queryService.buscarComVencimentoProximo();
        return ResponseEntity.ok(apolices);
    }
    
    // === CONSULTAS POR PRODUTO ===
    
    @GetMapping("/produto/{produto}")
    @Operation(summary = "Buscar apólices por produto", description = "Lista apólices de um produto específico")
    public ResponseEntity<Page<ApoliceListView>> buscarPorProduto(
            @Parameter(description = "Nome do produto", example = "Seguro Auto")
            @PathVariable String produto,
            
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "vigenciaInicio") Pageable pageable) {
        
        log.info("Buscando apólices por produto: {}", produto);
        
        Page<ApoliceListView> apolices = queryService.buscarPorProduto(produto, pageable);
        return ResponseEntity.ok(apolices);
    }
    
    // === CONSULTAS POR COBERTURA ===
    
    @GetMapping("/cobertura/{cobertura}")
    @Operation(summary = "Buscar apólices por tipo de cobertura", description = "Lista apólices com cobertura específica")
    public ResponseEntity<List<ApoliceListView>> buscarPorCobertura(
            @Parameter(description = "Tipo de cobertura", example = "TOTAL")
            @PathVariable TipoCobertura cobertura) {
        
        log.info("Buscando apólices por cobertura: {}", cobertura);
        
        List<ApoliceListView> apolices = queryService.buscarPorCobertura(cobertura);
        return ResponseEntity.ok(apolices);
    }
    
    @GetMapping("/cobertura-total")
    @Operation(summary = "Buscar apólices com cobertura total", description = "Lista apólices com cobertura total")
    public ResponseEntity<List<ApoliceListView>> buscarComCoberturaTotal() {
        
        log.info("Buscando apólices com cobertura total");
        
        List<ApoliceListView> apolices = queryService.buscarComCoberturaTotal();
        return ResponseEntity.ok(apolices);
    }
    
    // === CONSULTAS POR VALOR ===
    
    @GetMapping("/valor/faixa")
    @Operation(summary = "Buscar apólices por faixa de valor", description = "Lista apólices dentro de uma faixa de valor")
    public ResponseEntity<Page<ApoliceListView>> buscarPorFaixaValor(
            @Parameter(description = "Valor mínimo", example = "10000.00")
            @RequestParam BigDecimal valorMin,
            
            @Parameter(description = "Valor máximo", example = "100000.00")
            @RequestParam BigDecimal valorMax,
            
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "valorTotal") Pageable pageable) {
        
        log.info("Buscando apólices por faixa de valor: {} - {}", valorMin, valorMax);
        
        Page<ApoliceListView> apolices = queryService.buscarPorFaixaValor(valorMin, valorMax, pageable);
        return ResponseEntity.ok(apolices);
    }
    
    @GetMapping("/alto-valor")
    @Operation(summary = "Buscar apólices de alto valor", description = "Lista apólices acima de um valor mínimo")
    public ResponseEntity<List<ApoliceListView>> buscarAltoValor(
            @Parameter(description = "Valor mínimo", example = "500000.00")
            @RequestParam BigDecimal valorMinimo) {
        
        log.info("Buscando apólices de alto valor: {}", valorMinimo);
        
        List<ApoliceListView> apolices = queryService.buscarAltoValor(valorMinimo);
        return ResponseEntity.ok(apolices);
    }
    
    // === CONSULTAS DE RENOVAÇÃO ===
    
    @GetMapping("/renovacao/elegiveis")
    @Operation(summary = "Buscar apólices elegíveis para renovação automática", description = "Lista apólices que podem ser renovadas automaticamente")
    public ResponseEntity<List<ApoliceVencimentoView>> buscarElegiveisRenovacao() {
        
        log.info("Buscando apólices elegíveis para renovação automática");
        
        List<ApoliceVencimentoView> apolices = queryService.buscarElegiveisRenovacaoAutomatica();
        return ResponseEntity.ok(apolices);
    }
    
    @GetMapping("/renovacao/atencao")
    @Operation(summary = "Buscar apólices que precisam de atenção", description = "Lista apólices que precisam de atenção para renovação")
    public ResponseEntity<List<ApoliceVencimentoView>> buscarPrecisandoAtencao() {
        
        log.info("Buscando apólices que precisam de atenção para renovação");
        
        List<ApoliceVencimentoView> apolices = queryService.buscarPrecisandoAtencaoRenovacao();
        return ResponseEntity.ok(apolices);
    }
    
    @GetMapping("/renovacao/score")
    @Operation(summary = "Buscar apólices por score de renovação", description = "Lista apólices dentro de uma faixa de score")
    public ResponseEntity<List<ApoliceListView>> buscarPorScoreRenovacao(
            @Parameter(description = "Score mínimo", example = "70")
            @RequestParam int scoreMin,
            
            @Parameter(description = "Score máximo", example = "100")
            @RequestParam int scoreMax) {
        
        log.info("Buscando apólices por score de renovação: {} - {}", scoreMin, scoreMax);
        
        List<ApoliceListView> apolices = queryService.buscarPorScoreRenovacao(scoreMin, scoreMax);
        return ResponseEntity.ok(apolices);
    }
    
    // === CONSULTAS POR LOCALIZAÇÃO ===
    
    @GetMapping("/localizacao/cidade/{cidade}")
    @Operation(summary = "Buscar apólices por cidade", description = "Lista apólices de segurados de uma cidade")
    public ResponseEntity<Page<ApoliceListView>> buscarPorCidade(
            @Parameter(description = "Nome da cidade", example = "São Paulo")
            @PathVariable String cidade,
            
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "seguradoNome") Pageable pageable) {
        
        log.info("Buscando apólices por cidade: {}", cidade);
        
        Page<ApoliceListView> apolices = queryService.buscarPorCidade(cidade, pageable);
        return ResponseEntity.ok(apolices);
    }
    
    @GetMapping("/localizacao/estado/{estado}")
    @Operation(summary = "Buscar apólices por estado", description = "Lista apólices de segurados de um estado")
    public ResponseEntity<Page<ApoliceListView>> buscarPorEstado(
            @Parameter(description = "Sigla do estado", example = "SP")
            @PathVariable String estado,
            
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "seguradoNome") Pageable pageable) {
        
        log.info("Buscando apólices por estado: {}", estado);
        
        Page<ApoliceListView> apolices = queryService.buscarPorEstado(estado, pageable);
        return ResponseEntity.ok(apolices);
    }
    
    // === CONSULTAS CUSTOMIZADAS ===
    
    @GetMapping("/filtros")
    @Operation(summary = "Buscar com múltiplos filtros", description = "Busca avançada com múltiplos critérios")
    public ResponseEntity<Page<ApoliceListView>> buscarComFiltros(
            @Parameter(description = "Status da apólice") @RequestParam(required = false) StatusApolice status,
            @Parameter(description = "Produto") @RequestParam(required = false) String produto,
            @Parameter(description = "CPF do segurado") @RequestParam(required = false) String seguradoCpf,
            @Parameter(description = "Data início vigência") @RequestParam(required = false) LocalDate vigenciaInicio,
            @Parameter(description = "Data fim vigência") @RequestParam(required = false) LocalDate vigenciaFim,
            @Parameter(description = "Valor mínimo") @RequestParam(required = false) BigDecimal valorMin,
            @Parameter(description = "Valor máximo") @RequestParam(required = false) BigDecimal valorMax,
            
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "vigenciaFim") Pageable pageable) {
        
        log.info("Buscando apólices com filtros múltiplos");
        
        Page<ApoliceListView> apolices = queryService.buscarComFiltros(
            status, produto, seguradoCpf, vigenciaInicio, vigenciaFim, 
            valorMin, valorMax, pageable);
        
        return ResponseEntity.ok(apolices);
    }
    
    // === VERIFICAÇÕES E UTILITÁRIOS ===
    
    @GetMapping("/existe/numero/{numero}")
    @Operation(summary = "Verificar se existe apólice com número", description = "Verifica existência por número")
    public ResponseEntity<Map<String, Boolean>> verificarExistenciaPorNumero(
            @Parameter(description = "Número da apólice", example = "AP-2024-001234")
            @PathVariable String numero) {
        
        boolean existe = queryService.existeComNumero(numero);
        return ResponseEntity.ok(Map.of("existe", existe));
    }
    
    @GetMapping("/segurado/{cpf}/count")
    @Operation(summary = "Contar apólices ativas por CPF", description = "Retorna quantidade de apólices ativas do segurado")
    public ResponseEntity<Map<String, Long>> contarAtivasPorCpf(
            @Parameter(description = "CPF do segurado", example = "12345678901")
            @PathVariable String cpf) {
        
        long count = queryService.contarAtivasPorCpf(cpf);
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    @GetMapping("/segurado/{cpf}/possui-ativas")
    @Operation(summary = "Verificar se segurado possui apólices ativas", description = "Verifica se o segurado tem apólices ativas")
    public ResponseEntity<Map<String, Boolean>> verificarSeguradoPossuiAtivas(
            @Parameter(description = "CPF do segurado", example = "12345678901")
            @PathVariable String cpf) {
        
        boolean possui = queryService.seguradoPossuiApolicesAtivas(cpf);
        return ResponseEntity.ok(Map.of("possuiAtivas", possui));
    }
    
    // === HEALTH CHECK ===
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verifica saúde do serviço de consultas")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "ApoliceQueryService",
            "timestamp", java.time.Instant.now(),
            "version", "1.0.0"
        );
        
        return ResponseEntity.ok(health);
    }
}