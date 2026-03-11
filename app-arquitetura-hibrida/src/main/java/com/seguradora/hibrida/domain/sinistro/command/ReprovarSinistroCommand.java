package com.seguradora.hibrida.domain.sinistro.command;

import com.seguradora.hibrida.command.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Comando para reprovação de sinistro após análise técnica.
 *
 * <p>Este comando finaliza a análise com reprovação do sinistro, documentando
 * o motivo e fundamentação legal para a decisão.
 *
 * <p><strong>Motivos comuns de reprovação:</strong>
 * <ul>
 *   <li>FORA_COBERTURA: Tipo de sinistro não coberto pela apólice</li>
 *   <li>CARENCIA_NAO_CUMPRIDA: Sinistro ocorreu durante período de carência</li>
 *   <li>DOCUMENTACAO_INSUFICIENTE: Falta de documentos essenciais</li>
 *   <li>INCONSISTENCIA_DADOS: Dados conflitantes ou suspeitos</li>
 *   <li>SINISTRO_PROPOSITAL: Evidências de fraude ou ação intencional</li>
 *   <li>CONDUTOR_IRREGULAR: Condutor sem habilitação ou embriagado</li>
 *   <li>VEICULO_IRREGULAR: Veículo com documentação vencida</li>
 * </ul>
 *
 * <p><strong>Validações realizadas:</strong>
 * <ul>
 *   <li>Motivo é válido e fundamentado</li>
 *   <li>Justificativa detalhada fornecida</li>
 *   <li>Fundamento legal citado quando aplicável</li>
 *   <li>Análise foi concluída adequadamente</li>
 * </ul>
 *
 * <p><strong>Pós-condições:</strong>
 * <ul>
 *   <li>Sinistro transicionado para REPROVADO</li>
 *   <li>Motivo e justificativa registrados</li>
 *   <li>Notificações enviadas ao segurado</li>
 *   <li>Documentação arquivada para auditoria</li>
 *   <li>Possibilidade de recurso habilitada</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReprovarSinistroCommand implements Command {

    /**
     * ID do sinistro a ser reprovado.
     */
    @NotBlank(message = "ID do sinistro não pode ser vazio")
    private String sinistroId;

    /**
     * Motivo principal da reprovação (código padronizado).
     */
    @NotBlank(message = "Motivo não pode ser vazio")
    @Size(min = 5, max = 100, message = "Motivo deve ter entre 5 e 100 caracteres")
    private String motivo;

    /**
     * Justificativa detalhada da reprovação.
     */
    @NotBlank(message = "Justificativa detalhada não pode ser vazia")
    @Size(min = 100, max = 3000, message = "Justificativa deve ter entre 100 e 3000 caracteres")
    private String justificativaDetalhada;

    /**
     * ID do analista responsável pela reprovação.
     */
    @NotBlank(message = "ID do analista não pode ser vazio")
    private String analistaId;

    /**
     * Fundamento legal ou cláusula contratual aplicável.
     */
    @Size(max = 500, message = "Fundamento legal não pode exceder 500 caracteres")
    private String fundamentoLegal;

    // Campos da interface Command
    @Builder.Default
    private UUID commandId = UUID.randomUUID();

    @Builder.Default
    private Instant timestamp = Instant.now();

    private UUID correlationId;

    private String userId;

    /**
     * Enum para motivos padronizados de reprovação.
     */
    public enum MotivoReprovacao {
        FORA_COBERTURA,
        CARENCIA_NAO_CUMPRIDA,
        DOCUMENTACAO_INSUFICIENTE,
        INCONSISTENCIA_DADOS,
        SINISTRO_PROPOSITAL,
        CONDUTOR_IRREGULAR,
        VEICULO_IRREGULAR,
        APOLICE_CANCELADA,
        VALOR_INCOMPATIVEL,
        OUTRO
    }

    /**
     * Verifica se o motivo é um dos motivos padronizados.
     *
     * @return true se padronizado, false caso contrário
     */
    public boolean isMotivoValido() {
        try {
            MotivoReprovacao.valueOf(motivo.toUpperCase().replace(" ", "_"));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Verifica se há fundamento legal fornecido.
     *
     * @return true se há fundamento, false caso contrário
     */
    public boolean hasFundamentoLegal() {
        return fundamentoLegal != null && !fundamentoLegal.trim().isEmpty();
    }

    /**
     * Verifica se a justificativa é adequada (mínimo de palavras).
     *
     * @return true se adequada (mínimo 30 palavras), false caso contrário
     */
    public boolean isJustificativaAdequada() {
        if (justificativaDetalhada == null || justificativaDetalhada.trim().isEmpty()) {
            return false;
        }
        String[] palavras = justificativaDetalhada.trim().split("\\s+");
        return palavras.length >= 30;
    }

    /**
     * Verifica se é reprovação por fraude.
     *
     * @return true se por fraude, false caso contrário
     */
    public boolean isReprovacaoPorFraude() {
        return motivo != null &&
               (motivo.toUpperCase().contains("PROPOSITAL") ||
                motivo.toUpperCase().contains("FRAUDE"));
    }

    @Override
    public String getCommandType() {
        return "ReprovarSinistroCommand";
    }

    @Override
    public String toString() {
        return String.format(
            "ReprovarSinistroCommand{sinistroId='%s', motivo='%s', " +
            "analistaId='%s', hasFundamentoLegal=%b}",
            sinistroId, motivo, analistaId, hasFundamentoLegal()
        );
    }
}
