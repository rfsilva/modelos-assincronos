# 🔄 ETAPA 05: IMPLEMENTAÇÃO DE PROJEÇÕES
## Query Side (Read) - Projections e Query Models

### 🎯 **OBJETIVO DA ETAPA**

Implementar o lado de consulta (read) da arquitetura CQRS, criando projeções otimizadas para leitura, query models desnormalizados e serviços de consulta eficientes.

**⏱️ Duração Estimada:** 4-6 horas  
**👥 Participantes:** Desenvolvedor + Tech Lead  
**📋 Pré-requisitos:** Etapa 04 concluída e aprovada

---

## 📋 **CHECKLIST DE IMPLEMENTAÇÃO**

### **🗃️ 1. CRIAÇÃO DE QUERY MODELS**

#### **📊 Estrutura Base do Query Model:**
```java
@Entity
@Table(name = "[dominio]_view", schema = "projections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class [Dominio]QueryModel {
    
    // ========== IDENTIFICAÇÃO ==========
    @Id
    private UUID id;
    
    // ========== CAMPOS DE NEGÓCIO DESNORMALIZADOS ==========
    @Column(name = "[campo1]")
    private String [campo1];
    
    @Column(name = "[campo2]")
    private String [campo2];
    
    @Column(name = "[campo_numerico]")
    private BigDecimal [campoNumerico];
    
    @Column(name = "[campo_data]")
    private Instant [campoData];
    
    // ========== CAMPOS DE BUSCA OTIMIZADOS ==========
    @Column(name = "search_text")
    private String searchText; // Para full-text search
    
    @ElementCollection
    @CollectionTable(name = "[dominio]_tags", joinColumns = @JoinColumn(name = "[dominio]_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();
    
    // ========== CAMPOS DE CONTROLE ==========
    @Column(name = "created_at")
    @CreationTimestamp
    private Instant createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;
    
    @Column(name = "last_event_id")
    private Long lastEventId;
    
    @Version
    private Long version;
    
    // ========== MÉTODOS DE CONVENIÊNCIA ==========
    public void updateSearchText() {
        this.searchText = String.join(" ",
            Optional.ofNullable([campo1]).orElse(""),
            Optional.ofNullable([campo2]).orElse(""),
            String.join(" ", tags)
        ).toLowerCase();
    }
    
    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
            updateSearchText();
        }
    }
    
    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
            updateSearchText();
        }
    }
    
    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }
    
    // ========== LIFECYCLE CALLBACKS ==========
    @PrePersist
    @PreUpdate
    public void updateTimestamps() {
        updateSearchText();
    }
    
    @Override
    public String toString() {
        return String.format("[Dominio]QueryModel{id=%s, [campo1]='%s', version=%d}",
                           id, [campo1], version);
    }
}
```

#### **🗂️ Índices de Performance:**
```sql
-- Criar índices otimizados para consultas frequentes
CREATE INDEX idx_[dominio]_view_[campo1] ON projections.[dominio]_view ([campo1]);
CREATE INDEX idx_[dominio]_view_[campo_data] ON projections.[dominio]_view ([campo_data]);
CREATE INDEX idx_[dominio]_view_search_text ON projections.[dominio]_view USING gin(to_tsvector('portuguese', search_text));
CREATE INDEX idx_[dominio]_view_tags ON projections.[dominio]_tags (tag);
CREATE INDEX idx_[dominio]_view_composite ON projections.[dominio]_view ([campo1], [campo_data], created_at);
```

#### **✅ Checklist de Query Models:**
- [ ] **Campos desnormalizados** para consultas frequentes
- [ ] **Índices otimizados** criados
- [ ] **Full-text search** configurado
- [ ] **Tags/categorias** implementadas
- [ ] **Campos de controle** (timestamps, version) presentes

---

### **🔄 2. IMPLEMENTAÇÃO DE PROJECTION HANDLERS**

