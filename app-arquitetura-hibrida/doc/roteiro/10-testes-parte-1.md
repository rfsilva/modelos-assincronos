# 🧪 TESTES E BOAS PRÁTICAS - PARTE 1
## Fundamentos de Testes na Arquitetura Híbrida

### 🎯 **OBJETIVOS DESTA PARTE**
- Compreender a estratégia de testes na arquitetura híbrida
- Entender os diferentes tipos de testes implementados
- Conhecer as ferramentas e frameworks utilizados
- Identificar padrões de testes no projeto

---

## 📚 **CONCEITOS FUNDAMENTAIS**

### **🔍 Estratégia de Testes na Arquitetura Híbrida**

A arquitetura híbrida com Event Sourcing e CQRS apresenta desafios únicos para testes:

#### **Características Especiais:**
- **Separação Command/Query**: Testes devem validar ambos os lados
- **Eventos Assíncronos**: Necessidade de testes temporais
- **Projeções**: Validação de consistência eventual
- **Agregados**: Testes de comportamento e invariantes

#### **Pirâmide de Testes Adaptada:**
```
    🔺 E2E Tests (Poucos)
   🔺🔺 Integration Tests (Alguns)
  🔺🔺🔺 Unit Tests (Muitos)
 🔺🔺🔺🔺 Domain Tests (Mais ainda)
```

---

## 🛠️ **FERRAMENTAS E FRAMEWORKS**

### **📦 Dependências de Teste no Projeto**

```xml
<!-- Principais dependências de teste -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.awaitility</groupId>
    <artifactId>awaitility</artifactId>
    <scope>test</scope>
</dependency>
```

### **🔧 Ferramentas Utilizadas:**

| **Ferramenta** | **Propósito** | **Uso no Projeto** |
|----------------|---------------|-------------------|
| **JUnit 5** | Framework base | Todos os testes |
| **Mockito** | Mocking | Testes unitários |
| **TestContainers** | Containers para teste | Testes de integração |
| **Awaitility** | Testes assíncronos | Event Bus e Projections |
| **Spring Boot Test** | Contexto Spring | Testes de integração |
| **H2 Database** | BD em memória | Testes rápidos |

---

## 📋 **TIPOS DE TESTES IMPLEMENTADOS**

### **1. 🎯 Testes de Domínio (Domain Tests)**

**Objetivo**: Validar regras de negócio e comportamento dos agregados

**Características:**
- Focam na lógica de domínio pura
- Não dependem de infraestrutura
- Executam rapidamente
- Validam invariantes de negócio

**Exemplo de Estrutura:**
```java
@DisplayName("Sinistro Aggregate - Regras de Negócio")
class SinistroAggregateTest {
    
    @Test
    @DisplayName("Deve criar sinistro com dados válidos")
    void deveCriarSinistroComDadosValidos() {
        // Given, When, Then
    }
    
    @Test
    @DisplayName("Não deve permitir valor estimado negativo")
    void naoDevePermitirValorEstimadoNegativo() {
        // Teste de invariante
    }
}
```

### **2. ⚙️ Testes Unitários (Unit Tests)**

**Objetivo**: Validar componentes individuais isoladamente

**Características:**
- Testam uma única unidade de código
- Usam mocks para dependências
- Executam muito rapidamente
- Cobertura alta de cenários

**Componentes Testados:**
- Command Handlers
- Event Handlers
- Projection Handlers
- Services
- Repositories

### **3. 🔗 Testes de Integração (Integration Tests)**

**Objetivo**: Validar interação entre componentes

**Características:**
- Testam fluxos completos
- Usam banco de dados real (TestContainers)
- Validam persistência e consultas
- Testam configurações

**Cenários Cobertos:**
- Persistência de eventos
- Atualização de projeções
- Consultas complexas
- Configurações de DataSource

### **4. 🌐 Testes End-to-End (E2E Tests)**

**Objetivo**: Validar fluxos completos da aplicação

**Características:**
- Testam através da API REST
- Validam comportamento do usuário
- Usam ambiente completo
- Executam mais lentamente

---

## 📁 **ESTRUTURA DE TESTES NO PROJETO**

### **🗂️ Organização dos Arquivos:**

```
src/test/java/
├── com/seguradora/hibrida/
│   ├── aggregate/
│   │   ├── AggregateRootTest.java
│   │   ├── ExampleAggregateTest.java
│   │   └── repository/
│   │       └── EventSourcingAggregateRepositoryTest.java
│   ├── command/
│   │   ├── CommandBusTest.java
│   │   ├── CommandHandlerTest.java
│   │   └── impl/
│   │       └── SimpleCommandBusTest.java
│   ├── eventbus/
│   │   ├── EventBusTest.java
│   │   ├── EventHandlerTest.java
│   │   └── impl/
│   │       ├── SimpleEventBusTest.java
│   │       └── KafkaEventBusTest.java
│   ├── eventstore/
│   │   ├── EventStoreTest.java
│   │   ├── impl/
│   │   │   └── PostgreSQLEventStoreTest.java
│   │   └── serialization/
│   │       └── JsonEventSerializerTest.java
│   ├── projection/
│   │   ├── ProjectionHandlerTest.java
│   │   ├── ProjectionEventProcessorTest.java
│   │   └── example/
│   │       ├── SinistroProjectionHandlerTest.java
│   │       └── SeguradoProjectionHandlerTest.java
│   ├── query/
│   │   ├── service/
│   │   │   └── SinistroQueryServiceTest.java
│   │   └── repository/
│   │       └── SinistroQueryRepositoryTest.java
│   └── integration/
│       ├── SinistroFlowIntegrationTest.java
│       ├── EventSourcingIntegrationTest.java
│       └── CQRSIntegrationTest.java
```

