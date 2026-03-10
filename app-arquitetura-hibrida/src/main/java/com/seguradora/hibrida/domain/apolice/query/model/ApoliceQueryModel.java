package com.seguradora.hibrida.domain.apolice.query.model;

import com.seguradora.hibrida.domain.apolice.model.StatusApolice;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Query Model para consultas otimizadas de apólices com dados desnormalizados.
 * 
 * <p>Contém dados da apólice e do segurado desnormalizados para
 * consultas eficientes sem joins complexos.</p>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Entity
@Table(name = "apolice_view", schema = "projections", indexes = {
    @Index(name = "idx_apolice_numero", columnList = "numero", unique = true),
    @Index(name = "idx_apolice_segurado_cpf", columnList = "segurado_cpf"),
    @Index(name = "idx_apolice_vigencia_status", columnList = "vigencia_inicio, vigencia_fim, status"),
    @Index(name = "idx_apolice_vencimento", columnList = "vigencia_fim, status"),
    @Index(name = "idx_apolice_produto_status", columnList = "produto, status"),
    @Index(name = "idx_apolice_valor", columnList = "valor_total"),
    @Index(name = "idx_apolice_created", columnList = "created_at")
})
public class ApoliceQueryModel {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    // === DADOS DA APÓLICE ===
    
    @Column(name = "numero", length = 20, nullable = false, unique = true)
    private String numero;
    
    @Column(name = "produto", length = 50, nullable = false)
    private String produto;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StatusApolice status;
    
    @Column(name = "vigencia_inicio", nullable = false)
    private LocalDate vigenciaInicio;
    
    @Column(name = "vigencia_fim", nullable = false)
    private LocalDate vigenciaFim;
    
    @Column(name = "valor_segurado", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorSegurado;
    
    @Column(name = "valor_premio", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorPremio;
    
    @Column(name = "valor_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorTotal;
    
    @Column(name = "forma_pagamento", length = 20)
    private String formaPagamento;
    
    @Column(name = "parcelas")
    private Integer parcelas;
    
    // === DADOS DO SEGURADO (DESNORMALIZADOS) ===
    
    @Column(name = "segurado_id", length = 36, nullable = false)
    private String seguradoId;
    
    @Column(name = "segurado_cpf", length = 11, nullable = false)
    private String seguradoCpf;
    
    @Column(name = "segurado_nome", length = 100, nullable = false)
    private String seguradoNome;
    
    @Column(name = "segurado_email", length = 100)
    private String seguradoEmail;
    
    @Column(name = "segurado_telefone", length = 15)
    private String seguradoTelefone;
    
    @Column(name = "segurado_cidade", length = 50)
    private String seguradoCidade;
    
    @Column(name = "segurado_estado", length = 2)
    private String seguradoEstado;
    
    // === COBERTURAS (RESUMO) ===
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "apolice_coberturas_view", 
        schema = "projections",
        joinColumns = @JoinColumn(name = "apolice_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cobertura")
    private List<TipoCobertura> coberturas = new ArrayList<>();
    
    @Column(name = "coberturas_resumo", length = 500)
    private String coberturasResumo;
    
    @Column(name = "tem_cobertura_total")
    private Boolean temCoberturaTotal;
    
    // === DADOS DE CONTROLE ===
    
    @Column(name = "operador_responsavel", length = 50)
    private String operadorResponsavel;
    
    @Column(name = "canal_venda", length = 30)
    private String canalVenda;
    
    @Column(name = "observacoes", length = 1000)
    private String observacoes;
    
    // === MÉTRICAS E ALERTAS ===
    
    @Column(name = "dias_para_vencimento")
    private Integer diasParaVencimento;
    
    @Column(name = "vencimento_proximo")
    private Boolean vencimentoProximo;
    
    @Column(name = "renovacao_automatica")
    private Boolean renovacaoAutomatica;
    
    @Column(name = "score_renovacao")
    private Integer scoreRenovacao;
    
    // === AUDITORIA ===
    
    @Column(name = "last_event_id")
    private Long lastEventId;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    // === CONSTRUTORES ===
    
    protected ApoliceQueryModel() {
        // JPA
    }
    
    public ApoliceQueryModel(String id, String numero, String seguradoId) {
        this.id = Objects.requireNonNull(id, "ID não pode ser nulo");
        this.numero = Objects.requireNonNull(numero, "Número não pode ser nulo");
        this.seguradoId = Objects.requireNonNull(seguradoId, "ID do segurado não pode ser nulo");
    }
    
    // === MÉTODOS DE NEGÓCIO ===
    
    /**
     * Verifica se a apólice está ativa.
     */
    public boolean isAtiva() {
        return StatusApolice.ATIVA.equals(status);
    }
    
    /**
     * Verifica se a apólice está vencida.
     */
    public boolean isVencida() {
        return vigenciaFim != null && vigenciaFim.isBefore(LocalDate.now());
    }
    
    /**
     * Verifica se a apólice vence nos próximos dias.
     */
    public boolean venceEm(int dias) {
        if (vigenciaFim == null) return false;
        return vigenciaFim.isBefore(LocalDate.now().plusDays(dias + 1));
    }
    
    /**
     * Calcula dias restantes para vencimento.
     */
    public int calcularDiasParaVencimento() {
        if (vigenciaFim == null) return -1;
        return (int) LocalDate.now().until(vigenciaFim).getDays();
    }
    
    /**
     * Verifica se possui cobertura específica.
     */
    public boolean possuiCobertura(TipoCobertura tipo) {
        return coberturas != null && coberturas.contains(tipo);
    }
    
    /**
     * Obtém valor da franquia estimado (10% do valor segurado).
     */
    public BigDecimal getValorFranquiaEstimado() {
        if (valorSegurado == null) return BigDecimal.ZERO;
        return valorSegurado.multiply(BigDecimal.valueOf(0.10));
    }
    
    /**
     * Verifica se é elegível para renovação automática.
     */
    public boolean isElegivelRenovacaoAutomatica() {
        return Boolean.TRUE.equals(renovacaoAutomatica) && 
               isAtiva() && 
               venceEm(30) &&
               (scoreRenovacao == null || scoreRenovacao >= 70);
    }
    
    // === CALLBACKS JPA ===
    
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
        this.diasParaVencimento = calcularDiasParaVencimento();
        this.vencimentoProximo = venceEm(30);
    }
    
    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        onUpdate();
    }
    
    // === GETTERS E SETTERS ===
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    
    /**
     * Alias para getNumero() - compatibilidade com scheduler.
     */
    public String getNumeroApolice() { return numero; }
    
    public String getProduto() { return produto; }
    public void setProduto(String produto) { this.produto = produto; }
    
    public StatusApolice getStatus() { return status; }
    public void setStatus(StatusApolice status) { this.status = status; }
    
    public LocalDate getVigenciaInicio() { return vigenciaInicio; }
    public void setVigenciaInicio(LocalDate vigenciaInicio) { this.vigenciaInicio = vigenciaInicio; }
    
    public LocalDate getVigenciaFim() { return vigenciaFim; }
    public void setVigenciaFim(LocalDate vigenciaFim) { this.vigenciaFim = vigenciaFim; }
    
    public BigDecimal getValorSegurado() { return valorSegurado; }
    public void setValorSegurado(BigDecimal valorSegurado) { this.valorSegurado = valorSegurado; }
    
    public BigDecimal getValorPremio() { return valorPremio; }
    public void setValorPremio(BigDecimal valorPremio) { this.valorPremio = valorPremio; }
    
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
    
    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }
    
