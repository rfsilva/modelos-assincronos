package com.seguradora.hibrida.domain.veiculo.query.service;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import com.seguradora.hibrida.domain.veiculo.query.dto.VeiculoDetailView;
import com.seguradora.hibrida.domain.veiculo.query.dto.VeiculoListView;
import com.seguradora.hibrida.domain.veiculo.query.model.VeiculoQueryModel;
import com.seguradora.hibrida.domain.veiculo.query.repository.VeiculoQueryRepository;
import com.seguradora.hibrida.domain.veiculo.query.repository.VeiculoQueryRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do serviço de consulta de veículos.
 * 
 * <p>Fornece operações de leitura otimizadas para o domínio de veículos,
 * com cache inteligente e consultas de alta performance.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class VeiculoQueryServiceImpl implements VeiculoQueryService {
    
    private static final Logger log = LoggerFactory.getLogger(VeiculoQueryServiceImpl.class);
    
    private final VeiculoQueryRepository veiculoRepository;
    private final VeiculoQueryRepositoryImpl veiculoRepositoryImpl;
    
    public VeiculoQueryServiceImpl(VeiculoQueryRepository veiculoRepository,
                                   VeiculoQueryRepositoryImpl veiculoRepositoryImpl) {
        this.veiculoRepository = veiculoRepository;
        this.veiculoRepositoryImpl = veiculoRepositoryImpl;
    }
    
    // === CONSULTAS BÁSICAS ===
    
    @Override
    @Cacheable(value = "veiculo-detail", key = "#id")
    public Optional<VeiculoDetailView> buscarPorId(String id) {
        log.debug("Buscando veículo por ID: {}", id);
        
        return veiculoRepository.findById(id)
            .map(this::toDetailView);
    }
    
    @Override
    @Cacheable(value = "veiculo-placa", key = "#placa")
    public Optional<VeiculoDetailView> buscarPorPlaca(String placa) {
        log.debug("Buscando veículo por placa: {}", placa);
        
        return veiculoRepository.findByPlaca(placa)
            .map(this::toDetailView);
    }
    
    @Override
    @Cacheable(value = "veiculo-renavam", key = "#renavam")
    public Optional<VeiculoDetailView> buscarPorRenavam(String renavam) {
        log.debug("Buscando veículo por RENAVAM: {}", renavam);
        
        return veiculoRepository.findByRenavam(renavam)
            .map(this::toDetailView);
    }
    
    @Override
    @Cacheable(value = "veiculo-chassi", key = "#chassi")
    public Optional<VeiculoDetailView> buscarPorChassi(String chassi) {
        log.debug("Buscando veículo por chassi: {}", chassi);
        
        return veiculoRepository.findByChassi(chassi)
            .map(this::toDetailView);
    }
    
    @Override
    public Page<VeiculoListView> listarTodos(Pageable pageable) {
        log.debug("Listando todos os veículos - página: {}, tamanho: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        return veiculoRepository.findAll(pageable)
            .map(this::toListView);
    }
    
    // === CONSULTAS POR PROPRIETÁRIO ===
    
    @Override
    @Cacheable(value = "veiculo-proprietario", key = "#cpf")
    public List<VeiculoListView> buscarPorProprietarioCpf(String cpf) {
        log.debug("Buscando veículos por CPF do proprietário: {}", cpf);
        
        return veiculoRepository.findByProprietarioCpf(cpf).stream()
            .map(this::toListView)
            .toList();
    }
    
    @Override
    public Page<VeiculoListView> buscarPorProprietarioNome(String nome, Pageable pageable) {
        log.debug("Buscando veículos por nome do proprietário: {}", nome);
        
        return veiculoRepositoryImpl.findByProprietarioNomeContainingIgnoreCase(nome, pageable)
            .map(this::toListView);
    }
    
    // === CONSULTAS POR STATUS ===
    
    @Override
    public Page<VeiculoListView> buscarPorStatus(StatusVeiculo status, Pageable pageable) {
        log.debug("Buscando veículos por status: {}", status);
        
        return veiculoRepository.findByStatus(status, pageable)
            .map(this::toListView);
    }
    
    @Override
    public Page<VeiculoListView> buscarAtivos(Pageable pageable) {
        return buscarPorStatus(StatusVeiculo.ATIVO, pageable);
    }
    
    @Override
    public Page<VeiculoListView> buscarComApoliceAtiva(Pageable pageable) {
        log.debug("Buscando veículos com apólice ativa");
        
        return veiculoRepository.findByApoliceAtivaTrue(pageable)
            .map(this::toListView);
    }
    
    @Override
    public Page<VeiculoListView> buscarSemApoliceAtiva(Pageable pageable) {
        log.debug("Buscando veículos sem apólice ativa");
        
        return veiculoRepository.findByApoliceAtivaFalse(pageable)
            .map(this::toListView);
    }
    
    // === CONSULTAS POR MARCA/MODELO ===
    
    @Override
    public Page<VeiculoListView> buscarPorMarcaEModelo(String marca, String modelo, Pageable pageable) {
        log.debug("Buscando veículos por marca: {} e modelo: {}", marca, modelo);
        
        return veiculoRepository.findByMarcaAndModelo(marca, modelo, pageable)
            .map(this::toListView);
    }
    
    @Override
    public Page<VeiculoListView> buscarPorMarca(String marca, Pageable pageable) {
        log.debug("Buscando veículos por marca: {}", marca);
        
        return veiculoRepository.findByMarcaContainingIgnoreCase(marca, pageable)
            .map(this::toListView);
    }
    
    @Override
    public Page<VeiculoListView> buscarPorMarcaOuModelo(String termo, Pageable pageable) {
        log.debug("Buscando veículos por termo: {}", termo);
        
        return veiculoRepository.findByMarcaOrModeloFuzzy(termo, pageable)
            .map(this::toListView);
    }
    
    // === CONSULTAS POR ANO ===
    
    @Override
    public Page<VeiculoListView> buscarPorFaixaAno(Integer anoInicio, Integer anoFim, Pageable pageable) {
        log.debug("Buscando veículos por faixa de ano: {} a {}", anoInicio, anoFim);
        
        return veiculoRepository.findByAnoFabricacaoBetween(anoInicio, anoFim, pageable)
            .map(this::toListView);
    }
    
    @Override
    public Page<VeiculoListView> buscarPorAno(Integer ano, Pageable pageable) {
        return buscarPorFaixaAno(ano, ano, pageable);
    }
    
    // === CONSULTAS GEOGRÁFICAS ===
    
    @Override
    public Page<VeiculoListView> buscarPorCidade(String cidade, Pageable pageable) {
        log.debug("Buscando veículos por cidade: {}", cidade);
        
        return veiculoRepositoryImpl.findByCidadeContainingIgnoreCase(cidade, pageable)
            .map(this::toListView);
    }
    
    @Override
    public Page<VeiculoListView> buscarPorEstado(String estado, Pageable pageable) {
        log.debug("Buscando veículos por estado: {}", estado);
        
        return veiculoRepository.findByEstado(estado, pageable)
            .map(this::toListView);
    }
    
    @Override
    public Page<VeiculoListView> buscarPorCidadeEEstado(String cidade, String estado, Pageable pageable) {
        log.debug("Buscando veículos por cidade: {} e estado: {}", cidade, estado);
        
        return veiculoRepository.findByCidadeAndEstado(cidade, estado, pageable)
            .map(this::toListView);
    }
    
    @Override
    public Page<VeiculoListView> buscarPorRegiao(String regiao, Pageable pageable) {
        log.debug("Buscando veículos por região: {}", regiao);
        
        return veiculoRepository.findByRegiao(regiao, pageable)
            .map(this::toListView);
    }
    
    // === CONSULTAS CUSTOMIZADAS ===
    
    @Override
    public Page<VeiculoListView> buscarComFiltros(String marca, String modelo, StatusVeiculo status,
                                                 Integer anoInicio, Integer anoFim, String estado,
                                                 Pageable pageable) {
        log.debug("Buscando veículos com filtros - marca: {}, modelo: {}, status: {}, anos: {}-{}, estado: {}",
                marca, modelo, status, anoInicio, anoFim, estado);
        
        return veiculoRepository.findByMultiplosCriterios(marca, modelo, status, anoInicio, anoFim, estado, pageable)
            .map(this::toListView);
    }
    
    // === VERIFICAÇÕES ===
    
    @Override
    @Cacheable(value = "veiculo-exists-placa", key = "#placa")
    public boolean existeComPlaca(String placa) {
        return veiculoRepository.existsByPlaca(placa);
    }
    
    @Override
    @Cacheable(value = "veiculo-exists-renavam", key = "#renavam")
    public boolean existeComRenavam(String renavam) {
        return veiculoRepository.existsByRenavam(renavam);
    }
    
    @Override
    @Cacheable(value = "veiculo-exists-chassi", key = "#chassi")
    public boolean existeComChassi(String chassi) {
        return veiculoRepository.existsByChassi(chassi);
    }
    
    @Override
    public long contarPorProprietarioEStatus(String cpf, StatusVeiculo status) {
        return veiculoRepository.countByProprietarioCpfAndStatus(cpf, status);
    }
    
    // === ESTATÍSTICAS ===
    
    @Override
    @Cacheable(value = "veiculo-statistics", key = "'global'")
    public VeiculoStatistics obterEstatisticas() {
        log.debug("Obtendo estatísticas gerais de veículos");
        
        long totalVeiculos = veiculoRepository.count();
        long veiculosAtivos = veiculoRepositoryImpl.countByStatus(StatusVeiculo.ATIVO);
        long veiculosComApolice = veiculoRepositoryImpl.countByApoliceAtivaTrue();
        long veiculosSemApolice = totalVeiculos - veiculosComApolice;
        
        double percentualComApolice = totalVeiculos > 0 ? 
            (double) veiculosComApolice / totalVeiculos * 100 : 0.0;
        
        // Buscar marca mais comum
        List<Object[]> marcas = obterEstatisticasPorMarca();
        String marcaMaisComum = marcas.isEmpty() ? "N/A" : (String) marcas.get(0)[0];
        
        // Buscar estado mais comum
        List<Object[]> estados = obterEstatisticasPorEstado();
        String estadoMaisComum = estados.isEmpty() ? "N/A" : (String) estados.get(0)[0];
        
        return new VeiculoStatistics(
            totalVeiculos,
            veiculosAtivos,
            veiculosComApolice,
            veiculosSemApolice,
            percentualComApolice,
            marcaMaisComum,
            estadoMaisComum
        );
    }
    
    @Override
    @Cacheable(value = "veiculo-stats-estado", key = "'estados'")
    public List<Object[]> obterEstatisticasPorEstado() {
        return veiculoRepositoryImpl.countByEstado();
    }
    
    @Override
    @Cacheable(value = "veiculo-stats-marca", key = "'marcas'")
    public List<Object[]> obterEstatisticasPorMarca() {
        return veiculoRepositoryImpl.countByMarca();
    }
    
    // === MÉTODOS DE CONVERSÃO ===
    
    private VeiculoListView toListView(VeiculoQueryModel model) {
        return new VeiculoListView(
            model.getId(),
            model.getPlaca(),
            model.getMarca(),
            model.getModelo(),
            model.getAnoFabricacao(),
            model.getAnoModelo(),
            model.getCor(),
            model.getProprietarioNome(),
            model.getProprietarioCpf(),
            model.getStatus(),
            model.getApoliceAtiva(),
            model.getCidade(),
            model.getEstado(),
            model.getCreatedAt(),
            model.getUpdatedAt()
        );
    }
    
    private VeiculoDetailView toDetailView(VeiculoQueryModel model) {
        return new VeiculoDetailView(
            model.getId(),
            model.getPlaca(),
            model.getRenavam(),
            model.getChassi(),
            model.getMarca(),
            model.getModelo(),
            model.getAnoFabricacao(),
            model.getAnoModelo(),
            model.getCor(),
            model.getTipoCombustivel(),
            model.getCategoria(),
            model.getCilindrada(),
            model.getProprietarioNome(),
            model.getProprietarioCpf(),
            model.getProprietarioTipo(),
            model.getStatus(),
            model.getApoliceAtiva(),
            model.getCidade(),
            model.getEstado(),
            model.getRegiao(),
            model.getVersion(),
            model.getLastEventId(),
            model.getCreatedAt(),
            model.getUpdatedAt(),
            // Por enquanto, listas vazias - serão implementadas quando necessário
            Collections.emptyList(), // apolicesAssociadas
            Collections.emptyList()  // historicoAlteracoes
        );
    }
}
