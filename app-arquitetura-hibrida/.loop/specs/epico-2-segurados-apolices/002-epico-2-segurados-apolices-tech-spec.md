# Technical Specification - Épico 2: Domínio de Segurados e Apólices

## 1. Arquitetura Técnica

### 1.1 Visão Geral da Arquitetura

O Épico 2 implementa o domínio de negócios core do sistema de seguros utilizando **Event Sourcing** e **CQRS** sobre a infraestrutura estabelecida no Épico 1.

```
┌─────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐ │
│  │  REST APIs   │  │   GraphQL    │  │  WebSocket Events    │ │
│  └──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘ │
└─────────┼──────────────────┼───────────────────────┼────────────┘
          │                  │                       │
┌─────────▼──────────────────▼───────────────────────▼────────────┐
│                       APPLICATION LAYER                          │
│  ┌──────────────┐         ┌─────────────────────────────────┐  │
│  │ Command Side │◄────────┤       Command Bus               │  │
│  │              │         └─────────────────────────────────┘  │
│  │ • Commands   │                                               │
│  │ • Handlers   │         ┌─────────────────────────────────┐  │
│  │ • Validation │◄────────┤       Event Bus                 │  │
│  └──────────────┘         └─────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────┐         ┌─────────────────────────────────┐  │
│  │  Query Side  │         │   Query Services                │  │
│  │              │         │   • Cache Layer (Redis)         │  │
│  │ • Queries    │◄────────┤   • Pagination                  │  │
│  │ • Read Models│         │   • Filtering                   │  │
│  └──────────────┘         └─────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
          │                                       │
┌─────────▼───────────────────────────────────────▼────────────────┐
│                         DOMAIN LAYER                             │
│  ┌───────────────────────┐      ┌──────────────────────────┐    │
│  │  SeguradoAggregate    │      │   ApoliceAggregate       │    │
│  │  • State              │◄─────┤   • State                │    │
│  │  • Business Rules     │      │   • Business Rules       │    │
│  │  • Domain Events      │      │   • Domain Events        │    │
│  │  • Invariants         │      │   • Invariants           │    │
│  └───────────────────────┘      └──────────────────────────┘    │
└──────────────────────────────────────────────────────────────────┘
          │                                       │
┌─────────▼───────────────────────────────────────▼────────────────┐
│                    INFRASTRUCTURE LAYER                          │
│  ┌──────────────────┐        ┌───────────────────────────────┐  │
│  │  Event Store     │        │   Projection Store            │  │
│  │  (Write DB)      │        │   (Read DB)                   │  │
│  │  • Events        │        │   • Query Models              │  │
│  │  • Snapshots     │        │   • Denormalized Data         │  │
│  │  PostgreSQL:5435 │        │   PostgreSQL:5436             │  │
│  └──────────────────┘        └───────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────┐        ┌───────────────────────────────┐  │
│  │  Message Broker  │        │   Cache Layer                 │  │
│  │  Apache Kafka    │        │   Redis                       │  │
│  │  • Event Topics  │        │   • Query Cache               │  │
│  │  • DLQ           │        │   • Session Cache             │  │
│  └──────────────────┘        └───────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
```

### 1.2 Bounded Contexts

#### Contexto: Segurados
**Responsabilidades**:
- Gerenciamento do ciclo de vida de segurados
- Validação de dados cadastrais
- Controle de status (ativo/inativo)
- Auditoria de alterações

**Linguagem Ubíqua**:
- **Segurado**: Entidade principal representando cliente
- **CPF**: Identificador único nacional
- **Status**: Estado do segurado (ATIVO/INATIVO)
- **Endereço**: Localização física do segurado

#### Contexto: Apólices
**Responsabilidades**:
- Emissão e gestão de contratos de seguro
- Cálculo de prêmios
- Controle de vigência
- Relacionamento com segurados e veículos

**Linguagem Ubíqua**:
- **Apólice**: Contrato de seguro
- **Prêmio**: Valor do seguro
- **Cobertura**: Tipos de proteção
- **Franquia**: Participação do segurado
- **Vigência**: Período de validade

## 2. Modelo de Domínio

### 2.1 Aggregate: SeguradoAggregate

#### Estrutura de Estado
```java
package com.seguradora.hibrida.aggregate.segurado;

import com.seguradora.hibrida.aggregate.AggregateRoot;
import com.seguradora.hibrida.aggregate.BusinessRule;
import java.time.LocalDate;
import java.util.UUID;

public class SeguradoAggregate extends AggregateRoot {
    
    // Identificação
    private UUID seguradoId;
    private String cpf;
    private String nome;
    private LocalDate dataNascimento;
    
    // Contato
    private Email email;
    private Telefone telefone;
    
    // Endereço
    private Endereco endereco;
    
    // Estado
    private SeguradoStatus status;
    private LocalDate dataCadastro;
    private LocalDate dataUltimaAtualizacao;
    
    // Metadados
    private String motivoDesativacao;
    
    // Construtor para reconstrução via Event Sourcing
    public SeguradoAggregate() {
        super();
    }
    
    // Factory Method - Command: CriarSeguradoCommand
    public static SeguradoAggregate criar(
            String cpf,
            String nome,
            LocalDate dataNascimento,
            Email email,
            Telefone telefone,
            Endereco endereco) {
        
        // Validações de invariantes
        validarInvariantes(cpf, nome, dataNascimento, email, telefone, endereco);
        
        // Criar aggregate
        SeguradoAggregate segurado = new SeguradoAggregate();
        
        // Aplicar evento
        segurado.apply(new SeguradoCriadoEvent(
            UUID.randomUUID(),
            cpf,
            nome,
            dataNascimento,
            email,
            telefone,
            endereco,
            LocalDate.now()
        ));
        
        return segurado;
    }
    
    // Command: AtualizarSeguradoCommand
    public void atualizar(
            String nome,
            Email email,
            Telefone telefone,
            Endereco endereco) {
        
        checkInvariant(this.status == SeguradoStatus.ATIVO,
            "Somente segurados ativos podem ser atualizados");
        
        // Detectar mudanças
        boolean houveAlteracao = false;
        
        if (!this.nome.equals(nome) ||
            !this.email.equals(email) ||
            !this.telefone.equals(telefone) ||
            !this.endereco.equals(endereco)) {
            houveAlteracao = true;
        }
        
        if (houveAlteracao) {
            apply(new SeguradoAtualizadoEvent(
                this.seguradoId,
                nome,
                email,
                telefone,
                endereco,
                LocalDate.now()
            ));
        }
    }
    
    // Command: DesativarSeguradoCommand
    public void desativar(String motivo) {
        checkInvariant(this.status == SeguradoStatus.ATIVO,
            "Segurado já está desativado");
        checkInvariant(motivo != null && !motivo.isBlank(),
            "Motivo da desativação é obrigatório");
        
        apply(new SeguradoDesativadoEvent(
            this.seguradoId,
            motivo,
            LocalDate.now()
        ));
    }
    
    // Command: ReativarSeguradoCommand
    public void reativar() {
        checkInvariant(this.status == SeguradoStatus.INATIVO,
            "Segurado já está ativo");
        
        apply(new SeguradoReativadoEvent(
            this.seguradoId,
            LocalDate.now()
        ));
    }
    
    // Event Handlers - Aplicação de estado
    @EventSourcingHandler
    private void on(SeguradoCriadoEvent event) {
        this.seguradoId = event.getSeguradoId();
        this.cpf = event.getCpf();
        this.nome = event.getNome();
        this.dataNascimento = event.getDataNascimento();
        this.email = event.getEmail();
        this.telefone = event.getTelefone();
        this.endereco = event.getEndereco();
        this.status = SeguradoStatus.ATIVO;
        this.dataCadastro = event.getDataCadastro();
    }
    
    @EventSourcingHandler
    private void on(SeguradoAtualizadoEvent event) {
        this.nome = event.getNome();
        this.email = event.getEmail();
        this.telefone = event.getTelefone();
        this.endereco = event.getEndereco();
        this.dataUltimaAtualizacao = event.getDataAtualizacao();
    }
    
    @EventSourcingHandler
    private void on(SeguradoDesativadoEvent event) {
        this.status = SeguradoStatus.INATIVO;
        this.motivoDesativacao = event.getMotivo();
        this.dataUltimaAtualizacao = event.getDataDesativacao();
    }
    
    @EventSourcingHandler
    private void on(SeguradoReativadoEvent event) {
        this.status = SeguradoStatus.ATIVO;
        this.motivoDesativacao = null;
        this.dataUltimaAtualizacao = event.getDataReativacao();
    }
    
    // Validações de Invariantes
    private static void validarInvariantes(
            String cpf,
            String nome,
            LocalDate dataNascimento,
            Email email,
            Telefone telefone,
            Endereco endereco) {
        
        // CPF
        BusinessRule.check(cpf != null && !cpf.isBlank(),
            "CPF é obrigatório");
        BusinessRule.check(CpfValidator.isValid(cpf),
            "CPF inválido");
        
        // Nome
        BusinessRule.check(nome != null && !nome.isBlank(),
            "Nome é obrigatório");
        BusinessRule.check(nome.length() >= 3 && nome.length() <= 200,
            "Nome deve ter entre 3 e 200 caracteres");
        
        // Data de Nascimento
        BusinessRule.check(dataNascimento != null,
            "Data de nascimento é obrigatória");
        int idade = Period.between(dataNascimento, LocalDate.now()).getYears();
        BusinessRule.check(idade >= 18,
            "Segurado deve ter no mínimo 18 anos");
        BusinessRule.check(idade <= 80,
            "Segurado deve ter no máximo 80 anos");
        
        // Email, Telefone, Endereço validados em Value Objects
    }
    
    // Getters
    public UUID getSeguradoId() { return seguradoId; }
    public String getCpf() { return cpf; }
    public String getNome() { return nome; }
    public SeguradoStatus getStatus() { return status; }
    // ... outros getters
}
```

