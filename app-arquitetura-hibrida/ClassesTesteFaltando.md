# Classes sem cobertura de testes

> Gerado em: 2026-03-17
> Última revisão: 2026-03-18 (varredura completa confirmada)
> **446** classes de implementação · **446** arquivos de teste · **0** classes de produção sem testes
> Todos os domínios concluídos: `(root)`, `aggregate`, `command`, `config`, `cqrs`, `domain/apolice`, `domain/analytics`, `domain/documento`, `domain/segurado`, `domain/sinistro`, `domain/veiculo`, `domain/workflow`, `snapshot`, `eventbus`, `projection`, `eventstore`
>
> ✅ Varredura realizada em 2026-03-18: todas as 446 classes de produção possuem arquivo de teste associado.

---

## Cobertura de código (JaCoCo) — 2026-03-18

| Métrica | Coberto | Total | Percentual |
|---------|---------|-------|------------|
| Instruções | 91.663 | 141.143 | **64,9%** |
| Branches | 5.752 | 12.840 | **44,8%** |
| Linhas | 17.486 | 22.697 | **77,0%** |
| Métodos | 6.369 | 8.757 | **72,7%** |
| Complexidade ciclomática | 8.439 | 15.291 | **55,2%** |

### Pacotes com cobertura abaixo de 20% (oportunidades de melhoria)

| Pacote | Instruções | Branches | Linhas | Métodos |
|--------|-----------|---------|--------|---------|
| `cqrs.config` | 0% | 0% | 0% | 0% |
| `eventstore.replay.impl` | 0% | 0% | 0% | 0% |
| `eventstore.impl` | 0% | 0% | 0% | 0% |
| `eventstore.replay.example` | 0% | 0% | 0% | 0% |
| `eventstore.archive.impl` | 0% | 0% | 0% | 0% |
| `eventstore.scheduler` | 0% | 0% | 0% | 0% |
| `projection.scheduler` | 1% | 0% | 1% | 7% |
| `domain.analytics.handler` | 3% | 0% | 5% | 27% |
| `eventbus.impl` | 14% | 18% | 19% | 24% |
| `snapshot.impl` | 18% | 15% | 17% | 38% |

### Pacotes com cobertura ≥ 90%

| Pacote | Instruções | Branches |
|--------|-----------|---------|
| `domain.veiculo.relationship.model` | 100% | 98% |
| `domain.veiculo.query.model` | 100% | 88% |
| `domain.apolice.event` | 100% | 79% |
| `snapshot.controller` | 100% | 93% |
| `snapshot.trigger` | 100% | 86% |
| `domain.veiculo.service` | 100% | 100% |
| `config.ratelimit` | 100% | 100% |
| `domain.veiculo.command.handler` | 100% | 100% |
| `domain.veiculo.relationship.scheduler` | 100% | 100% |
| `eventstore.controller` | 100% | 100% |
| `domain.apolice.command.handler` | 98% | 96% |
| `domain.apolice.service` | 98% | 91% |
| `domain.veiculo.controller` | 98% | 83% |
| `domain.apolice.command` | 98% | 65% |
| `domain.apolice.query.service` | 98% | 67% |

---

Legenda de status: `[ ]` pendente · `[x]` concluído · `[-]` não aplicável (interface sem lógica própria)

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
| [x] | `domain/analytics/model` | `AnalyticsProjection` |
| [x] | `domain/analytics/model` | `TipoMetrica` |
| [x] | `domain/analytics/handler` | `AnalyticsProjectionHandler` |
| [x] | `domain/analytics/repository` | `AnalyticsProjectionRepository` |
| [x] | `domain/analytics/service` | `RelatorioService` |
| [x] | `domain/analytics/controller` | `RelatorioController` |
| [x] | `domain/analytics/dto` | `DashboardExecutivoView` |
| [x] | `domain/analytics/dto` | `EvolucaoTemporalView` |
| [x] | `domain/analytics/dto` | `RelatorioApolicesView` |
| [x] | `domain/analytics/dto` | `RelatorioPerformanceView` |
| [x] | `domain/analytics/dto` | `RelatorioRenovacoesView` |
| [x] | `domain/analytics/dto` | `RelatorioSeguradosView` |

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

