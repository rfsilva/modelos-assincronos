# Task List - Épico 2: Domínio de Segurados e Apólices

## 📋 Informações Gerais

| Campo | Valor |
|-------|-------|
| **Épico** | Épico 2 - Segurados e Apólices |
| **Total de User Stories** | 8 (US009-US016) |
| **Total de Story Points** | 165 |
| **Duração Estimada** | 8 semanas (4 sprints) |
| **Equipe** | 1 Desenvolvedor Sênior + 2 Desenvolvedores Plenos |
| **Status** | 🟡 Planejado |

---

## 🎯 SPRINT 1: Fundação - Domínio de Segurados (2 semanas)
**Período**: Semanas 1-2  
**Objetivo**: CRUD completo de segurados com Event Sourcing funcionando  
**Story Points**: 47

---

### US009: Aggregate de Segurado com Eventos Ricos (21 pts)

#### Tarefa 1.1: Criar Estrutura de Domínio Base
**Responsável**: Dev Sênior  
**Estimativa**: 3 pts  
**Status**: ⬜ Não Iniciado

**Subtarefas**:
- [ ] Criar package `com.seguradora.hibrida.aggregate.segurado`
- [ ] Definir enum `SeguradoStatus` com valores ATIVO, INATIVO
- [ ] Criar diagrama de máquina de estados para ciclo de vida do segurado
- [ ] Documentar transições válidas de estado
- [ ] Adicionar JavaDoc explicando cada status

**Critérios de Aceite**:
- ✅ Package criado seguindo estrutura do projeto
- ✅ Enum com JavaDoc completo
- ✅ Diagrama de estados documentado

**Arquivos Afetados**:
- `src/main/java/com/seguradora/hibrida/aggregate/segurado/SeguradoStatus.java` (criar)
- `doc/diagramas/segurado-state-machine.mmd` (criar)

---

#### Tarefa 1.2: Implementar Value Objects
**Responsável**: Dev Pleno 1  
**Estimativa**: 5 pts  
**Status**: ⬜ Não Iniciado

**Subtarefas**:
- [ ] Criar classe `Email` com validação RFC 5322
  - [ ] Adicionar construtor com validação
  - [ ] Implementar `equals()` e `hashCode()`
  - [ ] Tornar classe imutável (final)
  - [ ] Adicionar método `getEndereco()`
- [ ] Criar classe `Telefone` com validação brasileira
  - [ ] Validar DDD (2 dígitos)
  - [ ] Validar número (8 ou 9 dígitos)
  - [ ] Adicionar método `getTelefoneCompleto()` formatado
  - [ ] Implementar `equals()` e `hashCode()`
- [ ] Criar classe `Endereco` completa
  - [ ] Validar CEP (8 dígitos)
  - [ ] Validar campos obrigatórios
  - [ ] Validar UF com enum de estados
  - [ ] Implementar `equals()` e `hashCode()`
- [ ] Criar utilitário `CpfValidator`
  - [ ] Implementar validação de dígito verificador
  - [ ] Adicionar método `isValid(String cpf)`
  - [ ] Adicionar testes unitários completos

**Critérios de Aceite**:
- ✅ Todos os Value Objects imutáveis
- ✅ Validações implementadas e testadas
- ✅ Cobertura de testes > 90%
- ✅ JavaDoc completo em todas as classes

**Arquivos Afetados**:
- `src/main/java/com/seguradora/hibrida/aggregate/segurado/Email.java` (criar)
- `src/main/java/com/seguradora/hibrida/aggregate/segurado/Telefone.java` (criar)
- `src/main/java/com/seguradora/hibrida/aggregate/segurado/Endereco.java` (criar)
- `src/main/java/com/seguradora/hibrida/util/CpfValidator.java` (criar)
- `src/test/java/com/seguradora/hibrida/util/CpfValidatorTest.java` (criar)

---

#### Tarefa 1.3: Implementar SeguradoAggregate
**Responsável**: Dev Sênior  
**Estimativa**: 8 pts  
**Status**: ⬜ Não Iniciado  
**Dependências**: Tarefas 1.1, 1.2

**Subtarefas**:
- [ ] Criar classe `SeguradoAggregate extends AggregateRoot`
- [ ] Adicionar campos de estado:
  - [ ] `UUID seguradoId`
  - [ ] `String cpf`
  - [ ] `String nome`
  - [ ] `LocalDate dataNascimento`
  - [ ] `Email email`
  - [ ] `Telefone telefone`
  - [ ] `Endereco endereco`
  - [ ] `SeguradoStatus status`
  - [ ] `LocalDate dataCadastro`
  - [ ] `LocalDate dataUltimaAtualizacao`
  - [ ] `String motivoDesativacao`
- [ ] Implementar factory method `criar()`
  - [ ] Validar invariantes de negócio
  - [ ] Aplicar evento `SeguradoCriadoEvent`
  - [ ] Retornar aggregate criado
- [ ] Implementar método `atualizar()`
  - [ ] Validar status ativo
  - [ ] Detectar mudanças nos campos
  - [ ] Aplicar evento `SeguradoAtualizadoEvent` se houver mudança
