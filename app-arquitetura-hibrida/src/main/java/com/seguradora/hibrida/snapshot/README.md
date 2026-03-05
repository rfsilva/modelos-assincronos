# 📸 Sistema de Snapshots Automático

## 🎯 Visão Geral

O Sistema de Snapshots Automático é uma implementação completa para otimização de reconstrução de aggregates em arquiteturas Event Sourcing. Ele cria automaticamente snapshots dos aggregates em intervalos configuráveis, aplicando compressão inteligente e mantendo limpeza automática.

## ✨ Características Principais

- **🔄 Criação Automática**: Snapshots criados automaticamente a cada N eventos
- **🗜️ Compressão Inteligente**: GZIP com threshold configurável e validação de eficiência
- **🧹 Limpeza Automática**: Manutenção automática dos snapshots mais recentes
- **⚡ Operações Assíncronas**: Não bloqueia operações principais
- **📊 Métricas Completas**: Monitoramento detalhado via Prometheus
- **🏥 Health Checks**: Verificação contínua de saúde do sistema
- **🔧 Configuração Flexível**: Todas as configurações via properties

## 🏗️ Arquitetura

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Application   │───▶│  SnapshotStore   │───▶│   PostgreSQL    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │ SnapshotSerializer│
                       └──────────────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │  GZIP Compression │
                       └──────────────────┘
```

## 🚀 Início Rápido

### 1. Configuração Básica

```yaml
snapshot:
  snapshot-threshold: 50              # Criar snapshot a cada 50 eventos
  max-snapshots-per-aggregate: 5      # Manter últimos 5 snapshots
  compression-threshold: 1024         # Comprimir se > 1KB
  auto-cleanup-enabled: true          # Limpeza automática
  async-snapshot-creation: true       # Criação assíncrona
```

### 2. Uso Básico

```java
@Autowired
private SnapshotStore snapshotStore;

// Verificar se deve criar snapshot
boolean shouldCreate = snapshotStore.shouldCreateSnapshot("aggregate-123", 50);

// Criar snapshot
if (shouldCreate) {
    AggregateSnapshot snapshot = new AggregateSnapshot(
        "aggregate-123", 
        "MyAggregate", 
        50, 
        aggregateData
    );
    snapshotStore.saveSnapshot(snapshot);
}

// Recuperar snapshot mais recente
Optional<AggregateSnapshot> latest = snapshotStore.getLatestSnapshot("aggregate-123");
```

### 3. APIs REST

```bash
# Obter snapshot mais recente
GET /api/snapshots/aggregates/{aggregateId}/latest

# Obter estatísticas
GET /api/snapshots/statistics

# Verificar saúde
GET /api/snapshots/health

# Limpeza manual
DELETE /api/snapshots/aggregates/{aggregateId}/cleanup?keepCount=5
```

## 📋 Configurações Detalhadas

### Configurações Principais

| Propriedade | Padrão | Descrição |
|-------------|--------|-----------|
| `snapshot-threshold` | 50 | Eventos necessários para criar snapshot |
| `max-snapshots-per-aggregate` | 5 | Snapshots mantidos por aggregate |
| `compression-threshold` | 1024 | Tamanho mínimo para compressão (bytes) |
| `compression-algorithm` | GZIP | Algoritmo de compressão |

### Configurações de Comportamento

| Propriedade | Padrão | Descrição |
|-------------|--------|-----------|
| `compression-enabled` | true | Habilita compressão automática |
| `auto-cleanup-enabled` | true | Habilita limpeza automática |
| `async-snapshot-creation` | true | Criação assíncrona |
| `integrity-validation-enabled` | true | Validação com hash SHA-256 |

### Configurações de Tempo

| Propriedade | Padrão | Descrição |
|-------------|--------|-----------|
| `cleanup-interval-hours` | 24 | Intervalo de limpeza automática |
| `operation-timeout-seconds` | 30 | Timeout para operações |
| `retention-days` | 365 | Dias para manter snapshots |

### Pool de Threads Assíncronas

| Propriedade | Padrão | Descrição |
|-------------|--------|-----------|
| `async-thread-pool-size` | 5 | Tamanho do pool |
| `async-queue-capacity` | 100 | Capacidade da fila |
| `async-thread-name-prefix` | "snapshot-" | Prefixo dos nomes |

### Monitoramento

| Propriedade | Padrão | Descrição |
|-------------|--------|-----------|
| `metrics-enabled` | true | Habilita métricas |
| `health-check-enabled` | true | Habilita health checks |
| `health-check-interval-seconds` | 60 | Intervalo de verificação |
| `max-consecutive-failures` | 3 | Falhas antes de unhealthy |

## 📊 Métricas Disponíveis

### Contadores
- `snapshots_created_total` - Snapshots criados
- `snapshots_failed_total` - Falhas na criação
- `snapshots_loaded_total` - Snapshots carregados
- `snapshots_deleted_total` - Snapshots deletados

### Timers
- `snapshots_creation_time` - Tempo de criação
- `snapshots_load_time` - Tempo de carregamento
- `snapshots_compression_time` - Tempo de compressão

### Gauges
- `snapshots_total` - Total no sistema
- `snapshots_storage_used_bytes` - Armazenamento usado
- `snapshots_storage_saved_bytes` - Espaço economizado
- `snapshots_compression_ratio` - Taxa de compressão
- `snapshots_storage_efficiency` - Eficiência de armazenamento

## 🏥 Health Checks

O sistema fornece health checks detalhados:

```json
{
  "status": "UP",
  "responseTimeMs": 45,
  "totalSnapshots": 1250,
  "compressionRatio": "65%",
  "compressionEffective": true,
  "snapshotsLast24Hours": 25,
  "spaceSavedMB": 128,
  "performanceOk": true
}
```

## 🔧 Operações de Manutenção

### Limpeza Manual

```bash
# Limpar snapshots de um aggregate específico
DELETE /api/snapshots/aggregates/my-aggregate-123/cleanup?keepCount=3

