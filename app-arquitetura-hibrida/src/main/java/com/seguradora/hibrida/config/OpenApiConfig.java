package com.seguradora.hibrida.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Bean
    public OpenAPI arquiteturaHibridaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🔄 Arquitetura Híbrida - Sinistros API")
                        .description("""
                                **Sistema de Gestão de Sinistros com Arquitetura Híbrida**
                                
                                Esta API implementa uma arquitetura que combina **Event Sourcing** para auditoria completa, 
                                **CQRS** para separação de responsabilidades e **processamento híbrido** (síncrono para 
                                operações críticas, assíncrono para integrações).
                                
                                ### 🔧 **Características Principais**
                                - 📚 **Event Sourcing** para histórico completo
                                - 🔄 **CQRS** para separação Command/Query
                                - ⚡ **Processamento Híbrido** (sync + async)
                                - 🗄️ **Cache Inteligente** para performance
                                - 📊 **Projections Otimizadas** para consultas
                                
                                ### 🎯 **Padrões Implementados**
                                - **Command Side**: Operações de escrita com Event Sourcing
                                - **Query Side**: Projections otimizadas para leitura
                                - **Event Processing**: Assíncrono com Kafka
                                - **Consistency**: Eventual com garantias de ordem
                                - **Replay**: Capacidade de reprocessar eventos históricos
                                
                                ### 📈 **Observabilidade**
                                - Timeline completa de eventos por aggregate
                                - Métricas de performance por projection
                                - Monitoramento de lag entre command e query
                                - Dashboard de eventos em tempo real
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe de Arquitetura Híbrida")
                                .email("arquitetura-hibrida@seguradora.com")
                                .url("https://github.com/seguradora/arquitetura-hibrida"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8083" + contextPath)
                                .description("🏠 Ambiente Local"),
                        new Server()
                                .url("https://hibrida-dev.seguradora.com" + contextPath)
                                .description("🧪 Ambiente de Desenvolvimento"),
                        new Server()
                                .url("https://hibrida.seguradora.com" + contextPath)
                                .description("🚀 Ambiente de Produção")))
                .tags(List.of(
                        new Tag()
                                .name("📝 Commands")
                                .description("Operações de escrita (Command Side) com Event Sourcing"),
                        new Tag()
                                .name("🔍 Queries")
                                .description("Operações de leitura (Query Side) com projections otimizadas"),
                        new Tag()
                                .name("📚 Events")
                                .description("Timeline de eventos e Event Store"),
                        new Tag()
                                .name("📊 Projections")
                                .description("Status e controle de projections"),
                        new Tag()
                                .name("🔧 Sistema")
                                .description("Endpoints de controle e status do sistema")));
    }
}