- [ ] Implementar método `desativar(String motivo)`
  - [ ] Validar status ativo
  - [ ] Validar motivo não vazio
  - [ ] Aplicar evento `SeguradoDesativadoEvent`
- [ ] Implementar método `reativar()`
  - [ ] Validar status inativo
  - [ ] Aplicar evento `SeguradoReativadoEvent`
- [ ] Adicionar validações de invariantes
  - [ ] CPF válido e não vazio
  - [ ] Nome entre 3-200 caracteres
  - [ ] Idade entre 18-80 anos
  - [ ] Email, telefone, endereço válidos

**Critérios de Aceite**:
- ✅ Aggregate completo com todos os métodos públicos
- ✅ Validações de invariantes funcionando
- ✅ Eventos aplicados corretamente
- ✅ Testes unitários cobrindo todos os cenários
- ✅ Tratamento de exceções adequado

**Arquivos Afetados**:
- `src/main/java/com/seguradora/hibrida/aggregate/segurado/SeguradoAggregate.java` (criar)
- `src/test/java/com/seguradora/hibrida/aggregate/segurado/SeguradoAggregateTest.java` (criar)

---

#### Tarefa 1.4: Criar Domain Events
**Responsável**: Dev Pleno 2  
**Estimativa**: 3 pts  
**Status**: ⬜ Não Iniciado

**Subtarefas**:
- [ ] Criar package `com.seguradora.hibrida.aggregate.segurado.events`
- [ ] Criar `SeguradoCriadoEvent`
  - [ ] Adicionar todos os campos do aggregate
  - [ ] Implementar construtor completo
  - [ ] Adicionar getters
  - [ ] Adicionar timestamp
  - [ ] Implementar interface `DomainEvent`
- [ ] Criar `SeguradoAtualizadoEvent`
  - [ ] Campos: seguradoId, nome, email, telefone, endereco, dataAtualizacao
  - [ ] Implementar interface `DomainEvent`
- [ ] Criar `SeguradoDesativadoEvent`
  - [ ] Campos: seguradoId, motivo, dataDesativacao
  - [ ] Implementar interface `DomainEvent`
- [ ] Criar `SeguradoReativadoEvent`
  - [ ] Campos: seguradoId, dataReativacao
  - [ ] Implementar interface `DomainEvent`
- [ ] Testar serialização JSON de todos os eventos

**Critérios de Aceite**:
- ✅ Todos os eventos implementam `DomainEvent`
- ✅ Eventos serializáveis para JSON
- ✅ Timestamp automático em todos os eventos
- ✅ JavaDoc descrevendo quando cada evento é disparado

**Arquivos Afetados**:
- `src/main/java/com/seguradora/hibrida/aggregate/segurado/events/SeguradoCriadoEvent.java` (criar)
- `src/main/java/com/seguradora/hibrida/aggregate/segurado/events/SeguradoAtualizadoEvent.java` (criar)
- `src/main/java/com/seguradora/hibrida/aggregate/segurado/events/SeguradoDesativadoEvent.java` (criar)
- `src/main/java/com/seguradora/hibrida/aggregate/segurado/events/SeguradoReativadoEvent.java` (criar)

---

#### Tarefa 1.5: Implementar Event Sourcing Handlers
**Responsável**: Dev Sênior  
**Estimativa**: 2 pts  
**Status**: ⬜ Não Iniciado  
**Dependências**: Tarefas 1.3, 1.4

**Subtarefas**:
- [ ] Adicionar `@EventSourcingHandler` para `on(SeguradoCriadoEvent)`
  - [ ] Aplicar todos os campos do evento no aggregate
  - [ ] Definir status como ATIVO
  - [ ] Definir dataCadastro
- [ ] Adicionar handler para `on(SeguradoAtualizadoEvent)`
  - [ ] Atualizar campos modificados
  - [ ] Atualizar dataUltimaAtualizacao
- [ ] Adicionar handler para `on(SeguradoDesativadoEvent)`
  - [ ] Mudar status para INATIVO
  - [ ] Definir motivoDesativacao
  - [ ] Atualizar dataUltimaAtualizacao
- [ ] Adicionar handler para `on(SeguradoReativadoEvent)`
  - [ ] Mudar status para ATIVO
  - [ ] Limpar motivoDesativacao
  - [ ] Atualizar dataUltimaAtualizacao
- [ ] Testar reconstrução de estado via replay de eventos
  - [ ] Criar teste com sequência de eventos
  - [ ] Validar estado final do aggregate

**Critérios de Aceite**:
- ✅ Todos os handlers implementados
- ✅ Reconstrução via Event Sourcing funcionando
- ✅ Testes de replay passando
- ✅ Estado final consistente após replay

**Arquivos Afetados**:
- `src/main/java/com/seguradora/hibrida/aggregate/segurado/SeguradoAggregate.java` (modificar)
- `src/test/java/com/seguradora/hibrida/aggregate/segurado/SeguradoAggregateReplayTest.java` (criar)

