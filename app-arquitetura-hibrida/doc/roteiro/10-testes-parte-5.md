# 🧪 TESTES E BOAS PRÁTICAS - PARTE 5
## Testes E2E e Automação

### 🎯 **OBJETIVOS DESTA PARTE**
- Implementar testes End-to-End completos
- Configurar automação de testes
- Estabelecer pipelines de CI/CD
- Definir estratégias de monitoramento de testes

---

## 🌐 **TESTES END-TO-END (E2E)**

### **📋 Estratégia de Testes E2E**

Testes E2E validam fluxos completos da aplicação do ponto de vista do usuário:

#### **Características dos Testes E2E:**
- ✅ **Realismo**: Simulam uso real da aplicação
- ✅ **Integração**: Testam todos os componentes juntos
- ✅ **Confiança**: Maior garantia de funcionamento
- ❌ **Lentidão**: Executam mais devagar
- ❌ **Fragilidade**: Mais sujeitos a falhas

#### **Quando Usar Testes E2E:**
- Fluxos críticos de negócio
- Jornadas completas do usuário
- Validação de releases
- Testes de regressão

### **🔧 Configuração Base para Testes E2E**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Testes E2E - Fluxo Completo de Sinistros")
class SinistroE2ETest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    // Containers para ambiente completo
    @Container
    static PostgreSQLContainer<?> eventStoreDb = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("eventstore_e2e")
            .withUsername("eventstore")
            .withPassword("eventstore123")
            .withInitScript("db/migration/V1__Create_Events_Table.sql");
    
    @Container
    static PostgreSQLContainer<?> projectionsDb = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("projections_e2e")
            .withUsername("projections")
            .withPassword("projections123")
            .withInitScript("db/migration-projections/V1__Create_Projections_Schema.sql");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("app.datasource.write.url", eventStoreDb::getJdbcUrl);
        registry.add("app.datasource.write.username", eventStoreDb::getUsername);
        registry.add("app.datasource.write.password", eventStoreDb::getPassword);
        
        registry.add("app.datasource.read.url", projectionsDb::getJdbcUrl);
        registry.add("app.datasource.read.username", projectionsDb::getUsername);
        registry.add("app.datasource.read.password", projectionsDb::getPassword);
        
        // Configurações para testes
        registry.add("app.eventbus.enabled", () -> "true");
        registry.add("app.projection.enabled", () -> "true");
        registry.add("logging.level.com.seguradora.hibrida", () -> "DEBUG");
    }
    
    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
```

---

## 🎭 **CENÁRIOS DE TESTE E2E**

### **📝 Cenário 1: Jornada Completa do Sinistro**

```java
@Nested
@DisplayName("Jornada Completa do Sinistro")
class JornadaCompletaSinistro {
    
    private static String sinistroId;
    private static String protocolo;
    
    @Test
    @Order(1)
    @DisplayName("1. Deve criar novo sinistro via API")
    void deveCriarNovoSinistroViaApi() {
        // Given
        Map<String, Object> criarSinistroRequest = Map.of(
            "protocolo", "SIN-E2E-001",
            "cpfSegurado", "12345678901",
            "nomeSegurado", "João Silva E2E",
            "descricao", "Colisão traseira na Av. Paulista - Teste E2E",
            "valorEstimado", 8500.00,
            "placa", "E2E1234",
            "tipoSinistro", "COLISAO",
            "dataOcorrencia", Instant.now().minus(2, ChronoUnit.DAYS).toString(),
            "enderecoOcorrencia", "Av. Paulista, 1000 - São Paulo/SP",
            "canalAbertura", "PORTAL_WEB"
        );
        
        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl() + "/api/sinistros",
            criarSinistroRequest,
            Map.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("success")).isEqualTo(true);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
        sinistroId = (String) data.get("id");
        protocolo = (String) data.get("protocolo");
        
