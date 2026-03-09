package com.seguradora.hibrida.domain.veiculo.command.handler;

import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.veiculo.aggregate.VeiculoAggregate;
import com.seguradora.hibrida.domain.veiculo.command.DesassociarVeiculoCommand;
import com.seguradora.hibrida.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler para o comando de desassociação de veículo de apólice.
 * Carrega o aggregate do Event Store e desassocia da apólice.
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DesassociarVeiculoCommandHandler implements CommandHandler<DesassociarVeiculoCommand> {
    
    private final EventStore eventStore;

    @Override
    public CommandResult handle(DesassociarVeiculoCommand command) {
        log.info("Processando comando DesassociarVeiculo para ID: {}, Apólice: {}", 
            command.getVeiculoId(), command.getApoliceId());
        
        try {
            // Carregar aggregate do Event Store
            VeiculoAggregate aggregate = new VeiculoAggregate();
            aggregate.loadFromHistory(eventStore.loadEvents(command.getVeiculoId()));
            
            // Aplicar desassociação
            aggregate.desassociarApolice(
                command.getApoliceId(),
                command.getDataFim(),
                command.getMotivo(),
                command.getOperadorId()
            );
            
            // Persistir eventos
            eventStore.saveEvents(
                aggregate.getId(),
                aggregate.getUncommittedEvents(),
                aggregate.getVersion()
            );
            
            aggregate.markEventsAsCommitted();
            
            log.info("Veículo {} desassociado da apólice {} com sucesso", 
                command.getVeiculoId(), command.getApoliceId());
            
            return CommandResult.success(command.getVeiculoId());
            
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.error("Erro de validação ao desassociar veículo {} da apólice {}: {}", 
                command.getVeiculoId(), command.getApoliceId(), e.getMessage());
            return CommandResult.failure(e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao desassociar veículo {} da apólice {}: {}", 
                command.getVeiculoId(), command.getApoliceId(), e.getMessage(), e);
            return CommandResult.failure("Erro ao desassociar veículo: " + e.getMessage());
        }
    }

    @Override
    public Class<DesassociarVeiculoCommand> getCommandType() {
        return DesassociarVeiculoCommand.class;
    }
}
