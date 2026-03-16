package com.seguradora.hibrida.domain.apolice.aggregate;

import com.seguradora.hibrida.aggregate.AggregateRoot;
import com.seguradora.hibrida.aggregate.EventSourcingHandler;
import com.seguradora.hibrida.aggregate.validation.BusinessRule;
import com.seguradora.hibrida.domain.apolice.event.*;
import com.seguradora.hibrida.domain.apolice.model.*;
import com.seguradora.hibrida.domain.apolice.service.CalculadoraPremioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregate que representa uma apólice de seguro.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class ApoliceAggregate extends AggregateRoot {
    
    private static final Logger log = LoggerFactory.getLogger(ApoliceAggregate.class);
    
    // Estado do aggregate
    private NumeroApolice numeroApolice;
    private String seguradoId;
    private String produto;
    private StatusApolice status;
    private Vigencia vigencia;
    private Valor valorSegurado;
    private FormaPagamento formaPagamento;
    private List<Cobertura> coberturas;
    private Premio premio;
    private String operadorResponsavel;
    
    // Histórico e auditoria
    private Map<String, Object> metadados;
    private List<String> historicoAlteracoes;
    
    /**
     * Construtor padrão para reconstrução via Event Sourcing.
     */
    public ApoliceAggregate() {
        super();
        this.coberturas = new ArrayList<>();
        this.metadados = new HashMap<>();
        this.historicoAlteracoes = new ArrayList<>();
        registerBusinessRules();
    }
    
    /**
     * Construtor para criação de nova apólice.
     */
    public ApoliceAggregate(
            String apoliceId,
            NumeroApolice numeroApolice,
            String seguradoId,
            String produto,
            Vigencia vigencia,
            Valor valorSegurado,
            FormaPagamento formaPagamento,
            List<Cobertura> coberturas,
            String operadorId) {
        
        super(apoliceId);
        this.coberturas = new ArrayList<>();
        this.metadados = new HashMap<>();
        this.historicoAlteracoes = new ArrayList<>();
        
        // Validações de criação
        validarCriacaoApolice(numeroApolice, seguradoId, produto, vigencia, valorSegurado, formaPagamento, coberturas, operadorId);
        
        // Calcular prêmio
        CalculadoraPremioService calculadora = new CalculadoraPremioService();
        Premio premioCalculado = calculadora.calcularPremio(valorSegurado, coberturas, formaPagamento, vigencia.getInicio());
        
        // Aplicar evento de criação
        applyEvent(ApoliceCriadaEvent.create(
                apoliceId,
                numeroApolice,
                seguradoId,
                produto,
                vigencia,
                valorSegurado,
                formaPagamento,
                coberturas,
                premioCalculado,
                operadorId
        ));
        
        registerBusinessRules();
    }
    
    /**
     * Atualiza dados da apólice.
     */
    public void atualizarDados(
            Valor novoValorSegurado,
            List<Cobertura> novasCoberturas,
            String operadorId,
            String motivo) {
        
        validarAtualizacao(novoValorSegurado, novasCoberturas, operadorId, motivo);
        
        // Preparar dados das alterações
        Map<String, Object> alteracoes = new HashMap<>();
        Map<String, Object> valoresAnteriores = new HashMap<>();
        Map<String, Object> novosValores = new HashMap<>();
        
        // Verificar alteração no valor segurado
        if (!valorSegurado.ehIgualA(novoValorSegurado)) {
            alteracoes.put("valorSegurado", true);
            valoresAnteriores.put("valorSegurado", valorSegurado.getQuantia().toString());
            novosValores.put("valorSegurado", novoValorSegurado.getQuantia().toString());
        }
        
        // Verificar alterações nas coberturas
        if (!coberturasSaoIguais(coberturas, novasCoberturas)) {
            alteracoes.put("coberturas", true);
            valoresAnteriores.put("coberturas", serializarCoberturas(coberturas));
            novosValores.put("coberturas", serializarCoberturas(novasCoberturas));
        }
        
        if (!alteracoes.isEmpty()) {
            // Recalcular prêmio se necessário
            CalculadoraPremioService calculadora = new CalculadoraPremioService();
            Premio novoPremio = calculadora.calcularPremio(novoValorSegurado, novasCoberturas, formaPagamento, vigencia.getInicio());
            
            if (!premio.getValorTotal().ehIgualA(novoPremio.getValorTotal())) {
                alteracoes.put("premio", true);
                valoresAnteriores.put("premio", premio.getValorTotal().getQuantia().toString());
                novosValores.put("premio", novoPremio.getValorTotal().getQuantia().toString());
            }
            
            applyEvent(ApoliceAtualizadaEvent.create(
                    getId(),
                    getVersion() + 1,
                    numeroApolice.getNumero(),
                    seguradoId,
                    alteracoes,
                    valoresAnteriores,
                    novosValores,
                    operadorId,
                    motivo
            ));
        }
    }
    
    /**
     * Cancela a apólice.
     */
    public void cancelar(
            String motivo,
            LocalDate dataEfeito,
            String operadorId,
            String observacoes,
            ApoliceCanceladaEvent.TipoCancelamento tipoCancelamento) {
        
        validarCancelamento(motivo, dataEfeito, operadorId, tipoCancelamento);
        
        // Calcular valor de reembolso
        Valor valorReembolso = calcularReembolso(dataEfeito);
        
        applyEvent(ApoliceCanceladaEvent.create(
                getId(),
                getVersion() + 1,
                numeroApolice.getNumero(),
                seguradoId,
                valorSegurado.getQuantia().toString(),
                motivo,
                dataEfeito,
                valorReembolso.getQuantia().toString(),
                operadorId,
                observacoes,
                tipoCancelamento
        ));
    }
    
    /**
     * Renova a apólice.
     */
    public void renovar(
            Vigencia novaVigencia,
            Valor novoValorSegurado,
            List<Cobertura> novasCoberturas,
            FormaPagamento novaFormaPagamento,
            String operadorId,
            ApoliceRenovadaEvent.TipoRenovacao tipoRenovacao,
            String observacoes) {
        
        validarRenovacao(novaVigencia, novoValorSegurado, novasCoberturas, novaFormaPagamento, operadorId, tipoRenovacao);
        
        // Calcular novo prêmio
        CalculadoraPremioService calculadora = new CalculadoraPremioService();
        Premio novoPremio = calculadora.calcularPremio(novoValorSegurado, novasCoberturas, novaFormaPagamento, novaVigencia.getInicio());
        
        // Preparar alterações de coberturas
        List<Map<String, Object>> alteracoesCoberturas = new ArrayList<>();
        if (!coberturasSaoIguais(coberturas, novasCoberturas)) {
            alteracoesCoberturas = compararCoberturas(coberturas, novasCoberturas);
        }
        
        applyEvent(ApoliceRenovadaEvent.create(
                getId(),
                getVersion() + 1,
                numeroApolice.getNumero(),
                seguradoId,
                novaVigencia.getInicio().toString(),
                novaVigencia.getFim().toString(),
                novoValorSegurado.getQuantia().toString(),
                novoPremio.getValorTotal().getQuantia().toString(),
                alteracoesCoberturas,
                novaFormaPagamento.name(),
                operadorId,
                tipoRenovacao,
                observacoes
        ));
    }
    
    /**
     * Adiciona uma nova cobertura.
     */
    public void adicionarCobertura(
            Cobertura novaCobertura,
            LocalDate dataEfeito,
            String operadorId,
            String motivo) {
        
        validarAdicaoCobertura(novaCobertura, dataEfeito, operadorId, motivo);
        
        // Calcular valor adicional do prêmio
        Valor valorAdicional = novaCobertura.calcularPremio(valorSegurado);
        
        applyEvent(CoberturaAdicionadaEvent.create(
                getId(),
                getVersion() + 1,
                numeroApolice.getNumero(),
                seguradoId,
                novaCobertura.getTipo().name(),
                novaCobertura.getValorCobertura().getQuantia().toString(),
                novaCobertura.getFranquia().getQuantia().toString(),
                novaCobertura.getCarenciaDias(),
                valorAdicional.getQuantia().toString(),
                dataEfeito.toString(),
                operadorId,
                motivo
        ));
    }
    
    // Event Sourcing Handlers
    
    @EventSourcingHandler
    protected void on(ApoliceCriadaEvent event) {
        this.numeroApolice = NumeroApolice.of(event.getNumeroApolice());
        this.seguradoId = event.getSeguradoId();
        this.produto = event.getProduto();
        this.status = StatusApolice.ATIVA;
        this.vigencia = Vigencia.of(
                LocalDate.parse(event.getVigenciaInicio()),
                LocalDate.parse(event.getVigenciaFim())
        );
        // Corrigir ambiguidade usando BigDecimal explicitamente
        this.valorSegurado = Valor.reais(new BigDecimal(event.getValorSegurado()));
        this.formaPagamento = FormaPagamento.valueOf(event.getFormaPagamento());
        this.coberturas = deserializarCoberturas(event.getCoberturas());
        this.premio = Premio.of(valorSegurado, formaPagamento, vigencia.getInicio());
        this.operadorResponsavel = event.getOperadorId();
        
        historicoAlteracoes.add("Apólice criada em " + event.getTimestamp());
        
        log.info("Apólice criada: {} para segurado: {}", numeroApolice.getNumero(), seguradoId);
    }
    
    @EventSourcingHandler
    protected void on(ApoliceAtualizadaEvent event) {
        // Aplicar alterações
        if (event.foiAlterado("valorSegurado")) {
            String valorStr = event.getNovoValor("valorSegurado");
            this.valorSegurado = Valor.reais(new BigDecimal(valorStr));
        }
        
        if (event.foiAlterado("coberturas")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> novasCoberturas = event.getNovoValor("coberturas");
            this.coberturas = deserializarCoberturas(novasCoberturas);
        }
        
        if (event.foiAlterado("premio")) {
            // Recalcular prêmio
            CalculadoraPremioService calculadora = new CalculadoraPremioService();
            this.premio = calculadora.calcularPremio(valorSegurado, coberturas, formaPagamento, vigencia.getInicio());
        }
        
        this.operadorResponsavel = event.getOperadorId();
        
        historicoAlteracoes.add(String.format("Apólice atualizada em %s: %s (Operador: %s)", 
                event.getTimestamp(), event.getDescricaoAlteracoes(), event.getOperadorId()));
        
        log.info("Apólice atualizada: {} - {}", numeroApolice.getNumero(), event.getDescricaoAlteracoes());
    }
    
    @EventSourcingHandler
    protected void on(ApoliceCanceladaEvent event) {
        this.status = StatusApolice.CANCELADA;
        this.operadorResponsavel = event.getOperadorId();
        
        historicoAlteracoes.add(String.format("Apólice cancelada em %s: %s (Operador: %s)", 
                event.getTimestamp(), event.getMotivo(), event.getOperadorId()));
        
        log.info("Apólice cancelada: {} - Motivo: {}", numeroApolice.getNumero(), event.getMotivo());
    }
    
    @EventSourcingHandler
    protected void on(ApoliceRenovadaEvent event) {
        this.vigencia = Vigencia.of(
                LocalDate.parse(event.getNovaVigenciaInicio()),
                LocalDate.parse(event.getNovaVigenciaFim())
        );
        this.valorSegurado = Valor.reais(new BigDecimal(event.getNovoValorSegurado()));
        this.formaPagamento = FormaPagamento.valueOf(event.getNovaFormaPagamento());
        
        // Recalcular prêmio
        CalculadoraPremioService calculadora = new CalculadoraPremioService();
        this.premio = calculadora.calcularPremio(valorSegurado, coberturas, formaPagamento, vigencia.getInicio());
        
        this.operadorResponsavel = event.getOperadorId();
        
        historicoAlteracoes.add(String.format("Apólice renovada em %s: %s (Operador: %s)", 
                event.getTimestamp(), event.getTipoRenovacao(), event.getOperadorId()));
        
        log.info("Apólice renovada: {} - Tipo: {}", numeroApolice.getNumero(), event.getTipoRenovacao());
    }
    
    @EventSourcingHandler
    protected void on(CoberturaAdicionadaEvent event) {
        // Adicionar nova cobertura
        TipoCobertura tipo = TipoCobertura.valueOf(event.getTipoCobertura());
        Valor valorCobertura = Valor.reais(new BigDecimal(event.getValorCobertura()));
        Valor franquia = Valor.reais(new BigDecimal(event.getFranquia()));
        
        Cobertura novaCobertura = Cobertura.of(tipo, valorCobertura, franquia, event.getCarenciaDias());
        this.coberturas.add(novaCobertura);
        
        // Recalcular prêmio
        CalculadoraPremioService calculadora = new CalculadoraPremioService();
        this.premio = calculadora.calcularPremio(valorSegurado, coberturas, formaPagamento, vigencia.getInicio());
        
        this.operadorResponsavel = event.getOperadorId();
        
        historicoAlteracoes.add(String.format("Cobertura adicionada em %s: %s (Operador: %s)", 
                event.getTimestamp(), event.getTipoCobertura(), event.getOperadorId()));
        
        log.info("Cobertura adicionada à apólice: {} - Tipo: {}", numeroApolice.getNumero(), event.getTipoCobertura());
    }
    
    // Métodos de validação
    
    private void validarCriacaoApolice(
            NumeroApolice numeroApolice,
            String seguradoId,
            String produto,
            Vigencia vigencia,
            Valor valorSegurado,
            FormaPagamento formaPagamento,
            List<Cobertura> coberturas,
            String operadorId) {
        
        if (numeroApolice == null) {
            throw new IllegalArgumentException("Número da apólice não pode ser nulo");
        }
        
        if (seguradoId == null || seguradoId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do segurado não pode ser nulo ou vazio");
        }
        
        if (produto == null || produto.trim().isEmpty()) {
            throw new IllegalArgumentException("Produto não pode ser nulo ou vazio");
        }
        
        if (vigencia == null) {
            throw new IllegalArgumentException("Vigência não pode ser nula");
        }
        
        if (valorSegurado == null || !valorSegurado.isPositivo()) {
            throw new IllegalArgumentException("Valor segurado deve ser positivo");
        }
        
        if (formaPagamento == null) {
            throw new IllegalArgumentException("Forma de pagamento não pode ser nula");
        }
        
        if (coberturas == null || coberturas.isEmpty()) {
            throw new IllegalArgumentException("Deve haver pelo menos uma cobertura");
        }
        
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        
        // Validar se não há coberturas duplicadas
        Set<TipoCobertura> tiposCoberturas = coberturas.stream()
                .map(Cobertura::getTipo)
                .collect(Collectors.toSet());
        
        if (tiposCoberturas.size() != coberturas.size()) {
            throw new IllegalArgumentException("Não pode haver coberturas duplicadas");
        }
    }
    
    private void validarAtualizacao(Valor novoValorSegurado, List<Cobertura> novasCoberturas, String operadorId, String motivo) {
        if (status != StatusApolice.ATIVA) {
            throw new IllegalStateException("Apenas apólices ativas podem ser atualizadas");
        }
        
        if (novoValorSegurado == null || !novoValorSegurado.isPositivo()) {
            throw new IllegalArgumentException("Novo valor segurado deve ser positivo");
        }
        
        if (novasCoberturas == null || novasCoberturas.isEmpty()) {
            throw new IllegalArgumentException("Deve haver pelo menos uma cobertura");
        }
        
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo da alteração não pode ser nulo ou vazio");
        }
    }
    
    private void validarCancelamento(String motivo, LocalDate dataEfeito, String operadorId, ApoliceCanceladaEvent.TipoCancelamento tipoCancelamento) {
        if (!status.podeSerCancelada()) {
            throw new IllegalStateException("Apólice não pode ser cancelada no status atual: " + status);
        }
        
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo do cancelamento não pode ser nulo ou vazio");
        }
        
        if (dataEfeito == null) {
            throw new IllegalArgumentException("Data de efeito não pode ser nula");
        }
        
        if (dataEfeito.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Data de efeito não pode ser anterior à data atual");
        }
        
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        
        if (tipoCancelamento == null) {
            throw new IllegalArgumentException("Tipo de cancelamento não pode ser nulo");
        }
    }
    
    private void validarRenovacao(Vigencia novaVigencia, Valor novoValorSegurado, List<Cobertura> novasCoberturas, FormaPagamento novaFormaPagamento, String operadorId, ApoliceRenovadaEvent.TipoRenovacao tipoRenovacao) {
        if (!status.podeSerRenovada()) {
            throw new IllegalStateException("Apólice não pode ser renovada no status atual: " + status);
        }
        
        if (novaVigencia == null) {
            throw new IllegalArgumentException("Nova vigência não pode ser nula");
        }
        
        if (novaVigencia.getInicio().isBefore(vigencia.getFim().plusDays(1))) {
            throw new IllegalArgumentException("Nova vigência deve começar após o fim da vigência atual");
        }
        
        if (novoValorSegurado == null || !novoValorSegurado.isPositivo()) {
            throw new IllegalArgumentException("Novo valor segurado deve ser positivo");
        }
        
        if (novasCoberturas == null || novasCoberturas.isEmpty()) {
            throw new IllegalArgumentException("Deve haver pelo menos uma cobertura");
        }
        
        if (novaFormaPagamento == null) {
            throw new IllegalArgumentException("Nova forma de pagamento não pode ser nula");
        }
        
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        
        if (tipoRenovacao == null) {
            throw new IllegalArgumentException("Tipo de renovação não pode ser nulo");
        }
    }
    
    private void validarAdicaoCobertura(Cobertura novaCobertura, LocalDate dataEfeito, String operadorId, String motivo) {
        if (status != StatusApolice.ATIVA) {
            throw new IllegalStateException("Apenas apólices ativas podem ter coberturas adicionadas");
        }
        
        if (novaCobertura == null) {
            throw new IllegalArgumentException("Nova cobertura não pode ser nula");
        }
        
        // Verificar se já existe cobertura do mesmo tipo
        boolean jaExiste = coberturas.stream()
                .anyMatch(c -> c.getTipo() == novaCobertura.getTipo());
        
        if (jaExiste) {
            throw new IllegalArgumentException("Já existe cobertura do tipo: " + novaCobertura.getTipo());
        }
        
        if (dataEfeito == null) {
            throw new IllegalArgumentException("Data de efeito não pode ser nula");
        }
        
        if (dataEfeito.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Data de efeito não pode ser anterior à data atual");
        }
        
        if (!vigencia.estaVigenteEm(dataEfeito)) {
            throw new IllegalArgumentException("Data de efeito deve estar dentro da vigência da apólice");
        }
        
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser nulo ou vazio");
        }
        
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo da adição não pode ser nulo ou vazio");
        }
    }
    
    // Métodos auxiliares
    
    private Valor calcularReembolso(LocalDate dataEfeito) {
        if (premio == null || !vigencia.estaVigenteEm(dataEfeito)) {
            return Valor.zero();
        }
        
        // Calcular proporcionalmente baseado nos dias restantes
        long diasRestantes = vigencia.getDiasRestantes();
        long diasTotais = vigencia.getDiasVigencia();
        
        if (diasRestantes <= 0) {
            return Valor.zero();
        }
        
        double proporcao = (double) diasRestantes / diasTotais;
        return premio.getValorTotal().multiplicar(proporcao);
    }
    
    private boolean coberturasSaoIguais(List<Cobertura> coberturas1, List<Cobertura> coberturas2) {
        if (coberturas1.size() != coberturas2.size()) {
            return false;
        }
        
        return coberturas1.containsAll(coberturas2) && coberturas2.containsAll(coberturas1);
    }
    
    private List<Map<String, Object>> serializarCoberturas(List<Cobertura> coberturas) {
        return coberturas.stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("tipo", c.getTipo().name());
                    map.put("valorCobertura", c.getValorCobertura().getQuantia().toString());
                    map.put("franquia", c.getFranquia().getQuantia().toString());
                    map.put("carenciaDias", c.getCarenciaDias());
                    map.put("ativa", c.isAtiva());
                    return map;
                })
                .collect(Collectors.toList());
    }
    
    private List<Cobertura> deserializarCoberturas(List<Map<String, Object>> coberturasData) {
        return coberturasData.stream()
                .map(data -> {
                    TipoCobertura tipo = TipoCobertura.valueOf((String) data.get("tipo"));
                    Valor valorCobertura = Valor.reais(new BigDecimal((String) data.get("valorCobertura")));
                    Valor franquia = Valor.reais(new BigDecimal((String) data.get("franquia")));
                    int carenciaDias = (Integer) data.get("carenciaDias");
                    
                    Cobertura cobertura = Cobertura.of(tipo, valorCobertura, franquia, carenciaDias);
                    
                    if (!(Boolean) data.get("ativa")) {
                        cobertura = cobertura.desativar();
                    }
                    
                    return cobertura;
                })
                .collect(Collectors.toList());
    }
    
    private List<Map<String, Object>> compararCoberturas(List<Cobertura> coberturasAnteriores, List<Cobertura> novasCoberturas) {
        List<Map<String, Object>> alteracoes = new ArrayList<>();
        
        // Implementar comparação detalhada das coberturas
        // Por simplicidade, retornando lista vazia
        return alteracoes;
    }
    
    private void registerBusinessRules() {
        // Registrar regras de negócio específicas da apólice
        registerBusinessRule(new BusinessRule() {
            @Override
            public boolean isValid(AggregateRoot aggregate) {
                ApoliceAggregate apolice = (ApoliceAggregate) aggregate;
                return apolice.vigencia == null || apolice.vigencia.getDiasVigencia() >= 30;
            }
            
            @Override
            public String getErrorMessage() {
                return "Vigência mínima de 30 dias";
            }
        });
        
        registerBusinessRule(new BusinessRule() {
            @Override
            public boolean isValid(AggregateRoot aggregate) {
                ApoliceAggregate apolice = (ApoliceAggregate) aggregate;
                return apolice.coberturas == null || !apolice.coberturas.isEmpty();
            }
            
            @Override
            public String getErrorMessage() {
                return "Deve ter pelo menos uma cobertura";
            }
        });
    }
    
    // Getters
    public NumeroApolice getNumeroApolice() { return numeroApolice; }
    public String getSeguradoId() { return seguradoId; }
    public String getProduto() { return produto; }
    public StatusApolice getStatus() { return status; }
    public Vigencia getVigencia() { return vigencia; }
    public Valor getValorSegurado() { return valorSegurado; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public List<Cobertura> getCoberturas() { return Collections.unmodifiableList(coberturas); }
    public Premio getPremio() { return premio; }
    public String getOperadorResponsavel() { return operadorResponsavel; }
    public List<String> getHistoricoAlteracoes() { return Collections.unmodifiableList(historicoAlteracoes); }
    
    // Métodos de consulta
    public boolean isAtiva() { return status == StatusApolice.ATIVA; }
    public boolean isVigente() { return vigencia != null && vigencia.estaVigenteHoje(); }
    public boolean temCobertura(TipoCobertura tipo) { 
        return coberturas.stream().anyMatch(c -> c.getTipo() == tipo && c.isAtiva()); 
    }
    
    @Override
    public Object createSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("numeroApolice", numeroApolice != null ? numeroApolice.getNumero() : null);
        snapshot.put("seguradoId", seguradoId);
        snapshot.put("produto", produto);
        snapshot.put("status", status != null ? status.name() : null);
        snapshot.put("vigencia", vigencia);
        snapshot.put("valorSegurado", valorSegurado);
        snapshot.put("formaPagamento", formaPagamento != null ? formaPagamento.name() : null);
        snapshot.put("coberturas", serializarCoberturas(coberturas));
        snapshot.put("premio", premio);
        snapshot.put("operadorResponsavel", operadorResponsavel);
        snapshot.put("metadados", new HashMap<>(metadados));
        snapshot.put("historicoAlteracoes", new ArrayList<>(historicoAlteracoes));
        snapshot.put("version", getVersion());
        return snapshot;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void restoreFromSnapshot(Object snapshotData) {
        Map<String, Object> snapshot = (Map<String, Object>) snapshotData;
        
        String numeroStr = (String) snapshot.get("numeroApolice");
        this.numeroApolice = numeroStr != null ? NumeroApolice.of(numeroStr) : null;
        
        this.seguradoId = (String) snapshot.get("seguradoId");
        this.produto = (String) snapshot.get("produto");
        
        String statusStr = (String) snapshot.get("status");
        this.status = statusStr != null ? StatusApolice.valueOf(statusStr) : null;
        
        this.vigencia = (Vigencia) snapshot.get("vigencia");
        this.valorSegurado = (Valor) snapshot.get("valorSegurado");
        
        String formaPagamentoStr = (String) snapshot.get("formaPagamento");
        this.formaPagamento = formaPagamentoStr != null ? FormaPagamento.valueOf(formaPagamentoStr) : null;
        
        List<Map<String, Object>> coberturasData = (List<Map<String, Object>>) snapshot.get("coberturas");
        this.coberturas = coberturasData != null ? deserializarCoberturas(coberturasData) : new ArrayList<>();
        
        this.premio = (Premio) snapshot.get("premio");
        this.operadorResponsavel = (String) snapshot.get("operadorResponsavel");
        this.metadados = (Map<String, Object>) snapshot.get("metadados");
        this.historicoAlteracoes = (List<String>) snapshot.get("historicoAlteracoes");
    }
    
    @Override
    public void clearState() {
        this.numeroApolice = null;
        this.seguradoId = null;
        this.produto = null;
        this.status = null;
        this.vigencia = null;
        this.valorSegurado = null;
        this.formaPagamento = null;
        this.coberturas.clear();
        this.premio = null;
        this.operadorResponsavel = null;
        this.metadados.clear();
        this.historicoAlteracoes.clear();
    }
    
    @Override
    public String getAggregateType() {
        return "ApoliceAggregate";
    }
}