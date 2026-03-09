# ⚙️ CONFIGURAÇÕES E DATASOURCES
## Roteiro Técnico - Módulo 09

### 🎯 **OBJETIVO DO MÓDULO**
Dominar a configuração completa de múltiplos DataSources, propriedades de ambiente e estratégias de configuração para arquitetura híbrida, preparando os analistas para configurar ambientes produtivos e resolver problemas de configuração.

---

## 📚 **ESTRUTURA DO MÓDULO**

### **📋 Partes Disponíveis:**

| **Parte** | **Tópico** | **Duração** | **Foco Principal** |
|-----------|------------|-------------|-------------------|
| **[Parte 1](./09-configuracoes-parte-1.md)** | Fundamentos de Configuração | 45 min | Conceitos base e propriedades |
| **[Parte 2](./09-configuracoes-parte-2.md)** | DataSources Write e Read | 50 min | Configuração de múltiplos bancos |
| **[Parte 3](./09-configuracoes-parte-3.md)** | Cache e Message Broker | 60 min | Redis e Kafka/Simple Event Bus |
| **[Parte 4](./09-configuracoes-parte-4.md)** | Health Checks e Monitoramento | 50 min | Observabilidade e métricas |
| **[Parte 5](./09-configuracoes-parte-5.md)** | Configurações Avançadas | 60 min | Performance e troubleshooting |

**⏱️ Duração Total:** 4 horas e 45 minutos

---

## 🎓 **OBJETIVOS DE APRENDIZADO**

### **Ao completar este módulo, você será capaz de:**

#### **⚙️ Configuração de DataSources:**
- ✅ Configurar múltiplos DataSources (Write/Read)
- ✅ Otimizar pools de conexão HikariCP
- ✅ Implementar separação CQRS em nível de dados
- ✅ Configurar Flyway para múltiplos bancos

#### **🚀 Cache e Messaging:**
- ✅ Configurar Redis para cache multi-level
- ✅ Implementar Event Bus com Kafka
- ✅ Configurar fallback para Simple Event Bus
- ✅ Otimizar configurações de performance

#### **🏥 Monitoramento e Health:**
- ✅ Implementar health checks customizados
- ✅ Configurar métricas com Micrometer
- ✅ Configurar Actuator endpoints
- ✅ Implementar alertas e observabilidade

#### **🔧 Configurações Avançadas:**
- ✅ Configurar ambientes específicos (dev/test/prod)
- ✅ Implementar configurações de performance
- ✅ Resolver problemas de configuração
- ✅ Aplicar boas práticas de segurança

---

## 📋 **PRÉ-REQUISITOS**

### **🔧 Conhecimentos Técnicos:**
- **Spring Boot**: Configuração avançada e profiles
- **PostgreSQL**: Configuração e otimização
- **Redis**: Conceitos básicos de cache
- **Docker**: Configuração de containers
- **YAML**: Sintaxe e estruturação

### **📚 Módulos Anteriores:**
- **[Módulo 01](./01-introducao-arquitetura-README.md)**: Fundamentos da arquitetura
- **[Módulo 02](./02-event-sourcing-README.md)**: Event Store e persistência
- **[Módulo 03](./03-cqrs-README.md)**: Separação Command/Query
- **[Módulo 06](./06-event-bus-README.md)**: Event Bus e comunicação

### **🛠️ Ferramentas Necessárias:**
- **Docker Desktop**: Para infraestrutura local
- **IDE**: IntelliJ IDEA ou VS Code
- **PostgreSQL Client**: Para testes de conexão
- **Redis CLI**: Para testes de cache
- **Postman**: Para testes de APIs

---

## 🚀 **ROTEIRO DE ESTUDOS**

### **📅 Cronograma Sugerido:**

#### **Dia 1 - Manhã (2h 30min):**
- **Parte 1**: Fundamentos de Configuração (45 min)
- **Parte 2**: DataSources Write e Read (50 min)
- **Parte 3**: Cache e Message Broker (60 min)
- **Intervalo**: 15 min entre cada parte

#### **Dia 1 - Tarde (2h 15min):**
- **Parte 4**: Health Checks e Monitoramento (50 min)
- **Parte 5**: Configurações Avançadas (60 min)
- **Revisão**: 25 min para consolidação

