# 🔄 Sistema de Replay de Eventos

## 📋 Visão Geral

O Sistema de Replay de Eventos permite reprocessar eventos históricos do Event Store de forma controlada e auditável. É uma ferramenta essencial para manutenção, correção de inconsistências e reprocessamento de projeções.

## 🎯 Funcionalidades Principais

### ✅ Tipos de Replay Suportados
- **Por Período**: Reprocessa eventos em um intervalo de tempo específico
- **Por Tipo de Evento**: Reprocessa apenas eventos de tipos específicos
- **Por Aggregate**: Reprocessa todos os eventos de um aggregate específico
- **Com Filtros Avançados**: Combinação de múltiplos critérios com operadores lógicos

### ✅ Controles de Execução
- **Pausar/Retomar**: Controle total sobre a execução
- **Cancelar**: Interrupção segura de replays em andamento
- **Controle de Velocidade**: Throttling configurável (eventos/segundo)
- **Processamento em Lotes**: Otimização de performance

### ✅ Modo Simulação
- **Sem Efeitos Colaterais**: Validação prévia sem impacto
- **Relatórios de Impacto**: Análise detalhada do que seria processado
- **Recomendações**: Sugestões automáticas baseadas na análise

### ✅ Monitoramento Completo
- **Progresso em Tempo Real**: Acompanhamento detalhado da execução
- **Métricas Prometheus**: Integração com sistemas de monitoramento
- **Health Checks**: Verificação automática de saúde do sistema
- **APIs REST**: Interface completa para integração

## 🏗️ Arquitetura

```
EventReplayer (Interface)
    ↓
DefaultEventReplayer (Implementação)
    ↓
┌─────────────────┬─────────────────┬─────────────────┐
│   EventStore    │    EventBus     │ HandlerRegistry │
│  (Fonte dos     │  (Publicação    │   (Handlers     │
│   eventos)      │   de eventos)   │   disponíveis)  │
└─────────────────┴─────────────────┴─────────────────┘
```

## 🚀 Como Usar

### 1. Replay por Período

```java
@Autowired
private EventReplayer eventReplayer;

// Configuração do replay
ReplayConfiguration config = ReplayConfiguration.builder()
    .name("Replay Últimas 24h")
    .fromTimestamp(Instant.now().minus(24, ChronoUnit.HOURS))
    .toTimestamp(Instant.now())
    .eventsPerSecond(500) // Controle de velocidade
    .batchSize(100)
    .generateDetailedReport(true)
    .build();

// Execução assíncrona
CompletableFuture<ReplayResult> future = eventReplayer.replayByPeriod(config);

// Tratamento do resultado
future.whenComplete((result, throwable) -> {
    if (throwable != null) {
        log.error("Erro no replay", throwable);
    } else {
        log.info("Replay concluído: {}", result.getSummary());
    }
});
```

### 2. Replay por Tipo de Evento

```java
// Replay de eventos específicos
CompletableFuture<ReplayResult> future = eventReplayer.replayByEventType(
    "SinistroCriado", 
    startTime, 
    endTime, 
    config
);
```

### 3. Replay com Filtros Avançados

```java
// Filtro complexo
ReplayFilter filter = ReplayFilter.builder()
    .eventTypes(List.of("SinistroCriado", "SinistroAtualizado"))
    .metadataFilters(Map.of("valorEstimado", ">10000"))
    .operator(ReplayFilter.LogicalOperator.AND)
    .build();

CompletableFuture<ReplayResult> future = eventReplayer.replayWithFilter(filter, config);
```

### 4. Modo Simulação

```java
// Simulação sem efeitos colaterais
ReplayConfiguration simulationConfig = ReplayConfiguration.forSimulation(baseConfig);
CompletableFuture<ReplayResult> future = eventReplayer.simulateReplay(simulationConfig);
```

### 5. Controle de Execução

```java
UUID replayId = config.getReplayId();

// Pausar
boolean paused = eventReplayer.pauseReplay(replayId);

// Retomar
boolean resumed = eventReplayer.resumeReplay(replayId);

// Cancelar
boolean cancelled = eventReplayer.cancelReplay(replayId);
```

### 6. Monitoramento

```java
// Progresso específico
ReplayProgress progress = eventReplayer.getProgress(replayId);
System.out.println("Progresso: " + progress.getProgressPercentage() + "%");

// Replays ativos
List<ReplayProgress> activeReplays = eventReplayer.getActiveReplays();

// Estatísticas gerais
ReplayStatistics stats = eventReplayer.getStatistics();
System.out.println("Taxa de sucesso: " + stats.getOverallSuccessRate() + "%");
```

## 🔧 Configuração

### application.yml

