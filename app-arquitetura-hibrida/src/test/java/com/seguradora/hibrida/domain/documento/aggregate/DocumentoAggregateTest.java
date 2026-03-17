package com.seguradora.hibrida.domain.documento.aggregate;

import com.seguradora.hibrida.domain.documento.event.*;
import com.seguradora.hibrida.domain.documento.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários completos para {@link DocumentoAggregate}.
 *
 * <p>Testa o ciclo de vida completo de documentos incluindo:
 * <ul>
 *   <li>Criação e validações</li>
 *   <li>Atualizações e versionamento</li>
 *   <li>Validação e rejeição</li>
 *   <li>Assinaturas digitais</li>
 *   <li>Geração de eventos</li>
 *   <li>Transições de estado</li>
 *   <li>Invariantes do aggregate</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("DocumentoAggregate - Testes Unitários")
class DocumentoAggregateTest {

    // ==================== CONSTANTES DE TESTE ====================

    private static final String DOC_ID = "DOC-001";
    private static final String SINISTRO_ID = "SIN-001";
    private static final String OPERADOR_ID = "OP-001";
    private static final String VALIDADOR_ID = "VAL-001";
    private static final String VALIDADOR_NOME = "João Validador";
    private static final String NOME_ARQUIVO = "documento_teste.pdf";
    private static final String FORMATO_PDF = "application/pdf";
    private static final String CONTEUDO_PATH = "/storage/docs/doc-001.pdf";

    // ==================== NESTED: CRIAÇÃO DO AGGREGATE ====================

    @Nested
    @DisplayName("Criação do Aggregate")
    class CriacaoAggregate {

        @Test
        @DisplayName("Deve criar documento com dados válidos")
        void deveCriarDocumentoComDadosValidos() {
            // Arrange
            byte[] conteudo = criarConteudoValido();
            TipoDocumento tipo = TipoDocumento.BOLETIM_OCORRENCIA;

            // Act
            DocumentoAggregate aggregate = new DocumentoAggregate(
                    DOC_ID, NOME_ARQUIVO, tipo, conteudo,
                    FORMATO_PDF, CONTEUDO_PATH, SINISTRO_ID, OPERADOR_ID
            );

            // Assert
            assertThat(aggregate.getId()).isEqualTo(DOC_ID);
            assertThat(aggregate.getDocumentoId()).isEqualTo(DOC_ID);
            assertThat(aggregate.getDocumento()).isNotNull();
            assertThat(aggregate.getDocumento().getNome()).isEqualTo(NOME_ARQUIVO);
            assertThat(aggregate.getDocumento().getTipo()).isEqualTo(tipo);
            assertThat(aggregate.getStatus()).isEqualTo(StatusDocumento.PENDENTE);
            assertThat(aggregate.getDocumento().getVersao().getNumero()).isEqualTo(1);
            assertThat(aggregate.getDocumento().getTamanho()).isEqualTo(conteudo.length);
            assertThat(aggregate.getUncommittedEvents()).hasSize(1);
            assertThat(aggregate.getUncommittedEvents().get(0)).isInstanceOf(DocumentoCriadoEvent.class);
        }

        @Test
        @DisplayName("Deve gerar hash correto ao criar documento")
        void deveGerarHashCorretoAoCriarDocumento() {
            // Arrange
            byte[] conteudo = criarConteudoValido();
            HashDocumento hashEsperado = HashDocumento.calcular(conteudo);

            // Act
            DocumentoAggregate aggregate = criarDocumentoValido();

            // Assert
            assertThat(aggregate.getDocumento().getHashValor()).isEqualTo(hashEsperado.getValor());
        }

        @Test
        @DisplayName("Deve criar versão inicial 1 ao criar documento")
        void deveCriarVersaoInicial1AoCriarDocumento() {
            // Act
            DocumentoAggregate aggregate = criarDocumentoValido();

            // Assert
            VersaoDocumento versao = aggregate.getDocumento().getVersao();
            assertThat(versao.getNumero()).isEqualTo(1);
            assertThat(versao.isVersaoInicial()).isTrue();
            assertThat(versao.getDescricaoAlteracao()).isEqualTo("Versão inicial");
        }

