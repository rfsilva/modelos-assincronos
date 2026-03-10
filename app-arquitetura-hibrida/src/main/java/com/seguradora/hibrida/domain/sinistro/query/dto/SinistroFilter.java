package com.seguradora.hibrida.domain.sinistro.query.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * DTO para filtros de consulta de sinistros.
 * 
 * <p>Permite filtrar sinistros por diversos critérios de forma
 * dinâmica e combinada.
 */
@Schema(description = "Filtros para consulta de sinistros")
public class SinistroFilter {
    
    @Schema(description = "Filtrar por status do sinistro", example = "ABERTO")
    private String status;
    
    @Schema(description = "Filtrar por tipo de sinistro", example = "COLISAO")
    private String tipoSinistro;
    
    @Schema(description = "Filtrar por operador responsável", example = "João Silva")
    private String operadorResponsavel;
    
    @Schema(description = "Data de abertura inicial (inclusive)")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant dataAberturaInicio;
    
    @Schema(description = "Data de abertura final (inclusive)")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant dataAberturaFim;
    
    @Schema(description = "Filtrar por CPF do segurado", example = "12345678901")
    private String cpfSegurado;
    
    @Schema(description = "Filtrar por placa do veículo", example = "ABC1234")
    private String placa;
    
    @Schema(description = "Filtrar por número da apólice", example = "AP-2024-001234")
    private String apoliceNumero;
    
    @Schema(description = "Filtrar por prioridade", example = "ALTA")
    private String prioridade;
    
    @Schema(description = "Filtrar por canal de abertura", example = "WEB")
    private String canalAbertura;
    
    @Schema(description = "Filtrar apenas sinistros com consulta DETRAN pendente")
    private Boolean consultaDetranPendente;
    
    @Schema(description = "Filtrar por tag específica", example = "URGENTE")
    private String tag;
    
    // Construtores
    public SinistroFilter() {}
    
    public SinistroFilter(String status, String tipoSinistro, String operadorResponsavel, 
                         Instant dataAberturaInicio, Instant dataAberturaFim, 
                         String cpfSegurado, String placa, String apoliceNumero,
                         String prioridade, String canalAbertura, 
                         Boolean consultaDetranPendente, String tag) {
        this.status = status;
        this.tipoSinistro = tipoSinistro;
        this.operadorResponsavel = operadorResponsavel;
        this.dataAberturaInicio = dataAberturaInicio;
        this.dataAberturaFim = dataAberturaFim;
        this.cpfSegurado = cpfSegurado;
        this.placa = placa;
        this.apoliceNumero = apoliceNumero;
        this.prioridade = prioridade;
        this.canalAbertura = canalAbertura;
        this.consultaDetranPendente = consultaDetranPendente;
        this.tag = tag;
    }
    
    /**
     * Verifica se algum filtro foi aplicado.
     */
    public boolean hasFilters() {
        return status != null || 
               tipoSinistro != null || 
               operadorResponsavel != null ||
               dataAberturaInicio != null || 
               dataAberturaFim != null ||
               cpfSegurado != null || 
               placa != null ||
               apoliceNumero != null ||
               prioridade != null ||
               canalAbertura != null ||
               consultaDetranPendente != null ||
               tag != null;
    }
    
    /**
     * Cria filtro vazio.
     */
    public static SinistroFilter empty() {
        return new SinistroFilter();
    }
    
    /**
     * Cria filtro por status.
     */
    public static SinistroFilter porStatus(String status) {
        SinistroFilter filter = new SinistroFilter();
        filter.setStatus(status);
        return filter;
    }
    
    /**
     * Cria filtro por CPF.
     */
    public static SinistroFilter porCpf(String cpf) {
        SinistroFilter filter = new SinistroFilter();
        filter.setCpfSegurado(cpf);
        return filter;
    }
    
    /**
     * Cria filtro por placa.
     */
    public static SinistroFilter porPlaca(String placa) {
        SinistroFilter filter = new SinistroFilter();
        filter.setPlaca(placa);
        return filter;
    }
    
    // Getters e Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getTipoSinistro() { return tipoSinistro; }
    public void setTipoSinistro(String tipoSinistro) { this.tipoSinistro = tipoSinistro; }
    
    public String getOperadorResponsavel() { return operadorResponsavel; }
    public void setOperadorResponsavel(String operadorResponsavel) { this.operadorResponsavel = operadorResponsavel; }
    
    public Instant getDataAberturaInicio() { return dataAberturaInicio; }
    public void setDataAberturaInicio(Instant dataAberturaInicio) { this.dataAberturaInicio = dataAberturaInicio; }
    
    public Instant getDataAberturaFim() { return dataAberturaFim; }
    public void setDataAberturaFim(Instant dataAberturaFim) { this.dataAberturaFim = dataAberturaFim; }
    
    public String getCpfSegurado() { return cpfSegurado; }
    public void setCpfSegurado(String cpfSegurado) { this.cpfSegurado = cpfSegurado; }
    
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    
    public String getApoliceNumero() { return apoliceNumero; }
    public void setApoliceNumero(String apoliceNumero) { this.apoliceNumero = apoliceNumero; }
    
    public String getPrioridade() { return prioridade; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }
    
    public String getCanalAbertura() { return canalAbertura; }
    public void setCanalAbertura(String canalAbertura) { this.canalAbertura = canalAbertura; }
    
    public Boolean getConsultaDetranPendente() { return consultaDetranPendente; }
    public void setConsultaDetranPendente(Boolean consultaDetranPendente) { this.consultaDetranPendente = consultaDetranPendente; }
    
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    
    @Override
    public String toString() {
        return "SinistroFilter{" +
               "status='" + status + '\'' +
               ", tipoSinistro='" + tipoSinistro + '\'' +
               ", operadorResponsavel='" + operadorResponsavel + '\'' +
               ", cpfSegurado='" + cpfSegurado + '\'' +
               ", placa='" + placa + '\'' +
               '}';
    }
}