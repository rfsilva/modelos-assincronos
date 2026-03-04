package com.seguradora.consistente.config;

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
    public OpenAPI arquiteturaConsistenteOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("⚖️ Arquitetura Consistente - Sinistros API")
                        .description("""
                                **Sistema de Gestão de Sinistros com Arquitetura Focada em Consistência**
                                
                                Esta API implementa uma arquitetura que prioriza a **consistência e integridade dos dados**, 
                                utilizando o padrão Saga para garantir que todas as operações sejam executadas com sucesso 
                                ou revertidas completamente.
                                
                                ### 🔧 **Características Principais**
                                - 📋 **Saga Pattern** para transações distribuídas
                                - ⚖️ **Consistência Garantida** através de compensação
                                - 📊 **Auditoria Completa** de todas as operações
                                - 🔄 **Rollback Automático** em caso de falha
                                - 📈 **Visibilidade Total** do processamento
                                
                                ### 🎯 **Padrões de Consistência**
                                - **Transações ACID** para operações críticas
                                - **Saga Orchestrator** para coordenação
                                - **Compensação Automática** em caso de falha
                                - **Auditoria Detalhada** de cada step
                                - **Timeout Configurável** por operação
                                
                                ### 📈 **Observabilidade**
                                - Timeline completa de cada saga
                                - Métricas de sucesso/falha por step
                                - Logs detalhados de compensação
                                - Dashboard de sagas em execução
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe de Arquitetura Consistente")
                                .email("arquitetura-consistente@seguradora.com")
                                .url("https://github.com/seguradora/arquitetura-consistente"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082" + contextPath)
                                .description("🏠 Ambiente Local"),
                        new Server()
                                .url("https://consistente-dev.seguradora.com" + contextPath)
                                .description("🧪 Ambiente de Desenvolvimento"),
                        new Server()
                                .url("https://consistente.seguradora.com" + contextPath)
                                .description("🚀 Ambiente de Produção")))
                .tags(List.of(
                        new Tag()
                                .name("🚗 Sinistros")
                                .description("Operações de gestão de sinistros com consistência garantida"),
                        new Tag()
                                .name("📋 Sagas")
                                .description("Monitoramento e controle de sagas em execução"),
                        new Tag()
                                .name("📊 Auditoria")
                                .description("Logs e histórico detalhado de operações"),
                        new Tag()
                                .name("🔧 Sistema")
                                .description("Endpoints de controle e status do sistema")));
    }
}