# 🏗️ AGREGADOS - PARTE 5: TESTES E BOAS PRÁTICAS
## Roteiro Técnico para Analistas Java Junior

### 🎯 **OBJETIVO DESTA PARTE**
Dominar estratégias de teste para agregados, debugging, troubleshooting e consolidar as melhores práticas de implementação.

---

## 🧪 **ESTRATÉGIAS DE TESTE PARA AGREGADOS**

### **📋 Tipos de Teste**

```
Testes de Agregados
├── Testes Unitários (Comportamento isolado)
├── Testes de Integração (Com Event Store)
├── Testes de Regras de Negócio (Validações)
├── Testes de Performance (Snapshots/Replay)
└── Testes de Concorrência (Versionamento)
```

### **🎯 Testes Unitários - Comportamento**

```java
@ExtendWith(MockitoExtension.class)
class SinistroAggregateTest {
    
    @Mock
    private BusinessRuleValidator ruleValidator;
    
    private SinistroAggregate sinistro;
    
    @BeforeEach
    void setUp() {
        // Mock validador para sempre retornar válido nos testes unitários
        when(ruleValidator.validate(any(AggregateRoot.class)))
            .thenReturn(ValidationResult.valid());
        
        sinistro = new SinistroAggregate("sinistro-123", ruleValidator);
    }
    
    @Test
    @DisplayName("Deve criar sinistro com dados válidos")
    void deveCriarSinistroComDadosValidos() {
        // Given
        String numeroSinistro = "SIN-2024-001234";
        String cpfSegurado = "12345678901";
        String descricao = "Colisão na Av. Paulista";
        LocalDate dataOcorrencia = LocalDate.now().minusDays(1);
        BigDecimal valorEstimado = new BigDecimal("5000.00");
        
        // When
        sinistro.criar(numeroSinistro, cpfSegurado, descricao, dataOcorrencia, valorEstimado);
        
        // Then
        assertThat(sinistro.getNumeroSinistro()).isEqualTo(numeroSinistro);
        assertThat(sinistro.getCpfSegurado()).isEqualTo(cpfSegurado);
        assertThat(sinistro.getDescricao()).isEqualTo(descricao);
        assertThat(sinistro.getDataOcorrencia()).isEqualTo(dataOcorrencia);
        assertThat(sinistro.getValorEstimado()).isEqualByComparingTo(valorEstimado);
        assertThat(sinistro.getStatus()).isEqualTo(SinistroStatus.ABERTO);
        assertThat(sinistro.getVersion()).isEqualTo(1);
        assertThat(sinistro.hasUncommittedEvents()).isTrue();
        assertThat(sinistro.getUncommittedEvents()).hasSize(1);
        
        // Verificar evento gerado
        List<DomainEvent> events = sinistro.getUncommittedEvents();
        DomainEvent event = events.get(0);
        assertThat(event).isInstanceOf(SinistroCriadoEvent.class);
        assertThat(event.getAggregateId()).isEqualTo("sinistro-123");
        assertThat(event.getVersion()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Deve atualizar valor estimado")
    void deveAtualizarValorEstimado() {
        // Given - sinistro já criado
        sinistro.criar("SIN-2024-001234", "12345678901", "Teste", 
                      LocalDate.now(), new BigDecimal("5000"));
        sinistro.markEventsAsCommitted(); // Simular persistência
        
        BigDecimal novoValor = new BigDecimal("7500.00");
        
        // When
        sinistro.atualizarValorEstimado(novoValor);
        
        // Then
        assertThat(sinistro.getValorEstimado()).isEqualByComparingTo(novoValor);
        assertThat(sinistro.getVersion()).isEqualTo(2);
        assertThat(sinistro.getUncommittedEvents()).hasSize(1);
        
        DomainEvent event = sinistro.getUncommittedEvents().get(0);
        assertThat(event).isInstanceOf(SinistroValorAtualizadoEvent.class);
    }
    
    @Test
    @DisplayName("Deve rejeitar valor estimado inválido")
    void deveRejeitarValorEstimadoInvalido() {
        // Given
        sinistro.criar("SIN-2024-001234", "12345678901", "Teste", 
                      LocalDate.now(), new BigDecimal("5000"));
        
        // When & Then
        assertThatThrownBy(() -> sinistro.atualizarValorEstimado(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Valor estimado deve ser positivo");
        
        assertThatThrownBy(() -> sinistro.atualizarValorEstimado(BigDecimal.ZERO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Valor estimado deve ser positivo");
        
        assertThatThrownBy(() -> sinistro.atualizarValorEstimado(new BigDecimal("-100")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Valor estimado deve ser positivo");
    }
    
    @Test
    @DisplayName("Deve finalizar sinistro corretamente")
    void deveFinalizarSinistroCorretamente() {
        // Given
        sinistro.criar("SIN-2024-001234", "12345678901", "Teste", 
                      LocalDate.now(), new BigDecimal("5000"));
        sinistro.markEventsAsCommitted();
        
        BigDecimal valorFinal = new BigDecimal("4800.00");
        
        // When
        sinistro.finalizar(valorFinal);
        
        // Then
        assertThat(sinistro.getStatus()).isEqualTo(SinistroStatus.FINALIZADO);
        assertThat(sinistro.getValorFinal()).isEqualByComparingTo(valorFinal);
        assertThat(sinistro.getDataFechamento()).isNotNull();
        assertThat(sinistro.getVersion()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Deve ser idempotente para operações repetidas")
    void deveSerIdempotente() {
        // Given
        sinistro.criar("SIN-2024-001234", "12345678901", "Teste", 
                      LocalDate.now(), new BigDecimal("5000"));
        sinistro.markEventsAsCommitted();
        
        BigDecimal valorFinal = new BigDecimal("4800.00");
        
        // When - finalizar duas vezes
        sinistro.finalizar(valorFinal);
        long versaoAposPrimeiraFinalizacao = sinistro.getVersion();
        int eventosAposPrimeiraFinalizacao = sinistro.getUncommittedEvents().size();
        
        sinistro.finalizar(valorFinal); // Segunda chamada
        
        // Then - deve ser idempotente
        assertThat(sinistro.getVersion()).isEqualTo(versaoAposPrimeiraFinalizacao);
        assertThat(sinistro.getUncommittedEvents()).hasSize(eventosAposPrimeiraFinalizacao);
    }
    
    @Test
    @DisplayName("Deve reconstruir estado a partir de eventos")
    void deveReconstruirEstadoAPartirDeEventos() {
        // Given - lista de eventos históricos
        List<DomainEvent> eventos = Arrays.asList(
            new SinistroCriadoEvent("sinistro-123", "SIN-2024-001234", "12345678901", 
                                   "Colisão", LocalDate.now().minusDays(1), new BigDecimal("5000")),
            new SinistroValorAtualizadoEvent("sinistro-123", 2, new BigDecimal("7500")),
            new SinistroFinalizadoEvent("sinistro-123", 3, new BigDecimal("7200"))
        );
        
        // Configurar versões dos eventos
        for (int i = 0; i < eventos.size(); i++) {
            eventos.get(i).setVersion(i + 1);
            eventos.get(i).setTimestamp(Instant.now().minusSeconds(60 * (eventos.size() - i)));
        }
        
        // When
        SinistroAggregate sinistroReconstruido = new SinistroAggregate(ruleValidator);
        sinistroReconstruido.loadFromHistory(eventos);
        
        // Then
        assertThat(sinistroReconstruido.getAggregateId()).isEqualTo("sinistro-123");
        assertThat(sinistroReconstruido.getNumeroSinistro()).isEqualTo("SIN-2024-001234");
        assertThat(sinistroReconstruido.getValorEstimado()).isEqualByComparingTo(new BigDecimal("7500"));
        assertThat(sinistroReconstruido.getValorFinal()).isEqualByComparingTo(new BigDecimal("7200"));
        assertThat(sinistroReconstruido.getStatus()).isEqualTo(SinistroStatus.FINALIZADO);
        assertThat(sinistroReconstruido.getVersion()).isEqualTo(3);
        assertThat(sinistroReconstruido.hasUncommittedEvents()).isFalse();
    }
}
```

