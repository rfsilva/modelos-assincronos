# 📖 CAPÍTULO 04: DOMAIN-DRIVEN DESIGN - PARTE 4
## Integração entre Bounded Contexts

### 🎯 **OBJETIVOS DESTA PARTE**
- Dominar integração entre contextos
- Implementar Anti-Corruption Layers
- Configurar Context Mapping
- Gerenciar Shared Kernels

---

## 🗺️ **CONTEXT MAPPING**

### **🔄 Padrões de Integração**

```java
// Localização: domain/integration/ContextMap.java

/**
 * MAPA DE INTEGRAÇÃO ENTRE CONTEXTOS
 * 
 * SINISTROS CONTEXT (Upstream) → PAGAMENTOS CONTEXT (Downstream)
 * Padrão: Customer/Supplier
 * - Sinistros publica eventos de aprovação
 * - Pagamentos consome e processa pagamentos
 * 
 * SINISTROS CONTEXT ↔ SEGURADOS CONTEXT
 * Padrão: Shared Kernel
 * - Compartilham conceitos básicos: CPF, Email, Telefone
 * - Ambos precisam de dados consistentes do segurado
 * 
 * SINISTROS CONTEXT → DETRAN CONTEXT (External)
 * Padrão: Anti-Corruption Layer
 * - DETRAN é sistema externo com modelo próprio
 * - ACL traduz entre modelos
 * 
 * SINISTROS CONTEXT ↔ APÓLICES CONTEXT
 * Padrão: Partnership
 * - Colaboração próxima para validações
 * - Evolução coordenada
 */

@Component
public class ContextIntegrationMap {
    
    // Definir relacionamentos entre contextos
    private final Map<String, ContextRelationship> relationships;
    
    public ContextIntegrationMap() {
        this.relationships = Map.of(
            "SINISTROS->PAGAMENTOS", ContextRelationship.CUSTOMER_SUPPLIER,
            "SINISTROS<->SEGURADOS", ContextRelationship.SHARED_KERNEL,
            "SINISTROS->DETRAN", ContextRelationship.ANTI_CORRUPTION_LAYER,
            "SINISTROS<->APOLICES", ContextRelationship.PARTNERSHIP
        );
    }
    
    public ContextRelationship getRelationship(String fromContext, String toContext) {
        String key = fromContext + "->" + toContext;
        return relationships.getOrDefault(key, ContextRelationship.SEPARATE_WAYS);
    }
}

public enum ContextRelationship {
    SHARED_KERNEL,
    CUSTOMER_SUPPLIER,
    PARTNERSHIP,
    ANTI_CORRUPTION_LAYER,
    OPEN_HOST_SERVICE,
    PUBLISHED_LANGUAGE,
    SEPARATE_WAYS,
    CONFORMIST
}
```

### **🛡️ Anti-Corruption Layer**