---

## 🎯 **PADRÕES DE NOMENCLATURA**

### **📝 Convenções Adotadas:**

#### **Classes de Teste:**
- `{ClasseTestada}Test.java` - Testes unitários
- `{Fluxo}IntegrationTest.java` - Testes de integração
- `{Feature}E2ETest.java` - Testes end-to-end

#### **Métodos de Teste:**
- `deve{ComportamentoEsperado}()` - Cenários positivos
- `naoDeve{ComportamentoInvalido}()` - Cenários negativos
- `deveLancar{Excecao}Quando{Condicao}()` - Testes de exceção

#### **Anotações Utilizadas:**
```java
@DisplayName("Descrição clara do teste")
@Test
@ParameterizedTest
@ValueSource
@CsvSource
@TestMethodOrder
@Nested
```

---

## 📊 **MÉTRICAS DE QUALIDADE**

### **🎯 Metas de Cobertura:**

| **Tipo de Componente** | **Meta de Cobertura** | **Justificativa** |
|------------------------|----------------------|-------------------|
| **Domain/Aggregates** | 95%+ | Lógica crítica de negócio |
| **Command Handlers** | 90%+ | Ponto de entrada de comandos |
| **Event Handlers** | 85%+ | Processamento de eventos |
| **Projection Handlers** | 85%+ | Atualização de projeções |
| **Repositories** | 80%+ | Persistência de dados |
| **Controllers** | 75%+ | APIs REST |
| **Configuration** | 60%+ | Configurações básicas |

### **📈 Ferramentas de Medição:**
- **JaCoCo**: Cobertura de código
- **SonarQube**: Qualidade de código
- **Maven Surefire**: Relatórios de teste

---

## 🔍 **EXEMPLO PRÁTICO: ESTRUTURA DE TESTE**

### **📝 Exemplo de Teste de Domínio:**

```java
@DisplayName("Sinistro Aggregate - Testes de Domínio")
class SinistroAggregateTest {
    
    private SinistroAggregate sinistro;
    
    @BeforeEach
    void setUp() {
        sinistro = new SinistroAggregate();
    }
    
    @Nested
    @DisplayName("Criação de Sinistro")
    class CriacaoSinistro {
        
        @Test
        @DisplayName("Deve criar sinistro com dados válidos")
        void deveCriarSinistroComDadosValidos() {
            // Given
            String protocolo = "SIN-2024-001";
            String descricao = "Colisão traseira";
            BigDecimal valorEstimado = new BigDecimal("5000.00");
            
            // When
            sinistro.criar(protocolo, descricao, valorEstimado);
            
            // Then
            assertThat(sinistro.getProtocolo()).isEqualTo(protocolo);
            assertThat(sinistro.getDescricao()).isEqualTo(descricao);
            assertThat(sinistro.getValorEstimado()).isEqualTo(valorEstimado);
            assertThat(sinistro.getStatus()).isEqualTo(StatusSinistro.ABERTO);
            
            // Verifica eventos gerados
            List<DomainEvent> eventos = sinistro.getUncommittedEvents();
            assertThat(eventos).hasSize(1);
            assertThat(eventos.get(0)).isInstanceOf(SinistroCriadoEvent.class);
        }
        
        @Test
        @DisplayName("Não deve criar sinistro com valor negativo")
        void naoDeveCriarSinistroComValorNegativo() {
            // Given
            String protocolo = "SIN-2024-002";
            String descricao = "Teste";
            BigDecimal valorNegativo = new BigDecimal("-1000.00");
            
            // When & Then
            assertThatThrownBy(() -> 
                sinistro.criar(protocolo, descricao, valorNegativo)
            )
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Valor estimado não pode ser negativo");
        }
    }
}
```

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [TestContainers Documentation](https://www.testcontainers.org/)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)

### **📖 Próximas Partes:**
- **Parte 2**: Testes de Command Handlers e Event Handlers
- **Parte 3**: Testes de Projeções e Consultas
- **Parte 4**: Testes de Integração e TestContainers
- **Parte 5**: Testes E2E e Automação

---

**📝 Parte 1 de 5 - Fundamentos de Testes**  
**⏱️ Tempo estimado**: 45 minutos  
**🎯 Próximo**: [Parte 2 - Testes de Handlers](./10-testes-parte-2.md)