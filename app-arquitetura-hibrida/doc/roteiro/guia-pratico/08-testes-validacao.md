# 🧪 ETAPA 08: TESTES & VALIDAÇÃO
## Suite Completa de Testes para Garantia de Qualidade

### 🎯 **OBJETIVO DA ETAPA**

Implementar uma suite completa de testes (unitários, integração, contrato e E2E) para garantir qualidade, confiabilidade e aderência aos requisitos da arquitetura híbrida CQRS + Event Sourcing.

**⏱️ Duração Estimada:** 4-6 horas  
**👥 Participantes:** Desenvolvedor + QA + Tech Lead  
**📋 Pré-requisitos:** Etapa 07 concluída e aprovada

---

## 📋 **CHECKLIST DE IMPLEMENTAÇÃO**

### **🔬 1. TESTES UNITÁRIOS**

#### **📦 Estrutura de Testes:**
```
src/test/java/
└── com/seguradora/hibrida/[dominio]/
    ├── aggregate/
    │   └── [Dominio]AggregateTest.java
    ├── command/
    │   ├── [Comando]HandlerTest.java
    │   └── [Comando]ValidatorTest.java
    ├── event/
    │   └── [Evento]HandlerTest.java
    ├── projection/
    │   └── [Dominio]ProjectionHandlerTest.java
    ├── query/
    │   └── [Dominio]QueryServiceTest.java
    └── controller/
        └── [Dominio]ControllerTest.java
```

#### **🧩 Testes de Agregados:**
```java
@ExtendWith(MockitoExtension.class)
class [Dominio]AggregateTest {
    
    private [Dominio]Aggregate aggregate;
    
    @BeforeEach
    void setUp() {
        aggregate = new [Dominio]Aggregate();
    }
    
    // ========== TESTES DE CRIAÇÃO ==========
    @Test
    @DisplayName("Deve criar agregado com dados válidos")
    void deveCriarAgregadoComDadosValidos() {
        // Given
        String id = UUID.randomUUID().toString();
        String [campo] = "valor válido";
        
        // When
        aggregate.criar(id, [campo], "user123");
        
        // Then
        assertThat(aggregate.getId()).isEqualTo(id);
        assertThat(aggregate.get[Campo]()).isEqualTo([campo]);
        assertThat(aggregate.getUncommittedEvents()).hasSize(1);
        assertThat(aggregate.getUncommittedEvents().get(0))
            .isInstanceOf([Dominio]CriadoEvent.class);
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar com dados inválidos")
    void deveLancarExcecaoAoCriarComDadosInvalidos() {
        // Given
        String id = UUID.randomUUID().toString();
        String [campoInvalido] = null;
        
        // When & Then
        assertThatThrownBy(() -> aggregate.criar(id, [campoInvalido], "user123"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("[Campo] é obrigatório");
    }
    
    // ========== TESTES DE ATUALIZAÇÃO ==========
    @Test
    @DisplayName("Deve atualizar agregado com novos dados")
    void deveAtualizarAgregadoComNovosDados() {
        // Given
        aggregate.criar(UUID.randomUUID().toString(), "valor inicial", "user123");
        aggregate.clearUncommittedEvents();
        String novoValor = "valor atualizado";
        
        // When
        aggregate.atualizar(novoValor, "user123");
        
        // Then
        assertThat(aggregate.get[Campo]()).isEqualTo(novoValor);
        assertThat(aggregate.getUncommittedEvents()).hasSize(1);
        assertThat(aggregate.getUncommittedEvents().get(0))
            .isInstanceOf([Dominio]AtualizadoEvent.class);
    }
    
    // ========== TESTES DE BUSINESS RULES ==========
    @Test
    @DisplayName("Deve validar invariantes do agregado")
    void deveValidarInvariantesDoAgregado() {
        // Given
        aggregate.criar(UUID.randomUUID().toString(), "valor válido", "user123");
        
        // When & Then
        assertThatThrownBy(() -> aggregate.[operacaoQueViolaInvariante]())
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Violação de regra de negócio");
    }
    
    // ========== TESTES DE EVENT SOURCING ==========
    @Test
    @DisplayName("Deve reconstruir estado a partir de eventos")
    void deveReconstruirEstadoAPartirDeEventos() {
        // Given
        String aggregateId = UUID.randomUUID().toString();
        List<DomainEvent> events = Arrays.asList(
            new [Dominio]CriadoEvent(aggregateId, 1L, "valor1"),
            new [Dominio]AtualizadoEvent(aggregateId, 2L, "valor2")
        );
        
        // When
        [Dominio]Aggregate reconstructed = new [Dominio]Aggregate();
        events.forEach(reconstructed::applyEvent);
        
        // Then
        assertThat(reconstructed.getId()).isEqualTo(aggregateId);
        assertThat(reconstructed.get[Campo]()).isEqualTo("valor2");
        assertThat(reconstructed.getVersion()).isEqualTo(2L);
    }
    
    // ========== TESTES DE SNAPSHOTS ==========
    @Test
    @DisplayName("Deve criar e restaurar snapshot corretamente")
    void deveCriarERestaurarSnapshotCorretamente() {
        // Given
        aggregate.criar(UUID.randomUUID().toString(), "valor teste", "user123");
        aggregate.atualizar("valor atualizado", "user123");
        
        // When
        Object snapshot = aggregate.createSnapshot();
        [Dominio]Aggregate restored = new [Dominio]Aggregate();
        restored.restoreFromSnapshot(snapshot);
        
        // Then
        assertThat(restored.getId()).isEqualTo(aggregate.getId());
        assertThat(restored.get[Campo]()).isEqualTo(aggregate.get[Campo]());
        assertThat(restored.getVersion()).isEqualTo(aggregate.getVersion());
    }
}
```

