package com.seguradora.detran.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "detran.simulator")
@Data
public class SimulatorConfig {
    
    private Instability instability = new Instability();
    private Performance performance = new Performance();
    private DataConfig data = new DataConfig();
    
    @Data
    public static class Instability {
        private boolean enabled = true;
        private double failureRate = 0.15;      // 15% de falhas
        private double timeoutRate = 0.10;      // 10% de timeouts
        private double slowResponseRate = 0.25; // 25% de respostas lentas
    }
    
    @Data
    public static class Performance {
        private long minResponseTime = 500;     // 500ms mínimo
        private long maxResponseTime = 8000;    // 8s máximo
        private long slowResponseTime = 5000;   // 5s para "lento"
        private long timeoutThreshold = 30000;  // 30s timeout
    }
    
    @Data
    public static class DataConfig {
        private double invalidDataRate = 0.10;  // 10% dados inválidos
        private boolean cacheSimulation = true;
        private long cacheTtl = 300;            // 5 minutos
    }
}