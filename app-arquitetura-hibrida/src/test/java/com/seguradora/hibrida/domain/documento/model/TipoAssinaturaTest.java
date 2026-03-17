package com.seguradora.hibrida.domain.documento.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TipoAssinatura - Testes Unitários")
class TipoAssinaturaTest {

    @Nested
    @DisplayName("Valores do Enum")
    class ValoresEnum {

        @Test
        @DisplayName("Deve conter todos os valores esperados")
        void deveConterTodosValores() {
            TipoAssinatura[] valores = TipoAssinatura.values();

            assertThat(valores)
                    .hasSize(4)
                    .containsExactlyInAnyOrder(
                            TipoAssinatura.DIGITAL,
                            TipoAssinatura.ELETRONICA,
                            TipoAssinatura.FISICA_DIGITALIZADA,
                            TipoAssinatura.SEM_ASSINATURA
                    );
        }

        @Test
        @DisplayName("Deve converter string para enum corretamente")
        void deveConverterStringParaEnum() {
            assertThat(TipoAssinatura.valueOf("DIGITAL")).isEqualTo(TipoAssinatura.DIGITAL);
            assertThat(TipoAssinatura.valueOf("ELETRONICA")).isEqualTo(TipoAssinatura.ELETRONICA);
            assertThat(TipoAssinatura.valueOf("FISICA_DIGITALIZADA")).isEqualTo(TipoAssinatura.FISICA_DIGITALIZADA);
            assertThat(TipoAssinatura.valueOf("SEM_ASSINATURA")).isEqualTo(TipoAssinatura.SEM_ASSINATURA);
        }
    }

    @Nested
    @DisplayName("Descrições")
    class Descricoes {

        @Test
        @DisplayName("DIGITAL deve ter descrição correta")
        void digitalDeveRetornarDescricaoCorreta() {
            assertThat(TipoAssinatura.DIGITAL.getDescricao())
                    .isEqualTo("Assinatura Digital ICP-Brasil");
        }

        @Test
        @DisplayName("ELETRONICA deve ter descrição correta")
        void eletronicaDeveRetornarDescricaoCorreta() {
            assertThat(TipoAssinatura.ELETRONICA.getDescricao())
                    .isEqualTo("Assinatura Eletrônica");
        }

        @Test
        @DisplayName("FISICA_DIGITALIZADA deve ter descrição correta")
        void fisicaDigitalizadaDeveRetornarDescricaoCorreta() {
            assertThat(TipoAssinatura.FISICA_DIGITALIZADA.getDescricao())
                    .isEqualTo("Assinatura Física Digitalizada");
        }

        @Test
        @DisplayName("SEM_ASSINATURA deve ter descrição correta")
        void semAssinaturaDeveRetornarDescricaoCorreta() {
            assertThat(TipoAssinatura.SEM_ASSINATURA.getDescricao())
                    .isEqualTo("Sem Assinatura");
        }
    }

    @Nested
    @DisplayName("Nível de Segurança")
    class NivelSeguranca {

        @Test
        @DisplayName("Deve retornar níveis de segurança corretos")
        void deveRetornarNiveisCorretos() {
            assertThat(TipoAssinatura.DIGITAL.getNivelSeguranca()).isEqualTo(3);
            assertThat(TipoAssinatura.ELETRONICA.getNivelSeguranca()).isEqualTo(2);
            assertThat(TipoAssinatura.FISICA_DIGITALIZADA.getNivelSeguranca()).isEqualTo(1);
            assertThat(TipoAssinatura.SEM_ASSINATURA.getNivelSeguranca()).isEqualTo(0);
        }

        @Test
        @DisplayName("Deve atender nível de segurança mínimo")
        void deveAtenderNivelMinimo() {
            assertThat(TipoAssinatura.DIGITAL.atendeNivelSeguranca(3)).isTrue();
            assertThat(TipoAssinatura.DIGITAL.atendeNivelSeguranca(2)).isTrue();
            assertThat(TipoAssinatura.DIGITAL.atendeNivelSeguranca(1)).isTrue();

            assertThat(TipoAssinatura.ELETRONICA.atendeNivelSeguranca(2)).isTrue();
            assertThat(TipoAssinatura.ELETRONICA.atendeNivelSeguranca(3)).isFalse();

            assertThat(TipoAssinatura.SEM_ASSINATURA.atendeNivelSeguranca(1)).isFalse();
        }
    }

    @Nested
    @DisplayName("Requisito de Certificado")
    class RequisitoCertificado {

        @Test
        @DisplayName("Apenas DIGITAL requer certificado")
        void apenasDigitalRequerCertificado() {
            assertThat(TipoAssinatura.DIGITAL.requerCertificado()).isTrue();
            assertThat(TipoAssinatura.ELETRONICA.requerCertificado()).isFalse();
            assertThat(TipoAssinatura.FISICA_DIGITALIZADA.requerCertificado()).isFalse();
            assertThat(TipoAssinatura.SEM_ASSINATURA.requerCertificado()).isFalse();
        }
    }

    @Nested
    @DisplayName("Validação Automática")
    class ValidacaoAutomatica {

        @Test
        @DisplayName("DIGITAL e ELETRONICA permitem validação automática")
        void digitaisPermitemValidacaoAutomatica() {
            assertThat(TipoAssinatura.DIGITAL.permiteValidacaoAutomatica()).isTrue();
            assertThat(TipoAssinatura.ELETRONICA.permiteValidacaoAutomatica()).isTrue();
        }

