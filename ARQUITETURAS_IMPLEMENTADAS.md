# 🏗️ Arquiteturas Implementadas - Sistema de Sinistros

Este repositório contém a implementação inicial de três opções arquiteturais para o sistema de gestão de sinistros, cada uma com características e trade-offs específicos.

## 📁 Estrutura do Projeto

```
├── detran-simulator/              # Simulador do sistema legado Detran
├── app-arquitetura-resiliente/    # Opção 1: Foco em Resiliência
├── app-arquitetura-consistente/   # Opção 2: Foco em Consistência
├── app-arquitetura-hibrida/       # Opção 3: Abordagem Híbrida
└── doc/                          # Documentação das arquiteturas
```

## 🎯 Visão Geral das Opções

### 🛡️ Opção 1: Arquitetura Resiliente
**Porta: 8081** | **Foco: Disponibilidade e Performance**

```bash
cd app-arquitetura-resiliente
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Características:**
- ⚡ Circuit Breaker para integração com Detran
- 🗄️ Cache Distribuído (Redis) para performance
- 📨 Processamento Assíncrono com Kafka
- 🔄 Retry Automático com backoff exponencial

**Ideal para:**
- Ambientes com alta instabilidade do Detran
- Volume alto de consultas simultâneas
- SLA rigoroso de disponibilidade
- Tolerância a eventual consistência

---

### ⚖️ Opção 2: Arquitetura Consistente
**Porta: 8082** | **Foco: Integridade e Auditoria**

```bash
cd app-arquitetura-consistente
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Características:**
- 📋 Saga Pattern para transações distribuídas
- ⚖️ Consistência Garantida através de compensação
- 📊 Auditoria Completa de todas as operações
- 🔄 Rollback Automático em caso de falha

**Ideal para:**
- Ambientes regulamentados (seguros, bancos)
- Processos críticos onde consistência é fundamental
- Necessidade de auditoria detalhada
- Baixa tolerância a inconsistências

---

### 🔄 Opção 3: Arquitetura Híbrida
**Porta: 8083** | **Foco: Flexibilidade e Escalabilidade**

```bash
cd app-arquitetura-hibrida
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Características:**
- 📚 Event Sourcing para histórico completo
- 🔄 CQRS para separação Command/Query
- ⚡ Processamento Híbrido (sync + async)
- 📊 Projections Otimizadas para consultas

**Ideal para:**
- Sistemas com alta carga de leitura e escrita
- Necessidade de auditoria e compliance
- Requisitos de escalabilidade horizontal
- Análise histórica e replay de eventos

## 🚀 Quick Start

### 1. Pré-requisitos
```bash
# Verificar versões
java -version    # Java 21+
mvn -version     # Maven 3.8+
docker --version # Docker (opcional)
```

### 2. Iniciar Simulador Detran
```bash
cd detran-simulator
mvn spring-boot:run
# Acesse: http://localhost:8080/detran-api/swagger-ui.html
```

### 3. Escolher e Executar uma Arquitetura
```bash
# Opção 1 - Resiliente
cd app-arquitetura-resiliente
mvn spring-boot:run -Dspring-boot.run.profiles=local
# Acesse: http://localhost:8081/api/v1/swagger-ui.html

# Opção 2 - Consistente  
cd app-arquitetura-consistente
mvn spring-boot:run -Dspring-boot.run.profiles=local
# Acesse: http://localhost:8082/api/v1/swagger-ui.html

# Opção 3 - Híbrida
cd app-arquitetura-hibrida
mvn spring-boot:run -Dspring-boot.run.profiles=local
# Acesse: http://localhost:8083/api/v1/swagger-ui.html
```

## 📊 Comparativo das Arquiteturas

| Aspecto | Resiliente | Consistente | Híbrida |
|---------|------------|-------------|---------|
| **Disponibilidade** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Consistência** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Performance** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Complexidade** | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Auditoria** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Escalabilidade** | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

## 🔧 Configurações Comuns

### Perfis Disponíveis
- **local**: H2 em memória, sem dependências externas
- **dev**: PostgreSQL, Redis, Kafka (desenvolvimento)
- **prod**: Configuração completa para produção

### Dependências Externas (Opcional)
```bash
# Docker Compose para dependências completas
docker-compose up -d postgres redis kafka
```

### Variáveis de Ambiente
```bash
# Database
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Redis (apenas Resiliente e Híbrida)
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka (apenas Resiliente e Híbrida)
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Detran Simulator
DETRAN_BASE_URL=http://localhost:8080/detran-api
```

## 📚 Documentação Detalhada

### Documentos de Arquitetura
- [📋 Requisitos Funcionais](doc/01-requisitos-funcionais.md)
- [🛡️ Arquitetura Resiliente](doc/02-arquitetura-opcao1-resiliente.md)
- [⚖️ Arquitetura Consistente](doc/03-arquitetura-opcao2-consistencia.md)
- [🔄 Arquitetura Híbrida](doc/04-arquitetura-opcao3-hibrida.md)
- [📊 Comparativo](doc/05-comparativo-arquiteturas.md)

### APIs Documentadas
Cada aplicação possui documentação OpenAPI completa:
- **Resiliente**: http://localhost:8081/api/v1/swagger-ui.html
- **Consistente**: http://localhost:8082/api/v1/swagger-ui.html
- **Híbrida**: http://localhost:8083/api/v1/swagger-ui.html

## 🧪 Testes

### Testes Unitários
```bash
# Executar em cada projeto
mvn test
```

### Testes de Integração
```bash
# Com Testcontainers
mvn verify
```

### Testes com Postman
```bash
# Collections disponíveis em detran-simulator/postman/
# Importar no Postman e testar cada arquitetura
```

## 📈 Monitoramento

### Health Checks
- **Resiliente**: http://localhost:8081/api/v1/sistema/health
- **Consistente**: http://localhost:8082/api/v1/sistema/health
- **Híbrida**: http://localhost:8083/api/v1/sistema/health

### Métricas Prometheus
- **Resiliente**: http://localhost:8081/api/v1/actuator/prometheus
- **Consistente**: http://localhost:8082/api/v1/actuator/prometheus
- **Híbrida**: http://localhost:8083/api/v1/actuator/prometheus

### Endpoints Específicos
- **Circuit Breakers**: `/actuator/circuitbreakers` (Resiliente)
- **Sagas**: `/actuator/saga` (Consistente)
- **Event Store**: `/actuator/eventstore` (Híbrida)

## 🎯 Próximos Passos

### Fase 1: Infraestrutura ✅
- [x] Estrutura base dos projetos
- [x] Configurações de infraestrutura
- [x] Health checks e monitoramento
- [x] Documentação OpenAPI

### Fase 2: Implementação (Próxima)
- [ ] Modelos de domínio
- [ ] Regras de negócio
- [ ] Integração com Detran
- [ ] Testes automatizados

### Fase 3: Avançado
- [ ] Implementação completa dos padrões
- [ ] Testes de carga
- [ ] Observabilidade avançada
- [ ] Deploy automatizado

## 🤝 Contribuição

1. Escolha uma das arquiteturas para implementar
2. Siga os padrões definidos na documentação
3. Implemente testes para suas funcionalidades
4. Atualize a documentação conforme necessário

## 📞 Suporte

- **Documentação**: Consulte os arquivos em `/doc/`
- **APIs**: Use o Swagger UI de cada aplicação
- **Testes**: Execute as collections do Postman
- **Monitoramento**: Verifique os health checks

---

**🚀 Três caminhos, uma meta: o melhor sistema de sinistros para cada cenário!**