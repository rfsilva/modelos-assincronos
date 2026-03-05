# 📋 ÉPICO 1.5: IMPLEMENTAÇÃO COMPLETA DO CQRS
## Sistema de Gestão de Sinistros - Query Side e Configuração de DataSources

### 🎯 **OBJETIVO DO ÉPICO**
Completar a implementação do CQRS no sistema híbrido, implementando o **Query Side** completo, configuração de múltiplos datasources e projection handlers para garantir separação total entre Command e Query.

---

## 📊 **VISÃO GERAL DO ÉPICO**

| **Métrica** | **Valor** |
|-------------|-----------|
| **Total de Pontos** | **89 pontos** |
| **Histórias** | **5 histórias** |
| **Duração Estimada** | **3-4 sprints** |
| **Prioridade** | **CRÍTICA** |
| **Dependências** | US001, US002, US003 (Event Store) |

---

## 🏗️ **HISTÓRIAS DO ÉPICO**

### **US015 - Configuração de Múltiplos DataSources**
**Como** arquiteto de software  
**Eu quero** configurar datasources separados para Command e Query  
**Para que** o CQRS tenha separação física completa entre escrita e leitura  

**Estimativa:** 13 pontos  
**Prioridade:** CRÍTICA  
**Dependências:** US001 (Event Store Base)

---

### **US016 - Base de Projection Handlers**
**Como** desenvolvedor  
**Eu quero** implementar a infraestrutura base de projection handlers  
**Para que** eventos sejam processados e projetados para o lado de leitura  

**Estimativa:** 21 pontos  
**Prioridade:** CRÍTICA  
**Dependências:** US015 (DataSources)

---

### **US017 - Query Models e Repositories**
**Como** desenvolvedor  
**Eu quero** implementar query models otimizados para leitura  
**Para que** consultas sejam performáticas e desnormalizadas  

**Estimativa:** 21 pontos  
**Prioridade:** ALTA  
**Dependências:** US016 (Projection Base)

---

### **US018 - Query Services e APIs**
**Como** usuário do sistema  
**Eu quero** consultar dados através de APIs otimizadas  
**Para que** tenha acesso rápido às informações sem impactar o lado de escrita  

**Estimativa:** 21 pontos  
**Prioridade:** ALTA  
**Dependências:** US017 (Query Models)

---

### **US019 - Monitoramento e Health Checks CQRS**
**Como** operador do sistema  
**Eu quero** monitorar o lag entre Command e Query sides  
**Para que** possa garantir a consistência eventual do sistema  

**Estimativa:** 13 pontos  
**Prioridade:** MÉDIA  
**Dependências:** US018 (Query Services)

---

## 📋 **REFINAMENTO DETALHADO DAS HISTÓRIAS**

## **US015 - Configuração de Múltiplos DataSources**

### **📋 TAREFAS TÉCNICAS**

#### **T015.1 - Configuração de DataSources**
**Estimativa:** 5 pontos
- [ ] **ST015.1.1** - Criar `DataSourceConfiguration` class:
  ```java
  @Configuration
  @EnableConfigurationProperties({WriteDataSourceProperties.class, ReadDataSourceProperties.class})
  public class DataSourceConfiguration {
      
      @Bean
      @Primary
      @ConfigurationProperties("app.datasource.write")
      public DataSource writeDataSource() {
          return DataSourceBuilder.create().build();
      }
      
      @Bean
      @ConfigurationProperties("app.datasource.read")
      public DataSource readDataSource() {
          return DataSourceBuilder.create().build();
      }
  }
  ```
- [ ] **ST015.1.2** - Configurar properties classes para validação
- [ ] **ST015.1.3** - Implementar health checks para ambos datasources
- [ ] **ST015.1.4** - Configurar connection pools otimizados por uso
- [ ] **ST015.1.5** - Implementar fallback para datasource de leitura

