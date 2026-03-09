# 📊 MONITORAMENTO E OBSERVABILIDADE - PARTE 4
## Alertas e Notificações

### 🎯 **OBJETIVOS DESTA PARTE**
- Configurar sistema de alertas inteligentes
- Implementar notificações multi-canal
- Definir escalation policies e runbooks
- Configurar alertas específicos para Event Sourcing e CQRS

---

## 🚨 **ESTRATÉGIA DE ALERTAS**

### **📋 Princípios de Alerting**

#### **Golden Rules of Alerting:**
- ✅ **Alert on Symptoms, not Causes**: Foque no impacto ao usuário
- ✅ **Actionable Alerts Only**: Todo alerta deve ter uma ação clara
- ✅ **Reduce Alert Fatigue**: Evite alertas desnecessários
- ✅ **Context is King**: Forneça contexto suficiente para ação

#### **Níveis de Severidade:**
| **Nível** | **Descrição** | **Resposta** | **Exemplo** |
|-----------|---------------|--------------|-------------|
| **CRITICAL** | Sistema indisponível | Imediata (24/7) | Event Store down |
| **WARNING** | Degradação de performance | 1-4 horas | Alta latência |
| **INFO** | Informacional | Próximo dia útil | Deployment |

---

## ⚙️ **CONFIGURAÇÃO DO ALERTMANAGER**

### **📝 alertmanager.yml - Configuração Principal**

```yaml
global:
  smtp_smarthost: 'smtp.gmail.com:587'
  smtp_from: 'alerts@seguradora.com'
  smtp_auth_username: 'alerts@seguradora.com'
  smtp_auth_password: 'app-password'
  
  # Slack configuration
  slack_api_url: 'https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK'
  
  # PagerDuty configuration
  pagerduty_url: 'https://events.pagerduty.com/v2/enqueue'

# Templates para customização de mensagens
templates:
  - '/etc/alertmanager/templates/*.tmpl'

# Configuração de rotas
route:
  group_by: ['alertname', 'cluster', 'service']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'default-receiver'
  
  routes:
    # Alertas críticos vão para PagerDuty
    - match:
        severity: critical
      receiver: 'pagerduty-critical'
      group_wait: 10s
      repeat_interval: 5m
      
    # Alertas de warning vão para Slack
    - match:
        severity: warning
      receiver: 'slack-warnings'
      group_wait: 30s
      repeat_interval: 30m
      
    # Alertas específicos da arquitetura híbrida
    - match:
        component: cqrs
      receiver: 'architecture-team'
      
    - match:
        component: eventstore
      receiver: 'data-team'

# Configuração de receivers
receivers:
  - name: 'default-receiver'
    email_configs:
      - to: 'dev-team@seguradora.com'
        subject: '[ALERT] {{ .GroupLabels.alertname }}'
        body: |
          {{ range .Alerts }}
          Alert: {{ .Annotations.summary }}
          Description: {{ .Annotations.description }}
          Labels: {{ range .Labels.SortedPairs }}{{ .Name }}={{ .Value }} {{ end }}
          {{ end }}

  - name: 'pagerduty-critical'
    pagerduty_configs:
      - service_key: 'YOUR-PAGERDUTY-SERVICE-KEY'
        description: '{{ .GroupLabels.alertname }}: {{ .GroupLabels.instance }}'
        details:
          firing: '{{ .Alerts.Firing | len }}'
          resolved: '{{ .Alerts.Resolved | len }}'
          
  - name: 'slack-warnings'
    slack_configs:
      - channel: '#alerts-arquitetura-hibrida'
        title: 'Alert: {{ .GroupLabels.alertname }}'
        text: |
          {{ range .Alerts }}
          *Alert:* {{ .Annotations.summary }}
          *Description:* {{ .Annotations.description }}
          *Severity:* {{ .Labels.severity }}
          *Component:* {{ .Labels.component }}
          {{ end }}
        color: '{{ if eq .Status "firing" }}danger{{ else }}good{{ end }}'
        
  - name: 'architecture-team'
    slack_configs:
      - channel: '#architecture-team'
        title: 'Architecture Alert: {{ .GroupLabels.alertname }}'
        text: |
          🏗️ *Architecture Component Alert*
          {{ range .Alerts }}
          *Component:* {{ .Labels.component }}
          *Issue:* {{ .Annotations.summary }}
          *Details:* {{ .Annotations.description }}
          *Runbook:* {{ .Annotations.runbook_url }}
          {{ end }}
        
  - name: 'data-team'
    email_configs:
      - to: 'data-team@seguradora.com'
        subject: '[DATA ALERT] {{ .GroupLabels.alertname }}'
        body: |
          🗄️ Data Layer Alert
          {{ range .Alerts }}
          Component: {{ .Labels.component }}
          Issue: {{ .Annotations.summary }}
          Description: {{ .Annotations.description }}
          Runbook: {{ .Annotations.runbook_url }}
          {{ end }}

# Configuração de inibição (evita spam de alertas relacionados)
inhibit_rules:
  - source_match:
      severity: 'critical'
    target_match:
      severity: 'warning'
    equal: ['alertname', 'cluster', 'service']
    
  - source_match:
      alertname: 'EventStoreDown'
    target_match_re:
      alertname: '(HighCQRSLag|ProjectionErrors)'
    equal: ['instance']
```

