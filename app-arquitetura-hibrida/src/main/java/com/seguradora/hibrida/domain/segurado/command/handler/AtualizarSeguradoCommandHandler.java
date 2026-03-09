package com.seguradora.hibrida.domain.segurado.command.handler;

import com.seguradora.hibrida.aggregate.exception.BusinessRuleViolationException;
import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.segurado.aggregate.SeguradoAggregate;
import com.seguradora.hibrida.domain.segurado.command.AtualizarSeguradoCommand;
import com.seguradora.hibrida.domain.segurado.service.SeguradoValidationService;
import com.seguradora.hibrida.eventstore.exception.ConcurrencyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Handler para atualização de segurado com controle de concorrência.
 * 
 * <p>Implementa todos os requisitos da US010:
 * <ul>
 *   <li>Controle de versão otimista</li>
 *   <li>Retry automático para conflitos</li>
 *   <li>Validações síncronas</li>
 *   <li>Tratamento de erros robusto</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AtualizarSeguradoCommandHandler implements CommandHandler<AtualizarSeguradoCommand> {
    
    private final AggregateRepository<SeguradoAggregate> aggregateRepository;
    private final SeguradoValidationService validationService;
    
    @Override
    @Retryable(
        value = {ConcurrencyException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public CommandResult handle(AtualizarSeguradoCommand command) {
        log.info("Processando comando AtualizarSegurado - ID: {}", command.getSeguradoId());
        
        try {
            // Carregar aggregate do repositório
            SeguradoAggregate aggregate = aggregateRepository.getById(command.getSeguradoId());
            
            // Verificar versão esperada para controle de concorrência
            if (command.getVersaoEsperada() != null && 
                !command.getVersaoEsperada().equals(aggregate.getVersion())) {
                
                throw new ConcurrencyException(
                    command.getSeguradoId(),
                    command.getVersaoEsperada(),
                    aggregate.getVersion()
                );
            }
            
            // Validações síncronas se email foi alterado
            if (command.getEmail() != null && !command.getEmail().equals(aggregate.getEmail())) {
                if (!validationService.isEmailUnico(command.getEmail())) {
                    throw new BusinessRuleViolationException(
                        "Email já está em uso por outro segurado",
                        java.util.List.of("Email deve ser único no sistema")
                    );
                }
            }
            
            // Aplicar alterações no aggregate
            aggregate.atualizarDados(
                command.getNome(),
                command.getEmail(),
                command.getTelefone(),
                command.getDataNascimento(),
                command.getEndereco()
            );
            
            // Salvar aggregate no repositório
            aggregateRepository.save(aggregate);
            
            log.info("Segurado atualizado com sucesso - ID: {}, Nova versão: {}", 
                    command.getSeguradoId(), aggregate.getVersion());
            
            return CommandResult.success(command.getSeguradoId())
                    .withMetadata("versao", aggregate.getVersion());
            
        } catch (ConcurrencyException e) {
            log.warn("Conflito de concorrência ao atualizar segurado {}: versão esperada {}, versão atual {}", 
                    command.getSeguradoId(), e.getExpectedVersion(), e.getActualVersion());
            throw e; // Será capturado pelo @Retryable
            
        } catch (BusinessRuleViolationException e) {
            log.warn("Falha na validação ao atualizar segurado {}: {}", 
                    command.getSeguradoId(), e.getMessage());
            return CommandResult.failure(e.getMessage());
            
        } catch (Exception e) {
            log.error("Erro ao processar comando AtualizarSegurado para ID: {}", 
                    command.getSeguradoId(), e);
            return CommandResult.failure("Erro interno do sistema");
        }
    }
    
    @Override
    public Class<AtualizarSeguradoCommand> getCommandType() {
        return AtualizarSeguradoCommand.class;
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 15; // Timeout maior devido às validações síncronas
    }
}