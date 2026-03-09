# 📊 MONITORAMENTO E OBSERVABILIDADE - PARTE 2
## Configuração de Prometheus e Grafana

### 🎯 **OBJETIVOS DESTA PARTE**
- Configurar Prometheus para coleta de métricas
- Implementar Grafana para visualização
- Criar dashboards específicos para a arquitetura híbrida
- Configurar service discovery e targets

---

## 🔥 **CONFIGURAÇÃO DO PROMETHEUS**

### **📋 Visão Geral do Prometheus**

Prometheus é um sistema de monitoramento e alerta open-source que:

#### **Características Principais:**
- ✅ **Pull-based**: Coleta métricas fazendo requisições HTTP
- ✅ **Time Series**: Armazena dados como séries temporais
- ✅ **PromQL**: Linguagem de consulta poderosa
- ✅ **Service Discovery**: Descoberta automática de targets
- ✅ **Alerting**: Sistema integrado de alertas

### **🔧 Configuração do Prometheus**

#### **prometheus.yml - Configuração Principal:**
```yaml
# Configuração global
global:
  scrape_interval: 15s # Intervalo padrão de coleta
  evaluation_interval: 15s # Intervalo de avaliação de regras
  external_labels:
    monitor: 'arquitetura-hibrida-monitor'
    environment: 'development'

# Configuração de regras de alerta
rule_files:
  - "alert_rules.yml"
  - "recording_rules.yml"

# Configuração do AlertManager
alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

# Configuração de targets para coleta
scrape_configs:
  # Prometheus self-monitoring
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
    scrape_interval: 5s
    metrics_path: /metrics

  # Aplicação Spring Boot
  - job_name: 'app-arquitetura-hibrida'
    static_configs:
      - targets: ['app:8080']
    scrape_interval: 10s
    metrics_path: /actuator/prometheus
    scrape_timeout: 5s
    honor_labels: true
    params:
      format: ['prometheus']

  # Event Store Database
  - job_name: 'eventstore-postgres'
    static_configs:
      - targets: ['eventstore-db:5432']
    scrape_interval: 30s
    metrics_path: /metrics

  # Projections Database
  - job_name: 'projections-postgres'
    static_configs:
      - targets: ['projections-db:5432']
    scrape_interval: 30s
    metrics_path: /metrics

  # Kafka (se usando Kafka Event Bus)
  - job_name: 'kafka'
    static_configs:
      - targets: ['kafka:9092']
    scrape_interval: 30s

  # Redis (se usando cache)
  - job_name: 'redis'
    static_configs:
      - targets: ['redis:6379']
    scrape_interval: 30s

# Configuração de retenção
storage:
  tsdb:
    retention.time: 30d
    retention.size: 10GB
```

#### **alert_rules.yml - Regras de Alerta:**
```yaml
groups:
  - name: arquitetura-hibrida-alerts
    rules:
      # Alerta para alta taxa de erro em comandos
      - alert: HighCommandErrorRate
        expr: rate(commands_failed_total[5m]) / rate(commands_processed_total[5m]) > 0.1
        for: 2m
        labels:
          severity: warning
          component: command-bus
        annotations:
          summary: "Alta taxa de erro em comandos"
          description: "Taxa de erro de comandos está acima de 10% por mais de 2 minutos"

      # Alerta para lag alto entre command e query side
      - alert: HighCQRSLag
        expr: cqrs_lag_seconds > 60
        for: 5m
        labels:
          severity: critical
          component: cqrs
        annotations:
          summary: "Lag alto entre Command e Query side"
          description: "Lag entre command e query side está acima de 60 segundos"

      # Alerta para projeções com erro
      - alert: ProjectionErrors
        expr: projections_error_rate > 0.05
        for: 1m
        labels:
          severity: warning
          component: projections
        annotations:
          summary: "Projeções com alta taxa de erro"
          description: "Taxa de erro das projeções está acima de 5%"

      # Alerta para Event Store indisponível
      - alert: EventStoreDown
        expr: up{job="app-arquitetura-hibrida"} == 0
        for: 1m
        labels:
          severity: critical
          component: eventstore
        annotations:
          summary: "Event Store indisponível"
          description: "Event Store não está respondendo"

      # Alerta para alta latência de consultas
      - alert: HighQueryLatency
        expr: histogram_quantile(0.95, rate(queries_execution_time_seconds_bucket[5m])) > 2
        for: 3m
        labels:
          severity: warning
          component: query-side
        annotations:
          summary: "Alta latência em consultas"
          description: "95% das consultas estão levando mais de 2 segundos"

      # Alerta para uso alto de memória
      - alert: HighMemoryUsage
        expr: (jvm_memory_used_bytes / jvm_memory_max_bytes) > 0.8
        for: 5m
        labels:
          severity: warning
          component: jvm
        annotations:
          summary: "Alto uso de memória JVM"
          description: "Uso de memória JVM está acima de 80%"

      # Alerta para muitas conexões de banco
      - alert: HighDatabaseConnections
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.8
        for: 2m
        labels:
          severity: warning
          component: database
        annotations:
          summary: "Muitas conexões ativas no banco"
          description: "Mais de 80% das conexões do pool estão ativas"
```

