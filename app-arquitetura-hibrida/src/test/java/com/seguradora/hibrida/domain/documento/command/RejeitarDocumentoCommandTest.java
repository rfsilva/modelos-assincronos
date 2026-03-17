package com.seguradora.hibrida.domain.documento.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários para {@link RejeitarDocumentoCommand}.
 *
 * <p>Verifica:
 * <ul>
 *   <li>Criação usando builder</li>
 *   <li>Validações de campos obrigatórios</li>
 *   <li>Validações de campos vazios</li>
 *   <li>Métodos auxiliares (possuiProblemasDetalhados, possuiAcoesCorretivas, getQuantidadeProblemas)</li>
 *   <li>Builder default para permiteReenvio</li>
 *   <li>toString e campos opcionais</li>
 * </ul>
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("RejeitarDocumentoCommand - Testes Unitários")
class RejeitarDocumentoCommandTest {

    @Nested
    @DisplayName("Criação e Validação")
    class CriacaoValidacao {

        @Test
        @DisplayName("Deve criar command com todos os campos válidos usando builder")
        void deveCriarCommandComTodosCamposValidosUsandoBuilder() {
            // Arrange
            List<String> problemas = Arrays.asList(
                    "Documento ilegível",
                    "Informações incompletas"
            );

            // Act
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documentação inadequada")
                    .problemasIdentificados(problemas)
                    .validadorId("VAL-123")
                    .validadorNome("Carlos Oliveira")
                    .acoesCorretivas("Reenviar documento com melhor qualidade")
                    .permiteReenvio(true)
                    .build();

            // Assert
            assertThat(command).isNotNull();
            assertThat(command.getDocumentoId()).isEqualTo("DOC-001");
            assertThat(command.getMotivo()).isEqualTo("Documentação inadequada");
            assertThat(command.getProblemasIdentificados()).hasSize(2);
            assertThat(command.getValidadorId()).isEqualTo("VAL-123");
            assertThat(command.getValidadorNome()).isEqualTo("Carlos Oliveira");
            assertThat(command.getAcoesCorretivas()).isEqualTo("Reenviar documento com melhor qualidade");
            assertThat(command.isPermiteReenvio()).isTrue();
        }

        @Test
        @DisplayName("Deve criar command com valores padrão usando builder")
        void deveCriarCommandComValoresPadraoUsandoBuilder() {
            // Arrange & Act
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-002")
                    .motivo("Formato incorreto")
                    .validadorId("VAL-456")
                    .build();

            // Assert
            assertThat(command).isNotNull();
            assertThat(command.isPermiteReenvio()).isTrue(); // Valor padrão
            assertThat(command.getValidadorNome()).isNull();
            assertThat(command.getAcoesCorretivas()).isNull();
            // @Singular no Lombok cria lista vazia quando não há elementos, não null
            assertThat(command.getProblemasIdentificados()).isEmpty();
        }

        @Test
        @DisplayName("Deve validar command com todos os campos válidos sem lançar exceção")
        void deveValidarCommandComTodosCamposValidosSemLancarExcecao() {
            // Arrange
            RejeitarDocumentoCommand command = criarCommandValido();

            // Act & Assert
            command.validar();
        }

