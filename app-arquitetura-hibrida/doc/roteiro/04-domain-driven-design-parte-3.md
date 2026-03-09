# 📖 CAPÍTULO 04: DOMAIN-DRIVEN DESIGN - PARTE 3
## Domain Events e Integração

### 🎯 **OBJETIVOS DESTA PARTE**
- Dominar Domain Events
- Implementar Factories e Builders
- Criar políticas de domínio
- Configurar validações complexas

---

## 📡 **DOMAIN EVENTS**

### **🔄 Eventos de Domínio Ricos**

```java
// Localização: domain/event/SinistroDomainEvents.java
public abstract class SinistroDomainEvent extends DomainEvent {
    
    protected SinistroDomainEvent(String aggregateId) {
        super(aggregateId);
    }
    
    // Dados comuns a todos os eventos de sinistro
    public abstract SinistroId getSinistroId();
    public abstract SinistroNumero getNumeroSinistro();
}

// Evento específico com dados ricos
public class SinistroAprovadoEvent extends SinistroDomainEvent {
    
    private final SinistroId sinistroId;
    private final SinistroNumero numeroSinistro;
    private final ValorMonetario valorIndenizacao;
    private final OperadorId operadorAprovador;
    private final String motivoAprovacao;
    private final Instant dataAprovacao;
    
    // Dados adicionais para integração
    private final SeguradoId seguradoId;
    private final ApoliceId apoliceId;
    private final ContaBancaria contaDestino;
    
    public SinistroAprovadoEvent(SinistroId sinistroId,
                                SinistroNumero numeroSinistro,
                                ValorMonetario valorIndenizacao,
                                OperadorId operadorAprovador,
                                String motivoAprovacao,
                                SeguradoId seguradoId,
                                ApoliceId apoliceId,
                                ContaBancaria contaDestino) {
        
        super(sinistroId.getValue());
        this.sinistroId = sinistroId;
        this.numeroSinistro = numeroSinistro;
        this.valorIndenizacao = valorIndenizacao;
        this.operadorAprovador = operadorAprovador;
        this.motivoAprovacao = motivoAprovacao;
        this.dataAprovacao = Instant.now();
        this.seguradoId = seguradoId;
        this.apoliceId = apoliceId;
        this.contaDestino = contaDestino;
    }
    
    // Métodos de conveniência para handlers
    public boolean isValorAlto() {
        return valorIndenizacao.ehMaiorQue(ValorMonetario.of(100000));
    }
    
    public boolean precisaNotificacaoEspecial() {
        return isValorAlto() || contaDestino.isTipoEspecial();
    }
    
    // Getters...
}

// Evento de rejeição
public class SinistroRejeitadoEvent extends SinistroDomainEvent {
    
    private final SinistroId sinistroId;
    private final SinistroNumero numeroSinistro;
    private final MotivoRejeicao motivoRejeicao;
    private final OperadorId operadorRejeicao;
    private final boolean podeRecorrer;
    private final Instant dataRejeicao;
    
    // Dados para notificação
    private final SeguradoId seguradoId;
    private final Email emailSegurado;
    private final Telefone telefoneSegurado;
    
    // Construtor e getters...
    
    public boolean requerNotificacaoUrgente() {
        return motivoRejeicao.isGrave();
    }
}
```

### **🎯 Event Handlers Especializados**

