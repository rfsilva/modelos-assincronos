# 📊 PROJECTION HANDLERS - PARTE 3: QUERY MODELS E REPOSITÓRIOS
## Roteiro Técnico para Analistas Java Junior

### 🎯 **OBJETIVO DESTA PARTE**
Compreender a estrutura dos Query Models, implementação de repositórios otimizados e estratégias de consulta eficientes.

---

## 🏗️ **QUERY MODELS - ESTRUTURA E DESIGN**

### **📋 Conceitos Fundamentais**

**Query Models** são entidades JPA otimizadas para **leitura**, projetadas especificamente para atender às necessidades de consulta da aplicação.

**Características Principais:**
- ✅ **Desnormalizados**: Dados agregados em uma estrutura
- ✅ **Otimizados**: Índices específicos para consultas
- ✅ **Flexíveis**: Múltiplas visões dos mesmos dados
- ✅ **Performáticos**: Consultas diretas sem JOINs complexos

### **🎯 SinistroQueryModel - Exemplo Completo**

**Localização**: `com.seguradora.hibrida.query.model.SinistroQueryModel`

```java
@Entity
@Table(name = "sinistro_view", schema = "projections", indexes = {
    @Index(name = "idx_sinistro_protocolo", columnList = "protocolo", unique = true),
    @Index(name = "idx_sinistro_cpf_segurado", columnList = "cpfSegurado"),
    @Index(name = "idx_sinistro_placa", columnList = "placa"),
    @Index(name = "idx_sinistro_status", columnList = "status"),
    @Index(name = "idx_sinistro_data_abertura", columnList = "dataAbertura"),
    @Index(name = "idx_sinistro_operador", columnList = "operadorResponsavel"),
    @Index(name = "idx_sinistro_consulta_detran", columnList = "consultaDetranRealizada"),
    @Index(name = "idx_sinistro_last_event", columnList = "lastEventId")
})
public class SinistroQueryModel {
    
    // === IDENTIFICAÇÃO ===
    @Id
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String protocolo;
    
    @Column(nullable = false)
    private Long version;
    
    @Column(nullable = false)
    private Long lastEventId;
    
    // === DADOS BÁSICOS ===
    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false)
    private String tipoSinistro;
    
    @Column(columnDefinition = "TEXT")
    private String descricao;
    
    private String prioridade;
    private String canalAbertura;
    private String operadorResponsavel;
    
    // === DADOS TEMPORAIS ===
    @Column(nullable = false)
    private Instant dataAbertura;
    
    private Instant dataOcorrencia;
    private Instant dataFechamento;
    
    // === DADOS DO SEGURADO ===
    @Column(nullable = false)
    private String cpfSegurado;
    
    @Column(nullable = false)
    private String nomeSegurado;
    
    private String emailSegurado;
    private String telefoneSegurado;
    
    // === DADOS DO VEÍCULO ===
    @Column(nullable = false)
    private String placa;
    
    private String marca;
    private String modelo;
    private String cor;
    private String chassi;
    private String renavam;
    private Integer anoFabricacao;
    private Integer anoModelo;
    
    // === DADOS DA APÓLICE ===
    private String apoliceNumero;
    private LocalDate apoliceVigenciaInicio;
    private LocalDate apoliceVigenciaFim;
    private BigDecimal apoliceValorSegurado;
    private BigDecimal valorFranquia;
    
    // === DADOS FINANCEIROS ===
    private BigDecimal valorEstimado;
    
    // === LOCALIZAÇÃO ===
    private String enderecoOcorrencia;
    private String cidadeOcorrencia;
    private String estadoOcorrencia;
    private String cepOcorrencia;
    
    // === DADOS DETRAN ===
    private Boolean consultaDetranRealizada = false;
    private String consultaDetranStatus;
    private Instant consultaDetranTimestamp;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> dadosDetran;
    
    // === TAGS E METADADOS ===
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> tags = new ArrayList<>();
    
    // === CONTROLE ===
    @CreationTimestamp
    private Instant createdAt;
    
    @UpdateTimestamp
    private Instant updatedAt;
    
    // === MÉTODOS DE NEGÓCIO ===
    
    public boolean isAberto() {
        return "ABERTO".equals(status) || "EM_ANALISE".equals(status);
    }
    
    public boolean isFechado() {
        return "FECHADO".equals(status) || "CANCELADO".equals(status);
    }
    
    public boolean isConsultaDetranSucesso() {
        return Boolean.TRUE.equals(consultaDetranRealizada) && 
               "SUCCESS".equals(consultaDetranStatus);
    }
    
    public boolean isApoliceVigenteNaOcorrencia() {
        if (dataOcorrencia == null || apoliceVigenciaInicio == null || apoliceVigenciaFim == null) {
            return false;
        }
        
        LocalDate dataOcorrenciaLocal = dataOcorrencia.atZone(ZoneId.systemDefault()).toLocalDate();
        return !dataOcorrenciaLocal.isBefore(apoliceVigenciaInicio) && 
               !dataOcorrenciaLocal.isAfter(apoliceVigenciaFim);
    }
    
    public void adicionarTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }
    
    public void removerTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }
    
    public boolean possuiTag(String tag) {
        return tags != null && tags.contains(tag);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getDadoDetran(String chave, Class<T> tipo) {
        if (dadosDetran == null || !dadosDetran.containsKey(chave)) {
            return null;
        }
        
        Object valor = dadosDetran.get(chave);
        if (tipo.isInstance(valor)) {
            return (T) valor;
        }
        
        return null;
    }
    
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
    
    // === GETTERS E SETTERS ===
    // ... (implementação completa dos getters/setters)
}
```

