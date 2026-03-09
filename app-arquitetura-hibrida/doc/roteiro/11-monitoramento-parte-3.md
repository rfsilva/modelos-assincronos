# 📊 MONITORAMENTO E OBSERVABILIDADE - PARTE 3
## Dashboards e Visualizações Avançadas

### 🎯 **OBJETIVOS DESTA PARTE**
- Criar dashboards avançados para diferentes personas
- Implementar visualizações específicas para Event Sourcing e CQRS
- Configurar alertas visuais e SLIs/SLOs
- Otimizar performance dos dashboards

---

## 🎨 **DASHBOARDS POR PERSONA**

### **👨‍💼 Dashboard Executivo - Business Overview**

Dashboard focado em métricas de negócio para gestores:

#### **business-overview-dashboard.json:**
```json
{
  "dashboard": {
    "id": null,
    "title": "Business Overview - Sinistros",
    "tags": ["business", "executive", "sinistros"],
    "timezone": "America/Sao_Paulo",
    "panels": [
      {
        "id": 1,
        "title": "Sinistros Criados Hoje",
        "type": "stat",
        "targets": [
          {
            "expr": "increase(commands_processed_total{type=\"CriarSinistroCommand\"}[1d])",
            "legendFormat": "Sinistros"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "color": {"mode": "palette-classic"},
            "unit": "short",
            "thresholds": {
              "steps": [
                {"color": "green", "value": 0},
                {"color": "yellow", "value": 50},
                {"color": "red", "value": 100}
              ]
            }
          }
        },
        "gridPos": {"h": 6, "w": 4, "x": 0, "y": 0}
      },
      {
        "id": 2,
        "title": "Tempo Médio de Processamento",
        "type": "stat",
        "targets": [
          {
            "expr": "avg(rate(commands_execution_time_seconds_sum[1h]) / rate(commands_execution_time_seconds_count[1h]))",
            "legendFormat": "Tempo Médio"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "unit": "s",
            "decimals": 2,
            "thresholds": {
              "steps": [
                {"color": "green", "value": 0},
                {"color": "yellow", "value": 2},
                {"color": "red", "value": 5}
              ]
            }
          }
        },
        "gridPos": {"h": 6, "w": 4, "x": 4, "y": 0}
      },
      {
        "id": 3,
        "title": "SLA - Disponibilidade do Sistema",
        "type": "stat",
        "targets": [
          {
            "expr": "avg_over_time(up{job=\"app-arquitetura-hibrida\"}[24h]) * 100",
            "legendFormat": "Disponibilidade %"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "unit": "percent",
            "decimals": 2,
            "thresholds": {
              "steps": [
                {"color": "red", "value": 0},
                {"color": "yellow", "value": 95},
                {"color": "green", "value": 99}
              ]
            }
          }
        },
        "gridPos": {"h": 6, "w": 4, "x": 8, "y": 0}
      },
      {
        "id": 4,
        "title": "Taxa de Sucesso de Operações",
        "type": "stat",
        "targets": [
          {
            "expr": "(1 - (rate(commands_failed_total[1h]) / rate(commands_processed_total[1h]))) * 100",
            "legendFormat": "Taxa de Sucesso %"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "unit": "percent",
            "decimals": 1,
            "thresholds": {
              "steps": [
                {"color": "red", "value": 0},
                {"color": "yellow", "value": 95},
                {"color": "green", "value": 99}
              ]
            }
          }
        },
        "gridPos": {"h": 6, "w": 4, "x": 12, "y": 0}
      },
      {
        "id": 5,
        "title": "Sinistros por Status - Últimas 24h",
        "type": "piechart",
        "targets": [
          {
            "expr": "sum by (status) (increase(sinistro_status_changes_total[24h]))",
            "legendFormat": "{{status}}"
          }
        ],
        "options": {
          "pieType": "pie",
          "tooltip": {"mode": "single"},
          "legend": {"displayMode": "table", "placement": "right"}
        },
        "gridPos": {"h": 8, "w": 8, "x": 0, "y": 6}
      },
      {
        "id": 6,
        "title": "Tendência de Volume - Últimos 7 dias",
        "type": "graph",
        "targets": [
          {
            "expr": "sum(rate(commands_processed_total{type=\"CriarSinistroCommand\"}[1h])) * 3600",
            "legendFormat": "Sinistros/hora"
          }
        ],
        "yAxes": [
          {"label": "Sinistros/hora", "min": 0}
        ],
        "xAxis": {"mode": "time"},
        "gridPos": {"h": 8, "w": 16, "x": 8, "y": 6}
      }
    ],
    "time": {"from": "now-24h", "to": "now"},
    "refresh": "1m"
  }
}
```

