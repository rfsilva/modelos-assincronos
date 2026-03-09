package com.seguradora.hibrida.domain.veiculo.event;

import com.seguradora.hibrida.domain.veiculo.model.*;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Evento de domínio disparado quando um veículo é criado no sistema.
 * Contém todos os dados iniciais do veículo incluindo placa, RENAVAM, chassi,
 * marca, modelo, ano, especificações e proprietário.
 */
@Getter
@NoArgsConstructor
public class VeiculoCriadoEvent extends DomainEvent {
    
    private Placa placa;
    private Renavam renavam;
    private Chassi chassi;
    private String marca;
    private String modelo;
    private AnoModelo anoModelo;
    private Especificacao especificacao;
    private Proprietario proprietario;
    private StatusVeiculo status;

    public VeiculoCriadoEvent(
            String aggregateId,
            Placa placa,
            Renavam renavam,
            Chassi chassi,
            String marca,
            String modelo,
            AnoModelo anoModelo,
            Especificacao especificacao,
            Proprietario proprietario,
            StatusVeiculo status) {
        
        super(aggregateId, "VeiculoAggregate", 0);
        this.placa = placa;
        this.renavam = renavam;
        this.chassi = chassi;
        this.marca = validarMarca(marca);
        this.modelo = validarModelo(modelo);
        this.anoModelo = anoModelo;
        this.especificacao = especificacao;
        this.proprietario = proprietario;
        this.status = status != null ? status : StatusVeiculo.ATIVO;
    }

    private String validarMarca(String marca) {
        if (marca == null || marca.trim().isEmpty()) {
            throw new IllegalArgumentException("Marca não pode ser nula ou vazia");
        }
        if (marca.length() > 100) {
            throw new IllegalArgumentException("Marca não pode ter mais de 100 caracteres");
        }
        return marca.trim().toUpperCase();
    }

    private String validarModelo(String modelo) {
        if (modelo == null || modelo.trim().isEmpty()) {
            throw new IllegalArgumentException("Modelo não pode ser nulo ou vazio");
        }
        if (modelo.length() > 100) {
            throw new IllegalArgumentException("Modelo não pode ter mais de 100 caracteres");
        }
        return modelo.trim().toUpperCase();
    }

    @Override
    public String getEventType() {
        return "VeiculoCriado";
    }
}
