# 📖 CAPÍTULO 04: DOMAIN-DRIVEN DESIGN - PARTE 1
## Fundamentos e Modelagem de Domínio

### 🎯 **OBJETIVOS DESTA PARTE**
- Compreender os princípios fundamentais do DDD
- Entender a importância da linguagem ubíqua
- Explorar bounded contexts no projeto
- Conhecer os building blocks táticos do DDD

---

## 🧠 **FUNDAMENTOS DO DDD**

### **📚 O que é Domain-Driven Design?**

**Domain-Driven Design (DDD)** é uma abordagem de desenvolvimento de software que coloca o **domínio de negócio** no centro do design, promovendo uma colaboração próxima entre especialistas técnicos e de domínio.

### **🎯 Princípios Fundamentais**

#### **1. Foco no Domínio Central**
```java
// ❌ Modelo anêmico - apenas dados
public class Sinistro {
    private String id;
    private String status;
    private BigDecimal valor;
    // apenas getters e setters
}

// ✅ Modelo rico - comportamento do domínio
public class SinistroAggregate extends AggregateRoot {
    
    private SinistroId id;
    private SinistroStatus status;
    private ValorMonetario valorEstimado;
    private Segurado segurado;
    private Veiculo veiculo;
    
    /**
     * Regra de negócio: Sinistro só pode ser aprovado se valor < limite da apólice
     */
    public void aprovar(ValorMonetario limiteApolice, String operadorId) {
        
        if (!this.status.podeSerAprovado()) {
            throw new SinistroNaoPodeSerAprovadoException(
                "Sinistro no status " + status + " não pode ser aprovado"
            );
        }
        
        if (this.valorEstimado.ehMaiorQue(limiteApolice)) {
            throw new ValorExcedeLimiteException(
                "Valor estimado excede limite da apólice"
            );
        }
        
        // Aplicar regra de negócio
        this.status = SinistroStatus.APROVADO;
        this.adicionarEvento(new SinistroAprovadoEvent(
            this.id.getValue(),
            this.valorEstimado.getValor(),
            operadorId,
            Instant.now()
        ));
    }
    
    /**
     * Regra de negócio: Sinistro de alto valor precisa de aprovação especial
     */
    public boolean precisaAprovacaoEspecial() {
        return this.valorEstimado.ehMaiorQue(ValorMonetario.de(50000));
    }
}
```

#### **2. Linguagem Ubíqua**
```java
// Localização: domain/model/linguagem-ubiqua.md

/**
 * GLOSSÁRIO - LINGUAGEM UBÍQUA DO DOMÍNIO DE SINISTROS
 * 
 * SINISTRO: Evento coberto pela apólice que gera direito à indenização
 * SEGURADO: Pessoa física ou jurídica que contratou o seguro
 * APÓLICE: Contrato de seguro entre seguradora e segurado
 * FRANQUIA: Valor que fica por conta do segurado em caso de sinistro
 * PERITO: Profissional que avalia danos e estima valores
 * REGULAÇÃO: Processo de análise e aprovação do sinistro
 * INDENIZAÇÃO: Valor pago pela seguradora ao segurado
 * 
 * ESTADOS DO SINISTRO:
 * - ABERTO: Sinistro registrado, aguardando análise
 * - EM_ANALISE: Sinistro sendo analisado por operador
 * - AGUARDANDO_PERITO: Aguardando avaliação pericial
 * - AGUARDANDO_DOCUMENTOS: Faltam documentos do segurado
 * - APROVADO: Sinistro aprovado para pagamento
 * - REJEITADO: Sinistro rejeitado (não coberto)
 * - PAGO: Indenização paga ao segurado
 * - CANCELADO: Sinistro cancelado pelo segurado
 */

// Implementação usando linguagem ubíqua
public enum SinistroStatus {
    
    ABERTO("Aberto", "Sinistro registrado, aguardando análise"),
    EM_ANALISE("Em Análise", "Sinistro sendo analisado por operador"),
    AGUARDANDO_PERITO("Aguardando Perito", "Aguardando avaliação pericial"),
    AGUARDANDO_DOCUMENTOS("Aguardando Documentos", "Faltam documentos do segurado"),
    APROVADO("Aprovado", "Sinistro aprovado para pagamento"),
    REJEITADO("Rejeitado", "Sinistro rejeitado - não coberto"),
    PAGO("Pago", "Indenização paga ao segurado"),
    CANCELADO("Cancelado", "Sinistro cancelado pelo segurado");
    
    private final String displayName;
    private final String descricao;
    
    // Regras de transição usando linguagem do domínio
    public boolean podeSerAprovado() {
        return this == EM_ANALISE || this == AGUARDANDO_PERITO;
    }
    
    public boolean podeSerRejeitado() {
        return this == EM_ANALISE || this == AGUARDANDO_PERITO || this == AGUARDANDO_DOCUMENTOS;
    }
    
    public boolean podeSerPago() {
        return this == APROVADO;
    }
    
    public boolean estaNaRegulacao() {
        return this == EM_ANALISE || this == AGUARDANDO_PERITO || this == AGUARDANDO_DOCUMENTOS;
    }
}
```

