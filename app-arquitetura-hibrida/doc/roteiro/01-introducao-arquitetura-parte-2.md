# 📖 INTRODUÇÃO À ARQUITETURA - PARTE 2
## Estrutura do Projeto e Organização de Código

### 🎯 **OBJETIVOS DESTA PARTE**
- Compreender a organização de pacotes do projeto
- Entender a separação de responsabilidades por módulo
- Conhecer as convenções de nomenclatura adotadas
- Identificar onde encontrar cada tipo de componente

---

## 📁 **ESTRUTURA GERAL DO PROJETO**

### **🗂️ Visão de Alto Nível**

```
app-arquitetura-hibrida/
├── 📁 src/main/java/com/seguradora/hibrida/
│   ├── 📁 aggregate/           # Event Sourcing & Agregados
│   ├── 📁 command/             # Command Side (CQRS)
│   ├── 📁 query/               # Query Side (CQRS)
│   ├── 📁 eventstore/          # Persistência de Eventos
│   ├── 📁 eventbus/            # Comunicação de Eventos
│   ├── 📁 projection/          # Handlers de Projeção
│   ├── 📁 cqrs/                # Monitoramento CQRS
│   ├── 📁 config/              # Configurações Spring
│   └── 📁 controller/          # Health Checks Gerais
├── 📁 src/main/resources/
│   ├── 📄 application.yml      # Configuração Principal
│   ├── 📁 db/migration/        # Scripts Flyway (Write DB)
│   └── 📁 db/migration-projections/ # Scripts Flyway (Read DB)
├── 📁 docker/                  # Configurações Docker
├── 📄 docker-compose.yml      # Ambiente Local
└── 📄 pom.xml                  # Dependências Maven
```

---

## 🧩 **MÓDULOS PRINCIPAIS**

### **1. 📝 Command Side - Pacote `command`**

#### **Estrutura:**
```java
com.seguradora.hibrida.command/
├── 📄 Command.java                    // Interface base
├── 📄 CommandHandler.java             // Interface para handlers
├── 📄 CommandBus.java                 // Interface do bus
├── 📄 CommandResult.java              // Resultado de execução
├── 📄 CommandHandlerRegistry.java     // Registro de handlers
├── 📁 config/
│   ├── 📄 CommandBusConfiguration.java
│   ├── 📄 CommandBusProperties.java
│   ├── 📄 CommandBusMetrics.java
│   └── 📄 CommandBusHealthIndicator.java
├── 📁 controller/
│   └── 📄 CommandBusController.java   // API de monitoramento
├── 📁 exception/
│   ├── 📄 CommandException.java
│   ├── 📄 CommandHandlerNotFoundException.java
│   ├── 📄 CommandTimeoutException.java
│   └── 📄 CommandValidationException.java
├── 📁 impl/
│   └── 📄 SimpleCommandBus.java       // Implementação em memória
├── 📁 validation/
│   ├── 📄 CommandValidator.java
│   └── 📄 ValidationResult.java
└── 📁 example/
    ├── 📄 TestCommand.java
    └── 📄 TestCommandHandler.java
```

#### **Responsabilidades:**
- **Commands**: Representam intenções de mudança
- **Handlers**: Processam comandos e aplicam regras
- **Bus**: Roteia comandos para handlers apropriados
- **Validation**: Valida comandos antes do processamento

#### **Exemplo de Uso:**
```java
// Localizar um Command Handler
@Component
public class CriarSinistroCommandHandler implements CommandHandler<CriarSinistroCommand> {
    
    @Override
    public CommandResult handle(CriarSinistroCommand command) {
        // Implementação em: command/example/
        // Padrão: {Ação}{Entidade}CommandHandler
    }
}
```

### **2. 🔍 Query Side - Pacote `query`**

#### **Estrutura:**
```java
com.seguradora.hibrida.query/
├── 📁 model/
│   └── 📄 SinistroQueryModel.java     // Modelo de leitura
├── 📁 repository/
│   └── 📄 SinistroQueryRepository.java // JPA Repository
├── 📁 service/
│   ├── 📄 SinistroQueryService.java   // Interface
│   └── 📄 SinistroQueryServiceImpl.java // Implementação
├── 📁 controller/
│   └── 📄 SinistroQueryController.java // REST API
├── 📁 dto/
│   ├── 📄 SinistroListView.java       // DTO para listagem
│   ├── 📄 SinistroDetailView.java     // DTO para detalhes
│   ├── 📄 DashboardView.java          // DTO para dashboard
│   └── 📄 SinistroFilter.java         // Filtros de consulta
└── 📁 config/
    └── 📄 QueryCacheConfiguration.java // Cache para consultas
```