### **🔗 Testes de Integração com Event Store**

```java
@SpringBootTest
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SinistroAggregateIntegrationTest {
    
    @Autowired
    private AggregateRepository<SinistroAggregate> sinistroRepository;
    
    @Autowired
    private EventStore eventStore;
    
    @Autowired
    private BusinessRuleValidator ruleValidator;
    
    @Test
    @DisplayName("Deve persistir e carregar agregado completo")
    void devePersistirECarregarAgregadoCompleto() {
        // Given
        String aggregateId = UUID.randomUUID().toString();
        SinistroAggregate sinistro = new SinistroAggregate(aggregateId, ruleValidator);
        
        sinistro.criar("SIN-2024-001234", "12345678901", "Colisão na Av. Paulista",
                      LocalDate.now().minusDays(1), new BigDecimal("5000"));
        sinistro.atualizarValorEstimado(new BigDecimal("7500"));
        
        // When - salvar
        sinistroRepository.save(sinistro);
        
        // Then - carregar e verificar
        Optional<SinistroAggregate> carregado = sinistroRepository.findById(aggregateId);
        
        assertThat(carregado).isPresent();
        SinistroAggregate sinistroCarregado = carregado.get();
        
        assertThat(sinistroCarregado.getAggregateId()).isEqualTo(aggregateId);
        assertThat(sinistroCarregado.getNumeroSinistro()).isEqualTo("SIN-2024-001234");
        assertThat(sinistroCarregado.getValorEstimado()).isEqualByComparingTo(new BigDecimal("7500"));
        assertThat(sinistroCarregado.getVersion()).isEqualTo(2);
        assertThat(sinistroCarregado.hasUncommittedEvents()).isFalse();
    }
    
    @Test
    @DisplayName("Deve detectar conflito de concorrência")
    void deveDetectarConflitoDeConc correncia() {
        // Given
        String aggregateId = UUID.randomUUID().toString();
        SinistroAggregate sinistro1 = new SinistroAggregate(aggregateId, ruleValidator);
        SinistroAggregate sinistro2 = new SinistroAggregate(aggregateId, ruleValidator);
        
        sinistro1.criar("SIN-2024-001234", "12345678901", "Teste", 
                       LocalDate.now(), new BigDecimal("5000"));
        sinistroRepository.save(sinistro1);
        
        // Carregar duas instâncias do mesmo agregado
        SinistroAggregate instancia1 = sinistroRepository.getById(aggregateId);
        SinistroAggregate instancia2 = sinistroRepository.getById(aggregateId);
        
        // When - modificar ambas as instâncias
        instancia1.atualizarValorEstimado(new BigDecimal("6000"));
        instancia2.atualizarValorEstimado(new BigDecimal("7000"));
        
        // Salvar primeira instância
        sinistroRepository.save(instancia1);
        
        // Then - segunda instância deve gerar erro de concorrência
        assertThatThrownBy(() -> sinistroRepository.save(instancia2))
            .isInstanceOf(ConcurrencyException.class);
    }
    
    @Test
    @DisplayName("Deve carregar versão específica do agregado")
    void deveCarregarVersaoEspecifica() {
        // Given
        String aggregateId = UUID.randomUUID().toString();
        SinistroAggregate sinistro = new SinistroAggregate(aggregateId, ruleValidator);
        
        sinistro.criar("SIN-2024-001234", "12345678901", "Teste", 
                      LocalDate.now(), new BigDecimal("5000"));
        sinistroRepository.save(sinistro);
        
        sinistro.atualizarValorEstimado(new BigDecimal("7500"));
        sinistroRepository.save(sinistro);
        
        sinistro.finalizar(new BigDecimal("7200"));
        sinistroRepository.save(sinistro);
        
        // When - carregar versão específica
        Optional<SinistroAggregate> versao1 = sinistroRepository.findByIdAndVersion(aggregateId, 1);
        Optional<SinistroAggregate> versao2 = sinistroRepository.findByIdAndVersion(aggregateId, 2);
        Optional<SinistroAggregate> versao3 = sinistroRepository.findByIdAndVersion(aggregateId, 3);
        
        // Then
        assertThat(versao1).isPresent();
        assertThat(versao1.get().getVersion()).isEqualTo(1);
        assertThat(versao1.get().getValorEstimado()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(versao1.get().getStatus()).isEqualTo(SinistroStatus.ABERTO);
        
        assertThat(versao2).isPresent();
        assertThat(versao2.get().getVersion()).isEqualTo(2);
        assertThat(versao2.get().getValorEstimado()).isEqualByComparingTo(new BigDecimal("7500"));
        assertThat(versao2.get().getStatus()).isEqualTo(SinistroStatus.ABERTO);
        
        assertThat(versao3).isPresent();
        assertThat(versao3.get().getVersion()).isEqualTo(3);
        assertThat(versao3.get().getStatus()).isEqualTo(SinistroStatus.FINALIZADO);
    }
}
```