```java
// Localização: domain/handler/SinistroEventHandlers.java

// Handler para notificações
@Component
public class SinistroNotificacaoEventHandler {
    
    private final NotificacaoService notificacaoService;
    private final SeguradoService seguradoService;
    
    @EventHandler
    public void handle(SinistroAprovadoEvent event) {
        
        // Notificar segurado sobre aprovação
        Segurado segurado = seguradoService.buscarPorId(event.getSeguradoId());
        
        NotificacaoAprovacao notificacao = NotificacaoAprovacao.builder()
            .destinatario(segurado.getEmail())
            .numeroSinistro(event.getNumeroSinistro().getValor())
            .valorIndenizacao(event.getValorIndenizacao().formatado())
            .contaDestino(event.getContaDestino().getNumeroFormatado())
            .prazoCredito(calcularPrazoCredito(event))
            .build();
        
        notificacaoService.enviarNotificacao(notificacao);
        
        // Se valor alto, notificar também por SMS
        if (event.isValorAlto()) {
            NotificacaoSMS sms = NotificacaoSMS.builder()
                .telefone(segurado.getTelefone())
                .mensagem(criarMensagemSMS(event))
                .build();
                
            notificacaoService.enviarSMS(sms);
        }
    }
    
    @EventHandler
    public void handle(SinistroRejeitadoEvent event) {
        
        Segurado segurado = seguradoService.buscarPorId(event.getSeguradoId());
        
        NotificacaoRejeicao notificacao = NotificacaoRejeicao.builder()
            .destinatario(segurado.getEmail())
            .numeroSinistro(event.getNumeroSinistro().getValor())
            .motivoRejeicao(event.getMotivoRejeicao().getDescricao())
            .podeRecorrer(event.isPodeRecorrer())
            .prazoRecurso(event.isPodeRecorrer() ? Duration.ofDays(30) : null)
            .build();
        
        notificacaoService.enviarNotificacao(notificacao);
    }
}

// Handler para integração com pagamentos
@Component
public class SinistroPagamentoEventHandler {
    
    private final PagamentoService pagamentoService;
    private final ContabilidadeService contabilidadeService;
    
    @EventHandler
    public void handle(SinistroAprovadoEvent event) {
        
        // Criar ordem de pagamento
        OrdemPagamento ordem = OrdemPagamento.builder()
            .beneficiario(event.getSeguradoId().getValue())
            .valor(event.getValorIndenizacao())
            .contaDestino(event.getContaDestino())
            .referencia("SINISTRO-" + event.getNumeroSinistro().getValor())
            .dataVencimento(calcularDataVencimento(event))
            .prioridade(event.isValorAlto() ? Prioridade.ALTA : Prioridade.NORMAL)
            .build();
        
        pagamentoService.criarOrdemPagamento(ordem);
        
        // Registrar na contabilidade
        LancamentoContabil lancamento = LancamentoContabil.builder()
            .tipoOperacao(TipoOperacao.PAGAMENTO_SINISTRO)
            .valor(event.getValorIndenizacao())
            .apolice(event.getApoliceId())
            .sinistro(event.getSinistroId())
            .dataOperacao(event.getDataAprovacao())
            .build();
        
        contabilidadeService.registrarLancamento(lancamento);
    }
}

// Handler para auditoria e compliance
@Component
public class SinistroAuditoriaEventHandler {
    
    private final AuditoriaService auditoriaService;
    private final ComplianceService complianceService;
    
    @EventHandler
    public void handle(SinistroAprovadoEvent event) {
        
        // Registrar auditoria
        RegistroAuditoria registro = RegistroAuditoria.builder()
            .tipoEvento("SINISTRO_APROVADO")
            .entidadeId(event.getSinistroId().getValue())
            .operador(event.getOperadorAprovador().getValue())
            .valor(event.getValorIndenizacao().getValor())
            .timestamp(event.getDataAprovacao())
            .detalhes(Map.of(
                "numero_sinistro", event.getNumeroSinistro().getValor(),
                "motivo_aprovacao", event.getMotivoAprovacao(),
                "valor_formatado", event.getValorIndenizacao().formatado()
            ))
            .build();
        
        auditoriaService.registrar(registro);
        
        // Verificar compliance para valores altos
        if (event.isValorAlto()) {
            AnaliseCompliance analise = complianceService.analisar(
                event.getSinistroId(),
                event.getValorIndenizacao(),
                event.getSeguradoId()
            );
            
            if (analise.requerRevisao()) {
                complianceService.criarCasoRevisao(analise);
            }
        }
    }
}
```

---

## 🏭 **FACTORIES E BUILDERS**

### **🔧 Domain Factories**