#### **📡 Projection Handler Base:**
```java
@Component
@Slf4j
@Transactional("readTransactionManager")
public class [Dominio]ProjectionHandler extends AbstractProjectionHandler<[Dominio][Acao]Event> {
    
    private final [Dominio]QueryRepository queryRepository;
    private final [Dominio]ProjectionService projectionService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // ========== CONSTRUTOR ==========
    public [Dominio]ProjectionHandler(
            [Dominio]QueryRepository queryRepository,
            [Dominio]ProjectionService projectionService,
            RedisTemplate<String, Object> redisTemplate) {
        this.queryRepository = queryRepository;
        this.projectionService = projectionService;
        this.redisTemplate = redisTemplate;
    }
    
    // ========== PROCESSAMENTO PRINCIPAL ==========
    @Override
    protected void doHandle([Dominio][Acao]Event event) {
        log.debug("Processando evento para projeção: {} - Agregado: {}", 
                 event.getClass().getSimpleName(), event.getAggregateId());
        
        try {
            // 1. Verificar se já foi processado (idempotência)
            if (isAlreadyProcessed(event)) {
                log.debug("Evento já processado: {}", event);
                return;
            }
            
            // 2. Processar baseado no tipo de evento
            processEventByType(event);
            
            // 3. Invalidar cache relacionado
            invalidateRelatedCache(event);
            
            // 4. Marcar como processado
            markAsProcessed(event);
            
        } catch (Exception e) {
            log.error("Erro ao processar evento na projeção: {}", event, e);
            throw new ProjectionException(
                getProjectionName(),
                event.getClass().getSimpleName(),
                "Erro no processamento da projeção",
                e
            );
        }
    }
    
    // ========== PROCESSAMENTO POR TIPO DE EVENTO ==========
    private void processEventByType([Dominio][Acao]Event event) {
        switch (event.getClass().getSimpleName()) {
            case "[Dominio]CriadoEvent":
                handleCriado(([Dominio]CriadoEvent) event);
                break;
            case "[Dominio]AtualizadoEvent":
                handleAtualizado(([Dominio]AtualizadoEvent) event);
                break;
            case "[Dominio]RemovidoEvent":
                handleRemovido(([Dominio]RemovidoEvent) event);
                break;
            default:
                log.warn("Tipo de evento não suportado: {}", event.getClass().getSimpleName());
        }
    }
    
    private void handleCriado([Dominio]CriadoEvent event) {
        [Dominio]QueryModel queryModel = [Dominio]QueryModel.builder()
            .id(UUID.fromString(event.getAggregateId()))
            .[campo1](event.get[Campo1]())
            .[campo2](event.get[Campo2]())
            .lastEventId(event.getEventId())
            .build();
            
        queryRepository.save(queryModel);
        log.debug("Projeção criada para agregado: {}", event.getAggregateId());
    }
    
    private void handleAtualizado([Dominio]AtualizadoEvent event) {
        Optional<[Dominio]QueryModel> optionalModel = queryRepository.findById(
            UUID.fromString(event.getAggregateId())
        );
        
        if (optionalModel.isPresent()) {
            [Dominio]QueryModel queryModel = optionalModel.get();
            
            // Atualizar campos modificados
            if (event.get[Campo1]() != null) {
                queryModel.set[Campo1](event.get[Campo1]());
            }
            if (event.get[Campo2]() != null) {
                queryModel.set[Campo2](event.get[Campo2]());
            }
            
            queryModel.setLastEventId(event.getEventId());
            queryRepository.save(queryModel);
            
            log.debug("Projeção atualizada para agregado: {}", event.getAggregateId());
        } else {
            log.warn("Projeção não encontrada para atualização: {}", event.getAggregateId());
        }
    }
    
    private void handleRemovido([Dominio]RemovidoEvent event) {
        queryRepository.deleteById(UUID.fromString(event.getAggregateId()));
        log.debug("Projeção removida para agregado: {}", event.getAggregateId());
    }
    
    // ========== CONTROLE DE IDEMPOTÊNCIA ==========
    private boolean isAlreadyProcessed([Dominio][Acao]Event event) {
        String key = String.format("projection:[dominio]:%s:last_event", event.getAggregateId());
        Long lastProcessedEventId = (Long) redisTemplate.opsForValue().get(key);
        
        return lastProcessedEventId != null && lastProcessedEventId >= event.getEventId();
    }
    
    private void markAsProcessed([Dominio][Acao]Event event) {
        String key = String.format("projection:[dominio]:%s:last_event", event.getAggregateId());
        redisTemplate.opsForValue().set(key, event.getEventId(), Duration.ofDays(7));
    }
    
    // ========== CACHE MANAGEMENT ==========
    private void invalidateRelatedCache([Dominio][Acao]Event event) {
        // Invalidar caches relacionados
        String pattern = String.format("query:[dominio]:*:%s:*", event.getAggregateId());
        Set<String> keys = redisTemplate.keys(pattern);
        
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Cache invalidado para {} chaves relacionadas ao agregado: {}", 
                     keys.size(), event.getAggregateId());
        }
    }
    
    // ========== CONFIGURAÇÃO ==========
    @Override
    public Class<[Dominio][Acao]Event> getEventType() {
        return [Dominio][Acao]Event.class;
    }
    
    @Override
    public String getProjectionName() {
        return "[Dominio]Projection";
    }
    
    @Override
    public boolean isAsync() {
        return true;
    }
    
    @Override
    public int getOrder() {
        return 50; // Prioridade média para projeções
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 30;
    }
    
    @Override
    public boolean supports([Dominio][Acao]Event event) {
        return event != null && 
               event.getAggregateId() != null && 
               event.getEventId() != null;
    }
}
```

