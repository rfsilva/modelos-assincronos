# 🧪 TESTES E BOAS PRÁTICAS - PARTE 3
## Testes de Projeções e Consultas

### 🎯 **OBJETIVOS DESTA PARTE**
- Implementar testes para Projection Handlers
- Criar testes para Query Services e Repositories
- Validar consistência eventual das projeções
- Testar consultas complexas e performance

---

## 📊 **TESTES DE PROJECTION HANDLERS**

### **📋 Estratégia de Testes para Projection Handlers**

Projection Handlers são responsáveis por manter as projeções atualizadas. Seus testes devem validar:

#### **Aspectos a Testar:**
- ✅ **Criação de projeções a partir de eventos**
- ✅ **Atualização incremental de projeções**
- ✅ **Idempotência no processamento**
- ✅ **Tratamento de eventos fora de ordem**
- ✅ **Recuperação de falhas**

### **🔧 Estrutura Base para Testes de Projection Handler**

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Sinistro Projection Handler - Testes")
class SinistroProjectionHandlerTest {
    
    @Mock
    private SinistroQueryRepository queryRepository;
    
    @Mock
    private ProjectionTrackerRepository trackerRepository;
    
    @InjectMocks
    private SinistroProjectionHandler handler;
    
    @Captor
    private ArgumentCaptor<SinistroQueryModel> queryModelCaptor;
    
