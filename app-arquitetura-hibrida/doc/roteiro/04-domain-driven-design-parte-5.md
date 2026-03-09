# 📖 CAPÍTULO 04: DOMAIN-DRIVEN DESIGN - PARTE 5
## Padrões Avançados e Evolução do Domínio

### 🎯 **OBJETIVOS DESTA PARTE**
- Dominar padrões avançados de DDD
- Implementar evolução de domínio
- Executar refactoring de bounded contexts
- Aplicar boas práticas e lições aprendidas

---

## 🚀 **PADRÕES AVANÇADOS**

### **🎭 Specification Pattern Avançado**

```java
// Localização: domain/specification/CompositeSpecification.java
public abstract class CompositeSpecification<T> {
    
    public abstract boolean isSatisfiedBy(T candidate);
    
    public CompositeSpecification<T> and(CompositeSpecification<T> other) {
        return new AndSpecification<>(this, other);
    }
    
    public CompositeSpecification<T> or(CompositeSpecification<T> other) {
        return new OrSpecification<>(this, other);
    }
    
    public CompositeSpecification<T> not() {
        return new NotSpecification<>(this);
    }
    
    // Método para SQL/JPA queries
    public abstract Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
    
    // Método para explicar por que não foi satisfeita
    public abstract String getFailureReason(T candidate);
}

// Especificação complexa para aprovação de sinistro
public class SinistroAprovacaoSpecification extends CompositeSpecification<SinistroAggregate> {
    
    private final ValorMonetario limiteAutomatico;
    private final Duration periodoSemSinistros;
    private final int maxSinistrosAno;
    
    public SinistroAprovacaoSpecification(ValorMonetario limiteAutomatico,
                                        Duration periodoSemSinistros,
                                        int maxSinistrosAno) {
        this.limiteAutomatico = limiteAutomatico;
        this.periodoSemSinistros = periodoSemSinistros;
        this.maxSinistrosAno = maxSinistrosAno;
    }
    
    @Override
    public boolean isSatisfiedBy(SinistroAggregate sinistro) {
        
        // Especificações individuais
        CompositeSpecification<SinistroAggregate> valorSpec = 
            new ValorDentroLimiteSpecification(limiteAutomatico);
            
        CompositeSpecification<SinistroAggregate> documentosSpec = 
            new DocumentosCompletosSpecification();
            
        CompositeSpecification<SinistroAggregate> historicoSpec = 
            new HistoricoLimpoSpecification(periodoSemSinistros, maxSinistrosAno);
            
        CompositeSpecification<SinistroAggregate> fraudeSpec = 
            new SemIndiciosFraudeSpecification();
        
        // Combinar especificações
        CompositeSpecification<SinistroAggregate> especificacaoCompleta = 
            valorSpec.and(documentosSpec).and(historicoSpec).and(fraudeSpec);
        
        return especificacaoCompleta.isSatisfiedBy(sinistro);
    }
    
    @Override
    public Predicate toPredicate(Root<SinistroAggregate> root, 
                               CriteriaQuery<?> query, 
                               CriteriaBuilder cb) {
        
        // Construir predicado JPA para consultas
        Predicate valorPredicate = cb.lessThanOrEqualTo(
            root.get("valorEstimado").get("valor"), 
            limiteAutomatico.getValor()
        );
        
        Predicate statusPredicate = cb.equal(
            root.get("status"), 
            SinistroStatus.EM_ANALISE
        );
        
        Predicate documentosPredicate = cb.isTrue(
            root.get("temDocumentosObrigatorios")
        );
        
        return cb.and(valorPredicate, statusPredicate, documentosPredicate);
    }
    
    @Override
    public String getFailureReason(SinistroAggregate sinistro) {
        
        List<String> motivos = new ArrayList<>();
        
        if (sinistro.getValorEstimado().ehMaiorQue(limiteAutomatico)) {
            motivos.add("Valor excede limite automático de " + limiteAutomatico.formatado());
        }
        
        if (!sinistro.temDocumentosObrigatorios()) {
            motivos.add("Documentação incompleta");
        }
        
        // Verificar outras condições...
        
        return String.join("; ", motivos);
    }
}

// Factory para especificações complexas
@Component
public class SinistroSpecificationFactory {
    
    private final ConfiguracaoService configuracaoService;
    private final HistoricoSeguradoService historicoService;
    
    public CompositeSpecification<SinistroAggregate> criarEspecificacaoAprovacao(
            SeguradoId seguradoId) {
        
        // Obter configurações dinâmicas
        ValorMonetario limite = configuracaoService.getLimiteAprovacaoAutomatica();
        Duration periodo = configuracaoService.getPeriodoAnaliseHistorico();
        int maxSinistros = configuracaoService.getMaxSinistrosPorAno();
        
        // Obter histórico do segurado
        HistoricoSegurado historico = historicoService.obterHistorico(seguradoId);
        
        // Criar especificação personalizada
        return new SinistroAprovacaoSpecification(limite, periodo, maxSinistros)
            .and(new HistoricoSeguradoSpecification(historico))
            .and(new RegrasNegocioEspeciaisSpecification(seguradoId));
    }
}
```

