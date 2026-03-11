package com.seguradora.hibrida.domain.sinistro.query.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO para view detalhada de sinistro.
 * 
 * <p>Contém todas as informações de um sinistro de forma desnormalizada
 * e otimizada para consultas detalhadas.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "View detalhada de um sinistro")
public record SinistroDetailView(
        
        @Schema(description = "ID único do sinistro", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        
        @Schema(description = "Protocolo único do sinistro", example = "SIN-2024-001234")
        String protocolo,
        
        @Schema(description = "Informações do segurado")
        SeguradoInfo segurado,
        
        @Schema(description = "Informações do veículo")
        VeiculoInfo veiculo,
        
        @Schema(description = "Informações da apólice")
        ApoliceInfo apolice,
        
        @Schema(description = "Tipo do sinistro", example = "COLISAO")
        String tipoSinistro,
        
        @Schema(description = "Status atual do sinistro", example = "ABERTO")
        String status,
        
        @Schema(description = "Data e hora da ocorrência")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant dataOcorrencia,
        
        @Schema(description = "Data e hora de abertura do sinistro")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant dataAbertura,
        
        @Schema(description = "Data e hora de fechamento do sinistro")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant dataFechamento,
        
        @Schema(description = "Operador responsável pelo sinistro", example = "João Silva")
        String operadorResponsavel,
        
        @Schema(description = "Descrição detalhada do sinistro")
        String descricao,
        
        @Schema(description = "Valor estimado do sinistro", example = "15000.00")
        BigDecimal valorEstimado,
        
        @Schema(description = "Valor da franquia", example = "2000.00")
        BigDecimal valorFranquia,
        
        @Schema(description = "Informações da consulta ao DETRAN")
        ConsultaDetranInfo consultaDetran,
        
        @Schema(description = "Informações de localização da ocorrência")
        LocalizacaoInfo localizacao,
        
        @Schema(description = "Tags associadas ao sinistro")
        List<String> tags,
        
        @Schema(description = "Prioridade do sinistro", example = "NORMAL")
        String prioridade,
        
        @Schema(description = "Canal de abertura do sinistro", example = "WEB")
        String canalAbertura,
        
        @Schema(description = "Data de criação do registro")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant createdAt,
        
        @Schema(description = "Data da última atualização")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant updatedAt
) {
    
    @Builder
    @Schema(description = "Informações do segurado")
    public record SeguradoInfo(
            @Schema(description = "CPF do segurado", example = "12345678901")
            String cpf,
            
            @Schema(description = "Nome completo do segurado", example = "João da Silva")
            String nome,
            
            @Schema(description = "Email do segurado", example = "joao@email.com")
            String email,
            
            @Schema(description = "Telefone do segurado", example = "(11) 99999-9999")
            String telefone
    ) {}
    
    @Builder
    @Schema(description = "Informações do veículo")
    public record VeiculoInfo(
            @Schema(description = "Placa do veículo", example = "ABC1234")
            String placa,
            
            @Schema(description = "RENAVAM do veículo", example = "12345678901")
            String renavam,
            
            @Schema(description = "Chassi do veículo", example = "1HGBH41JXMN109186")
            String chassi,
            
            @Schema(description = "Marca do veículo", example = "Toyota")
            String marca,
            
            @Schema(description = "Modelo do veículo", example = "Corolla")
            String modelo,
            
            @Schema(description = "Ano de fabricação", example = "2020")
            Integer anoFabricacao,
            
            @Schema(description = "Ano do modelo", example = "2021")
            Integer anoModelo,
            
            @Schema(description = "Cor do veículo", example = "Branco")
            String cor
    ) {}
    
    @Builder
    @Schema(description = "Informações da apólice")
    public record ApoliceInfo(
            @Schema(description = "Número da apólice", example = "AP-2024-001234")
            String numero,
            
            @Schema(description = "Data de início da vigência")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            LocalDate vigenciaInicio,
            
            @Schema(description = "Data de fim da vigência")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            LocalDate vigenciaFim,
            
            @Schema(description = "Valor segurado", example = "50000.00")
            BigDecimal valorSegurado
    ) {}
    
    @Builder
    @Schema(description = "Informações da consulta ao DETRAN")
    public record ConsultaDetranInfo(
            @Schema(description = "Indica se a consulta foi realizada")
            Boolean realizada,
            
            @Schema(description = "Timestamp da consulta")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
            Instant timestamp,
            
            @Schema(description = "Status da consulta", example = "SUCCESS")
            String status,
            
            @Schema(description = "Dados retornados pelo DETRAN")
            Map<String, Object> dados
    ) {}
    
    @Builder
    @Schema(description = "Informações de localização da ocorrência")
    public record LocalizacaoInfo(
            @Schema(description = "CEP da ocorrência", example = "01234567")
            String cep,
            
            @Schema(description = "Endereço completo da ocorrência")
            String endereco,
            
            @Schema(description = "Cidade da ocorrência", example = "São Paulo")
            String cidade,
            
            @Schema(description = "Estado da ocorrência", example = "SP")
            String estado
    ) {}
}