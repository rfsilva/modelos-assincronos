package com.seguradora.hibrida.snapshot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguradora.hibrida.snapshot.SnapshotProperties;
import com.seguradora.hibrida.snapshot.SnapshotStore;
import com.seguradora.hibrida.snapshot.impl.PostgreSQLSnapshotStore;
import com.seguradora.hibrida.snapshot.repository.SnapshotRepository;
import com.seguradora.hibrida.snapshot.serialization.JsonSnapshotSerializer;
import com.seguradora.hibrida.snapshot.serialization.SnapshotSerializer;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuração do sistema de snapshots.
 * 
 * <p>Configura:
 * <ul>
 *   <li>Beans principais do sistema de snapshots</li>
 *   <li>Executor para operações assíncronas</li>
 *   <li>Serialização e compressão</li>
 *   <li>Métricas e monitoramento</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(SnapshotProperties.class)
@EnableAsync
@EnableScheduling
@RequiredArgsConstructor
public class SnapshotConfiguration {
    
    private final SnapshotProperties snapshotProperties;
    
    /**
     * Configura o serializer de snapshots.
     * 
     * @param objectMapper ObjectMapper configurado para o sistema
     * @return Serializer de snapshots
     */
    @Bean
    public SnapshotSerializer snapshotSerializer(ObjectMapper objectMapper) {
        return new JsonSnapshotSerializer(objectMapper);
    }
    
    /**
     * Configura o store principal de snapshots.
     * 
     * @param snapshotRepository Repository para persistência
     * @param snapshotSerializer Serializer para conversões
     * @param snapshotProperties Propriedades de configuração
     * @return Store de snapshots
     */
    @Bean
    public SnapshotStore snapshotStore(SnapshotRepository snapshotRepository,
                                     SnapshotSerializer snapshotSerializer,
                                     SnapshotProperties snapshotProperties) {
        return new PostgreSQLSnapshotStore(snapshotRepository, snapshotSerializer, snapshotProperties);
    }
    
    /**
     * Configura executor para operações assíncronas de snapshot.
     * 
     * @return Executor configurado
     */
    @Bean("snapshotTaskExecutor")
    public Executor snapshotTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(snapshotProperties.getAsyncThreadPoolSize());
        executor.setMaxPoolSize(snapshotProperties.getAsyncThreadPoolSize() * 2);
        executor.setQueueCapacity(snapshotProperties.getAsyncQueueCapacity());
        executor.setThreadNamePrefix(snapshotProperties.getAsyncThreadNamePrefix());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
    
    /**
     * Configura métricas de snapshots.
     * 
     * @param meterRegistry Registry de métricas
     * @param snapshotStore Store de snapshots
     * @return Métricas configuradas
     */
    @Bean
    public SnapshotMetrics snapshotMetrics(MeterRegistry meterRegistry, SnapshotStore snapshotStore) {
        SnapshotMetrics metrics = new SnapshotMetrics(meterRegistry, snapshotStore);
        metrics.initializeGauges();
        return metrics;
    }
    
    /**
     * Configura health indicator para snapshots.
     * 
     * @param snapshotStore Store de snapshots
     * @param snapshotProperties Propriedades de configuração
     * @return Health indicator
     */
    @Bean
    public SnapshotHealthIndicator snapshotHealthIndicator(SnapshotStore snapshotStore,
                                                          SnapshotProperties snapshotProperties) {
        return new SnapshotHealthIndicator(snapshotStore, snapshotProperties);
    }
    
    /**
     * Configura scheduler para limpeza automática.
     * 
     * @param snapshotStore Store de snapshots
     * @param snapshotProperties Propriedades de configuração
     * @return Scheduler configurado
     */
    @Bean
    public SnapshotCleanupScheduler snapshotCleanupScheduler(SnapshotStore snapshotStore,
                                                           SnapshotProperties snapshotProperties) {
        return new SnapshotCleanupScheduler(snapshotStore, snapshotProperties);
    }
}