        @Test
        @DisplayName("FISICA_DIGITALIZADA e SEM_ASSINATURA não permitem validação automática")
        void fisicaNaoPermiteValidacaoAutomatica() {
            assertThat(TipoAssinatura.FISICA_DIGITALIZADA.permiteValidacaoAutomatica()).isFalse();
            assertThat(TipoAssinatura.SEM_ASSINATURA.permiteValidacaoAutomatica()).isFalse();
        }
    }

    @Nested
    @DisplayName("Assinatura Válida")
    class AssinaturaValida {

        @Test
        @DisplayName("Todos exceto SEM_ASSINATURA são assinaturas válidas")
        void todosExcetoSemAssinaturaSaoValidos() {
            assertThat(TipoAssinatura.DIGITAL.isAssinaturaValida()).isTrue();
            assertThat(TipoAssinatura.ELETRONICA.isAssinaturaValida()).isTrue();
            assertThat(TipoAssinatura.FISICA_DIGITALIZADA.isAssinaturaValida()).isTrue();
            assertThat(TipoAssinatura.SEM_ASSINATURA.isAssinaturaValida()).isFalse();
        }
    }

    @Nested
    @DisplayName("Comparação de Segurança")
    class ComparacaoSeguranca {

        @Test
        @DisplayName("DIGITAL é mais seguro que todos os outros")
        void digitalEMaisSeguro() {
            assertThat(TipoAssinatura.DIGITAL.isMaisSeguroQue(TipoAssinatura.ELETRONICA)).isTrue();
            assertThat(TipoAssinatura.DIGITAL.isMaisSeguroQue(TipoAssinatura.FISICA_DIGITALIZADA)).isTrue();
            assertThat(TipoAssinatura.DIGITAL.isMaisSeguroQue(TipoAssinatura.SEM_ASSINATURA)).isTrue();
        }

        @Test
        @DisplayName("ELETRONICA é mais seguro que FISICA_DIGITALIZADA e SEM_ASSINATURA")
        void eletronicaEMaisSeguroQue() {
            assertThat(TipoAssinatura.ELETRONICA.isMaisSeguroQue(TipoAssinatura.DIGITAL)).isFalse();
            assertThat(TipoAssinatura.ELETRONICA.isMaisSeguroQue(TipoAssinatura.FISICA_DIGITALIZADA)).isTrue();
            assertThat(TipoAssinatura.ELETRONICA.isMaisSeguroQue(TipoAssinatura.SEM_ASSINATURA)).isTrue();
        }

        @Test
        @DisplayName("FISICA_DIGITALIZADA é mais seguro apenas que SEM_ASSINATURA")
        void fisicaDigitalizadaEMaisSeguroQue() {
            assertThat(TipoAssinatura.FISICA_DIGITALIZADA.isMaisSeguroQue(TipoAssinatura.DIGITAL)).isFalse();
            assertThat(TipoAssinatura.FISICA_DIGITALIZADA.isMaisSeguroQue(TipoAssinatura.ELETRONICA)).isFalse();
            assertThat(TipoAssinatura.FISICA_DIGITALIZADA.isMaisSeguroQue(TipoAssinatura.SEM_ASSINATURA)).isTrue();
        }

        @Test
        @DisplayName("SEM_ASSINATURA não é mais seguro que nenhum")
        void semAssinaturaNaoEMaisSeguro() {
            assertThat(TipoAssinatura.SEM_ASSINATURA.isMaisSeguroQue(TipoAssinatura.DIGITAL)).isFalse();
            assertThat(TipoAssinatura.SEM_ASSINATURA.isMaisSeguroQue(TipoAssinatura.ELETRONICA)).isFalse();
            assertThat(TipoAssinatura.SEM_ASSINATURA.isMaisSeguroQue(TipoAssinatura.FISICA_DIGITALIZADA)).isFalse();
        }

        @Test
        @DisplayName("Comparação com null deve retornar true")
        void comparacaoComNullRetornaTrue() {
            assertThat(TipoAssinatura.DIGITAL.isMaisSeguroQue(null)).isTrue();
            assertThat(TipoAssinatura.SEM_ASSINATURA.isMaisSeguroQue(null)).isTrue();
        }

        @Test
        @DisplayName("Tipos iguais não são mais seguros entre si")
        void tiposIguaisNaoSaoMaisSeguro() {
            assertThat(TipoAssinatura.DIGITAL.isMaisSeguroQue(TipoAssinatura.DIGITAL)).isFalse();
            assertThat(TipoAssinatura.ELETRONICA.isMaisSeguroQue(TipoAssinatura.ELETRONICA)).isFalse();
        }
    }

    @Nested
    @DisplayName("Métodos Estáticos")
    class MetodosEstaticos {

        @Test
        @DisplayName("Deve retornar DIGITAL como recomendado para documentos críticos")
        void deveRetornarDigitalParaDocumentosCriticos() {
            assertThat(TipoAssinatura.getRecomendadoParaDocumentosCriticos())
                    .isEqualTo(TipoAssinatura.DIGITAL);
        }

        @Test
        @DisplayName("Deve retornar ELETRONICA como mínimo aceitável")
        void deveRetornarEletronicaComoMinimoAceitavel() {
            assertThat(TipoAssinatura.getMinimoAceitavel())
                    .isEqualTo(TipoAssinatura.ELETRONICA);
        }
    }
}
