package com.seguradora.hibrida.domain.apolice.query.service;

import com.seguradora.hibrida.domain.apolice.model.StatusApolice;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceDetailView;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceListView;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceVencimentoView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link ApoliceQueryService}.
 *
 * <p>Valida a interface do serviço de consulta de apólices,
 * verificando assinaturas de métodos e contratos esperados.</p>
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("ApoliceQueryService - Testes Unitários")
class ApoliceQueryServiceTest {

    @Nested
    @DisplayName("Validação da Interface")
    class ValidacaoInterface {

        @Test
        @DisplayName("Interface deve existir e ser pública")
        void interfaceDeveExistirESerPublica() {
            // Assert
            assertThat(ApoliceQueryService.class).isInterface();
            assertThat(ApoliceQueryService.class).isPublic();
        }

        @Test
        @DisplayName("Interface deve ter métodos esperados")
        void interfaceDeveTerMetodosEsperados() {
            // Act
            Method[] methods = ApoliceQueryService.class.getDeclaredMethods();

            // Assert
            assertThat(methods).hasSizeGreaterThanOrEqualTo(20);
        }
    }

    @Nested
    @DisplayName("Consultas Básicas")
    class ConsultasBasicas {

        @Test
        @DisplayName("Deve ter método buscarPorId")
        void deveTerMetodoBuscarPorId() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarPorId", String.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Optional.class);
        }

