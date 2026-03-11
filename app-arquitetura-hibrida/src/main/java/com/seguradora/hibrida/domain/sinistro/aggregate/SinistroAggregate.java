package com.seguradora.hibrida.domain.sinistro.aggregate;

import com.seguradora.hibrida.aggregate.AggregateRoot;
import com.seguradora.hibrida.aggregate.EventSourcingHandler;
import com.seguradora.hibrida.aggregate.exception.BusinessRuleViolationException;
import com.seguradora.hibrida.aggregate.validation.BusinessRule;
import com.seguradora.hibrida.domain.sinistro.event.*;
import com.seguradora.hibrida.domain.sinistro.model.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregate Root para o domínio de Sinistro.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Criar novos sinistros com validações completas</li>
 *   <li>Validar dados complementares e documentos</li>
 *   <li>Gerenciar fluxo de análise técnica</li>
 *   <li>Coordenar consultas ao Detran de forma assíncrona</li>
 *   <li>Aprovar ou reprovar sinistros com justificativa</li>
 *   <li>Gerenciar documentação e validação de documentos</li>
 *   <li>Garantir invariantes de negócio e transições de estado válidas</li>
 *   <li>Gerar eventos de domínio para cada operação</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Getter
public class SinistroAggregate extends AggregateRoot {

    // Estado do aggregate
    private Sinistro sinistro;
    private List<String> documentosValidados = new ArrayList<>();
    private List<String> documentosRejeitados = new ArrayList<>();
    private Map<String, Object> dadosDetranCache = new HashMap<>();

    /**
     * Construtor padrão para reconstrução do aggregate via Event Sourcing.
     */
    public SinistroAggregate() {
        super();
        registerBusinessRules();
    }

    /**
     * Construtor para criação de novo sinistro.
     *
     * @param id ID único do sinistro
     * @param protocolo Protocolo de identificação do sinistro
     * @param seguradoId ID do segurado envolvido
     * @param veiculoId ID do veículo sinistrado
     * @param apoliceId ID da apólice relacionada
     * @param tipoSinistro Tipo de sinistro (COLISAO, ROUBO, etc)
     * @param ocorrencia Descrição detalhada da ocorrência
     * @param operadorId ID do operador que registrou o sinistro
     * @throws BusinessRuleViolationException se alguma validação falhar
     */
    public SinistroAggregate(String id, ProtocoloSinistro protocolo, String seguradoId,
                            String veiculoId, String apoliceId, TipoSinistro tipoSinistro,
                            OcorrenciaSinistro ocorrencia, String operadorId) {
        super(id);
        registerBusinessRules();

        // Validações antes de criar o evento
        validarProtocolo(protocolo);
        validarSeguradoId(seguradoId);
        validarVeiculoId(veiculoId);
        validarApoliceId(apoliceId);
        validarTipoSinistro(tipoSinistro);
        validarOcorrencia(ocorrencia);
        validarOperadorId(operadorId);

        // Aplicar evento de criação
        applyEvent(new SinistroCriadoEvent(
            id,
            protocolo.getValor(),
            seguradoId,
            veiculoId,
            apoliceId,
            tipoSinistro.name(),
            ocorrencia.getDescricao(),
            operadorId
        ));
    }

    /**
     * Valida os dados complementares e documentos do sinistro.
     *
     * @param dadosComplementares Mapa com dados adicionais coletados
     * @param documentosAnexados Lista de IDs dos documentos anexados
     * @param operadorId ID do operador que realizou a validação
     * @throws BusinessRuleViolationException se o sinistro não estiver em estado válido
     */
    public void validarDados(Map<String, Object> dadosComplementares,
                            List<String> documentosAnexados, String operadorId) {
        // Validar estado atual
        if (sinistro == null) {
            throw new BusinessRuleViolationException(
                "Sinistro não foi criado",
                List.of("O sinistro deve ser criado antes de validar dados")
            );
        }

        if (sinistro.getStatus() != StatusSinistro.NOVO) {
            throw new BusinessRuleViolationException(
                "Sinistro não está em estado NOVO",
                List.of("Apenas sinistros novos podem ter dados validados. Status atual: " + sinistro.getStatus())
            );
        }

        // Validar dados de entrada
        if (dadosComplementares == null) {
            throw new BusinessRuleViolationException(
                "Dados complementares são obrigatórios",
                List.of("Informe os dados complementares do sinistro")
            );
        }

        if (documentosAnexados == null || documentosAnexados.isEmpty()) {
            throw new BusinessRuleViolationException(
                "Documentos são obrigatórios",
                List.of("Anexe pelo menos um documento ao sinistro")
            );
        }

        validarOperadorId(operadorId);

        // Validar transição de status
        if (!SinistroStateMachine.podeTransicionar(sinistro.getStatus(), StatusSinistro.VALIDADO)) {
            throw new BusinessRuleViolationException(
                "Transição de status inválida",
                List.of(String.format("Não é possível transicionar de %s para VALIDADO",
                    sinistro.getStatus()))
            );
        }

        // Aplicar evento
        applyEvent(new SinistroValidadoEvent(
            getId(),
            getId(),
            dadosComplementares,
            documentosAnexados,
            operadorId
        ));
    }

