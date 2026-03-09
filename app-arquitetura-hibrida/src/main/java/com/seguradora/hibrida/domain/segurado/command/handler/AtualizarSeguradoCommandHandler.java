package com.seguradora.hibrida.domain.segurado.command.handler;

import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.segurado.aggregate.SeguradoAggregate;
import com.seguradora.hibrida.domain.segurado.command.AtualizarSeguradoCommand;
import com.seguradora.hibrida.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler responsável por processar comandos de atualização de segurado.
 * 
 * <p>Este handler carrega o aggregate do Event Store, aplica as alterações
 * e persiste os novos eventos gerados.</p>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AtualizarSeguradoCommandHandler implements CommandHandler<AtualizarSeguradoCommand> {
    
    private final EventStore eventStore;
    
    /**
     * Processa o comando de atualização de segurado.
     * 
     * @param command Comando contendo os dados atualizados do segurado
     * @return CommandResult com o ID do segurado atualizado
     */
    @Override
    public CommandResult handle(AtualizarSeguradoCommand command) {
        log.info("Processando comando AtualizarSeguradoCommand para Segurado ID: {}", command.getSeguradoId());
        
        try {
            // Carregar aggregate do Event Store
            SeguradoAggregate aggregate = new SeguradoAggregate();
            aggregate.loadFromHistory(eventStore.loadEvents(command.getSeguradoId()));
            
            // Executar lógica de negócio
            aggregate.atualizarDados(
                command.getNome(),
                command.getEmail(),
                command.getTelefone(),
                command.getDataNascimento(),
                command.getEndereco()
            );
            
            // Persistir novos eventos no Event Store
            eventStore.saveEvents(
                aggregate.getId(),
                aggregate.getUncommittedEvents(),
                aggregate.getVersion() - aggregate.getUncommittedEvents().size()
            );
            
            // Marcar eventos como commitados
            aggregate.markEventsAsCommitted();
            
            log.info("Segurado atualizado com sucesso. ID: {}", command.getSeguradoId());
            
            return CommandResult.success(command.getSeguradoId());
            
        } catch (Exception e) {
            log.error("Erro ao processar AtualizarSeguradoCommand para Segurado ID: {}", command.getSeguradoId(), e);
            return CommandResult.failure(e.getMessage());
        }
    }
    
    @Override
    public Class<AtualizarSeguradoCommand> getCommandType() {
        return AtualizarSeguradoCommand.class;
    }
}