#### **recording_rules.yml - Regras de Gravação:**
```yaml
groups:
  - name: arquitetura-hibrida-recording-rules
    interval: 30s
    rules:
      # Taxa de comandos por minuto
      - record: commands:rate5m
        expr: rate(commands_processed_total[5m])

      # Taxa de erro de comandos
      - record: commands:error_rate5m
        expr: rate(commands_failed_total[5m]) / rate(commands_processed_total[5m])

      # Latência média de comandos
      - record: commands:latency_avg5m
        expr: rate(commands_execution_time_seconds_sum[5m]) / rate(commands_execution_time_seconds_count[5m])

      # Taxa de consultas por minuto
      - record: queries:rate5m
        expr: rate(queries_executed_total[5m])

      # Taxa de cache hit
      - record: cache:hit_rate5m
        expr: rate(cache_hits_total[5m]) / (rate(cache_hits_total[5m]) + rate(cache_misses_total[5m]))

      # Throughput de eventos
      - record: events:throughput5m
        expr: rate(eventbus_events_published_total[5m])

      # Taxa de erro de eventos
      - record: events:error_rate5m
        expr: rate(eventbus_events_failed_total[5m]) / rate(eventbus_events_published_total[5m])
```

---

## 📊 **CONFIGURAÇÃO DO GRAFANA**

### **🔧 Configuração Inicial do Grafana**

#### **grafana.ini - Configuração Principal:**
```ini
[server]
http_port = 3000
domain = localhost
root_url = http://localhost:3000

[database]
type = sqlite3
path = grafana.db

[security]
admin_user = admin
admin_password = admin123
secret_key = SW2YcwTIb9zpOOhoPsMm

[auth.anonymous]
enabled = false

[dashboards]
default_home_dashboard_path = /var/lib/grafana/dashboards/overview.json

[provisioning]
dashboards = /etc/grafana/provisioning/dashboards
datasources = /etc/grafana/provisioning/datasources

[alerting]
enabled = true
execute_alerts = true

[smtp]
enabled = false
host = localhost:587
user = 
password = 
from_address = admin@grafana.localhost
```

#### **datasources.yml - Configuração de Data Sources:**
```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
    jsonData:
      timeInterval: "5s"
      queryTimeout: "60s"
      httpMethod: "POST"
    secureJsonData: {}

  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100
    editable: true
    jsonData:
      maxLines: 1000
      timeout: 60
      derivedFields:
        - datasourceUid: "prometheus_uid"
          matcherRegex: "traceID=(\\w+)"
          name: "TraceID"
          url: "http://jaeger:16686/trace/$${__value.raw}"

  - name: Jaeger
    type: jaeger
    access: proxy
    url: http://jaeger:16686
    editable: true
```

#### **dashboards.yml - Configuração de Dashboards:**
```yaml
apiVersion: 1

providers:
  - name: 'default'
    orgId: 1
    folder: ''
    type: file
    disableDeletion: false
    updateIntervalSeconds: 10
    allowUiUpdates: true
    options:
      path: /var/lib/grafana/dashboards

  - name: 'arquitetura-hibrida'
    orgId: 1
    folder: 'Arquitetura Híbrida'
    type: file
    disableDeletion: false
    updateIntervalSeconds: 10
    allowUiUpdates: true
    options:
      path: /var/lib/grafana/dashboards/arquitetura-hibrida
```

---

## 📈 **DASHBOARDS ESPECÍFICOS**

### **🎯 Dashboard Overview - Visão Geral**

