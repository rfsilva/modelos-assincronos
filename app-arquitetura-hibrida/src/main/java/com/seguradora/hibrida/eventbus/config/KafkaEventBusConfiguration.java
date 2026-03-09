package com.seguradora.hibrida.eventbus.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguradora.hibrida.eventbus.EventBus;
import com.seguradora.hibrida.eventbus.EventHandlerRegistry;
import com.seguradora.hibrida.eventbus.impl.KafkaEventBus;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Configuração Spring para o Event Bus com integração Kafka.
 * 
 * <p>Esta configuração é ativada quando a propriedade 
 * {@code event-bus.kafka.enabled=true} está definida.
 * 
 * <p>Funcionalidades incluídas:
 * <ul>
 *   <li>Configuração automática do KafkaEventBus</li>
 *   <li>Criação automática de tópicos necessários</li>
 *   <li>Configuração de admin client para gerenciamento</li>
 *   <li>Integração com métricas e health checks</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "event-bus.kafka.enabled", havingValue = "true")
public class KafkaEventBusConfiguration {
    
    private static final Logger log = LoggerFactory.getLogger(KafkaEventBusConfiguration.class);
    
    @Autowired
    private EventBusProperties properties;
    
    /**
     * Configura o Kafka Event Bus como implementação principal.
     */
    @Bean
    @Primary
    public EventBus kafkaEventBus(EventHandlerRegistry handlerRegistry, ObjectMapper objectMapper) {
        log.info("Configuring Kafka Event Bus with servers: {}", 
                properties.getKafka().getBootstrapServers());
        return new KafkaEventBus(handlerRegistry, properties, objectMapper);
    }
    
    /**
     * Configura o ObjectMapper para serialização de eventos.
     */
    @Bean
    public ObjectMapper eventBusObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }
    
    /**
     * Configura o admin client do Kafka para gerenciamento de tópicos.
     */
    @Bean
    public AdminClient kafkaAdminClient() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafka().getBootstrapServers());
        return AdminClient.create(props);
    }
    
    /**
     * Cria automaticamente os tópicos necessários no Kafka.
     */
    @PostConstruct
    public void createKafkaTopics() {
        if (!properties.getKafka().isEnabled()) {
            return;
        }
        
        log.info("Creating Kafka topics...");
        
        try (AdminClient adminClient = kafkaAdminClient()) {
            // Tópico padrão para eventos
            NewTopic defaultTopic = new NewTopic(
                properties.getKafka().getDefaultTopic(),
                properties.getKafka().getPartitions(),
                properties.getKafka().getReplicationFactor()
            );
            
            // Tópico para dead letter queue
            NewTopic dlqTopic = new NewTopic(
                properties.getKafka().getDefaultTopic() + "-dlq",
                properties.getKafka().getPartitions(),
                properties.getKafka().getReplicationFactor()
            );
            
            // Criar tópicos
            adminClient.createTopics(Arrays.asList(defaultTopic, dlqTopic))
                    .all()
                    .get();
            
            log.info("Kafka topics created successfully: {}, {}", 
                    defaultTopic.name(), dlqTopic.name());
            
        } catch (ExecutionException e) {
            if (e.getCause().getMessage().contains("already exists")) {
                log.info("Kafka topics already exist, skipping creation");
            } else {
                log.error("Failed to create Kafka topics", e);
                throw new RuntimeException("Failed to create Kafka topics", e);
            }
        } catch (Exception e) {
            log.error("Failed to create Kafka topics", e);
            throw new RuntimeException("Failed to create Kafka topics", e);
        }
    }
}