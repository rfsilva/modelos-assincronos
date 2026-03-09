# Business Specification - Épico 2: Domínio de Segurados e Apólices

## 1. Visão Geral do Negócio

### 1.1 Contexto de Negócio
O Épico 2 representa a implementação do domínio central de negócios do sistema de seguros automotivos, abrangendo a gestão completa de segurados e suas apólices. Este épico é fundamental para operação da seguradora, estabelecendo as bases para relacionamentos com clientes e produtos de seguro.

### 1.2 Objetivos de Negócio
- **Gestão de Relacionamento**: Manter histórico completo e auditável de segurados
- **Comercialização Ágil**: Permitir criação e gestão eficiente de apólices
- **Compliance Regulatório**: Atender normas SUSEP e LGPD
- **Experiência do Cliente**: Notificações proativas sobre vencimentos e alterações
- **Inteligência de Negócio**: Análises e relatórios para tomada de decisão

### 1.3 Problemas Resolvidos
- ❌ **Antes**: Cadastros manuais sujeitos a erros e duplicações
- ✅ **Depois**: Validações automáticas com invariantes de domínio
- ❌ **Antes**: Falta de auditoria de alterações
- ✅ **Depois**: Event Sourcing garante histórico completo
- ❌ **Antes**: Notificações manuais de vencimento
- ✅ **Depois**: Alertas automáticos multi-canal
- ❌ **Antes**: Relatórios manuais e desatualizados
- ✅ **Depois**: Dashboards em tempo real

## 2. Stakeholders e Usuários

### 2.1 Stakeholders Principais
| Stakeholder | Interesse | Prioridade |
|-------------|-----------|------------|
| **Gerente Comercial** | Volume de apólices vendidas, renovações | Crítica |
| **Operadores** | Eficiência no atendimento, redução de erros | Crítica |
| **Compliance** | Conformidade SUSEP, LGPD, auditoria | Crítica |
| **TI/Arquitetura** | Escalabilidade, manutenibilidade | Alta |
| **Segurados** | Facilidade de acesso, transparência | Alta |
| **Atuária** | Precificação, análise de risco | Média |

### 2.2 Personas de Usuário

**Persona 1: Maria - Operadora de Atendimento**
- **Idade**: 28 anos
- **Experiência**: 3 anos no setor de seguros
- **Objetivos**: Cadastrar segurados rapidamente sem erros, consultar histórico
- **Dores**: Sistemas lentos, validações confusas, falta de histórico
- **Tecnologia**: Usuária intermediária de sistemas web

**Persona 2: João - Gerente Comercial**
- **Idade**: 42 anos
- **Experiência**: 15 anos no mercado de seguros
- **Objetivos**: Acompanhar vendas, identificar oportunidades de renovação
- **Dores**: Relatórios desatualizados, falta de visão consolidada
- **Tecnologia**: Usuário avançado, prefere dashboards visuais

**Persona 3: Carlos - Segurado**
- **Idade**: 35 anos
- **Perfil**: Profissional liberal com veículo próprio
- **Objetivos**: Ser notificado de vencimentos, consultar cobertura
- **Dores**: Esquecimento de vencimentos, dificuldade em contato
- **Tecnologia**: Usuário mobile-first, prefere WhatsApp

## 3. Requisitos Funcionais

### 3.1 RF01: Gestão de Segurados (US009-US011)

#### RF01.1 - Cadastro de Segurado
**Prioridade**: Crítica  
**Complexidade**: Média

**Descrição**:  
Permitir cadastro completo de segurados com validações automáticas e prevenção de duplicidade.

**Regras de Negócio**:
- RN01.1: CPF deve ser válido e único no sistema
- RN01.2: Email deve ser válido e único
- RN01.3: Telefone celular é obrigatório para notificações
- RN01.4: Data de nascimento: idade mínima 18 anos, máxima 80 anos
- RN01.5: Endereço completo é obrigatório (CEP, logradouro, número, cidade, UF)
- RN01.6: Todos os campos obrigatórios devem ser preenchidos antes do commit