---

### US010: Command Handlers para Segurado (13 pts)

#### Tarefa 2.1: Criar Commands
**Responsável**: Dev Pleno 1  
**Estimativa**: 3 pts  
**Status**: ⬜ Não Iniciado

**Subtarefas**:
- [ ] Criar package `com.seguradora.hibrida.command.segurado`
- [ ] Criar `CriarSeguradoCommand`
  - [ ] Adicionar campos: cpf, nome, dataNascimento, email, telefone, endereco
  - [ ] Adicionar Bean Validation annotations
  - [ ] Implementar interface `Command`
  - [ ] Criar builder pattern
- [ ] Criar `AtualizarSeguradoCommand`
  - [ ] Campos: seguradoId, nome, email, telefone, endereco, version
  - [ ] Adicionar Bean Validation
- [ ] Criar `DesativarSeguradoCommand`
  - [ ] Campos: seguradoId, motivo
  - [ ] Validar motivo não vazio
- [ ] Criar `ReativarSeguradoCommand`
  - [ ] Campos: seguradoId

**Critérios de Aceite**:
- ✅ Todos os commands com Bean Validation
- ✅ Builders implementados
- ✅ Interface `Command` implementada
- ✅ JavaDoc descrevendo cada campo

**Arquivos Afetados**:
- `src/main/java/com/seguradora/hibrida/command/segurado/CriarSeguradoCommand.java` (criar)
- `src/main/java/com/seguradora/hibrida/command/segurado/AtualizarSeguradoCommand.java` (criar)
- `src/main/java/com/seguradora/hibrida/command/segurado/DesativarSeguradoCommand.java` (criar)
- `src/main/java/com/seguradora/hibrida/command/segurado/ReativarSeguradoCommand.java` (criar)

---

#### Tarefa 2.2: Implementar CriarSeguradoCommandHandler
**Responsável**: Dev Pleno 1  
**Estimativa**: 4 pts  
**Status**: ⬜ Não Iniciado  
**Dependências**: Tarefa 2.1, US009 completo

**Subtarefas**:
- [ ] Criar `CriarSeguradoCommandHandler implements CommandHandler<CriarSeguradoCommand>`
- [ ] Injetar dependências:
  - [ ] `EventSourcingAggregateRepository<SeguradoAggregate>`
  - [ ] `SeguradoQueryRepository`
- [ ] Implementar método `handle(CriarSeguradoCommand command)`
  - [ ] Limpar e validar CPF
  - [ ] Verificar unicidade de CPF via query repository
  - [ ] Verificar unicidade de email
  - [ ] Construir Value Objects (Email, Telefone, Endereco)
  - [ ] Criar aggregate via factory method
  - [ ] Persistir aggregate via repository
  - [ ] Retornar `CommandResult` com sucesso
- [ ] Implementar tratamento de exceções
  - [ ] `BusinessRuleException` para violações de negócio
  - [ ] Logging adequado de erros
- [ ] Adicionar annotation `@Transactional("writeTransactionManager")`

**Critérios de Aceite**:
- ✅ Handler processando comando com sucesso
- ✅ Validações de unicidade funcionando
- ✅ Value Objects construídos corretamente
- ✅ Transação write correta
- ✅ Testes unitários e de integração passando

**Arquivos Afetados**:
- `src/main/java/com/seguradora/hibrida/command/segurado/CriarSeguradoCommandHandler.java` (criar)
- `src/test/java/com/seguradora/hibrida/command/segurado/CriarSeguradoCommandHandlerTest.java` (criar)
- `src/test/java/com/seguradora/hibrida/command/segurado/CriarSeguradoCommandHandlerIntegrationTest.java` (criar)

---

#### Tarefa 2.3: Implementar Demais Command Handlers
**Responsável**: Dev Pleno 2  
**Estimativa**: 4 pts  
**Status**: ⬜ Não Iniciado  
**Dependências**: Tarefa 2.2

**Subtarefas**:
- [ ] Criar `AtualizarSeguradoCommandHandler`
  - [ ] Carregar aggregate do repository
  - [ ] Validar version para controle de concorrência
  - [ ] Chamar método `atualizar()` do aggregate
  - [ ] Persistir mudanças
- [ ] Criar `DesativarSeguradoCommandHandler`
  - [ ] Carregar aggregate
  - [ ] Chamar método `desativar(motivo)`
  - [ ] Persistir mudanças
- [ ] Criar `ReativarSeguradoCommandHandler`
  - [ ] Carregar aggregate
  - [ ] Chamar método `reativar()`
  - [ ] Persistir mudanças
- [ ] Adicionar testes unitários para cada handler
- [ ] Adicionar testes de integração

**Critérios de Aceite**:
- ✅ Todos os handlers funcionando
- ✅ Controle de concorrência testado
- ✅ Tratamento de erros adequado
- ✅ Cobertura de testes > 85%

