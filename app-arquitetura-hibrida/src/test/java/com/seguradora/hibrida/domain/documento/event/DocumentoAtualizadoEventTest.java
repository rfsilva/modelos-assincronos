package com.seguradora.hibrida.domain.documento.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link DocumentoAtualizadoEvent}.
 *
 * <p>Valida a criação e comportamento do evento de documento atualizado,
 * incluindo:
 * <ul>
 *   <li>Construção com todos os campos de versionamento</li>
 *   <li>Métodos de validação de conteúdo alterado</li>
 *   <li>Cálculo de diferenças de tamanho</li>
 *   <li>Formatação de versões</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("DocumentoAtualizadoEvent - Testes Unitários")
class DocumentoAtualizadoEventTest {

    /**
     * Cria um evento de documento atualizado válido para testes.
     *
     * @return Evento atualizado com dados de exemplo
     */
    private DocumentoAtualizadoEvent criarEventoPadrao() {
        return new DocumentoAtualizadoEvent(
                "doc-123",
                2,
                "Correção de informações",
                "hashAntigo123",
                "hashNovo456",
                6291456L, // 6 MB
                "/storage/docs/doc-123-v2.pdf",
                "op-789"
        );
    }

    @Nested
    @DisplayName("Criação e Validação")
    class CriacaoValidacao {

        @Test
        @DisplayName("Deve criar evento com construtor completo")
        void deveCriarEventoComConstrutorCompleto() {
            // Arrange & Act
            DocumentoAtualizadoEvent evento = new DocumentoAtualizadoEvent(
                    "doc-001",
                    3,
                    "Atualização de valores",
                    "oldHash",
                    "newHash",
                    7340032L, // 7 MB
                    "/storage/docs/doc-001-v3.pdf",
                    "op-001"
            );

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.getDocumentoId()).isEqualTo("doc-001");
            assertThat(evento.getNovaVersao()).isEqualTo(3);
            assertThat(evento.getMotivo()).isEqualTo("Atualização de valores");
            assertThat(evento.getHashAnterior()).isEqualTo("oldHash");
            assertThat(evento.getHashNovo()).isEqualTo("newHash");
            assertThat(evento.getNovoTamanho()).isEqualTo(7340032L);
            assertThat(evento.getNovoConteudoPath()).isEqualTo("/storage/docs/doc-001-v3.pdf");
            assertThat(evento.getOperadorId()).isEqualTo("op-001");
        }