---

## 🎯 **ALERTAS ESPECÍFICOS PARA ARQUITETURA HÍBRIDA**

### **📝 Regras de Alerta Avançadas**

#### **architecture-specific-alerts.yml:**
```yaml
groups:
  - name: event-sourcing-alerts
    rules:
      # Event Store Health
      - alert: EventStoreDown
        expr: up{job="app-arquitetura-hibrida"} == 0
        for: 1m
        labels:
          severity: critical
          component: eventstore
          team: architecture
        annotations:
          summary: "Event Store is down"
          description: "Event Store has been down for more than 1 minute"
          runbook_url: "https://wiki.seguradora.com/runbooks/eventstore-down"
          dashboard_url: "http://grafana:3000/d/eventstore/event-store-dashboard"

      # Event Store Performance
      - alert: EventStoreHighLatency
        expr: histogram_quantile(0.95, rate(eventstore_save_duration_seconds_bucket[5m])) > 2
        for: 5m
        labels:
          severity: warning
          component: eventstore
          team: architecture
        annotations:
          summary: "High Event Store save latency"
          description: "95th percentile save latency is {{ $value }}s"
          runbook_url: "https://wiki.seguradora.com/runbooks/eventstore-performance"

      # Event Store Disk Space
      - alert: EventStoreDiskSpaceHigh
        expr: (node_filesystem_avail_bytes{mountpoint="/var/lib/postgresql"} / node_filesystem_size_bytes{mountpoint="/var/lib/postgresql"}) * 100 < 10
        for: 5m
        labels:
          severity: critical
          component: eventstore
          team: data
        annotations:
          summary: "Event Store disk space critically low"
          description: "Only {{ $value }}% disk space remaining"
          runbook_url: "https://wiki.seguradora.com/runbooks/disk-space-cleanup"

  - name: cqrs-alerts
    rules:
      # CQRS Lag
      - alert: HighCQRSLag
        expr: cqrs_lag_seconds > 60
        for: 2m
        labels:
          severity: warning
          component: cqrs
          team: architecture
        annotations:
          summary: "High CQRS lag detected"
          description: "Command-Query lag is {{ $value }} seconds"
          runbook_url: "https://wiki.seguradora.com/runbooks/cqrs-lag"
          
      - alert: CriticalCQRSLag
        expr: cqrs_lag_seconds > 300
        for: 1m
        labels:
          severity: critical
          component: cqrs
          team: architecture
        annotations:
          summary: "Critical CQRS lag detected"
          description: "Command-Query lag is {{ $value }} seconds (>5min)"
          runbook_url: "https://wiki.seguradora.com/runbooks/cqrs-lag-critical"

      # Projection Health
      - alert: ProjectionDown
        expr: projection_status == 0
        for: 1m
        labels:
          severity: critical
          component: projections
          team: architecture
        annotations:
          summary: "Projection {{ $labels.projection_name }} is down"
          description: "Projection has been down for more than 1 minute"
          runbook_url: "https://wiki.seguradora.com/runbooks/projection-down"

      - alert: ProjectionHighErrorRate
        expr: rate(projection_errors_total[5m]) / rate(projection_events_processed_total[5m]) > 0.05
        for: 3m
        labels:
          severity: warning
          component: projections
          team: architecture
        annotations:
          summary: "High error rate in projection {{ $labels.projection_name }}"
          description: "Error rate is {{ $value | humanizePercentage }}"
          runbook_url: "https://wiki.seguradora.com/runbooks/projection-errors"

  - name: command-bus-alerts
    rules:
      # Command Bus Performance
      - alert: HighCommandLatency
        expr: histogram_quantile(0.95, rate(commands_execution_time_seconds_bucket[5m])) > 5
        for: 3m
        labels:
          severity: warning
          component: command-bus
          team: architecture
        annotations:
          summary: "High command execution latency"
          description: "95th percentile latency is {{ $value }}s for {{ $labels.type }}"
          runbook_url: "https://wiki.seguradora.com/runbooks/command-performance"

      # Command Error Rate
      - alert: HighCommandErrorRate
        expr: rate(commands_failed_total[5m]) / rate(commands_processed_total[5m]) > 0.1
        for: 2m
        labels:
          severity: warning
          component: command-bus
          team: architecture
        annotations:
          summary: "High command error rate"
          description: "Error rate is {{ $value | humanizePercentage }} for {{ $labels.type }}"
          runbook_url: "https://wiki.seguradora.com/runbooks/command-errors"

      # Command Queue Backup
      - alert: CommandQueueBackup
        expr: command_queue_size > 1000
        for: 5m
        labels:
          severity: warning
          component: command-bus
          team: architecture
        annotations:
          summary: "Command queue backup detected"
          description: "Queue size is {{ $value }} commands"
          runbook_url: "https://wiki.seguradora.com/runbooks/command-queue-backup"

  - name: business-alerts
    rules:
      # Business Metrics
      - alert: LowSinistroCreationRate
        expr: rate(commands_processed_total{type="CriarSinistroCommand"}[1h]) * 3600 < 10
        for: 30m
        labels:
          severity: info
          component: business
          team: product
        annotations:
          summary: "Low sinistro creation rate"
          description: "Only {{ $value }} sinistros created in the last hour"
          runbook_url: "https://wiki.seguradora.com/runbooks/low-business-activity"

      - alert: HighSinistroCreationRate
        expr: rate(commands_processed_total{type="CriarSinistroCommand"}[5m]) * 60 > 100
        for: 10m
        labels:
          severity: warning
          component: business
          team: product
        annotations:
          summary: "Unusually high sinistro creation rate"
          description: "{{ $value }} sinistros/minute being created"
          runbook_url: "https://wiki.seguradora.com/runbooks/high-business-activity"
```