        @Test
        @DisplayName("Deve ter método buscarPorNumero")
        void deveTerMetodoBuscarPorNumero() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarPorNumero", String.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Optional.class);
        }

        @Test
        @DisplayName("Deve ter método listarTodas")
        void deveTerMetodoListarTodas() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("listarTodas", Pageable.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Page.class);
        }
    }

    @Nested
    @DisplayName("Consultas por Segurado")
    class ConsultasPorSegurado {

        @Test
        @DisplayName("Deve ter método buscarPorCpfSegurado")
        void deveTerMetodoBuscarPorCpfSegurado() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarPorCpfSegurado", String.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Deve ter método buscarAtivasPorCpfSegurado")
        void deveTerMetodoBuscarAtivasPorCpfSegurado() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarAtivasPorCpfSegurado", String.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Deve ter método buscarPorNomeSegurado")
        void deveTerMetodoBuscarPorNomeSegurado() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarPorNomeSegurado", String.class, Pageable.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Page.class);
        }
    }

    @Nested
    @DisplayName("Consultas por Status")
    class ConsultasPorStatus {

        @Test
        @DisplayName("Deve ter método buscarPorStatus")
        void deveTerMetodoBuscarPorStatus() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarPorStatus", StatusApolice.class, Pageable.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Page.class);
        }

        @Test
        @DisplayName("Deve ter método buscarAtivas")
        void deveTerMetodoBuscarAtivas() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarAtivas", Pageable.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Page.class);
        }

        @Test
        @DisplayName("Deve ter método buscarVencidas")
        void deveTerMetodoBuscarVencidas() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarVencidas", Pageable.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Page.class);
        }
    }

    @Nested
    @DisplayName("Consultas por Vencimento")
    class ConsultasPorVencimento {

        @Test
        @DisplayName("Deve ter método buscarVencendoEntre")
        void deveTerMetodoBuscarVencendoEntre() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarVencendoEntre", LocalDate.class, LocalDate.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Deve ter método buscarVencendoEm")
        void deveTerMetodoBuscarVencendoEm() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarVencendoEm", int.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Deve ter método buscarComVencimentoProximo")
        void deveTerMetodoBuscarComVencimentoProximo() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarComVencimentoProximo");

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
            assertThat(method.getParameterCount()).isZero();
        }
    }

    @Nested
    @DisplayName("Consultas por Produto")
    class ConsultasPorProduto {

        @Test
        @DisplayName("Deve ter método buscarPorProduto")
        void deveTerMetodoBuscarPorProduto() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarPorProduto", String.class, Pageable.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Page.class);
        }
    }

    @Nested
    @DisplayName("Consultas por Cobertura")
    class ConsultasPorCobertura {

        @Test
        @DisplayName("Deve ter método buscarPorCobertura")
        void deveTerMetodoBuscarPorCobertura() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarPorCobertura", TipoCobertura.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Deve ter método buscarComCoberturaTotal")
        void deveTerMetodoBuscarComCoberturaTotal() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarComCoberturaTotal");

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
            assertThat(method.getParameterCount()).isZero();
        }
    }

    @Nested
    @DisplayName("Consultas por Valor")
    class ConsultasPorValor {

        @Test
        @DisplayName("Deve ter método buscarPorFaixaValor")
        void deveTerMetodoBuscarPorFaixaValor() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod(
                "buscarPorFaixaValor", BigDecimal.class, BigDecimal.class, Pageable.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Page.class);
        }

        @Test
        @DisplayName("Deve ter método buscarAltoValor")
        void deveTerMetodoBuscarAltoValor() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarAltoValor", BigDecimal.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }
    }

    @Nested
    @DisplayName("Consultas de Renovação")
    class ConsultasRenovacao {

        @Test
        @DisplayName("Deve ter método buscarElegiveisRenovacaoAutomatica")
        void deveTerMetodoBuscarElegiveisRenovacaoAutomatica() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarElegiveisRenovacaoAutomatica");

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
            assertThat(method.getParameterCount()).isZero();
        }

        @Test
        @DisplayName("Deve ter método buscarPrecisandoAtencaoRenovacao")
        void deveTerMetodoBuscarPrecisandoAtencaoRenovacao() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarPrecisandoAtencaoRenovacao");

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
            assertThat(method.getParameterCount()).isZero();
        }

        @Test
        @DisplayName("Deve ter método buscarPorScoreRenovacao")
        void deveTerMetodoBuscarPorScoreRenovacao() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarPorScoreRenovacao", int.class, int.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }
    }

    @Nested
    @DisplayName("Consultas por Localização")
    class ConsultasPorLocalizacao {

        @Test
        @DisplayName("Deve ter método buscarPorCidade")
        void deveTerMetodoBuscarPorCidade() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarPorCidade", String.class, Pageable.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Page.class);
        }

        @Test
        @DisplayName("Deve ter método buscarPorEstado")
        void deveTerMetodoBuscarPorEstado() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("buscarPorEstado", String.class, Pageable.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Page.class);
        }
    }

    @Nested
    @DisplayName("Consultas Customizadas")
    class ConsultasCustomizadas {

        @Test
        @DisplayName("Deve ter método buscarComFiltros")
        void deveTerMetodoBuscarComFiltros() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod(
                "buscarComFiltros",
                StatusApolice.class,
                String.class,
                String.class,
                LocalDate.class,
                LocalDate.class,
                BigDecimal.class,
                BigDecimal.class,
                Pageable.class
            );

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Page.class);
            assertThat(method.getParameterCount()).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("Verificações")
    class Verificacoes {

        @Test
        @DisplayName("Deve ter método existeComNumero")
        void deveTerMetodoExisteComNumero() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("existeComNumero", String.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("Deve ter método contarAtivasPorCpf")
        void deveTerMetodoContarAtivasPorCpf() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("contarAtivasPorCpf", String.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(long.class);
        }

        @Test
        @DisplayName("Deve ter método seguradoPossuiApolicesAtivas")
        void deveTerMetodoSeguradoPossuiApolicesAtivas() throws NoSuchMethodException {
            // Act
            Method method = ApoliceQueryService.class.getDeclaredMethod("seguradoPossuiApolicesAtivas", String.class);

            // Assert
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(boolean.class);
        }
    }

    @Nested
    @DisplayName("Validação de Tipos de Retorno")
    class ValidacaoTiposRetorno {

        @Test
        @DisplayName("Métodos de consulta única devem retornar Optional")
        void metodosConsultaUnicaDevemRetornarOptional() throws NoSuchMethodException {
            // Arrange
            Method buscarPorId = ApoliceQueryService.class.getDeclaredMethod("buscarPorId", String.class);
            Method buscarPorNumero = ApoliceQueryService.class.getDeclaredMethod("buscarPorNumero", String.class);

            // Assert
            assertThat(buscarPorId.getReturnType()).isEqualTo(Optional.class);
            assertThat(buscarPorNumero.getReturnType()).isEqualTo(Optional.class);
        }

        @Test
        @DisplayName("Métodos de listagem devem retornar List ou Page")
        void metodosListagemDevemRetornarListOuPage() throws NoSuchMethodException {
            // Arrange
            Method buscarPorCpfSegurado = ApoliceQueryService.class.getDeclaredMethod("buscarPorCpfSegurado", String.class);
            Method listarTodas = ApoliceQueryService.class.getDeclaredMethod("listarTodas", Pageable.class);

            // Assert
            assertThat(buscarPorCpfSegurado.getReturnType()).isEqualTo(List.class);
            assertThat(listarTodas.getReturnType()).isEqualTo(Page.class);
        }

        @Test
        @DisplayName("Métodos de verificação devem retornar boolean ou long")
        void metodosVerificacaoDevemRetornarBooleanOuLong() throws NoSuchMethodException {
            // Arrange
            Method existeComNumero = ApoliceQueryService.class.getDeclaredMethod("existeComNumero", String.class);
            Method contarAtivasPorCpf = ApoliceQueryService.class.getDeclaredMethod("contarAtivasPorCpf", String.class);
            Method seguradoPossuiAtivas = ApoliceQueryService.class.getDeclaredMethod("seguradoPossuiApolicesAtivas", String.class);

            // Assert
            assertThat(existeComNumero.getReturnType()).isEqualTo(boolean.class);
            assertThat(contarAtivasPorCpf.getReturnType()).isEqualTo(long.class);
            assertThat(seguradoPossuiAtivas.getReturnType()).isEqualTo(boolean.class);
        }
    }

    @Nested
    @DisplayName("Validação de Parâmetros")
    class ValidacaoParametros {

        @Test
        @DisplayName("Métodos paginados devem aceitar Pageable")
        void metodosPaginadosDevemAceitarPageable() throws NoSuchMethodException {
            // Arrange
            Method listarTodas = ApoliceQueryService.class.getDeclaredMethod("listarTodas", Pageable.class);
            Method buscarPorStatus = ApoliceQueryService.class.getDeclaredMethod("buscarPorStatus", StatusApolice.class, Pageable.class);

            // Assert
            assertThat(listarTodas.getParameterTypes()).contains(Pageable.class);
            assertThat(buscarPorStatus.getParameterTypes()).contains(Pageable.class);
        }

        @Test
        @DisplayName("Métodos de busca por identificador devem aceitar String")
        void metodosBuscaPorIdentificadorDevemAceitarString() throws NoSuchMethodException {
            // Arrange
            Method buscarPorId = ApoliceQueryService.class.getDeclaredMethod("buscarPorId", String.class);
            Method buscarPorNumero = ApoliceQueryService.class.getDeclaredMethod("buscarPorNumero", String.class);
            Method buscarPorCpfSegurado = ApoliceQueryService.class.getDeclaredMethod("buscarPorCpfSegurado", String.class);

            // Assert
            assertThat(buscarPorId.getParameterTypes()[0]).isEqualTo(String.class);
            assertThat(buscarPorNumero.getParameterTypes()[0]).isEqualTo(String.class);
            assertThat(buscarPorCpfSegurado.getParameterTypes()[0]).isEqualTo(String.class);
        }

        @Test
        @DisplayName("Métodos de busca por período devem aceitar LocalDate")
        void metodosBuscaPorPeriodoDevemAceitarLocalDate() throws NoSuchMethodException {
            // Arrange
            Method buscarVencendoEntre = ApoliceQueryService.class.getDeclaredMethod(
                "buscarVencendoEntre", LocalDate.class, LocalDate.class);

            // Assert
            assertThat(buscarVencendoEntre.getParameterTypes()[0]).isEqualTo(LocalDate.class);
            assertThat(buscarVencendoEntre.getParameterTypes()[1]).isEqualTo(LocalDate.class);
        }

        @Test
        @DisplayName("Métodos de busca por valor devem aceitar BigDecimal")
        void metodosBuscaPorValorDevemAceitarBigDecimal() throws NoSuchMethodException {
            // Arrange
            Method buscarPorFaixaValor = ApoliceQueryService.class.getDeclaredMethod(
                "buscarPorFaixaValor", BigDecimal.class, BigDecimal.class, Pageable.class);
            Method buscarAltoValor = ApoliceQueryService.class.getDeclaredMethod("buscarAltoValor", BigDecimal.class);

            // Assert
            assertThat(buscarPorFaixaValor.getParameterTypes()[0]).isEqualTo(BigDecimal.class);
            assertThat(buscarAltoValor.getParameterTypes()[0]).isEqualTo(BigDecimal.class);
        }
    }

    @Nested
    @DisplayName("Validação de Implementação")
    class ValidacaoImplementacao {

        @Test
        @DisplayName("Interface pode ser implementada")
        void interfacePodeSerImplementada() {
            // Arrange
            class TestImplementation implements ApoliceQueryService {
                @Override public Optional<ApoliceDetailView> buscarPorId(String id) { return Optional.empty(); }
                @Override public Optional<ApoliceDetailView> buscarPorNumero(String numero) { return Optional.empty(); }
                @Override public Page<ApoliceListView> listarTodas(Pageable pageable) { return Page.empty(); }
                @Override public List<ApoliceListView> buscarPorCpfSegurado(String cpf) { return List.of(); }
                @Override public List<ApoliceListView> buscarAtivasPorCpfSegurado(String cpf) { return List.of(); }
                @Override public Page<ApoliceListView> buscarPorNomeSegurado(String nome, Pageable pageable) { return Page.empty(); }
                @Override public Page<ApoliceListView> buscarPorStatus(StatusApolice status, Pageable pageable) { return Page.empty(); }
                @Override public Page<ApoliceListView> buscarAtivas(Pageable pageable) { return Page.empty(); }
                @Override public Page<ApoliceListView> buscarVencidas(Pageable pageable) { return Page.empty(); }
                @Override public List<ApoliceVencimentoView> buscarVencendoEntre(LocalDate inicio, LocalDate fim) { return List.of(); }
                @Override public List<ApoliceVencimentoView> buscarVencendoEm(int dias) { return List.of(); }
                @Override public List<ApoliceVencimentoView> buscarComVencimentoProximo() { return List.of(); }
                @Override public Page<ApoliceListView> buscarPorProduto(String produto, Pageable pageable) { return Page.empty(); }
                @Override public List<ApoliceListView> buscarPorCobertura(TipoCobertura cobertura) { return List.of(); }
                @Override public List<ApoliceListView> buscarComCoberturaTotal() { return List.of(); }
                @Override public Page<ApoliceListView> buscarPorFaixaValor(BigDecimal min, BigDecimal max, Pageable pageable) { return Page.empty(); }
                @Override public List<ApoliceListView> buscarAltoValor(BigDecimal valorMinimo) { return List.of(); }
                @Override public List<ApoliceVencimentoView> buscarElegiveisRenovacaoAutomatica() { return List.of(); }
                @Override public List<ApoliceVencimentoView> buscarPrecisandoAtencaoRenovacao() { return List.of(); }
                @Override public List<ApoliceListView> buscarPorScoreRenovacao(int min, int max) { return List.of(); }
                @Override public Page<ApoliceListView> buscarPorCidade(String cidade, Pageable pageable) { return Page.empty(); }
                @Override public Page<ApoliceListView> buscarPorEstado(String estado, Pageable pageable) { return Page.empty(); }
                @Override public Page<ApoliceListView> buscarComFiltros(StatusApolice status, String produto, String cpf, LocalDate vi, LocalDate vf, BigDecimal vmin, BigDecimal vmax, Pageable p) { return Page.empty(); }
                @Override public boolean existeComNumero(String numero) { return false; }
                @Override public long contarAtivasPorCpf(String cpf) { return 0; }
                @Override public boolean seguradoPossuiApolicesAtivas(String cpf) { return false; }
            }

            // Act
            TestImplementation impl = new TestImplementation();

            // Assert
            assertThat(impl).isNotNull();
            assertThat(impl).isInstanceOf(ApoliceQueryService.class);
        }

        @Test
        @DisplayName("Todos os métodos são públicos")
        void todosOsMetodosSaoPublicos() {
            // Act
            Method[] methods = ApoliceQueryService.class.getDeclaredMethods();

            // Assert
            assertThat(methods).allMatch(method ->
                java.lang.reflect.Modifier.isPublic(method.getModifiers()));
        }
    }
}
