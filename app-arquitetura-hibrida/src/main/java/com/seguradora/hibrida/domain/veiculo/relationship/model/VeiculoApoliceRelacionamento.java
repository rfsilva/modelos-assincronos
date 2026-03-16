package com.seguradora.hibrida.domain.veiculo.relationship.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade que representa o relacionamento entre Veículo e Apólice.
 * Mantém histórico completo de associações para auditoria e análise.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Entity
@Table(name = "veiculo_apolice_relacionamento", indexes = {
    @Index(name = "idx_rel_veiculo", columnList = "veiculo_id"),
    @Index(name = "idx_rel_apolice", columnList = "apolice_id"),
    @Index(name = "idx_rel_status", columnList = "status"),
    @Index(name = "idx_rel_data_inicio", columnList = "data_inicio"),
    @Index(name = "idx_rel_data_fim", columnList = "data_fim"),
    @Index(name = "idx_rel_ativo", columnList = "veiculo_id, status")
})
@Data
@NoArgsConstructor
public class VeiculoApoliceRelacionamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "veiculo_id", nullable = false, length = 100)
    private String veiculoId;

    @Column(name = "apolice_id", nullable = false, length = 100)
    private String apoliceId;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusRelacionamento status;

    @Column(name = "tipo_relacionamento", length = 20)
    @Enumerated(EnumType.STRING)
    private TipoRelacionamento tipoRelacionamento;

    // Dados desnormalizados do veículo para consultas rápidas
    @Column(name = "veiculo_placa", length = 10)
    private String veiculoPlaca;

    @Column(name = "veiculo_marca", length = 50)
    private String veiculoMarca;

    @Column(name = "veiculo_modelo", length = 100)
    private String veiculoModelo;

    @Column(name = "veiculo_categoria", length = 30)
    private String veiculoCategoria;

    // Dados desnormalizados da apólice
    @Column(name = "apolice_numero", length = 50)
    private String apoliceNumero;

    @Column(name = "segurado_cpf", length = 14)
    private String seguradoCpf;

    @Column(name = "segurado_nome", length = 200)
    private String seguradoNome;

    @Column(name = "tipo_cobertura", length = 30)
    private String tipoCobertura;

    // Motivo de desassociação
    @Column(name = "motivo_desassociacao", length = 500)
    private String motivoDesassociacao;

    // Auditoria
    @Column(name = "operador_associacao_id", length = 100)
    private String operadorAssociacaoId;

    @Column(name = "operador_desassociacao_id", length = 100)
    private String operadorDesassociacaoId;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Column(name = "observacoes", length = 1000)
    private String observacoes;

    /**
     * Verifica se o relacionamento está ativo.
     */
    public boolean isAtivo() {
        return StatusRelacionamento.ATIVO.equals(status);
    }

    /**
     * Verifica se o relacionamento está encerrado.
     */
    public boolean isEncerrado() {
        return StatusRelacionamento.ENCERRADO.equals(status);
    }

    /**
     * Calcula a duração da cobertura em dias.
     */
    public long calcularDuracaoDias() {
        LocalDate fim = dataFim != null ? dataFim : LocalDate.now();
        return java.time.temporal.ChronoUnit.DAYS.between(dataInicio, fim);
    }

    /**
     * Verifica se o relacionamento está vigente em uma data específica.
     */
    public boolean estaVigenteEm(LocalDate data) {
        if (data == null) {
            return false;
        }

        boolean iniciado = !data.isBefore(dataInicio);
        boolean naoEncerrado = dataFim == null || !data.isAfter(dataFim);

        return iniciado && naoEncerrado && isAtivo();
    }

    /**
     * Verifica se há gap de cobertura (relacionamento inativo).
     */
    public boolean temGapCobertura() {
        return isAtivo() && dataFim != null && dataFim.isBefore(LocalDate.now());
    }

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = LocalDateTime.now();
        if (this.status == null) {
            this.status = StatusRelacionamento.ATIVO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VeiculoApoliceRelacionamento that = (VeiculoApoliceRelacionamento) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("VeiculoApoliceRelacionamento{id='%s', veiculoPlaca='%s', apoliceNumero='%s', status=%s, dataInicio=%s, dataFim=%s}",
            id, veiculoPlaca, apoliceNumero, status != null ? status.name() : "null", dataInicio, dataFim);
    }
}