### **👨‍💻 Dashboard Técnico - System Health**

Dashboard focado em métricas técnicas para desenvolvedores e SREs:

#### **system-health-dashboard.json:**
```json
{
  "dashboard": {
    "id": null,
    "title": "System Health - Arquitetura Híbrida",
    "tags": ["technical", "health", "sre"],
    "panels": [
      {
        "id": 1,
        "title": "JVM Memory Usage",
        "type": "graph",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes{area=\"heap\"}",
            "legendFormat": "Heap Used"
          },
          {
            "expr": "jvm_memory_max_bytes{area=\"heap\"}",
            "legendFormat": "Heap Max"
          }
        ],
        "yAxes": [
          {"label": "Bytes", "logBase": 1}
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 0}
      },
      {
        "id": 2,
        "title": "GC Activity",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(jvm_gc_collection_seconds_sum[5m])",
            "legendFormat": "GC Time/sec"
          },
          {
            "expr": "rate(jvm_gc_collection_seconds_count[5m])",
            "legendFormat": "GC Collections/sec"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 0}
      },
      {
        "id": 3,
        "title": "Database Connection Pools",
        "type": "graph",
        "targets": [
          {
            "expr": "hikaricp_connections_active",
            "legendFormat": "Active - {{pool}}"
          },
          {
            "expr": "hikaricp_connections_idle",
            "legendFormat": "Idle - {{pool}}"
          },
          {
            "expr": "hikaricp_connections_max",
            "legendFormat": "Max - {{pool}}"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 8}
      },
      {
        "id": 4,
        "title": "HTTP Request Rate & Latency",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count[5m])",
            "legendFormat": "Requests/sec - {{uri}}"
          },
          {
            "expr": "histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))",
            "legendFormat": "P95 Latency - {{uri}}"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 8}
      },
      {
        "id": 5,
        "title": "Thread Pool Status",
        "type": "table",
        "targets": [
          {
            "expr": "executor_active_threads",
            "legendFormat": "Active",
            "format": "table"
          },
          {
            "expr": "executor_pool_size_threads",
            "legendFormat": "Pool Size",
            "format": "table"
          },
          {
            "expr": "executor_queue_remaining_tasks",
            "legendFormat": "Queue Remaining",
            "format": "table"
          }
        ],
        "gridPos": {"h": 6, "w": 24, "x": 0, "y": 16}
      }
    ]
  }
}
```

---

## 🎯 **DASHBOARDS ESPECÍFICOS PARA EVENT SOURCING**

### **📊 Event Store Analytics Dashboard**

#### **eventstore-analytics-dashboard.json:**
```json
{
  "dashboard": {
    "id": null,
    "title": "Event Store Analytics",
    "tags": ["eventstore", "eventsourcing"],
    "panels": [
      {
        "id": 1,
        "title": "Event Store Growth",
        "type": "graph",
        "targets": [
          {
            "expr": "eventstore_events_total",
            "legendFormat": "Total Events"
          },
          {
            "expr": "rate(eventstore_events_total[1h]) * 3600",
            "legendFormat": "Events/hour"
          }
        ],
        "yAxes": [
          {"label": "Events", "min": 0}
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 0}
      },
      {
        "id": 2,
        "title": "Events by Type",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(eventstore_events_by_type_total[5m]) by (event_type)",
            "legendFormat": "{{event_type}}"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 0}
      },
      {
        "id": 3,
        "title": "Event Store Performance",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(eventstore_save_duration_seconds_bucket[5m]))",
            "legendFormat": "P95 Save Time"
          },
          {
            "expr": "histogram_quantile(0.95, rate(eventstore_load_duration_seconds_bucket[5m]))",
            "legendFormat": "P95 Load Time"
          }
        ],
        "yAxes": [
          {"label": "Seconds", "min": 0}
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 8}
      },
      {
        "id": 4,
        "title": "Aggregate Statistics",
        "type": "table",
        "targets": [
          {
            "expr": "eventstore_aggregates_total",
            "legendFormat": "Total Aggregates",
            "format": "table"
          },
          {
            "expr": "avg(eventstore_events_per_aggregate)",
            "legendFormat": "Avg Events/Aggregate",
            "format": "table"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 8}
      },
      {
        "id": 5,
        "title": "Snapshot Usage",
        "type": "graph",
        "targets": [
          {
            "expr": "snapshots_created_total",
            "legendFormat": "Snapshots Created"
          },
          {
            "expr": "snapshots_loaded_total",
            "legendFormat": "Snapshots Loaded"
          },
          {
            "expr": "snapshot_compression_ratio",
            "legendFormat": "Compression Ratio"
          }
        ],
        "gridPos": {"h": 8, "w": 24, "x": 0, "y": 16}
      }
    ]
  }
}
```

