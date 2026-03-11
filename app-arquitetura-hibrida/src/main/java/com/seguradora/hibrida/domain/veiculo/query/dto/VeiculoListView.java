package com.seguradora.hibrida.domain.veiculo.query.dto;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * DTO para listagem de veículos.
 * 
 * <p>Contém informações essenciais para exibição em listas,
 * otimizado para performance e redução de tráfego de rede.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Schema(description = "Dados resumidos de veículo para listagem")
public record VeiculoListView(
    
    @Schema(description = "ID único do veículo", example = "123e4567-e89b-12d3-a456-426614174000")
    String id,
    
    @Schema(description = "Placa do veículo", example = "ABC1234")
    String placa,
    
    @Schema(description = "Marca do veículo", example = "Honda")
    String marca,
    
    @Schema(description = "Modelo do veículo", example = "Civic")
    String modelo,
    
    @Schema(description = "Ano de fabricação", example = "2020")
    Integer anoFabricacao,
    
    @Schema(description = "Ano do modelo", example = "2021")
    Integer anoModelo,
    
    @Schema(description = "Cor do veículo", example = "Branco")
    String cor,
    
    @Schema(description = "Nome do proprietário", example = "João Silva")
    String proprietarioNome,
    
    @Schema(description = "CPF do proprietário", example = "123.456.789-01")
    String proprietarioCpf,
    
    @Schema(description = "Status do veículo")
    StatusVeiculo status,
    
    @Schema(description = "Indica se tem apólice ativa", example = "true")
    Boolean apoliceAtiva,
    
    @Schema(description = "Cidade do proprietário", example = "São Paulo")
    String cidade,
    
    @Schema(description = "Estado do proprietário", example = "SP")
    String estado,
    
    @Schema(description = "Data de criação do registro")
    Instant criadoEm,
    
    @Schema(description = "Data da última atualização")
    Instant atualizadoEm
) {
    
    /**
     * Retorna descrição completa do veículo.
     */
    public String getDescricaoCompleta() {
        return String.format("%s %s %d/%d - %s", marca, modelo, anoFabricacao, anoModelo, placa);
    }
    
    /**
     * Calcula a idade do veículo em anos.
     */
    public int getIdade() {
        return java.time.Year.now().getValue() - anoFabricacao;
    }
    
    /**
     * Verifica se o veículo está ativo.
     */
    public boolean isAtivo() {
        return StatusVeiculo.ATIVO.equals(status);
    }
    
    /**
     * Verifica se tem apólice ativa.
     */
    public boolean temApoliceAtiva() {
        return Boolean.TRUE.equals(apoliceAtiva);
    }
    
    /**
     * Formata CPF para exibição.
     */
    public String getCpfFormatado() {
        if (proprietarioCpf == null || proprietarioCpf.length() != 11) {
            return proprietarioCpf;
        }
        
        return String.format("%s.%s.%s-%s",
            proprietarioCpf.substring(0, 3),
            proprietarioCpf.substring(3, 6),
            proprietarioCpf.substring(6, 9),
            proprietarioCpf.substring(9, 11)
        );
    }
    
    /**
     * Retorna localização completa.
     */
    public String getLocalizacaoCompleta() {
        if (cidade != null && estado != null) {
            return cidade + " - " + estado;
        } else if (cidade != null) {
            return cidade;
        } else if (estado != null) {
            return estado;
        }
        return "Não informado";
    }
}