#### **⚡ Testes de Command Handlers:**
```java
@ExtendWith(MockitoExtension.class)
class [Comando]HandlerTest {
    
    @Mock
    private AggregateRepository<[Dominio]Aggregate> repository;
    
    @Mock
    private MeterRegistry meterRegistry;
    
    @InjectMocks
    private [Comando]Handler handler;
    
    @BeforeEach
    void setUp() {
        when(meterRegistry.counter(anyString(), any(Tags.class)))
            .thenReturn(mock(Counter.class));
        when(meterRegistry.timer(anyString(), any(Tags.class)))
            .thenReturn(mock(Timer.class));
    }
    
    @Test
    @DisplayName("Deve processar comando com sucesso")
    void deveProcessarComandoComSucesso() {
        // Given
        var command = [Comando].builder()
            .aggregateId(UUID.randomUUID().toString())
            .userId("user123")
            .[campo]("valor válido")
            .build();
            
        [Dominio]Aggregate aggregate = new [Dominio]Aggregate();
        when(repository.getById(command.getAggregateId())).thenReturn(aggregate);
        
        // When
        CommandResult result = handler.handle(command);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getCorrelationId()).isEqualTo(command.getCorrelationId());
        verify(repository).save(aggregate);
    }
    
    @Test
    @DisplayName("Deve falhar ao processar comando inválido")
    void deveFalharAoProcessarComandoInvalido() {
        // Given
        var command = [Comando].builder()
            .aggregateId(UUID.randomUUID().toString())
            .userId("user123")
            .[campo](null) // Campo inválido
            .build();
        
        // When
        CommandResult result = handler.handle(command);
        
        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("BUSINESS_RULE_VIOLATION");
        verify(repository, never()).save(any());
    }
    
    @Test
    @DisplayName("Deve tratar erro de concorrência")
    void deveTratarErroDeConcorrencia() {
        // Given
        var command = [Comando].builder()
            .aggregateId(UUID.randomUUID().toString())
            .userId("user123")
            .[campo]("valor")
            .build();
            
        when(repository.getById(command.getAggregateId()))
            .thenThrow(new ConcurrencyException("Versão desatualizada"));
        
        // When
        CommandResult result = handler.handle(command);
        
        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("CONCURRENCY_CONFLICT");
    }
}
```