#### Value Objects

**Email**:
```java
public class Email {
    private final String endereco;
    
    public Email(String endereco) {
        BusinessRule.check(endereco != null && !endereco.isBlank(),
            "Email é obrigatório");
        BusinessRule.check(EmailValidator.isValid(endereco),
            "Email inválido");
        this.endereco = endereco.toLowerCase();
    }
    
    public String getEndereco() { return endereco; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email)) return false;
        Email email = (Email) o;
        return endereco.equals(email.endereco);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(endereco);
    }
}
```

**Telefone**:
```java
public class Telefone {
    private final String ddd;
    private final String numero;
    
    public Telefone(String ddd, String numero) {
        BusinessRule.check(ddd != null && ddd.matches("\\d{2}"),
            "DDD inválido (2 dígitos)");
        BusinessRule.check(numero != null && numero.matches("\\d{8,9}"),
            "Número inválido (8 ou 9 dígitos)");
        this.ddd = ddd;
        this.numero = numero;
    }
    
    public String getTelefoneCompleto() {
        return String.format("(%s) %s", ddd, numero);
    }
    
    // equals, hashCode
}
```

**Endereco**:
```java
public class Endereco {
    private final String cep;
    private final String logradouro;
    private final String numero;
    private final String complemento;
    private final String bairro;
    private final String cidade;
    private final String uf;
    
    public Endereco(String cep, String logradouro, String numero,
                    String complemento, String bairro, String cidade, String uf) {
        // Validações
        BusinessRule.check(cep != null && cep.matches("\\d{8}"),
            "CEP inválido (8 dígitos)");
        BusinessRule.check(logradouro != null && !logradouro.isBlank(),
            "Logradouro é obrigatório");
        BusinessRule.check(numero != null && !numero.isBlank(),
            "Número é obrigatório");
        BusinessRule.check(bairro != null && !bairro.isBlank(),
            "Bairro é obrigatório");
        BusinessRule.check(cidade != null && !cidade.isBlank(),
            "Cidade é obrigatória");
        BusinessRule.check(uf != null && UF.isValid(uf),
            "UF inválida");
        
        this.cep = cep;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.uf = uf;
    }
    
    // getters, equals, hashCode
}
```

#### Enums

**SeguradoStatus**:
```java
public enum SeguradoStatus {
    ATIVO,
    INATIVO
}
```

### 2.2 Aggregate: ApoliceAggregate

