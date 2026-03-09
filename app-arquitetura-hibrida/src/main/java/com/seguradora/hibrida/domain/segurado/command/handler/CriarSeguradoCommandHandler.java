package com.seguradora.hibrida.domain.segurado.command.handler;

import com.seguradora.hibrida.aggregate.exception.BusinessRuleViolationException;
import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.segurado.aggregate.SeguradoAggregate;
import com.seguradora.hibrida.domain.segurado.command.CriarSeguradoCommand;
import com.seguradora.hibrida.domain.segurado.service.SeguradoValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Handler para criação de segurado com validações síncronas.
 * 
 * <p>Implementa todos os requisitos da US010:
 * <ul>
 *   <li>Validação de unicidade de CPF</li>
 *   <li>Verificação em bureaus de crédito</li>
 *   <li>Validação de unicidade de email</li>
 *   <li>Cache de validações</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CriarSeguradoCommandHandler implements CommandHandler<CriarSeguradoCommand> {
    
    private final AggregateRepository<SeguradoAggregate> aggregateRepository;
    private final SeguradoValidationService validationService;
    
    @Override
    public CommandResult handle(CriarSeguradoCommand command) {
        log.info("Processando comando CriarSegurado com validações - CPF: {}", 
                command.getCpf().replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.***.***-$4"));
        
        try {
            // Validações síncronas antes da criação
            SeguradoValidationService.ValidationResult validationResult = 
                validationService.validarCriacaoSegurado(command.getCpf(), command.getEmail());
            
            if (!validationResult.isValido()) {
                throw new BusinessRuleViolationException(
                    "Falha na validação do segurado",
                    validationResult.getErros()
                );
            }
            
            // Gerar novo ID para o aggregate
            String seguradoId = UUID.randomUUID().toString();
            
            // Criar nova instância do aggregate
            SeguradoAggregate aggregate = new SeguradoAggregate(
                seguradoId,
                command.getCpf(),
                command.getNome(),
                command.getEmail(),
                command.getTelefone(),
                command.getDataNascimento(),
                command.getEndereco()
            );
            
            // Salvar aggregate no repositório
            aggregateRepository.save(aggregate);
            
            log.info("Segurado criado com sucesso - ID: {}", seguradoId);
            
            return CommandResult.success(seguradoId);
            
        } catch (BusinessRuleViolationException e) {
            log.warn("Falha na validação ao criar segurado: {}", e.getMessage());
            return CommandResult.failure(e.getMessage());
            
        } catch (Exception e) {
            log.error("Erro ao processar comando CriarSegurado", e);
            return CommandResult.failure("Erro interno do sistema");
        }
    }
    
    @Override
    public Class<CriarSeguradoCommand> getCommandType() {
        return CriarSeguradoCommand.class;
    }
}