### **🏭 Domain Service Orchestration**

```java
// Localização: domain/service/SinistroOrchestrationService.java
@DomainService
public class SinistroOrchestrationService {
    
    private final RegulacaoService regulacaoService;
    private final FraudeDetectionService fraudeService;
    private final CalculoIndenizacaoService calculoService;
    private final NotificacaoService notificacaoService;
    
    /**
     * Orquestração complexa: Processamento completo de sinistro
     */
    public ProcessamentoCompletoResult processarSinistroCompleto(
            SinistroAggregate sinistro,
            ProcessamentoContext context) {
        
        ProcessamentoCompletoResult.Builder result = ProcessamentoCompletoResult.builder()
            .sinistroId(sinistro.getId())
            .inicioProcessamento(Instant.now());
        
        try {
            // Fase 1: Análise de Fraude
            FraudeAnalysisResult fraudeResult = fraudeService.analisarCompleto(
                sinistro, context.getHistoricoSegurado(), context.getVeiculo()
            );
            result.fraudeAnalysis(fraudeResult);
            
            if (fraudeResult.isAltoRisco()) {
                return result
                    .status(ProcessamentoStatus.REJEITADO_FRAUDE)
                    .motivoRejeicao("Alto risco de fraude detectado")
                    .build();
            }
            
            // Fase 2: Regulação
            RegulacaoResult regulacaoResult = regulacaoService.regularSinistro(
                sinistro, context.getApolice(), context.getVeiculo()
            );
            result.regulacaoResult(regulacaoResult);
            
            if (!regulacaoResult.isAprovado()) {
                return result
                    .status(ProcessamentoStatus.REJEITADO_REGULACAO)
                    .motivoRejeicao(regulacaoResult.getMotivoRejeicao())
                    .build();
            }
            
            // Fase 3: Cálculo de Indenização
            ValorMonetario indenizacao = calculoService.calcularIndenizacao(
                sinistro, context.getApolice(), regulacaoResult.getLaudo()
            );
            result.valorIndenizacao(indenizacao);
            
            // Fase 4: Verificação de Limites
            if (indenizacao.ehMaiorQue(context.getApolice().getLimiteAprovacaoAutomatica())) {
                return result
                    .status(ProcessamentoStatus.PENDENTE_APROVACAO_MANUAL)
                    .valorIndenizacao(indenizacao)
                    .build();
            }
            
            // Fase 5: Aprovação Automática
            sinistro.aprovar(indenizacao, context.getOperadorId());
            
            // Fase 6: Notificações
            notificacaoService.notificarAprovacao(sinistro, indenizacao);
            
            return result
                .status(ProcessamentoStatus.APROVADO_AUTOMATICAMENTE)
                .valorIndenizacao(indenizacao)
                .build();
                
        } catch (Exception e) {
            log.error("Erro no processamento completo do sinistro {}: {}", 
                     sinistro.getId(), e.getMessage(), e);
            
            return result
                .status(ProcessamentoStatus.ERRO_PROCESSAMENTO)
                .erro(e.getMessage())
                .build();
        } finally {
            result.fimProcessamento(Instant.now());
        }
    }
    
    /**
     * Processamento em lote otimizado
     */
    public List<ProcessamentoCompletoResult> processarLote(
            List<SinistroAggregate> sinistros,
            ProcessamentoContext context) {
        
        // Pré-carregar dados necessários
        preCarregarDados(sinistros, context);
        
        // Processar em paralelo (quando possível)
        return sinistros.parallelStream()
            .map(sinistro -> processarSinistroCompleto(sinistro, context))
            .collect(Collectors.toList());
    }
    
    private void preCarregarDados(List<SinistroAggregate> sinistros, 
                                 ProcessamentoContext context) {
        
        // Pré-carregar dados de segurados
        Set<SeguradoId> seguradoIds = sinistros.stream()
            .map(SinistroAggregate::getSeguradoId)
            .collect(Collectors.toSet());
        context.preCarregarSegurados(seguradoIds);
        
        // Pré-carregar dados de veículos
        Set<VeiculoId> veiculoIds = sinistros.stream()
            .map(SinistroAggregate::getVeiculoId)
            .collect(Collectors.toSet());
        context.preCarregarVeiculos(veiculoIds);
        
        // Pré-carregar apólices
        Set<ApoliceId> apoliceIds = sinistros.stream()
            .map(SinistroAggregate::getApoliceId)
            .collect(Collectors.toSet());
        context.preCarregarApolices(apoliceIds);
    }
}
```

