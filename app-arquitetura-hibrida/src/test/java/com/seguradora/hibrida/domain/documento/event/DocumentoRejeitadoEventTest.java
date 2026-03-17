package com.seguradora.hibrida.domain.documento.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link DocumentoRejeitadoEvent}.
 *
 * <p>Valida a criação e comportamento do evento de documento rejeitado,
 * incluindo:
 * <ul>
 *   <li>Construção com dados completos e simplificados</li>
 *   <li>Gerenciamento de problemas identificados</li>
 *   <li>Ações corretivas sugeridas</li>
 *   <li>Controle de permissão de reenvio</li>
 *   <li>Geração de mensagem completa</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("DocumentoRejeitadoEvent - Testes Unitários")
class DocumentoRejeitadoEventTest {

    /**
     * Cria um evento de documento rejeitado válido para testes.
     *
     * @return Evento rejeitado com dados de exemplo
     */
    private DocumentoRejeitadoEvent criarEventoPadrao() {
        List<String> problemas = Arrays.asList(
                "Assinatura digital inválida",
                "Documento com rasuras",
                "Formato não compatível"
        );

        return new DocumentoRejeitadoEvent(
                "doc-123",
                "Documento não atende aos requisitos mínimos",
                problemas,
                "val-456",
                "Maria Validadora",
                "Refazer assinatura e submeter documento limpo",
                true
        );
    }

    @Nested
    @DisplayName("Criação e Validação")
    class CriacaoValidacao {