---

## 🗃️ **REPOSITÓRIOS OTIMIZADOS**

### **📋 SinistroQueryRepository - Interface Completa**

**Localização**: `com.seguradora.hibrida.query.repository.SinistroQueryRepository`

```java
@Repository
public interface SinistroQueryRepository extends JpaRepository<SinistroQueryModel, UUID>, 
                                               JpaSpecificationExecutor<SinistroQueryModel> {
    
    // === CONSULTAS BÁSICAS ===
    
    /**
     * Busca sinistro por protocolo.
     */
    Optional<SinistroQueryModel> findByProtocolo(String protocolo);
    
    /**
     * Busca sinistros por CPF do segurado.
     */
    List<SinistroQueryModel> findByCpfSeguradoOrderByDataAberturaDesc(String cpfSegurado);
    
    /**
     * Busca sinistros por placa do veículo.
     */
    List<SinistroQueryModel> findByPlacaOrderByDataAberturaDesc(String placa);
    
    /**
     * Busca sinistros por número da apólice.
     */
    List<SinistroQueryModel> findByApoliceNumeroOrderByDataAberturaDesc(String apoliceNumero);
    
    // === CONSULTAS POR STATUS ===
    
    /**
     * Busca sinistros por status com paginação.
     */
    Page<SinistroQueryModel> findByStatusOrderByDataAberturaDesc(String status, Pageable pageable);
    
    /**
     * Busca sinistros abertos (múltiplos status).
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE s.status IN ('ABERTO', 'EM_ANALISE') ORDER BY s.dataAbertura DESC")
    Page<SinistroQueryModel> findSinistrosAbertos(Pageable pageable);
    
    /**
     * Busca sinistros fechados (múltiplos status).
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE s.status IN ('FECHADO', 'CANCELADO') ORDER BY s.dataFechamento DESC")
    Page<SinistroQueryModel> findSinistrosFechados(Pageable pageable);
    
    // === CONSULTAS POR PERÍODO ===
    
    /**
     * Busca sinistros por período de ocorrência.
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE s.dataOcorrencia BETWEEN :inicio AND :fim ORDER BY s.dataOcorrencia DESC")
    Page<SinistroQueryModel> findByPeriodoOcorrencia(@Param("inicio") Instant inicio, 
                                                     @Param("fim") Instant fim, 
                                                     Pageable pageable);
    
    /**
     * Busca sinistros por período de abertura.
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE s.dataAbertura BETWEEN :inicio AND :fim ORDER BY s.dataAbertura DESC")
    Page<SinistroQueryModel> findByPeriodoAbertura(@Param("inicio") Instant inicio, 
                                                   @Param("fim") Instant fim, 
                                                   Pageable pageable);
    
    // === CONSULTAS POR OPERADOR ===
    
    /**
     * Busca sinistros por operador responsável.
     */
    Page<SinistroQueryModel> findByOperadorResponsavelOrderByDataAberturaDesc(String operador, Pageable pageable);
    
    /**
     * Busca sinistros sem operador responsável.
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE s.operadorResponsavel IS NULL ORDER BY s.dataAbertura ASC")
    Page<SinistroQueryModel> findSinistrosSemOperador(Pageable pageable);
    
    // === FULL-TEXT SEARCH ===
    
    /**
     * Busca por texto livre usando full-text search do PostgreSQL.
     */
    @Query(value = """
        SELECT * FROM projections.sinistro_view s 
        WHERE to_tsvector('portuguese', 
            COALESCE(s.protocolo, '') || ' ' || 
            COALESCE(s.nome_segurado, '') || ' ' || 
            COALESCE(s.descricao, '') || ' ' ||
            COALESCE(s.placa, '') || ' ' ||
            COALESCE(s.apolice_numero, '')
        ) @@ plainto_tsquery('portuguese', :termo)
        ORDER BY s.data_abertura DESC
        """, nativeQuery = true)
    List<SinistroQueryModel> findByFullTextSearch(@Param("termo") String termo);
    
    /**
     * Busca por texto livre com paginação.
     */
    @Query(value = """
        SELECT * FROM projections.sinistro_view s 
        WHERE to_tsvector('portuguese', 
            COALESCE(s.protocolo, '') || ' ' || 
            COALESCE(s.nome_segurado, '') || ' ' || 
            COALESCE(s.descricao, '') || ' ' ||
            COALESCE(s.placa, '') || ' ' ||
            COALESCE(s.apolice_numero, '')
        ) @@ plainto_tsquery('portuguese', :termo)
        ORDER BY s.data_abertura DESC
        LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
        """, 
        countQuery = """
        SELECT COUNT(*) FROM projections.sinistro_view s 
        WHERE to_tsvector('portuguese', 
            COALESCE(s.protocolo, '') || ' ' || 
            COALESCE(s.nome_segurado, '') || ' ' || 
            COALESCE(s.descricao, '') || ' ' ||
            COALESCE(s.placa, '') || ' ' ||
            COALESCE(s.apolice_numero, '')
        ) @@ plainto_tsquery('portuguese', :termo)
        """, nativeQuery = true)
    Page<SinistroQueryModel> findByFullTextSearchPaged(@Param("termo") String termo, Pageable pageable);
    
    // === CONSULTAS POR TAGS ===
    
    /**
     * Busca sinistros que possuem uma tag específica.
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE :tag = ANY(s.tags) ORDER BY s.dataAbertura DESC")
    List<SinistroQueryModel> findByTag(@Param("tag") String tag);
    
    /**
     * Busca sinistros que possuem qualquer uma das tags especificadas.
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE s.tags && CAST(:tags AS text[]) ORDER BY s.dataAbertura DESC")
    List<SinistroQueryModel> findByAnyTag(@Param("tags") String[] tags);
    
    // === CONSULTAS DETRAN ===
    
    /**
     * Busca sinistros que ainda não tiveram consulta ao DETRAN.
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE s.consultaDetranRealizada = false OR s.consultaDetranRealizada IS NULL ORDER BY s.dataAbertura ASC")
    List<SinistroQueryModel> findSinistrosSemConsultaDetran();
    
    /**
     * Busca sinistros com consulta DETRAN com erro.
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE s.consultaDetranStatus = 'ERROR' ORDER BY s.consultaDetranTimestamp DESC")
    List<SinistroQueryModel> findSinistrosComErroDetran();
    
    // === CONSULTAS DE AGREGAÇÃO ===
    
    /**
     * Conta sinistros por status.
     */
    @Query("SELECT s.status, COUNT(s) FROM SinistroQueryModel s GROUP BY s.status")
    List<Object[]> countByStatus();
    
    /**
     * Conta sinistros por tipo.
     */
    @Query("SELECT s.tipoSinistro, COUNT(s) FROM SinistroQueryModel s GROUP BY s.tipoSinistro ORDER BY COUNT(s) DESC")
    List<Object[]> countByTipo();
    
    /**
     * Conta sinistros por operador.
     */
    @Query("SELECT s.operadorResponsavel, COUNT(s) FROM SinistroQueryModel s WHERE s.operadorResponsavel IS NOT NULL GROUP BY s.operadorResponsavel ORDER BY COUNT(s) DESC")
    List<Object[]> countByOperador();
    
    /**
     * Estatísticas de sinistros por período.
     */
    @Query(value = """
        SELECT 
            DATE_TRUNC('day', s.data_abertura) as dia,
            COUNT(*) as total,
            COUNT(CASE WHEN s.status IN ('ABERTO', 'EM_ANALISE') THEN 1 END) as abertos,
            COUNT(CASE WHEN s.status IN ('FECHADO', 'CANCELADO') THEN 1 END) as fechados
        FROM projections.sinistro_view s 
        WHERE s.data_abertura BETWEEN :inicio AND :fim
        GROUP BY DATE_TRUNC('day', s.data_abertura)
        ORDER BY dia DESC
        """, nativeQuery = true)
    List<Object[]> getEstatisticasPorDia(@Param("inicio") Instant inicio, @Param("fim") Instant fim);
    
    // === CONSULTAS DE PERFORMANCE ===
    
    /**
     * Busca sinistros atualizados recentemente.
     */
    List<SinistroQueryModel> findByUpdatedAtGreaterThanOrderByUpdatedAtDesc(Instant since);
    
    /**
     * Busca sinistros por último evento processado.
     */
    List<SinistroQueryModel> findByLastEventIdGreaterThanOrderByLastEventIdAsc(Long eventId);
    
    // === CONSULTAS CUSTOMIZADAS ===
    
    /**
     * Busca sinistros com filtros múltiplos.
     */
    @Query("""
        SELECT s FROM SinistroQueryModel s 
        WHERE (:status IS NULL OR s.status = :status)
        AND (:tipoSinistro IS NULL OR s.tipoSinistro = :tipoSinistro)
        AND (:operador IS NULL OR s.operadorResponsavel = :operador)
        AND (:cpfSegurado IS NULL OR s.cpfSegurado = :cpfSegurado)
        AND (:dataInicio IS NULL OR s.dataAbertura >= :dataInicio)
        AND (:dataFim IS NULL OR s.dataAbertura <= :dataFim)
        ORDER BY s.dataAbertura DESC
        """)
    Page<SinistroQueryModel> findWithFilters(@Param("status") String status,
                                            @Param("tipoSinistro") String tipoSinistro,
                                            @Param("operador") String operador,
                                            @Param("cpfSegurado") String cpfSegurado,
                                            @Param("dataInicio") Instant dataInicio,
                                            @Param("dataFim") Instant dataFim,
                                            Pageable pageable);
    
    /**
     * Obtém resumo executivo de sinistros.
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total,
            COUNT(CASE WHEN status IN ('ABERTO', 'EM_ANALISE') THEN 1 END) as abertos,
            COUNT(CASE WHEN status IN ('FECHADO', 'CANCELADO') THEN 1 END) as fechados,
            AVG(CASE WHEN valor_estimado IS NOT NULL THEN valor_estimado ELSE 0 END) as valor_medio,
            COUNT(CASE WHEN consulta_detran_realizada = true THEN 1 END) as com_detran
        FROM projections.sinistro_view
        WHERE data_abertura >= :desde
        """, nativeQuery = true)
    Object[] getResumoExecutivo(@Param("desde") Instant desde);
    
    // === MÉTODOS DE VERIFICAÇÃO ===
    
    /**
     * Verifica se existe sinistro com protocolo.
     */
    boolean existsByProtocolo(String protocolo);
    
    /**
     * Conta sinistros por CPF do segurado.
     */
    long countByCpfSegurado(String cpfSegurado);
    
    /**
     * Conta sinistros por placa.
     */
    long countByPlaca(String placa);
}
```

