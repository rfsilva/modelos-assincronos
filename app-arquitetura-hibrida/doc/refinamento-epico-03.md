# 🔧 REFINAMENTO ÉPICO 3: DOMÍNIO DE VEÍCULOS E RELACIONAMENTOS
## Tarefas e Subtarefas Detalhadas

---

## **US017 - Aggregate de Veículo com Validações Avançadas**
**Estimativa:** 21 pontos | **Prioridade:** Alta

### **📋 TAREFAS FUNCIONAIS**

#### **T017.1 - Modelagem do Domínio de Veículo**
**Estimativa:** 4 pontos
- [ ] **ST017.1.1** - Definir entidades do domínio:
  - `Veiculo` (placa, renavam, chassi, marca, modelo, ano)
  - `Especificacao` (cor, combustivel, categoria, cilindrada)
  - `Proprietario` (cpf, nome, tipo_pessoa)
- [ ] **ST017.1.2** - Criar value objects:
  - `Placa` com validação formato brasileiro e Mercosul
  - `Renavam` com validação de dígito verificador
  - `Chassi` com validação de formato VIN
  - `AnoModelo` com validação de ano fabricação/modelo
- [ ] **ST017.1.3** - Definir enums:
  - `StatusVeiculo` (ATIVO, INATIVO, BLOQUEADO, SINISTRADO)
  - `TipoCombustivel` (GASOLINA, ETANOL, FLEX, DIESEL, GNV, ELETRICO)
  - `CategoriaVeiculo` (PASSEIO, UTILITARIO, MOTOCICLETA, CAMINHAO)
- [ ] **ST017.1.4** - Documentar regras de negócio:
  - Validações específicas por categoria
  - Regras de transferência de propriedade
  - Restrições por idade do veículo

#### **T017.2 - Implementação do VeiculoAggregate**
**Estimativa:** 6 pontos
- [ ] **ST017.2.1** - Criar classe `VeiculoAggregate` extends `AggregateRoot`
- [ ] **ST017.2.2** - Implementar construtor para novo veículo
- [ ] **ST017.2.3** - Implementar métodos de negócio:
  - `atualizarEspecificacoes(cor, combustivel)`
  - `transferirPropriedade(novoProprietario)`
  - `associarApolice(apoliceId, dataInicio)`
  - `desassociarApolice(apoliceId, dataFim)`
  - `bloquear(motivo, dataEfeito)`
  - `desbloquear(operadorId)`
- [ ] **ST017.2.4** - Implementar validações de invariantes:
  - Placa única no sistema
  - RENAVAM único no sistema
  - Ano fabricação <= ano modelo
  - Categoria compatível com especificações
- [ ] **ST017.2.5** - Configurar aplicação de eventos com `@EventSourcingHandler`

#### **T017.3 - Validações Avançadas**
**Estimativa:** 5 pontos
- [ ] **ST017.3.1** - Implementar validador de placa brasileira:
  - Formato antigo (ABC-1234)
  - Formato Mercosul (ABC1D23)
  - Validação de caracteres permitidos
- [ ] **ST017.3.2** - Implementar validador de RENAVAM:
  - Algoritmo oficial de dígito verificador
  - Validação de sequência numérica
  - Verificação de formato (11 dígitos)
- [ ] **ST017.3.3** - Implementar validador de chassi/VIN:
  - Formato internacional (17 caracteres)
  - Validação de dígito verificador
  - Verificação de caracteres proibidos (I, O, Q)
- [ ] **ST017.3.4** - Criar validador de ano/modelo:
  - Ano fabricação válido (1900 - ano atual + 1)
  - Ano modelo válido (ano fabricação - 1 até + 1)
  - Validação de combinações impossíveis

### **📋 TAREFAS TÉCNICAS**

#### **T017.4 - Eventos de Domínio**
**Estimativa:** 4 pontos
- [ ] **ST017.4.1** - Criar evento `VeiculoCriadoEvent`:
  - veiculoId, placa, renavam, chassi, marca, modelo, ano, proprietario
