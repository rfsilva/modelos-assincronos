# Classes sem cobertura de testes

> Gerado em: 2026-03-17
> **446** classes de implementação · **248** com testes · **204** sem testes
> Última atualização: 2026-03-17 — implementados testes de `(root)`, `aggregate`, `command`, `config`, `cqrs`, `domain/apolice`, `domain/documento`, `domain/segurado`, `domain/veiculo` e `snapshot`

Legenda de status: `[ ]` pendente · `[x]` concluído

---

## (root)

| Status | Classe |
|--------|--------|
| [x] | `ArquiteturaHibridaApplication` |

---

## aggregate

| Status | Pacote | Classe |
|--------|--------|--------|
| [x] | `aggregate` | `AggregateRoot` |
| [x] | `aggregate` | `EventSourcingHandler` |
| [x] | `aggregate/config` | `AggregateConfiguration` |
| [x] | `aggregate/repository` | `AggregateRepository` |
| [x] | `aggregate/repository` | `EventSourcingAggregateRepository` |
| [x] | `aggregate/validation` | `BusinessRule` |
| [x] | `aggregate/example` | `ExampleActivatedEvent` |
| [x] | `aggregate/example` | `ExampleAggregate` |
| [x] | `aggregate/example` | `ExampleCreatedEvent` |
| [x] | `aggregate/example` | `ExampleDeactivatedEvent` |
| [x] | `aggregate/example` | `ExampleUpdatedEvent` |

---

## command

| Status | Pacote | Classe |
|--------|--------|--------|
| [x] | `command` | `Command` |
| [x] | `command` | `CommandBus` |
| [x] | `command` | `CommandHandler` |
| [x] | `command/config` | `CommandBusConfiguration` |
| [x] | `command/config` | `CommandBusHealthIndicator` |
| [x] | `command/config` | `CommandBusMetrics` |
| [x] | `command/exception` | `CommandException` |
| [x] | `command/exception` | `CommandTimeoutException` |
| [x] | `command/impl` | `SimpleCommandBus` |
| [x] | `command/validation` | `CommandValidator` |
| [x] | `command/validation` | `ValidationResult` |
| [x] | `command/example` | `TestCommand` |
| [x] | `command/example` | `TestCommandHandler` |

---

## config

| Status | Pacote | Classe |
|--------|--------|--------|
| [x] | `config/datasource` | `DataSourceConfiguration` |
| [x] | `config/datasource` | `ReadJpaConfiguration` |
| [x] | `config/datasource` | `WriteJpaConfiguration` |
| [x] | `config/datasource` | `SimpleDataSourceHealthIndicator` |
| [x] | `config/ratelimit` | `RateLimitInterceptor` |

---

## cqrs

| Status | Pacote | Classe |
|--------|--------|--------|
| [x] | `cqrs/config` | `CQRSConfiguration` |
| [x] | `cqrs/health` | `CQRSHealthIndicator` |
| [x] | `cqrs/monitoring` | `CQRSHealthIndicator` |

---

## domain/analytics

| Status | Pacote | Classe |
|--------|--------|--------|
| [ ] | `domain/analytics/model` | `AnalyticsProjection` |
| [ ] | `domain/analytics/model` | `TipoMetrica` |
| [ ] | `domain/analytics/handler` | `AnalyticsProjectionHandler` |
| [ ] | `domain/analytics/repository` | `AnalyticsProjectionRepository` |
| [ ] | `domain/analytics/service` | `RelatorioService` |
| [ ] | `domain/analytics/controller` | `RelatorioController` |
| [ ] | `domain/analytics/dto` | `DashboardExecutivoView` |
| [ ] | `domain/analytics/dto` | `EvolucaoTemporalView` |
| [ ] | `domain/analytics/dto` | `RelatorioApolicesView` |
| [ ] | `domain/analytics/dto` | `RelatorioPerformanceView` |
| [ ] | `domain/analytics/dto` | `RelatorioRenovacoesView` |
| [ ] | `domain/analytics/dto` | `RelatorioSeguradosView` |

---

## domain/apolice

| Status | Pacote | Classe |
|--------|--------|--------|
| [x] | `domain/apolice/service` | `ApoliceValidationService` |

---

## domain/documento

| Status | Pacote | Classe |
|--------|--------|--------|
| [x] | `domain/documento/event` | `DocumentoRejeitadoEvent` |
| [x] | `domain/documento/event` | `DocumentoValidadoEvent` |

---

## domain/segurado

| Status | Pacote | Classe |
|--------|--------|--------|
| [x] | `domain/segurado/controller/dto` | `CommandResponseDTO` |