#### **✅ Checklist de Projection Handlers:**
- [ ] **Idempotência** implementada
- [ ] **Processamento por tipo** de evento
- [ ] **Cache invalidation** configurado
- [ ] **Tratamento de erros** específico
- [ ] **Logs estruturados** implementados

---

### **🗃️ 3. QUERY REPOSITORY**

#### **📊 Repository com Consultas Otimizadas:**
```java
@Repository
public interface [Dominio]QueryRepository extends JpaRepository<[Dominio]QueryModel, UUID>, 
                                                 JpaSpecificationExecutor<[Dominio]QueryModel> {
    
    // ========== CONSULTAS BÁSICAS ==========
    
    /**
     * Busca por campo específico com ordenação.
     */
    List<[Dominio]QueryModel> findBy[Campo1]OrderByCreatedAtDesc(String [campo1]);
    
    /**
     * Busca por múltiplos campos.
     */
    Page<[Dominio]QueryModel> findBy[Campo1]And[Campo2](String [campo1], String [campo2], Pageable pageable);
    
    /**
     * Busca por período.
     */
    @Query("SELECT d FROM [Dominio]QueryModel d WHERE d.createdAt BETWEEN :inicio AND :fim ORDER BY d.createdAt DESC")
    Page<[Dominio]QueryModel> findByPeriodo(@Param("inicio") Instant inicio, 
                                           @Param("fim") Instant fim, 
                                           Pageable pageable);
    
    // ========== FULL-TEXT SEARCH ==========
    
    /**
     * Busca textual usando PostgreSQL full-text search.
     */
    @Query(value = """
        SELECT * FROM projections.[dominio]_view d 
        WHERE to_tsvector('portuguese', d.search_text) @@ plainto_tsquery('portuguese', :termo)
        ORDER BY ts_rank(to_tsvector('portuguese', d.search_text), plainto_tsquery('portuguese', :termo)) DESC
        """, nativeQuery = true)
    List<[Dominio]QueryModel> findByFullTextSearch(@Param("termo") String termo);
    
    /**
     * Busca textual com paginação.
     */
    @Query(value = """
        SELECT * FROM projections.[dominio]_view d 
        WHERE to_tsvector('portuguese', d.search_text) @@ plainto_tsquery('portuguese', :termo)
        ORDER BY ts_rank(to_tsvector('portuguese', d.search_text), plainto_tsquery('portuguese', :termo)) DESC
        LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
        """, 
        countQuery = """
        SELECT COUNT(*) FROM projections.[dominio]_view d 
        WHERE to_tsvector('portuguese', d.search_text) @@ plainto_tsquery('portuguese', :termo)
        """, nativeQuery = true)
    Page<[Dominio]QueryModel> findByFullTextSearchPaged(@Param("termo") String termo, Pageable pageable);
    
    // ========== CONSULTAS POR TAGS ==========
    
    /**
     * Busca por tag específica.
     */
    @Query("SELECT d FROM [Dominio]QueryModel d JOIN d.tags t WHERE t = :tag ORDER BY d.createdAt DESC")
    List<[Dominio]QueryModel> findByTag(@Param("tag") String tag);
    
    /**
     * Busca por qualquer uma das tags.
     */
    @Query("SELECT DISTINCT d FROM [Dominio]QueryModel d JOIN d.tags t WHERE t IN :tags ORDER BY d.createdAt DESC")
    List<[Dominio]QueryModel> findByAnyTag(@Param("tags") List<String> tags);
    
    // ========== CONSULTAS AGREGADAS ==========
    
    /**
     * Conta por campo específico.
     */
    @Query("SELECT d.[campo1], COUNT(d) FROM [Dominio]QueryModel d GROUP BY d.[campo1] ORDER BY COUNT(d) DESC")
    List<Object[]> countBy[Campo1]();
    
    /**
     * Estatísticas por período.
     */
    @Query(value = """
        SELECT 
            DATE_TRUNC('day', created_at) as dia,
            COUNT(*) as total,
            AVG([campo_numerico]) as media
        FROM projections.[dominio]_view 
        WHERE created_at BETWEEN :inicio AND :fim
        GROUP BY DATE_TRUNC('day', created_at)
        ORDER BY dia DESC
        """, nativeQuery = true)
    List<Object[]> getEstatisticasPorDia(@Param("inicio") Instant inicio, @Param("fim") Instant fim);
    
    // ========== CONSULTAS DE PERFORMANCE ==========
    
    /**
     * Busca otimizada com índice composto.
     */
    @Query("SELECT d FROM [Dominio]QueryModel d WHERE d.[campo1] = :campo1 AND d.createdAt >= :desde ORDER BY d.createdAt DESC")
    List<[Dominio]QueryModel> findRecent(@Param("campo1") String campo1, @Param("desde") Instant desde);
    
    /**
     * Busca com filtros múltiplos otimizada.
     */
    @Query("""
        SELECT d FROM [Dominio]QueryModel d 
        WHERE (:campo1 IS NULL OR d.[campo1] = :campo1)
        AND (:campo2 IS NULL OR d.[campo2] = :campo2)
        AND (:dataInicio IS NULL OR d.createdAt >= :dataInicio)
        AND (:dataFim IS NULL OR d.createdAt <= :dataFim)
        ORDER BY d.createdAt DESC
        """)
    Page<[Dominio]QueryModel> findWithFilters(@Param("campo1") String campo1,
                                             @Param("campo2") String campo2,
                                             @Param("dataInicio") Instant dataInicio,
                                             @Param("dataFim") Instant dataFim,
                                             Pageable pageable);
    
    // ========== CONSULTAS DE MANUTENÇÃO ==========
    
    /**
     * Busca registros desatualizados.
     */
    @Query("SELECT d FROM [Dominio]QueryModel d WHERE d.lastEventId < :eventId")
    List<[Dominio]QueryModel> findOutdated(@Param("eventId") Long eventId);
    
    /**
     * Remove registros antigos.
     */
    @Modifying
    @Query("DELETE FROM [Dominio]QueryModel d WHERE d.createdAt < :cutoffDate")
    int deleteOlderThan(@Param("cutoffDate") Instant cutoffDate);
}
```