---

## 📱 **CONFIGURAÇÃO DE CANAIS DE NOTIFICAÇÃO**

### **🔔 Slack Integration**

#### **slack-templates.tmpl:**
```go
{{ define "slack.title" }}
[{{ .Status | toUpper }}{{ if eq .Status "firing" }}:{{ .Alerts.Firing | len }}{{ end }}] {{ .GroupLabels.alertname }}
{{ end }}

{{ define "slack.text" }}
{{ if eq .Status "firing" }}
🚨 *FIRING ALERTS*
{{ range .Alerts.Firing }}
*Alert:* {{ .Annotations.summary }}
*Description:* {{ .Annotations.description }}
*Severity:* {{ .Labels.severity }}
*Component:* {{ .Labels.component }}
*Started:* {{ .StartsAt.Format "2006-01-02 15:04:05" }}
{{ if .Annotations.runbook_url }}*Runbook:* {{ .Annotations.runbook_url }}{{ end }}
{{ if .Annotations.dashboard_url }}*Dashboard:* {{ .Annotations.dashboard_url }}{{ end }}
---
{{ end }}
{{ else }}
✅ *RESOLVED ALERTS*
{{ range .Alerts.Resolved }}
*Alert:* {{ .Annotations.summary }}
*Resolved:* {{ .EndsAt.Format "2006-01-02 15:04:05" }}
{{ end }}
{{ end }}
{{ end }}

{{ define "slack.color" }}
{{ if eq .Status "firing" }}
  {{ if eq .CommonLabels.severity "critical" }}danger{{ else }}warning{{ end }}
{{ else }}good{{ end }}
{{ end }}
```

### **📧 Email Templates**