```java
// Localização: domain/integration/DetranAntiCorruptionLayer.java
@Component
public class DetranAntiCorruptionLayer {
    
    private final DetranExternalService detranService;
    private final DetranModelTranslator translator;
    private final DetranCircuitBreaker circuitBreaker;
    
    /**
     * ACL: Traduz consulta do domínio para API externa
     */
    public ConsultaDetranResult consultarVeiculo(PlacaVeiculo placa) {
        
        try {
            // 1. Traduzir entrada para modelo externo
            DetranConsultaRequest externalRequest = translator.toExternalRequest(placa);
            
            // 2. Chamar serviço externo com circuit breaker
            DetranResponse externalResponse = circuitBreaker.execute(() -> 
                detranService.consultarVeiculo(externalRequest)
            );
            
            // 3. Traduzir resposta para modelo do domínio
            return translator.toDomainResult(externalResponse);
            
        } catch (DetranServiceUnavailableException e) {
            return ConsultaDetranResult.indisponivel(
                "Serviço DETRAN temporariamente indisponível"
            );
            
        } catch (DetranInvalidDataException e) {
            return ConsultaDetranResult.dadosInvalidos(
                "Dados do veículo inválidos: " + e.getMessage()
            );
            
        } catch (Exception e) {
            log.error("Erro inesperado na consulta DETRAN: {}", e.getMessage(), e);
            return ConsultaDetranResult.erro(
                "Erro interno na consulta DETRAN"
            );
        }
    }
    
    /**
     * ACL: Traduz modelo externo para domínio
     */
    public DadosVeiculoDetran traduzirDadosVeiculo(DetranVeiculoData external) {
        
        return DadosVeiculoDetran.builder()
            .placa(PlacaVeiculo.of(external.getPlaca()))
            .chassi(Chassi.of(external.getChassi()))
            .renavam(Renavam.of(external.getRenavam()))
            .marca(MarcaVeiculo.of(external.getMarca()))
            .modelo(ModeloVeiculo.of(external.getModelo()))
            .anoFabricacao(AnoFabricacao.of(external.getAnoFabricacao()))
            .anoModelo(AnoModelo.of(external.getAnoModelo()))
            .cor(CorVeiculo.of(external.getCor()))
            .situacao(traduzirSituacaoVeiculo(external.getSituacao()))
            .restricoes(traduzirRestricoes(external.getRestricoes()))
            .dataConsulta(Instant.now())
            .build();
    }
    
    private SituacaoVeiculo traduzirSituacaoVeiculo(String situacaoExternal) {
        
        return switch (situacaoExternal.toUpperCase()) {
            case "REGULAR", "NORMAL" -> SituacaoVeiculo.REGULAR;
            case "ROUBO", "ROUBADO" -> SituacaoVeiculo.ROUBO;
            case "FURTO", "FURTADO" -> SituacaoVeiculo.FURTO;
            case "SINISTRO", "SINISTRADO" -> SituacaoVeiculo.SINISTRADO;
            case "BLOQUEIO", "BLOQUEADO" -> SituacaoVeiculo.BLOQUEADO;
            default -> {
                log.warn("Situação DETRAN desconhecida: {}", situacaoExternal);
                yield SituacaoVeiculo.DESCONHECIDA;
            }
        };
    }
    
    private List<RestricaoVeiculo> traduzirRestricoes(List<String> restricoesExternal) {
        
        if (restricoesExternal == null || restricoesExternal.isEmpty()) {
            return Collections.emptyList();
        }
        
        return restricoesExternal.stream()
            .map(this::traduzirRestricao)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    private RestricaoVeiculo traduzirRestricao(String restricaoExternal) {
        
        return switch (restricaoExternal.toUpperCase()) {
            case "ALIENACAO", "ALIENACAO_FIDUCIARIA" -> RestricaoVeiculo.ALIENACAO_FIDUCIARIA;
            case "RESERVA", "RESERVA_DOMINIO" -> RestricaoVeiculo.RESERVA_DOMINIO;
            case "JUDICIAL", "BLOQUEIO_JUDICIAL" -> RestricaoVeiculo.BLOQUEIO_JUDICIAL;
            case "ADMINISTRATIVO" -> RestricaoVeiculo.BLOQUEIO_ADMINISTRATIVO;
            case "COMUNICACAO_VENDA" -> RestricaoVeiculo.COMUNICACAO_VENDA;
            default -> {
                log.warn("Restrição DETRAN desconhecida: {}", restricaoExternal);
                yield null;
            }
        };
    }
}

// Localização: domain/integration/DetranModelTranslator.java
@Component
public class DetranModelTranslator {
    
    public DetranConsultaRequest toExternalRequest(PlacaVeiculo placa) {
        
        // Validar formato da placa para DETRAN
        String placaFormatada = formatarPlacaParaDetran(placa.getValor());
        
        return DetranConsultaRequest.builder()
            .placa(placaFormatada)
            .tipoConsulta("COMPLETA")
            .incluirRestricoes(true)
            .incluirHistorico(false) // Não precisamos do histórico
            .build();
    }
    
    public ConsultaDetranResult toDomainResult(DetranResponse external) {
        
        if (!external.isSuccess()) {
            return ConsultaDetranResult.erro(external.getErrorMessage());
        }
        
        DetranVeiculoData veiculoData = external.getVeiculoData();
        
        // Traduzir dados do veículo
        DadosVeiculoDetran dadosVeiculo = DadosVeiculoDetran.builder()
            .placa(PlacaVeiculo.of(veiculoData.getPlaca()))
            .chassi(Chassi.of(veiculoData.getChassi()))
            .renavam(Renavam.of(veiculoData.getRenavam()))
            .marca(MarcaVeiculo.of(veiculoData.getMarca()))
            .modelo(ModeloVeiculo.of(veiculoData.getModelo()))
            .anoFabricacao(AnoFabricacao.of(veiculoData.getAnoFabricacao()))
            .anoModelo(AnoModelo.of(veiculoData.getAnoModelo()))
            .cor(CorVeiculo.of(veiculoData.getCor()))
            .situacao(traduzirSituacao(veiculoData.getSituacao()))
            .restricoes(traduzirRestricoes(veiculoData.getRestricoes()))
            .build();
        
        return ConsultaDetranResult.sucesso(dadosVeiculo);
    }
    
    private String formatarPlacaParaDetran(String placa) {
        // DETRAN espera formato específico: ABC1234 ou ABC1D23
        return placa.replaceAll("[^A-Z0-9]", "").toUpperCase();
    }
}
```