**Critérios de Aceite**:
- ✅ Sistema valida CPF em tempo real (dígito verificador)
- ✅ Sistema impede cadastro duplicado de CPF
- ✅ Sistema valida formato de email (RFC 5322)
- ✅ Sistema valida telefone celular brasileiro (11 dígitos)
- ✅ Sistema calcula idade automaticamente pela data nascimento
- ✅ Sistema consulta CEP em API externa (ViaCEP) para autocompletar endereço
- ✅ Sistema exibe mensagens de erro claras para cada validação falha
- ✅ Sistema persiste evento `SeguradoCriado` no Event Store

**Fluxo Principal**:
1. Operador acessa tela "Novo Segurado"
2. Sistema exibe formulário de cadastro
3. Operador preenche dados obrigatórios:
   - CPF
   - Nome completo
   - Data de nascimento
   - Email
   - Telefone celular
   - CEP
4. Sistema valida CPF em tempo real
5. Sistema consulta CEP e autocompleta endereço
6. Operador confirma ou ajusta endereço
7. Operador clica em "Salvar"
8. Sistema valida todos os campos
9. Sistema cria comando `CriarSeguradoCommand`
10. Command Handler processa e cria aggregate
11. Sistema persiste evento `SeguradoCriado`
12. Sistema exibe mensagem de sucesso com ID do segurado
13. Sistema atualiza projeção de consulta assincronamente

**Fluxos Alternativos**:
- FA01: CPF já cadastrado → Sistema exibe mensagem "CPF já cadastrado" com link para consulta
- FA02: CPF inválido → Sistema exibe mensagem "CPF inválido"
- FA03: Email duplicado → Sistema sugere adicionar sufixo (ex: +seguros)
- FA04: CEP não encontrado → Sistema permite cadastro manual do endereço
- FA05: Falha na validação → Sistema destaca campos com erro em vermelho

#### RF01.2 - Consulta de Segurados
**Prioridade**: Crítica  
**Complexidade**: Baixa

**Descrição**:  
Permitir consulta rápida de segurados com múltiplos filtros.

**Regras de Negócio**:
- RN02.1: Busca deve retornar em < 50ms (P95)
- RN02.2: Resultados devem ser paginados (20 por página)
- RN02.3: Cache de consultas frequentes (TTL 5 minutos)
- RN02.4: Ordenação padrão: nome ascendente

**Critérios de Aceite**:
- ✅ Busca por CPF exato
- ✅ Busca por nome (parcial, case-insensitive)
- ✅ Filtro por status (ativo/inativo)
- ✅ Filtro por data de cadastro (range)
- ✅ Ordenação por nome, CPF, data cadastro
- ✅ Paginação com cursores
- ✅ Exportação para Excel/CSV

#### RF01.3 - Atualização de Segurado
**Prioridade**: Alta  
**Complexidade**: Média

**Descrição**:  
Permitir atualização controlada de dados cadastrais.

**Regras de Negócio**:
- RN03.1: CPF não pode ser alterado após criação
- RN03.2: Alterações sensíveis geram evento de auditoria
- RN03.3: Email e telefone podem ser atualizados livremente
- RN03.4: Alteração de endereço requer confirmação
- RN03.5: Controle de concorrência otimista (version)

**Critérios de Aceite**:
- ✅ Sistema impede alteração de CPF
- ✅ Sistema valida novos dados antes de atualizar
- ✅ Sistema persiste evento `SeguradoAtualizado` com diff
- ✅ Sistema detecta conflito de versão
- ✅ Sistema mantém histórico de alterações

#### RF01.4 - Desativação/Reativação de Segurado
**Prioridade**: Alta  
**Complexidade**: Baixa

**Descrição**:  
Permitir desativação lógica e reativação de segurados.

