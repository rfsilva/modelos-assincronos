# 📖 CAPÍTULO 03: CQRS - PARTE 3
## Query Side - Implementação e Otimização

### 🎯 **OBJETIVOS DESTA PARTE**
- Implementar Query Side otimizado
- Configurar índices e consultas eficientes
- Implementar cache inteligente
- Criar múltiplas projeções especializadas

---

## 📊 **QUERY REPOSITORIES OTIMIZADOS**

### **🔍 Repository com Consultas Especializadas**

```java
// Localização: query/repository/SinistroQueryRepository.java
@Repository
public interface SinistroQueryRepository extends JpaRepository<SinistroQueryModel, UUID>, 
                                               JpaSpecificationExecutor<SinistroQueryModel> {
    
    // === CONSULTAS BÁSICAS ===
    Optional<SinistroQueryModel> findByProtocolo(String protocolo);
    List<SinistroQueryModel> findByCpfSeguradoOrderByDataAberturaDesc(String cpfSegurado);
    List<SinistroQueryModel> findByPlacaOrderByDataAberturaDesc(String placa);
    
    // === CONSULTAS OTIMIZADAS COM ÍNDICES ===
    
    @Query(value = """
        SELECT * FROM projections.sinistro_view s 
        WHERE s.status = :status 
        AND s.data_abertura >= :dataInicio
        ORDER BY s.data_abertura DESC
        LIMIT :limite
        """, nativeQuery = true)
    List<SinistroQueryModel> findSinistrosRecentesPorStatus(
        @Param("status") String status,
        @Param("dataInicio") Instant dataInicio,
        @Param("limite") int limite
    );
    
    // === FULL-TEXT SEARCH OTIMIZADO ===
    
    @Query(value = """
        SELECT *, ts_rank(search_vector, plainto_tsquery('portuguese', :termo)) as rank
        FROM projections.sinistro_view s 
        WHERE search_vector @@ plainto_tsquery('portuguese', :termo)
        ORDER BY rank DESC, data_abertura DESC
        LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
        """, nativeQuery = true)
    Page<SinistroQueryModel> findByFullTextSearchRanked(@Param("termo") String termo, Pageable pageable);
    
    // === CONSULTAS AGREGADAS ===
    
    @Query("""
        SELECT new com.seguradora.hibrida.query.dto.StatusSummary(
            s.status, 
            COUNT(s), 
            AVG(s.valorEstimado),
            MIN(s.dataAbertura),
            MAX(s.dataAbertura)
        )
        FROM SinistroQueryModel s 
        WHERE s.dataAbertura >= :desde
        GROUP BY s.status
        ORDER BY COUNT(s) DESC
        """)
    List<StatusSummary> getResumoStatusSinistros(@Param("desde") Instant desde);
    
    // === CONSULTAS GEOESPACIAIS ===
    
    @Query(value = """
        SELECT *, 
               ST_Distance(
                   ST_Point(longitude, latitude), 
                   ST_Point(:longitude, :latitude)
               ) as distancia
        FROM projections.sinistro_view s 
        WHERE ST_DWithin(
            ST_Point(longitude, latitude), 
            ST_Point(:longitude, :latitude), 
            :raioKm * 1000
        )
        ORDER BY distancia
        """, nativeQuery = true)
    List<SinistroQueryModel> findSinistrosProximos(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("raioKm") double raioKm
    );
    
    // === CONSULTAS DE PERFORMANCE ===
    
    @Query(value = """
        SELECT 
            DATE_TRUNC('hour', data_abertura) as hora,
            COUNT(*) as total,
            AVG(valor_estimado) as valor_medio
        FROM projections.sinistro_view 
        WHERE data_abertura >= NOW() - INTERVAL '24 hours'
        GROUP BY DATE_TRUNC('hour', data_abertura)
        ORDER BY hora
        """, nativeQuery = true)
    List<Object[]> getEstatisticasHorarias();
}
```