```java
// Localização: domain/factory/SinistroFactory.java
@Component
public class SinistroFactory {
    
    private final SeguradoService seguradoService;
    private final ApoliceService apoliceService;
    private final VeiculoService veiculoService;
    
    /**
     * Factory method com validações de domínio
     */
    public SinistroAggregate criarSinistro(CriarSinistroRequest request) {
        
        // 1. Validar dados básicos
        validarDadosBasicos(request);
        
        // 2. Validar relacionamentos
        ValidacaoRelacionamentos validacao = validarRelacionamentos(request);
        
        if (!validacao.isValida()) {
            throw new SinistroNaoPodeCriadoException(validacao.getErros());
        }
        
        // 3. Determinar tipo de sinistro automaticamente
        TipoSinistro tipo = determinarTipoSinistro(request);
        
        // 4. Calcular valor estimado inicial se não informado
        ValorMonetario valorEstimado = request.getValorEstimado();
        if (valorEstimado == null) {
            valorEstimado = calcularValorEstimadoInicial(request, validacao.getVeiculo());
        }
        
        // 5. Criar aggregate
        SinistroAggregate sinistro = SinistroAggregate.builder()
            .id(SinistroId.generate())
            .numero(SinistroNumero.generate())
            .tipo(tipo)
            .status(SinistroStatus.ABERTO)
            .seguradoId(validacao.getSegurado().getId())
            .apoliceId(validacao.getApolice().getId())
            .veiculoId(validacao.getVeiculo().getId())
            .dataOcorrencia(DataOcorrencia.of(request.getDataOcorrencia()))
            .enderecoOcorrencia(EnderecoOcorrencia.of(request.getEnderecoOcorrencia()))
            .descricao(DescricaoOcorrencia.of(request.getDescricao()))
            .valorEstimado(valorEstimado)
            .build();
        
        // 6. Aplicar regras de negócio pós-criação
        aplicarRegrasPosCriacao(sinistro, validacao);
        
        return sinistro;
    }
    
    private ValidacaoRelacionamentos validarRelacionamentos(CriarSinistroRequest request) {
        
        ValidacaoRelacionamentos.Builder builder = ValidacaoRelacionamentos.builder();
        
        // Validar segurado
        try {
            Segurado segurado = seguradoService.buscarPorCpf(request.getCpfSegurado());
            
            if (!segurado.isAtivo()) {
                builder.adicionarErro("Segurado não está ativo");
            } else {
                builder.segurado(segurado);
            }
            
        } catch (SeguradoNaoEncontradoException e) {
            builder.adicionarErro("Segurado não encontrado");
        }
        
        // Validar veículo
        try {
            Veiculo veiculo = veiculoService.buscarPorPlaca(request.getPlacaVeiculo());
            builder.veiculo(veiculo);
            
        } catch (VeiculoNaoEncontradoException e) {
            builder.adicionarErro("Veículo não encontrado");
        }
        
        // Validar apólice (se segurado e veículo válidos)
        if (builder.temSeguradoEVeiculo()) {
            try {
                Apolice apolice = apoliceService.buscarApoliceVigente(
                    builder.getSegurado().getId(),
                    builder.getVeiculo().getId(),
                    request.getDataOcorrencia().toLocalDate()
                );
                
                builder.apolice(apolice);
                
            } catch (ApoliceNaoEncontradaException e) {
                builder.adicionarErro("Não há apólice vigente para o veículo na data da ocorrência");
            }
        }
        
        return builder.build();
    }
    
    private TipoSinistro determinarTipoSinistro(CriarSinistroRequest request) {
        
        String descricao = request.getDescricao().toLowerCase();
        
        // Regras para determinar tipo automaticamente
        if (descricao.contains("roubo") || descricao.contains("furto")) {
            return TipoSinistro.ROUBO_FURTO;
        }
        
        if (descricao.contains("colisão") || descricao.contains("batida") || descricao.contains("acidente")) {
            return TipoSinistro.COLISAO;
        }
        
        if (descricao.contains("incêndio") || descricao.contains("fogo")) {
            return TipoSinistro.INCENDIO;
        }
        
        if (descricao.contains("vidro") || descricao.contains("para-brisa")) {
            return TipoSinistro.QUEBRA_VIDROS;
        }
        
        // Padrão
        return TipoSinistro.OUTROS;
    }
    
    private ValorMonetario calcularValorEstimadoInicial(CriarSinistroRequest request, Veiculo veiculo) {
        
        TipoSinistro tipo = determinarTipoSinistro(request);
        
        // Estimativa baseada no tipo e valor do veículo
        BigDecimal percentualEstimado = switch (tipo) {
            case ROUBO_FURTO -> BigDecimal.valueOf(1.0); // 100% do valor
            case COLISAO -> BigDecimal.valueOf(0.3);     // 30% do valor
            case INCENDIO -> BigDecimal.valueOf(0.8);    // 80% do valor
            case QUEBRA_VIDROS -> BigDecimal.valueOf(0.05); // 5% do valor
            default -> BigDecimal.valueOf(0.2);          // 20% do valor
        };
        
        BigDecimal valorEstimado = veiculo.getValorMercado()
            .multiply(percentualEstimado);
        
        return ValorMonetario.of(valorEstimado);
    }
}

// Builder para objetos complexos
public class SinistroAggregateBuilder {
    
    private SinistroId id;
    private SinistroNumero numero;
    private TipoSinistro tipo;
    private SinistroStatus status;
    private SeguradoId seguradoId;
    private ApoliceId apoliceId;
    private VeiculoId veiculoId;
    private DataOcorrencia dataOcorrencia;
    private EnderecoOcorrencia enderecoOcorrencia;
    private DescricaoOcorrencia descricao;
    private ValorMonetario valorEstimado;
    
    public static SinistroAggregateBuilder builder() {
        return new SinistroAggregateBuilder();
    }
    
    public SinistroAggregateBuilder id(SinistroId id) {
        this.id = id;
        return this;
    }
    
    public SinistroAggregateBuilder numero(SinistroNumero numero) {
        this.numero = numero;
        return this;
    }
    
    // ... outros métodos builder
    
    public SinistroAggregate build() {
        
        // Validações obrigatórias
        Objects.requireNonNull(id, "ID é obrigatório");
        Objects.requireNonNull(numero, "Número é obrigatório");
        Objects.requireNonNull(tipo, "Tipo é obrigatório");
        Objects.requireNonNull(status, "Status é obrigatório");
        
        // Criar aggregate
        return new SinistroAggregate(
            id, numero, tipo, status, seguradoId, apoliceId, veiculoId,
            dataOcorrencia, enderecoOcorrencia, descricao, valorEstimado
        );
    }
}
```