#### **✅ Checklist de Testes Unitários:**
- [ ] **Testes de agregados** cobrindo todas operações
- [ ] **Testes de command handlers** com cenários positivos e negativos
- [ ] **Testes de event handlers** validando processamento
- [ ] **Testes de business rules** com todas validações
- [ ] **Coverage mínimo** de 80% atingido

---

### **🔗 2. TESTES DE INTEGRAÇÃO**

#### **🌐 Testes de Command Bus:**
```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.write.url=jdbc:h2:mem:testdb",
    "spring.datasource.read.url=jdbc:h2:mem:testdb",
    "event-bus.type=simple"
})
class [Dominio]CommandIntegrationTest {
    
    @Autowired
    private CommandBus commandBus;
    
    @Autowired
    private AggregateRepository<[Dominio]Aggregate> repository;
    
    @Autowired
    private EventStore eventStore;
    
    @Test
    @DisplayName("Deve processar comando via Command Bus")
    void deveProcessarComandoViaCommandBus() {
        // Given
        var command = Criar[Dominio]Command.builder()
            .aggregateId(UUID.randomUUID().toString())
            .userId("test-user")
            .[campo]("valor teste")
            .build();
        
        // When
        CommandResult result = commandBus.send(command);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        
        // Validar que eventos foram persistidos
        List<EventStoreEntry> events = eventStore.getEventsForAggregate(
            command.getAggregateId()
        );
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getEventType())
            .isEqualTo([Dominio]CriadoEvent.class.getName());
    }
    
    @Test
    @DisplayName("Deve processar múltiplos comandos em sequência")
    void deveProcessarMultiplosComandosEmSequencia() {
        // Given
        String aggregateId = UUID.randomUUID().toString();
        
        var criarCmd = Criar[Dominio]Command.builder()
            .aggregateId(aggregateId)
            .userId("test-user")
            .[campo]("valor inicial")
            .build();
            
        var atualizarCmd = Atualizar[Dominio]Command.builder()
            .aggregateId(aggregateId)
            .userId("test-user")
            .[campo]("valor atualizado")
            .build();
        
        // When
        CommandResult result1 = commandBus.send(criarCmd);
        CommandResult result2 = commandBus.send(atualizarCmd);
        
        // Then
        assertThat(result1.isSuccess()).isTrue();
        assertThat(result2.isSuccess()).isTrue();
        
        [Dominio]Aggregate aggregate = repository.getById(aggregateId);
        assertThat(aggregate.get[Campo]()).isEqualTo("valor atualizado");
        assertThat(aggregate.getVersion()).isEqualTo(2L);
    }
}
```

#### **📡 Testes de Event Bus:**
```java
@SpringBootTest
@TestPropertySource(properties = {
    "event-bus.type=simple",
    "event-bus.enabled=true"
})
class [Dominio]EventIntegrationTest {
    
    @Autowired
    private EventBus eventBus;
    
    @Autowired
    private EventHandlerRegistry handlerRegistry;
    
    private List<DomainEvent> processedEvents;
    
    @BeforeEach
    void setUp() {
        processedEvents = new ArrayList<>();
        
        // Registrar handler de teste
        handlerRegistry.register(new EventHandler<[Dominio]CriadoEvent>() {
            @Override
            public void handle([Dominio]CriadoEvent event) {
                processedEvents.add(event);
            }
            
            @Override
            public Class<[Dominio]CriadoEvent> getEventType() {
                return [Dominio]CriadoEvent.class;
            }
        });
    }
    
    @Test
    @DisplayName("Deve publicar e processar evento via Event Bus")
    void devePublicarEProcessarEventoViaEventBus() throws InterruptedException {
        // Given
        var event = new [Dominio]CriadoEvent(
            UUID.randomUUID().toString(),
            1L,
            "valor teste"
        );
        
        // When
        eventBus.publish(event);
        
        // Aguardar processamento assíncrono
        Thread.sleep(1000);
        
        // Then
        assertThat(processedEvents).hasSize(1);
        assertThat(processedEvents.get(0).getAggregateId())
            .isEqualTo(event.getAggregateId());
    }
}
```

