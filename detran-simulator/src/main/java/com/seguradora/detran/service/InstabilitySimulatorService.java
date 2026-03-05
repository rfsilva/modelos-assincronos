package com.seguradora.detran.service;

import com.seguradora.detran.config.SimulatorConfig;
import com.seguradora.detran.exception.DetranIndisponivelException;
import com.seguradora.detran.exception.DetranTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstabilitySimulatorService {
    
    private final SimulatorConfig config;
    private final Random random = new Random();
    
    /**
     * Simula instabilidades do sistema Detran
     * @return tipo de comportamento simulado
     * @throws DetranIndisponivelException se simular indisponibilidade
     * @throws DetranTimeoutException se simular timeout
     */
    public String simulateInstability() throws DetranIndisponivelException, DetranTimeoutException {
        if (!config.getInstability().isEnabled()) {
            return "NORMAL";
        }
        
        double randomValue = random.nextDouble();
        
        // Simular indisponibilidade (sistema fora do ar)
        if (randomValue < config.getInstability().getFailureRate()) {
            log.warn("🔴 Simulando indisponibilidade do Detran");
            throw new DetranIndisponivelException("Sistema Detran temporariamente indisponível");
        }
        
        // Simular timeout
        randomValue -= config.getInstability().getFailureRate();
        if (randomValue < config.getInstability().getTimeoutRate()) {
            log.warn("⏱️ Simulando timeout do Detran");
            throw new DetranTimeoutException("Timeout na consulta ao Detran");
        }
        
        // Simular resposta lenta
        randomValue -= config.getInstability().getTimeoutRate();
        if (randomValue < config.getInstability().getSlowResponseRate()) {
            log.info("🐌 Simulando resposta lenta do Detran");
            return "SLOW_RESPONSE";
        }
        
        return "NORMAL";
    }
    
    /**
     * Calcula tempo de resposta baseado no comportamento simulado
     */
    public long calculateResponseTime(String behavior) {
        return switch (behavior) {
            case "SLOW_RESPONSE" -> ThreadLocalRandom.current().nextLong(
                config.getPerformance().getSlowResponseTime(),
                config.getPerformance().getMaxResponseTime()
            );
            case "NORMAL" -> ThreadLocalRandom.current().nextLong(
                config.getPerformance().getMinResponseTime(),
                config.getPerformance().getSlowResponseTime()
            );
            default -> config.getPerformance().getMinResponseTime();
        };
    }
    
    /**
     * Simula se os dados fornecidos são válidos
     */
    public boolean isDataValid(String placa, String renavam) {
        // 10% de chance de dados inválidos
        if (random.nextDouble() < config.getData().getInvalidDataRate()) {
            log.info("❌ Simulando dados inválidos para placa: {} renavam: {}", placa, renavam);
            return false;
        }
        
        // Validações básicas de formato
        if (placa == null || renavam == null) {
            return false;
        }
        
        // Placa deve ter formato brasileiro (ABC1234 ou ABC1D23)
        if (!placa.matches("^[A-Z]{3}[0-9]{4}$") && !placa.matches("^[A-Z]{3}[0-9][A-Z][0-9]{2}$")) {
            return false;
        }
        
        // RENAVAM deve ter 11 dígitos
        if (!renavam.matches("^[0-9]{11}$")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Simula cache interno do Detran
     */
    public boolean shouldUseCache(String placa, String renavam) {
        if (!config.getData().isCacheSimulation()) {
            return false;
        }
        
        // Simula 30% de chance de hit no cache
        return random.nextDouble() < 0.3;
    }
}