    /**
     * Inicia a análise técnica do sinistro.
     *
     * @param analistaId ID do analista designado
     * @param prioridadeAnalise Prioridade da análise (1 a 5, sendo 5 mais urgente)
     * @throws BusinessRuleViolationException se o sinistro não estiver validado
     */
    public void iniciarAnalise(String analistaId, int prioridadeAnalise) {
        // Validar estado atual
        if (sinistro == null || sinistro.getStatus() != StatusSinistro.VALIDADO) {
            throw new BusinessRuleViolationException(
                "Sinistro não está validado",
                List.of("Apenas sinistros validados podem iniciar análise. Status atual: " +
                    (sinistro != null ? sinistro.getStatus() : "null"))
            );
        }

        // Validar analista
        if (analistaId == null || analistaId.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "Analista é obrigatório",
                List.of("Informe o ID do analista responsável")
            );
        }

        // Validar prioridade
        if (prioridadeAnalise < 1 || prioridadeAnalise > 5) {
            throw new BusinessRuleViolationException(
                "Prioridade inválida",
                List.of("Prioridade deve estar entre 1 (baixa) e 5 (urgente)")
            );
        }

        // Validar transição de status
        if (!SinistroStateMachine.podeTransicionar(sinistro.getStatus(), StatusSinistro.EM_ANALISE)) {
            throw new BusinessRuleViolationException(
                "Transição de status inválida",
                List.of(String.format("Não é possível transicionar de %s para EM_ANALISE",
                    sinistro.getStatus()))
            );
        }

        // Calcular prazo de análise baseado no tipo de sinistro
        TipoSinistro tipo = sinistro.getTipoSinistro();
        Instant prazoAnalise = Instant.now().plusSeconds(tipo.getPrazoProcessamentoDias() * 86400L);

