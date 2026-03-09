package com.seguradora.hibrida.domain.veiculo.command.handler;

import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.veiculo.aggregate.VeiculoAggregate;
import com.seguradora.hibrida.domain.veiculo.command.AtualizarVeiculoCommand;
import com.seguradora.hibrida.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler para o comando de atualização de especificações de veículo.
 * Carrega o aggregate do Event Store e aplica as alterações.
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AtualizarVeiculoCommandHandler implements CommandHandler<AtualizarVeiculoCommand> {
    
    private final EventStore eventStore;

    @Override
    public CommandResult handle(AtualizarVeiculoCommand command) {
        log.info("Processando comando AtualizarVeiculo para ID: {}", command.getVeiculoId());
        
        try {
            // Carregar aggregate do Event Store
            VeiculoAggregate aggregate = new VeiculoAggregate();
            aggregate.loadFromHistory(eventStore.loadEvents(command.getVeiculoId()));
            
            // Verificar versão esperada (controle de concorrência otimista)
            if (aggregate.getVersion() != command.getVersaoEsperada()) {
                String errorMsg = String.format(
                    "Conflito de concorrência: versão esperada %d, versão atual %d",
                    command.getVersaoEsperada(), aggregate.getVersion()
                );
                log.warn(errorMsg);
                return CommandResult.failure(errorMsg);
            }
            
            // Aplicar alterações
            aggregate.atualizarEspecificacoes(
                command.getNovaEspecificacao(),
                command.getOperadorId()
            );
            
            // Persistir eventos
            eventStore.saveEvents(
                aggregate.getId(),
                aggregate.getUncommittedEvents(),
                aggregate.getVersion()
            );
            
            aggregate.markEventsAsCommitted();
            
            log.info("Veículo {} atualizado com sucesso", command.getVeiculoId());
            
            return CommandResult.success(command.getVeiculoId());
            
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.error("Erro de validação ao atualizar veículo {}: {}", 
                command.getVeiculoId(), e.getMessage());
            return CommandResult.failure(e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar veículo {}: {}", 
                command.getVeiculoId(), e.getMessage(), e);
            return CommandResult.failure("Erro ao atualizar veículo: " + e.getMessage());
        }
    }

    @Override
    public Class<AtualizarVeiculoCommand> getCommandType() {
        return AtualizarVeiculoCommand.class;
    }
}