### **🗂️ Índices Otimizados**

```sql
-- Localização: db/migration-projections/V2__Create_Optimized_Indexes.sql

-- Índices básicos
CREATE INDEX CONCURRENTLY idx_sinistro_view_protocolo ON projections.sinistro_view (protocolo);
CREATE INDEX CONCURRENTLY idx_sinistro_view_cpf_segurado ON projections.sinistro_view (cpf_segurado);
CREATE INDEX CONCURRENTLY idx_sinistro_view_placa ON projections.sinistro_view (placa);

-- Índices compostos para consultas frequentes
CREATE INDEX CONCURRENTLY idx_sinistro_view_status_data 
    ON projections.sinistro_view (status, data_abertura DESC);

CREATE INDEX CONCURRENTLY idx_sinistro_view_operador_status 
    ON projections.sinistro_view (operador_responsavel, status) 
    WHERE operador_responsavel IS NOT NULL;

-- Índice para full-text search
ALTER TABLE projections.sinistro_view 
ADD COLUMN search_vector tsvector;

CREATE INDEX CONCURRENTLY idx_sinistro_view_search 
    ON projections.sinistro_view USING gin(search_vector);

-- Trigger para manter search_vector atualizado
CREATE OR REPLACE FUNCTION update_sinistro_search_vector()
RETURNS trigger AS $$
BEGIN
    NEW.search_vector := 
        setweight(to_tsvector('portuguese', COALESCE(NEW.protocolo, '')), 'A') ||
        setweight(to_tsvector('portuguese', COALESCE(NEW.nome_segurado, '')), 'B') ||
        setweight(to_tsvector('portuguese', COALESCE(NEW.descricao, '')), 'C') ||
        setweight(to_tsvector('portuguese', COALESCE(NEW.placa, '')), 'B') ||
        setweight(to_tsvector('portuguese', COALESCE(NEW.endereco_ocorrencia, '')), 'D');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_search_vector
    BEFORE INSERT OR UPDATE ON projections.sinistro_view
    FOR EACH ROW EXECUTE FUNCTION update_sinistro_search_vector();

-- Índices geoespaciais (se necessário)
CREATE INDEX CONCURRENTLY idx_sinistro_view_location 
    ON projections.sinistro_view USING gist(ST_Point(longitude, latitude))
    WHERE longitude IS NOT NULL AND latitude IS NOT NULL;

-- Índices parciais para consultas específicas
CREATE INDEX CONCURRENTLY idx_sinistro_view_abertos 
    ON projections.sinistro_view (data_abertura DESC) 
    WHERE status IN ('ABERTO', 'EM_ANALISE');

CREATE INDEX CONCURRENTLY idx_sinistro_view_alto_valor 
    ON projections.sinistro_view (valor_estimado DESC, data_abertura DESC) 
    WHERE valor_estimado > 50000;
```

---

## 🚀 **CACHE INTELIGENTE**

### **⚡ Configuração de Cache**

```java
// Localização: query/config/QueryCacheConfiguration.java
@Configuration
@EnableCaching
public class QueryCacheConfiguration {
    
    @Bean
    public CacheManager queryCacheManager(RedisConnectionFactory connectionFactory) {
        
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        // Configurações específicas por cache
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Cache de sinistros individuais - TTL longo
        cacheConfigurations.put("sinistros", defaultConfig
            .entryTtl(Duration.ofHours(2)));
        
        // Cache de listas - TTL curto
        cacheConfigurations.put("sinistros-lista", defaultConfig
            .entryTtl(Duration.ofMinutes(5)));
        
        // Cache de dashboard - TTL muito curto
        cacheConfigurations.put("dashboard", defaultConfig
            .entryTtl(Duration.ofMinutes(2)));
        
        // Cache de estatísticas - TTL médio
        cacheConfigurations.put("estatisticas", defaultConfig
            .entryTtl(Duration.ofMinutes(15)));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
    
    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Erro ao buscar no cache {}: {}", cache.getName(), exception.getMessage());
                // Continuar sem cache em caso de erro
            }
            
            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Erro ao salvar no cache {}: {}", cache.getName(), exception.getMessage());
                // Continuar sem cache em caso de erro
            }
        };
    }
}
```

