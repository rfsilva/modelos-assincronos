package com.seguradora.hibrida.domain.sinistro.command;

import com.seguradora.hibrida.command.Command;
import com.seguradora.hibrida.domain.sinistro.model.TipoSinistro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Comando para criação de novo sinistro.
 *
 * <p>Este comando inicia o processo de registro de sinistro, validando os dados básicos
 * e informações obrigatórias conforme o tipo de sinistro.
 *
 * <p><strong>Validações realizadas:</strong>
 * <ul>
 *   <li>Protocolo único e não vazio</li>
 *   <li>Segurado, veículo e apólice válidos</li>
 *   <li>Tipo de sinistro suportado</li>
 *   <li>Data de ocorrência não futura</li>
 *   <li>Descrição com mínimo de caracteres</li>
 * </ul>
 *
 * <p><strong>Pós-condições:</strong>
 * <ul>
 *   <li>Sinistro criado com status AGUARDANDO_VALIDACAO</li>
 *   <li>Protocolo gerado e associado</li>
 *   <li>Eventos de criação publicados</li>
 *   <li>Se tipo requer Detran, consulta iniciada automaticamente</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriarSinistroCommand implements Command {

    /**
     * Protocolo único do sinistro (formato: SIN-YYYY-NNNNNN).
     */
    @NotBlank(message = "Protocolo não pode ser vazio")
    @Size(min = 10, max = 50, message = "Protocolo deve ter entre 10 e 50 caracteres")
    private String protocolo;

    /**
     * ID do segurado responsável pelo sinistro.
     */
    @NotBlank(message = "ID do segurado não pode ser vazio")
    private String seguradoId;

    /**
     * ID do veículo envolvido no sinistro.
     */
    @NotBlank(message = "ID do veículo não pode ser vazio")
    private String veiculoId;

    /**
     * ID da apólice vigente para cobertura.
     */
    @NotBlank(message = "ID da apólice não pode ser vazio")
    private String apoliceId;

    /**
     * Tipo do sinistro (COLISAO, ROUBO_FURTO, etc.).
     */
    @NotNull(message = "Tipo do sinistro não pode ser nulo")
    private TipoSinistro tipoSinistro;

    /**
     * Data e hora da ocorrência do sinistro.
     */
    @NotNull(message = "Data de ocorrência não pode ser nula")
    private LocalDateTime dataOcorrencia;

    /**
     * Local onde ocorreu o sinistro.
     */
    @NotBlank(message = "Local de ocorrência não pode ser vazio")
    @Size(min = 10, max = 500, message = "Local deve ter entre 10 e 500 caracteres")
    private String localOcorrencia;

    /**
     * Descrição detalhada do sinistro.
     */
    @NotBlank(message = "Descrição não pode ser vazia")
    @Size(min = 20, max = 2000, message = "Descrição deve ter entre 20 e 2000 caracteres")
    private String descricao;

    /**
     * Número do boletim de ocorrência policial (se aplicável).
     */
    @Size(max = 50, message = "Boletim de ocorrência não pode exceder 50 caracteres")
    private String boletimOcorrencia;

    /**
     * ID do operador que registrou o sinistro.
     */
    @NotBlank(message = "ID do operador não pode ser vazio")
    private String operadorId;

    // Campos da interface Command
    @Builder.Default
    private UUID commandId = UUID.randomUUID();

    @Builder.Default
    private Instant timestamp = Instant.now();

    private UUID correlationId;

    private String userId;

    /**
     * Valida se o boletim de ocorrência é obrigatório para o tipo de sinistro.
     *
     * @return true se válido, false caso contrário
     */
    public boolean isBoletimObrigatorioValido() {
        if (tipoSinistro != null && tipoSinistro.requerBoletimOcorrencia()) {
            return boletimOcorrencia != null && !boletimOcorrencia.trim().isEmpty();
        }
        return true;
    }

    /**
     * Verifica se a data de ocorrência é válida (não futura).
     *
     * @return true se válida, false caso contrário
     */
    public boolean isDataOcorrenciaValida() {
        if (dataOcorrencia == null) {
            return false;
        }
        return !dataOcorrencia.isAfter(LocalDateTime.now());
    }

    @Override
    public String getCommandType() {
        return "CriarSinistroCommand";
    }

    @Override
    public String toString() {
        return String.format(
            "CriarSinistroCommand{protocolo='%s', seguradoId='%s', veiculoId='%s', " +
            "apoliceId='%s', tipoSinistro=%s, dataOcorrencia=%s, operadorId='%s'}",
            protocolo, seguradoId, veiculoId, apoliceId, tipoSinistro,
            dataOcorrencia, operadorId
        );
    }
}
