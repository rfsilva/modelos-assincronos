package com.seguradora.hibrida.query.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Query Model otimizado para consultas de sinistros.
 * 
 * <p>Esta é uma projeção desnormalizada que agrega dados de múltiplos
 * agregados para otimizar consultas do lado de leitura (Query Side).
 * 
 * <p>Características:
 * <ul>
 *   <li>Dados desnormalizados para performance</li>
 *   <li>Índices otimizados para consultas frequentes</li>
 *   <li>Campos JSON para flexibilidade</li>
 *   <li>Full-text search habilitado</li>
 * </ul>
 */
@Entity
@Table(name = "sinistro_view", schema = "projections")
public class SinistroQueryModel {
    
    @Id
    private UUID id;
    
    @Column(name = "protocolo", length = 20, nullable = false, unique = true)
    private String protocolo;
    
    // === DADOS DO SEGURADO ===
    
    @Column(name = "cpf_segurado", length = 11, nullable = false)
    private String cpfSegurado;
    
    @Column(name = "nome_segurado", length = 200, nullable = false)
    private String nomeSegurado;
    
    @Column(name = "email_segurado", length = 100)
    private String emailSegurado;
    
    @Column(name = "telefone_segurado", length = 20)
    private String telefoneSegurado;
    
    // === DADOS DO VEÍCULO ===
    
    @Column(name = "placa", length = 8, nullable = false)
    private String placa;
    
    @Column(name = "renavam", length = 11)
    private String renavam;
    
    @Column(name = "chassi", length = 17)
    private String chassi;
    
    @Column(name = "marca", length = 50)
    private String marca;
    
    @Column(name = "modelo", length = 100)
    private String modelo;
    
    @Column(name = "ano_fabricacao")
    private Integer anoFabricacao;
    
    @Column(name = "ano_modelo")
    private Integer anoModelo;
    
    @Column(name = "cor", length = 30)
    private String cor;
    
    // === DADOS DA APÓLICE ===
    
    @Column(name = "apolice_numero", length = 20, nullable = false)
    private String apoliceNumero;
    
    @Column(name = "apolice_vigencia_inicio")
    private LocalDate apoliceVigenciaInicio;
    
    @Column(name = "apolice_vigencia_fim")
    private LocalDate apoliceVigenciaFim;
    
    @Column(name = "apolice_valor_segurado", precision = 15, scale = 2)
    private BigDecimal apoliceValorSegurado;
    
    // === DADOS DO SINISTRO ===
    
    @Column(name = "tipo_sinistro", length = 50, nullable = false)
    private String tipoSinistro;
    
    @Column(name = "status", length = 30, nullable = false)
    private String status;
    
    @Column(name = "data_ocorrencia", nullable = false)
    private Instant dataOcorrencia;
    
    @Column(name = "data_abertura", nullable = false)
    private Instant dataAbertura;
    
    @Column(name = "data_fechamento")
    private Instant dataFechamento;
    
    @Column(name = "operador_responsavel", length = 100)
    private String operadorResponsavel;
    
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;
    
    @Column(name = "valor_estimado", precision = 15, scale = 2)
    private BigDecimal valorEstimado;
    
    @Column(name = "valor_franquia", precision = 15, scale = 2)
    private BigDecimal valorFranquia;
    
    // === DADOS DO DETRAN ===
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dados_detran", columnDefinition = "jsonb")
    private Map<String, Object> dadosDetran;
    
    @Column(name = "consulta_detran_realizada")
    private Boolean consultaDetranRealizada = false;
    
    @Column(name = "consulta_detran_timestamp")
    private Instant consultaDetranTimestamp;
    
    @Column(name = "consulta_detran_status", length = 20)
    private String consultaDetranStatus;
    
    // === LOCALIZAÇÃO ===
    
    @Column(name = "cep_ocorrencia", length = 8)
    private String cepOcorrencia;
    
    @Column(name = "endereco_ocorrencia", columnDefinition = "TEXT")
    private String enderecoOcorrencia;
    
    @Column(name = "cidade_ocorrencia", length = 100)
    private String cidadeOcorrencia;
    
    @Column(name = "estado_ocorrencia", length = 2)
    private String estadoOcorrencia;
    
