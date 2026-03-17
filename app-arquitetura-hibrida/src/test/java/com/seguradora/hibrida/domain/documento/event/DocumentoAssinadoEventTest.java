package com.seguradora.hibrida.domain.documento.event;

import com.seguradora.hibrida.domain.documento.model.TipoAssinatura;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link DocumentoAssinadoEvent}.
 *
 * <p>Valida a criação e comportamento do evento de documento assinado,
 * incluindo:
 * <ul>
 *   <li>Construção com assinatura digital e eletrônica</li>
 *   <li>Validação de certificados e validades</li>
 *   <li>Formatação de CPF e cálculos de expiração</li>
 *   <li>Métodos auxiliares de verificação</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("DocumentoAssinadoEvent - Testes Unitários")
class DocumentoAssinadoEventTest {

    /**
     * Cria um evento de documento assinado com assinatura digital para testes.
     *
     * @return Evento assinado com dados de exemplo
     */
    private DocumentoAssinadoEvent criarEventoAssinaturaDigital() {
        return new DocumentoAssinadoEvent(
                "doc-123",
                TipoAssinatura.DIGITAL,
                "SHA256withRSA",
                "MIIC...certificadoBase64...==",
                "João da Silva",
                "12345678901",
                LocalDate.now().minusYears(1),
                LocalDate.now().plusYears(2),
                "hashDocumento123"
        );
    }

    /**
     * Cria um evento de documento assinado com assinatura eletrônica para testes.
     *
     * @return Evento com assinatura eletrônica
     */
    private DocumentoAssinadoEvent criarEventoAssinaturaEletronica() {
        return new DocumentoAssinadoEvent(
                "doc-456",
                "Maria Santos",
                "98765432100",
                "hashDocumento456"
        );
    }

    @Nested
    @DisplayName("Criação e Validação")
    class CriacaoValidacao {

