package com.seguradora.detran.config;

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
    public OpenAPI detranSimulatorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🚗 Detran Simulator API")
                        .description("""
                                **Simulador do Sistema Legado do Detran**
                                
                                Esta API simula o comportamento do sistema legado do Detran, incluindo:
                                
                                - ✅ **Consultas de veículos** por placa e RENAVAM
                                - 🔄 **Instabilidades simuladas** (timeouts, indisponibilidade)
                                - 📊 **Monitoramento** e métricas em tempo real
                                - 🐌 **Respostas lentas** para simular performance real
                                - 🔧 **Endpoints de status** e manutenção
                                
                                ### 📋 Configurações de Instabilidade
                                - **15%** de falhas (indisponibilidade)
                                - **10%** de timeouts
                                - **25%** de respostas lentas (>5s)
                                - **10%** de dados inválidos
                                
                                ### ⏱️ Performance Simulada
                                - **Mínimo**: 500ms
                                - **Máximo**: 8000ms
                                - **Timeout**: 30000ms
                                
                                ### 🎯 Casos de Uso
                                - Testes de integração
                                - Validação de resiliência
                                - Simulação de cenários reais
                                - Desenvolvimento de circuit breakers
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe de Arquitetura")
                                .email("arquitetura@seguradora.com")
                                .url("https://github.com/seguradora/detran-simulator"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080" + contextPath)
                                .description("🏠 Ambiente Local"),
                        new Server()
                                .url("https://detran-simulator-dev.exemplo.com" + contextPath)
                                .description("🧪 Ambiente de Desenvolvimento"),
                        new Server()
                                .url("https://detran-simulator.exemplo.com" + contextPath)
                                .description("🚀 Ambiente de Produção")))
                .tags(List.of(
                        new Tag()
                                .name("🚗 Consultas de Veículos")
                                .description("Endpoints para consulta de dados de veículos"),
                        new Tag()
                                .name("📊 Monitoramento")
                                .description("Endpoints para monitoramento e métricas do simulador"),
                        new Tag()
                                .name("🔧 Sistema")
                                .description("Endpoints de status e controle do sistema")));
    }
}