---

## 📋 **POLÍTICAS DE DOMÍNIO**

### **🎯 Domain Policies**

```java
// Localização: domain/policy/SinistroAprovacaoPolicy.java
@Component
public class SinistroAprovacaoPolicy {
    
    /**
     * Política: Determinar se sinistro pode ser aprovado automaticamente
     */
    public AprovacaoDecision avaliarAprovacaoAutomatica(SinistroAggregate sinistro, 
                                                       Apolice apolice,
                                                       HistoricoSegurado historico) {
        
        AprovacaoDecision.Builder decision = AprovacaoDecision.builder()
            .sinistroId(sinistro.getId());
        
        // Regra 1: Valor limite
        ValorMonetario limiteAutomatico = apolice.getLimiteAprovacaoAutomatica();
        if (sinistro.getValorEstimado().ehMaiorQue(limiteAutomatico)) {
            return decision
                .aprovacaoAutomatica(false)
                .motivo("Valor excede limite de aprovação automática")
                .requerAprovacaoManual(true)
                .build();
        }
        
        // Regra 2: Histórico do segurado
        if (historico.temSinistrosRecentes(Duration.ofDays(90))) {
            return decision
                .aprovacaoAutomatica(false)
                .motivo("Segurado possui sinistros recentes")
                .requerAnaliseEspecial(true)
                .build();
        }
        
        // Regra 3: Tipo de sinistro
        if (sinistro.getTipo().requerAnaliseEspecial()) {
            return decision
                .aprovacaoAutomatica(false)
                .motivo("Tipo de sinistro requer análise especial")
                .requerAnaliseEspecial(true)
                .build();
        }
        
        // Regra 4: Documentação completa
        if (!sinistro.temDocumentosObrigatorios()) {
            return decision
                .aprovacaoAutomatica(false)
                .motivo("Documentação incompleta")
                .requerDocumentosAdicionais(true)
                .build();
        }
        
        // Regra 5: Score de fraude
        ScoreFraude score = calcularScoreFraude(sinistro, historico);
        if (score.isAltoRisco()) {
            return decision
                .aprovacaoAutomatica(false)
                .motivo("Alto risco de fraude detectado")
                .requerInvestigacao(true)
                .scoreFraude(score)
                .build();
        }
        
        // Pode ser aprovado automaticamente
        return decision
            .aprovacaoAutomatica(true)
            .motivo("Atende todos os critérios para aprovação automática")
            .build();
    }
    
    private ScoreFraude calcularScoreFraude(SinistroAggregate sinistro, HistoricoSegurado historico) {
        
        int score = 0;
        List<String> indicadores = new ArrayList<>();
        
        // Indicador: Múltiplos sinistros em pouco tempo
        if (historico.getQuantidadeSinistrosUltimos12Meses() > 3) {
            score += 25;
            indicadores.add("Múltiplos sinistros nos últimos 12 meses");
        }
        
        // Indicador: Valor muito alto para o veículo
        if (sinistro.getValorEstimado().ehMaiorQue(
            historico.getVeiculo().getValorMercado().multiplicar(BigDecimal.valueOf(0.8)))) {
            score += 20;
            indicadores.add("Valor estimado muito alto para o veículo");
        }
        
        // Indicador: Sinistro em local de alto risco
        if (isLocalAltoRisco(sinistro.getEnderecoOcorrencia())) {
            score += 15;
            indicadores.add("Ocorrência em local de alto risco");
        }
        
        // Indicador: Horário suspeito
        LocalTime horario = sinistro.getDataOcorrencia().getValue().toLocalTime();
        if (horario.isAfter(LocalTime.of(23, 0)) || horario.isBefore(LocalTime.of(6, 0))) {
            score += 10;
            indicadores.add("Horário de ocorrência suspeito");
        }
        
        return ScoreFraude.builder()
            .valor(score)
            .nivel(determinarNivelRisco(score))
            .indicadores(indicadores)
            .build();
    }
}

// Localização: domain/policy/NotificacaoPolicy.java
@Component
public class NotificacaoPolicy {
    
    /**
     * Política: Determinar canais de notificação baseado no evento
     */
    public List<CanalNotificacao> determinarCanaisNotificacao(SinistroDomainEvent event, 
                                                             Segurado segurado) {
        
        List<CanalNotificacao> canais = new ArrayList<>();
        
        // Email sempre obrigatório
        canais.add(CanalNotificacao.EMAIL);
        
        // SMS para eventos importantes
        if (event instanceof SinistroAprovadoEvent aprovado) {
            
            if (aprovado.isValorAlto()) {
                canais.add(CanalNotificacao.SMS);
            }
            
            if (aprovado.precisaNotificacaoEspecial()) {
                canais.add(CanalNotificacao.TELEFONE);
                canais.add(CanalNotificacao.CARTA_REGISTRADA);
            }
        }
        
        if (event instanceof SinistroRejeitadoEvent rejeitado) {
            
            canais.add(CanalNotificacao.SMS);
            
            if (rejeitado.requerNotificacaoUrgente()) {
                canais.add(CanalNotificacao.TELEFONE);
            }
        }
        
        // Considerar preferências do segurado
        if (segurado.getPreferenciasNotificacao().isApenasDigital()) {
            canais.removeIf(canal -> canal.isFisico());
        }
        
        return canais;
    }
}
```

