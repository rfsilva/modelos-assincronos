package com.seguradora.resiliente.config;

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
    public OpenAPI arquiteturaResilienteOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🛡️ Arquitetura Resiliente - Sinistros API")
                        .description("""
                                **Sistema de Gestão de Sinistros com Arquitetura Resiliente**
                                
                                Esta API implementa uma arquitetura focada em **resiliência e disponibilidade**, 
                                utilizando padrões como Circuit Breaker, Cache Distribuído e processamento assíncrono.
                                
                                ### 🔧 **Características Principais**
                                - ⚡ **Circuit Breaker** para integração com Detran
                                - 🗄️ **Cache Distribuído** (Redis) para performance
                                - 📨 **Processamento Assíncrono** com Kafka
                                - 🔄 **Retry Automático** com backoff exponencial
                                - 📊 **Monitoramento** completo com métricas
                                
                                ### 🎯 **Padrões de Resiliência**
                                - **Timeout**: 30s para consultas Detran
                                - **Retry**: Até 3 tentativas com backoff
                                - **Circuit Breaker**: Abre com 50% de falhas
                                - **Cache**: TTL de 24h para dados do Detran
                                - **Fallback**: Processamento assíncrono em caso de falha
                                
                                ### 📈 **Observabilidade**
                                - Métricas Prometheus disponíveis em `/actuator/prometheus`
                                - Health checks em `/actuator/health`
                                - Circuit breaker status em `/actuator/circuitbreakers`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe de Arquitetura Resiliente")
                                .email("arquitetura-resiliente@seguradora.com")
                                .url("https://github.com/seguradora/arquitetura-resiliente"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081" + contextPath)
                                .description("🏠 Ambiente Local"),
                        new Server()
                                .url("https://resiliente-dev.seguradora.com" + contextPath)
                                .description("🧪 Ambiente de Desenvolvimento"),
                        new Server()
                                .url("https://resiliente.seguradora.com" + contextPath)
                                .description("🚀 Ambiente de Produção")))
                .tags(List.of(
                        new Tag()
                                .name("🚗 Sinistros")
                                .description("Operações de gestão de sinistros com resiliência"),
                        new Tag()
                                .name("📊 Monitoramento")
                                .description("Endpoints de monitoramento e métricas de resiliência"),
                        new Tag()
                                .name("🔧 Sistema")
                                .description("Endpoints de controle e status do sistema"),
                        new Tag()
                                .name("⚡ Circuit Breaker")
                                .description("Informações sobre circuit breakers e resiliência")));
    }
}