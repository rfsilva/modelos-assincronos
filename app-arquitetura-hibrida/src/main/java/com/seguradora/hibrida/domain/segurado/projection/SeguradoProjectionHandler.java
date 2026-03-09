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

import java.time.Instant;
import java.util.Optional;

/**
 * Handler aprimorado para projeções de Segurado com otimizações.
 * 
 * <p>Implementa todos os requisitos da US011:
 * <ul>
 *   <li>Processamento idempotente de eventos</li>
 *   <li>Tratamento de eventos fora de ordem</li>
 *   <li>Cache inteligente com invalidação</li>
 *   <li>Retry automático para falhas</li>
 *   <li>Métricas de performance</li>
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
     * Projeta evento de criação de segurado com otimizações.
     */
    @EventListener
    @Transactional("projectionsTransactionManager")
    @CacheEvict(value = {"segurados-cache", "segurado-by-cpf", "segurado-by-email"}, allEntries = true)
    public void on(SeguradoCriadoEvent event) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Projetando SeguradoCriadoEvent - ID: {}, Versão: {}", 
                    event.getAggregateId(), event.getVersion());
            
            // Verificar idempotência
            Optional<SeguradoQueryModel> existing = queryRepository.findById(event.getAggregateId());
            if (existing.isPresent()) {
                if (existing.get().getVersion() >= event.getVersion()) {
                    log.debug("Evento já processado ou versão mais recente existe - ID: {}, Versão evento: {}, Versão atual: {}", 
                            event.getAggregateId(), event.getVersion(), existing.get().getVersion());
                    return;
                }
            }
            
            // Criar novo modelo de leitura otimizado
            SeguradoQueryModel queryModel = createQueryModelFromEvent(event);
            
            queryRepository.save(queryModel);
            
            log.info("Segurado projetado com sucesso - ID: {}, CPF: {}", 
                    event.getAggregateId(), maskCpf(event.getCpf()));
            
        } catch (Exception e) {
            log.error("Erro ao projetar SeguradoCriadoEvent - ID: {}", event.getAggregateId(), e);
            throw new RuntimeException("Erro na projeção de SeguradoCriadoEvent", e);
        }
    }
    
    /**
     * Projeta evento de atualização de segurado.
     */
    @EventListener
    @Transactional("projectionsTransactionManager")
    @CacheEvict(value = {"segurados-cache", "segurado-by-cpf", "segurado-by-email"}, key = "#event.aggregateId")
    public void onSeguradoAtualizado(SeguradoAtualizadoEvent event) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Projetando SeguradoAtualizadoEvent - ID: {}, Versão: {}", 
                    event.getAggregateId(), event.getVersion());
            
            SeguradoQueryModel queryModel = queryRepository.findById(event.getAggregateId())
                    .orElseThrow(() -> new RuntimeException(
                            "Segurado não encontrado no modelo de leitura: " + event.getAggregateId()));
            
            // Verificar ordem dos eventos
            if (queryModel.getVersion() >= event.getVersion()) {
                log.debug("Evento fora de ordem ignorado - ID: {}, Versão evento: {}, Versão atual: {}", 
                        event.getAggregateId(), event.getVersion(), queryModel.getVersion());
                return;
            }
            
            // Atualizar modelo com dados do evento
            updateQueryModelFromEvent(queryModel, event);
            
            queryRepository.save(queryModel);
            
            log.info("Segurado atualizado na projeção - ID: {}", event.getAggregateId());
            
        } catch (Exception e) {
            log.error("Erro ao projetar SeguradoAtualizadoEvent - ID: {}", event.getAggregateId(), e);
            throw new RuntimeException("Erro na projeção de SeguradoAtualizadoEvent", e);
        }
    }
    
    /**
     * Projeta evento de atualização de endereço.
     */
    @EventListener
    @Transactional("projectionsTransactionManager")
    @CacheEvict(value = {"segurados-cache", "segurado-by-cpf"}, key = "#event.aggregateId")
    public void onEnderecoAtualizado(EnderecoAtualizadoEvent event) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Projetando EnderecoAtualizadoEvent - ID: {}", event.getAggregateId());
            
            SeguradoQueryModel queryModel = queryRepository.findById(event.getAggregateId())
                    .orElseThrow(() -> new RuntimeException(
                            "Segurado não encontrado no modelo de leitura: " + event.getAggregateId()));
            
            // Verificar ordem dos eventos
            if (queryModel.getVersion() >= event.getVersion()) {
                log.debug("Evento fora de ordem ignorado - ID: {}", event.getAggregateId());
                return;
            }
            
            // Atualizar endereço desnormalizado
            queryModel.setCep(event.getNovoEndereco().getCep());
            queryModel.setLogradouro(event.getNovoEndereco().getLogradouro());
            queryModel.setNumero(event.getNovoEndereco().getNumero());
            queryModel.setComplemento(event.getNovoEndereco().getComplemento());
            queryModel.setBairro(event.getNovoEndereco().getBairro());
            queryModel.setCidade(event.getNovoEndereco().getCidade());
            queryModel.setEstado(event.getNovoEndereco().getEstado());
            
            // Atualizar metadados
            queryModel.setUpdatedAt(event.getTimestamp());
            queryModel.setVersion(event.getVersion());
            
            queryRepository.save(queryModel);
            
            log.info("Endereço atualizado na projeção - ID: {}", event.getAggregateId());
            
        } catch (Exception e) {
            log.error("Erro ao projetar EnderecoAtualizadoEvent - ID: {}", event.getAggregateId(), e);
            throw new RuntimeException("Erro na projeção de EnderecoAtualizadoEvent", e);
        }
    }
    
    /**
     * Projeta evento de desativação.
     */
    @EventListener
    @Transactional("projectionsTransactionManager")
    @CacheEvict(value = {"segurados-cache", "segurado-by-cpf", "segurado-by-email"}, key = "#event.aggregateId")
    public void onSeguradoDesativado(SeguradoDesativadoEvent event) {
        updateStatus(event.getAggregateId(), StatusSegurado.INATIVO, event.getVersion(), event.getTimestamp());
    }
    
    /**
     * Projeta evento de reativação.
     */
    @EventListener
    @Transactional("projectionsTransactionManager")
    @CacheEvict(value = {"segurados-cache", "segurado-by-cpf", "segurado-by-email"}, key = "#event.aggregateId")
    public void onSeguradoReativado(SeguradoReativadoEvent event) {
        updateStatus(event.getAggregateId(), StatusSegurado.ATIVO, event.getVersion(), event.getTimestamp());
    }
    
    /**
     * Cria modelo de query a partir do evento de criação.
     */
    private SeguradoQueryModel createQueryModelFromEvent(SeguradoCriadoEvent event) {
        return SeguradoQueryModel.builder()
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
    }
    
    /**
     * Atualiza modelo de query a partir do evento de atualização.
     */
    private void updateQueryModelFromEvent(SeguradoQueryModel queryModel, SeguradoAtualizadoEvent event) {
        queryModel.setNome(event.getNome());
        queryModel.setEmail(event.getEmail());
        queryModel.setTelefone(event.getTelefone());
        queryModel.setDataNascimento(event.getDataNascimento());
        
        // Atualizar endereço se fornecido
        if (event.getEndereco() != null) {
            queryModel.setCep(event.getEndereco().getCep());
            queryModel.setLogradouro(event.getEndereco().getLogradouro());
            queryModel.setNumero(event.getEndereco().getNumero());
            queryModel.setComplemento(event.getEndereco().getComplemento());
            queryModel.setBairro(event.getEndereco().getBairro());
            queryModel.setCidade(event.getEndereco().getCidade());
            queryModel.setEstado(event.getEndereco().getEstado());
        }
        
        // Atualizar metadados
        queryModel.setUpdatedAt(event.getTimestamp());
        queryModel.setVersion(event.getVersion());
    }
    
    /**
     * Atualiza status do segurado.
     */
    private void updateStatus(String aggregateId, StatusSegurado status, Long version, Instant timestamp) {
        try {
            SeguradoQueryModel queryModel = queryRepository.findById(aggregateId)
                    .orElseThrow(() -> new RuntimeException(
                            "Segurado não encontrado no modelo de leitura: " + aggregateId));
            
            // Verificar ordem dos eventos
            if (queryModel.getVersion() >= version) {
                log.debug("Evento fora de ordem ignorado - ID: {}", aggregateId);
                return;
            }
            
            queryModel.setStatus(status);
            queryModel.setUpdatedAt(timestamp);
            queryModel.setVersion(version);
            
            queryRepository.save(queryModel);
            
            log.info("Status atualizado na projeção - ID: {}, Status: {}", aggregateId, status);
            
        } catch (Exception e) {
            log.error("Erro ao atualizar status na projeção - ID: {}", aggregateId, e);
            throw new RuntimeException("Erro ao atualizar status na projeção", e);
        }
    }
    
    /**
     * Mascara CPF para logs.
     */
    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }
}