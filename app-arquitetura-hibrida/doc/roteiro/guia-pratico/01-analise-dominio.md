# 🔍 ETAPA 01: ANÁLISE DE DOMÍNIO
## Fundamentos para Implementação Arquitetural

### 🎯 **OBJETIVO DA ETAPA**

Realizar análise completa do domínio de negócio para estabelecer as bases sólidas da implementação, garantindo alinhamento com os princípios de Domain-Driven Design e a arquitetura híbrida estabelecida.

**⏱️ Duração Estimada:** 2-4 horas  
**👥 Participantes:** Desenvolvedor + Domain Expert + Tech Lead  
**📋 Pré-requisitos:** Requisitos funcionais definidos

---

## 📋 **CHECKLIST DE ANÁLISE**

### **🔍 1. COMPREENSÃO DO DOMÍNIO**

#### **📚 Estudo dos Requisitos:**
- [ ] **Requisitos funcionais** lidos e compreendidos
- [ ] **Regras de negócio** identificadas e documentadas
- [ ] **Casos de uso** principais mapeados
- [ ] **Fluxos de processo** compreendidos
- [ ] **Exceções e edge cases** identificados

#### **🎯 Definição do Escopo:**
- [ ] **Bounded Context** claramente definido
- [ ] **Limites do domínio** estabelecidos
- [ ] **Integrações necessárias** identificadas
- [ ] **Dependências externas** mapeadas
- [ ] **Critérios de aceite** validados

#### **💬 Validação com Domain Expert:**
- [ ] **Linguagem ubíqua** estabelecida
- [ ] **Termos de negócio** definidos e documentados
- [ ] **Conceitos complexos** esclarecidos
- [ ] **Variações de processo** compreendidas
- [ ] **Cenários de erro** discutidos

---

### **🧩 2. IDENTIFICAÇÃO DE AGREGADOS**

#### **🏗️ Análise de Entidades:**
- [ ] **Entidades principais** identificadas
- [ ] **Value Objects** candidatos mapeados
- [ ] **Relacionamentos** entre entidades analisados
- [ ] **Ciclos de vida** das entidades compreendidos
- [ ] **Invariantes de negócio** identificadas

#### **🔄 Definição de Boundaries:**
- [ ] **Aggregate Roots** identificados
- [ ] **Limites transacionais** definidos
- [ ] **Consistência forte vs eventual** decidida
- [ ] **Tamanho dos agregados** validado
- [ ] **Performance implications** consideradas

#### **📊 Modelagem Conceitual:**
```
📋 TEMPLATE DE AGREGADO:

Agregado: [Nome do Agregado]
├── Root Entity: [Entidade Raiz]
├── Child Entities: [Entidades Filhas]
├── Value Objects: [Objetos de Valor]
├── Invariantes: [Regras que devem ser sempre válidas]
├── Comandos: [Operações que podem ser executadas]
└── Eventos: [Eventos gerados pelas operações]
```

---

### **⚡ 3. MAPEAMENTO DE COMANDOS**

#### **🎯 Identificação de Operações:**
- [ ] **Operações de criação** identificadas
- [ ] **Operações de atualização** mapeadas
- [ ] **Operações de remoção** definidas
- [ ] **Operações complexas** analisadas
- [ ] **Operações batch** consideradas

#### **📝 Definição de Commands:**
```java
// TEMPLATE DE COMANDO
public class [Operacao][Agregado]Command implements Command {
    // Dados necessários para a operação
    // Validações básicas
    // Metadados (userId, correlationId, etc.)
}
```

#### **✅ Validações de Comando:**
- [ ] **Validações de entrada** definidas
- [ ] **Regras de autorização** identificadas
- [ ] **Pré-condições** estabelecidas
- [ ] **Validações de negócio** mapeadas
- [ ] **Tratamento de erros** planejado

---

### **📡 4. DEFINIÇÃO DE EVENTOS**

#### **🔄 Eventos de Domínio:**
- [ ] **Eventos de criação** definidos
- [ ] **Eventos de mudança de estado** identificados
- [ ] **Eventos de integração** mapeados
- [ ] **Eventos de erro** considerados
- [ ] **Eventos de auditoria** planejados