#### **Convenções de Nomenclatura:**
- **Models**: `{Entidade}QueryModel` - Entidades JPA otimizadas para leitura
- **Repositories**: `{Entidade}QueryRepository` - Repositórios JPA com consultas customizadas
- **Services**: `{Entidade}QueryService` - Lógica de consulta
- **Controllers**: `{Entidade}QueryController` - APIs REST de leitura
- **DTOs**: `{Entidade}{Tipo}View` - Objetos de transferência

### **3. 💾 Event Store - Pacote `eventstore`**

#### **Estrutura:**
```java
com.seguradora.hibrida.eventstore/
├── 📄 EventStore.java                 // Interface principal
├── 📁 model/
│   ├── 📄 DomainEvent.java           // Evento base
│   └── 📄 EventMetadata.java         // Metadados do evento
├── 📁 entity/
│   └── 📄 EventStoreEntry.java       // Entidade JPA
├── 📁 repository/
│   └── 📄 EventStoreRepository.java  // Repositório JPA
├── 📁 impl/
│   └── 📄 PostgreSQLEventStore.java  // Implementação PostgreSQL
├── 📁 serialization/
│   ├── 📄 EventSerializer.java       // Interface
│   ├── 📄 JsonEventSerializer.java   // Implementação JSON
│   └── 📄 SerializationResult.java   // Resultado da serialização
├── 📁 config/
│   ├── 📄 EventStoreConfiguration.java
│   ├── 📄 EventStoreProperties.java
│   ├── 📄 EventStoreMetrics.java
│   └── 📄 EventStoreHealthIndicator.java
├── 📁 controller/
│   ├── 📄 EventStoreController.java  // API de consulta
│   └── 📄 EventStoreMaintenanceController.java // API de manutenção
├── 📁 partition/
│   ├── 📄 PartitionManager.java      // Gerenciamento de partições
│   └── 📄 PartitionStatistics.java  // Estatísticas
├── 📁 archive/
│   ├── 📄 EventArchiver.java         // Arquivamento
│   ├── 📄 ArchiveStorageService.java // Interface de storage
│   └── 📁 impl/
│       └── 📄 FileSystemArchiveStorage.java
└── 📁 exception/
    ├── 📄 EventStoreException.java
    ├── 📄 ConcurrencyException.java
    └── 📄 SerializationException.java
```

#### **Funcionalidades Implementadas:**
- **Persistência**: Armazenamento imutável de eventos
- **Serialização**: JSON com suporte a compressão
- **Particionamento**: Partições mensais para performance
- **Arquivamento**: Storage frio para eventos antigos
- **Consultas**: APIs para acesso aos eventos

### **4. 📡 Event Bus - Pacote `eventbus`**

#### **Estrutura:**
```java
com.seguradora.hibrida.eventbus/
├── 📄 EventBus.java                   // Interface principal
├── 📄 EventHandler.java               // Interface para handlers
├── 📄 EventHandlerRegistry.java       // Registro de handlers
├── 📁 impl/
│   ├── 📄 SimpleEventBus.java         // Implementação em memória
│   └── 📄 KafkaEventBus.java          // Implementação Kafka
├── 📁 config/
│   ├── 📄 EventBusConfiguration.java
│   ├── 📄 EventBusProperties.java
│   ├── 📄 EventBusMetrics.java
│   ├── 📄 EventBusHealthIndicator.java
│   └── 📄 KafkaEventBusConfiguration.java
├── 📁 controller/
│   └── 📄 EventBusController.java     // API de monitoramento
├── 📁 exception/
│   ├── 📄 EventBusException.java
│   ├── 📄 EventHandlingException.java
│   ├── 📄 EventPublishingException.java
│   └── 📄 EventHandlerTimeoutException.java
└── 📁 example/
    ├── 📄 SinistroEvent.java          // Evento de exemplo
    ├── 📄 SinistroEventHandler.java   // Handler de exemplo
    ├── 📄 TestEvent.java
    └── 📄 TestEventHandler.java
```

#### **Implementações Disponíveis:**
- **SimpleEventBus**: Para desenvolvimento e testes
- **KafkaEventBus**: Para produção com alta disponibilidade

### **5. 🔄 Projections - Pacote `projection`**