    // === METADADOS ===
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "text[]")
    private List<String> tags;
    
    @Column(name = "prioridade", length = 10)
    private String prioridade = "NORMAL";
    
    @Column(name = "canal_abertura", length = 30)
    private String canalAbertura;
    
    // === AUDITORIA ===
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "last_event_id")
    private Long lastEventId;
    
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 1L;
    
    // === CONSTRUTORES ===
    
    public SinistroQueryModel() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public SinistroQueryModel(UUID id, String protocolo) {
        this();
        this.id = id;
        this.protocolo = protocolo;
    }
    
    // === MÉTODOS DE NEGÓCIO ===
    
    /**
     * Verifica se o sinistro está aberto.
     */
    public boolean isAberto() {
        return "ABERTO".equals(status) || "EM_ANALISE".equals(status);
    }
    
    /**
     * Verifica se o sinistro está fechado.
     */
    public boolean isFechado() {
        return "FECHADO".equals(status) || "CANCELADO".equals(status);
    }
    
    /**
     * Verifica se a consulta ao DETRAN foi realizada com sucesso.
     */
    public boolean isConsultaDetranSucesso() {
        return Boolean.TRUE.equals(consultaDetranRealizada) && 
               "SUCCESS".equals(consultaDetranStatus);
    }
    
    /**
     * Verifica se a apólice está vigente na data de ocorrência.
     */
    public boolean isApoliceVigenteNaOcorrencia() {
        if (apoliceVigenciaInicio == null || apoliceVigenciaFim == null || dataOcorrencia == null) {
            return false;
        }
        
        LocalDate dataOcorrenciaLocal = dataOcorrencia.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return !dataOcorrenciaLocal.isBefore(apoliceVigenciaInicio) && 
               !dataOcorrenciaLocal.isAfter(apoliceVigenciaFim);
    }
    
    /**
     * Obtém valor do dado do DETRAN.
     */
    @SuppressWarnings("unchecked")
    public <T> T getDadoDetran(String chave, Class<T> tipo) {
        if (dadosDetran == null || !dadosDetran.containsKey(chave)) {
            return null;
        }
        
        Object valor = dadosDetran.get(chave);
        if (tipo.isInstance(valor)) {
            return (T) valor;
        }
        
        return null;
    }
    
    /**
     * Adiciona tag se não existir.
     */
    public void adicionarTag(String tag) {
        if (tags == null) {
            tags = new java.util.ArrayList<>();
        }
        
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }
    
    /**
     * Remove tag se existir.
     */
    public void removerTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }
    
    /**
     * Verifica se possui tag específica.
     */
    public boolean possuiTag(String tag) {
        return tags != null && tags.contains(tag);
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
    
    // === GETTERS E SETTERS ===
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getProtocolo() {
        return protocolo;
    }
    
    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }
    
    public String getCpfSegurado() {
        return cpfSegurado;
    }
    
    public void setCpfSegurado(String cpfSegurado) {
        this.cpfSegurado = cpfSegurado;
    }
    
    public String getNomeSegurado() {
        return nomeSegurado;
    }
    
    public void setNomeSegurado(String nomeSegurado) {
        this.nomeSegurado = nomeSegurado;
    }
    
    public String getEmailSegurado() {
        return emailSegurado;
    }
    
    public void setEmailSegurado(String emailSegurado) {
        this.emailSegurado = emailSegurado;
    }
    
    public String getTelefoneSegurado() {
        return telefoneSegurado;
    }
    
    public void setTelefoneSegurado(String telefoneSegurado) {
        this.telefoneSegurado = telefoneSegurado;
    }
    
    public String getPlaca() {
        return placa;
    }
    
    public void setPlaca(String placa) {
        this.placa = placa;
    }
    
    public String getRenavam() {
        return renavam;
    }
    
    public void setRenavam(String renavam) {
        this.renavam = renavam;
    }
    
    public String getChassi() {
        return chassi;
    }
    
    public void setChassi(String chassi) {
        this.chassi = chassi;
    }
    
    public String getMarca() {
        return marca;
    }
    
    public void setMarca(String marca) {
        this.marca = marca;
    }
    
    public String getModelo() {
        return modelo;
    }
    
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
    
    public Integer getAnoFabricacao() {
        return anoFabricacao;
    }
    
    public void setAnoFabricacao(Integer anoFabricacao) {
        this.anoFabricacao = anoFabricacao;
    }
    
    public Integer getAnoModelo() {
        return anoModelo;
    }
    
    public void setAnoModelo(Integer anoModelo) {
        this.anoModelo = anoModelo;
    }
    
    public String getCor() {
        return cor;
    }
    
    public void setCor(String cor) {
        this.cor = cor;
    }
    
    public String getApoliceNumero() {
        return apoliceNumero;
    }
    
    public void setApoliceNumero(String apoliceNumero) {
        this.apoliceNumero = apoliceNumero;
    }
    
    public LocalDate getApoliceVigenciaInicio() {
        return apoliceVigenciaInicio;
    }
    
    public void setApoliceVigenciaInicio(LocalDate apoliceVigenciaInicio) {
        this.apoliceVigenciaInicio = apoliceVigenciaInicio;
    }
    
    public LocalDate getApoliceVigenciaFim() {
        return apoliceVigenciaFim;
    }
    
    public void setApoliceVigenciaFim(LocalDate apoliceVigenciaFim) {
        this.apoliceVigenciaFim = apoliceVigenciaFim;
    }
    
    public BigDecimal getApoliceValorSegurado() {
        return apoliceValorSegurado;
    }
    
    public void setApoliceValorSegurado(BigDecimal apoliceValorSegurado) {
        this.apoliceValorSegurado = apoliceValorSegurado;
    }
    
    public String getTipoSinistro() {
        return tipoSinistro;
    }
    
    public void setTipoSinistro(String tipoSinistro) {
        this.tipoSinistro = tipoSinistro;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Instant getDataOcorrencia() {
        return dataOcorrencia;
    }
    
    public void setDataOcorrencia(Instant dataOcorrencia) {
        this.dataOcorrencia = dataOcorrencia;
    }
    
    public Instant getDataAbertura() {
        return dataAbertura;
    }
    
    public void setDataAbertura(Instant dataAbertura) {
        this.dataAbertura = dataAbertura;
    }
    
    public Instant getDataFechamento() {
        return dataFechamento;
    }
    
    public void setDataFechamento(Instant dataFechamento) {
        this.dataFechamento = dataFechamento;
    }
    
    public String getOperadorResponsavel() {
        return operadorResponsavel;
    }
    
    public void setOperadorResponsavel(String operadorResponsavel) {
        this.operadorResponsavel = operadorResponsavel;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public BigDecimal getValorEstimado() {
        return valorEstimado;
    }
    
    public void setValorEstimado(BigDecimal valorEstimado) {
        this.valorEstimado = valorEstimado;
    }
    
    public BigDecimal getValorFranquia() {
        return valorFranquia;
    }
    
    public void setValorFranquia(BigDecimal valorFranquia) {
        this.valorFranquia = valorFranquia;
    }
    
    public Map<String, Object> getDadosDetran() {
        return dadosDetran;
    }
    
    public void setDadosDetran(Map<String, Object> dadosDetran) {
        this.dadosDetran = dadosDetran;
    }
    
    public Boolean getConsultaDetranRealizada() {
        return consultaDetranRealizada;
    }
    
    public void setConsultaDetranRealizada(Boolean consultaDetranRealizada) {
        this.consultaDetranRealizada = consultaDetranRealizada;
    }
    
    public Instant getConsultaDetranTimestamp() {
        return consultaDetranTimestamp;
    }
    
    public void setConsultaDetranTimestamp(Instant consultaDetranTimestamp) {
        this.consultaDetranTimestamp = consultaDetranTimestamp;
    }
    
    public String getConsultaDetranStatus() {
        return consultaDetranStatus;
    }
    
    public void setConsultaDetranStatus(String consultaDetranStatus) {
        this.consultaDetranStatus = consultaDetranStatus;
    }
    
    public String getCepOcorrencia() {
        return cepOcorrencia;
    }
    
    public void setCepOcorrencia(String cepOcorrencia) {
        this.cepOcorrencia = cepOcorrencia;
    }
    
    public String getEnderecoOcorrencia() {
        return enderecoOcorrencia;
    }
    
    public void setEnderecoOcorrencia(String enderecoOcorrencia) {
        this.enderecoOcorrencia = enderecoOcorrencia;
    }
    
    public String getCidadeOcorrencia() {
        return cidadeOcorrencia;
    }
    
    public void setCidadeOcorrencia(String cidadeOcorrencia) {
        this.cidadeOcorrencia = cidadeOcorrencia;
    }
    
    public String getEstadoOcorrencia() {
        return estadoOcorrencia;
    }
    
    public void setEstadoOcorrencia(String estadoOcorrencia) {
        this.estadoOcorrencia = estadoOcorrencia;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public String getPrioridade() {
        return prioridade;
    }
    
    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }
    
    public String getCanalAbertura() {
        return canalAbertura;
    }
    
    public void setCanalAbertura(String canalAbertura) {
        this.canalAbertura = canalAbertura;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getLastEventId() {
        return lastEventId;
    }
    
    public void setLastEventId(Long lastEventId) {
        this.lastEventId = lastEventId;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return "SinistroQueryModel{" +
               "id=" + id +
               ", protocolo='" + protocolo + '\'' +
               ", cpfSegurado='" + cpfSegurado + '\'' +
               ", nomeSegurado='" + nomeSegurado + '\'' +
               ", placa='" + placa + '\'' +
               ", status='" + status + '\'' +
               ", dataOcorrencia=" + dataOcorrencia +
               '}';
    }
}