> **Situação atual:** 44 de 58 classes com testes → **14 pendentes** (grupos `query/dto` parcial, `query/model`, `query/repository` e `query/service`).

### aggregate / command / command.handler / config / controller / event / model / projection — todos concluídos ✓

| Status | Pacote | Classe |
|--------|--------|--------|
| [x] | `domain/sinistro/aggregate` | `SinistroAggregate` |
| [x] | `domain/sinistro/command` | `AnexarDocumentoCommand` |
| [x] | `domain/sinistro/command` | `AprovarSinistroCommand` |
| [x] | `domain/sinistro/command` | `CriarSinistroCommand` |
| [x] | `domain/sinistro/command` | `IniciarAnaliseCommand` |
| [x] | `domain/sinistro/command` | `ReprovarSinistroCommand` |
| [x] | `domain/sinistro/command` | `ValidarSinistroCommand` |
| [x] | `domain/sinistro/command/handler` | `AnexarDocumentoCommandHandler` |
| [x] | `domain/sinistro/command/handler` | `AprovarSinistroCommandHandler` |
| [x] | `domain/sinistro/command/handler` | `CriarSinistroCommandHandler` |
| [x] | `domain/sinistro/command/handler` | `IniciarAnaliseCommandHandler` |
| [x] | `domain/sinistro/command/handler` | `ReprovarSinistroCommandHandler` |
| [x] | `domain/sinistro/command/handler` | `ValidarSinistroCommandHandler` |
| [x] | `domain/sinistro/config` | `SinistroCacheConfig` |
| [x] | `domain/sinistro/config` | `SinistroCacheConfiguration` |
| [x] | `domain/sinistro/controller` | `SinistroQueryController` |
| [x] | `domain/sinistro/event` | `ConsultaDetranConcluidaEvent` |
| [x] | `domain/sinistro/event` | `ConsultaDetranFalhadaEvent` |
| [x] | `domain/sinistro/event` | `ConsultaDetranIniciadaEvent` |
| [x] | `domain/sinistro/event` | `DocumentoAnexadoEvent` |
| [x] | `domain/sinistro/event` | `DocumentoRejeitadoEvent` |
| [x] | `domain/sinistro/event` | `DocumentoValidadoEvent` |
| [x] | `domain/sinistro/event` | `SinistroAprovadoEvent` |
| [x] | `domain/sinistro/event` | `SinistroCriadoEvent` |
| [x] | `domain/sinistro/event` | `SinistroEmAnaliseEvent` |
| [x] | `domain/sinistro/event` | `SinistroEvent` |
| [x] | `domain/sinistro/event` | `SinistroEventHandler` |
| [x] | `domain/sinistro/event` | `SinistroReprovadoEvent` |
| [x] | `domain/sinistro/event` | `SinistroValidadoEvent` |
| [x] | `domain/sinistro/model` | `AvaliacaoDanos` |
| [x] | `domain/sinistro/model` | `DetranConsultaStatus` |
| [x] | `domain/sinistro/model` | `LocalOcorrencia` |
| [x] | `domain/sinistro/model` | `OcorrenciaSinistro` |
| [x] | `domain/sinistro/model` | `PrazoProcessamento` |
| [x] | `domain/sinistro/model` | `ProcessamentoDetran` |
| [x] | `domain/sinistro/model` | `ProtocoloSinistro` |
| [x] | `domain/sinistro/model` | `Sinistro` |
| [x] | `domain/sinistro/model` | `SinistroStateMachine` |
| [x] | `domain/sinistro/model` | `StatusSinistro` |
| [x] | `domain/sinistro/model` | `TipoDano` |
| [x] | `domain/sinistro/model` | `TipoSinistro` |
| [x] | `domain/sinistro/model` | `ValorIndenizacao` |
| [x] | `domain/sinistro/projection` | `SinistroDashboardProjection` |
| [x] | `domain/sinistro/projection` | `SinistroProjectionHandler` |
| [x] | `domain/sinistro/projection` | `SinistroProjectionRebuilder` |

### query/dto — parcialmente concluído