---

## 🔍 **SPECIFICATIONS PARA CONSULTAS DINÂMICAS**

### **📋 SinistroSpecifications**

```java
public class SinistroSpecifications {
    
    public static Specification<SinistroQueryModel> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null || status.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }
    
    public static Specification<SinistroQueryModel> hasCpfSegurado(String cpf) {
        return (root, query, criteriaBuilder) -> {
            if (cpf == null || cpf.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("cpfSegurado"), cpf);
        };
    }
    
    public static Specification<SinistroQueryModel> hasPlaca(String placa) {
        return (root, query, criteriaBuilder) -> {
            if (placa == null || placa.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("placa"), placa.toUpperCase());
        };
    }
    
    public static Specification<SinistroQueryModel> hasOperador(String operador) {
        return (root, query, criteriaBuilder) -> {
            if (operador == null || operador.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("operadorResponsavel"), operador);
        };
    }
    
    public static Specification<SinistroQueryModel> betweenDates(Instant inicio, Instant fim) {
        return (root, query, criteriaBuilder) -> {
            if (inicio == null && fim == null) {
                return criteriaBuilder.conjunction();
            }
            
            if (inicio != null && fim != null) {
                return criteriaBuilder.between(root.get("dataAbertura"), inicio, fim);
            } else if (inicio != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("dataAbertura"), inicio);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("dataAbertura"), fim);
            }
        };
    }
    
    public static Specification<SinistroQueryModel> hasTag(String tag) {
        return (root, query, criteriaBuilder) -> {
            if (tag == null || tag.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            // Usar função PostgreSQL para buscar em array JSON
            return criteriaBuilder.isTrue(
                criteriaBuilder.function("jsonb_exists", Boolean.class,
                    root.get("tags"), criteriaBuilder.literal(tag))
            );
        };
    }
    
    public static Specification<SinistroQueryModel> consultaDetranPendente(Boolean pendente) {
        return (root, query, criteriaBuilder) -> {
            if (pendente == null) {
                return criteriaBuilder.conjunction();
            }
            
            if (pendente) {
                return criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("consultaDetranRealizada")),
                    criteriaBuilder.equal(root.get("consultaDetranRealizada"), false)
                );
            } else {
                return criteriaBuilder.equal(root.get("consultaDetranRealizada"), true);
            }
        };
    }
    
    public static Specification<SinistroQueryModel> fullTextSearch(String termo) {
        return (root, query, criteriaBuilder) -> {
            if (termo == null || termo.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            // Usar função PostgreSQL para full-text search
            Expression<Boolean> searchExpression = criteriaBuilder.function(
                "fts_match", Boolean.class,
                criteriaBuilder.concat(
                    criteriaBuilder.concat(
                        criteriaBuilder.coalesce(root.get("protocolo"), ""), " "),
                    criteriaBuilder.concat(
                        criteriaBuilder.coalesce(root.get("nomeSegurado"), ""), " ")
                ),
                criteriaBuilder.literal(termo)
            );
            
            return criteriaBuilder.isTrue(searchExpression);
        };
    }
}
```

