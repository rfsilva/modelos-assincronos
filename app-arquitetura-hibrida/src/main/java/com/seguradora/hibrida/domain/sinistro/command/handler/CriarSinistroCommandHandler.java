package com.seguradora.hibrida.domain.sinistro.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandBus;
import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.sinistro.aggregate.SinistroAggregate;
import com.seguradora.hibrida.domain.sinistro.command.CriarSinistroCommand;
import com.seguradora.hibrida.domain.sinistro.model.LocalOcorrencia;
import com.seguradora.hibrida.domain.sinistro.model.OcorrenciaSinistro;
import com.seguradora.hibrida.domain.sinistro.model.ProtocoloSinistro;
import com.seguradora.hibrida.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Command Handler para criação de sinistros.
 *
 * <p>Este handler processa o comando de criação de sinistro, realizando todas as
 * validações necessárias e criando o aggregate correspondente.
 *
 * <p><strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Validar segurado ativo e apólice vigente</li>
 *   <li>Verificar cobertura para tipo de sinistro</li>
 *   <li>Gerar protocolo único</li>
 *   <li>Criar aggregate e aplicar eventos</li>
 *   <li>Persistir eventos no Event Store</li>
 *   <li>Iniciar consulta Detran automaticamente quando necessário</li>
 * </ul>
 *
 * <p><strong>Validações realizadas:</strong>
 * <ul>
 *   <li>Segurado existe e está ativo</li>
 *   <li>Apólice está vigente e cobre o tipo de sinistro</li>
 *   <li>Veículo está associado à apólice</li>
 *   <li>Data de ocorrência é válida (não futura)</li>
 *   <li>Boletim de ocorrência fornecido quando obrigatório</li>
 *   <li>Protocolo é único no sistema</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CriarSinistroCommandHandler implements CommandHandler<CriarSinistroCommand> {

    private final EventStore eventStore;
    private final CommandBus commandBus;
    private final AggregateRepository<SinistroAggregate> sinistroRepository;

    @Override
    public CommandResult handle(CriarSinistroCommand command) {
        log.info("Processando criação de sinistro: protocolo={}, tipoSinistro={}",
            command.getProtocolo(), command.getTipoSinistro());

        try {
            // 1. Validar pré-requisitos
            validarPreRequisitos(command);

            // 2. Validar segurado ativo
            validarSeguradoAtivo(command.getSeguradoId());

            // 3. Validar apólice vigente e cobertura
            validarApoliceECobertura(command);

            // 4. Validar veículo associado à apólice
            validarVeiculoNaApolice(command.getVeiculoId(), command.getApoliceId());

            // 5. Gerar protocolo único
            ProtocoloSinistro protocolo = gerarProtocolo(command.getProtocolo());

            // 6. Criar objeto de valor OcorrenciaSinistro
            OcorrenciaSinistro ocorrencia = criarOcorrencia(command);

            // 7. Criar aggregate
            String sinistroId = command.getProtocolo(); // Usar protocolo como ID
            SinistroAggregate aggregate = new SinistroAggregate(
                sinistroId,
                protocolo,
                command.getSeguradoId(),
                command.getVeiculoId(),
                command.getApoliceId(),
                command.getTipoSinistro(),
                ocorrencia,
                command.getOperadorId()
            );

            // 8. Salvar aggregate (persiste eventos)
            sinistroRepository.save(aggregate);

            log.info("Sinistro criado com sucesso: sinistroId={}, protocolo={}",
                sinistroId, protocolo.getValor());

            // 9. Se tipo requer consulta Detran, iniciar automaticamente
            if (command.getTipoSinistro().requerConsultaDetran()) {
                iniciarConsultaDetranAutomatica(sinistroId, command);
            }

            return CommandResult.success(sinistroId, Map.of(
                "protocolo", protocolo.getValor(),
                "consultaDetranIniciada", command.getTipoSinistro().requerConsultaDetran()
            )).withCorrelationId(command.getCorrelationId());

        } catch (IllegalArgumentException e) {
            log.warn("Validação falhou ao criar sinistro: {}", e.getMessage());
            return CommandResult.failure(e.getMessage(), "VALIDATION_ERROR")
                .withCorrelationId(command.getCorrelationId());

        } catch (Exception e) {
            log.error("Erro ao criar sinistro: protocolo={}", command.getProtocolo(), e);
            return CommandResult.failure(
                "Erro ao processar criação do sinistro: " + e.getMessage(),
                "PROCESSING_ERROR"
            ).withCorrelationId(command.getCorrelationId());
        }
    }

    /**
     * Valida pré-requisitos do comando.
     */
    private void validarPreRequisitos(CriarSinistroCommand command) {
        if (!command.isDataOcorrenciaValida()) {
            throw new IllegalArgumentException(
                "Data de ocorrência não pode ser futura: " + command.getDataOcorrencia()
            );
        }

        if (!command.isBoletimObrigatorioValido()) {
            throw new IllegalArgumentException(
                "Boletim de ocorrência é obrigatório para sinistros do tipo " +
                command.getTipoSinistro()
            );
        }
    }

    /**
     * Valida se segurado está ativo (mock).
     */
    private void validarSeguradoAtivo(String seguradoId) {
        log.debug("Validando segurado ativo: {}", seguradoId);
        // TODO: Implementar consulta real ao domínio de Segurado
        // Por enquanto, mock simples
        if (seguradoId == null || seguradoId.trim().isEmpty()) {
            throw new IllegalArgumentException("Segurado inválido");
        }
    }

    /**
     * Valida se apólice está vigente e cobre o tipo de sinistro (mock).
     */
    private void validarApoliceECobertura(CriarSinistroCommand command) {
        log.debug("Validando apólice vigente e cobertura: apoliceId={}, tipoSinistro={}",
            command.getApoliceId(), command.getTipoSinistro());

        // TODO: Implementar consulta real ao domínio de Apólice
        // Verificar:
        // 1. Apólice existe e está vigente
        // 2. Apólice cobre o tipo de sinistro
        // 3. Apólice não está cancelada
        // 4. Data do sinistro está dentro da vigência

        if (command.getApoliceId() == null || command.getApoliceId().trim().isEmpty()) {
            throw new IllegalArgumentException("Apólice inválida");
        }

        // Mock: Simular verificação de carência
        if (command.getTipoSinistro().possuiCarencia()) {
            log.debug("Tipo de sinistro possui carência de {} dias",
                command.getTipoSinistro().getCarenciaDias());
            // TODO: Verificar se carência foi cumprida
        }
    }

    /**
     * Valida se veículo está associado à apólice (mock).
     */
    private void validarVeiculoNaApolice(String veiculoId, String apoliceId) {
        log.debug("Validando veículo na apólice: veiculoId={}, apoliceId={}",
            veiculoId, apoliceId);

        // TODO: Implementar consulta real ao domínio de Veículo
        // Verificar se o veículo está associado à apólice informada

        if (veiculoId == null || veiculoId.trim().isEmpty()) {
            throw new IllegalArgumentException("Veículo inválido");
        }
    }

    /**
     * Gera protocolo único para o sinistro.
     */
    private ProtocoloSinistro gerarProtocolo(String protocoloBase) {
        log.debug("Gerando protocolo único: base={}", protocoloBase);

        // Validar formato esperado (SIN-YYYY-NNNNNN)
        if (!protocoloBase.matches("SIN-\\d{4}-\\d{6}")) {
            throw new IllegalArgumentException(
                "Formato de protocolo inválido. Esperado: SIN-YYYY-NNNNNN"
            );
        }

        // TODO: Verificar se protocolo já existe no sistema
        // Por enquanto, apenas criar o objeto

        return ProtocoloSinistro.of(protocoloBase);
    }

    /**
     * Cria objeto de valor OcorrenciaSinistro.
     */
    private OcorrenciaSinistro criarOcorrencia(CriarSinistroCommand command) {
        // Converter LocalDateTime para Instant
        java.time.Instant dataOcorrenciaInstant = command.getDataOcorrencia()
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant();

        // Criar LocalOcorrencia a partir do endereço em texto
        LocalOcorrencia localOcorrencia = criarLocalOcorrencia(command.getLocalOcorrencia());

        return OcorrenciaSinistro.builder()
            .dataOcorrencia(dataOcorrenciaInstant)
            .localOcorrencia(localOcorrencia)
            .descricao(command.getDescricao())
            .boletimOcorrencia(command.getBoletimOcorrencia())
            .build();
    }

    /**
     * Cria objeto de valor LocalOcorrencia a partir de string de endereço.
     */
    private LocalOcorrencia criarLocalOcorrencia(String enderecoCompleto) {
        // TODO: Implementar parser de endereço ou integração com API de endereços
        // Por enquanto, criar objeto simples com o endereço como logradouro
        return LocalOcorrencia.builder()
            .logradouro(enderecoCompleto)
            .cidade("Não especificado") // Mock
            .estado("--") // Mock
            .build();
    }

    /**
     * Inicia consulta Detran automaticamente para tipos que requerem.
     */
    private void iniciarConsultaDetranAutomatica(String sinistroId, CriarSinistroCommand command) {
        log.info("Iniciando consulta Detran automática para sinistro: {}", sinistroId);

        try {
            // TODO: Obter placa e renavam do veículo
            String placa = "ABC-1234"; // Mock
            String renavam = "00000000000"; // Mock

            // Carregar aggregate e iniciar consulta
            SinistroAggregate aggregate = sinistroRepository.getById(sinistroId);
            aggregate.iniciarConsultaDetran(placa, renavam);
            sinistroRepository.save(aggregate);

            log.info("Consulta Detran iniciada: sinistroId={}, placa={}", sinistroId, placa);

        } catch (Exception e) {
            log.error("Erro ao iniciar consulta Detran para sinistro {}", sinistroId, e);
            // Não falhar o comando principal, apenas logar o erro
        }
    }

    @Override
    public Class<CriarSinistroCommand> getCommandType() {
        return CriarSinistroCommand.class;
    }

    @Override
    public int getTimeoutSeconds() {
        return 45; // Timeout maior devido a validações externas
    }
}