**Arquivos Afetados**:
- `src/main/java/com/seguradora/hibrida/command/segurado/AtualizarSeguradoCommandHandler.java` (criar)
- `src/main/java/com/seguradora/hibrida/command/segurado/DesativarSeguradoCommandHandler.java` (criar)
- `src/main/java/com/seguradora/hibrida/command/segurado/ReativarSeguradoCommandHandler.java` (criar)
- `src/test/java/.../...Test.java` (criar testes)

---

#### Tarefa 2.4: Configurar Command Bus
**Responsável**: Dev Sênior  
**Estimativa**: 2 pts  
**Status**: ⬜ Não Iniciado  
**Dependências**: Tarefas 2.2, 2.3

**Subtarefas**:
- [ ] Registrar handlers no `CommandHandlerRegistry`
  - [ ] Configurar descoberta automática via `@Component`
  - [ ] Validar registro de todos os 4 handlers
- [ ] Configurar timeout específico para comandos de segurado
  - [ ] 15 segundos para `CriarSeguradoCommand`
  - [ ] 10 segundos para outros comandos
- [ ] Configurar métricas customizadas
  - [ ] Counter para comandos processados
  - [ ] Timer para latência
  - [ ] Counter para erros
- [ ] Testar roteamento automático
  - [ ] Enviar cada tipo de comando
  - [ ] Validar handler correto executado

**Critérios de Aceite**:
- ✅ Todos os handlers registrados automaticamente
- ✅ Timeout configurado corretamente
- ✅ Métricas sendo coletadas
- ✅ Roteamento funcionando perfeitamente

**Arquivos Afetados**:
- `src/main/resources/command-bus.yml` (modificar - adicionar config segurado)
- `src/main/java/com/seguradora/hibrida/config/Epico2CommandBusConfiguration.java` (criar)

---

### US011: Projeções Otimizadas de Segurado (13 pts)

#### Tarefa 3.1: Criar Query Model (Entity JPA)
**Responsável**: Dev Pleno 1  
**Estimativa**: 3 pts  
**Status**: ⬜ Não Iniciado

**Subtarefas**:
- [ ] Criar package `com.seguradora.hibrida.projection.segurado`
- [ ] Criar `SeguradoQueryModel`
  - [ ] Anotar com `@Entity`
  - [ ] Definir `@Table(name = "segurado_view", schema = "projections")`
  - [ ] Adicionar campos:
    - [ ] `@Id UUID seguradoId`
    - [ ] `@Column(unique = true) String cpf`
    - [ ] `String nome`
    - [ ] `LocalDate dataNascimento`
    - [ ] `@Column(unique = true) String email`
    - [ ] `String telefone`
    - [ ] `@Column(columnDefinition = "TEXT") String endereco`
    - [ ] `@Enumerated(EnumType.STRING) SeguradoStatus status`
    - [ ] `String motivoDesativacao`
    - [ ] `LocalDate dataCadastro`
    - [ ] `LocalDate dataUltimaAtualizacao`
  - [ ] Adicionar getters e setters
  - [ ] Adicionar índices via annotations:
    - [ ] `@Index` para cpf
    - [ ] `@Index` para nome
    - [ ] `@Index` para email
    - [ ] `@Index` para status
    - [ ] `@Index` composto (status, dataCadastro)

**Critérios de Aceite**:
- ✅ Entity JPA corretamente mapeada
- ✅ Índices definidos
- ✅ Schema `projections` configurado
- ✅ Campos unique onde necessário

**Arquivos Afetados**:
- `src/main/java/com/seguradora/hibrida/projection/segurado/SeguradoQueryModel.java` (criar)

---

#### Tarefa 3.2: Criar Migração Flyway
**Responsável**: Dev Pleno 1  
**Estimativa**: 2 pts  
**Status**: ⬜ Não Iniciado

**Subtarefas**:
- [ ] Criar arquivo `V6__Create_Segurado_View.sql`
- [ ] Definir tabela `projections.segurado_view`
  - [ ] Todos os campos com tipos adequados
  - [ ] Constraints: PK, UNIQUE, NOT NULL
- [ ] Criar índices:
  - [ ] `idx_segurado_cpf ON projections.segurado_view(cpf)`
  - [ ] `idx_segurado_nome ON projections.segurado_view(nome)`
  - [ ] `idx_segurado_email ON projections.segurado_view(email)`
  - [ ] `idx_segurado_status ON projections.segurado_view(status)`
  - [ ] `idx_segurado_data_cadastro ON projections.segurado_view(data_cadastro)`
  - [ ] `idx_segurado_status_data ON projections.segurado_view(status, data_cadastro)`
- [ ] Adicionar comentários descritivos
  - [ ] Comentário na tabela
  - [ ] Comentários nas colunas principais
- [ ] Testar migração em ambiente local
  - [ ] Executar Flyway migrate
  - [ ] Validar estrutura criada

**Critérios de Aceite**:
- ✅ Migração executada com sucesso
- ✅ Tabela criada com estrutura correta
- ✅ Índices criados e funcionando
- ✅ Comentários descritivos presentes