---

## 🤝 **SHARED KERNEL**

### **💎 Conceitos Compartilhados**

```java
// Localização: shared/kernel/CommonValueObjects.java

/**
 * SHARED KERNEL: Value Objects compartilhados entre contextos
 * 
 * Estes objetos são compartilhados entre:
 * - Sinistros Context
 * - Segurados Context  
 * - Apólices Context
 * 
 * IMPORTANTE: Mudanças aqui afetam múltiplos contextos
 */

// CPF - usado em todos os contextos
public class Cpf {
    
    private final String valor;
    
    private Cpf(String valor) {
        this.valor = validarEFormatar(valor);
    }
    
    public static Cpf of(String valor) {
        return new Cpf(valor);
    }
    
    private String validarEFormatar(String cpf) {
        
        if (StringUtils.isBlank(cpf)) {
            throw new IllegalArgumentException("CPF não pode estar vazio");
        }
        
        // Remover formatação
        String apenasNumeros = cpf.replaceAll("[^0-9]", "");
        
        if (apenasNumeros.length() != 11) {
            throw new IllegalArgumentException("CPF deve ter 11 dígitos");
        }
        
        // Validar dígitos verificadores
        if (!validarDigitosVerificadores(apenasNumeros)) {
            throw new IllegalArgumentException("CPF inválido");
        }
        
        return apenasNumeros;
    }
    
    private boolean validarDigitosVerificadores(String cpf) {
        
        // Verificar se todos os dígitos são iguais
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }
        
        // Calcular primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int primeiroDigito = 11 - (soma % 11);
        if (primeiroDigito >= 10) primeiroDigito = 0;
        
        // Calcular segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int segundoDigito = 11 - (soma % 11);
        if (segundoDigito >= 10) segundoDigito = 0;
        
        // Verificar dígitos
        return Character.getNumericValue(cpf.charAt(9)) == primeiroDigito &&
               Character.getNumericValue(cpf.charAt(10)) == segundoDigito;
    }
    
    public String getValor() {
        return valor;
    }
    
    public String getValorFormatado() {
        return String.format("%s.%s.%s-%s",
            valor.substring(0, 3),
            valor.substring(3, 6),
            valor.substring(6, 9),
            valor.substring(9, 11)
        );
    }
    
    public String getValorMascarado() {
        return String.format("***.***.%s-%s",
            valor.substring(6, 9),
            valor.substring(9, 11)
        );
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cpf cpf = (Cpf) obj;
        return Objects.equals(valor, cpf.valor);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
    
    @Override
    public String toString() {
        return getValorFormatado();
    }
}

// Email - compartilhado entre contextos
public class Email {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );
    
    private final String endereco;
    
    private Email(String endereco) {
        this.endereco = validarEFormatar(endereco);
    }
    
    public static Email of(String endereco) {
        return new Email(endereco);
    }
    
    private String validarEFormatar(String email) {
        
        if (StringUtils.isBlank(email)) {
            throw new IllegalArgumentException("Email não pode estar vazio");
        }
        
        String emailLimpo = email.trim().toLowerCase();
        
        if (!EMAIL_PATTERN.matcher(emailLimpo).matches()) {
            throw new IllegalArgumentException("Formato de email inválido: " + email);
        }
        
        if (emailLimpo.length() > 254) {
            throw new IllegalArgumentException("Email muito longo (máximo 254 caracteres)");
        }
        
        return emailLimpo;
    }
    
    public String getEndereco() {
        return endereco;
    }
    
    public String getDominio() {
        return endereco.substring(endereco.indexOf('@') + 1);
    }
    
    public String getUsuario() {
        return endereco.substring(0, endereco.indexOf('@'));
    }
    
    public boolean isGmail() {
        return getDominio().equals("gmail.com");
    }
    
    public boolean isDominioEmpresarial() {
        return !getDominio().matches(".*(gmail|hotmail|yahoo|outlook)\\.com");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Email email = (Email) obj;
        return Objects.equals(endereco, email.endereco);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(endereco);
    }
    
    @Override
    public String toString() {
        return endereco;
    }
}

// Telefone - compartilhado entre contextos
public class Telefone {
    
    private static final Pattern TELEFONE_PATTERN = Pattern.compile(
        "^\\(?([0-9]{2})\\)?[-. ]?([0-9]{4,5})[-. ]?([0-9]{4})$"
    );
    
    private final String numero;
    private final String ddd;
    private final TipoTelefone tipo;
    
    private Telefone(String numero, String ddd, TipoTelefone tipo) {
        this.numero = numero;
        this.ddd = ddd;
        this.tipo = tipo;
    }
    
    public static Telefone of(String telefoneCompleto) {
        
        if (StringUtils.isBlank(telefoneCompleto)) {
            throw new IllegalArgumentException("Telefone não pode estar vazio");
        }
        
        // Remover formatação
        String apenasNumeros = telefoneCompleto.replaceAll("[^0-9]", "");
        
        if (apenasNumeros.length() < 10 || apenasNumeros.length() > 11) {
            throw new IllegalArgumentException("Telefone deve ter 10 ou 11 dígitos");
        }
        
        String ddd = apenasNumeros.substring(0, 2);
        String numero = apenasNumeros.substring(2);
        
        // Validar DDD
        if (!isValidDdd(ddd)) {
            throw new IllegalArgumentException("DDD inválido: " + ddd);
        }
        
        // Determinar tipo
        TipoTelefone tipo = numero.length() == 9 && numero.startsWith("9") ? 
            TipoTelefone.CELULAR : TipoTelefone.FIXO;
        
        return new Telefone(numero, ddd, tipo);
    }
    
    private static boolean isValidDdd(String ddd) {
        // DDDs válidos no Brasil
        Set<String> dddsValidos = Set.of(
            "11", "12", "13", "14", "15", "16", "17", "18", "19", // SP
            "21", "22", "24", // RJ
            "27", "28", // ES
            "31", "32", "33", "34", "35", "37", "38", // MG
            "41", "42", "43", "44", "45", "46", // PR
            "47", "48", "49", // SC
            "51", "53", "54", "55", // RS
            "61", // DF
            "62", "64", // GO
            "63", // TO
            "65", "66", // MT
            "67", // MS
            "68", // AC
            "69", // RO
            "71", "73", "74", "75", "77", // BA
            "79", // SE
            "81", "87", // PE
            "82", // AL
            "83", // PB
            "84", // RN
            "85", "88", // CE
            "86", "89", // PI
            "91", "93", "94", // PA
            "92", "97", // AM
            "95", // RR
            "96", // AP
            "98", "99" // MA
        );
        
        return dddsValidos.contains(ddd);
    }
    
    public String getNumeroCompleto() {
        return ddd + numero;
    }
    
    public String getNumeroFormatado() {
        if (tipo == TipoTelefone.CELULAR) {
            return String.format("(%s) %s-%s",
                ddd,
                numero.substring(0, 5),
                numero.substring(5)
            );
        } else {
            return String.format("(%s) %s-%s",
                ddd,
                numero.substring(0, 4),
                numero.substring(4)
            );
        }
    }
    
    // Getters
    public String getNumero() { return numero; }
    public String getDdd() { return ddd; }
    public TipoTelefone getTipo() { return tipo; }
    
    public boolean isCelular() { return tipo == TipoTelefone.CELULAR; }
    public boolean isFixo() { return tipo == TipoTelefone.FIXO; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Telefone telefone = (Telefone) obj;
        return Objects.equals(numero, telefone.numero) && Objects.equals(ddd, telefone.ddd);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(numero, ddd);
    }
    
    @Override
    public String toString() {
        return getNumeroFormatado();
    }
}

public enum TipoTelefone {
    CELULAR, FIXO
}
```

