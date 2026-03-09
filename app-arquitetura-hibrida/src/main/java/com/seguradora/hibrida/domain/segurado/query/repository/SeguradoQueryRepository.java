package com.seguradora.hibrida.domain.segurado.query.repository;

import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoQueryModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para consultas otimizadas de Segurado (CQRS - Query Side).
 * 
 * <p>Utiliza índices e consultas otimizadas para garantir performance
 * nas operações de leitura.</p>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Repository
public interface SeguradoQueryRepository extends JpaRepository<SeguradoQueryModel, String> {
    
    /**
     * Busca segurado por CPF.
     * 
     * @param cpf CPF do segurado (11 dígitos)
     * @return Optional com o segurado encontrado
     */
    Optional<SeguradoQueryModel> findByCpf(String cpf);
    
    /**
     * Busca segurado por email.
     * 
     * @param email Email do segurado
     * @return Optional com o segurado encontrado
     */
    Optional<SeguradoQueryModel> findByEmail(String email);
    
    /**
     * Busca segurados por status com paginação.
     * 
     * @param status Status do segurado
     * @param pageable Configuração de paginação
     * @return Página de segurados
     */
    Page<SeguradoQueryModel> findByStatus(StatusSegurado status, Pageable pageable);
    
    /**
     * Busca segurados por nome (busca parcial, case-insensitive).
     * 
     * @param nome Nome ou parte do nome do segurado
     * @param pageable Configuração de paginação
     * @return Página de segurados
     */
    @Query("SELECT s FROM SeguradoQueryModel s WHERE LOWER(s.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    Page<SeguradoQueryModel> findByNomeContaining(@Param("nome") String nome, Pageable pageable);
    
    /**
     * Busca segurados por CPF (busca parcial).
     * 
     * @param cpf CPF ou parte do CPF
     * @param pageable Configuração de paginação
     * @return Página de segurados
     */
    @Query("SELECT s FROM SeguradoQueryModel s WHERE s.cpf LIKE CONCAT('%', :cpf, '%')")
    Page<SeguradoQueryModel> findByCpfContaining(@Param("cpf") String cpf, Pageable pageable);
    
    /**
     * Busca segurados por cidade.
     * 
     * @param cidade Cidade do segurado
     * @param pageable Configuração de paginação
     * @return Página de segurados
     */
    Page<SeguradoQueryModel> findByCidade(String cidade, Pageable pageable);
    
    /**
     * Busca segurados por estado.
     * 
     * @param estado Sigla do estado (UF)
     * @param pageable Configuração de paginação
     * @return Página de segurados
     */
    Page<SeguradoQueryModel> findByEstado(String estado, Pageable pageable);
    
    /**
     * Verifica se existe segurado com o CPF informado.
     * 
     * @param cpf CPF do segurado
     * @return true se existir, false caso contrário
     */
    boolean existsByCpf(String cpf);
    
    /**
     * Verifica se existe segurado com o email informado.
     * 
     * @param email Email do segurado
     * @return true se existir, false caso contrário
     */
    boolean existsByEmail(String email);
    
    /**
     * Conta segurados por status.
     * 
     * @param status Status do segurado
     * @return Quantidade de segurados com o status
     */
    long countByStatus(StatusSegurado status);
}
