package com.seguradora.hibrida.domain.segurado.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.segurado.aggregate.SeguradoAggregate;
import com.seguradora.hibrida.domain.segurado.command.RemoverContatoCommand;
import com.seguradora.hibrida.eventstore.exception.ConcurrencyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Command Handler para remover contato do segurado.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoverContatoCommandHandler implements CommandHandler<RemoverContatoCommand> {
    
    private final AggregateRepository<SeguradoAggregate> aggregateRepository;
    
    @Override
    @Retryable(
        value = {ConcurrencyException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public CommandResult handle(RemoverContatoCommand command) {
        log.info("Processando comando RemoverContato - SeguradoId: {}, Tipo: {}", 
                command.getSeguradoId(), command.getTipo());
        
        try {
            // Carregar aggregate do Event Store
            SeguradoAggregate segurado = aggregateRepository.getById(command.getSeguradoId());
            
            // Remover contato no aggregate
            segurado.removerContato(command.getTipo(), command.getValor());
            
            // Salvar aggregate
            aggregateRepository.save(segurado);
            
            log.info("Contato removido com sucesso - SeguradoId: {}, Tipo: {}", 
                    command.getSeguradoId(), command.getTipo());
            
            return CommandResult.success(segurado.getId());
            
        } catch (ConcurrencyException e) {
            log.warn("Conflito de concorrência ao remover contato - SeguradoId: {}", 
                    command.getSeguradoId());
            throw e; // Será tratado pelo @Retryable
            
        } catch (Exception e) {
            log.error("Erro ao processar comando RemoverContato - SeguradoId: {}", 
                     command.getSeguradoId(), e);
            return CommandResult.failure(e.getMessage());
        }
    }
    
    @Override
    public Class<RemoverContatoCommand> getCommandType() {
        return RemoverContatoCommand.class;
    }
}