#### **overview-dashboard.json:**
```json
{
  "dashboard": {
    "id": null,
    "title": "Arquitetura Híbrida - Overview",
    "tags": ["arquitetura-hibrida", "overview"],
    "timezone": "browser",
    "panels": [
      {
        "id": 1,
        "title": "Status Geral do Sistema",
        "type": "stat",
        "targets": [
          {
            "expr": "up{job=\"app-arquitetura-hibrida\"}",
            "legendFormat": "Sistema"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "color": {
              "mode": "thresholds"
            },
            "thresholds": {
              "steps": [
                {"color": "red", "value": 0},
                {"color": "green", "value": 1}
              ]
            },
            "mappings": [
              {"options": {"0": {"text": "DOWN"}}, "type": "value"},
              {"options": {"1": {"text": "UP"}}, "type": "value"}
            ]
          }
        },
        "gridPos": {"h": 4, "w": 6, "x": 0, "y": 0}
      },
      {
        "id": 2,
        "title": "Comandos por Minuto",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(commands_processed_total[5m]) * 60",
            "legendFormat": "Comandos/min"
          }
        ],
        "yAxes": [
          {"label": "Comandos/min", "min": 0}
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 4}
      },
      {
        "id": 3,
        "title": "Taxa de Erro de Comandos",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(commands_failed_total[5m]) / rate(commands_processed_total[5m]) * 100",
            "legendFormat": "Taxa de Erro %"
          }
        ],
        "yAxes": [
          {"label": "Erro %", "min": 0, "max": 100}
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 4}
      },
      {
        "id": 4,
        "title": "Lag CQRS",
        "type": "graph",
        "targets": [
          {
            "expr": "cqrs_lag_seconds",
            "legendFormat": "Lag (segundos)"
          }
        ],
        "yAxes": [
          {"label": "Segundos", "min": 0}
        ],
        "alert": {
          "conditions": [
            {
              "evaluator": {"params": [60], "type": "gt"},
              "operator": {"type": "and"},
              "query": {"params": ["A", "5m", "now"]},
              "reducer": {"params": [], "type": "avg"},
              "type": "query"
            }
          ],
          "executionErrorState": "alerting",
          "for": "5m",
          "frequency": "10s",
          "handler": 1,
          "name": "Lag CQRS Alto",
          "noDataState": "no_data",
          "notifications": []
        },
        "gridPos": {"h": 8, "w": 24, "x": 0, "y": 12}
      }
    ],
    "time": {"from": "now-1h", "to": "now"},
    "refresh": "5s"
  }
}
```

### **🎯 Dashboard Command Side**

#### **command-side-dashboard.json:**
```json
{
  "dashboard": {
    "id": null,
    "title": "Command Side - Monitoramento",
    "tags": ["arquitetura-hibrida", "command-side"],
    "panels": [
      {
        "id": 1,
        "title": "Comandos por Tipo",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(commands_processed_total[5m]) by (type)",
            "legendFormat": "{{type}}"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 0}
      },
      {
        "id": 2,
        "title": "Latência de Comandos (P95)",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(commands_execution_time_seconds_bucket[5m])) by (type)",
            "legendFormat": "P95 - {{type}}"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 0}
      },
      {
        "id": 3,
        "title": "Event Store - Eventos Salvos",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(eventstore_events_saved_total[5m])",
            "legendFormat": "Eventos/s"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 8}
      },
      {
        "id": 4,
        "title": "Conexões de Banco (Write)",
        "type": "graph",
        "targets": [
          {
            "expr": "hikaricp_connections_active{pool=\"writeDataSource\"}",
            "legendFormat": "Ativas"
          },
          {
            "expr": "hikaricp_connections_idle{pool=\"writeDataSource\"}",
            "legendFormat": "Idle"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 8}
      }
    ]
  }
}
```

### **🎯 Dashboard Query Side**

#### **query-side-dashboard.json:**
```json
{
  "dashboard": {
    "id": null,
    "title": "Query Side - Monitoramento",
    "tags": ["arquitetura-hibrida", "query-side"],
    "panels": [
      {
        "id": 1,
        "title": "Consultas por Tipo",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(queries_executed_total[5m]) by (type)",
            "legendFormat": "{{type}}"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 0}
      },
      {
        "id": 2,
        "title": "Cache Hit Rate",
        "type": "stat",
        "targets": [
          {
            "expr": "rate(cache_hits_total[5m]) / (rate(cache_hits_total[5m]) + rate(cache_misses_total[5m])) * 100",
            "legendFormat": "Hit Rate %"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "unit": "percent",
            "thresholds": {
              "steps": [
                {"color": "red", "value": 0},
                {"color": "yellow", "value": 70},
                {"color": "green", "value": 90}
              ]
            }
          }
        },
        "gridPos": {"h": 4, "w": 6, "x": 12, "y": 0}
      },
      {
        "id": 3,
        "title": "Status das Projeções",
        "type": "table",
        "targets": [
          {
            "expr": "projections_active_count",
            "legendFormat": "Ativas",
            "format": "table"
          },
          {
            "expr": "projections_error_count",
            "legendFormat": "Com Erro",
            "format": "table"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 8}
      },
      {
        "id": 4,
        "title": "Conexões de Banco (Read)",
        "type": "graph",
        "targets": [
          {
            "expr": "hikaricp_connections_active{pool=\"readDataSource\"}",
            "legendFormat": "Ativas"
          },
          {
            "expr": "hikaricp_connections_idle{pool=\"readDataSource\"}",
            "legendFormat": "Idle"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 8}
      }
    ]
  }
}
```

