package com.seguradora.hibrida.domain.veiculo.query.dto;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * DTO para detalhes completos de veículo.
 * 
 * <p>Contém todas as informações detalhadas do veículo,
 * incluindo histórico e relacionamentos.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Schema(description = "Dados completos de veículo")
public record VeiculoDetailView(
    
    @Schema(description = "ID único do veículo", example = "123e4567-e89b-12d3-a456-426614174000")
    String id,
    
    // Dados básicos
    @Schema(description = "Placa do veículo", example = "ABC1234")
    String placa,
    
    @Schema(description = "RENAVAM do veículo", example = "12345678901")
    String renavam,
    
    @Schema(description = "Chassi do veículo", example = "1HGBH41JXMN109186")
    String chassi,
    
    @Schema(description = "Marca do veículo", example = "Honda")
    String marca,
    
    @Schema(description = "Modelo do veículo", example = "Civic")
    String modelo,
    
    @Schema(description = "Ano de fabricação", example = "2020")
    Integer anoFabricacao,
    
    @Schema(description = "Ano do modelo", example = "2021")
    Integer anoModelo,
    
    // Especificações
    @Schema(description = "Cor do veículo", example = "Branco")
    String cor,
    
    @Schema(description = "Tipo de combustível", example = "FLEX")
    String tipoCombustivel,
    
    @Schema(description = "Categoria do veículo", example = "PASSEIO")
    String categoria,
    
    @Schema(description = "Cilindrada do motor", example = "1600")
    Integer cilindrada,
    
    // Proprietário
    @Schema(description = "Nome do proprietário", example = "João Silva")
    String proprietarioNome,
    
    @Schema(description = "CPF/CNPJ do proprietário", example = "123.456.789-01")
    String proprietarioCpf,
    
    @Schema(description = "Tipo de pessoa", example = "FISICA")
    String proprietarioTipo,
    
    // Status e relacionamentos
    @Schema(description = "Status do veículo")
    StatusVeiculo status,
    
    @Schema(description = "Indica se tem apólice ativa", example = "true")
    Boolean apoliceAtiva,
    
    // Localização
    @Schema(description = "Cidade do proprietário", example = "São Paulo")
    String cidade,
    
    @Schema(description = "Estado do proprietário", example = "SP")
    String estado,
    
    @Schema(description = "Região geográfica", example = "SUDESTE")
    String regiao,
    
    // Metadados
    @Schema(description = "Versão do aggregate", example = "5")
    Long version,
    
    @Schema(description = "ID do último evento processado", example = "12345")
    Long lastEventId,
    
    @Schema(description = "Data de criação do registro")
    Instant criadoEm,
    
    @Schema(description = "Data da última atualização")
    Instant atualizadoEm,
    
    // Dados adicionais
    @Schema(description = "Lista de apólices associadas")
    List<ApoliceAssociadaView> apolicesAssociadas,
    
    @Schema(description = "Histórico de alterações")
    List<HistoricoAlteracaoView> historicoAlteracoes
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
     * Formata CPF/CNPJ para exibição.
     */
    public String getCpfCnpjFormatado() {
        if (proprietarioCpf == null) {
            return null;
        }
        
        if (proprietarioCpf.length() == 11) {
            // CPF
            return String.format("%s.%s.%s-%s",
                proprietarioCpf.substring(0, 3),
                proprietarioCpf.substring(3, 6),
                proprietarioCpf.substring(6, 9),
                proprietarioCpf.substring(9, 11)
            );
        } else if (proprietarioCpf.length() == 14) {
            // CNPJ
            return String.format("%s.%s.%s/%s-%s",
                proprietarioCpf.substring(0, 2),
                proprietarioCpf.substring(2, 5),
                proprietarioCpf.substring(5, 8),
                proprietarioCpf.substring(8, 12),
                proprietarioCpf.substring(12, 14)
            );
        }
        
        return proprietarioCpf;
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
    
    /**
     * Conta número de apólices ativas.
     */
    public int getNumeroApolicesAtivas() {
        if (apolicesAssociadas == null) {
            return 0;
        }
        
        return (int) apolicesAssociadas.stream()
            .filter(ApoliceAssociadaView::ativa)
            .count();
    }
    
    /**
     * Verifica se é veículo novo (até 3 anos).
     */
    public boolean isVeiculoNovo() {
        return getIdade() <= 3;
    }
    
    /**
     * Verifica se é veículo seminovo (4 a 10 anos).
     */
    public boolean isVeiculoSeminovo() {
        int idade = getIdade();
        return idade >= 4 && idade <= 10;
    }
    
    /**
     * Verifica se é veículo usado (mais de 10 anos).
     */
    public boolean isVeiculoUsado() {
        return getIdade() > 10;
    }
    
    /**
     * DTO para apólices associadas.
     */
    @Schema(description = "Dados de apólice associada ao veículo")
    public record ApoliceAssociadaView(
        @Schema(description = "ID da apólice", example = "POL-2024-001234")
        String apoliceId,
        
        @Schema(description = "Número da apólice", example = "123456789")
        String numero,
        
        @Schema(description = "Status da apólice", example = "ATIVA")
        String status,
        
        @Schema(description = "Indica se está ativa", example = "true")
        Boolean ativa,
        
        @Schema(description = "Data de início da cobertura")
        Instant dataInicio,
        
        @Schema(description = "Data de fim da cobertura")
        Instant dataFim,
        
        @Schema(description = "Tipo de cobertura", example = "COMPREENSIVA")
        String tipoCobertura
    ) {}
    
    /**
     * DTO para histórico de alterações.
     */
    @Schema(description = "Histórico de alteração do veículo")
    public record HistoricoAlteracaoView(
        @Schema(description = "Data da alteração")
        Instant dataAlteracao,
        
        @Schema(description = "Tipo de alteração", example = "ESPECIFICACAO_ATUALIZADA")
        String tipoAlteracao,
        
        @Schema(description = "Descrição da alteração", example = "Cor alterada de Branco para Azul")
        String descricao,
        
        @Schema(description = "Operador responsável", example = "operador123")
        String operador,
        
        @Schema(description = "Versão do aggregate após a alteração", example = "3")
        Long versao
    ) {}
}