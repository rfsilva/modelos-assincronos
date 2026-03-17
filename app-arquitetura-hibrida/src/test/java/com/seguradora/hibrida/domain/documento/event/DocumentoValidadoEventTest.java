package com.seguradora.hibrida.domain.documento.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link DocumentoValidadoEvent}.
 *
 * <p>Valida a criação e comportamento do evento de documento validado,
 * incluindo:
 * <ul>
 *   <li>Construção com dados completos e simplificados</li>
 *   <li>Validação manual vs automática</li>
 *   <li>Métodos auxiliares de verificação</li>
 *   <li>Observações e critérios aplicados</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("DocumentoValidadoEvent - Testes Unitários")
class DocumentoValidadoEventTest {

    /**
     * Cria um evento de documento validado válido para testes.
     *
     * @return Evento validado com dados de exemplo
     */
    private DocumentoValidadoEvent criarEventoPadrao() {
        return new DocumentoValidadoEvent(
                "doc-123",
                "val-456",
                "João Silva",
                "Documento aprovado sem ressalvas",
                false,
                "Verificação de autenticidade, integridade e conformidade"
        );
    }

    /**
     * Cria um evento de validação automática.
     *
     * @return Evento de validação automática
     */
    private DocumentoValidadoEvent criarEventoValidacaoAutomatica() {
        return new DocumentoValidadoEvent(
                "doc-auto-123",
                "system",
                "Sistema Automático",
                "Validação automática bem-sucedida",
                true,
                "OCR, Verificação de assinatura digital, Validação de formato"
        );
    }

    @Nested
    @DisplayName("Criação e Validação")
    class CriacaoValidacao {

        @Test
        @DisplayName("Deve criar evento com construtor completo")
        void deveCriarEventoComConstrutorCompleto() {
            // Arrange & Act
            DocumentoValidadoEvent evento = new DocumentoValidadoEvent(
                    "doc-001",
                    "val-001",
                    "Maria Santos",
                    "Documento validado com sucesso",
                    false,
                    "Análise técnica completa"
            );

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.getDocumentoId()).isEqualTo("doc-001");
            assertThat(evento.getValidadorId()).isEqualTo("val-001");
            assertThat(evento.getValidadorNome()).isEqualTo("Maria Santos");
            assertThat(evento.getObservacoes()).isEqualTo("Documento validado com sucesso");
            assertThat(evento.isValidacaoAutomatica()).isFalse();
            assertThat(evento.getCriteriosAplicados()).isEqualTo("Análise técnica completa");
        }

