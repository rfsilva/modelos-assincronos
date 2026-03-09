# 🚀 BRIEFING POCKET - ARQUITETURA HÍBRIDA
## Guia Rápido para Analistas Java Junior

### 🎯 **VISÃO GERAL**
Este projeto implementa uma **arquitetura híbrida** combinando **Event Sourcing + CQRS + DDD** para o domínio de seguros, especificamente para gestão de sinistros. É uma solução robusta que separa comandos (escrita) de consultas (leitura) usando eventos como fonte da verdade.

---

## 🏗️ **CONCEITOS FUNDAMENTAIS**

### **🔄 CQRS (Command Query Responsibility Segregation)**
- **Command Side**: Processa comandos, gera eventos, persiste no Event Store
- **Query Side**: Consome eventos, atualiza projeções, serve consultas otimizadas
- **Separação física**: Bancos diferentes para escrita e leitura

### **💾 Event Sourcing**
- **Eventos como fonte da verdade**: Estado é reconstruído a partir do histórico
- **Event Store**: PostgreSQL com particionamento por data
- **Snapshots**: Otimização para agregados com muitos eventos
- **Replay**: Capacidade de reconstruir estado histórico

### **🏗️ Domain-Driven Design**
- **Agregados**: Entidades que garantem consistência (ex: Sinistro)
- **Bounded Contexts**: Fronteiras claras de responsabilidade
- **Business Rules**: Validações de negócio encapsuladas
- **Domain Events**: Comunicação entre contextos

---

## 📁 **ESTRUTURA DO PROJETO**

```
src/main/java/com/seguradora/hibrida/
├── aggregate/          # Agregados e repositórios
├── command/           # Command Bus e handlers
├── eventbus/          # Event Bus e processamento
├── eventstore/        # Persistência de eventos
├── projection/        # Projeções e handlers
├── query/            # APIs de consulta
├── cqrs/             # Métricas e saúde CQRS
└── config/           # Configurações e DataSources
```

### **🔧 Componentes Principais**
- **Command Bus**: Roteamento e execução de comandos
- **Event Bus**: Publicação e consumo de eventos
- **Event Store**: Armazenamento otimizado de eventos
- **Projection Handlers**: Atualização de views de leitura
- **Aggregate Repository**: Reconstrução de agregados

---

## 🚀 **FLUXO BÁSICO DE OPERAÇÃO**

### **📝 Escrita (Command Side)**
1. **API REST** recebe requisição
2. **Command** é criado e validado
3. **Command Handler** processa comando
4. **Aggregate** aplica business rules
5. **Eventos** são persistidos no Event Store
6. **Event Bus** publica eventos

### **📊 Leitura (Query Side)**
1. **Projection Handler** consome evento
2. **Projeção** é atualizada no banco de leitura
3. **Query API** serve dados otimizados
4. **Cache Redis** acelera consultas frequentes

---

## 🛠️ **TECNOLOGIAS UTILIZADAS**

### **🔧 Core**
- **Java 17** + **Spring Boot 3.x**
- **PostgreSQL** (Event Store + Projeções)
- **Redis** (Cache e sessões)
- **Kafka** (Event Bus distribuído - opcional)

### **📊 Monitoramento**
- **Micrometer** + **Prometheus** (Métricas)
- **Spring Actuator** (Health checks)
- **Logback** (Logging estruturado)

### **🧪 Testes**
- **JUnit 5** + **Testcontainers**
- **WireMock** (Mocks de APIs externas)
- **Arquillian** (Testes de integração)

---

## 📚 **MÓDULOS DE APRENDIZADO**

### **🎓 Roteiro Completo (44h)**
| Módulo | Tópico | Duração | Essencial |
|--------|--------|---------|-----------|
| **01** | 📖 Introdução à Arquitetura | 5h | ⭐⭐⭐ |
| **02** | 💾 Event Sourcing | 4h20 | ⭐⭐⭐ |
| **03** | 🔄 CQRS | 4h10 | ⭐⭐⭐ |
| **04** | 🏗️ Domain-Driven Design | 3h25 | ⭐⭐ |
| **05** | ⚡ Command Bus | 3h15 | ⭐⭐⭐ |
| **06** | 📡 Event Bus | 3h15 | ⭐⭐⭐ |
| **07** | 🔄 Projections | 3h20 | ⭐⭐⭐ |
| **08** | 🧩 Agregados | 3h50 | ⭐⭐ |
| **09** | ⚙️ Configurações | 4h45 | ⭐⭐ |
| **10** | 🧪 Testes | 3h40 | ⭐ |
| **11** | 📊 Monitoramento | 3h15 | ⭐ |
| **12** | 🚀 Práticas | 2h45 | ⭐ |

### **🚀 Trilha Rápida (16h)**
Para começar rapidamente, foque nos módulos **essenciais (⭐⭐⭐)**:
1. **Módulo 01**: Fundamentos (5h)
2. **Módulo 02**: Event Sourcing (4h20)
3. **Módulo 03**: CQRS (4h10)
4. **Módulo 05**: Command Bus (3h15)
5. **Módulo 06**: Event Bus (3h15)
6. **Módulo 07**: Projections (3h20)

---

## 🔧 **SETUP RÁPIDO**