- [ ] **ST017.4.2** - Criar evento `VeiculoAtualizadoEvent`:
  - veiculoId, especificacoesAlteradas, valoresAnteriores, novosValores
- [ ] **ST017.4.3** - Criar evento `VeiculoAssociadoEvent`:
  - veiculoId, apoliceId, dataInicio, operadorId
- [ ] **ST017.4.4** - Criar evento `VeiculoDesassociadoEvent`:
  - veiculoId, apoliceId, dataFim, motivo, operadorId
- [ ] **ST017.4.5** - Criar evento `PropriedadeTransferidaEvent`:
  - veiculoId, proprietarioAnterior, novoProprietario, dataTransferencia
- [ ] **ST017.4.6** - Implementar serialização e versionamento de eventos

#### **T017.5 - Testes e Documentação**
**Estimativa:** 2 pontos
- [ ] **ST017.5.1** - Criar testes unitários para VeiculoAggregate
- [ ] **ST017.5.2** - Implementar testes de validações avançadas
- [ ] **ST017.5.3** - Criar testes de aplicação de eventos
- [ ] **ST017.5.4** - Implementar testes de invariantes de negócio
- [ ] **ST017.5.5** - Documentar API do aggregate e regras de negócio

---

## **US018 - Command Handlers para Veículo**
**Estimativa:** 13 pontos | **Prioridade:** Alta

### **📋 TAREFAS FUNCIONAIS**

#### **T018.1 - Comandos de Veículo**
**Estimativa:** 3 pontos
- [ ] **ST018.1.1** - Criar `CriarVeiculoCommand`:
  - placa, renavam, chassi, marca, modelo, ano, cor, combustivel, proprietario
- [ ] **ST018.1.2** - Criar `AtualizarVeiculoCommand`:
  - veiculoId, especificacoesAtualizacao, operadorId, versaoEsperada
- [ ] **ST018.1.3** - Criar `AssociarVeiculoCommand`:
  - veiculoId, apoliceId, dataInicio, operadorId
- [ ] **ST018.1.4** - Criar `DesassociarVeiculoCommand`:
  - veiculoId, apoliceId, dataFim, motivo, operadorId
- [ ] **ST018.1.5** - Implementar validações Bean Validation em todos os comandos

#### **T018.2 - Command Handlers Principais**
**Estimativa:** 5 pontos
- [ ] **ST018.2.1** - Implementar `CriarVeiculoCommandHandler`:
  - Validar unicidade de placa e RENAVAM
  - Verificar dados no Detran (se disponível)
  - Validar proprietário no sistema
  - Criar aggregate e aplicar evento
- [ ] **ST018.2.2** - Implementar `AtualizarVeiculoCommandHandler`:
  - Carregar aggregate do Event Store
  - Validar alterações permitidas
  - Verificar impacto em apólices ativas
  - Aplicar alterações
- [ ] **ST018.2.3** - Implementar `AssociarVeiculoCommandHandler`:
  - Validar apólice ativa e compatível
  - Verificar se veículo não está associado
  - Validar cobertura para tipo de veículo
  - Aplicar associação
- [ ] **ST018.2.4** - Implementar `DesassociarVeiculoCommandHandler`:
  - Verificar se não há sinistros em aberto
  - Validar permissões do operador
  - Calcular período de cobertura
  - Aplicar desassociação

### **📋 TAREFAS TÉCNICAS**

#### **T018.3 - Validações de Unicidade**
**Estimativa:** 3 pontos
- [ ] **ST018.3.1** - Implementar validação de placa única:
  - Consulta otimizada na projeção
  - Cache de placas ativas (TTL 1 hora)
  - Tratamento de placas transferidas
- [ ] **ST018.3.2** - Implementar validação de RENAVAM único:
  - Consulta otimizada na projeção
  - Cache de RENAVAMs ativos
  - Validação cruzada com Detran
- [ ] **ST018.3.3** - Configurar validação de chassi único:
  - Consulta por chassi completo
  - Validação de formato VIN
  - Cache de chassis cadastrados