---

## ✅ **VALIDAÇÕES COMPLEXAS**

### **🔍 Domain Validators**

```java
// Localização: domain/validator/SinistroValidator.java
@Component
public class SinistroValidator {
    
    private final List<SinistroValidationRule> rules;
    
    public SinistroValidator(List<SinistroValidationRule> rules) {
        this.rules = rules.stream()
            .sorted(Comparator.comparing(SinistroValidationRule::getPriority))
            .collect(Collectors.toList());
    }
    
    public ValidationResult validar(SinistroAggregate sinistro, ValidationContext context) {
        
        ValidationResult.Builder result = ValidationResult.builder();
        
        for (SinistroValidationRule rule : rules) {
            
            if (rule.appliesTo(sinistro, context)) {
                
                RuleValidationResult ruleResult = rule.validate(sinistro, context);
                
                if (!ruleResult.isValid()) {
                    result.addViolation(ruleResult.getViolation());
                    
                    // Se regra é crítica, parar validação
                    if (rule.isCritical()) {
                        break;
                    }
                }
            }
        }
        
        return result.build();
    }
}

// Regras específicas de validação
@Component
public class DataOcorrenciaValidationRule implements SinistroValidationRule {
    
    @Override
    public RuleValidationResult validate(SinistroAggregate sinistro, ValidationContext context) {
        
        LocalDateTime dataOcorrencia = sinistro.getDataOcorrencia().getValue();
        LocalDateTime agora = LocalDateTime.now();
        
        // Não pode ser futura
        if (dataOcorrencia.isAfter(agora)) {
            return RuleValidationResult.invalid(
                "Data da ocorrência não pode ser futura",
                "DATA_OCORRENCIA_FUTURA"
            );
        }
        
        // Não pode ser muito antiga (mais de 1 ano)
        if (dataOcorrencia.isBefore(agora.minusYears(1))) {
            return RuleValidationResult.invalid(
                "Data da ocorrência não pode ser anterior a 1 ano",
                "DATA_OCORRENCIA_MUITO_ANTIGA"
            );
        }
        
        // Se apólice disponível, verificar vigência
        if (context.hasApolice()) {
            Apolice apolice = context.getApolice();
            
            if (!apolice.estaVigenteEm(dataOcorrencia.toLocalDate())) {
                return RuleValidationResult.invalid(
                    "Apólice não estava vigente na data da ocorrência",
                    "APOLICE_NAO_VIGENTE"
                );
            }
        }
        
        return RuleValidationResult.valid();
    }
    
    @Override
    public boolean appliesTo(SinistroAggregate sinistro, ValidationContext context) {
        return sinistro.getDataOcorrencia() != null;
    }
    
    @Override
    public int getPriority() {
        return 1; // Alta prioridade
    }
    
    @Override
    public boolean isCritical() {
        return true; // Crítica - para validação se falhar
    }
}

@Component
public class ValorEstimadoValidationRule implements SinistroValidationRule {
    
    @Override
    public RuleValidationResult validate(SinistroAggregate sinistro, ValidationContext context) {
        
        ValorMonetario valorEstimado = sinistro.getValorEstimado();
        
        if (valorEstimado == null) {
            return RuleValidationResult.valid(); // Opcional
        }
        
        // Valor mínimo
        if (valorEstimado.ehMenorQue(ValorMonetario.of(100))) {
            return RuleValidationResult.invalid(
                "Valor estimado deve ser pelo menos R$ 100,00",
                "VALOR_MUITO_BAIXO"
            );
        }
        
        // Valor máximo baseado no veículo
        if (context.hasVeiculo()) {
            Veiculo veiculo = context.getVeiculo();
            ValorMonetario valorMaximo = veiculo.getValorMercado().multiplicar(BigDecimal.valueOf(1.5));
            
            if (valorEstimado.ehMaiorQue(valorMaximo)) {
                return RuleValidationResult.warning(
                    "Valor estimado é muito alto comparado ao valor do veículo",
                    "VALOR_SUSPEITO"
                );
            }
        }
        
        return RuleValidationResult.valid();
    }
    
    @Override
    public int getPriority() {
        return 5; // Prioridade média
    }
}
```

