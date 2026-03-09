# 🧪 TESTES E BOAS PRÁTICAS - PARTE 4
## Testes de Integração com TestContainers

### 🎯 **OBJETIVOS DESTA PARTE**
- Implementar testes de integração com TestContainers
- Configurar ambientes de teste isolados
- Testar fluxos completos da aplicação
- Validar integrações com bancos de dados reais

---

## 🐳 **INTRODUÇÃO AO TESTCONTAINERS**

### **📋 O que é TestContainers?**

TestContainers é uma biblioteca Java que permite executar containers Docker durante os testes, fornecendo:

#### **Vantagens:**
- ✅ **Isolamento**: Cada teste executa em ambiente limpo
- ✅ **Realismo**: Usa bancos de dados reais, não mocks
- ✅ **Portabilidade**: Funciona em qualquer ambiente com Docker
- ✅ **Limpeza**: Containers são destruídos automaticamente

#### **Casos de Uso:**
- Testes de integração com banco de dados
- Testes de APIs externas
- Testes de mensageria (Kafka, RabbitMQ)
- Testes de cache (Redis)

### **🔧 Configuração no Projeto**

```xml
<!-- Dependências TestContainers -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>kafka</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 🗄️ **TESTES COM POSTGRESQL CONTAINER**

### **📝 Configuração Base para PostgreSQL**

```java
@Testcontainers
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:13:///testdb",
    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true"
})
@DisplayName("Testes de Integração - PostgreSQL")
class PostgreSQLIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("test-data.sql");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

### **📝 Exemplo Completo: Teste de Event Store**

