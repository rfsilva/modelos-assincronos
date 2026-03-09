# 📖 CAPÍTULO 04: DOMAIN-DRIVEN DESIGN - PARTE 2
## Aggregates e Domain Services

### 🎯 **OBJETIVOS DESTA PARTE**
- Dominar o conceito de Aggregates
- Implementar Domain Services
- Entender boundaries e consistência
- Aplicar padrões de Repository

---

## 🏛️ **AGGREGATES**

### **🎯 Definição e Responsabilidades**

```java
// Localização: aggregate/SinistroAggregate.java
public class SinistroAggregate extends AggregateRoot {
    
    // AGGREGATE ROOT - ponto de entrada único
    private SinistroId id; // Identity
    
    // INVARIANTES DO AGGREGATE
    private SinistroStatus status;
    private ValorMonetario valorEstimado;
    private DataOcorrencia dataOcorrencia;
    
    // ENTITIES FILHAS (dentro do boundary)
    private List<SinistroInteracao> interacoes;
    private List<SinistroDocumento> documentos;
    private List<SinistroAvaliacao> avaliacoes;
    
    // REFERÊNCIAS EXTERNAS (apenas IDs)
    private SeguradoId seguradoId;
    private ApoliceId apoliceId;
    private VeiculoId veiculoId;
    
    /**
     * INVARIANTE: Sinistro só pode ser aprovado se:
     * 1. Status permite aprovação
     * 2. Tem documentos obrigatórios
     * 3. Valor não excede limite da apólice
     * 4. Passou por avaliação técnica
     */
    public void aprovar(ValorMonetario limiteApolice, OperadorId operadorId) {
        
        // Verificar invariantes
        if (!this.status.podeSerAprovado()) {
            throw new SinistroNaoPodeSerAprovadoException(
                "Status atual não permite aprovação: " + this.status
            );
        }
        
        if (!temDocumentosObrigatorios()) {
            throw new DocumentosObrigatoriosAusentesException(
                "Sinistro não possui todos os documentos obrigatórios"
            );
        }
        
        if (this.valorEstimado.ehMaiorQue(limiteApolice)) {
            throw new ValorExcedeLimiteException(
                "Valor estimado excede limite da apólice"
            );
        }
        
        if (!temAvaliacaoTecnica()) {
            throw new AvaliacaoTecnicaAusenteException(
                "Sinistro precisa de avaliação técnica antes da aprovação"
            );
        }
        
        // Aplicar mudança de estado
        this.status = SinistroStatus.APROVADO;
        
        // Registrar interação
        SinistroInteracao aprovacao = SinistroInteracao.aprovacao(
            operadorId, 
            "Sinistro aprovado para pagamento",
            Instant.now()
        );
        this.interacoes.add(aprovacao);
        
        // Gerar evento de domínio
        this.adicionarEvento(new SinistroAprovadoEvent(
            this.id.getValue(),
            this.valorEstimado.getValor(),
            operadorId.getValue(),
            Instant.now()
        ));
    }
    
    /**
     * INVARIANTE: Avaliação só pode ser adicionada se:
     * 1. Sinistro está em análise
     * 2. Perito está qualificado
     * 3. Não há avaliação conflitante
     */
    public void adicionarAvaliacao(PeritoId peritoId, 
                                  ValorMonetario valorAvaliado, 
                                  String observacoes) {
        
        if (!this.status.permiteAvaliacao()) {
            throw new AvaliacaoNaoPermitidaException(
                "Status atual não permite avaliação: " + this.status
            );
        }
        
        // Verificar se já existe avaliação do mesmo perito
        boolean jaAvaliado = this.avaliacoes.stream()
            .anyMatch(av -> av.getPeritoId().equals(peritoId));
            
        if (jaAvaliado) {
            throw new PeritoJaAvalouException(
                "Perito já realizou avaliação para este sinistro"
            );
        }
        
        // Criar avaliação
        SinistroAvaliacao avaliacao = SinistroAvaliacao.criar(
            peritoId,
            valorAvaliado,
            observacoes,
            Instant.now()
        );
        
        this.avaliacoes.add(avaliacao);
        
        // Atualizar valor estimado se necessário
        if (this.valorEstimado == null || 
            valorAvaliado.ehDiferenteDe(this.valorEstimado)) {
            this.valorEstimado = valorAvaliado;
        }
        
        // Evento de domínio
        this.adicionarEvento(new SinistroAvaliadoEvent(
            this.id.getValue(),
            peritoId.getValue(),
            valorAvaliado.getValor(),
            observacoes
        ));
    }
    
    // Métodos de consulta (não alteram estado)
    private boolean temDocumentosObrigatorios() {
        Set<TipoDocumento> obrigatorios = TipoDocumento.getObrigatoriosPorStatus(this.status);
        
        return obrigatorios.stream()
            .allMatch(tipo -> documentos.stream()
                .anyMatch(doc -> doc.getTipo() == tipo && doc.isValido()));
    }
    
    private boolean temAvaliacaoTecnica() {
        return avaliacoes.stream()
            .anyMatch(av -> av.getTipo() == TipoAvaliacao.TECNICA);
    }
    
    public ValorMonetario getValorMedioAvaliacoes() {
        if (avaliacoes.isEmpty()) {
            return ValorMonetario.zero();
        }
        
        BigDecimal soma = avaliacoes.stream()
            .map(av -> av.getValorAvaliado().getValor())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal media = soma.divide(
            BigDecimal.valueOf(avaliacoes.size()), 
            2, 
            RoundingMode.HALF_UP
        );
        
        return ValorMonetario.of(media);
    }
}
```

