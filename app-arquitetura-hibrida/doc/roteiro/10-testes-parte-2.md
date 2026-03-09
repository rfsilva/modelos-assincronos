# 🧪 TESTES E BOAS PRÁTICAS - PARTE 2
## Testes de Command Handlers e Event Handlers

### 🎯 **OBJETIVOS DESTA PARTE**
- Implementar testes para Command Handlers
- Criar testes para Event Handlers
- Aplicar padrões de mocking e stubbing
- Validar comportamentos assíncronos

---

## 🎯 **TESTES DE COMMAND HANDLERS**

### **📋 Estratégia de Testes para Command Handlers**

Os Command Handlers são o ponto de entrada para operações de escrita. Seus testes devem validar:

#### **Aspectos a Testar:**
- ✅ **Validação de comandos**
- ✅ **Execução da lógica de negócio**
- ✅ **Persistência de eventos**
- ✅ **Tratamento de erros**
- ✅ **Retorno de resultados**

### **🔧 Estrutura Base para Testes de Command Handler**

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Criar Sinistro Command Handler - Testes")
class CriarSinistroCommandHandlerTest {
    
    @Mock
    private AggregateRepository<SinistroAggregate> repository;
    
    @Mock
    private EventBus eventBus;
    
    @InjectMocks
    private CriarSinistroCommandHandler handler;
    
    @Captor
    private ArgumentCaptor<SinistroAggregate> aggregateCaptor;
    
    @BeforeEach
    void setUp() {
        // Setup comum se necessário
    }
}
```

### **📝 Exemplo Completo: Teste de Command Handler**

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Criar Sinistro Command Handler")
class CriarSinistroCommandHandlerTest {
    
    @Mock
    private AggregateRepository<SinistroAggregate> repository;
    
    @InjectMocks
    private CriarSinistroCommandHandler handler;
    
    @Captor
    private ArgumentCaptor<SinistroAggregate> aggregateCaptor;
    
    @Nested
    @DisplayName("Cenários de Sucesso")
    class CenariosSuccesso {
        
        @Test
        @DisplayName("Deve criar sinistro com dados válidos")
        void deveCriarSinistroComDadosValidos() {
            // Given
            CriarSinistroCommand command = CriarSinistroCommand.builder()
                .protocolo("SIN-2024-001")
                .cpfSegurado("12345678901")
                .descricao("Colisão traseira na Av. Paulista")
                .valorEstimado(new BigDecimal("5000.00"))
                .placa("ABC1234")
                .build();
            
            // When
            CommandResult result = handler.handle(command);
            
            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
            
            // Verifica se o aggregate foi salvo
            verify(repository).save(aggregateCaptor.capture());
            
            SinistroAggregate sinistroSalvo = aggregateCaptor.getValue();
            assertThat(sinistroSalvo.getProtocolo()).isEqualTo("SIN-2024-001");
            assertThat(sinistroSalvo.getCpfSegurado()).isEqualTo("12345678901");
            assertThat(sinistroSalvo.getStatus()).isEqualTo(StatusSinistro.ABERTO);
            
            // Verifica eventos gerados
            List<DomainEvent> eventos = sinistroSalvo.getUncommittedEvents();
            assertThat(eventos).hasSize(1);
            assertThat(eventos.get(0)).isInstanceOf(SinistroCriadoEvent.class);
        }
    }
    
    @Nested
    @DisplayName("Cenários de Erro")
    class CenariosErro {
        
        @Test
        @DisplayName("Deve falhar com CPF inválido")
        void deveFalharComCpfInvalido() {
            // Given
            CriarSinistroCommand command = CriarSinistroCommand.builder()
                .protocolo("SIN-2024-002")
                .cpfSegurado("123") // CPF inválido
                .descricao("Teste")
                .valorEstimado(new BigDecimal("1000.00"))
                .placa("ABC1234")
                .build();
            
            // When
            CommandResult result = handler.handle(command);
            
            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorMessage()).contains("CPF inválido");
            
            // Verifica que não houve tentativa de salvar
            verify(repository, never()).save(any());
        }
        
        @Test
        @DisplayName("Deve tratar erro de persistência")
        void deveTratarErroPersistencia() {
            // Given
            CriarSinistroCommand command = CriarSinistroCommand.builder()
                .protocolo("SIN-2024-003")
                .cpfSegurado("12345678901")
                .descricao("Teste")
                .valorEstimado(new BigDecimal("1000.00"))
                .placa("ABC1234")
                .build();
            
            // Simula erro de persistência
            doThrow(new EventStoreException("Erro de conexão"))
                .when(repository).save(any());
            
            // When
            CommandResult result = handler.handle(command);
            
            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorMessage()).contains("Erro ao persistir sinistro");
        }
    }
    
    @Nested
    @DisplayName("Validações de Negócio")
    class ValidacoesNegocio {
        
        @ParameterizedTest
        @DisplayName("Deve validar protocolos com diferentes formatos")
        @ValueSource(strings = {"SIN-2024-001", "SIN-2024-999999", "SIN-2025-000001"})
        void deveValidarProtocolosValidos(String protocolo) {
            // Given
            CriarSinistroCommand command = CriarSinistroCommand.builder()
                .protocolo(protocolo)
                .cpfSegurado("12345678901")
                .descricao("Teste")
                .valorEstimado(new BigDecimal("1000.00"))
                .placa("ABC1234")
                .build();
            
            // When
            CommandResult result = handler.handle(command);
            
            // Then
            assertThat(result.isSuccess()).isTrue();
        }
        
        @ParameterizedTest
        @DisplayName("Deve rejeitar valores estimados inválidos")
        @CsvSource({
            "-1000.00, 'Valor não pode ser negativo'",
            "0.00, 'Valor deve ser maior que zero'",
            "999999999.99, 'Valor excede limite máximo'"
        })
        void deveRejeitarValoresInvalidos(String valor, String mensagemEsperada) {
            // Given
            CriarSinistroCommand command = CriarSinistroCommand.builder()
                .protocolo("SIN-2024-001")
                .cpfSegurado("12345678901")
                .descricao("Teste")
                .valorEstimado(new BigDecimal(valor))
                .placa("ABC1234")
                .build();
            
            // When
            CommandResult result = handler.handle(command);
            
            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorMessage()).contains(mensagemEsperada);
        }
    }
}
```