---

## 🎯 **SERVIÇOS DE CONSULTA**

### **📋 SinistroQueryService - Implementação**

```java
@Service
@Transactional(readOnly = true)
public class SinistroQueryServiceImpl implements SinistroQueryService {
    
    private final SinistroQueryRepository repository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public SinistroQueryServiceImpl(SinistroQueryRepository repository,
                                  RedisTemplate<String, Object> redisTemplate) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    @Cacheable(value = "sinistro-detail", key = "#id")
    public Optional<SinistroDetailView> buscarPorId(UUID id) {
        return repository.findById(id)
            .map(this::toDetailView);
    }
    
    @Override
    @Cacheable(value = "sinistro-detail", key = "#protocolo")
    public Optional<SinistroDetailView> buscarPorProtocolo(String protocolo) {
        return repository.findByProtocolo(protocolo)
            .map(this::toDetailView);
    }
    
    @Override
    public Page<SinistroListView> listar(SinistroFilter filter, Pageable pageable) {
        Specification<SinistroQueryModel> spec = buildSpecification(filter);
        
        return repository.findAll(spec, pageable)
            .map(this::toListView);
    }
    
    @Override
    public Page<SinistroListView> buscarPorCpfSegurado(String cpf, Pageable pageable) {
        return repository.findAll(
            SinistroSpecifications.hasCpfSegurado(cpf), 
            pageable
        ).map(this::toListView);
    }
    
    @Override
    public Page<SinistroListView> buscarPorPlaca(String placa, Pageable pageable) {
        return repository.findAll(
            SinistroSpecifications.hasPlaca(placa), 
            pageable
        ).map(this::toListView);
    }
    
    @Override
    public Page<SinistroListView> buscarPorTexto(String termo, Pageable pageable) {
        // Para termos simples, usar repository method
        if (termo.split("\\s+").length == 1) {
            return repository.findByFullTextSearchPaged(termo, pageable)
                .map(this::toListView);
        }
        
        // Para termos complexos, usar Specification
        return repository.findAll(
            SinistroSpecifications.fullTextSearch(termo), 
            pageable
        ).map(this::toListView);
    }
    
    @Override
    public Page<SinistroListView> buscarPorTag(String tag, Pageable pageable) {
        return repository.findAll(
            SinistroSpecifications.hasTag(tag), 
            pageable
        ).map(this::toListView);
    }
    
    @Override
    @Cacheable(value = "dashboard", key = "'sinistro-dashboard'")
    public DashboardView obterDashboard() {
        Instant umMesAtras = Instant.now().minus(30, ChronoUnit.DAYS);
        
        Object[] resumo = repository.getResumoExecutivo(umMesAtras);
        List<Object[]> statusCount = repository.countByStatus();
        List<Object[]> tipoCount = repository.countByTipo();
        
        return DashboardView.builder()
            .totalSinistros(((Number) resumo[0]).longValue())
            .sinistrosAbertos(((Number) resumo[1]).longValue())
            .sinistrosFechados(((Number) resumo[2]).longValue())
            .valorMedioSinistro(((Number) resumo[3]).doubleValue())
            .sinistrosComDetran(((Number) resumo[4]).longValue())
            .distribuicaoStatus(convertToMap(statusCount))
            .distribuicaoTipo(convertToMap(tipoCount))
            .periodoReferencia(umMesAtras)
            .build();
    }
    
    private Specification<SinistroQueryModel> buildSpecification(SinistroFilter filter) {
        return Specification.where(SinistroSpecifications.hasStatus(filter.getStatus()))
            .and(SinistroSpecifications.hasCpfSegurado(filter.getCpfSegurado()))
            .and(SinistroSpecifications.hasPlaca(filter.getPlaca()))
            .and(SinistroSpecifications.hasOperador(filter.getOperadorResponsavel()))
            .and(SinistroSpecifications.betweenDates(filter.getDataAberturaInicio(), filter.getDataAberturaFim()))
            .and(SinistroSpecifications.hasTag(filter.getTag()))
            .and(SinistroSpecifications.consultaDetranPendente(filter.getConsultaDetranPendente()));
    }
    
    private SinistroDetailView toDetailView(SinistroQueryModel model) {
        return SinistroDetailView.builder()
            .id(model.getId())
            .protocolo(model.getProtocolo())
            .status(model.getStatus())
            .tipoSinistro(model.getTipoSinistro())
            .descricao(model.getDescricao())
            .dataAbertura(model.getDataAbertura())
            .dataOcorrencia(model.getDataOcorrencia())
            .nomeSegurado(model.getNomeSegurado())
            .cpfSegurado(model.getCpfSegurado())
            .emailSegurado(model.getEmailSegurado())
            .placa(model.getPlaca())
            .marca(model.getMarca())
            .modelo(model.getModelo())
            .valorEstimado(model.getValorEstimado())
            .consultaDetranRealizada(model.getConsultaDetranRealizada())
            .dadosDetran(model.getDadosDetran())
            .tags(model.getTags())
            .build();
    }
    
    private SinistroListView toListView(SinistroQueryModel model) {
        return SinistroListView.builder()
            .id(model.getId())
            .protocolo(model.getProtocolo())
            .status(model.getStatus())
            .nomeSegurado(model.getNomeSegurado())
            .placa(model.getPlaca())
            .dataAbertura(model.getDataAbertura())
            .valorEstimado(model.getValorEstimado())
            .operadorResponsavel(model.getOperadorResponsavel())
            .consultaDetranRealizada(model.getConsultaDetranRealizada())
            .build();
    }
}
```