**Regras de Negócio**:
- RN04.1: Desativação é lógica (não exclui dados)
- RN04.2: Segurado com apólice ativa não pode ser desativado
- RN04.3: Reativação requer aprovação do supervisor
- RN04.4: Motivo da desativação é obrigatório

**Critérios de Aceite**:
- ✅ Sistema valida se segurado tem apólice ativa
- ✅ Sistema exige motivo da desativação (campo texto)
- ✅ Sistema persiste eventos `SeguradoDesativado`/`SeguradoReativado`
- ✅ Sistema atualiza status nas projeções
- ✅ Segurado desativado não aparece em buscas padrão

### 3.2 RF02: Gestão de Apólices (US012-US014)

#### RF02.1 - Criação de Apólice
**Prioridade**: Crítica  
**Complexidade**: Alta

**Descrição**:  
Permitir emissão de apólices de seguro automotivo com cálculo automático de prêmio.

**Regras de Negócio**:
- RN05.1: Segurado deve existir e estar ativo
- RN05.2: Veículo deve estar cadastrado e sem cobertura vigente
- RN05.3: Vigência mínima: 30 dias, máxima: 365 dias
- RN05.4: Data início vigência >= data atual
- RN05.5: Prêmio calculado com base em: valor do veículo, cobertura, franquia, perfil do segurado
- RN05.6: Coberturas disponíveis: Compreensiva, Terceiros, Colisão, Roubo/Furto
- RN05.7: Franquias disponíveis: Normal, Reduzida, Majorada
- RN05.8: Desconto máximo: 30% (com aprovação gerencial)

**Critérios de Aceite**:
- ✅ Sistema valida existência e status do segurado
- ✅ Sistema valida veículo sem cobertura vigente
- ✅ Sistema calcula prêmio automaticamente ao selecionar cobertura
- ✅ Sistema recalcula prêmio ao alterar franquia
- ✅ Sistema aplica descontos configuráveis
- ✅ Sistema valida período de vigência
- ✅ Sistema gera número único de apólice (formato: APO-AAAA-NNNNNN)
- ✅ Sistema persiste evento `ApoliceCriada` com todos os dados
- ✅ Sistema envia notificação de boas-vindas ao segurado

**Fluxo Principal**:
1. Operador acessa tela "Nova Apólice"
2. Sistema solicita CPF do segurado
3. Operador informa CPF
4. Sistema busca segurado e exibe dados
5. Sistema lista veículos do segurado
6. Operador seleciona veículo
7. Sistema valida disponibilidade do veículo
8. Operador seleciona:
   - Coberturas desejadas
   - Tipo de franquia
   - Data início vigência
   - Período de vigência (meses)
9. Sistema calcula prêmio automaticamente
10. Sistema exibe simulação:
    - Prêmio total
    - Parcelas (1x, 2x, 3x, 4x, 6x, 12x)
    - Coberturas incluídas
11. Operador confirma emissão
12. Sistema cria comando `CriarApoliceCommand`
13. Command Handler valida e cria aggregate
14. Sistema persiste evento `ApoliceCriada`
15. Sistema gera PDF da apólice
16. Sistema envia notificação com PDF anexo
17. Sistema exibe mensagem de sucesso

**Fluxos Alternativos**:
- FA06: Segurado inexistente → Sistema oferece cadastrar novo segurado
- FA07: Segurado inativo → Sistema exibe alerta e impede emissão
- FA08: Veículo com cobertura vigente → Sistema exibe data fim da cobertura atual
- FA09: Desconto acima do permitido → Sistema solicita aprovação gerencial
- FA10: Falha no cálculo → Sistema exibe mensagem de erro e registra log

#### RF02.2 - Renovação de Apólice
**Prioridade**: Crítica  
**Complexidade**: Média

**Descrição**:  
Permitir renovação simplificada de apólices vencidas ou próximas do vencimento.

