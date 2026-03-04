# 🔄 Arquitetura Híbrida - Sistema de Sinistros

## 📋 Visão Geral

Esta aplicação implementa a **Opção 3** da arquitetura de sinistros, combinando **Event Sourcing** para auditoria completa, **CQRS** para separação de responsabilidades e **processamento híbrido** (síncrono para operações críticas, assíncrono para integrações).

## 🏗️ Arquitetura Implementada

### Características Principais
- 📚 **Event Sourcing** para histórico completo
- 🔄 **CQRS** para separação Command/Query
- ⚡ **Processamento Híbrido** (sync + async)
- 🗄️ **Cache Inteligente** para performance
- 📊 **Projections Otimizadas** para consultas

### Padrões Implementados
- **Command Side**: Operações de escrita com Event Sourcing
- **Query Side**: Projections otimizadas para leitura
- **Event Processing**: Assíncrono com Kafka
- **Consistency**: Eventual com garantias de ordem
- **Replay**: Capacidade de reprocessar eventos históricos

## 🚀 Como Executar

### Pré-requisitos
- Java 21+
- Maven 3.8+
- Docker e Docker Compose (para dependências)

### Dependências Externas
```bash
# Subir dependências com Docker Compose
docker-compose up -d postgres redis kafka
```

### Executar Aplicação
```bash
# Perfil local (H2 em memória)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Perfil completo (PostgreSQL + Redis + Kafka)
mvn spring-boot:run
```

### Acessar Aplicação
- **🎨 Swagger UI**: http://localhost:8083/api/v1/swagger-ui.html
- **📄 OpenAPI Docs**: http://localhost:8083/api/v1/api-docs
- **🌐 API Base**: http://localhost:8083/api/v1
- **📊 Actuator**: http://localhost:8083/api/v1/actuator
- **📈 Métricas**: http://localhost:8083/api/v1/actuator/prometheus
- **📚 Event Store**: http://localhost:8083/api/v1/actuator/eventstore
- **📊 Projections**: http://localhost:8083/api/v1/actuator/projections

## 🔧 Configuração

### CQRS
```yaml
cqrs:
  command:
    timeout: 30s
    retry:
      max-attempts: 3
      backoff: 1s
  query:
    cache:
      ttl: 300s
      max-size: 10000
```

### Event Store
```yaml
eventstore:
  batch-size: 100
  snapshot:
    frequency: 50  # snapshot a cada 50 eventos
    async: true
  serialization:
    format: json
    compression: gzip
```

### Axon Framework
```yaml
axon:
  eventhandling:
    processors:
      sinistro-projection:
        mode: tracking
        batch-size: 50
        thread-count: 2
```

## 📊 Monitoramento

### Métricas Disponíveis
- Timeline completa de eventos por aggregate
- Métricas de performance por projection
- Monitoramento de lag entre command e query
- Dashboard de eventos em tempo real

### Health Checks
- **Sistema**: `/sistema/status`
- **Detalhado**: `/sistema/health`
- **Actuator**: `/actuator/health`
- **Event Store**: `/actuator/eventstore`
- **Projections**: `/actuator/projections`

### Observabilidade
- **Event Timeline**: Histórico completo de eventos
- **Projection Status**: Status e lag das projections
- **Command Metrics**: Performance do lado de escrita
- **Query Metrics**: Performance do lado de leitura

## 🧪 Testes

```bash
# Executar testes unitários
mvn test

# Executar testes de integração
mvn verify

# Testes específicos do Axon
mvn test -Dtest=*AggregateTest

# Relatório de cobertura
mvn jacoco:report
```

## 📦 Dependências Principais

- **Spring Boot 3.2.1** - Framework base
- **Axon Framework 4.9.1** - CQRS e Event Sourcing
- **Spring Data JPA** - Persistência para projections
- **Spring Data Redis** - Cache para queries
- **Spring Kafka** - Processamento de eventos
- **SpringDoc OpenAPI 2.3.0** - Documentação da API
- **Micrometer** - Métricas e observabilidade

## 🎯 Casos de Uso Ideais

- **Sistemas com alta carga de leitura e escrita**
- **Necessidade de auditoria detalhada e compliance**
- **Requisitos de escalabilidade horizontal**
- **Tolerância a consistência eventual**
- **Necessidade de análise histórica e replay de eventos**
- **Arquiteturas orientadas a eventos**

## ✅ Vantagens

- ✅ **Auditoria Completa**: Event Sourcing mantém histórico completo
- ✅ **Performance de Leitura**: CQRS otimiza consultas
- ✅ **Escalabilidade**: Separação de responsabilidades permite escala independente
- ✅ **Flexibilidade**: Novas projections podem ser criadas facilmente
- ✅ **Resiliência**: Processamento assíncrono com retry automático
- ✅ **Consistência Eventual**: Melhor performance com garantias de consistência
- ✅ **Replay de Eventos**: Possibilidade de reprocessar eventos históricos

## ❌ Desvantagens

- ❌ **Complexidade Arquitetural**: Múltiplos padrões e conceitos
- ❌ **Curva de Aprendizado**: Equipe precisa dominar Event Sourcing e CQRS
- ❌ **Consistência Eventual**: Pode haver delay entre comando e query
- ❌ **Overhead de Armazenamento**: Event Store pode crescer rapidamente
- ❌ **Debugging Complexo**: Rastreamento através de múltiplos componentes

## 🔄 Fluxo de Processamento

### Command Side (Escrita)
1. **Command Handler** recebe comando
2. **Aggregate** aplica regras de negócio
3. **Event** é gerado e persistido no Event Store
4. **Event Bus** publica evento para processamento

### Query Side (Leitura)
1. **Event Handler** recebe evento
2. **Projection Handler** atualiza view models
3. **Query Handler** responde consultas otimizadas
4. **Cache** acelera consultas frequentes

### Event Processing (Assíncrono)
1. **Integration Handler** processa integrações
2. **Retry Logic** garante entrega de eventos
3. **Dead Letter Queue** para eventos falhados
4. **Monitoring** acompanha processamento

---

**🔄 O melhor dos dois mundos: consistência eventual com alta performance!**