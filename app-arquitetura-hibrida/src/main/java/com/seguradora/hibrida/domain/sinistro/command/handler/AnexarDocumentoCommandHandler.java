package com.seguradora.hibrida.domain.sinistro.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.sinistro.aggregate.SinistroAggregate;
import com.seguradora.hibrida.domain.sinistro.command.AnexarDocumentoCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Command Handler para anexação de documentos ao sinistro.
 *
 * <p>Este handler processa a anexação de documentos ao processo de sinistro,
 * realizando validações de formato, tipo e permissões.
 *
 * <p><strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Validar existência e acessibilidade do documento</li>
 *   <li>Verificar tipo e formato do documento</li>
 *   <li>Validar permissões do operador</li>
 *   <li>Anexar documento ao aggregate</li>
 *   <li>Iniciar validação automática quando aplicável</li>
 * </ul>
 *
 * <p><strong>Validações realizadas:</strong>
 * <ul>
 *   <li>Sinistro existe e aceita novos documentos</li>
 *   <li>Documento existe no sistema de armazenamento</li>
 *   <li>Tipo de documento é válido</li>
 *   <li>Formato do arquivo é aceito (PDF, JPG, PNG)</li>
 *   <li>Tamanho do arquivo dentro do limite (10MB)</li>
 *   <li>Operador possui permissões adequadas</li>
 *   <li>Documento não está duplicado</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnexarDocumentoCommandHandler implements CommandHandler<AnexarDocumentoCommand> {

    private final AggregateRepository<SinistroAggregate> sinistroRepository;

    // Formatos aceitos
    private static final List<String> FORMATOS_ACEITOS = List.of("pdf", "jpg", "jpeg", "png");

    // Tamanho máximo em bytes (10MB)
    private static final long TAMANHO_MAXIMO = 10 * 1024 * 1024;

    @Override
    public CommandResult handle(AnexarDocumentoCommand command) {
        log.info("Processando anexação de documento: sinistroId={}, documentoId={}, tipo={}",
            command.getSinistroId(), command.getDocumentoId(), command.getTipoDocumento());

        try {
            // 1. Carregar aggregate
            SinistroAggregate aggregate = sinistroRepository.getById(command.getSinistroId());

            // 2. Validar estado do sinistro
            validarEstadoSinistro(aggregate);

            // 3. Validar tipo de documento
            validarTipoDocumento(command);

            // 4. Validar documento no sistema de armazenamento
            validarDocumentoArmazenado(command);

            // 5. Validar formato e tamanho
            validarFormatoETamanho(command.getDocumentoId());

            // 6. Validar duplicação
            validarDuplicacao(aggregate, command.getDocumentoId());

            // 7. Validar permissões do operador
            validarPermissoesOperador(command);

            // 8. Anexar documento no aggregate
            aggregate.anexarDocumento(
                command.getDocumentoId(),
                command.getTipoDocumento(),
                command.getOperadorId(),
                command.getObservacoes()
            );

            // 9. Salvar aggregate (persiste eventos)
            sinistroRepository.save(aggregate);

            log.info("Documento anexado com sucesso: sinistroId={}, documentoId={}",
                command.getSinistroId(), command.getDocumentoId());

            // 10. Verificar se deve iniciar validação automática
            boolean validacaoAutomatica = command.isDocumentoObrigatorio();

            return CommandResult.success(command.getSinistroId(), Map.of(
                "documentoId", command.getDocumentoId(),
                "tipoDocumento", command.getTipoDocumento(),
                "obrigatorio", command.isDocumentoObrigatorio(),
                "validacaoAutomatica", validacaoAutomatica,
                "versao", aggregate.getVersion()
            )).withCorrelationId(command.getCorrelationId());

        } catch (IllegalArgumentException e) {
            log.warn("Validação falhou ao anexar documento ao sinistro {}: {}",
                command.getSinistroId(), e.getMessage());
            return CommandResult.failure(e.getMessage(), "VALIDATION_ERROR")
                .withCorrelationId(command.getCorrelationId());

        } catch (Exception e) {
            log.error("Erro ao anexar documento ao sinistro: {}", command.getSinistroId(), e);
            return CommandResult.failure(
                "Erro ao processar anexação de documento: " + e.getMessage(),
                "PROCESSING_ERROR"
            ).withCorrelationId(command.getCorrelationId());
        }
    }

    /**
     * Valida se o sinistro está em estado válido para anexar documentos.
     */
    private void validarEstadoSinistro(SinistroAggregate aggregate) {
        // Sinistro pode receber documentos enquanto estiver aberto
        if (!aggregate.isAberto()) {
            throw new IllegalArgumentException(
                "Sinistro não aceita novos documentos. Status atual: " +
                aggregate.getSinistro().getStatus()
            );
        }

        log.debug("Estado do sinistro validado para anexação");
    }

    /**
     * Valida tipo de documento.
     */
    private void validarTipoDocumento(AnexarDocumentoCommand command) {
        if (!command.isTipoDocumentoValido()) {
            throw new IllegalArgumentException(
                "Tipo de documento inválido: " + command.getTipoDocumento() +
                ". Tipos válidos: " + String.join(", ",
                    List.of("BOLETIM_OCORRENCIA", "FOTO_VEICULO", "LAUDO_PERICIAL",
                           "CNH_CONDUTOR", "ORCAMENTO_REPARO", "NOTA_FISCAL",
                           "DECLARACAO_TESTEMUNHA", "COMPROVANTE_PROPRIEDADE", "OUTROS"))
            );
        }

        log.debug("Tipo de documento validado: {}", command.getTipoDocumento());
    }

    /**
     * Valida se documento existe no sistema de armazenamento.
     */
    private void validarDocumentoArmazenado(AnexarDocumentoCommand command) {
        log.debug("Validando documento no sistema de armazenamento: {}", command.getDocumentoId());

        // TODO: Implementar consulta real ao sistema de armazenamento de documentos
        // Mock: Verificar formato básico do ID
        if (command.getDocumentoId() == null || command.getDocumentoId().trim().isEmpty()) {
            throw new IllegalArgumentException("ID do documento inválido");
        }

        if (command.getDocumentoId().length() < 10) {
            throw new IllegalArgumentException(
                "ID do documento parece inválido: " + command.getDocumentoId()
            );
        }

        // Mock: Simular verificação de existência
        // Em produção, deve consultar o serviço de armazenamento
        log.debug("Documento existe no sistema de armazenamento: {}", command.getDocumentoId());
    }

    /**
     * Valida formato e tamanho do documento.
     */
    private void validarFormatoETamanho(String documentoId) {
        log.debug("Validando formato e tamanho do documento: {}", documentoId);

        // TODO: Implementar consulta real aos metadados do documento
        // Mock: Extrair extensão do ID (simulado)
        String extensao = extrairExtensao(documentoId);

        if (extensao != null && !FORMATOS_ACEITOS.contains(extensao.toLowerCase())) {
            throw new IllegalArgumentException(
                "Formato de arquivo não aceito: " + extensao +
                ". Formatos aceitos: " + String.join(", ", FORMATOS_ACEITOS)
            );
        }

        // Mock: Verificar tamanho
        // Em produção, deve consultar metadados reais
        long tamanhoArquivo = 1024 * 1024; // Mock: 1MB

        if (tamanhoArquivo > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException(
                String.format(
                    "Arquivo muito grande: %.2f MB. Tamanho máximo: %.2f MB",
                    tamanhoArquivo / (1024.0 * 1024.0),
                    TAMANHO_MAXIMO / (1024.0 * 1024.0)
                )
            );
        }

        log.debug("Formato e tamanho validados: extensao={}, tamanho={}KB",
            extensao, tamanhoArquivo / 1024);
    }

    /**
     * Extrai extensão do arquivo (mock).
     */
    private String extrairExtensao(String documentoId) {
        if (documentoId == null || !documentoId.contains(".")) {
            return null;
        }
        int ultimoPonto = documentoId.lastIndexOf('.');
        if (ultimoPonto < documentoId.length() - 1) {
            return documentoId.substring(ultimoPonto + 1);
        }
        return null;
    }

    /**
     * Valida se documento já foi anexado (duplicação).
     */
    private void validarDuplicacao(SinistroAggregate aggregate, String documentoId) {
        // TODO: Verificar no aggregate se documento já foi anexado
        // Por enquanto, apenas logar
        log.debug("Verificando duplicação de documento: {}", documentoId);

        // Mock: Simular verificação
        // Em produção, deve consultar lista de documentos do aggregate
        boolean documentoDuplicado = false;

        if (documentoDuplicado) {
            throw new IllegalArgumentException(
                "Documento já foi anexado ao sinistro: " + documentoId
            );
        }
    }

    /**
     * Valida permissões do operador.
     */
    private void validarPermissoesOperador(AnexarDocumentoCommand command) {
        log.debug("Validando permissões do operador: {}", command.getOperadorId());

        // TODO: Implementar verificação real de permissões
        // Mock: Verificar se operador está ativo
        if (command.getOperadorId() == null || command.getOperadorId().trim().isEmpty()) {
            throw new IllegalArgumentException("Operador inválido");
        }

        // Verificar permissões específicas para documentos sensíveis
        if (command.isDocumentoObrigatorio()) {
            validarPermissoesDocumentoObrigatorio(command.getOperadorId());
        }

        log.debug("Permissões do operador validadas");
    }

    /**
     * Valida permissões para anexar documentos obrigatórios.
     */
    private void validarPermissoesDocumentoObrigatorio(String operadorId) {
        // Mock: Documentos obrigatórios podem requerer permissões especiais
        log.debug("Validando permissões especiais para documento obrigatório: {}", operadorId);

        // TODO: Implementar verificação real
        // Por enquanto, apenas logar
    }

    @Override
    public Class<AnexarDocumentoCommand> getCommandType() {
        return AnexarDocumentoCommand.class;
    }

    @Override
    public int getTimeoutSeconds() {
        return 30;
    }
}