    @Captor
    private ArgumentCaptor<ProjectionTracker> trackerCaptor;
}
```

### **📝 Exemplo Completo: Teste de Projection Handler**

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Sinistro Projection Handler")
class SinistroProjectionHandlerTest {
    
    @Mock
    private SinistroQueryRepository queryRepository;
    
    @Mock
    private ProjectionTrackerRepository trackerRepository;
    
    @InjectMocks
    private SinistroProjectionHandler handler;
    
    @Captor
    private ArgumentCaptor<SinistroQueryModel> queryModelCaptor;
    
    @Captor
    private ArgumentCaptor<ProjectionTracker> trackerCaptor;
    
    @Nested
    @DisplayName("Processamento de Eventos de Criação")
    class ProcessamentoEventosCriacao {
        
        @Test
        @DisplayName("Deve criar nova projeção para SinistroCriadoEvent")
        void deveCriarNovaProjecaoParaSinistroCriado() {
            // Given
            SinistroCriadoEvent event = SinistroCriadoEvent.builder()
                .aggregateId("sinistro-123")
                .eventId(1L)
                .protocolo("SIN-2024-001")
                .cpfSegurado("12345678901")
                .nomeSegurado("João Silva")
                .descricao("Colisão traseira na Av. Paulista")
                .valorEstimado(new BigDecimal("5000.00"))
                .placa("ABC1234")
                .dataOcorrencia(Instant.now().minus(1, ChronoUnit.DAYS))
                .timestamp(Instant.now())
                .build();
            
            when(queryRepository.findById(UUID.fromString("sinistro-123")))
                .thenReturn(Optional.empty());
            
            // When
            handler.handle(event);
            
            // Then
            verify(queryRepository).save(queryModelCaptor.capture());
            
            SinistroQueryModel projecao = queryModelCaptor.getValue();
            assertThat(projecao.getId()).isEqualTo(UUID.fromString("sinistro-123"));
            assertThat(projecao.getProtocolo()).isEqualTo("SIN-2024-001");
            assertThat(projecao.getCpfSegurado()).isEqualTo("12345678901");
            assertThat(projecao.getNomeSegurado()).isEqualTo("João Silva");
            assertThat(projecao.getStatus()).isEqualTo("ABERTO");
            assertThat(projecao.getValorEstimado()).isEqualTo(new BigDecimal("5000.00"));
            assertThat(projecao.getLastEventId()).isEqualTo(1L);
            
            // Verifica atualização do tracker
            verify(trackerRepository).save(trackerCaptor.capture());
            ProjectionTracker tracker = trackerCaptor.getValue();
            assertThat(tracker.getProjectionName()).isEqualTo("SinistroProjection");
            assertThat(tracker.getLastProcessedEventId()).isEqualTo(1L);
        }
        
        @Test
        @DisplayName("Deve ignorar evento já processado (idempotência)")
        void deveIgnorarEventoJaProcessado() {
            // Given
            SinistroCriadoEvent event = SinistroCriadoEvent.builder()
                .aggregateId("sinistro-123")
                .eventId(1L)
                .protocolo("SIN-2024-001")
                .timestamp(Instant.now())
                .build();
            
            SinistroQueryModel projecaoExistente = new SinistroQueryModel();
            projecaoExistente.setId(UUID.fromString("sinistro-123"));
            projecaoExistente.setLastEventId(1L); // Mesmo eventId
            
            when(queryRepository.findById(UUID.fromString("sinistro-123")))
                .thenReturn(Optional.of(projecaoExistente));
            
            // When
            handler.handle(event);
            
            // Then - não deve processar novamente
            verify(queryRepository, never()).save(any());
            verify(trackerRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Processamento de Eventos de Atualização")
    class ProcessamentoEventosAtualizacao {
        
        @Test
        @DisplayName("Deve atualizar projeção existente")
        void deveAtualizarProjecaoExistente() {
            // Given
            SinistroAtualizadoEvent event = SinistroAtualizadoEvent.builder()
                .aggregateId("sinistro-123")
                .eventId(2L)
                .novoStatus("EM_ANALISE")
                .novaDescricao("Análise técnica iniciada")
                .novoValorEstimado(new BigDecimal("6000.00"))
                .operadorResponsavel("Maria Santos")
                .timestamp(Instant.now())
                .build();
            
            SinistroQueryModel projecaoExistente = new SinistroQueryModel();
            projecaoExistente.setId(UUID.fromString("sinistro-123"));
            projecaoExistente.setProtocolo("SIN-2024-001");
            projecaoExistente.setStatus("ABERTO");
            projecaoExistente.setDescricao("Descrição original");
            projecaoExistente.setValorEstimado(new BigDecimal("5000.00"));
            projecaoExistente.setLastEventId(1L);
            
            when(queryRepository.findById(UUID.fromString("sinistro-123")))
                .thenReturn(Optional.of(projecaoExistente));
            
            // When
            handler.handle(event);
            
            // Then
            verify(queryRepository).save(queryModelCaptor.capture());
            
            SinistroQueryModel projecaoAtualizada = queryModelCaptor.getValue();
            assertThat(projecaoAtualizada.getStatus()).isEqualTo("EM_ANALISE");
            assertThat(projecaoAtualizada.getDescricao()).isEqualTo("Análise técnica iniciada");
            assertThat(projecaoAtualizada.getValorEstimado()).isEqualTo(new BigDecimal("6000.00"));
            assertThat(projecaoAtualizada.getOperadorResponsavel()).isEqualTo("Maria Santos");
            assertThat(projecaoAtualizada.getLastEventId()).isEqualTo(2L);
            
            // Campos não alterados devem permanecer
            assertThat(projecaoAtualizada.getProtocolo()).isEqualTo("SIN-2024-001");
        }
        
        @Test
        @DisplayName("Deve tratar evento fora de ordem")
        void deveTratarEventoForaDeOrdem() {
            // Given - evento com ID menor que o já processado
            SinistroAtualizadoEvent eventoAntigo = SinistroAtualizadoEvent.builder()
                .aggregateId("sinistro-123")
                .eventId(1L) // ID menor
                .novoStatus("CANCELADO")
                .timestamp(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();
            
            SinistroQueryModel projecaoExistente = new SinistroQueryModel();
            projecaoExistente.setId(UUID.fromString("sinistro-123"));
            projecaoExistente.setStatus("EM_ANALISE");
            projecaoExistente.setLastEventId(2L); // ID maior
            
            when(queryRepository.findById(UUID.fromString("sinistro-123")))
                .thenReturn(Optional.of(projecaoExistente));
            
            // When
            handler.handle(eventoAntigo);
            
            // Then - deve ignorar evento antigo
            verify(queryRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("Processamento de Eventos Especiais")
    class ProcessamentoEventosEspeciais {
        
        @Test
        @DisplayName("Deve processar ConsultaDetranConcluida")
        void deveProcessarConsultaDetranConcluida() {
            // Given
            ConsultaDetranConcluidaEvent event = ConsultaDetranConcluidaEvent.builder()
                .aggregateId("sinistro-123")
                .eventId(3L)
                .placa("ABC1234")
                .situacaoVeiculo("REGULAR")
                .proprietario("João Silva")
                .anoFabricacao(2020)
                .modelo("Civic")
                .marca("Honda")
                .timestamp(Instant.now())
                .build();
            
            SinistroQueryModel projecaoExistente = new SinistroQueryModel();
            projecaoExistente.setId(UUID.fromString("sinistro-123"));
            projecaoExistente.setPlaca("ABC1234");
            projecaoExistente.setConsultaDetranRealizada(false);
            projecaoExistente.setLastEventId(2L);
            
            when(queryRepository.findById(UUID.fromString("sinistro-123")))
                .thenReturn(Optional.of(projecaoExistente));
            
            // When
            handler.handle(event);
            
            // Then
            verify(queryRepository).save(queryModelCaptor.capture());
            
            SinistroQueryModel projecaoAtualizada = queryModelCaptor.getValue();
            assertThat(projecaoAtualizada.getConsultaDetranRealizada()).isTrue();
            assertThat(projecaoAtualizada.getConsultaDetranStatus()).isEqualTo("SUCESSO");
            assertThat(projecaoAtualizada.getAnoFabricacao()).isEqualTo(2020);
            assertThat(projecaoAtualizada.getModelo()).isEqualTo("Civic");
            assertThat(projecaoAtualizada.getMarca()).isEqualTo("Honda");
            assertThat(projecaoAtualizada.getLastEventId()).isEqualTo(3L);
        }
        
        @Test
        @DisplayName("Deve adicionar tags ao processar eventos")
        void deveAdicionarTagsAoProcessarEventos() {
            // Given
            SinistroAtualizadoEvent event = SinistroAtualizadoEvent.builder()
                .aggregateId("sinistro-123")
                .eventId(4L)
                .novoValorEstimado(new BigDecimal("50000.00")) // Valor alto
                .timestamp(Instant.now())
                .build();
            
            SinistroQueryModel projecaoExistente = new SinistroQueryModel();
            projecaoExistente.setId(UUID.fromString("sinistro-123"));
            projecaoExistente.setValorEstimado(new BigDecimal("5000.00"));
            projecaoExistente.setTags(new ArrayList<>());
            projecaoExistente.setLastEventId(3L);
            
            when(queryRepository.findById(UUID.fromString("sinistro-123")))
                .thenReturn(Optional.of(projecaoExistente));
            
            // When
            handler.handle(event);
            
            // Then
            verify(queryRepository).save(queryModelCaptor.capture());
            
            SinistroQueryModel projecaoAtualizada = queryModelCaptor.getValue();
            assertThat(projecaoAtualizada.getTags()).contains("ALTO_VALOR");
            assertThat(projecaoAtualizada.getValorEstimado()).isEqualTo(new BigDecimal("50000.00"));
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
                .eventId(1L)
                .protocolo("SIN-2024-001")
                .timestamp(Instant.now())
                .build();
            
            when(queryRepository.findById(any())).thenReturn(Optional.empty());
            doThrow(new DataAccessException("Erro de conexão") {})
                .when(queryRepository).save(any());
            
            // When & Then
            assertThatThrownBy(() -> handler.handle(event))
                .isInstanceOf(ProjectionException.class)
                .hasMessageContaining("Erro ao atualizar projeção");
        }
        
        @Test
        @DisplayName("Deve recuperar de falha e reprocessar")
        void deveRecuperarDeFalhaEReprocessar() {
            // Given
            SinistroCriadoEvent event = SinistroCriadoEvent.builder()
                .aggregateId("sinistro-123")
                .eventId(1L)
                .protocolo("SIN-2024-001")
                .timestamp(Instant.now())
                .build();
            
            when(queryRepository.findById(any())).thenReturn(Optional.empty());
            
            // Simula falha na primeira tentativa, sucesso na segunda
            doThrow(new DataAccessException("Erro temporário") {})
                .doNothing()
                .when(queryRepository).save(any());
            
            // When - primeira tentativa falha
            assertThatThrownBy(() -> handler.handle(event))
                .isInstanceOf(ProjectionException.class);
            
            // When - segunda tentativa sucede
            assertThatCode(() -> handler.handle(event))
                .doesNotThrowAnyException();
            
            // Then
            verify(queryRepository, times(2)).save(any());
        }
    }
}
```