        // Aplicar evento
        applyEvent(new SinistroEmAnaliseEvent(
            getId(),
            getId(),
            analistaId,
            prazoAnalise.toString(),
            String.valueOf(prioridadeAnalise)
        ));
    }

    /**
     * Inicia consulta assíncrona ao Detran.
     *
     * @param placa Placa do veículo
     * @param renavam Número RENAVAM do veículo
     * @throws BusinessRuleViolationException se o sinistro não estiver em análise
     */
    public void iniciarConsultaDetran(String placa, String renavam) {
        // Validar estado atual
        if (sinistro == null || sinistro.getStatus() != StatusSinistro.EM_ANALISE) {
            throw new BusinessRuleViolationException(
                "Sinistro não está em análise",
                List.of("Apenas sinistros em análise podem consultar o Detran. Status atual: " +
                    (sinistro != null ? sinistro.getStatus() : "null"))
            );
        }

        // Validar placa
        if (placa == null || placa.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "Placa é obrigatória",
                List.of("Informe a placa do veículo")
            );
        }

        // Validar RENAVAM
        if (renavam == null || renavam.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "RENAVAM é obrigatório",
                List.of("Informe o RENAVAM do veículo")
            );
        }

        // Validar transição de status
        if (!SinistroStateMachine.podeTransicionar(sinistro.getStatus(), StatusSinistro.AGUARDANDO_DETRAN)) {
            throw new BusinessRuleViolationException(
                "Transição de status inválida",
                List.of(String.format("Não é possível transicionar de %s para AGUARDANDO_DETRAN",
                    sinistro.getStatus()))
            );
        }

        // Aplicar evento (primeira tentativa)
        applyEvent(new ConsultaDetranIniciadaEvent(
            getId(),
            getId(),
            placa.toUpperCase(),
            renavam,
            1
        ));
    }

    /**
     * Registra conclusão bem-sucedida da consulta ao Detran.
     *
     * @param dadosDetran Mapa com dados retornados pelo Detran
     * @throws BusinessRuleViolationException se o sinistro não estiver aguardando Detran
     */
    public void concluirConsultaDetran(Map<String, Object> dadosDetran) {
        // Validar estado atual
        if (sinistro == null || sinistro.getStatus() != StatusSinistro.AGUARDANDO_DETRAN) {
            throw new BusinessRuleViolationException(
                "Sinistro não está aguardando Detran",
                List.of("Status atual: " + (sinistro != null ? sinistro.getStatus() : "null"))
            );
        }

        // Validar dados
        if (dadosDetran == null || dadosDetran.isEmpty()) {
            throw new BusinessRuleViolationException(
                "Dados do Detran são obrigatórios",
                List.of("Informe os dados retornados pela consulta")
            );
        }

        // Validar transição de status
        if (!SinistroStateMachine.podeTransicionar(sinistro.getStatus(), StatusSinistro.DADOS_COLETADOS)) {
            throw new BusinessRuleViolationException(
                "Transição de status inválida",
                List.of(String.format("Não é possível transicionar de %s para DADOS_COLETADOS",
                    sinistro.getStatus()))
            );
        }

        // Aplicar evento
        applyEvent(new ConsultaDetranConcluidaEvent(
            getId(),
            getId(),
            dadosDetran,
            Instant.now().toString()
        ));
    }

    /**
     * Registra falha na consulta ao Detran.
     *
     * @param erro Descrição do erro
     * @param tentativa Número da tentativa que falhou
     * @throws BusinessRuleViolationException se o sinistro não estiver aguardando Detran
     */
    public void falharConsultaDetran(String erro, int tentativa) {
        // Validar estado atual
        if (sinistro == null || sinistro.getStatus() != StatusSinistro.AGUARDANDO_DETRAN) {
            throw new BusinessRuleViolationException(
                "Sinistro não está aguardando Detran",
                List.of("Status atual: " + (sinistro != null ? sinistro.getStatus() : "null"))
            );
        }

        // Validar erro
        if (erro == null || erro.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "Descrição do erro é obrigatória",
                List.of("Informe a descrição do erro ocorrido")
            );
        }

        // Validar tentativa
        if (tentativa < 1) {
            throw new BusinessRuleViolationException(
                "Número de tentativa inválido",
                List.of("Tentativa deve ser maior que zero")
            );
        }

        // Calcular próxima tentativa (backoff exponencial) se não exceder limite
        String proximaTentativa = null;
        final int MAX_TENTATIVAS = 3;

        if (tentativa < MAX_TENTATIVAS) {
            long delaySegundos = (long) Math.pow(2, tentativa) * 60; // 2, 4, 8 minutos
            proximaTentativa = Instant.now().plusSeconds(delaySegundos).toString();
        }

        // Aplicar evento
        applyEvent(new ConsultaDetranFalhadaEvent(
            getId(),
            getId(),
            erro,
            tentativa,
            proximaTentativa
        ));
    }

    /**
     * Aprova o sinistro para pagamento de indenização.
     *
     * @param valorIndenizacao Valor da indenização aprovada
     * @param justificativa Justificativa técnica da aprovação
     * @param analistaId ID do analista que aprovou
     * @param documentosComprobatorios IDs dos documentos que fundamentaram a aprovação
     * @throws BusinessRuleViolationException se o sinistro não puder ser aprovado
     */
    public void aprovar(ValorIndenizacao valorIndenizacao, String justificativa,
                       String analistaId, List<String> documentosComprobatorios) {
        // Validar estado atual
        if (sinistro == null) {
            throw new BusinessRuleViolationException(
                "Sinistro não existe",
                List.of("O sinistro deve ser criado antes de ser aprovado")
            );
        }

        if (!sinistro.podeAprovar()) {
            throw new BusinessRuleViolationException(
                "Sinistro não pode ser aprovado",
                List.of("Status atual: " + sinistro.getStatus() + ". Sinistro deve estar em DADOS_COLETADOS ou EM_ANALISE")
            );
        }

        // Validar valor de indenização
        if (valorIndenizacao == null) {
            throw new BusinessRuleViolationException(
                "Valor de indenização é obrigatório",
                List.of("Informe o valor da indenização a ser paga")
            );
        }

        if (!valorIndenizacao.isValido()) {
            throw new BusinessRuleViolationException(
                "Valor de indenização inválido",
                List.of("O valor líquido da indenização deve ser maior que zero")
            );
        }

        // Validar justificativa
        if (justificativa == null || justificativa.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "Justificativa é obrigatória",
                List.of("Informe a justificativa técnica da aprovação")
            );
        }

        if (justificativa.length() < 50) {
            throw new BusinessRuleViolationException(
                "Justificativa insuficiente",
                List.of("A justificativa deve ter pelo menos 50 caracteres")
            );
        }

        // Validar analista
        if (analistaId == null || analistaId.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "Analista é obrigatório",
                List.of("Informe o ID do analista responsável")
            );
        }

        // Validar documentos
        if (documentosComprobatorios == null || documentosComprobatorios.isEmpty()) {
            throw new BusinessRuleViolationException(
                "Documentos comprobatórios são obrigatórios",
                List.of("Informe os documentos que fundamentaram a aprovação")
            );
        }

        // Validar transição de status
        if (!SinistroStateMachine.podeTransicionar(sinistro.getStatus(), StatusSinistro.APROVADO)) {
            throw new BusinessRuleViolationException(
                "Transição de status inválida",
                List.of(String.format("Não é possível transicionar de %s para APROVADO",
                    sinistro.getStatus()))
            );
        }

        // Aplicar evento
        applyEvent(new SinistroAprovadoEvent(
            getId(),
            getId(),
            valorIndenizacao.getValorLiquido().toString(),
            justificativa,
            analistaId,
            documentosComprobatorios
        ));
    }

    /**
     * Reprova o sinistro com justificativa.
     *
     * @param motivo Motivo principal da reprovação
     * @param justificativa Justificativa técnica detalhada
     * @param analistaId ID do analista que reprovou
     * @param fundamentoLegal Fundamento legal ou contratual
     * @throws BusinessRuleViolationException se o sinistro não puder ser reprovado
     */
    public void reprovar(String motivo, String justificativa,
                        String analistaId, String fundamentoLegal) {
        // Validar estado atual
        if (sinistro == null) {
            throw new BusinessRuleViolationException(
                "Sinistro não existe",
                List.of("O sinistro deve ser criado antes de ser reprovado")
            );
        }

        if (!sinistro.podeReprovar()) {
            throw new BusinessRuleViolationException(
                "Sinistro não pode ser reprovado",
                List.of("Status atual: " + sinistro.getStatus() + ". Sinistro deve estar em DADOS_COLETADOS ou EM_ANALISE")
            );
        }

        // Validar motivo
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "Motivo é obrigatório",
                List.of("Informe o motivo da reprovação")
            );
        }

        // Validar justificativa
        if (justificativa == null || justificativa.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "Justificativa é obrigatória",
                List.of("Informe a justificativa técnica da reprovação")
            );
        }

        if (justificativa.length() < 50) {
            throw new BusinessRuleViolationException(
                "Justificativa insuficiente",
                List.of("A justificativa deve ter pelo menos 50 caracteres")
            );
        }

        // Validar analista
        if (analistaId == null || analistaId.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "Analista é obrigatório",
                List.of("Informe o ID do analista responsável")
            );
        }

        // Validar fundamento legal
        if (fundamentoLegal == null || fundamentoLegal.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "Fundamento legal é obrigatório",
                List.of("Informe o fundamento legal ou contratual da reprovação")
            );
        }

        // Validar transição de status
        if (!SinistroStateMachine.podeTransicionar(sinistro.getStatus(), StatusSinistro.REPROVADO)) {
            throw new BusinessRuleViolationException(
                "Transição de status inválida",
                List.of(String.format("Não é possível transicionar de %s para REPROVADO",
                    sinistro.getStatus()))
            );
        }

        // Aplicar evento
        applyEvent(new SinistroReprovadoEvent(
            getId(),
            getId(),
            motivo,
            justificativa,
            analistaId,
            fundamentoLegal
        ));
    }

    /**
     * Anexa um documento ao sinistro.
     *
     * @param documentoId ID único do documento
     * @param tipoDocumento Tipo/categoria do documento
     * @param operadorId ID do operador que anexou
     * @param observacoes Observações sobre o documento (opcional)
     * @throws BusinessRuleViolationException se o sinistro não existir
     */
    public void anexarDocumento(String documentoId, String tipoDocumento,
                               String operadorId, String observacoes) {
        // Validar estado atual
        if (sinistro == null) {
            throw new BusinessRuleViolationException(
                "Sinistro não existe",
                List.of("O sinistro deve ser criado antes de anexar documentos")
            );
        }

        // Validar documento ID
        if (documentoId == null || documentoId.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "ID do documento é obrigatório",
                List.of("Informe o ID do documento a ser anexado")
            );
        }

        // Validar tipo de documento
        if (tipoDocumento == null || tipoDocumento.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "Tipo do documento é obrigatório",
                List.of("Informe o tipo/categoria do documento")
            );
        }

        // Validar operador
        validarOperadorId(operadorId);

        // Aplicar evento
        applyEvent(new DocumentoAnexadoEvent(
            getId(),
            getId(),
            documentoId,
            tipoDocumento,
            operadorId,
            observacoes
        ));
    }

    /**
     * Valida um documento anexado ao sinistro.
     *
     * @param documentoId ID do documento a ser validado
     * @param validadorId ID do operador que validou
     * @throws BusinessRuleViolationException se o documento não existir ou já estiver validado
     */
    public void validarDocumento(String documentoId, String validadorId) {
        // Validar estado atual
        if (sinistro == null) {
            throw new BusinessRuleViolationException(
                "Sinistro não existe",
                List.of("O sinistro deve ser criado antes de validar documentos")
            );
        }

        // Validar documento ID
        if (documentoId == null || documentoId.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "ID do documento é obrigatório",
                List.of("Informe o ID do documento a ser validado")
            );
        }

        // Verificar se documento já foi validado
        if (documentosValidados.contains(documentoId)) {
            throw new BusinessRuleViolationException(
                "Documento já validado",
                List.of("O documento " + documentoId + " já foi validado anteriormente")
            );
        }

        // Verificar se documento foi rejeitado
        if (documentosRejeitados.contains(documentoId)) {
            throw new BusinessRuleViolationException(
                "Documento foi rejeitado",
                List.of("O documento " + documentoId + " foi rejeitado e não pode ser validado")
            );
        }

        // Validar validador
        if (validadorId == null || validadorId.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "Validador é obrigatório",
                List.of("Informe o ID do validador")
            );
        }

        // Aplicar evento
        applyEvent(new DocumentoValidadoEvent(
            getId(),
            getId(),
            documentoId,
            validadorId,
            Instant.now().toString()
        ));
    }

    /**
     * Rejeita um documento anexado ao sinistro.
     *
     * @param documentoId ID do documento a ser rejeitado
     * @param motivo Motivo da rejeição
     * @param validadorId ID do operador que rejeitou
     * @throws BusinessRuleViolationException se o documento não existir ou já estiver validado
     */
    public void rejeitarDocumento(String documentoId, String motivo, String validadorId) {
        // Validar estado atual
        if (sinistro == null) {
            throw new BusinessRuleViolationException(
                "Sinistro não existe",
                List.of("O sinistro deve ser criado antes de rejeitar documentos")
            );
        }

        // Validar documento ID
        if (documentoId == null || documentoId.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "ID do documento é obrigatório",
                List.of("Informe o ID do documento a ser rejeitado")
            );
        }

        // Verificar se documento já foi validado
        if (documentosValidados.contains(documentoId)) {
            throw new BusinessRuleViolationException(
                "Documento já validado",
                List.of("O documento " + documentoId + " já foi validado e não pode ser rejeitado")
            );
        }

        // Validar motivo
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "Motivo é obrigatório",
                List.of("Informe o motivo da rejeição do documento")
            );
        }

        // Validar validador
        if (validadorId == null || validadorId.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "Validador é obrigatório",
                List.of("Informe o ID do validador")
            );
        }

        // Aplicar evento
        applyEvent(new DocumentoRejeitadoEvent(
            getId(),
            getId(),
            documentoId,
            motivo,
            validadorId
        ));
    }

    // ==================== GETTERS ADICIONAIS ====================

    public StatusSinistro getStatus() {
        return sinistro != null ? sinistro.getStatus() : null;
    }

    public ProtocoloSinistro getProtocolo() {
        return sinistro != null ? sinistro.getProtocolo() : null;
    }

    public TipoSinistro getTipoSinistro() {
        return sinistro != null ? sinistro.getTipoSinistro() : null;
    }

    public boolean isAberto() {
        return sinistro != null && sinistro.getStatus().isAberto();
    }

    public boolean isDentroDoPrazo() {
        return sinistro != null && sinistro.isDentroDoPrazo();
    }

    // ==================== EVENT SOURCING HANDLERS ====================

    @EventSourcingHandler
    protected void on(SinistroCriadoEvent event) {
        this.sinistro = Sinistro.builder()
            .id(event.getAggregateId())
            .protocolo(ProtocoloSinistro.of(event.getProtocolo()))
            .seguradoId(event.getSeguradoId())
            .veiculoId(event.getVeiculoId())
            .apoliceId(event.getApoliceId())
            .tipoSinistro(TipoSinistro.valueOf(event.getTipoSinistro()))
            .status(StatusSinistro.NOVO)
            .ocorrencia(OcorrenciaSinistro.builder()
                .descricao(event.getOcorrencia())
                .dataOcorrencia(event.getTimestamp())
                .build())
            .operadorCriacao(event.getOperadorId())
            .dataCriacao(event.getTimestamp())
            .dataUltimaAtualizacao(event.getTimestamp())
            .build();

        log.debug("Sinistro criado: Protocolo={}, Tipo={}",
            event.getProtocolo(), event.getTipoSinistro());
    }

    @EventSourcingHandler
    protected void on(SinistroValidadoEvent event) {
        this.sinistro = Sinistro.builder()
            .id(sinistro.getId())
            .protocolo(sinistro.getProtocolo())
            .seguradoId(sinistro.getSeguradoId())
            .veiculoId(sinistro.getVeiculoId())
            .apoliceId(sinistro.getApoliceId())
            .tipoSinistro(sinistro.getTipoSinistro())
            .status(StatusSinistro.VALIDADO)
            .ocorrencia(sinistro.getOcorrencia())
            .dadosComplementares(event.getDadosComplementares())
            .documentosAnexados(event.getDocumentosAnexados())
            .operadorCriacao(sinistro.getOperadorCriacao())
            .dataCriacao(sinistro.getDataCriacao())
            .dataUltimaAtualizacao(event.getTimestamp())
            .build();

        log.debug("Sinistro validado: ID={}, Documentos={}",
            getId(), event.getDocumentosAnexados().size());
    }

    @EventSourcingHandler
    protected void on(SinistroEmAnaliseEvent event) {
        this.sinistro = Sinistro.builder()
            .id(sinistro.getId())
            .protocolo(sinistro.getProtocolo())
            .seguradoId(sinistro.getSeguradoId())
            .veiculoId(sinistro.getVeiculoId())
            .apoliceId(sinistro.getApoliceId())
            .tipoSinistro(sinistro.getTipoSinistro())
            .status(StatusSinistro.EM_ANALISE)
            .ocorrencia(sinistro.getOcorrencia())
            .dadosComplementares(sinistro.getDadosComplementares())
            .documentosAnexados(sinistro.getDocumentosAnexados())
            .analistaResponsavel(event.getAnalistaId())
            .dataInicioAnalise(event.getTimestamp())
            .operadorCriacao(sinistro.getOperadorCriacao())
            .dataCriacao(sinistro.getDataCriacao())
            .dataUltimaAtualizacao(event.getTimestamp())
            .build();

        log.debug("Sinistro em análise: ID={}, Analista={}, Prioridade={}",
            getId(), event.getAnalistaId(), event.getPrioridadeAnalise());
    }

    @EventSourcingHandler
    protected void on(ConsultaDetranIniciadaEvent event) {
        this.sinistro = Sinistro.builder()
            .id(sinistro.getId())
            .protocolo(sinistro.getProtocolo())
            .seguradoId(sinistro.getSeguradoId())
            .veiculoId(sinistro.getVeiculoId())
            .apoliceId(sinistro.getApoliceId())
            .tipoSinistro(sinistro.getTipoSinistro())
            .status(StatusSinistro.AGUARDANDO_DETRAN)
            .ocorrencia(sinistro.getOcorrencia())
            .dadosComplementares(sinistro.getDadosComplementares())
            .documentosAnexados(sinistro.getDocumentosAnexados())
            .analistaResponsavel(sinistro.getAnalistaResponsavel())
            .dataInicioAnalise(sinistro.getDataInicioAnalise())
            .operadorCriacao(sinistro.getOperadorCriacao())
            .dataCriacao(sinistro.getDataCriacao())
            .dataUltimaAtualizacao(event.getTimestamp())
            .build();

        log.debug("Consulta Detran iniciada: ID={}, Placa={}, Tentativa={}",
            getId(), event.getPlaca(), event.getTentativa());
    }

    @EventSourcingHandler
    protected void on(ConsultaDetranConcluidaEvent event) {
        // Armazenar dados do Detran no cache
        this.dadosDetranCache = new HashMap<>(event.getDadosDetran());

        this.sinistro = Sinistro.builder()
            .id(sinistro.getId())
            .protocolo(sinistro.getProtocolo())
            .seguradoId(sinistro.getSeguradoId())
            .veiculoId(sinistro.getVeiculoId())
            .apoliceId(sinistro.getApoliceId())
            .tipoSinistro(sinistro.getTipoSinistro())
            .status(StatusSinistro.DADOS_COLETADOS)
            .ocorrencia(sinistro.getOcorrencia())
            .dadosComplementares(sinistro.getDadosComplementares())
            .documentosAnexados(sinistro.getDocumentosAnexados())
            .analistaResponsavel(sinistro.getAnalistaResponsavel())
            .dataInicioAnalise(sinistro.getDataInicioAnalise())
            .processamentoDetran(ProcessamentoDetran.builder()
                .status(DetranConsultaStatus.CONCLUIDA)
                .dadosRetornados(event.getDadosDetran())
                .dataFim(Instant.parse(event.getTimestampConsulta()))
                .tentativas(1)
                .build())
            .operadorCriacao(sinistro.getOperadorCriacao())
            .dataCriacao(sinistro.getDataCriacao())
            .dataUltimaAtualizacao(event.getTimestamp())
            .build();

        log.debug("Consulta Detran concluída: ID={}, Dados recebidos={}",
            getId(), event.getDadosDetran().size());
    }

    @EventSourcingHandler
    protected void on(ConsultaDetranFalhadaEvent event) {
        this.sinistro = Sinistro.builder()
            .id(sinistro.getId())
            .protocolo(sinistro.getProtocolo())
            .seguradoId(sinistro.getSeguradoId())
            .veiculoId(sinistro.getVeiculoId())
            .apoliceId(sinistro.getApoliceId())
            .tipoSinistro(sinistro.getTipoSinistro())
            .status(event.getProximaTentativa() != null ?
                StatusSinistro.AGUARDANDO_DETRAN : StatusSinistro.EM_ANALISE)
            .ocorrencia(sinistro.getOcorrencia())
            .dadosComplementares(sinistro.getDadosComplementares())
            .documentosAnexados(sinistro.getDocumentosAnexados())
            .analistaResponsavel(sinistro.getAnalistaResponsavel())
            .dataInicioAnalise(sinistro.getDataInicioAnalise())
            .processamentoDetran(ProcessamentoDetran.builder()
                .status(DetranConsultaStatus.FALHADA)
                .mensagemErro(event.getErro())
                .tentativas(event.getTentativa())
                .dataFim(event.getTimestamp())
                .build())
            .operadorCriacao(sinistro.getOperadorCriacao())
            .dataCriacao(sinistro.getDataCriacao())
            .dataUltimaAtualizacao(event.getTimestamp())
            .build();

        log.warn("Consulta Detran falhou: ID={}, Erro={}, Tentativa={}",
            getId(), event.getErro(), event.getTentativa());
    }

    @EventSourcingHandler
    protected void on(SinistroAprovadoEvent event) {
        this.sinistro = Sinistro.builder()
            .id(sinistro.getId())
            .protocolo(sinistro.getProtocolo())
            .seguradoId(sinistro.getSeguradoId())
            .veiculoId(sinistro.getVeiculoId())
            .apoliceId(sinistro.getApoliceId())
            .tipoSinistro(sinistro.getTipoSinistro())
            .status(StatusSinistro.APROVADO)
            .ocorrencia(sinistro.getOcorrencia())
            .dadosComplementares(sinistro.getDadosComplementares())
            .documentosAnexados(sinistro.getDocumentosAnexados())
            .analistaResponsavel(event.getAnalistaId())
            .dataInicioAnalise(sinistro.getDataInicioAnalise())
            .dataFimAnalise(event.getTimestamp())
            .processamentoDetran(sinistro.getProcessamentoDetran())
            .valorIndenizacao(ValorIndenizacao.builder()
                .valorBruto(new java.math.BigDecimal(event.getValorIndenizacao()))
                .moeda("BRL")
                .build())
            .justificativa(event.getJustificativa())
            .operadorCriacao(sinistro.getOperadorCriacao())
            .dataCriacao(sinistro.getDataCriacao())
            .dataUltimaAtualizacao(event.getTimestamp())
            .build();

        log.info("Sinistro aprovado: ID={}, Valor={}, Analista={}",
            getId(), event.getValorIndenizacao(), event.getAnalistaId());
    }

    @EventSourcingHandler
    protected void on(SinistroReprovadoEvent event) {
        this.sinistro = Sinistro.builder()
            .id(sinistro.getId())
            .protocolo(sinistro.getProtocolo())
            .seguradoId(sinistro.getSeguradoId())
            .veiculoId(sinistro.getVeiculoId())
            .apoliceId(sinistro.getApoliceId())
            .tipoSinistro(sinistro.getTipoSinistro())
            .status(StatusSinistro.REPROVADO)
            .ocorrencia(sinistro.getOcorrencia())
            .dadosComplementares(sinistro.getDadosComplementares())
            .documentosAnexados(sinistro.getDocumentosAnexados())
            .analistaResponsavel(event.getAnalistaId())
            .dataInicioAnalise(sinistro.getDataInicioAnalise())
            .dataFimAnalise(event.getTimestamp())
            .processamentoDetran(sinistro.getProcessamentoDetran())
            .justificativa(event.getJustificativa())
            .motivoReprovacao(event.getMotivo())
            .fundamentoLegal(event.getFundamentoLegal())
            .operadorCriacao(sinistro.getOperadorCriacao())
            .dataCriacao(sinistro.getDataCriacao())
            .dataUltimaAtualizacao(event.getTimestamp())
            .build();

        log.info("Sinistro reprovado: ID={}, Motivo={}, Analista={}",
            getId(), event.getMotivo(), event.getAnalistaId());
    }

    @EventSourcingHandler
    protected void on(DocumentoAnexadoEvent event) {
        // Adicionar documento à lista se ainda não existir
        List<String> documentos = new ArrayList<>(sinistro.getDocumentosAnexados());
        if (!documentos.contains(event.getDocumentoId())) {
            documentos.add(event.getDocumentoId());
        }

        this.sinistro = Sinistro.builder()
            .id(sinistro.getId())
            .protocolo(sinistro.getProtocolo())
            .seguradoId(sinistro.getSeguradoId())
            .veiculoId(sinistro.getVeiculoId())
            .apoliceId(sinistro.getApoliceId())
            .tipoSinistro(sinistro.getTipoSinistro())
            .status(sinistro.getStatus())
            .ocorrencia(sinistro.getOcorrencia())
            .dadosComplementares(sinistro.getDadosComplementares())
            .documentosAnexados(documentos)
            .analistaResponsavel(sinistro.getAnalistaResponsavel())
            .dataInicioAnalise(sinistro.getDataInicioAnalise())
            .dataFimAnalise(sinistro.getDataFimAnalise())
            .processamentoDetran(sinistro.getProcessamentoDetran())
            .valorIndenizacao(sinistro.getValorIndenizacao())
            .justificativa(sinistro.getJustificativa())
            .motivoReprovacao(sinistro.getMotivoReprovacao())
            .fundamentoLegal(sinistro.getFundamentoLegal())
            .operadorCriacao(sinistro.getOperadorCriacao())
            .dataCriacao(sinistro.getDataCriacao())
            .dataUltimaAtualizacao(event.getTimestamp())
            .build();

        log.debug("Documento anexado: ID={}, DocumentoId={}, Tipo={}",
            getId(), event.getDocumentoId(), event.getTipoDocumento());
    }

    @EventSourcingHandler
    protected void on(DocumentoValidadoEvent event) {
        this.documentosValidados.add(event.getDocumentoId());

        log.debug("Documento validado: ID={}, DocumentoId={}, Validador={}",
            getId(), event.getDocumentoId(), event.getValidadorId());
    }

    @EventSourcingHandler
    protected void on(DocumentoRejeitadoEvent event) {
        this.documentosRejeitados.add(event.getDocumentoId());

        log.warn("Documento rejeitado: ID={}, DocumentoId={}, Motivo={}",
            getId(), event.getDocumentoId(), event.getMotivo());
    }

    // ==================== VALIDAÇÕES ====================

    private void validarProtocolo(ProtocoloSinistro protocolo) {
        if (protocolo == null) {
            throw new BusinessRuleViolationException(
                "Protocolo é obrigatório",
                List.of("Informe o protocolo do sinistro")
            );
        }
    }

    private void validarSeguradoId(String seguradoId) {
        if (seguradoId == null || seguradoId.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "ID do segurado é obrigatório",
                List.of("Informe o ID do segurado envolvido")
            );
        }
    }

    private void validarVeiculoId(String veiculoId) {
        if (veiculoId == null || veiculoId.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "ID do veículo é obrigatório",
                List.of("Informe o ID do veículo sinistrado")
            );
        }
    }

    private void validarApoliceId(String apoliceId) {
        if (apoliceId == null || apoliceId.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "ID da apólice é obrigatório",
                List.of("Informe o ID da apólice relacionada")
            );
        }
    }

    private void validarTipoSinistro(TipoSinistro tipoSinistro) {
        if (tipoSinistro == null) {
            throw new BusinessRuleViolationException(
                "Tipo de sinistro é obrigatório",
                List.of("Informe o tipo do sinistro")
            );
        }
    }

    private void validarOcorrencia(OcorrenciaSinistro ocorrencia) {
        if (ocorrencia == null) {
            throw new BusinessRuleViolationException(
                "Ocorrência é obrigatória",
                List.of("Informe os dados da ocorrência")
            );
        }

        if (!ocorrencia.isValida()) {
            throw new BusinessRuleViolationException(
                "Dados da ocorrência incompletos",
                List.of("A descrição da ocorrência deve ter pelo menos 20 caracteres e incluir local e data")
            );
        }
    }

    private void validarOperadorId(String operadorId) {
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                "ID do operador é obrigatório",
                List.of("Informe o ID do operador")
            );
        }
    }

    // ==================== BUSINESS RULES ====================

    private void registerBusinessRules() {
        // Regra: Sinistro deve ter dados básicos válidos
        registerBusinessRule(new BusinessRule() {
            @Override
            public boolean isValid(AggregateRoot aggregate) {
                if (sinistro == null) return true; // Ainda não foi criado
                return sinistro.getProtocolo() != null &&
                       sinistro.getSeguradoId() != null &&
                       sinistro.getVeiculoId() != null &&
                       sinistro.getApoliceId() != null;
            }

            @Override
            public String getErrorMessage() {
                return "Sinistro deve ter protocolo, segurado, veículo e apólice válidos";
            }
        });

        // Regra: Status final não permite alterações
        registerBusinessRule(new BusinessRule() {
            @Override
            public boolean isValid(AggregateRoot aggregate) {
                if (sinistro == null) return true;
                // Se está arquivado, não deve ter eventos não commitados (exceto o próprio arquivamento)
                return sinistro.getStatus() != StatusSinistro.ARQUIVADO ||
                       !hasUncommittedEvents();
            }

            @Override
            public String getErrorMessage() {
                return "Sinistro arquivado não pode ser modificado";
            }
        });
    }

    // ==================== SNAPSHOT SUPPORT ====================

    @Override
    public Object createSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("sinistro", this.sinistro);
        snapshot.put("documentosValidados", new ArrayList<>(this.documentosValidados));
        snapshot.put("documentosRejeitados", new ArrayList<>(this.documentosRejeitados));
        snapshot.put("dadosDetranCache", new HashMap<>(this.dadosDetranCache));
        return snapshot;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void restoreFromSnapshot(Object snapshotData) {
        if (snapshotData instanceof Map) {
            Map<String, Object> snapshot = (Map<String, Object>) snapshotData;
            this.sinistro = (Sinistro) snapshot.get("sinistro");
            this.documentosValidados = new ArrayList<>((List<String>) snapshot.get("documentosValidados"));
            this.documentosRejeitados = new ArrayList<>((List<String>) snapshot.get("documentosRejeitados"));
            this.dadosDetranCache = new HashMap<>((Map<String, Object>) snapshot.get("dadosDetranCache"));
        }
    }

    @Override
    protected void clearState() {
        this.sinistro = null;
        this.documentosValidados.clear();
        this.documentosRejeitados.clear();
        this.dadosDetranCache.clear();
    }
}