        @Test
        @DisplayName("Deve lançar exceção quando documentoId é nulo")
        void deveLancarExcecaoQuandoDocumentoIdENulo() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId(null)
                    .motivo("Motivo da rejeição")
                    .validadorId("VAL-123")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Documento ID não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção quando motivo é nulo")
        void deveLancarExcecaoQuandoMotivoENulo() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo(null)
                    .validadorId("VAL-123")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Motivo não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção quando validadorId é nulo")
        void deveLancarExcecaoQuandoValidadorIdENulo() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Motivo da rejeição")
                    .validadorId(null)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Validador ID não pode ser nulo");
        }
    }

    @Nested
    @DisplayName("Validações de Campos Vazios")
    class ValidacoesCamposVazios {

        @Test
        @DisplayName("Deve lançar exceção quando documentoId é vazio")
        void deveLancarExcecaoQuandoDocumentoIdEVazio() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("")
                    .motivo("Motivo da rejeição")
                    .validadorId("VAL-123")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Documento ID não pode ser vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção quando documentoId contém apenas espaços")
        void deveLancarExcecaoQuandoDocumentoIdContemApenasEspacos() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("   ")
                    .motivo("Motivo da rejeição")
                    .validadorId("VAL-123")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Documento ID não pode ser vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção quando motivo é vazio")
        void deveLancarExcecaoQuandoMotivoEVazio() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("")
                    .validadorId("VAL-123")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Motivo não pode ser vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção quando motivo contém apenas espaços")
        void deveLancarExcecaoQuandoMotivoContemApenasEspacos() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("   ")
                    .validadorId("VAL-123")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Motivo não pode ser vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção quando validadorId é vazio")
        void deveLancarExcecaoQuandoValidadorIdEVazio() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Motivo da rejeição")
                    .validadorId("")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Validador ID não pode ser vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção quando validadorId contém apenas espaços")
        void deveLancarExcecaoQuandoValidadorIdContemApenasEspacos() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Motivo da rejeição")
                    .validadorId("   ")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> command.validar())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Validador ID não pode ser vazio");
        }
    }

    @Nested
    @DisplayName("Regras de Negócio - Problemas Identificados")
    class RegrasNegocioProblemas {

        @Test
        @DisplayName("Deve retornar true quando possui problemas detalhados")
        void deveRetornarTrueQuandoPossuiProblemasDetalhados() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documentação inadequada")
                    .problemasIdentificado("Imagem borrada")
                    .problemasIdentificado("Dados faltando")
                    .validadorId("VAL-123")
                    .build();

            // Act
            boolean possuiProblemas = command.possuiProblemasDetalhados();

            // Assert
            assertThat(possuiProblemas).isTrue();
            assertThat(command.getQuantidadeProblemas()).isEqualTo(2);
        }

        @Test
        @DisplayName("Deve retornar false quando problemas é nulo")
        void deveRetornarFalseQuandoProblemasENulo() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Motivo genérico")
                    .validadorId("VAL-123")
                    .build();

            // Act
            boolean possuiProblemas = command.possuiProblemasDetalhados();

            // Assert
            assertThat(possuiProblemas).isFalse();
            assertThat(command.getQuantidadeProblemas()).isZero();
        }

        @Test
        @DisplayName("Deve retornar false quando lista de problemas é vazia")
        void deveRetornarFalseQuandoListaDeProblemasEVazia() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Motivo genérico")
                    .problemasIdentificados(Collections.emptyList())
                    .validadorId("VAL-123")
                    .build();

            // Act
            boolean possuiProblemas = command.possuiProblemasDetalhados();

            // Assert
            assertThat(possuiProblemas).isFalse();
            assertThat(command.getQuantidadeProblemas()).isZero();
        }

        @Test
        @DisplayName("Deve usar @Singular builder para adicionar problemas individualmente")
        void deveUsarSingularBuilderParaAdicionarProblemasIndividualmente() {
            // Arrange & Act
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Múltiplos problemas")
                    .problemasIdentificado("Problema 1")
                    .problemasIdentificado("Problema 2")
                    .problemasIdentificado("Problema 3")
                    .validadorId("VAL-123")
                    .build();

            // Assert
            assertThat(command.getProblemasIdentificados()).hasSize(3);
            assertThat(command.possuiProblemasDetalhados()).isTrue();
            assertThat(command.getQuantidadeProblemas()).isEqualTo(3);
        }

        @Test
        @DisplayName("Deve aceitar lista de problemas diversos")
        void deveAceitarListaDeProblemasDiversos() {
            // Arrange
            List<String> problemas = Arrays.asList(
                    "Documento ilegível em várias partes",
                    "Falta assinatura do responsável",
                    "Data de emissão inválida",
                    "Informações contraditórias na seção 3"
            );

            // Act
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documentação com múltiplos problemas")
                    .problemasIdentificados(problemas)
                    .validadorId("VAL-123")
                    .build();

            // Assert
            assertThat(command.getProblemasIdentificados()).hasSize(4);
            assertThat(command.getQuantidadeProblemas()).isEqualTo(4);
            assertThat(command.possuiProblemasDetalhados()).isTrue();
        }
    }

    @Nested
    @DisplayName("Regras de Negócio - Ações Corretivas")
    class RegrasNegocioAcoesCorretivas {

        @Test
        @DisplayName("Deve retornar true quando possui ações corretivas preenchidas")
        void deveRetornarTrueQuandoPossuiAcoesCorretivasPreenchidas() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documentação inadequada")
                    .validadorId("VAL-123")
                    .acoesCorretivas("Reenviar com melhor qualidade")
                    .build();

            // Act
            boolean possuiAcoes = command.possuiAcoesCorretivas();

            // Assert
            assertThat(possuiAcoes).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando ações corretivas é nulo")
        void deveRetornarFalseQuandoAcoesCorretivasENulo() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Motivo da rejeição")
                    .validadorId("VAL-123")
                    .acoesCorretivas(null)
                    .build();

            // Act
            boolean possuiAcoes = command.possuiAcoesCorretivas();

            // Assert
            assertThat(possuiAcoes).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando ações corretivas é vazio")
        void deveRetornarFalseQuandoAcoesCorretivasEVazio() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Motivo da rejeição")
                    .validadorId("VAL-123")
                    .acoesCorretivas("")
                    .build();

            // Act
            boolean possuiAcoes = command.possuiAcoesCorretivas();

            // Assert
            assertThat(possuiAcoes).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando ações corretivas contém apenas espaços")
        void deveRetornarFalseQuandoAcoesCorretivasContemApenasEspacos() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Motivo da rejeição")
                    .validadorId("VAL-123")
                    .acoesCorretivas("   ")
                    .build();

            // Act
            boolean possuiAcoes = command.possuiAcoesCorretivas();

            // Assert
            assertThat(possuiAcoes).isFalse();
        }

        @Test
        @DisplayName("Deve aceitar ações corretivas detalhadas")
        void deveAceitarAcoesCorretivasDetalhadas() {
            // Arrange
            String acoesDetalhadas = "1. Escanear documento novamente com resolução mínima de 300 DPI\n" +
                    "2. Garantir assinatura do responsável legal\n" +
                    "3. Verificar data de emissão antes de reenviar";

            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documentação inadequada")
                    .validadorId("VAL-123")
                    .acoesCorretivas(acoesDetalhadas)
                    .build();

            // Act & Assert
            command.validar();
            assertThat(command.getAcoesCorretivas()).isEqualTo(acoesDetalhadas);
            assertThat(command.possuiAcoesCorretivas()).isTrue();
        }
    }

    @Nested
    @DisplayName("Regras de Negócio - Permite Reenvio")
    class RegrasNegocioPermiteReenvio {

        @Test
        @DisplayName("Deve ter permiteReenvio true por padrão")
        void deveTerPermiteReenvioTruePorPadrao() {
            // Arrange & Act
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Motivo da rejeição")
                    .validadorId("VAL-123")
                    .build();

            // Assert
            assertThat(command.isPermiteReenvio()).isTrue();
        }

        @Test
        @DisplayName("Deve permitir configurar permiteReenvio como false")
        void devePermitirConfigurarPermiteReenvioComoFalse() {
            // Arrange & Act
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documento fraudulento")
                    .validadorId("VAL-123")
                    .permiteReenvio(false)
                    .build();

            // Assert
            assertThat(command.isPermiteReenvio()).isFalse();
        }

        @Test
        @DisplayName("Deve configurar explicitamente permiteReenvio como true")
        void deveConfigurarExplicitamentePermiteReenvioComoTrue() {
            // Arrange & Act
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Motivo da rejeição")
                    .validadorId("VAL-123")
                    .permiteReenvio(true)
                    .build();

            // Assert
            assertThat(command.isPermiteReenvio()).isTrue();
        }
    }

    @Nested
    @DisplayName("Métodos Auxiliares")
    class MetodosAuxiliares {

        @Test
        @DisplayName("Deve retornar toString formatado corretamente")
        void deveRetornarToStringFormatadoCorretamente() {
            // Arrange
            RejeitarDocumentoCommand command = criarCommandValido();

            // Act
            String toString = command.toString();

            // Assert
            assertThat(toString)
                    .contains("RejeitarDocumentoCommand")
                    .contains("DOC-001")
                    .contains("Documentação inadequada")
                    .contains("VAL-123");
        }

        @Test
        @DisplayName("Deve aceitar validadorNome nulo")
        void deveAceitarValidadorNomeNulo() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Motivo da rejeição")
                    .validadorId("VAL-123")
                    .validadorNome(null)
                    .build();

            // Act & Assert
            command.validar();
            assertThat(command.getValidadorNome()).isNull();
        }

        @Test
        @DisplayName("Deve preservar dados do validador para auditoria")
        void devePreservarDadosDoValidadorParaAuditoria() {
            // Arrange
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documentação inadequada")
                    .validadorId("VAL-999")
                    .validadorNome("Fernanda Costa")
                    .build();

            // Act
            command.validar();

            // Assert
            assertThat(command.getValidadorId()).isEqualTo("VAL-999");
            assertThat(command.getValidadorNome()).isEqualTo("Fernanda Costa");
        }

        @Test
        @DisplayName("Deve aceitar motivos com caracteres especiais")
        void deveAceitarMotivosComCaracteresEspeciais() {
            // Arrange
            String motivoEspecial = "Rejeição: documento não conforme (§2º - art. 5º)";

            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo(motivoEspecial)
                    .validadorId("VAL-123")
                    .build();

            // Act & Assert
            command.validar();
            assertThat(command.getMotivo()).isEqualTo(motivoEspecial);
        }

        @Test
        @DisplayName("Deve criar command completo com todos os campos")
        void deveCriarCommandCompletoComTodosCampos() {
            // Arrange & Act
            RejeitarDocumentoCommand command = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-999")
                    .motivo("Documentação completamente inadequada")
                    .problemasIdentificado("Problema A")
                    .problemasIdentificado("Problema B")
                    .problemasIdentificado("Problema C")
                    .validadorId("VAL-888")
                    .validadorNome("Supervisor Técnico")
                    .acoesCorretivas("Refazer completamente o documento")
                    .permiteReenvio(false)
                    .build();

            // Assert
            command.validar();
            assertThat(command.getDocumentoId()).isEqualTo("DOC-999");
            assertThat(command.getMotivo()).isEqualTo("Documentação completamente inadequada");
            assertThat(command.getQuantidadeProblemas()).isEqualTo(3);
            assertThat(command.possuiProblemasDetalhados()).isTrue();
            assertThat(command.getValidadorId()).isEqualTo("VAL-888");
            assertThat(command.getValidadorNome()).isEqualTo("Supervisor Técnico");
            assertThat(command.possuiAcoesCorretivas()).isTrue();
            assertThat(command.isPermiteReenvio()).isFalse();
        }
    }

    /**
     * Helper method para criar um command válido para testes.
     *
     * @return Command válido
     */
    private RejeitarDocumentoCommand criarCommandValido() {
        return RejeitarDocumentoCommand.builder()
                .documentoId("DOC-001")
                .motivo("Documentação inadequada")
                .problemasIdentificado("Imagem ilegível")
                .problemasIdentificado("Dados incompletos")
                .validadorId("VAL-123")
                .validadorNome("Carlos Oliveira")
                .acoesCorretivas("Reenviar com melhor qualidade")
                .build();
    }
}