#### **T015.2 - Configuração JPA Separada**
**Estimativa:** 5 pontos
- [ ] **ST015.2.1** - Criar `WriteJpaConfiguration`:
  ```java
  @Configuration
  @EnableJpaRepositories(
      basePackages = "com.seguradora.hibrida.eventstore.repository",
      entityManagerFactoryRef = "writeEntityManagerFactory",
      transactionManagerRef = "writeTransactionManager"
  )
  public class WriteJpaConfiguration {
      // Configuração para Event Store
  }
  ```
- [ ] **ST015.2.2** - Criar `ReadJpaConfiguration` para projections
- [ ] **ST015.2.3** - Configurar entity managers separados
- [ ] **ST015.2.4** - Implementar transaction managers específicos
- [ ] **ST015.2.5** - Configurar Flyway para ambos bancos

#### **T015.3 - Atualização do application.yml**
**Estimativa:** 3 pontos
- [ ] **ST015.3.1** - Configurar datasources no application.yml:
  ```yaml
  app:
    datasource:
      write:
        url: jdbc:postgresql://localhost:5435/sinistros_eventstore
        username: postgres
        password: postgres
        hikari:
          maximum-pool-size: 20
          connection-timeout: 30000
      read:
        url: jdbc:postgresql://localhost:5436/sinistros_projections
        username: postgres
        password: postgres
        hikari:
          maximum-pool-size: 50
          connection-timeout: 20000
  ```
- [ ] **ST015.3.2** - Configurar profiles específicos (local, test, prod)
- [ ] **ST015.3.3** - Implementar configurações de retry e timeout
- [ ] **ST015.3.4** - Configurar métricas por datasource
- [ ] **ST015.3.5** - Documentar configurações no README

### **📋 CRITÉRIOS DE ACEITAÇÃO**
- [ ] Dois datasources configurados e funcionando independentemente
- [ ] Connection pools otimizados para cada tipo de uso
- [ ] Health checks específicos para cada datasource
- [ ] Fallback configurado para datasource de leitura
- [ ] Métricas separadas por datasource
- [ ] Testes de conectividade automatizados

### **📋 DEFINIÇÃO DE PRONTO**
- [ ] Datasources separados funcionando
- [ ] JPA configurado para ambos bancos
- [ ] Health checks implementados
- [ ] Testes de integração passando
- [ ] Documentação atualizada

---

## **US016 - Base de Projection Handlers**

### **📋 TAREFAS TÉCNICAS**

#### **T016.1 - Interface e Classes Base**
**Estimativa:** 8 pontos
- [ ] **ST016.1.1** - Criar interface `ProjectionHandler<T extends DomainEvent>`:
  ```java
  public interface ProjectionHandler<T extends DomainEvent> {
      void handle(T event);
      Class<T> getEventType();
      String getProjectionName();
      boolean supports(T event);
      int getOrder();
  }
  ```
- [ ] **ST016.1.2** - Implementar `AbstractProjectionHandler` base class
- [ ] **ST016.1.3** - Criar `ProjectionRegistry` para descoberta automática
- [ ] **ST016.1.4** - Implementar `ProjectionEventProcessor` para processamento
- [ ] **ST016.1.5** - Configurar processamento em lote para performance
- [ ] **ST016.1.6** - Implementar retry policy para falhas
- [ ] **ST016.1.7** - Criar sistema de versionamento de projections
- [ ] **ST016.1.8** - Implementar dead letter queue para eventos problemáticos

#### **T016.2 - Sistema de Tracking**
**Estimativa:** 6 pontos
- [ ] **ST016.2.1** - Criar `ProjectionTracker` entity:
  ```java
  @Entity
  @Table(name = "projection_tracking", schema = "eventstore")
  public class ProjectionTracker {
      private String projectionName;
      private Long lastProcessedEventId;
      private Instant lastProcessedAt;
      private String status;
      private Long eventsProcessed;
      private Long eventsFailed;
  }
  ```
