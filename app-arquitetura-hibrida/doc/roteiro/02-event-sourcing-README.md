# 💾 EVENT SOURCING
## Roteiro Técnico - Módulo 02

### 🎯 **OBJETIVO DO MÓDULO**
Dominar os conceitos e implementação de Event Sourcing no projeto, compreendendo como eventos são modelados, armazenados e utilizados para reconstruir estado, preparando para desenvolvimento de agregados robustos.

---

## 📚 **ESTRUTURA DO MÓDULO**

### **📋 Partes Disponíveis:**

| **Parte** | **Tópico** | **Duração** | **Foco Principal** |
|-----------|------------|-------------|-------------------|
| **[Parte 1](./02-event-sourcing-parte-1.md)** | Fundamentos do Event Sourcing | 50 min | Conceitos e modelagem de eventos |
| **[Parte 2](./02-event-sourcing-parte-2.md)** | Event Store e Persistência | 55 min | Armazenamento e recuperação |
| **[Parte 3](./02-event-sourcing-parte-3.md)** | Snapshots e Otimizações | 45 min | Performance e estratégias |
| **[Parte 4](./02-event-sourcing-parte-4.md)** | Versionamento e Evolução | 50 min | Evolução de eventos e schemas |
| **[Parte 5](./02-event-sourcing-parte-5.md)** | Replay e Reconstrução | 60 min | Rebuild de estado e debugging |

**⏱️ Duração Total:** 4 horas e 20 minutos

---

## 🎓 **OBJETIVOS DE APRENDIZADO**

### **Ao completar este módulo, você será capaz de:**

#### **📝 Modelagem de Eventos:**
- ✅ Projetar eventos de domínio eficazes
- ✅ Implementar versionamento de eventos
- ✅ Aplicar padrões de evolução de schema
- ✅ Definir granularidade adequada de eventos

#### **💾 Event Store:**
- ✅ Utilizar a implementação PostgreSQL do Event Store
- ✅ Compreender estratégias de particionamento
- ✅ Implementar consultas otimizadas
- ✅ Gerenciar concorrência e consistência

#### **🚀 Performance:**
- ✅ Implementar snapshots eficazes
- ✅ Otimizar reconstrução de agregados
- ✅ Configurar arquivamento de eventos
- ✅ Monitorar performance do Event Store

#### **🔄 Operações Avançadas:**
- ✅ Executar replay de eventos
- ✅ Reconstruir estado histórico
- ✅ Implementar debugging de eventos
- ✅ Gerenciar migração de dados

---

## 📋 **PRÉ-REQUISITOS**

### **🔧 Conhecimentos Técnicos:**
- **[Módulo 01](./01-introducao-arquitetura-README.md)**: Fundamentos da arquitetura
- **PostgreSQL**: Consultas avançadas e otimização
- **JSON**: Manipulação e serialização
- **Concorrência**: Conceitos de locks e transações

### **🛠️ Componentes do Projeto:**
- **Event Store**: `com.seguradora.hibrida.eventstore`
- **Domain Events**: `com.seguradora.hibrida.eventstore.model`
- **Serialization**: `com.seguradora.hibrida.eventstore.serialization`
- **Aggregates**: `com.seguradora.hibrida.aggregate`

---

## 🚀 **ROTEIRO DE ESTUDOS**

### **📅 Cronograma Sugerido:**

#### **Dia 1 - Manhã (2h 30min):**
- **Parte 1**: Fundamentos do Event Sourcing (50 min)
- **Parte 2**: Event Store e Persistência (55 min)
- **Parte 3**: Snapshots e Otimizações (45 min)

#### **Dia 1 - Tarde (1h 50min):**
- **Parte 4**: Versionamento e Evolução (50 min)
- **Parte 5**: Replay e Reconstrução (60 min)

---

## ✅ **CHECKPOINTS DE VALIDAÇÃO**

### **🎯 Checkpoint 1 - Após Parte 2:**
**Validar compreensão do Event Store**

#### **Exercícios Práticos:**
- [ ] Examinar estrutura da tabela de eventos
- [ ] Criar evento customizado
- [ ] Testar persistência e recuperação
- [ ] Analisar estratégias de particionamento

### **🎯 Checkpoint Final - Após Parte 5:**
**Projeto prático completo**

#### **Entregável:**
- Implementação de novo tipo de evento
- Estratégia de snapshot personalizada
- Processo de replay documentado

---

## 🎯 **Próximo Passo:**
Após aprovação: **[Módulo 03 - CQRS](./03-cqrs-README.md)**

---

**📚 Módulo elaborado por:** Principal Java Architect  
**⏱️ Duração Total:** 4h 20min