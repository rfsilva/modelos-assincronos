package com.seguradora.hibrida.query.service;

import com.seguradora.hibrida.query.dto.SinistroDetailView;
import com.seguradora.hibrida.query.dto.SinistroListView;
import com.seguradora.hibrida.query.dto.SinistroFilter;
import com.seguradora.hibrida.query.dto.DashboardView;
import com.seguradora.hibrida.query.model.SinistroQueryModel;
import com.seguradora.hibrida.query.repository.SinistroQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementação do serviço de consultas para sinistros no Query Side do CQRS.
 */
@Service
@Transactional(readOnly = true, transactionManager = "readTransactionManager")
public class SinistroQueryServiceImpl implements SinistroQueryService {
    
    private static final Logger log = LoggerFactory.getLogger(SinistroQueryServiceImpl.class);
    
    private final SinistroQueryRepository repository;
    
    public SinistroQueryServiceImpl(SinistroQueryRepository repository) {
        this.repository = repository;
    }
    
    @Override
    @Cacheable(value = "sinistro-detail", key = "#id", unless = "#result == null")
    public Optional<SinistroDetailView> buscarPorId(UUID id) {
        log.debug("Buscando sinistro por ID: {}", id);
        
        return repository.findById(id)
                .map(this::toDetailView);
    }
    
    @Override
    @Cacheable(value = "sinistro-detail", key = "#protocolo", unless = "#result == null")
    public Optional<SinistroDetailView> buscarPorProtocolo(String protocolo) {
        log.debug("Buscando sinistro por protocolo: {}", protocolo);
        
        return repository.findByProtocolo(protocolo)
                .map(this::toDetailView);
    }
    
    @Override
    public Page<SinistroListView> listar(SinistroFilter filter, Pageable pageable) {
        log.debug("Listando sinistros com filtros: {}", filter);
        
        Specification<SinistroQueryModel> spec = buildSpecification(filter);
        
        return repository.findAll(spec, pageable)
                .map(this::toListView);
    }
    
    @Override
    @Cacheable(value = "sinistros-por-cpf", key = "#cpf + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<SinistroListView> buscarPorCpfSegurado(String cpf, Pageable pageable) {
        log.debug("Buscando sinistros por CPF: {}", cpf);
        
        // Usar método simples por enquanto
        return repository.findAll(pageable).map(this::toListView);
    }
    
    @Override
    @Cacheable(value = "sinistros-por-placa", key = "#placa + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<SinistroListView> buscarPorPlaca(String placa, Pageable pageable) {
        log.debug("Buscando sinistros por placa: {}", placa);
        
        // Usar método simples por enquanto
        return repository.findAll(pageable).map(this::toListView);
    }
    
    @Override
    public Page<SinistroListView> buscarPorTexto(String termo, Pageable pageable) {
        log.debug("Realizando busca textual: {}", termo);
        
        // Usar método simples por enquanto
        return repository.findAll(pageable).map(this::toListView);
    }
    
    @Override
    public Page<SinistroListView> buscarPorTag(String tag, Pageable pageable) {
        log.debug("Buscando sinistros por tag: {}", tag);
        
        // Usar método simples por enquanto
        return repository.findAll(pageable).map(this::toListView);
    }
    
    @Override
    @Cacheable(value = "dashboard", key = "'sinistros'", unless = "#result == null")
    public DashboardView obterDashboard() {
        log.debug("Obtendo dados do dashboard");
        
        // Total de sinistros
        long totalSinistros = repository.count();
        
        return new DashboardView(
                totalSinistros,
                0L, // sinistrosAbertos
                0L, // consultasPendentes
                null, // estatisticasPorStatus
                null, // estatisticasDiarias
                null, // estatisticasPorTipo
                null, // estatisticasPorOperador
                null, // estatisticasPorPrioridade
                null, // taxaResolucao
                null, // tempoMedioResolucao
                null  // valorTotalAbertos
        );
    }
    
    /**
     * Constrói Specification baseada nos filtros.
     */
    private Specification<SinistroQueryModel> buildSpecification(SinistroFilter filter) {
        // Começar com uma Specification vazia tipada corretamente
        Specification<SinistroQueryModel> spec = (root, query, cb) -> cb.conjunction();
        
        if (filter.getStatus() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        
        if (filter.getTipoSinistro() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tipoSinistro"), filter.getTipoSinistro()));
        }
        
        if (filter.getOperadorResponsavel() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("operadorResponsavel"), filter.getOperadorResponsavel()));
        }
        
        if (filter.getDataAberturaInicio() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataAbertura"), filter.getDataAberturaInicio()));
        }
        
        if (filter.getDataAberturaFim() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataAbertura"), filter.getDataAberturaFim()));
        }
        
        if (filter.getCpfSegurado() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("cpfSegurado"), filter.getCpfSegurado()));
        }
        
        if (filter.getPlaca() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("placa"), filter.getPlaca()));
        }
        
        return spec;
    }
    
    /**
     * Converte SinistroQueryModel para SinistroDetailView.
     */
    private SinistroDetailView toDetailView(SinistroQueryModel model) {
        return new SinistroDetailView(
                model.getId(),
                model.getProtocolo(),
                new SinistroDetailView.SeguradoInfo(
                        model.getCpfSegurado(),
                        model.getNomeSegurado(),
                        model.getEmailSegurado(),
                        model.getTelefoneSegurado()
                ),
                new SinistroDetailView.VeiculoInfo(
                        model.getPlaca(),
                        model.getRenavam(),
                        model.getChassi(),
                        model.getMarca(),
                        model.getModelo(),
                        model.getAnoFabricacao(),
                        model.getAnoModelo(),
                        model.getCor()
                ),
                new SinistroDetailView.ApoliceInfo(
                        model.getApoliceNumero(),
                        model.getApoliceVigenciaInicio(),
                        model.getApoliceVigenciaFim(),
                        model.getApoliceValorSegurado()
                ),
                model.getTipoSinistro(),
                model.getStatus(),
                model.getDataOcorrencia(),
                model.getDataAbertura(),
                model.getDataFechamento(),
                model.getOperadorResponsavel(),
                model.getDescricao(),
                model.getValorEstimado(),
                model.getValorFranquia(),
                model.isConsultaDetranSucesso() ? 
                        new SinistroDetailView.ConsultaDetranInfo(
                                model.getConsultaDetranRealizada(),
                                model.getConsultaDetranTimestamp(),
                                model.getConsultaDetranStatus(),
                                model.getDadosDetran()
                        ) : null,
                new SinistroDetailView.LocalizacaoInfo(
                        model.getCepOcorrencia(),
                        model.getEnderecoOcorrencia(),
                        model.getCidadeOcorrencia(),
                        model.getEstadoOcorrencia()
                ),
                model.getTags(),
                model.getPrioridade(),
                model.getCanalAbertura(),
                model.getCreatedAt(),
                model.getUpdatedAt()
        );
    }
    
    /**
     * Converte SinistroQueryModel para SinistroListView.
     */
    private SinistroListView toListView(SinistroQueryModel model) {
        return new SinistroListView(
                model.getId(),
                model.getProtocolo(),
                model.getCpfSegurado(),
                model.getNomeSegurado(),
                model.getPlaca(),
                model.getTipoSinistro(),
                model.getStatus(),
                model.getDataOcorrencia(),
                model.getDataAbertura(),
                model.getOperadorResponsavel(),
                model.getValorEstimado(),
                model.getConsultaDetranRealizada(),
                model.getTags(),
                model.getPrioridade()
        );
    }
}