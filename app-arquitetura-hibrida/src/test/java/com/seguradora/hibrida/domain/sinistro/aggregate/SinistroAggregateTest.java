package com.seguradora.hibrida.domain.sinistro.aggregate;

import com.seguradora.hibrida.aggregate.exception.BusinessRuleViolationException;
import com.seguradora.hibrida.domain.sinistro.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SinistroAggregate Tests")
class SinistroAggregateTest {

    private OcorrenciaSinistro ocorrenciaValida;

    @BeforeEach
    void setUp() {
        ocorrenciaValida = OcorrenciaSinistro.builder()
                .dataOcorrencia(Instant.now().minusSeconds(3600))
                .localOcorrencia(LocalOcorrencia.builder()
                        .logradouro("Rua das Flores")
                        .cidade("São Paulo")
                        .estado("SP")
                        .build())
                .descricao("Colisão frontal com outro veículo em cruzamento sinalizado")
                .build();
    }

    // ==================== CONSTRUTOR PADRÃO ====================

    @Test
    @DisplayName("construtor padrão deve criar aggregate sem estado")
    void defaultConstructorShouldCreateEmptyAggregate() {
        SinistroAggregate aggregate = new SinistroAggregate();

        assertThat(aggregate.getSinistro()).isNull();
        assertThat(aggregate.getDocumentosValidados()).isEmpty();
        assertThat(aggregate.getDocumentosRejeitados()).isEmpty();
        assertThat(aggregate.getDadosDetranCache()).isEmpty();
    }

    // ==================== CONSTRUTOR COMPLETO (criar) ====================

    @Test
    @DisplayName("construtor completo deve criar sinistro com status NOVO")
    void fullConstructorShouldCreateSinistroWithStatusNovo() {
        ProtocoloSinistro protocolo = ProtocoloSinistro.of("2024-000001");

        SinistroAggregate aggregate = new SinistroAggregate(
                "SIN-001",
                protocolo,
                "SEG-001",
                "VEI-001",
                "APO-001",
                TipoSinistro.COLISAO,
                ocorrenciaValida,
                "OP-001"
        );

        assertThat(aggregate.getSinistro()).isNotNull();
        assertThat(aggregate.getSinistro().getStatus()).isEqualTo(StatusSinistro.NOVO);
        assertThat(aggregate.getSinistro().getTipoSinistro()).isEqualTo(TipoSinistro.COLISAO);
        assertThat(aggregate.isAberto()).isTrue();
        assertThat(aggregate.getId()).isEqualTo("SIN-001");
    }

