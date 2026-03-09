package com.seguradora.hibrida.domain.veiculo.projection;

import com.seguradora.hibrida.domain.veiculo.event.*;
import com.seguradora.hibrida.domain.veiculo.query.model.VeiculoQueryModel;
import com.seguradora.hibrida.domain.veiculo.query.repository.VeiculoQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Handler responsável por atualizar as projeções de veículo.
 * Processa eventos de domínio e atualiza o query model.
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VeiculoProjectionHandler {
    
    private final VeiculoQueryRepository queryRepository;

    @TransactionalEventListener
    @Transactional
    @CacheEvict(value = "veiculos", allEntries = true)
    public void on(VeiculoCriadoEvent event) {
        log.info("Processando VeiculoCriadoEvent para veículo ID: {}", event.getAggregateId());
        
        try {
            VeiculoQueryModel queryModel = VeiculoQueryModel.builder()
                .id(event.getAggregateId())
                .placa(event.getPlaca().getValor())
                .renavam(event.getRenavam().getValor())
                .chassi(event.getChassi().getValor())
                .marca(event.getMarca())
                .modelo(event.getModelo())
                .anoFabricacao(event.getAnoModelo().getAnoFabricacao())
                .anoModelo(event.getAnoModelo().getAnoModelo())
                .cor(event.getEspecificacao().getCor())
                .tipoCombustivel(event.getEspecificacao().getTipoCombustivel().name())
                .categoria(event.getEspecificacao().getCategoria().name())
                .cilindrada(event.getEspecificacao().getCilindrada())
                .proprietarioCpf(event.getProprietario().getCpfCnpj())
                .proprietarioNome(event.getProprietario().getNome())
                .proprietarioTipo(event.getProprietario().getTipoPessoa().name())
                .status(event.getStatus())
                .apoliceAtiva(false)
                .quantidadeApolices(0)
                .build();
            
            queryRepository.save(queryModel);
            
            log.info("Projeção criada para veículo ID: {}", event.getAggregateId());
            
        } catch (Exception e) {
            log.error("Erro ao processar VeiculoCriadoEvent para veículo ID: {}", 
                event.getAggregateId(), e);
            throw e;
        }
    }

    @TransactionalEventListener
    @Transactional
    @CacheEvict(value = "veiculos", key = "#event.aggregateId")
    public void on(VeiculoAtualizadoEvent event) {
        log.info("Processando VeiculoAtualizadoEvent para veículo ID: {}", event.getAggregateId());
        
        try {
            VeiculoQueryModel queryModel = queryRepository.findById(event.getAggregateId())
                .orElseThrow(() -> new IllegalStateException(
                    "Veículo não encontrado para atualização: " + event.getAggregateId()));
            
            // Atualizar especificações
            queryModel.setCor(event.getNovaEspecificacao().getCor());
            queryModel.setTipoCombustivel(event.getNovaEspecificacao().getTipoCombustivel().name());
            queryModel.setCilindrada(event.getNovaEspecificacao().getCilindrada());
            
            queryRepository.save(queryModel);
            
            log.info("Projeção atualizada para veículo ID: {}", event.getAggregateId());
            
        } catch (Exception e) {
            log.error("Erro ao processar VeiculoAtualizadoEvent para veículo ID: {}", 
                event.getAggregateId(), e);
            throw e;
        }
    }

    @TransactionalEventListener
    @Transactional
    @CacheEvict(value = "veiculos", key = "#event.aggregateId")
    public void on(PropriedadeTransferidaEvent event) {
        log.info("Processando PropriedadeTransferidaEvent para veículo ID: {}", event.getAggregateId());
        
        try {
            VeiculoQueryModel queryModel = queryRepository.findById(event.getAggregateId())
                .orElseThrow(() -> new IllegalStateException(
                    "Veículo não encontrado para transferência: " + event.getAggregateId()));
            
            // Atualizar dados do proprietário
            queryModel.setProprietarioCpf(event.getNovoProprietario().getCpfCnpj());
            queryModel.setProprietarioNome(event.getNovoProprietario().getNome());
            queryModel.setProprietarioTipo(event.getNovoProprietario().getTipoPessoa().name());
            
            queryRepository.save(queryModel);
            
            log.info("Propriedade transferida na projeção para veículo ID: {}", event.getAggregateId());
            
        } catch (Exception e) {
            log.error("Erro ao processar PropriedadeTransferidaEvent para veículo ID: {}", 
                event.getAggregateId(), e);
            throw e;
        }
    }

    @TransactionalEventListener
    @Transactional
    @CacheEvict(value = "veiculos", key = "#event.aggregateId")
    public void on(VeiculoAssociadoEvent event) {
        log.info("Processando VeiculoAssociadoEvent para veículo ID: {}, apólice: {}", 
            event.getAggregateId(), event.getApoliceId());
        
        try {
            VeiculoQueryModel queryModel = queryRepository.findById(event.getAggregateId())
                .orElseThrow(() -> new IllegalStateException(
                    "Veículo não encontrado para associação: " + event.getAggregateId()));
            
            // Atualizar informações de apólice
            queryModel.setApoliceAtiva(true);
            queryModel.setQuantidadeApolices(queryModel.getQuantidadeApolices() + 1);
            
            queryRepository.save(queryModel);
            
            log.info("Veículo associado na projeção ID: {}, apólice: {}", 
                event.getAggregateId(), event.getApoliceId());
            
        } catch (Exception e) {
            log.error("Erro ao processar VeiculoAssociadoEvent para veículo ID: {}", 
                event.getAggregateId(), e);
            throw e;
        }
    }

    @TransactionalEventListener
    @Transactional
    @CacheEvict(value = "veiculos", key = "#event.aggregateId")
    public void on(VeiculoDesassociadoEvent event) {
        log.info("Processando VeiculoDesassociadoEvent para veículo ID: {}, apólice: {}", 
            event.getAggregateId(), event.getApoliceId());
        
        try {
            VeiculoQueryModel queryModel = queryRepository.findById(event.getAggregateId())
                .orElseThrow(() -> new IllegalStateException(
                    "Veículo não encontrado para desassociação: " + event.getAggregateId()));
            
            // Atualizar informações de apólice
            int novaQuantidade = Math.max(0, queryModel.getQuantidadeApolices() - 1);
            queryModel.setQuantidadeApolices(novaQuantidade);
            queryModel.setApoliceAtiva(novaQuantidade > 0);
            
            queryRepository.save(queryModel);
            
            log.info("Veículo desassociado na projeção ID: {}, apólice: {}", 
                event.getAggregateId(), event.getApoliceId());
            
        } catch (Exception e) {
            log.error("Erro ao processar VeiculoDesassociadoEvent para veículo ID: {}", 
                event.getAggregateId(), e);
            throw e;
        }
    }
}