**Regras de Negócio**:
- RN06.1: Renovação disponível 45 dias antes do vencimento
- RN06.2: Sistema sugere mesmas coberturas da apólice anterior
- RN06.3: Prêmio recalculado com tabela vigente
- RN06.4: Desconto de renovação automático: 5%
- RN06.5: Dados do segurado e veículo devem estar atualizados

**Critérios de Aceite**:
- ✅ Sistema identifica apólices próximas do vencimento
- ✅ Sistema preenche automaticamente com dados da apólice anterior
- ✅ Sistema recalcula prêmio com tabela atual
- ✅ Sistema aplica desconto de renovação
- ✅ Sistema valida atualização de dados cadastrais
- ✅ Sistema persiste evento `ApoliceRenovada`
- ✅ Sistema envia notificação de renovação

#### RF02.3 - Cancelamento de Apólice
**Prioridade**: Alta  
**Complexidade**: Média

**Descrição**:  
Permitir cancelamento de apólices com cálculo de valores devidos.

**Regras de Negócio**:
- RN07.1: Apólice pode ser cancelada a qualquer momento
- RN07.2: Cancelamento até 7 dias: devolução integral (direito de arrependimento)
- RN07.3: Cancelamento após 7 dias: cálculo pro-rata
- RN07.4: Sinistros em aberto impedem cancelamento
- RN07.5: Motivo do cancelamento é obrigatório
- RN07.6: Cancelamento gera crédito para devolução

**Critérios de Aceite**:
- ✅ Sistema valida existência de sinistros em aberto
- ✅ Sistema calcula valor de devolução automaticamente
- ✅ Sistema exige motivo do cancelamento
- ✅ Sistema persiste evento `ApoliceCancelada` com valores
- ✅ Sistema gera ordem de pagamento de devolução
- ✅ Sistema envia notificação de cancelamento

#### RF02.4 - Consulta de Apólices
**Prioridade**: Crítica  
**Complexidade**: Média

**Descrição**:  
Permitir consulta eficiente de apólices com filtros avançados.

**Regras de Negócio**:
- RN08.1: Consulta deve retornar em < 50ms (P95)
- RN08.2: Dados desnormalizados incluindo segurado e veículo
- RN08.3: Cache inteligente por CPF (TTL 10 minutos)
- RN08.4: Paginação com cursores para grandes volumes

**Critérios de Aceite**:
- ✅ Busca por número de apólice
- ✅ Busca por CPF do segurado
- ✅ Busca por placa do veículo
- ✅ Filtro por status (vigente/vencida/cancelada)
- ✅ Filtro por período de vigência
- ✅ Filtro por tipo de cobertura
- ✅ Ordenação por data, valor, status
- ✅ Exportação com dados completos

### 3.3 RF03: Sistema de Notificações (US015)

#### RF03.1 - Notificações de Vencimento
**Prioridade**: Alta  
**Complexidade**: Média

**Descrição**:  
Enviar notificações automáticas sobre vencimento de apólices.

**Regras de Negócio**:
- RN09.1: Primeira notificação: 30 dias antes do vencimento
- RN09.2: Segunda notificação: 15 dias antes do vencimento
- RN09.3: Terceira notificação: 7 dias antes do vencimento
- RN09.4: Notificação final: no dia do vencimento
- RN09.5: Canais em ordem de preferência: WhatsApp, SMS, Email
- RN09.6: Segurado pode configurar canais preferidos

**Critérios de Aceite**:
- ✅ Sistema envia notificações nos prazos definidos
- ✅ Sistema tenta múltiplos canais em caso de falha
- ✅ Sistema registra todas as tentativas de envio
- ✅ Sistema exibe link para renovação simplificada
- ✅ Sistema respeita preferências do segurado

#### RF03.2 - Notificações de Alteração
**Prioridade**: Média  
**Complexidade**: Baixa

**Descrição**:  
Notificar segurado sobre alterações em sua apólice.

**Regras de Negócio**:
- RN10.1: Notificação em tempo real para alterações críticas
- RN10.2: Notificação deve incluir diff (antes/depois)
- RN10.3: Tipos de alteração: cobertura, franquia, valor