---

## 🧪 **TESTES DE REGRAS DE NEGÓCIO**

### **📋 Testes de BusinessRule**

```java
class BusinessRuleTest {
    
    @Test
    @DisplayName("CPF válido deve passar na validação")
    void cpfValidoDevePassarNaValidacao() {
        // Given
        CpfValidoRule rule = new CpfValidoRule();
        SeguradoAggregate segurado = new SeguradoAggregate();
        segurado.setCpf("11144477735"); // CPF válido
        
        // When
        boolean isValid = rule.isValid(segurado);
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    @DisplayName("CPF inválido deve falhar na validação")
    void cpfInvalidoDeveFalharNaValidacao() {
        // Given
        CpfValidoRule rule = new CpfValidoRule();
        SeguradoAggregate segurado = new SeguradoAggregate();
        
        // When & Then - CPF com todos os dígitos iguais
        segurado.setCpf("11111111111");
        assertThat(rule.isValid(segurado)).isFalse();
        
        // CPF com tamanho incorreto
        segurado.setCpf("123456789");
        assertThat(rule.isValid(segurado)).isFalse();
        
        // CPF null
        segurado.setCpf(null);
        assertThat(rule.isValid(segurado)).isFalse();
        
        // CPF com dígitos verificadores incorretos
        segurado.setCpf("12345678901");
        assertThat(rule.isValid(segurado)).isFalse();
    }
    
    @Test
    @DisplayName("Regra de valor máximo deve funcionar corretamente")
    void regraDeValorMaximoDeveFuncionarCorretamente() {
        // Given
        BigDecimal valorMaximo = new BigDecimal("100000");
        SinistroValorMaximoRule rule = new SinistroValorMaximoRule(valorMaximo);
        SinistroAggregate sinistro = new SinistroAggregate();
        
        // When & Then - valor dentro do limite
        sinistro.setValorEstimado(new BigDecimal("50000"));
        assertThat(rule.isValid(sinistro)).isTrue();
        
        // Valor no limite exato
        sinistro.setValorEstimado(valorMaximo);
        assertThat(rule.isValid(sinistro)).isTrue();
        
        // Valor acima do limite
        sinistro.setValorEstimado(new BigDecimal("150000"));
        assertThat(rule.isValid(sinistro)).isFalse();
        
        // Valor null (permitido)
        sinistro.setValorEstimado(null);
        assertThat(rule.isValid(sinistro)).isTrue();
    }
}

@ExtendWith(MockitoExtension.class)
class BusinessRuleValidatorTest {
    
    @Mock
    private SeguradoRepository seguradoRepository;
    
    private BusinessRuleValidator validator;
    
    @BeforeEach
    void setUp() {
        List<BusinessRule> rules = Arrays.asList(
            new CpfValidoRule(),
            new SinistroValorMaximoRule(new BigDecimal("100000")),
            new SeguradoAtivoRule(seguradoRepository)
        );
        
        validator = new BusinessRuleValidator(rules);
    }
    
    @Test
    @DisplayName("Deve validar agregado com sucesso quando todas as regras passam")
    void deveValidarAgregadoComSucessoQuandoTodasAsRegrasPassam() {
        // Given
        SinistroAggregate sinistro = new SinistroAggregate();
        sinistro.setCpfSegurado("11144477735");
        sinistro.setValorEstimado(new BigDecimal("50000"));
        
        // Mock segurado ativo
        SeguradoAggregate seguradoAtivo = new SeguradoAggregate();
        seguradoAtivo.setAtivo(true);
        when(seguradoRepository.findByCpf("11144477735"))
            .thenReturn(Optional.of(seguradoAtivo));
        
        // When
        ValidationResult result = validator.validate(sinistro);
        
        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrorMessages()).isEmpty();
    }
    
    @Test
    @DisplayName("Deve falhar validação quando regras são violadas")
    void deveFalharValidacaoQuandoRegrasVioladas() {
        // Given
        SinistroAggregate sinistro = new SinistroAggregate();
        sinistro.setCpfSegurado("12345678901"); // CPF inválido
        sinistro.setValorEstimado(new BigDecimal("150000")); // Valor acima do limite
        
        // Mock segurado inativo
        SeguradoAggregate seguradoInativo = new SeguradoAggregate();
        seguradoInativo.setAtivo(false);
        when(seguradoRepository.findByCpf("12345678901"))
            .thenReturn(Optional.of(seguradoInativo));
        
        // When
        ValidationResult result = validator.validate(sinistro);
        
        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessages()).hasSize(3);
        assertThat(result.getErrorMessages()).contains(
            "[CPF_VALIDO] CPF deve ser válido e estar no formato correto",
            "[SINISTRO_VALOR_MAXIMO] Valor do sinistro não pode exceder R$ 100.000,00",
            "[SEGURADO_ATIVO] Segurado deve estar ativo para abertura de sinistro"
        );
    }
}
```