---

## 🔄 **EVOLUÇÃO DO DOMÍNIO**

### **📈 Domain Model Evolution**

```java
// Localização: domain/evolution/DomainModelVersioning.java

/**
 * ESTRATÉGIAS DE EVOLUÇÃO DO MODELO DE DOMÍNIO
 * 
 * 1. VERSIONAMENTO DE AGGREGATES
 * 2. MIGRAÇÃO DE VALUE OBJECTS
 * 3. EVOLUÇÃO DE DOMAIN EVENTS
 * 4. REFACTORING DE BOUNDED CONTEXTS
 */

// Versionamento de Aggregates
public abstract class VersionedAggregate extends AggregateRoot {
    
    protected int modelVersion;
    
    protected VersionedAggregate() {
        this.modelVersion = getCurrentModelVersion();
    }
    
    protected abstract int getCurrentModelVersion();
    
    /**
     * Migração automática quando aggregate é carregado
     */
    @PostLoad
    public void migrateIfNeeded() {
        
        int currentVersion = getCurrentModelVersion();
        
        if (this.modelVersion < currentVersion) {
            log.info("Migrando aggregate {} da versão {} para {}", 
                    getId(), this.modelVersion, currentVersion);
            
            performMigration(this.modelVersion, currentVersion);
            this.modelVersion = currentVersion;
        }
    }
    
    protected abstract void performMigration(int fromVersion, int toVersion);
}

// Implementação para Sinistro
public class SinistroAggregate extends VersionedAggregate {
    
    private static final int CURRENT_MODEL_VERSION = 3;
    
    // Campos da versão atual
    private SinistroId id;
    private SinistroNumero numero;
    private SinistroStatus status;
    
    // Novos campos adicionados na versão 2
    private TipoSinistro tipo; // Adicionado na v2
    private PrioridadeSinistro prioridade; // Adicionado na v2
    
    // Novos campos adicionados na versão 3
    private ScoreFraude scoreFraude; // Adicionado na v3
    private List<TagSinistro> tags; // Adicionado na v3
    
    @Override
    protected int getCurrentModelVersion() {
        return CURRENT_MODEL_VERSION;
    }
    
    @Override
    protected void performMigration(int fromVersion, int toVersion) {
        
        for (int version = fromVersion + 1; version <= toVersion; version++) {
            
            switch (version) {
                case 2 -> migrateTo_v2();
                case 3 -> migrateTo_v3();
                default -> log.warn("Migração desconhecida para versão {}", version);
            }
        }
    }
    
    private void migrateTo_v2() {
        
        // Determinar tipo baseado em dados existentes
        if (this.tipo == null) {
            this.tipo = determinarTipoBaseadoEmDescricao();
        }
        
        // Definir prioridade padrão
        if (this.prioridade == null) {
            this.prioridade = PrioridadeSinistro.NORMAL;
        }
        
        log.debug("Sinistro {} migrado para v2: tipo={}, prioridade={}", 
                 id, tipo, prioridade);
    }
    
    private void migrateTo_v3() {
        
        // Calcular score de fraude inicial
        if (this.scoreFraude == null) {
            this.scoreFraude = calcularScoreFraudeInicial();
        }
        
        // Inicializar tags vazias
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        
        log.debug("Sinistro {} migrado para v3: scoreFraude={}", id, scoreFraude);
    }
    
    private TipoSinistro determinarTipoBaseadoEmDescricao() {
        // Lógica para determinar tipo baseado em dados existentes
        String descricao = this.getDescricao().getTexto().toLowerCase();
        
        if (descricao.contains("roubo") || descricao.contains("furto")) {
            return TipoSinistro.ROUBO_FURTO;
        } else if (descricao.contains("colisão") || descricao.contains("batida")) {
            return TipoSinistro.COLISAO;
        } else {
            return TipoSinistro.OUTROS;
        }
    }
}

// Migração de Value Objects
public class ValueObjectMigration {
    
    /**
     * Migração de CPF para incluir validação de órgão emissor
     */
    public static Cpf migrateCpf_v1_to_v2(String cpfAntigo) {
        
        // Na v1, CPF era apenas string
        // Na v2, CPF inclui validação e órgão emissor
        
        return Cpf.builder()
            .numero(cpfAntigo)
            .orgaoEmissor(determinarOrgaoEmissor(cpfAntigo))
            .dataEmissao(estimarDataEmissao(cpfAntigo))
            .build();
    }
    
    /**
     * Migração de Endereço para incluir coordenadas
     */
    public static Endereco migrateEndereco_v2_to_v3(EnderecoV2 enderecoAntigo) {
        
        // Na v3, Endereço inclui coordenadas geográficas
        Coordenadas coordenadas = geocodingService.obterCoordenadas(
            enderecoAntigo.getCep()
        );
        
        return Endereco.builder()
            .logradouro(enderecoAntigo.getLogradouro())
            .numero(enderecoAntigo.getNumero())
            .complemento(enderecoAntigo.getComplemento())
            .bairro(enderecoAntigo.getBairro())
            .cidade(enderecoAntigo.getCidade())
            .estado(enderecoAntigo.getEstado())
            .cep(enderecoAntigo.getCep())
            .coordenadas(coordenadas) // Novo campo
            .build();
    }
}
```