```java
@Testcontainers
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Event Store - Testes de Integração")
class EventStoreIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("eventstore_test")
            .withUsername("eventstore")
            .withPassword("eventstore123")
            .withInitScript("db/migration/V1__Create_Events_Table.sql");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("app.datasource.write.url", postgres::getJdbcUrl);
        registry.add("app.datasource.write.username", postgres::getUsername);
        registry.add("app.datasource.write.password", postgres::getPassword);
        registry.add("app.datasource.read.url", postgres::getJdbcUrl);
        registry.add("app.datasource.read.username", postgres::getUsername);
        registry.add("app.datasource.read.password", postgres::getPassword);
    }
    
    @Autowired
    private EventStore eventStore;
    
    @Autowired
    private EventStoreRepository repository;
    
    @Nested
    @DisplayName("Persistência de Eventos")
    class PersistenciaEventos {
        
        @Test
        @Order(1)
        @DisplayName("Deve salvar e carregar eventos de um aggregate")
        void deveSalvarECarregarEventosDeUmAggregate() {
            // Given
            String aggregateId = "sinistro-integration-001";
            
            List<DomainEvent> eventos = Arrays.asList(
                SinistroCriadoEvent.builder()
                    .aggregateId(aggregateId)
                    .protocolo("SIN-INT-001")
                    .cpfSegurado("12345678901")
                    .descricao("Teste integração")
                    .valorEstimado(new BigDecimal("5000.00"))
                    .placa("INT1234")
                    .timestamp(Instant.now())
                    .build(),
                
                SinistroAtualizadoEvent.builder()
                    .aggregateId(aggregateId)
                    .novoStatus("EM_ANALISE")
                    .novaDescricao("Análise técnica iniciada")
                    .timestamp(Instant.now().plusSeconds(1))
                    .build()
            );
            
            // When - salva eventos
            eventStore.saveEvents(aggregateId, eventos, 0L);
            
            // Then - carrega e verifica
            List<DomainEvent> eventosCarregados = eventStore.loadEvents(aggregateId);
            
            assertThat(eventosCarregados).hasSize(2);
            
            // Verifica primeiro evento
            SinistroCriadoEvent eventoCriacao = (SinistroCriadoEvent) eventosCarregados.get(0);
            assertThat(eventoCriacao.getProtocolo()).isEqualTo("SIN-INT-001");
            assertThat(eventoCriacao.getCpfSegurado()).isEqualTo("12345678901");
            
            // Verifica segundo evento
            SinistroAtualizadoEvent eventoAtualizacao = (SinistroAtualizadoEvent) eventosCarregados.get(1);
            assertThat(eventoAtualizacao.getNovoStatus()).isEqualTo("EM_ANALISE");
        }
        
        @Test
        @Order(2)
        @DisplayName("Deve tratar concorrência otimista")
        void deveTratarConcorrenciaOtimista() {
            // Given
            String aggregateId = "sinistro-concorrencia-001";
            
            DomainEvent evento1 = SinistroCriadoEvent.builder()
                .aggregateId(aggregateId)
                .protocolo("SIN-CONC-001")
                .cpfSegurado("12345678901")
                .descricao("Teste concorrência")
                .valorEstimado(new BigDecimal("3000.00"))
                .placa("CONC123")
                .timestamp(Instant.now())
                .build();
            
            // When - salva primeiro evento
            eventStore.saveEvents(aggregateId, Arrays.asList(evento1), 0L);
            
            // Then - tenta salvar com versão incorreta
            DomainEvent evento2 = SinistroAtualizadoEvent.builder()
                .aggregateId(aggregateId)
                .novoStatus("CANCELADO")
                .timestamp(Instant.now())
                .build();
            
            assertThatThrownBy(() -> 
                eventStore.saveEvents(aggregateId, Arrays.asList(evento2), 0L) // Versão incorreta
            ).isInstanceOf(ConcurrencyException.class);
        }
    }
    
    @Nested
    @DisplayName("Consultas Complexas")
    class ConsultasComplexas {
        
        @Test
        @Order(3)
        @DisplayName("Deve buscar eventos por tipo e período")
        void deveBuscarEventosPorTipoEPeriodo() {
            // Given
            Instant inicio = Instant.now().minus(1, ChronoUnit.HOURS);
            Instant fim = Instant.now().plus(1, ChronoUnit.HOURS);
            
            // When
            List<DomainEvent> eventos = eventStore.loadEventsByType(
                "SinistroCriadoEvent", inicio, fim
            );
            
            // Then
            assertThat(eventos).isNotEmpty();
            eventos.forEach(evento -> {
                assertThat(evento).isInstanceOf(SinistroCriadoEvent.class);
                assertThat(evento.getTimestamp()).isBetween(inicio, fim);
            });
        }
        
        @Test
        @Order(4)
        @DisplayName("Deve buscar eventos por correlation ID")
        void deveBuscarEventosPorCorrelationId() {
            // Given
            UUID correlationId = UUID.randomUUID();
            String aggregateId = "sinistro-correlation-001";
            
            DomainEvent evento = SinistroCriadoEvent.builder()
                .aggregateId(aggregateId)
                .protocolo("SIN-CORR-001")
                .cpfSegurado("12345678901")
                .descricao("Teste correlation")
                .valorEstimado(new BigDecimal("2000.00"))
                .placa("CORR123")
                .timestamp(Instant.now())
                .build();
            
            evento.setCorrelationId(correlationId);
            
            // When
            eventStore.saveEvents(aggregateId, Arrays.asList(evento), 0L);
            
            List<DomainEvent> eventosEncontrados = 
                eventStore.loadEventsByCorrelationId(correlationId);
            
            // Then
            assertThat(eventosEncontrados).hasSize(1);
            assertThat(eventosEncontrados.get(0).getCorrelationId()).isEqualTo(correlationId);
        }
    }
    
    @Nested
    @DisplayName("Performance e Escalabilidade")
    class PerformanceEscalabilidade {
        
        @Test
        @Order(5)
        @DisplayName("Deve manter performance com muitos eventos")
        void deveManterPerformanceComMuitosEventos() {
            // Given
            String aggregateId = "sinistro-performance-001";
            List<DomainEvent> muitosEventos = new ArrayList<>();
            
            // Cria 1000 eventos
            for (int i = 1; i <= 1000; i++) {
                muitosEventos.add(
                    SinistroAtualizadoEvent.builder()
                        .aggregateId(aggregateId)
                        .novaDescricao("Atualização " + i)
                        .timestamp(Instant.now().plusSeconds(i))
                        .build()
                );
            }
            
            // When - mede tempo de persistência
            long startTime = System.currentTimeMillis();
            eventStore.saveEvents(aggregateId, muitosEventos, 0L);
            long saveTime = System.currentTimeMillis() - startTime;
            
            // When - mede tempo de carregamento
            startTime = System.currentTimeMillis();
            List<DomainEvent> eventosCarregados = eventStore.loadEvents(aggregateId);
            long loadTime = System.currentTimeMillis() - startTime;
            
            // Then
            assertThat(eventosCarregados).hasSize(1000);
            assertThat(saveTime).isLessThan(5000); // Menos de 5 segundos para salvar
            assertThat(loadTime).isLessThan(2000); // Menos de 2 segundos para carregar
        }
        
        @Test
        @Order(6)
        @DisplayName("Deve carregar eventos incrementalmente")
        void deveCarregarEventosIncrementalmente() {
            // Given
            String aggregateId = "sinistro-incremental-001";
            
            // Salva eventos iniciais
            List<DomainEvent> eventosIniciais = Arrays.asList(
                SinistroCriadoEvent.builder()
                    .aggregateId(aggregateId)
                    .protocolo("SIN-INC-001")
                    .cpfSegurado("12345678901")
                    .descricao("Evento inicial")
                    .valorEstimado(new BigDecimal("1000.00"))
                    .placa("INC1234")
                    .timestamp(Instant.now())
                    .build()
            );
            
            eventStore.saveEvents(aggregateId, eventosIniciais, 0L);
            
            // Salva mais eventos
            List<DomainEvent> eventosAdicionais = Arrays.asList(
                SinistroAtualizadoEvent.builder()
                    .aggregateId(aggregateId)
                    .novoStatus("EM_ANALISE")
                    .timestamp(Instant.now().plusSeconds(1))
                    .build(),
                
                SinistroAtualizadoEvent.builder()
                    .aggregateId(aggregateId)
                    .novoStatus("FECHADO")
                    .timestamp(Instant.now().plusSeconds(2))
                    .build()
            );
            
            eventStore.saveEvents(aggregateId, eventosAdicionais, 1L);
            
            // When - carrega apenas eventos a partir da versão 2
            List<DomainEvent> eventosIncrementais = 
                eventStore.loadEvents(aggregateId, 2L);
            
            // Then
            assertThat(eventosIncrementais).hasSize(2);
            assertThat(eventosIncrementais.get(0)).isInstanceOf(SinistroAtualizadoEvent.class);
            assertThat(eventosIncrementais.get(1)).isInstanceOf(SinistroAtualizadoEvent.class);
        }
    }
}
```

