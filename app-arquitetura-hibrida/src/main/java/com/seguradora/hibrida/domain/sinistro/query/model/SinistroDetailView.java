package com.seguradora.hibrida.domain.sinistro.query.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * View completa com todos os detalhes de um sinistro incluindo timeline e documentos.
 *
 * <p>Esta view contém todas as informações necessárias para exibir
 * a tela de detalhes completa de um sinistro, evitando múltiplas consultas.
 *
 * <p>Características:
 * <ul>
 *   <li>Dados completos do sinistro, segurado, veículo e apólice</li>
 *   <li>Timeline de eventos em JSONB</li>
 *   <li>Lista de documentos anexados</li>
 *   <li>Histórico de consultas externas (Detran)</li>
 *   <li>Observações e notas do analista</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Entity
@Table(name = "sinistro_detail_view", schema = "projections", indexes = {
    @Index(name = "idx_detail_protocolo", columnList = "protocolo", unique = true),
    @Index(name = "idx_detail_segurado", columnList = "segurado_id"),
    @Index(name = "idx_detail_apolice", columnList = "apolice_id"),
    @Index(name = "idx_detail_updated", columnList = "updated_at DESC")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SinistroDetailView {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // === IDENTIFICAÇÃO ===

    @Column(name = "protocolo", length = 20, nullable = false, unique = true)
    private String protocolo;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "tipo_sinistro", length = 50, nullable = false)
    private String tipoSinistro;

    @Column(name = "prioridade", length = 10)
    @Builder.Default
    private String prioridade = "NORMAL";

    // === DADOS DO SEGURADO (COMPLETO) ===

    @Column(name = "segurado_id", length = 36)
    private String seguradoId;

    @Column(name = "segurado_nome", length = 200, nullable = false)
    private String seguradoNome;

    @Column(name = "segurado_cpf", length = 11)
    private String seguradoCpf;

    @Column(name = "segurado_email", length = 100)
    private String seguradoEmail;

    @Column(name = "segurado_telefone", length = 20)
    private String seguradoTelefone;

    @Column(name = "segurado_endereco", columnDefinition = "TEXT")
    private String seguradoEndereco;

    @Column(name = "segurado_cidade", length = 100)
    private String seguradoCidade;

    @Column(name = "segurado_estado", length = 2)
    private String seguradoEstado;

    // === DADOS DO VEÍCULO (COMPLETO) ===

    @Column(name = "veiculo_placa", length = 8, nullable = false)
    private String veiculoPlaca;

    @Column(name = "veiculo_renavam", length = 11)
    private String veiculoRenavam;

    @Column(name = "veiculo_chassi", length = 17)
    private String veiculoChassi;

    @Column(name = "veiculo_marca", length = 50)
    private String veiculoMarca;

    @Column(name = "veiculo_modelo", length = 100)
    private String veiculoModelo;

    @Column(name = "veiculo_ano_fabricacao")
    private Integer veiculoAnoFabricacao;

    @Column(name = "veiculo_ano_modelo")
    private Integer veiculoAnoModelo;

    @Column(name = "veiculo_cor", length = 30)
    private String veiculoCor;

    // === DADOS DA APÓLICE ===

    @Column(name = "apolice_id", length = 36)
    private String apoliceId;

    @Column(name = "apolice_numero", length = 20)
    private String apoliceNumero;

    @Column(name = "apolice_produto", length = 50)
    private String apoliceProduto;

    @Column(name = "apolice_valor_segurado", precision = 15, scale = 2)
    private BigDecimal apoliceValorSegurado;

    @Column(name = "apolice_franquia", precision = 15, scale = 2)
    private BigDecimal apoliceFranquia;

    // === DADOS DO SINISTRO ===

    @Column(name = "data_ocorrencia", nullable = false)
    private Instant dataOcorrencia;

    @Column(name = "data_abertura", nullable = false)
    private Instant dataAbertura;

    @Column(name = "data_fechamento")
    private Instant dataFechamento;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "valor_estimado", precision = 15, scale = 2)
    private BigDecimal valorEstimado;

    @Column(name = "valor_aprovado", precision = 15, scale = 2)
    private BigDecimal valorAprovado;

    @Column(name = "valor_franquia", precision = 15, scale = 2)
    private BigDecimal valorFranquia;

    @Column(name = "analista_responsavel", length = 100)
    private String analistaResponsavel;

    @Column(name = "email_analista", length = 100)
    private String emailAnalista;

    // === LOCALIZAÇÃO DA OCORRÊNCIA ===

    @Column(name = "cep_ocorrencia", length = 8)
    private String cepOcorrencia;

    @Column(name = "endereco_ocorrencia", columnDefinition = "TEXT")
    private String enderecoOcorrencia;

    @Column(name = "cidade_ocorrencia", length = 100)
    private String cidadeOcorrencia;

    @Column(name = "estado_ocorrencia", length = 2)
    private String estadoOcorrencia;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // === TIMELINE (JSONB) ===

    /**
     * Timeline completa de eventos do sinistro.
     * Formato: [
     *   {
     *     "timestamp": "2024-01-15T10:30:00Z",
     *     "evento": "SINISTRO_CRIADO",
     *     "descricao": "Sinistro aberto",
     *     "usuario": "sistema",
     *     "detalhes": {...}
     *   },
     *   ...
     * ]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "timeline", columnDefinition = "jsonb")
    private List<Map<String, Object>> timeline;

    // === DOCUMENTOS (JSONB) ===

    /**
     * Lista de documentos anexados ao sinistro.
     * Formato: [
     *   {
     *     "id": "doc-123",
     *     "tipo": "FOTO_VEICULO",
     *     "nome": "foto-dianteira.jpg",
     *     "tamanho": 1024000,
     *     "status": "VALIDADO",
     *     "dataUpload": "2024-01-15T11:00:00Z",
     *     "url": "/api/documentos/doc-123"
     *   },
     *   ...
     * ]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "documentos", columnDefinition = "jsonb")
    private List<Map<String, Object>> documentos;

    // === HISTÓRICO DETRAN (JSONB) ===

    /**
     * Dados completos da consulta Detran.
     * Formato: {
     *   "consultaRealizada": true,
     *   "timestamp": "2024-01-15T12:00:00Z",
     *   "status": "SUCCESS",
     *   "dados": {
     *     "situacao": "REGULAR",
     *     "restricoes": [],
     *     "multas": [...],
     *     "debitos": [...]
     *   }
     * }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "historico_detran", columnDefinition = "jsonb")
    private Map<String, Object> historicoDetran;

    // === OBSERVAÇÕES ===

    /**
     * Observações e notas do analista.
     */
    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    /**
     * Motivo de reprovação (se aplicável).
     */
    @Column(name = "motivo_reprovacao", columnDefinition = "TEXT")
    private String motivoReprovacao;

    // === MÉTRICAS E INDICADORES ===

    @Column(name = "dentro_sla")
    @Builder.Default
    private Boolean dentroSla = true;

    @Column(name = "tempo_processamento_minutos")
    private Long tempoProcessamentoMinutos;

    @Column(name = "documentos_pendentes")
    @Builder.Default
    private Boolean documentosPendentes = false;

    @Column(name = "quantidade_documentos")
    @Builder.Default
    private Integer quantidadeDocumentos = 0;

    @Column(name = "consulta_detran_realizada")
    @Builder.Default
    private Boolean consultaDetranRealizada = false;

    // === AUDITORIA ===

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "last_event_id")
    private Long lastEventId;

    // === MÉTODOS DE NEGÓCIO ===

    /**
     * Retorna a timeline ordenada por timestamp (mais recente primeiro).
     *
     * @return lista de eventos ordenados
     */
    public List<Map<String, Object>> getTimelineOrdenada() {
        if (timeline == null || timeline.isEmpty()) {
            return Collections.emptyList();
        }

        return timeline.stream()
            .sorted((e1, e2) -> {
                String ts1 = (String) e1.getOrDefault("timestamp", "");
                String ts2 = (String) e2.getOrDefault("timestamp", "");
                return ts2.compareTo(ts1); // DESC
            })
            .collect(Collectors.toList());
    }

    /**
     * Retorna documentos agrupados por tipo.
     *
     * @return mapa de tipo -> lista de documentos
     */
    public Map<String, List<Map<String, Object>>> getDocumentosPorTipo() {
        if (documentos == null || documentos.isEmpty()) {
            return Collections.emptyMap();
        }

        return documentos.stream()
            .collect(Collectors.groupingBy(
                doc -> (String) doc.getOrDefault("tipo", "OUTROS")
            ));
    }

    /**
     * Retorna apenas documentos validados.
     *
     * @return lista de documentos validados
     */
    public List<Map<String, Object>> getDocumentosValidados() {
        if (documentos == null || documentos.isEmpty()) {
            return Collections.emptyList();
        }

        return documentos.stream()
            .filter(doc -> "VALIDADO".equals(doc.get("status")))
            .collect(Collectors.toList());
    }

    /**
     * Retorna apenas documentos pendentes ou rejeitados.
     *
     * @return lista de documentos pendentes
     */
    public List<Map<String, Object>> getDocumentosPendentesOuRejeitados() {
        if (documentos == null || documentos.isEmpty()) {
            return Collections.emptyList();
        }

        return documentos.stream()
            .filter(doc -> {
                String status = (String) doc.get("status");
                return "PENDENTE".equals(status) || "REJEITADO".equals(status);
            })
            .collect(Collectors.toList());
    }

    /**
     * Retorna o último evento da timeline.
     *
     * @return último evento ou vazio
     */
    public Optional<Map<String, Object>> getUltimoEvento() {
        if (timeline == null || timeline.isEmpty()) {
            return Optional.empty();
        }

        return timeline.stream()
            .max(Comparator.comparing(e -> (String) e.getOrDefault("timestamp", "")));
    }

    /**
     * Retorna a última atualização (maior timestamp da timeline ou updated_at).
     *
     * @return timestamp da última atualização
     */
    public Instant getUltimaAtualizacao() {
        Optional<Map<String, Object>> ultimoEvento = getUltimoEvento();
        if (ultimoEvento.isPresent()) {
            String timestamp = (String) ultimoEvento.get().get("timestamp");
            if (timestamp != null) {
                try {
                    return Instant.parse(timestamp);
                } catch (Exception e) {
                    // Ignora e retorna updatedAt
                }
            }
        }
        return updatedAt;
    }

    /**
     * Adiciona um novo evento à timeline.
     *
     * @param evento tipo do evento
     * @param descricao descrição do evento
     * @param usuario usuário que gerou o evento
     */
    public void adicionarEvento(String evento, String descricao, String usuario) {
        if (timeline == null) {
            timeline = new ArrayList<>();
        }

        Map<String, Object> novoEvento = new HashMap<>();
        novoEvento.put("timestamp", Instant.now().toString());
        novoEvento.put("evento", evento);
        novoEvento.put("descricao", descricao);
        novoEvento.put("usuario", usuario);

        timeline.add(novoEvento);
    }

    /**
     * Adiciona um documento à lista.
     *
     * @param documento dados do documento
     */
    public void adicionarDocumento(Map<String, Object> documento) {
        if (documentos == null) {
            documentos = new ArrayList<>();
        }
        documentos.add(documento);
        quantidadeDocumentos = documentos.size();
        atualizarStatusDocumentos();
    }

    /**
     * Remove um documento da lista.
     *
     * @param documentoId ID do documento
     */
    public void removerDocumento(String documentoId) {
        if (documentos == null) {
            return;
        }
        documentos.removeIf(doc -> documentoId.equals(doc.get("id")));
        quantidadeDocumentos = documentos.size();
        atualizarStatusDocumentos();
    }

    /**
     * Atualiza o status de documentos pendentes.
     */
    private void atualizarStatusDocumentos() {
        if (documentos == null || documentos.isEmpty()) {
            documentosPendentes = false;
            return;
        }

        documentosPendentes = documentos.stream()
            .anyMatch(doc -> {
                String status = (String) doc.get("status");
                return "PENDENTE".equals(status) || "REJEITADO".equals(status);
            });
    }

    /**
     * Verifica se o sinistro pode ser aprovado.
     *
     * @return true se todas as condições para aprovação estão atendidas
     */
    public boolean podeSerAprovado() {
        return Boolean.FALSE.equals(documentosPendentes) &&
               Boolean.TRUE.equals(consultaDetranRealizada) &&
               ("EM_ANALISE".equals(status) || "ABERTO".equals(status));
    }

    /**
     * Verifica se há restrições no Detran.
     *
     * @return true se houver restrições
     */
    @SuppressWarnings("unchecked")
    public boolean hasRestricoesDetran() {
        if (historicoDetran == null) {
            return false;
        }

        Map<String, Object> dados = (Map<String, Object>) historicoDetran.get("dados");
        if (dados == null) {
            return false;
        }

        List<Object> restricoes = (List<Object>) dados.get("restricoes");
        return restricoes != null && !restricoes.isEmpty();
    }

    /**
     * Calcula o tempo de processamento em dias.
     *
     * @return tempo em dias
     */
    public Double getTempoProcessamentoDias() {
        if (tempoProcessamentoMinutos == null || tempoProcessamentoMinutos == 0) {
            return 0.0;
        }
        return tempoProcessamentoMinutos / (60.0 * 24.0);
    }

    /**
     * Retorna endereço completo do segurado.
     *
     * @return endereço formatado
     */
    public String getSeguradoEnderecoCompleto() {
        StringBuilder sb = new StringBuilder();
        if (seguradoEndereco != null) {
            sb.append(seguradoEndereco);
        }
        if (seguradoCidade != null) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(seguradoCidade);
        }
        if (seguradoEstado != null) {
            if (seguradoCidade != null) {
                sb.append("/");
            } else if (sb.length() > 0) {
                sb.append(" - ");
            }
            sb.append(seguradoEstado);
        }
        return sb.length() > 0 ? sb.toString() : "Não informado";
    }

    /**
     * Retorna endereço completo da ocorrência.
     *
     * @return endereço formatado
     */
    public String getOcorrenciaEnderecoCompleto() {
        StringBuilder sb = new StringBuilder();
        if (enderecoOcorrencia != null) {
            sb.append(enderecoOcorrencia);
        }
        if (cidadeOcorrencia != null) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(cidadeOcorrencia);
        }
        if (estadoOcorrencia != null) {
            if (cidadeOcorrencia != null) {
                sb.append("/");
            } else if (sb.length() > 0) {
                sb.append(" - ");
            }
            sb.append(estadoOcorrencia);
        }
        if (cepOcorrencia != null) {
            sb.append(" - CEP: ").append(cepOcorrencia);
        }
        return sb.length() > 0 ? sb.toString() : "Não informado";
    }

    // === CALLBACKS JPA ===

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
    }

    @Override
    public String toString() {
        return String.format(
            "SinistroDetailView{protocolo='%s', segurado='%s', status='%s', docs=%d}",
            protocolo, seguradoNome, status, quantidadeDocumentos
        );
    }
}