    @Test
    @DisplayName("construtor deve lançar exceção para protocolo nulo")
    void constructorShouldThrowForNullProtocolo() {
        assertThatThrownBy(() -> new SinistroAggregate(
                "SIN-001", null, "SEG-001", "VEI-001", "APO-001",
                TipoSinistro.COLISAO, ocorrenciaValida, "OP-001"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("construtor deve lançar exceção para seguradoId nulo")
    void constructorShouldThrowForNullSeguradoId() {
        ProtocoloSinistro protocolo = ProtocoloSinistro.of("2024-000001");

        assertThatThrownBy(() -> new SinistroAggregate(
                "SIN-001", protocolo, null, "VEI-001", "APO-001",
                TipoSinistro.COLISAO, ocorrenciaValida, "OP-001"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("construtor deve lançar exceção para veiculoId vazio")
    void constructorShouldThrowForEmptyVeiculoId() {
        ProtocoloSinistro protocolo = ProtocoloSinistro.of("2024-000001");

        assertThatThrownBy(() -> new SinistroAggregate(
                "SIN-001", protocolo, "SEG-001", "", "APO-001",
                TipoSinistro.COLISAO, ocorrenciaValida, "OP-001"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("construtor deve lançar exceção para tipoSinistro nulo")
    void constructorShouldThrowForNullTipoSinistro() {
        ProtocoloSinistro protocolo = ProtocoloSinistro.of("2024-000001");

        assertThatThrownBy(() -> new SinistroAggregate(
                "SIN-001", protocolo, "SEG-001", "VEI-001", "APO-001",
                null, ocorrenciaValida, "OP-001"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("construtor deve lançar exceção para ocorrência inválida")
    void constructorShouldThrowForInvalidOcorrencia() {
        ProtocoloSinistro protocolo = ProtocoloSinistro.of("2024-000001");
        OcorrenciaSinistro ocorrenciaInvalida = OcorrenciaSinistro.builder()
                .descricao("curta")
                .build();

        assertThatThrownBy(() -> new SinistroAggregate(
                "SIN-001", protocolo, "SEG-001", "VEI-001", "APO-001",
                TipoSinistro.COLISAO, ocorrenciaInvalida, "OP-001"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    // ==================== validarDados ====================

    @Test
    @DisplayName("validarDados deve mudar status para VALIDADO")
    void validarDadosShouldChangeStatusToValidado() {
        SinistroAggregate aggregate = criarAggregate();

        Map<String, Object> dados = new HashMap<>();
        dados.put("k1", "v1");
        aggregate.validarDados(dados, List.of("DOC-001"), "OP-001");

        assertThat(aggregate.getSinistro().getStatus()).isEqualTo(StatusSinistro.VALIDADO);
    }

    @Test
    @DisplayName("validarDados deve lançar exceção quando status não é NOVO")
    void validarDadosShouldThrowWhenNotNovo() {
        SinistroAggregate aggregate = criarAggregate();
        Map<String, Object> dados = Map.of("k", "v");
        aggregate.validarDados(dados, List.of("DOC-001"), "OP-001");
        // Agora está VALIDADO — tentar validar novamente deve lançar
        assertThatThrownBy(() -> aggregate.validarDados(dados, List.of("DOC-002"), "OP-001"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("validarDados deve lançar exceção para dadosComplementares nulos")
    void validarDadosShouldThrowForNullDados() {
        SinistroAggregate aggregate = criarAggregate();

        assertThatThrownBy(() -> aggregate.validarDados(null, List.of("DOC-001"), "OP-001"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("validarDados deve lançar exceção para documentos vazios")
    void validarDadosShouldThrowForEmptyDocumentos() {
        SinistroAggregate aggregate = criarAggregate();

        assertThatThrownBy(() -> aggregate.validarDados(Map.of("k", "v"), List.of(), "OP-001"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    // ==================== iniciarAnalise ====================

    @Test
    @DisplayName("iniciarAnalise deve mudar status para EM_ANALISE")
    void iniciarAnaliseShouldChangeStatusToEmAnalise() {
        SinistroAggregate aggregate = criarAggregateValidado();

        aggregate.iniciarAnalise("ANALISTA-01", 3);

        assertThat(aggregate.getSinistro().getStatus()).isEqualTo(StatusSinistro.EM_ANALISE);
        assertThat(aggregate.getSinistro().getAnalistaResponsavel()).isEqualTo("ANALISTA-01");
    }

    @Test
    @DisplayName("iniciarAnalise deve lançar exceção quando status não é VALIDADO")
    void iniciarAnaliseShouldThrowWhenNotValidado() {
        SinistroAggregate aggregate = criarAggregate();

        assertThatThrownBy(() -> aggregate.iniciarAnalise("ANALISTA-01", 3))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("iniciarAnalise deve lançar exceção para prioridade inválida (0)")
    void iniciarAnaliseShouldThrowForInvalidPrioridade() {
        SinistroAggregate aggregate = criarAggregateValidado();

        assertThatThrownBy(() -> aggregate.iniciarAnalise("ANALISTA-01", 0))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("iniciarAnalise deve lançar exceção para prioridade inválida (6)")
    void iniciarAnaliseShouldThrowForInvalidPrioridade6() {
        SinistroAggregate aggregate = criarAggregateValidado();

        assertThatThrownBy(() -> aggregate.iniciarAnalise("ANALISTA-01", 6))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("iniciarAnalise deve lançar exceção para analistaId nulo")
    void iniciarAnaliseShouldThrowForNullAnalistaId() {
        SinistroAggregate aggregate = criarAggregateValidado();

        assertThatThrownBy(() -> aggregate.iniciarAnalise(null, 3))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    // ==================== iniciarConsultaDetran ====================

    @Test
    @DisplayName("iniciarConsultaDetran deve mudar status para AGUARDANDO_DETRAN")
    void iniciarConsultaDetranShouldChangeStatus() {
        SinistroAggregate aggregate = criarAggregateEmAnalise();

        aggregate.iniciarConsultaDetran("ABC1234", "12345678901");

        assertThat(aggregate.getSinistro().getStatus()).isEqualTo(StatusSinistro.AGUARDANDO_DETRAN);
    }

    @Test
    @DisplayName("iniciarConsultaDetran deve lançar exceção para placa nula")
    void iniciarConsultaDetranShouldThrowForNullPlaca() {
        SinistroAggregate aggregate = criarAggregateEmAnalise();

        assertThatThrownBy(() -> aggregate.iniciarConsultaDetran(null, "12345678901"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("iniciarConsultaDetran deve lançar exceção quando não está em EM_ANALISE")
    void iniciarConsultaDetranShouldThrowWhenNotEmAnalise() {
        SinistroAggregate aggregate = criarAggregateValidado();

        assertThatThrownBy(() -> aggregate.iniciarConsultaDetran("ABC1234", "12345678901"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    // ==================== concluirConsultaDetran ====================

    @Test
    @DisplayName("concluirConsultaDetran deve mudar status para DADOS_COLETADOS")
    void concluirConsultaDetranShouldChangeStatus() {
        SinistroAggregate aggregate = criarAggregateAguardandoDetran();

        aggregate.concluirConsultaDetran(Map.of("restricao", "false"));

        assertThat(aggregate.getSinistro().getStatus()).isEqualTo(StatusSinistro.DADOS_COLETADOS);
    }

    @Test
    @DisplayName("concluirConsultaDetran deve lançar exceção para dados nulos")
    void concluirConsultaDetranShouldThrowForNullDados() {
        SinistroAggregate aggregate = criarAggregateAguardandoDetran();

        assertThatThrownBy(() -> aggregate.concluirConsultaDetran(null))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("concluirConsultaDetran deve lançar exceção para dados vazios")
    void concluirConsultaDetranShouldThrowForEmptyDados() {
        SinistroAggregate aggregate = criarAggregateAguardandoDetran();

        assertThatThrownBy(() -> aggregate.concluirConsultaDetran(Map.of()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    // ==================== falharConsultaDetran ====================

    @Test
    @DisplayName("falharConsultaDetran deve manter AGUARDANDO_DETRAN quando tentativa < 3")
    void falharConsultaDetranShouldKeepAguardandoForFirstAttempt() {
        SinistroAggregate aggregate = criarAggregateAguardandoDetran();

        aggregate.falharConsultaDetran("Timeout", 1);

        assertThat(aggregate.getSinistro().getStatus()).isEqualTo(StatusSinistro.AGUARDANDO_DETRAN);
    }

    @Test
    @DisplayName("falharConsultaDetran deve voltar para EM_ANALISE quando tentativa = 3")
    void falharConsultaDetranShouldReturnToEmAnaliseForMaxAttempt() {
        SinistroAggregate aggregate = criarAggregateAguardandoDetran();

        aggregate.falharConsultaDetran("Timeout", 3);

        assertThat(aggregate.getSinistro().getStatus()).isEqualTo(StatusSinistro.EM_ANALISE);
    }

    @Test
    @DisplayName("falharConsultaDetran deve lançar exceção para erro nulo")
    void falharConsultaDetranShouldThrowForNullErro() {
        SinistroAggregate aggregate = criarAggregateAguardandoDetran();

        assertThatThrownBy(() -> aggregate.falharConsultaDetran(null, 1))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("falharConsultaDetran deve lançar exceção para tentativa < 1")
    void falharConsultaDetranShouldThrowForInvalidTentativa() {
        SinistroAggregate aggregate = criarAggregateAguardandoDetran();

        assertThatThrownBy(() -> aggregate.falharConsultaDetran("Erro", 0))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    // ==================== aprovar ====================

    @Test
    @DisplayName("aprovar deve lançar exceção quando avaliacaoDanos não está definida")
    void aprovarShouldThrowWhenAvaliacaoDanosNotSet() {
        // criarAggregateEmAnalise() não define avaliacaoDanos, portanto podeAprovar() retorna false
        SinistroAggregate aggregate = criarAggregateEmAnalise();

        ValorIndenizacao valor = ValorIndenizacao.builder()
                .valorBruto(new BigDecimal("10000.00"))
                .build();

        assertThatThrownBy(() -> aggregate.aprovar(valor,
                "Justificativa técnica com pelo menos 50 caracteres detalhando a aprovação do sinistro",
                "ANALISTA-01",
                List.of("DOC-001")))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("aprovar deve lançar exceção quando sinistro não pode ser aprovado")
    void aprovarShouldThrowWhenCannotAprovar() {
        SinistroAggregate aggregate = criarAggregate(); // status NOVO

        ValorIndenizacao valor = ValorIndenizacao.builder()
                .valorBruto(new BigDecimal("10000.00"))
                .build();

        assertThatThrownBy(() -> aggregate.aprovar(valor, "Justificativa longa o suficiente para aprovação do sinistro aqui",
                "ANALISTA-01", List.of("DOC-001")))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("aprovar deve lançar exceção para justificativa muito curta (< 50 chars)")
    void aprovarShouldThrowForShortJustificativa() {
        SinistroAggregate aggregate = criarAggregateEmAnalise();

        ValorIndenizacao valor = ValorIndenizacao.builder()
                .valorBruto(new BigDecimal("10000.00"))
                .build();

        assertThatThrownBy(() -> aggregate.aprovar(valor, "Curta demais.", "ANALISTA-01", List.of("DOC-001")))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("aprovar deve lançar exceção para documentos vazios")
    void aprovarShouldThrowForEmptyDocumentos() {
        SinistroAggregate aggregate = criarAggregateEmAnalise();

        ValorIndenizacao valor = ValorIndenizacao.builder()
                .valorBruto(new BigDecimal("10000.00"))
                .build();

        assertThatThrownBy(() -> aggregate.aprovar(valor,
                "Justificativa técnica com pelo menos 50 caracteres detalhando a aprovação do sinistro",
                "ANALISTA-01", List.of()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    // ==================== reprovar ====================

    @Test
    @DisplayName("reprovar deve mudar status para REPROVADO")
    void reprovarShouldChangeStatusToReprovado() {
        SinistroAggregate aggregate = criarAggregateEmAnalise();

        aggregate.reprovar(
                "FORA_COBERTURA",
                "Justificativa técnica com pelo menos 50 caracteres para reprovação do sinistro",
                "ANALISTA-01",
                "Art. 5 do contrato"
        );

        assertThat(aggregate.getSinistro().getStatus()).isEqualTo(StatusSinistro.REPROVADO);
    }

    @Test
    @DisplayName("reprovar deve lançar exceção quando sinistro não pode ser reprovado")
    void reprovarShouldThrowWhenCannotReprovar() {
        SinistroAggregate aggregate = criarAggregate(); // status NOVO

        assertThatThrownBy(() -> aggregate.reprovar("FORA_COBERTURA",
                "Justificativa técnica com pelo menos 50 caracteres para reprovação do sinistro",
                "ANALISTA-01", "Art. 5"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("reprovar deve lançar exceção para justificativa curta")
    void reprovarShouldThrowForShortJustificativa() {
        SinistroAggregate aggregate = criarAggregateEmAnalise();

        assertThatThrownBy(() -> aggregate.reprovar("FORA_COBERTURA", "Curta", "ANALISTA-01", "Art. 5"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("reprovar deve lançar exceção para fundamentoLegal nulo")
    void reprovarShouldThrowForNullFundamentoLegal() {
        SinistroAggregate aggregate = criarAggregateEmAnalise();

        assertThatThrownBy(() -> aggregate.reprovar("FORA_COBERTURA",
                "Justificativa técnica com pelo menos 50 caracteres para reprovação do sinistro",
                "ANALISTA-01", null))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    // ==================== anexarDocumento ====================

    @Test
    @DisplayName("anexarDocumento deve adicionar documento à lista")
    void anexarDocumentoShouldAddToList() {
        SinistroAggregate aggregate = criarAggregate();

        int tamanhoAntes = aggregate.getSinistro().getDocumentosAnexados().size();
        aggregate.anexarDocumento("DOC-001", "FOTO_VEICULO", "OP-001", null);

        assertThat(aggregate.getSinistro().getDocumentosAnexados()).hasSize(tamanhoAntes + 1);
        assertThat(aggregate.getSinistro().getDocumentosAnexados()).contains("DOC-001");
    }

    @Test
    @DisplayName("anexarDocumento deve lançar exceção para documentoId nulo")
    void anexarDocumentoShouldThrowForNullDocumentoId() {
        SinistroAggregate aggregate = criarAggregate();

        assertThatThrownBy(() -> aggregate.anexarDocumento(null, "FOTO_VEICULO", "OP-001", null))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("anexarDocumento deve lançar exceção para tipoDocumento vazio")
    void anexarDocumentoShouldThrowForEmptyTipoDocumento() {
        SinistroAggregate aggregate = criarAggregate();

        assertThatThrownBy(() -> aggregate.anexarDocumento("DOC-001", "", "OP-001", null))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    // ==================== validarDocumento ====================

    @Test
    @DisplayName("validarDocumento deve adicionar a documentosValidados")
    void validarDocumentoShouldAddToValidados() {
        SinistroAggregate aggregate = criarAggregate();
        aggregate.anexarDocumento("DOC-001", "FOTO_VEICULO", "OP-001", null);

        aggregate.validarDocumento("DOC-001", "VALID-01");

        assertThat(aggregate.getDocumentosValidados()).contains("DOC-001");
    }

    @Test
    @DisplayName("validarDocumento deve lançar exceção se já validado")
    void validarDocumentoShouldThrowIfAlreadyValidated() {
        SinistroAggregate aggregate = criarAggregate();
        aggregate.anexarDocumento("DOC-001", "FOTO_VEICULO", "OP-001", null);
        aggregate.validarDocumento("DOC-001", "VALID-01");

        assertThatThrownBy(() -> aggregate.validarDocumento("DOC-001", "VALID-01"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    // ==================== rejeitarDocumento ====================

    @Test
    @DisplayName("rejeitarDocumento deve adicionar a documentosRejeitados")
    void rejeitarDocumentoShouldAddToRejeitados() {
        SinistroAggregate aggregate = criarAggregate();
        aggregate.anexarDocumento("DOC-001", "FOTO_VEICULO", "OP-001", null);

        aggregate.rejeitarDocumento("DOC-001", "Ilegível", "VALID-01");

        assertThat(aggregate.getDocumentosRejeitados()).contains("DOC-001");
    }

    @Test
    @DisplayName("rejeitarDocumento deve lançar exceção se já validado")
    void rejeitarDocumentoShouldThrowIfAlreadyValidated() {
        SinistroAggregate aggregate = criarAggregate();
        aggregate.anexarDocumento("DOC-001", "FOTO_VEICULO", "OP-001", null);
        aggregate.validarDocumento("DOC-001", "VALID-01");

        assertThatThrownBy(() -> aggregate.rejeitarDocumento("DOC-001", "Motivo", "VALID-01"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @DisplayName("rejeitarDocumento deve lançar exceção para motivo nulo")
    void rejeitarDocumentoShouldThrowForNullMotivo() {
        SinistroAggregate aggregate = criarAggregate();
        aggregate.anexarDocumento("DOC-001", "FOTO_VEICULO", "OP-001", null);

        assertThatThrownBy(() -> aggregate.rejeitarDocumento("DOC-001", null, "VALID-01"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    // ==================== getters derivados ====================

    @Test
    @DisplayName("getStatus deve retornar null quando sinistro é null")
    void getStatusShouldReturnNullWhenNoSinistro() {
        SinistroAggregate aggregate = new SinistroAggregate();
        assertThat(aggregate.getStatus()).isNull();
    }

    @Test
    @DisplayName("getProtocolo deve retornar protocolo do sinistro")
    void getProtocoloShouldReturnProtocolo() {
        SinistroAggregate aggregate = criarAggregate();
        assertThat(aggregate.getProtocolo()).isNotNull();
        assertThat(aggregate.getProtocolo().getValor()).isEqualTo("2024-000001");
    }

    @Test
    @DisplayName("isAberto deve retornar false quando sinistro é null")
    void isabertoShouldReturnFalseWhenNoSinistro() {
        SinistroAggregate aggregate = new SinistroAggregate();
        assertThat(aggregate.isAberto()).isFalse();
    }

    // ==================== HELPERS ====================

    private SinistroAggregate criarAggregate() {
        return new SinistroAggregate(
                "SIN-001",
                ProtocoloSinistro.of("2024-000001"),
                "SEG-001",
                "VEI-001",
                "APO-001",
                TipoSinistro.COLISAO,
                ocorrenciaValida,
                "OP-001"
        );
    }

    private SinistroAggregate criarAggregateValidado() {
        SinistroAggregate aggregate = criarAggregate();
        aggregate.validarDados(Map.of("k", "v"), List.of("DOC-001"), "OP-001");
        return aggregate;
    }

    private SinistroAggregate criarAggregateEmAnalise() {
        SinistroAggregate aggregate = criarAggregateValidado();
        aggregate.iniciarAnalise("ANALISTA-01", 3);
        return aggregate;
    }

    private SinistroAggregate criarAggregateAguardandoDetran() {
        SinistroAggregate aggregate = criarAggregateEmAnalise();
        aggregate.iniciarConsultaDetran("ABC1234", "12345678901");
        return aggregate;
    }
}