#### **🔄 Testes de Projeções:**
```java
@SpringBootTest
@Sql(scripts = "/test-data/clean-projections.sql", 
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class [Dominio]ProjectionIntegrationTest {
    
    @Autowired
    private [Dominio]ProjectionHandler projectionHandler;
    
    @Autowired
    private [Dominio]QueryRepository queryRepository;
    
    @Test
    @DisplayName("Deve atualizar projeção ao processar evento")
    void deveAtualizarProjecaoAoProcessarEvento() {
        // Given
        String aggregateId = UUID.randomUUID().toString();
        var event = new [Dominio]CriadoEvent(aggregateId, 1L, "valor teste");
        
        // When
        projectionHandler.handle(event);
        
        // Then
        Optional<[Dominio]QueryModel> projection = queryRepository.findById(
            UUID.fromString(aggregateId)
        );
        
        assertThat(projection).isPresent();
        assertThat(projection.get().get[Campo]()).isEqualTo("valor teste");
    }
    
    @Test
    @DisplayName("Deve ser idempotente ao processar evento duplicado")
    void deveSerIdemotenteAoProcessarEventoDuplicado() {
        // Given
        String aggregateId = UUID.randomUUID().toString();
        var event = new [Dominio]CriadoEvent(aggregateId, 1L, "valor teste");
        
        // When
        projectionHandler.handle(event);
        projectionHandler.handle(event); // Duplicado
        
        // Then
        List<[Dominio]QueryModel> projections = queryRepository.findAll();
        assertThat(projections).hasSize(1); // Não duplicou
    }
}
```

#### **✅ Checklist de Testes de Integração:**
- [ ] **Testes de Command Bus** com comandos reais
- [ ] **Testes de Event Bus** com eventos reais
- [ ] **Testes de projeções** com banco de dados
- [ ] **Testes de idempotência** validados
- [ ] **Testes de concorrência** implementados

---

### **📜 3. TESTES DE CONTRATO**

