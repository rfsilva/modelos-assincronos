package com.seguradora.hibrida.domain.apolice.notification.handler;

import com.seguradora.hibrida.domain.apolice.event.*;
import com.seguradora.hibrida.domain.apolice.notification.model.*;
import com.seguradora.hibrida.domain.apolice.notification.repository.ApoliceNotificationRepository;
import com.seguradora.hibrida.domain.apolice.notification.service.NotificationTemplateService;
import com.seguradora.hibrida.eventbus.EventHandler;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Event Handler para notificações de apólice.
 * 
 * <p>Processa eventos de apólice e gera notificações automáticas
 * para segurados através de múltiplos canais (Email, SMS, WhatsApp).
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ApoliceNotificationEventHandler implements EventHandler<DomainEvent> {
    
    private final ApoliceNotificationRepository notificationRepository;
    private final NotificationTemplateService templateService;
    
    @Override
    public Class<DomainEvent> getEventType() {
        return DomainEvent.class;
    }
    
    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof ApoliceCriadaEvent ||
               event instanceof ApoliceAtualizadaEvent ||
               event instanceof ApoliceCanceladaEvent ||
               event instanceof ApoliceRenovadaEvent ||
               event instanceof CoberturaAdicionadaEvent;
    }
    
    @Override
    @Async
    public void handle(DomainEvent event) {
        try {
            log.info("Processando evento de notificação: {} para aggregate {}", 
                    event.getEventType(), event.getAggregateId());
            
            switch (event) {
                case ApoliceCriadaEvent e -> handleApoliceCriada(e);
                case ApoliceAtualizadaEvent e -> handleApoliceAtualizada(e);
                case ApoliceCanceladaEvent e -> handleApoliceCancelada(e);
                case ApoliceRenovadaEvent e -> handleApoliceRenovada(e);
                case CoberturaAdicionadaEvent e -> handleCoberturaAdicionada(e);
                default -> log.debug("Evento {} não requer notificação", event.getEventType());
            }
            
        } catch (Exception ex) {
            log.error("Erro ao processar notificação para evento {}: {}", 
                     event.getEventType(), ex.getMessage(), ex);
        }
    }
    
    /**
     * Processa evento de apólice criada.
     */
    private void handleApoliceCriada(ApoliceCriadaEvent event) {
        log.info("Gerando notificações para apólice criada: {}", event.getNumeroApolice());
        
        Map<String, String> parameters = Map.of(
            "numeroApolice", event.getNumeroApolice(),
            "seguradoNome", obterNomeSegurado(event.getSeguradoId()), // Simulado
            "produto", event.getProduto(),
            "valorTotal", event.getValorSegurado(),
            "vigenciaFim", event.getVigenciaFim()
        );
        
        // Criar notificações para todos os canais preferenciais
        createNotifications(
            event.getAggregateId(),
            event.getSeguradoId(),
            NotificationType.APOLICE_CRIADA,
            parameters
        );
    }
    
    /**
     * Processa evento de apólice atualizada.
     */
    private void handleApoliceAtualizada(ApoliceAtualizadaEvent event) {
        log.info("Gerando notificações para apólice atualizada: {}", event.getNumeroApolice());
        
        Map<String, String> parameters = Map.of(
            "numeroApolice", event.getNumeroApolice(),
            "seguradoNome", obterNomeSegurado(event.getSeguradoId()), // Simulado
            "alteracoes", obterAlteracoes(event) // Simulado
        );
        
        createNotifications(
            event.getAggregateId(),
            event.getSeguradoId(),
            NotificationType.APOLICE_ATUALIZADA,
            parameters
        );
    }
    
    /**
     * Processa evento de apólice cancelada.
     */
    private void handleApoliceCancelada(ApoliceCanceladaEvent event) {
        log.info("Gerando notificações para apólice cancelada: {}", event.getNumeroApolice());
        
        Map<String, String> parameters = Map.of(
            "numeroApolice", event.getNumeroApolice(),
            "seguradoNome", obterNomeSegurado(event.getSeguradoId()), // Simulado
            "motivo", event.getMotivo(),
            "valorReembolso", event.getValorReembolso() != null ? event.getValorReembolso() : "0"
        );
        
        createNotifications(
            event.getAggregateId(),
            event.getSeguradoId(),
            NotificationType.APOLICE_CANCELADA,
            parameters
        );
    }
    
    /**
     * Processa evento de apólice renovada.
     */
    private void handleApoliceRenovada(ApoliceRenovadaEvent event) {
        log.info("Gerando notificações para apólice renovada: {}", event.getNumeroApolice());
        
        Map<String, String> parameters = Map.of(
            "numeroApolice", event.getNumeroApolice(),
            "seguradoNome", obterNomeSegurado(event.getSeguradoId()), // Simulado
            "novaVigenciaFim", event.getNovaVigenciaFim(),
            "novoValorTotal", event.getNovoValorSegurado() != null ? event.getNovoValorSegurado() : "0"
        );
        
        createNotifications(
            event.getAggregateId(),
            event.getSeguradoId(),
            NotificationType.APOLICE_RENOVADA,
            parameters
        );
    }
    
    /**
     * Processa evento de cobertura adicionada.
     */
    private void handleCoberturaAdicionada(CoberturaAdicionadaEvent event) {
        log.info("Gerando notificações para cobertura adicionada: {}", event.getNumeroApolice());
        
        Map<String, String> parameters = Map.of(
            "numeroApolice", event.getNumeroApolice(),
            "seguradoNome", obterNomeSegurado(event.getSeguradoId()), // Simulado
            "tipoCobertura", event.getTipoCobertura(),
            "valorAdicional", event.getValorCobertura() != null ? event.getValorCobertura() : "0"
        );
        
        createNotifications(
            event.getAggregateId(),
            event.getSeguradoId(),
            NotificationType.COBERTURA_ADICIONADA,
            parameters
        );
    }
    
    /**
     * Cria notificações para todos os canais preferenciais do segurado.
     */
    private void createNotifications(String apoliceId, String seguradoId, 
                                   NotificationType type, Map<String, String> parameters) {
        
        // Obter canais preferenciais do segurado (simulado)
        NotificationChannel[] canaisPreferenciais = getCanaisPreferenciais(seguradoId);
        
        for (NotificationChannel canal : canaisPreferenciais) {
            try {
                createNotification(apoliceId, seguradoId, type, canal, parameters);
            } catch (Exception ex) {
                log.error("Erro ao criar notificação {} para canal {}: {}", 
                         type, canal, ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * Cria uma notificação específica.
     */
    private void createNotification(String apoliceId, String seguradoId, 
                                  NotificationType type, NotificationChannel channel,
                                  Map<String, String> parameters) {
        
        // Gerar conteúdo da notificação
        String titulo = templateService.generateTitle(type, channel, parameters);
        String mensagem = templateService.generateMessage(type, channel, parameters);
        
        // Criar entidade de notificação usando builder
        ApoliceNotification notification = ApoliceNotification.builder()
            .id(UUID.randomUUID().toString())
            .apoliceId(apoliceId)
            .seguradoId(seguradoId)
            .apoliceNumero(parameters.get("numeroApolice"))
            .seguradoCpf(obterCpfSegurado(seguradoId)) // Simulado
            .seguradoNome(parameters.get("seguradoNome"))
            .type(type)
            .channel(channel)
            .titulo(titulo)
            .mensagem(mensagem)
            .status(NotificationStatus.PENDING)
            .tentativas(0)
            .maxTentativas(getMaxTentativas(channel))
            .agendadaPara(Instant.now().plusSeconds(getDelaySegundos(type)))
            .expiraEm(Instant.now().plusSeconds(templateService.getExpirationHours(type) * 3600))
            .metadata(new HashMap<>(parameters))
            .build();
        
        // Salvar notificação
        notificationRepository.save(notification);
        
        log.info("Notificação {} criada para apólice {} via {}", 
                type, apoliceId, channel);
    }
    
    // === MÉTODOS AUXILIARES (SIMULADOS) ===
    
    /**
     * Obtém nome do segurado (simulado).
     */
    private String obterNomeSegurado(String seguradoId) {
        // Em implementação real, consultaria o segurado
        return "Segurado " + seguradoId.substring(0, Math.min(8, seguradoId.length()));
    }
    
    /**
     * Obtém CPF do segurado (simulado).
     */
    private String obterCpfSegurado(String seguradoId) {
        // Em implementação real, consultaria o segurado
        return "12345678901"; // CPF simulado
    }
    
    /**
     * Obtém alterações do evento (simulado).
     */
    private String obterAlteracoes(ApoliceAtualizadaEvent event) {
        // Em implementação real, extrairia as alterações do evento
        return "dados da apólice";
    }
    
    /**
     * Obtém canais preferenciais do segurado (simulado).
     */
    private NotificationChannel[] getCanaisPreferenciais(String seguradoId) {
        // Por enquanto, retorna todos os canais
        // Em implementação real, consultaria preferências do segurado
        return new NotificationChannel[] {
            NotificationChannel.EMAIL,
            NotificationChannel.SMS
        };
    }
    
    /**
     * Obtém número máximo de tentativas por canal.
     */
    private int getMaxTentativas(NotificationChannel channel) {
        switch (channel) {
            case EMAIL:
                return 3;
            case SMS:
                return 2;
            case WHATSAPP:
                return 2;
            case PUSH:
                return 1;
            default:
                return 2;
        }
    }
    
    /**
     * Obtém delay em segundos antes do envio.
     */
    private long getDelaySegundos(NotificationType type) {
        switch (type) {
            case APOLICE_CRIADA:
            case APOLICE_RENOVADA:
                return 60; // 1 minuto
            case APOLICE_CANCELADA:
                return 30; // 30 segundos
            case VENCIMENTO_1_DIA:
            case APOLICE_VENCIDA:
                return 0; // Imediato
            case APOLICE_ATUALIZADA:
            case COBERTURA_ADICIONADA:
            case VENCIMENTO_30_DIAS:
            case VENCIMENTO_15_DIAS:
            case VENCIMENTO_7_DIAS:
            case SCORE_BAIXO:
            default:
                return 120; // 2 minutos
        }
    }
    
    @Override
    public int getPriority() {
        return 50; // Prioridade média para notificações
    }
    
    @Override
    public boolean isAsync() {
        return true; // Processamento assíncrono
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 30; // Timeout de 30 segundos
    }
}