        assertThat(sinistroId).isNotNull();
        assertThat(protocolo).isEqualTo("SIN-E2E-001");
    }
    
    @Test
    @Order(2)
    @DisplayName("2. Deve consultar sinistro criado")
    void deveConsultarSinistroCriado() {
        // Aguarda processamento das projeções
        await()
            .atMost(Duration.ofSeconds(15))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted(() -> {
                // When
                ResponseEntity<Map> response = restTemplate.getForEntity(
                    baseUrl() + "/api/sinistros/{id}",
                    Map.class,
                    sinistroId
                );
                
                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                
                Map<String, Object> sinistro = response.getBody();
                assertThat(sinistro).isNotNull();
                assertThat(sinistro.get("protocolo")).isEqualTo("SIN-E2E-001");
                assertThat(sinistro.get("status")).isEqualTo("ABERTO");
                assertThat(sinistro.get("cpfSegurado")).isEqualTo("12345678901");
                assertThat(sinistro.get("valorEstimado")).isEqualTo(8500.0);
            });
    }
    
    @Test
    @Order(3)
    @DisplayName("3. Deve atualizar sinistro para análise")
    void deveAtualizarSinistroParaAnalise() {
        // Given
        Map<String, Object> atualizarRequest = Map.of(
            "novoStatus", "EM_ANALISE",
            "novaDescricao", "Análise técnica iniciada - aguardando laudo pericial",
            "operadorResponsavel", "Maria Santos",
            "prioridade", "ALTA"
        );
        
        // When
        restTemplate.put(
            baseUrl() + "/api/sinistros/{id}",
            atualizarRequest,
            sinistroId
        );
        
        // Then - verifica atualização
        await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted(() -> {
                ResponseEntity<Map> response = restTemplate.getForEntity(
                    baseUrl() + "/api/sinistros/{id}",
                    Map.class,
                    sinistroId
                );
                
                Map<String, Object> sinistro = response.getBody();
                assertThat(sinistro.get("status")).isEqualTo("EM_ANALISE");
                assertThat(sinistro.get("operadorResponsavel")).isEqualTo("Maria Santos");
                assertThat(sinistro.get("prioridade")).isEqualTo("ALTA");
            });
    }
    
    @Test
    @Order(4)
    @DisplayName("4. Deve iniciar consulta DETRAN")
    void deveIniciarConsultaDetran() {
        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl() + "/api/sinistros/{id}/consulta-detran",
            null,
            Map.class,
            sinistroId
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        
        // Aguarda processamento da consulta
        await()
            .atMost(Duration.ofSeconds(20))
            .pollInterval(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                ResponseEntity<Map> consultaResponse = restTemplate.getForEntity(
                    baseUrl() + "/api/sinistros/{id}",
                    Map.class,
                    sinistroId
                );
                
                Map<String, Object> sinistro = consultaResponse.getBody();
                assertThat(sinistro.get("consultaDetranRealizada")).isEqualTo(true);
                assertThat(sinistro.get("consultaDetranStatus")).isIn("SUCESSO", "ERROR");
            });
    }
    
    @Test
    @Order(5)
    @DisplayName("5. Deve finalizar sinistro")
    void deveFinalizarSinistro() {
        // Given
        Map<String, Object> finalizarRequest = Map.of(
            "novoStatus", "FECHADO",
            "valorFinal", 9200.00,
            "observacoesFechamento", "Sinistro finalizado após análise técnica completa"
        );
        
        // When
        restTemplate.put(
            baseUrl() + "/api/sinistros/{id}/finalizar",
            finalizarRequest,
            sinistroId
        );
        
        // Then
        await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted(() -> {
                ResponseEntity<Map> response = restTemplate.getForEntity(
                    baseUrl() + "/api/sinistros/{id}",
                    Map.class,
                    sinistroId
                );
                
                Map<String, Object> sinistro = response.getBody();
                assertThat(sinistro.get("status")).isEqualTo("FECHADO");
                assertThat(sinistro.get("valorFinal")).isEqualTo(9200.0);
                assertThat(sinistro.get("dataFechamento")).isNotNull();
            });
    }
    
    @Test
    @Order(6)
    @DisplayName("6. Deve aparecer nas consultas e relatórios")
    void deveAparecerNasConsultasERelatorios() {
        // Consulta por CPF
        ResponseEntity<Map> responseCpf = restTemplate.getForEntity(
            baseUrl() + "/api/sinistros/segurado/{cpf}?page=0&size=10",
            Map.class,
            "12345678901"
        );
        
        assertThat(responseCpf.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> pageCpf = responseCpf.getBody();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> contentCpf = (List<Map<String, Object>>) pageCpf.get("content");
        
        boolean encontradoPorCpf = contentCpf.stream()
            .anyMatch(s -> protocolo.equals(s.get("protocolo")));
        assertThat(encontradoPorCpf).isTrue();
        
        // Consulta por placa
        ResponseEntity<Map> responsePlaca = restTemplate.getForEntity(
            baseUrl() + "/api/sinistros/placa/{placa}?page=0&size=10",
            Map.class,
            "E2E1234"
        );
        
        assertThat(responsePlaca.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Dashboard
        ResponseEntity<Map> responseDashboard = restTemplate.getForEntity(
            baseUrl() + "/api/sinistros/dashboard",
            Map.class
        );
        
        assertThat(responseDashboard.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        Map<String, Object> dashboard = responseDashboard.getBody();
        assertThat(dashboard.get("totalSinistros")).isNotNull();
        assertThat(dashboard.get("sinistrosFechados")).isNotNull();
    }
}
```

### **📝 Cenário 2: Fluxos de Erro e Recuperação**

```java
@Nested
@DisplayName("Fluxos de Erro e Recuperação")
class FluxosErroRecuperacao {
    
    @Test
    @DisplayName("Deve tratar erro de validação na criação")
    void deveTratarErroValidacaoNaCriacao() {
        // Given - dados inválidos
        Map<String, Object> requestInvalido = Map.of(
            "protocolo", "", // Protocolo vazio
            "cpfSegurado", "123", // CPF inválido
            "valorEstimado", -1000.0 // Valor negativo
        );
        
        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl() + "/api/sinistros",
            requestInvalido,
            Map.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        Map<String, Object> errorResponse = response.getBody();
        assertThat(errorResponse.get("success")).isEqualTo(false);
        assertThat(errorResponse.get("errors")).isNotNull();
    }
    
    @Test
    @DisplayName("Deve tratar sinistro não encontrado")
    void deveTratarSinistroNaoEncontrado() {
        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl() + "/api/sinistros/{id}",
            Map.class,
            UUID.randomUUID().toString()
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    
    @Test
    @DisplayName("Deve recuperar de falha temporária")
    void deveRecuperarDeFalhaTemporaria() {
        // Este teste simula uma falha temporária e recuperação
        // Em um ambiente real, você poderia parar/iniciar containers
        
        // Given - cria sinistro
        Map<String, Object> criarRequest = Map.of(
            "protocolo", "SIN-RECOVERY-001",
            "cpfSegurado", "98765432100",
            "nomeSegurado", "Teste Recovery",
            "descricao", "Teste de recuperação",
            "valorEstimado", 5000.0,
            "placa", "REC1234",
            "tipoSinistro", "COLISAO"
        );
        
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
            baseUrl() + "/api/sinistros",
            criarRequest,
            Map.class
        );
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) createResponse.getBody().get("data");
        String sinistroId = (String) data.get("id");
        
        // When - tenta consultar (deve funcionar após processamento)
        await()
            .atMost(Duration.ofSeconds(15))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted(() -> {
                ResponseEntity<Map> response = restTemplate.getForEntity(
                    baseUrl() + "/api/sinistros/{id}",
                    Map.class,
                    sinistroId
                );
                
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            });
    }
}
```

---

## 🔄 **AUTOMAÇÃO DE TESTES**

### **📝 Configuração de Pipeline CI/CD**

#### **GitHub Actions Workflow:**

```yaml
# .github/workflows/tests.yml
name: Testes Automatizados

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  unit-tests:
    name: Testes Unitários
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Executar Testes Unitários
      run: mvn test -Dtest="**/*Test" -DfailIfNoTests=false
    
    - name: Gerar Relatório de Cobertura
      run: mvn jacoco:report
    
    - name: Upload Coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: ./target/site/jacoco/jacoco.xml

  integration-tests:
    name: Testes de Integração
    runs-on: ubuntu-latest
    needs: unit-tests
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Executar Testes de Integração
      run: mvn test -Dtest="**/*IntegrationTest" -DfailIfNoTests=false
    
    - name: Upload Test Results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: integration-test-results
        path: target/surefire-reports/

  e2e-tests:
    name: Testes E2E
    runs-on: ubuntu-latest
    needs: integration-tests
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Executar Testes E2E
      run: mvn test -Dtest="**/*E2ETest" -DfailIfNoTests=false
    
    - name: Upload E2E Test Results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: e2e-test-results
        path: target/surefire-reports/

  quality-gate:
    name: Quality Gate
    runs-on: ubuntu-latest
    needs: [unit-tests, integration-tests, e2e-tests]
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache SonarCloud packages
      uses: actions/cache@v3
      with:
        path: ~/.sonar/cache
        key: ${{ runner.os }}-sonar
    
    - name: Análise SonarCloud
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
      run: mvn sonar:sonar
```

### **📝 Configuração Maven para Diferentes Tipos de Teste**

```xml
<!-- pom.xml - Configuração de profiles para testes -->
<profiles>
    <!-- Profile para testes unitários -->
    <profile>
        <id>unit-tests</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <configuration>
                        <includes>
                            <include>**/*Test.java</include>
                        </includes>
                        <excludes>
                            <exclude>**/*IntegrationTest.java</exclude>
                            <exclude>**/*E2ETest.java</exclude>
                        </excludes>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
    
    <!-- Profile para testes de integração -->
    <profile>
        <id>integration-tests</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-failsafe-plugin</artifactId>
                    <configuration>
                        <includes>
                            <include>**/*IntegrationTest.java</include>
                        </includes>
                    </configuration>
                    <executions>
                        <execution>
                            <goals>
                                <goal>integration-test</goal>
                                <goal>verify</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>
    
    <!-- Profile para testes E2E -->
    <profile>
        <id>e2e-tests</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-failsafe-plugin</artifactId>
                    <configuration>
                        <includes>
                            <include>**/*E2ETest.java</include>
                        </includes>
                        <systemPropertyVariables>
                            <spring.profiles.active>e2e</spring.profiles.active>
                        </systemPropertyVariables>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

---

## 📊 **MONITORAMENTO E RELATÓRIOS**

### **📝 Configuração de Métricas de Teste**

```java
@Component
@ConditionalOnProperty(name = "app.testing.metrics.enabled", havingValue = "true")
public class TestMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    private final Counter testExecutions;
    private final Counter testFailures;
    private final Timer testDuration;
    
    public TestMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.testExecutions = Counter.builder("tests.executions")
            .description("Total number of test executions")
            .register(meterRegistry);
        this.testFailures = Counter.builder("tests.failures")
            .description("Total number of test failures")
            .register(meterRegistry);
        this.testDuration = Timer.builder("tests.duration")
            .description("Test execution duration")
            .register(meterRegistry);
    }
    
    public void recordTestExecution(String testClass, String testMethod, boolean success, Duration duration) {
        testExecutions.increment(
            Tags.of(
                "class", testClass,
                "method", testMethod,
                "success", String.valueOf(success)
            )
        );
        
        if (!success) {
            testFailures.increment(
                Tags.of("class", testClass, "method", testMethod)
            );
        }
        
        testDuration.record(duration, 
            Tags.of("class", testClass, "method", testMethod)
        );
    }
}
```

### **📝 Listener de Testes para Coleta de Métricas**

```java
public class TestMetricsListener implements TestExecutionListener {
    
    private static final Logger logger = LoggerFactory.getLogger(TestMetricsListener.class);
    private final Map<String, Instant> testStartTimes = new ConcurrentHashMap<>();
    
    @Override
    public void testExecutionStarted(TestIdentifier testIdentifier) {
        testStartTimes.put(testIdentifier.getUniqueId(), Instant.now());
        logger.debug("Iniciando teste: {}", testIdentifier.getDisplayName());
    }
    
    @Override
    public void testExecutionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        Instant startTime = testStartTimes.remove(testIdentifier.getUniqueId());
        if (startTime != null) {
            Duration duration = Duration.between(startTime, Instant.now());
            boolean success = testExecutionResult.getStatus() == TestExecutionResult.Status.SUCCESSFUL;
            
            logger.info("Teste finalizado: {} - Status: {} - Duração: {}ms", 
                testIdentifier.getDisplayName(), 
                testExecutionResult.getStatus(),
                duration.toMillis()
            );
            
            // Aqui você pode enviar métricas para sistemas de monitoramento
            recordTestMetrics(testIdentifier, success, duration);
        }
    }
    
    private void recordTestMetrics(TestIdentifier testIdentifier, boolean success, Duration duration) {
        // Implementar envio de métricas para Prometheus, DataDog, etc.
    }
}
```

---

## 📈 **RELATÓRIOS E DASHBOARDS**

### **📝 Configuração de Relatórios HTML**

```xml
<!-- Plugin para relatórios HTML -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-report-plugin</artifactId>
    <version>3.0.0-M9</version>
    <configuration>
        <outputDirectory>${project.build.directory}/surefire-reports-html</outputDirectory>
    </configuration>
</plugin>

<!-- Plugin JaCoCo para cobertura -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
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
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>INSTRUCTION</counter>
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

### **📝 Dashboard de Testes (Grafana)**

```yaml
# docker-compose.yml para monitoramento
version: '3.8'
services:
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
  
  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - ./monitoring/grafana/dashboards:/var/lib/grafana/dashboards
      - ./monitoring/grafana/provisioning:/etc/grafana/provisioning
```

---

## 🚀 **ESTRATÉGIAS DE OTIMIZAÇÃO**

### **📝 Paralelização de Testes**

```xml
<!-- Configuração para execução paralela -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>methods</parallel>
        <threadCount>4</threadCount>
        <perCoreThreadCount>true</perCoreThreadCount>
        <useUnlimitedThreads>false</useUnlimitedThreads>
    </configuration>
</plugin>
```

### **📝 Cache de Dependências**

```java
@TestConfiguration
public class TestCacheConfiguration {
    
    @Bean
    @Primary
    public CacheManager testCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
    
    @Bean
    @ConditionalOnProperty(name = "app.testing.cache.enabled", havingValue = "true")
    public TestDataCache testDataCache() {
        return new TestDataCache();
    }
}

public class TestDataCache {
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        return (T) cache.get(key);
    }
    
    public void put(String key, Object value) {
        cache.put(key, value);
    }
    
    public void clear() {
        cache.clear();
    }
}
```

---

## 📋 **CHECKLIST DE QUALIDADE**

### **✅ Checklist para Testes E2E:**

#### **Preparação:**
- [ ] Ambiente isolado configurado
- [ ] Dados de teste preparados
- [ ] Containers inicializados
- [ ] Configurações de teste aplicadas

#### **Execução:**
- [ ] Fluxos críticos cobertos
- [ ] Cenários de erro testados
- [ ] Performance validada
- [ ] Logs coletados

#### **Validação:**
- [ ] Resultados verificados
- [ ] Métricas coletadas
- [ ] Relatórios gerados
- [ ] Cleanup executado

### **📊 Métricas de Qualidade:**

| **Métrica** | **Meta** | **Descrição** |
|-------------|----------|---------------|
| **Cobertura E2E** | 80%+ | Cobertura de fluxos críticos |
| **Tempo Execução** | < 15 min | Tempo total dos testes E2E |
| **Taxa Sucesso** | 95%+ | Percentual de testes passando |
| **Flakiness** | < 5% | Testes instáveis |

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [Spring Boot Testing Best Practices](https://spring.io/guides/gs/testing-web/)
- [TestContainers Best Practices](https://www.testcontainers.org/test_framework_integration/junit_5/)
- [GitHub Actions for Java](https://docs.github.com/en/actions/automating-builds-and-tests/building-and-testing-java-with-maven)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)

### **📖 Resumo do Módulo:**
- **Parte 1**: Fundamentos de Testes
- **Parte 2**: Testes de Handlers
- **Parte 3**: Testes de Projeções e Consultas
- **Parte 4**: Testes de Integração com TestContainers
- **Parte 5**: Testes E2E e Automação ✅

---

## 🎯 **PRÓXIMOS PASSOS**

Após completar este módulo de testes, você deve:

1. **Implementar testes** para os componentes do core de sinistros
2. **Configurar pipeline** de CI/CD no seu projeto
3. **Estabelecer métricas** de qualidade e monitoramento
4. **Documentar estratégias** de teste para a equipe

---

**📝 Parte 5 de 5 - Testes E2E e Automação**  
**⏱️ Tempo estimado**: 90 minutos  
**🎯 Próximo**: [Monitoramento e Observabilidade](./11-monitoramento-parte-1.md)