---

## domain/sinistro

| Status | Pacote | Classe |
|--------|--------|--------|
| [ ] | `domain/sinistro/aggregate` | `SinistroAggregate` |
| [ ] | `domain/sinistro/command` | `AnexarDocumentoCommand` |
| [ ] | `domain/sinistro/command` | `AprovarSinistroCommand` |
| [ ] | `domain/sinistro/command` | `CriarSinistroCommand` |
| [ ] | `domain/sinistro/command` | `IniciarAnaliseCommand` |
| [ ] | `domain/sinistro/command` | `ReprovarSinistroCommand` |
| [ ] | `domain/sinistro/command` | `ValidarSinistroCommand` |
| [ ] | `domain/sinistro/command/handler` | `AnexarDocumentoCommandHandler` |
| [ ] | `domain/sinistro/command/handler` | `AprovarSinistroCommandHandler` |
| [ ] | `domain/sinistro/command/handler` | `CriarSinistroCommandHandler` |
| [ ] | `domain/sinistro/command/handler` | `IniciarAnaliseCommandHandler` |
| [ ] | `domain/sinistro/command/handler` | `ReprovarSinistroCommandHandler` |
| [ ] | `domain/sinistro/command/handler` | `ValidarSinistroCommandHandler` |
| [ ] | `domain/sinistro/config` | `SinistroCacheConfig` |
| [ ] | `domain/sinistro/config` | `SinistroCacheConfiguration` |
| [ ] | `domain/sinistro/controller` | `SinistroQueryController` |
| [ ] | `domain/sinistro/event` | `ConsultaDetranConcluidaEvent` |
| [ ] | `domain/sinistro/event` | `ConsultaDetranFalhadaEvent` |
| [ ] | `domain/sinistro/event` | `ConsultaDetranIniciadaEvent` |
| [ ] | `domain/sinistro/event` | `DocumentoAnexadoEvent` |
| [ ] | `domain/sinistro/event` | `DocumentoRejeitadoEvent` |
| [ ] | `domain/sinistro/event` | `DocumentoValidadoEvent` |
| [ ] | `domain/sinistro/event` | `SinistroAprovadoEvent` |
| [ ] | `domain/sinistro/event` | `SinistroCriadoEvent` |
| [ ] | `domain/sinistro/event` | `SinistroEmAnaliseEvent` |
| [ ] | `domain/sinistro/event` | `SinistroEvent` |
| [ ] | `domain/sinistro/event` | `SinistroEventHandler` |
| [ ] | `domain/sinistro/event` | `SinistroReprovadoEvent` |
| [ ] | `domain/sinistro/event` | `SinistroValidadoEvent` |
| [ ] | `domain/sinistro/model` | `AvaliacaoDanos` |
| [ ] | `domain/sinistro/model` | `DetranConsultaStatus` |
| [ ] | `domain/sinistro/model` | `LocalOcorrencia` |
| [ ] | `domain/sinistro/model` | `OcorrenciaSinistro` |
| [ ] | `domain/sinistro/model` | `PrazoProcessamento` |
| [ ] | `domain/sinistro/model` | `ProcessamentoDetran` |
| [ ] | `domain/sinistro/model` | `ProtocoloSinistro` |
| [ ] | `domain/sinistro/model` | `Sinistro` |
| [ ] | `domain/sinistro/model` | `SinistroStateMachine` |
| [ ] | `domain/sinistro/model` | `StatusSinistro` |
| [ ] | `domain/sinistro/model` | `TipoDano` |
| [ ] | `domain/sinistro/model` | `TipoSinistro` |
| [ ] | `domain/sinistro/model` | `ValorIndenizacao` |
| [ ] | `domain/sinistro/projection` | `SinistroDashboardProjection` |
| [ ] | `domain/sinistro/projection` | `SinistroProjectionHandler` |
| [ ] | `domain/sinistro/projection` | `SinistroProjectionRebuilder` |
| [ ] | `domain/sinistro/query/dto` | `DashboardView` |
| [ ] | `domain/sinistro/query/dto` | `SinistroDetailView` |
| [ ] | `domain/sinistro/query/dto` | `SinistroFilter` |
| [ ] | `domain/sinistro/query/dto` | `SinistroListView` |
| [ ] | `domain/sinistro/query/model` | `SinistroAnalyticsView` |
| [ ] | `domain/sinistro/query/model` | `SinistroDashboardView` |
| [ ] | `domain/sinistro/query/model` | `SinistroDetailView` |
| [ ] | `domain/sinistro/query/model` | `SinistroListView` |
| [ ] | `domain/sinistro/query/model` | `SinistroQueryModel` |
| [ ] | `domain/sinistro/query/repository` | `SinistroAnalyticsRepository` |
| [ ] | `domain/sinistro/query/repository` | `SinistroDashboardRepository` |
| [ ] | `domain/sinistro/query/repository` | `SinistroDetailRepository` |
| [ ] | `domain/sinistro/query/repository` | `SinistroListRepository` |
| [ ] | `domain/sinistro/query/repository` | `SinistroQueryRepository` |
| [ ] | `domain/sinistro/query/service` | `SinistroQueryService` |
| [ ] | `domain/sinistro/query/service` | `SinistroQueryServiceImpl` |

