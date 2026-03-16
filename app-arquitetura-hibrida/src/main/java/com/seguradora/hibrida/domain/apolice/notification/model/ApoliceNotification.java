package com.seguradora.hibrida.domain.apolice.notification.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Entidade que representa uma notificação de apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Entity
@Table(name = "apolice_notifications", schema = "projections", indexes = {
    @Index(name = "idx_notification_apolice", columnList = "apolice_id"),
    @Index(name = "idx_notification_segurado", columnList = "segurado_cpf"),
    @Index(name = "idx_notification_status", columnList = "status"),
    @Index(name = "idx_notification_type", columnList = "notification_type"),
    @Index(name = "idx_notification_channel", columnList = "channel"),
    @Index(name = "idx_notification_scheduled", columnList = "scheduled_at"),
    @Index(name = "idx_notification_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApoliceNotification {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    // === IDENTIFICAÇÃO ===
    
    @Column(name = "apolice_id", length = 36, nullable = false)
    private String apoliceId;
    
    @Column(name = "segurado_id", length = 36, nullable = false)
    private String seguradoId;
    
    @Column(name = "apolice_numero", length = 20, nullable = false)
    private String apoliceNumero;
    
    @Column(name = "segurado_cpf", length = 11, nullable = false)
    private String seguradoCpf;
    
    @Column(name = "segurado_nome", length = 100, nullable = false)
    private String seguradoNome;
    
    @Column(name = "segurado_email", length = 100)
    private String seguradoEmail;
    
    @Column(name = "segurado_telefone", length = 15)
    private String seguradoTelefone;
    
    // === NOTIFICAÇÃO ===
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", length = 30, nullable = false)
    @Builder.Default
    private NotificationType type = NotificationType.APOLICE_CRIADA;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 20, nullable = false)
    @Builder.Default
    private NotificationChannel channel = NotificationChannel.EMAIL;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;
    
    @Column(name = "priority", nullable = false)
    private Integer priority;
    
    // === CONTEÚDO ===
    
    @Column(name = "titulo", length = 200, nullable = false)
    private String titulo;
    
    @Column(name = "mensagem", length = 1000, nullable = false)
    private String mensagem;
    
    @Column(name = "template_name", length = 50)
    private String templateName;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "notification_parameters", 
        schema = "projections",
        joinColumns = @JoinColumn(name = "notification_id")
    )
    @MapKeyColumn(name = "parameter_key")
    @Column(name = "parameter_value", length = 500)
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();
    
    // === AGENDAMENTO ===
    
    @Column(name = "agendada_para")
    private Instant agendadaPara;
    
    @Column(name = "enviada_em")
    private Instant enviadaEm;
    
    @Column(name = "entregue_em")
    private Instant entregueEm;
    
    @Column(name = "lida_em")
    private Instant lidaEm;
    
    @Column(name = "expira_em")
    private Instant expiraEm;
    
    // === CONTROLE ===
    
    @Column(name = "tentativas", nullable = false)
    @Builder.Default
    private Integer tentativas = 0;
    
    @Column(name = "max_tentativas", nullable = false)
    @Builder.Default
    private Integer maxTentativas = 3;
    
    @Column(name = "ultimo_erro", length = 500)
    private String ultimoErro;
    
    @Column(name = "external_id", length = 100)
    private String externalId; // ID do provedor externo
    
    // === AUDITORIA ===
    
    @CreationTimestamp
    @Column(name = "criada_em", nullable = false, updatable = false)
    private Instant criadaEm;
    
    @UpdateTimestamp
    @Column(name = "atualizada_em", nullable = false)
    private Instant atualizadaEm;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    // === CONSTRUTORES ADICIONAIS ===
    
    public ApoliceNotification(String id, String apoliceId, String apoliceNumero, 
                              String seguradoCpf, String seguradoNome) {
        this.id = Objects.requireNonNull(id, "ID não pode ser nulo");
        this.apoliceId = Objects.requireNonNull(apoliceId, "ID da apólice não pode ser nulo");
        this.apoliceNumero = Objects.requireNonNull(apoliceNumero, "Número da apólice não pode ser nulo");
        this.seguradoCpf = Objects.requireNonNull(seguradoCpf, "CPF não pode ser nulo");
        this.seguradoNome = Objects.requireNonNull(seguradoNome, "Nome não pode ser nulo");
        this.status = NotificationStatus.PENDING;
        this.tentativas = 0;
        this.maxTentativas = 3;
        this.metadata = new HashMap<>();
    }
    
    // === MÉTODOS DE NEGÓCIO ===
    
    /**
     * Verifica se a notificação está expirada.
     */
    public boolean isExpired() {
        return expiraEm != null && Instant.now().isAfter(expiraEm);
    }
    
    /**
     * Verifica se pode ser enviada agora.
     */
    public boolean canSendNow() {
        return status == NotificationStatus.PENDING && 
               (agendadaPara == null || !Instant.now().isBefore(agendadaPara)) &&
               !isExpired();
    }
    
    /**
     * Verifica se pode ser reprocessada.
     */
    public boolean canRetry() {
        return status == NotificationStatus.FAILED && 
               tentativas < maxTentativas && 
               !isExpired();
    }
    
    /**
     * Marca como enviada.
     */
    public void markAsSent(String externalId) {
        this.status = NotificationStatus.SENT;
        this.enviadaEm = Instant.now();
        this.externalId = externalId;
        this.ultimoErro = null;
    }
    
    /**
     * Marca como falha e incrementa retry.
     */
    public void markAsFailed(String error) {
        this.status = NotificationStatus.FAILED;
        this.ultimoErro = error;
        this.tentativas++;
        
        if (tentativas >= maxTentativas) {
            this.status = NotificationStatus.EXPIRED;
        }
    }
    
    /**
     * Cancela a notificação.
     */
    public void cancel(String reason) {
        this.status = NotificationStatus.CANCELLED;
        this.ultimoErro = reason;
    }
    
    /**
     * Agenda para envio futuro.
     */
    public void scheduleFor(Instant when) {
        this.agendadaPara = when;
        this.status = NotificationStatus.PENDING;
    }
    
    /**
     * Adiciona parâmetro para template.
     */
    public void addParameter(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }
    
    /**
     * Obtém parâmetro do template.
     */
    public String getParameter(String key) {
        return metadata != null ? metadata.get(key) : null;
    }
    
    /**
     * Verifica se é notificação crítica.
     */
    public boolean isCritical() {
        return type != null && type.isCritica();
    }
    
    /**
     * Calcula próximo horário de retry.
     */
    public Instant calculateNextRetry() {
        if (!canRetry()) return null;
        
        // Backoff exponencial: 5min, 15min, 45min
        long delayMinutes = (long) (5 * Math.pow(3, tentativas));
        return Instant.now().plusSeconds(delayMinutes * 60);
    }
    
    // === CALLBACKS JPA ===
    
    @PrePersist
    public void onCreate() {
        if (criadaEm == null) {
            criadaEm = Instant.now();
        }
        if (priority == null) {
            priority = (type != null) ? type.getPrioridade() : 5;
        }
        if (metadata == null) {
            metadata = new HashMap<>();
        }
    }
    
    @PreUpdate
    public void onUpdate() {
        atualizadaEm = Instant.now();
    }
    
    // === EQUALS, HASHCODE E TOSTRING ===
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApoliceNotification that = (ApoliceNotification) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("ApoliceNotification{id='%s', apolice='%s', tipo=%s, canal=%s, status=%s}", 
                           id, apoliceNumero, type, channel, status);
    }
}