# 🏗️ AGREGADOS - PARTE 4: REGRAS DE NEGÓCIO E VALIDAÇÕES
## Roteiro Técnico para Analistas Java Junior

### 🎯 **OBJETIVO DESTA PARTE**
Dominar a implementação de regras de negócio, validações complexas e padrões de consistência em agregados.

---

## 🎯 **REGRAS DE NEGÓCIO EM AGREGADOS**

### **📋 Conceitos Fundamentais**

**Regras de Negócio** são:
- ✅ **Invariantes** que devem sempre ser verdadeiras
- ✅ **Validações** específicas do domínio
- ✅ **Políticas** de negócio encapsuladas
- ✅ **Constraints** que garantem consistência

**Localização das Regras:**
```
Onde implementar regras de negócio?
├── ✅ Dentro do Agregado (Invariantes)
├── ✅ BusinessRule classes (Reutilizáveis)
├── ✅ Domain Services (Regras complexas)
└── ❌ Controllers/Services (Violação DDD)
```

---

## 🏛️ **INTERFACE BUSINESSRULE**

### **📋 Definição da Interface**

**Localização**: `com.seguradora.hibrida.aggregate.validation.BusinessRule`

```java
public interface BusinessRule {
    
    /**
     * Valida se a regra de negócio é atendida pelo agregado.
     */
    boolean isValid(AggregateRoot aggregate);
    
    /**
     * Retorna mensagem de erro clara quando a regra é violada.
     */
    String getErrorMessage();
    
    /**
     * Retorna nome identificador da regra para logs e debugging.
     */
    default String getRuleName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * Retorna prioridade da regra para ordenação de validação.
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * Indica se a regra deve ser executada durante replay de eventos.
     */
    default boolean validateOnReplay() {
        return true;
    }
    
    /**
     * Indica se a regra se aplica ao tipo específico de aggregate.
     */
    default boolean appliesTo(Class<? extends AggregateRoot> aggregateType) {
        return true;
    }
}
```

### **🎯 Implementações de Regras Específicas**

