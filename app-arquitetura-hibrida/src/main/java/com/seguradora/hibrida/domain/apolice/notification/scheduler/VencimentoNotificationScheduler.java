package com.seguradora.hibrida.domain.apolice.notification.scheduler;

import com.seguradora.hibrida.domain.apolice.notification.model.*;
import com.seguradora.hibrida.domain.apolice.notification.repository.ApoliceNotificationRepository;
import com.seguradora.hibrida.domain.apolice.notification.service.NotificationTemplateService;
import com.seguradora.hibrida.domain.apolice.query.model.ApoliceQueryModel;
import com.seguradora.hibrida.domain.apolice.query.repository.ApoliceQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Scheduler para detectar e notificar vencimentos próximos de apólices.
 * 
 * <p>Executa verificações periódicas para identificar apólices que estão
 * próximas do vencimento e gera notificações automáticas.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class VencimentoNotificationScheduler {
    
    private final ApoliceQueryRepository apoliceRepository;
    private final ApoliceNotificationRepository notificationRepository;
    private final NotificationTemplateService templateService;
    
    /**
     * Verifica vencimentos próximos a cada 6 horas.
     */
    @Scheduled(cron = "0 0 */6 * * *") // A cada 6 horas
    @Transactional
    public void verificarVencimentosProximos() {
        try {
            log.info("Iniciando verificação de vencimentos próximos");
            
            LocalDate hoje = LocalDate.now();
            
            // Verificar diferentes períodos de vencimento
            verificarVencimento30Dias(hoje);
            verificarVencimento15Dias(hoje);
            verificarVencimento7Dias(hoje);
            verificarVencimento1Dia(hoje);
            verificarApolicesVencidas(hoje);
            
            log.info("Verificação de vencimentos concluída");
            
        } catch (Exception ex) {
            log.error("Erro na verificação de vencimentos: {}", ex.getMessage(), ex);
        }
    }
    
    /**
     * Verifica apólices que vencem em 30 dias.
     */
    private void verificarVencimento30Dias(LocalDate hoje) {
        LocalDate dataVencimento = hoje.plusDays(30);
        
        List<ApoliceQueryModel> apolices = apoliceRepository
            .findByVigenciaFimAndStatusOrderByNumeroApolice(dataVencimento, "ATIVA");
        
        log.info("Encontradas {} apólices vencendo em 30 dias", apolices.size());
        
        for (ApoliceQueryModel apolice : apolices) {
            if (!jaNotificado(apolice.getId(), NotificationType.VENCIMENTO_30_DIAS)) {
                criarNotificacaoVencimento(apolice, NotificationType.VENCIMENTO_30_DIAS);
            }
        }
    }
    
    /**
     * Verifica apólices que vencem em 15 dias.
     */
    private void verificarVencimento15Dias(LocalDate hoje) {
        LocalDate dataVencimento = hoje.plusDays(15);
        
        List<ApoliceQueryModel> apolices = apoliceRepository
            .findByVigenciaFimAndStatusOrderByNumeroApolice(dataVencimento, "ATIVA");
        
        log.info("Encontradas {} apólices vencendo em 15 dias", apolices.size());
        
        for (ApoliceQueryModel apolice : apolices) {
            if (!jaNotificado(apolice.getId(), NotificationType.VENCIMENTO_15_DIAS)) {
                criarNotificacaoVencimento(apolice, NotificationType.VENCIMENTO_15_DIAS);
            }
        }
    }
    
    /**
     * Verifica apólices que vencem em 7 dias.
     */
    private void verificarVencimento7Dias(LocalDate hoje) {
        LocalDate dataVencimento = hoje.plusDays(7);
        
        List<ApoliceQueryModel> apolices = apoliceRepository
            .findByVigenciaFimAndStatusOrderByNumeroApolice(dataVencimento, "ATIVA");
        
        log.info("Encontradas {} apólices vencendo em 7 dias", apolices.size());
        
        for (ApoliceQueryModel apolice : apolices) {
            if (!jaNotificado(apolice.getId(), NotificationType.VENCIMENTO_7_DIAS)) {
                criarNotificacaoVencimento(apolice, NotificationType.VENCIMENTO_7_DIAS);
            }
        }
    }
    
    /**
     * Verifica apólices que vencem em 1 dia.
     */
    private void verificarVencimento1Dia(LocalDate hoje) {
        LocalDate dataVencimento = hoje.plusDays(1);
        
        List<ApoliceQueryModel> apolices = apoliceRepository
            .findByVigenciaFimAndStatusOrderByNumeroApolice(dataVencimento, "ATIVA");
        
        log.info("Encontradas {} apólices vencendo em 1 dia", apolices.size());
        
        for (ApoliceQueryModel apolice : apolices) {
            if (!jaNotificado(apolice.getId(), NotificationType.VENCIMENTO_1_DIA)) {
                criarNotificacaoVencimento(apolice, NotificationType.VENCIMENTO_1_DIA);
            }
        }
    }
    
    /**
     * Verifica apólices já vencidas.
     */
    private void verificarApolicesVencidas(LocalDate hoje) {
        List<ApoliceQueryModel> apolices = apoliceRepository
            .findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(hoje, "ATIVA");
        
        log.info("Encontradas {} apólices vencidas", apolices.size());
        
        for (ApoliceQueryModel apolice : apolices) {
            if (!jaNotificado(apolice.getId(), NotificationType.APOLICE_VENCIDA)) {
                criarNotificacaoVencimento(apolice, NotificationType.APOLICE_VENCIDA);
            }
        }
    }
    
    /**
     * Cria notificação de vencimento para uma apólice.
     */
    private void criarNotificacaoVencimento(ApoliceQueryModel apolice, NotificationType tipo) {
        try {
            log.info("Criando notificação {} para apólice {}", tipo, apolice.getNumeroApolice());
            
            Map<String, String> parameters = Map.of(
                "numeroApolice", apolice.getNumeroApolice(),
                "seguradoNome", apolice.getSeguradoNome(),
                "produto", apolice.getProduto(),
                "vigenciaFim", apolice.getVigenciaFim().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );
            
            // Obter canais preferenciais (simulado)
            NotificationChannel[] canais = getCanaisPreferenciais(apolice.getSeguradoId());
            
            for (NotificationChannel canal : canais) {
                criarNotificacao(apolice, tipo, canal, parameters);
            }
            
        } catch (Exception ex) {
            log.error("Erro ao criar notificação de vencimento para apólice {}: {}", 
                     apolice.getNumeroApolice(), ex.getMessage(), ex);
        }
    }
    
    /**
     * Cria uma notificação específica.
     */
    private void criarNotificacao(ApoliceQueryModel apolice, NotificationType tipo, 
                                 NotificationChannel canal, Map<String, String> parameters) {
        
        String titulo = templateService.generateTitle(tipo, canal, parameters);
        String mensagem = templateService.generateMessage(tipo, canal, parameters);
        
        ApoliceNotification notification = ApoliceNotification.builder()
            .id(UUID.randomUUID().toString())
            .apoliceId(apolice.getId())
            .seguradoId(apolice.getSeguradoId())
            .type(tipo)
            .channel(canal)
            .titulo(titulo)
            .mensagem(mensagem)
            .status(NotificationStatus.PENDING)
            .tentativas(0)
            .maxTentativas(getMaxTentativas(canal))
            .criadaEm(Instant.now())
            .agendadaPara(getAgendamento(tipo))
            .expiraEm(Instant.now().plusSeconds(templateService.getExpirationHours(tipo) * 3600))
            .metadata(new HashMap<>(parameters))
            .build();
        
        notificationRepository.save(notification);
        
        log.info("Notificação {} criada para apólice {} via {}", 
                tipo, apolice.getNumeroApolice(), canal);
    }
    
    /**
     * Verifica se já foi notificado para o tipo específico nas últimas 24 horas.
     */
    private boolean jaNotificado(String apoliceId, NotificationType tipo) {
        Instant cutoff = Instant.now().minusSeconds(24 * 3600); // 24 horas atrás
        
        return notificationRepository.existsByApoliceIdAndTypeAndCriadaEmGreaterThan(
            apoliceId, tipo, cutoff
        );
    }
    
    /**
     * Obtém canais preferenciais do segurado (simulado).
     */
    private NotificationChannel[] getCanaisPreferenciais(String seguradoId) {
        // Por enquanto, retorna canais baseados no tipo de vencimento
        return new NotificationChannel[] {
            NotificationChannel.EMAIL,
            NotificationChannel.SMS
        };
    }
    
    /**
     * Obtém número máximo de tentativas por canal.
     */
    private int getMaxTentativas(NotificationChannel canal) {
        return switch (canal) {
            case EMAIL -> 3;
            case SMS -> 2;
            case WHATSAPP -> 2;
            case PUSH -> 1;
            case IN_APP -> 1;
        };
    }
    
    /**
     * Obtém agendamento baseado no tipo de notificação.
     */
    private Instant getAgendamento(NotificationType tipo) {
        return switch (tipo) {
            case VENCIMENTO_1_DIA, APOLICE_VENCIDA -> Instant.now(); // Imediato
            case VENCIMENTO_7_DIAS -> Instant.now().plusSeconds(300); // 5 minutos
            case VENCIMENTO_15_DIAS -> Instant.now().plusSeconds(600); // 10 minutos
            case VENCIMENTO_30_DIAS -> Instant.now().plusSeconds(1800); // 30 minutos
            default -> Instant.now().plusSeconds(60); // 1 minuto
        };
    }
    
    /**
     * Verifica score de renovação baixo (executado semanalmente).
     */
    @Scheduled(cron = "0 0 8 * * MON") // Segunda-feira às 8:00
    @Transactional
    public void verificarScoreBaixo() {
        try {
            log.info("Verificando apólices com score de renovação baixo");
            
            // Buscar apólices ativas que vencem nos próximos 60 dias
            LocalDate limite = LocalDate.now().plusDays(60);
            List<ApoliceQueryModel> apolices = apoliceRepository
                .findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(limite, "ATIVA");
            
            int scoreBaixoCount = 0;
            
            for (ApoliceQueryModel apolice : apolices) {
                double score = calcularScoreRenovacao(apolice);
                
                if (score < 50.0 && !jaNotificado(apolice.getId(), NotificationType.SCORE_BAIXO)) {
                    criarNotificacaoScoreBaixo(apolice, score);
                    scoreBaixoCount++;
                }
            }
            
            log.info("Encontradas {} apólices com score baixo", scoreBaixoCount);
            
        } catch (Exception ex) {
            log.error("Erro na verificação de score baixo: {}", ex.getMessage(), ex);
        }
    }
    
    /**
     * Calcula score de renovação (simulado).
     */
    private double calcularScoreRenovacao(ApoliceQueryModel apolice) {
        // Algoritmo simulado de score
        double score = 100.0;
        
        // Reduzir score baseado em fatores simulados
        if (apolice.getValorSegurado().doubleValue() < 10000) {
            score -= 20; // Valor baixo
        }
        
        if (apolice.getCoberturas() == null || apolice.getCoberturas().size() < 2) {
            score -= 15; // Poucas coberturas
        }
        
        // Adicionar variação aleatória
        score += (Math.random() - 0.5) * 30;
        
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * Cria notificação de score baixo.
     */
    private void criarNotificacaoScoreBaixo(ApoliceQueryModel apolice, double score) {
        Map<String, String> parameters = Map.of(
            "numeroApolice", apolice.getNumeroApolice(),
            "seguradoNome", apolice.getSeguradoNome(),
            "scoreRenovacao", String.format("%.1f", score)
        );
        
        criarNotificacaoVencimento(apolice, NotificationType.SCORE_BAIXO);
    }
}