# 🧪 Diretório de Testes Unitários

Este diretório contém todos os testes unitários do projeto **app-arquitetura-hibrida**.

## 📊 Status Atual

- ✅ **41 arquivos de teste** criados
- ✅ **215+ métodos de teste** implementados
- ✅ **~3.500 linhas** de código de teste
- ✅ **Fase 1 completa**: Classes Auxiliares (não-domínio)
- ⏳ **Fase 2 pendente**: Classes de Domínio

## 📁 Estrutura

A estrutura de testes espelha a estrutura do código-fonte em `src/main/java`:

```
src/test/java/com/seguradora/hibrida/
├── aggregate/           # Testes de Aggregate Infrastructure
│   ├── config/         # Properties (1 arquivo)
│   ├── exception/      # Exceptions (4 arquivos)
│   ├── health/         # Health Indicators (1 arquivo)
│   └── metrics/        # Metrics (1 arquivo)
├── command/            # Testes de Command Bus
│   ├── config/         # Properties (1 arquivo)
│   └── exception/      # Exceptions (5 arquivos)
├── config/             # Testes de Configuração
│   └── datasource/     # DataSource Properties (2 arquivos)
├── cqrs/               # Testes de CQRS
│   ├── health/         # Health Indicators (1 arquivo)
│   └── metrics/        # Metrics (1 arquivo)
├── eventbus/           # Testes de Event Bus
│   ├── config/         # Properties (1 arquivo)
│   └── exception/      # Exceptions (4 arquivos)
├── eventstore/         # Testes de Event Store
│   ├── archive/        # Archive Properties (1 arquivo)
│   ├── config/         # Properties (1 arquivo)
│   ├── exception/      # Exceptions (3 arquivos)
│   └── replay/         # Replay (4 arquivos)
├── projection/         # Testes de Projection
│   ├── config/         # Properties (1 arquivo)
│   ├── consistency/    # Consistency Properties (1 arquivo)
│   └── rebuild/        # Rebuild (2 arquivos)
└── snapshot/           # Testes de Snapshot
    ├── exception/      # Exceptions (2 arquivos)
    └── serialization/  # Serialization Exceptions (1 arquivo)
```

## 🎯 Categorias de Testes

### ✅ Completas (Fase 1)

1. **Properties Classes** (12 testes)
   - Configurações de módulos
   - Validação de valores padrão
   - Inner classes
   - Métodos auxiliares

2. **Exception Classes** (24 testes)
   - Hierarquia de exceções
   - Construtores com mensagem/causa
   - Getters de mensagem e causa

3. **Health Indicators** (2 testes)
   - Verificação de saúde
   - Status UP/DOWN/UNKNOWN
   - Detalhes de componentes

4. **Metrics** (2 testes)
   - Contadores e gauges
   - Timers de operações
   - Estatísticas customizadas

5. **DTOs** (1 teste)
   - Builder pattern
   - Method chaining
   - Getters e métodos auxiliares

### ⏳ Pendentes (Fase 2)

- Classes de Domínio (Aggregates, Events, Commands, Queries)
- Controllers
- Configuration Classes complexas
- Services de infraestrutura
- Repositories
- Health Indicators restantes
- Metrics restantes

## 🔧 Tecnologias

- **JUnit 5 (Jupiter)** - Framework de testes
- **AssertJ** - Assertions fluentes
- **Mockito** - Mocking de dependências
- **Micrometer** - Testing de métricas

## 🚀 Executar Testes

### Todos os testes

```bash
./mvnw test
```

### Por categoria

```bash
# Properties
./mvnw test -Dtest=*PropertiesTest

# Exceptions
./mvnw test -Dtest=*ExceptionTest

# Health Indicators
./mvnw test -Dtest=*HealthIndicatorTest

# Metrics
./mvnw test -Dtest=*MetricsTest
```

### Teste específico

```bash
./mvnw test -Dtest=CommandResultTest
```

### Com relatório de cobertura

```bash
./mvnw test jacoco:report
# Relatório: target/site/jacoco/index.html
```

## 📝 Padrões de Teste

### Nomenclatura

- **Arquivo**: `{NomeClasse}Test.java`
- **Pacote**: Mesmo pacote da classe em `src/main/java`
- **Método**: `shouldDoSomething()` ou `deveFazerAlgo()`

### Estrutura

```java
@DisplayName("Descrição legível do teste")
class MinhaClasseTest {

    @BeforeEach
    void setUp() {
        // Setup comum
    }

    @Test
    @DisplayName("Deve fazer algo específico")
    void shouldDoSomething() {
        // Given (Arrange)

        // When (Act)

        // Then (Assert)
    }
}
```

### Assertions

```java
// AssertJ (preferido)
assertThat(result).isNotNull();
assertThat(result.getValue()).isEqualTo(expected);
assertThat(list).hasSize(3).contains("item");

// JUnit 5 (quando necessário)
assertEquals(expected, actual);
assertNotNull(object);
```

### Mocking

```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {

    @Mock
    private Dependency dependency;

    @InjectMocks
    private MyService service;

    @Test
    void testMethod() {
        when(dependency.method()).thenReturn(value);
        // ...
        verify(dependency).method();
    }
}
```

## 📊 Cobertura

### Por Categoria

| Categoria | Cobertura |
|-----------|-----------|
| Properties | 90-95% |
| Exceptions | 95-100% |
| Health Indicators | 85-90% |
| Metrics | 85-90% |
| DTOs | 90-95% |
| **Média Geral** | **~92%** |

### Por Módulo

| Módulo | Classes | Testadas | % |
|--------|---------|----------|---|
| aggregate | 15 | 7 | 47% |
| command | 10 | 6 | 60% |
| eventbus | 8 | 5 | 63% |
| eventstore | 20 | 9 | 45% |
| projection | 12 | 5 | 42% |
| snapshot | 8 | 4 | 50% |
| cqrs | 6 | 2 | 33% |
| config | 4 | 2 | 50% |

## 📚 Documentação

### Relatórios

- **Sumário Executivo**: `doc/SUMARIO_TESTES_AUXILIARES.txt`
- **Relatório Final**: `doc/RELATORIO_FINAL_TESTES_AUXILIARES.md`
- **Relatório Técnico**: `doc/Relatorio_Testes_Unitarios_Classes_Auxiliares.md`

### Referências Externas

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

## ✅ Boas Práticas

1. ✅ **Um conceito por teste** - Teste apenas uma coisa
2. ✅ **Testes independentes** - Não dependem de ordem
3. ✅ **Nomenclatura clara** - Nome descreve o que testa
4. ✅ **Setup mínimo** - Apenas o necessário
5. ✅ **Assertions específicas** - Evite assertTrue genérico
6. ✅ **Mock apenas quando necessário** - Prefira objetos reais
7. ✅ **AAA pattern** - Arrange/Act/Assert
8. ✅ **DisplayName descritivo** - Facilita leitura de relatórios

## 🎯 Próximos Passos

1. ⏳ Adicionar testes de Controllers
2. ⏳ Adicionar testes de Configuration classes
3. ⏳ Completar Health Indicators e Metrics
4. ⏳ **Fase 2**: Testes de classes de Domínio
5. ⏳ Testes de integração
6. ⏳ Configurar threshold de cobertura no CI/CD

---

**Última atualização:** 13/03/2026
**Versão:** 1.0
**Status:** Fase 1 completa ✅