```java
// === REGRA: CPF VÁLIDO ===
public class CpfValidoRule implements BusinessRule {
    
    @Override
    public boolean isValid(AggregateRoot aggregate) {
        if (!(aggregate instanceof SeguradoAggregate)) {
            return true; // Não se aplica
        }
        
        SeguradoAggregate segurado = (SeguradoAggregate) aggregate;
        String cpf = segurado.getCpf();
        
        return cpf != null && isValidCpf(cpf);
    }
    
    @Override
    public String getErrorMessage() {
        return "CPF deve ser válido e estar no formato correto";
    }
    
    @Override
    public String getRuleName() {
        return "CPF_VALIDO";
    }
    
    @Override
    public int getPriority() {
        return 100; // Alta prioridade
    }
    
    @Override
    public boolean appliesTo(Class<? extends AggregateRoot> aggregateType) {
        return SeguradoAggregate.class.isAssignableFrom(aggregateType);
    }
    
    private boolean isValidCpf(String cpf) {
        // Implementação de validação de CPF
        if (cpf == null || cpf.length() != 11) {
            return false;
        }
        
        // Verificar se todos os dígitos são iguais
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }
        
        // Calcular dígitos verificadores
        return calculateCpfDigit(cpf, 10) == Character.getNumericValue(cpf.charAt(9)) &&
               calculateCpfDigit(cpf, 11) == Character.getNumericValue(cpf.charAt(10));
    }
    
    private int calculateCpfDigit(String cpf, int weight) {
        int sum = 0;
        for (int i = 0; i < weight - 1; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (weight - i);
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}

// === REGRA: SINISTRO VALOR MÁXIMO ===
public class SinistroValorMaximoRule implements BusinessRule {
    
    private final BigDecimal valorMaximo;
    
    public SinistroValorMaximoRule(BigDecimal valorMaximo) {
        this.valorMaximo = valorMaximo;
    }
    
    @Override
    public boolean isValid(AggregateRoot aggregate) {
        if (!(aggregate instanceof SinistroAggregate)) {
            return true;
        }
        
        SinistroAggregate sinistro = (SinistroAggregate) aggregate;
        BigDecimal valorEstimado = sinistro.getValorEstimado();
        
        return valorEstimado == null || 
               valorEstimado.compareTo(valorMaximo) <= 0;
    }
    
    @Override
    public String getErrorMessage() {
        return String.format("Valor do sinistro não pode exceder R$ %s", 
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"))
                .format(valorMaximo));
    }
    
    @Override
    public String getRuleName() {
        return "SINISTRO_VALOR_MAXIMO";
    }
    
    @Override
    public boolean appliesTo(Class<? extends AggregateRoot> aggregateType) {
        return SinistroAggregate.class.isAssignableFrom(aggregateType);
    }
}

// === REGRA: APÓLICE VIGENTE ===
public class ApoliceVigenteRule implements BusinessRule {
    
    @Override
    public boolean isValid(AggregateRoot aggregate) {
        if (!(aggregate instanceof SinistroAggregate)) {
            return true;
        }
        
        SinistroAggregate sinistro = (SinistroAggregate) aggregate;
        
        LocalDate dataOcorrencia = sinistro.getDataOcorrencia();
        LocalDate vigenciaInicio = sinistro.getApoliceVigenciaInicio();
        LocalDate vigenciaFim = sinistro.getApoliceVigenciaFim();
        
        if (dataOcorrencia == null || vigenciaInicio == null || vigenciaFim == null) {
            return false; // Dados obrigatórios
        }
        
        return !dataOcorrencia.isBefore(vigenciaInicio) && 
               !dataOcorrencia.isAfter(vigenciaFim);
    }
    
    @Override
    public String getErrorMessage() {
        return "Sinistro deve ocorrer dentro do período de vigência da apólice";
    }
    
    @Override
    public String getRuleName() {
        return "APOLICE_VIGENTE";
    }
    
    @Override
    public int getPriority() {
        return 90; // Alta prioridade
    }
}

// === REGRA: SEGURADO ATIVO ===
public class SeguradoAtivoRule implements BusinessRule {
    
    private final SeguradoRepository seguradoRepository;
    
    public SeguradoAtivoRule(SeguradoRepository seguradoRepository) {
        this.seguradoRepository = seguradoRepository;
    }
    
    @Override
    public boolean isValid(AggregateRoot aggregate) {
        if (!(aggregate instanceof SinistroAggregate)) {
            return true;
        }
        
        SinistroAggregate sinistro = (SinistroAggregate) aggregate;
        String cpfSegurado = sinistro.getCpfSegurado();
        
        if (cpfSegurado == null) {
            return false;
        }
        
        // Consultar se segurado está ativo
        Optional<SeguradoAggregate> segurado = seguradoRepository.findByCpf(cpfSegurado);
        return segurado.isPresent() && segurado.get().isAtivo();
    }
    
    @Override
    public String getErrorMessage() {
        return "Segurado deve estar ativo para abertura de sinistro";
    }
    
    @Override
    public String getRuleName() {
        return "SEGURADO_ATIVO";
    }
    
    @Override
    public boolean validateOnReplay() {
        return false; // Não validar durante replay (dados podem ter mudado)
    }
}
```

---

## 🔧 **VALIDADOR DE REGRAS**

### **📋 BusinessRuleValidator**

