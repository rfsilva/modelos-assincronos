package com.seguradora.hibrida.domain.documento.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AssinaturaDigital - Testes Unitários")
class AssinaturaDigitalTest {

    @Nested
    @DisplayName("Criação de Assinatura Digital")
    class CriacaoAssinaturaDigital {

        @Test
        @DisplayName("Deve criar assinatura digital com certificado")
        void deveCriarAssinaturaDigitalComCertificado() {
            LocalDate inicio = LocalDate.now();
            LocalDate fim = LocalDate.now().plusYears(2);

            AssinaturaDigital assinatura = AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado_base64_aqui",
                    "João Silva",
                    "12345678901",
                    inicio,
                    fim
            );

            assertThat(assinatura).isNotNull();
            assertThat(assinatura.getTipo()).isEqualTo(TipoAssinatura.DIGITAL);
            assertThat(assinatura.getAlgoritmo()).isEqualTo("RSA-2048");
            assertThat(assinatura.getCertificado()).isEqualTo("certificado_base64_aqui");
            assertThat(assinatura.getAssinanteNome()).isEqualTo("João Silva");
            assertThat(assinatura.getAssinanteCpf()).isEqualTo("12345678901");
            assertThat(assinatura.getValidadeInicio()).isEqualTo(inicio);
            assertThat(assinatura.getValidadeFim()).isEqualTo(fim);
            assertThat(assinatura.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção se algoritmo for nulo")
        void deveLancarExcecaoSeAlgoritmoNulo() {
            LocalDate inicio = LocalDate.now();
            LocalDate fim = LocalDate.now().plusYears(1);

            assertThatThrownBy(() -> AssinaturaDigital.digital(
                    null,
                    "certificado",
                    "João Silva",
                    "12345678901",
                    inicio,
                    fim
            )).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Algoritmo não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção se nome for nulo")
        void deveLancarExcecaoSeNomeNulo() {
            LocalDate inicio = LocalDate.now();
            LocalDate fim = LocalDate.now().plusYears(1);

            assertThatThrownBy(() -> AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado",
                    null,
                    "12345678901",
                    inicio,
                    fim
            )).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Nome do assinante não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção se nome for vazio")
        void deveLancarExcecaoSeNomeVazio() {
            LocalDate inicio = LocalDate.now();
            LocalDate fim = LocalDate.now().plusYears(1);

            assertThatThrownBy(() -> AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado",
                    "   ",
                    "12345678901",
                    inicio,
                    fim
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nome do assinante não pode ser vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção se CPF for nulo")
        void deveLancarExcecaoSeCpfNulo() {
            LocalDate inicio = LocalDate.now();
            LocalDate fim = LocalDate.now().plusYears(1);

            assertThatThrownBy(() -> AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado",
                    "João Silva",
                    null,
                    inicio,
                    fim
            )).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("CPF do assinante não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção se CPF for inválido")
        void deveLancarExcecaoSeCpfInvalido() {
            LocalDate inicio = LocalDate.now();
            LocalDate fim = LocalDate.now().plusYears(1);

            assertThatThrownBy(() -> AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado",
                    "João Silva",
                    "123",
                    inicio,
                    fim
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CPF inválido");
        }

        @Test
        @DisplayName("Deve lançar exceção se certificado for nulo")
        void deveLancarExcecaoSeCertificadoNulo() {
            LocalDate inicio = LocalDate.now();
            LocalDate fim = LocalDate.now().plusYears(1);

            assertThatThrownBy(() -> AssinaturaDigital.digital(
                    "RSA-2048",
                    null,
                    "João Silva",
                    "12345678901",
                    inicio,
                    fim
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Certificado digital é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar exceção se data de início for nula")
        void deveLancarExcecaoSeDataInicioNula() {
            LocalDate fim = LocalDate.now().plusYears(1);

            assertThatThrownBy(() -> AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado",
                    "João Silva",
                    "12345678901",
                    null,
                    fim
            )).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Data de início da validade não pode ser nula");
        }

        @Test
        @DisplayName("Deve lançar exceção se data de fim for nula")
        void deveLancarExcecaoSeDataFimNula() {
            LocalDate inicio = LocalDate.now();

            assertThatThrownBy(() -> AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado",
                    "João Silva",
                    "12345678901",
                    inicio,
                    null
            )).isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Data de fim da validade não pode ser nula");
        }

        @Test
        @DisplayName("Deve lançar exceção se data de fim for antes da data de início")
        void deveLancarExcecaoSeDataFimAnterior() {
            LocalDate inicio = LocalDate.now();
            LocalDate fim = LocalDate.now().minusDays(1);

            assertThatThrownBy(() -> AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado",
                    "João Silva",
                    "12345678901",
                    inicio,
                    fim
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Data de fim não pode ser anterior à data de início");
        }

        @Test
        @DisplayName("Deve lançar exceção se certificado já estiver expirado")
        void deveLancarExcecaoSeCertificadoExpirado() {
            LocalDate inicio = LocalDate.now().minusYears(2);
            LocalDate fim = LocalDate.now().minusDays(1);

            assertThatThrownBy(() -> AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado",
                    "João Silva",
                    "12345678901",
                    inicio,
                    fim
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Certificado já expirado");
        }
    }

    @Nested
    @DisplayName("Criação de Assinatura Eletrônica")
    class CriacaoAssinaturaEletronica {

        @Test
        @DisplayName("Deve criar assinatura eletrônica sem certificado")
        void deveCriarAssinaturaEletronica() {
            AssinaturaDigital assinatura = AssinaturaDigital.eletronica(
                    "Maria Santos",
                    "98765432109"
            );

            assertThat(assinatura).isNotNull();
            assertThat(assinatura.getTipo()).isEqualTo(TipoAssinatura.ELETRONICA);
            assertThat(assinatura.getAlgoritmo()).isEqualTo("ELETRONICA");
            assertThat(assinatura.getCertificado()).isNull();
            assertThat(assinatura.getAssinanteNome()).isEqualTo("Maria Santos");
            assertThat(assinatura.getAssinanteCpf()).isEqualTo("98765432109");
            assertThat(assinatura.getValidadeInicio()).isEqualTo(LocalDate.now());
            assertThat(assinatura.getValidadeFim()).isEqualTo(LocalDate.now().plusYears(1));
        }

        @Test
        @DisplayName("Deve lançar exceção se nome for nulo")
        void deveLancarExcecaoSeNomeNulo() {
            assertThatThrownBy(() -> AssinaturaDigital.eletronica(null, "98765432109"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Nome do assinante não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção se CPF for inválido")
        void deveLancarExcecaoSeCpfInvalido() {
            assertThatThrownBy(() -> AssinaturaDigital.eletronica("Maria Santos", "123"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CPF inválido");
        }
    }

    @Nested
    @DisplayName("Criação de Assinatura Física Digitalizada")
    class CriacaoAssinaturaFisica {

        @Test
        @DisplayName("Deve criar assinatura física digitalizada")
        void deveCriarAssinaturaFisica() {
            AssinaturaDigital assinatura = AssinaturaDigital.fisicaDigitalizada(
                    "Pedro Oliveira",
                    "11122233344"
            );

            assertThat(assinatura).isNotNull();
            assertThat(assinatura.getTipo()).isEqualTo(TipoAssinatura.FISICA_DIGITALIZADA);
            assertThat(assinatura.getAlgoritmo()).isEqualTo("FISICA");
            assertThat(assinatura.getCertificado()).isNull();
            assertThat(assinatura.getAssinanteNome()).isEqualTo("Pedro Oliveira");
            assertThat(assinatura.getAssinanteCpf()).isEqualTo("11122233344");
            assertThat(assinatura.getValidadeInicio()).isEqualTo(LocalDate.now());
            assertThat(assinatura.getValidadeFim()).isNull(); // Não expira
        }

        @Test
        @DisplayName("Deve lançar exceção se nome for nulo")
        void deveLancarExcecaoSeNomeNulo() {
            assertThatThrownBy(() -> AssinaturaDigital.fisicaDigitalizada(null, "11122233344"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Nome do assinante não pode ser nulo");
        }
    }

    @Nested
    @DisplayName("Validação de Assinatura")
    class ValidacaoAssinatura {

        @Test
        @DisplayName("Assinatura digital válida deve ser reconhecida como válida")
        void assinaturaDigitalValidaDeveSerReconhecida() {
            AssinaturaDigital assinatura = AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado",
                    "João Silva",
                    "12345678901",
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusYears(1)
            );

            assertThat(assinatura.isValida()).isTrue();
        }

        @Test
        @DisplayName("Assinatura eletrônica válida deve ser reconhecida como válida")
        void assinaturaEletronicaValidaDeveSerReconhecida() {
            AssinaturaDigital assinatura = AssinaturaDigital.eletronica(
                    "Maria Santos",
                    "98765432109"
            );

            assertThat(assinatura.isValida()).isTrue();
        }

        @Test
        @DisplayName("Assinatura física digitalizada sempre é válida")
        void assinaturaFisicaSempreEValida() {
            AssinaturaDigital assinatura = AssinaturaDigital.fisicaDigitalizada(
                    "Pedro Oliveira",
                    "11122233344"
            );

            assertThat(assinatura.isValida()).isTrue();
        }

        @Test
        @DisplayName("Não é possível criar assinatura digital com certificado expirado")
        void naoPodeCriarAssinaturaDigitalComCertificadoExpirado() {
            // Tentar criar assinatura com certificado já expirado deve lançar exceção
            assertThatThrownBy(() -> AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado",
                    "João Silva",
                    "12345678901",
                    LocalDate.now().minusYears(2),
                    LocalDate.now().minusDays(1)
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Certificado já expirado");
        }

        @Test
        @DisplayName("Assinatura que ainda não iniciou não deve ser válida")
        void assinaturaNaoIniciadaNaoDeveSerValida() {
            AssinaturaDigital assinatura = AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado",
                    "João Silva",
                    "12345678901",
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusYears(1)
            );

            assertThat(assinatura.isValida()).isFalse();
            assertThat(assinatura.naoIniciada()).isTrue();
        }
    }

    @Nested
    @DisplayName("Verificação de Expiração")
    class VerificacaoExpiracao {

        @Test
        @DisplayName("Assinatura física não expira")
        void assinaturaFisicaNaoExpira() {
            AssinaturaDigital assinatura = AssinaturaDigital.fisicaDigitalizada(
                    "Pedro Oliveira",
                    "11122233344"
            );

            assertThat(assinatura.expirada()).isFalse();
        }

        @Test
        @DisplayName("Assinatura dentro do prazo não está expirada")
        void assinaturaDentroDoPrazoNaoEstaExpirada() {
            AssinaturaDigital assinatura = AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado",
                    "João Silva",
                    "12345678901",
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusYears(1)
            );

            assertThat(assinatura.expirada()).isFalse();
        }

        @Test
        @DisplayName("Deve calcular dias para expirar")
        void deveCalcularDiasParaExpirar() {
            AssinaturaDigital assinatura = AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado",
                    "João Silva",
                    "12345678901",
                    LocalDate.now(),
                    LocalDate.now().plusDays(30)
            );

            long dias = assinatura.diasParaExpirar();

            assertThat(dias).isBetween(29L, 30L); // Pode variar por hora do dia
        }

        @Test
        @DisplayName("Assinatura física deve retornar -1 para dias para expirar")
        void assinaturaFisicaDeveRetornarMenos1() {
            AssinaturaDigital assinatura = AssinaturaDigital.fisicaDigitalizada(
                    "Pedro Oliveira",
                    "11122233344"
            );

            assertThat(assinatura.diasParaExpirar()).isEqualTo(-1);
        }

        @Test
        @DisplayName("Deve identificar assinatura próxima de expirar")
        void deveIdentificarAssinaturaProximaDeExpirar() {
            AssinaturaDigital assinatura = AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado",
                    "João Silva",
                    "12345678901",
                    LocalDate.now(),
                    LocalDate.now().plusDays(20)
            );

            assertThat(assinatura.proximaDeExpirar()).isTrue();
        }

        @Test
        @DisplayName("Assinatura com mais de 30 dias não está próxima de expirar")
        void assinaturaComMaisDe30DiasNaoEstaProxima() {
            AssinaturaDigital assinatura = AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado",
                    "João Silva",
                    "12345678901",
                    LocalDate.now(),
                    LocalDate.now().plusDays(60)
            );

            assertThat(assinatura.proximaDeExpirar()).isFalse();
        }
    }

    @Nested
    @DisplayName("Validação de CPF")
    class ValidacaoCpf {

        @Test
        @DisplayName("Deve validar CPF correspondente sem formatação")
        void deveValidarCpfSemFormatacao() {
            AssinaturaDigital assinatura = AssinaturaDigital.eletronica(
                    "João Silva",
                    "12345678901"
            );

            assertThat(assinatura.cpfCorresponde("12345678901")).isTrue();
        }

        @Test
        @DisplayName("Deve validar CPF correspondente com formatação")
        void deveValidarCpfComFormatacao() {
            AssinaturaDigital assinatura = AssinaturaDigital.eletronica(
                    "João Silva",
                    "12345678901"
            );

            assertThat(assinatura.cpfCorresponde("123.456.789-01")).isTrue();
        }

        @Test
        @DisplayName("Deve rejeitar CPF diferente")
        void deveRejeitarCpfDiferente() {
            AssinaturaDigital assinatura = AssinaturaDigital.eletronica(
                    "João Silva",
                    "12345678901"
            );

            assertThat(assinatura.cpfCorresponde("98765432109")).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false para CPF nulo")
        void deveRetornarFalseParaCpfNulo() {
            AssinaturaDigital assinatura = AssinaturaDigital.eletronica(
                    "João Silva",
                    "12345678901"
            );

            assertThat(assinatura.cpfCorresponde(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Verificação de Certificado")
    class VerificacaoCertificado {

        @Test
        @DisplayName("Assinatura digital deve possuir certificado")
        void assinaturaDigitalDevePossuirCertificado() {
            AssinaturaDigital assinatura = AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado_base64",
                    "João Silva",
                    "12345678901",
                    LocalDate.now(),
                    LocalDate.now().plusYears(1)
            );

            assertThat(assinatura.possuiCertificado()).isTrue();
        }

        @Test
        @DisplayName("Assinatura eletrônica não deve possuir certificado")
        void assinaturaEletronicaNaoDevePossuirCertificado() {
            AssinaturaDigital assinatura = AssinaturaDigital.eletronica(
                    "Maria Santos",
                    "98765432109"
            );

            assertThat(assinatura.possuiCertificado()).isFalse();
        }

        @Test
        @DisplayName("Assinatura física não deve possuir certificado")
        void assinaturaFisicaNaoDevePossuirCertificado() {
            AssinaturaDigital assinatura = AssinaturaDigital.fisicaDigitalizada(
                    "Pedro Oliveira",
                    "11122233344"
            );

            assertThat(assinatura.possuiCertificado()).isFalse();
        }
    }

    @Nested
    @DisplayName("Detalhes e Formatação")
    class DetalhesFormatacao {

        @Test
        @DisplayName("Deve retornar detalhes completos da assinatura")
        void deveRetornarDetalhesCompletos() {
            AssinaturaDigital assinatura = AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado",
                    "João Silva",
                    "12345678901",
                    LocalDate.now(),
                    LocalDate.now().plusYears(1)
            );

            String detalhes = assinatura.getDetalhes();

            assertThat(detalhes)
                    .contains("Tipo:")
                    .contains("Assinante:")
                    .contains("João Silva")
                    .contains("123.456.789-01") // CPF formatado
                    .contains("Algoritmo:")
                    .contains("RSA-2048")
                    .contains("Status:")
                    .contains("Certificado: Presente");
        }

        @Test
        @DisplayName("Detalhes devem mostrar status VÁLIDA")
        void detalhesDevemMostrarStatusValida() {
            AssinaturaDigital assinatura = AssinaturaDigital.eletronica(
                    "Maria Santos",
                    "98765432109"
            );

            String detalhes = assinatura.getDetalhes();

            assertThat(detalhes).contains("VÁLIDA");
        }
    }

    @Nested
    @DisplayName("Equals e HashCode")
    class EqualsEHashCode {

        @Test
        @DisplayName("Mesma instância deve ser igual a si mesma")
        void mesmaInstanciaDeveSerIgual() {
            LocalDate inicio = LocalDate.now();
            LocalDate fim = LocalDate.now().plusYears(1);

            AssinaturaDigital assinatura1 = AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado",
                    "João Silva",
                    "12345678901",
                    inicio,
                    fim
            );

            // Mesma instância deve ser igual a si mesma
            assertThat(assinatura1).isEqualTo(assinatura1);
            assertThat(assinatura1.hashCode()).isEqualTo(assinatura1.hashCode());
        }

        @Test
        @DisplayName("Assinaturas diferentes não devem ser iguais")
        void assinaturasDiferentesNaoDevemSerIguais() {
            AssinaturaDigital assinatura1 = AssinaturaDigital.eletronica(
                    "João Silva",
                    "12345678901"
            );

            AssinaturaDigital assinatura2 = AssinaturaDigital.eletronica(
                    "Maria Santos",
                    "98765432109"
            );

            assertThat(assinatura1).isNotEqualTo(assinatura2);
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToString {

        @Test
        @DisplayName("ToString não deve incluir certificado")
        void toStringNaoDeveIncluirCertificado() {
            AssinaturaDigital assinatura = AssinaturaDigital.digital(
                    "RSA-2048",
                    "certificado_secreto_muito_longo",
                    "João Silva",
                    "12345678901",
                    LocalDate.now(),
                    LocalDate.now().plusYears(1)
            );

            String toString = assinatura.toString();

            assertThat(toString).doesNotContain("certificado_secreto");
        }

        @Test
        @DisplayName("ToString deve conter informações básicas")
        void toStringDeveConterInformacoesBasicas() {
            AssinaturaDigital assinatura = AssinaturaDigital.eletronica(
                    "Maria Santos",
                    "98765432109"
            );

            String toString = assinatura.toString();

            assertThat(toString)
                    .contains("Maria Santos")
                    .contains("98765432109");
        }
    }
}
