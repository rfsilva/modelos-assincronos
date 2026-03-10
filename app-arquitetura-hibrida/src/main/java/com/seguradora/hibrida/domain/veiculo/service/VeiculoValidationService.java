package com.seguradora.hibrida.domain.veiculo.service;

import com.seguradora.hibrida.domain.veiculo.query.repository.VeiculoQueryRepository;
import org.springframework.stereotype.Service;

/**
 * Serviço de validação para operações de veículo.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Service
public class VeiculoValidationService {
    
    private final VeiculoQueryRepository veiculoQueryRepository;
    
    public VeiculoValidationService(VeiculoQueryRepository veiculoQueryRepository) {
        this.veiculoQueryRepository = veiculoQueryRepository;
    }
    
    /**
     * Valida unicidade de placa, RENAVAM e chassi.
     */
    public void validarUnicidade(String placa, String renavam, String chassi) {
        if (veiculoQueryRepository.existsByPlaca(placa)) {
            throw new IllegalArgumentException("Placa já cadastrada no sistema: " + placa);
        }
        
        if (veiculoQueryRepository.existsByRenavam(renavam)) {
            throw new IllegalArgumentException("RENAVAM já cadastrado no sistema: " + renavam);
        }
        
        if (veiculoQueryRepository.existsByChassi(chassi)) {
            throw new IllegalArgumentException("Chassi já cadastrado no sistema: " + chassi);
        }
    }
}