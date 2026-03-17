package com.seguradora.hibrida.domain.documento.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("StatusDocumento - Testes Unitários")
class StatusDocumentoTest {

    @Nested
    @DisplayName("Valores do Enum")
    class ValoresEnum {

        @Test
        @DisplayName("Deve conter todos os valores esperados")
        void deveConterTodosValores() {
            StatusDocumento[] valores = StatusDocumento.values();

            assertThat(valores)
                    .hasSize(4)
                    .containsExactlyInAnyOrder(
                            StatusDocumento.PENDENTE,
                            StatusDocumento.VALIDADO,
                            StatusDocumento.REJEITADO,
                            StatusDocumento.ARQUIVADO
                    );
        }

        @Test
        @DisplayName("Deve converter string para enum corretamente")
        void deveConverterStringParaEnum() {
            assertThat(StatusDocumento.valueOf("PENDENTE")).isEqualTo(StatusDocumento.PENDENTE);
            assertThat(StatusDocumento.valueOf("VALIDADO")).isEqualTo(StatusDocumento.VALIDADO);
            assertThat(StatusDocumento.valueOf("REJEITADO")).isEqualTo(StatusDocumento.REJEITADO);
            assertThat(StatusDocumento.valueOf("ARQUIVADO")).isEqualTo(StatusDocumento.ARQUIVADO);
        }
    }

    @Nested
    @DisplayName("Status Final")
    class StatusFinal {

        @Test
        @DisplayName("PENDENTE não deve ser status final")
        void pendenteNaoDeveSerFinal() {
            assertThat(StatusDocumento.PENDENTE.isFinal()).isFalse();
        }

        @Test
        @DisplayName("VALIDADO não deve ser status final")
        void validadoNaoDeveSerFinal() {
            assertThat(StatusDocumento.VALIDADO.isFinal()).isFalse();
        }

        @Test
        @DisplayName("REJEITADO deve ser status final")
        void rejeitadoDeveSerFinal() {
            assertThat(StatusDocumento.REJEITADO.isFinal()).isTrue();
        }

        @Test
        @DisplayName("ARQUIVADO deve ser status final")
        void arquivadoDeveSerFinal() {
            assertThat(StatusDocumento.ARQUIVADO.isFinal()).isTrue();
        }
    }

    @Nested
    @DisplayName("Transições de Status")
    class TransicoesStatus {

        @Test
        @DisplayName("PENDENTE pode transicionar para VALIDADO")
        void pendentePodeTransicionarParaValidado() {
            assertThat(StatusDocumento.PENDENTE.podeTransicionarPara(StatusDocumento.VALIDADO))
                    .isTrue();
        }

        @Test
        @DisplayName("PENDENTE pode transicionar para REJEITADO")
        void pendentePodeTransicionarParaRejeitado() {
            assertThat(StatusDocumento.PENDENTE.podeTransicionarPara(StatusDocumento.REJEITADO))
                    .isTrue();
        }

        @Test
        @DisplayName("PENDENTE não pode transicionar para ARQUIVADO")
        void pendenteNaoPodeTransicionarParaArquivado() {
            assertThat(StatusDocumento.PENDENTE.podeTransicionarPara(StatusDocumento.ARQUIVADO))
                    .isFalse();
        }

        @Test
        @DisplayName("VALIDADO pode transicionar para ARQUIVADO")
        void validadoPodeTransicionarParaArquivado() {
            assertThat(StatusDocumento.VALIDADO.podeTransicionarPara(StatusDocumento.ARQUIVADO))
                    .isTrue();
        }

        @Test
        @DisplayName("VALIDADO não pode transicionar para REJEITADO")
        void validadoNaoPodeTransicionarParaRejeitado() {
            assertThat(StatusDocumento.VALIDADO.podeTransicionarPara(StatusDocumento.REJEITADO))
                    .isFalse();
        }

        @Test
        @DisplayName("REJEITADO não pode transicionar para nenhum status")
        void rejeitadoNaoPodeTransicionar() {
            assertThat(StatusDocumento.REJEITADO.podeTransicionarPara(StatusDocumento.PENDENTE))
                    .isFalse();
            assertThat(StatusDocumento.REJEITADO.podeTransicionarPara(StatusDocumento.VALIDADO))
                    .isFalse();
            assertThat(StatusDocumento.REJEITADO.podeTransicionarPara(StatusDocumento.ARQUIVADO))
                    .isFalse();
        }

        @Test
        @DisplayName("ARQUIVADO não pode transicionar para nenhum status")
        void arquivadoNaoPodeTransicionar() {
            assertThat(StatusDocumento.ARQUIVADO.podeTransicionarPara(StatusDocumento.PENDENTE))
                    .isFalse();
            assertThat(StatusDocumento.ARQUIVADO.podeTransicionarPara(StatusDocumento.VALIDADO))
                    .isFalse();
            assertThat(StatusDocumento.ARQUIVADO.podeTransicionarPara(StatusDocumento.REJEITADO))
                    .isFalse();
        }

