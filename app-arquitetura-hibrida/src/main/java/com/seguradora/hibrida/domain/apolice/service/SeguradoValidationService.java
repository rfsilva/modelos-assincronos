package com.seguradora.hibrida.domain.apolice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Serviço de validações relacionadas ao segurado.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Service
public class SeguradoValidationService {
    
    private static final Logger log = LoggerFactory.getLogger(SeguradoValidationService.class);
    
    // Cache simulado para dados de segurados
    private final Map<String, SeguradoInfo> seguradosCache = new ConcurrentHashMap<>();
    
    /**
     * Verifica se o segurado está ativo.
     */
    @Cacheable(value = "segurado-status", key = "#seguradoId")
    public boolean isSeguradoAtivo(String seguradoId) {
        log.debug("Verificando status do segurado: {}", seguradoId);
        
        // Simulação - em produção seria uma consulta ao banco/serviço
        SeguradoInfo info = obterInfoSegurado(seguradoId);
        boolean ativo = info.isAtivo();
        
        log.debug("Segurado {} está {}", seguradoId, ativo ? "ativo" : "inativo");
        return ativo;
    }
    
    /**
     * Conta o número de apólices ativas do segurado.
     */
    @Cacheable(value = "segurado-apolices", key = "#seguradoId")
    public long contarApolicesAtivas(String seguradoId) {
        log.debug("Contando apólices ativas do segurado: {}", seguradoId);
        
        // Simulação - em produção seria uma consulta ao banco
        SeguradoInfo info = obterInfoSegurado(seguradoId);
        long count = info.getApolicesAtivas();
        
        log.debug("Segurado {} possui {} apólices ativas", seguradoId, count);
        return count;
    }
    
    /**
     * Obtém o score de crédito do segurado.
     */
    @Cacheable(value = "segurado-score", key = "#seguradoId")
    public int obterScoreCredito(String seguradoId) {
        log.debug("Obtendo score de crédito do segurado: {}", seguradoId);
        
        // Simulação - em produção seria integração com bureaus de crédito
        SeguradoInfo info = obterInfoSegurado(seguradoId);
        int score = info.getScoreCredito();
        
        log.debug("Score de crédito do segurado {}: {}", seguradoId, score);
        return score;
    }
    
    /**
     * Verifica se o segurado tem restrições.
     */
    @Cacheable(value = "segurado-restricoes", key = "#seguradoId")
    public boolean temRestricoes(String seguradoId) {
        log.debug("Verificando restrições do segurado: {}", seguradoId);
        
        // Simulação - em produção seria consulta a órgãos de proteção ao crédito
        SeguradoInfo info = obterInfoSegurado(seguradoId);
        boolean restricoes = info.isTemRestricoes();
        
        log.debug("Segurado {} {} restrições", seguradoId, restricoes ? "possui" : "não possui");
        return restricoes;
    }
    
    /**
     * Obtém o histórico de sinistros do segurado.
     */
    @Cacheable(value = "segurado-sinistros", key = "#seguradoId")
    public HistoricoSinistros obterHistoricoSinistros(String seguradoId) {
        log.debug("Obtendo histórico de sinistros do segurado: {}", seguradoId);
        
        // Simulação - em produção seria consulta ao histórico
        SeguradoInfo info = obterInfoSegurado(seguradoId);
        HistoricoSinistros historico = info.getHistoricoSinistros();
        
        log.debug("Segurado {} possui {} sinistros nos últimos 5 anos", 
                 seguradoId, historico.getTotalSinistros());
        return historico;
    }
    
    /**
     * Valida se o segurado pode contratar nova apólice.
     */
    public void validarElegibilidade(String seguradoId) {
        log.debug("Validando elegibilidade do segurado: {}", seguradoId);
        
        // Verificar se está ativo
        if (!isSeguradoAtivo(seguradoId)) {
            throw new IllegalArgumentException("Segurado não está ativo: " + seguradoId);
        }
        
        // Verificar restrições
        if (temRestricoes(seguradoId)) {
            throw new IllegalArgumentException("Segurado possui restrições: " + seguradoId);
        }
        
        // Verificar score mínimo
        int scoreMinimo = 300;
        int score = obterScoreCredito(seguradoId);
        if (score < scoreMinimo) {
            throw new IllegalArgumentException(
                String.format("Score insuficiente: %d (mínimo: %d)", score, scoreMinimo)
            );
        }
        
        // Verificar histórico de sinistros
        HistoricoSinistros historico = obterHistoricoSinistros(seguradoId);
        if (historico.getTotalSinistros() > 5) {
            throw new IllegalArgumentException("Muitos sinistros no histórico: " + historico.getTotalSinistros());
        }
        
        log.debug("Segurado {} elegível para nova apólice", seguradoId);
    }
    
    private SeguradoInfo obterInfoSegurado(String seguradoId) {
        return seguradosCache.computeIfAbsent(seguradoId, id -> {
            // Simulação de dados - em produção viria do banco/serviço
            return new SeguradoInfo(
                    id,
                    true, // ativo
                    ThreadLocalRandom.current().nextLong(0, 6), // 0-5 apólices
                    ThreadLocalRandom.current().nextInt(250, 850), // score 250-850
                    ThreadLocalRandom.current().nextBoolean() && ThreadLocalRandom.current().nextDouble() < 0.1, // 10% chance de restrições
                    new HistoricoSinistros(ThreadLocalRandom.current().nextInt(0, 8)) // 0-7 sinistros
            );
        });
    }
    
    /**
     * Classe interna para informações do segurado.
     */
    private static class SeguradoInfo {
        private final String id;
        private final boolean ativo;
        private final long apolicesAtivas;
        private final int scoreCredito;
        private final boolean temRestricoes;
        private final HistoricoSinistros historicoSinistros;
        
        public SeguradoInfo(String id, boolean ativo, long apolicesAtivas, int scoreCredito, 
                           boolean temRestricoes, HistoricoSinistros historicoSinistros) {
            this.id = id;
            this.ativo = ativo;
            this.apolicesAtivas = apolicesAtivas;
            this.scoreCredito = scoreCredito;
            this.temRestricoes = temRestricoes;
            this.historicoSinistros = historicoSinistros;
        }
        
        public String getId() { return id; }
        public boolean isAtivo() { return ativo; }
        public long getApolicesAtivas() { return apolicesAtivas; }
        public int getScoreCredito() { return scoreCredito; }
        public boolean isTemRestricoes() { return temRestricoes; }
        public HistoricoSinistros getHistoricoSinistros() { return historicoSinistros; }
    }
    
    /**
     * Classe para histórico de sinistros.
     */
    public static class HistoricoSinistros {
        private final int totalSinistros;
        
        public HistoricoSinistros(int totalSinistros) {
            this.totalSinistros = totalSinistros;
        }
        
        public int getTotalSinistros() { return totalSinistros; }
        
        public boolean isPerfilAltoRisco() {
            return totalSinistros > 3;
        }
        
        public double getFatorRisco() {
            return 1.0 + (totalSinistros * 0.1); // 10% adicional por sinistro
        }
    }
}