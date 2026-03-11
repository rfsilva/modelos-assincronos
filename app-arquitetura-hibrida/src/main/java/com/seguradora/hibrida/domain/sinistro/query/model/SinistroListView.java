package com.seguradora.hibrida.domain.sinistro.query.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * View otimizada para listagens de sinistros.
 *
 * <p>Entidade projetada especificamente para listagens rápidas,
 * contendo apenas os campos essenciais exibidos em grids e tabelas.
 *
 * <p>Características:
 * <ul>
 *   <li>Campos desnormalizados para evitar joins</li>
 *   <li>Índices otimizados para filtros comuns</li>
 *   <li>Métodos auxiliares para formatação</li>
 *   <li>Leve e eficiente para paginação</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Entity
@Table(name = "sinistro_list_view", schema = "projections", indexes = {
    @Index(name = "idx_list_protocolo", columnList = "protocolo", unique = true),
    @Index(name = "idx_list_status_data", columnList = "status, data_ocorrencia DESC"),
    @Index(name = "idx_list_segurado", columnList = "segurado_nome"),
    @Index(name = "idx_list_data_ocorrencia", columnList = "data_ocorrencia DESC"),
    @Index(name = "idx_list_status", columnList = "status"),
    @Index(name = "idx_list_analista", columnList = "analista_responsavel"),
    @Index(name = "idx_list_placa", columnList = "veiculo_placa")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SinistroListView {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // === IDENTIFICAÇÃO ===

    /**
     * Protocolo único do sinistro.
     */
    @Column(name = "protocolo", length = 20, nullable = false, unique = true)
    private String protocolo;

    // === DADOS DO SEGURADO ===

    /**
     * Nome do segurado.
     */
    @Column(name = "segurado_nome", length = 200, nullable = false)
    private String seguradoNome;

    /**
     * CPF do segurado (apenas para busca).
     */
    @Column(name = "segurado_cpf", length = 11)
    private String seguradoCpf;

    /**
     * Telefone do segurado.
     */
    @Column(name = "segurado_telefone", length = 20)
    private String seguradoTelefone;

    // === DADOS DO VEÍCULO ===

    /**
     * Placa do veículo.
     */
    @Column(name = "veiculo_placa", length = 8, nullable = false)
    private String veiculoPlaca;

    /**
     * Marca e modelo do veículo (concatenado).
     */
    @Column(name = "veiculo_modelo", length = 150)
    private String veiculoModelo;

    /**
     * Ano do veículo (fabricação/modelo).
     */
    @Column(name = "veiculo_ano", length = 10)
    private String veiculoAno;

    // === DADOS DO SINISTRO ===

    /**
     * Tipo do sinistro (COLISAO, ROUBO, INCENDIO, etc).
     */
    @Column(name = "tipo", length = 50, nullable = false)
    private String tipo;

    /**
     * Status atual do sinistro.
     */
    @Column(name = "status", length = 30, nullable = false)
    private String status;

    /**
     * Data/hora da ocorrência.
     */
    @Column(name = "data_ocorrencia", nullable = false)
    private Instant dataOcorrencia;

    /**
     * Data/hora da abertura do sinistro.
     */
    @Column(name = "data_abertura", nullable = false)
    private Instant dataAbertura;

    /**
     * Valor estimado do sinistro.
     */
    @Column(name = "valor_estimado", precision = 15, scale = 2)
    private BigDecimal valorEstimado;

    /**
     * Analista responsável pelo sinistro.
     */
    @Column(name = "analista_responsavel", length = 100)
    private String analistaResponsavel;

    /**
     * Cidade da ocorrência.
     */
    @Column(name = "cidade_ocorrencia", length = 100)
    private String cidadeOcorrencia;

    /**
     * Estado da ocorrência (UF).
     */
    @Column(name = "estado_ocorrencia", length = 2)
    private String estadoOcorrencia;

    /**
     * Número da apólice.
     */
    @Column(name = "apolice_numero", length = 20)
    private String apoliceNumero;

    // === INDICADORES VISUAIS ===

    /**
     * Prioridade do sinistro (BAIXA, NORMAL, ALTA, URGENTE).
     */
    @Column(name = "prioridade", length = 10)
    @Builder.Default
    private String prioridade = "NORMAL";

    /**
     * Indica se está dentro do SLA.
     */
    @Column(name = "dentro_sla")
    @Builder.Default
    private Boolean dentroSla = true;

    /**
     * Indica se possui documentos pendentes.
     */
    @Column(name = "documentos_pendentes")
    @Builder.Default
    private Boolean documentosPendentes = false;

    /**
     * Indica se a consulta Detran foi realizada.
     */
    @Column(name = "detran_consultado")
    @Builder.Default
    private Boolean detranConsultado = false;

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

    // === MÉTODOS AUXILIARES ===

    /**
     * Formata a data de ocorrência para exibição (dd/MM/yyyy HH:mm).
     *
     * @return data formatada
     */
    public String getDataOcorrenciaFormatada() {
        if (dataOcorrencia == null) {
            return "";
        }
        LocalDateTime ldt = LocalDateTime.ofInstant(dataOcorrencia, ZoneId.systemDefault());
        return ldt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /**
     * Formata a data de abertura para exibição (dd/MM/yyyy HH:mm).
     *
     * @return data formatada
     */
    public String getDataAberturaFormatada() {
        if (dataAbertura == null) {
            return "";
        }
        LocalDateTime ldt = LocalDateTime.ofInstant(dataAbertura, ZoneId.systemDefault());
        return ldt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    /**
     * Formata o valor estimado para exibição (R$ 1.234,56).
     *
     * @return valor formatado
     */
    public String getValorEstimadoFormatado() {
        if (valorEstimado == null) {
            return "R$ 0,00";
        }
        return String.format("R$ %,.2f", valorEstimado);
    }

    /**
     * Retorna a localização completa (Cidade/UF).
     *
     * @return localização formatada
     */
    public String getLocalizacaoCompleta() {
        if (cidadeOcorrencia == null && estadoOcorrencia == null) {
            return "Não informado";
        }
        if (cidadeOcorrencia != null && estadoOcorrencia != null) {
            return cidadeOcorrencia + "/" + estadoOcorrencia;
        }
        return cidadeOcorrencia != null ? cidadeOcorrencia : estadoOcorrencia;
    }

    /**
     * Retorna o CPF formatado (###.###.###-##).
     *
     * @return CPF formatado
     */
    public String getSeguradoCpfFormatado() {
        if (seguradoCpf == null || seguradoCpf.length() != 11) {
            return seguradoCpf;
        }
        return String.format("%s.%s.%s-%s",
            seguradoCpf.substring(0, 3),
            seguradoCpf.substring(3, 6),
            seguradoCpf.substring(6, 9),
            seguradoCpf.substring(9, 11)
        );
    }

    /**
     * Retorna a placa formatada (ABC-1234 ou ABC1D23).
     *
     * @return placa formatada
     */
    public String getVeiculoPlacaFormatada() {
        if (veiculoPlaca == null || veiculoPlaca.length() < 7) {
            return veiculoPlaca;
        }
        // Formato Mercosul: ABC1D23
        if (veiculoPlaca.length() == 7) {
            return String.format("%s%s%s",
                veiculoPlaca.substring(0, 3),
                veiculoPlaca.substring(3, 4),
                veiculoPlaca.substring(4, 7)
            );
        }
        // Formato antigo: ABC-1234
        return String.format("%s-%s",
            veiculoPlaca.substring(0, 3),
            veiculoPlaca.substring(3)
        );
    }

    /**
     * Retorna descrição amigável do status.
     *
     * @return status formatado
     */
    public String getStatusDescricao() {
        if (status == null) {
            return "Desconhecido";
        }
        return switch (status) {
            case "ABERTO" -> "Aberto";
            case "EM_ANALISE" -> "Em Análise";
            case "AGUARDANDO_DOCUMENTOS" -> "Aguard. Documentos";
            case "APROVADO" -> "Aprovado";
            case "REPROVADO" -> "Reprovado";
            case "CANCELADO" -> "Cancelado";
            case "FECHADO" -> "Fechado";
            default -> status;
        };
    }

    /**
     * Retorna a cor do badge de status para UI.
     *
     * @return código de cor (success, warning, danger, info)
     */
    public String getStatusBadgeColor() {
        if (status == null) {
            return "secondary";
        }
        return switch (status) {
            case "ABERTO" -> "info";
            case "EM_ANALISE" -> "warning";
            case "AGUARDANDO_DOCUMENTOS" -> "warning";
            case "APROVADO" -> "success";
            case "REPROVADO" -> "danger";
            case "CANCELADO" -> "secondary";
            case "FECHADO" -> "dark";
            default -> "secondary";
        };
    }

    /**
     * Retorna a cor do badge de prioridade para UI.
     *
     * @return código de cor
     */
    public String getPrioridadeBadgeColor() {
        if (prioridade == null) {
            return "secondary";
        }
        return switch (prioridade) {
            case "BAIXA" -> "success";
            case "NORMAL" -> "info";
            case "ALTA" -> "warning";
            case "URGENTE" -> "danger";
            default -> "secondary";
        };
    }

    /**
     * Verifica se o sinistro está aberto.
     *
     * @return true se status for ABERTO ou EM_ANALISE
     */
    public boolean isAberto() {
        return "ABERTO".equals(status) || "EM_ANALISE".equals(status);
    }

    /**
     * Verifica se precisa de atenção urgente.
     *
     * @return true se for urgente ou fora do SLA
     */
    public boolean isPrecisaAtencao() {
        return "URGENTE".equals(prioridade) ||
               Boolean.FALSE.equals(dentroSla) ||
               Boolean.TRUE.equals(documentosPendentes);
    }

    /**
     * Retorna ícone representativo do tipo de sinistro.
     *
     * @return nome do ícone (Font Awesome ou similar)
     */
    public String getTipoIcone() {
        if (tipo == null) {
            return "fa-file";
        }
        return switch (tipo) {
            case "COLISAO" -> "fa-car-crash";
            case "ROUBO", "FURTO" -> "fa-user-secret";
            case "INCENDIO" -> "fa-fire";
            case "DANO_NATURAL" -> "fa-cloud-showers-heavy";
            case "VIDROS" -> "fa-glass-whiskey";
            case "TERCEIROS" -> "fa-users";
            default -> "fa-exclamation-triangle";
        };
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
            "SinistroListView{protocolo='%s', segurado='%s', status='%s', tipo='%s'}",
            protocolo, seguradoNome, status, tipo
        );
    }
}
