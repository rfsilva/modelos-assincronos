package com.seguradora.hibrida.domain.segurado.query.service;

import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoQueryModel;
import com.seguradora.hibrida.domain.segurado.query.repository.SeguradoQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service para consultas de Segurado (CQRS - Query Side).
 * 
 * <p>Implementa operações de leitura com cache Redis para otimização de performance.</p>
 * 
 * <p>Responsabilidades:
 * <ul>
 *   <li>Buscar segurados por diversos critérios</li>
 *   <li>Implementar cache para consultas frequentes</li>
 *   <li>Fornecer paginação para listagens</li>
 *   <li>Garantir performance nas consultas</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(value = "projectionsTransactionManager", readOnly = true)
public class SeguradoQueryService {
    
    private final SeguradoQueryRepository repository;
    
    /**
     * Busca segurado por ID com cache.
     * 
     * @param id ID do segurado
     * @return Optional com o segurado encontrado
     */
    @Cacheable(value = "segurados", key = "#id")
    public Optional<SeguradoQueryModel> findById(String id) {
        log.debug("Buscando segurado por ID: {}", id);
        return repository.findById(id);
    }
    
    /**
     * Busca segurado por CPF com cache.
     * 
     * @param cpf CPF do segurado (11 dígitos)
     * @return Optional com o segurado encontrado
     */
    @Cacheable(value = "seguradoByCpf", key = "#cpf")
    public Optional<SeguradoQueryModel> findByCpf(String cpf) {
        log.debug("Buscando segurado por CPF: {}", cpf);
        return repository.findByCpf(cpf);
    }
    
    /**
     * Busca segurado por email com cache.
     * 
     * @param email Email do segurado
     * @return Optional com o segurado encontrado
     */
    @Cacheable(value = "seguradoByEmail", key = "#email")
    public Optional<SeguradoQueryModel> findByEmail(String email) {
        log.debug("Buscando segurado por email: {}", email);
        return repository.findByEmail(email);
    }
    
    /**
     * Lista segurados por status com paginação.
     * 
     * @param status Status do segurado
     * @param pageable Configuração de paginação
     * @return Página de segurados
     */
    public Page<SeguradoQueryModel> findByStatus(StatusSegurado status, Pageable pageable) {
        log.debug("Listando segurados por status: {} - Página: {}", status, pageable.getPageNumber());
        return repository.findByStatus(status, pageable);
    }
    
    /**
     * Busca segurados por nome (busca parcial).
     * 
     * @param nome Nome ou parte do nome
     * @param pageable Configuração de paginação
     * @return Página de segurados
     */
    public Page<SeguradoQueryModel> findByNome(String nome, Pageable pageable) {
        log.debug("Buscando segurados por nome: {} - Página: {}", nome, pageable.getPageNumber());
        return repository.findByNomeContaining(nome, pageable);
    }
    
    /**
     * Busca segurados por CPF (busca parcial).
     * 
     * @param cpf CPF ou parte do CPF
     * @param pageable Configuração de paginação
     * @return Página de segurados
     */
    public Page<SeguradoQueryModel> findByCpfContaining(String cpf, Pageable pageable) {
        log.debug("Buscando segurados por CPF parcial: {} - Página: {}", cpf, pageable.getPageNumber());
        return repository.findByCpfContaining(cpf, pageable);
    }
    
    /**
     * Lista todos os segurados com paginação.
     * 
     * @param pageable Configuração de paginação
     * @return Página de segurados
     */
    public Page<SeguradoQueryModel> findAll(Pageable pageable) {
        log.debug("Listando todos os segurados - Página: {}", pageable.getPageNumber());
        return repository.findAll(pageable);
    }
    
    /**
     * Busca segurados por cidade.
     * 
     * @param cidade Cidade do segurado
     * @param pageable Configuração de paginação
     * @return Página de segurados
     */
    public Page<SeguradoQueryModel> findByCidade(String cidade, Pageable pageable) {
        log.debug("Buscando segurados por cidade: {} - Página: {}", cidade, pageable.getPageNumber());
        return repository.findByCidade(cidade, pageable);
    }
    
    /**
     * Busca segurados por estado.
     * 
     * @param estado Sigla do estado (UF)
     * @param pageable Configuração de paginação
     * @return Página de segurados
     */
    public Page<SeguradoQueryModel> findByEstado(String estado, Pageable pageable) {
        log.debug("Buscando segurados por estado: {} - Página: {}", estado, pageable.getPageNumber());
        return repository.findByEstado(estado, pageable);
    }
    
    /**
     * Verifica se existe segurado com o CPF informado.
     * 
     * @param cpf CPF do segurado
     * @return true se existir, false caso contrário
     */
    public boolean existsByCpf(String cpf) {
        log.debug("Verificando existência de segurado com CPF: {}", cpf);
        return repository.existsByCpf(cpf);
    }
    
    /**
     * Verifica se existe segurado com o email informado.
     * 
     * @param email Email do segurado
     * @return true se existir, false caso contrário
     */
    public boolean existsByEmail(String email) {
        log.debug("Verificando existência de segurado com email: {}", email);
        return repository.existsByEmail(email);
    }
    
    /**
     * Conta segurados por status.
     * 
     * @param status Status do segurado
     * @return Quantidade de segurados
     */
    @Cacheable(value = "seguradosCountByStatus", key = "#status")
    public long countByStatus(StatusSegurado status) {
        log.debug("Contando segurados por status: {}", status);
        return repository.countByStatus(status);
    }
}
