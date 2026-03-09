package com.seguradora.hibrida.domain.segurado.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.segurado.aggregate.SeguradoAggregate;
import com.seguradora.hibrida.domain.segurado.command.AtualizarEnderecoCommand;
import com.seguradora.hibrida.domain.segurado.service.CepValidationService;
import com.seguradora.hibrida.eventstore.exception.ConcurrencyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Command Handler para atualizar endereço do segurado.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AtualizarEnderecoCommandHandler implements CommandHandler<AtualizarEnderecoCommand> {
    
    private final AggregateRepository<SeguradoAggregate> aggregateRepository;
    private final CepValidationService cepValidationService;
    
    @Override
    @Retryable(
        value = {ConcurrencyException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public CommandResult handle(AtualizarEnderecoCommand command) {
        log.info("Processando comando AtualizarEndereco - SeguradoId: {}, CEP: {}", 
                command.getSeguradoId(), command.getEndereco().getCep());
        
        try {
            // Carregar aggregate do Event Store
            SeguradoAggregate segurado = aggregateRepository.getById(command.getSeguradoId());
            
            // Validar CEP se o serviço estiver disponível
            if (cepValidationService != null && 
                !cepValidationService.isCepValido(command.getEndereco().getCep())) {
                return CommandResult.failure("CEP inválido: " + command.getEndereco().getCep());
            }
            
            // Atualizar endereço no aggregate
            segurado.atualizarEndereco(command.getEndereco());
            
            // Salvar aggregate
            aggregateRepository.save(segurado);
            
            log.info("Endereço atualizado com sucesso - SeguradoId: {}, CEP: {}", 
                    command.getSeguradoId(), command.getEndereco().getCep());
            
            return CommandResult.success(segurado.getId());
            
        } catch (ConcurrencyException e) {
            log.warn("Conflito de concorrência ao atualizar endereço - SeguradoId: {}", 
                    command.getSeguradoId());
            throw e; // Será tratado pelo @Retryable
            
        } catch (Exception e) {
            log.error("Erro ao processar comando AtualizarEndereco - SeguradoId: {}", 
                     command.getSeguradoId(), e);
            return CommandResult.failure(e.getMessage());
        }
    }
    
    @Override
    public Class<AtualizarEnderecoCommand> getCommandType() {
        return AtualizarEnderecoCommand.class;
    }
}