### **🔧 Bounded Context Refactoring**

```java
// Localização: domain/refactoring/BoundedContextRefactoring.java

/**
 * REFACTORING DE BOUNDED CONTEXTS
 * 
 * Cenário: Separar contexto de Perícia do contexto de Sinistros
 */

// Antes: Perícia dentro do contexto de Sinistros
public class SinistroAggregate_Old {
    
    private SinistroId id;
    private List<SinistroAvaliacao> avaliacoes; // Será movido
    private List<PeritoAtribuicao> peritos;     // Será movido
    
    public void atribuirPerito(PeritoId peritoId) {
        // Lógica que será movida para contexto de Perícia
    }
    
    public void adicionarAvaliacao(Avaliacao avaliacao) {
        // Lógica que será movida para contexto de Perícia
    }
}

// Depois: Contextos separados
public class SinistroAggregate_New {
    
    private SinistroId id;
    // Avaliacoes e peritos removidos
    
    // Apenas referência para o contexto de Perícia
    private PericiaId periciaId; // Referência externa
    
    public void solicitarPericia(TipoPericia tipo, Urgencia urgencia) {
        
        // Gerar evento para contexto de Perícia
        this.adicionarEvento(new PericiasolicitadaEvent(
            this.id,
            tipo,
            urgencia,
            this.getEnderecoOcorrencia(),
            this.getValorEstimado()
        ));
    }
}

// Novo contexto de Perícia
public class PericiaAggregate {
    
    private PericiaId id;
    private SinistroId sinistroId; // Referência para Sinistros
    private List<PeritoAtribuicao> peritos;
    private List<Avaliacao> avaliacoes;
    private StatusPericia status;
    
    public static PericiaAggregate iniciar(PericiasolicitadaEvent evento) {
        
        PericiaAggregate pericia = new PericiaAggregate();
        pericia.id = PericiaId.generate();
        pericia.sinistroId = SinistroId.of(evento.getSinistroId());
        pericia.status = StatusPericia.SOLICITADA;
        pericia.peritos = new ArrayList<>();
        pericia.avaliacoes = new ArrayList<>();
        
        // Gerar evento de perícia iniciada
        pericia.adicionarEvento(new PericiaIniciadaEvent(
            pericia.id,
            evento.getSinistroId(),
            evento.getTipo(),
            evento.getUrgencia()
        ));
        
        return pericia;
    }
    
    public void atribuirPerito(PeritoId peritoId, TipoAvaliacao tipoAvaliacao) {
        
        // Validações específicas do contexto de Perícia
        if (this.status != StatusPericia.SOLICITADA) {
            throw new PericiaJaIniciadaException();
        }
        
        if (jaTemPeritoParaTipo(tipoAvaliacao)) {
            throw new PeritoJaAtribuidoException(tipoAvaliacao);
        }
        
        PeritoAtribuicao atribuicao = PeritoAtribuicao.criar(
            peritoId, tipoAvaliacao, Instant.now()
        );
        
        this.peritos.add(atribuicao);
        this.status = StatusPericia.EM_ANDAMENTO;
        
        // Evento para notificar outros contextos
        this.adicionarEvento(new PeritoAtribuidoEvent(
            this.id,
            this.sinistroId,
            peritoId,
            tipoAvaliacao
        ));
    }
}

// Migração de dados entre contextos
@Component
public class ContextMigrationService {
    
    private final SinistroRepository sinistroRepository;
    private final PericiaRepository periciaRepository;
    
    /**
     * Migração: Mover dados de perícia de Sinistros para contexto próprio
     */
    @Transactional
    public MigrationResult migratePericiaData() {
        
        MigrationResult result = new MigrationResult();
        
        // Buscar todos os sinistros com dados de perícia
        List<SinistroAggregate> sinistrosComPericia = sinistroRepository
            .findSinistrosComAvaliacoes();
        
        for (SinistroAggregate sinistro : sinistrosComPericia) {
            
            try {
                // Criar aggregate de Perícia
                PericiaAggregate pericia = criarPericiaAPartirDeSinistro(sinistro);
                
                // Salvar no novo contexto
                periciaRepository.save(pericia);
                
                // Limpar dados de perícia do sinistro
                sinistro.removerDadosPericia();
                sinistro.setPericiaId(pericia.getId());
                
                // Salvar sinistro atualizado
                sinistroRepository.save(sinistro);
                
                result.incrementSuccess();
                
            } catch (Exception e) {
                log.error("Erro ao migrar perícia do sinistro {}: {}", 
                         sinistro.getId(), e.getMessage());
                result.incrementError();
            }
        }
        
        return result;
    }
    
    private PericiaAggregate criarPericiaAPartirDeSinistro(SinistroAggregate sinistro) {
        
        PericiaAggregate pericia = new PericiaAggregate();
        pericia.setId(PericiaId.generate());
        pericia.setSinistroId(sinistro.getId());
        
        // Migrar avaliações
        for (SinistroAvaliacao avaliacaoAntiga : sinistro.getAvaliacoes()) {
            Avaliacao avaliacaoNova = Avaliacao.builder()
                .peritoId(avaliacaoAntiga.getPeritoId())
                .tipoAvaliacao(avaliacaoAntiga.getTipo())
                .valorAvaliado(avaliacaoAntiga.getValorAvaliado())
                .observacoes(avaliacaoAntiga.getObservacoes())
                .dataAvaliacao(avaliacaoAntiga.getDataAvaliacao())
                .build();
                
            pericia.adicionarAvaliacao(avaliacaoNova);
        }
        
        // Migrar atribuições de perito
        for (PeritoAtribuicao atribuicaoAntiga : sinistro.getPeritos()) {
            pericia.atribuirPerito(
                atribuicaoAntiga.getPeritoId(),
                atribuicaoAntiga.getTipoAvaliacao()
            );
        }
        
        return pericia;
    }
}
```