| Status | Pacote | Classe |
|--------|--------|--------|
| [x] | `domain/sinistro/query/dto` | `DashboardView` |
| [x] | `domain/sinistro/query/dto` | `SinistroDetailView` |
| [x] | `domain/sinistro/query/dto` | `SinistroFilter` |
| [x] | `domain/sinistro/query/dto` | `SinistroListView` |

### query/model

| Status | Pacote | Classe |
|--------|--------|--------|
| [x] | `domain/sinistro/query/model` | `SinistroAnalyticsView` |
| [x] | `domain/sinistro/query/model` | `SinistroDashboardView` |
| [x] | `domain/sinistro/query/model` | `SinistroDetailView` |
| [x] | `domain/sinistro/query/model` | `SinistroListView` |
| [x] | `domain/sinistro/query/model` | `SinistroQueryModel` |

### query/repository

| Status | Pacote | Classe |
|--------|--------|--------|
| [x] | `domain/sinistro/query/repository` | `SinistroAnalyticsRepository` |
| [x] | `domain/sinistro/query/repository` | `SinistroDashboardRepository` |
| [x] | `domain/sinistro/query/repository` | `SinistroDetailRepository` |
| [x] | `domain/sinistro/query/repository` | `SinistroListRepository` |
| [x] | `domain/sinistro/query/repository` | `SinistroQueryRepository` |

### query/service

| Status | Pacote | Classe |
|--------|--------|--------|
| [x] | `domain/sinistro/query/service` | `SinistroQueryService` |
| [x] | `domain/sinistro/query/service` | `SinistroQueryServiceImpl` |

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
| [x] | `domain/workflow/approval` | `Aprovacao` |
| [x] | `domain/workflow/approval` | `AprovacaoNotificationService` |
| [x] | `domain/workflow/approval` | `AprovacaoService` |
| [x] | `domain/workflow/approval` | `AprovadorPolicy` |
| [x] | `domain/workflow/config` | `WorkflowConfiguration` |
| [x] | `domain/workflow/defaults` | `WorkflowTemplates` |
| [x] | `domain/workflow/engine` | `WorkflowEngine` |
| [x] | `domain/workflow/engine` | `WorkflowEngineImpl` |
| [x] | `domain/workflow/engine` | `WorkflowExecutor` |
| [x] | `domain/workflow/execution` | `EtapaExecucao` |
| [x] | `domain/workflow/execution` | `WorkflowContext` |
| [x] | `domain/workflow/execution` | `WorkflowInstance` |
| [x] | `domain/workflow/execution` | `WorkflowResult` |
| [x] | `domain/workflow/metrics` | `SlaConfiguration` |
| [x] | `domain/workflow/metrics` | `SlaMonitor` |
| [x] | `domain/workflow/metrics` | `WorkflowMetrics` |
| [x] | `domain/workflow/model` | `EtapaWorkflow` |
| [x] | `domain/workflow/model` | `NivelAprovacao` |
| [x] | `domain/workflow/model` | `StatusEtapa` |
| [x] | `domain/workflow/model` | `TipoEtapa` |
| [x] | `domain/workflow/model` | `TransicaoWorkflow` |
| [x] | `domain/workflow/model` | `WorkflowDefinition` |
| [x] | `domain/workflow/repository` | `AprovacaoRepository` |
| [x] | `domain/workflow/repository` | `WorkflowDefinitionRepository` |
| [x] | `domain/workflow/repository` | `WorkflowInstanceRepository` |

---

## eventbus

| Status | Pacote | Classe |
|--------|--------|--------|
| [x] | `eventbus` | `EventBus` |
| [x] | `eventbus` | `EventBusStatistics` |
| [x] | `eventbus` | `EventHandler` |
| [x] | `eventbus` | `EventHandlerRegistry` |
| [x] | `eventbus/config` | `EventBusConfiguration` |
| [x] | `eventbus/config` | `EventBusHealthIndicator` |
| [x] | `eventbus/config` | `EventBusMetrics` |
| [x] | `eventbus/config` | `KafkaEventBusConfiguration` |
| [x] | `eventbus/impl` | `KafkaEventBus` |
| [x] | `eventbus/impl` | `SimpleEventBus` |
| [x] | `eventbus/example` | `TestEvent` |
| [x] | `eventbus/example` | `TestEventHandler` |