#### Estrutura de Estado
```java
package com.seguradora.hibrida.aggregate.apolice;

import com.seguradora.hibrida.aggregate.AggregateRoot;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public class ApoliceAggregate extends AggregateRoot {
    
    // Identificação
    private UUID apoliceId;
    private String numeroApolice; // APO-AAAA-NNNNNN
    private UUID seguradoId;
    private UUID veiculoId;
    
    // Vigência
    private LocalDate dataInicioVigencia;
    private LocalDate dataFimVigencia;
    private ApoliceStatus status;
    
    // Cobertura
    private Set<TipoCobertura> coberturas;
    private TipoFranquia franquia;
    private BigDecimal valorVeiculo;
    
    // Valores
    private BigDecimal premioTotal;
    private Integer numeroParcelas;
    private BigDecimal valorParcela;
    private BigDecimal percentualDesconto;
    
    // Cancelamento
    private LocalDate dataCancelamento;
    private String motivoCancelamento;
    private BigDecimal valorDevolucao;
    
    public ApoliceAggregate() {
        super();
    }
    
    // Factory Method - Command: CriarApoliceCommand
    public static ApoliceAggregate criar(
            UUID seguradoId,
            UUID veiculoId,
            Set<TipoCobertura> coberturas,
            TipoFranquia franquia,
            BigDecimal valorVeiculo,
            LocalDate dataInicioVigencia,
            int mesesVigencia,
            BigDecimal percentualDesconto,
            CalculadoraPremio calculadora) {
        
        // Validações
        validarInvariantes(seguradoId, veiculoId, coberturas, franquia, 
                          valorVeiculo, dataInicioVigencia, mesesVigencia);
        
        // Cálculo do prêmio
        BigDecimal premioTotal = calculadora.calcular(
            valorVeiculo, coberturas, franquia, percentualDesconto
        );
        
        // Data fim vigência
        LocalDate dataFimVigencia = dataInicioVigencia.plusMonths(mesesVigencia);
        
        // Criar aggregate
        ApoliceAggregate apolice = new ApoliceAggregate();
        
        // Aplicar evento
        apolice.apply(new ApoliceCriadaEvent(
            UUID.randomUUID(),
            gerarNumeroApolice(),
            seguradoId,
            veiculoId,
            coberturas,
            franquia,
            valorVeiculo,
            dataInicioVigencia,
            dataFimVigencia,
            premioTotal,
            percentualDesconto,
            LocalDate.now()
        ));
        
        return apolice;
    }
    
    // Command: AtualizarCoberturaCommand
    public void atualizarCobertura(
            Set<TipoCobertura> novasCoberturas,
            TipoFranquia novaFranquia,
            CalculadoraPremio calculadora) {
        
        checkInvariant(this.status == ApoliceStatus.VIGENTE,
            "Somente apólices vigentes podem ser alteradas");
        checkInvariant(!novasCoberturas.isEmpty(),
            "Pelo menos uma cobertura deve ser selecionada");
        
        // Recalcular prêmio
        BigDecimal novoPremio = calculadora.calcular(
            this.valorVeiculo, novasCoberturas, novaFranquia, this.percentualDesconto
        );
        
        apply(new ApoliceAtualizadaEvent(
            this.apoliceId,
            novasCoberturas,
            novaFranquia,
            novoPremio,
            LocalDate.now()
        ));
    }
    
    // Command: CancelarApoliceCommand
    public void cancelar(String motivo, CalculadoraDevolucao calculadora) {
        checkInvariant(this.status == ApoliceStatus.VIGENTE,
            "Somente apólices vigentes podem ser canceladas");
        checkInvariant(motivo != null && !motivo.isBlank(),
            "Motivo do cancelamento é obrigatório");
        
        // Calcular devolução
        BigDecimal valorDevolucao = calculadora.calcular(
            this.premioTotal,
            this.dataInicioVigencia,
            LocalDate.now(),
            this.dataFimVigencia
        );
        
        apply(new ApoliceCanceladaEvent(
            this.apoliceId,
            LocalDate.now(),
            motivo,
            valorDevolucao
        ));
    }
    
    // Command: RenovarApoliceCommand
    public ApoliceAggregate renovar(
            LocalDate novaDataInicio,
            int mesesVigencia,
            CalculadoraPremio calculadora) {
        
        checkInvariant(
            this.status == ApoliceStatus.VIGENTE || this.status == ApoliceStatus.VENCIDA,
            "Apólice não pode ser renovada no status atual"
        );
        checkInvariant(
            novaDataInicio.isAfter(this.dataFimVigencia) ||
            novaDataInicio.isEqual(this.dataFimVigencia.plusDays(1)),
            "Data de início deve ser após o fim da vigência atual"
        );
        
        // Criar nova apólice com desconto de renovação
        BigDecimal descontoRenovacao = BigDecimal.valueOf(0.05); // 5%
        
        return ApoliceAggregate.criar(
            this.seguradoId,
            this.veiculoId,
            this.coberturas,
            this.franquia,
            this.valorVeiculo,
            novaDataInicio,
            mesesVigencia,
            descontoRenovacao,
            calculadora
        );
    }
    
    // Event Handlers
    @EventSourcingHandler
    private void on(ApoliceCriadaEvent event) {
        this.apoliceId = event.getApoliceId();
        this.numeroApolice = event.getNumeroApolice();
        this.seguradoId = event.getSeguradoId();
        this.veiculoId = event.getVeiculoId();
        this.coberturas = event.getCoberturas();
        this.franquia = event.getFranquia();
        this.valorVeiculo = event.getValorVeiculo();
        this.dataInicioVigencia = event.getDataInicioVigencia();
        this.dataFimVigencia = event.getDataFimVigencia();
        this.premioTotal = event.getPremioTotal();
        this.percentualDesconto = event.getPercentualDesconto();
        this.status = ApoliceStatus.VIGENTE;
    }
    
    @EventSourcingHandler
    private void on(ApoliceAtualizadaEvent event) {
        this.coberturas = event.getCoberturas();
        this.franquia = event.getFranquia();
        this.premioTotal = event.getPremioTotal();
    }
    
    @EventSourcingHandler
    private void on(ApoliceCanceladaEvent event) {
        this.status = ApoliceStatus.CANCELADA;
        this.dataCancelamento = event.getDataCancelamento();
        this.motivoCancelamento = event.getMotivo();
        this.valorDevolucao = event.getValorDevolucao();
    }
    
    // Validações de Invariantes
    private static void validarInvariantes(
            UUID seguradoId,
            UUID veiculoId,
            Set<TipoCobertura> coberturas,
            TipoFranquia franquia,
            BigDecimal valorVeiculo,
            LocalDate dataInicioVigencia,
            int mesesVigencia) {
        
        BusinessRule.check(seguradoId != null,
            "Segurado é obrigatório");
        BusinessRule.check(veiculoId != null,
            "Veículo é obrigatório");
        BusinessRule.check(coberturas != null && !coberturas.isEmpty(),
            "Pelo menos uma cobertura deve ser selecionada");
        BusinessRule.check(franquia != null,
            "Franquia é obrigatória");
        BusinessRule.check(valorVeiculo != null && valorVeiculo.compareTo(BigDecimal.ZERO) > 0,
            "Valor do veículo deve ser maior que zero");
        BusinessRule.check(dataInicioVigencia != null,
            "Data de início de vigência é obrigatória");
        BusinessRule.check(!dataInicioVigencia.isBefore(LocalDate.now()),
            "Data de início não pode ser no passado");
        BusinessRule.check(mesesVigencia >= 1 && mesesVigencia <= 12,
            "Vigência deve ser entre 1 e 12 meses");
    }
    
    // Utilitários
    private static String gerarNumeroApolice() {
        int ano = LocalDate.now().getYear();
        int sequencial = generateSequencial(); // Implementar gerador de sequencial
        return String.format("APO-%04d-%06d", ano, sequencial);
    }
    
    // Getters
    public UUID getApoliceId() { return apoliceId; }
    public String getNumeroApolice() { return numeroApolice; }
    public ApoliceStatus getStatus() { return status; }
    // ... outros getters
}
```

#### Enums

**ApoliceStatus**:
```java
public enum ApoliceStatus {
    VIGENTE,
    VENCIDA,
    CANCELADA,
    SUSPENSA
}
```

**TipoCobertura**:
```java
public enum TipoCobertura {
    COMPREENSIVA("Compreensiva", 1.0),
    TERCEIROS("Responsabilidade Civil", 0.3),
    COLISAO("Colisão", 0.6),
    ROUBO_FURTO("Roubo e Furto", 0.5),
    INCENDIO("Incêndio", 0.2),
    VIDROS("Vidros", 0.1);
    
    private final String descricao;
    private final double fatorPremio;
    
    TipoCobertura(String descricao, double fatorPremio) {
        this.descricao = descricao;
        this.fatorPremio = fatorPremio;
    }
    
    public String getDescricao() { return descricao; }
    public double getFatorPremio() { return fatorPremio; }
}
```

**TipoFranquia**:
```java
public enum TipoFranquia {
    NORMAL("Normal", 1.0),
    REDUZIDA("Reduzida", 1.2),
    MAJORADA("Majorada", 0.8);
    
    private final String descricao;
    private final double fatorPremio;
    
    TipoFranquia(String descricao, double fatorPremio) {
        this.descricao = descricao;
        this.fatorPremio = fatorPremio;
    }
    
    public String getDescricao() { return descricao; }
    public double getFatorPremio() { return fatorPremio; }
}
```

#### Domain Services

**CalculadoraPremio**:
```java
@Service
public class CalculadoraPremio {
    
    @Value("${apolice.premios.taxa-base}")
    private BigDecimal taxaBase; // 0.05 (5% do valor do veículo)
    
    public BigDecimal calcular(
            BigDecimal valorVeiculo,
            Set<TipoCobertura> coberturas,
            TipoFranquia franquia,
            BigDecimal percentualDesconto) {
        
        // Base: 5% do valor do veículo
        BigDecimal premioBase = valorVeiculo.multiply(taxaBase);
        
        // Fator de coberturas
        double fatorCoberturas = coberturas.stream()
            .mapToDouble(TipoCobertura::getFatorPremio)
            .sum();
        
        // Aplicar fator de coberturas
        BigDecimal premioComCoberturas = premioBase
            .multiply(BigDecimal.valueOf(fatorCoberturas));
        
        // Aplicar fator de franquia
        BigDecimal premioComFranquia = premioComCoberturas
            .multiply(BigDecimal.valueOf(franquia.getFatorPremio()));
        
        // Aplicar desconto
        if (percentualDesconto != null && percentualDesconto.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal desconto = premioComFranquia
                .multiply(percentualDesconto)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            premioComFranquia = premioComFranquia.subtract(desconto);
        }
        
        return premioComFranquia.setScale(2, RoundingMode.HALF_UP);
    }
}
```