---

## 📚 **BOAS PRÁTICAS E LIÇÕES APRENDIDAS**

### **✅ Checklist de DDD**

```java
// Localização: domain/guidelines/DDDGuidelines.java

/**
 * DIRETRIZES DE DOMAIN-DRIVEN DESIGN
 * 
 * ✅ MODELAGEM DE DOMÍNIO
 * - [ ] Linguagem ubíqua definida e documentada
 * - [ ] Bounded contexts claramente delimitados
 * - [ ] Aggregates com boundaries corretos
 * - [ ] Value objects imutáveis e válidos
 * - [ ] Domain events expressivos
 * 
 * ✅ IMPLEMENTAÇÃO
 * - [ ] Entities com identidade clara
 * - [ ] Repositories com interface de domínio
 * - [ ] Domain services para lógica complexa
 * - [ ] Factories para criação complexa
 * - [ ] Specifications para regras de negócio
 * 
 * ✅ INTEGRAÇÃO
 * - [ ] Anti-corruption layers para sistemas externos
 * - [ ] Published language para integração
 * - [ ] Context mapping documentado
 * - [ ] Shared kernel minimizado
 * 
 * ✅ EVOLUÇÃO
 * - [ ] Versionamento de aggregates
 * - [ ] Migração de dados planejada
 * - [ ] Refactoring de contextos documentado
 * - [ ] Backward compatibility considerada
 */

@Component
public class DomainModelValidator {
    
    /**
     * Validação automática do modelo de domínio
     */
    public ValidationReport validateDomainModel() {
        
        ValidationReport report = new ValidationReport();
        
        // Validar aggregates
        report.addSection("Aggregates", validateAggregates());
        
        // Validar value objects
        report.addSection("Value Objects", validateValueObjects());
        
        // Validar domain events
        report.addSection("Domain Events", validateDomainEvents());
        
        // Validar bounded contexts
        report.addSection("Bounded Contexts", validateBoundedContexts());
        
        return report;
    }
    
    private List<ValidationIssue> validateAggregates() {
        
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Verificar se aggregates têm identidade
        // Verificar se invariantes estão implementadas
        // Verificar se boundaries estão corretos
        // Verificar se eventos são gerados adequadamente
        
        return issues;
    }
    
    private List<ValidationIssue> validateValueObjects() {
        
        List<ValidationIssue> issues = new ArrayList<>();
        
        // Verificar imutabilidade
        // Verificar validação no construtor
        // Verificar equals/hashCode implementados
        // Verificar se não têm identidade
        
        return issues;
    }
}

// Métricas de qualidade do domínio
@Component
public class DomainQualityMetrics {
    
    public DomainQualityReport generateReport() {
        
        return DomainQualityReport.builder()
            .aggregateComplexity(calculateAggregateComplexity())
            .valueObjectCoverage(calculateValueObjectCoverage())
            .domainEventRichness(calculateDomainEventRichness())
            .boundedContextCohesion(calculateBoundedContextCohesion())
            .linguagemUbiquaUsage(calculateLinguagemUbiquaUsage())
            .build();
    }
    
    private double calculateAggregateComplexity() {
        // Medir complexidade ciclomática dos aggregates
        // Contar número de métodos por aggregate
        // Analisar profundidade de herança
        return 0.0;
    }
    
    private double calculateValueObjectCoverage() {
        // Percentual de primitivos encapsulados em value objects
        // Verificar uso de String vs value objects específicos
        return 0.0;
    }
}
```