---

## 🚀 **TESTES DE PERFORMANCE**

### **📊 Testes de Snapshot e Replay**

```java
@SpringBootTest
class AggregatePerformanceTest {
    
    @Autowired
    private AggregateRepository<SinistroAggregate> repository;
    
    @Autowired
    private EventStore eventStore;
    
    @Test
    @DisplayName("Deve ter performance aceitável com muitos eventos")
    void deveTerPerformanceAceitavelComMuitosEventos() {
        // Given
        String aggregateId = UUID.randomUUID().toString();
        SinistroAggregate sinistro = new SinistroAggregate(aggregateId);
        
        sinistro.criar("SIN-2024-001234", "12345678901", "Teste performance", 
                      LocalDate.now(), new BigDecimal("5000"));
        repository.save(sinistro);
        
        // Adicionar muitos eventos
        for (int i = 0; i < 1000; i++) {
            sinistro.atualizarValorEstimado(new BigDecimal("5000").add(new BigDecimal(i)));
            repository.save(sinistro);
        }
        
        // When - medir tempo de carregamento
        long startTime = System.currentTimeMillis();
        Optional<SinistroAggregate> carregado = repository.findById(aggregateId);
        long loadTime = System.currentTimeMillis() - startTime;
        
        // Then
        assertThat(carregado).isPresent();
        assertThat(carregado.get().getVersion()).isEqualTo(1001);
        assertThat(loadTime).isLessThan(5000); // Menos de 5 segundos
        
        log.info("Tempo de carregamento com 1001 eventos: {}ms", loadTime);
    }
    
    @Test
    @DisplayName("Snapshot deve melhorar performance significativamente")
    void snapshotDeveMelhorarPerformanceSignificativamente() {
        // Given
        String aggregateId = UUID.randomUUID().toString();
        SinistroAggregate sinistro = new SinistroAggregate(aggregateId);
        
        sinistro.criar("SIN-2024-001234", "12345678901", "Teste snapshot", 
                      LocalDate.now(), new BigDecimal("5000"));
        repository.save(sinistro);
        
        // Adicionar eventos até trigger de snapshot
        for (int i = 0; i < 150; i++) {
            sinistro.atualizarValorEstimado(new BigDecimal("5000").add(new BigDecimal(i)));
            repository.save(sinistro);
        }
        
        // Forçar criação de snapshot
        repository.createSnapshot(aggregateId);
        
        // Adicionar mais eventos após snapshot
        for (int i = 0; i < 50; i++) {
            sinistro.atualizarValorEstimado(new BigDecimal("6000").add(new BigDecimal(i)));
            repository.save(sinistro);
        }
        
        // When - medir tempo com snapshot
        long startTime = System.currentTimeMillis();
        Optional<SinistroAggregate> carregado = repository.findById(aggregateId);
        long loadTimeWithSnapshot = System.currentTimeMillis() - startTime;
        
        // Then
        assertThat(carregado).isPresent();
        assertThat(carregado.get().getVersion()).isEqualTo(201);
        assertThat(loadTimeWithSnapshot).isLessThan(1000); // Menos de 1 segundo
        
        log.info("Tempo de carregamento com snapshot (201 eventos): {}ms", loadTimeWithSnapshot);
    }
}
```

