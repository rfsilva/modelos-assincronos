# 🛡️ Arquitetura Resiliente - Sistema de Sinistros

## 📋 Visão Geral

Esta aplicação implementa a **Opção 1** da arquitetura de sinistros, focada em **resiliência e disponibilidade**. Utiliza padrões como Circuit Breaker, Cache Distribuído e processamento assíncrono para garantir que o sistema continue operacional mesmo com instabilidades no Detran.

## 🏗️ Arquitetura Implementada

### Características Principais
- ⚡ **Circuit Breaker** para integração com Detran
- 🗄️ **Cache Distribuído** (Redis) para performance
- 📨 **Processamento Assíncrono** com Kafka
- 🔄 **Retry Automático** com backoff exponencial
- 📊 **Monitoramento** completo com métricas

### Padrões de Resiliência
- **Timeout**: 30s para consultas Detran
- **Retry**: Até 3 tentativas com backoff
- **Circuit Breaker**: Abre com 50% de falhas
- **Cache**: TTL de 24h para dados do Detran
- **Fallback**: Processamento assíncrono em caso de falha

## 🚀 Como Executar

### Pré-requisitos
- Java 21+
- Maven 3.8+
- Docker e Docker Compose (para dependências)

### Dependências Externas
```bash
# Subir dependências com Docker Compose
docker-compose up -d redis kafka postgres
```

### Executar Aplicação
```bash
# Perfil local (H2 em memória)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Perfil completo (PostgreSQL + Redis + Kafka)
mvn spring-boot:run
```

### Acessar Aplicação
- **🎨 Swagger UI**: http://localhost:8081/api/v1/swagger-ui.html
- **📄 OpenAPI Docs**: http://localhost:8081/api/v1/api-docs
- **🌐 API Base**: http://localhost:8081/api/v1
- **📊 Actuator**: http://localhost:8081/api/v1/actuator
- **📈 Métricas**: http://localhost:8081/api/v1/actuator/prometheus
- **⚡ Circuit Breakers**: http://localhost:8081/api/v1/actuator/circuitbreakers

## 🔧 Configuração

### Resilience4j
```yaml
resilience4j:
  circuitbreaker:
    instances:
      detran:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
```

### Cache Redis
```yaml
app:
  cache:
    detran:
      ttl: 24h
      max-size: 10000
```

### Processamento Assíncrono
```yaml
app:
  async:
    core-pool-size: 10
    max-pool-size: 50
    queue-capacity: 1000
```

## 📊 Monitoramento

### Métricas Disponíveis
- Taxa de sucesso/falha das consultas Detran
- Tempo de resposta médio do Detran
- Status do Circuit Breaker
- Hit rate do cache Redis
- Tamanho das filas Kafka

### Health Checks
- **Sistema**: `/sistema/status`
- **Detalhado**: `/sistema/health`
- **Actuator**: `/actuator/health`

## 🧪 Testes

```bash
# Executar testes unitários
mvn test

# Executar testes de integração
mvn verify

# Relatório de cobertura
mvn jacoco:report
```

## 📦 Dependências Principais

- **Spring Boot 3.2.1** - Framework base
- **Resilience4j 2.2.0** - Circuit Breaker e Retry
- **Spring Data Redis** - Cache distribuído
- **Spring Kafka** - Processamento assíncrono
- **SpringDoc OpenAPI 2.3.0** - Documentação da API
- **Micrometer** - Métricas e observabilidade

## 🎯 Casos de Uso Ideais

- **Ambientes com alta instabilidade do Detran**
- **Volume alto de consultas simultâneas**
- **Necessidade de SLA rigoroso de disponibilidade**
- **Tolerância a eventual consistência dos dados**

## ✅ Vantagens

- ✅ **Alta Resiliência**: Sistema continua funcionando mesmo com Detran instável
- ✅ **Performance**: Cache Redis reduz consultas desnecessárias
- ✅ **Escalabilidade**: Processamento assíncrono permite alta concorrência
- ✅ **Observabilidade**: Métricas detalhadas para monitoramento
- ✅ **Recuperação Automática**: Retry automático com backoff exponencial

## ❌ Desvantagens

- ❌ **Complexidade**: Múltiplos componentes para gerenciar
- ❌ **Latência**: Processamento assíncrono pode aumentar tempo total
- ❌ **Dependências**: Redis e Kafka como pontos de falha adicionais
- ❌ **Custo**: Infraestrutura adicional para cache e mensageria

---

**🚀 Pronto para ambientes que exigem máxima disponibilidade e resiliência!**