#### **🤝 Testes de API (Consumer-Driven Contracts):**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class [Dominio]ApiContractTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("POST /api/v1/[dominio] - deve criar com contrato válido")
    void postDeveResponderComContratoValido() throws Exception {
        // Given
        var request = Criar[Dominio]Request.builder()
            .[campo]("valor teste")
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/[dominio]")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.[campo]").value("valor teste"))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(header().exists("Location"));
    }
    
    @Test
    @DisplayName("GET /api/v1/[dominio]/{id} - deve retornar com contrato válido")
    void getDeveRetornarComContratoValido() throws Exception {
        // Given
        String id = UUID.randomUUID().toString();
        // Criar dados de teste...
        
        // When & Then
        mockMvc.perform(get("/api/v1/[dominio]/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.[campo]").exists())
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists());
    }
    
    @Test
    @DisplayName("POST /api/v1/[dominio] - deve validar request inválido")
    void postDeveValidarRequestInvalido() throws Exception {
        // Given
        var invalidRequest = Criar[Dominio]Request.builder()
            // Campo obrigatório omitido
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/v1/[dominio]")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors[0].field").exists())
            .andExpect(jsonPath("$.errors[0].message").exists());
    }
}
```

#### **✅ Checklist de Testes de Contrato:**
- [ ] **Contratos de request** validados
- [ ] **Contratos de response** verificados
- [ ] **Validações de erro** testadas
- [ ] **Headers esperados** validados
- [ ] **Status codes** corretos

---

### **🌐 4. TESTES END-TO-END**

#### **🎯 Cenários Completos:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.write.url=jdbc:h2:mem:e2edb",
    "spring.datasource.read.url=jdbc:h2:mem:e2edb"
})
class [Dominio]E2ETest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private [Dominio]QueryRepository queryRepository;
    
    @Test
    @DisplayName("Fluxo completo: Criar -> Consultar -> Atualizar -> Consultar")
    void fluxoCompletoDeOperacoes() throws InterruptedException {
        // ========== 1. CRIAR ==========
        var criarRequest = Criar[Dominio]Request.builder()
            .[campo]("valor inicial")
            .build();
        
        ResponseEntity<[Dominio]Response> criarResponse = restTemplate.postForEntity(
            "/api/v1/[dominio]",
            criarRequest,
            [Dominio]Response.class
        );
        
        assertThat(criarResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String id = criarResponse.getBody().getId();
        
        // Aguardar eventual consistency
        Thread.sleep(1000);
        
        // ========== 2. CONSULTAR (após criação) ==========
        ResponseEntity<[Dominio]Response> consultaResponse1 = restTemplate.getForEntity(
            "/api/v1/[dominio]/" + id,
            [Dominio]Response.class
        );
        
        assertThat(consultaResponse1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(consultaResponse1.getBody().get[Campo]()).isEqualTo("valor inicial");
        
        // ========== 3. ATUALIZAR ==========
        var atualizarRequest = Atualizar[Dominio]Request.builder()
            .[campo]("valor atualizado")
            .build();
        
        restTemplate.put(
            "/api/v1/[dominio]/" + id,
            atualizarRequest
        );
        
        // Aguardar eventual consistency
        Thread.sleep(1000);
        
        // ========== 4. CONSULTAR (após atualização) ==========
        ResponseEntity<[Dominio]Response> consultaResponse2 = restTemplate.getForEntity(
            "/api/v1/[dominio]/" + id,
            [Dominio]Response.class
        );
        
        assertThat(consultaResponse2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(consultaResponse2.getBody().get[Campo]()).isEqualTo("valor atualizado");
        
        // ========== 5. VALIDAR PROJEÇÃO ==========
        Optional<[Dominio]QueryModel> projection = queryRepository.findById(UUID.fromString(id));
        assertThat(projection).isPresent();
        assertThat(projection.get().get[Campo]()).isEqualTo("valor atualizado");
    }
    
    @Test
    @DisplayName("Cenário de eventual consistency: Write -> Wait -> Read")
    void cenarioDeEventualConsistency() throws InterruptedException {
        // Given
        var request = Criar[Dominio]Request.builder()
            .[campo]("teste consistency")
            .build();
        
        // When - Write
        ResponseEntity<[Dominio]Response> createResponse = restTemplate.postForEntity(
            "/api/v1/[dominio]",
            request,
            [Dominio]Response.class
        );
        String id = createResponse.getBody().getId();
        
        // Imediatamente após criação, projeção pode não estar pronta
        Optional<[Dominio]QueryModel> immediate = queryRepository.findById(UUID.fromString(id));
        
        // Aguardar propagação
        Thread.sleep(2000);
        
        // Then - Read (após eventual consistency)
        Optional<[Dominio]QueryModel> eventual = queryRepository.findById(UUID.fromString(id));
        assertThat(eventual).isPresent();
        assertThat(eventual.get().get[Campo]()).isEqualTo("teste consistency");
    }
}
```

#### **✅ Checklist de Testes E2E:**
- [ ] **Fluxos completos** testados (criar → consultar → atualizar)
- [ ] **Eventual consistency** validada
- [ ] **Cenários de erro** end-to-end
- [ ] **Performance** aceitável em fluxos completos
- [ ] **Rollback** e recuperação testados

---

### **⚡ 5. TESTES DE PERFORMANCE**