**CalculadoraDevolucao**:
```java
@Service
public class CalculadoraDevolucao {
    
    public BigDecimal calcular(
            BigDecimal premioTotal,
            LocalDate dataInicio,
            LocalDate dataCancelamento,
            LocalDate dataFim) {
        
        // Direito de arrependimento (7 dias)
        long diasDecorridos = ChronoUnit.DAYS.between(dataInicio, dataCancelamento);
        if (diasDecorridos <= 7) {
            return premioTotal; // Devolução integral
        }
        
        // Cálculo pro-rata
        long diasTotais = ChronoUnit.DAYS.between(dataInicio, dataFim);
        long diasRestantes = ChronoUnit.DAYS.between(dataCancelamento, dataFim);
        
        if (diasRestantes <= 0) {
            return BigDecimal.ZERO; // Apólice já vencida
        }
        
        // Proporcional aos dias restantes
        BigDecimal proporcao = BigDecimal.valueOf(diasRestantes)
            .divide(BigDecimal.valueOf(diasTotais), 4, RoundingMode.HALF_UP);
        
        return premioTotal
            .multiply(proporcao)
            .setScale(2, RoundingMode.HALF_UP);
    }
}
```

## 3. Camada de Aplicação

### 3.1 Commands e Command Handlers

#### Segurado

**CriarSeguradoCommand**:
```java
public class CriarSeguradoCommand implements Command {
    @NotBlank
    private String cpf;
    
    @NotBlank
    @Size(min = 3, max = 200)
    private String nome;
    
    @NotNull
    @Past
    private LocalDate dataNascimento;
    
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    @Pattern(regexp = "\\d{2}")
    private String telefoneDdd;
    
    @NotBlank
    @Pattern(regexp = "\\d{8,9}")
    private String telefoneNumero;
    
    @NotBlank
    @Pattern(regexp = "\\d{8}")
    private String enderecoCep;
    
    @NotBlank
    private String enderecoLogradouro;
    
    @NotBlank
    private String enderecoNumero;
    
    private String enderecoComplemento;
    
    @NotBlank
    private String enderecoBairro;
    
    @NotBlank
    private String enderecoCidade;
    
    @NotBlank
    @Pattern(regexp = "[A-Z]{2}")
    private String enderecoUf;
    
    // getters, setters, builder
}
```

**CriarSeguradoCommandHandler**:
```java
@Component
public class CriarSeguradoCommandHandler implements CommandHandler<CriarSeguradoCommand> {
    
    @Autowired
    private EventSourcingAggregateRepository<SeguradoAggregate> repository;
    
    @Autowired
    private SeguradoQueryRepository queryRepository;
    
    @Autowired
    private CommandBus commandBus;
    
    @Override
    @Transactional("writeTransactionManager")
    public CommandResult handle(CriarSeguradoCommand command) {
        
        // Validação de unicidade de CPF
        String cpfLimpo = CpfFormatter.limpar(command.getCpf());
        if (queryRepository.existsByCpf(cpfLimpo)) {
            throw new BusinessRuleException("CPF já cadastrado no sistema");
        }
        
        // Validação de unicidade de Email
        if (queryRepository.existsByEmail(command.getEmail())) {
            throw new BusinessRuleException("Email já cadastrado no sistema");
        }
        
        // Construir Value Objects
        Email email = new Email(command.getEmail());
        Telefone telefone = new Telefone(
            command.getTelefoneDdd(),
            command.getTelefoneNumero()
        );
        Endereco endereco = new Endereco(
            command.getEnderecoCep(),
            command.getEnderecoLogradouro(),
            command.getEnderecoNumero(),
            command.getEnderecoComplemento(),
            command.getEnderecoBairro(),
            command.getEnderecoCidade(),
            command.getEnderecoUf()
        );
        
        // Criar Aggregate
        SeguradoAggregate segurado = SeguradoAggregate.criar(
            cpfLimpo,
            command.getNome(),
            command.getDataNascimento(),
            email,
            telefone,
            endereco
        );
        
        // Persistir
        repository.save(segurado);
        
        return CommandResult.success(
            segurado.getSeguradoId(),
            "Segurado criado com sucesso"
        );
    }
    
    @Override
    public Class<CriarSeguradoCommand> getCommandType() {
        return CriarSeguradoCommand.class;
    }
}
```

#### Apólice

**CriarApoliceCommand**:
```java
public class CriarApoliceCommand implements Command {
    @NotNull
    private UUID seguradoId;
    
    @NotNull
    private UUID veiculoId;
    
    @NotEmpty
    private Set<TipoCobertura> coberturas;
    
    @NotNull
    private TipoFranquia franquia;
    
    @NotNull
    @DecimalMin("1000.00")
    private BigDecimal valorVeiculo;
    
    @NotNull
    @FutureOrPresent
    private LocalDate dataInicioVigencia;
    
    @Min(1)
    @Max(12)
    private int mesesVigencia;
    
    @DecimalMin("0")
    @DecimalMax("30")
    private BigDecimal percentualDesconto;
    
    @Min(1)
    @Max(12)
    private Integer numeroParcelas;
    
    // getters, setters, builder
}
```

**CriarApoliceCommandHandler**:
```java
@Component
public class CriarApoliceCommandHandler implements CommandHandler<CriarApoliceCommand> {
    
    @Autowired
    private EventSourcingAggregateRepository<ApoliceAggregate> repository;
    
    @Autowired
    private SeguradoQueryRepository seguradoQueryRepository;
    
    @Autowired
    private CalculadoraPremio calculadoraPremio;
    
    @Override
    @Transactional("writeTransactionManager")
    public CommandResult handle(CriarApoliceCommand command) {
        
        // Validar segurado
        SeguradoQueryModel segurado = seguradoQueryRepository
            .findById(command.getSeguradoId())
            .orElseThrow(() -> new BusinessRuleException("Segurado não encontrado"));
        
        if (segurado.getStatus() != SeguradoStatus.ATIVO) {
            throw new BusinessRuleException("Segurado não está ativo");
        }
        
        // Validar veículo (assumindo que existe)
        // TODO: Implementar após Épico 3
        
        // Criar Aggregate
        ApoliceAggregate apolice = ApoliceAggregate.criar(
            command.getSeguradoId(),
            command.getVeiculoId(),
            command.getCoberturas(),
            command.getFranquia(),
            command.getValorVeiculo(),
            command.getDataInicioVigencia(),
            command.getMesesVigencia(),
            command.getPercentualDesconto(),
            calculadoraPremio
        );
        
        // Persistir
        repository.save(apolice);
        
        // Gerar PDF da apólice (assíncrono)
        // TODO: Publicar evento para gerar PDF
        
        return CommandResult.success(
            apolice.getApoliceId(),
            "Apólice criada com sucesso",
            Map.of(
                "numeroApolice", apolice.getNumeroApolice(),
                "premioTotal", apolice.getPremioTotal()
            )
        );
    }
    
    @Override
    public Class<CriarApoliceCommand> getCommandType() {
        return CriarApoliceCommand.class;
    }
}
```

### 3.2 Domain Events

#### Segurado Events

**SeguradoCriadoEvent**:
```java
public class SeguradoCriadoEvent implements DomainEvent {
    private UUID seguradoId;
    private String cpf;
    private String nome;
    private LocalDate dataNascimento;
    private Email email;
    private Telefone telefone;
    private Endereco endereco;
    private LocalDate dataCadastro;
    private LocalDateTime timestamp;
    
    // constructors, getters
}
```

#### Apólice Events

**ApoliceCriadaEvent**:
```java
public class ApoliceCriadaEvent implements DomainEvent {
    private UUID apoliceId;
    private String numeroApolice;
    private UUID seguradoId;
    private UUID veiculoId;
    private Set<TipoCobertura> coberturas;
    private TipoFranquia franquia;
    private BigDecimal valorVeiculo;
    private LocalDate dataInicioVigencia;
    private LocalDate dataFimVigencia;
    private BigDecimal premioTotal;
    private BigDecimal percentualDesconto;
    private LocalDate dataCriacao;
    private LocalDateTime timestamp;
    
    // constructors, getters
}
```