### **🎯 Service com Cache Inteligente**

```java
// Implementação com cache otimizado
@Service
@Transactional(readOnly = true)
public class SinistroQueryServiceImpl implements SinistroQueryService {
    
    private final SinistroQueryRepository repository;
    private final CacheManager cacheManager;
    
    @Override
    @Cacheable(value = "sinistros", key = "#id", unless = "#result.isEmpty()")
    public Optional<SinistroDetailView> buscarPorId(UUID id) {
        return repository.findById(id).map(this::toDetailView);
    }
    
    @Override
    @Cacheable(value = "sinistros", key = "#protocolo", unless = "#result.isEmpty()")
    public Optional<SinistroDetailView> buscarPorProtocolo(String protocolo) {
        return repository.findByProtocolo(protocolo).map(this::toDetailView);
    }
    
    @Override
    @Cacheable(value = "sinistros-lista", 
               key = "#filter.hashCode() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize",
               condition = "#filter.isCacheable()")
    public Page<SinistroListView> listar(SinistroFilter filter, Pageable pageable) {
        
        Specification<SinistroQueryModel> spec = buildSpecification(filter);
        return repository.findAll(spec, pageable).map(this::toListView);
    }
    
    @Override
    @Cacheable(value = "dashboard", key = "'sinistros'")
    public DashboardView obterDashboard() {
        
        Instant umMesAtras = Instant.now().minus(30, ChronoUnit.DAYS);
        
        // Usar consultas otimizadas
        List<StatusSummary> resumoStatus = repository.getResumoStatusSinistros(umMesAtras);
        List<Object[]> estatisticasHorarias = repository.getEstatisticasHorarias();
        
        return DashboardView.builder()
            .resumoStatus(resumoStatus)
            .estatisticasHorarias(convertToHourlyStats(estatisticasHorarias))
            .ultimaAtualizacao(Instant.now())
            .build();
    }
    
    // Cache invalidation quando dados mudam
    @CacheEvict(value = {"sinistros", "sinistros-lista", "dashboard"}, allEntries = true)
    public void invalidarCachesSinistro(String sinistroId) {
        log.debug("Cache invalidado para sinistro: {}", sinistroId);
    }
    
    // Cache warming para dados frequentemente acessados
    @Scheduled(fixedRate = 300000) // A cada 5 minutos
    public void aquecerCache() {
        
        try {
            // Pré-carregar dashboard
            obterDashboard();
            
            // Pré-carregar sinistros recentes
            Instant ontemAgo = Instant.now().minus(1, ChronoUnit.DAYS);
            List<SinistroQueryModel> recentes = repository.findSinistrosRecentesPorStatus(
                "ABERTO", ontemAgo, 50
            );
            
            // Carregar detalhes dos mais acessados
            recentes.stream()
                .limit(10)
                .forEach(s -> buscarPorId(s.getId()));
                
        } catch (Exception e) {
            log.warn("Erro no aquecimento de cache: {}", e.getMessage());
        }
    }
}
```

---

## 📋 **MÚLTIPLAS PROJEÇÕES**

### **🎯 Projeções Especializadas**