        @Test
        @DisplayName("Deve criar evento com builder do Lombok")
        void deveCriarEventoComBuilder() {
            // Arrange & Act
            DocumentoAtualizadoEvent evento = DocumentoAtualizadoEvent.builder()
                    .documentoId("doc-002")
                    .novaVersao(4)
                    .motivo("Revisão técnica")
                    .hashAnterior("prevHash")
                    .hashNovo("currentHash")
                    .novoTamanho(8388608L) // 8 MB
                    .novoConteudoPath("/storage/docs/doc-002-v4.pdf")
                    .operadorId("op-002")
                    .build();

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.getDocumentoId()).isEqualTo("doc-002");
            assertThat(evento.getNovaVersao()).isEqualTo(4);
            assertThat(evento.getMotivo()).isEqualTo("Revisão técnica");
        }

        @Test
        @DisplayName("Deve criar evento sem chamar super() explicitamente")
        void deveCriarEventoComNoArgsConstructor() {
            // Arrange & Act
            DocumentoAtualizadoEvent evento = new DocumentoAtualizadoEvent();

            // Assert
            assertThat(evento).isNotNull();
        }

        @Test
        @DisplayName("Deve preservar todos os campos após criação")
        void devePreservarTodosOsCampos() {
            // Arrange
            String documentoId = "doc-preserve";
            int novaVersao = 5;
            String motivo = "Motivo de preservação";
            String hashAnterior = "hashOld999";
            String hashNovo = "hashNew999";
            long novoTamanho = 9437184L;
            String novoConteudoPath = "/storage/preserve/v5.pdf";
            String operadorId = "op-preserve";

            // Act
            DocumentoAtualizadoEvent evento = new DocumentoAtualizadoEvent(
                    documentoId, novaVersao, motivo, hashAnterior, hashNovo,
                    novoTamanho, novoConteudoPath, operadorId
            );

            // Assert
            assertThat(evento.getDocumentoId()).isEqualTo(documentoId);
            assertThat(evento.getNovaVersao()).isEqualTo(novaVersao);
            assertThat(evento.getMotivo()).isEqualTo(motivo);
            assertThat(evento.getHashAnterior()).isEqualTo(hashAnterior);
            assertThat(evento.getHashNovo()).isEqualTo(hashNovo);
            assertThat(evento.getNovoTamanho()).isEqualTo(novoTamanho);
            assertThat(evento.getNovoConteudoPath()).isEqualTo(novoConteudoPath);
            assertThat(evento.getOperadorId()).isEqualTo(operadorId);
        }
    }

    @Nested
    @DisplayName("Propriedades do Evento")
    class PropriedadesEvento {

        @Test
        @DisplayName("Deve ter eventType correto")
        void deveTerEventTypeCorreto() {
            // Arrange
            DocumentoAtualizadoEvent evento = criarEventoPadrao();

            // Act
            String eventType = evento.getEventType();

            // Assert
            assertThat(eventType).isEqualTo("DocumentoAtualizadoEvent");
        }

        @Test
        @DisplayName("Deve permitir acesso a todos os getters")
        void devePermitirAcessoATodosOsGetters() {
            // Arrange
            DocumentoAtualizadoEvent evento = criarEventoPadrao();

            // Act & Assert
            assertThat(evento.getDocumentoId()).isNotNull();
            assertThat(evento.getNovaVersao()).isPositive();
            assertThat(evento.getMotivo()).isNotNull();
            assertThat(evento.getHashAnterior()).isNotNull();
            assertThat(evento.getHashNovo()).isNotNull();
            assertThat(evento.getNovoTamanho()).isPositive();
            assertThat(evento.getNovoConteudoPath()).isNotNull();
            assertThat(evento.getOperadorId()).isNotNull();
        }

        @Test
        @DisplayName("Deve ter toString implementado")
        void deveTerToStringImplementado() {
            // Arrange
            DocumentoAtualizadoEvent evento = criarEventoPadrao();

            // Act
            String toString = evento.toString();

            // Assert
            assertThat(toString).isNotNull();
            assertThat(toString).contains("DocumentoAtualizadoEvent");
        }
    }

    @Nested
    @DisplayName("Métodos Auxiliares")
    class MetodosAuxiliares {

        @Test
        @DisplayName("Deve detectar conteúdo alterado quando hashes são diferentes")
        void deveDetectarConteudoAlterado() {
            // Arrange
            DocumentoAtualizadoEvent evento = new DocumentoAtualizadoEvent(
                    "doc-001",
                    2,
                    "Alteração de conteúdo",
                    "hashDiferente1",
                    "hashDiferente2",
                    5242880L,
                    "/storage/v2.pdf",
                    "op-001"
            );

            // Act
            boolean conteudoAlterado = evento.conteudoAlterado();

            // Assert
            assertThat(conteudoAlterado).isTrue();
        }

        @Test
        @DisplayName("Deve detectar conteúdo não alterado quando hashes são iguais")
        void deveDetectarConteudoNaoAlterado() {
            // Arrange
            DocumentoAtualizadoEvent evento = new DocumentoAtualizadoEvent(
                    "doc-002",
                    2,
                    "Atualização de metadados apenas",
                    "hashIgual123",
                    "hashIgual123",
                    5242880L,
                    "/storage/v2.pdf",
                    "op-002"
            );

            // Act
            boolean conteudoAlterado = evento.conteudoAlterado();

            // Assert
            assertThat(conteudoAlterado).isFalse();
        }

        @Test
        @DisplayName("Deve calcular diferença positiva de tamanho (aumento)")
        void deveCalcularDiferencaPositivaDeTamanho() {
            // Arrange
            DocumentoAtualizadoEvent evento = new DocumentoAtualizadoEvent(
                    "doc-003",
                    2,
                    "Adição de conteúdo",
                    "hashOld",
                    "hashNew",
                    7340032L, // 7 MB
                    "/storage/v2.pdf",
                    "op-003"
            );
            long tamanhoAnterior = 5242880L; // 5 MB

            // Act
            long diferenca = evento.calcularDiferencaTamanho(tamanhoAnterior);

            // Assert
            assertThat(diferenca).isEqualTo(2097152L); // 2 MB de aumento
            assertThat(diferenca).isPositive();
        }

        @Test
        @DisplayName("Deve calcular diferença negativa de tamanho (redução)")
        void deveCalcularDiferencaNegativaDeTamanho() {
            // Arrange
            DocumentoAtualizadoEvent evento = new DocumentoAtualizadoEvent(
                    "doc-004",
                    2,
                    "Compressão de arquivo",
                    "hashOld",
                    "hashNew",
                    3145728L, // 3 MB
                    "/storage/v2.pdf",
                    "op-004"
            );
            long tamanhoAnterior = 5242880L; // 5 MB

            // Act
            long diferenca = evento.calcularDiferencaTamanho(tamanhoAnterior);

            // Assert
            assertThat(diferenca).isEqualTo(-2097152L); // 2 MB de redução
            assertThat(diferenca).isNegative();
        }

        @Test
        @DisplayName("Deve calcular diferença zero quando tamanho não muda")
        void deveCalcularDiferencaZeroDeTamanho() {
            // Arrange
            DocumentoAtualizadoEvent evento = new DocumentoAtualizadoEvent(
                    "doc-005",
                    2,
                    "Atualização sem mudança de tamanho",
                    "hashOld",
                    "hashNew",
                    5242880L, // 5 MB
                    "/storage/v2.pdf",
                    "op-005"
            );
            long tamanhoAnterior = 5242880L; // 5 MB

            // Act
            long diferenca = evento.calcularDiferencaTamanho(tamanhoAnterior);

            // Assert
            assertThat(diferenca).isZero();
        }

        @Test
        @DisplayName("Deve formatar versão corretamente")
        void deveFormatarVersaoCorretamente() {
            // Arrange
            DocumentoAtualizadoEvent evento = new DocumentoAtualizadoEvent(
                    "doc-006",
                    2,
                    "Teste formatação",
                    "hashOld",
                    "hashNew",
                    5242880L,
                    "/storage/v2.pdf",
                    "op-006"
            );

            // Act
            String versaoFormatada = evento.getVersaoFormatada();

            // Assert
            assertThat(versaoFormatada).isEqualTo("v2.0");
        }

        @Test
        @DisplayName("Deve formatar diferentes números de versão")
        void deveFormatarDiferentesNumeros() {
            // Arrange
            DocumentoAtualizadoEvent evento1 = DocumentoAtualizadoEvent.builder()
                    .documentoId("doc-v1")
                    .novaVersao(1)
                    .motivo("Primeira versão")
                    .hashAnterior("hash0")
                    .hashNovo("hash1")
                    .novoTamanho(1048576L)
                    .novoConteudoPath("/storage/v1.pdf")
                    .operadorId("op-001")
                    .build();

            DocumentoAtualizadoEvent evento10 = DocumentoAtualizadoEvent.builder()
                    .documentoId("doc-v10")
                    .novaVersao(10)
                    .motivo("Décima versão")
                    .hashAnterior("hash9")
                    .hashNovo("hash10")
                    .novoTamanho(1048576L)
                    .novoConteudoPath("/storage/v10.pdf")
                    .operadorId("op-010")
                    .build();

            DocumentoAtualizadoEvent evento100 = DocumentoAtualizadoEvent.builder()
                    .documentoId("doc-v100")
                    .novaVersao(100)
                    .motivo("Centésima versão")
                    .hashAnterior("hash99")
                    .hashNovo("hash100")
                    .novoTamanho(1048576L)
                    .novoConteudoPath("/storage/v100.pdf")
                    .operadorId("op-100")
                    .build();

            // Act & Assert
            assertThat(evento1.getVersaoFormatada()).isEqualTo("v1.0");
            assertThat(evento10.getVersaoFormatada()).isEqualTo("v10.0");
            assertThat(evento100.getVersaoFormatada()).isEqualTo("v100.0");
        }
    }

    @Nested
    @DisplayName("Cenários Específicos")
    class CenariosEspecificos {

        @Test
        @DisplayName("Deve criar evento para primeira atualização")
        void deveCriarEventoParaPrimeiraAtualizacao() {
            // Arrange & Act
            DocumentoAtualizadoEvent evento = new DocumentoAtualizadoEvent(
                    "doc-first-update",
                    2, // Segunda versão (primeira atualização)
                    "Primeira atualização do documento",
                    "hashOriginal",
                    "hashPrimeiraAtualizacao",
                    5242880L,
                    "/storage/docs/doc-first-update-v2.pdf",
                    "op-first"
            );

            // Assert
            assertThat(evento.getNovaVersao()).isEqualTo(2);
            assertThat(evento.getVersaoFormatada()).isEqualTo("v2.0");
            assertThat(evento.conteudoAlterado()).isTrue();
        }

        @Test
        @DisplayName("Deve criar evento para múltiplas atualizações")
        void deveCriarEventoParaMultiplasAtualizacoes() {
            // Arrange & Act
            DocumentoAtualizadoEvent evento = new DocumentoAtualizadoEvent(
                    "doc-multi-update",
                    15, // Décima quinta versão
                    "Atualização recorrente",
                    "hash14",
                    "hash15",
                    5242880L,
                    "/storage/docs/doc-multi-update-v15.pdf",
                    "op-multi"
            );

            // Assert
            assertThat(evento.getNovaVersao()).isEqualTo(15);
            assertThat(evento.getVersaoFormatada()).isEqualTo("v15.0");
        }

        @Test
        @DisplayName("Deve criar evento com diferentes motivos de atualização")
        void deveCriarEventoComDiferentesMotivos() {
            // Arrange & Act
            DocumentoAtualizadoEvent eventoCorrecao = new DocumentoAtualizadoEvent(
                    "doc-1", 2, "Correção de erros",
                    "hash1", "hash2", 5242880L, "/storage/v2.pdf", "op-1"
            );

            DocumentoAtualizadoEvent eventoRevisao = new DocumentoAtualizadoEvent(
                    "doc-2", 2, "Revisão técnica completa",
                    "hash1", "hash2", 5242880L, "/storage/v2.pdf", "op-2"
            );

            DocumentoAtualizadoEvent eventoAtualizacao = new DocumentoAtualizadoEvent(
                    "doc-3", 2, "Atualização de valores e informações",
                    "hash1", "hash2", 5242880L, "/storage/v2.pdf", "op-3"
            );

            // Assert
            assertThat(eventoCorrecao.getMotivo()).contains("Correção");
            assertThat(eventoRevisao.getMotivo()).contains("Revisão");
            assertThat(eventoAtualizacao.getMotivo()).contains("Atualização");
        }

        @Test
        @DisplayName("Deve criar evento com aumento significativo de tamanho")
        void deveCriarEventoComAumentoSignificativo() {
            // Arrange
            DocumentoAtualizadoEvent evento = new DocumentoAtualizadoEvent(
                    "doc-big-increase",
                    2,
                    "Adição de anexos",
                    "hashSmall",
                    "hashLarge",
                    20971520L, // 20 MB
                    "/storage/large-v2.pdf",
                    "op-big"
            );
            long tamanhoAnterior = 2097152L; // 2 MB

            // Act
            long diferenca = evento.calcularDiferencaTamanho(tamanhoAnterior);

            // Assert
            assertThat(diferenca).isEqualTo(18874368L); // ~18 MB de aumento
            assertThat(evento.getNovoTamanho()).isGreaterThan(tamanhoAnterior);
        }

        @Test
        @DisplayName("Deve criar evento com redução significativa de tamanho")
        void deveCriarEventoComReducaoSignificativa() {
            // Arrange
            DocumentoAtualizadoEvent evento = new DocumentoAtualizadoEvent(
                    "doc-big-decrease",
                    2,
                    "Compressão e otimização",
                    "hashLarge",
                    "hashSmall",
                    2097152L, // 2 MB
                    "/storage/small-v2.pdf",
                    "op-compress"
            );
            long tamanhoAnterior = 20971520L; // 20 MB

            // Act
            long diferenca = evento.calcularDiferencaTamanho(tamanhoAnterior);

            // Assert
            assertThat(diferenca).isEqualTo(-18874368L); // ~18 MB de redução
            assertThat(evento.getNovoTamanho()).isLessThan(tamanhoAnterior);
        }

        @Test
        @DisplayName("Deve criar evento para atualização de metadados sem mudança de conteúdo")
        void deveCriarEventoParaAtualizacaoMetadados() {
            // Arrange & Act
            DocumentoAtualizadoEvent evento = new DocumentoAtualizadoEvent(
                    "doc-metadata",
                    2,
                    "Atualização apenas de metadados",
                    "hashSameContent",
                    "hashSameContent", // Mesmo hash
                    5242880L,
                    "/storage/metadata-v2.pdf",
                    "op-metadata"
            );

            // Assert
            assertThat(evento.conteudoAlterado()).isFalse();
            assertThat(evento.getHashAnterior()).isEqualTo(evento.getHashNovo());
        }
    }
}