### 3.3 Projection Handlers

#### SeguradoProjectionHandler

```java
@Component
public class SeguradoProjectionHandler extends AbstractProjectionHandler {
    
    @Autowired
    private SeguradoQueryRepository repository;
    
    @EventHandler
    public void on(SeguradoCriadoEvent event) {
        SeguradoQueryModel queryModel = new SeguradoQueryModel();
        queryModel.setSeguradoId(event.getSeguradoId());
        queryModel.setCpf(event.getCpf());
        queryModel.setNome(event.getNome());
        queryModel.setDataNascimento(event.getDataNascimento());
        queryModel.setEmail(event.getEmail().getEndereco());
        queryModel.setTelefone(event.getTelefone().getTelefoneCompleto());
        queryModel.setEndereco(formatarEndereco(event.getEndereco()));
        queryModel.setStatus(SeguradoStatus.ATIVO);
        queryModel.setDataCadastro(event.getDataCadastro());
        
        repository.save(queryModel);
    }
    
    @EventHandler
    public void on(SeguradoAtualizadoEvent event) {
        SeguradoQueryModel queryModel = repository
            .findById(event.getSeguradoId())
            .orElseThrow();
        
        queryModel.setNome(event.getNome());
        queryModel.setEmail(event.getEmail().getEndereco());
        queryModel.setTelefone(event.getTelefone().getTelefoneCompleto());
        queryModel.setEndereco(formatarEndereco(event.getEndereco()));
        queryModel.setDataUltimaAtualizacao(event.getDataAtualizacao());
        
        repository.save(queryModel);
    }
    
    @EventHandler
    public void on(SeguradoDesativadoEvent event) {
        SeguradoQueryModel queryModel = repository
            .findById(event.getSeguradoId())
            .orElseThrow();
        
        queryModel.setStatus(SeguradoStatus.INATIVO);
        queryModel.setMotivoDesativacao(event.getMotivo());
        queryModel.setDataUltimaAtualizacao(event.getDataDesativacao());
        
        repository.save(queryModel);
    }
    
    @EventHandler
    public void on(SeguradoReativadoEvent event) {
        SeguradoQueryModel queryModel = repository
            .findById(event.getSeguradoId())
            .orElseThrow();
        
        queryModel.setStatus(SeguradoStatus.ATIVO);
        queryModel.setMotivoDesativacao(null);
        queryModel.setDataUltimaAtualizacao(event.getDataReativacao());
        
        repository.save(queryModel);
    }
    
    @Override
    public String getProjectionName() {
        return "SeguradoProjection";
    }
    
    private String formatarEndereco(Endereco endereco) {
        return String.format("%s, %s - %s, %s/%s, CEP: %s",
            endereco.getLogradouro(),
            endereco.getNumero(),
            endereco.getBairro(),
            endereco.getCidade(),
            endereco.getUf(),
            endereco.getCep());
    }
}
```

#### ApoliceProjectionHandler

```java
@Component
public class ApoliceProjectionHandler extends AbstractProjectionHandler {
    
    @Autowired
    private ApoliceQueryRepository repository;
    
    @Autowired
    private SeguradoQueryRepository seguradoRepository;
    
    @EventHandler
    public void on(ApoliceCriadaEvent event) {
        // Buscar dados do segurado para desnormalizar
        SeguradoQueryModel segurado = seguradoRepository
            .findById(event.getSeguradoId())
            .orElseThrow();
        
        ApoliceQueryModel queryModel = new ApoliceQueryModel();
        queryModel.setApoliceId(event.getApoliceId());
        queryModel.setNumeroApolice(event.getNumeroApolice());
        queryModel.setSeguradoId(event.getSeguradoId());
        queryModel.setSeguradoNome(segurado.getNome());
        queryModel.setSeguradoCpf(segurado.getCpf());
        queryModel.setVeiculoId(event.getVeiculoId());
        queryModel.setCoberturas(event.getCoberturas());
        queryModel.setFranquia(event.getFranquia());
        queryModel.setValorVeiculo(event.getValorVeiculo());
        queryModel.setDataInicioVigencia(event.getDataInicioVigencia());
        queryModel.setDataFimVigencia(event.getDataFimVigencia());
        queryModel.setPremioTotal(event.getPremioTotal());
        queryModel.setPercentualDesconto(event.getPercentualDesconto());
        queryModel.setStatus(ApoliceStatus.VIGENTE);
        queryModel.setDataCriacao(event.getDataCriacao());
        
        repository.save(queryModel);
    }
    
    @EventHandler
    public void on(ApoliceAtualizadaEvent event) {
        ApoliceQueryModel queryModel = repository
            .findById(event.getApoliceId())
            .orElseThrow();
        
        queryModel.setCoberturas(event.getCoberturas());
        queryModel.setFranquia(event.getFranquia());
        queryModel.setPremioTotal(event.getPremioTotal());
        queryModel.setDataUltimaAtualizacao(event.getDataAtualizacao());
        
        repository.save(queryModel);
    }
    
    @EventHandler
    public void on(ApoliceCanceladaEvent event) {
        ApoliceQueryModel queryModel = repository
            .findById(event.getApoliceId())
            .orElseThrow();
        
        queryModel.setStatus(ApoliceStatus.CANCELADA);
        queryModel.setDataCancelamento(event.getDataCancelamento());
        queryModel.setMotivoCancelamento(event.getMotivo());
        queryModel.setValorDevolucao(event.getValorDevolucao());
        
        repository.save(queryModel);
    }
    
    @Override
    public String getProjectionName() {
        return "ApoliceProjection";
    }
}
```

## 4. Camada de Query (Read Models)

### 4.1 Query Models (Entities JPA)

**SeguradoQueryModel**:
```java
@Entity
@Table(name = "segurado_view", schema = "projections")
public class SeguradoQueryModel {
    
    @Id
    private UUID seguradoId;
    
    @Column(unique = true, nullable = false)
    private String cpf;
    
    @Column(nullable = false)
    private String nome;
    
    @Column(nullable = false)
    private LocalDate dataNascimento;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String telefone;
    
    @Column(columnDefinition = "TEXT")
    private String endereco;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeguradoStatus status;
    
    private String motivoDesativacao;
    
    @Column(nullable = false)
    private LocalDate dataCadastro;
    
    private LocalDate dataUltimaAtualizacao;
    
    // getters, setters
}
```

**ApoliceQueryModel**:
```java
@Entity
@Table(name = "apolice_view", schema = "projections")
public class ApoliceQueryModel {
    
    @Id
    private UUID apoliceId;
    
    @Column(unique = true, nullable = false)
    private String numeroApolice;
    
    @Column(nullable = false)
    private UUID seguradoId;
    
    private String seguradoNome;
    private String seguradoCpf;
    
    @Column(nullable = false)
    private UUID veiculoId;
    
    @ElementCollection
    @CollectionTable(name = "apolice_coberturas", schema = "projections")
    @Enumerated(EnumType.STRING)
    private Set<TipoCobertura> coberturas;
    
    @Enumerated(EnumType.STRING)
    private TipoFranquia franquia;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal valorVeiculo;
    
    @Column(nullable = false)
    private LocalDate dataInicioVigencia;
    
    @Column(nullable = false)
    private LocalDate dataFimVigencia;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal premioTotal;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal percentualDesconto;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApoliceStatus status;
    
    private LocalDate dataCancelamento;
    private String motivoCancelamento;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal valorDevolucao;
    
    @Column(nullable = false)
    private LocalDate dataCriacao;
    
    private LocalDate dataUltimaAtualizacao;
    
    // getters, setters
}
```

### 4.2 Query Repositories