        @Test
        @DisplayName("Deve criar evento com construtor completo")
        void deveCriarEventoComConstrutorCompleto() {
            // Arrange
            List<String> problemas = Arrays.asList(
                    "Problema 1",
                    "Problema 2"
            );

            // Act
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-001",
                    "Motivo da rejeição",
                    problemas,
                    "val-001",
                    "João Validador",
                    "Ações corretivas sugeridas",
                    true
            );

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.getDocumentoId()).isEqualTo("doc-001");
            assertThat(evento.getMotivo()).isEqualTo("Motivo da rejeição");
            assertThat(evento.getProblemasIdentificados()).hasSize(2);
            assertThat(evento.getValidadorId()).isEqualTo("val-001");
            assertThat(evento.getValidadorNome()).isEqualTo("João Validador");
            assertThat(evento.getAcoesCorretivas()).isEqualTo("Ações corretivas sugeridas");
            assertThat(evento.isPermiteReenvio()).isTrue();
        }

        @Test
        @DisplayName("Deve criar evento com construtor simplificado")
        void deveCriarEventoComConstrutorSimplificado() {
            // Arrange & Act
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-002",
                    "Documento ilegível",
                    "val-002"
            );

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.getDocumentoId()).isEqualTo("doc-002");
            assertThat(evento.getMotivo()).isEqualTo("Documento ilegível");
            assertThat(evento.getValidadorId()).isEqualTo("val-002");
            assertThat(evento.getProblemasIdentificados()).isNull();
            assertThat(evento.getValidadorNome()).isNull();
            assertThat(evento.getAcoesCorretivas()).isNull();
            assertThat(evento.isPermiteReenvio()).isTrue(); // Default
        }

        @Test
        @DisplayName("Deve criar evento com builder do Lombok")
        void deveCriarEventoComBuilder() {
            // Arrange
            List<String> problemas = Collections.singletonList("Problema único");

            // Act
            DocumentoRejeitadoEvent evento = DocumentoRejeitadoEvent.builder()
                    .documentoId("doc-003")
                    .motivo("Motivo teste")
                    .problemasIdentificados(problemas)
                    .validadorId("val-003")
                    .validadorNome("Pedro Validador")
                    .acoesCorretivas("Ações de teste")
                    .permiteReenvio(false)
                    .build();

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.isPermiteReenvio()).isFalse();
            assertThat(evento.getProblemasIdentificados()).hasSize(1);
        }

        @Test
        @DisplayName("Deve criar evento sem chamar super() explicitamente")
        void deveCriarEventoComNoArgsConstructor() {
            // Arrange & Act
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent();

            // Assert
            assertThat(evento).isNotNull();
        }

        @Test
        @DisplayName("Deve preservar todos os campos após criação")
        void devePreservarTodosOsCampos() {
            // Arrange
            String documentoId = "doc-preserve";
            String motivo = "Motivo de preservação";
            List<String> problemas = Arrays.asList("P1", "P2", "P3");
            String validadorId = "val-preserve";
            String validadorNome = "Validador Preserve";
            String acoesCorretivas = "Ações de preservação";
            boolean permiteReenvio = false;

            // Act
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    documentoId, motivo, problemas, validadorId,
                    validadorNome, acoesCorretivas, permiteReenvio
            );

            // Assert
            assertThat(evento.getDocumentoId()).isEqualTo(documentoId);
            assertThat(evento.getMotivo()).isEqualTo(motivo);
            assertThat(evento.getProblemasIdentificados()).isEqualTo(problemas);
            assertThat(evento.getValidadorId()).isEqualTo(validadorId);
            assertThat(evento.getValidadorNome()).isEqualTo(validadorNome);
            assertThat(evento.getAcoesCorretivas()).isEqualTo(acoesCorretivas);
            assertThat(evento.isPermiteReenvio()).isEqualTo(permiteReenvio);
        }
    }

    @Nested
    @DisplayName("Propriedades do Evento")
    class PropriedadesEvento {

        @Test
        @DisplayName("Deve ter eventType correto")
        void deveTerEventTypeCorreto() {
            // Arrange
            DocumentoRejeitadoEvent evento = criarEventoPadrao();

            // Act
            String eventType = evento.getEventType();

            // Assert
            assertThat(eventType).isEqualTo("DocumentoRejeitadoEvent");
        }

        @Test
        @DisplayName("Deve permitir acesso a todos os getters")
        void devePermitirAcessoATodosOsGetters() {
            // Arrange
            DocumentoRejeitadoEvent evento = criarEventoPadrao();

            // Act & Assert
            assertThat(evento.getDocumentoId()).isNotNull();
            assertThat(evento.getMotivo()).isNotNull();
            assertThat(evento.getProblemasIdentificados()).isNotNull();
            assertThat(evento.getValidadorId()).isNotNull();
            assertThat(evento.getValidadorNome()).isNotNull();
            assertThat(evento.getAcoesCorretivas()).isNotNull();
        }

        @Test
        @DisplayName("Deve ter toString implementado")
        void deveTerToStringImplementado() {
            // Arrange
            DocumentoRejeitadoEvent evento = criarEventoPadrao();

            // Act
            String toString = evento.toString();

            // Assert
            assertThat(toString).isNotNull();
            assertThat(toString).contains("DocumentoRejeitadoEvent");
        }
    }

    @Nested
    @DisplayName("Métodos Auxiliares")
    class MetodosAuxiliares {

        @Test
        @DisplayName("Deve detectar presença de problemas detalhados")
        void deveDetectarPresencaDeProblemasDetalhados() {
            // Arrange
            List<String> problemas = Arrays.asList("Problema 1", "Problema 2");
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-001", "Motivo", problemas, "val-001", "Nome", "Ações", true
            );

            // Act
            boolean possuiProblemas = evento.possuiProblemasDetalhados();

            // Assert
            assertThat(possuiProblemas).isTrue();
        }

        @Test
        @DisplayName("Deve detectar ausência de problemas quando null")
        void deveDetectarAusenciaDeProblemasQuandoNull() {
            // Arrange
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-002", "Motivo", null, "val-002", "Nome", "Ações", true
            );

            // Act
            boolean possuiProblemas = evento.possuiProblemasDetalhados();

            // Assert
            assertThat(possuiProblemas).isFalse();
        }

        @Test
        @DisplayName("Deve detectar ausência de problemas quando lista vazia")
        void deveDetectarAusenciaDeProblemasQuandoListaVazia() {
            // Arrange
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-003", "Motivo", Collections.emptyList(), "val-003", "Nome", "Ações", true
            );

            // Act
            boolean possuiProblemas = evento.possuiProblemasDetalhados();

            // Assert
            assertThat(possuiProblemas).isFalse();
        }

        @Test
        @DisplayName("Deve contar quantidade de problemas corretamente")
        void deveContarQuantidadeDeProblemasCorretamente() {
            // Arrange
            List<String> problemas = Arrays.asList("P1", "P2", "P3", "P4", "P5");
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-004", "Motivo", problemas, "val-004", "Nome", "Ações", true
            );

            // Act
            int quantidade = evento.getQuantidadeProblemas();

            // Assert
            assertThat(quantidade).isEqualTo(5);
        }

        @Test
        @DisplayName("Deve retornar zero quando não há problemas")
        void deveRetornarZeroQuandoNaoHaProblemas() {
            // Arrange
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-005", "Motivo", null, "val-005", "Nome", "Ações", true
            );

            // Act
            int quantidade = evento.getQuantidadeProblemas();

            // Assert
            assertThat(quantidade).isZero();
        }

        @Test
        @DisplayName("Deve detectar presença de ações corretivas")
        void deveDetectarPresencaDeAcoesCorretivas() {
            // Arrange
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-006", "Motivo", null, "val-006", "Nome",
                    "Refazer documento", true
            );

            // Act
            boolean possuiAcoes = evento.possuiAcoesCorretivas();

            // Assert
            assertThat(possuiAcoes).isTrue();
        }

        @Test
        @DisplayName("Deve detectar ausência de ações corretivas quando null")
        void deveDetectarAusenciaDeAcoesQuandoNull() {
            // Arrange
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-007", "Motivo", null, "val-007", "Nome", null, true
            );

            // Act
            boolean possuiAcoes = evento.possuiAcoesCorretivas();

            // Assert
            assertThat(possuiAcoes).isFalse();
        }

        @Test
        @DisplayName("Deve detectar ausência de ações corretivas quando vazia")
        void deveDetectarAusenciaDeAcoesQuandoVazia() {
            // Arrange
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-008", "Motivo", null, "val-008", "Nome", "", true
            );

            // Act
            boolean possuiAcoes = evento.possuiAcoesCorretivas();

            // Assert
            assertThat(possuiAcoes).isFalse();
        }

        @Test
        @DisplayName("Deve detectar ausência de ações corretivas quando apenas espaços")
        void deveDetectarAusenciaDeAcoesQuandoApenasEspacos() {
            // Arrange
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-009", "Motivo", null, "val-009", "Nome", "   ", true
            );

            // Act
            boolean possuiAcoes = evento.possuiAcoesCorretivas();

            // Assert
            assertThat(possuiAcoes).isFalse();
        }
    }

    @Nested
    @DisplayName("Mensagem Completa")
    class MensagemCompleta {

        @Test
        @DisplayName("Deve gerar mensagem completa com todos os elementos")
        void deveGerarMensagemCompletaComTodosElementos() {
            // Arrange
            List<String> problemas = Arrays.asList(
                    "Assinatura inválida",
                    "Documento ilegível",
                    "Formato incorreto"
            );
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-001",
                    "Múltiplos problemas identificados",
                    problemas,
                    "val-001",
                    "Validador",
                    "Refazer documento seguindo as diretrizes",
                    true
            );

            // Act
            String mensagem = evento.getMensagemCompleta();

            // Assert
            assertThat(mensagem).contains("DOCUMENTO REJEITADO");
            assertThat(mensagem).contains("Motivo: Múltiplos problemas identificados");
            assertThat(mensagem).contains("Problemas Identificados:");
            assertThat(mensagem).contains("1. Assinatura inválida");
            assertThat(mensagem).contains("2. Documento ilegível");
            assertThat(mensagem).contains("3. Formato incorreto");
            assertThat(mensagem).contains("Ações Corretivas:");
            assertThat(mensagem).contains("Refazer documento seguindo as diretrizes");
            assertThat(mensagem).contains("Reenvio: Permitido");
        }

        @Test
        @DisplayName("Deve gerar mensagem sem problemas detalhados")
        void deveGerarMensagemSemProblemasDetalhados() {
            // Arrange
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-002",
                    "Documento inadequado",
                    null,
                    "val-002",
                    "Validador",
                    "Submeter novo documento",
                    false
            );

            // Act
            String mensagem = evento.getMensagemCompleta();

            // Assert
            assertThat(mensagem).contains("DOCUMENTO REJEITADO");
            assertThat(mensagem).contains("Motivo: Documento inadequado");
            assertThat(mensagem).doesNotContain("Problemas Identificados:");
            assertThat(mensagem).contains("Ações Corretivas:");
            assertThat(mensagem).contains("Reenvio: Não permitido");
        }

        @Test
        @DisplayName("Deve gerar mensagem sem ações corretivas")
        void deveGerarMensagemSemAcoesCorretivas() {
            // Arrange
            List<String> problemas = Collections.singletonList("Problema único");
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-003",
                    "Rejeição definitiva",
                    problemas,
                    "val-003",
                    "Validador",
                    null,
                    false
            );

            // Act
            String mensagem = evento.getMensagemCompleta();

            // Assert
            assertThat(mensagem).contains("DOCUMENTO REJEITADO");
            assertThat(mensagem).contains("Problemas Identificados:");
            assertThat(mensagem).doesNotContain("Ações Corretivas:");
            assertThat(mensagem).contains("Reenvio: Não permitido");
        }

        @Test
        @DisplayName("Deve gerar mensagem mínima")
        void deveGerarMensagemMinima() {
            // Arrange
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-004",
                    "Motivo genérico",
                    null,
                    "val-004",
                    "Validador",
                    null,
                    true
            );

            // Act
            String mensagem = evento.getMensagemCompleta();

            // Assert
            assertThat(mensagem).contains("DOCUMENTO REJEITADO");
            assertThat(mensagem).contains("Motivo: Motivo genérico");
            assertThat(mensagem).contains("Reenvio: Permitido");
            assertThat(mensagem).doesNotContain("Problemas Identificados:");
            assertThat(mensagem).doesNotContain("Ações Corretivas:");
        }
    }

    @Nested
    @DisplayName("Cenários Específicos")
    class CenariosEspecificos {

        @Test
        @DisplayName("Deve criar evento para rejeição por assinatura inválida")
        void deveCriarEventoParaRejeicaoPorAssinaturaInvalida() {
            // Arrange & Act
            List<String> problemas = Collections.singletonList("Assinatura digital não verificada");
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-assinatura",
                    "Assinatura digital inválida",
                    problemas,
                    "val-assinatura",
                    "Sistema de Validação",
                    "Refazer assinatura com certificado digital válido",
                    true
            );

            // Assert
            assertThat(evento.getMotivo()).contains("Assinatura");
            assertThat(evento.possuiProblemasDetalhados()).isTrue();
            assertThat(evento.possuiAcoesCorretivas()).isTrue();
            assertThat(evento.isPermiteReenvio()).isTrue();
        }

        @Test
        @DisplayName("Deve criar evento para rejeição por documento ilegível")
        void deveCriarEventoParaRejeicaoPorDocumentoIlegivel() {
            // Arrange & Act
            List<String> problemas = Arrays.asList(
                    "Imagem de baixa qualidade",
                    "Texto não reconhecível por OCR",
                    "Documento parcialmente cortado"
            );
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-ilegivel",
                    "Documento ilegível",
                    problemas,
                    "val-ocr",
                    "Sistema OCR",
                    "Escanear documento novamente com resolução mínima de 300 DPI",
                    true
            );

            // Assert
            assertThat(evento.getQuantidadeProblemas()).isEqualTo(3);
            assertThat(evento.getAcoesCorretivas()).contains("300 DPI");
        }

        @Test
        @DisplayName("Deve criar evento para rejeição definitiva")
        void deveCriarEventoParaRejeicaoDefinitiva() {
            // Arrange & Act
            List<String> problemas = Collections.singletonList("Documento fraudulento");
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-fraude",
                    "Suspeita de fraude detectada",
                    problemas,
                    "val-fraude",
                    "Equipe de Segurança",
                    "Entrar em contato com o departamento de compliance",
                    false // Não permite reenvio
            );

            // Assert
            assertThat(evento.isPermiteReenvio()).isFalse();
            assertThat(evento.getMotivo()).contains("fraude");
            assertThat(evento.getMensagemCompleta()).contains("Não permitido");
        }

        @Test
        @DisplayName("Deve criar evento com múltiplos problemas identificados")
        void deveCriarEventoComMultiplosProblemas() {
            // Arrange & Act
            List<String> problemas = Arrays.asList(
                    "Formato de arquivo não suportado",
                    "Tamanho excede o limite máximo",
                    "Metadados ausentes",
                    "Hash de integridade inválido",
                    "Data de criação inconsistente"
            );
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-multiplos",
                    "Múltiplas violações de requisitos",
                    problemas,
                    "val-multi",
                    "Validador Automático",
                    "Verificar todos os requisitos antes de submeter novamente",
                    true
            );

            // Assert
            assertThat(evento.getQuantidadeProblemas()).isEqualTo(5);
            assertThat(evento.possuiProblemasDetalhados()).isTrue();
            String mensagem = evento.getMensagemCompleta();
            assertThat(mensagem).contains("1. Formato de arquivo não suportado");
            assertThat(mensagem).contains("5. Data de criação inconsistente");
        }

        @Test
        @DisplayName("Deve criar evento com problema único detalhado")
        void deveCriarEventoComProblemaUnico() {
            // Arrange & Act
            List<String> problemas = Collections.singletonList(
                    "Documento não corresponde ao tipo declarado no sistema"
            );
            DocumentoRejeitadoEvent evento = new DocumentoRejeitadoEvent(
                    "doc-unico",
                    "Tipo de documento incorreto",
                    problemas,
                    "val-unico",
                    "Classificador Automático",
                    "Verificar o tipo do documento e submeter na categoria correta",
                    true
            );

            // Assert
            assertThat(evento.getQuantidadeProblemas()).isEqualTo(1);
            assertThat(evento.possuiProblemasDetalhados()).isTrue();
        }
    }
}
