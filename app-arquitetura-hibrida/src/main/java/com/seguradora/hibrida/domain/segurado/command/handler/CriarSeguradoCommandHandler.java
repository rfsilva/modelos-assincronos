package com.seguradora.hibrida.domain.segurado.command.handler;

import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.segurado.aggregate.SeguradoAggregate;
import com.seguradora.hibrida.domain.segurado.command.CriarSeguradoCommand;
import com.seguradora.hibrida.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Handler responsável por processar comandos de criação de segurado.
 * 
 * <p>Este handler valida o comando, cria uma nova instância do aggregate
 * SeguradoAggregate e persiste os eventos gerados no Event Store.</p>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CriarSeguradoCommandHandler implements CommandHandler<CriarSeguradoCommand> {
    
    private final EventStore eventStore;
    
    /**
     * Processa o comando de criação de segurado.
     * 
     * @param command Comando contendo os dados do novo segurado
     * @return CommandResult com o ID do segurado criado
     */
    @Override
    public CommandResult handle(CriarSeguradoCommand command) {
        log.info("Processando comando CriarSeguradoCommand para CPF: {}", command.getCpf());
        
        try {
            // Gerar novo ID para o aggregate (String para compatibilidade com AggregateRoot)
            String seguradoId = UUID.randomUUID().toString();
            
            // Criar nova instância do aggregate usando construtor que aplica evento
            SeguradoAggregate aggregate = new SeguradoAggregate(
                seguradoId,
                command.getCpf(),
                command.getNome(),
                command.getEmail(),
                command.getTelefone(),
                command.getDataNascimento(),
                command.getEndereco()
            );
            
            // Persistir eventos no Event Store
            eventStore.saveEvents(
                aggregate.getId(),
                aggregate.getUncommittedEvents(),
                0 // Versão inicial
            );
            
            // Marcar eventos como commitados
            aggregate.markEventsAsCommitted();
            
            log.info("Segurado criado com sucesso. ID: {}, CPF: {}", seguradoId, command.getCpf());
            
            return CommandResult.success(seguradoId);
            
        } catch (Exception e) {
            log.error("Erro ao processar CriarSeguradoCommand para CPF: {}", command.getCpf(), e);
            return CommandResult.failure(e.getMessage());
        }
    }
    
    @Override
    public Class<CriarSeguradoCommand> getCommandType() {
        return CriarSeguradoCommand.class;
    }
}
