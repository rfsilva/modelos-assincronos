package com.seguradora.hibrida.domain.sinistro.command;

import com.seguradora.hibrida.command.Command;
import com.seguradora.hibrida.domain.sinistro.model.ValorIndenizacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Comando para aprovação de sinistro após análise técnica.
 *
 * <p>Este comando finaliza a análise com aprovação do sinistro, definindo
 * o valor de indenização e documentação comprobatória.
 *
 * <p><strong>Validações realizadas:</strong>
 * <ul>
 *   <li>Analista possui alçada para o valor aprovado</li>
 *   <li>Valor de indenização dentro dos limites da apólice</li>
 *   <li>Justificativa adequada e completa</li>
 *   <li>Documentos comprobatórios anexados</li>
 *   <li>Análise foi concluída e está completa</li>
 * </ul>
 *
 * <p><strong>Limites de alçada (exemplo):</strong>
 * <ul>
 *   <li>Analista Jr: Até R$ 10.000</li>
 *   <li>Analista Pl: Até R$ 50.000</li>
 *   <li>Analista Sr: Até R$ 100.000</li>
 *   <li>Gerente: Acima de R$ 100.000</li>
 * </ul>
 *
 * <p><strong>Pós-condições:</strong>
 * <ul>
 *   <li>Sinistro transicionado para APROVADO</li>
 *   <li>Valor de indenização registrado</li>
 *   <li>Processo de pagamento iniciado</li>
 *   <li>Notificações enviadas ao segurado</li>
 *   <li>Documentação arquivada</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AprovarSinistroCommand implements Command {

    /**
     * ID do sinistro a ser aprovado.
     */
    @NotBlank(message = "ID do sinistro não pode ser vazio")
    private String sinistroId;

    /**
     * Valor da indenização aprovada com todos os cálculos.
     */
    @NotNull(message = "Valor de indenização não pode ser nulo")
    @Valid
    private ValorIndenizacao valorIndenizacao;

    /**
     * Justificativa técnica da aprovação.
     */
    @NotBlank(message = "Justificativa não pode ser vazia")
    @Size(min = 50, max = 2000, message = "Justificativa deve ter entre 50 e 2000 caracteres")
    private String justificativa;

    /**
     * ID do analista responsável pela aprovação.
     */
    @NotBlank(message = "ID do analista não pode ser vazio")
    private String analistaId;

    /**
     * Lista de IDs dos documentos comprobatórios da aprovação.
     */
    @NotNull(message = "Lista de documentos não pode ser nula")
    @Builder.Default
    private List<String> documentosComprobatorios = List.of();

    // Campos da interface Command
    @Builder.Default
    private UUID commandId = UUID.randomUUID();

    @Builder.Default
    private Instant timestamp = Instant.now();

    private UUID correlationId;

    private String userId;

    /**
     * Verifica se há documentos comprobatórios suficientes.
     *
     * @return true se há pelo menos 2 documentos
     */
    public boolean hasDocumentosComprobatoriosSuficientes() {
        return documentosComprobatorios != null && documentosComprobatorios.size() >= 2;
    }

    /**
     * Verifica se o valor de indenização é válido e positivo.
     *
     * @return true se válido, false caso contrário
     */
    public boolean isValorIndenizacaoValido() {
        return valorIndenizacao != null && valorIndenizacao.isValido();
    }

    /**
     * Verifica se a justificativa é adequada (mínimo de palavras).
     *
     * @return true se adequada (mínimo 20 palavras), false caso contrário
     */
    public boolean isJustificativaAdequada() {
        if (justificativa == null || justificativa.trim().isEmpty()) {
            return false;
        }
        String[] palavras = justificativa.trim().split("\\s+");
        return palavras.length >= 20;
    }

    @Override
    public String getCommandType() {
        return "AprovarSinistroCommand";
    }

    @Override
    public String toString() {
        return String.format(
            "AprovarSinistroCommand{sinistroId='%s', valorIndenizacao=%s, " +
            "analistaId='%s', documentosComprobatorios=%d}",
            sinistroId,
            valorIndenizacao != null ? valorIndenizacao.getValorLiquido() : "null",
            analistaId,
            documentosComprobatorios != null ? documentosComprobatorios.size() : 0
        );
    }
}