---

## 🔄 **CUSTOMER/SUPPLIER**

### **📤 Published Language**

```java
// Localização: domain/integration/SinistroPublishedLanguage.java

/**
 * PUBLISHED LANGUAGE: Linguagem publicada para integração
 * 
 * Define contratos estáveis para outros contextos consumirem
 * eventos do contexto de Sinistros
 */

// DTOs para integração
public class SinistroIntegrationEvent {
    
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String sinistroId;
    private String numeroSinistro;
    private SinistroIntegrationData sinistroData;
    
    // Construtor, getters e setters...
}

public class SinistroIntegrationData {
    
    // Dados básicos sempre presentes
    private String status;
    private String cpfSegurado;
    private String placaVeiculo;
    private String apoliceNumero;
    private LocalDateTime dataOcorrencia;
    
    // Dados opcionais dependendo do evento
    private BigDecimal valorEstimado;
    private BigDecimal valorIndenizacao;
    private String operadorResponsavel;
    private String motivoRejeicao;
    private LocalDateTime dataAprovacao;
    private LocalDateTime dataRejeicao;
    
    // Dados para pagamento (apenas em eventos de aprovação)
    private ContaBancariaIntegration contaDestino;
    private String codigoBarras;
    private LocalDate dataVencimento;
    
    // Getters e setters...
}

// Tradutor para Published Language
@Component
public class SinistroIntegrationTranslator {
    
    public SinistroIntegrationEvent translate(SinistroAprovadoEvent domainEvent) {
        
        SinistroIntegrationData data = SinistroIntegrationData.builder()
            .status("APROVADO")
            .cpfSegurado(domainEvent.getCpfSegurado())
            .placaVeiculo(domainEvent.getPlacaVeiculo())
            .apoliceNumero(domainEvent.getApoliceNumero())
            .dataOcorrencia(domainEvent.getDataOcorrencia())
            .valorEstimado(domainEvent.getValorEstimado().getValor())
            .valorIndenizacao(domainEvent.getValorIndenizacao().getValor())
            .operadorResponsavel(domainEvent.getOperadorAprovador().getValue())
            .dataAprovacao(domainEvent.getDataAprovacao())
            .contaDestino(translateContaBancaria(domainEvent.getContaDestino()))
            .build();
        
        return SinistroIntegrationEvent.builder()
            .eventId(domainEvent.getEventId().toString())
            .eventType("SINISTRO_APROVADO")
            .timestamp(domainEvent.getTimestamp())
            .sinistroId(domainEvent.getSinistroId().getValue())
            .numeroSinistro(domainEvent.getNumeroSinistro().getValor())
            .sinistroData(data)
            .build();
    }
    
    public SinistroIntegrationEvent translate(SinistroRejeitadoEvent domainEvent) {
        
        SinistroIntegrationData data = SinistroIntegrationData.builder()
            .status("REJEITADO")
            .cpfSegurado(domainEvent.getCpfSegurado())
            .placaVeiculo(domainEvent.getPlacaVeiculo())
            .apoliceNumero(domainEvent.getApoliceNumero())
            .dataOcorrencia(domainEvent.getDataOcorrencia())
            .valorEstimado(domainEvent.getValorEstimado().getValor())
            .operadorResponsavel(domainEvent.getOperadorRejeicao().getValue())
            .motivoRejeicao(domainEvent.getMotivoRejeicao().getDescricao())
            .dataRejeicao(domainEvent.getDataRejeicao())
            .build();
        
        return SinistroIntegrationEvent.builder()
            .eventId(domainEvent.getEventId().toString())
            .eventType("SINISTRO_REJEITADO")
            .timestamp(domainEvent.getTimestamp())
            .sinistroId(domainEvent.getSinistroId().getValue())
            .numeroSinistro(domainEvent.getNumeroSinistro().getValor())
            .sinistroData(data)
            .build();
    }
}

// Publisher para outros contextos
@Component
public class SinistroIntegrationPublisher {
    
    private final EventBus eventBus;
    private final SinistroIntegrationTranslator translator;
    
    @EventHandler
    public void handle(SinistroAprovadoEvent domainEvent) {
        
        // Traduzir para linguagem de integração
        SinistroIntegrationEvent integrationEvent = translator.translate(domainEvent);
        
        // Publicar para outros contextos
        eventBus.publish(integrationEvent);
        
        // Log para auditoria
        log.info("Evento de integração publicado: {} para sinistro {}",
                integrationEvent.getEventType(),
                integrationEvent.getNumeroSinistro());
    }
    
    @EventHandler
    public void handle(SinistroRejeitadoEvent domainEvent) {
        
        SinistroIntegrationEvent integrationEvent = translator.translate(domainEvent);
        eventBus.publish(integrationEvent);
        
        log.info("Evento de integração publicado: {} para sinistro {}",
                integrationEvent.getEventType(),
                integrationEvent.getNumeroSinistro());
    }
}
```

