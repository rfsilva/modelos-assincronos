package com.seguradora.hibrida.domain.apolice.query.service;

import com.seguradora.hibrida.domain.apolice.model.StatusApolice;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceDetailView;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceListView;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceVencimentoView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interface para serviços de consulta de apólices.
 * 
 * <p>Define operações de consulta otimizadas para diferentes
 * cenários de negócio relacionados a apólices.</p>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface ApoliceQueryService {
    
    // === CONSULTAS BÁSICAS ===
    
    /**
     * Busca apólice por ID.
     */
    Optional<ApoliceDetailView> buscarPorId(String id);
    
    /**
     * Busca apólice por número.
     */
    Optional<ApoliceDetailView> buscarPorNumero(String numero);
    
    /**
     * Lista todas as apólices com paginação.
     */
    Page<ApoliceListView> listarTodas(Pageable pageable);
    
    // === CONSULTAS POR SEGURADO ===
    
    /**
     * Busca apólices por CPF do segurado.
     */
    List<ApoliceListView> buscarPorCpfSegurado(String cpf);
    
    /**
     * Busca apólices ativas por CPF do segurado.
     */
    List<ApoliceListView> buscarAtivasPorCpfSegurado(String cpf);
    
    /**
     * Busca apólices por nome do segurado.
     */
    Page<ApoliceListView> buscarPorNomeSegurado(String nome, Pageable pageable);
    
    // === CONSULTAS POR STATUS ===
    
    /**
     * Busca apólices por status.
     */
    Page<ApoliceListView> buscarPorStatus(StatusApolice status, Pageable pageable);
    
    /**
     * Busca apólices ativas.
     */
    Page<ApoliceListView> buscarAtivas(Pageable pageable);
    
    /**
     * Busca apólices vencidas.
     */
    Page<ApoliceListView> buscarVencidas(Pageable pageable);
    
    // === CONSULTAS POR VENCIMENTO ===
    
    /**
     * Busca apólices que vencem em um período.
     */
    List<ApoliceVencimentoView> buscarVencendoEntre(LocalDate inicio, LocalDate fim);
    
    /**
     * Busca apólices que vencem nos próximos dias.
     */
    List<ApoliceVencimentoView> buscarVencendoEm(int dias);
    
    /**
     * Busca apólices com vencimento próximo.
     */
    List<ApoliceVencimentoView> buscarComVencimentoProximo();
    
    // === CONSULTAS POR PRODUTO ===
    
    /**
     * Busca apólices por produto.
     */
    Page<ApoliceListView> buscarPorProduto(String produto, Pageable pageable);
    
    // === CONSULTAS POR COBERTURA ===
    
    /**
     * Busca apólices por tipo de cobertura.
     */
    List<ApoliceListView> buscarPorCobertura(TipoCobertura cobertura);
    
    /**
     * Busca apólices com cobertura total.
     */
    List<ApoliceListView> buscarComCoberturaTotal();
    
    // === CONSULTAS POR VALOR ===
    
    /**
     * Busca apólices por faixa de valor.
     */
    Page<ApoliceListView> buscarPorFaixaValor(BigDecimal valorMin, BigDecimal valorMax, Pageable pageable);
    
    /**
     * Busca apólices de alto valor.
     */
    List<ApoliceListView> buscarAltoValor(BigDecimal valorMinimo);
    
    // === CONSULTAS DE RENOVAÇÃO ===
    
    /**
     * Busca apólices elegíveis para renovação automática.
     */
    List<ApoliceVencimentoView> buscarElegiveisRenovacaoAutomatica();
    
    /**
     * Busca apólices que precisam de atenção para renovação.
     */
    List<ApoliceVencimentoView> buscarPrecisandoAtencaoRenovacao();
    
    /**
     * Busca apólices por score de renovação.
     */
    List<ApoliceListView> buscarPorScoreRenovacao(int scoreMin, int scoreMax);
    
    // === CONSULTAS POR LOCALIZAÇÃO ===
    
    /**
     * Busca apólices por cidade do segurado.
     */
    Page<ApoliceListView> buscarPorCidade(String cidade, Pageable pageable);
    
    /**
     * Busca apólices por estado do segurado.
     */
    Page<ApoliceListView> buscarPorEstado(String estado, Pageable pageable);
    
    // === CONSULTAS CUSTOMIZADAS ===
    
    /**
     * Busca com múltiplos filtros.
     */
    Page<ApoliceListView> buscarComFiltros(
        StatusApolice status,
        String produto,
        String seguradoCpf,
        LocalDate vigenciaInicio,
        LocalDate vigenciaFim,
        BigDecimal valorMin,
        BigDecimal valorMax,
        Pageable pageable
    );
    
    // === VERIFICAÇÕES ===
    
    /**
     * Verifica se existe apólice com o número.
     */
    boolean existeComNumero(String numero);
    
    /**
     * Conta apólices ativas por CPF do segurado.
     */
    long contarAtivasPorCpf(String cpf);
    
    /**
     * Verifica se segurado possui apólices ativas.
     */
    boolean seguradoPossuiApolicesAtivas(String cpf);
}