**SeguradoQueryRepository**:
```java
@Repository
public interface SeguradoQueryRepository extends JpaRepository<SeguradoQueryModel, UUID> {
    
    Optional<SeguradoQueryModel> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
    
    @Query("SELECT s FROM SeguradoQueryModel s WHERE " +
           "LOWER(s.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "s.cpf LIKE CONCAT('%', :termo, '%')")
    Page<SeguradoQueryModel> buscarPorTermo(
        @Param("termo") String termo,
        Pageable pageable
    );
    
    Page<SeguradoQueryModel> findByStatus(
        SeguradoStatus status,
        Pageable pageable
    );
    
    @Query("SELECT s FROM SeguradoQueryModel s WHERE " +
           "s.dataCadastro BETWEEN :inicio AND :fim")
    Page<SeguradoQueryModel> findByPeriodo(
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim,
        Pageable pageable
    );
}
```

**ApoliceQueryRepository**:
```java
@Repository
public interface ApoliceQueryRepository extends JpaRepository<ApoliceQueryModel, UUID> {
    
    Optional<ApoliceQueryModel> findByNumeroApolice(String numeroApolice);
    
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.seguradoCpf = :cpf")
    Page<ApoliceQueryModel> findBySeguradoCpf(
        @Param("cpf") String cpf,
        Pageable pageable
    );
    
    Page<ApoliceQueryModel> findBySeguradoId(
        UUID seguradoId,
        Pageable pageable
    );
    
    Page<ApoliceQueryModel> findByStatus(
        ApoliceStatus status,
        Pageable pageable
    );
    
    @Query("SELECT a FROM ApoliceQueryModel a WHERE " +
           "a.dataFimVigencia BETWEEN :inicio AND :fim AND " +
           "a.status = 'VIGENTE'")
    List<ApoliceQueryModel> findVencendoEntre(
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim
    );
    
    @Query("SELECT COUNT(a) FROM ApoliceQueryModel a WHERE " +
           "a.status = :status AND " +
           "a.dataCriacao BETWEEN :inicio AND :fim")
    long countByStatusAndPeriodo(
        @Param("status") ApoliceStatus status,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim
    );
    
    @Query("SELECT AVG(a.premioTotal) FROM ApoliceQueryModel a WHERE " +
           "a.status = 'VIGENTE'")
    BigDecimal calcularPremioMedio();
}
```

### 4.3 Query Services

**SeguradoQueryService**:
```java
@Service
public class SeguradoQueryService {
    
    @Autowired
    private SeguradoQueryRepository repository;
    
    @Autowired
    @Qualifier("readCacheManager")
    private CacheManager cacheManager;
    
    @Cacheable(value = "segurados", key = "#cpf")
    public Optional<SeguradoQueryModel> buscarPorCpf(String cpf) {
        return repository.findByCpf(cpf);
    }
    
    public Page<SeguradoQueryModel> buscar(
            String termo,
            SeguradoStatus status,
            LocalDate dataInicio,
            LocalDate dataFim,
            Pageable pageable) {
        
        if (termo != null && !termo.isBlank()) {
            return repository.buscarPorTermo(termo, pageable);
        }
        
        if (status != null) {
            return repository.findByStatus(status, pageable);
        }
        
        if (dataInicio != null && dataFim != null) {
            return repository.findByPeriodo(dataInicio, dataFim, pageable);
        }
        
        return repository.findAll(pageable);
    }
    
    public Map<String, Object> obterEstatisticas() {
        long totalAtivos = repository.countByStatus(SeguradoStatus.ATIVO);
        long totalInativos = repository.countByStatus(SeguradoStatus.INATIVO);
        
        return Map.of(
            "totalAtivos", totalAtivos,
            "totalInativos", totalInativos,
            "total", totalAtivos + totalInativos
        );
    }
}
```

**ApoliceQueryService**:
```java
@Service
public class ApoliceQueryService {
    
    @Autowired
    private ApoliceQueryRepository repository;
    
    @Cacheable(value = "apolices", key = "#numeroApolice")
    public Optional<ApoliceQueryModel> buscarPorNumero(String numeroApolice) {
        return repository.findByNumeroApolice(numeroApolice);
    }
    
    @Cacheable(value = "apolices-segurado", key = "#cpf + '-' + #pageable.pageNumber")
    public Page<ApoliceQueryModel> buscarPorSegurado(String cpf, Pageable pageable) {
        return repository.findBySeguradoCpf(cpf, pageable);
    }
    
    public Page<ApoliceQueryModel> buscar(
            UUID seguradoId,
            ApoliceStatus status,
            LocalDate vigenciaInicio,
            LocalDate vigenciaFim,
            Pageable pageable) {
        
        if (seguradoId != null) {
            return repository.findBySeguradoId(seguradoId, pageable);
        }
        
        if (status != null) {
            return repository.findByStatus(status, pageable);
        }
        
        // Implementar filtros adicionais conforme necessário
        
        return repository.findAll(pageable);
    }
    
    public List<ApoliceQueryModel> buscarVencendoProximos30Dias() {
        LocalDate hoje = LocalDate.now();
        LocalDate daqui30Dias = hoje.plusDays(30);
        return repository.findVencendoEntre(hoje, daqui30Dias);
    }
    
    public Map<String, Object> obterDashboard() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        
        long totalVigentes = repository.countByStatus(ApoliceStatus.VIGENTE);
        long emitidasNoMes = repository.countByStatusAndPeriodo(
            ApoliceStatus.VIGENTE, inicioMes, hoje
        );
        long vencendoEm30Dias = buscarVencendoProximos30Dias().size();
        BigDecimal premioMedio = repository.calcularPremioMedio();
        
        return Map.of(
            "totalVigentes", totalVigentes,
            "emitidasNoMes", emitidasNoMes,
            "vencendoEm30Dias", vencendoEm30Dias,
            "premioMedio", premioMedio
        );
    }
}
```

## 5. Camada de Apresentação (REST APIs)

### 5.1 Controllers

**SeguradoController**:
```java
@RestController
@RequestMapping("/api/v1/segurados")
@Validated
public class SeguradoController {
    
    @Autowired
    private CommandBus commandBus;
    
    @Autowired
    private SeguradoQueryService queryService;
    
    @PostMapping
    public ResponseEntity<CommandResult> criar(
            @Valid @RequestBody CriarSeguradoCommand command) {
        
        CommandResult result = commandBus.send(command);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SeguradoQueryModel> buscarPorId(
            @PathVariable UUID id) {
        
        return queryService.buscarPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<SeguradoQueryModel> buscarPorCpf(
            @PathVariable String cpf) {
        
        return queryService.buscarPorCpf(cpf)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public Page<SeguradoQueryModel> buscar(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) SeguradoStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dataFim,
            Pageable pageable) {
        
        return queryService.buscar(termo, status, dataInicio, dataFim, pageable);
    }
    
    @GetMapping("/estatisticas")
    public Map<String, Object> obterEstatisticas() {
        return queryService.obterEstatisticas();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CommandResult> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarSeguradoCommand command) {
        
        command.setSeguradoId(id);
        CommandResult result = commandBus.send(command);
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<CommandResult> desativar(
            @PathVariable UUID id,
            @RequestParam String motivo) {
        
        DesativarSeguradoCommand command = DesativarSeguradoCommand.builder()
            .seguradoId(id)
            .motivo(motivo)
            .build();
        
        CommandResult result = commandBus.send(command);
        return ResponseEntity.ok(result);
    }
}
```