**Arquivos Afetados**:
- `src/main/resources/db/migration-projections/V6__Create_Segurado_View.sql` (criar)

---

#### Tarefa 3.3: Implementar SeguradoProjectionHandler
**Responsável**: Dev Pleno 2  
**Estimativa**: 5 pts  
**Status**: ⬜ Não Iniciado  
**Dependências**: Tarefas 3.1, 3.2, US009 completo

**Subtarefas**:
- [ ] Criar `SeguradoProjectionHandler extends AbstractProjectionHandler`
- [ ] Injetar `SeguradoQueryRepository`
- [ ] Implementar `@EventHandler` para `SeguradoCriadoEvent`
  - [ ] Criar novo `SeguradoQueryModel`
  - [ ] Mapear todos os campos do evento
  - [ ] Formatar endereço como string
  - [ ] Persistir no repository
- [ ] Implementar handler para `SeguradoAtualizadoEvent`
  - [ ] Buscar query model existente
  - [ ] Atualizar campos modificados
  - [ ] Persistir mudanças
- [ ] Implementar handler para `SeguradoDesativadoEvent`
  - [ ] Buscar query model
  - [ ] Atualizar status para INATIVO
  - [ ] Definir motivoDesativacao
  - [ ] Persistir
- [ ] Implementar handler para `SeguradoReativadoEvent`
  - [ ] Buscar query model
  - [ ] Atualizar status para ATIVO
  - [ ] Limpar motivoDesativacao
  - [ ] Persistir
- [ ] Adicionar tratamento de erros
  - [ ] Try-catch em cada handler
  - [ ] Logging de erros
  - [ ] Retry automático (via framework)
- [ ] Implementar método auxiliar `formatarEndereco(Endereco)`
- [ ] Anotar classe com `@Component`
- [ ] Implementar método `getProjectionName()` retornando "SeguradoProjection"

**Critérios de Aceite**:
- ✅ Handlers processando todos os eventos
- ✅ Mapeamento correto para query model
- ✅ Tratamento de erros adequado
- ✅ Testes unitários > 85%
- ✅ Projeção sincronizada com write model

**Arquivos Afetados**:
- `src/main/java/com/seguradora/hibrida/projection/segurado/SeguradoProjectionHandler.java` (criar)
- `src/test/java/com/seguradora/hibrida/projection/segurado/SeguradoProjectionHandlerTest.java` (criar)

---

#### Tarefa 3.4: Criar Query Repository
**Responsável**: Dev Pleno 1  
**Estimativa**: 2 pts  
**Status**: ⬜ Não Iniciado  
**Dependências**: Tarefa 3.1

**Subtarefas**:
- [ ] Criar `SeguradoQueryRepository extends JpaRepository<SeguradoQueryModel, UUID>`
- [ ] Adicionar query methods:
  - [ ] `Optional<SeguradoQueryModel> findByCpf(String cpf)`
  - [ ] `boolean existsByCpf(String cpf)`
  - [ ] `boolean existsByEmail(String email)`
  - [ ] `long countByStatus(SeguradoStatus status)`
- [ ] Adicionar query customizada `buscarPorTermo`
  ```java
  @Query("SELECT s FROM SeguradoQueryModel s WHERE " +
         "LOWER(s.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
         "s.cpf LIKE CONCAT('%', :termo, '%')")
  Page<SeguradoQueryModel> buscarPorTermo(@Param("termo") String termo, Pageable pageable);
  ```
- [ ] Adicionar query por status
  - [ ] `Page<SeguradoQueryModel> findByStatus(SeguradoStatus status, Pageable pageable)`
- [ ] Adicionar query por período
  ```java
  @Query("SELECT s FROM SeguradoQueryModel s WHERE s.dataCadastro BETWEEN :inicio AND :fim")
  Page<SeguradoQueryModel> findByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim, Pageable pageable);
  ```

**Critérios de Aceite**:
- ✅ Repository criado e funcionando
- ✅ Todas as queries implementadas
- ✅ Paginação funcionando corretamente
- ✅ Queries otimizadas (usar EXPLAIN ANALYZE)

**Arquivos Afetados**:
- `src/main/java/com/seguradora/hibrida/projection/segurado/SeguradoQueryRepository.java` (criar)

---

#### Tarefa 3.5: Implementar Query Service
**Responsável**: Dev Pleno 2  
**Estimativa**: 1 pt  
**Status**: ⬜ Não Iniciado  
**Dependências**: Tarefas 3.3, 3.4

**Subtarefas**:
- [ ] Criar package `com.seguradora.hibrida.query`
- [ ] Criar `SeguradoQueryService`
- [ ] Injetar `SeguradoQueryRepository`
- [ ] Injetar `CacheManager` (qualifier "readCacheManager")
- [ ] Implementar método `buscarPorCpf(String cpf)`
  - [ ] Anotar com `@Cacheable(value = "segurados", key = "#cpf")`
  - [ ] Retornar `Optional<SeguradoQueryModel>`