**Critérios de Aceite**:
- ✅ Notificação enviada imediatamente após alteração
- ✅ Conteúdo claro explicando a alteração
- ✅ Link para consultar apólice atualizada

### 3.4 RF04: Relatórios e Analytics (US016)

#### RF04.1 - Dashboard Operacional
**Prioridade**: Alta  
**Complexidade**: Média

**Descrição**:  
Dashboard em tempo real com métricas principais de segurados e apólices.

**Métricas Incluídas**:
- Total de segurados (ativos/inativos)
- Total de apólices (vigentes/vencidas/canceladas)
- Apólices emitidas no mês
- Apólices vencendo nos próximos 30 dias
- Taxa de renovação (%)
- Prêmio médio por apólice
- Top 5 coberturas mais vendidas
- Evolução mensal de emissões (gráfico)

**Critérios de Aceite**:
- ✅ Atualização em tempo real (< 2 segundos de lag)
- ✅ Filtros por período, produto, operador
- ✅ Gráficos interativos
- ✅ Drill-down para detalhes
- ✅ Exportação de dados

#### RF04.2 - Relatórios Analíticos
**Prioridade**: Média  
**Complexidade**: Média

**Descrição**:  
Relatórios detalhados para análise de negócio.

**Relatórios Disponíveis**:
1. **Segurados por Perfil**: Distribuição por faixa etária, região, renda
2. **Apólices por Produto**: Análise por tipo de cobertura e franquia
3. **Renovação e Cancelamento**: Taxa de renovação, motivos de cancelamento
4. **Performance Comercial**: Vendas por operador, período, produto

**Critérios de Aceite**:
- ✅ Agendamento automático de relatórios
- ✅ Exportação em PDF, Excel, CSV
- ✅ Envio por email para stakeholders
- ✅ Filtros customizáveis

## 4. Requisitos Não Funcionais

### 4.1 Performance
| ID | Requisito | Meta | Crítico |
|----|-----------|------|--------|
| RNF01 | Tempo de resposta consultas | < 50ms (P95) | Sim |
| RNF02 | Tempo de resposta comandos | < 100ms (P95) | Sim |
| RNF03 | Throughput de eventos | > 1000 eventos/segundo | Não |
| RNF04 | Cache hit rate | > 80% | Não |
| RNF05 | Lag de projeções | < 2 segundos (P95) | Sim |

### 4.2 Escalabilidade
| ID | Requisito | Meta | Crítico |
|----|-----------|------|--------|
| RNF06 | Suporte a segurados | 500.000 registros | Sim |
| RNF07 | Suporte a apólices | 1.000.000 registros | Sim |
| RNF08 | Crescimento anual | 30% sem degradação | Não |
| RNF09 | Consultas simultâneas | 100 usuários | Sim |

### 4.3 Disponibilidade
| ID | Requisito | Meta | Crítico |
|----|-----------|------|--------|
| RNF10 | Disponibilidade geral | 99.5% | Sim |
| RNF11 | Janela de manutenção | Máximo 4h/mês | Sim |
| RNF12 | RTO (Recovery Time Objective) | < 4 horas | Sim |
| RNF13 | RPO (Recovery Point Objective) | < 1 hora | Sim |

### 4.4 Segurança
| ID | Requisito | Meta | Crítico |
|----|-----------|------|--------|
| RNF14 | Autenticação | OAuth2 + MFA | Sim |
| RNF15 | Autorização | RBAC granular | Sim |
| RNF16 | Criptografia em repouso | AES-256 | Sim |
| RNF17 | Criptografia em trânsito | TLS 1.3 | Sim |
| RNF18 | Auditoria | Todos os eventos logados | Sim |
| RNF19 | Conformidade LGPD | 100% | Sim |