---

## 🔍 **TESTES DE QUERY SERVICES**

### **📋 Estratégia de Testes para Query Services**

Query Services orquestram consultas complexas. Seus testes devem validar:

#### **Aspectos a Testar:**
- ✅ **Consultas simples e complexas**
- ✅ **Filtros e paginação**
- ✅ **Transformação de dados**
- ✅ **Cache e performance**
- ✅ **Tratamento de erros**

### **📝 Exemplo de Teste de Query Service**

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Sinistro Query Service")
class SinistroQueryServiceTest {
    
    @Mock
    private SinistroQueryRepository repository;
    
    @Mock
    private CacheManager cacheManager;
    
    @InjectMocks
    private SinistroQueryServiceImpl service;
    
    @Nested
    @DisplayName("Consultas Básicas")
    class ConsultasBasicas {
        
        @Test
        @DisplayName("Deve buscar sinistro por ID")
        void deveBuscarSinistroPorId() {
            // Given
            UUID sinistroId = UUID.randomUUID();
            SinistroQueryModel model = criarSinistroQueryModel(sinistroId);
            
            when(repository.findById(sinistroId))
                .thenReturn(Optional.of(model));
            
            // When
            Optional<SinistroDetailView> result = service.buscarPorId(sinistroId);
            
            // Then
            assertThat(result).isPresent();
            SinistroDetailView view = result.get();
            assertThat(view.getId()).isEqualTo(sinistroId);
            assertThat(view.getProtocolo()).isEqualTo("SIN-2024-001");
            assertThat(view.getStatus()).isEqualTo("ABERTO");
        }
        
