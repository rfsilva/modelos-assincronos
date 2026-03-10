package com.seguradora.hibrida.domain.veiculo.query.repository;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import com.seguradora.hibrida.domain.veiculo.query.model.VeiculoQueryModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementação customizada do repositório de veículos.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Repository
public class VeiculoQueryRepositoryImpl {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Busca veículos por nome do proprietário (busca parcial).
     */
    public Page<VeiculoQueryModel> findByProprietarioNomeContainingIgnoreCase(String nome, Pageable pageable) {
        String jpql = "SELECT v FROM VeiculoQueryModel v WHERE LOWER(v.proprietarioNome) LIKE LOWER(CONCAT('%', :nome, '%'))";
        
        TypedQuery<VeiculoQueryModel> query = entityManager.createQuery(jpql, VeiculoQueryModel.class);
        query.setParameter("nome", nome);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        
        List<VeiculoQueryModel> results = query.getResultList();
        
        // Count query
        String countJpql = "SELECT COUNT(v) FROM VeiculoQueryModel v WHERE LOWER(v.proprietarioNome) LIKE LOWER(CONCAT('%', :nome, '%'))";
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        countQuery.setParameter("nome", nome);
        Long total = countQuery.getSingleResult();
        
        return new PageImpl<>(results, pageable, total);
    }
    
    /**
     * Busca veículos por cidade (busca parcial).
     */
    public Page<VeiculoQueryModel> findByCidadeContainingIgnoreCase(String cidade, Pageable pageable) {
        String jpql = "SELECT v FROM VeiculoQueryModel v WHERE LOWER(v.cidade) LIKE LOWER(CONCAT('%', :cidade, '%'))";
        
        TypedQuery<VeiculoQueryModel> query = entityManager.createQuery(jpql, VeiculoQueryModel.class);
        query.setParameter("cidade", cidade);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        
        List<VeiculoQueryModel> results = query.getResultList();
        
        // Count query
        String countJpql = "SELECT COUNT(v) FROM VeiculoQueryModel v WHERE LOWER(v.cidade) LIKE LOWER(CONCAT('%', :cidade, '%'))";
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        countQuery.setParameter("cidade", cidade);
        Long total = countQuery.getSingleResult();
        
        return new PageImpl<>(results, pageable, total);
    }
    
    /**
     * Conta veículos por status.
     */
    public long countByStatus(StatusVeiculo status) {
        String jpql = "SELECT COUNT(v) FROM VeiculoQueryModel v WHERE v.status = :status";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("status", status);
        return query.getSingleResult();
    }
    
    /**
     * Conta veículos com apólice ativa.
     */
    public long countByApoliceAtivaTrue() {
        String jpql = "SELECT COUNT(v) FROM VeiculoQueryModel v WHERE v.apoliceAtiva = true";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        return query.getSingleResult();
    }
    
    /**
     * Estatísticas por estado.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> countByEstado() {
        String jpql = "SELECT v.estado, COUNT(v) FROM VeiculoQueryModel v GROUP BY v.estado ORDER BY COUNT(v) DESC";
        return entityManager.createQuery(jpql).getResultList();
    }
    
    /**
     * Estatísticas por marca.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> countByMarca() {
        String jpql = "SELECT v.marca, COUNT(v) FROM VeiculoQueryModel v GROUP BY v.marca ORDER BY COUNT(v) DESC";
        return entityManager.createQuery(jpql).getResultList();
    }
    
    /**
     * Estatísticas por categoria.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> countByCategoria() {
        String jpql = "SELECT v.categoria, COUNT(v) FROM VeiculoQueryModel v GROUP BY v.categoria ORDER BY COUNT(v) DESC";
        return entityManager.createQuery(jpql).getResultList();
    }
    
    /**
     * Estatísticas por combustível.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> countByTipoCombustivel() {
        String jpql = "SELECT v.tipoCombustivel, COUNT(v) FROM VeiculoQueryModel v GROUP BY v.tipoCombustivel ORDER BY COUNT(v) DESC";
        return entityManager.createQuery(jpql).getResultList();
    }
}