### 4.5 Usabilidade
| ID | Requisito | Meta | Crítico |
|----|-----------|------|--------|
| RNF20 | Tempo de aprendizado | < 2 horas para operador | Não |
| RNF21 | Taxa de erro do usuário | < 5% | Não |
| RNF22 | Satisfação do usuário | > 4.0/5.0 | Não |
| RNF23 | Acessibilidade | WCAG 2.1 AA | Não |

## 5. Restrições e Premissas

### 5.1 Restrições Técnicas
- **RT01**: Uso obrigatório de Event Sourcing para auditoria
- **RT02**: Separação CQRS entre write e read models
- **RT03**: PostgreSQL como banco de dados principal
- **RT04**: Kafka para comunicação assíncrona
- **RT05**: Redis para cache distribuído
- **RT06**: Java 17+ e Spring Boot 3.x
- **RT07**: APIs RESTful com OpenAPI 3.0

### 5.2 Restrições de Negócio
- **RN01**: Conformidade com normas SUSEP
- **RN02**: Conformidade total com LGPD
- **RN03**: Retenção de dados por 7 anos (regulação)
- **RN04**: Auditoria completa de alterações
- **RN05**: Integração com sistema legado via eventos

### 5.3 Premissas
- **P01**: Épico 1 (Infraestrutura Event Sourcing) já implementado
- **P02**: Ambiente de desenvolvimento configurado
- **P03**: Equipe treinada em DDD e Event Sourcing
- **P04**: APIs externas (ViaCEP) disponíveis
- **P05**: Infraestrutura Kafka operacional

## 6. Riscos de Negócio

### 6.1 Riscos Identificados
| ID | Risco | Probabilidade | Impacto | Mitigação |
|----|-------|---------------|---------|----------|
| R01 | Complexidade do cálculo de prêmio | Média | Alto | Validação com atuária, testes extensivos |
| R02 | Volume de notificações | Baixa | Médio | Sistema de throttling, monitoramento |
| R03 | Performance de consultas | Média | Alto | Cache inteligente, índices otimizados |
| R04 | Inconsistência de projeções | Baixa | Alto | Rebuild automático, monitoramento de lag |
| R05 | Falha em notificações | Média | Médio | Retry automático, múltiplos canais |
| R06 | Integração com legado | Alta | Alto | Testes de integração, ambiente de staging |

## 7. Critérios de Sucesso

### 7.1 Métricas de Sucesso
| Métrica | Meta | Prazo |
|---------|------|-------|
| Tempo de cadastro de segurado | < 2 minutos | Sprint 1 |
| Tempo de emissão de apólice | < 5 minutos | Sprint 2 |
| Taxa de erro em cadastros | < 2% | Sprint 3 |
| Satisfação de operadores | > 4.0/5.0 | Sprint 4 |
| Cobertura de testes | > 90% | Contínuo |
| Performance de consultas | < 50ms P95 | Sprint 3 |
| Disponibilidade | > 99.5% | Produção |

### 7.2 Validação de Negócio
- ✅ Demonstração funcional para stakeholders ao fim de cada sprint
- ✅ Validação de regras de negócio com gerente comercial
- ✅ Validação de compliance com time jurídico
- ✅ Teste de usabilidade com operadores reais
- ✅ Validação de relatórios com gestores

## 8. Fora de Escopo

Os seguintes itens **NÃO** fazem parte deste épico:
- ❌ Gestão de veículos (Épico 3)
- ❌ Processamento de sinistros (Épico 4)
- ❌ Integração com Detran (Épico 5)
- ❌ Processamento de pagamentos (Épico 6)
- ❌ Portal do segurado (futuro)
- ❌ App mobile nativo (futuro)
- ❌ Cotação online automatizada (futuro)
- ❌ Integração com corretoras (futuro)

## 9. Dependências