        @Test
        @DisplayName("Deve criar evento com construtor completo para assinatura digital")
        void deveCriarEventoComConstrutorCompletoDigital() {
            // Arrange
            LocalDate validadeInicio = LocalDate.of(2024, 1, 1);
            LocalDate validadeFim = LocalDate.of(2027, 1, 1);

            // Act
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    "doc-001",
                    TipoAssinatura.DIGITAL,
                    "SHA256withRSA",
                    "MIIC123456789==",
                    "Carlos Pereira",
                    "11122233344",
                    validadeInicio,
                    validadeFim,
                    "hashABC123"
            );

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.getDocumentoId()).isEqualTo("doc-001");
            assertThat(evento.getTipoAssinatura()).isEqualTo(TipoAssinatura.DIGITAL);
            assertThat(evento.getAlgoritmo()).isEqualTo("SHA256withRSA");
            assertThat(evento.getCertificado()).isEqualTo("MIIC123456789==");
            assertThat(evento.getAssinanteNome()).isEqualTo("Carlos Pereira");
            assertThat(evento.getAssinanteCpf()).isEqualTo("11122233344");
            assertThat(evento.getValidadeInicio()).isEqualTo(validadeInicio);
            assertThat(evento.getValidadeFim()).isEqualTo(validadeFim);
            assertThat(evento.getHashDocumento()).isEqualTo("hashABC123");
        }

        @Test
        @DisplayName("Deve criar evento com construtor simplificado para assinatura eletrônica")
        void deveCriarEventoComConstrutorSimplificadoEletronica() {
            // Arrange & Act
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    "doc-002",
                    "Ana Costa",
                    "55566677788",
                    "hashXYZ789"
            );

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.getDocumentoId()).isEqualTo("doc-002");
            assertThat(evento.getAssinanteNome()).isEqualTo("Ana Costa");
            assertThat(evento.getAssinanteCpf()).isEqualTo("55566677788");
            assertThat(evento.getHashDocumento()).isEqualTo("hashXYZ789");
            assertThat(evento.getTipoAssinatura()).isEqualTo(TipoAssinatura.ELETRONICA);
            assertThat(evento.getAlgoritmo()).isEqualTo("ELETRONICA");
            assertThat(evento.getCertificado()).isNull();
            assertThat(evento.getValidadeInicio()).isNotNull();
            assertThat(evento.getValidadeFim()).isNotNull();
        }

        @Test
        @DisplayName("Deve criar evento com builder do Lombok")
        void deveCriarEventoComBuilder() {
            // Arrange & Act
            DocumentoAssinadoEvent evento = DocumentoAssinadoEvent.builder()
                    .documentoId("doc-003")
                    .tipoAssinatura(TipoAssinatura.DIGITAL)
                    .algoritmo("SHA512withRSA")
                    .certificado("CERT123==")
                    .assinanteNome("Pedro Silva")
                    .assinanteCpf("99988877766")
                    .validadeInicio(LocalDate.now())
                    .validadeFim(LocalDate.now().plusYears(3))
                    .hashDocumento("hash999")
                    .build();

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.getAlgoritmo()).isEqualTo("SHA512withRSA");
        }

        @Test
        @DisplayName("Deve criar evento sem chamar super() explicitamente")
        void deveCriarEventoComNoArgsConstructor() {
            // Arrange & Act
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent();

            // Assert
            assertThat(evento).isNotNull();
        }

        @Test
        @DisplayName("Deve preservar todos os campos após criação")
        void devePreservarTodosOsCampos() {
            // Arrange
            String documentoId = "doc-preserve";
            TipoAssinatura tipoAssinatura = TipoAssinatura.DIGITAL;
            String algoritmo = "SHA256withECDSA";
            String certificado = "CERTPRESERVE==";
            String assinanteNome = "Assinante Preserve";
            String assinanteCpf = "12312312312";
            LocalDate validadeInicio = LocalDate.of(2025, 1, 1);
            LocalDate validadeFim = LocalDate.of(2028, 1, 1);
            String hashDocumento = "hashPreserve";

            // Act
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    documentoId, tipoAssinatura, algoritmo, certificado,
                    assinanteNome, assinanteCpf, validadeInicio, validadeFim, hashDocumento
            );

            // Assert
            assertThat(evento.getDocumentoId()).isEqualTo(documentoId);
            assertThat(evento.getTipoAssinatura()).isEqualTo(tipoAssinatura);
            assertThat(evento.getAlgoritmo()).isEqualTo(algoritmo);
            assertThat(evento.getCertificado()).isEqualTo(certificado);
            assertThat(evento.getAssinanteNome()).isEqualTo(assinanteNome);
            assertThat(evento.getAssinanteCpf()).isEqualTo(assinanteCpf);
            assertThat(evento.getValidadeInicio()).isEqualTo(validadeInicio);
            assertThat(evento.getValidadeFim()).isEqualTo(validadeFim);
            assertThat(evento.getHashDocumento()).isEqualTo(hashDocumento);
        }
    }

    @Nested
    @DisplayName("Propriedades do Evento")
    class PropriedadesEvento {

        @Test
        @DisplayName("Deve ter eventType correto")
        void deveTerEventTypeCorreto() {
            // Arrange
            DocumentoAssinadoEvent evento = criarEventoAssinaturaDigital();

            // Act
            String eventType = evento.getEventType();

            // Assert
            assertThat(eventType).isEqualTo("DocumentoAssinadoEvent");
        }

        @Test
        @DisplayName("Deve permitir acesso a todos os getters")
        void devePermitirAcessoATodosOsGetters() {
            // Arrange
            DocumentoAssinadoEvent evento = criarEventoAssinaturaDigital();

            // Act & Assert
            assertThat(evento.getDocumentoId()).isNotNull();
            assertThat(evento.getTipoAssinatura()).isNotNull();
            assertThat(evento.getAlgoritmo()).isNotNull();
            assertThat(evento.getCertificado()).isNotNull();
            assertThat(evento.getAssinanteNome()).isNotNull();
            assertThat(evento.getAssinanteCpf()).isNotNull();
            assertThat(evento.getValidadeInicio()).isNotNull();
            assertThat(evento.getValidadeFim()).isNotNull();
            assertThat(evento.getHashDocumento()).isNotNull();
        }

        @Test
        @DisplayName("Deve ter toString implementado sem incluir certificado")
        void deveTerToStringImplementadoSemCertificado() {
            // Arrange
            DocumentoAssinadoEvent evento = criarEventoAssinaturaDigital();

            // Act
            String toString = evento.toString();

            // Assert
            assertThat(toString).isNotNull();
            assertThat(toString).contains("DocumentoAssinadoEvent");
            // O certificado está excluído do toString conforme annotation
        }
    }

    @Nested
    @DisplayName("Métodos Auxiliares - Certificado")
    class MetodosAuxiliaresCertificado {

        @Test
        @DisplayName("Deve detectar presença de certificado")
        void deveDetectarPresencaDeCertificado() {
            // Arrange
            DocumentoAssinadoEvent evento = criarEventoAssinaturaDigital();

            // Act
            boolean possuiCertificado = evento.possuiCertificado();

            // Assert
            assertThat(possuiCertificado).isTrue();
        }

        @Test
        @DisplayName("Deve detectar ausência de certificado quando null")
        void deveDetectarAusenciaDeCertificadoQuandoNull() {
            // Arrange
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    "doc-no-cert", "Nome", "12345678901", "hash123"
            );

            // Act
            boolean possuiCertificado = evento.possuiCertificado();

            // Assert
            assertThat(possuiCertificado).isFalse();
        }

        @Test
        @DisplayName("Deve detectar ausência de certificado quando vazio")
        void deveDetectarAusenciaDeCertificadoQuandoVazio() {
            // Arrange
            DocumentoAssinadoEvent evento = DocumentoAssinadoEvent.builder()
                    .documentoId("doc-empty-cert")
                    .tipoAssinatura(TipoAssinatura.ELETRONICA)
                    .algoritmo("ELETRONICA")
                    .certificado("")
                    .assinanteNome("Nome")
                    .assinanteCpf("12345678901")
                    .validadeInicio(LocalDate.now())
                    .validadeFim(LocalDate.now().plusYears(1))
                    .hashDocumento("hash")
                    .build();

            // Act
            boolean possuiCertificado = evento.possuiCertificado();

            // Assert
            assertThat(possuiCertificado).isFalse();
        }

        @Test
        @DisplayName("Deve identificar quando requer certificado (Digital)")
        void deveIdentificarQuandoRequerCertificadoDigital() {
            // Arrange
            DocumentoAssinadoEvent evento = criarEventoAssinaturaDigital();

            // Act
            boolean requer = evento.requerCertificado();

            // Assert
            assertThat(requer).isTrue();
        }

        @Test
        @DisplayName("Deve identificar quando não requer certificado (Eletrônica)")
        void deveIdentificarQuandoNaoRequerCertificadoEletronica() {
            // Arrange
            DocumentoAssinadoEvent evento = criarEventoAssinaturaEletronica();

            // Act
            boolean requer = evento.requerCertificado();

            // Assert
            assertThat(requer).isFalse();
        }
    }

    @Nested
    @DisplayName("Métodos Auxiliares - Validade")
    class MetodosAuxiliaresValidade {

        @Test
        @DisplayName("Deve validar assinatura dentro do período de validade")
        void deveValidarAssinaturaDentroDoPeriodo() {
            // Arrange
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    "doc-valid",
                    TipoAssinatura.DIGITAL,
                    "SHA256withRSA",
                    "CERT123",
                    "Nome",
                    "12345678901",
                    LocalDate.now().minusYears(1),
                    LocalDate.now().plusYears(1),
                    "hash"
            );

            // Act
            boolean isValida = evento.isAssinaturaValida();

            // Assert
            assertThat(isValida).isTrue();
        }

        @Test
        @DisplayName("Deve invalidar assinatura antes do início da validade")
        void deveInvalidarAssinaturaAntesDoInicio() {
            // Arrange
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    "doc-future",
                    TipoAssinatura.DIGITAL,
                    "SHA256withRSA",
                    "CERT123",
                    "Nome",
                    "12345678901",
                    LocalDate.now().plusDays(1), // Começa amanhã
                    LocalDate.now().plusYears(1),
                    "hash"
            );

            // Act
            boolean isValida = evento.isAssinaturaValida();

            // Assert
            assertThat(isValida).isFalse();
        }

        @Test
        @DisplayName("Deve invalidar assinatura após o fim da validade")
        void deveInvalidarAssinaturaAposOFim() {
            // Arrange
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    "doc-expired",
                    TipoAssinatura.DIGITAL,
                    "SHA256withRSA",
                    "CERT123",
                    "Nome",
                    "12345678901",
                    LocalDate.now().minusYears(3),
                    LocalDate.now().minusDays(1), // Expirou ontem
                    "hash"
            );

            // Act
            boolean isValida = evento.isAssinaturaValida();

            // Assert
            assertThat(isValida).isFalse();
        }

        @Test
        @DisplayName("Deve validar assinatura no dia de início da validade")
        void deveValidarAssinaturaNoDiaDeInicio() {
            // Arrange
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    "doc-starts-today",
                    TipoAssinatura.DIGITAL,
                    "SHA256withRSA",
                    "CERT123",
                    "Nome",
                    "12345678901",
                    LocalDate.now(), // Começa hoje
                    LocalDate.now().plusYears(1),
                    "hash"
            );

            // Act
            boolean isValida = evento.isAssinaturaValida();

            // Assert
            assertThat(isValida).isTrue();
        }

        @Test
        @DisplayName("Deve validar assinatura no dia de fim da validade")
        void deveValidarAssinaturaNoDiaDeFim() {
            // Arrange
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    "doc-ends-today",
                    TipoAssinatura.DIGITAL,
                    "SHA256withRSA",
                    "CERT123",
                    "Nome",
                    "12345678901",
                    LocalDate.now().minusYears(1),
                    LocalDate.now(), // Expira hoje
                    "hash"
            );

            // Act
            boolean isValida = evento.isAssinaturaValida();

            // Assert
            assertThat(isValida).isTrue();
        }

        @Test
        @DisplayName("Deve validar assinatura quando datas de validade são null")
        void deveValidarAssinaturaQuandoDatasNull() {
            // Arrange
            DocumentoAssinadoEvent evento = DocumentoAssinadoEvent.builder()
                    .documentoId("doc-no-dates")
                    .tipoAssinatura(TipoAssinatura.ELETRONICA)
                    .algoritmo("ELETRONICA")
                    .assinanteNome("Nome")
                    .assinanteCpf("12345678901")
                    .validadeInicio(null)
                    .validadeFim(null)
                    .hashDocumento("hash")
                    .build();

            // Act
            boolean isValida = evento.isAssinaturaValida();

            // Assert
            assertThat(isValida).isTrue();
        }
    }

    @Nested
    @DisplayName("Métodos Auxiliares - CPF")
    class MetodosAuxiliaresCPF {

        @Test
        @DisplayName("Deve formatar CPF válido corretamente")
        void deveFormatarCpfValidoCorretamente() {
            // Arrange
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    "doc-cpf", "Nome", "12345678901", "hash"
            );

            // Act
            String cpfFormatado = evento.getCpfFormatado();

            // Assert
            assertThat(cpfFormatado).isEqualTo("123.456.789-01");
        }

        @Test
        @DisplayName("Deve retornar CPF sem formatação quando inválido")
        void deveRetornarCpfSemFormatacaoQuandoInvalido() {
            // Arrange
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    "doc-cpf-invalid", "Nome", "123", "hash"
            );

            // Act
            String cpfFormatado = evento.getCpfFormatado();

            // Assert
            assertThat(cpfFormatado).isEqualTo("123");
        }

        @Test
        @DisplayName("Deve retornar CPF sem formatação quando null")
        void deveRetornarCpfSemFormatacaoQuandoNull() {
            // Arrange
            DocumentoAssinadoEvent evento = DocumentoAssinadoEvent.builder()
                    .documentoId("doc-no-cpf")
                    .tipoAssinatura(TipoAssinatura.ELETRONICA)
                    .assinanteNome("Nome")
                    .assinanteCpf(null)
                    .hashDocumento("hash")
                    .build();

            // Act
            String cpfFormatado = evento.getCpfFormatado();

            // Assert
            assertThat(cpfFormatado).isNull();
        }

        @Test
        @DisplayName("Deve formatar diferentes CPFs corretamente")
        void deveFormatarDiferentesCpfs() {
            // Arrange
            DocumentoAssinadoEvent evento1 = new DocumentoAssinadoEvent(
                    "doc-1", "Nome1", "11111111111", "hash1"
            );
            DocumentoAssinadoEvent evento2 = new DocumentoAssinadoEvent(
                    "doc-2", "Nome2", "99999999999", "hash2"
            );

            // Act & Assert
            assertThat(evento1.getCpfFormatado()).isEqualTo("111.111.111-11");
            assertThat(evento2.getCpfFormatado()).isEqualTo("999.999.999-99");
        }
    }

    @Nested
    @DisplayName("Métodos Auxiliares - Dias Para Expirar")
    class MetodosAuxiliaresDiasParaExpirar {

        @Test
        @DisplayName("Deve calcular dias para expirar corretamente")
        void deveCalcularDiasParaExpirarCorretamente() {
            // Arrange
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    "doc-expire",
                    TipoAssinatura.DIGITAL,
                    "SHA256withRSA",
                    "CERT",
                    "Nome",
                    "12345678901",
                    LocalDate.now().minusYears(1),
                    LocalDate.now().plusDays(30),
                    "hash"
            );

            // Act
            long diasParaExpirar = evento.diasParaExpirar();

            // Assert
            assertThat(diasParaExpirar).isEqualTo(30);
        }

        @Test
        @DisplayName("Deve retornar zero quando já expirou")
        void deveRetornarZeroQuandoJaExpirou() {
            // Arrange
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    "doc-expired",
                    TipoAssinatura.DIGITAL,
                    "SHA256withRSA",
                    "CERT",
                    "Nome",
                    "12345678901",
                    LocalDate.now().minusYears(2),
                    LocalDate.now().minusDays(1),
                    "hash"
            );

            // Act
            long diasParaExpirar = evento.diasParaExpirar();

            // Assert
            assertThat(diasParaExpirar).isZero();
        }

        @Test
        @DisplayName("Deve retornar -1 quando validadeFim é null")
        void deveRetornarMenosUmQuandoValidadeFimNull() {
            // Arrange
            DocumentoAssinadoEvent evento = DocumentoAssinadoEvent.builder()
                    .documentoId("doc-no-end")
                    .tipoAssinatura(TipoAssinatura.ELETRONICA)
                    .assinanteNome("Nome")
                    .assinanteCpf("12345678901")
                    .validadeInicio(LocalDate.now())
                    .validadeFim(null)
                    .hashDocumento("hash")
                    .build();

            // Act
            long diasParaExpirar = evento.diasParaExpirar();

            // Assert
            assertThat(diasParaExpirar).isEqualTo(-1);
        }

        @Test
        @DisplayName("Deve calcular dias para expirar para data distante")
        void deveCalcularDiasParaExpirarParaDataDistante() {
            // Arrange
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    "doc-long",
                    TipoAssinatura.DIGITAL,
                    "SHA256withRSA",
                    "CERT",
                    "Nome",
                    "12345678901",
                    LocalDate.now(),
                    LocalDate.now().plusYears(3),
                    "hash"
            );

            // Act
            long diasParaExpirar = evento.diasParaExpirar();

            // Assert
            assertThat(diasParaExpirar).isGreaterThan(1000); // Aproximadamente 3 anos
        }
    }

    @Nested
    @DisplayName("Cenários Específicos")
    class CenariosEspecificos {

        @Test
        @DisplayName("Deve criar evento para assinatura digital ICP-Brasil")
        void deveCriarEventoParaAssinaturaDigitalICPBrasil() {
            // Arrange & Act
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    "doc-icp",
                    TipoAssinatura.DIGITAL,
                    "SHA256withRSA",
                    "MIIEowIBAAKCAQEAwJkB...", // Certificado exemplo
                    "Roberto Alves",
                    "11122233344",
                    LocalDate.of(2023, 1, 1),
                    LocalDate.of(2026, 1, 1),
                    "hashDocICP123"
            );

            // Assert
            assertThat(evento.getTipoAssinatura()).isEqualTo(TipoAssinatura.DIGITAL);
            assertThat(evento.requerCertificado()).isTrue();
            assertThat(evento.possuiCertificado()).isTrue();
            assertThat(evento.getAlgoritmo()).contains("SHA256");
        }

        @Test
        @DisplayName("Deve criar evento para assinatura eletrônica simples")
        void deveCriarEventoParaAssinaturaEletronicaSimples() {
            // Arrange & Act
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    "doc-eletronica",
                    "Fernanda Costa",
                    "55566677788",
                    "hashEletronica456"
            );

            // Assert
            assertThat(evento.getTipoAssinatura()).isEqualTo(TipoAssinatura.ELETRONICA);
            assertThat(evento.requerCertificado()).isFalse();
            assertThat(evento.possuiCertificado()).isFalse();
            assertThat(evento.getAlgoritmo()).isEqualTo("ELETRONICA");
        }

        @Test
        @DisplayName("Deve criar evento para assinatura física digitalizada")
        void deveCriarEventoParaAssinaturaFisicaDigitalizada() {
            // Arrange & Act
            DocumentoAssinadoEvent evento = DocumentoAssinadoEvent.builder()
                    .documentoId("doc-fisica")
                    .tipoAssinatura(TipoAssinatura.FISICA_DIGITALIZADA)
                    .algoritmo("NONE")
                    .certificado(null)
                    .assinanteNome("Lucia Mendes")
                    .assinanteCpf("77788899900")
                    .validadeInicio(LocalDate.now())
                    .validadeFim(null) // Sem validade
                    .hashDocumento("hashFisica789")
                    .build();

            // Assert
            assertThat(evento.getTipoAssinatura()).isEqualTo(TipoAssinatura.FISICA_DIGITALIZADA);
            assertThat(evento.requerCertificado()).isFalse();
            assertThat(evento.possuiCertificado()).isFalse();
        }

        @Test
        @DisplayName("Deve criar evento com certificado próximo à expiração")
        void deveCriarEventoComCertificadoProximoExpiracao() {
            // Arrange & Act
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    "doc-expiring",
                    TipoAssinatura.DIGITAL,
                    "SHA256withRSA",
                    "CERT_EXPIRING",
                    "Marcos Silva",
                    "12312312312",
                    LocalDate.now().minusYears(2),
                    LocalDate.now().plusDays(15), // Expira em 15 dias
                    "hashExpiring"
            );

            // Assert
            assertThat(evento.isAssinaturaValida()).isTrue();
            assertThat(evento.diasParaExpirar()).isEqualTo(15);
        }

        @Test
        @DisplayName("Deve criar evento com certificado expirado")
        void deveCriarEventoComCertificadoExpirado() {
            // Arrange & Act
            DocumentoAssinadoEvent evento = new DocumentoAssinadoEvent(
                    "doc-expired",
                    TipoAssinatura.DIGITAL,
                    "SHA256withRSA",
                    "CERT_EXPIRED",
                    "Paula Santos",
                    "45645645645",
                    LocalDate.of(2020, 1, 1),
                    LocalDate.of(2023, 1, 1), // Expirado
                    "hashExpired"
            );

            // Assert
            assertThat(evento.isAssinaturaValida()).isFalse();
            assertThat(evento.diasParaExpirar()).isZero();
        }

        @Test
        @DisplayName("Deve criar evento com diferentes algoritmos de assinatura")
        void deveCriarEventoComDiferentesAlgoritmos() {
            // Arrange & Act
            DocumentoAssinadoEvent eventoSHA256 = DocumentoAssinadoEvent.builder()
                    .documentoId("doc-sha256")
                    .tipoAssinatura(TipoAssinatura.DIGITAL)
                    .algoritmo("SHA256withRSA")
                    .certificado("CERT1")
                    .assinanteNome("Assinante 1")
                    .assinanteCpf("11111111111")
                    .validadeInicio(LocalDate.now())
                    .validadeFim(LocalDate.now().plusYears(1))
                    .hashDocumento("hash1")
                    .build();

            DocumentoAssinadoEvent eventoSHA512 = DocumentoAssinadoEvent.builder()
                    .documentoId("doc-sha512")
                    .tipoAssinatura(TipoAssinatura.DIGITAL)
                    .algoritmo("SHA512withRSA")
                    .certificado("CERT2")
                    .assinanteNome("Assinante 2")
                    .assinanteCpf("22222222222")
                    .validadeInicio(LocalDate.now())
                    .validadeFim(LocalDate.now().plusYears(1))
                    .hashDocumento("hash2")
                    .build();

            // Assert
            assertThat(eventoSHA256.getAlgoritmo()).isEqualTo("SHA256withRSA");
            assertThat(eventoSHA512.getAlgoritmo()).isEqualTo("SHA512withRSA");
        }
    }
}
