package com.seguradora.hibrida.domain.apolice.query.dto;

import com.seguradora.hibrida.domain.apolice.model.StatusApolice;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO para visualização detalhada de apólices.
 * 
 * <p>Contém todos os dados da apólice, incluindo histórico
 * e informações relacionadas para visualização completa.</p>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public record ApoliceDetailView(
    // Identificação
    String id,
    String numero,
    String produto,
    StatusApolice status,
    
    // Dados do segurado completos
    String seguradoId,
    String seguradoNome,
    String seguradoCpf,
    String seguradoEmail,
    String seguradoTelefone,
    String seguradoCidade,
    String seguradoEstado,
    
    // Vigência detalhada
    LocalDate vigenciaInicio,
    LocalDate vigenciaFim,
    Integer diasParaVencimento,
    Boolean vencimentoProximo,
    Integer duracaoMeses,
    
    // Valores detalhados
    BigDecimal valorSegurado,
    BigDecimal valorPremio,
    BigDecimal valorTotal,
    BigDecimal valorFranquiaEstimado,
    String formaPagamento,
    Integer parcelas,
    BigDecimal valorParcela,
    
    // Coberturas detalhadas
    List<TipoCobertura> coberturas,
    String coberturasResumo,
    Boolean temCoberturaTotal,
    Map<TipoCobertura, BigDecimal> valoresPorCobertura,
    
    // Controle e gestão
    String operadorResponsavel,
    String canalVenda,
    String observacoes,
    Boolean renovacaoAutomatica,
    Integer scoreRenovacao,
    
    // Histórico e auditoria
    Instant criadaEm,
    Instant atualizadaEm,
    Long versao,
    Long ultimoEventoId,
    
    // Relacionamentos
    List<String> sinistrosRelacionados,
    List<String> renovacoesAnteriores,
    
    // Métricas e análises
    Map<String, Object> metricas,
    List<String> alertas,
    List<String> recomendacoes
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
     * Verifica se é elegível para renovação.
     */
    public boolean isElegivelRenovacao() {
        return isAtiva() && 
               Boolean.TRUE.equals(vencimentoProximo) &&
               (scoreRenovacao == null || scoreRenovacao >= 50);
    }
    
    /**
     * Obtém status detalhado com contexto.
     */
    public String getStatusDetalhado() {
        if (status == null) return "Status indefinido";
        
        return switch (status) {
            case ATIVA -> {
                if (isVencida()) yield "Ativa (Vencida)";
                if (Boolean.TRUE.equals(vencimentoProximo)) {
                    yield String.format("Ativa (Vence em %d dias)", diasParaVencimento);
                }
                yield "Ativa";
            }
            case CANCELADA -> "Cancelada";
            case SUSPENSA -> "Suspensa";
            case VENCIDA -> "Vencida";
        };
    }
    
    /**
     * Obtém período de vigência formatado.
     */
    public String getPeriodoVigencia() {
        if (vigenciaInicio == null || vigenciaFim == null) {
            return "Período não definido";
        }
        
        return String.format("%s a %s", 
            vigenciaInicio.toString(), 
            vigenciaFim.toString());
    }
    
    /**
     * Obtém resumo financeiro.
     */
    public String getResumoFinanceiro() {
        if (valorTotal == null) return "Valores não definidos";
        
        StringBuilder resumo = new StringBuilder();
        resumo.append(String.format("Total: R$ %.2f", valorTotal));
        
        if (parcelas != null && parcelas > 1) {
            resumo.append(String.format(" (%dx R$ %.2f)", parcelas, valorParcela));
        }
        
        return resumo.toString();
    }
    
    /**
     * Obtém lista de alertas ativos.
     */
    public List<String> getAlertasAtivos() {
        List<String> alertasAtivos = new java.util.ArrayList<>();
        
        if (Boolean.TRUE.equals(vencimentoProximo)) {
            alertasAtivos.add(String.format("Vence em %d dias", diasParaVencimento));
        }
        
        if (isVencida()) {
            alertasAtivos.add("Apólice vencida");
        }
        
        if (scoreRenovacao != null && scoreRenovacao < 50) {
            alertasAtivos.add("Score de renovação baixo");
        }
        
        if (alertas != null) {
            alertasAtivos.addAll(alertas);
        }
        
        return alertasAtivos;
    }
    
    /**
     * Obtém recomendações de ação.
     */
    public List<String> getRecomendacoesAcao() {
        List<String> acoes = new java.util.ArrayList<>();
        
        if (Boolean.TRUE.equals(vencimentoProximo) && isElegivelRenovacao()) {
            acoes.add("Iniciar processo de renovação");
        }
        
        if (scoreRenovacao != null && scoreRenovacao < 70) {
            acoes.add("Revisar condições para melhorar score");
        }
        
        if (isVencida()) {
            acoes.add("Verificar necessidade de reativação");
        }
        
        if (recomendacoes != null) {
            acoes.addAll(recomendacoes);
        }
        
        return acoes;
    }
    
    /**
     * Verifica se tem dados completos.
     */
    public boolean temDadosCompletos() {
        return numero != null && 
               seguradoNome != null && 
               vigenciaInicio != null && 
               vigenciaFim != null && 
               valorTotal != null &&
               coberturas != null && !coberturas.isEmpty();
    }
    
    /**
     * Obtém nível de completude dos dados (0-100).
     */
    public int getNivelCompletude() {
        int pontos = 0;
        int total = 10;
        
        if (numero != null) pontos++;
        if (seguradoNome != null) pontos++;
        if (vigenciaInicio != null && vigenciaFim != null) pontos++;
        if (valorTotal != null) pontos++;
        if (coberturas != null && !coberturas.isEmpty()) pontos++;
        if (operadorResponsavel != null) pontos++;
        if (canalVenda != null) pontos++;
        if (scoreRenovacao != null) pontos++;
        if (observacoes != null && !observacoes.trim().isEmpty()) pontos++;
        if (formaPagamento != null) pontos++;
        
        return (pontos * 100) / total;
    }
}