---

## 🎧 **TESTES DE EVENT HANDLERS**

### **📋 Estratégia de Testes para Event Handlers**

Event Handlers processam eventos de forma assíncrona. Seus testes devem validar:

#### **Aspectos a Testar:**
- ✅ **Processamento correto de eventos**
- ✅ **Atualização de projeções**
- ✅ **Tratamento de erros**
- ✅ **Comportamento assíncrono**
- ✅ **Idempotência**

### **🔧 Estrutura Base para Testes de Event Handler**

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Sinistro Event Handler - Testes")
class SinistroEventHandlerTest {
    
    @Mock
    private SinistroQueryRepository queryRepository;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private SinistroEventHandler handler;
    
    @Captor
    private ArgumentCaptor<SinistroQueryModel> queryModelCaptor;
}
```

### **📝 Exemplo Completo: Teste de Event Handler**

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Sinistro Event Handler")
class SinistroEventHandlerTest {
    
    @Mock
    private SinistroQueryRepository queryRepository;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private SinistroEventHandler handler;
    
    @Captor
    private ArgumentCaptor<SinistroQueryModel> queryModelCaptor;
    
    @Nested
    @DisplayName("Processamento de SinistroCriadoEvent")
    class ProcessamentoSinistroCriado {
        
        @Test
        @DisplayName("Deve criar projeção quando sinistro é criado")
        void deveCriarProjecaoQuandoSinistroECriado() {
            // Given
            SinistroCriadoEvent event = SinistroCriadoEvent.builder()
                .aggregateId("sinistro-123")
                .protocolo("SIN-2024-001")
                .cpfSegurado("12345678901")
                .descricao("Colisão traseira")
                .valorEstimado(new BigDecimal("5000.00"))
                .placa("ABC1234")
                .timestamp(Instant.now())
                .build();
            
            // When
            handler.handle(event);
            
            // Then
            verify(queryRepository).save(queryModelCaptor.capture());
            
            SinistroQueryModel projecao = queryModelCaptor.getValue();
            assertThat(projecao.getId()).isEqualTo(UUID.fromString("sinistro-123"));
            assertThat(projecao.getProtocolo()).isEqualTo("SIN-2024-001");
            assertThat(projecao.getCpfSegurado()).isEqualTo("12345678901");
            assertThat(projecao.getStatus()).isEqualTo("ABERTO");
            
            // Verifica notificação
            verify(notificationService).enviarNotificacao(
                eq("12345678901"), 
                contains("Sinistro SIN-2024-001 foi criado")
            );
        }
        
        @Test
        @DisplayName("Deve ser idempotente ao processar mesmo evento")
        void deveSerIdempotente() {
            // Given
            SinistroCriadoEvent event = SinistroCriadoEvent.builder()
                .aggregateId("sinistro-123")
                .protocolo("SIN-2024-001")
                .cpfSegurado("12345678901")
                .descricao("Teste")
                .valorEstimado(new BigDecimal("1000.00"))
                .placa("ABC1234")
                .timestamp(Instant.now())
                .build();
            
            // Simula que já existe projeção
            SinistroQueryModel projecaoExistente = new SinistroQueryModel();
            projecaoExistente.setId(UUID.fromString("sinistro-123"));
            projecaoExistente.setLastEventId(event.getEventId());
            
            when(queryRepository.findById(UUID.fromString("sinistro-123")))
                .thenReturn(Optional.of(projecaoExistente));
            
            // When
            handler.handle(event);
            
            // Then - não deve processar novamente
            verify(queryRepository, never()).save(any());
            verify(notificationService, never()).enviarNotificacao(any(), any());
        }
    }
    
    @Nested
    @DisplayName("Processamento de SinistroAtualizadoEvent")
    class ProcessamentoSinistroAtualizado {
        
        @Test
        @DisplayName("Deve atualizar projeção existente")
        void deveAtualizarProjecaoExistente() {
            // Given
            SinistroAtualizadoEvent event = SinistroAtualizadoEvent.builder()
                .aggregateId("sinistro-123")
                .novoStatus("EM_ANALISE")
                .novaDescricao("Análise técnica iniciada")
                .timestamp(Instant.now())
                .build();
            
            SinistroQueryModel projecaoExistente = new SinistroQueryModel();
            projecaoExistente.setId(UUID.fromString("sinistro-123"));
            projecaoExistente.setStatus("ABERTO");
            projecaoExistente.setDescricao("Descrição original");
            
            when(queryRepository.findById(UUID.fromString("sinistro-123")))
                .thenReturn(Optional.of(projecaoExistente));
            
            // When
            handler.handle(event);
            
            // Then
            verify(queryRepository).save(queryModelCaptor.capture());
            
            SinistroQueryModel projecaoAtualizada = queryModelCaptor.getValue();
            assertThat(projecaoAtualizada.getStatus()).isEqualTo("EM_ANALISE");
            assertThat(projecaoAtualizada.getDescricao()).isEqualTo("Análise técnica iniciada");
        }
        
        @Test
        @DisplayName("Deve criar projeção se não existir")
        void deveCriarProjecaoSeNaoExistir() {
            // Given
            SinistroAtualizadoEvent event = SinistroAtualizadoEvent.builder()
                .aggregateId("sinistro-456")
                .novoStatus("EM_ANALISE")
                .timestamp(Instant.now())
                .build();
            
            when(queryRepository.findById(UUID.fromString("sinistro-456")))
                .thenReturn(Optional.empty());
            
            // When
            handler.handle(event);
            
            // Then
            verify(queryRepository).save(any(SinistroQueryModel.class));
        }
    }
    
    @Nested
    @DisplayName("Tratamento de Erros")
    class TratamentoErros {
        
        @Test
        @DisplayName("Deve tratar erro de persistência")
        void deveTratarErroPersistencia() {
            // Given
            SinistroCriadoEvent event = SinistroCriadoEvent.builder()
                .aggregateId("sinistro-123")
                .protocolo("SIN-2024-001")
                .cpfSegurado("12345678901")
                .descricao("Teste")
                .valorEstimado(new BigDecimal("1000.00"))
                .placa("ABC1234")
                .timestamp(Instant.now())
                .build();
            
            // Simula erro de persistência
            doThrow(new DataAccessException("Erro de conexão") {})
                .when(queryRepository).save(any());
            
            // When & Then
            assertThatThrownBy(() -> handler.handle(event))
                .isInstanceOf(EventHandlingException.class)
                .hasMessageContaining("Erro ao processar evento");
        }
        
        @Test
        @DisplayName("Deve continuar processamento mesmo com erro de notificação")
        void deveContinuarProcessamentoComErroNotificacao() {
            // Given
            SinistroCriadoEvent event = SinistroCriadoEvent.builder()
                .aggregateId("sinistro-123")
                .protocolo("SIN-2024-001")
                .cpfSegurado("12345678901")
                .descricao("Teste")
                .valorEstimado(new BigDecimal("1000.00"))
                .placa("ABC1234")
                .timestamp(Instant.now())
                .build();
            
            // Simula erro de notificação
            doThrow(new RuntimeException("Serviço indisponível"))
                .when(notificationService).enviarNotificacao(any(), any());
            
            // When
            assertThatCode(() -> handler.handle(event))
                .doesNotThrowAnyException();
            
            // Then - deve ter salvo a projeção mesmo com erro de notificação
            verify(queryRepository).save(any());
        }
    }
}
```