#### **✅ Checklist de Query Repository:**
- [ ] **Consultas básicas** implementadas
- [ ] **Full-text search** configurado
- [ ] **Consultas por tags** funcionais
- [ ] **Consultas agregadas** otimizadas
- [ ] **Consultas de manutenção** implementadas

---

### **🔍 4. QUERY SERVICE**

#### **📊 Service com Cache e Otimizações:**
```java
@Service
@Transactional(value = "readTransactionManager", readOnly = true)
@Slf4j
public class [Dominio]QueryService {
    
    private final [Dominio]QueryRepository queryRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MeterRegistry meterRegistry;
    
    // ========== CONSTRUTOR ==========
    public [Dominio]QueryService(
            [Dominio]QueryRepository queryRepository,
            RedisTemplate<String, Object> redisTemplate,
            MeterRegistry meterRegistry) {
        this.queryRepository = queryRepository;
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
    }
    
    // ========== CONSULTAS BÁSICAS ==========
    
    @Cacheable(value = "dominio-query", key = "#id")
    public Optional<[Dominio]DetailView> findById(UUID id) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            Optional<[Dominio]QueryModel> result = queryRepository.findById(id);
            recordQueryMetrics("findById", result.isPresent());
            
            return result.map(this::toDetailView);
            
        } finally {
            sample.stop(Timer.builder("query.execution.time")
                .tag("operation", "findById")
                .register(meterRegistry));
        }
    }
    
    @Cacheable(value = "dominio-list", key = "#campo1 + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<[Dominio]ListView> findBy[Campo1](String [campo1], Pageable pageable) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            Page<[Dominio]QueryModel> result = queryRepository.findBy[Campo1]And[Campo2]([campo1], null, pageable);
            recordQueryMetrics("findBy[Campo1]", !result.isEmpty());
            
            return result.map(this::toListView);
            
        } finally {
            sample.stop(Timer.builder("query.execution.time")
                .tag("operation", "findBy[Campo1]")
                .register(meterRegistry));
        }
    }
    
    // ========== BUSCA TEXTUAL ==========
    
    @Cacheable(value = "dominio-search", key = "#termo + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<[Dominio]ListView> search(String termo, Pageable pageable) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            Page<[Dominio]QueryModel> result = queryRepository.findByFullTextSearchPaged(termo, pageable);
            recordQueryMetrics("search", !result.isEmpty());
            
            return result.map(this::toListView);
            
        } finally {
            sample.stop(Timer.builder("query.execution.time")
                .tag("operation", "search")
                .register(meterRegistry));
        }
    }
    
    // ========== CONSULTAS COMPLEXAS ==========
    
    public Page<[Dominio]ListView> findWithFilters([Dominio]Filter filter, Pageable pageable) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // Usar cache apenas para filtros simples
            String cacheKey = filter.toCacheKey() + "_" + pageable.getPageNumber() + "_" + pageable.getPageSize();
            
            if (filter.isSimple()) {
                Page<[Dominio]ListView> cached = getCachedResult(cacheKey);
                if (cached != null) {
                    recordQueryMetrics("findWithFilters", true, true);
                    return cached;
                }
            }
            
            Page<[Dominio]QueryModel> result = queryRepository.findWithFilters(
                filter.get[Campo1](),
                filter.get[Campo2](),
                filter.getDataInicio(),
                filter.getDataFim(),
                pageable
            );
            
            Page<[Dominio]ListView> viewResult = result.map(this::toListView);
            
            if (filter.isSimple()) {
                cacheResult(cacheKey, viewResult);
            }
            
            recordQueryMetrics("findWithFilters", !result.isEmpty(), false);
            return viewResult;
            
        } finally {
            sample.stop(Timer.builder("query.execution.time")
                .tag("operation", "findWithFilters")
                .register(meterRegistry));
        }
    }
    
    // ========== CONSULTAS AGREGADAS ==========
    
    @Cacheable(value = "dominio-stats", key = "'dashboard'")
    public [Dominio]DashboardView getDashboard() {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // Executar múltiplas consultas em paralelo
            CompletableFuture<Long> totalFuture = CompletableFuture.supplyAsync(() -> 
                queryRepository.count());
                
            CompletableFuture<List<Object[]>> statsFuture = CompletableFuture.supplyAsync(() -> 
                queryRepository.countBy[Campo1]());
                
            CompletableFuture<List<Object[]>> recentFuture = CompletableFuture.supplyAsync(() -> 
                queryRepository.getEstatisticasPorDia(
                    Instant.now().minus(30, ChronoUnit.DAYS),
                    Instant.now()
                ));
            
            // Aguardar todos os resultados
            CompletableFuture.allOf(totalFuture, statsFuture, recentFuture).join();
            
            [Dominio]DashboardView dashboard = [Dominio]DashboardView.builder()
                .total(totalFuture.get())
                .estatisticasPor[Campo1](processStats(statsFuture.get()))
                .estatisticasRecentes(processRecentStats(recentFuture.get()))
                .ultimaAtualizacao(Instant.now())
                .build();
                
            recordQueryMetrics("getDashboard", true);
            return dashboard;
            
        } catch (Exception e) {
            log.error("Erro ao gerar dashboard", e);
            recordQueryMetrics("getDashboard", false);
            throw new QueryException("Erro ao gerar dashboard", e);
        } finally {
            sample.stop(Timer.builder("query.execution.time")
                .tag("operation", "getDashboard")
                .register(meterRegistry));
        }
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    private [Dominio]DetailView toDetailView([Dominio]QueryModel model) {
        return [Dominio]DetailView.builder()
            .id(model.getId())
            .[campo1](model.get[Campo1]())
            .[campo2](model.get[Campo2]())
            .tags(model.getTags())
            .createdAt(model.getCreatedAt())
            .updatedAt(model.getUpdatedAt())
            .build();
    }
    
    private [Dominio]ListView toListView([Dominio]QueryModel model) {
        return [Dominio]ListView.builder()
            .id(model.getId())
            .[campo1](model.get[Campo1]())
            .[campo2](model.get[Campo2]())
            .createdAt(model.getCreatedAt())
            .build();
    }
    
    @SuppressWarnings("unchecked")
    private Page<[Dominio]ListView> getCachedResult(String cacheKey) {
        try {
            return (Page<[Dominio]ListView>) redisTemplate.opsForValue().get("query:cache:" + cacheKey);
        } catch (Exception e) {
            log.warn("Erro ao recuperar cache: {}", cacheKey, e);
            return null;
        }
    }
    
    private void cacheResult(String cacheKey, Page<[Dominio]ListView> result) {
        try {
            redisTemplate.opsForValue().set("query:cache:" + cacheKey, result, Duration.ofMinutes(15));
        } catch (Exception e) {
            log.warn("Erro ao armazenar cache: {}", cacheKey, e);
        }
    }
    
    private void recordQueryMetrics(String operation, boolean success) {
        recordQueryMetrics(operation, success, false);
    }
    
    private void recordQueryMetrics(String operation, boolean success, boolean fromCache) {
        meterRegistry.counter("query.executed",
            "operation", operation,
            "status", success ? "success" : "failure",
            "source", fromCache ? "cache" : "database")
            .increment();
    }
    
    private Map<String, Long> processStats(List<Object[]> stats) {
        return stats.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1]
            ));
    }
    
    private List<[Dominio]DailyStats> processRecentStats(List<Object[]> stats) {
        return stats.stream()
            .map(row -> [Dominio]DailyStats.builder()
                .dia((LocalDate) row[0])
                .total((Long) row[1])
                .media((BigDecimal) row[2])
                .build())
            .collect(Collectors.toList());
    }
}
```

