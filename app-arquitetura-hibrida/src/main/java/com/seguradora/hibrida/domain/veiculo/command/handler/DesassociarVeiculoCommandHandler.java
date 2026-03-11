package com.seguradora.hibrida.domain.veiculo.command.handler;

import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.veiculo.aggregate.VeiculoAggregate;
import com.seguradora.hibrida.domain.veiculo.command.DesassociarVeiculoCommand;
import com.seguradora.hibrida.aggregate.repository.AggregateRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Handler para o comando de desassociação de veículo da apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
public class DesassociarVeiculoCommandHandler implements CommandHandler<DesassociarVeiculoCommand> {
    
    private final AggregateRepository<VeiculoAggregate> veiculoRepository;
    
    public DesassociarVeiculoCommandHandler(AggregateRepository<VeiculoAggregate> veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }
    
    @Override
    @Transactional
    public CommandResult handle(DesassociarVeiculoCommand command) {
        try {
            // Carregar aggregate do veículo
            VeiculoAggregate veiculo = veiculoRepository.getById(command.getVeiculoId());
            
            // Desassociar veículo da apólice
            veiculo.desassociarApolice(
                command.getApoliceId(),
                command.getDataFim(),
                command.getMotivo(),
                command.getOperadorId()
            );
            
            // Salvar alterações
            veiculoRepository.save(veiculo);
            
            // Retornar resultado de sucesso
            return CommandResult.success(command.getVeiculoId(), Map.of(
                "apoliceId", command.getApoliceId(),
                "dataFim", command.getDataFim(),
                "motivo", command.getMotivo(),
                "version", veiculo.getVersion()
            )).withCorrelationId(command.getCorrelationId());
            
        } catch (Exception e) {
            return CommandResult.failure(e)
                .withCorrelationId(command.getCorrelationId())
                .withMetadata("veiculoId", command.getVeiculoId())
                .withMetadata("apoliceId", command.getApoliceId());
        }
    }
    
    @Override
    public Class<DesassociarVeiculoCommand> getCommandType() {
        return DesassociarVeiculoCommand.class;
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 20;
    }
}