- [ ] **ST016.2.2** - Implementar `ProjectionTrackerRepository`
- [ ] **ST016.2.3** - Criar serviço de controle de posição
- [ ] **ST016.2.4** - Implementar checkpoint automático
- [ ] **ST016.2.5** - Configurar recovery automático após falhas
- [ ] **ST016.2.6** - Implementar métricas de lag por projection

#### **T016.3 - Processamento Assíncrono**
**Estimativa:** 7 pontos
- [ ] **ST016.3.1** - Configurar `ProjectionTaskExecutor`:
  ```java
  @Configuration
  public class ProjectionConfiguration {
      
      @Bean("projectionTaskExecutor")
      public TaskExecutor projectionTaskExecutor() {
          ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
          executor.setCorePoolSize(5);
          executor.setMaxPoolSize(20);
          executor.setQueueCapacity(1000);
          executor.setThreadNamePrefix("projection-");
          return executor;
      }
  }
  ```
- [ ] **ST016.3.2** - Implementar processamento paralelo por aggregate
- [ ] **ST016.3.3** - Configurar ordenação de eventos por aggregate
- [ ] **ST016.3.4** - Implementar backpressure para controle de carga
- [ ] **ST016.3.5** - Criar sistema de priorização de eventos
- [ ] **ST016.3.6** - Implementar circuit breaker para projections
- [ ] **ST016.3.7** - Configurar timeout por tipo de projection

### **📋 CRITÉRIOS DE ACEITAÇÃO**
- [ ] Sistema de projection handlers funcionando
- [ ] Tracking de posição implementado
- [ ] Processamento assíncrono configurado
- [ ] Recovery automático após falhas
- [ ] Métricas de performance coletadas
- [ ] Dead letter queue funcionando

### **📋 DEFINIÇÃO DE PRONTO**
- [ ] Interface base implementada
- [ ] Sistema de tracking funcionando
- [ ] Processamento assíncrono ativo
- [ ] Testes unitários e integração passando
- [ ] Documentação técnica completa

---

## **US017 - Query Models e Repositories**

### **📋 TAREFAS FUNCIONAIS**

#### **T017.1 - Query Models Base**
**Estimativa:** 8 pontos
- [ ] **ST017.1.1** - Criar `SinistroQueryModel`:
  ```java
  @Entity
  @Table(name = "sinistro_view", schema = "projections")
  public class SinistroQueryModel {
      private UUID id;
      private String protocolo;
      private String cpfSegurado;
      private String nomeSegurado;
      private String placa;
      private String apoliceNumero;
      private String tipoSinistro;
      private String status;
      private LocalDateTime dataOcorrencia;
      private LocalDateTime dataAbertura;
      private String operadorResponsavel;
      private String descricao;
      private Map<String, Object> dadosDetran;
      private List<String> tags;
      private Instant lastUpdated;
  }
  ```
- [ ] **ST017.1.2** - Criar `DetranConsultaQueryModel` específica
- [ ] **ST017.1.3** - Implementar `EventTimelineQueryModel` para auditoria
- [ ] **ST017.1.4** - Criar `MetricasAgregadasQueryModel` para dashboard
- [ ] **ST017.1.5** - Configurar mapeamentos JPA otimizados
- [ ] **ST017.1.6** - Implementar conversores para tipos complexos
- [ ] **ST017.1.7** - Configurar índices compostos para performance
- [ ] **ST017.1.8** - Implementar soft delete para auditoria

#### **T017.2 - Query Repositories**
**Estimativa:** 7 pontos
- [ ] **ST017.2.1** - Criar `SinistroQueryRepository`:
  ```java
  @Repository
  public interface SinistroQueryRepository extends JpaRepository<SinistroQueryModel, UUID> {
      
      Page<SinistroQueryModel> findByStatus(String status, Pageable pageable);
      
      @Query("SELECT s FROM SinistroQueryModel s WHERE s.cpfSegurado = :cpf")
      List<SinistroQueryModel> findByCpfSegurado(@Param("cpf") String cpf);
      
      @Query(value = "SELECT * FROM projections.sinistro_view WHERE " +
                     "to_tsvector('portuguese', descricao || ' ' || protocolo) " +
                     "@@ plainto_tsquery('portuguese', :termo)", nativeQuery = true)
      List<SinistroQueryModel> findByFullTextSearch(@Param("termo") String termo);
  }
  ```