#### **T018.4 - Validações de Relacionamento**
**Estimativa:** 2 pontos
- [ ] **ST018.4.1** - Implementar validação de apólice ativa:
  - Consulta de status da apólice
  - Verificação de vigência
  - Validação de cobertura compatível
- [ ] **ST018.4.2** - Configurar validação de proprietário:
  - Verificação de segurado ativo
  - Validação de CPF/CNPJ
  - Consulta de restrições

---

## **US019 - Projeções de Veículo com Índices Geográficos**
**Estimativa:** 21 pontos | **Prioridade:** Alta

### **📋 TAREFAS FUNCIONAIS**

#### **T019.1 - Modelagem de Query Models**
**Estimativa:** 4 pontos
- [ ] **ST019.1.1** - Criar `VeiculoQueryModel` com dados desnormalizados:
  - id, placa, renavam, chassi, marca, modelo, ano, cor
  - proprietario_cpf, proprietario_nome, cidade, estado
  - status, data_criacao, apolice_ativa
- [ ] **ST019.1.2** - Criar `VeiculoListView` para listagens:
  - placa, marca, modelo, ano, proprietario, status, cidade
- [ ] **ST019.1.3** - Criar `VeiculoDetailView` para detalhes:
  - Dados completos + histórico + apólices + sinistros
- [ ] **ST019.1.4** - Criar `VeiculoGeoView` para consultas geográficas:
  - placa, marca, modelo, coordenadas, cidade, estado, regiao

#### **T019.2 - Projection Handlers**
**Estimativa:** 5 pontos
- [ ] **ST019.2.1** - Implementar `VeiculoProjectionHandler`:
  - Handler para `VeiculoCriadoEvent`
  - Handler para `VeiculoAtualizadoEvent`
  - Handler para `VeiculoAssociadoEvent`
  - Handler para `VeiculoDesassociadoEvent`
  - Handler para `PropriedadeTransferidaEvent`
- [ ] **ST019.2.2** - Configurar desnormalização de dados do proprietário
- [ ] **ST019.2.3** - Implementar sincronização com dados geográficos
- [ ] **ST019.2.4** - Configurar processamento idempotente
- [ ] **ST019.2.5** - Implementar tratamento de eventos fora de ordem

### **📋 TAREFAS TÉCNICAS**

#### **T019.3 - Índices e Consultas Otimizadas**
**Estimativa:** 6 pontos
- [ ] **ST019.3.1** - Criar índices otimizados:
  - `idx_veiculo_placa` (único)
  - `idx_veiculo_renavam` (único)
  - `idx_veiculo_proprietario_cpf`
  - `idx_veiculo_marca_modelo` (composto)
  - `idx_veiculo_cidade_estado` (composto)
  - `idx_veiculo_status_ano` (composto)
- [ ] **ST019.3.2** - Implementar consultas customizadas:
  - `findByPlaca`, `findByRenavam`, `findByProprietarioCpf`
  - `findByMarcaAndModelo`, `findByCidadeAndEstado`
  - `findByAnoFabricacaoBetween`, `findByStatusAndCategoria`
- [ ] **ST019.3.3** - Configurar consultas geográficas:
  - Busca por raio (coordenadas + distância)
  - Busca por região administrativa
  - Busca por estado/cidade
- [ ] **ST019.3.4** - Implementar paginação otimizada com cursor
- [ ] **ST019.3.5** - Configurar busca fuzzy por marca/modelo

#### **T019.4 - Cache Multi-Nível**
**Estimativa:** 4 pontos
- [ ] **ST019.4.1** - Configurar cache L1 (Caffeine) para consultas por placa:
  - TTL 1 hora para dados básicos
  - Invalidação automática em alterações
  - Máximo 10.000 itens em memória
- [ ] **ST019.4.2** - Implementar cache L2 (Redis) para consultas complexas:
  - Cache de listagens por filtros
  - TTL 30 minutos para consultas geográficas
  - Particionamento por região
- [ ] **ST019.4.3** - Configurar cache warming para placas populares:
  - Preload de veículos com sinistros recentes
  - Atualização em background
  - Métricas de hit rate por tipo