### **⚡ Ambiente Local (5 minutos)**
```bash
# 1. Clone e navegue
git clone <repo>
cd app-arquitetura-hibrida

# 2. Suba infraestrutura
docker-compose up -d

# 3. Execute aplicação
mvn spring-boot:run

# 4. Verifique saúde
curl http://localhost:8083/api/v1/actuator/health
```

### **📊 URLs Importantes**
- **Swagger UI**: http://localhost:8083/api/v1/swagger-ui.html
- **Health Check**: http://localhost:8083/api/v1/actuator/health
- **Métricas CQRS**: http://localhost:8083/api/v1/actuator/cqrs
- **Prometheus**: http://localhost:8083/api/v1/actuator/prometheus

---

## 🎯 **PADRÕES DE DESENVOLVIMENTO**

### **📝 Comandos**
```java
// 1. Criar comando
public class CriarSinistroCommand implements Command {
    private final UUID commandId = UUID.randomUUID();
    private final Instant timestamp = Instant.now();
    // ... dados do comando
}

// 2. Implementar handler
@Component
public class CriarSinistroHandler implements CommandHandler<CriarSinistroCommand> {
    public CommandResult handle(CriarSinistroCommand command) {
        // Validar, processar, persistir
    }
}
```

### **📡 Eventos**
```java
// 1. Definir evento
public class SinistroCriadoEvent extends DomainEvent {
    // ... dados do evento
}

// 2. Handler de projeção
@Component
public class SinistroProjectionHandler implements ProjectionHandler<SinistroCriadoEvent> {
    public void handle(SinistroCriadoEvent event) {
        // Atualizar projeção
    }
}
```

### **🧩 Agregados**
```java
@AggregateRoot
public class Sinistro extends AggregateRoot {
    public void criarSinistro(String protocolo, String descricao) {
        // Validar business rules
        applyEvent(new SinistroCriadoEvent(...));
    }
    
    @EventSourcingHandler
    public void on(SinistroCriadoEvent event) {
        // Atualizar estado interno
    }
}
```

---

## 🚨 **PONTOS DE ATENÇÃO**

### **⚠️ Cuidados Importantes**
- **Eventual Consistency**: Query side pode estar atrasado
- **Idempotência**: Handlers devem ser idempotentes
- **Versionamento**: Eventos são imutáveis, versione com cuidado
- **Performance**: Use snapshots para agregados grandes
- **Monitoramento**: Acompanhe lag entre Command e Query

### **🔍 Troubleshooting Comum**
- **Lag alto**: Verificar performance dos projection handlers
- **Eventos perdidos**: Verificar configuração do Event Bus
- **Erro de concorrência**: Implementar retry com backoff
- **Memory leak**: Verificar cleanup de snapshots antigos

---

## 📊 **MÉTRICAS ESSENCIAIS**

### **🎯 KPIs de Saúde**
- **Command/Query Lag**: < 1 segundo (normal), < 5 segundos (aceitável)
- **Event Store Performance**: < 100ms para persistência
- **Projection Throughput**: > 1000 eventos/segundo
- **Error Rate**: < 1% para handlers críticos

### **📈 Monitoramento**
```bash
# Verificar saúde geral
curl http://localhost:8083/api/v1/actuator/health

# Métricas CQRS
curl http://localhost:8083/api/v1/cqrs/metrics

# Status das projeções
curl http://localhost:8083/api/v1/projections/status
```

---

## 🎓 **PRÓXIMOS PASSOS**

### **🚀 Para Começar Hoje**
1. **Configure o ambiente** (5 min)
2. **Explore as APIs** via Swagger (10 min)
3. **Leia Módulo 01** - Introdução (1h)
4. **Execute exemplos** práticos (30 min)

### **📚 Para Aprofundar**
1. **Siga a trilha rápida** (16h essenciais)
2. **Implemente uma funcionalidade** simples
3. **Participe de code reviews** da equipe
4. **Contribua com melhorias** na documentação

### **🏆 Objetivo Final**
Estar preparado para **desenvolver o core de sinistros** com autonomia, seguindo os padrões arquiteturais estabelecidos e contribuindo para a evolução da plataforma.

---

## 📞 **SUPORTE**

### **🤝 Canais de Ajuda**
- **Documentação Completa**: `doc/roteiro/README.md`
- **Código de Exemplo**: Explore os packages `example`
- **Health Checks**: APIs de monitoramento em tempo real
- **Logs**: `logs/hibrida.log` para debugging

### **❓ Dúvidas Frequentes**
- **"Por que não CRUD?"**: Ver Módulo 01, Parte 1
- **"Como debugar eventos?"**: Ver Módulo 02, Parte 5
- **"Performance está lenta?"**: Ver Módulo 11
- **"Como testar?"**: Ver Módulo 10

---

**🎯 Este briefing cobre 80% do que você precisa saber para começar!**  
**📚 Para detalhes completos, consulte a documentação completa no roteiro.**

---

**📝 Elaborado por:** Principal Java Architect  
**🎯 Público:** Analistas Java Junior  
**⏱️ Tempo de leitura:** 15 minutos  
**🚀 Tempo para começar:** 30 minutos  
**📅 Atualizado:** Março 2024