```java
@Component
public class BusinessRuleValidator {
    
    private static final Logger log = LoggerFactory.getLogger(BusinessRuleValidator.class);
    
    private final List<BusinessRule> globalRules;
    private final Map<Class<? extends AggregateRoot>, List<BusinessRule>> typeSpecificRules;
    
    public BusinessRuleValidator(List<BusinessRule> businessRules) {
        this.globalRules = new ArrayList<>();
        this.typeSpecificRules = new HashMap<>();
        
        categorizeRules(businessRules);
    }
    
    /**
     * Valida todas as regras aplicáveis ao agregado.
     */
    public ValidationResult validate(AggregateRoot aggregate) {
        return validate(aggregate, false);
    }
    
    /**
     * Valida regras com controle de replay.
     */
    public ValidationResult validate(AggregateRoot aggregate, boolean isReplay) {
        List<String> violations = new ArrayList<>();
        
        try {
            // Validar regras globais
            validateRules(globalRules, aggregate, isReplay, violations);
            
            // Validar regras específicas do tipo
            List<BusinessRule> specificRules = typeSpecificRules.get(aggregate.getClass());
            if (specificRules != null) {
                validateRules(specificRules, aggregate, isReplay, violations);
            }
            
            if (violations.isEmpty()) {
                log.debug("Validação bem-sucedida para agregado: {} ({})", 
                    aggregate.getAggregateId(), aggregate.getClass().getSimpleName());
                return ValidationResult.valid();
            } else {
                log.warn("Violações de regras de negócio encontradas para agregado: {} - {}", 
                    aggregate.getAggregateId(), violations);
                return ValidationResult.invalid(violations);
            }
            
        } catch (Exception e) {
            log.error("Erro durante validação de regras de negócio para agregado: {}", 
                aggregate.getAggregateId(), e);
            return ValidationResult.invalid(List.of("Erro interno na validação: " + e.getMessage()));
        }
    }
    
    /**
     * Valida uma regra específica.
     */
    public ValidationResult validateRule(AggregateRoot aggregate, BusinessRule rule) {
        try {
            if (!rule.appliesTo(aggregate.getClass())) {
                return ValidationResult.valid();
            }
            
            if (rule.isValid(aggregate)) {
                return ValidationResult.valid();
            } else {
                return ValidationResult.invalid(List.of(rule.getErrorMessage()));
            }
            
        } catch (Exception e) {
            log.error("Erro ao validar regra {} para agregado: {}", 
                rule.getRuleName(), aggregate.getAggregateId(), e);
            return ValidationResult.invalid(List.of("Erro na validação da regra: " + rule.getRuleName()));
        }
    }
    
    /**
     * Obtém todas as regras aplicáveis a um tipo de agregado.
     */
    public List<BusinessRule> getApplicableRules(Class<? extends AggregateRoot> aggregateType) {
        List<BusinessRule> applicable = new ArrayList<>();
        
        // Adicionar regras globais aplicáveis
        globalRules.stream()
            .filter(rule -> rule.appliesTo(aggregateType))
            .forEach(applicable::add);
        
        // Adicionar regras específicas do tipo
        List<BusinessRule> specificRules = typeSpecificRules.get(aggregateType);
        if (specificRules != null) {
            applicable.addAll(specificRules);
        }
        
        // Ordenar por prioridade (maior prioridade primeiro)
        applicable.sort((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()));
        
        return applicable;
    }
    
    /**
     * Registra uma nova regra dinamicamente.
     */
    public void registerRule(BusinessRule rule) {
        if (rule.appliesTo(null)) { // Regra global
            globalRules.add(rule);
        } else {
            // Determinar tipos específicos (implementação simplificada)
            // Em implementação real, seria mais sofisticado
            log.info("Regra registrada dinamicamente: {}", rule.getRuleName());
        }
        
        // Reordenar por prioridade
        globalRules.sort((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()));
    }
    
    /**
     * Remove uma regra.
     */
    public boolean unregisterRule(String ruleName) {
        boolean removed = globalRules.removeIf(rule -> rule.getRuleName().equals(ruleName));
        
        for (List<BusinessRule> rules : typeSpecificRules.values()) {
            removed |= rules.removeIf(rule -> rule.getRuleName().equals(ruleName));
        }
        
        if (removed) {
            log.info("Regra removida: {}", ruleName);
        }
        
        return removed;
    }
    
    /**
     * Obtém estatísticas das regras.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGlobalRules", globalRules.size());
        stats.put("totalTypeSpecificRules", typeSpecificRules.values().stream()
            .mapToInt(List::size).sum());
        stats.put("totalAggregateTypes", typeSpecificRules.size());
        
        // Estatísticas por tipo
        Map<String, Integer> rulesByType = new HashMap<>();
        typeSpecificRules.forEach((type, rules) -> 
            rulesByType.put(type.getSimpleName(), rules.size()));
        stats.put("rulesByType", rulesByType);
        
        return stats;
    }
    
    // === MÉTODOS PRIVADOS ===
    
    private void categorizeRules(List<BusinessRule> businessRules) {
        for (BusinessRule rule : businessRules) {
            if (isGlobalRule(rule)) {
                globalRules.add(rule);
            } else {
                categorizeTypeSpecificRule(rule);
            }
        }
        
        // Ordenar regras por prioridade
        globalRules.sort((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()));
        typeSpecificRules.values().forEach(rules -> 
            rules.sort((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority())));
        
        log.info("Regras categorizadas: {} globais, {} específicas por tipo", 
            globalRules.size(), 
            typeSpecificRules.values().stream().mapToInt(List::size).sum());
    }
    
    private boolean isGlobalRule(BusinessRule rule) {
        // Regra é global se se aplica a qualquer tipo (implementação simplificada)
        return rule.appliesTo(AggregateRoot.class);
    }
    
    private void categorizeTypeSpecificRule(BusinessRule rule) {
        // Implementação simplificada - em cenário real seria mais sofisticada
        // Baseada em anotações, reflexão, ou configuração explícita
        
        // Por enquanto, assumir que regras específicas são registradas manualmente
        log.debug("Regra específica de tipo detectada: {}", rule.getRuleName());
    }
    
    private void validateRules(List<BusinessRule> rules, 
                             AggregateRoot aggregate, 
                             boolean isReplay, 
                             List<String> violations) {
        
        for (BusinessRule rule : rules) {
            try {
                // Pular regras que não devem ser validadas durante replay
                if (isReplay && !rule.validateOnReplay()) {
                    continue;
                }
                
                // Pular regras que não se aplicam ao tipo
                if (!rule.appliesTo(aggregate.getClass())) {
                    continue;
                }
                
                // Executar validação
                if (!rule.isValid(aggregate)) {
                    violations.add(String.format("[%s] %s", 
                        rule.getRuleName(), rule.getErrorMessage()));
                }
                
            } catch (Exception e) {
                log.error("Erro ao executar regra {}: {}", rule.getRuleName(), e.getMessage(), e);
                violations.add(String.format("[%s] Erro interno na validação", rule.getRuleName()));
            }
        }
    }
}
```