- [ ] **ST017.2.2** - Implementar `DetranConsultaQueryRepository`
- [ ] **ST017.2.3** - Criar `EventTimelineQueryRepository`
- [ ] **ST017.2.4** - Implementar queries customizadas com Criteria API
- [ ] **ST017.2.5** - Configurar paginação otimizada
- [ ] **ST017.2.6** - Implementar full-text search
- [ ] **ST017.2.7** - Criar queries para relatórios e dashboard

#### **T017.3 - DTOs e Mappers**
**Estimativa:** 6 pontos
- [ ] **ST017.3.1** - Criar DTOs de resposta:
  ```java
  public record SinistroDetailView(
      UUID id,
      String protocolo,
      SeguradoInfo segurado,
      VeiculoInfo veiculo,
      ApoliceInfo apolice,
      String status,
      LocalDateTime dataOcorrencia,
      DetranConsultaInfo consultaDetran,
      List<EventoTimeline> timeline
  ) {}
  ```
- [ ] **ST017.3.2** - Implementar `SinistroListView` para listagens
- [ ] **ST017.3.3** - Criar `DashboardView` com métricas
- [ ] **ST017.3.4** - Implementar mappers com MapStruct
- [ ] **ST017.3.5** - Configurar serialização JSON otimizada
- [ ] **ST017.3.6** - Implementar filtros dinâmicos

### **📋 CRITÉRIOS DE ACEITAÇÃO**
- [ ] Query models otimizados criados
- [ ] Repositories com queries customizadas
- [ ] DTOs e mappers implementados
- [ ] Full-text search funcionando
- [ ] Índices otimizados configurados
- [ ] Performance de consultas < 100ms

### **📋 DEFINIÇÃO DE PRONTO**
- [ ] Models e repositories funcionando
- [ ] Queries otimizadas testadas
- [ ] DTOs e mappers implementados
- [ ] Testes de performance validados
- [ ] Documentação de APIs atualizada

---

## **US018 - Query Services e APIs**

### **📋 TAREFAS FUNCIONAIS**

#### **T018.1 - Query Services**
**Estimativa:** 8 pontos
- [ ] **ST018.1.1** - Criar `SinistroQueryService`:
  ```java
  @Service
  @Transactional(readOnly = true)
  public class SinistroQueryService {
      
      public SinistroDetailView buscarPorId(UUID id) {
          // Implementação com cache
      }
      
      public Page<SinistroListView> listar(SinistroFilter filter, Pageable pageable) {
          // Implementação com filtros dinâmicos
      }
      
      public List<SinistroQueryModel> buscarPorTexto(String termo) {
          // Full-text search
      }
  }
  ```
- [ ] **ST018.1.2** - Implementar `DetranConsultaQueryService`
- [ ] **ST018.1.3** - Criar `DashboardQueryService` para métricas
- [ ] **ST018.1.4** - Implementar `AuditoriaQueryService` para timeline
- [ ] **ST018.1.5** - Configurar cache Redis para consultas frequentes
- [ ] **ST018.1.6** - Implementar filtros dinâmicos com Specification
- [ ] **ST018.1.7** - Configurar paginação inteligente
- [ ] **ST018.1.8** - Implementar agregações para relatórios

#### **T018.2 - Controllers de Query**
**Estimativa:** 7 pontos
- [ ] **ST018.2.1** - Criar `SinistroQueryController`:
  ```java
  @RestController
  @RequestMapping("/api/v1/query/sinistros")
  @Tag(name = "🔍 Queries - Sinistros")
  public class SinistroQueryController {
      
      @GetMapping("/{id}")
      public ResponseEntity<SinistroDetailView> buscarPorId(@PathVariable UUID id) {
          // Implementação
      }
      
      @GetMapping
      public ResponseEntity<Page<SinistroListView>> listar(
          @ModelAttribute SinistroFilter filter,
          Pageable pageable) {
          // Implementação
      }
  }
  ```