---

## 🔧 **DEBUGGING E TROUBLESHOOTING**

### **📝 Ferramentas de Debug**

```java
@Component
public class AggregateDebugger {
    
    private static final Logger log = LoggerFactory.getLogger(AggregateDebugger.class);
    
    /**
     * Gera relatório detalhado do estado do agregado.
     */
    public String generateDebugReport(AggregateRoot aggregate) {
        StringBuilder report = new StringBuilder();
        
        report.append("=== AGGREGATE DEBUG REPORT ===\n");
        report.append("Type: ").append(aggregate.getClass().getSimpleName()).append("\n");
        report.append("ID: ").append(aggregate.getAggregateId()).append("\n");
        report.append("Version: ").append(aggregate.getVersion()).append("\n");
        report.append("Created: ").append(aggregate.getCreatedAt()).append("\n");
        report.append("Updated: ").append(aggregate.getUpdatedAt()).append("\n");
        report.append("Modified: ").append(aggregate.isModified()).append("\n");
        report.append("Uncommitted Events: ").append(aggregate.getUncommittedEvents().size()).append("\n");
        
        // Eventos não commitados
        if (aggregate.hasUncommittedEvents()) {
            report.append("\n--- UNCOMMITTED EVENTS ---\n");
            for (DomainEvent event : aggregate.getUncommittedEvents()) {
                report.append("- ").append(event.getEventType())
                      .append(" (v").append(event.getVersion()).append(")")
                      .append(" at ").append(event.getTimestamp()).append("\n");
            }
        }
        
        // Metadados
        Map<String, Object> metadata = aggregate.getMetadata();
        if (!metadata.isEmpty()) {
            report.append("\n--- METADATA ---\n");
            metadata.forEach((key, value) -> 
                report.append("- ").append(key).append(": ").append(value).append("\n"));
        }
        
        // Informações específicas do tipo
        report.append("\n--- TYPE SPECIFIC INFO ---\n");
        if (aggregate instanceof SinistroAggregate) {
            appendSinistroDebugInfo(report, (SinistroAggregate) aggregate);
        }
        
        report.append("===============================\n");
        
        return report.toString();
    }
    
    private void appendSinistroDebugInfo(StringBuilder report, SinistroAggregate sinistro) {
        report.append("Número: ").append(sinistro.getNumeroSinistro()).append("\n");
        report.append("Status: ").append(sinistro.getStatus()).append("\n");
        report.append("CPF Segurado: ").append(sinistro.getCpfSegurado()).append("\n");
        report.append("Valor Estimado: ").append(sinistro.getValorEstimado()).append("\n");
        report.append("Valor Final: ").append(sinistro.getValorFinal()).append("\n");
        report.append("Data Ocorrência: ").append(sinistro.getDataOcorrencia()).append("\n");
        report.append("Data Fechamento: ").append(sinistro.getDataFechamento()).append("\n");
    }
    
    /**
     * Valida integridade do agregado.
     */
    public List<String> validateIntegrity(AggregateRoot aggregate) {
        List<String> issues = new ArrayList<>();
        
        // Validações básicas
        if (aggregate.getAggregateId() == null) {
            issues.add("Aggregate ID é null");
        }
        
        if (aggregate.getVersion() < 0) {
            issues.add("Versão é negativa: " + aggregate.getVersion());
        }
        
        if (aggregate.getCreatedAt() == null) {
            issues.add("Data de criação é null");
        }
        
        if (aggregate.getUpdatedAt() == null) {
            issues.add("Data de atualização é null");
        }
        
        if (aggregate.getCreatedAt() != null && aggregate.getUpdatedAt() != null &&
            aggregate.getUpdatedAt().isBefore(aggregate.getCreatedAt())) {
            issues.add("Data de atualização é anterior à data de criação");
        }
        
        // Validar eventos não commitados
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        for (int i = 0; i < events.size(); i++) {
            DomainEvent event = events.get(i);
            
            if (!Objects.equals(event.getAggregateId(), aggregate.getAggregateId())) {
                issues.add("Evento " + i + " tem aggregate ID diferente");
            }
            
            if (event.getVersion() <= 0) {
                issues.add("Evento " + i + " tem versão inválida: " + event.getVersion());
            }
            
            if (event.getTimestamp() == null) {
                issues.add("Evento " + i + " não tem timestamp");
            }
        }
        
        return issues;
    }
    
    /**
     * Simula replay de eventos para debug.
     */
    public AggregateRoot simulateReplay(List<DomainEvent> events, Class<? extends AggregateRoot> aggregateType) {
        try {
            AggregateRoot aggregate = aggregateType.getDeclaredConstructor().newInstance();
            
            log.info("Simulando replay de {} eventos para {}", events.size(), aggregateType.getSimpleName());
            
            for (DomainEvent event : events) {
                log.debug("Aplicando evento: {} (v{})", event.getEventType(), event.getVersion());
                aggregate.applyEventToState(event);
            }
            
            log.info("Replay concluído. Estado final: versão {}", aggregate.getVersion());
            
            return aggregate;
            
        } catch (Exception e) {
            log.error("Erro durante simulação de replay", e);
            throw new RuntimeException("Erro no replay de eventos", e);
        }
    }
}

// Controller para debug em desenvolvimento
@RestController
@RequestMapping("/debug/aggregates")
@Profile("dev")
public class AggregateDebugController {
    
    private final AggregateDebugger debugger;
    private final AggregateRepository<SinistroAggregate> sinistroRepository;
    
    @GetMapping("/{aggregateId}/report")
    public ResponseEntity<String> getDebugReport(@PathVariable String aggregateId) {
        Optional<SinistroAggregate> aggregate = sinistroRepository.findById(aggregateId);
        
        if (aggregate.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        String report = debugger.generateDebugReport(aggregate.get());
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(report);
    }
    
    @GetMapping("/{aggregateId}/integrity")
    public ResponseEntity<List<String>> checkIntegrity(@PathVariable String aggregateId) {
        Optional<SinistroAggregate> aggregate = sinistroRepository.findById(aggregateId);
        
        if (aggregate.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<String> issues = debugger.validateIntegrity(aggregate.get());
        return ResponseEntity.ok(issues);
    }
}
```