---

## 🔄 **TESTES DE FLUXO COMPLETO (CQRS)**

### **📝 Exemplo de Teste End-to-End CQRS**

```java
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Fluxo Completo CQRS - Testes de Integração")
class CQRSFluxoCompletoTest {
    
    @Container
    static PostgreSQLContainer<?> eventStoreDb = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("eventstore")
            .withUsername("eventstore")
            .withPassword("eventstore123");
    
    @Container
    static PostgreSQLContainer<?> projectionsDb = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("projections")
            .withUsername("projections")
            .withPassword("projections123");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Event Store (Write Side)
        registry.add("app.datasource.write.url", eventStoreDb::getJdbcUrl);
        registry.add("app.datasource.write.username", eventStoreDb::getUsername);
        registry.add("app.datasource.write.password", eventStoreDb::getPassword);
        
        // Projections (Read Side)
        registry.add("app.datasource.read.url", projectionsDb::getJdbcUrl);
        registry.add("app.datasource.read.username", projectionsDb::getUsername);
        registry.add("app.datasource.read.password", projectionsDb::getPassword);
    }
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private CommandBus commandBus;
    
    @Autowired
    private SinistroQueryService queryService;
    
    @Autowired
    private EventStore eventStore;
    
    @Autowired
    private SinistroQueryRepository queryRepository;
    
    private static String sinistroId;
    private static String protocolo;
    
    @Test
    @Order(1)
    @DisplayName("Deve criar sinistro via command e refletir na query")
    void deveCriarSinistroViaCommandERefletirNaQuery() {
        // Given
        sinistroId = UUID.randomUUID().toString();
        protocolo = "SIN-CQRS-001";
        
        CriarSinistroCommand command = CriarSinistroCommand.builder()
            .aggregateId(sinistroId)
            .protocolo(protocolo)
            .cpfSegurado("12345678901")
            .nomeSegurado("João Silva")
            .descricao("Colisão traseira na Av. Paulista")
            .valorEstimado(new BigDecimal("8000.00"))
            .placa("CQRS123")
            .tipoSinistro("COLISAO")
            .dataOcorrencia(Instant.now().minus(1, ChronoUnit.DAYS))
            .build();
        
        // When - executa command (Write Side)
        CommandResult result = commandBus.send(command);
        
        // Then - verifica sucesso do command
        assertThat(result.isSuccess()).isTrue();
        
        // Verifica persistência no Event Store
        List<DomainEvent> eventos = eventStore.loadEvents(sinistroId);
        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0)).isInstanceOf(SinistroCriadoEvent.class);
        
        // Aguarda processamento assíncrono das projeções
        await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted(() -> {
                // Verifica projeção (Read Side)
                Optional<SinistroDetailView> sinistroView = 
                    queryService.buscarPorId(UUID.fromString(sinistroId));
                
                assertThat(sinistroView).isPresent();
                SinistroDetailView view = sinistroView.get();
                assertThat(view.getProtocolo()).isEqualTo(protocolo);
                assertThat(view.getCpfSegurado()).isEqualTo("12345678901");
                assertThat(view.getStatus()).isEqualTo("ABERTO");
                assertThat(view.getValorEstimado()).isEqualTo(new BigDecimal("8000.00"));
            });
    }
    
    @Test
    @Order(2)
    @DisplayName("Deve atualizar sinistro e refletir mudanças na query")
    void deveAtualizarSinistroERefletirMudancasNaQuery() {
        // Given
        AtualizarSinistroCommand command = AtualizarSinistroCommand.builder()
            .aggregateId(sinistroId)
            .novoStatus("EM_ANALISE")
            .novaDescricao("Análise técnica iniciada - aguardando laudo")
            .novoValorEstimado(new BigDecimal("9500.00"))
            .operadorResponsavel("Maria Santos")
            .build();
        
        // When
        CommandResult result = commandBus.send(command);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        
        // Verifica eventos no Event Store
        List<DomainEvent> eventos = eventStore.loadEvents(sinistroId);
        assertThat(eventos).hasSize(2);
        assertThat(eventos.get(1)).isInstanceOf(SinistroAtualizadoEvent.class);
        
        // Aguarda atualização da projeção
        await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted(() -> {
                Optional<SinistroDetailView> sinistroView = 
                    queryService.buscarPorId(UUID.fromString(sinistroId));
                
                assertThat(sinistroView).isPresent();
                SinistroDetailView view = sinistroView.get();
                assertThat(view.getStatus()).isEqualTo("EM_ANALISE");
                assertThat(view.getDescricao()).isEqualTo("Análise técnica iniciada - aguardando laudo");
                assertThat(view.getValorEstimado()).isEqualTo(new BigDecimal("9500.00"));
                assertThat(view.getOperadorResponsavel()).isEqualTo("Maria Santos");
            });
    }
    
    @Test
    @Order(3)
    @DisplayName("Deve consultar sinistro via API REST")
    void deveConsultarSinistroViaApiRest() {
        // When - consulta via API
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/api/sinistros/{id}", 
            Map.class, 
            sinistroId
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        Map<String, Object> sinistro = response.getBody();
        assertThat(sinistro).isNotNull();
        assertThat(sinistro.get("protocolo")).isEqualTo(protocolo);
        assertThat(sinistro.get("status")).isEqualTo("EM_ANALISE");
        assertThat(sinistro.get("cpfSegurado")).isEqualTo("12345678901");
    }
    
    @Test
    @Order(4)
    @DisplayName("Deve listar sinistros com filtros via API")
    void deveListarSinistrosComFiltrosViaApi() {
        // When - lista com filtros
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/api/sinistros?status=EM_ANALISE&cpfSegurado=12345678901&size=10&page=0",
            Map.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        Map<String, Object> page = response.getBody();
        assertThat(page).isNotNull();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) page.get("content");
        assertThat(content).isNotEmpty();
        
        // Verifica se o sinistro criado está na lista
        boolean sinistroEncontrado = content.stream()
            .anyMatch(s -> protocolo.equals(s.get("protocolo")));
        assertThat(sinistroEncontrado).isTrue();
    }
    
    @Test
    @Order(5)
    @DisplayName("Deve manter consistência após múltiplas operações")
    void deveManterConsistenciaAposMultiplasOperacoes() {
        // Given - executa várias operações
        List<Command> commands = Arrays.asList(
            AtualizarSinistroCommand.builder()
                .aggregateId(sinistroId)
                .novoStatus("AGUARDANDO_DOCUMENTOS")
                .build(),
            
            AtualizarSinistroCommand.builder()
                .aggregateId(sinistroId)
                .novaDescricao("Documentos recebidos - análise final")
                .build(),
            
            AtualizarSinistroCommand.builder()
                .aggregateId(sinistroId)
                .novoStatus("FECHADO")
                .novoValorEstimado(new BigDecimal("10000.00"))
                .build()
        );
        
        // When - executa commands sequencialmente
        commands.forEach(command -> {
            CommandResult result = commandBus.send(command);
            assertThat(result.isSuccess()).isTrue();
        });
        
        // Then - verifica consistência final
        await()
            .atMost(Duration.ofSeconds(15))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted(() -> {
                // Verifica Event Store
                List<DomainEvent> eventos = eventStore.loadEvents(sinistroId);
                assertThat(eventos).hasSize(5); // 1 criação + 4 atualizações
                
                // Verifica projeção final
                Optional<SinistroDetailView> sinistroView = 
                    queryService.buscarPorId(UUID.fromString(sinistroId));
                
                assertThat(sinistroView).isPresent();
                SinistroDetailView view = sinistroView.get();
                assertThat(view.getStatus()).isEqualTo("FECHADO");
                assertThat(view.getDescricao()).isEqualTo("Documentos recebidos - análise final");
                assertThat(view.getValorEstimado()).isEqualTo(new BigDecimal("10000.00"));
            });
    }
}
```