#### **email-templates.tmpl:**
```html
{{ define "email.subject" }}
[{{ .Status | toUpper }}] {{ .GroupLabels.alertname }} - Arquitetura Híbrida
{{ end }}

{{ define "email.html" }}
<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; }
        .alert { padding: 10px; margin: 10px 0; border-radius: 5px; }
        .critical { background-color: #ffebee; border-left: 5px solid #f44336; }
        .warning { background-color: #fff3e0; border-left: 5px solid #ff9800; }
        .info { background-color: #e3f2fd; border-left: 5px solid #2196f3; }
        .resolved { background-color: #e8f5e8; border-left: 5px solid #4caf50; }
        .details { margin-top: 10px; }
        .label { font-weight: bold; }
    </style>
</head>
<body>
    <h2>Alert Notification - Arquitetura Híbrida</h2>
    
    {{ if eq .Status "firing" }}
    <h3>🚨 Firing Alerts ({{ .Alerts.Firing | len }})</h3>
    {{ range .Alerts.Firing }}
    <div class="alert {{ .Labels.severity }}">
        <div class="label">Alert:</div>
        <div>{{ .Annotations.summary }}</div>
        
        <div class="details">
            <div><span class="label">Description:</span> {{ .Annotations.description }}</div>
            <div><span class="label">Severity:</span> {{ .Labels.severity }}</div>
            <div><span class="label">Component:</span> {{ .Labels.component }}</div>
            <div><span class="label">Started:</span> {{ .StartsAt.Format "2006-01-02 15:04:05" }}</div>
            {{ if .Annotations.runbook_url }}
            <div><span class="label">Runbook:</span> <a href="{{ .Annotations.runbook_url }}">{{ .Annotations.runbook_url }}</a></div>
            {{ end }}
            {{ if .Annotations.dashboard_url }}
            <div><span class="label">Dashboard:</span> <a href="{{ .Annotations.dashboard_url }}">View Dashboard</a></div>
            {{ end }}
        </div>
    </div>
    {{ end }}
    {{ else }}
    <h3>✅ Resolved Alerts ({{ .Alerts.Resolved | len }})</h3>
    {{ range .Alerts.Resolved }}
    <div class="alert resolved">
        <div class="label">Alert:</div>
        <div>{{ .Annotations.summary }}</div>
        <div class="details">
            <div><span class="label">Resolved:</span> {{ .EndsAt.Format "2006-01-02 15:04:05" }}</div>
        </div>
    </div>
    {{ end }}
    {{ end }}
    
    <hr>
    <p><small>This alert was generated by the Arquitetura Híbrida monitoring system.</small></p>
</body>
</html>
{{ end }}
```

---

## 📋 **RUNBOOKS E PROCEDIMENTOS**

### **🔧 Runbook: Event Store Down**

#### **runbooks/eventstore-down.md:**
```markdown
# Runbook: Event Store Down

## Symptoms
- Alert: EventStoreDown
- Application returning 500 errors
- Commands failing to persist

## Immediate Actions (< 5 minutes)

### 1. Verify the Issue
```bash
# Check application health
curl -f http://app:8080/actuator/health

# Check database connectivity
docker exec eventstore-db pg_isready -U eventstore

# Check container status
docker ps | grep eventstore
```

### 2. Quick Recovery Attempts
```bash
# Restart application container
docker restart app-arquitetura-hibrida

# If database is down, restart it
docker restart eventstore-db

# Check logs for errors
docker logs eventstore-db --tail 100
docker logs app-arquitetura-hibrida --tail 100
```

## Investigation (5-15 minutes)

### 3. Database Health Check
```sql
-- Connect to database
psql -h eventstore-db -U eventstore -d eventstore

-- Check connections
SELECT count(*) FROM pg_stat_activity;

-- Check disk space
SELECT pg_size_pretty(pg_database_size('eventstore'));

-- Check for locks
SELECT * FROM pg_locks WHERE NOT granted;
```

### 4. Application Health Check
```bash
# Check JVM metrics
curl http://app:8080/actuator/metrics/jvm.memory.used