### 9.1 Dependências Técnicas
- **D01**: Épico 1 (Event Store, Command Bus, Event Bus) - **Crítico**
- **D02**: Infraestrutura Kafka operacional - **Crítico**
- **D03**: Bancos de dados PostgreSQL (write/read) - **Crítico**
- **D04**: Redis para cache - **Importante**
- **D05**: API ViaCEP para consulta de endereços - **Importante**

### 9.2 Dependências de Negócio
- **D06**: Tabelas de precificação fornecidas pela atuária - **Crítico**
- **D07**: Templates de notificação aprovados - **Importante**
- **D08**: Regras de desconto definidas - **Importante**
- **D09**: Processos de aprovação mapeados - **Importante**

## 10. Roadmap de Entrega

### Sprint 1 (2 semanas): Fundação - Segurados
**Entregas**:
- US009: SeguradoAggregate completo
- US010: Command Handlers de Segurado
- US011: Projeções de Segurado básicas

**Critério de Aceite da Sprint**:
- ✅ CRUD completo de segurados funcionando
- ✅ Validações automáticas implementadas
- ✅ Consultas < 50ms
- ✅ Testes unitários > 85%

### Sprint 2 (2 semanas): Apólices Core
**Entregas**:
- US012: ApoliceAggregate completo
- US013: Command Handlers de Apólice
- Integração Segurado-Apólice

**Critério de Aceite da Sprint**:
- ✅ Emissão de apólice funcionando end-to-end
- ✅ Cálculo de prêmio correto
- ✅ Relacionamento segurado-apólice validado
- ✅ Testes de integração passando

### Sprint 3 (2 semanas): Consultas e Notificações
**Entregas**:
- US014: Projeções completas de Apólice
- US015: Sistema de notificações multi-canal

**Critério de Aceite da Sprint**:
- ✅ Dashboard operacional funcionando
- ✅ Notificações de vencimento automáticas
- ✅ Consultas avançadas < 50ms
- ✅ Cache inteligente operacional

### Sprint 4 (2 semanas): Analytics e Refinamentos
**Entregas**:
- US016: Relatórios analíticos
- Refinamentos de performance
- Ajustes de UX

**Critério de Aceite da Sprint**:
- ✅ Relatórios completos disponíveis
- ✅ Performance atendendo NFRs
- ✅ Feedbacks de usabilidade incorporados
- ✅ Sistema pronto para produção

## 11. Glossário de Negócio

| Termo | Definição |
|-------|----------|
| **Segurado** | Pessoa física ou jurídica que contrata seguro |
| **Apólice** | Contrato de seguro entre seguradora e segurado |
| **Prêmio** | Valor pago pelo segurado pela cobertura |
| **Cobertura** | Riscos cobertos pela apólice |
| **Franquia** | Valor de responsabilidade do segurado em caso de sinistro |
| **Vigência** | Período de validade da apólice |
| **Renovação** | Emissão de nova apólice ao fim da vigência |
| **Sinistro** | Evento coberto pela apólice (acidente, roubo, etc) |
| **Indenização** | Valor pago pela seguradora ao segurado |
| **Pro-rata** | Cálculo proporcional de valores |
| **SUSEP** | Superintendência de Seguros Privados (regulador) |
| **LGPD** | Lei Geral de Proteção de Dados |

## 12. Aprovações

### 12.1 Stakeholders Aprovadores
| Nome | Papel | Data Aprovação | Status |
|------|-------|----------------|--------|
| [Nome] | Gerente Comercial | [Data] | ⏳ Pendente |
| [Nome] | Gerente de TI | [Data] | ⏳ Pendente |
| [Nome] | Compliance Officer | [Data] | ⏳ Pendente |
| [Nome] | Arquiteto de Software | [Data] | ⏳ Pendente |

### 12.2 Controle de Versão
| Versão | Data | Autor | Mudanças |
|--------|------|-------|----------|
| 1.0 | 09/03/2026 | Sistema | Versão inicial completa |

---

**Documento gerado automaticamente pelo sistema de especificações Turing Loop**  
**Próximo passo**: Revisar Technical Specification para detalhes de implementação