**ApoliceController**:
```java
@RestController
@RequestMapping("/api/v1/apolices")
@Validated
public class ApoliceController {
    
    @Autowired
    private CommandBus commandBus;
    
    @Autowired
    private ApoliceQueryService queryService;
    
    @PostMapping
    public ResponseEntity<CommandResult> criar(
            @Valid @RequestBody CriarApoliceCommand command) {
        
        CommandResult result = commandBus.send(command);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApoliceQueryModel> buscarPorId(
            @PathVariable UUID id) {
        
        return queryService.buscarPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/numero/{numero}")
    public ResponseEntity<ApoliceQueryModel> buscarPorNumero(
            @PathVariable String numero) {
        
        return queryService.buscarPorNumero(numero)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public Page<ApoliceQueryModel> buscar(
            @RequestParam(required = false) UUID seguradoId,
            @RequestParam(required = false) ApoliceStatus status,
            Pageable pageable) {
        
        return queryService.buscar(seguradoId, status, null, null, pageable);
    }
    
    @GetMapping("/dashboard")
    public Map<String, Object> obterDashboard() {
        return queryService.obterDashboard();
    }
    
    @GetMapping("/vencendo")
    public List<ApoliceQueryModel> buscarVencendo() {
        return queryService.buscarVencendoProximos30Dias();
    }
    
    @PutMapping("/{id}/cobertura")
    public ResponseEntity<CommandResult> atualizarCobertura(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarCoberturaCommand command) {
        
        command.setApoliceId(id);
        CommandResult result = commandBus.send(command);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<CommandResult> cancelar(
            @PathVariable UUID id,
            @RequestParam String motivo) {
        
        CancelarApoliceCommand command = CancelarApoliceCommand.builder()
            .apoliceId(id)
            .motivo(motivo)
            .build();
        
        CommandResult result = commandBus.send(command);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/{id}/renovar")
    public ResponseEntity<CommandResult> renovar(
            @PathVariable UUID id,
            @Valid @RequestBody RenovarApoliceCommand command) {
        
        command.setApoliceOriginalId(id);
        CommandResult result = commandBus.send(command);
        return ResponseEntity.ok(result);
    }
}
```

## 6. Configurações

### 6.1 application.yml

```yaml
spring:
  application:
    name: app-arquitetura-hibrida-epico2
  
  # DataSources configurados no Épico 1
  
  # Cache
  cache:
    type: redis
    redis:
      time-to-live: 300000 # 5 minutos
      cache-null-values: false
  
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5

# Configurações de Domínio
apolice:
  premios:
    taxa-base: 0.05 # 5% do valor do veículo
  descontos:
    maximo: 0.30 # 30%
    renovacao: 0.05 # 5%
  vigencia:
    minima-dias: 30
    maxima-dias: 365

segurado:
  idade:
    minima: 18
    maxima: 80

# Notificações
notificacao:
  vencimento:
    dias-antes:
      - 30
      - 15
      - 7
      - 0
  canais:
    ordem:
      - WHATSAPP
      - SMS
      - EMAIL

# Observabilidade
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus,cqrs
  metrics:
    tags:
      application: ${spring.application.name}
      epico: epico-2
```

## 7. Estratégia de Testes

### 7.1 Testes Unitários

**SeguradoAggregateTest**:
```java
@Test
void deveCriarSeguradoComSucesso() {
    // Arrange
    String cpf = "12345678901";
    String nome = "João da Silva";
    LocalDate dataNascimento = LocalDate.of(1990, 1, 1);
    Email email = new Email("joao@example.com");
    Telefone telefone = new Telefone("11", "987654321");
    Endereco endereco = criarEnderecoValido();
    
    // Act
    SeguradoAggregate segurado = SeguradoAggregate.criar(
        cpf, nome, dataNascimento, email, telefone, endereco
    );
    
    // Assert
    assertNotNull(segurado.getSeguradoId());
    assertEquals(cpf, segurado.getCpf());
    assertEquals(nome, segurado.getNome());
    assertEquals(SeguradoStatus.ATIVO, segurado.getStatus());
    assertEquals(1, segurado.getUncommittedEvents().size());
    assertTrue(segurado.getUncommittedEvents().get(0) instanceof SeguradoCriadoEvent);
}

@Test
void deveLancarExcecaoAoCriarSeguradoComCpfInvalido() {
    // Arrange
    String cpfInvalido = "123";
    
    // Act & Assert
    assertThrows(BusinessRuleException.class, () -> {
        SeguradoAggregate.criar(
            cpfInvalido, "Nome", LocalDate.now().minusYears(20),
            new Email("email@test.com"),
            new Telefone("11", "987654321"),
            criarEnderecoValido()
        );
    });
}
```

### 7.2 Testes de Integração

**CriarSeguradoCommandHandlerIntegrationTest**:
```java
@SpringBootTest
@Transactional
class CriarSeguradoCommandHandlerIntegrationTest {
    
    @Autowired
    private CommandBus commandBus;
    
    @Autowired
    private SeguradoQueryRepository queryRepository;
    
    @Test
    void deveProcessarComandoECriarProjecao() {
        // Arrange
        CriarSeguradoCommand command = CriarSeguradoCommand.builder()
            .cpf("12345678901")
            .nome("João da Silva")
            .dataNascimento(LocalDate.of(1990, 1, 1))
            .email("joao@example.com")
            .telefoneDdd("11")
            .telefoneNumero("987654321")
            .enderecoCep("01310100")
            .enderecoLogradouro("Avenida Paulista")
            .enderecoNumero("1000")
            .enderecoBairro("Bela Vista")
            .enderecoCidade("São Paulo")
            .enderecoUf("SP")
            .build();
        
        // Act
        CommandResult result = commandBus.send(command);
        
        // Assert
        assertTrue(result.isSuccess());
        UUID seguradoId = (UUID) result.getAggregateId();
        
        // Aguardar projeção assíncrona
        await().atMost(Duration.ofSeconds(5))
            .until(() -> queryRepository.findById(seguradoId).isPresent());
        
        Optional<SeguradoQueryModel> queryModel = queryRepository.findById(seguradoId);
        assertTrue(queryModel.isPresent());
        assertEquals("João da Silva", queryModel.get().getNome());
        assertEquals(SeguradoStatus.ATIVO, queryModel.get().getStatus());
    }
}
```

### 7.3 Testes de Performance

**SeguradoQueryPerformanceTest**:
```java
@SpringBootTest
class SeguradoQueryPerformanceTest {
    
    @Autowired
    private SeguradoQueryService queryService;
    
    @Test
    void consultaPorCpfDeveSerMenorQue50ms() {
        // Arrange
        String cpf = "12345678901";
        
        // Warm-up
        for (int i = 0; i < 100; i++) {
            queryService.buscarPorCpf(cpf);
        }
        
        // Act & Assert
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        for (int i = 0; i < 1000; i++) {
            queryService.buscarPorCpf(cpf);
        }
        
        stopWatch.stop();
        double avgTime = stopWatch.getTotalTimeMillis() / 1000.0;
        
        assertTrue(avgTime < 50, 
            String.format("Tempo médio %.2fms excedeu limite de 50ms", avgTime));
    }
}
```

## 8. Observabilidade e Monitoramento

### 8.1 Métricas Customizadas

**Epico2Metrics**:
```java
@Component
public class Epico2Metrics {
    
    private final Counter seguradosCriados;
    private final Counter apolicesEmitidas;
    private final Gauge apolicesVigentes;
    private final Timer tempoEmissaoApolice;
    
    public Epico2Metrics(MeterRegistry registry) {
        this.seguradosCriados = Counter.builder("segurados.criados")
            .description("Total de segurados criados")
            .register(registry);
        
        this.apolicesEmitidas = Counter.builder("apolices.emitidas")
            .description("Total de apólices emitidas")
            .register(registry);
        
        this.apolicesVigentes = Gauge.builder("apolices.vigentes", this::contarApolicesVigentes)
            .description("Total de apólices vigentes")
            .register(registry);
        
        this.tempoEmissaoApolice = Timer.builder("apolice.emissao.tempo")
            .description("Tempo de emissão de apólice")
            .register(registry);
    }
    
    public void incrementarSeguradosCriados() {
        seguradosCriados.increment();
    }
    
    public void incrementarApolicesEmitidas() {
        apolicesEmitidas.increment();
    }
    
    public void registrarTempoEmissao(long millis) {
        tempoEmissaoApolice.record(millis, TimeUnit.MILLISECONDS);
    }
    
    private double contarApolicesVigentes() {
        // Implementar consulta ao repository
        return 0;
    }
}
```