---

## 🎯 **OTIMIZAÇÕES DE PERFORMANCE**

### **📊 Índices Estratégicos**

```sql
-- Índices para consultas frequentes
CREATE INDEX CONCURRENTLY idx_sinistro_view_composite_search 
ON projections.sinistro_view (status, data_abertura DESC, operador_responsavel);

-- Índice para full-text search
CREATE INDEX CONCURRENTLY idx_sinistro_view_fts 
ON projections.sinistro_view USING gin(to_tsvector('portuguese', 
    COALESCE(protocolo, '') || ' ' || 
    COALESCE(nome_segurado, '') || ' ' || 
    COALESCE(descricao, '')));

-- Índice para consultas por tags
CREATE INDEX CONCURRENTLY idx_sinistro_view_tags 
ON projections.sinistro_view USING gin(tags);

-- Índice para dados DETRAN
CREATE INDEX CONCURRENTLY idx_sinistro_view_detran_status 
ON projections.sinistro_view (consulta_detran_realizada, consulta_detran_status);
```

### **🚀 Cache Strategy**

```java
@Configuration
@EnableCaching
public class QueryCacheConfiguration {
    
    @Bean
    public CacheManager queryCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(15))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .transactionAware()
            .build();
    }
}
```

---

## 🎓 **EXERCÍCIO PRÁTICO**

### **📝 Implementar Query Model Completo**

Crie um `SeguradoQueryModel` e `SeguradoQueryRepository` que:

1. **Modele dados otimizados de segurado**
2. **Implemente consultas específicas**
3. **Use Specifications para filtros dinâmicos**
4. **Inclua cache estratégico**

**Campos sugeridos:**
- Dados pessoais (nome, CPF, email, telefone)
- Endereços (múltiplos)
- Apólices ativas
- Histórico de sinistros
- Status e tags

---

## 📚 **REFERÊNCIAS**

- **Código**: `com.seguradora.hibrida.query`
- **Modelos**: `SinistroQueryModel`
- **Repositórios**: `SinistroQueryRepository`
- **Serviços**: `SinistroQueryService`

---

**📍 Próxima Parte**: [Projections - Parte 4: Rebuild e Consistência](./07-projections-parte-4.md)

---

**📚 Roteiro elaborado por:** Principal Java Architect  
**🎯 Foco:** Query Models e repositórios otimizados  
**⏱️ Tempo estimado:** 60 minutos  
**🔧 Hands-on:** Implementação completa de query model