#### **✅ Checklist de Query Service:**
- [ ] **Cache** implementado adequadamente
- [ ] **Métricas** de performance coletadas
- [ ] **Consultas paralelas** para dashboard
- [ ] **Tratamento de erros** específico
- [ ] **Conversão para DTOs** otimizada

---

### **📊 5. VIEWS E DTOS**

#### **🎯 DTOs de Resposta:**
```java
// DTO para listagem
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class [Dominio]ListView {
    private UUID id;
    private String [campo1];
    private String [campo2];
    private Instant createdAt;
    
    // Campos calculados
    public String getDisplayName() {
        return String.format("%s - %s", [campo1], [campo2]);
    }
}

// DTO para detalhes
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class [Dominio]DetailView {
    private UUID id;
    private String [campo1];
    private String [campo2];
    private BigDecimal [campoNumerico];
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Métodos de conveniência
    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }
}

// DTO para dashboard
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class [Dominio]DashboardView {
    private Long total;
    private Map<String, Long> estatisticasPor[Campo1];
    private List<[Dominio]DailyStats> estatisticasRecentes;
    private Instant ultimaAtualizacao;
}

// DTO para filtros
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class [Dominio]Filter {
    private String [campo1];
    private String [campo2];
    private Instant dataInicio;
    private Instant dataFim;
    private List<String> tags;
    
    public boolean isSimple() {
        return tags == null || tags.isEmpty();
    }
    
    public String toCacheKey() {
        return String.format("%s_%s_%s_%s", 
            [campo1], [campo2], dataInicio, dataFim);
    }
}
```

