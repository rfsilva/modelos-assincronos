package com.seguradora.hibrida.command.validation;

import com.seguradora.hibrida.command.Command;

/**
 * Interface para validadores customizados de comando.
 * 
 * <p>Esta interface permite implementar validações específicas de negócio
 * que vão além das validações Bean Validation padrão.</p>
 * 
 * <p><strong>Casos de Uso:</strong></p>
 * <ul>
 *   <li>Validações que dependem de estado do sistema</li>
 *   <li>Validações que requerem consultas ao banco</li>
 *   <li>Validações de regras de negócio complexas</li>
 *   <li>Validações que dependem de serviços externos</li>
 * </ul>
 * 
 * <p><strong>Exemplo de Implementação:</strong></p>
 * <pre>{@code
 * @Component
 * public class CpfUnicoValidator implements CommandValidator<CriarSeguradoCommand> {
 *     
 *     @Autowired
 *     private SeguradoRepository repository;
 *     
 *     @Override
 *     public ValidationResult validate(CriarSeguradoCommand command) {
 *         if (repository.existsByCpf(command.getCpf())) {
 *             return ValidationResult.invalid("CPF já cadastrado no sistema");
 *         }
 *         return ValidationResult.valid();
 *     }
 *     
 *     @Override
 *     public Class<CriarSeguradoCommand> getCommandType() {
 *         return CriarSeguradoCommand.class;
 *     }
 * }
 * }</pre>
 * 
 * @param <T> Tipo específico do comando a ser validado
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface CommandValidator<T extends Command> {
    
    /**
     * Valida o comando e retorna o resultado.
     * 
     * @param command Comando a ser validado
     * @return Resultado da validação
     */
    ValidationResult validate(T command);
    
    /**
     * Retorna o tipo de comando que este validador processa.
     * 
     * @return Classe do comando validado por este validador
     */
    Class<T> getCommandType();
    
    /**
     * Indica se este validador suporta o tipo de comando especificado.
     * 
     * @param commandType Tipo do comando a verificar
     * @return true se suporta o comando, false caso contrário
     */
    default boolean supports(Class<? extends Command> commandType) {
        return getCommandType().equals(commandType);
    }
    
    /**
     * Prioridade do validador (menor valor = maior prioridade).
     * 
     * <p>Validadores com maior prioridade são executados primeiro.
     * Útil quando há dependências entre validações.</p>
     * 
     * @return Prioridade do validador (padrão: 100)
     */
    default int getPriority() {
        return 100;
    }
}