package com.seguradora.detran;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DetranSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DetranSimulatorApplication.class, args);
        System.out.println("""
            
            ╔══════════════════════════════════════════════════════════════╗
            ║                    DETRAN SIMULATOR                          ║
            ║                                                              ║
            ║  🚗 Simulador do Sistema Legado do Detran                   ║
            ║  📊 Simula instabilidades e baixa performance               ║
            ║  🔧 Configurado para testes de integração                   ║
            ║                                                              ║
            ║  📍 API: http://localhost:8080/detran-api                   ║
            ║  🗄️  H2 Console: http://localhost:8080/detran-api/h2-console║
            ║  📈 Metrics: http://localhost:8080/detran-api/actuator      ║
            ║                                                              ║
            ║  📋 Endpoint Principal:                                      ║
            ║     GET /veiculo?placa={placa}&renavam={renavam}            ║
            ║                                                              ║
            ╚══════════════════════════════════════════════════════════════╝
            """);
    }
}