---

## domain/veiculo

| Status | Pacote | Classe |
|--------|--------|--------|
| [x] | `domain/veiculo/service` | `ApoliceValidationService` |
| [x] | `domain/veiculo/controller/dto` | `CommandResponseDTO` |

---

## domain/workflow

| Status | Pacote | Classe |
|--------|--------|--------|
| [ ] | `domain/workflow/approval` | `Aprovacao` |
| [ ] | `domain/workflow/approval` | `AprovacaoNotificationService` |
| [ ] | `domain/workflow/approval` | `AprovacaoService` |
| [ ] | `domain/workflow/approval` | `AprovadorPolicy` |
| [ ] | `domain/workflow/config` | `WorkflowConfiguration` |
| [ ] | `domain/workflow/defaults` | `WorkflowTemplates` |
| [ ] | `domain/workflow/engine` | `WorkflowEngine` |
| [ ] | `domain/workflow/engine` | `WorkflowEngineImpl` |
| [ ] | `domain/workflow/engine` | `WorkflowExecutor` |
| [ ] | `domain/workflow/execution` | `EtapaExecucao` |
| [ ] | `domain/workflow/execution` | `WorkflowContext` |
| [ ] | `domain/workflow/execution` | `WorkflowInstance` |
| [ ] | `domain/workflow/execution` | `WorkflowResult` |
| [ ] | `domain/workflow/metrics` | `SlaConfiguration` |
| [ ] | `domain/workflow/metrics` | `SlaMonitor` |
| [ ] | `domain/workflow/metrics` | `WorkflowMetrics` |
| [ ] | `domain/workflow/model` | `EtapaWorkflow` |
| [ ] | `domain/workflow/model` | `NivelAprovacao` |
| [ ] | `domain/workflow/model` | `StatusEtapa` |
| [ ] | `domain/workflow/model` | `TipoEtapa` |
| [ ] | `domain/workflow/model` | `TransicaoWorkflow` |
| [ ] | `domain/workflow/model` | `WorkflowDefinition` |
| [ ] | `domain/workflow/repository` | `AprovacaoRepository` |
| [ ] | `domain/workflow/repository` | `WorkflowDefinitionRepository` |
| [ ] | `domain/workflow/repository` | `WorkflowInstanceRepository` |

---

## eventbus

| Status | Pacote | Classe |
|--------|--------|--------|
| [ ] | `eventbus` | `EventBus` |
| [ ] | `eventbus` | `EventBusStatistics` |
| [ ] | `eventbus` | `EventHandler` |
| [ ] | `eventbus` | `EventHandlerRegistry` |
| [ ] | `eventbus/config` | `EventBusConfiguration` |
| [ ] | `eventbus/config` | `EventBusHealthIndicator` |
| [ ] | `eventbus/config` | `EventBusMetrics` |
| [ ] | `eventbus/config` | `KafkaEventBusConfiguration` |
| [ ] | `eventbus/impl` | `KafkaEventBus` |
| [ ] | `eventbus/impl` | `SimpleEventBus` |
| [ ] | `eventbus/example` | `TestEvent` |
| [ ] | `eventbus/example` | `TestEventHandler` |

---

## eventstore

