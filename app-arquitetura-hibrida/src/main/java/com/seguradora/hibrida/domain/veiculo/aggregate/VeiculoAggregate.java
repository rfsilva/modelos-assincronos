package com.seguradora.hibrida.domain.veiculo.aggregate;

import com.seguradora.hibrida.aggregate.AggregateRoot;
import com.seguradora.hibrida.aggregate.EventSourcingHandler;
import com.seguradora.hibrida.domain.veiculo.event.*;
import com.seguradora.hibrida.domain.veiculo.model.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.*;

/**
 * Aggregate Root para o domínio de Veículo.
 * Gerencia o ciclo de vida completo de um veículo, incluindo criação,
 * atualização de especificações, transferência de propriedade e
 * associação com apólices de seguro.
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
@Getter
@NoArgsConstructor
public class VeiculoAggregate extends AggregateRoot {

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
    private Map<String, LocalDate> apolicesAtivas = new HashMap<>();
    
    // Histórico de propriedade
    private List<Proprietario> historicoProprietarios = new ArrayList<>();

    /**
     * Construtor para criação de um novo veículo.
     * Aplica evento VeiculoCriadoEvent.
     */
    public VeiculoAggregate(
            String veiculoId,
            Placa placa,
            Renavam renavam,
            Chassi chassi,
            String marca,
            String modelo,
            AnoModelo anoModelo,
            Especificacao especificacao,
            Proprietario proprietario) {
        
        // Validações de negócio
        validarCriacaoVeiculo(placa, renavam, chassi, marca, modelo, anoModelo, especificacao, proprietario);
        
        // Aplicar evento
        applyEvent(new VeiculoCriadoEvent(
            veiculoId,
            placa,
            renavam,
            chassi,
            marca,
            modelo,
            anoModelo,
            especificacao,
            proprietario,
            StatusVeiculo.ATIVO
        ));
    }

    /**
     * Atualiza as especificações do veículo.
     * Valida compatibilidade e aplica evento VeiculoAtualizadoEvent.
     */
    public void atualizarEspecificacoes(Especificacao novaEspecificacao, String operadorId) {
        validarAtualizacao(novaEspecificacao);
        
        Map<String, Object> valoresAnteriores = new HashMap<>();
        Map<String, Object> novosValores = new HashMap<>();
        
        if (!especificacao.getCor().equals(novaEspecificacao.getCor())) {
            valoresAnteriores.put("cor", especificacao.getCor());
            novosValores.put("cor", novaEspecificacao.getCor());
        }
        
        if (especificacao.getTipoCombustivel() != novaEspecificacao.getTipoCombustivel()) {
            valoresAnteriores.put("tipoCombustivel", especificacao.getTipoCombustivel());
            novosValores.put("tipoCombustivel", novaEspecificacao.getTipoCombustivel());
        }
        
        if (especificacao.getCilindrada() != novaEspecificacao.getCilindrada()) {
            valoresAnteriores.put("cilindrada", especificacao.getCilindrada());
            novosValores.put("cilindrada", novaEspecificacao.getCilindrada());
        }
        
        applyEvent(new VeiculoAtualizadoEvent(
            getId(),
            novaEspecificacao,
            valoresAnteriores,
            novosValores,
            operadorId
        ));
    }

    /**
     * Transfere a propriedade do veículo para novo proprietário.
     * Aplica evento PropriedadeTransferidaEvent.
     */
    public void transferirPropriedade(Proprietario novoProprietario, LocalDate dataTransferencia, String operadorId) {
        validarTransferenciaPropriedade(novoProprietario);
        
        applyEvent(new PropriedadeTransferidaEvent(
            getId(),
            proprietario,
            novoProprietario,
            dataTransferencia,
            operadorId
        ));
    }

    /**
     * Associa o veículo a uma apólice de seguro.
     * Aplica evento VeiculoAssociadoEvent.
     */
    public void associarApolice(String apoliceId, LocalDate dataInicio, String operadorId) {
        validarAssociacaoApolice(apoliceId);
        
        applyEvent(new VeiculoAssociadoEvent(
            getId(),
            apoliceId,
            dataInicio,
            operadorId
        ));
    }

    /**
     * Desassocia o veículo de uma apólice de seguro.
     * Aplica evento VeiculoDesassociadoEvent.
     */
    public void desassociarApolice(String apoliceId, LocalDate dataFim, String motivo, String operadorId) {
        validarDesassociacaoApolice(apoliceId);
        
        applyEvent(new VeiculoDesassociadoEvent(
            getId(),
            apoliceId,
            dataFim,
            motivo,
            operadorId
        ));
    }

    // =========================
    // EVENT SOURCING HANDLERS
    // =========================

    @EventSourcingHandler
    protected void on(VeiculoCriadoEvent event) {
        this.placa = event.getPlaca();
        this.renavam = event.getRenavam();
        this.chassi = event.getChassi();
        this.marca = event.getMarca();
        this.modelo = event.getModelo();
        this.anoModelo = event.getAnoModelo();
        this.especificacao = event.getEspecificacao();
        this.proprietario = event.getProprietario();
        this.status = event.getStatus();
        this.historicoProprietarios.add(event.getProprietario());
    }

    @EventSourcingHandler
    protected void on(VeiculoAtualizadoEvent event) {
        this.especificacao = event.getNovaEspecificacao();
    }

    @EventSourcingHandler
    protected void on(PropriedadeTransferidaEvent event) {
        this.proprietario = event.getNovoProprietario();
        this.historicoProprietarios.add(event.getNovoProprietario());
    }

    @EventSourcingHandler
    protected void on(VeiculoAssociadoEvent event) {
        this.apolicesAtivas.put(event.getApoliceId(), event.getDataInicio());
    }

    @EventSourcingHandler
    protected void on(VeiculoDesassociadoEvent event) {
        this.apolicesAtivas.remove(event.getApoliceId());
    }

    // =========================
    // VALIDAÇÕES DE NEGÓCIO
    // =========================

    private void validarCriacaoVeiculo(
            Placa placa, Renavam renavam, Chassi chassi,
            String marca, String modelo, AnoModelo anoModelo,
            Especificacao especificacao, Proprietario proprietario) {
        
        if (placa == null) {
            throw new IllegalArgumentException("Placa não pode ser nula");
        }
        if (renavam == null) {
            throw new IllegalArgumentException("RENAVAM não pode ser nulo");
        }
        if (chassi == null) {
            throw new IllegalArgumentException("Chassi não pode ser nulo");
        }
        if (marca == null || marca.trim().isEmpty()) {
            throw new IllegalArgumentException("Marca não pode ser nula ou vazia");
        }
        if (modelo == null || modelo.trim().isEmpty()) {
            throw new IllegalArgumentException("Modelo não pode ser nulo ou vazio");
        }
        if (anoModelo == null) {
            throw new IllegalArgumentException("Ano/Modelo não pode ser nulo");
        }
        if (especificacao == null) {
            throw new IllegalArgumentException("Especificação não pode ser nula");
        }
        if (proprietario == null) {
            throw new IllegalArgumentException("Proprietário não pode ser nulo");
        }
        
        // Validar compatibilidade entre categoria e especificações
        if (!especificacao.isCompativel(especificacao.getCategoria())) {
            throw new IllegalArgumentException("Especificação incompatível com categoria do veículo");
        }
        
        if (!especificacao.isCombustivelCompativel()) {
            throw new IllegalArgumentException("Tipo de combustível incompatível com categoria do veículo");
        }
    }

    private void validarAtualizacao(Especificacao novaEspecificacao) {
        if (status == StatusVeiculo.BLOQUEADO) {
            throw new IllegalStateException("Não é possível atualizar veículo bloqueado");
        }
        if (status == StatusVeiculo.SINISTRADO) {
            throw new IllegalStateException("Não é possível atualizar veículo sinistrado");
        }
        if (novaEspecificacao == null) {
            throw new IllegalArgumentException("Nova especificação não pode ser nula");
        }
        // Categoria não pode ser alterada após criação
        if (novaEspecificacao.getCategoria() != especificacao.getCategoria()) {
            throw new IllegalArgumentException("Categoria do veículo não pode ser alterada");
        }
    }

    private void validarTransferenciaPropriedade(Proprietario novoProprietario) {
        if (status == StatusVeiculo.BLOQUEADO) {
            throw new IllegalStateException("Não é possível transferir veículo bloqueado");
        }
        if (novoProprietario == null) {
            throw new IllegalArgumentException("Novo proprietário não pode ser nulo");
        }
        if (novoProprietario.getCpfCnpj().equals(proprietario.getCpfCnpj())) {
            throw new IllegalArgumentException("Novo proprietário não pode ser igual ao proprietário atual");
        }
    }

    private void validarAssociacaoApolice(String apoliceId) {
        if (status != StatusVeiculo.ATIVO) {
            throw new IllegalStateException("Veículo deve estar ativo para ser associado a uma apólice");
        }
        if (apoliceId == null || apoliceId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID da apólice não pode ser nulo ou vazio");
        }
        if (apolicesAtivas.containsKey(apoliceId)) {
            throw new IllegalArgumentException("Veículo já está associado a esta apólice");
        }
    }

    private void validarDesassociacaoApolice(String apoliceId) {
        if (apoliceId == null || apoliceId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID da apólice não pode ser nulo ou vazio");
        }
        if (!apolicesAtivas.containsKey(apoliceId)) {
            throw new IllegalArgumentException("Veículo não está associado a esta apólice");
        }
    }

    // =========================
    // SNAPSHOT SUPPORT
    // =========================

    @Override
    public Object createSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("id", getId());
        snapshot.put("placa", placa);
        snapshot.put("renavam", renavam);
        snapshot.put("chassi", chassi);
        snapshot.put("marca", marca);
        snapshot.put("modelo", modelo);
        snapshot.put("anoModelo", anoModelo);
        snapshot.put("especificacao", especificacao);
        snapshot.put("proprietario", proprietario);
        snapshot.put("status", status);
        snapshot.put("apolicesAtivas", new HashMap<>(apolicesAtivas));
        snapshot.put("historicoProprietarios", new ArrayList<>(historicoProprietarios));
        snapshot.put("version", getVersion());
        return snapshot;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void restoreFromSnapshot(Object snapshotData) {
        if (!(snapshotData instanceof Map)) {
            throw new IllegalArgumentException("Snapshot data must be a Map");
        }
        
        Map<String, Object> snapshot = (Map<String, Object>) snapshotData;
        this.placa = (Placa) snapshot.get("placa");
        this.renavam = (Renavam) snapshot.get("renavam");
        this.chassi = (Chassi) snapshot.get("chassi");
        this.marca = (String) snapshot.get("marca");
        this.modelo = (String) snapshot.get("modelo");
        this.anoModelo = (AnoModelo) snapshot.get("anoModelo");
        this.especificacao = (Especificacao) snapshot.get("especificacao");
        this.proprietario = (Proprietario) snapshot.get("proprietario");
        this.status = (StatusVeiculo) snapshot.get("status");
        this.apolicesAtivas = (Map<String, LocalDate>) snapshot.get("apolicesAtivas");
        this.historicoProprietarios = (List<Proprietario>) snapshot.get("historicoProprietarios");
    }

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
        this.apolicesAtivas.clear();
        this.historicoProprietarios.clear();
    }

    // =========================
    // MÉTODOS AUXILIARES
    // =========================

    /**
     * Verifica se o veículo possui apólices ativas.
     */
    public boolean temApolicesAtivas() {
        return !apolicesAtivas.isEmpty();
    }

    /**
     * Retorna a quantidade de apólices ativas.
     */
    public int getQuantidadeApolicesAtivas() {
        return apolicesAtivas.size();
    }

    /**
     * Verifica se o veículo está associado a uma apólice específica.
     */
    public boolean isAssociadoA(String apoliceId) {
        return apolicesAtivas.containsKey(apoliceId);
    }
}