#### **📋 Estrutura de Eventos:**
```java
// TEMPLATE DE EVENTO
public class [Agregado][Acao]Event extends DomainEvent {
    // Dados do evento (imutáveis)
    // Timestamp automático
    // Metadados de rastreamento
}
```

#### **🎯 Consumidores de Eventos:**
- [ ] **Projections** que precisam do evento identificadas
- [ ] **Integrações** que consomem o evento mapeadas
- [ ] **Notificações** necessárias definidas
- [ ] **Auditoria** e logging planejados
- [ ] **Métricas** a serem coletadas identificadas

---

### **🔄 5. PLANEJAMENTO DE PROJECTIONS**

#### **📊 Modelos de Leitura:**
- [ ] **Views necessárias** identificadas
- [ ] **Consultas frequentes** mapeadas
- [ ] **Filtros e ordenações** definidos
- [ ] **Agregações** necessárias identificadas
- [ ] **Performance requirements** estabelecidos

#### **🗃️ Estrutura de Dados:**
```sql
-- TEMPLATE DE PROJECTION
CREATE TABLE [dominio]_view (
    id UUID PRIMARY KEY,
    -- Campos desnormalizados para consulta
    -- Índices otimizados
    -- Campos de controle (created_at, updated_at, version)
);
```

#### **🔍 Estratégias de Query:**
- [ ] **Consultas por ID** planejadas
- [ ] **Consultas por filtros** definidas
- [ ] **Consultas full-text** consideradas
- [ ] **Consultas agregadas** identificadas
- [ ] **Paginação** e ordenação planejadas

---

## 📊 **ARTEFATOS DE SAÍDA**

### **📋 1. DOCUMENTO DE ANÁLISE DE DOMÍNIO**

```markdown
# ANÁLISE DE DOMÍNIO: [Nome do Domínio]

## 1. VISÃO GERAL
- **Bounded Context**: [Descrição]
- **Linguagem Ubíqua**: [Termos principais]
- **Escopo**: [Limites do domínio]

## 2. AGREGADOS IDENTIFICADOS
### [Nome do Agregado 1]
- **Responsabilidade**: [O que gerencia]
- **Entidades**: [Lista de entidades]
- **Value Objects**: [Lista de VOs]
- **Invariantes**: [Regras de consistência]

## 3. COMANDOS MAPEADOS
- **[Comando 1]**: [Descrição e validações]
- **[Comando 2]**: [Descrição e validações]

## 4. EVENTOS DEFINIDOS
- **[Evento 1]**: [Quando ocorre e dados]
- **[Evento 2]**: [Quando ocorre e dados]

## 5. PROJECTIONS PLANEJADAS
- **[View 1]**: [Propósito e estrutura]
- **[View 2]**: [Propósito e estrutura]
```

### **📊 2. DIAGRAMA DE DOMÍNIO**

```
┌─────────────────────────────────────────────────────────────┐
│                    BOUNDED CONTEXT                         │
│                   [Nome do Domínio]                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────┐    ┌─────────────────┐                │
│  │   AGREGADO 1    │    │   AGREGADO 2    │                │
│  │                 │    │                 │                │
│  │ • Root Entity   │    │ • Root Entity   │                │
│  │ • Child Entity  │    │ • Value Object  │                │
│  │ • Value Object  │    │                 │                │
│  └─────────────────┘    └─────────────────┘                │
│           │                       │                        │
│           ▼                       ▼                        │
│  ┌─────────────────┐    ┌─────────────────┐                │
│  │    EVENTOS      │    │    EVENTOS      │                │
│  │                 │    │                 │                │
│  │ • Evento1       │    │ • Evento3       │                │
│  │ • Evento2       │    │ • Evento4       │                │
│  └─────────────────┘    └─────────────────┘                │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### **📋 3. MATRIZ DE RASTREABILIDADE**

| **Requisito** | **Agregado** | **Comando** | **Evento** | **Projection** |
|---------------|--------------|-------------|------------|----------------|
| REQ-001 | Agregado1 | CriarCmd | CriadoEvent | View1 |
| REQ-002 | Agregado1 | AtualizarCmd | AtualizadoEvent | View1, View2 |
| REQ-003 | Agregado2 | ProcessarCmd | ProcessadoEvent | View2 |

---

## ✅ **CHECKPOINT DE VALIDAÇÃO**

### **🎯 Critérios de Aprovação:**

#### **📋 Completude da Análise:**
- [ ] **Todos os requisitos** foram analisados
- [ ] **Agregados identificados** cobrem todo o escopo
- [ ] **Comandos mapeados** atendem todos os casos de uso
- [ ] **Eventos definidos** suportam todas as integrações
- [ ] **Projections planejadas** atendem todas as consultas

#### **🏗️ Qualidade Arquitetural:**
- [ ] **Bounded Context** bem definido e coeso
- [ ] **Agregados** com tamanho adequado
- [ ] **Separação de responsabilidades** clara
- [ ] **Consistência eventual** adequadamente tratada
- [ ] **Performance** considerada no design

#### **📚 Documentação:**
- [ ] **Linguagem ubíqua** documentada
- [ ] **Decisões arquiteturais** justificadas
- [ ] **Diagramas** claros e atualizados
- [ ] **Rastreabilidade** estabelecida
- [ ] **Riscos identificados** e mitigados

---

## 🚨 **PONTOS DE ATENÇÃO**

### **⚠️ Armadilhas Comuns:**

#### **🚫 Agregados Muito Grandes:**
```java
// ❌ EVITAR: Agregado com muitas responsabilidades
public class SuperAggregate {
    // Muitas entidades filhas
    // Muitas operações diferentes
    // Invariantes complexas demais
}