- [ ] **ST019.4.4** - Implementar invalidação inteligente:
  - Invalidação por tags (placa, proprietário, região)
  - Invalidação em cascata para dados relacionados
  - Logs de invalidação para debugging

#### **T019.5 - Busca Avançada**
**Estimativa:** 2 pontos
- [ ] **ST019.5.1** - Implementar busca fuzzy por marca/modelo:
  - Algoritmo de distância de Levenshtein
  - Sugestões automáticas de correção
  - Cache de termos populares
- [ ] **ST019.5.2** - Configurar busca por múltiplos critérios:
  - Combinação AND/OR de filtros
  - Ordenação por relevância
  - Faceted search por categoria

---

## **US020 - Sistema de Relacionamentos Veículo-Apólice**
**Estimativa:** 21 pontos | **Prioridade:** Alta

### **📋 TAREFAS FUNCIONAIS**

#### **T020.1 - Modelagem de Relacionamentos**
**Estimativa:** 4 pontos
- [ ] **ST020.1.1** - Definir entidade `VeiculoApoliceRelacionamento`:
  - veiculoId, apoliceId, dataInicio, dataFim, status
  - tipoCobertura, valorCobertura, franquia
- [ ] **ST020.1.2** - Criar value objects:
  - `PeriodoCobertura` com validações de data
  - `ValorCobertura` com moeda e limites
  - `TipoRelacionamento` (PRINCIPAL, ADICIONAL, TEMPORARIO)
- [ ] **ST020.1.3** - Definir regras de negócio:
  - Um veículo pode ter múltiplas apólices (diferentes períodos)
  - Uma apólice pode cobrir múltiplos veículos
  - Validação de sobreposição de períodos
- [ ] **ST020.1.4** - Documentar matriz de compatibilidade:
  - Tipo de veículo x tipo de cobertura
  - Categoria x valor máximo de cobertura

#### **T020.2 - Implementação do Relationship Handler**
**Estimativa:** 6 pontos
- [ ] **ST020.2.1** - Criar `VeiculoApoliceRelationshipHandler`:
  - Handler para `VeiculoAssociadoEvent`
  - Handler para `VeiculoDesassociadoEvent`
  - Handler para `ApoliceCanceladaEvent`
  - Handler para `ApoliceVencidaEvent`
- [ ] **ST020.2.2** - Implementar validações de associação:
  - Verificar compatibilidade veículo x apólice
  - Validar período de cobertura
  - Verificar limites de valor
- [ ] **ST020.2.3** - Configurar eventos automáticos:
  - Associação automática em criação de apólice
  - Desassociação automática em cancelamento
  - Alertas para vencimentos próximos
- [ ] **ST020.2.4** - Implementar sincronização bidirecional:
  - Atualização de dados do veículo na apólice
  - Atualização de dados da apólice no veículo
  - Resolução de conflitos de dados

### **📋 TAREFAS TÉCNICAS**

#### **T020.3 - Validações de Cobertura**
**Estimativa:** 5 pontos
- [ ] **ST020.3.1** - Implementar validador de compatibilidade:
  - Matriz de compatibilidade veículo x cobertura
  - Validação de idade máxima do veículo
  - Verificação de categoria permitida
- [ ] **ST020.3.2** - Criar validador de valor de cobertura:
  - Tabela FIPE para valor de referência
  - Limites por tipo de cobertura
  - Validação de franquia mínima/máxima
- [ ] **ST020.3.3** - Implementar validador de período:
  - Verificação de sobreposição de coberturas
  - Validação de carência entre apólices
  - Controle de gaps de cobertura
- [ ] **ST020.3.4** - Configurar cache de validações:
  - Cache de tabela FIPE (TTL 24 horas)
  - Cache de matriz de compatibilidade
  - Cache de validações por veículo

#### **T020.4 - Histórico e Auditoria**
**Estimativa:** 4 pontos
- [ ] **ST020.4.1** - Implementar histórico completo de relacionamentos:
  - Todas as associações/desassociações
  - Alterações de cobertura
  - Transferências de propriedade