```yaml
eventstore:
  replay:
    enabled: true
    defaults:
      batch-size: 100
      batch-timeout-seconds: 30
      max-retries: 3
      retry-delay-ms: 1000
      stop-on-error: false
      generate-detailed-report: false
      progress-notification-interval: 1000
    performance:
      max-concurrent-replays: 5
      thread-pool-size: 10
      operation-timeout-seconds: 3600
      enable-event-cache: true
      event-cache-ttl-seconds: 300
    monitoring:
      enable-detailed-metrics: true
      enable-health-checks: true
      max-history-size: 1000
      log-level: INFO
```

### Configurações por Ambiente

```yaml
# Desenvolvimento
spring:
  profiles: development
eventstore:
  replay:
    defaults:
      batch-size: 50
      generate-detailed-report: true
    monitoring:
      log-level: DEBUG

# Produção
spring:
  profiles: production
eventstore:
  replay:
    defaults:
      batch-size: 200
      batch-timeout-seconds: 60
    performance:
      max-concurrent-replays: 10
      thread-pool-size: 20
```

## 🌐 APIs REST

### Execução de Replays

```bash
# Replay por período
POST /api/v1/replay/period
{
  "from": "2024-01-01T00:00:00",
  "to": "2024-01-31T23:59:59",
  "name": "Replay Janeiro 2024",
  "simulationMode": false,
  "eventsPerSecond": 100
}

# Replay por tipo de evento
POST /api/v1/replay/event-type/SinistroCriado
{
  "from": "2024-01-01T00:00:00",
  "to": "2024-01-31T23:59:59",
  "simulationMode": false
}

# Replay por aggregate
POST /api/v1/replay/aggregate/sinistro-123
{
  "fromVersion": 10,
  "simulationMode": false
}
```

### Controle de Execução

```bash
# Pausar replay
POST /api/v1/replay/{replayId}/pause

# Retomar replay
POST /api/v1/replay/{replayId}/resume

# Cancelar replay
POST /api/v1/replay/{replayId}/cancel
```

### Monitoramento

```bash
# Progresso específico
GET /api/v1/replay/{replayId}/progress

# Replays ativos
GET /api/v1/replay/active

# Histórico
GET /api/v1/replay/history?limit=50

# Estatísticas
GET /api/v1/replay/statistics

# Health check
GET /api/v1/replay/health

# Métricas
GET /api/v1/replay/metrics
```

## 📊 Métricas Prometheus

```prometheus
# Número de replays ativos
replay_active_count

# Taxa de sucesso dos replays (%)
replay_success_rate

# Taxa de erro dos replays (%)
replay_error_rate

# Throughput médio (eventos/segundo)
replay_average_throughput

# Tempo de execução de replays
replay_execution_time

# Tempo de processamento de eventos
replay_event_processing_time
```

## 🔍 Troubleshooting

### Problemas Comuns

#### 1. Replay Lento
```yaml
# Aumentar throughput
eventstore:
  replay:
    defaults:
      batch-size: 200
    performance:
      thread-pool-size: 20
```

#### 2. Muitos Erros
```yaml
# Configurar retry
eventstore:
  replay:
    defaults:
      max-retries: 5
      retry-delay-ms: 2000
      stop-on-error: false
```

#### 3. Memória Insuficiente
```yaml
# Reduzir lote e cache
eventstore:
  replay:
    defaults:
      batch-size: 50
    performance:
      enable-event-cache: false
```

### Logs Úteis

```bash
# Habilitar logs detalhados
logging:
  level:
    com.seguradora.hibrida.eventstore.replay: DEBUG

# Verificar logs específicos
grep "Replay" logs/arquitetura-hibrida.log
```

## 🚨 Considerações de Segurança

### Validações Implementadas
- ✅ Validação de configurações
- ✅ Controle de estados
- ✅ Timeout de operações
- ✅ Limite de replays simultâneos

### Boas Práticas
- 🔒 Sempre usar modo simulação primeiro
- 📊 Monitorar métricas durante execução
- ⏰ Configurar timeouts adequados
- 🔄 Implementar retry para falhas temporárias

## 🔮 Roadmap

### Próximas Funcionalidades
- [ ] **Persistência de Estado**: Salvar progresso em banco
- [ ] **Distribuição**: Suporte a replays distribuídos
- [ ] **UI Web**: Interface gráfica para gerenciamento
- [ ] **Agendamento**: Sistema de agendamento automático
- [ ] **Notificações**: Alertas por email/Slack

### Melhorias Planejadas
- [ ] **Filtros Avançados**: Mais tipos de filtros
- [ ] **Performance**: Otimizações de throughput
- [ ] **Observabilidade**: Mais métricas e dashboards
- [ ] **Integração**: APIs para sistemas externos

## 📚 Documentação Adicional

- [JavaDoc Completo](./javadoc/index.html)
- [OpenAPI/Swagger](http://localhost:8083/api/v1/swagger-ui.html)
- [Métricas Prometheus](http://localhost:8083/api/v1/actuator/prometheus)
- [Health Checks](http://localhost:8083/api/v1/actuator/health)

---

**Desenvolvido por:** Principal Java Architect  
**Versão:** 1.0.0  
**Data:** 2024-12-19