---

## 📨 **TESTES COM KAFKA CONTAINER**

### **📝 Exemplo de Teste com Kafka**

```java
@Testcontainers
@SpringBootTest
@DisplayName("Event Bus Kafka - Testes de Integração")
class KafkaEventBusIntegrationTest {
    
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withEmbeddedZookeeper();
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("app.eventbus.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
    
    @Autowired
    private EventBus eventBus;
    
    @MockBean
    private SinistroEventHandler eventHandler;
    
    @Test
    @DisplayName("Deve publicar e consumir eventos via Kafka")
    void devePublicarEConsumirEventosViaKafka() {
        // Given
        SinistroCriadoEvent event = SinistroCriadoEvent.builder()
            .aggregateId("sinistro-kafka-001")
            .protocolo("SIN-KAFKA-001")
            .cpfSegurado("12345678901")
            .descricao("Teste Kafka")
            .valorEstimado(new BigDecimal("5000.00"))
            .placa("KAFKA123")
            .timestamp(Instant.now())
            .build();
        
        // When
        eventBus.publish(event);
        
        // Then - aguarda processamento assíncrono
        await()
            .atMost(Duration.ofSeconds(30))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted(() -> {
                verify(eventHandler, atLeastOnce()).handle(any(SinistroCriadoEvent.class));
            });
    }
}
```