### 8.2 Health Indicators

**Epico2HealthIndicator**:
```java
@Component
public class Epico2HealthIndicator implements HealthIndicator {
    
    @Autowired
    private SeguradoQueryRepository seguradoRepository;
    
    @Autowired
    private ApoliceQueryRepository apoliceRepository;
    
    @Override
    public Health health() {
        try {
            long seguradosAtivos = seguradoRepository.countByStatus(SeguradoStatus.ATIVO);
            long apolicesVigentes = apoliceRepository.countByStatus(ApoliceStatus.VIGENTE);
            
            return Health.up()
                .withDetail("seguradosAtivos", seguradosAtivos)
                .withDetail("apolicesVigentes", apolicesVigentes)
                .withDetail("epico", "epico-2")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withException(e)
                .build();
        }
    }
}
```

## 9. Segurança

### 9.1 Autenticação e Autorização

**SecurityConfiguration** (extends Épico 1):
```java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers("/api/v1/actuator/**").permitAll()
                
                // Segurados
                .requestMatchers(POST, "/api/v1/segurados").hasRole("OPERADOR")
                .requestMatchers(GET, "/api/v1/segurados/**").hasAnyRole("OPERADOR", "CONSULTA")
                .requestMatchers(PUT, "/api/v1/segurados/**").hasRole("OPERADOR")
                .requestMatchers(DELETE, "/api/v1/segurados/**").hasRole("SUPERVISOR")
                
                // Apólices
                .requestMatchers(POST, "/api/v1/apolices").hasRole("OPERADOR")
                .requestMatchers(GET, "/api/v1/apolices/**").hasAnyRole("OPERADOR", "CONSULTA")
                .requestMatchers(PUT, "/api/v1/apolices/**").hasRole("OPERADOR")
                .requestMatchers(POST, "/api/v1/apolices/*/cancelar").hasRole("SUPERVISOR")
                
                // Dashboards
                .requestMatchers("/api/v1/**/dashboard").hasAnyRole("GESTOR", "SUPERVISOR")
                
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        
        return http.build();
    }
}
```

### 9.2 Auditoria de Eventos Sensíveis

**AuditoriaEventHandler**:
```java
@Component
public class AuditoriaEventHandler {
    
    @Autowired
    private AuditoriaRepository auditoriaRepository;
    
    @Autowired
    private SecurityContext securityContext;
    
    @EventHandler
    public void on(SeguradoDesativadoEvent event) {
        registrarAuditoria(
            "SEGURADO_DESATIVADO",
            event.getSeguradoId(),
            Map.of("motivo", event.getMotivo())
        );
    }
    
    @EventHandler
    public void on(ApoliceCanceladaEvent event) {
        registrarAuditoria(
            "APOLICE_CANCELADA",
            event.getApoliceId(),
            Map.of(
                "motivo", event.getMotivo(),
                "valorDevolucao", event.getValorDevolucao()
            )
        );
    }
    
    private void registrarAuditoria(String tipoOperacao, UUID entityId, Map<String, Object> detalhes) {
        AuditoriaEntry entry = new AuditoriaEntry();
        entry.setTipoOperacao(tipoOperacao);
        entry.setEntityId(entityId);
        entry.setUsuario(securityContext.getUsuarioAtual());
        entry.setTimestamp(LocalDateTime.now());
        entry.setDetalhes(detalhes);
        
        auditoriaRepository.save(entry);
    }
}
```

## 10. Migrações de Banco de Dados

### 10.1 Flyway - Projeções

**V6__Create_Segurado_View.sql**:
```sql
-- Tabela de projeção de segurados
CREATE TABLE projections.segurado_view (
    segurado_id UUID PRIMARY KEY,
    cpf VARCHAR(11) UNIQUE NOT NULL,
    nome VARCHAR(200) NOT NULL,
    data_nascimento DATE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    endereco TEXT,
    status VARCHAR(20) NOT NULL,
    motivo_desativacao TEXT,
    data_cadastro DATE NOT NULL,
    data_ultima_atualizacao DATE
);

-- Índices para performance
CREATE INDEX idx_segurado_cpf ON projections.segurado_view(cpf);
CREATE INDEX idx_segurado_nome ON projections.segurado_view(nome);
CREATE INDEX idx_segurado_email ON projections.segurado_view(email);
CREATE INDEX idx_segurado_status ON projections.segurado_view(status);
CREATE INDEX idx_segurado_data_cadastro ON projections.segurado_view(data_cadastro);

-- Comentários
COMMENT ON TABLE projections.segurado_view IS 'Projeção desnormalizada de segurados para consultas';
COMMENT ON COLUMN projections.segurado_view.cpf IS 'CPF sem formatação (11 dígitos)';
```

**V7__Create_Apolice_View.sql**:
```sql
-- Tabela de projeção de apólices
CREATE TABLE projections.apolice_view (
    apolice_id UUID PRIMARY KEY,
    numero_apolice VARCHAR(20) UNIQUE NOT NULL,
    segurado_id UUID NOT NULL,
    segurado_nome VARCHAR(200),
    segurado_cpf VARCHAR(11),
    veiculo_id UUID NOT NULL,
    franquia VARCHAR(20),
    valor_veiculo DECIMAL(19,2),
    data_inicio_vigencia DATE NOT NULL,
    data_fim_vigencia DATE NOT NULL,
    premio_total DECIMAL(19,2),
    percentual_desconto DECIMAL(5,2),
    status VARCHAR(20) NOT NULL,
    data_cancelamento DATE,
    motivo_cancelamento TEXT,
    valor_devolucao DECIMAL(19,2),
    data_criacao DATE NOT NULL,
    data_ultima_atualizacao DATE
);

-- Tabela de coberturas (relacionamento many-to-many)
CREATE TABLE projections.apolice_coberturas (
    apolice_id UUID NOT NULL,
    cobertura VARCHAR(50) NOT NULL,
    PRIMARY KEY (apolice_id, cobertura),
    FOREIGN KEY (apolice_id) REFERENCES projections.apolice_view(apolice_id) ON DELETE CASCADE
);

-- Índices para performance
CREATE INDEX idx_apolice_numero ON projections.apolice_view(numero_apolice);
CREATE INDEX idx_apolice_segurado ON projections.apolice_view(segurado_id);
CREATE INDEX idx_apolice_segurado_cpf ON projections.apolice_view(segurado_cpf);
CREATE INDEX idx_apolice_status ON projections.apolice_view(status);
CREATE INDEX idx_apolice_vigencia ON projections.apolice_view(data_inicio_vigencia, data_fim_vigencia);
CREATE INDEX idx_apolice_data_criacao ON projections.apolice_view(data_criacao);

-- Comentários
COMMENT ON TABLE projections.apolice_view IS 'Projeção desnormalizada de apólices com dados do segurado';
COMMENT ON COLUMN projections.apolice_view.numero_apolice IS 'Formato: APO-AAAA-NNNNNN';
```

## 11. Dependências Maven (pom.xml)

```xml
<dependencies>
    <!-- Event Sourcing & CQRS (herdado do Épico 1) -->
    
    <!-- Validação -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Cache -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>io.lettuce</groupId>
        <artifactId>lettuce-core</artifactId>
    </dependency>
    
    <!-- Utilitários -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
    </dependency>
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>32.1.3-jre</version>
    </dependency>
    
    <!-- Testes -->
    <dependency>
        <groupId>org.awaitility</groupId>
        <artifactId>awaitility</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

**Versão:** 1.0  
**Data:** 09/03/2026  
**Autor:** Sistema de Especificações Turing Loop  
**Próximo Passo**: Revisar Plan Specification para roadmap detalhado de implementação