- [ ] **ST018.2.2** - Implementar `DetranQueryController`
- [ ] **ST018.2.3** - Criar `DashboardController` para métricas
- [ ] **ST018.2.4** - Implementar `AuditoriaController` para timeline
- [ ] **ST018.2.5** - Configurar validação de parâmetros
- [ ] **ST018.2.6** - Implementar rate limiting
- [ ] **ST018.2.7** - Configurar documentação OpenAPI

#### **T018.3 - Cache e Performance**
**Estimativa:** 6 pontos
- [ ] **ST018.3.1** - Configurar cache Redis por tipo de consulta:
  ```java
  @Configuration
  @EnableCaching
  public class QueryCacheConfiguration {
      
      @Bean
      public CacheManager queryCacheManager() {
          RedisCacheManager.Builder builder = RedisCacheManager
              .RedisCacheManagerBuilder
              .fromConnectionFactory(redisConnectionFactory())
              .cacheDefaults(cacheConfiguration());
          return builder.build();
      }
  }
  ```
- [ ] **ST018.3.2** - Implementar cache warming para consultas frequentes
- [ ] **ST018.3.3** - Configurar invalidação inteligente de cache
- [ ] **ST018.3.4** - Implementar compressão para responses grandes
- [ ] **ST018.3.5** - Configurar ETags para cache HTTP
- [ ] **ST018.3.6** - Implementar métricas de hit/miss ratio

### **📋 CRITÉRIOS DE ACEITAÇÃO**
- [ ] Query services implementados
- [ ] APIs REST funcionando
- [ ] Cache Redis configurado
- [ ] Performance < 50ms para consultas simples
- [ ] Rate limiting implementado
- [ ] Documentação OpenAPI completa

### **📋 DEFINIÇÃO DE PRONTO**
- [ ] Services e controllers funcionando
- [ ] Cache implementado e testado
- [ ] APIs documentadas
- [ ] Testes de carga validados
- [ ] Monitoramento configurado

---

## **US019 - Monitoramento e Health Checks CQRS**

### **📋 TAREFAS TÉCNICAS**

#### **T019.1 - Health Checks CQRS**
**Estimativa:** 5 pontos
- [ ] **ST019.1.1** - Criar `CQRSHealthIndicator`:
  ```java
  @Component
  public class CQRSHealthIndicator implements HealthIndicator {
      
      @Override
      public Health health() {
          try {
              long commandSideEvents = eventStoreRepository.count();
              long querySideEvents = projectionTrackerRepository
                  .findAll()
                  .stream()
                  .mapToLong(ProjectionTracker::getLastProcessedEventId)
                  .max()
                  .orElse(0L);
              
              long lag = commandSideEvents - querySideEvents;
              
              if (lag > 1000) {
                  return Health.down()
                      .withDetail("lag", lag)
                      .withDetail("status", "HIGH_LAG")
                      .build();
              }
              
              return Health.up()
                  .withDetail("command-side-events", commandSideEvents)
                  .withDetail("query-side-events", querySideEvents)
                  .withDetail("lag", lag)
                  .build();
          } catch (Exception e) {
              return Health.down(e).build();
          }
      }
  }
  ```
- [ ] **ST019.1.2** - Implementar health check para cada projection
- [ ] **ST019.1.3** - Configurar alertas para lag alto
- [ ] **ST019.1.4** - Implementar health check para datasources
- [ ] **ST019.1.5** - Criar dashboard de saúde do sistema

#### **T019.2 - Métricas Customizadas**
**Estimativa:** 5 pontos
- [ ] **ST019.2.1** - Criar `CQRSMetrics`:
  ```java
  @Component
  public class CQRSMetrics implements MeterBinder {
      
      @Override
      public void bindTo(MeterRegistry registry) {
          Gauge.builder("cqrs.command.side.events")
              .description("Total events in command side")
              .register(registry, this, CQRSMetrics::getCommandSideEvents);
              
          Gauge.builder("cqrs.query.side.lag")
              .description("Lag between command and query side")
              .register(registry, this, CQRSMetrics::getQuerySideLag);
      }
  }
  ```