#### **Estrutura:**
```java
com.seguradora.hibrida.projection/
├── 📄 ProjectionHandler.java          // Interface base
├── 📄 ProjectionEventProcessor.java   // Processador principal
├── 📄 ProjectionRegistry.java         // Registro de handlers
├── 📄 AbstractProjectionHandler.java // Classe base
├── 📁 tracking/
│   ├── 📄 ProjectionTracker.java      // Entidade de tracking
│   ├── 📄 ProjectionTrackerRepository.java // Repositório
│   └── 📄 ProjectionStatus.java       // Enum de status
├── 📁 config/
│   ├── 📄 ProjectionConfiguration.java
│   ├── 📄 ProjectionProperties.java
│   └── 📄 ProjectionRebuildConfiguration.java
├── 📁 controller/
│   └── 📄 ProjectionController.java   // API de gerenciamento
├── 📁 rebuild/
│   ├── 📄 ProjectionRebuilder.java    // Rebuild de projeções
│   ├── 📄 ProjectionRebuildProperties.java
│   ├── 📄 RebuildResult.java
│   └── 📄 RebuildStatus.java
├── 📁 consistency/
│   ├── 📄 ProjectionConsistencyChecker.java
│   ├── 📄 ConsistencyReport.java
│   └── 📄 ConsistencyIssue.java
├── 📁 scheduler/
│   └── 📄 ProjectionMaintenanceScheduler.java
└── 📁 example/
    ├── 📄 SinistroProjectionHandler.java
    └── 📄 SeguradoProjectionHandler.java
```

#### **Funcionalidades:**
- **Tracking**: Controle de posição de cada projeção
- **Rebuild**: Reconstrução automática de projeções
- **Consistency**: Verificação de consistência
- **Maintenance**: Manutenção automática

### **6. 🧩 Aggregates - Pacote `aggregate`**

#### **Estrutura:**
```java
com.seguradora.hibrida.aggregate/
├── 📄 AggregateRoot.java              // Classe base
├── 📄 EventSourcingHandler.java       // Anotação para handlers
├── 📁 repository/
│   ├── 📄 AggregateRepository.java    // Interface
│   └── 📄 EventSourcingAggregateRepository.java // Implementação
├── 📁 config/
│   ├── 📄 AggregateConfiguration.java
│   ├── 📄 AggregateProperties.java
│   └── 📄 AggregateMetrics.java
├── 📁 controller/
│   └── 📄 AggregateController.java    // API de monitoramento
├── 📁 validation/
│   └── 📄 BusinessRule.java           // Interface para regras
├── 📁 exception/
│   ├── 📄 AggregateException.java
│   ├── 📄 AggregateNotFoundException.java
│   └── 📄 BusinessRuleViolationException.java
├── 📁 metrics/
│   └── 📄 AggregateMetrics.java       // Métricas específicas
├── 📁 health/
│   └── 📄 AggregateHealthIndicator.java
└── 📁 example/
    ├── 📄 ExampleAggregate.java       // Aggregate de exemplo
    ├── 📄 ExampleCreatedEvent.java
    ├── 📄 ExampleUpdatedEvent.java
    ├── 📄 ExampleActivatedEvent.java
    └── 📄 ExampleDeactivatedEvent.java
```

---

## 🔧 **CONFIGURAÇÕES E INFRAESTRUTURA**

### **⚙️ Pacote `config`**

#### **Estrutura:**
```java
com.seguradora.hibrida.config/
├── 📄 OpenApiConfig.java              // Swagger/OpenAPI
├── 📄 SecurityConfig.java             // Segurança básica
├── 📁 datasource/
│   ├── 📄 DataSourceConfiguration.java // Configuração geral
│   ├── 📄 WriteDataSourceConfiguration.java // Write DB
│   ├── 📄 ReadDataSourceConfiguration.java  // Read DB
│   ├── 📄 WriteJpaConfiguration.java
│   ├── 📄 ReadJpaConfiguration.java
│   ├── 📄 WriteDataSourceProperties.java
│   ├── 📄 ReadDataSourceProperties.java
│   └── 📄 SimpleDataSourceHealthIndicator.java
└── 📁 cqrs/
    ├── 📄 CQRSConfiguration.java      // Configuração CQRS
    ├── 📄 CQRSMetrics.java            // Métricas CQRS
    └── 📄 CQRSHealthIndicator.java    // Health Check CQRS
```

### **📊 Monitoramento - Pacote `cqrs`**

#### **Estrutura:**
```java
com.seguradora.hibrida.cqrs/
├── 📁 config/
│   └── 📄 CQRSConfiguration.java      // Configuração
├── 📁 controller/
│   └── 📄 CQRSController.java         // API de monitoramento
├── 📁 health/
│   └── 📄 CQRSHealthIndicator.java    // Health checks
└── 📁 metrics/
    └── 📄 CQRSMetrics.java            // Métricas específicas
```

---

## 📋 **CONVENÇÕES DE NOMENCLATURA**

### **🏷️ Padrões Adotados**