        @Test
        @DisplayName("Não pode transicionar para null")
        void naoPodeTransicionarParaNull() {
            assertThat(StatusDocumento.PENDENTE.podeTransicionarPara(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Transições Permitidas")
    class TransicoesPermitidas {

        @Test
        @DisplayName("PENDENTE deve retornar VALIDADO e REJEITADO como transições permitidas")
        void pendenteDeveRetornarTransicoesCorretas() {
            Set<StatusDocumento> transicoes = StatusDocumento.PENDENTE.getTransicoesPermitidas();

            assertThat(transicoes)
                    .hasSize(2)
                    .containsExactlyInAnyOrder(StatusDocumento.VALIDADO, StatusDocumento.REJEITADO);
        }

        @Test
        @DisplayName("VALIDADO deve retornar apenas ARQUIVADO como transição permitida")
        void validadoDeveRetornarTransicoesCorretas() {
            Set<StatusDocumento> transicoes = StatusDocumento.VALIDADO.getTransicoesPermitidas();

            assertThat(transicoes)
                    .hasSize(1)
                    .containsExactly(StatusDocumento.ARQUIVADO);
        }

        @Test
        @DisplayName("REJEITADO não deve ter transições permitidas")
        void rejeitadoNaoDeveTerTransicoes() {
            Set<StatusDocumento> transicoes = StatusDocumento.REJEITADO.getTransicoesPermitidas();

            assertThat(transicoes).isEmpty();
        }

        @Test
        @DisplayName("ARQUIVADO não deve ter transições permitidas")
        void arquivadoNaoDeveTerTransicoes() {
            Set<StatusDocumento> transicoes = StatusDocumento.ARQUIVADO.getTransicoesPermitidas();

            assertThat(transicoes).isEmpty();
        }
    }

    @Nested
    @DisplayName("Permissões de Operação")
    class PermissoesOperacao {

        @Test
        @DisplayName("Apenas PENDENTE pode ser atualizado")
        void apenasPendentePodeSerAtualizado() {
            assertThat(StatusDocumento.PENDENTE.podeAtualizar()).isTrue();
            assertThat(StatusDocumento.VALIDADO.podeAtualizar()).isFalse();
            assertThat(StatusDocumento.REJEITADO.podeAtualizar()).isFalse();
            assertThat(StatusDocumento.ARQUIVADO.podeAtualizar()).isFalse();
        }

        @Test
        @DisplayName("Apenas PENDENTE pode ser validado")
        void apenasPendentePodeSerValidado() {
            assertThat(StatusDocumento.PENDENTE.podeValidar()).isTrue();
            assertThat(StatusDocumento.VALIDADO.podeValidar()).isFalse();
            assertThat(StatusDocumento.REJEITADO.podeValidar()).isFalse();
            assertThat(StatusDocumento.ARQUIVADO.podeValidar()).isFalse();
        }

        @Test
        @DisplayName("Apenas PENDENTE pode ser rejeitado")
        void apenasPendentePodeSerRejeitado() {
            assertThat(StatusDocumento.PENDENTE.podeRejeitar()).isTrue();
            assertThat(StatusDocumento.VALIDADO.podeRejeitar()).isFalse();
            assertThat(StatusDocumento.REJEITADO.podeRejeitar()).isFalse();
            assertThat(StatusDocumento.ARQUIVADO.podeRejeitar()).isFalse();
        }
    }

    @Nested
    @DisplayName("Verificadores de Estado")
    class VerificadoresEstado {

        @Test
        @DisplayName("Apenas PENDENTE está pendente")
        void apenaspendenteEstaPendente() {
            assertThat(StatusDocumento.PENDENTE.estaPendente()).isTrue();
            assertThat(StatusDocumento.VALIDADO.estaPendente()).isFalse();
            assertThat(StatusDocumento.REJEITADO.estaPendente()).isFalse();
            assertThat(StatusDocumento.ARQUIVADO.estaPendente()).isFalse();
        }

        @Test
        @DisplayName("VALIDADO e ARQUIVADO estão validados")
        void validadoEArquivadoEstaoValidados() {
            assertThat(StatusDocumento.PENDENTE.estaValidado()).isFalse();
            assertThat(StatusDocumento.VALIDADO.estaValidado()).isTrue();
            assertThat(StatusDocumento.REJEITADO.estaValidado()).isFalse();
            assertThat(StatusDocumento.ARQUIVADO.estaValidado()).isTrue();
        }
    }

    @Nested
    @DisplayName("Validação de Transição")
    class ValidacaoTransicao {

        @Test
        @DisplayName("Deve validar transição permitida sem lançar exceção")
        void deveValidarTransicaoPermitida() {
            StatusDocumento.PENDENTE.validarTransicao(StatusDocumento.VALIDADO);
            StatusDocumento.PENDENTE.validarTransicao(StatusDocumento.REJEITADO);
            StatusDocumento.VALIDADO.validarTransicao(StatusDocumento.ARQUIVADO);
        }

        @Test
        @DisplayName("Deve lançar exceção para transição não permitida")
        void deveLancarExcecaoParaTransicaoNaoPermitida() {
            assertThatThrownBy(() ->
                    StatusDocumento.PENDENTE.validarTransicao(StatusDocumento.ARQUIVADO))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Transição não permitida de PENDENTE para ARQUIVADO");
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar transicionar de status final")
        void deveLancarExcecaoAoTransicionarDeStatusFinal() {
            assertThatThrownBy(() ->
                    StatusDocumento.REJEITADO.validarTransicao(StatusDocumento.PENDENTE))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Transição não permitida de REJEITADO para PENDENTE");

            assertThatThrownBy(() ->
                    StatusDocumento.ARQUIVADO.validarTransicao(StatusDocumento.VALIDADO))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Transição não permitida de ARQUIVADO para VALIDADO");
        }

        @Test
        @DisplayName("Deve lançar exceção ao validar transição com status nulo")
        void deveLancarExcecaoComStatusNulo() {
            assertThatThrownBy(() ->
                    StatusDocumento.PENDENTE.validarTransicao(null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Transição não permitida");
        }
    }
}