### **🎯 Padrões de Nomenclatura**

```java
/**
 * CONVENÇÕES DE NOMENCLATURA DDD
 * 
 * AGGREGATES:
 * - Substantivos no singular
 * - Sufixo "Aggregate" opcional
 * - Exemplos: Sinistro, SinistroAggregate, Perito
 * 
 * VALUE OBJECTS:
 * - Substantivos descritivos
 * - Sem sufixos especiais
 * - Exemplos: Cpf, Email, ValorMonetario, SinistroNumero
 * 
 * DOMAIN EVENTS:
 * - Verbo no passado + substantivo
 * - Sufixo "Event"
 * - Exemplos: SinistroCriadoEvent, PeritoAtribuidoEvent
 * 
 * DOMAIN SERVICES:
 * - Substantivo + "Service"
 * - Foco na ação/processo
 * - Exemplos: RegulacaoService, CalculoIndenizacaoService
 * 
 * REPOSITORIES:
 * - Nome do aggregate + "Repository"
 * - Interface no domínio, implementação na infraestrutura
 * - Exemplos: SinistroRepository, PeritoRepository
 * 
 * SPECIFICATIONS:
 * - Descrição da condição + "Specification"
 * - Exemplos: SinistroAprovacaoSpecification, ValorDentroLimiteSpecification
 */
```