---

## 📊 **RESULTADO DE VALIDAÇÃO**

### **📋 ValidationResult**

```java
public class ValidationResult {
    
    private final boolean valid;
    private final List<String> errorMessages;
    private final String errorCode;
    private final Map<String, Object> metadata;
    
    private ValidationResult(boolean valid, 
                           List<String> errorMessages, 
                           String errorCode,
                           Map<String, Object> metadata) {
        this.valid = valid;
        this.errorMessages = errorMessages != null ? 
            new ArrayList<>(errorMessages) : new ArrayList<>();
        this.errorCode = errorCode;
        this.metadata = metadata != null ? 
            new HashMap<>(metadata) : new HashMap<>();
    }
    
    // === FACTORY METHODS ===
    
    public static ValidationResult valid() {
        return new ValidationResult(true, null, null, null);
    }
    
    public static ValidationResult valid(Map<String, Object> metadata) {
        return new ValidationResult(true, null, null, metadata);
    }
    
    public static ValidationResult invalid(String errorMessage) {
        return new ValidationResult(false, List.of(errorMessage), null, null);
    }
    
    public static ValidationResult invalid(List<String> errorMessages) {
        return new ValidationResult(false, errorMessages, null, null);
    }
    
    public static ValidationResult invalid(String errorMessage, String errorCode) {
        return new ValidationResult(false, List.of(errorMessage), errorCode, null);
    }
    
    public static ValidationResult invalid(List<String> errorMessages, 
                                         String errorCode, 
                                         Map<String, Object> metadata) {
        return new ValidationResult(false, errorMessages, errorCode, metadata);
    }
    
    // === MÉTODOS DE COMBINAÇÃO ===
    
    public ValidationResult combine(ValidationResult other) {
        if (this.valid && other.valid) {
            Map<String, Object> combinedMetadata = new HashMap<>(this.metadata);
            combinedMetadata.putAll(other.metadata);
            return ValidationResult.valid(combinedMetadata);
        }
        
        List<String> combinedErrors = new ArrayList<>(this.errorMessages);
        combinedErrors.addAll(other.errorMessages);
        
        Map<String, Object> combinedMetadata = new HashMap<>(this.metadata);
        combinedMetadata.putAll(other.metadata);
        
        return ValidationResult.invalid(combinedErrors, this.errorCode, combinedMetadata);
    }
    
    public ValidationResult addErrorMessage(String message) {
        if (valid) {
            return ValidationResult.invalid(message);
        }
        
        List<String> newErrors = new ArrayList<>(errorMessages);
        newErrors.add(message);
        return new ValidationResult(false, newErrors, errorCode, metadata);
    }
    
    public ValidationResult addErrorMessages(List<String> messages) {
        List<String> newErrors = new ArrayList<>(errorMessages);
        newErrors.addAll(messages);
        return new ValidationResult(false, newErrors, errorCode, metadata);
    }
    
    public ValidationResult withMetadata(String key, Object value) {
        Map<String, Object> newMetadata = new HashMap<>(metadata);
        newMetadata.put(key, value);
        return new ValidationResult(valid, errorMessages, errorCode, newMetadata);
    }
    
    // === GETTERS ===
    
    public boolean isValid() { return valid; }
    public boolean isInvalid() { return !valid; }
    public List<String> getErrorMessages() { return new ArrayList<>(errorMessages); }
    public String getErrorCode() { return errorCode; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    
    public boolean hasErrorMessages() { return !errorMessages.isEmpty(); }
    
    public String getFirstErrorMessage() {
        return errorMessages.isEmpty() ? null : errorMessages.get(0);
    }
    
    @Override
    public String toString() {
        if (valid) {
            return "ValidationResult{valid=true}";
        } else {
            return String.format("ValidationResult{valid=false, errors=%d, firstError='%s'}", 
                errorMessages.size(), getFirstErrorMessage());
        }
    }
}
```