---

## 🔧 **CONFIGURAÇÕES AVANÇADAS**

### **📝 Configuração de Múltiplos Containers**

```java
@Testcontainers
@SpringBootTest
@DisplayName("Ambiente Completo - Múltiplos Containers")
class AmbienteCompletoTest {
    
    static Network network = Network.newNetwork();
    
    @Container
    static PostgreSQLContainer<?> eventStoreDb = new PostgreSQLContainer<>("postgres:13")
            .withNetwork(network)
            .withNetworkAliases("eventstore-db")
            .withDatabaseName("eventstore")
            .withUsername("eventstore")
            .withPassword("eventstore123");
    
    @Container
    static PostgreSQLContainer<?> projectionsDb = new PostgreSQLContainer<>("postgres:13")
            .withNetwork(network)
            .withNetworkAliases("projections-db")
            .withDatabaseName("projections")
            .withUsername("projections")
            .withPassword("projections123");
    
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withNetwork(network)
            .withNetworkAliases("kafka")
            .withEmbeddedZookeeper();
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:6-alpine")
            .withNetwork(network)
            .withNetworkAliases("redis")
            .withExposedPorts(6379);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Event Store
        registry.add("app.datasource.write.url", eventStoreDb::getJdbcUrl);
        registry.add("app.datasource.write.username", eventStoreDb::getUsername);
        registry.add("app.datasource.write.password", eventStoreDb::getPassword);
        
        // Projections
        registry.add("app.datasource.read.url", projectionsDb::getJdbcUrl);
        registry.add("app.datasource.read.username", projectionsDb::getUsername);
        registry.add("app.datasource.read.password", projectionsDb::getPassword);
        
        // Kafka
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        
        // Redis
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }
    
    @Test
    @DisplayName("Deve funcionar com ambiente completo")
    void deveFuncionarComAmbienteCompleto() {
        // Teste com todos os componentes funcionando
        assertThat(eventStoreDb.isRunning()).isTrue();
        assertThat(projectionsDb.isRunning()).isTrue();
        assertThat(kafka.isRunning()).isTrue();
        assertThat(redis.isRunning()).isTrue();
    }
}
```