#### **📊 Testes de Carga:**
```java
@SpringBootTest
class [Dominio]PerformanceTest {
    
    @Autowired
    private CommandBus commandBus;
    
    @Test
    @DisplayName("Deve processar 1000 comandos em menos de 10 segundos")
    void deveProcessarMilComandosRapidamente() {
        // Given
        int totalCommands = 1000;
        List<[Criar][Dominio]Command> commands = new ArrayList<>();
        
        for (int i = 0; i < totalCommands; i++) {
            commands.add([Criar][Dominio]Command.builder()
                .aggregateId(UUID.randomUUID().toString())
                .userId("perf-test")
                .[campo]("valor " + i)
                .build());
        }
        
        // When
        long startTime = System.currentTimeMillis();
        
        commands.parallelStream().forEach(commandBus::send);
        
        long duration = System.currentTimeMillis() - startTime;
        
        // Then
        assertThat(duration).isLessThan(10000); // 10 segundos
        
        double throughput = (totalCommands / (duration / 1000.0));
        System.out.println("Throughput: " + throughput + " comandos/segundo");
        assertThat(throughput).isGreaterThan(100); // Mínimo 100 comandos/s
    }
    
    @Test
    @DisplayName("Consultas devem responder em menos de 100ms (p95)")
    void consultasDevemSerRapidas() {
        // Given
        List<Long> durations = new ArrayList<>();
        
        // When
        for (int i = 0; i < 1000; i++) {
            long start = System.nanoTime();
            queryService.findAll(PageRequest.of(0, 10));
            long duration = (System.nanoTime() - start) / 1_000_000; // ms
            durations.add(duration);
        }
        
        // Then
        Collections.sort(durations);
        long p95 = durations.get((int) (durations.size() * 0.95));
        
        assertThat(p95).isLessThan(100); // p95 < 100ms
    }
}
```

#### **✅ Checklist de Testes de Performance:**
- [ ] **Throughput** de comandos validado (>100/s)
- [ ] **Latência** de consultas aceitável (<100ms p95)
- [ ] **Connection pool** não esgota sob carga
- [ ] **Memória** estável sem leaks
- [ ] **CPU** não satura (<80%)

---

### **🛡️ 6. CONFIGURAÇÃO DE QUALIDADE**

#### **📊 SonarQube Configuration:**
```properties
# sonar-project.properties
sonar.projectKey=seguradora-hibrida-[dominio]
sonar.projectName=Seguradora Híbrida - [Dominio]
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.java.test.binaries=target/test-classes
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml

# Thresholds
sonar.coverage.exclusions=**/*Config.java,**/*Properties.java,**/*Application.java
sonar.cpd.exclusions=**/*Test.java
```

#### **🎯 JaCoCo Configuration:**
```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>jacoco-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

#### **✅ Checklist de Configuração:**
- [ ] **JaCoCo** configurado com threshold de 80%
- [ ] **SonarQube** integrado ao pipeline
- [ ] **Mutation testing** configurado (opcional)
- [ ] **Relatórios** gerados automaticamente
- [ ] **Quality gates** definidos

---

## ✅ **CHECKPOINT DE VALIDAÇÃO**

### **🎯 Critérios de Aprovação:**

#### **🔬 Cobertura de Testes:**
- [ ] **Coverage total** >= 80%
- [ ] **Coverage de agregados** >= 90%
- [ ] **Coverage de handlers** >= 85%
- [ ] **Coverage de controllers** >= 80%
- [ ] **Branch coverage** >= 75%

#### **🧪 Qualidade dos Testes:**
- [ ] **Testes unitários** rápidos (<1s total)
- [ ] **Testes de integração** funcionais
- [ ] **Testes E2E** cobrindo fluxos principais
- [ ] **Testes de contrato** validados
- [ ] **Todos os testes** passando (0 falhas)

#### **📊 Métricas de Qualidade:**
- [ ] **Code smells** < 10
- [ ] **Bugs** = 0
- [ ] **Vulnerabilities** = 0
- [ ] **Technical debt** < 5%
- [ ] **Duplicações** < 3%

#### **📚 Documentação:**
- [ ] **Testes documentados** com DisplayName
- [ ] **Cenários complexos** comentados
- [ ] **Test data builders** implementados
- [ ] **Fixtures** organizadas e reutilizáveis
- [ ] **README** de testes atualizado

---

## 🚨 **PONTOS DE ATENÇÃO**

### **⚠️ Armadilhas Comuns:**

#### **🚫 Testes Frágeis:**
```java
// ❌ EVITAR: Testes dependentes de ordem
@Test
void teste1() {
    service.create("id1", "valor1");
}

