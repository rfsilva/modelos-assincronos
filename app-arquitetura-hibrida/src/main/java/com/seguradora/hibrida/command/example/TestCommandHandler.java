package com.seguradora.hibrida.command.example;

import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Handler de exemplo para TestCommand.
 * 
 * <p>Este handler demonstra como implementar um CommandHandler
 * com validações, processamento e retorno de resultado.</p>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
@Slf4j
public class TestCommandHandler implements CommandHandler<TestCommand> {
    
    @Override
    public CommandResult handle(TestCommand command) {
        log.info("Processando TestCommand: {} [{}]", 
                command.getData(), command.getCommandId());
        
        try {
            // Simular processamento
            Thread.sleep(100);
            
            // Validações específicas de negócio
            if (command.getValue() != null && command.getValue() < 0) {
                return CommandResult.failure("Valor não pode ser negativo", "INVALID_VALUE");
            }
            
            // Simular processamento bem-sucedido
            String resultId = "processed-" + command.getCommandId().toString().substring(0, 8);
            
            Map<String, Object> metadata = Map.of(
                "processedAt", java.time.Instant.now(),
                "originalData", command.getData(),
                "processedValue", command.getValue() != null ? command.getValue() * 2 : 0
            );
            
            log.info("TestCommand processado com sucesso: {} -> {}", 
                    command.getCommandId(), resultId);
            
            return CommandResult.success(resultId, metadata);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Processamento interrompido para comando: {}", command.getCommandId(), e);
            return CommandResult.failure("Processamento interrompido", "INTERRUPTED");
            
        } catch (Exception e) {
            log.error("Erro ao processar TestCommand: {}", command.getCommandId(), e);
            return CommandResult.failure("Erro interno: " + e.getMessage(), "INTERNAL_ERROR");
        }
    }
    
    @Override
    public Class<TestCommand> getCommandType() {
        return TestCommand.class;
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 10; // Timeout customizado de 10 segundos
    }
}