# 📖 INTRODUÇÃO À ARQUITETURA - PARTE 3
## Configuração do Ambiente de Desenvolvimento

### 🎯 **OBJETIVOS DESTA PARTE**
- Configurar ambiente local completo
- Verificar pré-requisitos e dependências
- Executar a aplicação pela primeira vez
- Validar funcionamento dos componentes principais

---

## 📋 **PRÉ-REQUISITOS**

### **🔧 Ferramentas Necessárias**

#### **1. Java Development Kit (JDK)**
```bash
# Verificar versão instalada
java -version

# Versão necessária: JDK 17 ou superior
# Exemplo de saída esperada:
# openjdk version "17.0.2" 2022-01-18
# OpenJDK Runtime Environment (build 17.0.2+8-Ubuntu-120.04)
```

**Download:** [OpenJDK 17](https://adoptium.net/temurin/releases/?version=17)

#### **2. Apache Maven**
```bash
# Verificar versão instalada
mvn -version

# Versão necessária: Maven 3.8 ou superior
# Exemplo de saída esperada:
# Apache Maven 3.8.6
# Maven home: /usr/share/maven
# Java version: 17.0.2
```

**Download:** [Apache Maven](https://maven.apache.org/download.cgi)

#### **3. Docker & Docker Compose**
```bash
# Verificar Docker
docker --version
# Docker version 20.10.17, build 100c701

# Verificar Docker Compose
docker-compose --version
# docker-compose version 1.29.2, build 5becea4c
```

**Download:** [Docker Desktop](https://www.docker.com/products/docker-desktop/)

#### **4. Git**
```bash
# Verificar Git
git --version
# git version 2.34.1
```

### **💻 IDE Recomendada**

#### **IntelliJ IDEA ou VS Code**
- **IntelliJ IDEA**: [Download](https://www.jetbrains.com/idea/)
- **VS Code**: [Download](https://code.visualstudio.com/)

**Plugins Essenciais:**
- Spring Boot Tools
- Lombok
- Docker
- Database Navigator

---

## 🚀 **CONFIGURAÇÃO DO AMBIENTE**

### **1. 📥 Clonar o Repositório**

```bash
# Navegar para diretório de trabalho
cd ~/workspace

# Clonar o projeto (substitua pela URL real)
git clone <repository-url>
cd app-arquitetura-hibrida

# Verificar estrutura do projeto
ls -la
```

**Estrutura esperada:**
```
app-arquitetura-hibrida/
├── docker-compose.yml
├── pom.xml
├── src/
├── docker/
└── README.md
```

### **2. 🐳 Configurar Infraestrutura com Docker**

#### **Verificar docker-compose.yml:**
```yaml
# Serviços que serão iniciados:
services:
  postgres-write:      # PostgreSQL para Event Store (porta 5435)
  postgres-read:       # PostgreSQL para Projections (porta 5436)
  redis:              # Cache Redis (porta 6379)
  kafka:              # Message Broker (porta 9092)
  zookeeper:          # Kafka dependency (porta 2181)
  prometheus:         # Monitoramento (porta 9090)
```

#### **Iniciar serviços:**
```bash
# Subir todos os serviços em background
docker-compose up -d

# Verificar se todos os serviços estão rodando
docker-compose ps

# Saída esperada:
# NAME                    STATUS
# postgres-write          Up (healthy)
# postgres-read           Up (healthy)
# redis                   Up (healthy)
# kafka                   Up (healthy)
# zookeeper              Up (healthy)
# prometheus             Up (healthy)
```

#### **Verificar logs (se necessário):**
```bash
# Ver logs de todos os serviços
docker-compose logs

# Ver logs de um serviço específico
docker-compose logs postgres-write
docker-compose logs kafka
```

### **3. 🗃️ Verificar Bancos de Dados**

#### **PostgreSQL Write (Event Store):**
```bash
# Conectar no banco de eventos
docker exec -it postgres-write psql -U postgres -d sinistros_eventstore

# Verificar schemas e tabelas
\dn
\dt eventstore.*

# Saída esperada:
# Schema: eventstore
# Tables: events, snapshots, projection_tracking, etc.

# Sair do psql
\q
```

#### **PostgreSQL Read (Projections):**
```bash
# Conectar no banco de projeções
docker exec -it postgres-read psql -U postgres -d sinistros_projections

# Verificar schemas e tabelas
\dn
\dt projections.*

# Saída esperada:
# Schema: projections
# Tables: sinistro_view, segurado_view, etc.

# Sair do psql
\q
```

#### **Redis:**
```bash
# Conectar no Redis
docker exec -it redis redis-cli

# Testar conexão
ping
# PONG

# Verificar configuração
info server

# Sair do redis-cli
exit
```

### **4. 📦 Compilar o Projeto**

```bash
# Limpar e compilar
mvn clean compile

# Saída esperada (sem erros):
# [INFO] BUILD SUCCESS
# [INFO] Total time: XX.XXX s

# Executar testes (opcional)
mvn test

# Verificar se há falhas nos testes
# [INFO] Tests run: XX, Failures: 0, Errors: 0, Skipped: 0
```

---

## 🏃 **EXECUTAR A APLICAÇÃO**

### **1. 🚀 Iniciar via Maven**

```bash
# Executar aplicação Spring Boot
mvn spring-boot:run

# Aguardar inicialização completa
# Saída esperada:
# Started ArquiteturaHibridaApplication in X.XXX seconds
```

### **2. 🚀 Iniciar via IDE**

#### **IntelliJ IDEA:**
1. Abrir projeto: `File > Open > app-arquitetura-hibrida`
2. Aguardar indexação do Maven
3. Localizar: `src/main/java/com/seguradora/hibrida/ArquiteturaHibridaApplication.java`
4. Clicar com botão direito > `Run 'ArquiteturaHibridaApplication'`

#### **VS Code:**
1. Abrir pasta: `File > Open Folder > app-arquitetura-hibrida`
2. Instalar extensão "Extension Pack for Java"
3. Pressionar `F5` ou usar Command Palette: `Java: Run`

### **3. ✅ Verificar Inicialização**

#### **Logs de Inicialização:**
```bash
# Procurar por estas mensagens nos logs:
2024-01-15 10:30:15.123  INFO --- [main] c.s.h.ArquiteturaHibridaApplication : Starting ArquiteturaHibridaApplication
2024-01-15 10:30:16.456  INFO --- [main] c.s.h.config.DataSourceConfiguration : Write DataSource configurado: jdbc:postgresql://localhost:5435/sinistros_eventstore
2024-01-15 10:30:16.789  INFO --- [main] c.s.h.config.DataSourceConfiguration : Read DataSource configurado: jdbc:postgresql://localhost:5436/sinistros_projections
2024-01-15 10:30:17.012  INFO --- [main] c.s.h.eventbus.config.EventBusConfiguration : Event Bus configurado: SimpleEventBus
2024-01-15 10:30:17.345  INFO --- [main] c.s.h.ArquiteturaHibridaApplication : Started ArquiteturaHibridaApplication in 3.456 seconds
```

#### **Porta da Aplicação:**
- **URL Base**: `http://localhost:8083/api/v1`
- **Contexto**: `/api/v1` (configurado no application.yml)

---

## 🔍 **VALIDAÇÃO DO AMBIENTE**

### **1. 🏥 Health Checks**

#### **Health Check Geral:**
```bash
# Verificar saúde da aplicação
curl http://localhost:8083/api/v1/actuator/health

# Resposta esperada:
{
  "status": "UP",
  "components": {
    "writeDataSource": {"status": "UP"},
    "readDataSource": {"status": "UP"},
    "redis": {"status": "UP"},
    "eventStore": {"status": "UP"},
    "projections": {"status": "UP"}
  }
}
```

#### **Health Check CQRS:**
```bash
# Verificar saúde específica do CQRS
curl http://localhost:8083/api/v1/actuator/cqrs

# Resposta esperada:
{
  "status": "HEALTHY",
  "commandSide": {"status": "UP", "events": 0},
  "querySide": {"status": "UP", "projections": 2},
  "lag": 0,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### **2. 📊 APIs de Monitoramento**

#### **Métricas Prometheus:**
```bash
# Verificar métricas
curl http://localhost:8083/api/v1/actuator/prometheus | head -20

# Procurar por métricas específicas:
# eventstore_events_total
# projections_active_count
# command_bus_commands_total
```

#### **Informações da Aplicação:**
```bash
# Informações gerais
curl http://localhost:8083/api/v1/actuator/info

# Resposta esperada:
{
  "app": {
    "name": "app-arquitetura-hibrida",
    "version": "1.0.0",
    "description": "Sistema de Sinistros com Arquitetura Híbrida"
  },
  "architecture": {
    "patterns": ["Event Sourcing", "CQRS", "DDD"],
    "components": {
      "eventstore": "PostgreSQL",
      "projections": "PostgreSQL",
      "cache": "Redis",
      "messaging": "simple"
    }
  }
}
```

### **3. 📖 Documentação da API**

#### **Swagger UI:**
```bash
# Abrir no navegador:
http://localhost:8083/api/v1/swagger-ui.html

# Ou acessar JSON da documentação:
curl http://localhost:8083/api/v1/v3/api-docs
```

**Endpoints disponíveis:**
- **Command Bus**: `/actuator/commandbus`
- **Event Bus**: `/actuator/eventbus`
- **Projections**: `/actuator/projections`
- **Event Store**: `/actuator/eventstore`

### **4. 🧪 Testes Básicos**

#### **Testar Command Bus:**
```bash
# Verificar handlers registrados
curl http://localhost:8083/api/v1/actuator/commandbus/handlers

# Resposta esperada:
{
  "registeredHandlers": 1,
  "handlers": [
    {
      "commandType": "TestCommand",
      "handlerClass": "TestCommandHandler"
    }
  ]
}
```

#### **Testar Event Bus:**
```bash
# Verificar handlers de eventos
curl http://localhost:8083/api/v1/actuator/eventbus/handlers

# Resposta esperada:
{
  "registeredHandlers": 3,
  "handlers": [
    {
      "eventType": "SinistroEvent",
      "handlerClass": "SinistroEventHandler"
    },
    {
      "eventType": "TestEvent", 
      "handlerClass": "TestEventHandler"
    }
  ]
}
```

#### **Testar Projections:**
```bash
# Listar projeções
curl http://localhost:8083/api/v1/actuator/projections

# Resposta esperada:
{
  "totalProjections": 2,
  "activeProjections": 2,
  "projections": [
    {
      "name": "SinistroProjectionHandler",
      "status": "ACTIVE",
      "lastProcessedEventId": 0,
      "eventsProcessed": 0,
      "eventsFailed": 0
    },
    {
      "name": "SeguradoProjectionHandler", 
      "status": "ACTIVE",
      "lastProcessedEventId": 0,
      "eventsProcessed": 0,
      "eventsFailed": 0
    }
  ]
}
```

---

## 🛠️ **TROUBLESHOOTING**

### **❌ Problemas Comuns**

#### **1. Porta já em uso:**
```bash
# Erro: Port 8083 is already in use
# Solução: Verificar processo usando a porta
lsof -i :8083

# Ou alterar porta no application.yml:
server:
  port: 8084
```

#### **2. Docker services não iniciam:**
```bash
# Verificar logs detalhados
docker-compose logs postgres-write

# Recriar containers se necessário
docker-compose down
docker-compose up -d --force-recreate
```

#### **3. Erro de conexão com banco:**
```bash
# Verificar se PostgreSQL está rodando
docker-compose ps postgres-write

# Testar conexão manual
docker exec -it postgres-write pg_isready -U postgres
```

#### **4. Erro de compilação Maven:**
```bash
# Limpar cache do Maven
mvn clean

# Verificar versão do Java
mvn -version

# Recompilar com debug
mvn clean compile -X
```

### **🔧 Comandos Úteis**

#### **Reiniciar ambiente completo:**
```bash
# Parar aplicação (Ctrl+C)
# Parar Docker services
docker-compose down

# Limpar volumes (CUIDADO: apaga dados)
docker-compose down -v

# Recriar tudo
docker-compose up -d
mvn clean compile
mvn spring-boot:run
```

#### **Verificar recursos do sistema:**
```bash
# Uso de memória dos containers
docker stats

# Espaço em disco
df -h

# Processos Java
jps -v
```

---

## 📚 **CONFIGURAÇÕES OPCIONAIS**

### **🔧 Configurações de IDE**

#### **IntelliJ IDEA:**
```properties
# File > Settings > Build > Compiler > Annotation Processors
# ✅ Enable annotation processing (para Lombok)

# File > Settings > Editor > Code Style > Java
# ✅ Importar code style do projeto (se disponível)
```

#### **VS Code:**
```json
// .vscode/settings.json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.compile.nullAnalysis.mode": "automatic",
  "spring-boot.ls.problem.application-properties.enabled": true
}
```

### **🐳 Docker Desktop Settings:**
```yaml
# Recursos recomendados:
Memory: 4GB mínimo (8GB recomendado)
CPUs: 2 cores mínimo (4 cores recomendado)
Disk: 20GB disponível
```

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [Spring Boot Getting Started](https://spring.io/guides/gs/spring-boot/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Maven Getting Started](https://maven.apache.org/guides/getting-started/)
- [PostgreSQL Docker](https://hub.docker.com/_/postgres)

### **📖 Próximas Partes:**
- **Parte 4**: Fluxos de Dados e Comunicação entre Componentes
- **Parte 5**: Exercícios Práticos e Checkpoint de Aprendizado

---

**📝 Parte 3 de 5 - Configuração do Ambiente**  
**⏱️ Tempo estimado**: 60 minutos  
**🎯 Próximo**: [Parte 4 - Fluxos de Dados](./01-introducao-arquitetura-parte-4.md)