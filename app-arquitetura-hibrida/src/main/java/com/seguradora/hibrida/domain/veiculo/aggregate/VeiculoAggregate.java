package com.seguradora.hibrida.domain.veiculo.aggregate;

import com.seguradora.hibrida.aggregate.AggregateRoot;
import com.seguradora.hibrida.aggregate.EventSourcingHandler;
import com.seguradora.hibrida.aggregate.exception.BusinessRuleViolationException;
import com.seguradora.hibrida.domain.veiculo.event.*;
import com.seguradora.hibrida.domain.veiculo.model.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Aggregate Root para o domínio de Veículo.
 * 
 * <p>Gerencia o ciclo de vida completo de um veículo, incluindo:
 * <ul>
 *   <li>Criação com validações específicas do domínio automotivo</li>
 *   <li>Atualização de especificações técnicas</li>
 *   <li>Associação/desassociação com apólices de seguro</li>
 *   <li>Transferência de propriedade</li>
 *   <li>Controle de status e histórico</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class VeiculoAggregate extends AggregateRoot {
    
    // Estado do veículo
    private Placa placa;
    private Renavam renavam;
    private Chassi chassi;
    private String marca;
    private String modelo;
    private AnoModelo anoModelo;
    private Especificacao especificacao;
    private Proprietario proprietario;
    private StatusVeiculo status;
    
    // Relacionamentos com apólices
    private final Set<String> apolicesAssociadas = new HashSet<>();
    
    // Metadados
    private String operadorCriacao;
    private String ultimoOperador;
    
    /**
     * Construtor padrão para reconstrução do histórico.
     */
    public VeiculoAggregate() {
        super();
        registerBusinessRules();
    }
    
    /**
     * Construtor para criação de novo veículo.
     */
    public VeiculoAggregate(String id) {
        super(id);
        registerBusinessRules();
    }
    
    /**
     * Cria um novo veículo no sistema.
     */
    public static VeiculoAggregate criarVeiculo(String id, String placa, String renavam, String chassi,
                                               String marca, String modelo, int anoFabricacao, int anoModelo,
                                               Especificacao especificacao, Proprietario proprietario,
                                               String operadorId) {
        VeiculoAggregate veiculo = new VeiculoAggregate(id);
        
        // Validações de criação
        veiculo.validarCriacaoVeiculo(
            Placa.of(placa), Renavam.of(renavam), Chassi.of(chassi),
            marca, modelo, AnoModelo.of(anoFabricacao, anoModelo),
            especificacao, proprietario
        );
        
        // Aplicar evento de criação
        VeiculoCriadoEvent evento = VeiculoCriadoEvent.create(
            id, 1L, placa, renavam, chassi, marca, modelo,
            anoFabricacao, anoModelo, especificacao, proprietario, operadorId
        );
        
        veiculo.applyEvent(evento);
        return veiculo;
    }
    
    /**
     * Atualiza as especificações do veículo.
     */
    public void atualizarEspecificacoes(Especificacao novaEspecificacao, String operadorId) {
        if (novaEspecificacao == null) {
            throw new BusinessRuleViolationException("Nova especificação é obrigatória", 
                Arrays.asList("Especificação não pode ser nula"));
        }
        
        validarAtualizacao(novaEspecificacao);
        
        VeiculoAtualizadoEvent evento = VeiculoAtualizadoEvent.create(
            getId(), getVersion() + 1, this.especificacao, 
            novaEspecificacao, operadorId, "Atualização de especificações"
        );
        
        applyEvent(evento);
    }
    
    /**
     * Associa o veículo a uma apólice.
     */
    public void associarApolice(String apoliceId, LocalDate dataInicio, String operadorId) {
        if (apoliceId == null || apoliceId.trim().isEmpty()) {
            throw new BusinessRuleViolationException("ID da apólice é obrigatório",
                Arrays.asList("ID da apólice não pode ser nulo ou vazio"));
        }
        
        validarAssociacaoApolice(apoliceId);
        
        VeiculoAssociadoEvent evento = VeiculoAssociadoEvent.create(
            getId(), getVersion() + 1, apoliceId, dataInicio, operadorId
        );
        
        applyEvent(evento);
    }
    
    /**
     * Desassocia o veículo de uma apólice.
     */
    public void desassociarApolice(String apoliceId, LocalDate dataFim, String motivo, String operadorId) {
        if (apoliceId == null || apoliceId.trim().isEmpty()) {
            throw new BusinessRuleViolationException("ID da apólice é obrigatório",
                Arrays.asList("ID da apólice não pode ser nulo ou vazio"));
        }
        
        validarDesassociacaoApolice(apoliceId);
        
        VeiculoDesassociadoEvent evento = VeiculoDesassociadoEvent.create(
            getId(), getVersion() + 1, apoliceId, dataFim, motivo, operadorId
        );
        
        applyEvent(evento);
    }
    
    /**
     * Transfere a propriedade do veículo.
     */
    public void transferirPropriedade(Proprietario novoProprietario, LocalDate dataTransferencia, String operadorId) {
        if (novoProprietario == null) {
            throw new BusinessRuleViolationException("Novo proprietário é obrigatório",
                Arrays.asList("Proprietário não pode ser nulo"));
        }
        
        validarTransferenciaPropriedade(novoProprietario);
        
        PropriedadeTransferidaEvent evento = PropriedadeTransferidaEvent.create(
            getId(), getVersion() + 1, this.proprietario, novoProprietario,
            dataTransferencia, operadorId, "Transferência de propriedade"
        );
        
        applyEvent(evento);
    }
    
    // Event Handlers
    
    @EventSourcingHandler
    protected void on(VeiculoCriadoEvent event) {
        this.placa = Placa.of(event.getPlaca());
        this.renavam = Renavam.of(event.getRenavam());
        this.chassi = Chassi.of(event.getChassi());
        this.marca = event.getMarca();
        this.modelo = event.getModelo();
        this.anoModelo = AnoModelo.of(event.getAnoFabricacao(), event.getAnoModelo());
        this.especificacao = event.getEspecificacao();
        this.proprietario = event.getProprietario();
        this.status = event.getStatus();
        this.operadorCriacao = event.getOperadorId();
        this.ultimoOperador = event.getOperadorId();
    }
    
    @EventSourcingHandler
    protected void on(VeiculoAtualizadoEvent event) {
        this.especificacao = event.getNovaEspecificacao();
        this.ultimoOperador = event.getOperadorId();
    }
    
    @EventSourcingHandler
    protected void on(VeiculoAssociadoEvent event) {
        this.apolicesAssociadas.add(event.getApoliceId());
        this.ultimoOperador = event.getOperadorId();
    }
    
    @EventSourcingHandler
    protected void on(VeiculoDesassociadoEvent event) {
        this.apolicesAssociadas.remove(event.getApoliceId());
        this.ultimoOperador = event.getOperadorId();
    }
    
    @EventSourcingHandler
    protected void on(PropriedadeTransferidaEvent event) {
        this.proprietario = event.getNovoProprietario();
        this.ultimoOperador = event.getOperadorId();
    }
    
    // Validações
    
    private void validarCriacaoVeiculo(Placa placa, Renavam renavam, Chassi chassi,
                                      String marca, String modelo, AnoModelo anoModelo,
                                      Especificacao especificacao, Proprietario proprietario) {
        List<String> erros = new ArrayList<>();
        
        if (placa == null) erros.add("Placa é obrigatória");
        if (renavam == null) erros.add("RENAVAM é obrigatório");
        if (chassi == null) erros.add("Chassi é obrigatório");
        if (marca == null || marca.trim().isEmpty()) erros.add("Marca é obrigatória");
        if (modelo == null || modelo.trim().isEmpty()) erros.add("Modelo é obrigatório");
        if (anoModelo == null) erros.add("Ano/modelo é obrigatório");
        if (especificacao == null) erros.add("Especificação é obrigatória");
        if (proprietario == null) erros.add("Proprietário é obrigatório");
        
        if (!erros.isEmpty()) {
            throw new BusinessRuleViolationException("Dados inválidos para criação do veículo", erros);
        }
    }
    
    private void validarAtualizacao(Especificacao novaEspecificacao) {
        if (this.status != StatusVeiculo.ATIVO) {
            throw new BusinessRuleViolationException("Veículo deve estar ativo para atualização",
                Arrays.asList("Status atual: " + this.status));
        }
        
        if (!novaEspecificacao.isCompativel(this.especificacao.getCategoria())) {
            throw new BusinessRuleViolationException("Nova especificação incompatível com categoria",
                Arrays.asList("Categoria: " + this.especificacao.getCategoria()));
        }
    }
    
    private void validarAssociacaoApolice(String apoliceId) {
        if (this.apolicesAssociadas.contains(apoliceId)) {
            throw new BusinessRuleViolationException("Veículo já associado à apólice",
                Arrays.asList("Apólice: " + apoliceId));
        }
        
        if (this.status != StatusVeiculo.ATIVO) {
            throw new BusinessRuleViolationException("Veículo deve estar ativo para associação",
                Arrays.asList("Status atual: " + this.status));
        }
    }
    
    private void validarDesassociacaoApolice(String apoliceId) {
        if (!this.apolicesAssociadas.contains(apoliceId)) {
            throw new BusinessRuleViolationException("Veículo não está associado à apólice",
                Arrays.asList("Apólice: " + apoliceId));
        }
    }
    
    private void validarTransferenciaPropriedade(Proprietario novoProprietario) {
        if (this.proprietario.equals(novoProprietario)) {
            throw new BusinessRuleViolationException("Novo proprietário deve ser diferente do atual",
                Arrays.asList("Proprietário atual: " + this.proprietario.getNome()));
        }
        
        if (this.status != StatusVeiculo.ATIVO) {
            throw new BusinessRuleViolationException("Veículo deve estar ativo para transferência",
                Arrays.asList("Status atual: " + this.status));
        }
    }
    
    private void registerBusinessRules() {
        // Registrar regras de negócio específicas do veículo
        // Implementação futura conforme necessário
    }
    
    // Métodos auxiliares
    
    public boolean isAssociadoA(String apoliceId) {
        return apolicesAssociadas.contains(apoliceId);
    }
    
    public boolean temApolicesAtivas() {
        return !apolicesAssociadas.isEmpty();
    }
    
    public int getQuantidadeApolicesAtivas() {
        return apolicesAssociadas.size();
    }
    
    // Implementação dos métodos abstratos
    
    @Override
    protected void clearState() {
        this.placa = null;
        this.renavam = null;
        this.chassi = null;
        this.marca = null;
        this.modelo = null;
        this.anoModelo = null;
        this.especificacao = null;
        this.proprietario = null;
        this.status = null;
        this.apolicesAssociadas.clear();
        this.operadorCriacao = null;
        this.ultimoOperador = null;
    }
    
    @Override
    protected void restoreFromSnapshot(Object snapshotData) {
        if (snapshotData instanceof VeiculoSnapshot snapshot) {
            this.placa = snapshot.getPlaca();
            this.renavam = snapshot.getRenavam();
            this.chassi = snapshot.getChassi();
            this.marca = snapshot.getMarca();
            this.modelo = snapshot.getModelo();
            this.anoModelo = snapshot.getAnoModelo();
            this.especificacao = snapshot.getEspecificacao();
            this.proprietario = snapshot.getProprietario();
            this.status = snapshot.getStatus();
            this.apolicesAssociadas.clear();
            this.apolicesAssociadas.addAll(snapshot.getApolicesAssociadas());
            this.operadorCriacao = snapshot.getOperadorCriacao();
            this.ultimoOperador = snapshot.getUltimoOperador();
        }
    }
    
    @Override
    public Object createSnapshot() {
        return new VeiculoSnapshot(
            getId(), getVersion(), placa, renavam, chassi, marca, modelo,
            anoModelo, especificacao, proprietario, status,
            new HashSet<>(apolicesAssociadas), operadorCriacao, ultimoOperador
        );
    }
    
    // Getters
    
    public Placa getPlaca() { return placa; }
    public Renavam getRenavam() { return renavam; }
    public Chassi getChassi() { return chassi; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public AnoModelo getAnoModelo() { return anoModelo; }
    public Especificacao getEspecificacao() { return especificacao; }
    public Proprietario getProprietario() { return proprietario; }
    public StatusVeiculo getStatus() { return status; }
    public Set<String> getApolicesAssociadas() { return Collections.unmodifiableSet(apolicesAssociadas); }
    public String getOperadorCriacao() { return operadorCriacao; }
    public String getUltimoOperador() { return ultimoOperador; }
    
    @Override
    public String toString() {
        return String.format("VeiculoAggregate{id='%s', placa='%s', marca='%s', modelo='%s', status=%s}",
            getId(), placa != null ? placa.getFormatada() : null, marca, modelo, status);
    }
    
    /**
     * Classe interna para snapshot do veículo.
     */
    public static class VeiculoSnapshot {
        private final String id;
        private final long version;
        private final Placa placa;
        private final Renavam renavam;
        private final Chassi chassi;
        private final String marca;
        private final String modelo;
        private final AnoModelo anoModelo;
        private final Especificacao especificacao;
        private final Proprietario proprietario;
        private final StatusVeiculo status;
        private final Set<String> apolicesAssociadas;
        private final String operadorCriacao;
        private final String ultimoOperador;
        
        public VeiculoSnapshot(String id, long version, Placa placa, Renavam renavam, Chassi chassi,
                              String marca, String modelo, AnoModelo anoModelo, Especificacao especificacao,
                              Proprietario proprietario, StatusVeiculo status, Set<String> apolicesAssociadas,
                              String operadorCriacao, String ultimoOperador) {
            this.id = id;
            this.version = version;
            this.placa = placa;
            this.renavam = renavam;
            this.chassi = chassi;
            this.marca = marca;
            this.modelo = modelo;
            this.anoModelo = anoModelo;
            this.especificacao = especificacao;
            this.proprietario = proprietario;
            this.status = status;
            this.apolicesAssociadas = apolicesAssociadas;
            this.operadorCriacao = operadorCriacao;
            this.ultimoOperador = ultimoOperador;
        }
        
        // Getters
        public String getId() { return id; }
        public long getVersion() { return version; }
        public Placa getPlaca() { return placa; }
        public Renavam getRenavam() { return renavam; }
        public Chassi getChassi() { return chassi; }
        public String getMarca() { return marca; }
        public String getModelo() { return modelo; }
        public AnoModelo getAnoModelo() { return anoModelo; }
        public Especificacao getEspecificacao() { return especificacao; }
        public Proprietario getProprietario() { return proprietario; }
        public StatusVeiculo getStatus() { return status; }
        public Set<String> getApolicesAssociadas() { return apolicesAssociadas; }
        public String getOperadorCriacao() { return operadorCriacao; }
        public String getUltimoOperador() { return ultimoOperador; }
    }
}