---

## 📊 **MONITORAMENTO E DEBUGGING**

### **📝 Logs e Debugging de Containers**

```java
@Test
@DisplayName("Deve capturar logs dos containers")
void deveCapturaLogsContainers() {
    // Captura logs do PostgreSQL
    String postgresLogs = postgres.getLogs();
    System.out.println("PostgreSQL Logs:");
    System.out.println(postgresLogs);
    
    // Verifica se não há erros críticos
    assertThat(postgresLogs).doesNotContain("FATAL");
    assertThat(postgresLogs).doesNotContain("ERROR");
}

@Test
@DisplayName("Deve verificar saúde dos containers")
void deveVerificarSaudeContainers() {
    // Verifica se containers estão saudáveis
    assertThat(postgres.isHealthy()).isTrue();
    assertThat(postgres.isRunning()).isTrue();
    
    // Verifica conectividade
    assertThat(postgres.getJdbcUrl()).isNotEmpty();
    assertThat(postgres.getUsername()).isEqualTo("test");
}
```

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [TestContainers Documentation](https://www.testcontainers.org/)
- [TestContainers with Spring Boot](https://spring.io/blog/2020/03/27/testcontainers-integration-testing-with-spring-boot)
- [Docker Best Practices for Testing](https://docs.docker.com/develop/dev-best-practices/)

### **📖 Próxima Parte:**
- **Parte 5**: Testes E2E e Automação

---

**📝 Parte 4 de 5 - Testes de Integração com TestContainers**  
**⏱️ Tempo estimado**: 75 minutos  
**🎯 Próximo**: [Parte 5 - Testes E2E e Automação](./10-testes-parte-5.md)