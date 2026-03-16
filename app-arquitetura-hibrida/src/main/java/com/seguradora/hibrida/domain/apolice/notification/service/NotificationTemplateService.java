package com.seguradora.hibrida.domain.apolice.notification.service;

import com.seguradora.hibrida.domain.apolice.notification.model.NotificationChannel;
import com.seguradora.hibrida.domain.apolice.notification.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Serviço para geração de templates de notificação.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Service
public class NotificationTemplateService {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationTemplateService.class);
    
    /**
     * Gera título da notificação.
     */
    public String generateTitle(NotificationType type, NotificationChannel channel, Map<String, String> parameters) {
        log.debug("Gerando título para tipo {} e canal {}", type, channel);

        // Handle null parameters gracefully
        if (parameters == null) {
            parameters = Map.of();
        }

        final Map<String, String> params = parameters;

        return switch (type) {
            case APOLICE_CRIADA -> "✅ Apólice Criada - " + params.getOrDefault("numeroApolice", "N/A");
            case APOLICE_ATUALIZADA -> "📝 Apólice Atualizada - " + params.getOrDefault("numeroApolice", "N/A");
            case APOLICE_CANCELADA -> "❌ Apólice Cancelada - " + params.getOrDefault("numeroApolice", "N/A");
            case APOLICE_RENOVADA -> "🔄 Apólice Renovada - " + params.getOrDefault("numeroApolice", "N/A");

            case VENCIMENTO_30_DIAS -> "⏰ Vencimento em 30 dias - " + params.getOrDefault("numeroApolice", "N/A");
            case VENCIMENTO_15_DIAS -> "⚠️ Vencimento em 15 dias - " + params.getOrDefault("numeroApolice", "N/A");
            case VENCIMENTO_7_DIAS -> "🚨 Vencimento em 7 dias - " + params.getOrDefault("numeroApolice", "N/A");
            case VENCIMENTO_1_DIA -> "🔴 Vence AMANHÃ - " + params.getOrDefault("numeroApolice", "N/A");
            case APOLICE_VENCIDA -> "💥 APÓLICE VENCIDA - " + params.getOrDefault("numeroApolice", "N/A");

            case COBERTURA_ADICIONADA -> "➕ Nova Cobertura - " + params.getOrDefault("numeroApolice", "N/A");
            case RENOVACAO_AUTOMATICA -> "🤖 Renovação Automática - " + params.getOrDefault("numeroApolice", "N/A");
            case SCORE_BAIXO -> "📉 Score Baixo - " + params.getOrDefault("numeroApolice", "N/A");
        };
    }
    
    /**
     * Gera mensagem da notificação.
     */
    public String generateMessage(NotificationType type, NotificationChannel channel, Map<String, String> parameters) {
        log.debug("Gerando mensagem para tipo {} e canal {}", type, channel);

        // Handle null parameters gracefully
        if (parameters == null) {
            parameters = Map.of();
        }

        String nomeCliente = parameters.getOrDefault("seguradoNome", "Cliente");
        String numeroApolice = parameters.getOrDefault("numeroApolice", "N/A");
        String produto = parameters.getOrDefault("produto", "Seguro");

        return switch (type) {
            case APOLICE_CRIADA -> generateApoliceCriadaMessage(nomeCliente, numeroApolice, produto, channel, parameters);
            case APOLICE_ATUALIZADA -> generateApoliceAtualizadaMessage(nomeCliente, numeroApolice, channel, parameters);
            case APOLICE_CANCELADA -> generateApoliceCanceladaMessage(nomeCliente, numeroApolice, channel, parameters);
            case APOLICE_RENOVADA -> generateApoliceRenovadaMessage(nomeCliente, numeroApolice, channel, parameters);
            
            case VENCIMENTO_30_DIAS -> generateVencimento30Message(nomeCliente, numeroApolice, channel, parameters);
            case VENCIMENTO_15_DIAS -> generateVencimento15Message(nomeCliente, numeroApolice, channel, parameters);
            case VENCIMENTO_7_DIAS -> generateVencimento7Message(nomeCliente, numeroApolice, channel, parameters);
            case VENCIMENTO_1_DIA -> generateVencimento1Message(nomeCliente, numeroApolice, channel, parameters);
            case APOLICE_VENCIDA -> generateApoliceVencidaMessage(nomeCliente, numeroApolice, channel, parameters);
            
            case COBERTURA_ADICIONADA -> generateCoberturaAdicionadaMessage(nomeCliente, numeroApolice, channel, parameters);
            case RENOVACAO_AUTOMATICA -> generateRenovacaoAutomaticaMessage(nomeCliente, numeroApolice, channel, parameters);
            case SCORE_BAIXO -> generateScoreBaixoMessage(nomeCliente, numeroApolice, channel, parameters);
        };
    }
    
    // === TEMPLATES POR TIPO ===
    
    private String generateApoliceCriadaMessage(String nome, String numero, String produto, NotificationChannel channel, Map<String, String> params) {
        String valor = params.getOrDefault("valorTotal", "N/A");
        String vigencia = params.getOrDefault("vigenciaFim", "N/A");
        
        return switch (channel) {
            case EMAIL -> String.format("""
                Olá %s,
                
                Sua apólice %s de %s foi criada com sucesso!
                
                📋 Detalhes:
                • Número: %s
                • Produto: %s
                • Valor Total: R$ %s
                • Vigência até: %s
                
                Sua apólice já está ativa e você está protegido.
                
                Em caso de dúvidas, entre em contato conosco.
                
                Atenciosamente,
                Equipe Seguradora
                """, nome, numero, produto, numero, produto, valor, vigencia);
                
            case SMS -> String.format("✅ %s, sua apólice %s foi criada! Valor: R$ %s. Vigência: %s. Você já está protegido!", 
                                     nome, numero, valor, vigencia);
                                     
            case WHATSAPP -> String.format("""
                🎉 Parabéns %s!
                
                Sua apólice %s foi criada com sucesso!
                
                📋 Resumo:
                • Produto: %s
                • Valor: R$ %s
                • Vigência: %s
                
                Você já está protegido! 🛡️
                """, nome, numero, produto, valor, vigencia);
                
            default -> String.format("Apólice %s criada para %s. Valor: R$ %s", numero, nome, valor);
        };
    }
    
    private String generateApoliceAtualizadaMessage(String nome, String numero, NotificationChannel channel, Map<String, String> params) {
        String alteracoes = params.getOrDefault("alteracoes", "dados da apólice");
        
        return switch (channel) {
            case EMAIL -> String.format("""
                Olá %s,
                
                Sua apólice %s foi atualizada.
                
                📝 Alterações realizadas:
                %s
                
                As alterações já estão em vigor.
                
                Atenciosamente,
                Equipe Seguradora
                """, nome, numero, alteracoes);
                
            case SMS -> String.format("📝 %s, sua apólice %s foi atualizada. Alterações: %s", nome, numero, alteracoes);
            
            default -> String.format("Apólice %s atualizada para %s", numero, nome);
        };
    }
    
    private String generateApoliceCanceladaMessage(String nome, String numero, NotificationChannel channel, Map<String, String> params) {
        String motivo = params.getOrDefault("motivo", "solicitação");
        String reembolso = params.getOrDefault("valorReembolso", "0");
        
        return switch (channel) {
            case EMAIL -> String.format("""
                Olá %s,
                
                Sua apólice %s foi cancelada.
                
                📋 Informações:
                • Motivo: %s
                • Valor de reembolso: R$ %s
                
                O reembolso será processado em até 5 dias úteis.
                
                Atenciosamente,
                Equipe Seguradora
                """, nome, numero, motivo, reembolso);
                
            case SMS -> String.format("❌ %s, apólice %s cancelada. Motivo: %s. Reembolso: R$ %s", nome, numero, motivo, reembolso);
            
            default -> String.format("Apólice %s cancelada para %s", numero, nome);
        };
    }
    
    private String generateApoliceRenovadaMessage(String nome, String numero, NotificationChannel channel, Map<String, String> params) {
        String novaVigencia = params.getOrDefault("novaVigenciaFim", "N/A");
        String novoValor = params.getOrDefault("novoValorTotal", "N/A");
        
        return switch (channel) {
            case EMAIL -> String.format("""
                Olá %s,
                
                Sua apólice %s foi renovada com sucesso!
                
                📋 Nova vigência:
                • Válida até: %s
                • Novo valor: R$ %s
                
                Sua proteção continua ativa.
                
                Atenciosamente,
                Equipe Seguradora
                """, nome, numero, novaVigencia, novoValor);
                
            case SMS -> String.format("🔄 %s, apólice %s renovada! Nova vigência: %s. Valor: R$ %s", nome, numero, novaVigencia, novoValor);
            
            default -> String.format("Apólice %s renovada para %s", numero, nome);
        };
    }
    
    private String generateVencimento30Message(String nome, String numero, NotificationChannel channel, Map<String, String> params) {
        String vigenciaFim = params.getOrDefault("vigenciaFim", "N/A");
        
        return switch (channel) {
            case EMAIL -> String.format("""
                Olá %s,
                
                Sua apólice %s vence em 30 dias (%s).
                
                🔄 Para renovar:
                • Acesse nosso portal
                • Entre em contato conosco
                • Aguarde nossa ligação
                
                Não deixe sua proteção vencer!
                
                Atenciosamente,
                Equipe Seguradora
                """, nome, numero, vigenciaFim);
                
            case SMS -> String.format("⏰ %s, sua apólice %s vence em 30 dias (%s). Renove já!", nome, numero, vigenciaFim);
            
            default -> String.format("Apólice %s vence em 30 dias", numero);
        };
    }
    
    private String generateVencimento15Message(String nome, String numero, NotificationChannel channel, Map<String, String> params) {
        String vigenciaFim = params.getOrDefault("vigenciaFim", "N/A");
        
        return switch (channel) {
            case EMAIL -> String.format("""
                Olá %s,
                
                ⚠️ ATENÇÃO: Sua apólice %s vence em apenas 15 dias (%s).
                
                🚨 Ação necessária:
                • Renove AGORA para não perder a proteção
                • Entre em contato urgente conosco
                • Evite ficar desprotegido
                
                Não deixe para a última hora!
                
                Atenciosamente,
                Equipe Seguradora
                """, nome, numero, vigenciaFim);
                
            case SMS -> String.format("⚠️ URGENTE %s! Apólice %s vence em 15 dias (%s). RENOVE JÁ!", nome, numero, vigenciaFim);
            
            default -> String.format("URGENTE: Apólice %s vence em 15 dias", numero);
        };
    }
    
    private String generateVencimento7Message(String nome, String numero, NotificationChannel channel, Map<String, String> params) {
        String vigenciaFim = params.getOrDefault("vigenciaFim", "N/A");
        
        return switch (channel) {
            case EMAIL -> String.format("""
                Olá %s,
                
                🚨 CRÍTICO: Sua apólice %s vence em apenas 7 dias (%s)!
                
                ⚡ AÇÃO IMEDIATA NECESSÁRIA:
                • RENOVE HOJE mesmo
                • Ligue AGORA: (11) 3000-0000
                • Acesse: www.seguradora.com/renovacao
                
                ⚠️ Após o vencimento você ficará SEM PROTEÇÃO!
                
                Equipe Seguradora
                """, nome, numero, vigenciaFim);
                
            case SMS -> String.format("🚨 CRÍTICO %s! Apólice %s vence em 7 dias (%s)! RENOVE HOJE! Tel: (11) 3000-0000", nome, numero, vigenciaFim);
            
            default -> String.format("CRÍTICO: Apólice %s vence em 7 dias!", numero);
        };
    }
    
    private String generateVencimento1Message(String nome, String numero, NotificationChannel channel, Map<String, String> params) {
        return switch (channel) {
            case EMAIL -> String.format("""
                %s,
                
                🔴 ÚLTIMA CHANCE: Sua apólice %s vence AMANHÃ!
                
                ⚡ RENOVE AGORA OU PERCA A PROTEÇÃO:
                • Ligue URGENTE: (11) 3000-0000
                • WhatsApp: (11) 99999-9999
                • Site: www.seguradora.com/emergencia
                
                ⚠️ Após amanhã você ficará DESPROTEGIDO!
                
                Equipe Seguradora
                """, nome, numero);
                
            case SMS -> String.format("🔴 ÚLTIMA CHANCE %s! Apólice %s vence AMANHÃ! LIGUE JÁ: (11) 3000-0000", nome, numero);
            
            default -> String.format("ÚLTIMA CHANCE: Apólice %s vence AMANHÃ!", numero);
        };
    }
    
    private String generateApoliceVencidaMessage(String nome, String numero, NotificationChannel channel, Map<String, String> params) {
        return switch (channel) {
            case EMAIL -> String.format("""
                %s,
                
                💥 SUA APÓLICE %s ESTÁ VENCIDA!
                
                ⚠️ VOCÊ ESTÁ SEM PROTEÇÃO:
                • Renove IMEDIATAMENTE
                • Ligue AGORA: (11) 3000-0000
                • Atendimento 24h disponível
                
                Não fique desprotegido!
                
                Equipe Seguradora
                """, nome, numero);
                
            case SMS -> String.format("💥 %s, APÓLICE %s VENCIDA! Você está SEM PROTEÇÃO! LIGUE JÁ: (11) 3000-0000", nome, numero);
            
            default -> String.format("APÓLICE VENCIDA: %s está sem proteção!", numero);
        };
    }
    
    private String generateCoberturaAdicionadaMessage(String nome, String numero, NotificationChannel channel, Map<String, String> params) {
        String cobertura = params.getOrDefault("tipoCobertura", "nova cobertura");
        String valor = params.getOrDefault("valorAdicional", "N/A");
        
        return switch (channel) {
            case EMAIL -> String.format("""
                Olá %s,
                
                ➕ Nova cobertura adicionada à sua apólice %s!
                
                📋 Detalhes:
                • Cobertura: %s
                • Valor adicional: R$ %s
                
                Sua proteção foi ampliada.
                
                Atenciosamente,
                Equipe Seguradora
                """, nome, numero, cobertura, valor);
                
            case SMS -> String.format("➕ %s, nova cobertura %s adicionada à apólice %s. Valor: R$ %s", nome, cobertura, numero, valor);
            
            default -> String.format("Nova cobertura adicionada à apólice %s", numero);
        };
    }
    
    private String generateRenovacaoAutomaticaMessage(String nome, String numero, NotificationChannel channel, Map<String, String> params) {
        String novaVigencia = params.getOrDefault("novaVigenciaFim", "N/A");
        
        return switch (channel) {
            case EMAIL -> String.format("""
                Olá %s,
                
                🤖 Sua apólice %s será renovada automaticamente!
                
                📋 Detalhes:
                • Nova vigência até: %s
                • Renovação automática ativa
                • Sem interrupção na proteção
                
                Você não precisa fazer nada.
                
                Atenciosamente,
                Equipe Seguradora
                """, nome, numero, novaVigencia);
                
            case SMS -> String.format("🤖 %s, apólice %s será renovada automaticamente até %s. Sem ação necessária!", nome, numero, novaVigencia);
            
            default -> String.format("Renovação automática configurada para apólice %s", numero);
        };
    }
    
    private String generateScoreBaixoMessage(String nome, String numero, NotificationChannel channel, Map<String, String> params) {
        String score = params.getOrDefault("scoreRenovacao", "N/A");
        
        return switch (channel) {
            case EMAIL -> String.format("""
                Olá %s,
                
                📉 Sua apólice %s tem score baixo para renovação (%s).
                
                💡 Para melhorar:
                • Mantenha dados atualizados
                • Quite em dia os pagamentos
                • Entre em contato conosco
                
                Vamos ajudar você a melhorar!
                
                Atenciosamente,
                Equipe Seguradora
                """, nome, numero, score);
                
            case SMS -> String.format("📉 %s, apólice %s com score baixo (%s). Entre em contato para melhorar!", nome, numero, score);
            
            default -> String.format("Score baixo para apólice %s: %s", numero, score);
        };
    }
    
    /**
     * Obtém nome do template baseado no tipo e canal.
     */
    public String getTemplateName(NotificationType type, NotificationChannel channel) {
        return String.format("%s_%s", type.name().toLowerCase(), channel.name().toLowerCase());
    }
    
    /**
     * Valida se o template existe para o tipo e canal.
     */
    public boolean hasTemplate(NotificationType type, NotificationChannel channel) {
        // Por enquanto, todos os tipos têm templates para todos os canais
        return true;
    }
    
    /**
     * Obtém configuração de expiração para o tipo de notificação.
     */
    public long getExpirationHours(NotificationType type) {
        return switch (type) {
            case VENCIMENTO_1_DIA, APOLICE_VENCIDA -> 6; // 6 horas
            case VENCIMENTO_7_DIAS -> 24; // 1 dia
            case VENCIMENTO_15_DIAS -> 48; // 2 dias
            case VENCIMENTO_30_DIAS -> 72; // 3 dias
            case APOLICE_CRIADA, APOLICE_RENOVADA -> 168; // 7 dias
            default -> 24; // 1 dia padrão
        };
    }
}