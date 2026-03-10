package com.seguradora.hibrida.domain.analytics.service;

import com.seguradora.hibrida.domain.analytics.dto.*;
import com.seguradora.hibrida.domain.analytics.model.AnalyticsProjection;
import com.seguradora.hibrida.domain.analytics.model.TipoMetrica;
import com.seguradora.hibrida.domain.analytics.repository.AnalyticsProjectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serviço para geração de relatórios analíticos.
 * 
 * <p>Processa dados das projeções analíticas e gera relatórios
 * formatados para dashboards e exportação.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RelatorioService {
    
    private final AnalyticsProjectionRepository analyticsRepository;
    
    /**
     * Obtém dashboard executivo com métricas principais.
     */
    @Cacheable(value = "dashboard", key = "#data")
    public DashboardExecutivoView obterDashboardExecutivo(LocalDate data) {
        log.info("Gerando dashboard executivo para {}", data);
        
        try {
            // Obter resumo executivo
            Object[] resumo = analyticsRepository.getResumoExecutivo(data);
            
            if (resumo == null || resumo[0] == null) {
                return criarDashboardVazio(data);
            }
            
            // Obter métricas de performance
            Object[] performance = analyticsRepository.getMetricasPerformance(data);
            
            // Obter comparação com período anterior
            LocalDate dataAnterior = data.minusDays(30);
            Object[] comparacao = analyticsRepository.comparePeríodos(
                data, data, dataAnterior, dataAnterior, TipoMetrica.GERAL
            );
            
            return DashboardExecutivoView.builder()
                .dataReferencia(data)
                .totalSegurados(((Number) resumo[0]).longValue())
                .totalApolices(((Number) resumo[1]).longValue())
                .receitaTotal((BigDecimal) resumo[2])
                .taxaRenovacaoMedia(((Number) resumo[3]).doubleValue())
                .taxaCancelamentoMedia(((Number) resumo[4]).doubleValue())
                .novosSegurados(performance != null ? ((Number) performance[0]).longValue() : 0L)
                .novasApolices(performance != null ? ((Number) performance[1]).longValue() : 0L)
                .renovacoes(performance != null ? ((Number) performance[2]).longValue() : 0L)
                .cancelamentos(performance != null ? ((Number) performance[3]).longValue() : 0L)
                .premioMedio(performance != null ? (BigDecimal) performance[4] : BigDecimal.ZERO)
                .scoreMedioRenovacao(performance != null ? (BigDecimal) performance[5] : BigDecimal.ZERO)
                .crescimentoSegurados(calcularCrescimento(comparacao, 0, 1))
                .crescimentoApolices(calcularCrescimento(comparacao, 2, 3))
                .build();
                
        } catch (Exception ex) {
            log.error("Erro ao gerar dashboard executivo: {}", ex.getMessage(), ex);
            return criarDashboardVazio(data);
        }
    }
    
    /**
     * Obtém relatório de segurados por período.
     */
    @Cacheable(value = "relatorio-segurados", key = "#inicio + '-' + #fim")
    public RelatorioSeguradosView obterRelatorioSegurados(LocalDate inicio, LocalDate fim) {
        log.info("Gerando relatório de segurados de {} a {}", inicio, fim);
        
        try {
            // Obter métricas do período
            Long totalSegurados = analyticsRepository.sumTotalSeguradosPorPeriodo(inicio, fim, TipoMetrica.DIARIA);
            
            // Obter distribuição por região
            List<AnalyticsProjection> porRegiao = analyticsRepository.findMetricasPorRegiaoNaData(fim);
            Map<String, Long> distribuicaoRegiao = porRegiao.stream()
                .collect(Collectors.toMap(
                    AnalyticsProjection::getValorDimensao,
                    AnalyticsProjection::getTotalSegurados
                ));
            
            // Obter distribuição por faixa etária
            List<AnalyticsProjection> porIdade = analyticsRepository.findMetricasPorFaixaEtariaNaData(fim);
            Map<String, Long> distribuicaoIdade = porIdade.stream()
                .collect(Collectors.toMap(
                    AnalyticsProjection::getValorDimensao,
                    AnalyticsProjection::getTotalSegurados
                ));
            
            // Obter evolução temporal
            List<Object[]> crescimento = analyticsRepository.findCrescimentoSegurados(inicio);
            List<EvolucaoTemporalView> evolucao = crescimento.stream()
                .map(row -> EvolucaoTemporalView.builder()
                    .data((LocalDate) row[0])
                    .valor(((Number) row[1]).longValue())
                    .build())
                .collect(Collectors.toList());
            
            return RelatorioSeguradosView.builder()
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalSegurados(totalSegurados != null ? totalSegurados : 0L)
                .distribuicaoPorRegiao(distribuicaoRegiao)
                .distribuicaoPorFaixaEtaria(distribuicaoIdade)
                .evolucaoTemporal(evolucao)
                .build();
                
        } catch (Exception ex) {
            log.error("Erro ao gerar relatório de segurados: {}", ex.getMessage(), ex);
            throw new RuntimeException("Erro ao gerar relatório de segurados", ex);
        }
    }
    
    /**
     * Obtém relatório de apólices por período.
     */
    @Cacheable(value = "relatorio-apolices", key = "#inicio + '-' + #fim")
    public RelatorioApolicesView obterRelatorioApolices(LocalDate inicio, LocalDate fim) {
        log.info("Gerando relatório de apólices de {} a {}", inicio, fim);
        
        try {
            // Obter métricas do período
            Long totalApolices = analyticsRepository.sumTotalApolicesPorPeriodo(inicio, fim, TipoMetrica.DIARIA);
            Double premioMedio = analyticsRepository.avgPremioPorPeriodo(inicio, fim, TipoMetrica.DIARIA);
            Double taxaRenovacao = analyticsRepository.avgTaxaRenovacaoPorPeriodo(inicio, fim, TipoMetrica.DIARIA);
            
            // Obter distribuição por produto
            List<AnalyticsProjection> porProduto = analyticsRepository.findMetricasPorProdutoNaData(fim);
            Map<String, Long> distribuicaoProduto = porProduto.stream()
                .collect(Collectors.toMap(
                    AnalyticsProjection::getValorDimensao,
                    AnalyticsProjection::getTotalApolices
                ));
            
            // Obter distribuição por canal
            List<AnalyticsProjection> porCanal = analyticsRepository.findMetricasPorCanalNaData(fim);
            Map<String, Long> distribuicaoCanal = porCanal.stream()
                .collect(Collectors.toMap(
                    AnalyticsProjection::getValorDimensao,
                    AnalyticsProjection::getTotalApolices
                ));
            
            // Obter evolução de receita
            List<Object[]> evolucaoReceita = analyticsRepository.findEvolucaoReceita(inicio);
            List<EvolucaoTemporalView> evolucao = evolucaoReceita.stream()
                .map(row -> EvolucaoTemporalView.builder()
                    .data((LocalDate) row[0])
                    .valor(((BigDecimal) row[1]).longValue())
                    .build())
                .collect(Collectors.toList());
            
            return RelatorioApolicesView.builder()
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalApolices(totalApolices != null ? totalApolices : 0L)
                .premioMedio(premioMedio != null ? BigDecimal.valueOf(premioMedio) : BigDecimal.ZERO)
                .taxaRenovacao(taxaRenovacao != null ? BigDecimal.valueOf(taxaRenovacao) : BigDecimal.ZERO)
                .distribuicaoPorProduto(distribuicaoProduto)
                .distribuicaoPorCanal(distribuicaoCanal)
                .evolucaoReceita(evolucao)
                .build();
                
        } catch (Exception ex) {
            log.error("Erro ao gerar relatório de apólices: {}", ex.getMessage(), ex);
            throw new RuntimeException("Erro ao gerar relatório de apólices", ex);
        }
    }
    
    /**
     * Obtém relatório de performance operacional.
     */
    @Cacheable(value = "relatorio-performance", key = "#inicio + '-' + #fim")
    public RelatorioPerformanceView obterRelatorioPerformance(LocalDate inicio, LocalDate fim) {
        log.info("Gerando relatório de performance de {} a {}", inicio, fim);
        
        try {
            // Obter tendência de taxa de renovação
            List<Object[]> tendenciaRenovacao = analyticsRepository.findTendenciaTaxaRenovacao(inicio);
            List<EvolucaoTemporalView> evolucaoRenovacao = tendenciaRenovacao.stream()
                .map(row -> EvolucaoTemporalView.builder()
                    .data((LocalDate) row[0])
                    .valor(((BigDecimal) row[1]).longValue())
                    .build())
                .collect(Collectors.toList());
            
            // Obter top regiões
            List<AnalyticsProjection> topRegioes = analyticsRepository
                .findTopRegioesPorApolices(fim, PageRequest.of(0, 5));
            
            // Obter top produtos
            List<AnalyticsProjection> topProdutos = analyticsRepository
                .findTopProdutosPorReceita(fim, PageRequest.of(0, 5));
            
            // Obter top canais
            List<AnalyticsProjection> topCanais = analyticsRepository
                .findTopCanaisPorVolume(fim, PageRequest.of(0, 5));
            
            return RelatorioPerformanceView.builder()
                .periodoInicio(inicio)
                .periodoFim(fim)
                .evolucaoTaxaRenovacao(evolucaoRenovacao)
                .topRegioes(topRegioes.stream()
                    .collect(Collectors.toMap(
                        AnalyticsProjection::getValorDimensao,
                        AnalyticsProjection::getTotalApolices
                    )))
                .topProdutos(topProdutos.stream()
                    .collect(Collectors.toMap(
                        AnalyticsProjection::getValorDimensao,
                        p -> p.getPremioTotal().longValue()
                    )))
                .topCanais(topCanais.stream()
                    .collect(Collectors.toMap(
                        AnalyticsProjection::getValorDimensao,
                        AnalyticsProjection::getNovasApolices
                    )))
                .build();
                
        } catch (Exception ex) {
            log.error("Erro ao gerar relatório de performance: {}", ex.getMessage(), ex);
            throw new RuntimeException("Erro ao gerar relatório de performance", ex);
        }
    }
    
    /**
     * Obtém relatório de renovações.
     */
    @Cacheable(value = "relatorio-renovacoes", key = "#inicio + '-' + #fim")
    public RelatorioRenovacoesView obterRelatorioRenovacoes(LocalDate inicio, LocalDate fim) {
        log.info("Gerando relatório de renovações de {} a {}", inicio, fim);
        
        try {
            // Buscar projeções de renovação do período
            List<AnalyticsProjection> renovacoes = analyticsRepository
                .findByDataReferenciaBetweenAndTipoMetricaOrderByDataReferencia(inicio, fim, TipoMetrica.RENOVACAO);
            
            if (renovacoes.isEmpty()) {
                return RelatorioRenovacoesView.builder()
                    .periodoInicio(inicio)
                    .periodoFim(fim)
                    .totalRenovacoes(0L)
                    .taxaRenovacaoMedia(BigDecimal.ZERO)
                    .receitaRenovacoes(BigDecimal.ZERO)
                    .build();
            }
            
            // Calcular totais
            long totalRenovacoes = renovacoes.stream()
                .mapToLong(AnalyticsProjection::getRenovacoes)
                .sum();
            
            BigDecimal receitaTotal = renovacoes.stream()
                .map(AnalyticsProjection::getPremioTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            double taxaMedia = renovacoes.stream()
                .mapToDouble(p -> p.getTaxaRenovacao().doubleValue())
                .average()
                .orElse(0.0);
            
            return RelatorioRenovacoesView.builder()
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalRenovacoes(totalRenovacoes)
                .taxaRenovacaoMedia(BigDecimal.valueOf(taxaMedia))
                .receitaRenovacoes(receitaTotal)
                .build();
                
        } catch (Exception ex) {
            log.error("Erro ao gerar relatório de renovações: {}", ex.getMessage(), ex);
            throw new RuntimeException("Erro ao gerar relatório de renovações", ex);
        }
    }
    
    // === MÉTODOS AUXILIARES ===
    
    /**
     * Cria dashboard vazio para casos de erro.
     */
    private DashboardExecutivoView criarDashboardVazio(LocalDate data) {
        return DashboardExecutivoView.builder()
            .dataReferencia(data)
            .totalSegurados(0L)
            .totalApolices(0L)
            .receitaTotal(BigDecimal.ZERO)
            .taxaRenovacaoMedia(0.0)
            .taxaCancelamentoMedia(0.0)
            .novosSegurados(0L)
            .novasApolices(0L)
            .renovacoes(0L)
            .cancelamentos(0L)
            .premioMedio(BigDecimal.ZERO)
            .scoreMedioRenovacao(BigDecimal.ZERO)
            .crescimentoSegurados(BigDecimal.ZERO)
            .crescimentoApolices(BigDecimal.ZERO)
            .build();
    }
    
    /**
     * Calcula crescimento percentual entre dois valores.
     */
    private BigDecimal calcularCrescimento(Object[] comparacao, int indiceAtual, int indiceAnterior) {
        if (comparacao == null || comparacao.length <= Math.max(indiceAtual, indiceAnterior)) {
            return BigDecimal.ZERO;
        }
        
        Number atual = (Number) comparacao[indiceAtual];
        Number anterior = (Number) comparacao[indiceAnterior];
        
        if (atual == null || anterior == null || anterior.doubleValue() == 0) {
            return BigDecimal.ZERO;
        }
        
        double crescimento = ((atual.doubleValue() - anterior.doubleValue()) / anterior.doubleValue()) * 100;
        return BigDecimal.valueOf(crescimento).setScale(2, RoundingMode.HALF_UP);
    }
}