---

## 🎯 **BOAS PRÁTICAS CONSOLIDADAS**

### **✅ Checklist de Qualidade**

```markdown
## Checklist para Implementação de Agregados

### Estrutura e Design
- [ ] Agregado herda de AggregateRoot
- [ ] Estado é privado e imutável
- [ ] Métodos de comando são públicos
- [ ] Event handlers são privados
- [ ] Implementa createSnapshot() e restoreFromSnapshot()

### Eventos
- [ ] Eventos são imutáveis
- [ ] Nomes de eventos são claros e no passado
- [ ] Eventos contêm todas as informações necessárias
- [ ] Versionamento de eventos implementado

### Regras de Negócio
- [ ] Regras implementadas como BusinessRule
- [ ] Validações executadas após aplicar eventos
- [ ] Mensagens de erro são claras
- [ ] Regras têm prioridades definidas

### Testes
- [ ] Testes unitários para todos os comandos
- [ ] Testes de integração com Event Store
- [ ] Testes de regras de negócio
- [ ] Testes de performance com muitos eventos
- [ ] Testes de concorrência

### Performance
- [ ] Snapshot implementado corretamente
- [ ] Política de snapshot configurada
- [ ] Eventos otimizados (tamanho mínimo)
- [ ] Índices adequados no Event Store

### Monitoramento
- [ ] Métricas de performance implementadas
- [ ] Logs estruturados
- [ ] Health checks
- [ ] Alertas para problemas críticos
```

