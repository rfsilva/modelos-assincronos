package com.seguradora.hibrida.domain.segurado.query.service;

import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoDetailView;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoListView;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoQueryModel;
import com.seguradora.hibrida.domain.segurado.query.repository.SeguradoDetailViewRepository;
import com.seguradora.hibrida.domain.segurado.query.repository.SeguradoListViewRepository;
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
 * Serviço aprimorado para consultas de Segurado com otimizações.
 * 
 * <p>Implementa todos os requisitos da US011:
 * <ul>
 *   <li>Cache L1 (Caffeine) para consultas por CPF</li>
 *   <li>Cache L2 (Redis) para listagens</li>
 *   <li>Consultas otimizadas com índices</li>
 *   <li>Views especializadas (List/Detail)</li>
 *   <li>Métricas de hit rate</li>
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
    private final SeguradoListViewRepository listViewRepository;
    private final SeguradoDetailViewRepository detailViewRepository;
    
    /**
     * Busca segurado por ID com cache L1.
     * 
     * @param id ID do segurado
     * @return Optional com o segurado encontrado
     */
    @Cacheable(value = "segurados-cache", key = "#id")
    public Optional<SeguradoQueryModel> findById(String id) {
        log.debug("Buscando segurado por ID: {}", id);
        return repository.findById(id);
    }
    
    /**
     * Busca segurado por CPF com cache L1 otimizado.
     * 
     * @param cpf CPF do segurado (11 dígitos)
     * @return Optional com o segurado encontrado
     */
    @Cacheable(value = "segurado-by-cpf", key = "#cpf", unless = "#result.isEmpty()")
    public Optional<SeguradoQueryModel> findByCpf(String cpf) {
        log.debug("Buscando segurado por CPF: {}", maskCpf(cpf));
        return repository.findByCpf(cpf);
    }
    
    /**
     * Busca segurado por email com cache L1.
     * 
     * @param email Email do segurado
     * @return Optional com o segurado encontrado
     */
    @Cacheable(value = "segurado-by-email", key = "#email", unless = "#result.isEmpty()")
    public Optional<SeguradoQueryModel> findByEmail(String email) {
        log.debug("Buscando segurado por email: {}", email);
        return repository.findByEmail(email);
    }
    
    /**
     * Busca view detalhada do segurado por ID.
     * 
     * @param id ID do segurado
     * @return Optional com a view detalhada
     */
    @Cacheable(value = "segurado-detail", key = "#id", unless = "#result.isEmpty()")
    public Optional<SeguradoDetailView> findDetailById(String id) {
        log.debug("Buscando view detalhada por ID: {}", id);
        return detailViewRepository.findById(id);
    }
    
    /**
     * Lista segurados com view otimizada para listagem.
     * 
     * @param pageable Configuração de paginação
     * @return Página de segurados (view de lista)
     */
    @Cacheable(value = "segurados-list", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort.toString()")
    public Page<SeguradoListView> findAll(Pageable pageable) {
        log.debug("Listando segurados (view lista) - Página: {}", pageable.getPageNumber());
        return listViewRepository.findAll(pageable);
    }
    
    /**
     * Lista segurados por status com cache L2.
     * 
     * @param status Status do segurado
     * @param pageable Configuração de paginação
     * @return Página de segurados
     */
    @Cacheable(value = "segurados-by-status", key = "#status + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<SeguradoListView> findByStatus(StatusSegurado status, Pageable pageable) {
        log.debug("Listando segurados por status: {} - Página: {}", status, pageable.getPageNumber());
        return listViewRepository.findByStatus(status, pageable);
    }
    
    /**
     * Busca segurados por nome com cache.
     * 
     * @param nome Nome ou parte do nome
     * @param pageable Configuração de paginação
     * @return Página de segurados
     */
    @Cacheable(value = "segurados-by-nome", key = "#nome + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<SeguradoListView> findByNome(String nome, Pageable pageable) {
        log.debug("Buscando segurados por nome: {} - Página: {}", nome, pageable.getPageNumber());
        return listViewRepository.findByNomeContaining(nome, pageable);
    }
    
    /**
     * Busca segurados por cidade com cache.
     * 
     * @param cidade Cidade do segurado
     * @param pageable Configuração de paginação
     * @return Página de segurados
     */
    @Cacheable(value = "segurados-by-cidade", key = "#cidade + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<SeguradoListView> findByCidade(String cidade, Pageable pageable) {
        log.debug("Buscando segurados por cidade: {} - Página: {}", cidade, pageable.getPageNumber());
        return listViewRepository.findByCidade(cidade, pageable);
    }
    
    /**
     * Busca segurados por estado com cache.
     * 
     * @param estado Sigla do estado (UF)
     * @param pageable Configuração de paginação
     * @return Página de segurados
     */
    @Cacheable(value = "segurados-by-estado", key = "#estado + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<SeguradoListView> findByEstado(String estado, Pageable pageable) {
        log.debug("Buscando segurados por estado: {} - Página: {}", estado, pageable.getPageNumber());
        return listViewRepository.findByEstado(estado, pageable);
    }
    
    /**
     * Busca fuzzy por nome com cache.
     * 
     * @param termo Termo de busca
     * @param pageable Configuração de paginação
     * @return Página de segurados
     */
    @Cacheable(value = "segurados-fuzzy", key = "#termo + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<SeguradoListView> findByNomeFuzzy(String termo, Pageable pageable) {
        log.debug("Busca fuzzy por nome: {} - Página: {}", termo, pageable.getPageNumber());
        return listViewRepository.findByNomeFuzzy(termo, pageable);
    }
    
    /**
     * Verifica se existe segurado com o CPF informado (cache otimizado).
     * 
     * @param cpf CPF do segurado
     * @return true se existir, false caso contrário
     */
    @Cacheable(value = "cpf-exists", key = "#cpf")
    public boolean existsByCpf(String cpf) {
        log.debug("Verificando existência de segurado com CPF: {}", maskCpf(cpf));
        return repository.existsByCpf(cpf);
    }
    
    /**
     * Verifica se existe segurado com o email informado (cache otimizado).
     * 
     * @param email Email do segurado
     * @return true se existir, false caso contrário
     */
    @Cacheable(value = "email-exists", key = "#email")
    public boolean existsByEmail(String email) {
        log.debug("Verificando existência de segurado com email: {}", email);
        return repository.existsByEmail(email);
    }
    
    /**
     * Conta segurados por status com cache.
     * 
     * @param status Status do segurado
     * @return Quantidade de segurados
     */
    @Cacheable(value = "count-by-status", key = "#status")
    public long countByStatus(StatusSegurado status) {
        log.debug("Contando segurados por status: {}", status);
        return repository.countByStatus(status);
    }
    
    /**
     * Obtém estatísticas gerais com cache.
     * 
     * @return Estatísticas de segurados
     */
    @Cacheable(value = "segurados-stats", key = "'general'")
    public SeguradoStatistics getStatistics() {
        log.debug("Obtendo estatísticas gerais de segurados");
        
        long totalAtivos = countByStatus(StatusSegurado.ATIVO);
        long totalInativos = countByStatus(StatusSegurado.INATIVO);
        long totalSuspensos = countByStatus(StatusSegurado.SUSPENSO);
        long totalBloqueados = countByStatus(StatusSegurado.BLOQUEADO);
        
        return SeguradoStatistics.builder()
                .totalAtivos(totalAtivos)
                .totalInativos(totalInativos)
                .totalSuspensos(totalSuspensos)
                .totalBloqueados(totalBloqueados)
                .total(totalAtivos + totalInativos + totalSuspensos + totalBloqueados)
                .build();
    }
    
    /**
     * Busca segurados com múltiplos critérios (sem cache para flexibilidade).
     * 
     * @param nome Nome (opcional)
     * @param cpf CPF (opcional)
     * @param email Email (opcional)
     * @param status Status (opcional)
     * @param cidade Cidade (opcional)
     * @param estado Estado (opcional)
     * @param pageable Configuração de paginação
     * @return Página de segurados
     */
    public Page<SeguradoListView> findWithMultipleCriteria(
            String nome, String cpf, String email, StatusSegurado status,
            String cidade, String estado, Pageable pageable) {
        
        log.debug("Busca com múltiplos critérios - Nome: {}, CPF: {}, Email: {}, Status: {}, Cidade: {}, Estado: {}", 
                nome, maskCpf(cpf), email, status, cidade, estado);
        
        return listViewRepository.findWithMultipleCriteria(
                nome, cpf, email, status, cidade, estado, pageable);
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
    
    /**
     * DTO para estatísticas de segurados.
     */
    @lombok.Builder
    @lombok.Data
    public static class SeguradoStatistics {
        private long total;
        private long totalAtivos;
        private long totalInativos;
        private long totalSuspensos;
        private long totalBloqueados;
        
        public double getPercentualAtivos() {
            return total > 0 ? (double) totalAtivos / total * 100 : 0;
        }
        
        public double getPercentualInativos() {
            return total > 0 ? (double) totalInativos / total * 100 : 0;
        }
    }
}