---

## 🧪 **EXERCÍCIO PRÁTICO**

### **🎯 Objetivo**: Implementar Domain Events completos

#### **Passo 1: Criar Evento Rico**
```java
public class PeritoAtribuidoEvent extends SinistroDomainEvent {
    
    private final PeritoId peritoId;
    private final SinistroId sinistroId;
    private final TipoAvaliacao tipoAvaliacao;
    private final Prazo prazoAvaliacao;
    private final Endereco localAvaliacao;
    
    // Dados para notificação
    private final Email emailPerito;
    private final Telefone telefonePerito;
    
    // Construtor e getters...
    
    public boolean isAvaliacaoUrgente() {
        return prazoAvaliacao.isDentroDeHoras(24);
    }
}
```

#### **Passo 2: Implementar Handler**
```java
@Component
public class PeritoNotificacaoEventHandler {
    
    @EventHandler
    public void handle(PeritoAtribuidoEvent event) {
        // Notificar perito sobre nova atribuição
        // Criar agenda automática
        // Enviar dados do sinistro
    }
}
```

#### **Passo 3: Testar Integração**
```java
@Test
public void testarEventoIntegracao() {
    // Criar sinistro
    SinistroAggregate sinistro = factory.criarSinistro(request);
    
    // Atribuir perito
    sinistro.atribuirPerito(peritoId, tipoAvaliacao);
    
    // Verificar evento gerado
    List<DomainEvent> eventos = sinistro.getUncommittedEvents();
    assertThat(eventos).hasSize(2); // Criado + Perito Atribuído
    
    PeritoAtribuidoEvent evento = (PeritoAtribuidoEvent) eventos.get(1);
    assertThat(evento.getPeritoId()).isEqualTo(peritoId);
}
```

---

## 📚 **CHECKPOINT DE APRENDIZADO**

### **✅ Você deve ser capaz de:**

1. **Criar** Domain Events ricos e expressivos
2. **Implementar** Event Handlers especializados
3. **Usar** Factories para criação complexa
4. **Definir** políticas de domínio
5. **Configurar** validações em camadas

### **❓ Perguntas para Reflexão:**

1. Como balancear riqueza vs simplicidade em eventos?
2. Quando usar Factory vs construtor direto?
3. Como organizar políticas de domínio?
4. Qual granularidade ideal para validações?

---

## 🔗 **PRÓXIMA PARTE**

Na **Parte 4**, vamos explorar:
- Bounded Context integration
- Anti-corruption layers
- Context mapping
- Shared kernels

---

**📖 Parte elaborada por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Estimada:** 36 minutos  
**📋 Pré-requisitos:** DDD Partes 1-2