# Check connection pool
curl http://app:8080/actuator/metrics/hikaricp.connections.active
```

## Resolution Steps

### 5. Common Fixes
- **Out of Memory**: Increase container memory limits
- **Connection Pool Exhausted**: Restart application
- **Disk Full**: Clean up old logs, increase disk space
- **Database Corruption**: Restore from backup

### 6. Escalation
If issue persists > 15 minutes:
- Page on-call architect: +55 11 99999-9999
- Create incident in PagerDuty
- Notify stakeholders via #incidents channel

## Prevention
- Monitor disk space alerts
- Set up connection pool monitoring
- Regular database maintenance
- Backup verification
```

### **🔧 Runbook: High CQRS Lag**

#### **runbooks/cqrs-lag.md:**
```markdown
# Runbook: High CQRS Lag

## Symptoms
- Alert: HighCQRSLag or CriticalCQRSLag
- Queries returning stale data
- Users reporting outdated information

## Immediate Actions (< 2 minutes)

### 1. Check Projection Status
```bash
# Check projection health endpoint
curl http://app:8080/actuator/health/projections

# Check individual projections
curl http://app:8080/api/projections
```

### 2. Identify Bottleneck
```bash
# Check projection processing metrics
curl http://app:8080/actuator/metrics/projection.events.processed

# Check for stuck projections
curl http://app:8080/api/projections/status
```

## Investigation (2-10 minutes)

### 3. Database Performance
```sql
-- Check read database performance
SELECT * FROM pg_stat_activity WHERE state = 'active';

-- Check for long-running queries
SELECT query, state, query_start 
FROM pg_stat_activity 
WHERE query_start < now() - interval '1 minute';
```

### 4. Event Processing
```bash
# Check event bus metrics
curl http://app:8080/actuator/metrics/eventbus.events.processed

# Check for event processing errors
docker logs app-arquitetura-hibrida | grep "EventHandlingException"
```

## Resolution Steps

### 5. Quick Fixes
```bash
# Restart projection processing
curl -X POST http://app:8080/api/projections/restart-all

# Clear projection cache
curl -X POST http://app:8080/api/cache/clear

# Scale up read replicas (if available)
docker-compose up --scale projections-db=2
```

### 6. Performance Optimization
- Check for missing database indexes
- Optimize projection queries
- Increase projection thread pool size
- Consider projection partitioning

## Prevention
- Monitor projection lag continuously
- Set up read replica auto-scaling
- Regular projection performance reviews
- Database query optimization
```

---

## 🎯 **ALERTAS INTELIGENTES**

### **📊 Machine Learning Based Alerts**

#### **Configuração de Alertas Adaptativos:**
```yaml
# anomaly-detection-alerts.yml
groups:
  - name: anomaly-detection
    rules:
      # Detecção de anomalia em throughput
      - alert: AnomalousCommandThroughput
        expr: |
          (
            rate(commands_processed_total[5m]) - 
            avg_over_time(rate(commands_processed_total[5m])[1w:1h])
          ) / stddev_over_time(rate(commands_processed_total[5m])[1w:1h]) > 3
        for: 10m
        labels:
          severity: warning
          component: anomaly-detection
        annotations:
          summary: "Anomalous command throughput detected"
          description: "Command throughput is {{ $value }} standard deviations from normal"

      # Detecção de padrão sazonal
      - alert: UnexpectedLowActivity
        expr: |
          rate(commands_processed_total[1h]) < 
          quantile_over_time(0.1, rate(commands_processed_total[1h])[4w:1h])
        for: 2h
        labels:
          severity: info
          component: business-intelligence
        annotations:
          summary: "Unexpectedly low business activity"
          description: "Activity is below 10th percentile for this time period"
```

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [Alertmanager Configuration](https://prometheus.io/docs/alerting/latest/configuration/)
- [Prometheus Alerting Best Practices](https://prometheus.io/docs/practices/alerting/)
- [SRE Alerting Principles](https://sre.google/sre-book/monitoring-distributed-systems/)
- [PagerDuty Integration Guide](https://www.pagerduty.com/docs/guides/prometheus-integration-guide/)

### **📖 Próxima Parte:**
- **Parte 5**: Troubleshooting e Debugging

---

**📝 Parte 4 de 5 - Alertas e Notificações**  
**⏱️ Tempo estimado**: 60 minutos  
**🎯 Próximo**: [Parte 5 - Troubleshooting e Debugging](./11-monitoramento-parte-5.md)