---

## 🔄 **DASHBOARDS PARA CQRS**

### **📈 CQRS Monitoring Dashboard**

#### **cqrs-monitoring-dashboard.json:**
```json
{
  "dashboard": {
    "id": null,
    "title": "CQRS Monitoring",
    "tags": ["cqrs", "command-query"],
    "panels": [
      {
        "id": 1,
        "title": "Command vs Query Load",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(commands_processed_total[5m])",
            "legendFormat": "Commands/sec"
          },
          {
            "expr": "rate(queries_executed_total[5m])",
            "legendFormat": "Queries/sec"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 0}
      },
      {
        "id": 2,
        "title": "CQRS Lag Distribution",
        "type": "heatmap",
        "targets": [
          {
            "expr": "increase(cqrs_lag_seconds_bucket[5m])",
            "legendFormat": "{{le}}"
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 0}
      },
      {
        "id": 3,
        "title": "Projection Status Overview",
        "type": "table",
        "targets": [
          {
            "expr": "projection_status",
            "legendFormat": "{{projection_name}}",
            "format": "table"
          }
        ],
        "transformations": [
          {
            "id": "organize",
            "options": {
              "excludeByName": {},
              "indexByName": {},
              "renameByName": {
                "projection_name": "Projection",
                "Value": "Status"
              }
            }
          }
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 8}
      },
      {
        "id": 4,
        "title": "Projection Lag by Type",
        "type": "graph",
        "targets": [
          {
            "expr": "projection_lag_seconds by (projection_name)",
            "legendFormat": "{{projection_name}}"
          }
        ],
        "yAxes": [
          {"label": "Lag (seconds)", "min": 0}
        ],
        "alert": {
          "conditions": [
            {
              "evaluator": {"params": [30], "type": "gt"},
              "operator": {"type": "and"},
              "query": {"params": ["A", "5m", "now"]},
              "reducer": {"params": [], "type": "max"},
              "type": "query"
            }
          ],
          "executionErrorState": "alerting",
          "for": "2m",
          "frequency": "10s",
          "handler": 1,
          "name": "High Projection Lag",
          "noDataState": "no_data"
        },
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 8}
      },
      {
        "id": 5,
        "title": "Read/Write DataSource Health",
        "type": "stat",
        "targets": [
          {
            "expr": "datasource_health{type=\"write\"}",
            "legendFormat": "Write DB"
          },
          {
            "expr": "datasource_health{type=\"read\"}",
            "legendFormat": "Read DB"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "mappings": [
              {"options": {"0": {"text": "DOWN", "color": "red"}}, "type": "value"},
              {"options": {"1": {"text": "UP", "color": "green"}}, "type": "value"}
            ]
          }
        },
        "gridPos": {"h": 4, "w": 24, "x": 0, "y": 16}
      }
    ]
  }
}
```

---

## 📊 **VISUALIZAÇÕES AVANÇADAS**

### **🎨 Custom Panels e Plugins**

#### **Configuração de Plugins Úteis:**
```bash
# Instalar plugins úteis no Grafana
grafana-cli plugins install grafana-piechart-panel
grafana-cli plugins install grafana-worldmap-panel
grafana-cli plugins install grafana-clock-panel
grafana-cli plugins install grafana-polystat-panel
```