### **📖 Metodologia de Estudo:**

#### **Para cada parte:**
1. **Teoria e Conceitos** (40% do tempo)
   - Compreender princípios de configuração
   - Entender padrões implementados
   - Relacionar com arquitetura geral

2. **Implementação Prática** (50% do tempo)
   - Examinar configurações existentes
   - Testar diferentes cenários
   - Modificar configurações localmente

3. **Troubleshooting** (10% do tempo)
   - Simular problemas comuns
   - Aplicar soluções apresentadas
   - Documentar aprendizados

---

## ✅ **CHECKPOINTS DE VALIDAÇÃO**

### **🎯 Checkpoint 1 - Após Parte 2:**
**Validar configuração de DataSources**

#### **Exercícios Práticos:**
- [ ] Configurar novo DataSource de teste
- [ ] Modificar pool de conexões
- [ ] Testar conexões Write e Read separadamente
- [ ] Verificar health checks dos bancos

#### **Critérios de Aprovação:**
- Aplicação inicia sem erros de conexão
- Health checks mostram status UP para ambos os bancos
- Pools de conexão configurados adequadamente

### **🎯 Checkpoint 2 - Após Parte 4:**
**Validar monitoramento e observabilidade**

#### **Exercícios Práticos:**
- [ ] Implementar health check customizado
- [ ] Configurar métrica personalizada
- [ ] Testar alertas básicos
- [ ] Verificar endpoints do Actuator

#### **Critérios de Aprovação:**
- Health checks customizados funcionando
- Métricas sendo coletadas corretamente
- Actuator endpoints acessíveis e seguros

### **🎯 Checkpoint Final - Após Parte 5:**
**Validação completa do módulo**

#### **Projeto Prático:**
Configurar ambiente completo com:
- [ ] DataSources otimizados para produção
- [ ] Cache Redis configurado
- [ ] Event Bus com Kafka
- [ ] Monitoramento completo
- [ ] Configurações por ambiente (dev/prod)

#### **Entregável:**
- Arquivo `application-custom.yml` funcional
- Documentação das configurações aplicadas
- Relatório de testes de performance básicos

---

## 🔗 **RECURSOS DE APOIO**

### **📚 Configurações de Referência:**
- **Write DataSource**: `com.seguradora.hibrida.config.datasource.WriteDataSourceConfiguration`
- **Read DataSource**: `com.seguradora.hibrida.config.datasource.ReadDataSourceConfiguration`
- **Redis**: `com.seguradora.hibrida.config.RedisConfiguration`
- **Kafka**: `com.seguradora.hibrida.eventbus.config.KafkaEventBusConfiguration`

### **🔧 Arquivos de Configuração:**
- **Principal**: `src/main/resources/application.yml`
- **Desenvolvimento**: `src/main/resources/application-dev.yml`
- **Produção**: `src/main/resources/application-prod.yml`
- **Testes**: `src/main/resources/application-test.yml`

### **🛠️ Ferramentas de Diagnóstico:**
- **Health Checks**: `http://localhost:8083/api/v1/actuator/health`
- **Métricas**: `http://localhost:8083/api/v1/actuator/metrics`
- **Configurações**: `http://localhost:8083/api/v1/actuator/configprops`
- **Diagnóstico**: `http://localhost:8083/api/v1/actuator/diagnostic`

### **📊 Monitoramento:**
- **Prometheus**: `http://localhost:9090`
- **Connection Pools**: Via JMX ou Actuator
- **Cache Statistics**: Redis INFO command
- **Event Bus**: Métricas específicas por implementação

---

## 🎯 **CENÁRIOS PRÁTICOS**

### **🏢 Cenário 1: Ambiente de Desenvolvimento**
**Objetivo:** Configurar ambiente local otimizado para desenvolvimento

#### **Configurações Específicas:**
- DataSources com pools menores
- Cache desabilitado para debugging
- Logs detalhados habilitados
- Todos os endpoints Actuator expostos

#### **Exercício:**
Criar `application-dev-local.yml` personalizado

### **🏭 Cenário 2: Ambiente de Produção**
**Objetivo:** Configurar ambiente produtivo com alta performance

