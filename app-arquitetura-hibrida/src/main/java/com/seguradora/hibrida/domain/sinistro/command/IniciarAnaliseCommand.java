package com.seguradora.hibrida.domain.sinistro.command;

import com.seguradora.hibrida.command.Command;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Comando para iniciar análise técnica do sinistro.
 *
 * <p>Este comando atribui um analista ao sinistro e define parâmetros para análise
 * técnica detalhada, incluindo prioridade e prazos.
 *
 * <p><strong>Prioridades de análise:</strong>
 * <ul>
 *   <li>ALTA: Sinistros com vítimas, valores elevados ou complexidade alta</li>
 *   <li>MEDIA: Sinistros padrão sem agravantes</li>
 *   <li>BAIXA: Sinistros simples e de baixo valor</li>
 * </ul>
 *
 * <p><strong>Pré-requisitos:</strong>
 * <ul>
 *   <li>Sinistro validado e com dados completos</li>
 *   <li>Consulta Detran concluída (se aplicável)</li>
 *   <li>Documentos obrigatórios anexados</li>
 *   <li>Analista disponível e qualificado</li>
 * </ul>
 *
 * <p><strong>Validações realizadas:</strong>
 * <ul>
 *   <li>Analista possui permissões necessárias</li>
 *   <li>Analista não está sobrecarregado</li>
 *   <li>Prazo estimado é realista</li>
 *   <li>Prioridade compatível com tipo de sinistro</li>
 * </ul>
 *
 * <p><strong>Pós-condições:</strong>
 * <ul>
 *   <li>Sinistro transicionado para EM_ANALISE</li>
 *   <li>Analista atribuído ao sinistro</li>
 *   <li>Prazo de análise registrado</li>
 *   <li>Notificações enviadas ao analista</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IniciarAnaliseCommand implements Command {

    /**
     * ID do sinistro a ser analisado.
     */
    @NotBlank(message = "ID do sinistro não pode ser vazio")
    private String sinistroId;

    /**
     * ID do analista responsável pela análise.
     */
    @NotBlank(message = "ID do analista não pode ser vazio")
    private String analistaId;

    /**
     * Prioridade da análise (ALTA, MEDIA, BAIXA).
     */
    @NotBlank(message = "Prioridade não pode ser vazia")
    private String prioridadeAnalise;

    /**
     * Prazo estimado para conclusão da análise em dias úteis.
     */
    @NotNull(message = "Prazo estimado não pode ser nulo")
    @Min(value = 1, message = "Prazo estimado deve ser no mínimo 1 dia útil")
    private Integer prazoEstimado;

    // Campos da interface Command
    @Builder.Default
    private UUID commandId = UUID.randomUUID();

    @Builder.Default
    private Instant timestamp = Instant.now();

    private UUID correlationId;

    private String userId;

    /**
     * Enum para prioridades válidas.
     */
    public enum Prioridade {
        ALTA, MEDIA, BAIXA
    }

    /**
     * Verifica se a prioridade é válida.
     *
     * @return true se válida, false caso contrário
     */
    public boolean isPrioridadeValida() {
        try {
            Prioridade.valueOf(prioridadeAnalise);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Verifica se o prazo é urgente (até 3 dias úteis).
     *
     * @return true se urgente, false caso contrário
     */
    public boolean isPrazoUrgente() {
        return prazoEstimado != null && prazoEstimado <= 3;
    }

    /**
     * Verifica se a prioridade é alta.
     *
     * @return true se alta, false caso contrário
     */
    public boolean isPrioridadeAlta() {
        return "ALTA".equalsIgnoreCase(prioridadeAnalise);
    }

    @Override
    public String getCommandType() {
        return "IniciarAnaliseCommand";
    }

    @Override
    public String toString() {
        return String.format(
            "IniciarAnaliseCommand{sinistroId='%s', analistaId='%s', " +
            "prioridadeAnalise='%s', prazoEstimado=%d dias}",
            sinistroId, analistaId, prioridadeAnalise, prazoEstimado
        );
    }
}