```java
// Projeção para dashboard executivo
@Entity
@Table(name = "dashboard_executivo_view", schema = "projections")
public class DashboardExecutivoProjection {
    
    @Id
    private String periodo; // YYYY-MM-DD
    
    private Long totalSinistros;
    private Long sinistrosAbertos;
    private Long sinistrosFechados;
    private BigDecimal valorTotalEstimado;
    private BigDecimal valorMedioSinistro;
    private Double tempoMedioResolucao; // em horas
    
    // Distribuição por tipo
    @Column(columnDefinition = "jsonb")
    private Map<String, Long> distribuicaoPorTipo;
    
    // Distribuição por região
    @Column(columnDefinition = "jsonb")
    private Map<String, Long> distribuicaoPorRegiao;
    
    // Top operadores
    @Column(columnDefinition = "jsonb")
    private Map<String, Long> topOperadores;
    
    private Instant ultimaAtualizacao;
    
    // Getters e setters...
}

// Projeção para análise de fraude
@Entity
@Table(name = "analise_fraude_view", schema = "projections")
public class AnaliseFraudeProjection {
    
    @Id
    private UUID sinistroId;
    
    private String cpfSegurado;
    private String placa;
    private Integer scoreFraude; // 0-100
    private String nivelRisco; // BAIXO, MEDIO, ALTO
    
    // Indicadores de risco
    private Boolean multiplasOcorrenciasMesmoLocal;
    private Boolean valorAcimaMedia;
    private Boolean ocorrenciaForaHorarioComum;
    private Boolean seguradoComHistoricoSuspeito;
    private Boolean veiculoComMultiplasOcorrencias;
    
    // Dados para análise
    private Integer quantidadeSinistrosUltimos12Meses;
    private BigDecimal valorTotalSinistrosAnteriores;
    private LocalDateTime ultimaOcorrenciaAnterior;
    
    private Instant dataAnalise;
    
    // Getters e setters...
}

// Handler para múltiplas projeções
@Component
public class MultiProjectionSinistroHandler extends AbstractProjectionHandler<SinistroEvent> {
    
    private final DashboardExecutivoRepository dashboardRepo;
    private final AnaliseFraudeRepository fraudeRepo;
    private final SinistroQueryRepository sinistroRepo;
    
    @Override
    public void doHandle(SinistroEvent event) {
        
        // Atualizar projeção principal
        atualizarProjecaoPrincipal(event);
        
        // Atualizar dashboard executivo
        atualizarDashboardExecutivo(event);
        
        // Atualizar análise de fraude
        atualizarAnaliseFraude(event);
    }
    
    private void atualizarDashboardExecutivo(SinistroEvent event) {
        
        String periodo = event.getTimestamp()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toString();
        
        DashboardExecutivoProjection dashboard = dashboardRepo
            .findById(periodo)
            .orElse(new DashboardExecutivoProjection(periodo));
        
        switch (event.getEventType()) {
            case "SinistroCriado":
                dashboard.incrementarTotalSinistros();
                dashboard.incrementarSinistrosAbertos();
                dashboard.adicionarValorEstimado(event.getValorEstimado());
                break;
                
            case "SinistroFechado":
                dashboard.decrementarSinistrosAbertos();
                dashboard.incrementarSinistrosFechados();
                break;
        }
        
        dashboard.setUltimaAtualizacao(Instant.now());
        dashboardRepo.save(dashboard);
    }
    
    private void atualizarAnaliseFraude(SinistroEvent event) {
        
        if (!"SinistroCriado".equals(event.getEventType())) {
            return;
        }
        
        AnaliseFraudeProjection analise = new AnaliseFraudeProjection();
        analise.setSinistroId(UUID.fromString(event.getAggregateId()));
        analise.setCpfSegurado(event.getCpfSegurado());
        analise.setPlaca(event.getPlaca());
        
        // Calcular score de fraude
        int score = calcularScoreFraude(event);
        analise.setScoreFraude(score);
        analise.setNivelRisco(determinarNivelRisco(score));
        
        // Definir indicadores
        analise.setMultiplasOcorrenciasMesmoLocal(
            verificarMultiplasOcorrenciasMesmoLocal(event)
        );
        analise.setValorAcimaMedia(
            verificarValorAcimaMedia(event.getValorEstimado())
        );
        
        analise.setDataAnalise(Instant.now());
        fraudeRepo.save(analise);
    }
    
    private int calcularScoreFraude(SinistroEvent event) {
        int score = 0;
        
        // Verificar histórico do segurado
        long sinistrosAnteriores = sinistroRepo.countByCpfSegurado(event.getCpfSegurado());
        if (sinistrosAnteriores > 3) score += 20;
        
        // Verificar valor do sinistro
        if (event.getValorEstimado() != null && 
            event.getValorEstimado().compareTo(new BigDecimal("50000")) > 0) {
            score += 15;
        }
        
        // Verificar horário da ocorrência
        LocalTime horario = event.getDataOcorrencia().toLocalTime();
        if (horario.isBefore(LocalTime.of(6, 0)) || horario.isAfter(LocalTime.of(22, 0))) {
            score += 10;
        }
        
        // Verificar múltiplas ocorrências do veículo
        long ocorrenciasVeiculo = sinistroRepo.countByPlaca(event.getPlaca());
        if (ocorrenciasVeiculo > 2) score += 25;
        
        return Math.min(score, 100);
    }
}
```