- [ ] **ST020.4.2** - Configurar auditoria de alterações:
  - Log de todas as mudanças
  - Identificação do operador responsável
  - Justificativas obrigatórias
- [ ] **ST020.4.3** - Criar consultas de histórico:
  - Histórico por veículo
  - Histórico por apólice
  - Relatório de alterações por período
- [ ] **ST020.4.4** - Implementar métricas de relacionamento:
  - Taxa de associação/desassociação
  - Tempo médio de cobertura
  - Análise de padrões

#### **T020.5 - Alertas e Monitoramento**
**Estimativa:** 2 pontos
- [ ] **ST020.5.1** - Implementar alertas para veículos sem cobertura:
  - Detecção automática de gaps
  - Notificação para segurado e corretor
  - Dashboard de veículos descobertos
- [ ] **ST020.5.2** - Configurar alertas para inconsistências:
  - Dados divergentes entre veículo e apólice
  - Coberturas incompatíveis
  - Valores fora dos limites
- [ ] **ST020.5.3** - Criar dashboard de relacionamentos:
  - Visão geral de associações ativas
  - Métricas de cobertura por região
  - Alertas em tempo real
- [ ] **ST020.5.4** - Implementar relatórios automáticos:
  - Relatório diário de novos relacionamentos
  - Relatório semanal de inconsistências
  - Relatório mensal de métricas

---

## 📊 **RESUMO DO REFINAMENTO - ÉPICO 3**

### **Distribuição de Tarefas:**
- **US017:** 5 tarefas, 21 subtarefas
- **US018:** 4 tarefas, 15 subtarefas  
- **US019:** 5 tarefas, 21 subtarefas
- **US020:** 5 tarefas, 19 subtarefas

### **Total do Épico 3:**
- **19 Tarefas Principais**
- **76 Subtarefas Detalhadas**
- **76 Story Points**

### **Domínios Implementados:**
- **Veículo:** Aggregate com validações específicas do domínio automotivo
- **Relacionamentos:** Sistema complexo de associações veículo-apólice
- **Validações:** Algoritmos específicos para placa, RENAVAM, chassi
- **Geografia:** Consultas e índices geográficos otimizados

### **Validações Específicas:**
- **Placa Brasileira:** Formato antigo e Mercosul
- **RENAVAM:** Algoritmo oficial de dígito verificador
- **Chassi/VIN:** Formato internacional com validações
- **Ano/Modelo:** Regras específicas da indústria automotiva

### **Padrões Técnicos:**
- **Domain-Driven Design** com agregados específicos
- **Event Sourcing** para rastreabilidade completa
- **CQRS** com projeções geográficas
- **Cache Multi-Nível** para performance
- **Busca Fuzzy** para experiência do usuário

### **Integrações Principais:**
- **Detran** para validação de dados (quando disponível)
- **Tabela FIPE** para valores de referência
- **ViaCEP** para dados geográficos
- **Sistema de Apólices** para relacionamentos
- **Sistema de Notificações** para alertas

### **Características Especiais:**
- **Consultas Geográficas:** Busca por raio, região, estado
- **Cache Inteligente:** Multi-nível com invalidação automática
- **Validações Avançadas:** Algoritmos específicos do domínio
- **Relacionamentos Complexos:** Matriz de compatibilidade
- **Auditoria Completa:** Histórico de todas as alterações

### **Métricas de Performance:**
- **Cache Hit Rate L1:** > 90% para consultas por placa
- **Cache Hit Rate L2:** > 70% para consultas complexas
- **Tempo de Consulta:** < 50ms para busca por placa
- **Tempo de Validação:** < 100ms para validações completas
- **Throughput:** > 1000 consultas/segundo

### **Próximos Passos:**
1. Implementar validações específicas primeiro
2. Desenvolver sistema de cache multi-nível
3. Configurar índices geográficos
4. Implementar relacionamentos com apólices
5. Configurar alertas e monitoramento