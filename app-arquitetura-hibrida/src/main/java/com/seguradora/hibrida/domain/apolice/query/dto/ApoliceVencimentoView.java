package com.seguradora.hibrida.domain.apolice.query.dto;

import com.seguradora.hibrida.domain.apolice.model.StatusApolice;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para alertas de vencimento de apólices.
 * 
 * <p>Contém dados essenciais para gestão de vencimentos
 * e renovações automáticas.</p>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public record ApoliceVencimentoView(
    String id,
    String numero,
    String produto,
    StatusApolice status,
    
    // Dados do segurado
    String seguradoNome,
    String seguradoCpf,
    String seguradoTelefone,
    String seguradoEmail,
    
    // Vencimento
    LocalDate vigenciaFim,
    Integer diasParaVencimento,
    String prioridadeVencimento,
    
    // Renovação
    Boolean renovacaoAutomatica,
    Integer scoreRenovacao,
    String statusRenovacao,
    
    // Valores
    BigDecimal valorTotal,
    String formaPagamento,
    
    // Controle
    String operadorResponsavel,
    Boolean precisaAcao
) {
    
    /**
     * Obtém prioridade baseada nos dias para vencimento.
     */
    public static String calcularPrioridade(Integer dias) {
        if (dias == null) return "INDEFINIDA";
        if (dias <= 0) return "VENCIDA";
        if (dias <= 7) return "CRÍTICA";
        if (dias <= 15) return "ALTA";
        if (dias <= 30) return "MÉDIA";
        return "BAIXA";
    }
    
    /**
     * Obtém status da renovação baseado no score.
     */
    public static String calcularStatusRenovacao(Boolean automatica, Integer score) {
        if (!Boolean.TRUE.equals(automatica)) {
            return "MANUAL";
        }
        
        if (score == null) return "PENDENTE_ANÁLISE";
        if (score >= 80) return "APROVADA";
        if (score >= 60) return "PROVÁVEL";
        if (score >= 40) return "DUVIDOSA";
        return "REJEITADA";
    }
    
    /**
     * Verifica se precisa de ação imediata.
     */
    public static boolean precisaAcaoImediata(Integer dias, Boolean automatica, Integer score) {
        // Vencida ou vence hoje
        if (dias != null && dias <= 0) return true;
        
        // Vence em poucos dias e não tem renovação automática
        if (dias != null && dias <= 7 && !Boolean.TRUE.equals(automatica)) return true;
        
        // Renovação automática com score baixo
        if (Boolean.TRUE.equals(automatica) && score != null && score < 50) return true;
        
        return false;
    }
    
    /**
     * Obtém cor do indicador de prioridade.
     */
    public String getCorPrioridade() {
        return switch (prioridadeVencimento) {
            case "VENCIDA" -> "#dc3545"; // Vermelho
            case "CRÍTICA" -> "#fd7e14"; // Laranja escuro
            case "ALTA" -> "#ffc107"; // Amarelo
            case "MÉDIA" -> "#17a2b8"; // Azul claro
            case "BAIXA" -> "#28a745"; // Verde
            default -> "#6c757d"; // Cinza
        };
    }
    
    /**
     * Obtém ícone para o status de renovação.
     */
    public String getIconeStatusRenovacao() {
        return switch (statusRenovacao) {
            case "APROVADA" -> "✅";
            case "PROVÁVEL" -> "🟡";
            case "DUVIDOSA" -> "⚠️";
            case "REJEITADA" -> "❌";
            case "MANUAL" -> "👤";
            default -> "⏳";
        };
    }
    
    /**
     * Obtém mensagem de ação recomendada.
     */
    public String getAcaoRecomendada() {
        if (diasParaVencimento == null) {
            return "Verificar data de vencimento";
        }
        
        if (diasParaVencimento <= 0) {
            return "Apólice vencida - Verificar reativação";
        }
        
        if (diasParaVencimento <= 7) {
            if (Boolean.TRUE.equals(renovacaoAutomatica)) {
                if (scoreRenovacao != null && scoreRenovacao >= 70) {
                    return "Renovação automática em andamento";
                } else {
                    return "Verificar aprovação da renovação";
                }
            } else {
                return "Contatar segurado para renovação";
            }
        }
        
        if (diasParaVencimento <= 15) {
            return "Preparar processo de renovação";
        }
        
        if (diasParaVencimento <= 30) {
            return "Agendar contato para renovação";
        }
        
        return "Monitorar vencimento";
    }
    
    /**
     * Verifica se é elegível para renovação automática.
     */
    public boolean isElegivelRenovacaoAutomatica() {
        return Boolean.TRUE.equals(renovacaoAutomatica) &&
               scoreRenovacao != null &&
               scoreRenovacao >= 60 &&
               StatusApolice.ATIVA.equals(status);
    }
}