        @Test
        @DisplayName("Deve retornar empty quando sinistro não existe")
        void deveRetornarEmptyQuandoSinistroNaoExiste() {
            // Given
            UUID sinistroId = UUID.randomUUID();
            when(repository.findById(sinistroId))
                .thenReturn(Optional.empty());
            
            // When
            Optional<SinistroDetailView> result = service.buscarPorId(sinistroId);
            
            // Then
            assertThat(result).isEmpty();
        }
    }
    
    @Nested
    @DisplayName("Consultas com Filtros")
    class ConsultasComFiltros {
        
        @Test
        @DisplayName("Deve listar sinistros com filtros")
        void deveListarSinistrosComFiltros() {
            // Given
            SinistroFilter filter = SinistroFilter.builder()
                .status("ABERTO")
                .cpfSegurado("12345678901")
                .dataAberturaInicio(Instant.now().minus(30, ChronoUnit.DAYS))
                .dataAberturaFim(Instant.now())
                .build();
            
            Pageable pageable = PageRequest.of(0, 10);
            
            List<SinistroQueryModel> models = Arrays.asList(
                criarSinistroQueryModel(UUID.randomUUID()),
                criarSinistroQueryModel(UUID.randomUUID())
            );
            
            Page<SinistroQueryModel> page = new PageImpl<>(models, pageable, 2);
            
            when(repository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
            
            // When
            Page<SinistroListView> result = service.listar(filter, pageable);
            
            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo("ABERTO");
        }
        
        @Test
        @DisplayName("Deve buscar por texto livre")
        void deveBuscarPorTextoLivre() {
            // Given
            String termo = "colisão avenida";
            Pageable pageable = PageRequest.of(0, 20);
            
            List<SinistroQueryModel> models = Arrays.asList(
                criarSinistroQueryModel(UUID.randomUUID())
            );
            
            Page<SinistroQueryModel> page = new PageImpl<>(models, pageable, 1);
            
            when(repository.findByFullTextSearchPaged(termo, pageable))
                .thenReturn(page);
            
            // When
            Page<SinistroListView> result = service.buscarPorTexto(termo, pageable);
            
            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(repository).findByFullTextSearchPaged(termo, pageable);
        }
    }
    
