# ⚖️ Arquitetura Consistente - Sistema de Sinistros

## 📋 Visão Geral

Esta aplicação implementa a **Opção 2** da arquitetura de sinistros, focada em **consistência e integridade dos dados**. Utiliza o padrão Saga para garantir que todas as operações sejam executadas com sucesso ou revertidas completamente.

## 🏗️ Arquitetura Implementada

### Características Principais
- 📋 **Saga Pattern** para transações distribuídas
- ⚖️ **Consistência Garantida** através de compensação
- 📊 **Auditoria Completa** de todas as operações
- 🔄 **Rollback Automático** em caso de falha
- 📈 **Visibilidade Total** do processamento

### Padrões de Consistência
- **Transações ACID** para operações críticas
- **Saga Orchestrator** para coordenação
- **Compensação Automática** em caso de falha
- **Auditoria Detalhada** de cada step
- **Timeout Configurável** por operação

## 🚀 Como Executar

### Pré-requisitos
- Java 21+
- Maven 3.8+
- Docker e Docker Compose (para dependências)

### Dependências Externas
```bash
# Subir dependências com Docker Compose
docker-compose up -d postgres
```

### Executar Aplicação
```bash
# Perfil local (H2 em memória)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Perfil completo (PostgreSQL)
mvn spring-boot:run
```

### Acessar Aplicação
- **🎨 Swagger UI**: http://localhost:8082/api/v1/swagger-ui.html
- **📄 OpenAPI Docs**: http://localhost:8082/api/v1/api-docs
- **🌐 API Base**: http://localhost:8082/api/v1
- **📊 Actuator**: http://localhost:8082/api/v1/actuator
- **📈 Métricas**: http://localhost:8082/api/v1/actuator/prometheus
- **📋 Sagas**: http://localhost:8082/api/v1/actuator/saga

## 🔧 Configuração

### Saga Pattern
```yaml
saga:
  timeout:
    step-execution: 60s
    detran-consultation: 30s
    total-saga: 300s
  retry:
    max-attempts: 3
    backoff-delay: 2s
```

### Transações
```yaml
app:
  transaction:
    timeout: 300s
    isolation-level: READ_COMMITTED
```

### Auditoria
```yaml
app:
  audit:
    enabled: true
    retention-days: 90
```

## 📊 Monitoramento

### Métricas Disponíveis
- Taxa de sucesso das sagas por tipo
- Tempo médio de execução por step
- Taxa de compensação por step
- Disponibilidade do Detran por período

### Health Checks
- **Sistema**: `/sistema/status`
- **Detalhado**: `/sistema/health`
- **Actuator**: `/actuator/health`

### Dashboard de Sagas
- Timeline completa de cada saga
- Status de execução em tempo real
- Histórico de compensações
- Métricas de performance por step

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
- **Spring State Machine 4.0.0** - Implementação do Saga Pattern
- **Spring Data JPA** - Persistência e transações
- **SpringDoc OpenAPI 2.3.0** - Documentação da API
- **Micrometer** - Métricas e observabilidade

## 🎯 Casos de Uso Ideais

- **Ambientes regulamentados** (seguros, bancos)
- **Processos críticos** onde consistência é fundamental
- **Necessidade de auditoria detalhada**
- **Baixa tolerância a inconsistências de dados**
- **Cenários onde rollback é obrigatório**

## ✅ Vantagens

- ✅ **Consistência Garantida**: Dados sempre íntegros através de compensação
- ✅ **Auditoria Completa**: Histórico detalhado de todas as operações
- ✅ **Recuperação Determinística**: Rollback automático em caso de falha
- ✅ **Visibilidade**: Acompanhamento em tempo real do processamento
- ✅ **Confiabilidade**: Operações atômicas distribuídas

## ❌ Desvantagens

- ❌ **Complexidade de Implementação**: Saga pattern requer código adicional
- ❌ **Performance**: Processamento síncrono pode ser mais lento
- ❌ **Overhead de Armazenamento**: Logs detalhados de cada step
- ❌ **Latência**: Compensação pode demorar em cenários de falha

## 🔄 Fluxo de Processamento

### Saga Steps
1. **Validar Segurado** - Verificação de dados e apólice
2. **Criar Sinistro** - Criação do registro inicial
3. **Consultar Detran** - Obtenção de dados do veículo
4. **Processar Dados** - Análise e validação
5. **Analisar Sinistro** - Decisão de aprovação
6. **Notificar Segurado** - Comunicação do resultado
7. **Processar Pagamento** - Execução financeira

### Compensação Automática
Em caso de falha em qualquer step, o sistema executa automaticamente a compensação de todos os steps já executados, garantindo a consistência dos dados.

---

**⚖️ Perfeito para ambientes que exigem máxima consistência e auditoria!**