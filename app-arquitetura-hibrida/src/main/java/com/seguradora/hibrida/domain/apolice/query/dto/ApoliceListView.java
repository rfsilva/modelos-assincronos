package com.seguradora.hibrida.domain.apolice.query.dto;

import com.seguradora.hibrida.domain.apolice.model.StatusApolice;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para listagem de apólices com dados essenciais.
 * 
 * <p>Contém apenas os dados necessários para exibição em listas,
 * otimizando a transferência de dados.</p>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public record ApoliceListView(
    String id,
    String numero,
    String produto,
    StatusApolice status,
    
    // Dados do segurado
    String seguradoNome,
    String seguradoCpf,
    String seguradoCidade,
    String seguradoEstado,
    
    // Vigência
    LocalDate vigenciaInicio,
    LocalDate vigenciaFim,
    Integer diasParaVencimento,
    Boolean vencimentoProximo,
    
    // Valores
    BigDecimal valorSegurado,
    BigDecimal valorPremio,
    BigDecimal valorTotal,
    String formaPagamento,
    Integer parcelas,
    
    // Coberturas
    List<TipoCobertura> coberturas,
    String coberturasResumo,
    Boolean temCoberturaTotal,
    
    // Controle
    String operadorResponsavel,
    String canalVenda,
    Boolean renovacaoAutomatica,
    Integer scoreRenovacao
) {
    
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
     * Obtém descrição do status formatada.
     */
    public String getStatusDescricao() {
        if (status == null) return "N/A";
        
        return switch (status) {
            case ATIVA -> isVencida() ? "Vencida" : "Ativa";
            case CANCELADA -> "Cancelada";
            case SUSPENSA -> "Suspensa";
            case VENCIDA -> "Vencida";
        };
    }
    
    /**
     * Obtém classe CSS para status.
     */
    public String getStatusCssClass() {
        if (status == null) return "status-unknown";
        
        return switch (status) {
            case ATIVA -> isVencida() ? "status-expired" : "status-active";
            case CANCELADA -> "status-cancelled";
            case SUSPENSA -> "status-suspended";
            case VENCIDA -> "status-expired";
        };
    }
    
    /**
     * Obtém valor da parcela mensal.
     */
    public BigDecimal getValorParcela() {
        if (valorTotal == null || parcelas == null || parcelas <= 0) {
            return valorTotal;
        }
        return valorTotal.divide(BigDecimal.valueOf(parcelas), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Obtém resumo das coberturas formatado.
     */
    public String getCoberturasFormatadas() {
        if (coberturasResumo != null && !coberturasResumo.trim().isEmpty()) {
            return coberturasResumo;
        }
        
        if (coberturas == null || coberturas.isEmpty()) {
            return "Nenhuma cobertura";
        }
        
        if (Boolean.TRUE.equals(temCoberturaTotal)) {
            return "Cobertura Total";
        }
        
        return String.join(", ", coberturas.stream()
            .map(TipoCobertura::getDescricao)
            .toList());
    }
    
    /**
     * Obtém indicador de prioridade para renovação.
     */
    public String getPrioridadeRenovacao() {
        if (!Boolean.TRUE.equals(vencimentoProximo)) {
            return "BAIXA";
        }
        
        if (diasParaVencimento != null) {
            if (diasParaVencimento <= 7) return "ALTA";
            if (diasParaVencimento <= 15) return "MÉDIA";
            if (diasParaVencimento <= 30) return "BAIXA";
        }
        
        return "BAIXA";
    }
    
    /**
     * Verifica se precisa de atenção especial.
     */
    public boolean precisaAtencao() {
        return Boolean.TRUE.equals(vencimentoProximo) ||
               (scoreRenovacao != null && scoreRenovacao < 50) ||
               isVencida();
    }
}