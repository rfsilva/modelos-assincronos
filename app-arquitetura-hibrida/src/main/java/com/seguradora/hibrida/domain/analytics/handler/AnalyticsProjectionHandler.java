package com.seguradora.hibrida.domain.analytics.handler;

import com.seguradora.hibrida.domain.analytics.model.AnalyticsProjection;
import com.seguradora.hibrida.domain.analytics.model.TipoMetrica;
import com.seguradora.hibrida.domain.analytics.repository.AnalyticsProjectionRepository;
import com.seguradora.hibrida.domain.apolice.event.*;
import com.seguradora.hibrida.domain.segurado.event.*;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.projection.AbstractProjectionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Projection Handler para métricas analíticas.
 * 
 * <p>Processa eventos de domínio e atualiza projeções analíticas
 * em tempo real para relatórios e dashboards.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AnalyticsProjectionHandler extends AbstractProjectionHandler<DomainEvent> {
    
    private final AnalyticsProjectionRepository analyticsRepository;
    
    @Override
    public String getProjectionName() {
        return "AnalyticsProjection";
    }
    
    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof SeguradoCriadoEvent ||
               event instanceof SeguradoDesativadoEvent ||
               event instanceof ApoliceCriadaEvent ||
               event instanceof ApoliceAtualizadaEvent ||
               event instanceof ApoliceCanceladaEvent ||
               event instanceof ApoliceRenovadaEvent ||
               event instanceof CoberturaAdicionadaEvent;
    }
    
    @Override
    @Transactional("readTransactionManager")
    protected void doHandle(DomainEvent event) throws Exception {
        log.debug("Processando evento analítico: {} para aggregate {}", 
                 event.getEventType(), event.getAggregateId());
        
        switch (event) {
            case SeguradoCriadoEvent e -> handleSeguradoCriado(e);
            case SeguradoDesativadoEvent e -> handleSeguradoDesativado(e);
            case ApoliceCriadaEvent e -> handleApoliceCriada(e);
            case ApoliceAtualizadaEvent e -> handleApoliceAtualizada(e);
            case ApoliceCanceladaEvent e -> handleApoliceCancelada(e);
            case ApoliceRenovadaEvent e -> handleApoliceRenovada(e);
            case CoberturaAdicionadaEvent e -> handleCoberturaAdicionada(e);
            default -> log.debug("Evento {} não requer processamento analítico", event.getEventType());
        }
    }
    
    /**
     * Processa criação de segurado.
     */
    private void handleSeguradoCriado(SeguradoCriadoEvent event) {
        log.info("Processando criação de segurado para analytics: {}", event.getCpf());
        
        LocalDate hoje = LocalDate.now();
        
        // Atualizar métricas gerais
        AnalyticsProjection geral = obterOuCriarProjecao(hoje, TipoMetrica.GERAL, null, null);
        geral.incrementarSegurados(1L);
        
        // Atualizar por região (simulado)
        String estado = simularEstado();
        if (estado != null) {
            AnalyticsProjection porRegiao = obterOuCriarProjecao(hoje, TipoMetrica.POR_REGIAO, "estado", estado);
            porRegiao.incrementarSegurados(1L);
            porRegiao.incrementarRegiao(estado);
            analyticsRepository.save(porRegiao);
        }
        
        // Atualizar por faixa etária (simulado)
        int idade = simularIdade();
        AnalyticsProjection porIdade = obterOuCriarProjecao(hoje, TipoMetrica.POR_FAIXA_ETARIA, "idade", getFaixaEtaria(idade));
        porIdade.incrementarSegurados(1L);
        porIdade.incrementarFaixaEtaria(idade);
        
        // Atualizar por canal (simulado)
        String canal = "ONLINE"; // Simulado
        AnalyticsProjection porCanal = obterOuCriarProjecao(hoje, TipoMetrica.POR_CANAL, "canal", canal);
        porCanal.incrementarSegurados(1L);
        porCanal.incrementarCanal(canal);
        
        // Salvar todas as projeções
        analyticsRepository.save(geral);
        analyticsRepository.save(porIdade);
        analyticsRepository.save(porCanal);
    }
    
    /**
     * Processa desativação de segurado.
     */
    private void handleSeguradoDesativado(SeguradoDesativadoEvent event) {
        log.info("Processando desativação de segurado para analytics: {}", event.getSeguradoId());
        
        LocalDate hoje = LocalDate.now();
        
        // Atualizar métricas gerais
        AnalyticsProjection geral = obterOuCriarProjecao(hoje, TipoMetrica.GERAL, null, null);
        geral.setSeguradosAtivos(Math.max(0, geral.getSeguradosAtivos() - 1));
        geral.setSeguradosInativos(geral.getSeguradosInativos() + 1);
        
        analyticsRepository.save(geral);
    }
    
    /**
     * Processa criação de apólice.
     */
    private void handleApoliceCriada(ApoliceCriadaEvent event) {
        log.info("Processando criação de apólice para analytics: {}", event.getNumeroApolice());
        
        LocalDate hoje = LocalDate.now();
        BigDecimal valorSegurado = new BigDecimal(event.getValorSegurado());
        BigDecimal premio = new BigDecimal(event.getPremioTotal());
        
        // Atualizar métricas gerais
        AnalyticsProjection geral = obterOuCriarProjecao(hoje, TipoMetrica.GERAL, null, null);
        geral.incrementarApolices(1L, valorSegurado, premio);
        
        // Atualizar por produto
        AnalyticsProjection porProduto = obterOuCriarProjecao(hoje, TipoMetrica.POR_PRODUTO, "produto", event.getProduto());
        porProduto.incrementarApolices(1L, valorSegurado, premio);
        
        // Atualizar métricas financeiras
        AnalyticsProjection financeiro = obterOuCriarProjecao(hoje, TipoMetrica.FINANCEIRO, null, null);
        financeiro.incrementarApolices(1L, valorSegurado, premio);
        
        // Salvar projeções
        analyticsRepository.save(geral);
        analyticsRepository.save(porProduto);
        analyticsRepository.save(financeiro);
    }
    
    /**
     * Processa atualização de apólice.
     */
    private void handleApoliceAtualizada(ApoliceAtualizadaEvent event) {
        log.debug("Processando atualização de apólice para analytics: {}", event.getNumeroApolice());
        
        // Por enquanto, apenas log - pode ser expandido conforme necessário
        // para rastrear tipos específicos de alterações
    }
    
    /**
     * Processa cancelamento de apólice.
     */
    private void handleApoliceCancelada(ApoliceCanceladaEvent event) {
        log.info("Processando cancelamento de apólice para analytics: {}", event.getNumeroApolice());
        
        LocalDate hoje = LocalDate.now();
        BigDecimal valorSegurado = new BigDecimal(event.getValorSegurado() != null ? event.getValorSegurado() : "0");
        BigDecimal premio = calcularPremio(valorSegurado);
        
        // Atualizar métricas gerais
        AnalyticsProjection geral = obterOuCriarProjecao(hoje, TipoMetrica.GERAL, null, null);
        geral.registrarCancelamento(valorSegurado, premio);
        
        // Atualizar métricas de cancelamento
        AnalyticsProjection cancelamento = obterOuCriarProjecao(hoje, TipoMetrica.CANCELAMENTO, "motivo", event.getMotivo());
        cancelamento.registrarCancelamento(valorSegurado, premio);
        
        analyticsRepository.save(geral);
        analyticsRepository.save(cancelamento);
    }
    
    /**
     * Processa renovação de apólice.
     */
    private void handleApoliceRenovada(ApoliceRenovadaEvent event) {
        log.info("Processando renovação de apólice para analytics: {}", event.getNumeroApolice());
        
        LocalDate hoje = LocalDate.now();
        BigDecimal novoValor = new BigDecimal(event.getNovoValorSegurado() != null ? event.getNovoValorSegurado() : "0");
        BigDecimal novoPremio = calcularPremio(novoValor);
        
        // Atualizar métricas gerais
        AnalyticsProjection geral = obterOuCriarProjecao(hoje, TipoMetrica.GERAL, null, null);
        geral.registrarRenovacao(novoValor, novoPremio);
        
        // Atualizar métricas de renovação
        AnalyticsProjection renovacao = obterOuCriarProjecao(hoje, TipoMetrica.RENOVACAO, null, null);
        renovacao.registrarRenovacao(novoValor, novoPremio);
        
        analyticsRepository.save(geral);
        analyticsRepository.save(renovacao);
    }
    
    /**
     * Processa adição de cobertura.
     */
    private void handleCoberturaAdicionada(CoberturaAdicionadaEvent event) {
        log.info("Processando adição de cobertura para analytics: {} - {}", 
                event.getNumeroApolice(), event.getTipoCobertura());
        
        LocalDate hoje = LocalDate.now();
        BigDecimal valorAdicional = new BigDecimal(event.getValorCobertura() != null ? event.getValorCobertura() : "0");
        
        // Atualizar métricas por produto
        AnalyticsProjection porProduto = obterOuCriarProjecao(hoje, TipoMetrica.POR_PRODUTO, "cobertura", event.getTipoCobertura());
        porProduto.setValorTotalSegurado(porProduto.getValorTotalSegurado().add(valorAdicional));
        
        analyticsRepository.save(porProduto);
    }
    
    // === MÉTODOS AUXILIARES ===
    
    /**
     * Obtém ou cria projeção analítica.
     */
    private AnalyticsProjection obterOuCriarProjecao(LocalDate data, TipoMetrica tipo, String dimensao, String valorDimensao) {
        return analyticsRepository
            .findByDataReferenciaAndTipoMetricaAndDimensaoAndValorDimensao(data, tipo, dimensao, valorDimensao)
            .orElseGet(() -> criarNovaProjecao(data, tipo, dimensao, valorDimensao));
    }
    
    /**
     * Cria nova projeção analítica.
     */
    private AnalyticsProjection criarNovaProjecao(LocalDate data, TipoMetrica tipo, String dimensao, String valorDimensao) {
        return AnalyticsProjection.builder()
            .id(UUID.randomUUID().toString())
            .dataReferencia(data)
            .tipoMetrica(tipo)
            .dimensao(dimensao)
            .valorDimensao(valorDimensao)
            .build();
    }
    
    /**
     * Simula estado do segurado.
     */
    private String simularEstado() {
        String[] estados = {"SP", "RJ", "MG", "RS", "PR", "SC", "BA", "GO", "PE", "CE"};
        return estados[(int) (Math.random() * estados.length)];
    }
    
    /**
     * Simula idade do segurado.
     */
    private int simularIdade() {
        return 25 + (int) (Math.random() * 40); // 25-65 anos
    }
    
    /**
     * Obtém faixa etária baseada na idade.
     */
    private String getFaixaEtaria(int idade) {
        if (idade >= 18 && idade <= 25) return "18-25";
        if (idade >= 26 && idade <= 35) return "26-35";
        if (idade >= 36 && idade <= 45) return "36-45";
        if (idade >= 46 && idade <= 55) return "46-55";
        if (idade >= 56 && idade <= 65) return "56-65";
        return "65+";
    }
    
    /**
     * Calcula prêmio baseado no valor segurado (simulado).
     */
    private BigDecimal calcularPremio(BigDecimal valorSegurado) {
        // Simulação: 5% do valor segurado
        return valorSegurado.multiply(BigDecimal.valueOf(0.05));
    }
    
    @Override
    public int getOrder() {
        return 200; // Baixa prioridade - processa depois das projeções principais
    }
    
    @Override
    public boolean isAsync() {
        return true; // Processamento assíncrono
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 60; // Timeout maior para processamento analítico
    }
}