    @Nested
    @DisplayName("Dashboard e Estatísticas")
    class DashboardEstatisticas {
        
        @Test
        @DisplayName("Deve obter dados do dashboard")
        void deveObterDadosDashboard() {
            // Given
            Instant desde = Instant.now().minus(30, ChronoUnit.DAYS);
            
            Object[] resumo = {100L, 60L, 40L, new BigDecimal("15000.00"), 80L};
            when(repository.getResumoExecutivo(any(Instant.class)))
                .thenReturn(resumo);
            
            List<Object[]> estatisticasPorStatus = Arrays.asList(
                new Object[]{"ABERTO", 60L},
                new Object[]{"FECHADO", 40L}
            );
            when(repository.countByStatus())
                .thenReturn(estatisticasPorStatus);
            
            List<Object[]> estatisticasPorTipo = Arrays.asList(
                new Object[]{"COLISAO", 45L},
                new Object[]{"ROUBO", 15L}
            );
            when(repository.countByTipo())
                .thenReturn(estatisticasPorTipo);
            
            // When
            DashboardView dashboard = service.obterDashboard();
            
            // Then
            assertThat(dashboard.getTotalSinistros()).isEqualTo(100L);
            assertThat(dashboard.getSinistrosAbertos()).isEqualTo(60L);
            assertThat(dashboard.getSinistrosFechados()).isEqualTo(40L);
            assertThat(dashboard.getValorMedio()).isEqualTo(new BigDecimal("15000.00"));
            assertThat(dashboard.getComDetran()).isEqualTo(80L);
            
            assertThat(dashboard.getEstatisticasPorStatus()).hasSize(2);
            assertThat(dashboard.getEstatisticasPorTipo()).hasSize(2);
        }
    }
    