---

## 🏗️ **BOUNDED CONTEXTS**

### **🎯 Identificação de Contextos**

```java
// Localização: domain/context/BoundedContextMap.java

/**
 * MAPA DE CONTEXTOS DELIMITADOS
 * 
 * 1. CONTEXTO DE SINISTROS (Core Domain)
 *    - Responsabilidade: Gestão completa do ciclo de vida dos sinistros
 *    - Entidades: Sinistro, Ocorrencia, Regulacao
 *    - Serviços: SinistroService, RegulacaoService
 * 
 * 2. CONTEXTO DE SEGURADOS (Supporting Domain)
 *    - Responsabilidade: Gestão de dados dos segurados
 *    - Entidades: Segurado, Endereco, Contato
 *    - Serviços: SeguradoService
 * 
 * 3. CONTEXTO DE APÓLICES (Supporting Domain)
 *    - Responsabilidade: Gestão de contratos de seguro
 *    - Entidades: Apolice, Cobertura, Premio
 *    - Serviços: ApoliceService
 * 
 * 4. CONTEXTO DE VEÍCULOS (Supporting Domain)
 *    - Responsabilidade: Gestão de dados dos veículos
 *    - Entidades: Veiculo, Modelo, Marca
 *    - Serviços: VeiculoService
 * 
 * 5. CONTEXTO DE PERÍCIA (Supporting Domain)
 *    - Responsabilidade: Avaliação técnica de sinistros
 *    - Entidades: Perito, Laudo, Avaliacao
 *    - Serviços: PericiaService
 * 
 * 6. CONTEXTO DE PAGAMENTOS (Generic Domain)
 *    - Responsabilidade: Processamento de pagamentos
 *    - Entidades: Pagamento, ContaBancaria
 *    - Serviços: PagamentoService
 */

// Implementação do contexto principal
@BoundedContext("Sinistros")
public class SinistroContext {
    
    // Aggregate Root do contexto
    private final SinistroAggregateRepository sinistroRepository;
    
    // Domain Services específicos do contexto
    private final RegulacaoService regulacaoService;
    private final CalculoIndenizacaoService calculoService;
    
    // Anti-Corruption Layer para outros contextos
    private final SeguradoContextAdapter seguradoAdapter;
    private final ApoliceContextAdapter apoliceAdapter;
    private final VeiculoContextAdapter veiculoAdapter;
    
    /**
     * Caso de uso principal: Processar sinistro completo
     */
    public ProcessamentoSinistroResult processarSinistro(ProcessarSinistroCommand command) {
        
        // 1. Carregar aggregate
        SinistroAggregate sinistro = sinistroRepository.getById(command.getSinistroId());
        
        // 2. Obter dados de outros contextos via adapters
        Segurado segurado = seguradoAdapter.obterSegurado(sinistro.getCpfSegurado());
        Apolice apolice = apoliceAdapter.obterApoliceVigente(segurado.getId(), sinistro.getDataOcorrencia());
        Veiculo veiculo = veiculoAdapter.obterVeiculo(sinistro.getPlacaVeiculo());
        
        // 3. Aplicar regras de negócio do domínio
        RegulacaoResult regulacao = regulacaoService.regularSinistro(sinistro, apolice, veiculo);
        
        if (regulacao.isAprovado()) {
            ValorMonetario indenizacao = calculoService.calcularIndenizacao(
                sinistro, apolice, regulacao.getLaudo()
            );
            
            sinistro.aprovar(indenizacao, command.getOperadorId());
        } else {
            sinistro.rejeitar(regulacao.getMotivoRejeicao(), command.getOperadorId());
        }
        
        // 4. Persistir mudanças
        sinistroRepository.save(sinistro);
        
        return ProcessamentoSinistroResult.sucesso(sinistro.getId(), regulacao);
    }
}
```

