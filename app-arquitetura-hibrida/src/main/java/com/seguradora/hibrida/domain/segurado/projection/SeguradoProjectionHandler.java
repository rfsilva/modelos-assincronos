package com.seguradora.hibrida.domain.segurado.projection;

import com.seguradora.hibrida.domain.segurado.event.*;
import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoQueryModel;
import com.seguradora.hibrida.domain.segurado.query.repository.SeguradoQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler responsável por projetar eventos de domínio para o modelo de leitura de Segurado.
 * 
 * <p>Este handler escuta eventos do Event Bus e atualiza o banco de dados de leitura,
 * mantendo a consistência eventual entre o Command Side e o Query Side.</p>
 * 
 * <p>Responsabilidades:
 * <ul>
 *   <li>Atualizar modelo de leitura em resposta a eventos</li>
 *   <li>Invalidar cache quando dados são modificados</li>
 *   <li>Garantir idempotência nas projeções</li>
 *   <li>Tratar erros e inconsistências</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeguradoProjectionHandler {
    
    private final SeguradoQueryRepository queryRepository;
    
    /**
     * Projeta evento de criação de segurado.
     * 
     * @param event Evento de criação
     */
    @EventListener
    @Transactional("projectionsTransactionManager")
    @CacheEvict(value = {"segurados", "seguradoByCpf", "seguradoByEmail"}, allEntries = true)
    public void on(SeguradoCriadoEvent event) {
        log.info("Projetando SeguradoCriadoEvent para ID: {}", event.getAggregateId());
        
        try {
            // Verificar se já existe (idempotência)
            if (queryRepository.existsById(event.getAggregateId())) {
                log.warn("Segurado já existe no modelo de leitura. ID: {}", event.getAggregateId());
                return;
            }
            
            // Criar novo modelo de leitura
            SeguradoQueryModel queryModel = SeguradoQueryModel.builder()
                    .id(event.getAggregateId())
                    .cpf(event.getCpf())
                    .nome(event.getNome())
                    .email(event.getEmail())
                    .telefone(event.getTelefone())
                    .dataNascimento(event.getDataNascimento())
                    .status(StatusSegurado.ATIVO)
                    // Endereço desnormalizado
                    .cep(event.getEndereco().getCep())
                    .logradouro(event.getEndereco().getLogradouro())
                    .numero(event.getEndereco().getNumero())
                    .complemento(event.getEndereco().getComplemento())
                    .bairro(event.getEndereco().getBairro())
                    .cidade(event.getEndereco().getCidade())
                    .estado(event.getEndereco().getEstado())
                    // Metadados
                    .createdAt(event.getTimestamp())
                    .updatedAt(event.getTimestamp())
                    .version(event.getVersion())
                    .build();
            
            queryRepository.save(queryModel);
            
            log.info("Segurado projetado com sucesso. ID: {}, CPF: {}", 
                    event.getAggregateId(), event.getCpf());
            
        } catch (Exception e) {
            log.error("Erro ao projetar SeguradoCriadoEvent. ID: {}", event.getAggregateId(), e);
            throw new RuntimeException("Erro na projeção de SeguradoCriadoEvent", e);
        }
    }
    
    /**
     * Projeta evento de atualização de segurado.
     * 
     * @param event Evento de atualização
     */
    @EventListener
    @Transactional("projectionsTransactionManager")
    @CacheEvict(value = {"segurados", "seguradoByCpf", "seguradoByEmail"}, key = "#event.aggregateId")
    public void on(SeguradoAtualizadoEvent event) {
        log.info("Projetando SeguradoAtualizadoEvent para ID: {}", event.getAggregateId());
        
        try {
            SeguradoQueryModel queryModel = queryRepository.findById(event.getAggregateId())
                    .orElseThrow(() -> new RuntimeException(
                            "Segurado não encontrado no modelo de leitura: " + event.getAggregateId()));
            
            // Atualizar dados
            queryModel.setNome(event.getNome());
            queryModel.setEmail(event.getEmail());
            queryModel.setTelefone(event.getTelefone());
            queryModel.setDataNascimento(event.getDataNascimento());
            
            // Atualizar endereço desnormalizado
            queryModel.setCep(event.getEndereco().getCep());
            queryModel.setLogradouro(event.getEndereco().getLogradouro());
            queryModel.setNumero(event.getEndereco().getNumero());
            queryModel.setComplemento(event.getEndereco().getComplemento());
            queryModel.setBairro(event.getEndereco().getBairro());
            queryModel.setCidade(event.getEndereco().getCidade());
            queryModel.setEstado(event.getEndereco().getEstado());
            
            // Atualizar metadados
            queryModel.setUpdatedAt(event.getTimestamp());
            queryModel.setVersion(event.getVersion());
            
            queryRepository.save(queryModel);
            
            log.info("Segurado atualizado com sucesso no modelo de leitura. ID: {}", event.getAggregateId());
            
        } catch (Exception e) {
            log.error("Erro ao projetar SeguradoAtualizadoEvent. ID: {}", event.getAggregateId(), e);
            throw new RuntimeException("Erro na projeção de SeguradoAtualizadoEvent", e);
        }
    }
    
    /**
     * Projeta evento de desativação de segurado.
     * 
     * @param event Evento de desativação
     */
    @EventListener
    @Transactional("projectionsTransactionManager")
    @CacheEvict(value = {"segurados", "seguradoByCpf", "seguradoByEmail"}, key = "#event.aggregateId")
    public void on(SeguradoDesativadoEvent event) {
        log.info("Projetando SeguradoDesativadoEvent para ID: {}", event.getAggregateId());
        
        try {
            SeguradoQueryModel queryModel = queryRepository.findById(event.getAggregateId())
                    .orElseThrow(() -> new RuntimeException(
                            "Segurado não encontrado no modelo de leitura: " + event.getAggregateId()));
            
            // Atualizar status
            queryModel.setStatus(StatusSegurado.INATIVO);
            queryModel.setUpdatedAt(event.getTimestamp());
            queryModel.setVersion(event.getVersion());
            
            queryRepository.save(queryModel);
            
            log.info("Segurado desativado com sucesso no modelo de leitura. ID: {}", event.getAggregateId());
            
        } catch (Exception e) {
            log.error("Erro ao projetar SeguradoDesativadoEvent. ID: {}", event.getAggregateId(), e);
            throw new RuntimeException("Erro na projeção de SeguradoDesativadoEvent", e);
        }
    }
    
    /**
     * Projeta evento de reativação de segurado.
     * 
     * @param event Evento de reativação
     */
    @EventListener
    @Transactional("projectionsTransactionManager")
    @CacheEvict(value = {"segurados", "seguradoByCpf", "seguradoByEmail"}, key = "#event.aggregateId")
    public void on(SeguradoReativadoEvent event) {
        log.info("Projetando SeguradoReativadoEvent para ID: {}", event.getAggregateId());
        
        try {
            SeguradoQueryModel queryModel = queryRepository.findById(event.getAggregateId())
                    .orElseThrow(() -> new RuntimeException(
                            "Segurado não encontrado no modelo de leitura: " + event.getAggregateId()));
            
            // Atualizar status
            queryModel.setStatus(StatusSegurado.ATIVO);
            queryModel.setUpdatedAt(event.getTimestamp());
            queryModel.setVersion(event.getVersion());
            
            queryRepository.save(queryModel);
            
            log.info("Segurado reativado com sucesso no modelo de leitura. ID: {}", event.getAggregateId());
            
        } catch (Exception e) {
            log.error("Erro ao projetar SeguradoReativadoEvent. ID: {}", event.getAggregateId(), e);
            throw new RuntimeException("Erro na projeção de SeguradoReativadoEvent", e);
        }
    }
}