    public Integer getParcelas() { return parcelas; }
    public void setParcelas(Integer parcelas) { this.parcelas = parcelas; }
    
    public String getSeguradoId() { return seguradoId; }
    public void setSeguradoId(String seguradoId) { this.seguradoId = seguradoId; }
    
    public String getSeguradoCpf() { return seguradoCpf; }
    public void setSeguradoCpf(String seguradoCpf) { this.seguradoCpf = seguradoCpf; }
    
    public String getSeguradoNome() { return seguradoNome; }
    public void setSeguradoNome(String seguradoNome) { this.seguradoNome = seguradoNome; }
    
    public String getSeguradoEmail() { return seguradoEmail; }
    public void setSeguradoEmail(String seguradoEmail) { this.seguradoEmail = seguradoEmail; }
    
    public String getSeguradoTelefone() { return seguradoTelefone; }
    public void setSeguradoTelefone(String seguradoTelefone) { this.seguradoTelefone = seguradoTelefone; }
    
    public String getSeguradoCidade() { return seguradoCidade; }
    public void setSeguradoCidade(String seguradoCidade) { this.seguradoCidade = seguradoCidade; }
    
    public String getSeguradoEstado() { return seguradoEstado; }
    public void setSeguradoEstado(String seguradoEstado) { this.seguradoEstado = seguradoEstado; }
    
    public List<TipoCobertura> getCoberturas() { return coberturas; }
    public void setCoberturas(List<TipoCobertura> coberturas) { this.coberturas = coberturas; }
    
    public String getCoberturasResumo() { return coberturasResumo; }
    public void setCoberturasResumo(String coberturasResumo) { this.coberturasResumo = coberturasResumo; }
    
    public Boolean getTemCoberturaTotal() { return temCoberturaTotal; }
    public void setTemCoberturaTotal(Boolean temCoberturaTotal) { this.temCoberturaTotal = temCoberturaTotal; }
    
    public String getOperadorResponsavel() { return operadorResponsavel; }
    public void setOperadorResponsavel(String operadorResponsavel) { this.operadorResponsavel = operadorResponsavel; }
    
    public String getCanalVenda() { return canalVenda; }
    public void setCanalVenda(String canalVenda) { this.canalVenda = canalVenda; }
    
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    
    public Integer getDiasParaVencimento() { return diasParaVencimento; }
    public void setDiasParaVencimento(Integer diasParaVencimento) { this.diasParaVencimento = diasParaVencimento; }
    
    public Boolean getVencimentoProximo() { return vencimentoProximo; }
    public void setVencimentoProximo(Boolean vencimentoProximo) { this.vencimentoProximo = vencimentoProximo; }
    
    public Boolean getRenovacaoAutomatica() { return renovacaoAutomatica; }
    public void setRenovacaoAutomatica(Boolean renovacaoAutomatica) { this.renovacaoAutomatica = renovacaoAutomatica; }
    
    public Integer getScoreRenovacao() { return scoreRenovacao; }
    public void setScoreRenovacao(Integer scoreRenovacao) { this.scoreRenovacao = scoreRenovacao; }
    
    public Long getLastEventId() { return lastEventId; }
    public void setLastEventId(Long lastEventId) { this.lastEventId = lastEventId; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    // === EQUALS, HASHCODE E TOSTRING ===
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApoliceQueryModel that = (ApoliceQueryModel) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("ApoliceQueryModel{id='%s', numero='%s', segurado='%s', status=%s}", 
                           id, numero, seguradoNome, status);
    }
}