package com.seguradora.hibrida.domain.veiculo.command.handler;

import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.veiculo.aggregate.VeiculoAggregate;
import com.seguradora.hibrida.domain.veiculo.command.CriarVeiculoCommand;
import com.seguradora.hibrida.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Handler para o comando de criação de veículo.
 * Valida os dados e cria um novo VeiculoAggregate.
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CriarVeiculoCommandHandler implements CommandHandler<CriarVeiculoCommand> {
    
    private final EventStore eventStore;

    @Override
    public CommandResult handle(CriarVeiculoCommand command) {
        log.info("Processando comando CriarVeiculo para placa: {}", command.getPlaca());
        
        try {
            // Gerar ID único para o veículo
            String veiculoId = UUID.randomUUID().toString();
            
            // Criar novo aggregate
            VeiculoAggregate aggregate = new VeiculoAggregate(
                veiculoId,
                command.getPlaca(),
                command.getRenavam(),
                command.getChassi(),
                command.getMarca(),
                command.getModelo(),
                command.getAnoModelo(),
                command.getEspecificacao(),
                command.getProprietario()
            );
            
            // Persistir eventos no Event Store
            eventStore.saveEvents(
                aggregate.getId(), 
                aggregate.getUncommittedEvents(), 
                0 // Versão inicial
            );
            
            // Marcar eventos como commitados
            aggregate.markEventsAsCommitted();
            
            log.info("Veículo criado com sucesso. ID: {}, Placa: {}", veiculoId, command.getPlaca());
            
            return CommandResult.success(veiculoId);
            
        } catch (Exception e) {
            log.error("Erro ao criar veículo com placa {}: {}", command.getPlaca(), e.getMessage(), e);
            return CommandResult.failure(e.getMessage());
        }
    }

    @Override
    public Class<CriarVeiculoCommand> getCommandType() {
        return CriarVeiculoCommand.class;
    }
}