        @Test
        @DisplayName("Deve criar evento com construtor simplificado")
        void deveCriarEventoComConstrutorSimplificado() {
            // Arrange & Act
            DocumentoValidadoEvent evento = new DocumentoValidadoEvent(
                    "doc-002",
                    "val-002",
                    "Aprovado rapidamente"
            );

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.getDocumentoId()).isEqualTo("doc-002");
            assertThat(evento.getValidadorId()).isEqualTo("val-002");
            assertThat(evento.getObservacoes()).isEqualTo("Aprovado rapidamente");
            assertThat(evento.getValidadorNome()).isNull();
            assertThat(evento.isValidacaoAutomatica()).isFalse();
            assertThat(evento.getCriteriosAplicados()).isNull();
        }

        @Test
        @DisplayName("Deve criar evento com builder do Lombok")
        void deveCriarEventoComBuilder() {
            // Arrange & Act
            DocumentoValidadoEvent evento = DocumentoValidadoEvent.builder()
                    .documentoId("doc-003")
                    .validadorId("val-003")
                    .validadorNome("Pedro Oliveira")
                    .observacoes("Validação bem-sucedida")
                    .validacaoAutomatica(true)
                    .criteriosAplicados("IA + Verificação humana")
                    .build();

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.getDocumentoId()).isEqualTo("doc-003");
            assertThat(evento.isValidacaoAutomatica()).isTrue();
        }

        @Test
        @DisplayName("Deve criar evento sem chamar super() explicitamente")
        void deveCriarEventoComNoArgsConstructor() {
            // Arrange & Act
            DocumentoValidadoEvent evento = new DocumentoValidadoEvent();

            // Assert
            assertThat(evento).isNotNull();
        }

        @Test
        @DisplayName("Deve preservar todos os campos após criação")
        void devePreservarTodosOsCampos() {
            // Arrange
            String documentoId = "doc-preserve";
            String validadorId = "val-preserve";
            String validadorNome = "Validador Teste";
            String observacoes = "Observações de preservação";
            boolean validacaoAutomatica = true;
            String criteriosAplicados = "Critérios de teste";

            // Act
            DocumentoValidadoEvent evento = new DocumentoValidadoEvent(
                    documentoId, validadorId, validadorNome, observacoes,
                    validacaoAutomatica, criteriosAplicados
            );

            // Assert
            assertThat(evento.getDocumentoId()).isEqualTo(documentoId);
            assertThat(evento.getValidadorId()).isEqualTo(validadorId);
            assertThat(evento.getValidadorNome()).isEqualTo(validadorNome);
            assertThat(evento.getObservacoes()).isEqualTo(observacoes);
            assertThat(evento.isValidacaoAutomatica()).isEqualTo(validacaoAutomatica);
            assertThat(evento.getCriteriosAplicados()).isEqualTo(criteriosAplicados);
        }
    }

    @Nested
    @DisplayName("Propriedades do Evento")
    class PropriedadesEvento {

        @Test
        @DisplayName("Deve ter eventType correto")
        void deveTerEventTypeCorreto() {
            // Arrange
            DocumentoValidadoEvent evento = criarEventoPadrao();

            // Act
            String eventType = evento.getEventType();

            // Assert
            assertThat(eventType).isEqualTo("DocumentoValidadoEvent");
        }

        @Test
        @DisplayName("Deve permitir acesso a todos os getters")
        void devePermitirAcessoATodosOsGetters() {
            // Arrange
            DocumentoValidadoEvent evento = criarEventoPadrao();

            // Act & Assert
            assertThat(evento.getDocumentoId()).isNotNull();
            assertThat(evento.getValidadorId()).isNotNull();
            assertThat(evento.getValidadorNome()).isNotNull();
            assertThat(evento.getObservacoes()).isNotNull();
            assertThat(evento.getCriteriosAplicados()).isNotNull();
        }

        @Test
        @DisplayName("Deve ter toString implementado")
        void deveTerToStringImplementado() {
            // Arrange
            DocumentoValidadoEvent evento = criarEventoPadrao();

            // Act
            String toString = evento.toString();

            // Assert
            assertThat(toString).isNotNull();
            assertThat(toString).contains("DocumentoValidadoEvent");
        }
    }

    @Nested
    @DisplayName("Métodos Auxiliares")
    class MetodosAuxiliares {

        @Test
        @DisplayName("Deve detectar presença de observações")
        void deveDetectarPresencaDeObservacoes() {
            // Arrange
            DocumentoValidadoEvent evento = new DocumentoValidadoEvent(
                    "doc-001",
                    "val-001",
                    "Nome",
                    "Observação válida",
                    false,
                    "Critérios"
            );

            // Act
            boolean possuiObservacoes = evento.possuiObservacoes();

            // Assert
            assertThat(possuiObservacoes).isTrue();
        }

        @Test
        @DisplayName("Deve detectar ausência de observações quando null")
        void deveDetectarAusenciaDeObservacoesQuandoNull() {
            // Arrange
            DocumentoValidadoEvent evento = new DocumentoValidadoEvent(
                    "doc-002",
                    "val-002",
                    "Nome",
                    null,
                    false,
                    "Critérios"
            );

            // Act
            boolean possuiObservacoes = evento.possuiObservacoes();

            // Assert
            assertThat(possuiObservacoes).isFalse();
        }

        @Test
        @DisplayName("Deve detectar ausência de observações quando vazia")
        void deveDetectarAusenciaDeObservacoesQuandoVazia() {
            // Arrange
            DocumentoValidadoEvent evento = new DocumentoValidadoEvent(
                    "doc-003",
                    "val-003",
                    "Nome",
                    "",
                    false,
                    "Critérios"
            );

            // Act
            boolean possuiObservacoes = evento.possuiObservacoes();

            // Assert
            assertThat(possuiObservacoes).isFalse();
        }

        @Test
        @DisplayName("Deve detectar ausência de observações quando apenas espaços")
        void deveDetectarAusenciaDeObservacoesQuandoApenasEspacos() {
            // Arrange
            DocumentoValidadoEvent evento = new DocumentoValidadoEvent(
                    "doc-004",
                    "val-004",
                    "Nome",
                    "   ",
                    false,
                    "Critérios"
            );

            // Act
            boolean possuiObservacoes = evento.possuiObservacoes();

            // Assert
            assertThat(possuiObservacoes).isFalse();
        }

        @Test
        @DisplayName("Deve retornar tipo de validação manual")
        void deveRetornarTipoValidacaoManual() {
            // Arrange
            DocumentoValidadoEvent evento = new DocumentoValidadoEvent(
                    "doc-005",
                    "val-005",
                    "João",
                    "Obs",
                    false, // Manual
                    "Critérios"
            );

            // Act
            String tipoValidacao = evento.getTipoValidacao();

            // Assert
            assertThat(tipoValidacao).isEqualTo("Manual");
        }

        @Test
        @DisplayName("Deve retornar tipo de validação automática")
        void deveRetornarTipoValidacaoAutomatica() {
            // Arrange
            DocumentoValidadoEvent evento = new DocumentoValidadoEvent(
                    "doc-006",
                    "system",
                    "Sistema",
                    "Obs",
                    true, // Automática
                    "Critérios"
            );

            // Act
            String tipoValidacao = evento.getTipoValidacao();

            // Assert
            assertThat(tipoValidacao).isEqualTo("Automática");
        }
    }

    @Nested
    @DisplayName("Cenários Específicos")
    class CenariosEspecificos {

        @Test
        @DisplayName("Deve criar evento para validação manual completa")
        void deveCriarEventoParaValidacaoManualCompleta() {
            // Arrange & Act
            DocumentoValidadoEvent evento = new DocumentoValidadoEvent(
                    "doc-manual-001",
                    "val-human-001",
                    "Ana Pereira",
                    "Documento analisado e aprovado. Todas as informações conferidas.",
                    false,
                    "Verificação de autenticidade, checagem de assinaturas, análise de conteúdo"
            );

            // Assert
            assertThat(evento.isValidacaoAutomatica()).isFalse();
            assertThat(evento.getTipoValidacao()).isEqualTo("Manual");
            assertThat(evento.possuiObservacoes()).isTrue();
            assertThat(evento.getValidadorNome()).isNotNull();
            assertThat(evento.getCriteriosAplicados()).isNotNull();
        }

        @Test
        @DisplayName("Deve criar evento para validação automática")
        void deveCriarEventoParaValidacaoAutomatica() {
            // Arrange & Act
            DocumentoValidadoEvent evento = new DocumentoValidadoEvent(
                    "doc-auto-001",
                    "system",
                    "Sistema Automático",
                    "Validação automática concluída com sucesso",
                    true,
                    "OCR, Verificação de hash, Validação de assinatura digital"
            );

            // Assert
            assertThat(evento.isValidacaoAutomatica()).isTrue();
            assertThat(evento.getTipoValidacao()).isEqualTo("Automática");
            assertThat(evento.getValidadorId()).isEqualTo("system");
        }

        @Test
        @DisplayName("Deve criar evento para validação rápida sem observações")
        void deveCriarEventoParaValidacaoRapidaSemObservacoes() {
            // Arrange & Act
            DocumentoValidadoEvent evento = new DocumentoValidadoEvent(
                    "doc-quick-001",
                    "val-quick-001",
                    null // Sem observações
            );

            // Assert
            assertThat(evento.possuiObservacoes()).isFalse();
            assertThat(evento.getValidadorNome()).isNull();
            assertThat(evento.isValidacaoAutomatica()).isFalse();
        }

        @Test
        @DisplayName("Deve criar evento com observações detalhadas")
        void deveCriarEventoComObservacoesDetalhadas() {
            // Arrange & Act
            DocumentoValidadoEvent evento = new DocumentoValidadoEvent(
                    "doc-detailed-001",
                    "val-detailed-001",
                    "Carlos Mendes",
                    "Documento validado após análise criteriosa. " +
                            "Observadas pequenas inconsistências formais, " +
                            "mas que não impedem a aprovação. " +
                            "Recomenda-se atenção em futuras submissões.",
                    false,
                    "Análise completa de conformidade regulatória"
            );

            // Assert
            assertThat(evento.possuiObservacoes()).isTrue();
            assertThat(evento.getObservacoes()).contains("análise criteriosa");
            assertThat(evento.getObservacoes()).contains("Recomenda-se");
        }

        @Test
        @DisplayName("Deve criar evento com múltiplos critérios aplicados")
        void deveCriarEventoComMultiplosCriterios() {
            // Arrange & Act
            DocumentoValidadoEvent evento = new DocumentoValidadoEvent(
                    "doc-criteria-001",
                    "val-criteria-001",
                    "Fernanda Lima",
                    "Aprovado",
                    false,
                    "1. Verificação de autenticidade\n" +
                            "2. Validação de assinatura digital\n" +
                            "3. Conferência de dados\n" +
                            "4. Análise de integridade\n" +
                            "5. Validação regulatória"
            );

            // Assert
            assertThat(evento.getCriteriosAplicados()).isNotNull();
            assertThat(evento.getCriteriosAplicados()).contains("autenticidade");
            assertThat(evento.getCriteriosAplicados()).contains("integridade");
            assertThat(evento.getCriteriosAplicados()).contains("regulatória");
        }

        @Test
        @DisplayName("Deve criar evento para validação por diferentes validadores")
        void deveCriarEventoParaDiferentesValidadores() {
            // Arrange & Act
            DocumentoValidadoEvent eventoValidador1 = new DocumentoValidadoEvent(
                    "doc-v1", "val-001", "Validador Um", "Obs 1", false, "Critérios 1"
            );

            DocumentoValidadoEvent eventoValidador2 = new DocumentoValidadoEvent(
                    "doc-v2", "val-002", "Validador Dois", "Obs 2", false, "Critérios 2"
            );

            DocumentoValidadoEvent eventoSistema = new DocumentoValidadoEvent(
                    "doc-sys", "system", "Sistema", "Obs auto", true, "Critérios auto"
            );

            // Assert
            assertThat(eventoValidador1.getValidadorId()).isEqualTo("val-001");
            assertThat(eventoValidador2.getValidadorId()).isEqualTo("val-002");
            assertThat(eventoSistema.getValidadorId()).isEqualTo("system");
            assertThat(eventoSistema.isValidacaoAutomatica()).isTrue();
        }

        @Test
        @DisplayName("Deve criar evento com campos opcionais nulos")
        void deveCriarEventoComCamposOpcionaisNulos() {
            // Arrange & Act
            DocumentoValidadoEvent evento = DocumentoValidadoEvent.builder()
                    .documentoId("doc-minimal")
                    .validadorId("val-minimal")
                    .validadorNome(null)
                    .observacoes(null)
                    .validacaoAutomatica(false)
                    .criteriosAplicados(null)
                    .build();

            // Assert
            assertThat(evento.getDocumentoId()).isNotNull();
            assertThat(evento.getValidadorId()).isNotNull();
            assertThat(evento.getValidadorNome()).isNull();
            assertThat(evento.possuiObservacoes()).isFalse();
            assertThat(evento.getCriteriosAplicados()).isNull();
        }

        @Test
        @DisplayName("Deve criar evento para validação híbrida")
        void deveCriarEventoParaValidacaoHibrida() {
            // Arrange & Act
            DocumentoValidadoEvent evento = new DocumentoValidadoEvent(
                    "doc-hybrid-001",
                    "val-hybrid-001",
                    "Sistema + Humano",
                    "Pré-validação automática seguida de revisão manual",
                    true, // Marcada como automática, mas teve intervenção humana
                    "OCR automático + Revisão humana de exceções"
            );

            // Assert
            assertThat(evento.isValidacaoAutomatica()).isTrue();
            assertThat(evento.possuiObservacoes()).isTrue();
            assertThat(evento.getObservacoes()).contains("manual");
            assertThat(evento.getCriteriosAplicados()).contains("humana");
        }
    }
}
