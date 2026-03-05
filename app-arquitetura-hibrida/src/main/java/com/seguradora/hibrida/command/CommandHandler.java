package com.seguradora.hibrida.command;

/**
 * Interface base para todos os command handlers do sistema.
 * 
 * <p>Esta interface define o contrato que todos os handlers de comando devem implementar,
 * garantindo processamento consistente e tipado de comandos.</p>
 * 
 * <p><strong>Responsabilidades do Handler:</strong></p>
 * <ul>
 *   <li>Validar o comando recebido</li>
 *   <li>Executar a lógica de negócio</li>
 *   <li>Interagir com aggregates</li>
 *   <li>Persistir eventos no Event Store</li>
 *   <li>Retornar resultado da operação</li>
 * </ul>
 * 
 * <p><strong>Padrões de Implementação:</strong></p>
 * <ul>
 *   <li>Handlers devem ser stateless</li>
 *   <li>Usar @Component para registro automático</li>
 *   <li>Implementar validações síncronas críticas</li>
 *   <li>Usar transações quando necessário</li>
 *   <li>Implementar timeout adequado</li>
 * </ul>
 * 
 * <p><strong>Exemplo de Implementação:</strong></p>
 * <pre>{@code
 * @Component
 * @Slf4j
 * public class CriarSeguradoCommandHandler implements CommandHandler<CriarSeguradoCommand> {
 *     
 *     @Autowired
 *     private EventStore eventStore;
 *     
 *     @Override
 *     public CommandResult handle(CriarSeguradoCommand command) {
 *         try {
 *             // Validações síncronas críticas
 *             validarCpfUnico(command.getCpf());
 *             
 *             // Criar aggregate e aplicar evento
 *             SeguradoAggregate aggregate = new SeguradoAggregate(command);
 *             
 *             // Persistir eventos
 *             eventStore.saveEvents(aggregate.getId(), 
 *                                 aggregate.getUncommittedEvents(), 0);
 *             
 *             return CommandResult.success(aggregate.getId());
 *             
 *         } catch (Exception e) {
 *             log.error("Erro ao processar comando: {}", command, e);
 *             return CommandResult.failure(e.getMessage());
 *         }
 *     }
 *     
 *     @Override
 *     public Class<CriarSeguradoCommand> getCommandType() {
 *         return CriarSeguradoCommand.class;
 *     }
 * }
 * }</pre>
 * 
 * @param <T> Tipo específico do comando que este handler processa
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface CommandHandler<T extends Command> {
    
    /**
     * Processa o comando e retorna o resultado da operação.
     * 
     * <p>Este método deve:</p>
     * <ul>
     *   <li>Validar o comando</li>
     *   <li>Executar a lógica de negócio</li>
     *   <li>Persistir mudanças</li>
     *   <li>Retornar resultado apropriado</li>
     * </ul>
     * 
     * @param command Comando a ser processado
     * @return Resultado da execução do comando
     * @throws CommandValidationException se o comando for inválido
     * @throws CommandExecutionException se houver erro na execução
     */
    CommandResult handle(T command);
    
    /**
     * Retorna o tipo de comando que este handler processa.
     * 
     * <p>Usado pelo Command Bus para roteamento automático.</p>
     * 
     * @return Classe do comando processado por este handler
     */
    Class<T> getCommandType();
    
    /**
     * Indica se este handler suporta o tipo de comando especificado.
     * 
     * <p>Implementação padrão compara com getCommandType().
     * Pode ser sobrescrita para lógica customizada.</p>
     * 
     * @param commandType Tipo do comando a verificar
     * @return true se suporta o comando, false caso contrário
     */
    default boolean supports(Class<? extends Command> commandType) {
        return getCommandType().equals(commandType);
    }
    
    /**
     * Timeout em segundos para execução deste comando.
     * 
     * <p>Implementação padrão retorna 30 segundos.
     * Pode ser sobrescrita para comandos específicos.</p>
     * 
     * @return Timeout em segundos
     */
    default int getTimeoutSeconds() {
        return 30;
    }
}