- [ ] Implementar método `buscar(...)` com múltiplos filtros
  - [ ] Parâmetros: termo, status, dataInicio, dataFim, pageable
  - [ ] Lógica condicional para aplicar filtros
  - [ ] Retornar `Page<SeguradoQueryModel>`
- [ ] Implementar método `obterEstatisticas()`
  - [ ] Contar ativos e inativos
  - [ ] Retornar `Map<String, Object>`
- [ ] Configurar cache Redis
  - [ ] TTL de 5 minutos
  - [ ] Eviction policy: LRU

**Critérios de Aceite**:
- ✅ Query service funcionando
- ✅ Cache operacional
- ✅ Múltiplos filtros implementados
- ✅ Estatísticas corretas
- ✅ Testes unitários passando

**Arquivos Afetados**:
- `src/main/java/com/seguradora/hibrida/query/SeguradoQueryService.java` (criar)
- `src/test/java/com/seguradora/hibrida/query/SeguradoQueryServiceTest.java` (criar)

---

## 🎯 SPRINT 2: Core - Domínio de Apólices (2 semanas)
**Período**: Semanas 3-4  
**Objetivo**: Emissão de apólice funcionando end-to-end com cálculos automáticos  
**Story Points**: 55

---

### US012: Aggregate de Apólice com Relacionamentos (34 pts)

#### Tarefa 4.1: Criar Estrutura de Domínio
**Responsável**: Dev Sênior  
**Estimativa**: 5 pts  
**Status**: ⬜ Não Iniciado

**Subtarefas**:
- [ ] Criar package `com.seguradora.hibrida.aggregate.apolice`
- [ ] Criar enum `ApoliceStatus`
  - [ ] Valores: VIGENTE, VENCIDA, CANCELADA, SUSPENSA
  - [ ] JavaDoc descrevendo cada status
- [ ] Criar enum `TipoCobertura`
  - [ ] COMPREENSIVA("Compreensiva", 1.0)
  - [ ] TERCEIROS("Responsabilidade Civil", 0.3)
  - [ ] COLISAO("Colisão", 0.6)
  - [ ] ROUBO_FURTO("Roubo e Furto", 0.5)
  - [ ] INCENDIO("Incêndio", 0.2)
  - [ ] VIDROS("Vidros", 0.1)
  - [ ] Adicionar campos: descricao, fatorPremio
- [ ] Criar enum `TipoFranquia`
  - [ ] NORMAL("Normal", 1.0)
  - [ ] REDUZIDA("Reduzida", 1.2)
  - [ ] MAJORADA("Majorada", 0.8)
  - [ ] Adicionar campos: descricao, fatorPremio
- [ ] Criar diagrama de máquina de estados para apólice
- [ ] Documentar transições e regras

**Critérios de Aceite**:
- ✅ Enums completos com fatores de cálculo
- ✅ Diagrama de estados documentado
- ✅ JavaDoc completo em todos os enums

**Arquivos Afetados**:
- `src/main/java/com/seguradora/hibrida/aggregate/apolice/ApoliceStatus.java` (criar)
- `src/main/java/com/seguradora/hibrida/aggregate/apolice/TipoCobertura.java` (criar)
- `src/main/java/com/seguradora/hibrida/aggregate/apolice/TipoFranquia.java` (criar)
- `doc/diagramas/apolice-state-machine.mmd` (criar)

---

#### Tarefa 4.2: Implementar Domain Services
**Responsável**: Dev Pleno 1  
**Estimativa**: 8 pts  
**Status**: ⬜ Não Iniciado  
**Dependências**: Tarefa 4.1

**Subtarefas**:
- [ ] Criar `CalculadoraPremio` service
  - [ ] Anotar com `@Service`
  - [ ] Injetar propriedade `taxa-base` do application.yml (0.05)
  - [ ] Implementar método `calcular()`:
    ```java
    public BigDecimal calcular(
        BigDecimal valorVeiculo,
        Set<TipoCobertura> coberturas,
        TipoFranquia franquia,
        BigDecimal percentualDesconto)
    ```
  - [ ] Calcular prêmio base: `valorVeiculo * taxaBase`
  - [ ] Calcular fator de coberturas: somar `fatorPremio` de cada cobertura
  - [ ] Aplicar fator de coberturas ao prêmio
  - [ ] Aplicar fator de franquia
  - [ ] Aplicar desconto percentual
  - [ ] Arredondar para 2 casas decimais
  - [ ] Adicionar testes unitários com vários cenários
- [ ] Criar `CalculadoraDevolucao` service
  - [ ] Anotar com `@Service`
  - [ ] Implementar método `calcular()`:
    ```java
    public BigDecimal calcular(
        BigDecimal premioTotal,
        LocalDate dataInicio,
        LocalDate dataCancelamento,
        LocalDate dataFim)
    ```
  - [ ] Verificar direito de arrependimento (7 dias):
    - [ ] Se <= 7 dias: retornar prêmio integral
  - [ ] Calcular pro-rata:
    - [ ] `diasTotais = dataFim - dataInicio`
    - [ ] `diasRestantes = dataFim - dataCancelamento`
    - [ ] `proporcao = diasRestantes / diasTotais`
    - [ ] `devolucao = premioTotal * proporcao`
  - [ ] Arredondar para 2 casas decimais
  - [ ] Adicionar testes unitários