// ✅ PREFERIR: Agregados focados
public class FocusedAggregate {
    // Responsabilidade única e clara
    // Poucas entidades relacionadas
    // Invariantes simples e claras
}
```

#### **🚫 Eventos Muito Granulares:**
```java
// ❌ EVITAR: Eventos para cada campo
public class NomeAlteradoEvent { }
public class IdadeAlteradaEvent { }
public class EmailAlteradoEvent { }

// ✅ PREFERIR: Eventos de negócio
public class DadosPessoaisAtualizadosEvent {
    // Todos os dados alterados
}
```

#### **🚫 Projections Desnecessárias:**
```java
// ❌ EVITAR: Uma projection para cada tela
public class TelaAView { }
public class TelaBView { }
public class TelaCView { }

// ✅ PREFERIR: Projections por contexto de uso
public class ListagemView { }
public class DetalhesView { }
```

### **✅ Boas Práticas:**

#### **🎯 Foco no Negócio:**
- **Sempre** partir dos requisitos de negócio
- **Sempre** validar com domain experts
- **Sempre** usar linguagem ubíqua
- **Sempre** pensar em invariantes primeiro

#### **🏗️ Design Arquitetural:**
- **Sempre** considerar performance desde o início
- **Sempre** planejar para eventual consistency
- **Sempre** pensar em evolução futura
- **Sempre** documentar decisões importantes

---

## 🔄 **PRÓXIMOS PASSOS**

### **✅ Após Aprovação do Checkpoint:**
1. **[Etapa 02 - Modelagem de Agregados](./02-modelagem-agregados.md)**
2. Implementar os agregados identificados
3. Definir eventos de domínio
4. Estabelecer business rules

### **📋 Preparação para Próxima Etapa:**
- [ ] **Ambiente de desenvolvimento** configurado
- [ ] **Dependências** do projeto atualizadas
- [ ] **Padrões de código** revisados
- [ ] **Exemplos existentes** estudados

---

## 📚 **RECURSOS DE APOIO**

### **📖 Documentação de Referência:**
- **[Domain-Driven Design](../04-domain-driven-design-README.md)**: Conceitos fundamentais
- **[Agregados](../08-agregados-README.md)**: Implementação de agregados
- **Código Existente**: `ExampleAggregate` como referência

### **🛠️ Ferramentas Úteis:**
- **Miro/Lucidchart**: Para diagramas de domínio
- **Notion/Confluence**: Para documentação
- **PlantUML**: Para diagramas técnicos
- **Domain Storytelling**: Para capturar conhecimento

### **👥 Stakeholders:**
- **Domain Expert**: Validação de regras de negócio
- **Tech Lead**: Revisão arquitetural
- **Product Owner**: Alinhamento com requisitos
- **Arquiteto**: Validação de padrões

---

**📋 Checklist Total:** 45+ itens de validação  
**⏱️ Tempo Médio:** 2-4 horas  
**🎯 Resultado:** Modelo de domínio validado e documentado  
**✅ Próxima Etapa:** Modelagem de Agregados