---

## 🧪 **EXERCÍCIO PRÁTICO**

### **🎯 Objetivo**: Implementar integração entre contextos

#### **Passo 1: Criar Anti-Corruption Layer**
```java
@Component
public class SeguradoAntiCorruptionLayer {
    
    private final SeguradoExternalService seguradoService;
    
    public Segurado obterSegurado(Cpf cpf) {
        // Traduzir chamada externa para modelo do domínio
        SeguradoExternalModel external = seguradoService.buscarPorCpf(cpf.getValor());
        
        return Segurado.builder()
            .id(SeguradoId.of(external.getId()))
            .cpf(cpf)
            .nome(NomePessoa.of(external.getNome()))
            .email(Email.of(external.getEmail()))
            .telefone(Telefone.of(external.getTelefone()))
            .build();
    }
}
```

#### **Passo 2: Implementar Shared Kernel**
```java
// Criar Value Object compartilhado
public class PlacaVeiculo {
    
    private final String valor;
    
    private PlacaVeiculo(String valor) {
        this.valor = validarEFormatar(valor);
    }
    
    public static PlacaVeiculo of(String valor) {
        return new PlacaVeiculo(valor);
    }
    
    private String validarEFormatar(String placa) {
        // Validar formato brasileiro: ABC1234 ou ABC1D23
        String placaLimpa = placa.replaceAll("[^A-Z0-9]", "").toUpperCase();
        
        if (!placaLimpa.matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}")) {
            throw new IllegalArgumentException("Formato de placa inválido");
        }
        
        return placaLimpa;
    }
    
    // equals, hashCode, toString...
}
```