---

## eventstore

| Status | Pacote | Classe |
|--------|--------|--------|
| [x] | `eventstore` | `EventStore` |
| [x] | `eventstore/config` | `EventStoreConfiguration` |
| [x] | `eventstore/config` | `EventStoreHealthIndicator` |
| [x] | `eventstore/config` | `EventStoreMetrics` |
| [x] | `eventstore/entity` | `EventStoreEntry` |
| [x] | `eventstore/exception` | `ConcurrencyException` |
| [x] | `eventstore/impl` | `PostgreSQLEventStore` |
| [x] | `eventstore/model` | `DomainEvent` |
| [x] | `eventstore/model` | `EventMetadata` |
| [x] | `eventstore/partition` | `PartitionManager` |
| [x] | `eventstore/partition` | `PartitionStatistics` |
| [x] | `eventstore/repository` | `EventStoreRepository` |
| [x] | `eventstore/scheduler` | `EventStoreMaintenanceScheduler` |
| [x] | `eventstore/serialization` | `EventSerializer` |
| [x] | `eventstore/serialization` | `JsonEventSerializer` |
| [x] | `eventstore/serialization` | `SerializationResult` |
| [x] | `eventstore/archive` | `ArchiveMetadata` |
| [x] | `eventstore/archive` | `ArchiveResult` |
| [x] | `eventstore/archive` | `ArchiveStatistics` |
| [x] | `eventstore/archive` | `ArchiveStorageService` |
| [x] | `eventstore/archive` | `ArchiveSummary` |
| [x] | `eventstore/archive` | `EventArchiver` |
| [x] | `eventstore/archive/impl` | `FileSystemArchiveStorage` |
| [x] | `eventstore/replay` | `EventReplayer` |
| [x] | `eventstore/replay` | `ReplayConfiguration` |
| [x] | `eventstore/replay` | `ReplayDetailedReport` |
| [x] | `eventstore/replay` | `ReplayError` |
| [x] | `eventstore/replay` | `ReplayFilter` |
| [x] | `eventstore/replay` | `ReplayStatistics` |
| [x] | `eventstore/replay/config` | `ReplayConfiguration` |
| [x] | `eventstore/replay/config` | `ReplayHealthIndicator` |
| [x] | `eventstore/replay/config` | `ReplayMetrics` |
| [x] | `eventstore/replay/example` | `ReplayExampleService` |
| [x] | `eventstore/replay/impl` | `DefaultEventReplayer` |

---

## projection

| Status | Pacote | Classe |
|--------|--------|--------|
| [x] | `projection` | `AbstractProjectionHandler` |
| [x] | `projection` | `ProjectionEventProcessor` |
| [x] | `projection` | `ProjectionHandler` |
| [x] | `projection` | `ProjectionRegistry` |
| [x] | `projection/config` | `ProjectionConfiguration` |
| [x] | `projection/config` | `ProjectionRebuildConfiguration` |
| [x] | `projection/consistency` | `ConsistencyIssue` |
| [x] | `projection/consistency` | `ConsistencyReport` |
| [x] | `projection/consistency` | `IssueSeverity` |
| [x] | `projection/consistency` | `IssueType` |
| [x] | `projection/consistency` | `ProjectionConsistencyChecker` |
| [x] | `projection/rebuild` | `ProjectionRebuilder` |
| [x] | `projection/rebuild` | `RebuildResult` |
| [x] | `projection/rebuild` | `RebuildStatus` |
| [x] | `projection/rebuild` | `RebuildType` |
| [x] | `projection/scheduler` | `ProjectionMaintenanceScheduler` |
| [x] | `projection/tracking` | `ProjectionStatus` |
| [x] | `projection/tracking` | `ProjectionTracker` |
| [x] | `projection/tracking` | `ProjectionTrackerRepository` |
| [x] | `projection/versioning` | `ProjectionVersion` |
| [x] | `projection/versioning` | `ProjectionVersionRepository` |

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
