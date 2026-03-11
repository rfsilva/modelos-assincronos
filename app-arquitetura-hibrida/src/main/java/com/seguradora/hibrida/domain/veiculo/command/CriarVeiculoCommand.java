package com.seguradora.hibrida.domain.veiculo.command;

import com.seguradora.hibrida.command.Command;
import com.seguradora.hibrida.domain.veiculo.model.CategoriaVeiculo;
import com.seguradora.hibrida.domain.veiculo.model.TipoCombustivel;
import com.seguradora.hibrida.domain.veiculo.model.TipoPessoa;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Comando para criação de um novo veículo no sistema.
 * 
 * <p>Este comando encapsula todos os dados necessários para criar um veículo,
 * incluindo validações Bean Validation para garantir a integridade dos dados.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class CriarVeiculoCommand implements Command {
    
    private final UUID commandId;
    private final Instant timestamp;
    private final UUID correlationId;
    private final String userId;
    
    @NotBlank(message = "Placa é obrigatória")
    @Size(min = 7, max = 8, message = "Placa deve ter entre 7 e 8 caracteres")
    @Pattern(regexp = "^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$", 
             message = "Placa deve estar no formato brasileiro (ABC1234 ou ABC1D23)")
    private final String placa;
    
    @NotBlank(message = "RENAVAM é obrigatório")
    @Pattern(regexp = "^[0-9]{11}$", message = "RENAVAM deve ter 11 dígitos")
    private final String renavam;
    
    @NotBlank(message = "Chassi é obrigatório")
    @Size(min = 17, max = 17, message = "Chassi deve ter 17 caracteres")
    @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", 
             message = "Chassi deve seguir o padrão VIN (17 caracteres alfanuméricos, exceto I, O, Q)")
    private final String chassi;
    
    @NotBlank(message = "Marca é obrigatória")
    @Size(min = 2, max = 50, message = "Marca deve ter entre 2 e 50 caracteres")
    private final String marca;
    
    @NotBlank(message = "Modelo é obrigatório")
    @Size(min = 2, max = 100, message = "Modelo deve ter entre 2 e 100 caracteres")
    private final String modelo;
    
    @NotNull(message = "Ano de fabricação é obrigatório")
    @Min(value = 1900, message = "Ano de fabricação deve ser maior que 1900")
    @Max(value = 2030, message = "Ano de fabricação não pode ser superior a 2030")
    private final Integer anoFabricacao;
    
    @NotNull(message = "Ano modelo é obrigatório")
    @Min(value = 1900, message = "Ano modelo deve ser maior que 1900")
    @Max(value = 2032, message = "Ano modelo não pode ser superior a 2032")
    private final Integer anoModelo;
    
    @NotBlank(message = "Cor é obrigatória")
    @Size(min = 2, max = 50, message = "Cor deve ter entre 2 e 50 caracteres")
    private final String cor;
    
    @NotNull(message = "Tipo de combustível é obrigatório")
    private final TipoCombustivel tipoCombustivel;
    
    @NotNull(message = "Categoria do veículo é obrigatória")
    private final CategoriaVeiculo categoria;
    
    @Min(value = 50, message = "Cilindrada deve ser maior que 50cc")
    @Max(value = 20000, message = "Cilindrada não pode ser superior a 20000cc")
    private final Integer cilindrada;
    
    @NotBlank(message = "CPF/CNPJ do proprietário é obrigatório")
    @Pattern(regexp = "^[0-9]{11}$|^[0-9]{14}$", 
             message = "CPF deve ter 11 dígitos ou CNPJ deve ter 14 dígitos")
    private final String proprietarioCpfCnpj;
    
    @NotBlank(message = "Nome do proprietário é obrigatório")
    @Size(min = 2, max = 100, message = "Nome do proprietário deve ter entre 2 e 100 caracteres")
    private final String proprietarioNome;
    
    @NotNull(message = "Tipo de pessoa do proprietário é obrigatório")
    private final TipoPessoa proprietarioTipo;
    
    @NotBlank(message = "ID do operador é obrigatório")
    private final String operadorId;
    
    @Size(max = 500, message = "Observações não podem ter mais de 500 caracteres")
    private final String observacoes;
    
    /**
     * Construtor do comando.
     */
    public CriarVeiculoCommand(String placa, String renavam, String chassi, String marca,
                              String modelo, Integer anoFabricacao, Integer anoModelo, String cor,
                              TipoCombustivel tipoCombustivel, CategoriaVeiculo categoria,
                              Integer cilindrada, String proprietarioCpfCnpj, String proprietarioNome,
                              TipoPessoa proprietarioTipo, String operadorId, String observacoes,
                              UUID correlationId, String userId) {
        this.commandId = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.correlationId = correlationId;
        this.userId = userId;
        
        this.placa = placa != null ? placa.trim().toUpperCase() : null;
        this.renavam = renavam != null ? renavam.replaceAll("\\D", "") : null;
        this.chassi = chassi != null ? chassi.trim().toUpperCase() : null;
        this.marca = marca != null ? marca.trim() : null;
        this.modelo = modelo != null ? modelo.trim() : null;
        this.anoFabricacao = anoFabricacao;
        this.anoModelo = anoModelo;
        this.cor = cor != null ? cor.trim() : null;
        this.tipoCombustivel = tipoCombustivel;
        this.categoria = categoria;
        this.cilindrada = cilindrada;
        this.proprietarioCpfCnpj = proprietarioCpfCnpj != null ? proprietarioCpfCnpj.replaceAll("\\D", "") : null;
        this.proprietarioNome = proprietarioNome != null ? proprietarioNome.trim() : null;
        this.proprietarioTipo = proprietarioTipo;
        this.operadorId = operadorId != null ? operadorId.trim() : null;
        this.observacoes = observacoes != null ? observacoes.trim() : null;
    }
    
    // Implementação da interface Command
    @Override
    public UUID getCommandId() {
        return commandId;
    }
    
    @Override
    public Instant getTimestamp() {
        return timestamp;
    }
    
    @Override
    public UUID getCorrelationId() {
        return correlationId;
    }
    
    @Override
    public String getUserId() {
        return userId;
    }
    
    /**
     * Builder para facilitar a criação do comando.
     */
    public static class Builder {
        private String placa;
        private String renavam;
        private String chassi;
        private String marca;
        private String modelo;
        private Integer anoFabricacao;
        private Integer anoModelo;
        private String cor;
        private TipoCombustivel tipoCombustivel;
        private CategoriaVeiculo categoria;
        private Integer cilindrada;
        private String proprietarioCpfCnpj;
        private String proprietarioNome;
        private TipoPessoa proprietarioTipo;
        private String operadorId;
        private String observacoes;
        private UUID correlationId;
        private String userId;
        
        public Builder placa(String placa) {
            this.placa = placa;
            return this;
        }
        
        public Builder renavam(String renavam) {
            this.renavam = renavam;
            return this;
        }
        
        public Builder chassi(String chassi) {
            this.chassi = chassi;
            return this;
        }
        
        public Builder marca(String marca) {
            this.marca = marca;
            return this;
        }
        
        public Builder modelo(String modelo) {
            this.modelo = modelo;
            return this;
        }
        
        public Builder anoFabricacao(Integer anoFabricacao) {
            this.anoFabricacao = anoFabricacao;
            return this;
        }
        
        public Builder anoModelo(Integer anoModelo) {
            this.anoModelo = anoModelo;
            return this;
        }
        
        public Builder cor(String cor) {
            this.cor = cor;
            return this;
        }
        
        public Builder tipoCombustivel(TipoCombustivel tipoCombustivel) {
            this.tipoCombustivel = tipoCombustivel;
            return this;
        }
        
        public Builder categoria(CategoriaVeiculo categoria) {
            this.categoria = categoria;
            return this;
        }
        
        public Builder cilindrada(Integer cilindrada) {
            this.cilindrada = cilindrada;
            return this;
        }
        
        public Builder proprietarioCpfCnpj(String proprietarioCpfCnpj) {
            this.proprietarioCpfCnpj = proprietarioCpfCnpj;
            return this;
        }
        
        public Builder proprietarioNome(String proprietarioNome) {
            this.proprietarioNome = proprietarioNome;
            return this;
        }
        
        public Builder proprietarioTipo(TipoPessoa proprietarioTipo) {
            this.proprietarioTipo = proprietarioTipo;
            return this;
        }
        
        public Builder operadorId(String operadorId) {
            this.operadorId = operadorId;
            return this;
        }
        
        public Builder observacoes(String observacoes) {
            this.observacoes = observacoes;
            return this;
        }
        
        public Builder correlationId(UUID correlationId) {
            this.correlationId = correlationId;
            return this;
        }
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public CriarVeiculoCommand build() {
            return new CriarVeiculoCommand(placa, renavam, chassi, marca, modelo,
                                          anoFabricacao, anoModelo, cor, tipoCombustivel,
                                          categoria, cilindrada, proprietarioCpfCnpj,
                                          proprietarioNome, proprietarioTipo, operadorId, 
                                          observacoes, correlationId, userId);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    
    public String getPlaca() {
        return placa;
    }
    
    public String getRenavam() {
        return renavam;
    }
    
    public String getChassi() {
        return chassi;
    }
    
    public String getMarca() {
        return marca;
    }
    
    public String getModelo() {
        return modelo;
    }
    
    public Integer getAnoFabricacao() {
        return anoFabricacao;
    }
    
    public Integer getAnoModelo() {
        return anoModelo;
    }
    
    public String getCor() {
        return cor;
    }
    
    public TipoCombustivel getTipoCombustivel() {
        return tipoCombustivel;
    }
    
    public CategoriaVeiculo getCategoria() {
        return categoria;
    }
    
    public Integer getCilindrada() {
        return cilindrada;
    }
    
    public String getProprietarioCpfCnpj() {
        return proprietarioCpfCnpj;
    }
    
    public String getProprietarioNome() {
        return proprietarioNome;
    }
    
    public TipoPessoa getProprietarioTipo() {
        return proprietarioTipo;
    }
    
    public String getOperadorId() {
        return operadorId;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    /**
     * Verifica se o comando tem observações.
     */
    public boolean temObservacoes() {
        return observacoes != null && !observacoes.isEmpty();
    }
    
    /**
     * Verifica se o comando tem cilindrada informada.
     */
    public boolean temCilindrada() {
        return cilindrada != null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CriarVeiculoCommand that = (CriarVeiculoCommand) obj;
        return Objects.equals(commandId, that.commandId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(commandId);
    }
    
    @Override
    public String toString() {
        return String.format("CriarVeiculoCommand{id=%s, placa='%s', marca='%s', modelo='%s'}",
            commandId, placa, marca, modelo);
    }
}