### **🔄 Aggregate Boundaries**

```java
// Localização: domain/model/boundary/AggregateBoundaryDefinition.java

/**
 * DEFINIÇÃO DE BOUNDARIES DOS AGGREGATES
 * 
 * REGRA: Um aggregate deve ser a menor unidade de consistência
 * 
 * SINISTRO AGGREGATE:
 * ├── SinistroAggregate (ROOT)
 * ├── SinistroInteracao (ENTITY)
 * ├── SinistroDocumento (ENTITY)  
 * ├── SinistroAvaliacao (ENTITY)
 * └── SinistroObservacao (VALUE OBJECT)
 * 
 * FORA DO BOUNDARY (referências por ID):
 * - Segurado (SeguradoId)
 * - Apólice (ApoliceId)
 * - Veículo (VeiculoId)
 * - Perito (PeritoId)
 * 
 * JUSTIFICATIVA:
 * - Interações, documentos e avaliações são parte integral do sinistro
 * - Segurado, apólice e veículo têm ciclo de vida independente
 * - Mudanças no sinistro não afetam diretamente outros aggregates
 */

// Entity filha dentro do boundary
@Entity
public class SinistroInteracao {
    
    private SinistroInteracaoId id;
    private TipoInteracao tipo;
    private OperadorId operadorId;
    private String descricao;
    private Instant dataHora;
    private Map<String, String> metadados;
    
    // Construtor package-private - só pode ser criada pelo aggregate
    SinistroInteracao(TipoInteracao tipo, 
                     OperadorId operadorId, 
                     String descricao, 
                     Instant dataHora) {
        this.id = SinistroInteracaoId.generate();
        this.tipo = Objects.requireNonNull(tipo);
        this.operadorId = Objects.requireNonNull(operadorId);
        this.descricao = Objects.requireNonNull(descricao);
        this.dataHora = Objects.requireNonNull(dataHora);
        this.metadados = new HashMap<>();
    }
    
    // Factory methods específicos para cada tipo
    public static SinistroInteracao atribuicaoOperador(OperadorId operadorId, 
                                                      String motivo, 
                                                      Instant dataHora) {
        return new SinistroInteracao(
            TipoInteracao.ATRIBUICAO_OPERADOR, 
            operadorId, 
            motivo, 
            dataHora
        );
    }
    
    public static SinistroInteracao aprovacao(OperadorId operadorId, 
                                            String observacoes, 
                                            Instant dataHora) {
        return new SinistroInteracao(
            TipoInteracao.APROVACAO, 
            operadorId, 
            observacoes, 
            dataHora
        );
    }
    
    // Comportamentos específicos
    public void adicionarMetadado(String chave, String valor) {
        this.metadados.put(chave, valor);
    }
    
    public boolean isDoTipo(TipoInteracao tipo) {
        return this.tipo == tipo;
    }
    
    // Getters (sem setters - imutabilidade)
    public SinistroInteracaoId getId() { return id; }
    public TipoInteracao getTipo() { return tipo; }
    public OperadorId getOperadorId() { return operadorId; }
    public String getDescricao() { return descricao; }
    public Instant getDataHora() { return dataHora; }
    public Map<String, String> getMetadados() { return new HashMap<>(metadados); }
}
```

---

## 🔧 **DOMAIN SERVICES**

### **🎯 Quando Usar Domain Services**