---

## 🎓 **EXERCÍCIO FINAL**

### **📝 Implementação Completa**

Implemente um `ApoliceAggregate` completo que:

1. **Gerencie ciclo de vida** da apólice (criação, ativação, renovação, cancelamento)
2. **Controle vigência** e valores
3. **Implemente regras complexas** (limites, carências, exclusões)
4. **Tenha testes abrangentes** (unitários, integração, performance)
5. **Inclua debugging** e monitoramento

**Template:**
```java
public class ApoliceAggregate extends AggregateRoot {
    
    // Estado do agregado
    private String numeroApolice;
    private ApoliceStatus status;
    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFim;
    private BigDecimal valorSegurado;
    private List<Cobertura> coberturas;
    
    // Implementação completa com todos os padrões
}
```

---

## 📚 **RESUMO DO MÓDULO AGREGADOS**

Após completar as 5 partes do módulo Agregados, você deve ser capaz de:

✅ **Compreender** os fundamentos DDD e Event Sourcing  
✅ **Implementar** Event Sourcing Handlers e repositórios  
✅ **Otimizar** com snapshots e estratégias de performance  
✅ **Validar** com regras de negócio robustas  
✅ **Testar** e debuggar agregados complexos  

---

**📍 Próximo Módulo**: [Configurações - Parte Única](./09-configuracoes.md)

---

**📚 Roteiro elaborado por:** Principal Java Architect  
**🎯 Foco:** Testes completos e boas práticas  
**⏱️ Tempo estimado:** 60 minutos  
**🔧 Hands-on:** Implementação completa de agregado com testes