| Status | Pacote | Classe |
|--------|--------|--------|
| [ ] | `eventstore` | `EventStore` |
| [ ] | `eventstore/config` | `EventStoreConfiguration` |
| [ ] | `eventstore/config` | `EventStoreHealthIndicator` |
| [ ] | `eventstore/config` | `EventStoreMetrics` |
| [ ] | `eventstore/entity` | `EventStoreEntry` |
| [ ] | `eventstore/exception` | `ConcurrencyException` |
| [ ] | `eventstore/impl` | `PostgreSQLEventStore` |
| [ ] | `eventstore/model` | `DomainEvent` |
| [ ] | `eventstore/model` | `EventMetadata` |
| [ ] | `eventstore/partition` | `PartitionManager` |
| [ ] | `eventstore/partition` | `PartitionStatistics` |
| [ ] | `eventstore/repository` | `EventStoreRepository` |
| [ ] | `eventstore/scheduler` | `EventStoreMaintenanceScheduler` |
| [ ] | `eventstore/serialization` | `EventSerializer` |
| [ ] | `eventstore/serialization` | `JsonEventSerializer` |
| [ ] | `eventstore/serialization` | `SerializationResult` |
| [ ] | `eventstore/archive` | `ArchiveMetadata` |
| [ ] | `eventstore/archive` | `ArchiveResult` |
| [ ] | `eventstore/archive` | `ArchiveStatistics` |
| [ ] | `eventstore/archive` | `ArchiveStorageService` |
| [ ] | `eventstore/archive` | `ArchiveSummary` |
| [ ] | `eventstore/archive` | `EventArchiver` |
| [ ] | `eventstore/archive/impl` | `FileSystemArchiveStorage` |
| [ ] | `eventstore/replay` | `EventReplayer` |
| [ ] | `eventstore/replay` | `ReplayConfiguration` |
| [ ] | `eventstore/replay` | `ReplayDetailedReport` |
| [ ] | `eventstore/replay` | `ReplayError` |
| [ ] | `eventstore/replay` | `ReplayFilter` |
| [ ] | `eventstore/replay` | `ReplayStatistics` |
| [ ] | `eventstore/replay/config` | `ReplayConfiguration` |
| [ ] | `eventstore/replay/config` | `ReplayHealthIndicator` |
| [ ] | `eventstore/replay/config` | `ReplayMetrics` |
| [ ] | `eventstore/replay/example` | `ReplayExampleService` |
| [ ] | `eventstore/replay/impl` | `DefaultEventReplayer` |

---

## projection

| Status | Pacote | Classe |
|--------|--------|--------|
| [ ] | `projection` | `AbstractProjectionHandler` |
| [ ] | `projection` | `ProjectionEventProcessor` |
| [ ] | `projection` | `ProjectionHandler` |
| [ ] | `projection` | `ProjectionRegistry` |
| [ ] | `projection/config` | `ProjectionConfiguration` |
| [ ] | `projection/config` | `ProjectionRebuildConfiguration` |
| [ ] | `projection/consistency` | `ConsistencyIssue` |
| [ ] | `projection/consistency` | `ConsistencyReport` |
| [ ] | `projection/consistency` | `IssueSeverity` |
| [ ] | `projection/consistency` | `IssueType` |
| [ ] | `projection/consistency` | `ProjectionConsistencyChecker` |
| [ ] | `projection/rebuild` | `ProjectionRebuilder` |
| [ ] | `projection/rebuild` | `RebuildResult` |
| [ ] | `projection/rebuild` | `RebuildStatus` |
| [ ] | `projection/rebuild` | `RebuildType` |
| [ ] | `projection/scheduler` | `ProjectionMaintenanceScheduler` |
| [ ] | `projection/tracking` | `ProjectionStatus` |
| [ ] | `projection/tracking` | `ProjectionTracker` |
| [ ] | `projection/tracking` | `ProjectionTrackerRepository` |
| [ ] | `projection/versioning` | `ProjectionVersion` |
| [ ] | `projection/versioning` | `ProjectionVersionRepository` |

---

## snapshot

| Status | Pacote | Classe |
|--------|--------|--------|
| [x] | `snapshot` | `SnapshotEfficiencyMetrics` |
| [x] | `snapshot` | `SnapshotStatistics` |
| [x] | `snapshot` | `SnapshotStore` |
| [x] | `snapshot/config` | `SnapshotCleanupScheduler` |
| [x] | `snapshot/config` | `SnapshotConfiguration` |
| [x] | `snapshot/config` | `SnapshotHealthIndicator` |
| [x] | `snapshot/config` | `SnapshotMetrics` |
| [x] | `snapshot/entity` | `SnapshotEntry` |
| [x] | `snapshot/exception` | `SnapshotCompressionException` |
| [x] | `snapshot/impl` | `PostgreSQLSnapshotStore` |
| [x] | `snapshot/repository` | `SnapshotRepository` |
| [x] | `snapshot/serialization` | `JsonSnapshotSerializer` |
| [x] | `snapshot/serialization` | `SnapshotSerializationResult` |
| [x] | `snapshot/serialization` | `SnapshotSerializer` |
| [x] | `snapshot/trigger` | `SnapshotTrigger` |