#### **✅ Checklist de DTOs:**
- [ ] **DTOs específicos** para cada caso de uso
- [ ] **Métodos de conveniência** implementados
- [ ] **Validações** de entrada quando necessário
- [ ] **Serialização JSON** funcionando
- [ ] **Documentação** adequada

---

## ✅ **CHECKPOINT DE VALIDAÇÃO**

### **🎯 Critérios de Aprovação:**

#### **🗃️ Query Models:**
- [ ] **Campos desnormalizados** adequadamente
- [ ] **Índices otimizados** criados
- [ ] **Full-text search** funcionando
- [ ] **Tags/categorização** implementada
- [ ] **Performance** adequada para consultas

#### **🔄 Projection Handlers:**
- [ ] **Idempotência** garantida
- [ ] **Processamento por tipo** de evento
- [ ] **Cache invalidation** funcionando
- [ ] **Tratamento de erros** robusto
- [ ] **Métricas** sendo coletadas

#### **📊 Query Services:**
- [ ] **Cache** implementado e funcionando
- [ ] **Consultas otimizadas** com boa performance
- [ ] **Conversão para DTOs** eficiente
- [ ] **Tratamento de erros** adequado
- [ ] **Métricas** de performance coletadas

#### **🧪 Testes:**
- [ ] **Testes unitários** dos projection handlers
- [ ] **Testes de integração** com banco de dados
- [ ] **Testes de performance** das consultas
- [ ] **Testes de cache** funcionais
- [ ] **Coverage** adequado (>80%)

