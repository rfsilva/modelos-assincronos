package com.seguradora.hibrida.domain.apolice.notification.service;

import com.seguradora.hibrida.domain.apolice.notification.model.*;
import com.seguradora.hibrida.domain.apolice.notification.repository.ApoliceNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Serviço para envio de notificações de apólice.
 * 
 * <p>Processa notificações pendentes e as envia através dos
 * canais apropriados (Email, SMS, WhatsApp, Push, In-App).
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationSenderService {
    
    private final ApoliceNotificationRepository notificationRepository;
    
    /**
     * Processa notificações pendentes a cada 30 segundos.
     */
    @Scheduled(fixedDelay = 30000) // 30 segundos
    @Transactional
    public void processarNotificacoesPendentes() {
        try {
            List<ApoliceNotification> pendentes = notificationRepository
                .findByStatusAndScheduledAtLessThanEqual(
                    NotificationStatus.PENDING, 
                    Instant.now()
                );
            
            if (!pendentes.isEmpty()) {
                log.info("Processando {} notificações pendentes", pendentes.size());
                
                for (ApoliceNotification notification : pendentes) {
                    processarNotificacao(notification);
                }
            }
            
        } catch (Exception ex) {
            log.error("Erro ao processar notificações pendentes: {}", ex.getMessage(), ex);
        }
    }
    
    /**
     * Processa uma notificação específica.
     */
    @Async
    public CompletableFuture<Void> processarNotificacao(ApoliceNotification notification) {
        try {
            log.info("Processando notificação {} via {}", 
                    notification.getId(), notification.getChannel());
            
            // Verificar se não expirou
            if (notification.isExpired()) {
                log.warn("Notificação {} expirada, marcando como falha", notification.getId());
                marcarComoFalha(notification, "Notificação expirada");
                return CompletableFuture.completedFuture(null);
            }
            
            // Marcar como processando
            notification.setStatus(NotificationStatus.PROCESSING);
            notification.setAtualizadaEm(Instant.now());
            notificationRepository.save(notification);
            
            // Enviar através do canal apropriado
            boolean sucesso = enviarPorCanal(notification);
            
            if (sucesso) {
                marcarComoEnviada(notification);
            } else {
                tentarNovamente(notification);
            }
            
        } catch (Exception ex) {
            log.error("Erro ao processar notificação {}: {}", 
                     notification.getId(), ex.getMessage(), ex);
            tentarNovamente(notification);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Envia notificação através do canal específico.
     */
    private boolean enviarPorCanal(ApoliceNotification notification) {
        try {
            return switch (notification.getChannel()) {
                case EMAIL -> enviarEmail(notification);
                case SMS -> enviarSMS(notification);
                case WHATSAPP -> enviarWhatsApp(notification);
                case PUSH -> enviarPush(notification);
                case IN_APP -> enviarInApp(notification);
            };
        } catch (Exception ex) {
            log.error("Erro ao enviar notificação {} via {}: {}", 
                     notification.getId(), notification.getChannel(), ex.getMessage(), ex);
            return false;
        }
    }
    
    /**
     * Envia notificação por email.
     */
    private boolean enviarEmail(ApoliceNotification notification) {
        log.info("Enviando email para apólice {}: {}", 
                notification.getApoliceId(), notification.getTitulo());
        
        try {
            // Simular envio de email
            // Em implementação real, integraria com serviço de email (SendGrid, SES, etc.)
            
            String destinatario = notification.getSeguradoEmail();
            if (destinatario == null) {
                destinatario = obterEmailSegurado(notification.getSeguradoCpf());
            }
            
            log.info("Email enviado com sucesso para {}", destinatario);
            
            // Simular sucesso (95% de taxa de sucesso)
            return Math.random() > 0.05;
            
        } catch (Exception ex) {
            log.error("Falha ao enviar email: {}", ex.getMessage(), ex);
            return false;
        }
    }
    
    /**
     * Envia notificação por SMS.
     */
    private boolean enviarSMS(ApoliceNotification notification) {
        log.info("Enviando SMS para apólice {}: {}", 
                notification.getApoliceId(), notification.getTitulo());
        
        try {
            // Simular envio de SMS
            // Em implementação real, integraria com serviço de SMS (Twilio, AWS SNS, etc.)
            
            String telefone = notification.getSeguradoTelefone();
            if (telefone == null) {
                telefone = obterTelefoneSegurado(notification.getSeguradoCpf());
            }
            
            log.info("SMS enviado com sucesso para {}", telefone);
            
            // Simular sucesso (90% de taxa de sucesso)
            return Math.random() > 0.10;
            
        } catch (Exception ex) {
            log.error("Falha ao enviar SMS: {}", ex.getMessage(), ex);
            return false;
        }
    }
    
    /**
     * Envia notificação por WhatsApp.
     */
    private boolean enviarWhatsApp(ApoliceNotification notification) {
        log.info("Enviando WhatsApp para apólice {}: {}", 
                notification.getApoliceId(), notification.getTitulo());
        
        try {
            // Simular envio de WhatsApp
            // Em implementação real, integraria com WhatsApp Business API
            
            String telefone = notification.getSeguradoTelefone();
            if (telefone == null) {
                telefone = obterTelefoneSegurado(notification.getSeguradoCpf());
            }
            
            log.info("WhatsApp enviado com sucesso para {}", telefone);
            
            // Simular sucesso (85% de taxa de sucesso)
            return Math.random() > 0.15;
            
        } catch (Exception ex) {
            log.error("Falha ao enviar WhatsApp: {}", ex.getMessage(), ex);
            return false;
        }
    }
    
    /**
     * Envia notificação push.
     */
    private boolean enviarPush(ApoliceNotification notification) {
        log.info("Enviando Push para apólice {}: {}", 
                notification.getApoliceId(), notification.getTitulo());
        
        try {
            // Simular envio de push notification
            // Em implementação real, integraria com FCM, APNS, etc.
            
            String deviceToken = obterDeviceTokenSegurado(notification.getSeguradoCpf());
            
            log.info("Push notification enviado com sucesso para device {}", deviceToken);
            
            // Simular sucesso (80% de taxa de sucesso)
            return Math.random() > 0.20;
            
        } catch (Exception ex) {
            log.error("Falha ao enviar push notification: {}", ex.getMessage(), ex);
            return false;
        }
    }
    
    /**
     * Envia notificação in-app.
     */
    private boolean enviarInApp(ApoliceNotification notification) {
        log.info("Enviando notificação in-app para apólice {}: {}", 
                notification.getApoliceId(), notification.getTitulo());
        
        try {
            // Simular envio de notificação in-app
            // Em implementação real, salvaria na base de dados para exibição no app
            
            String userId = notification.getSeguradoCpf();
            
            log.info("Notificação in-app criada com sucesso para usuário {}", userId);
            
            // Simular sucesso (99% de taxa de sucesso)
            return Math.random() > 0.01;
            
        } catch (Exception ex) {
            log.error("Falha ao criar notificação in-app: {}", ex.getMessage(), ex);
            return false;
        }
    }
    
    /**
     * Marca notificação como enviada com sucesso.
     */
    private void marcarComoEnviada(ApoliceNotification notification) {
        notification.markAsSent("external_id_" + System.currentTimeMillis());
        notificationRepository.save(notification);
        
        log.info("Notificação {} marcada como enviada", notification.getId());
    }
    
    /**
     * Tenta enviar novamente ou marca como falha.
     */
    private void tentarNovamente(ApoliceNotification notification) {
        try {
            if (notification.canRetry()) {
                // Reagendar para próxima tentativa
                Instant proximaTentativa = notification.calculateNextRetry();
                notification.setStatus(NotificationStatus.PENDING);
                notification.setAgendadaPara(proximaTentativa);
                notification.setTentativas(notification.getTentativas() + 1);

                notificationRepository.save(notification);

                log.info("Notificação {} reagendada para tentativa {} em {}",
                        notification.getId(), notification.getTentativas(), proximaTentativa);
            } else {
                marcarComoFalha(notification, "Número máximo de tentativas excedido");
            }
        } catch (Exception e) {
            log.error("Erro ao tentar novamente notificação {}: {}", notification.getId(), e.getMessage());
        }
    }
    
    /**
     * Marca notificação como falha.
     */
    private void marcarComoFalha(ApoliceNotification notification, String erro) {
        try {
            notification.markAsFailed(erro);
            notificationRepository.save(notification);

            log.warn("Notificação {} marcada como falha: {}", notification.getId(), erro);
        } catch (Exception e) {
            log.error("Erro ao marcar notificação {} como falha: {}", notification.getId(), e.getMessage());
        }
    }
    
    /**
     * Limpa notificações antigas (executado diariamente).
     */
    @Scheduled(cron = "0 0 2 * * *") // 2:00 AM todos os dias
    @Transactional
    public void limparNotificacoes() {
        try {
            Instant cutoff = Instant.now().minusSeconds(30 * 24 * 3600); // 30 dias atrás
            
            int removidas = notificationRepository.deleteByUpdatedAtLessThan(cutoff);
            
            if (removidas > 0) {
                log.info("Removidas {} notificações antigas", removidas);
            }
            
        } catch (Exception ex) {
            log.error("Erro ao limpar notificações antigas: {}", ex.getMessage(), ex);
        }
    }
    
    // === MÉTODOS AUXILIARES (SIMULADOS) ===
    
    private String obterEmailSegurado(String cpf) {
        // Em implementação real, consultaria o segurado
        return "segurado" + cpf.substring(0, 8) + "@email.com";
    }
    
    private String obterTelefoneSegurado(String cpf) {
        // Em implementação real, consultaria o segurado
        return "+5511999" + cpf.substring(0, 6);
    }
    
    private String obterDeviceTokenSegurado(String cpf) {
        // Em implementação real, consultaria tokens de dispositivos
        return "device_token_" + cpf.substring(0, 10);
    }
}