**Critérios de Aceite**:
- ✅ CalculadoraPremio funcionando corretamente
- ✅ CalculadoraDevolucao funcionando corretamente
- ✅ Testes cobrindo casos edge
- ✅ Arredondamento correto
- ✅ Cobertura de testes > 90%

**Arquivos Afetados**:
- `src/main/java/com/seguradora/hibrida/aggregate/apolice/CalculadoraPremio.java` (criar)
- `src/main/java/com/seguradora/hibrida/aggregate/apolice/CalculadoraDevolucao.java` (criar)
- `src/test/java/.../CalculadoraPremioTest.java` (criar)
- `src/test/java/.../CalculadoraDevolucaoTest.java` (criar)

---

#### Tarefa 4.3: Implementar ApoliceAggregate
**Responsável**: Dev Sênior + Dev Pleno 1  
**Estimativa**: 12 pts  
**Status**: ⬜ Não Iniciado  
**Dependências**: Tarefas 4.1, 4.2

**Subtarefas**:
- [ ] Criar classe `ApoliceAggregate extends AggregateRoot`
- [ ] Adicionar campos de estado:
  - [ ] `UUID apoliceId`
  - [ ] `String numeroApolice`
  - [ ] `UUID seguradoId`
  - [ ] `UUID veiculoId`
  - [ ] `LocalDate dataInicioVigencia`
  - [ ] `LocalDate dataFimVigencia`
  - [ ] `ApoliceStatus status`
  - [ ] `Set<TipoCobertura> coberturas`
  - [ ] `TipoFranquia franquia`
  - [ ] `BigDecimal valorVeiculo`
  - [ ] `BigDecimal premioTotal`
  - [ ] `Integer numeroParcelas`
  - [ ] `BigDecimal valorParcela`
  - [ ] `BigDecimal percentualDesconto`
  - [ ] Campos de cancelamento
- [ ] Implementar factory method `criar()`
  - [ ] Validar invariantes de negócio
  - [ ] Calcular prêmio via `CalculadoraPremio`
  - [ ] Calcular data fim vigência
  - [ ] Gerar número de apólice (formato APO-AAAA-NNNNNN)
  - [ ] Aplicar evento `ApoliceCriadaEvent`
- [ ] Implementar método `atualizarCobertura()`
  - [ ] Validar status VIGENTE
  - [ ] Recalcular prêmio
  - [ ] Aplicar evento `ApoliceAtualizadaEvent`
- [ ] Implementar método `cancelar()`
  - [ ] Validar status VIGENTE
  - [ ] Validar motivo
  - [ ] Calcular devolução via `CalculadoraDevolucao`
  - [ ] Aplicar evento `ApoliceCanceladaEvent`
- [ ] Implementar método `renovar()`
  - [ ] Validar status (VIGENTE ou VENCIDA)
  - [ ] Criar nova apólice com desconto de renovação (5%)
  - [ ] Retornar novo aggregate
- [ ] Implementar método privado `gerarNumeroApolice()`
  - [ ] Formato: APO-2026-000001
  - [ ] Implementar gerador de sequencial (simples para POC)
- [ ] Adicionar validações de invariantes
  - [ ] Segurado e veículo obrigatórios
  - [ ] Pelo menos uma cobertura
  - [ ] Valor veículo > 0
  - [ ] Data início não no passado
  - [ ] Vigência entre 1-12 meses

**Critérios de Aceite**:
- ✅ Aggregate completo com todos os métodos
- ✅ Cálculos automáticos funcionando
- ✅ Validações de negócio implementadas
- ✅ Geração de número de apólice única
- ✅ Testes unitários > 90%

**Arquivos Afetados**:
- `src/main/java/com/seguradora/hibrida/aggregate/apolice/ApoliceAggregate.java` (criar)
- `src/test/java/com/seguradora/hibrida/aggregate/apolice/ApoliceAggregateTest.java` (criar)

---

#### Tarefa 4.4: Criar Domain Events
**Responsável**: Dev Pleno 2  
**Estimativa**: 5 pts  
**Status**: ⬜ Não Iniciado  
**Dependências**: Tarefa 4.3

**Subtarefas**:
- [ ] Criar package `com.seguradora.hibrida.aggregate.apolice.events`
- [ ] Criar `ApoliceCriadaEvent`
  - [ ] Campos: apoliceId, numeroApolice, seguradoId, veiculoId, coberturas, franquia, valorVeiculo, dataInicio, dataFim, premioTotal, percentualDesconto, dataCriacao
  - [ ] Implementar construtor completo
  - [ ] Implementar interface `DomainEvent`
  - [ ] Adicionar timestamp
- [ ] Criar `ApoliceAtualizadaEvent`
  - [ ] Campos: apoliceId, coberturas, franquia, premioTotal, dataAtualizacao