- [ ] **ST019.2.2** - Implementar métricas de throughput por projection
- [ ] **ST019.2.3** - Configurar métricas de latência de processamento
- [ ] **ST019.2.4** - Implementar métricas de erro por projection
- [ ] **ST019.2.5** - Criar alertas baseados em métricas

#### **T019.3 - Dashboard e Observabilidade**
**Estimativa:** 3 pontos
- [ ] **ST019.3.1** - Criar endpoint `/actuator/cqrs` para status:
  ```java
  @RestController
  @RequestMapping("/actuator/cqrs")
  public class CQRSActuatorController {
      
      @GetMapping
      public ResponseEntity<Map<String, Object>> getCQRSStatus() {
          // Status completo do CQRS
      }
      
      @GetMapping("/projections")
      public ResponseEntity<List<ProjectionStatus>> getProjectionsStatus() {
          // Status de cada projection
      }
  }
  ```
- [ ] **ST019.3.2** - Implementar logs estruturados para troubleshooting
- [ ] **ST019.3.3** - Configurar tracing distribuído
- [ ] **ST019.3.4** - Criar documentação de troubleshooting
- [ ] **ST019.3.5** - Implementar alertas proativos

### **📋 CRITÉRIOS DE ACEITAÇÃO**
- [ ] Health checks CQRS funcionando
- [ ] Métricas customizadas coletadas
- [ ] Dashboard de observabilidade ativo
- [ ] Alertas configurados para lag alto
- [ ] Logs estruturados implementados
- [ ] Documentação de troubleshooting completa

### **📋 DEFINIÇÃO DE PRONTO**
- [ ] Health checks implementados
- [ ] Métricas coletadas e visualizadas
- [ ] Alertas configurados
- [ ] Dashboard funcionando
- [ ] Documentação atualizada

---

## 📊 **RESUMO EXECUTIVO DO ÉPICO**

### **🎯 Objetivos Alcançados**
- ✅ **CQRS Completo**: Separação física total entre Command e Query
- ✅ **Performance**: Consultas otimizadas com cache inteligente
- ✅ **Observabilidade**: Monitoramento completo do lag CQRS
- ✅ **Escalabilidade**: Datasources independentes para escala horizontal

### **📈 Benefícios Esperados**
- **Performance**: Consultas 10x mais rápidas com projections
- **Escalabilidade**: Command e Query sides podem escalar independentemente
- **Manutenibilidade**: Separação clara de responsabilidades
- **Observabilidade**: Visibilidade completa do pipeline CQRS

### **🔧 Stack Tecnológico**
- **Command Side**: PostgreSQL (porta 5435) + Event Store
- **Query Side**: PostgreSQL (porta 5436) + Projections
- **Cache**: Redis para consultas frequentes
- **Processamento**: Assíncrono com Spring Task Executor
- **Monitoramento**: Micrometer + Actuator + Health Checks

### **📋 Critérios de Sucesso**
- [ ] Lag CQRS < 1 segundo em 95% do tempo
- [ ] Performance de consultas < 50ms
- [ ] Zero downtime durante deploys
- [ ] Health checks 100% funcionais
- [ ] Cobertura de testes > 90%

### **🚀 Próximos Passos**
Após a conclusão deste épico, o sistema terá:
- **CQRS Completo** implementado e funcionando
- **Separação física** total entre Command e Query
- **Observabilidade** completa do pipeline
- **Base sólida** para implementação dos próximos épicos de domínio

---

**🎯 Este épico é CRÍTICO para completar a base arquitetural e permitir que os próximos épicos de domínio (Segurados, Apólices, Sinistros) sejam implementados sobre uma fundação CQRS sólida e escalável!**