### **🔄 Anti-Corruption Layer**

```java
// Localização: domain/adapter/SeguradoContextAdapter.java
@Component
public class SeguradoContextAdapter {
    
    private final SeguradoExternalService seguradoExternalService;
    
    /**
     * Traduz modelo externo para modelo do domínio de sinistros
     */
    public Segurado obterSegurado(Cpf cpf) {
        
        try {
            // Buscar no contexto externo
            SeguradoExternalModel external = seguradoExternalService.buscarPorCpf(cpf.getValor());
            
            if (external == null) {
                throw new SeguradoNaoEncontradoException(cpf);
            }
            
            // Traduzir para modelo do domínio local
            return Segurado.builder()
                .id(SeguradoId.of(external.getId()))
                .cpf(Cpf.of(external.getCpf()))
                .nome(NomePessoa.of(external.getNome()))
                .email(Email.of(external.getEmail()))
                .telefone(Telefone.of(external.getTelefone()))
                .ativo(external.isAtivo())
                .build();
                
        } catch (SeguradoExternalServiceException e) {
            // Traduzir exceção externa para exceção do domínio
            throw new SeguradoIndisponivelException(
                "Erro ao consultar dados do segurado: " + e.getMessage(), e
            );
        }
    }
    
    /**
     * Validar se segurado pode ter sinistro
     */
    public boolean podeRegistrarSinistro(Cpf cpf) {
        
        try {
            Segurado segurado = obterSegurado(cpf);
            
            // Regras específicas do contexto de sinistros
            return segurado.isAtivo() && 
                   !segurado.estaSuspenso() && 
                   segurado.temApoliceVigente();
                   
        } catch (Exception e) {
            log.warn("Erro ao validar segurado para sinistro: {}", e.getMessage());
            return false;
        }
    }
}
```

---

## 🧩 **BUILDING BLOCKS TÁTICOS**

### **🏛️ Entities (Entidades)**

