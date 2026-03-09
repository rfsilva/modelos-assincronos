package com.seguradora.hibrida.domain.veiculo.command.handler;

import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.veiculo.aggregate.VeiculoAggregate;
import com.seguradora.hibrida.domain.veiculo.command.AssociarVeiculoCommand;
import com.seguradora.hibrida.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler para o comando de associação de veículo a apólice.
 * Carrega o aggregate do Event Store e associa à apólice.
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssociarVeiculoCommandHandler implements CommandHandler<AssociarVeiculoCommand> {
    
    private final EventStore eventStore;

    @Override
    public CommandResult handle(AssociarVeiculoCommand command) {
        log.info("Processando comando AssociarVeiculo para ID: {}, Apólice: {}", 
            command.getVeiculoId(), command.getApoliceId());
        
        try {
            // Carregar aggregate do Event Store
            VeiculoAggregate aggregate = new VeiculoAggregate();
            aggregate.loadFromHistory(eventStore.loadEvents(command.getVeiculoId()));
            
            // Aplicar associação
            aggregate.associarApolice(
                command.getApoliceId(),
                command.getDataInicio(),
                command.getOperadorId()
            );
            
            // Persistir eventos
            eventStore.saveEvents(
                aggregate.getId(),
                aggregate.getUncommittedEvents(),
                aggregate.getVersion()
            );
            
            aggregate.markEventsAsCommitted();
            
            log.info("Veículo {} associado à apólice {} com sucesso", 
                command.getVeiculoId(), command.getApoliceId());
            
            return CommandResult.success(command.getVeiculoId());
            
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.error("Erro de validação ao associar veículo {} à apólice {}: {}", 
                command.getVeiculoId(), command.getApoliceId(), e.getMessage());
            return CommandResult.failure(e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao associar veículo {} à apólice {}: {}", 
                command.getVeiculoId(), command.getApoliceId(), e.getMessage(), e);
            return CommandResult.failure("Erro ao associar veículo: " + e.getMessage());
        }
    }

    @Override
    public Class<AssociarVeiculoCommand> getCommandType() {
        return AssociarVeiculoCommand.class;
    }
}
