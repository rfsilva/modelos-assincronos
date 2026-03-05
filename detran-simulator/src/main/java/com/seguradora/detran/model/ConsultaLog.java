package com.seguradora.detran.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "consulta_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String placa;
    private String renavam;
    
    @Column(name = "client_ip")
    private String clientIp;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Enumerated(EnumType.STRING)
    private StatusConsulta status;
    
    @Column(name = "response_time_ms")
    private Long responseTimeMs;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "simulated_behavior")
    private String simulatedBehavior;
    
    @Column(name = "consulta_timestamp")
    private LocalDateTime consultaTimestamp;
    
    @PrePersist
    protected void onCreate() {
        consultaTimestamp = LocalDateTime.now();
    }
    
    public enum StatusConsulta {
        SUCESSO,
        DADOS_INVALIDOS,
        TIMEOUT_SIMULADO,
        INDISPONIBILIDADE_SIMULADA,
        ERRO_INTERNO
    }
}