#### **Panel Customizado para Event Flow:**
```json
{
  "id": 10,
  "title": "Event Flow Visualization",
  "type": "grafana-polystat-panel",
  "targets": [
    {
      "expr": "rate(eventbus_events_published_total[5m]) by (event_type)",
      "legendFormat": "{{event_type}}"
    }
  ],
  "polystat": {
    "animationSpeed": 4000,
    "columns": 4,
    "defaultClickThrough": "",
    "displayLimit": 100,
    "fontAutoColor": true,
    "fontSize": "12px",
    "fontType": "Roboto",
    "globalDisplayMode": "all",
    "globalOperatorName": "avg",
    "globalThresholds": [
      {"color": "rgba(245, 54, 54, 0.9)", "state": 0, "value": 0},
      {"color": "rgba(237, 129, 40, 0.89)", "state": 1, "value": 1},
      {"color": "rgba(50, 172, 45, 0.97)", "state": 2, "value": 10}
    ],
    "gradientEnabled": true,
    "hexagonSortByDirection": 1,
    "hexagonSortByField": "name",
    "maxMetrics": 0,
    "polygonBorderColor": "black",
    "polygonBorderSize": 2,
    "radius": "",
    "radiusAutoSize": true,
    "regexPattern": "",
    "rowAutoSize": true,
    "rows": 4,
    "shape": "hexagon_pointed_top",
    "tooltipDisplayMode": "all",
    "tooltipDisplayTextTriggeredEmpty": "OK",
    "tooltipFontSize": "12px",
    "tooltipFontType": "Roboto"
  },
  "gridPos": {"h": 8, "w": 24, "x": 0, "y": 20}
}
```

### **📈 SLI/SLO Dashboard**

#### **sli-slo-dashboard.json:**
```json
{
  "dashboard": {
    "id": null,
    "title": "SLI/SLO Dashboard",
    "tags": ["sli", "slo", "reliability"],
    "panels": [
      {
        "id": 1,
        "title": "Availability SLO (99.9%)",
        "type": "stat",
        "targets": [
          {
            "expr": "avg_over_time(up{job=\"app-arquitetura-hibrida\"}[30d]) * 100",
            "legendFormat": "30-day Availability"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "unit": "percent",
            "decimals": 3,
            "thresholds": {
              "steps": [
                {"color": "red", "value": 0},
                {"color": "yellow", "value": 99.0},
                {"color": "green", "value": 99.9}
              ]
            }
          }
        },
        "gridPos": {"h": 6, "w": 6, "x": 0, "y": 0}
      },
      {
        "id": 2,
        "title": "Latency SLO (95% < 2s)",
        "type": "stat",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[30d])) < 2",
            "legendFormat": "P95 Latency SLO"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "mappings": [
              {"options": {"0": {"text": "BREACH", "color": "red"}}, "type": "value"},
              {"options": {"1": {"text": "OK", "color": "green"}}, "type": "value"}
            ]
          }
        },
        "gridPos": {"h": 6, "w": 6, "x": 6, "y": 0}
      },
      {
        "id": 3,
        "title": "Error Rate SLO (< 1%)",
        "type": "stat",
        "targets": [
          {
            "expr": "(rate(commands_failed_total[30d]) / rate(commands_processed_total[30d])) * 100",
            "legendFormat": "30-day Error Rate"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "unit": "percent",
            "decimals": 2,
            "thresholds": {
              "steps": [
                {"color": "green", "value": 0},
                {"color": "yellow", "value": 0.5},
                {"color": "red", "value": 1.0}
              ]
            }
          }
        },
        "gridPos": {"h": 6, "w": 6, "x": 12, "y": 0}
      },
      {
        "id": 4,
        "title": "Error Budget Burn Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(commands_failed_total[1h]) / (0.01 * rate(commands_processed_total[1h]))",
            "legendFormat": "1h Burn Rate"
          },
          {
            "expr": "rate(commands_failed_total[6h]) / (0.01 * rate(commands_processed_total[6h]))",
            "legendFormat": "6h Burn Rate"
          }
        ],
        "yAxes": [
          {"label": "Burn Rate", "min": 0}
        ],
        "alert": {
          "conditions": [
            {
              "evaluator": {"params": [14.4], "type": "gt"},
              "operator": {"type": "and"},
              "query": {"params": ["A", "5m", "now"]},
              "reducer": {"params": [], "type": "avg"},
              "type": "query"
            }
          ],
          "executionErrorState": "alerting",
          "for": "2m",
          "frequency": "10s",
          "handler": 1,
          "name": "High Error Budget Burn Rate",
          "noDataState": "no_data"
        },
        "gridPos": {"h": 8, "w": 24, "x": 0, "y": 6}
      }
    ]
  }
}
```

