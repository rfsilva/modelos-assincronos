package com.seguradora.hibrida.domain.veiculo.query.service;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import com.seguradora.hibrida.domain.veiculo.query.dto.VeiculoDetailView;
import com.seguradora.hibrida.domain.veiculo.query.dto.VeiculoListView;
import com.seguradora.hibrida.domain.veiculo.query.model.VeiculoQueryModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Interface para serviços de consulta de veículos.
 * 
 * <p>Define operações de leitura otimizadas para o domínio de veículos,
 * incluindo consultas básicas, busca geográfica, filtros avançados e
 * estatísticas.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface VeiculoQueryService {
    
    // === CONSULTAS BÁSICAS ===
    
    /**
     * Busca veículo por ID.
     */
    Optional<VeiculoDetailView> buscarPorId(String id);
    
    /**
     * Busca veículo por placa.
     */
    Optional<VeiculoDetailView> buscarPorPlaca(String placa);
    
    /**
     * Busca veículo por RENAVAM.
     */
    Optional<VeiculoDetailView> buscarPorRenavam(String renavam);
    
    /**
     * Busca veículo por chassi.
     */
    Optional<VeiculoDetailView> buscarPorChassi(String chassi);
    
    /**
     * Lista todos os veículos com paginação.
     */
    Page<VeiculoListView> listarTodos(Pageable pageable);
    
    // === CONSULTAS POR PROPRIETÁRIO ===
    
    /**
     * Busca veículos por CPF do proprietário.
     */
    List<VeiculoListView> buscarPorProprietarioCpf(String cpf);
    
    /**
     * Busca veículos por nome do proprietário.
     */
    Page<VeiculoListView> buscarPorProprietarioNome(String nome, Pageable pageable);
    
    // === CONSULTAS POR STATUS ===
    
    /**
     * Busca veículos por status.
     */
    Page<VeiculoListView> buscarPorStatus(StatusVeiculo status, Pageable pageable);
    
    /**
     * Busca veículos ativos.
     */
    Page<VeiculoListView> buscarAtivos(Pageable pageable);
    
    /**
     * Busca veículos com apólice ativa.
     */
    Page<VeiculoListView> buscarComApoliceAtiva(Pageable pageable);
    
    /**
     * Busca veículos sem apólice ativa.
     */
    Page<VeiculoListView> buscarSemApoliceAtiva(Pageable pageable);
    
    // === CONSULTAS POR MARCA/MODELO ===
    
    /**
     * Busca veículos por marca e modelo.
     */
    Page<VeiculoListView> buscarPorMarcaEModelo(String marca, String modelo, Pageable pageable);
    
    /**
     * Busca veículos por marca.
     */
    Page<VeiculoListView> buscarPorMarca(String marca, Pageable pageable);
    
    /**
     * Busca fuzzy por marca ou modelo.
     */
    Page<VeiculoListView> buscarPorMarcaOuModelo(String termo, Pageable pageable);
    
    // === CONSULTAS POR ANO ===
    
    /**
     * Busca veículos por faixa de ano de fabricação.
     */
    Page<VeiculoListView> buscarPorFaixaAno(Integer anoInicio, Integer anoFim, Pageable pageable);
    
    /**
     * Busca veículos por ano específico.
     */
    Page<VeiculoListView> buscarPorAno(Integer ano, Pageable pageable);
    
    // === CONSULTAS GEOGRÁFICAS ===
    
    /**
     * Busca veículos por cidade.
     */
    Page<VeiculoListView> buscarPorCidade(String cidade, Pageable pageable);
    
    /**
     * Busca veículos por estado.
     */
    Page<VeiculoListView> buscarPorEstado(String estado, Pageable pageable);
    
    /**
     * Busca veículos por cidade e estado.
     */
    Page<VeiculoListView> buscarPorCidadeEEstado(String cidade, String estado, Pageable pageable);
    
    /**
     * Busca veículos por região.
     */
    Page<VeiculoListView> buscarPorRegiao(String regiao, Pageable pageable);
    
    // === CONSULTAS CUSTOMIZADAS ===
    
    /**
     * Busca com múltiplos critérios.
     */
    Page<VeiculoListView> buscarComFiltros(
        String marca, String modelo, StatusVeiculo status,
        Integer anoInicio, Integer anoFim, String estado,
        Pageable pageable
    );
    
    // === VERIFICAÇÕES ===
    
    /**
     * Verifica se existe veículo com a placa.
     */
    boolean existeComPlaca(String placa);
    
    /**
     * Verifica se existe veículo com o RENAVAM.
     */
    boolean existeComRenavam(String renavam);
    
    /**
     * Verifica se existe veículo com o chassi.
     */
    boolean existeComChassi(String chassi);
    
    /**
     * Conta veículos por proprietário e status.
     */
    long contarPorProprietarioEStatus(String cpf, StatusVeiculo status);
    
    // === ESTATÍSTICAS ===
    
    /**
     * Obtém estatísticas gerais de veículos.
     */
    VeiculoStatistics obterEstatisticas();
    
    /**
     * Obtém estatísticas por estado.
     */
    List<Object[]> obterEstatisticasPorEstado();
    
    /**
     * Obtém estatísticas por marca.
     */
    List<Object[]> obterEstatisticasPorMarca();
    
    /**
     * Classe para estatísticas de veículos.
     */
    record VeiculoStatistics(
        long totalVeiculos,
        long veiculosAtivos,
        long veiculosComApolice,
        long veiculosSemApolice,
        double percentualComApolice,
        String marcaMaisComum,
        String estadoMaisComum
    ) {}
}