---

## 🐳 **DOCKER COMPOSE PARA MONITORAMENTO**

### **📝 docker-compose.monitoring.yml:**
```yaml
version: '3.8'

services:
  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - ./monitoring/prometheus/alert_rules.yml:/etc/prometheus/alert_rules.yml
      - ./monitoring/prometheus/recording_rules.yml:/etc/prometheus/recording_rules.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--storage.tsdb.retention.time=30d'
      - '--web.enable-lifecycle'
      - '--web.enable-admin-api'
    networks:
      - monitoring

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin123
      - GF_USERS_ALLOW_SIGN_UP=false
    volumes:
      - ./monitoring/grafana/grafana.ini:/etc/grafana/grafana.ini
      - ./monitoring/grafana/provisioning:/etc/grafana/provisioning
      - ./monitoring/grafana/dashboards:/var/lib/grafana/dashboards
      - grafana_data:/var/lib/grafana
    networks:
      - monitoring
    depends_on:
      - prometheus

  alertmanager:
    image: prom/alertmanager:latest
    container_name: alertmanager
    ports:
      - "9093:9093"
    volumes:
      - ./monitoring/alertmanager/alertmanager.yml:/etc/alertmanager/alertmanager.yml
      - alertmanager_data:/alertmanager
    command:
      - '--config.file=/etc/alertmanager/alertmanager.yml'
      - '--storage.path=/alertmanager'
      - '--web.external-url=http://localhost:9093'
    networks:
      - monitoring

  node-exporter:
    image: prom/node-exporter:latest
    container_name: node-exporter
    ports:
      - "9100:9100"
    volumes:
      - /proc:/host/proc:ro
      - /sys:/host/sys:ro
      - /:/rootfs:ro
    command:
      - '--path.procfs=/host/proc'
      - '--path.rootfs=/rootfs'
      - '--path.sysfs=/host/sys'
      - '--collector.filesystem.mount-points-exclude=^/(sys|proc|dev|host|etc)($$|/)'
    networks:
      - monitoring

volumes:
  prometheus_data:
  grafana_data:
  alertmanager_data:

networks:
  monitoring:
    driver: bridge
```

---

## 🔧 **CONFIGURAÇÃO DE SERVICE DISCOVERY**

### **📝 Service Discovery com Consul (Opcional)**

```yaml
# Adição ao prometheus.yml para service discovery
scrape_configs:
  - job_name: 'consul-services'
    consul_sd_configs:
      - server: 'consul:8500'
        services: ['app-arquitetura-hibrida']
    relabel_configs:
      - source_labels: [__meta_consul_service]
        target_label: job
      - source_labels: [__meta_consul_service_address]
        target_label: __address__
      - source_labels: [__meta_consul_service_port]
        target_label: __address__
        regex: (.*)
        replacement: ${1}:${__meta_consul_service_port}
```

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [Prometheus Configuration](https://prometheus.io/docs/prometheus/latest/configuration/configuration/)
- [Grafana Provisioning](https://grafana.com/docs/grafana/latest/administration/provisioning/)
- [PromQL Tutorial](https://prometheus.io/docs/prometheus/latest/querying/basics/)
- [Grafana Dashboard Best Practices](https://grafana.com/docs/grafana/latest/best-practices/)

### **📖 Próximas Partes:**
- **Parte 3**: Dashboards e Visualizações Avançadas
- **Parte 4**: Alertas e Notificações
- **Parte 5**: Troubleshooting e Debugging

---

**📝 Parte 2 de 5 - Configuração de Prometheus e Grafana**  
**⏱️ Tempo estimado**: 60 minutos  
**🎯 Próximo**: [Parte 3 - Dashboards e Visualizações](./11-monitoramento-parte-3.md)