---

## 🚨 **PONTOS DE ATENÇÃO**

### **⚠️ Armadilhas Comuns:**

#### **🚫 Query Models Normalizados:**
```java
// ❌ EVITAR: Modelo normalizado como no write side
public class [Dominio]QueryModel {
    private UUID id;
    private String campo1; // Apenas campos básicos
    
    @OneToMany // Relacionamentos JPA
    private List<[Relacionado]> relacionados;
}

// ✅ PREFERIR: Modelo desnormalizado
public class [Dominio]QueryModel {
    private UUID id;
    private String campo1;
    private String campo2Desnormalizado; // Dados de outras entidades
    private String searchText; // Campo para busca
    private List<String> tags; // Categorização
}
```

#### **🚫 Falta de Idempotência:**
```java
// ❌ EVITAR: Processamento sem verificação
@Override
public void handle(Event event) {
    // Sempre processa, mesmo se já foi processado
    updateProjection(event);
}

// ✅ PREFERIR: Processamento idempotente
@Override
public void handle(Event event) {
    if (!isAlreadyProcessed(event)) {
        updateProjection(event);
        markAsProcessed(event);
    }
}
```

#### **🚫 Cache Inadequado:**
```java
// ❌ EVITAR: Cache sem invalidação
@Cacheable("queries")
public List<Model> findAll() {
    return repository.findAll(); // Cache nunca expira
}

// ✅ PREFERIR: Cache com invalidação
@Cacheable(value = "queries", key = "#filter.cacheKey()")
public Page<Model> findWithFilters(Filter filter, Pageable pageable) {
    // Cache com chave específica e TTL
}
```

### **✅ Boas Práticas:**

#### **🎯 Design de Projeções:**
- **Sempre** desnormalizar dados para consultas frequentes
- **Sempre** implementar idempotência
- **Sempre** usar índices otimizados
- **Sempre** invalidar cache relacionado

#### **📊 Performance:**
- **Sempre** usar paginação para listas grandes
- **Sempre** implementar cache para consultas frequentes
- **Sempre** monitorar performance das consultas
- **Sempre** otimizar consultas N+1

---

## 🔄 **PRÓXIMOS PASSOS**

### **✅ Após Aprovação do Checkpoint:**
1. **[Etapa 06 - Configuração de DataSources](./06-configuracao-datasources.md)**
2. Configurar conexões otimizadas
3. Implementar health checks
4. Configurar monitoramento de banco

### **📋 Preparação para Próxima Etapa:**
- [ ] **DataSource patterns** estudados
- [ ] **Connection pooling** compreendido
- [ ] **Database monitoring** revisado
- [ ] **Testes de projeção** passando

---

## 📚 **RECURSOS DE APOIO**

### **📖 Documentação de Referência:**
- **[Projections](../07-projections-README.md)**: Guia completo de projeções
- **[CQRS](../03-cqrs-README.md)**: Fundamentos do query side
- **Código Existente**: `SinistroProjectionHandler` como referência

### **🛠️ Ferramentas de Otimização:**
- **pgAdmin**: Análise de queries PostgreSQL
- **Redis CLI**: Debug de cache
- **JProfiler**: Profiling de performance
- **Spring Boot Actuator**: Métricas de aplicação

### **🧪 Exemplos de Teste:**
- **ProjectionHandlerTest**: Testes unitários
- **QueryServiceTest**: Testes de serviço
- **QueryPerformanceTest**: Testes de performance

---

**📋 Checklist Total:** 80+ itens de validação  
**⏱️ Tempo Médio:** 4-6 horas  
**🎯 Resultado:** Query side completo e otimizado  
**✅ Próxima Etapa:** Configuração de DataSources