---

## 🎯 **INTEGRAÇÃO COM AGREGADOS**

### **📋 Uso em Agregados**

```java
public class SinistroAggregate extends AggregateRoot {
    
    private final BusinessRuleValidator ruleValidator;
    
    // Construtor com injeção do validador
    public SinistroAggregate(BusinessRuleValidator ruleValidator) {
        super();
        this.ruleValidator = ruleValidator;
        registerBusinessRules();
    }
    
    /**
     * Cria um novo sinistro com validação completa.
     */
    public void criar(String numeroSinistro, 
                     String cpfSegurado,
                     String descricao,
                     LocalDate dataOcorrencia,
                     BigDecimal valorEstimado) {
        
        // Aplicar evento primeiro (para ter estado para validar)
        SinistroCriadoEvent event = new SinistroCriadoEvent(
            this.aggregateId, numeroSinistro, cpfSegurado, 
            descricao, dataOcorrencia, valorEstimado);
        
        applyEvent(event);
        
        // Validar regras de negócio após aplicar mudança
        ValidationResult validation = ruleValidator.validate(this);
        
        if (validation.isInvalid()) {
            // Reverter mudança (remover último evento)
            uncommittedEvents.remove(uncommittedEvents.size() - 1);
            this.version--;
            
            throw new BusinessRuleViolationException(
                "Violações de regras de negócio ao criar sinistro", 
                validation.getErrorMessages());
        }
    }
    
    /**
     * Atualiza valor estimado com validação.
     */
    public void atualizarValorEstimado(BigDecimal novoValor) {
        if (novoValor == null || novoValor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor estimado deve ser positivo");
        }
        
        // Aplicar evento
        SinistroValorAtualizadoEvent event = new SinistroValorAtualizadoEvent(
            this.aggregateId, this.version, novoValor);
        
        applyEvent(event);
        
        // Validar regras
        ValidationResult validation = ruleValidator.validate(this);
        
        if (validation.isInvalid()) {
            // Reverter
            uncommittedEvents.remove(uncommittedEvents.size() - 1);
            this.version--;
            
            throw new BusinessRuleViolationException(
                "Valor estimado viola regras de negócio", 
                validation.getErrorMessages());
        }
    }
    
    /**
     * Registra regras específicas do sinistro.
     */
    private void registerBusinessRules() {
        // Registrar regras inline para casos simples
        registerBusinessRule(new BusinessRule() {
            @Override
            public boolean isValid(AggregateRoot aggregate) {
                SinistroAggregate sinistro = (SinistroAggregate) aggregate;
                return sinistro.numeroSinistro != null && 
                       sinistro.numeroSinistro.matches("SIN-\\d{4}-\\d{6}");
            }
            
            @Override
            public String getErrorMessage() {
                return "Número do sinistro deve seguir padrão SIN-YYYY-NNNNNN";
            }
            
            @Override
            public String getRuleName() {
                return "NUMERO_SINISTRO_FORMATO";
            }
        });
        
        // Registrar outras regras específicas
        registerBusinessRule(new SinistroValorMaximoRule(new BigDecimal("1000000")));
        registerBusinessRule(new ApoliceVigenteRule());
    }
    
    @Override
    protected void validateBusinessRules() {
        ValidationResult validation = ruleValidator.validate(this);
        
        if (validation.isInvalid()) {
            throw new BusinessRuleViolationException(
                "Violações de regras de negócio", 
                validation.getErrorMessages());
        }
    }
}
```

