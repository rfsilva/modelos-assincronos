package com.seguradora.hibrida.domain.veiculo.command.handler;

import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.veiculo.aggregate.VeiculoAggregate;
import com.seguradora.hibrida.domain.veiculo.command.AssociarVeiculoCommand;
import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.veiculo.service.ApoliceValidationService;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Handler para o comando de associação de veículo à apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
public class AssociarVeiculoCommandHandler implements CommandHandler<AssociarVeiculoCommand> {
    
    private final AggregateRepository<VeiculoAggregate> veiculoRepository;
    private final ApoliceValidationService apoliceValidationService;
    
    public AssociarVeiculoCommandHandler(AggregateRepository<VeiculoAggregate> veiculoRepository,
                                        ApoliceValidationService apoliceValidationService) {
        this.veiculoRepository = veiculoRepository;
        this.apoliceValidationService = apoliceValidationService;
    }
    
    @Override
    @Transactional
    public CommandResult handle(AssociarVeiculoCommand command) {
        try {
            // Carregar aggregate do veículo
            VeiculoAggregate veiculo = veiculoRepository.getById(command.getVeiculoId());
            
            // Validar apólice
            apoliceValidationService.validarApoliceParaAssociacao(command.getApoliceId(), command.getVeiculoId());
            
            // Associar veículo à apólice
            veiculo.associarApolice(
                command.getApoliceId(),
                command.getDataInicio(),
                command.getOperadorId()
            );
            
            // Salvar alterações
            veiculoRepository.save(veiculo);
            
            // Retornar resultado de sucesso
            return CommandResult.success(command.getVeiculoId(), Map.of(
                "apoliceId", command.getApoliceId(),
                "dataInicio", command.getDataInicio(),
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
    public Class<AssociarVeiculoCommand> getCommandType() {
        return AssociarVeiculoCommand.class;
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 20;
    }
}