        @Test
        @DisplayName("Deve falhar ao criar documento com nome nulo")
        void deveFalharAoCriarDocumentoComNomeNulo() {
            // Arrange
            byte[] conteudo = criarConteudoValido();

            // Act & Assert
            assertThatThrownBy(() -> new DocumentoAggregate(
                    DOC_ID, null, TipoDocumento.BOLETIM_OCORRENCIA, conteudo,
                    FORMATO_PDF, CONTEUDO_PATH, SINISTRO_ID, OPERADOR_ID
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Nome do documento não pode ser nulo");
        }

        @Test
        @DisplayName("Deve falhar ao criar documento com nome vazio")
        void deveFalharAoCriarDocumentoComNomeVazio() {
            // Arrange
            byte[] conteudo = criarConteudoValido();

            // Act & Assert
            assertThatThrownBy(() -> new DocumentoAggregate(
                    DOC_ID, "  ", TipoDocumento.BOLETIM_OCORRENCIA, conteudo,
                    FORMATO_PDF, CONTEUDO_PATH, SINISTRO_ID, OPERADOR_ID
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nome do documento não pode ser vazio");
        }

        @Test
        @DisplayName("Deve falhar ao criar documento com tipo nulo")
        void deveFalharAoCriarDocumentoComTipoNulo() {
            // Arrange
            byte[] conteudo = criarConteudoValido();

            // Act & Assert
            assertThatThrownBy(() -> new DocumentoAggregate(
                    DOC_ID, NOME_ARQUIVO, null, conteudo,
                    FORMATO_PDF, CONTEUDO_PATH, SINISTRO_ID, OPERADOR_ID
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Tipo do documento não pode ser nulo");
        }

        @Test
        @DisplayName("Deve falhar ao criar documento com conteúdo nulo")
        void deveFalharAoCriarDocumentoComConteudoNulo() {
            // Act & Assert
            assertThatThrownBy(() -> new DocumentoAggregate(
                    DOC_ID, NOME_ARQUIVO, TipoDocumento.BOLETIM_OCORRENCIA, null,
                    FORMATO_PDF, CONTEUDO_PATH, SINISTRO_ID, OPERADOR_ID
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Conteúdo não pode ser nulo");
        }

        @Test
        @DisplayName("Deve falhar ao criar documento com conteúdo vazio")
        void deveFalharAoCriarDocumentoComConteudoVazio() {
            // Act & Assert
            assertThatThrownBy(() -> new DocumentoAggregate(
                    DOC_ID, NOME_ARQUIVO, TipoDocumento.BOLETIM_OCORRENCIA, new byte[0],
                    FORMATO_PDF, CONTEUDO_PATH, SINISTRO_ID, OPERADOR_ID
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Conteúdo do documento não pode ser vazio");
        }

        @Test
        @DisplayName("Deve falhar ao criar documento com formato inválido")
        void deveFalharAoCriarDocumentoComFormatoInvalido() {
            // Arrange
            byte[] conteudo = criarConteudoValido();
            String formatoInvalido = "application/zip";

            // Act & Assert
            assertThatThrownBy(() -> new DocumentoAggregate(
                    DOC_ID, NOME_ARQUIVO, TipoDocumento.BOLETIM_OCORRENCIA, conteudo,
                    formatoInvalido, CONTEUDO_PATH, SINISTRO_ID, OPERADOR_ID
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Formato")
                    .hasMessageContaining("não é aceito");
        }

        @Test
        @DisplayName("Deve falhar ao criar documento com tamanho excedente")
        void deveFalharAoCriarDocumentoComTamanhoExcedente() {
            // Arrange - Criar conteúdo maior que 10MB (limite do BOLETIM_OCORRENCIA)
            int tamanhoExcedente = 11 * 1024 * 1024; // 11 MB
            byte[] conteudoGrande = new byte[tamanhoExcedente];

            // Act & Assert
            assertThatThrownBy(() -> new DocumentoAggregate(
                    DOC_ID, NOME_ARQUIVO, TipoDocumento.BOLETIM_OCORRENCIA, conteudoGrande,
                    FORMATO_PDF, CONTEUDO_PATH, SINISTRO_ID, OPERADOR_ID
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("excede o limite");
        }

        @Test
        @DisplayName("Deve falhar ao criar documento sem sinistro ID")
        void deveFalharAoCriarDocumentoSemSinistroId() {
            // Arrange
            byte[] conteudo = criarConteudoValido();

            // Act & Assert
            assertThatThrownBy(() -> new DocumentoAggregate(
                    DOC_ID, NOME_ARQUIVO, TipoDocumento.BOLETIM_OCORRENCIA, conteudo,
                    FORMATO_PDF, CONTEUDO_PATH, null, OPERADOR_ID
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Sinistro ID não pode ser nulo");
        }

        @Test
        @DisplayName("Deve falhar ao criar documento sem operador ID")
        void deveFalharAoCriarDocumentoSemOperadorId() {
            // Arrange
            byte[] conteudo = criarConteudoValido();

            // Act & Assert
            assertThatThrownBy(() -> new DocumentoAggregate(
                    DOC_ID, NOME_ARQUIVO, TipoDocumento.BOLETIM_OCORRENCIA, conteudo,
                    FORMATO_PDF, CONTEUDO_PATH, SINISTRO_ID, null
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Operador ID não pode ser nulo");
        }
    }

    // ==================== NESTED: COMANDOS DE NEGÓCIO ====================

    @Nested
    @DisplayName("Comandos de Negócio")
    class ComandosNegocio {

        @Test
        @DisplayName("Deve atualizar documento pendente com novo conteúdo")
        void deveAtualizarDocumentoPendenteComNovoConteudo() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            aggregate.markEventsAsCommitted();
            byte[] novoConteudo = "Novo conteúdo do documento PDF".getBytes();
            String motivo = "Correção de informações";
            String novoPath = "/storage/docs/doc-001-v2.pdf";

            // Act
            aggregate.atualizar(novoConteudo, motivo, novoPath, OPERADOR_ID);

            // Assert
            assertThat(aggregate.getDocumento().getVersao().getNumero()).isEqualTo(2);
            assertThat(aggregate.getDocumento().getTamanho()).isEqualTo(novoConteudo.length);
            assertThat(aggregate.getDocumento().getConteudoPath()).isEqualTo(novoPath);
            assertThat(aggregate.getUncommittedEvents()).hasSize(1);
            assertThat(aggregate.getUncommittedEvents().get(0)).isInstanceOf(DocumentoAtualizadoEvent.class);
        }

        @Test
        @DisplayName("Deve validar documento pendente")
        void deveValidarDocumentoPendente() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            aggregate.markEventsAsCommitted();

            // Act
            aggregate.validar(VALIDADOR_ID, VALIDADOR_NOME, "Documento aprovado");

            // Assert
            assertThat(aggregate.getStatus()).isEqualTo(StatusDocumento.VALIDADO);
            assertThat(aggregate.getUncommittedEvents()).hasSize(1);
            assertThat(aggregate.getUncommittedEvents().get(0)).isInstanceOf(DocumentoValidadoEvent.class);
        }

        @Test
        @DisplayName("Deve rejeitar documento pendente")
        void deveRejeitarDocumentoPendente() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            aggregate.markEventsAsCommitted();
            String motivo = "Documento ilegível";
            List<String> problemas = Arrays.asList("Qualidade ruim", "Informações incompletas");

            // Act
            aggregate.rejeitar(motivo, problemas, VALIDADOR_ID, VALIDADOR_NOME, "Reenviar com melhor qualidade");

            // Assert
            assertThat(aggregate.getStatus()).isEqualTo(StatusDocumento.REJEITADO);
            assertThat(aggregate.getUncommittedEvents()).hasSize(1);
            assertThat(aggregate.getUncommittedEvents().get(0)).isInstanceOf(DocumentoRejeitadoEvent.class);
        }

        @Test
        @DisplayName("Deve adicionar assinatura digital ao documento")
        void deveAdicionarAssinaturaDigitalAoDocumento() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            aggregate.markEventsAsCommitted();
            AssinaturaDigital assinatura = criarAssinaturaValida();

            // Act
            aggregate.assinarDigitalmente(assinatura);

            // Assert
            assertThat(aggregate.getDocumento().getAssinaturas()).isNotEmpty();
            assertThat(aggregate.getDocumento().possuiAssinaturasValidas()).isTrue();
            assertThat(aggregate.getUncommittedEvents()).hasSize(1);
            assertThat(aggregate.getUncommittedEvents().get(0)).isInstanceOf(DocumentoAssinadoEvent.class);
        }

        @Test
        @DisplayName("Deve falhar ao atualizar documento não inicializado")
        void deveFalharAoAtualizarDocumentoNaoInicializado() {
            // Arrange
            DocumentoAggregate aggregate = new DocumentoAggregate();
            byte[] novoConteudo = criarConteudoValido();

            // Act & Assert
            assertThatThrownBy(() -> aggregate.atualizar(novoConteudo, "Motivo", CONTEUDO_PATH, OPERADOR_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Documento não inicializado");
        }

        @Test
        @DisplayName("Deve falhar ao atualizar documento validado")
        void deveFalharAoAtualizarDocumentoValidado() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            aggregate.validar(VALIDADOR_ID, VALIDADOR_NOME, null);
            byte[] novoConteudo = criarConteudoValido();

            // Act & Assert
            assertThatThrownBy(() -> aggregate.atualizar(novoConteudo, "Motivo", CONTEUDO_PATH, OPERADOR_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("não pode ser atualizado");
        }

        @Test
        @DisplayName("Deve falhar ao atualizar com conteúdo nulo")
        void deveFalharAoAtualizarComConteudoNulo() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();

            // Act & Assert
            assertThatThrownBy(() -> aggregate.atualizar(null, "Motivo", CONTEUDO_PATH, OPERADOR_ID))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Novo conteúdo não pode ser nulo");
        }

        @Test
        @DisplayName("Deve falhar ao atualizar sem motivo")
        void deveFalharAoAtualizarSemMotivo() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            byte[] novoConteudo = criarConteudoValido();

            // Act & Assert
            assertThatThrownBy(() -> aggregate.atualizar(novoConteudo, null, CONTEUDO_PATH, OPERADOR_ID))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Motivo da atualização é obrigatório");
        }

        @Test
        @DisplayName("Deve falhar ao atualizar com motivo vazio")
        void deveFalharAoAtualizarComMotivoVazio() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            byte[] novoConteudo = criarConteudoValido();

            // Act & Assert
            assertThatThrownBy(() -> aggregate.atualizar(novoConteudo, "  ", CONTEUDO_PATH, OPERADOR_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Motivo da atualização não pode ser vazio");
        }

        @Test
        @DisplayName("Deve falhar ao atualizar com mesmo conteúdo")
        void deveFalharAoAtualizarComMesmoConteudo() {
            // Arrange
            byte[] conteudo = criarConteudoValido();
            DocumentoAggregate aggregate = criarDocumentoValido();

            // Act & Assert
            assertThatThrownBy(() -> aggregate.atualizar(conteudo, "Motivo", CONTEUDO_PATH, OPERADOR_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Conteúdo não foi modificado");
        }

        @Test
        @DisplayName("Deve falhar ao validar documento não inicializado")
        void deveFalharAoValidarDocumentoNaoInicializado() {
            // Arrange
            DocumentoAggregate aggregate = new DocumentoAggregate();

            // Act & Assert
            assertThatThrownBy(() -> aggregate.validar(VALIDADOR_ID, VALIDADOR_NOME, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Documento não inicializado");
        }

        @Test
        @DisplayName("Deve falhar ao validar documento validado")
        void deveFalharAoValidarDocumentoValidado() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            aggregate.validar(VALIDADOR_ID, VALIDADOR_NOME, null);

            // Act & Assert
            assertThatThrownBy(() -> aggregate.validar(VALIDADOR_ID, VALIDADOR_NOME, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("não pode ser validado");
        }

        @Test
        @DisplayName("Deve falhar ao rejeitar documento não inicializado")
        void deveFalharAoRejeitarDocumentoNaoInicializado() {
            // Arrange
            DocumentoAggregate aggregate = new DocumentoAggregate();

            // Act & Assert
            assertThatThrownBy(() -> aggregate.rejeitar("Motivo", null, VALIDADOR_ID, VALIDADOR_NOME, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Documento não inicializado");
        }

        @Test
        @DisplayName("Deve falhar ao rejeitar documento rejeitado")
        void deveFalharAoRejeitarDocumentoRejeitado() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            aggregate.rejeitar("Motivo 1", null, VALIDADOR_ID, VALIDADOR_NOME, null);

            // Act & Assert
            assertThatThrownBy(() -> aggregate.rejeitar("Motivo 2", null, VALIDADOR_ID, VALIDADOR_NOME, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("não pode ser rejeitado");
        }

        @Test
        @DisplayName("Deve falhar ao rejeitar sem motivo")
        void deveFalharAoRejeitarSemMotivo() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();

            // Act & Assert
            assertThatThrownBy(() -> aggregate.rejeitar(null, null, VALIDADOR_ID, VALIDADOR_NOME, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Motivo da rejeição é obrigatório");
        }

        @Test
        @DisplayName("Deve falhar ao rejeitar com motivo vazio")
        void deveFalharAoRejeitarComMotivoVazio() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();

            // Act & Assert
            assertThatThrownBy(() -> aggregate.rejeitar("  ", null, VALIDADOR_ID, VALIDADOR_NOME, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Motivo da rejeição não pode ser vazio");
        }

        @Test
        @DisplayName("Deve falhar ao assinar documento não inicializado")
        void deveFalharAoAssinarDocumentoNaoInicializado() {
            // Arrange
            DocumentoAggregate aggregate = new DocumentoAggregate();
            AssinaturaDigital assinatura = criarAssinaturaValida();

            // Act & Assert
            assertThatThrownBy(() -> aggregate.assinarDigitalmente(assinatura))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Documento não inicializado");
        }

        @Test
        @DisplayName("Deve falhar ao assinar com assinatura nula")
        void deveFalharAoAssinarComAssinaturaNula() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();

            // Act & Assert
            assertThatThrownBy(() -> aggregate.assinarDigitalmente(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Assinatura não pode ser nula");
        }

        @Test
        @DisplayName("Deve falhar ao assinar com assinatura inválida")
        void deveFalharAoAssinarComAssinaturaInvalida() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();

            // Act & Assert - Tentar criar assinatura com certificado expirado
            assertThatThrownBy(() -> {
                AssinaturaDigital assinaturaExpirada = AssinaturaDigital.digital(
                        "RSA",
                        "certificado-base64",
                        "João Silva",
                        "12345678909",
                        LocalDate.now().minusYears(2),
                        LocalDate.now().minusYears(1) // Expirada
                );
            })
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Certificado já expirado");
        }
    }

    // ==================== NESTED: GERAÇÃO DE EVENTOS ====================

    @Nested
    @DisplayName("Geração de Eventos")
    class GeracaoEventos {

        @Test
        @DisplayName("Deve gerar DocumentoCriadoEvent ao criar documento")
        void deveGerarDocumentoCriadoEventAoCriarDocumento() {
            // Act
            DocumentoAggregate aggregate = criarDocumentoValido();

            // Assert
            assertThat(aggregate.getUncommittedEvents()).hasSize(1);

            DocumentoCriadoEvent event = (DocumentoCriadoEvent) aggregate.getUncommittedEvents().get(0);
            assertThat(event.getDocumentoId()).isEqualTo(DOC_ID);
            assertThat(event.getNome()).isNotBlank();
            assertThat(event.getTipo()).isEqualTo(TipoDocumento.FOTO_DANOS);
            assertThat(event.getFormato()).isEqualTo("image/jpeg");
            assertThat(event.getSinistroId()).isEqualTo(SINISTRO_ID);
            assertThat(event.getOperadorId()).isEqualTo(OPERADOR_ID);
            assertThat(event.getHash()).isNotNull();
        }

        @Test
        @DisplayName("Deve gerar DocumentoAtualizadoEvent ao atualizar documento")
        void deveGerarDocumentoAtualizadoEventAoAtualizarDocumento() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            aggregate.markEventsAsCommitted();
            byte[] novoConteudo = "Novo conteúdo".getBytes();

            // Act
            aggregate.atualizar(novoConteudo, "Motivo teste", "/novo/path", OPERADOR_ID);

            // Assert
            assertThat(aggregate.getUncommittedEvents()).hasSize(1);

            DocumentoAtualizadoEvent event = (DocumentoAtualizadoEvent) aggregate.getUncommittedEvents().get(0);
            assertThat(event.getDocumentoId()).isEqualTo(DOC_ID);
            assertThat(event.getNovaVersao()).isEqualTo(2);
            assertThat(event.getMotivo()).isEqualTo("Motivo teste");
            assertThat(event.getHashNovo()).isNotNull();
            assertThat(event.getOperadorId()).isEqualTo(OPERADOR_ID);
        }

        @Test
        @DisplayName("Deve gerar DocumentoValidadoEvent ao validar documento")
        void deveGerarDocumentoValidadoEventAoValidarDocumento() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            aggregate.markEventsAsCommitted();

            // Act
            aggregate.validar(VALIDADOR_ID, VALIDADOR_NOME, "Tudo OK");

            // Assert
            assertThat(aggregate.getUncommittedEvents()).hasSize(1);

            DocumentoValidadoEvent event = (DocumentoValidadoEvent) aggregate.getUncommittedEvents().get(0);
            assertThat(event.getDocumentoId()).isEqualTo(DOC_ID);
            assertThat(event.getValidadorId()).isEqualTo(VALIDADOR_ID);
            assertThat(event.getValidadorNome()).isEqualTo(VALIDADOR_NOME);
            assertThat(event.getObservacoes()).isEqualTo("Tudo OK");
        }

        @Test
        @DisplayName("Deve gerar DocumentoRejeitadoEvent ao rejeitar documento")
        void deveGerarDocumentoRejeitadoEventAoRejeitarDocumento() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            aggregate.markEventsAsCommitted();
            List<String> problemas = Arrays.asList("Problema 1", "Problema 2");

            // Act
            aggregate.rejeitar("Motivo rejeição", problemas, VALIDADOR_ID, VALIDADOR_NOME, "Ações");

            // Assert
            assertThat(aggregate.getUncommittedEvents()).hasSize(1);

            DocumentoRejeitadoEvent event = (DocumentoRejeitadoEvent) aggregate.getUncommittedEvents().get(0);
            assertThat(event.getDocumentoId()).isEqualTo(DOC_ID);
            assertThat(event.getMotivo()).isEqualTo("Motivo rejeição");
            assertThat(event.getProblemasIdentificados()).hasSize(2);
            assertThat(event.getValidadorId()).isEqualTo(VALIDADOR_ID);
        }

        @Test
        @DisplayName("Deve gerar DocumentoAssinadoEvent ao assinar documento")
        void deveGerarDocumentoAssinadoEventAoAssinarDocumento() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            aggregate.markEventsAsCommitted();
            AssinaturaDigital assinatura = criarAssinaturaValida();

            // Act
            aggregate.assinarDigitalmente(assinatura);

            // Assert
            assertThat(aggregate.getUncommittedEvents()).hasSize(1);

            DocumentoAssinadoEvent event = (DocumentoAssinadoEvent) aggregate.getUncommittedEvents().get(0);
            assertThat(event.getDocumentoId()).isEqualTo(DOC_ID);
            assertThat(event.getTipoAssinatura()).isEqualTo(TipoAssinatura.DIGITAL);
            assertThat(event.getAssinanteNome()).isEqualTo("João Silva");
            assertThat(event.getAssinanteCpf()).isEqualTo("12345678909");
        }
    }

    // ==================== NESTED: VALIDAÇÕES DE ESTADO ====================

    @Nested
    @DisplayName("Validações de Estado")
    class ValidacoesEstado {

        @Test
        @DisplayName("Deve manter status PENDENTE após criação")
        void deveManterStatusPendenteAposCriacao() {
            // Act
            DocumentoAggregate aggregate = criarDocumentoValido();

            // Assert
            assertThat(aggregate.getStatus()).isEqualTo(StatusDocumento.PENDENTE);
        }

        @Test
        @DisplayName("Deve transicionar para VALIDADO após validação")
        void deveTransicionarParaValidadoAposValidacao() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();

            // Act
            aggregate.validar(VALIDADOR_ID, VALIDADOR_NOME, null);

            // Assert
            assertThat(aggregate.getStatus()).isEqualTo(StatusDocumento.VALIDADO);
        }

        @Test
        @DisplayName("Deve transicionar para REJEITADO após rejeição")
        void deveTransicionarParaRejeitadoAposRejeicao() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();

            // Act
            aggregate.rejeitar("Motivo", null, VALIDADOR_ID, VALIDADOR_NOME, null);

            // Assert
            assertThat(aggregate.getStatus()).isEqualTo(StatusDocumento.REJEITADO);
        }

        @Test
        @DisplayName("Não deve permitir validação de documento rejeitado")
        void naoDevePermitirValidacaoDeDocumentoRejeitado() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            aggregate.rejeitar("Motivo", null, VALIDADOR_ID, VALIDADOR_NOME, null);

            // Act & Assert
            assertThatThrownBy(() -> aggregate.validar(VALIDADOR_ID, VALIDADOR_NOME, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("não pode ser validado");
        }

        @Test
        @DisplayName("Não deve permitir rejeição de documento validado")
        void naoDevePermitirRejeicaoDeDocumentoValidado() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            aggregate.validar(VALIDADOR_ID, VALIDADOR_NOME, null);

            // Act & Assert
            assertThatThrownBy(() -> aggregate.rejeitar("Motivo", null, VALIDADOR_ID, VALIDADOR_NOME, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("não pode ser rejeitado");
        }

        @Test
        @DisplayName("Não deve permitir atualização de documento rejeitado")
        void naoDevePermitirAtualizacaoDeDocumentoRejeitado() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            aggregate.rejeitar("Motivo", null, VALIDADOR_ID, VALIDADOR_NOME, null);
            byte[] novoConteudo = "Novo".getBytes();

            // Act & Assert
            assertThatThrownBy(() -> aggregate.atualizar(novoConteudo, "Motivo", "/path", OPERADOR_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("não pode ser atualizado");
        }
    }

    // ==================== NESTED: INVARIANTES DO AGGREGATE ====================

    @Nested
    @DisplayName("Invariantes do Aggregate")
    class InvariantesAggregate {

        @Test
        @DisplayName("Documento deve sempre ter versão válida")
        void documentoDeveSempreTerVersaoValida() {
            // Act
            DocumentoAggregate aggregate = criarDocumentoValido();

            // Assert
            assertThat(aggregate.getDocumento().getVersao()).isNotNull();
            assertThat(aggregate.getDocumento().getVersao().getNumero()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Documento deve sempre ter hash válido")
        void documentoDeveSempreTerHashValido() {
            // Act
            DocumentoAggregate aggregate = criarDocumentoValido();

            // Assert
            assertThat(aggregate.getDocumento().getHash()).isNotNull();
            assertThat(aggregate.getDocumento().getHashValor()).isNotBlank();
            assertThat(aggregate.getDocumento().getHashValor()).hasSize(64); // SHA-256
        }

        @Test
        @DisplayName("Versão deve incrementar a cada atualização")
        void versaoDeveIncrementarACadaAtualizacao() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            int versaoInicial = aggregate.getDocumento().getVersao().getNumero();

            // Act
            aggregate.atualizar("Novo1".getBytes(), "Motivo1", "/path1", OPERADOR_ID);
            int versaoAposAtualizacao1 = aggregate.getDocumento().getVersao().getNumero();

            aggregate.atualizar("Novo2".getBytes(), "Motivo2", "/path2", OPERADOR_ID);
            int versaoAposAtualizacao2 = aggregate.getDocumento().getVersao().getNumero();

            // Assert
            assertThat(versaoAposAtualizacao1).isEqualTo(versaoInicial + 1);
            assertThat(versaoAposAtualizacao2).isEqualTo(versaoInicial + 2);
        }

        @Test
        @DisplayName("Hash deve mudar após atualização de conteúdo")
        void hashDeveMudarAposAtualizacaoDeConteudo() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            String hashInicial = aggregate.getDocumento().getHashValor();

            // Act
            aggregate.atualizar("Conteúdo diferente".getBytes(), "Motivo", "/path", OPERADOR_ID);
            String hashAposAtualizacao = aggregate.getDocumento().getHashValor();

            // Assert
            assertThat(hashAposAtualizacao).isNotEqualTo(hashInicial);
        }

        @Test
        @DisplayName("Tipo do documento deve ser imutável")
        void tipoDoDocumentoDeveSerImutavel() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            TipoDocumento tipoInicial = aggregate.getTipo();

            // Act
            aggregate.atualizar("Novo conteúdo".getBytes(), "Motivo", "/path", OPERADOR_ID);

            // Assert
            assertThat(aggregate.getTipo()).isEqualTo(tipoInicial);
        }

        @Test
        @DisplayName("Aggregate deve registrar timestamp de criação")
        void aggregateDeveRegistrarTimestampDeCriacao() {
            // Act
            DocumentoAggregate aggregate = criarDocumentoValido();

            // Assert
            assertThat(aggregate.getDocumento().getCriadoEm()).isNotNull();
            assertThat(aggregate.getDocumento().getCriadoPor()).isEqualTo(OPERADOR_ID);
        }

        @Test
        @DisplayName("Aggregate deve atualizar timestamp de atualização")
        void aggregateDeveAtualizarTimestampDeAtualizacao() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            aggregate.markEventsAsCommitted();
            var timestampCriacao = aggregate.getDocumento().getAtualizadoEm();

            // Act - Aguardar um pouco para garantir diferença de timestamp
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // Ignorar
            }
            aggregate.atualizar("Novo conteúdo atualizado".getBytes(), "Motivo", "/path", OPERADOR_ID);

            // Assert
            assertThat(aggregate.getDocumento().getAtualizadoEm()).isAfterOrEqualTo(timestampCriacao);
        }
    }

    // ==================== NESTED: SNAPSHOTS ====================

    @Nested
    @DisplayName("Snapshots do Aggregate")
    class SnapshotsAggregate {

        @Test
        @DisplayName("Deve criar snapshot com estado atual")
        void deveCriarSnapshotComEstadoAtual() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();

            // Act
            Object snapshot = aggregate.createSnapshot();

            // Assert
            assertThat(snapshot).isNotNull();
            assertThat(snapshot).isInstanceOf(Documento.class);

            Documento docSnapshot = (Documento) snapshot;
            assertThat(docSnapshot.getId()).isEqualTo(aggregate.getDocumentoId());
            assertThat(docSnapshot.getNome()).isEqualTo(aggregate.getDocumento().getNome());
        }

        @Test
        @DisplayName("Deve restaurar aggregate do snapshot")
        void deveRestaurarAggregateDoSnapshot() {
            // Arrange
            DocumentoAggregate aggregateOriginal = criarDocumentoValido();
            aggregateOriginal.validar(VALIDADOR_ID, VALIDADOR_NOME, null);
            Object snapshot = aggregateOriginal.createSnapshot();

            // Act
            DocumentoAggregate aggregateRestaurado = new DocumentoAggregate();
            aggregateRestaurado.loadFromSnapshot(snapshot, List.of());

            // Assert
            assertThat(aggregateRestaurado.getDocumentoId()).isEqualTo(aggregateOriginal.getDocumentoId());
            assertThat(aggregateRestaurado.getStatus()).isEqualTo(aggregateOriginal.getStatus());
            assertThat(aggregateRestaurado.getTipo()).isEqualTo(aggregateOriginal.getTipo());
        }

        @Test
        @DisplayName("Deve limpar estado ao chamar clearState")
        void deveLimparEstadoAoChamarClearState() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();

            // Act
            aggregate.clearState();

            // Assert
            assertThat(aggregate.getDocumento()).isNull();
        }
    }

    // ==================== NESTED: EVENT SOURCING ====================

    @Nested
    @DisplayName("Event Sourcing - Reconstrução de Estado")
    class EventSourcingReconstrucao {

        @Test
        @DisplayName("Deve reconstruir aggregate do histórico de eventos")
        void deveReconstruirAggregateDoHistoricoDeEventos() {
            // Arrange
            DocumentoAggregate aggregateOriginal = criarDocumentoValido();
            aggregateOriginal.validar(VALIDADOR_ID, VALIDADOR_NOME, "OK");
            List<com.seguradora.hibrida.eventstore.model.DomainEvent> eventos = aggregateOriginal.getUncommittedEvents();

            // Act
            DocumentoAggregate aggregateReconstruido = new DocumentoAggregate();
            aggregateReconstruido.loadFromHistory(eventos);

            // Assert
            assertThat(aggregateReconstruido.getDocumentoId()).isEqualTo(aggregateOriginal.getDocumentoId());
            assertThat(aggregateReconstruido.getStatus()).isEqualTo(aggregateOriginal.getStatus());
            assertThat(aggregateReconstruido.getDocumento().getNome()).isEqualTo(aggregateOriginal.getDocumento().getNome());
        }

        @Test
        @DisplayName("Deve reconstruir versões sequenciais corretamente")
        void deveReconstruirVersoesSequenciaisCorretamente() {
            // Arrange
            DocumentoAggregate aggregate = criarDocumentoValido();
            aggregate.atualizar("Conteudo2".getBytes(), "Motivo1", "/path1", OPERADOR_ID);
            aggregate.atualizar("Conteudo3".getBytes(), "Motivo2", "/path2", OPERADOR_ID);
            List<com.seguradora.hibrida.eventstore.model.DomainEvent> eventos = aggregate.getUncommittedEvents();

            // Act
            DocumentoAggregate aggregateReconstruido = new DocumentoAggregate();
            aggregateReconstruido.loadFromHistory(eventos);

            // Assert
            assertThat(aggregateReconstruido.getDocumento().getVersao().getNumero()).isEqualTo(3);
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Cria um documento válido para testes.
     * Usa FOTO_DANOS que não requer assinatura para simplificar os testes.
     */
    private DocumentoAggregate criarDocumentoValido() {
        byte[] conteudo = criarConteudoValido();
        return new DocumentoAggregate(
                DOC_ID,
                "foto_danos.jpg",
                TipoDocumento.FOTO_DANOS,
                conteudo,
                "image/jpeg",
                CONTEUDO_PATH,
                SINISTRO_ID,
                OPERADOR_ID
        );
    }

    /**
     * Cria conteúdo válido para um documento de teste.
     */
    private byte[] criarConteudoValido() {
        return "Conteúdo do documento para teste unitário".getBytes();
    }

    /**
     * Cria uma assinatura digital válida para testes.
     */
    private AssinaturaDigital criarAssinaturaValida() {
        return AssinaturaDigital.digital(
                "RSA",
                "certificado-base64-teste",
                "João Silva",
                "12345678909",
                LocalDate.now(),
                LocalDate.now().plusYears(2)
        );
    }
}