---

## 🧪 **EXERCÍCIO FINAL**

### **🎯 Objetivo**: Implementar DDD completo para nova funcionalidade

#### **Passo 1: Modelar Novo Bounded Context**
```java
// Implementar contexto de "Ressarcimento"
public class RessarcimentoAggregate extends AggregateRoot {
    
    private RessarcimentoId id;
    private SinistroId sinistroOrigem;
    private TerceiroPagador terceiro;
    private ValorMonetario valorRessarcimento;
    private StatusRessarcimento status;
    
    // Comportamentos de domínio
    public void iniciarCobranca(TerceiroPagador terceiro, ValorMonetario valor) {
        // Implementar regras de negócio
    }
    
    public void receberPagamento(ValorMonetario valorPago, FormaPagamento forma) {
        // Implementar lógica de recebimento
    }
}
```

#### **Passo 2: Criar Value Objects Específicos**
```java
public class TerceiroPagador {
    private final TipoTerceiro tipo; // PESSOA_FISICA, PESSOA_JURIDICA, SEGURADORA
    private final String documento; // CPF ou CNPJ
    private final String nome;
    private final Endereco endereco;
    
    // Validações específicas do domínio
}

public class StatusRessarcimento {
    // Estados específicos do ressarcimento
    // INICIADO, EM_COBRANCA, PAGO_PARCIAL, PAGO_TOTAL, CANCELADO
}
```

#### **Passo 3: Implementar Domain Services**
```java
@DomainService
public class RessarcimentoCalculationService {
    
    public ValorMonetario calcularValorRessarcimento(SinistroAggregate sinistro,
                                                   PercentualCulpa percentualTerceiro) {
        // Lógica complexa de cálculo
    }
}
```

#### **Passo 4: Testar Integração Completa**
```java
@Test
public void testarFluxoCompletoRessarcimento() {
    // 1. Criar sinistro
    // 2. Identificar terceiro responsável
    // 3. Calcular valor de ressarcimento
    // 4. Iniciar cobrança
    // 5. Processar pagamento
    // 6. Verificar eventos gerados
    // 7. Validar integrações com outros contextos
}
```

---

## 🎓 **CONCLUSÃO DO CAPÍTULO DDD**

### **✅ Competências Adquiridas:**

1. **Modelagem** - Bounded contexts, aggregates, value objects
2. **Implementação** - Domain services, repositories, specifications
3. **Integração** - Anti-corruption layers, context mapping
4. **Evolução** - Versionamento, migração, refactoring
5. **Qualidade** - Boas práticas, métricas, validação

### **🚀 Próximo Capítulo**

No **Capítulo 05 - Command Bus**, vamos explorar:
- Implementação detalhada do Command Bus
- Padrões de Command Handlers
- Validação e middleware
- Performance e escalabilidade

---

**📖 Capítulo elaborado por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Total:** 3 horas (5 partes × 36 minutos)  
**📋 Pré-requisitos:** Capítulos 01-03 completos