- [ ] Criar `ApoliceCanceladaEvent`
  - [ ] Campos: apoliceId, dataCancelamento, motivo, valorDevolucao
- [ ] Criar `ApoliceRenovadaEvent` (opcional para v1)
  - [ ] Campos: apoliceOriginalId, novaApoliceId, dataRenovacao
- [ ] Testar serialização JSON de todos os eventos
  - [ ] Garantir que `Set<TipoCobertura>` serializa corretamente
  - [ ] Garantir que `BigDecimal` serializa com 2 decimais

**Critérios de Aceite**:
- ✅ Todos os eventos serializáveis
- ✅ Timestamp automático
- ✅ JavaDoc completo
- ✅ Testes de serialização passando

**Arquivos Afetados**:
- `src/main/java/com/seguradora/hibrida/aggregate/apolice/events/ApoliceCriadaEvent.java` (criar)
- `src/main/java/com/seguradora/hibrida/aggregate/apolice/events/ApoliceAtualizadaEvent.java` (criar)
- `src/main/java/com/seguradora/hibrida/aggregate/apolice/events/ApoliceCanceladaEvent.java` (criar)
- `src/test/java/.../events/EventSerializationTest.java` (criar)

---

#### Tarefa 4.5: Implementar Event Sourcing Handlers
**Responsável**: Dev Sênior  
**Estimativa**: 4 pts  
**Status**: ⬜ Não Iniciado  
**Dependências**: Tarefas 4.3, 4.4

**Subtarefas**:
- [ ] Adicionar `@EventSourcingHandler` para `on(ApoliceCriadaEvent)`
  - [ ] Aplicar todos os campos do evento
  - [ ] Definir status como VIGENTE
- [ ] Adicionar handler para `on(ApoliceAtualizadaEvent)`
  - [ ] Atualizar coberturas, franquia, prêmio
- [ ] Adicionar handler para `on(ApoliceCanceladaEvent)`
  - [ ] Mudar status para CANCELADA
  - [ ] Definir data, motivo, valor devolução
- [ ] Testar reconstrução complexa de estado
  - [ ] Criar apólice
  - [ ] Atualizar cobertura
  - [ ] Cancelar
  - [ ] Validar estado final
- [ ] Testar snapshots com apólice completa
  - [ ] Criar com 100 eventos
  - [ ] Validar snapshot criado automaticamente
  - [ ] Validar reconstrução usa snapshot

**Critérios de Aceite**:
- ✅ Todos os handlers implementados
- ✅ Reconstrução de estado funcionando
- ✅ Snapshots otimizando reconstrução
- ✅ Testes de replay passando

**Arquivos Afetados**:
- `src/main/java/com/seguradora/hibrida/aggregate/apolice/ApoliceAggregate.java` (modificar)
- `src/test/java/.../ApoliceAggregateReplayTest.java` (criar)

---

### US013: Command Handlers para Apólice (21 pts)

**[Continua com estrutura similar às tarefas de Segurado, mas para Apólice...]**

---

## 🎯 SPRINT 3: Consultas e Notificações (2 semanas)
**Período**: Semanas 5-6  
**Objetivo**: Dashboard e notificações automáticas funcionando  
**Story Points**: 42

**[Tarefas detalhadas das US014 e US015...]**

---

## 🎯 SPRINT 4: Analytics e Refinamentos (2 semanas)
**Período**: Semanas 7-8  
**Objetivo**: Sistema otimizado e pronto para produção  
**Story Points**: 21 + tarefas adicionais

**[Tarefas da US016 + otimizações...]**

---

## 📊 Resumo de Progresso

### Por Sprint
| Sprint | User Stories | Tarefas | Status | Pontos |
|--------|-------------|---------|--------|--------|
| Sprint 1 | US009-011 | 15 | ⬜ | 47 |
| Sprint 2 | US012-013 | 12 | ⬜ | 55 |
| Sprint 3 | US014-015 | 10 | ⬜ | 42 |
| Sprint 4 | US016 | 8 + extras | ⬜ | 21 |
| **Total** | **8** | **45+** | **0%** | **165** |

### Por Responsável
| Responsável | Tarefas Atribuídas | Pontos | Status |
|-------------|-------------------|--------|--------|
| Dev Sênior | 18 | 72 | ⬜ 0% |
| Dev Pleno 1 | 14 | 48 | ⬜ 0% |
| Dev Pleno 2 | 13 | 45 | ⬜ 0% |

---

## 🔄 Próximas Atualizações

**Este documento será atualizado**:
- ✅ Diariamente durante as sprints
- ✅ Após cada daily standup
- ✅ Ao completar cada tarefa
- ✅ Ao identificar novos bloqueios

**Versionamento**:
- v1.0 - 09/03/2026 - Versão inicial completa
- v1.1 - TBD - Atualização Sprint 1

---

**Documento gerado automaticamente pelo sistema de especificações Turing Loop**  
**Última atualização**: 09/03/2026 10:48 AM  
**Próximo checkpoint**: Início da Sprint 1