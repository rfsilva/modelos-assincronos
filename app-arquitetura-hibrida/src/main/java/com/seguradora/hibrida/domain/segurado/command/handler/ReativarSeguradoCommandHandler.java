package com.seguradora.hibrida.domain.segurado.command.handler;

import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.segurado.aggregate.SeguradoAggregate;
import com.seguradora.hibrida.domain.segurado.command.ReativarSeguradoCommand;
import com.seguradora.hibrida.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler responsável por processar comandos de reativação de segurado.
 * 
 * <p>Este handler carrega o aggregate do Event Store, aplica a reativação
 * e persiste os eventos gerados.</p>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReativarSeguradoCommandHandler implements CommandHandler<ReativarSeguradoCommand> {
    
    private final EventStore eventStore;
    
    /**
     * Processa o comando de reativação de segurado.
     * 
     * @param command Comando contendo o ID e motivo da reativação
     * @return CommandResult com o ID do segurado reativado
     */
    @Override
    public CommandResult handle(ReativarSeguradoCommand command) {
        log.info("Processando comando ReativarSeguradoCommand para Segurado ID: {}", command.getSeguradoId());
        
        try {
            // Carregar aggregate do Event Store
            SeguradoAggregate aggregate = new SeguradoAggregate();
            aggregate.loadFromHistory(eventStore.loadEvents(command.getSeguradoId()));
            
            // Executar lógica de negócio
            aggregate.reativar(command.getMotivo());
            
            // Persistir novos eventos no Event Store
            eventStore.saveEvents(
                aggregate.getId(),
                aggregate.getUncommittedEvents(),
                aggregate.getVersion() - aggregate.getUncommittedEvents().size()
            );
            
            // Marcar eventos como commitados
            aggregate.markEventsAsCommitted();
            
            log.info("Segurado reativado com sucesso. ID: {}, Motivo: {}", 
                    command.getSeguradoId(), command.getMotivo());
            
            return CommandResult.success(command.getSeguradoId());
            
        } catch (Exception e) {
            log.error("Erro ao processar ReativarSeguradoCommand para Segurado ID: {}", 
                    command.getSeguradoId(), e);
            return CommandResult.failure(e.getMessage());
        }
    }
    
    @Override
    public Class<ReativarSeguradoCommand> getCommandType() {
        return ReativarSeguradoCommand.class;
    }
}