---

## ⏱️ **TESTES ASSÍNCRONOS COM AWAITILITY**

### **🔧 Configuração do Awaitility**

Para testar comportamentos assíncronos, especialmente com Event Bus:

```java
@TestConfiguration
public class AsyncTestConfiguration {
    
    @Bean
    @Primary
    public Executor testTaskExecutor() {
        return Executors.newFixedThreadPool(2);
    }
}
```

### **📝 Exemplo de Teste Assíncrono**

```java
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Testes Assíncronos - Event Bus")
class EventBusAsyncTest {
    
    @Autowired
    private EventBus eventBus;
    
    @Autowired
    private SinistroQueryRepository queryRepository;
    
    @Test
    @Order(1)
    @DisplayName("Deve processar evento assincronamente")
    void deveProcessarEventoAssincronamente() {
        // Given
        SinistroCriadoEvent event = SinistroCriadoEvent.builder()
            .aggregateId("sinistro-async-123")
            .protocolo("SIN-ASYNC-001")
            .cpfSegurado("12345678901")
            .descricao("Teste assíncrono")
            .valorEstimado(new BigDecimal("1000.00"))
            .placa("ABC1234")
            .timestamp(Instant.now())
            .build();
        
        // When
        eventBus.publish(event);
        
        // Then - aguarda processamento assíncrono
        await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted(() -> {
                Optional<SinistroQueryModel> projecao = 
                    queryRepository.findById(UUID.fromString("sinistro-async-123"));
                
                assertThat(projecao).isPresent();
                assertThat(projecao.get().getProtocolo()).isEqualTo("SIN-ASYNC-001");
            });
    }
    
    @Test
    @Order(2)
    @DisplayName("Deve processar múltiplos eventos em ordem")
    void deveProcessarMultiplosEventosEmOrdem() {
        // Given
        String aggregateId = "sinistro-multi-123";
        
        SinistroCriadoEvent eventoCriacao = SinistroCriadoEvent.builder()
            .aggregateId(aggregateId)
            .protocolo("SIN-MULTI-001")
            .cpfSegurado("12345678901")
            .descricao("Teste múltiplos eventos")
            .valorEstimado(new BigDecimal("2000.00"))
            .placa("XYZ9876")
            .timestamp(Instant.now())
            .build();
        
        SinistroAtualizadoEvent eventoAtualizacao = SinistroAtualizadoEvent.builder()
            .aggregateId(aggregateId)
            .novoStatus("EM_ANALISE")
            .novaDescricao("Análise iniciada")
            .timestamp(Instant.now().plusSeconds(1))
            .build();
        
        // When
        eventBus.publish(eventoCriacao);
        eventBus.publish(eventoAtualizacao);
        
        // Then
        await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofMillis(200))
            .untilAsserted(() -> {
                Optional<SinistroQueryModel> projecao = 
                    queryRepository.findById(UUID.fromString(aggregateId));
                
                assertThat(projecao).isPresent();
                assertThat(projecao.get().getProtocolo()).isEqualTo("SIN-MULTI-001");
                assertThat(projecao.get().getStatus()).isEqualTo("EM_ANALISE");
                assertThat(projecao.get().getDescricao()).isEqualTo("Análise iniciada");
            });
    }
}
```