#### **Configurações Específicas:**
- Pools de conexão otimizados
- Cache Redis com clustering
- Kafka com múltiplas partições
- Monitoramento restrito e seguro

#### **Exercício:**
Adaptar configurações para cenário de alta carga

### **🧪 Cenário 3: Ambiente de Testes**
**Objetivo:** Configurar ambiente para testes automatizados

#### **Configurações Específicas:**
- H2 in-memory para velocidade
- Event Bus simples (não Kafka)
- Métricas desabilitadas
- Configurações mínimas

#### **Exercício:**
Criar profile de teste otimizado

---

## 🔧 **TROUBLESHOOTING GUIDE**

### **❌ Problemas Comuns:**

#### **1. Erro de Conexão com Banco:**
```yaml
# Sintomas: Connection refused, timeout
# Solução: Verificar configurações de rede e credenciais
spring:
  datasource:
    write:
      url: jdbc:postgresql://localhost:5435/eventstore
      hikari:
        connection-timeout: 30000
        validation-timeout: 5000
```

#### **2. Pool de Conexões Esgotado:**
```yaml
# Sintomas: HikariPool - Connection is not available
# Solução: Ajustar tamanhos do pool
spring:
  datasource:
    write:
      hikari:
        maximum-pool-size: 20
        minimum-idle: 5
        leak-detection-threshold: 60000
```

#### **3. Cache Redis Indisponível:**
```yaml
# Sintomas: RedisConnectionException
# Solução: Configurar fallback e timeouts
spring:
  redis:
    timeout: 2000ms
    lettuce:
      pool:
        max-wait: 2000ms
  cache:
    type: none  # Fallback sem cache
```

#### **4. Kafka Não Conecta:**
```yaml
# Sintomas: TimeoutException, NetworkException
# Solução: Fallback para SimpleEventBus
app:
  event-bus:
    type: simple  # Fallback
    simple:
      thread-pool:
        core-size: 5
```

---

## 📊 **MÉTRICAS DE SUCESSO**

### **🎯 Indicadores de Performance:**

#### **DataSources:**
- **Connection Pool Usage**: < 80% em operação normal
- **Connection Timeout**: < 5 segundos
- **Query Response Time**: < 100ms para consultas simples

#### **Cache:**
- **Hit Rate**: > 80% para consultas frequentes
- **Response Time**: < 10ms para cache hits
- **Memory Usage**: < 70% da memória alocada

#### **Event Bus:**
- **Message Throughput**: > 1000 msgs/segundo
- **Processing Latency**: < 50ms
- **Error Rate**: < 1%

#### **Monitoramento:**
- **Health Check Response**: < 2 segundos
- **Metrics Collection**: < 1% overhead
- **Alert Response Time**: < 30 segundos

---

## 🏆 **CERTIFICAÇÃO DE CONCLUSÃO**

### **📜 Critérios para Aprovação:**

#### **✅ Conhecimento Teórico:**
- [ ] Compreensão de padrões de configuração Spring Boot
- [ ] Entendimento de otimizações de DataSource
- [ ] Conhecimento de estratégias de cache
- [ ] Compreensão de monitoramento e observabilidade

#### **✅ Habilidades Práticas:**
- [ ] Configuração de múltiplos DataSources
- [ ] Implementação de cache Redis
- [ ] Configuração de Event Bus (Simple/Kafka)
- [ ] Implementação de health checks customizados

#### **✅ Resolução de Problemas:**
- [ ] Diagnóstico de problemas de conexão
- [ ] Otimização de performance
- [ ] Configuração por ambiente
- [ ] Implementação de fallbacks

### **🎓 Competências Desenvolvidas:**
- **Configuração Avançada**: Domínio completo de configurações Spring Boot
- **Performance Tuning**: Otimização de recursos e conexões
- **Observabilidade**: Implementação de monitoramento efetivo
- **Troubleshooting**: Resolução autônoma de problemas

### **🎯 Próximo Passo:**
Após aprovação, você está preparado para:
- **[Módulo 10 - Testes](./10-testes-README.md)**

---

**📚 Módulo elaborado por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**📅 Última Atualização:** Março 2024  
**⏱️ Duração Total:** 4h 45min  
**🏆 Pré-requisito para:** Módulos de Testes e Monitoramento