---

## 🎓 **EXERCÍCIO PRÁTICO**

### **📝 Implementar Sistema de Regras Completo**

Crie um sistema de regras para `SeguradoAggregate` que:

1. **Valide CPF** com algoritmo completo
2. **Verifique unicidade** de email
3. **Controle limites** de endereços por segurado
4. **Implemente regras temporais** (idade mínima/máxima)
5. **Registre métricas** de validação

**Template:**
```java
// Regra de idade mínima
public class IdadeMinimaRule implements BusinessRule {
    
    private final int idadeMinima;
    
    public IdadeMinimaRule(int idadeMinima) {
        this.idadeMinima = idadeMinima;
    }
    
    @Override
    public boolean isValid(AggregateRoot aggregate) {
        // Sua implementação aqui
        return true;
    }
    
    @Override
    public String getErrorMessage() {
        return "Segurado deve ter pelo menos " + idadeMinima + " anos";
    }
}

// Regra de unicidade de email
public class EmailUnicoRule implements BusinessRule {
    
    private final SeguradoRepository repository;
    
    // Sua implementação completa aqui
}
```

---

## 📚 **REFERÊNCIAS**

- **Código**: `com.seguradora.hibrida.aggregate.validation`
- **Interface**: `BusinessRule`
- **Validador**: `BusinessRuleValidator`
- **Resultado**: `ValidationResult`

---

**📍 Próxima Parte**: [Agregados - Parte 5: Testes e Boas Práticas](./08-agregados-parte-5.md)

---

**📚 Roteiro elaborado por:** Principal Java Architect  
**🎯 Foco:** Regras de negócio e validações complexas  
**⏱️ Tempo estimado:** 60 minutos  
**🔧 Hands-on:** Sistema completo de regras de negócio