---

## 🧪 **EXERCÍCIO PRÁTICO**

### **🎯 Objetivo**: Otimizar consultas do Query Side

#### **Passo 1: Testar Performance de Consultas**
```sql
-- Conectar no banco de leitura
docker exec -it postgres-read psql -U postgres -d sinistros_projections

-- Testar consulta sem índice
EXPLAIN ANALYZE SELECT * FROM projections.sinistro_view WHERE cpf_segurado = '12345678901';

-- Verificar uso de índices
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read 
FROM pg_stat_user_indexes 
WHERE schemaname = 'projections';
```

#### **Passo 2: Implementar Cache**
```java
@Test
public void testarCache() {
    // Primeira busca - deve ir ao banco
    long inicio1 = System.currentTimeMillis();
    Optional<SinistroDetailView> resultado1 = queryService.buscarPorId(sinistroId);
    long tempo1 = System.currentTimeMillis() - inicio1;
    
    // Segunda busca - deve vir do cache
    long inicio2 = System.currentTimeMillis();
    Optional<SinistroDetailView> resultado2 = queryService.buscarPorId(sinistroId);
    long tempo2 = System.currentTimeMillis() - inicio2;
    
    // Cache deve ser significativamente mais rápido
    assertThat(tempo2).isLessThan(tempo1 / 2);
    assertThat(resultado1).isEqualTo(resultado2);
}
```

#### **Passo 3: Verificar Projeções**
```bash
# Ver tabelas de projeção
curl http://localhost:8083/api/v1/actuator/projections | jq

# Dashboard executivo
curl http://localhost:8083/api/v1/query/dashboard/executivo | jq

# Análise de fraude
curl http://localhost:8083/api/v1/query/analise-fraude/sinistro/{id} | jq
```

---

## 📚 **CHECKPOINT DE APRENDIZADO**

### **✅ Você deve ser capaz de:**

1. **Criar** consultas otimizadas com índices apropriados
2. **Implementar** cache inteligente com Redis
3. **Desenvolver** múltiplas projeções especializadas
4. **Otimizar** performance de consultas complexas
5. **Monitorar** uso de cache e índices

### **❓ Perguntas para Reflexão:**

1. Quando usar cache vs consulta direta?
2. Como balancear múltiplas projeções vs complexidade?
3. Qual o impacto de índices na performance de escrita?
4. Como invalidar cache de forma eficiente?

---

## 🔗 **PRÓXIMA PARTE**

Na **Parte 4**, vamos explorar:
- Consistência eventual entre Command e Query
- Monitoramento de lag entre os lados
- Estratégias de sincronização
- Tratamento de falhas na projeção

---

**📖 Parte elaborada por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Estimada:** 48 minutos  
**📋 Pré-requisitos:** CQRS Partes 1-2