    private SinistroQueryModel criarSinistroQueryModel(UUID id) {
        SinistroQueryModel model = new SinistroQueryModel();
        model.setId(id);
        model.setProtocolo("SIN-2024-001");
        model.setStatus("ABERTO");
        model.setCpfSegurado("12345678901");
        model.setNomeSegurado("João Silva");
        model.setDescricao("Colisão traseira");
        model.setValorEstimado(new BigDecimal("5000.00"));
        model.setPlaca("ABC1234");
        model.setDataAbertura(Instant.now());
        return model;
    }
}
```

---

## 🗄️ **TESTES DE QUERY REPOSITORIES**

### **📋 Estratégia de Testes para Query Repositories**

Query Repositories executam consultas no banco. Seus testes devem validar:

#### **Aspectos a Testar:**
- ✅ **Consultas JPA e nativas**
- ✅ **Filtros complexos**
- ✅ **Performance de consultas**
- ✅ **Índices e otimizações**
- ✅ **Paginação e ordenação**

### **📝 Exemplo de Teste de Query Repository**

```java
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.show-sql=true"
})
@DisplayName("Sinistro Query Repository")
class SinistroQueryRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private SinistroQueryRepository repository;
    
    @BeforeEach
    void setUp() {
        // Criar dados de teste
        criarDadosTeste();
    }
    
    @Nested
    @DisplayName("Consultas Básicas")
    class ConsultasBasicas {
        
        @Test
        @DisplayName("Deve buscar por protocolo")
        void deveBuscarPorProtocolo() {
            // When
            Optional<SinistroQueryModel> result = 
                repository.findByProtocolo("SIN-2024-001");
            
            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getCpfSegurado()).isEqualTo("12345678901");
        }
        
        @Test
        @DisplayName("Deve buscar por CPF do segurado")
        void deveBuscarPorCpfSegurado() {
            // When
            List<SinistroQueryModel> result = 
                repository.findByCpfSeguradoOrderByDataAberturaDesc("12345678901");
            
            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getDataAbertura())
                .isAfter(result.get(1).getDataAbertura());
        }
    }
    
    @Nested
    @DisplayName("Consultas com Filtros")
    class ConsultasComFiltros {
        
        @Test
        @DisplayName("Deve filtrar por múltiplos critérios")
        void deveFiltrarPorMultiplosCriterios() {
            // Given
            Instant dataInicio = Instant.now().minus(10, ChronoUnit.DAYS);
            Instant dataFim = Instant.now();
            Pageable pageable = PageRequest.of(0, 10);
            
            // When
            Page<SinistroQueryModel> result = repository.findWithFilters(
                "ABERTO",           // status
                "COLISAO",          // tipoSinistro
                null,               // operador
                "12345678901",      // cpfSegurado
                dataInicio,         // dataInicio
                dataFim,            // dataFim
                pageable
            );
            
            // Then
            assertThat(result.getContent()).isNotEmpty();
            result.getContent().forEach(sinistro -> {
                assertThat(sinistro.getStatus()).isEqualTo("ABERTO");
                assertThat(sinistro.getTipoSinistro()).isEqualTo("COLISAO");
                assertThat(sinistro.getCpfSegurado()).isEqualTo("12345678901");
                assertThat(sinistro.getDataAbertura()).isBetween(dataInicio, dataFim);
            });
        }
        
        @Test
        @DisplayName("Deve buscar por full-text search")
        void deveBuscarPorFullTextSearch() {
            // When
            List<SinistroQueryModel> result = 
                repository.findByFullTextSearch("colisão avenida");
            
            // Then
            assertThat(result).isNotEmpty();
            assertThat(result.get(0).getDescricao()).containsIgnoringCase("colisão");
        }
    }
    
    @Nested
    @DisplayName("Consultas de Agregação")
    class ConsultasAgregacao {
        
        @Test
        @DisplayName("Deve contar por status")
        void deveContarPorStatus() {
            // When
            List<Object[]> result = repository.countByStatus();
            
            // Then
            assertThat(result).isNotEmpty();
            
            Map<String, Long> contadores = result.stream()
                .collect(Collectors.toMap(
                    row -> (String) row[0],
                    row -> (Long) row[1]
                ));
            
            assertThat(contadores).containsKey("ABERTO");
            assertThat(contadores.get("ABERTO")).isGreaterThan(0);
        }
        
        @Test
        @DisplayName("Deve obter estatísticas por dia")
        void deveObterEstatisticasPorDia() {
            // Given
            Instant inicio = Instant.now().minus(7, ChronoUnit.DAYS);
            Instant fim = Instant.now();
            
            // When
            List<Object[]> result = repository.getEstatisticasPorDia(inicio, fim);
            
            // Then
            assertThat(result).isNotEmpty();
            
            Object[] primeiraEstatistica = result.get(0);
            assertThat(primeiraEstatistica).hasSize(4); // dia, total, abertos, fechados
            assertThat(primeiraEstatistica[1]).isInstanceOf(Number.class); // total
        }
    }
    
    @Nested
    @DisplayName("Performance e Otimização")
    class PerformanceOtimizacao {
        
        @Test
        @DisplayName("Deve executar consulta com paginação eficiente")
        void deveExecutarConsultaComPaginacaoEficiente() {
            // Given
            Pageable pageable = PageRequest.of(0, 5, Sort.by("dataAbertura").descending());
            
            // When
            long startTime = System.currentTimeMillis();
            Page<SinistroQueryModel> result = repository.findAll(pageable);
            long endTime = System.currentTimeMillis();
            
            // Then
            assertThat(result.getContent()).hasSize(5);
            assertThat(endTime - startTime).isLessThan(1000); // Menos de 1 segundo
            
            // Verifica ordenação
            List<Instant> datas = result.getContent().stream()
                .map(SinistroQueryModel::getDataAbertura)
                .collect(Collectors.toList());
            
            assertThat(datas).isSortedAccordingTo(Comparator.reverseOrder());
        }
        
        @Test
        @DisplayName("Deve usar índices em consultas por placa")
        void deveUsarIndicesEmConsultasPorPlaca() {
            // When
            long startTime = System.currentTimeMillis();
            List<SinistroQueryModel> result = 
                repository.findByPlacaOrderByDataAberturaDesc("ABC1234");
            long endTime = System.currentTimeMillis();
            
            // Then
            assertThat(result).isNotEmpty();
            assertThat(endTime - startTime).isLessThan(100); // Muito rápido com índice
        }
    }
    
    private void criarDadosTeste() {
        // Criar sinistros de teste
        for (int i = 1; i <= 10; i++) {
            SinistroQueryModel sinistro = new SinistroQueryModel();
            sinistro.setId(UUID.randomUUID());
            sinistro.setProtocolo("SIN-2024-" + String.format("%03d", i));
            sinistro.setCpfSegurado(i <= 5 ? "12345678901" : "98765432100");
            sinistro.setNomeSegurado("Segurado " + i);
            sinistro.setStatus(i % 2 == 0 ? "ABERTO" : "FECHADO");
            sinistro.setTipoSinistro(i % 3 == 0 ? "ROUBO" : "COLISAO");
            sinistro.setDescricao("Colisão na Avenida " + i);
            sinistro.setValorEstimado(new BigDecimal(1000 * i));
            sinistro.setPlaca("ABC123" + i);
            sinistro.setDataAbertura(Instant.now().minus(i, ChronoUnit.DAYS));
            sinistro.setDataOcorrencia(Instant.now().minus(i + 1, ChronoUnit.DAYS));
            
            entityManager.persistAndFlush(sinistro);
        }
    }
}
```

---

## 📊 **TESTES DE CONSISTÊNCIA EVENTUAL**

### **📝 Exemplo de Teste de Consistência**

```java
@SpringBootTest
@Transactional
@DisplayName("Testes de Consistência Eventual")
class ConsistenciaEventualTest {
    