---

## ⚡ **OTIMIZAÇÃO DE PERFORMANCE**

### **🔧 Configurações de Performance para Dashboards**

#### **Otimizações no Grafana:**
```ini
# grafana.ini - Seção de performance
[database]
max_idle_conn = 2
max_open_conn = 0
conn_max_lifetime = 14400

[dataproxy]
timeout = 30
keep_alive_seconds = 30

[rendering]
server_url = http://renderer:8081/render
callback_url = http://grafana:3000/

[panels]
disable_sanitize_html = false
enable_alpha = true

[feature_toggles]
enable = ngalert
```

#### **Query Optimization Patterns:**
```promql
# ❌ Evitar - Query muito ampla
rate(http_server_requests_seconds_count[5m])

# ✅ Melhor - Query específica com filtros
rate(http_server_requests_seconds_count{job="app-arquitetura-hibrida",status!~"4.."}[5m])

# ❌ Evitar - Agregação desnecessária
sum(rate(commands_processed_total[5m])) by (instance, job, type)

# ✅ Melhor - Agregação focada
sum(rate(commands_processed_total[5m])) by (type)

# ✅ Usar recording rules para queries complexas
commands:rate5m  # Definido em recording_rules.yml
```

### **📊 Template Variables para Reutilização**

#### **Configuração de Variables:**
```json
{
  "templating": {
    "list": [
      {
        "name": "datasource",
        "type": "datasource",
        "query": "prometheus",
        "current": {
          "value": "Prometheus",
          "text": "Prometheus"
        }
      },
      {
        "name": "job",
        "type": "query",
        "datasource": "$datasource",
        "query": "label_values(up, job)",
        "current": {
          "value": "app-arquitetura-hibrida",
          "text": "app-arquitetura-hibrida"
        },
        "refresh": 1
      },
      {
        "name": "instance",
        "type": "query",
        "datasource": "$datasource",
        "query": "label_values(up{job=\"$job\"}, instance)",
        "current": {
          "value": "All",
          "text": "All"
        },
        "includeAll": true,
        "refresh": 1
      },
      {
        "name": "command_type",
        "type": "query",
        "datasource": "$datasource",
        "query": "label_values(commands_processed_total, type)",
        "current": {
          "value": "All",
          "text": "All"
        },
        "includeAll": true,
        "refresh": 1
      }
    ]
  }
}
```

---

## 📱 **DASHBOARDS RESPONSIVOS**

### **📋 Mobile-Friendly Configuration**

```json
{
  "dashboard": {
    "panels": [
      {
        "id": 1,
        "title": "Mobile Overview",
        "type": "stat",
        "gridPos": {
          "h": 4,
          "w": 12,
          "x": 0,
          "y": 0
        },
        "options": {
          "orientation": "horizontal",
          "textMode": "auto",
          "colorMode": "background"
        },
        "fieldConfig": {
          "defaults": {
            "displayName": "${__field.labels.type}",
            "unit": "short"
          }
        }
      }
    ],
    "tags": ["mobile", "responsive"]
  }
}
```

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [Grafana Dashboard Best Practices](https://grafana.com/docs/grafana/latest/best-practices/)
- [PromQL Best Practices](https://prometheus.io/docs/practices/queries/)
- [Grafana Plugins Directory](https://grafana.com/grafana/plugins/)
- [SLI/SLO Best Practices](https://sre.google/workbook/implementing-slos/)

### **📖 Próximas Partes:**
- **Parte 4**: Alertas e Notificações
- **Parte 5**: Troubleshooting e Debugging

---

**📝 Parte 3 de 5 - Dashboards e Visualizações Avançadas**  
**⏱️ Tempo estimado**: 75 minutos  
**🎯 Próximo**: [Parte 4 - Alertas e Notificações](./11-monitoramento-parte-4.md)