#### **Passo 3: Testar Integração**
```java
@Test
public void testarIntegracaoContextos() {
    // Criar sinistro que requer dados de outros contextos
    CriarSinistroRequest request = CriarSinistroRequest.builder()
        .cpfSegurado("12345678901")
        .placaVeiculo("ABC1234")
        .descricao("Teste integração")
        .build();
    
    // Factory deve usar ACLs para obter dados
    SinistroAggregate sinistro = sinistroFactory.criarSinistro(request);
    
    // Verificar se dados foram obtidos corretamente
    assertThat(sinistro.getSeguradoId()).isNotNull();
    assertThat(sinistro.getApoliceId()).isNotNull();
    assertThat(sinistro.getVeiculoId()).isNotNull();
}
```

---

## 📚 **CHECKPOINT DE APRENDIZADO**

### **✅ Você deve ser capaz de:**

1. **Mapear** relacionamentos entre contextos
2. **Implementar** Anti-Corruption Layers
3. **Definir** Shared Kernels apropriados
4. **Criar** Published Languages para integração
5. **Configurar** padrões Customer/Supplier

### **❓ Perguntas para Reflexão:**

1. Quando usar ACL vs Shared Kernel?
2. Como evoluir Shared Kernel sem quebrar contextos?
3. Qual granularidade ideal para Published Language?
4. Como gerenciar dependências entre contextos?

---

## 🔗 **PRÓXIMA PARTE**

Na **Parte 5**, vamos finalizar com:
- Padrões avançados de DDD
- Evolução de domínio
- Refactoring de bounded contexts
- Boas práticas e lições aprendidas

---

**📖 Parte elaborada por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Estimada:** 36 minutos  
**📋 Pré-requisitos:** DDD Partes 1-3