```java
// Localização: domain/model/Sinistro.java
@Entity
public class SinistroAggregate extends AggregateRoot {
    
    // Identity
    private SinistroId id;
    
    // Value Objects
    private SinistroNumero numero;
    private SinistroStatus status;
    private ValorMonetario valorEstimado;
    private DataOcorrencia dataOcorrencia;
    private EnderecoOcorrencia enderecoOcorrencia;
    private DescricaoOcorrencia descricao;
    
    // Referências para outros Aggregates (apenas IDs)
    private SeguradoId seguradoId;
    private VeiculoId veiculoId;
    private ApoliceId apoliceId;
    
    // Entities filhas (dentro do mesmo aggregate)
    private List<SinistroInteracao> interacoes;
    private List<SinistroDocumento> documentos;
    
    // Invariantes do aggregate
    protected SinistroAggregate() {
        this.interacoes = new ArrayList<>();
        this.documentos = new ArrayList<>();
    }
    
    /**
     * Factory method - garante criação válida
     */
    public static SinistroAggregate criar(CriarSinistroData data) {
        
        // Validações de criação
        Objects.requireNonNull(data.getCpfSegurado(), "CPF do segurado é obrigatório");
        Objects.requireNonNull(data.getPlacaVeiculo(), "Placa do veículo é obrigatória");
        Objects.requireNonNull(data.getDataOcorrencia(), "Data da ocorrência é obrigatória");
        
        if (data.getDataOcorrencia().isAfter(LocalDateTime.now())) {
            throw new DataOcorrenciaInvalidaException("Data da ocorrência não pode ser futura");
        }
        
        SinistroAggregate sinistro = new SinistroAggregate();
        sinistro.id = SinistroId.generate();
        sinistro.numero = SinistroNumero.generate();
        sinistro.status = SinistroStatus.ABERTO;
        sinistro.dataOcorrencia = DataOcorrencia.of(data.getDataOcorrencia());
        sinistro.enderecoOcorrencia = EnderecoOcorrencia.of(data.getEnderecoOcorrencia());
        sinistro.descricao = DescricaoOcorrencia.of(data.getDescricao());
        sinistro.seguradoId = SeguradoId.of(data.getCpfSegurado());
        sinistro.veiculoId = VeiculoId.of(data.getPlacaVeiculo());
        
        if (data.getValorEstimado() != null) {
            sinistro.valorEstimado = ValorMonetario.of(data.getValorEstimado());
        }
        
        // Evento de domínio
        sinistro.adicionarEvento(new SinistroCriadoEvent(
            sinistro.id.getValue(),
            sinistro.numero.getValor(),
            data.getCpfSegurado(),
            data.getPlacaVeiculo(),
            data.getDescricao(),
            data.getDataOcorrencia()
        ));
        
        return sinistro;
    }
    
    /**
     * Comportamento de domínio: Atribuir operador
     */
    public void atribuirOperador(OperadorId operadorId, String motivo) {
        
        if (!this.status.permiteAtribuicaoOperador()) {
            throw new OperacaoNaoPermitidaException(
                "Sinistro no status " + this.status + " não permite atribuição de operador"
            );
        }
        
        // Registrar interação
        SinistroInteracao interacao = SinistroInteracao.atribuicaoOperador(
            operadorId, motivo, Instant.now()
        );
        this.interacoes.add(interacao);
        
        // Mudar status se necessário
        if (this.status == SinistroStatus.ABERTO) {
            this.status = SinistroStatus.EM_ANALISE;
        }
        
        // Evento de domínio
        this.adicionarEvento(new SinistroAtribuidoEvent(
            this.id.getValue(),
            operadorId.getValue(),
            motivo
        ));
    }
    
    /**
     * Comportamento de domínio: Adicionar documento
     */
    public void adicionarDocumento(TipoDocumento tipo, String nomeArquivo, byte[] conteudo, String uploadedBy) {
        
        // Validações
        if (conteudo == null || conteudo.length == 0) {
            throw new DocumentoInvalidoException("Conteúdo do documento não pode estar vazio");
        }
        
        if (conteudo.length > 10_000_000) { // 10MB
            throw new DocumentoMuitoGrandeException("Documento não pode exceder 10MB");
        }
        
        // Verificar se tipo de documento é permitido para o status atual
        if (!tipo.isPermitidoParaStatus(this.status)) {
            throw new TipoDocumentoNaoPermitidoException(
                "Tipo de documento " + tipo + " não é permitido para status " + this.status
            );
        }
        
        SinistroDocumento documento = SinistroDocumento.criar(
            tipo, nomeArquivo, conteudo, uploadedBy
        );
        
        this.documentos.add(documento);
        
        // Evento de domínio
        this.adicionarEvento(new DocumentoAdicionadoEvent(
            this.id.getValue(),
            documento.getId().getValue(),
            tipo.name(),
            nomeArquivo,
            uploadedBy
        ));
    }
    
    // Métodos de consulta (não alteram estado)
    public boolean temDocumentosObrigatorios() {
        return TipoDocumento.getObrigatorios().stream()
            .allMatch(tipo -> temDocumento(tipo));
    }
    
    public boolean temDocumento(TipoDocumento tipo) {
        return documentos.stream()
            .anyMatch(doc -> doc.getTipo() == tipo);
    }
    
    public List<SinistroInteracao> getInteracoesOrdenadas() {
        return interacoes.stream()
            .sorted(Comparator.comparing(SinistroInteracao::getDataHora))
            .collect(Collectors.toList());
    }
    
    // Getters (sem setters - imutabilidade)
    public SinistroId getId() { return id; }
    public SinistroNumero getNumero() { return numero; }
    public SinistroStatus getStatus() { return status; }
    public ValorMonetario getValorEstimado() { return valorEstimado; }
    // ... outros getters
}
```