```java
// Localização: domain/service/RegulacaoService.java
@DomainService
public class RegulacaoService {
    
    private final ApoliceService apoliceService;
    private final FraudeDetectionService fraudeService;
    
    /**
     * DOMAIN SERVICE: Lógica que não pertence a nenhum aggregate específico
     * 
     * Regulação envolve múltiplos aggregates:
     * - Sinistro
     * - Apólice  
     * - Segurado
     * - Veículo
     */
    public RegulacaoResult regularSinistro(SinistroAggregate sinistro, 
                                          Apolice apolice, 
                                          Veiculo veiculo) {
        
        RegulacaoResult.Builder result = RegulacaoResult.builder()
            .sinistroId(sinistro.getId());
        
        // 1. Verificar cobertura da apólice
        CoberturaResult cobertura = verificarCobertura(sinistro, apolice);
        result.coberturaResult(cobertura);
        
        if (!cobertura.isCoberto()) {
            return result
                .aprovado(false)
                .motivoRejeicao("Sinistro não coberto pela apólice: " + cobertura.getMotivo())
                .build();
        }
        
        // 2. Verificar vigência da apólice
        if (!apolice.estaVigenteEm(sinistro.getDataOcorrencia().getValue())) {
            return result
                .aprovado(false)
                .motivoRejeicao("Apólice não estava vigente na data da ocorrência")
                .build();
        }
        
        // 3. Verificar se veículo está coberto
        if (!apolice.cobreVeiculo(veiculo.getId())) {
            return result
                .aprovado(false)
                .motivoRejeicao("Veículo não está coberto pela apólice")
                .build();
        }
        
        // 4. Análise de fraude
        FraudeAnalysisResult fraudeAnalysis = fraudeService.analisar(sinistro, apolice, veiculo);
        result.fraudeAnalysis(fraudeAnalysis);
        
        if (fraudeAnalysis.isAltoRisco()) {
            return result
                .aprovado(false)
                .motivoRejeicao("Sinistro rejeitado por suspeita de fraude")
                .requerInvestigacao(true)
                .build();
        }
        
        // 5. Calcular valor da indenização
        ValorMonetario valorIndenizacao = calcularIndenizacao(sinistro, apolice);
        result.valorIndenizacao(valorIndenizacao);
        
        // 6. Verificar se precisa de aprovação especial
        boolean precisaAprovacaoEspecial = valorIndenizacao.ehMaiorQue(
            apolice.getLimiteAprovacaoAutomatica()
        );
        
        return result
            .aprovado(!precisaAprovacaoEspecial)
            .requerAprovacaoEspecial(precisaAprovacaoEspecial)
            .build();
    }
    
    private CoberturaResult verificarCobertura(SinistroAggregate sinistro, Apolice apolice) {
        
        // Verificar tipo de sinistro vs coberturas da apólice
        TipoSinistro tipoSinistro = sinistro.getTipoSinistro();
        
        Optional<Cobertura> cobertura = apolice.getCoberturas().stream()
            .filter(c -> c.cobreTipoSinistro(tipoSinistro))
            .findFirst();
        
        if (cobertura.isEmpty()) {
            return CoberturaResult.naoCoberto(
                "Tipo de sinistro não coberto pela apólice"
            );
        }
        
        // Verificar limites
        Cobertura cob = cobertura.get();
        if (sinistro.getValorEstimado().ehMaiorQue(cob.getLimiteMaximo())) {
            return CoberturaResult.naoCoberto(
                "Valor excede limite máximo da cobertura"
            );
        }
        
        return CoberturaResult.coberto(cob);
    }
    
    private ValorMonetario calcularIndenizacao(SinistroAggregate sinistro, Apolice apolice) {
        
        ValorMonetario valorSinistro = sinistro.getValorEstimado();
        ValorMonetario franquia = apolice.getFranquia();
        
        // Indenização = Valor do sinistro - Franquia
        if (valorSinistro.ehMenorOuIgualA(franquia)) {
            return ValorMonetario.zero(); // Não há indenização
        }
        
        return valorSinistro.subtrair(franquia);
    }
}

// Localização: domain/service/CalculoIndenizacaoService.java
@DomainService
public class CalculoIndenizacaoService {
    
    /**
     * DOMAIN SERVICE: Cálculo complexo que envolve múltiplas regras
     */
    public ValorMonetario calcularIndenizacao(SinistroAggregate sinistro, 
                                            Apolice apolice, 
                                            LaudoPericial laudo) {
        
        // 1. Valor base do laudo
        ValorMonetario valorBase = laudo.getValorAvaliado();
        
        // 2. Aplicar depreciação se necessário
        if (apolice.temDepreciacao()) {
            DepreciacaoCalculator calculator = new DepreciacaoCalculator();
            valorBase = calculator.aplicarDepreciacao(
                valorBase, 
                sinistro.getVeiculo().getIdadeEmAnos(),
                apolice.getTabelaDepreciacao()
            );
        }
        
        // 3. Verificar limite máximo da cobertura
        ValorMonetario limiteCobertura = apolice.getLimiteCobertura(sinistro.getTipoSinistro());
        if (valorBase.ehMaiorQue(limiteCobertura)) {
            valorBase = limiteCobertura;
        }
        
        // 4. Subtrair franquia
        ValorMonetario franquia = apolice.getFranquia();
        if (valorBase.ehMenorOuIgualA(franquia)) {
            return ValorMonetario.zero();
        }
        
        ValorMonetario indenizacao = valorBase.subtrair(franquia);
        
        // 5. Aplicar participação obrigatória se houver
        if (apolice.temParticipacaoObrigatoria()) {
            BigDecimal percentualParticipacao = apolice.getPercentualParticipacaoObrigatoria();
            ValorMonetario participacao = indenizacao.multiplicar(percentualParticipacao);
            indenizacao = indenizacao.subtrair(participacao);
        }
        
        return indenizacao;
    }
}
```

