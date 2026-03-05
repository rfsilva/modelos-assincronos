package com.seguradora.hibrida.command;

import java.util.concurrent.CompletableFuture;

/**
 * Interface principal do Command Bus para processamento de comandos.
 * 
 * <p>O Command Bus é responsável por:</p>
 * <ul>
 *   <li>Receber comandos da camada de aplicação</li>
 *   <li>Rotear comandos para handlers apropriados</li>
 *   <li>Executar validações automáticas</li>
 *   <li>Gerenciar timeouts e retry</li>
 *   <li>Coletar métricas de execução</li>
 * </ul>
 * 
 * <p><strong>Características:</strong></p>
 * <ul>
 *   <li>Roteamento automático por tipo de comando</li>
 *   <li>Validação automática usando Bean Validation</li>
 *   <li>Suporte a execução síncrona e assíncrona</li>
 *   <li>Métricas detalhadas de performance</li>
 *   <li>Logs estruturados com correlation ID</li>
 * </ul>
 * 
 * <p><strong>Exemplo de Uso:</strong></p>
 * <pre>{@code
 * @RestController
 * public class SeguradoController {
 *     
 *     @Autowired
 *     private CommandBus commandBus;
 *     
 *     @PostMapping("/segurados")
 *     public ResponseEntity<ApiResponse> criarSegurado(@RequestBody CriarSeguradoRequest request) {
 *         CriarSeguradoCommand command = CriarSeguradoCommand.builder()
 *             .cpf(request.getCpf())
 *             .nome(request.getNome())
 *             .email(request.getEmail())
 *             .correlationId(UUID.randomUUID())
 *             .userId(getCurrentUserId())
 *             .build();
 *         
 *         CommandResult result = commandBus.send(command);
 *         
 *         if (result.isSuccess()) {
 *             return ResponseEntity.ok(ApiResponse.success(result.getData()));
 *         } else {
 *             return ResponseEntity.badRequest()
 *                 .body(ApiResponse.error(result.getErrorMessage()));
 *         }
 *     }
 * }
 * }</pre>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface CommandBus {
    
    /**
     * Envia um comando para processamento síncrono.
     * 
     * <p>Este método:</p>
     * <ul>
     *   <li>Valida o comando automaticamente</li>
     *   <li>Encontra o handler apropriado</li>
     *   <li>Executa o comando com timeout</li>
     *   <li>Coleta métricas de execução</li>
     *   <li>Retorna o resultado</li>
     * </ul>
     * 
     * @param command Comando a ser processado
     * @return Resultado da execução do comando
     * @throws CommandHandlerNotFoundException se não houver handler para o comando
     * @throws CommandValidationException se o comando for inválido
     * @throws CommandTimeoutException se exceder o timeout
     */
    CommandResult send(Command command);
    
    /**
     * Envia um comando para processamento assíncrono.
     * 
     * <p>Útil para comandos que podem demorar ou que não precisam
     * de resposta imediata.</p>
     * 
     * @param command Comando a ser processado
     * @return CompletableFuture com o resultado da execução
     */
    CompletableFuture<CommandResult> sendAsync(Command command);
    
    /**
     * Registra um handler no Command Bus.
     * 
     * <p>Normalmente chamado automaticamente durante a inicialização
     * do Spring para handlers anotados com @Component.</p>
     * 
     * @param handler Handler a ser registrado
     * @param <T> Tipo do comando processado pelo handler
     */
    <T extends Command> void registerHandler(CommandHandler<T> handler);
    
    /**
     * Remove um handler do Command Bus.
     * 
     * @param commandType Tipo do comando cujo handler deve ser removido
     */
    void unregisterHandler(Class<? extends Command> commandType);
    
    /**
     * Verifica se existe handler registrado para o tipo de comando.
     * 
     * @param commandType Tipo do comando a verificar
     * @return true se existe handler, false caso contrário
     */
    boolean hasHandler(Class<? extends Command> commandType);
    
    /**
     * Obtém estatísticas de execução do Command Bus.
     * 
     * @return Estatísticas de comandos processados
     */
    CommandBusStatistics getStatistics();
}