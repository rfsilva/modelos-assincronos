package com.seguradora.hibrida.domain.veiculo.projection;

import com.seguradora.hibrida.projection.AbstractProjectionHandler;
import com.seguradora.hibrida.domain.veiculo.event.*;
import com.seguradora.hibrida.domain.veiculo.query.model.VeiculoQueryModel;
import com.seguradora.hibrida.domain.veiculo.query.repository.VeiculoQueryRepository;
import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Handler de projeção para eventos de veículo.
 * 
 * <p>Responsável por manter as projeções de leitura atualizadas
 * com base nos eventos de domínio do aggregate VeiculoAggregate.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
public class VeiculoProjectionHandler extends AbstractProjectionHandler<VeiculoCriadoEvent> {
    
    private final VeiculoQueryRepository veiculoQueryRepository;
    
    public VeiculoProjectionHandler(VeiculoQueryRepository veiculoQueryRepository) {
        this.veiculoQueryRepository = veiculoQueryRepository;
    }
    
    @Override
    public Class<VeiculoCriadoEvent> getEventType() {
        return VeiculoCriadoEvent.class;
    }
    
    @Override
    public String getProjectionName() {
        return "VeiculoProjection";
    }
    
    @Override
    @Transactional
    protected void doHandle(VeiculoCriadoEvent event) {
        VeiculoQueryModel queryModel = new VeiculoQueryModel();
        
        // Dados básicos do veículo
        queryModel.setId(event.getAggregateId());
        queryModel.setPlaca(event.getPlaca());
        queryModel.setRenavam(event.getRenavam());
        queryModel.setChassi(event.getChassi());
        queryModel.setMarca(event.getMarca());
        queryModel.setModelo(event.getModelo());
        queryModel.setAnoFabricacao(event.getAnoFabricacao());
        queryModel.setAnoModelo(event.getAnoModelo());
        
        // Especificações
        queryModel.setCor(event.getCor());
        queryModel.setTipoCombustivel(event.getTipoCombustivel());
        queryModel.setCategoria(event.getCategoria());
        queryModel.setCilindrada(event.getCilindrada());
        
        // Proprietário
        queryModel.setProprietarioCpf(event.getProprietarioCpfCnpj());
        queryModel.setProprietarioNome(event.getProprietarioNome());
        queryModel.setProprietarioTipo(event.getProprietarioTipo());
        
        // Status e metadados
        queryModel.setStatus(StatusVeiculo.ATIVO);
        queryModel.setApoliceAtiva(false);
        queryModel.setVersion(event.getVersion());
        queryModel.setLastEventId(1L); // Primeiro evento
        
        veiculoQueryRepository.save(queryModel);
    }
    
    /**
     * Handler para evento de atualização de veículo.
     */
    public void on(VeiculoAtualizadoEvent event) {
        Optional<VeiculoQueryModel> optionalModel = veiculoQueryRepository.findById(event.getAggregateId());
        
        if (optionalModel.isPresent()) {
            VeiculoQueryModel queryModel = optionalModel.get();
            
            // Atualizar especificações
            if (event.getNovaEspecificacao() != null) {
                queryModel.setCor(event.getNovaEspecificacao().getCor());
                queryModel.setTipoCombustivel(event.getNovaEspecificacao().getTipoCombustivel().name());
                queryModel.setCilindrada(event.getNovaEspecificacao().getCilindrada());
            }
            
            // Atualizar metadados
            queryModel.setVersion(event.getVersion());
            queryModel.onUpdate();
            
            veiculoQueryRepository.save(queryModel);
        }
    }
    
    /**
     * Handler para evento de associação de veículo.
     */
    public void on(VeiculoAssociadoEvent event) {
        Optional<VeiculoQueryModel> optionalModel = veiculoQueryRepository.findById(event.getAggregateId());
        
        if (optionalModel.isPresent()) {
            VeiculoQueryModel queryModel = optionalModel.get();
            
            // Marcar como tendo apólice ativa
            queryModel.setApoliceAtiva(true);
            queryModel.setVersion(event.getVersion());
            queryModel.onUpdate();
            
            veiculoQueryRepository.save(queryModel);
        }
    }
    
    /**
     * Handler para evento de desassociação de veículo.
     */
    public void on(VeiculoDesassociadoEvent event) {
        Optional<VeiculoQueryModel> optionalModel = veiculoQueryRepository.findById(event.getAggregateId());
        
        if (optionalModel.isPresent()) {
            VeiculoQueryModel queryModel = optionalModel.get();
            
            // Verificar se ainda tem outras apólices ativas
            // Por simplicidade, assumindo que não tem
            queryModel.setApoliceAtiva(false);
            queryModel.setVersion(event.getVersion());
            queryModel.onUpdate();
            
            veiculoQueryRepository.save(queryModel);
        }
    }
    
    /**
     * Handler para evento de transferência de propriedade.
     */
    public void on(PropriedadeTransferidaEvent event) {
        Optional<VeiculoQueryModel> optionalModel = veiculoQueryRepository.findById(event.getAggregateId());
        
        if (optionalModel.isPresent()) {
            VeiculoQueryModel queryModel = optionalModel.get();
            
            // Atualizar dados do proprietário
            queryModel.setProprietarioCpf(event.getNovoProprietario().getCpfCnpj());
            queryModel.setProprietarioNome(event.getNovoProprietario().getNome());
            queryModel.setProprietarioTipo(event.getNovoProprietario().getTipoPessoa().name());
            
            // Atualizar metadados
            queryModel.setVersion(event.getVersion());
            queryModel.onUpdate();
            
            veiculoQueryRepository.save(queryModel);
        }
    }
    
    @Override
    public boolean supports(VeiculoCriadoEvent event) {
        return event != null && event.getAggregateId() != null;
    }
    
    @Override
    public int getOrder() {
        return 100;
    }
    
    @Override
    public boolean isAsync() {
        return true;
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 30;
    }
    
    @Override
    public boolean isRetryable() {
        return true;
    }
    
    @Override
    public int getMaxRetries() {
        return 3;
    }
}