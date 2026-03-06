package com.seguradora.hibrida.aggregate.repository;

import com.seguradora.hibrida.aggregate.AggregateRoot;
import com.seguradora.hibrida.aggregate.exception.AggregateNotFoundException;
import com.seguradora.hibrida.eventstore.exception.ConcurrencyException;

import java.util.Optional;

/**
 * Interface para repositório de Aggregates com Event Sourcing.
 * 
 * <p>Abstrai a persistência e recuperação de aggregates usando Event Store
 * e sistema de snapshots para otimização. Implementa o padrão Repository
 * específico para arquitetura Event Sourcing.
 * 
 * <p><strong>Funcionalidades principais:</strong>
 * <ul>
 *   <li>Persistência de aggregates via eventos</li>
 *   <li>Recuperação otimizada com snapshots</li>
 *   <li>Controle de concorrência otimista</li>
 *   <li>Suporte a diferentes tipos de aggregate</li>
 * </ul>
 * 
 * <p><strong>Exemplo de uso:</strong>
 * <pre>{@code
 * @Service
 * public class SeguradoService {
 *     
 *     private final AggregateRepository<SeguradoAggregate> repository;
 *     
 *     public void criarSegurado(String nome, String cpf) {
 *         SeguradoAggregate segurado = new SeguradoAggregate();
 *         segurado.criar(nome, cpf);
 *         
 *         repository.save(segurado);
 *     }
 *     
 *     public void atualizarSegurado(String id, String novoNome) {
 *         SeguradoAggregate segurado = repository.findById(id)
 *             .orElseThrow(() -> new SeguradoNotFoundException(id));
 *         
 *         segurado.atualizar(novoNome);
 *         repository.save(segurado);
 *     }
 * }
 * }</pre>
 * 
 * @param <T> Tipo do aggregate que estende AggregateRoot
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface AggregateRepository<T extends AggregateRoot> {
    
    /**
     * Salva um aggregate persistindo seus eventos não commitados.
     * 
     * <p>Este método:
     * <ul>
     *   <li>Persiste eventos não commitados no Event Store</li>
     *   <li>Marca eventos como commitados após sucesso</li>
     *   <li>Cria snapshot se necessário (baseado em configuração)</li>
     *   <li>Publica eventos no Event Bus para processamento</li>
     * </ul>
     * 
     * @param aggregate Aggregate a ser salvo
     * @throws ConcurrencyException se houver conflito de versão
     * @throws IllegalArgumentException se aggregate for null ou inválido
     */
    void save(T aggregate);
    
    /**
     * Busca um aggregate por ID.
     * 
     * <p>Utiliza estratégia otimizada:
     * <ol>
     *   <li>Busca snapshot mais recente (se disponível)</li>
     *   <li>Carrega eventos incrementais desde o snapshot</li>
     *   <li>Reconstrói estado do aggregate</li>
     * </ol>
     * 
     * @param id ID único do aggregate
     * @return Optional contendo o aggregate ou empty se não encontrado
     */
    Optional<T> findById(String id);
    
    /**
     * Busca um aggregate por ID, lançando exceção se não encontrado.
     * 
     * @param id ID único do aggregate
     * @return Aggregate encontrado
     * @throws AggregateNotFoundException se não encontrado
     */
    default T getById(String id) {
        return findById(id)
                .orElseThrow(() -> new AggregateNotFoundException(id, getAggregateType()));
    }
    
    /**
     * Busca um aggregate em uma versão específica.
     * 
     * <p>Útil para auditoria e análise histórica.
     * Reconstrói o aggregate até a versão especificada.
     * 
     * @param id ID único do aggregate
     * @param version Versão específica desejada
     * @return Optional contendo o aggregate na versão ou empty se não encontrado
     */
    Optional<T> findByIdAndVersion(String id, long version);
    
    /**
     * Verifica se um aggregate existe.
     * 
     * @param id ID único do aggregate
     * @return true se existe, false caso contrário
     */
    boolean exists(String id);
    
    /**
     * Retorna a versão atual de um aggregate sem carregá-lo completamente.
     * 
     * <p>Operação otimizada que consulta apenas metadados.
     * 
     * @param id ID único do aggregate
     * @return Versão atual ou 0 se não existir
     */
    long getCurrentVersion(String id);
    
    /**
     * Remove todos os eventos de um aggregate.
     * 
     * <p><strong>ATENÇÃO:</strong> Esta operação é irreversível e deve ser
     * usada apenas em casos específicos como LGPD ou testes.
     * 
     * @param id ID único do aggregate
     * @return true se foi removido, false se não existia
     */
    boolean delete(String id);
    
    /**
     * Força criação de snapshot para um aggregate.
     * 
     * <p>Útil para otimização manual ou manutenção.
     * 
     * @param id ID único do aggregate
     * @return true se snapshot foi criado com sucesso
     */
    boolean createSnapshot(String id);
    
    /**
     * Retorna o tipo de aggregate gerenciado por este repositório.
     * 
     * @return Classe do tipo de aggregate
     */
    Class<T> getAggregateType();
    
    /**
     * Retorna estatísticas do repositório para monitoramento.
     * 
     * @return Mapa com estatísticas (total de aggregates, snapshots, etc.)
     */
    java.util.Map<String, Object> getStatistics();
}