#### **Classes e Interfaces:**
```java
// Interfaces
public interface {Funcionalidade}                    // EventStore, CommandBus
public interface {Entidade}{Funcionalidade}          // SinistroQueryService

// Implementações
public class {Tecnologia}{Interface}                 // PostgreSQLEventStore
public class Simple{Interface}                       // SimpleCommandBus
public class {Entidade}{Interface}Impl               // SinistroQueryServiceImpl

// Configurações
public class {Modulo}Configuration                   // EventStoreConfiguration
public class {Modulo}Properties                      // CommandBusProperties

// Controllers
public class {Modulo}Controller                      // EventStoreController
public class {Entidade}QueryController               // SinistroQueryController

// Exceptions
public class {Modulo}Exception                       // EventStoreException
public class {Situacao}Exception                     // CommandTimeoutException
```

#### **Pacotes:**
```java
// Por funcionalidade
com.seguradora.hibrida.{funcionalidade}             // command, query, eventstore

// Por tipo dentro da funcionalidade
{funcionalidade}.config                              // Configurações
{funcionalidade}.controller                          // APIs REST
{funcionalidade}.exception                           // Exceções específicas
{funcionalidade}.impl                                // Implementações
{funcionalidade}.example                             // Exemplos e testes
```

#### **Métodos:**
```java
// Command Handlers
public CommandResult handle({Command} command)

// Event Handlers  
public void handle({Event} event)

// Query Services
public Page<{Entity}ListView> listar(Filter filter, Pageable pageable)
public Optional<{Entity}DetailView> buscarPorId(UUID id)

// Repositories
public List<{Entity}> findBy{Criterio}OrderBy{Campo}Asc(...)
```

---

## 🗃️ **RECURSOS E CONFIGURAÇÕES**

### **📁 Estrutura de Resources**

```
src/main/resources/
├── 📄 application.yml                 # Configuração principal
├── 📄 application-dev.yml             # Desenvolvimento
├── 📄 application-test.yml            # Testes
├── 📄 application-prod.yml            # Produção
├── 📄 aggregate.yml                   # Configurações de agregados
├── 📄 command-bus.yml                 # Configurações do Command Bus
├── 📄 event-bus.yml                   # Configurações do Event Bus
├── 📄 projection-rebuild.yml          # Configurações de rebuild
├── 📄 replay.yml                      # Configurações de replay
├── 📁 db/
│   └── 📁 migration/                  # Flyway - Write DB
│       ├── 📄 V1__Create_Events_Table.sql
│       ├── 📄 V2__Create_Snapshots_Table.sql
│       ├── 📄 V3__Create_Projection_Tracking_Table.sql
│       ├── 📄 V4__Implement_Event_Partitioning.sql
│       └── 📄 V5__Create_Archive_Tables.sql
└── 📁 db/
    └── 📁 migration-projections/      # Flyway - Read DB
        └── 📄 V1__Create_Projections_Schema.sql
```

### **🐳 Infraestrutura Docker**

```
docker/
├── 📄 init-eventstore-db.sql         # Inicialização Write DB
├── 📄 init-projections-db.sql        # Inicialização Read DB
├── 📄 prometheus.yml                  # Configuração Prometheus
└── 📄 redis.conf                      # Configuração Redis
```

---

## 🎯 **NAVEGAÇÃO PRÁTICA**

### **📍 Como Encontrar Componentes**

#### **Para implementar um novo Command:**
1. Criar em: `command/example/{NovoCommand}.java`
2. Handler em: `command/example/{NovoCommand}Handler.java`
3. Registrar automaticamente via `@Component`

#### **Para implementar uma nova Query:**
1. Model em: `query/model/{Entidade}QueryModel.java`
2. Repository em: `query/repository/{Entidade}QueryRepository.java`
3. Service em: `query/service/{Entidade}QueryService.java`
4. Controller em: `query/controller/{Entidade}QueryController.java`

#### **Para implementar um novo Event Handler:**
1. Handler em: `eventbus/example/{Evento}Handler.java`
2. Ou Projection em: `projection/example/{Entidade}ProjectionHandler.java`

#### **Para configurar um novo módulo:**
1. Configuration em: `config/{Modulo}Configuration.java`
2. Properties em: `config/{Modulo}Properties.java`
3. Health em: `config/{Modulo}HealthIndicator.java`

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [Spring Boot Package Structure](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.structuring-your-code)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Package by Feature](https://phauer.com/2020/package-by-feature/)

### **📖 Próximas Partes:**
- **Parte 3**: Configuração do Ambiente de Desenvolvimento
- **Parte 4**: Fluxos de Dados e Comunicação entre Componentes
- **Parte 5**: Exercícios Práticos e Checkpoint de Aprendizado

---

**📝 Parte 2 de 5 - Estrutura do Projeto**  
**⏱️ Tempo estimado**: 50 minutos  
**🎯 Próximo**: [Parte 3 - Configuração do Ambiente](./01-introducao-arquitetura-parte-3.md)