# Limpeza global
DELETE /api/snapshots/cleanup?keepCount=5
```

### Verificação de Necessidade

```bash
# Verificar se deve criar snapshot
GET /api/snapshots/aggregates/my-aggregate-123/should-create?currentVersion=75
```

### Estatísticas Detalhadas

```bash
# Estatísticas por aggregate
GET /api/snapshots/aggregates/my-aggregate-123/statistics

# Estatísticas globais
GET /api/snapshots/statistics

# Métricas de eficiência
GET /api/snapshots/aggregates/my-aggregate-123/efficiency?period=7
```

## 📈 Monitoramento e Alertas

### Dashboards Recomendados

1. **Performance**
   - Tempo médio de criação
   - Tempo médio de carregamento
   - Taxa de compressão

2. **Utilização**
   - Total de snapshots
   - Crescimento por dia
   - Espaço usado/economizado

3. **Saúde**
   - Taxa de falhas
   - Tempo de resposta
   - Status dos health checks

### Alertas Sugeridos

```yaml
# Prometheus AlertManager
- alert: SnapshotCreationSlow
  expr: rate(snapshots_creation_time_sum[5m]) / rate(snapshots_creation_time_count[5m]) > 5
  for: 2m
  labels:
    severity: warning
  annotations:
    summary: "Snapshot creation is slow"

- alert: SnapshotFailureRate
  expr: rate(snapshots_failed_total[5m]) > 0.1
  for: 1m
  labels:
    severity: critical
  annotations:
    summary: "High snapshot failure rate"
```

## 🧪 Testes

### Executar Testes

```bash
# Testes unitários
./mvnw test -Dtest=*SnapshotTest

# Testes de integração
./mvnw test -Dtest=*SnapshotIntegrationTest

# Todos os testes de snapshot
./mvnw test -Dtest=**/*Snapshot*Test
```

### Testes de Performance

```java
@Test
void performanceTest() {
    // Criar 1000 snapshots
    for (int i = 0; i < 1000; i++) {
        AggregateSnapshot snapshot = createTestSnapshot(i);
        snapshotStore.saveSnapshot(snapshot);
    }
    
    // Verificar tempo de recuperação
    long start = System.currentTimeMillis();
    Optional<AggregateSnapshot> latest = snapshotStore.getLatestSnapshot("test-aggregate");
    long duration = System.currentTimeMillis() - start;
    
    assertThat(duration).isLessThan(100); // < 100ms
}
```

## 🔍 Troubleshooting

### Problemas Comuns

#### 1. Snapshots não sendo criados
```bash
# Verificar configuração
GET /api/snapshots/aggregates/my-aggregate/should-create?currentVersion=50

# Verificar logs
tail -f logs/arquitetura-hibrida.log | grep "snapshot"
```

#### 2. Performance lenta
```bash
# Verificar métricas
GET /api/snapshots/metrics

# Verificar health
GET /api/snapshots/health
```

#### 3. Falhas de compressão
```bash
# Verificar logs de compressão
grep "compression" logs/arquitetura-hibrida.log

# Ajustar threshold
snapshot.compression-threshold: 2048
```

### Logs Importantes

```bash
# Criação de snapshots
grep "Snapshot saved successfully" logs/arquitetura-hibrida.log

# Limpeza automática
grep "Cleanup completed" logs/arquitetura-hibrida.log

# Falhas
grep "Failed to" logs/arquitetura-hibrida.log | grep "snapshot"
```

## 🔄 Integração com Event Store

O sistema está preparado para integração completa com o Event Store:

```java
// Reconstrução otimizada (futuro)
public MyAggregate reconstructAggregate(String aggregateId) {
    // 1. Buscar snapshot mais recente
    Optional<AggregateSnapshot> snapshot = snapshotStore.getLatestSnapshot(aggregateId);
    
    if (snapshot.isPresent()) {
        // 2. Reconstruir a partir do snapshot
        MyAggregate aggregate = deserializeFromSnapshot(snapshot.get());
        
        // 3. Aplicar eventos incrementais
        List<DomainEvent> events = eventStore.loadEvents(aggregateId, snapshot.get().getVersion() + 1);
        aggregate.loadFromHistory(events);
        
        return aggregate;
    } else {
        // 4. Fallback: reconstrução completa
        List<DomainEvent> allEvents = eventStore.loadEvents(aggregateId);
        return MyAggregate.fromHistory(allEvents);
    }
}
```

## 📚 Referências

- [Event Sourcing Pattern](https://martinfowler.com/eaaDev/EventSourcing.html)
- [CQRS Pattern](https://docs.microsoft.com/en-us/azure/architecture/patterns/cqrs)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Metrics](https://micrometer.io/docs)
- [PostgreSQL JSONB](https://www.postgresql.org/docs/current/datatype-json.html)

---

**Desenvolvido por:** Principal Java Architect  
**Versão:** 1.0.0  
**Data:** 2024-12-19