---

## 🎯 **PADRÕES DE MOCKING**

### **📋 Boas Práticas para Mocks**

#### **✅ O que Mockar:**
- Dependências externas (APIs, bancos de dados)
- Serviços de infraestrutura
- Componentes complexos não relacionados ao teste

#### **❌ O que NÃO Mockar:**
- Objetos de valor (Value Objects)
- DTOs simples
- Lógica de domínio sendo testada

### **🔧 Exemplos de Mocking Efetivo**

```java
// ✅ Bom: Mock de dependência externa
@Mock
private DetranService detranService;

// ✅ Bom: Mock de repositório
@Mock
private EventStore eventStore;

// ❌ Evitar: Mock de objeto simples
// @Mock
// private SinistroCommand command; // Prefira criar instância real

// ✅ Bom: Configuração de comportamento específico
when(detranService.consultarVeiculo("ABC1234"))
    .thenReturn(DetranResponse.builder()
        .placa("ABC1234")
        .situacao("REGULAR")
        .build());

// ✅ Bom: Verificação de interação
verify(eventStore).saveEvents(
    eq("sinistro-123"), 
    argThat(eventos -> eventos.size() == 1),
    eq(0L)
);
```

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [Mockito Best Practices](https://github.com/mockito/mockito/wiki/How-to-write-good-tests)
- [Awaitility Documentation](https://github.com/awaitility/awaitility/wiki/Usage)
- [AssertJ Documentation](https://assertj.github.io/doc/)

### **📖 Próximas Partes:**
- **Parte 3**: Testes de Projeções e Consultas
- **Parte 4**: Testes de Integração com TestContainers
- **Parte 5**: Testes E2E e Automação

---

**📝 Parte 2 de 5 - Testes de Handlers**  
**⏱️ Tempo estimado**: 60 minutos  
**🎯 Próximo**: [Parte 3 - Testes de Projeções](./10-testes-parte-3.md)