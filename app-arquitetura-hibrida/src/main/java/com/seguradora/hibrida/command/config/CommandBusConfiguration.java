package com.seguradora.hibrida.command.config;

import com.seguradora.hibrida.command.CommandBus;
import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandHandlerRegistry;
import com.seguradora.hibrida.command.impl.SimpleCommandBus;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * Configuração do Command Bus e componentes relacionados.
 * 
 * <p>Esta configuração:</p>
 * <ul>
 *   <li>Configura o Command Bus principal</li>
 *   <li>Registra handlers automaticamente</li>
 *   <li>Inicializa métricas e monitoramento</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Configuration
@Slf4j
public class CommandBusConfiguration {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    /**
     * Configura o Command Bus principal.
     * 
     * @param handlerRegistry Registry de handlers
     * @return Instância configurada do Command Bus
     */
    @Bean
    @Primary
    public CommandBus commandBus(CommandHandlerRegistry handlerRegistry) {
        log.info("Configurando Command Bus principal");
        return new SimpleCommandBus(handlerRegistry);
    }
    
    /**
     * Configura o registry de handlers.
     * 
     * @return Instância do registry
     */
    @Bean
    public CommandHandlerRegistry commandHandlerRegistry() {
        log.info("Configurando Command Handler Registry");
        return new CommandHandlerRegistry();
    }
    
    /**
     * Configura propriedades do Command Bus.
     * 
     * @return Propriedades configuradas
     */
    @Bean
    public CommandBusProperties commandBusProperties() {
        return new CommandBusProperties();
    }
    
    /**
     * Configura métricas do Command Bus.
     * 
     * @param meterRegistry Registry de métricas
     * @return Métricas configuradas
     */
    @Bean
    public CommandBusMetrics commandBusMetrics(MeterRegistry meterRegistry) {
        return new CommandBusMetrics(meterRegistry);
    }
    
    /**
     * Configura health indicator do Command Bus.
     * 
     * @param commandBus Command Bus principal
     * @return Health indicator configurado
     */
    @Bean
    public CommandBusHealthIndicator commandBusHealthIndicator(CommandBus commandBus) {
        return new CommandBusHealthIndicator(commandBus);
    }
    
    /**
     * Registra automaticamente todos os handlers encontrados no contexto.
     */
    @PostConstruct
    public void registerHandlers() {
        log.info("Iniciando registro automático de Command Handlers");
        
        CommandHandlerRegistry registry = applicationContext.getBean(CommandHandlerRegistry.class);
        Map<String, CommandHandler> handlers = applicationContext.getBeansOfType(CommandHandler.class);
        
        int registeredCount = 0;
        for (Map.Entry<String, CommandHandler> entry : handlers.entrySet()) {
            try {
                CommandHandler<?> handler = entry.getValue();
                registry.registerHandler(handler);
                registeredCount++;
                
                log.debug("Handler registrado: {} -> {}", 
                         handler.getCommandType().getSimpleName(), 
                         entry.getKey());
                
            } catch (Exception e) {
                log.error("Erro ao registrar handler: {}", entry.getKey(), e);
            }
        }
        
        log.info("Registro automático concluído. {} handlers registrados", registeredCount);
        
        // Log de handlers registrados para debug
        if (log.isDebugEnabled()) {
            registry.getDebugInfo().forEach((commandType, handlerClass) -> 
                log.debug("  {} -> {}", commandType, handlerClass));
        }
    }
}