@Test
void teste2() {
    // Depende de teste1 ter executado
    var result = service.findById("id1");
    assertThat(result).isPresent();
}

// ✅ PREFERIR: Testes isolados
@BeforeEach
void setUp() {
    service.create("id1", "valor1");
}

@Test
void teste2() {
    var result = service.findById("id1");
    assertThat(result).isPresent();
}
```

#### **🚫 Testes Lentos:**
```java
// ❌ EVITAR: Sleep desnecessário
@Test
void teste() {
    eventBus.publish(event);
    Thread.sleep(5000); // Muito tempo
    verify(handler).handle(event);
}

// ✅ PREFERIR: Await com timeout
@Test
void teste() {
    eventBus.publish(event);
    await().atMost(Duration.ofSeconds(2))
        .untilAsserted(() -> verify(handler).handle(event));
}
```

#### **🚫 Testes Sem Assertions:**
```java
// ❌ EVITAR: Teste que não valida nada
@Test
void teste() {
    service.process();
    // Sem validações!
}

// ✅ PREFERIR: Validações explícitas
@Test
void teste() {
    service.process();
    assertThat(service.getStatus()).isEqualTo(Status.PROCESSED);
    verify(repository).save(any());
}
```

### **✅ Boas Práticas:**

#### **🎯 Estrutura de Testes:**
- **Sempre** usar padrão Given-When-Then
- **Sempre** nomear testes descritivamente
- **Sempre** isolar testes (sem dependências)
- **Sempre** limpar dados de teste (@BeforeEach/@AfterEach)

#### **🔬 Qualidade dos Testes:**
- **Sempre** testar casos de sucesso E falha
- **Sempre** validar comportamentos, não implementações
- **Sempre** usar builders para test data
- **Sempre** mockar dependências externas

---

## 🔄 **PRÓXIMOS PASSOS**

### **✅ Após Aprovação do Checkpoint:**
1. **[Etapa 09 - Monitoramento & Métricas](./09-monitoramento-metricas.md)**
2. Configurar observabilidade
3. Implementar dashboards
4. Configurar alertas

### **📋 Preparação para Próxima Etapa:**
- [ ] **Métricas** compreendidas
- [ ] **Observabilidade** revisada
- [ ] **Ferramentas** de monitoramento estudadas
- [ ] **Todos os testes** passando

---

## 📚 **RECURSOS DE APOIO**

### **📖 Documentação de Referência:**
- **[Testes](../10-testes-README.md)**: Guia completo de testes
- **JUnit 5**: Documentação oficial
- **Mockito**: Documentação de mocking

### **🛠️ Ferramentas de Teste:**
- **JUnit 5**: Framework de testes
- **Mockito**: Mocking framework
- **AssertJ**: Assertions fluentes
- **TestContainers**: Containers para testes
- **RestAssured**: Testes de API
- **Awaitility**: Testes assíncronos

### **📊 Ferramentas de Qualidade:**
- **JaCoCo**: Coverage reports
- **SonarQube**: Análise de qualidade
- **Mutation Testing**: PIT framework

### **🧪 Exemplos de Teste:**
- **ExampleAggregateTest**: Testes unitários
- **ExampleCommandHandlerTest**: Testes de handlers
- **ExampleIntegrationTest**: Testes de integração

---

**📋 Checklist Total:** 100+ itens de validação  
**⏱️ Tempo Médio:** 4-6 horas  
**🎯 Resultado:** Suite completa de testes com coverage >= 80%  
**✅ Próxima Etapa:** Monitoramento & Métricas