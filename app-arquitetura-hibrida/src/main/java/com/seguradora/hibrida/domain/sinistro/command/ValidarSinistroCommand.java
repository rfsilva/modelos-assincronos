package com.seguradora.hibrida.domain.sinistro.command;

import com.seguradora.hibrida.command.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Comando para validação de dados complementares do sinistro.
 *
 * <p>Este comando é executado após a criação inicial do sinistro para validar
 * e complementar as informações necessárias para análise.
 *
 * <p><strong>Dados complementares típicos:</strong>
 * <ul>
 *   <li>testemunhas: Lista de nomes e contatos</li>
 *   <li>condicaoClimatica: Descrição do clima no momento</li>
 *   <li>condicaoPista: Estado da via (seca, molhada, etc.)</li>
 *   <li>velocidadeEstimada: Velocidade no momento do sinistro</li>
 *   <li>danosTerceiros: Informações sobre terceiros envolvidos</li>
 * </ul>
 *
 * <p><strong>Validações realizadas:</strong>
 * <ul>
 *   <li>Sinistro existe e está em estado válido</li>
 *   <li>Dados complementares mínimos fornecidos</li>
 *   <li>Documentos obrigatórios anexados</li>
 *   <li>Formato e conteúdo dos documentos válidos</li>
 * </ul>
 *
 * <p><strong>Pós-condições:</strong>
 * <ul>
 *   <li>Sinistro transicionado para EM_VALIDACAO</li>
 *   <li>Dados complementares armazenados</li>
 *   <li>Documentos registrados no sistema</li>
 *   <li>Eventos de validação publicados</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidarSinistroCommand implements Command {

    /**
     * ID do sinistro a ser validado.
     */
    @NotBlank(message = "ID do sinistro não pode ser vazio")
    private String sinistroId;

    /**
     * Dados complementares específicos do tipo de sinistro.
     * Chave: nome do campo, Valor: conteúdo do campo.
     */
    @NotNull(message = "Dados complementares não podem ser nulos")
    @Builder.Default
    private Map<String, Object> dadosComplementares = Map.of();

    /**
     * Lista de IDs dos documentos anexados para validação.
     */
    @NotNull(message = "Lista de documentos não pode ser nula")
    @Builder.Default
    private List<String> documentosAnexados = List.of();

    /**
     * ID do operador responsável pela validação.
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
     * Verifica se há dados complementares suficientes.
     *
     * @return true se há pelo menos 3 campos complementares
     */
    public boolean hasDadosComplementaresSuficientes() {
        return dadosComplementares != null && dadosComplementares.size() >= 3;
    }

    /**
     * Verifica se há documentos anexados.
     *
     * @return true se há pelo menos um documento
     */
    public boolean hasDocumentosAnexados() {
        return documentosAnexados != null && !documentosAnexados.isEmpty();
    }

    /**
     * Obtém um dado complementar específico.
     *
     * @param chave Chave do dado complementar
     * @return Valor do dado ou null se não existir
     */
    public Object getDadoComplementar(String chave) {
        return dadosComplementares != null ? dadosComplementares.get(chave) : null;
    }

    @Override
    public String getCommandType() {
        return "ValidarSinistroCommand";
    }

    @Override
    public String toString() {
        return String.format(
            "ValidarSinistroCommand{sinistroId='%s', dadosComplementares=%d campos, " +
            "documentosAnexados=%d, operadorId='%s'}",
            sinistroId,
            dadosComplementares != null ? dadosComplementares.size() : 0,
            documentosAnexados != null ? documentosAnexados.size() : 0,
            operadorId
        );
    }
}
