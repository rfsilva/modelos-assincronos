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
 * Comando para anexar documento ao sinistro.
 *
 * <p>Este comando registra a anexação de documentos ao processo de sinistro,
 * permitindo rastreamento e validação documental.
 *
 * <p><strong>Tipos de documentos suportados:</strong>
 * <ul>
 *   <li>BOLETIM_OCORRENCIA: Boletim policial do sinistro</li>
 *   <li>FOTO_VEICULO: Fotos dos danos ao veículo</li>
 *   <li>LAUDO_PERICIAL: Laudo técnico de avaliação</li>
 *   <li>CNH_CONDUTOR: CNH do condutor no momento</li>
 *   <li>ORCAMENTO_REPARO: Orçamentos de oficinas</li>
 *   <li>NOTA_FISCAL: Notas fiscais de reparos</li>
 *   <li>DECLARACAO_TESTEMUNHA: Declarações de testemunhas</li>
 *   <li>COMPROVANTE_PROPRIEDADE: Documento do veículo</li>
 *   <li>OUTROS: Outros documentos relevantes</li>
 * </ul>
 *
 * <p><strong>Validações realizadas:</strong>
 * <ul>
 *   <li>Documento existe e está acessível</li>
 *   <li>Tipo de documento é válido</li>
 *   <li>Formato do arquivo é aceito (PDF, JPG, PNG)</li>
 *   <li>Tamanho do arquivo dentro do limite</li>
 *   <li>Operador tem permissão para anexar</li>
 * </ul>
 *
 * <p><strong>Pós-condições:</strong>
 * <ul>
 *   <li>Documento registrado no sinistro</li>
 *   <li>Histórico de anexação criado</li>
 *   <li>Validação automática iniciada (quando aplicável)</li>
 *   <li>Notificações enviadas se documento obrigatório</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnexarDocumentoCommand implements Command {

    /**
     * ID do sinistro ao qual anexar o documento.
     */
    @NotBlank(message = "ID do sinistro não pode ser vazio")
    private String sinistroId;

    /**
     * ID do documento no sistema de armazenamento.
     */
    @NotBlank(message = "ID do documento não pode ser vazio")
    private String documentoId;

    /**
     * Tipo do documento sendo anexado.
     */
    @NotBlank(message = "Tipo de documento não pode ser vazio")
    private String tipoDocumento;

    /**
     * ID do operador responsável pela anexação.
     */
    @NotBlank(message = "ID do operador não pode ser vazio")
    private String operadorId;

    /**
     * Observações sobre o documento anexado.
     */
    @Size(max = 500, message = "Observações não podem exceder 500 caracteres")
    private String observacoes;

    // Campos da interface Command
    @Builder.Default
    private UUID commandId = UUID.randomUUID();

    @Builder.Default
    private Instant timestamp = Instant.now();

    private UUID correlationId;

    private String userId;

    /**
     * Enum para tipos padronizados de documentos.
     */
    public enum TipoDocumento {
        BOLETIM_OCORRENCIA,
        FOTO_VEICULO,
        LAUDO_PERICIAL,
        CNH_CONDUTOR,
        ORCAMENTO_REPARO,
        NOTA_FISCAL,
        DECLARACAO_TESTEMUNHA,
        COMPROVANTE_PROPRIEDADE,
        CONSULTA_DETRAN,
        OUTROS
    }

    /**
     * Verifica se o tipo de documento é válido.
     *
     * @return true se válido, false caso contrário
     */
    public boolean isTipoDocumentoValido() {
        try {
            TipoDocumento.valueOf(tipoDocumento.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Verifica se é um documento obrigatório.
     *
     * @return true se obrigatório, false caso contrário
     */
    public boolean isDocumentoObrigatorio() {
        if (tipoDocumento == null) {
            return false;
        }
        String tipo = tipoDocumento.toUpperCase();
        return tipo.equals("BOLETIM_OCORRENCIA") ||
               tipo.equals("CNH_CONDUTOR") ||
               tipo.equals("COMPROVANTE_PROPRIEDADE");
    }

    /**
     * Verifica se há observações fornecidas.
     *
     * @return true se há observações, false caso contrário
     */
    public boolean hasObservacoes() {
        return observacoes != null && !observacoes.trim().isEmpty();
    }

    /**
     * Obtém o tipo de documento como enum.
     *
     * @return TipoDocumento enum ou null se inválido
     */
    public TipoDocumento getTipoDocumentoEnum() {
        try {
            return TipoDocumento.valueOf(tipoDocumento.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String getCommandType() {
        return "AnexarDocumentoCommand";
    }

    @Override
    public String toString() {
        return String.format(
            "AnexarDocumentoCommand{sinistroId='%s', documentoId='%s', " +
            "tipoDocumento='%s', operadorId='%s'}",
            sinistroId, documentoId, tipoDocumento, operadorId
        );
    }
}