    @Autowired
    private EventStore eventStore;
    
    @Autowired
    private SinistroQueryRepository queryRepository;
    
    @Autowired
    private ProjectionEventProcessor projectionProcessor;
    
    @Test
    @DisplayName("Deve manter consistência entre eventos e projeções")
    void deveManterConsistenciaEntreEventosEProjecoes() {
        // Given
        String aggregateId = "sinistro-consistencia-123";
        
        List<DomainEvent> eventos = Arrays.asList(
            SinistroCriadoEvent.builder()
                .aggregateId(aggregateId)
                .protocolo("SIN-CONS-001")
                .cpfSegurado("12345678901")
                .descricao("Teste consistência")
                .valorEstimado(new BigDecimal("3000.00"))
                .placa("CON1234")
                .timestamp(Instant.now())
                .build(),
            
            SinistroAtualizadoEvent.builder()
                .aggregateId(aggregateId)
                .novoStatus("EM_ANALISE")
                .novaDescricao("Análise iniciada")
                .timestamp(Instant.now().plusSeconds(1))
                .build()
        );
        
        // When - salva eventos
        eventStore.saveEvents(aggregateId, eventos, 0L);
        
        // Processa eventos nas projeções
        eventos.forEach(evento -> 
            projectionProcessor.processEvent(evento, evento.getEventId())
        );
        
        // Then - verifica consistência
        await()
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> {
                Optional<SinistroQueryModel> projecao = 
                    queryRepository.findById(UUID.fromString(aggregateId));
                
                assertThat(projecao).isPresent();
                SinistroQueryModel model = projecao.get();
                
                // Verifica dados do evento de criação
                assertThat(model.getProtocolo()).isEqualTo("SIN-CONS-001");
                assertThat(model.getCpfSegurado()).isEqualTo("12345678901");
                assertThat(model.getValorEstimado()).isEqualTo(new BigDecimal("3000.00"));
                
                // Verifica dados do evento de atualização
                assertThat(model.getStatus()).isEqualTo("EM_ANALISE");
                assertThat(model.getDescricao()).isEqualTo("Análise iniciada");
            });
    }
}
```

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [Spring Data JPA Testing](https://spring.io/guides/gs/testing-web/)
- [H2 Database Testing](http://h2database.com/html/features.html#testing)
- [JPA Query Testing Best Practices](https://vladmihalcea.com/how-to-test-jpa-queries/)

### **📖 Próximas Partes:**
- **Parte 4**: Testes de Integração com TestContainers
- **Parte 5**: Testes E2E e Automação

---

**📝 Parte 3 de 5 - Testes de Projeções e Consultas**  
**⏱️ Tempo estimado**: 60 minutos  
**🎯 Próximo**: [Parte 4 - Testes de Integração](./10-testes-parte-4.md)