### **💎 Value Objects**

```java
// Localização: domain/model/valueobject/SinistroNumero.java
public class SinistroNumero {
    
    private final String valor;
    
    private SinistroNumero(String valor) {
        this.valor = Objects.requireNonNull(valor, "Número do sinistro não pode ser nulo");
    }
    
    /**
     * Factory method com geração automática
     */
    public static SinistroNumero generate() {
        // Formato: SIN-YYYY-NNNNNN
        String ano = String.valueOf(LocalDate.now().getYear());
        String sequencial = String.format("%06d", generateSequencial());
        
        return new SinistroNumero("SIN-" + ano + "-" + sequencial);
    }
    
    /**
     * Factory method com valor específico
     */
    public static SinistroNumero of(String valor) {
        if (StringUtils.isBlank(valor)) {
            throw new IllegalArgumentException("Número do sinistro não pode estar vazio");
        }
        
        if (!valor.matches("SIN-\\d{4}-\\d{6}")) {
            throw new IllegalArgumentException("Formato inválido para número do sinistro: " + valor);
        }
        
        return new SinistroNumero(valor);
    }
    
    public String getValor() {
        return valor;
    }
    
    public int getAno() {
        return Integer.parseInt(valor.substring(4, 8));
    }
    
    public int getSequencial() {
        return Integer.parseInt(valor.substring(9));
    }
    
    // Value Object - igualdade baseada em valor
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SinistroNumero that = (SinistroNumero) obj;
        return Objects.equals(valor, that.valor);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
    
    @Override
    public String toString() {
        return valor;
    }
    
    private static int generateSequencial() {
        // Implementação para gerar sequencial único
        // Pode usar banco de dados, Redis, etc.
        return (int) (System.currentTimeMillis() % 1000000);
    }
}

// Localização: domain/model/valueobject/ValorMonetario.java
public class ValorMonetario {
    
    private final BigDecimal valor;
    private final String moeda;
    
    private ValorMonetario(BigDecimal valor, String moeda) {
        this.valor = Objects.requireNonNull(valor, "Valor não pode ser nulo");
        this.moeda = Objects.requireNonNull(moeda, "Moeda não pode ser nula");
        
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor não pode ser negativo");
        }
    }
    
    public static ValorMonetario of(BigDecimal valor) {
        return new ValorMonetario(valor, "BRL");
    }
    
    public static ValorMonetario of(double valor) {
        return new ValorMonetario(BigDecimal.valueOf(valor), "BRL");
    }
    
    public static ValorMonetario zero() {
        return new ValorMonetario(BigDecimal.ZERO, "BRL");
    }
    
    // Operações matemáticas
    public ValorMonetario somar(ValorMonetario outro) {
        validarMoeda(outro);
        return new ValorMonetario(this.valor.add(outro.valor), this.moeda);
    }
    
    public ValorMonetario subtrair(ValorMonetario outro) {
        validarMoeda(outro);
        BigDecimal resultado = this.valor.subtract(outro.valor);
        
        if (resultado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Resultado não pode ser negativo");
        }
        
        return new ValorMonetario(resultado, this.moeda);
    }
    
    public ValorMonetario multiplicar(BigDecimal fator) {
        if (fator.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Fator não pode ser negativo");
        }
        
        return new ValorMonetario(this.valor.multiply(fator), this.moeda);
    }
    
    // Comparações
    public boolean ehMaiorQue(ValorMonetario outro) {
        validarMoeda(outro);
        return this.valor.compareTo(outro.valor) > 0;
    }
    
    public boolean ehMenorQue(ValorMonetario outro) {
        validarMoeda(outro);
        return this.valor.compareTo(outro.valor) < 0;
    }
    
    public boolean ehIgualA(ValorMonetario outro) {
        validarMoeda(outro);
        return this.valor.compareTo(outro.valor) == 0;
    }
    
    private void validarMoeda(ValorMonetario outro) {
        if (!this.moeda.equals(outro.moeda)) {
            throw new IllegalArgumentException("Não é possível operar com moedas diferentes");
        }
    }
    
    // Formatação
    public String formatado() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return formatter.format(valor);
    }
    
    // Getters
    public BigDecimal getValor() { return valor; }
    public String getMoeda() { return moeda; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ValorMonetario that = (ValorMonetario) obj;
        return Objects.equals(valor, that.valor) && Objects.equals(moeda, that.moeda);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(valor, moeda);
    }
    
    @Override
    public String toString() {
        return formatado();
    }
}
```