### **🔄 Application Services vs Domain Services**

```java
// APPLICATION SERVICE - Orquestração e coordenação
@ApplicationService
@Transactional
public class ProcessarSinistroApplicationService {
    
    private final SinistroAggregateRepository sinistroRepository;
    private final RegulacaoService regulacaoService; // DOMAIN SERVICE
    private final EventBus eventBus;
    
    public ProcessarSinistroResult processar(ProcessarSinistroCommand command) {
        
        // 1. Carregar aggregate
        SinistroAggregate sinistro = sinistroRepository.getById(command.getSinistroId());
        
        // 2. Obter dados de outros contextos (via adapters)
        Apolice apolice = apoliceAdapter.obterApolice(sinistro.getApoliceId());
        Veiculo veiculo = veiculoAdapter.obterVeiculo(sinistro.getVeiculoId());
        
        // 3. Usar DOMAIN SERVICE para lógica complexa
        RegulacaoResult regulacao = regulacaoService.regularSinistro(sinistro, apolice, veiculo);
        
        // 4. Aplicar resultado no aggregate
        if (regulacao.isAprovado()) {
            sinistro.aprovar(regulacao.getValorIndenizacao(), command.getOperadorId());
        } else {
            sinistro.rejeitar(regulacao.getMotivoRejeicao(), command.getOperadorId());
        }
        
        // 5. Persistir mudanças
        sinistroRepository.save(sinistro);
        
        // 6. Publicar eventos (infraestrutura)
        sinistro.getUncommittedEvents().forEach(eventBus::publish);
        
        return ProcessarSinistroResult.sucesso(sinistro.getId(), regulacao);
    }
}

// DOMAIN SERVICE - Lógica pura de domínio
@DomainService
public class RegulacaoService {
    // Sem dependências de infraestrutura
    // Apenas lógica de negócio pura
}
```

---

## 🗄️ **REPOSITORY PATTERN**

### **📊 Interface de Repository**

```java
// Localização: aggregate/repository/SinistroAggregateRepository.java
public interface SinistroAggregateRepository {
    
    /**
     * Salva aggregate (com eventos)
     */
    void save(SinistroAggregate aggregate);
    
    /**
     * Busca por ID (com snapshot se disponível)
     */
    Optional<SinistroAggregate> findById(SinistroId id);
    
    /**
     * Busca por ID ou lança exceção
     */
    default SinistroAggregate getById(SinistroId id) {
        return findById(id)
            .orElseThrow(() -> new SinistroNaoEncontradoException(id));
    }
    
    /**
     * Busca por número do sinistro
     */
    Optional<SinistroAggregate> findByNumero(SinistroNumero numero);
    
    /**
     * Verifica existência
     */
    boolean exists(SinistroId id);
    
    /**
     * Remove aggregate (soft delete)
     */
    void delete(SinistroId id);
    
    /**
     * Busca com especificação
     */
    List<SinistroAggregate> findBySpecification(SinistroSpecification spec);
}
```

### **🔍 Specifications Pattern**

