package com.seguradora.hibrida.domain.analytics.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Projeção analítica para dados agregados de segurados e apólices.
 * 
 * <p>Mantém métricas pré-calculadas para relatórios e dashboards,
 * atualizadas em tempo real através de eventos de domínio.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Entity
@Table(name = "analytics_projection", schema = "projections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AnalyticsProjection {
    
    @Id
    private String id;
    
    // === IDENTIFICAÇÃO ===
    
    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;
    
    @Column(name = "tipo_metrica", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TipoMetrica tipoMetrica;
    
    @Column(name = "dimensao", length = 100)
    private String dimensao; // região, produto, canal, etc.
    
    @Column(name = "valor_dimensao", length = 100)
    private String valorDimensao; // SP, Seguro Auto, Online, etc.
    
    // === MÉTRICAS DE SEGURADOS ===
    
    @Column(name = "total_segurados")
    private Long totalSegurados = 0L;
    
    @Column(name = "segurados_ativos")
    private Long seguradosAtivos = 0L;
    
    @Column(name = "segurados_inativos")
    private Long seguradosInativos = 0L;
    
    @Column(name = "novos_segurados")
    private Long novosSegurados = 0L;
    
    @Column(name = "segurados_cancelados")
    private Long seguradosCancelados = 0L;
    
    // === MÉTRICAS DE APÓLICES ===
    
    @Column(name = "total_apolices")
    private Long totalApolices = 0L;
    
    @Column(name = "apolices_ativas")
    private Long apolicesAtivas = 0L;
    
    @Column(name = "apolices_vencidas")
    private Long apolicesVencidas = 0L;
    
    @Column(name = "apolices_canceladas")
    private Long apolicesCanceladas = 0L;
    
    @Column(name = "novas_apolices")
    private Long novasApolices = 0L;
    
    @Column(name = "renovacoes")
    private Long renovacoes = 0L;
    
    // === MÉTRICAS FINANCEIRAS ===
    
    @Column(name = "valor_total_segurado", precision = 15, scale = 2)
    private BigDecimal valorTotalSegurado = BigDecimal.ZERO;
    
    @Column(name = "premio_total", precision = 15, scale = 2)
    private BigDecimal premioTotal = BigDecimal.ZERO;
    
    @Column(name = "premio_medio", precision = 15, scale = 2)
    private BigDecimal premioMedio = BigDecimal.ZERO;
    
    @Column(name = "valor_medio_segurado", precision = 15, scale = 2)
    private BigDecimal valorMedioSegurado = BigDecimal.ZERO;
    
    // === MÉTRICAS DE PERFORMANCE ===
    
    @Column(name = "taxa_renovacao", precision = 5, scale = 2)
    private BigDecimal taxaRenovacao = BigDecimal.ZERO;
    
    @Column(name = "taxa_cancelamento", precision = 5, scale = 2)
    private BigDecimal taxaCancelamento = BigDecimal.ZERO;
    
    @Column(name = "taxa_crescimento", precision = 5, scale = 2)
    private BigDecimal taxaCrescimento = BigDecimal.ZERO;
    
    @Column(name = "score_medio_renovacao", precision = 5, scale = 2)
    private BigDecimal scoreMedioRenovacao = BigDecimal.ZERO;
    
    // === DISTRIBUIÇÃO POR FAIXA ETÁRIA ===
    
    @Column(name = "faixa_18_25")
    private Long faixa18a25 = 0L;
    
    @Column(name = "faixa_26_35")
    private Long faixa26a35 = 0L;
    
    @Column(name = "faixa_36_45")
    private Long faixa36a45 = 0L;
    
    @Column(name = "faixa_46_55")
    private Long faixa46a55 = 0L;
    
    @Column(name = "faixa_56_65")
    private Long faixa56a65 = 0L;
    
    @Column(name = "faixa_65_mais")
    private Long faixa65Mais = 0L;
    
    // === DISTRIBUIÇÃO POR REGIÃO ===
    
    @Column(name = "regiao_norte")
    private Long regiaoNorte = 0L;
    
    @Column(name = "regiao_nordeste")
    private Long regiaoNordeste = 0L;
    
    @Column(name = "regiao_centro_oeste")
    private Long regiaoCentroOeste = 0L;
    
    @Column(name = "regiao_sudeste")
    private Long regiaoSudeste = 0L;
    
    @Column(name = "regiao_sul")
    private Long regiaoSul = 0L;
    
    // === DISTRIBUIÇÃO POR CANAL ===
    
    @Column(name = "canal_online")
    private Long canalOnline = 0L;
    
    @Column(name = "canal_telefone")
    private Long canalTelefone = 0L;
    
    @Column(name = "canal_agencia")
    private Long canalAgencia = 0L;
    
    @Column(name = "canal_corretor")
    private Long canalCorretor = 0L;
    
    // === CONTROLE ===
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "last_event_id")
    private Long lastEventId;
    
    @Column(name = "version")
    private Long version = 0L;
    
    // === CALLBACKS ===
    
    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
        version++;
    }
    
    // === MÉTODOS DE NEGÓCIO ===
    
    /**
     * Incrementa contador de segurados.
     */
    public void incrementarSegurados(Long quantidade) {
        this.totalSegurados += quantidade;
        this.novosSegurados += quantidade;
        this.seguradosAtivos += quantidade;
    }
    
    /**
     * Incrementa contador de apólices.
     */
    public void incrementarApolices(Long quantidade, BigDecimal valorSegurado, BigDecimal premio) {
        this.totalApolices += quantidade;
        this.novasApolices += quantidade;
        this.apolicesAtivas += quantidade;
        
        this.valorTotalSegurado = this.valorTotalSegurado.add(valorSegurado);
        this.premioTotal = this.premioTotal.add(premio);
        
        recalcularMedias();
    }
    
    /**
     * Registra cancelamento de apólice.
     */
    public void registrarCancelamento(BigDecimal valorSegurado, BigDecimal premio) {
        this.apolicesAtivas = Math.max(0, this.apolicesAtivas - 1);
        this.apolicesCanceladas++;
        
        this.valorTotalSegurado = this.valorTotalSegurado.subtract(valorSegurado);
        this.premioTotal = this.premioTotal.subtract(premio);
        
        recalcularMedias();
        recalcularTaxas();
    }
    
    /**
     * Registra renovação de apólice.
     */
    public void registrarRenovacao(BigDecimal novoValorSegurado, BigDecimal novoPremio) {
        this.renovacoes++;
        
        this.valorTotalSegurado = this.valorTotalSegurado.add(novoValorSegurado);
        this.premioTotal = this.premioTotal.add(novoPremio);
        
        recalcularMedias();
        recalcularTaxas();
    }
    
    /**
     * Recalcula valores médios.
     */
    private void recalcularMedias() {
        if (apolicesAtivas > 0) {
            this.premioMedio = premioTotal.divide(BigDecimal.valueOf(apolicesAtivas), 2, BigDecimal.ROUND_HALF_UP);
            this.valorMedioSegurado = valorTotalSegurado.divide(BigDecimal.valueOf(apolicesAtivas), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.premioMedio = BigDecimal.ZERO;
            this.valorMedioSegurado = BigDecimal.ZERO;
        }
    }
    
    /**
     * Recalcula taxas de performance.
     */
    private void recalcularTaxas() {
        if (totalApolices > 0) {
            // Taxa de renovação
            this.taxaRenovacao = BigDecimal.valueOf(renovacoes)
                .divide(BigDecimal.valueOf(totalApolices), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            
            // Taxa de cancelamento
            this.taxaCancelamento = BigDecimal.valueOf(apolicesCanceladas)
                .divide(BigDecimal.valueOf(totalApolices), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
    }
    
    /**
     * Incrementa contador por faixa etária.
     */
    public void incrementarFaixaEtaria(int idade) {
        if (idade >= 18 && idade <= 25) {
            faixa18a25++;
        } else if (idade >= 26 && idade <= 35) {
            faixa26a35++;
        } else if (idade >= 36 && idade <= 45) {
            faixa36a45++;
        } else if (idade >= 46 && idade <= 55) {
            faixa46a55++;
        } else if (idade >= 56 && idade <= 65) {
            faixa56a65++;
        } else if (idade > 65) {
            faixa65Mais++;
        }
    }
    
    /**
     * Incrementa contador por região.
     */
    public void incrementarRegiao(String estado) {
        switch (estado.toUpperCase()) {
            case "AC", "AP", "AM", "PA", "RO", "RR", "TO" -> regiaoNorte++;
            case "AL", "BA", "CE", "MA", "PB", "PE", "PI", "RN", "SE" -> regiaoNordeste++;
            case "GO", "MT", "MS", "DF" -> regiaoCentroOeste++;
            case "ES", "MG", "RJ", "SP" -> regiaoSudeste++;
            case "PR", "RS", "SC" -> regiaoSul++;
        }
    }
    
    /**
     * Incrementa contador por canal.
     */
    public void incrementarCanal(String canal) {
        switch (canal.toUpperCase()) {
            case "ONLINE", "WEB", "SITE" -> canalOnline++;
            case "TELEFONE", "CALL_CENTER" -> canalTelefone++;
            case "AGENCIA", "LOJA" -> canalAgencia++;
            case "CORRETOR", "BROKER" -> canalCorretor++;
        }
    }
}