---

## 🧪 **EXERCÍCIO PRÁTICO**

### **🎯 Objetivo**: Implementar Value Objects e Entities

#### **Passo 1: Criar Value Object CPF**
```java
public class Cpf {
    private final String valor;
    
    private Cpf(String valor) {
        this.valor = validarEFormatar(valor);
    }
    
    public static Cpf of(String valor) {
        return new Cpf(valor);
    }
    
    private String validarEFormatar(String cpf) {
        // Implementar validação de CPF
        // Remover formatação
        // Validar dígitos verificadores
        // Retornar apenas números
    }
    
    public String getValorFormatado() {
        // Retornar no formato XXX.XXX.XXX-XX
    }
    
    // equals, hashCode, toString
}
```

#### **Passo 2: Explorar Aggregate Existente**
```java
// 1. Abrir: aggregate/example/ExampleAggregate.java
// 2. Identificar Value Objects usados
// 3. Ver como eventos são gerados
// 4. Entender invariantes do aggregate
```

#### **Passo 3: Testar Comportamentos de Domínio**
```java
@Test
public void testarComportamentoDominio() {
    // Criar aggregate
    SinistroAggregate sinistro = SinistroAggregate.criar(dadosValidos);
    
    // Testar regra de negócio
    assertThatThrownBy(() -> {
        sinistro.aprovar(ValorMonetario.zero(), "operador123");
    }).isInstanceOf(SinistroNaoPodeSerAprovadoException.class);
    
    // Verificar eventos gerados
    List<DomainEvent> eventos = sinistro.getUncommittedEvents();
    assertThat(eventos).hasSize(1);
    assertThat(eventos.get(0)).isInstanceOf(SinistroCriadoEvent.class);
}
```

---

## 📚 **CHECKPOINT DE APRENDIZADO**

### **✅ Você deve ser capaz de:**

1. **Explicar** os princípios fundamentais do DDD
2. **Identificar** bounded contexts no domínio
3. **Implementar** entities com comportamento rico
4. **Criar** value objects imutáveis e válidos
5. **Aplicar** linguagem ubíqua no código

### **❓ Perguntas para Reflexão:**

1. Como identificar boundaries entre contextos?
2. Quando usar Entity vs Value Object?
3. Como garantir invariantes do aggregate?
4. Qual a importância da linguagem ubíqua?

---

## 🔗 **PRÓXIMA PARTE**

Na **Parte 2**, vamos aprofundar:
- Aggregates e suas responsabilidades
- Domain Services e Application Services
- Repositories e especificações
- Padrões de persistência

---

**📖 Parte elaborada por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Estimada:** 36 minutos  
**📋 Pré-requisitos:** Capítulos 01-03 completos