package com.seguradora.detran.service;

import com.seguradora.detran.exception.DetranDadosInvalidosException;
import com.seguradora.detran.exception.DetranIndisponivelException;
import com.seguradora.detran.exception.DetranTimeoutException;
import com.seguradora.detran.model.ConsultaLog;
import com.seguradora.detran.model.DetranResponse;
import com.seguradora.detran.model.VeiculoEntity;
import com.seguradora.detran.repository.ConsultaLogRepository;
import com.seguradora.detran.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetranSimulatorService {
    
    private final InstabilitySimulatorService instabilityService;
    private final VeiculoDataGeneratorService dataGeneratorService;
    private final VeiculoRepository veiculoRepository;
    private final ConsultaLogRepository consultaLogRepository;
    
    @Transactional
    public DetranResponse consultarVeiculo(String placa, String renavam, String clientIp, String userAgent) 
            throws DetranIndisponivelException, DetranTimeoutException, DetranDadosInvalidosException {
        
        long startTime = System.currentTimeMillis();
        String simulatedBehavior = "NORMAL";
        ConsultaLog.StatusConsulta status = ConsultaLog.StatusConsulta.SUCESSO;
        String errorMessage = null;
        
        try {
            log.info("🔍 Iniciando consulta - Placa: {} Renavam: {} IP: {}", placa, renavam, clientIp);
            
            // 1. Simular instabilidades do sistema
            simulatedBehavior = instabilityService.simulateInstability();
            
            // 2. Validar dados de entrada
            if (!instabilityService.isDataValid(placa, renavam)) {
                status = ConsultaLog.StatusConsulta.DADOS_INVALIDOS;
                errorMessage = "Dados de placa ou renavam inválidos";
                throw new DetranDadosInvalidosException("Placa ou RENAVAM inválidos");
            }
            
            // 3. Simular tempo de resposta
            long responseTime = instabilityService.calculateResponseTime(simulatedBehavior);
            simulateDelay(responseTime);
            
            // 4. Verificar se deve usar cache simulado
            DetranResponse response;
            if (instabilityService.shouldUseCache(placa, renavam)) {
                log.info("💾 Usando cache simulado para placa: {}", placa);
                response = getCachedOrGenerateData(placa, renavam);
                simulatedBehavior += "_CACHED";
            } else {
                response = getCachedOrGenerateData(placa, renavam);
            }
            
            log.info("✅ Consulta realizada com sucesso - Placa: {} Comportamento: {}", placa, simulatedBehavior);
            return response;
            
        } catch (DetranIndisponivelException e) {
            status = ConsultaLog.StatusConsulta.INDISPONIBILIDADE_SIMULADA;
            errorMessage = e.getMessage();
            simulatedBehavior = "INDISPONIVEL";
            throw e;
            
        } catch (DetranTimeoutException e) {
            status = ConsultaLog.StatusConsulta.TIMEOUT_SIMULADO;
            errorMessage = e.getMessage();
            simulatedBehavior = "TIMEOUT";
            throw e;
            
        } catch (DetranDadosInvalidosException e) {
            status = ConsultaLog.StatusConsulta.DADOS_INVALIDOS;
            errorMessage = e.getMessage();
            throw e;
            
        } catch (Exception e) {
            status = ConsultaLog.StatusConsulta.ERRO_INTERNO;
            errorMessage = e.getMessage();
            simulatedBehavior = "ERRO_INTERNO";
            log.error("❌ Erro interno na consulta", e);
            throw new DetranIndisponivelException("Erro interno do sistema Detran");
            
        } finally {
            // Registrar log da consulta
            long totalTime = System.currentTimeMillis() - startTime;
            registrarConsulta(placa, renavam, clientIp, userAgent, status, totalTime, errorMessage, simulatedBehavior);
        }
    }
    
    private DetranResponse getCachedOrGenerateData(String placa, String renavam) {
        // Verificar se já existe no "banco" (simulando cache persistente)
        return veiculoRepository.findByPlacaAndRenavam(placa, renavam)
            .map(this::convertToResponse)
            .orElseGet(() -> {
                // Gerar novos dados e salvar
                DetranResponse response = dataGeneratorService.generateVeiculoData(placa, renavam);
                salvarVeiculoEntity(response);
                return response;
            });
    }
    
    private DetranResponse convertToResponse(VeiculoEntity entity) {
        // Gerar resposta completa baseada nos dados básicos salvos
        DetranResponse response = dataGeneratorService.generateVeiculoData(entity.getPlaca(), entity.getRenavam());
        
        // Sobrescrever com dados salvos
        response.setPlaca(entity.getPlaca());
        response.setRenavam(entity.getRenavam());
        response.setAnoFabricacao(entity.getAnoFabricacao());
        response.setAnoModelo(entity.getAnoModelo());
        response.setMarcaModelo(entity.getMarcaModelo());
        response.setCor(entity.getCor());
        response.setCombustivel(entity.getCombustivel());
        response.setCategoria(entity.getCategoria());
        response.setCarroceria(entity.getCarroceria());
        response.setEspecie(entity.getEspecie());
        response.setProprietario(entity.getProprietario());
        response.setMunicipio(entity.getMunicipio());
        response.setSituacao(entity.getSituacao());
        response.setDataAquisicao(entity.getDataAquisicao());
        
        return response;
    }
    
    private void salvarVeiculoEntity(DetranResponse response) {
        try {
            VeiculoEntity entity = VeiculoEntity.builder()
                .placa(response.getPlaca())
                .renavam(response.getRenavam())
                .anoFabricacao(response.getAnoFabricacao())
                .anoModelo(response.getAnoModelo())
                .marcaModelo(response.getMarcaModelo())
                .cor(response.getCor())
                .combustivel(response.getCombustivel())
                .categoria(response.getCategoria())
                .carroceria(response.getCarroceria())
                .especie(response.getEspecie())
                .proprietario(response.getProprietario())
                .municipio(response.getMunicipio())
                .situacao(response.getSituacao())
                .dataAquisicao(response.getDataAquisicao())
                .build();
            
            veiculoRepository.save(entity);
            log.debug("💾 Dados do veículo salvos: {}", response.getPlaca());
            
        } catch (Exception e) {
            log.warn("⚠️ Erro ao salvar dados do veículo: {}", e.getMessage());
            // Não propagar erro - é apenas cache
        }
    }
    
    private void registrarConsulta(String placa, String renavam, String clientIp, String userAgent,
                                 ConsultaLog.StatusConsulta status, long responseTime, String errorMessage,
                                 String simulatedBehavior) {
        try {
            ConsultaLog log = ConsultaLog.builder()
                .placa(placa)
                .renavam(renavam)
                .clientIp(clientIp)
                .userAgent(userAgent)
                .status(status)
                .responseTimeMs(responseTime)
                .errorMessage(errorMessage)
                .simulatedBehavior(simulatedBehavior)
                .build();
            
            consultaLogRepository.save(log);
            
        } catch (Exception e) {
            log.error("❌ Erro ao registrar log de consulta", e);
            // Não propagar erro
        }
    }
    
    private void simulateDelay(long delayMs) {
        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Consulta interrompida", e);
            }
        }
    }
}