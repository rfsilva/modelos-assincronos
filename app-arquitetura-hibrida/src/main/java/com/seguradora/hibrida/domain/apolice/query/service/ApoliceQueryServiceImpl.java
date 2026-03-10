package com.seguradora.hibrida.domain.apolice.query.service;

import com.seguradora.hibrida.domain.apolice.model.StatusApolice;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceDetailView;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceListView;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceVencimentoView;
import com.seguradora.hibrida.domain.apolice.query.model.ApoliceQueryModel;
import com.seguradora.hibrida.domain.apolice.query.repository.ApoliceQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementação do serviço de consultas de apólices.
 * 
 * <p>Fornece operações de consulta otimizadas com cache
 * e conversão para DTOs de visualização.</p>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class ApoliceQueryServiceImpl implements ApoliceQueryService {
    
    private static final Logger log = LoggerFactory.getLogger(ApoliceQueryServiceImpl.class);
    
    private final ApoliceQueryRepository repository;
    
    public ApoliceQueryServiceImpl(ApoliceQueryRepository repository) {
        this.repository = repository;
    }
    
    // === CONSULTAS BÁSICAS ===
    
    @Override
    @Cacheable(value = "apolice-detail", key = "#id")
    public Optional<ApoliceDetailView> buscarPorId(String id) {
        log.debug("Buscando apólice por ID: {}", id);
        
        return repository.findById(id)
            .map(this::toDetailView);
    }
    
    @Override
    @Cacheable(value = "apolice-detail", key = "#numero")
    public Optional<ApoliceDetailView> buscarPorNumero(String numero) {
        log.debug("Buscando apólice por número: {}", numero);
        
        return repository.findByNumero(numero)
            .map(this::toDetailView);
    }
    
    @Override
    public Page<ApoliceListView> listarTodas(Pageable pageable) {
        log.debug("Listando todas as apólices - página: {}", pageable.getPageNumber());
        
        return repository.findAll(pageable)
            .map(this::toListView);
    }
    
    // === CONSULTAS POR SEGURADO ===
    
    @Override
    @Cacheable(value = "apolices-por-cpf", key = "#cpf")
    public List<ApoliceListView> buscarPorCpfSegurado(String cpf) {
        log.debug("Buscando apólices por CPF: {}", cpf);
        
        return repository.findBySeguradoCpfOrderByVigenciaInicioDesc(cpf)
            .stream()
            .map(this::toListView)
            .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "apolices-ativas-por-cpf", key = "#cpf")
    public List<ApoliceListView> buscarAtivasPorCpfSegurado(String cpf) {
        log.debug("Buscando apólices ativas por CPF: {}", cpf);
        
        return repository.findApolicesAtivasPorCpf(cpf)
            .stream()
            .map(this::toListView)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<ApoliceListView> buscarPorNomeSegurado(String nome, Pageable pageable) {
        log.debug("Buscando apólices por nome do segurado: {}", nome);
        
        return repository.findBySeguradoNomeContaining(nome, pageable)
            .map(this::toListView);
    }
    
    // === CONSULTAS POR STATUS ===
    
    @Override
    public Page<ApoliceListView> buscarPorStatus(StatusApolice status, Pageable pageable) {
        log.debug("Buscando apólices por status: {}", status);
        
        return repository.findByStatusOrderByVigenciaFimAsc(status, pageable)
            .map(this::toListView);
    }
    
    @Override
    @Cacheable(value = "apolices-ativas", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ApoliceListView> buscarAtivas(Pageable pageable) {
        log.debug("Buscando apólices ativas");
        
        return repository.findApolicesAtivas(pageable)
            .map(this::toListView);
    }
    
    @Override
    public Page<ApoliceListView> buscarVencidas(Pageable pageable) {
        log.debug("Buscando apólices vencidas");
        
        return repository.findApolicesVencidas(pageable)
            .map(this::toListView);
    }
    
    // === CONSULTAS POR VENCIMENTO ===
    
    @Override
    public List<ApoliceVencimentoView> buscarVencendoEntre(LocalDate inicio, LocalDate fim) {
        log.debug("Buscando apólices vencendo entre {} e {}", inicio, fim);
        
        return repository.findApolicesVencendoEntre(inicio, fim)
            .stream()
            .map(this::toVencimentoView)
            .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "apolices-vencendo", key = "#dias")
    public List<ApoliceVencimentoView> buscarVencendoEm(int dias) {
        log.debug("Buscando apólices vencendo em {} dias", dias);
        
        LocalDate dataLimite = LocalDate.now().plusDays(dias);
        return repository.findApolicesVencendoEm(dataLimite)
            .stream()
            .map(this::toVencimentoView)
            .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "apolices-vencimento-proximo")
    public List<ApoliceVencimentoView> buscarComVencimentoProximo() {
        log.debug("Buscando apólices com vencimento próximo");
        
        return repository.findApolicesComVencimentoProximo()
            .stream()
            .map(this::toVencimentoView)
            .collect(Collectors.toList());
    }
    
    // === CONSULTAS POR PRODUTO ===
    
    @Override
    public Page<ApoliceListView> buscarPorProduto(String produto, Pageable pageable) {
        log.debug("Buscando apólices por produto: {}", produto);
        
        return repository.findByProdutoOrderByVigenciaInicioDesc(produto, pageable)
            .map(this::toListView);
    }
    
    // === CONSULTAS POR COBERTURA ===
    
    @Override
    public List<ApoliceListView> buscarPorCobertura(TipoCobertura cobertura) {
        log.debug("Buscando apólices por cobertura: {}", cobertura);
        
        return repository.findByCobertura(cobertura)
            .stream()
            .map(this::toListView)
            .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "apolices-cobertura-total")
    public List<ApoliceListView> buscarComCoberturaTotal() {
        log.debug("Buscando apólices com cobertura total");
        
        return repository.findApolicesComCoberturaTotal()
            .stream()
            .map(this::toListView)
            .collect(Collectors.toList());
    }
    
    // === CONSULTAS POR VALOR ===
    
    @Override
    public Page<ApoliceListView> buscarPorFaixaValor(BigDecimal valorMin, BigDecimal valorMax, Pageable pageable) {
        log.debug("Buscando apólices por faixa de valor: {} - {}", valorMin, valorMax);
        
        return repository.findByValorTotalBetween(valorMin, valorMax, pageable)
            .map(this::toListView);
    }
    
    @Override
    public List<ApoliceListView> buscarAltoValor(BigDecimal valorMinimo) {
        log.debug("Buscando apólices de alto valor: {}", valorMinimo);
        
        return repository.findApolicesAltoValor(valorMinimo)
            .stream()
            .map(this::toListView)
            .collect(Collectors.toList());
    }
    
    // === CONSULTAS DE RENOVAÇÃO ===
    
    @Override
    @Cacheable(value = "apolices-elegiveis-renovacao")
    public List<ApoliceVencimentoView> buscarElegiveisRenovacaoAutomatica() {
        log.debug("Buscando apólices elegíveis para renovação automática");
        
        return repository.findElegiveisRenovacaoAutomatica()
            .stream()
            .map(this::toVencimentoView)
            .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "apolices-atencao-renovacao")
    public List<ApoliceVencimentoView> buscarPrecisandoAtencaoRenovacao() {
        log.debug("Buscando apólices que precisam de atenção para renovação");
        
        return repository.findPrecisandoAtencaoRenovacao()
            .stream()
            .map(this::toVencimentoView)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ApoliceListView> buscarPorScoreRenovacao(int scoreMin, int scoreMax) {
        log.debug("Buscando apólices por score de renovação: {} - {}", scoreMin, scoreMax);
        
        return repository.findByScoreRenovacao(scoreMin, scoreMax)
            .stream()
            .map(this::toListView)
            .collect(Collectors.toList());
    }
    
    // === CONSULTAS POR LOCALIZAÇÃO ===
    
    @Override
    public Page<ApoliceListView> buscarPorCidade(String cidade, Pageable pageable) {
        log.debug("Buscando apólices por cidade: {}", cidade);
        
        return repository.findBySeguradoCidadeOrderByVigenciaFimDesc(cidade, pageable)
            .map(this::toListView);
    }
    
    @Override
    public Page<ApoliceListView> buscarPorEstado(String estado, Pageable pageable) {
        log.debug("Buscando apólices por estado: {}", estado);
        
        return repository.findBySeguradoEstadoOrderByVigenciaFimDesc(estado, pageable)
            .map(this::toListView);
    }
    
    // === CONSULTAS CUSTOMIZADAS ===
    
    @Override
    public Page<ApoliceListView> buscarComFiltros(
            StatusApolice status,
            String produto,
            String seguradoCpf,
            LocalDate vigenciaInicio,
            LocalDate vigenciaFim,
            BigDecimal valorMin,
            BigDecimal valorMax,
            Pageable pageable) {
        
        log.debug("Buscando apólices com filtros múltiplos");
        
        return repository.findWithFilters(
            status, produto, seguradoCpf, 
            vigenciaInicio, vigenciaFim, 
            valorMin, valorMax, pageable
        ).map(this::toListView);
    }
    
    // === VERIFICAÇÕES ===
    
    @Override
    public boolean existeComNumero(String numero) {
        return repository.existsByNumero(numero);
    }
    
    @Override
    @Cacheable(value = "count-ativas-por-cpf", key = "#cpf")
    public long contarAtivasPorCpf(String cpf) {
        return repository.countApolicesAtivasPorCpf(cpf);
    }
    
    @Override
    @Cacheable(value = "segurado-possui-ativas", key = "#cpf")
    public boolean seguradoPossuiApolicesAtivas(String cpf) {
        return repository.seguradoPossuiApolicesAtivas(cpf);
    }
    
    // === MÉTODOS DE CONVERSÃO ===
    
    /**
     * Converte ApoliceQueryModel para ApoliceListView.
     */
    private ApoliceListView toListView(ApoliceQueryModel model) {
        return new ApoliceListView(
            model.getId(),
            model.getNumero(),
            model.getProduto(),
            model.getStatus(),
            
            // Dados do segurado
            model.getSeguradoNome(),
            model.getSeguradoCpf(),
            model.getSeguradoCidade(),
            model.getSeguradoEstado(),
            
            // Vigência
            model.getVigenciaInicio(),
            model.getVigenciaFim(),
            model.getDiasParaVencimento(),
            model.getVencimentoProximo(),
            
            // Valores
            model.getValorSegurado(),
            model.getValorPremio(),
            model.getValorTotal(),
            model.getFormaPagamento(),
            model.getParcelas(),
            
            // Coberturas
            model.getCoberturas(),
            model.getCoberturasResumo(),
            model.getTemCoberturaTotal(),
            
            // Controle
            model.getOperadorResponsavel(),
            model.getCanalVenda(),
            model.getRenovacaoAutomatica(),
            model.getScoreRenovacao()
        );
    }
    
    /**
     * Converte ApoliceQueryModel para ApoliceDetailView.
     */
    private ApoliceDetailView toDetailView(ApoliceQueryModel model) {
        // Calcular duração em meses
        Integer duracaoMeses = null;
        if (model.getVigenciaInicio() != null && model.getVigenciaFim() != null) {
            duracaoMeses = (int) java.time.temporal.ChronoUnit.MONTHS.between(
                model.getVigenciaInicio(), model.getVigenciaFim());
        }
        
        // Calcular valor da parcela
        BigDecimal valorParcela = null;
        if (model.getValorTotal() != null && model.getParcelas() != null && model.getParcelas() > 0) {
            valorParcela = model.getValorTotal().divide(
                BigDecimal.valueOf(model.getParcelas()), 2, BigDecimal.ROUND_HALF_UP);
        }
        
        // Criar métricas simuladas
        Map<String, Object> metricas = new HashMap<>();
        metricas.put("scoreRenovacao", model.getScoreRenovacao());
        metricas.put("diasParaVencimento", model.getDiasParaVencimento());
        metricas.put("valorFranquiaEstimado", model.getValorFranquiaEstimado());
        
        // Criar alertas
        List<String> alertas = new ArrayList<>();
        if (Boolean.TRUE.equals(model.getVencimentoProximo())) {
            alertas.add("Vencimento próximo");
        }
        if (model.getScoreRenovacao() != null && model.getScoreRenovacao() < 50) {
            alertas.add("Score de renovação baixo");
        }
        
        // Criar recomendações
        List<String> recomendacoes = new ArrayList<>();
        if (Boolean.TRUE.equals(model.getVencimentoProximo())) {
            recomendacoes.add("Iniciar processo de renovação");
        }
        
        return new ApoliceDetailView(
            model.getId(),
            model.getNumero(),
            model.getProduto(),
            model.getStatus(),
            
            // Dados do segurado completos
            model.getSeguradoId(),
            model.getSeguradoNome(),
            model.getSeguradoCpf(),
            model.getSeguradoEmail(),
            model.getSeguradoTelefone(),
            model.getSeguradoCidade(),
            model.getSeguradoEstado(),
            
            // Vigência detalhada
            model.getVigenciaInicio(),
            model.getVigenciaFim(),
            model.getDiasParaVencimento(),
            model.getVencimentoProximo(),
            duracaoMeses,
            
            // Valores detalhados
            model.getValorSegurado(),
            model.getValorPremio(),
            model.getValorTotal(),
            model.getValorFranquiaEstimado(),
            model.getFormaPagamento(),
            model.getParcelas(),
            valorParcela,
            
            // Coberturas detalhadas
            model.getCoberturas(),
            model.getCoberturasResumo(),
            model.getTemCoberturaTotal(),
            Collections.emptyMap(), // valoresPorCobertura - seria calculado
            
            // Controle e gestão
            model.getOperadorResponsavel(),
            model.getCanalVenda(),
            model.getObservacoes(),
            model.getRenovacaoAutomatica(),
            model.getScoreRenovacao(),
            
            // Histórico e auditoria
            model.getCreatedAt(),
            model.getUpdatedAt(),
            model.getVersion(),
            model.getLastEventId(),
            
            // Relacionamentos (simulados)
            Collections.emptyList(), // sinistrosRelacionados
            Collections.emptyList(), // renovacoesAnteriores
            
            // Métricas e análises
            metricas,
            alertas,
            recomendacoes
        );
    }
    
    /**
     * Converte ApoliceQueryModel para ApoliceVencimentoView.
     */
    private ApoliceVencimentoView toVencimentoView(ApoliceQueryModel model) {
        String prioridade = ApoliceVencimentoView.calcularPrioridade(model.getDiasParaVencimento());
        String statusRenovacao = ApoliceVencimentoView.calcularStatusRenovacao(
            model.getRenovacaoAutomatica(), model.getScoreRenovacao());
        boolean precisaAcao = ApoliceVencimentoView.precisaAcaoImediata(
            model.getDiasParaVencimento(), model.getRenovacaoAutomatica(), model.getScoreRenovacao());
        
        return new ApoliceVencimentoView(
            model.getId(),
            model.getNumero(),
            model.getProduto(),
            model.getStatus(),
            
            // Dados do segurado
            model.getSeguradoNome(),
            model.getSeguradoCpf(),
            model.getSeguradoTelefone(),
            model.getSeguradoEmail(),
            
            // Vencimento
            model.getVigenciaFim(),
            model.getDiasParaVencimento(),
            prioridade,
            
            // Renovação
            model.getRenovacaoAutomatica(),
            model.getScoreRenovacao(),
            statusRenovacao,
            
            // Valores
            model.getValorTotal(),
            model.getFormaPagamento(),
            
            // Controle
            model.getOperadorResponsavel(),
            precisaAcao
        );
    }
}