```java
// Localização: domain/specification/SinistroSpecification.java
public abstract class SinistroSpecification {
    
    public abstract boolean isSatisfiedBy(SinistroAggregate sinistro);
    
    public SinistroSpecification and(SinistroSpecification other) {
        return new AndSpecification(this, other);
    }
    
    public SinistroSpecification or(SinistroSpecification other) {
        return new OrSpecification(this, other);
    }
    
    public SinistroSpecification not() {
        return new NotSpecification(this);
    }
}

// Especificações concretas
public class SinistroComStatusSpecification extends SinistroSpecification {
    
    private final SinistroStatus status;
    
    public SinistroComStatusSpecification(SinistroStatus status) {
        this.status = status;
    }
    
    @Override
    public boolean isSatisfiedBy(SinistroAggregate sinistro) {
        return sinistro.getStatus().equals(status);
    }
}

public class SinistroComValorAcimaSpecification extends SinistroSpecification {
    
    private final ValorMonetario valorMinimo;
    
    public SinistroComValorAcimaSpecification(ValorMonetario valorMinimo) {
        this.valorMinimo = valorMinimo;
    }
    
    @Override
    public boolean isSatisfiedBy(SinistroAggregate sinistro) {
        return sinistro.getValorEstimado() != null && 
               sinistro.getValorEstimado().ehMaiorQue(valorMinimo);
    }
}

// Uso das especificações
public class SinistroService {
    
    public List<SinistroAggregate> buscarSinistrosParaAprovacaoEspecial() {
        
        SinistroSpecification spec = new SinistroComStatusSpecification(SinistroStatus.EM_ANALISE)
            .and(new SinistroComValorAcimaSpecification(ValorMonetario.of(50000)));
        
        return sinistroRepository.findBySpecification(spec);
    }
}
```

---

## 🧪 **EXERCÍCIO PRÁTICO**

### **🎯 Objetivo**: Implementar Aggregate completo

#### **Passo 1: Definir Boundaries**
```java
// Identificar o que faz parte do Perito Aggregate:
// - PeritoAggregate (ROOT)
// - PeritoEspecialidade (ENTITY)
// - PeritoAvaliacao (ENTITY)
// - PeritoQualificacao (VALUE OBJECT)

// Fora do boundary:
// - Sinistro (SinistroId)
// - Região (RegiaoId)
```

#### **Passo 2: Implementar Domain Service**
```java
@DomainService
public class AtribuicaoPeritoService {
    
    public PeritoId encontrarMelhorPerito(SinistroAggregate sinistro, 
                                         List<PeritoAggregate> peritos) {
        // Lógica para encontrar perito mais adequado
        // baseado em especialidade, localização, disponibilidade
    }
}
```

#### **Passo 3: Testar Invariantes**
```java
@Test
public void testarInvariantesAggregate() {
    SinistroAggregate sinistro = criarSinistroValido();
    
    // Testar invariante: não pode aprovar sem documentos
    assertThatThrownBy(() -> {
        sinistro.aprovar(ValorMonetario.of(10000), OperadorId.of("op123"));
    }).isInstanceOf(DocumentosObrigatoriosAusentesException.class);
    
    // Adicionar documentos e testar novamente
    sinistro.adicionarDocumento(TipoDocumento.BOLETIM_OCORRENCIA, "bo.pdf", conteudo, "user");
    // ... outros documentos
    
    // Agora deve funcionar
    assertThatCode(() -> {
        sinistro.aprovar(ValorMonetario.of(10000), OperadorId.of("op123"));
    }).doesNotThrowAnyException();
}
```

---

## 📚 **CHECKPOINT DE APRENDIZADO**

### **✅ Você deve ser capaz de:**

1. **Definir** boundaries corretos para aggregates
2. **Implementar** invariantes e regras de negócio
3. **Criar** Domain Services para lógica complexa
4. **Usar** Repository pattern adequadamente
5. **Aplicar** Specifications para consultas

### **❓ Perguntas para Reflexão:**

1. Como definir o tamanho ideal de um aggregate?
2. Quando usar Domain Service vs método no aggregate?
3. Como manter consistência entre aggregates?
4. Qual a diferença entre Repository e DAO?

---

## 🔗 **PRÓXIMA PARTE**

Na **Parte 3**, vamos explorar:
- Domain Events e integração
- Factories e Builder patterns
- Políticas de domínio
- Validações complexas

---

**📖 Parte elaborada por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Estimada:** 36 minutos  
**📋 Pré-requisitos:** DDD Parte 1