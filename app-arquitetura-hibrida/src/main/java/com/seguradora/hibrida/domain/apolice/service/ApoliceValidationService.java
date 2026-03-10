package com.seguradora.hibrida.domain.apolice.service;

import com.seguradora.hibrida.domain.apolice.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Serviço de validações relacionadas às apólices e segurados.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Service("apoliceValidationService")
public class ApoliceValidationService {
    
    private static final Logger log = LoggerFactory.getLogger(ApoliceValidationService.class);
    
    // Cache simulado para dados de segurados
    private final Map<String, SeguradoInfo> seguradosCache = new ConcurrentHashMap<>();
    
    // === MÉTODOS DE VALIDAÇÃO DE APÓLICE (SIMPLIFICADOS) ===
    
    /**
     * Valida vigência da apólice.
     */
    public void validarVigencia(Vigencia vigencia) {
        log.debug("Validando vigência: {} a {}", vigencia.getInicio(), vigencia.getFim());
        
        if (vigencia.getInicio().isAfter(vigencia.getFim())) {
            throw new IllegalArgumentException("Data de início não pode ser posterior à data de fim");
        }
        
        if (vigencia.getInicio().isBefore(LocalDate.now().minusDays(30))) {
            throw new IllegalArgumentException("Data de início não pode ser muito anterior à data atual");
        }
        
        log.debug("Vigência validada com sucesso");
    }
    
    /**
     * Valida valor segurado.
     */
    public void validarValorSegurado(Valor valorSegurado, String produto) {
        log.debug("Validando valor segurado para produto: {}", produto);
        
        if (!valorSegurado.isPositivo()) {
            throw new IllegalArgumentException("Valor segurado deve ser positivo");
        }
        
        log.debug("Valor segurado validado com sucesso");
    }
    
    /**
     * Valida coberturas da apólice.
     */
    public void validarCoberturas(List<Cobertura> coberturas, String produto) {
        log.debug("Validando {} coberturas para produto: {}", coberturas.size(), produto);
        
        if (coberturas.isEmpty()) {
            throw new IllegalArgumentException("Deve haver pelo menos uma cobertura");
        }
        
        log.debug("Coberturas validadas com sucesso");
    }
    
    /**
     * Valida combinação de coberturas.
     */
    public void validarCombinacaoCoberturas(List<Cobertura> coberturas) {
        log.debug("Validando combinação de {} coberturas", coberturas.size());
        
        if (coberturas.isEmpty()) {
            throw new IllegalArgumentException("Deve haver pelo menos uma cobertura");
        }
        
        log.debug("Combinação de coberturas validada com sucesso");
    }
    
    /**
     * Valida forma de pagamento.
     */
    public void validarFormaPagamento(FormaPagamento formaPagamento, Valor valorSegurado) {
        log.debug("Validando forma de pagamento para valor segurado");
        
        if (formaPagamento == null) {
            throw new IllegalArgumentException("Forma de pagamento é obrigatória");
        }
        
        if (!valorSegurado.isPositivo()) {
            throw new IllegalArgumentException("Valor segurado deve ser positivo");
        }
        
        log.debug("Forma de pagamento validada com sucesso");
    }
    
    /**
     * Valida renovação de apólice.
     */
    public void validarRenovacao(StatusApolice status, Vigencia vigenciaAtual, Vigencia novaVigencia) {
        log.debug("Validando renovação - status: {}", status);
        
        if (status == StatusApolice.CANCELADA) {
            throw new IllegalStateException("Apólice cancelada não pode ser renovada");
        }
        
        if (novaVigencia.getInicio().isBefore(vigenciaAtual.getFim())) {
            throw new IllegalArgumentException("Nova vigência deve ser posterior à vigência atual");
        }
        
        log.debug("Renovação validada com sucesso");
    }
    
    /**
     * Valida cancelamento de apólice.
     */
    public void validarCancelamento(StatusApolice status, Vigencia vigencia, LocalDate dataCancelamento) {
        log.debug("Validando cancelamento - status: {}", status);
        
        if (status == StatusApolice.CANCELADA) {
            throw new IllegalStateException("Apólice já está cancelada");
        }
        
        if (dataCancelamento.isBefore(vigencia.getInicio())) {
            throw new IllegalArgumentException("Data de cancelamento não pode ser anterior ao início da vigência");
        }
        
        log.debug("Cancelamento validado com sucesso");
    }
    
    /**
     * Valida alteração de apólice.
     */
    public void validarAlteracao(StatusApolice status, Vigencia vigencia) {
        log.debug("Validando alteração - status: {}", status);
        
        if (status == StatusApolice.CANCELADA) {
            throw new IllegalStateException("Apólice cancelada não pode ser alterada");
        }
        
        LocalDate hoje = LocalDate.now();
        if (hoje.isBefore(vigencia.getInicio()) || hoje.isAfter(vigencia.getFim())) {
            throw new IllegalArgumentException("Alterações só podem ser feitas durante a vigência");
        }
        
        log.debug("Alteração validada com sucesso");
    }
    
    // === MÉTODOS DE SEGURADO ===
    
    /**
     * Verifica se o segurado está ativo.
     */
    @Cacheable(value = "segurado-status", key = "#seguradoId")
    public boolean isSeguradoAtivo(String seguradoId) {
        log.debug("Verificando status do segurado: {}", seguradoId);
        
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
        
        SeguradoInfo info = obterInfoSegurado(seguradoId);
        HistoricoSinistros historico = info.getHistoricoSinistros();
        
        log.debug("Segurado {} possui {} sinistros nos últimos 5 anos", 
                 seguradoId, historico.getTotalSinistros());
        return historico;
    }
    
    // === MÉTODOS AUXILIARES ===
    
    private SeguradoInfo obterInfoSegurado(String seguradoId) {
        return seguradosCache.computeIfAbsent(seguradoId, id -> {
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