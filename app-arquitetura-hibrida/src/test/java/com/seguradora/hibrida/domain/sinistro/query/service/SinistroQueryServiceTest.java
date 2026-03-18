package com.seguradora.hibrida.domain.sinistro.query.service;

import com.seguradora.hibrida.domain.sinistro.query.dto.DashboardView;
import com.seguradora.hibrida.domain.sinistro.query.dto.SinistroDetailView;
import com.seguradora.hibrida.domain.sinistro.query.dto.SinistroFilter;
import com.seguradora.hibrida.domain.sinistro.query.dto.SinistroListView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SinistroQueryService - Testes da Interface")
class SinistroQueryServiceTest {

    @Nested
    @DisplayName("Validação da Interface")
    class ValidacaoInterface {

        @Test
        @DisplayName("Interface deve existir e ser pública")
        void interfaceDeveExistirESerPublica() {
            assertThat(SinistroQueryService.class).isInterface();
            assertThat(SinistroQueryService.class).isPublic();
        }

        @Test
        @DisplayName("Interface deve declarar 8 métodos")
        void interfaceDeveTer8Metodos() {
            Method[] methods = SinistroQueryService.class.getDeclaredMethods();
            assertThat(methods).hasSize(8);
        }

        @Test
        @DisplayName("Todos os métodos devem ser públicos")
        void todosOsMetodosSaoPublicos() {
            for (Method method : SinistroQueryService.class.getDeclaredMethods()) {
                assertThat(Modifier.isPublic(method.getModifiers()))
                        .as("Método %s deve ser público", method.getName())
                        .isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Assinaturas dos Métodos")
    class AssinaturasMetodos {

        @Test
        @DisplayName("buscarPorId deve aceitar UUID e retornar Optional<SinistroDetailView>")
        void buscarPorIdSignature() throws NoSuchMethodException {
            Method method = SinistroQueryService.class.getDeclaredMethod("buscarPorId", UUID.class);

            assertThat(method.getReturnType()).isEqualTo(Optional.class);
            assertThat(method.getParameterTypes()[0]).isEqualTo(UUID.class);
        }

        @Test
        @DisplayName("buscarPorProtocolo deve aceitar String e retornar Optional<SinistroDetailView>")
        void buscarPorProtocoloSignature() throws NoSuchMethodException {
            Method method = SinistroQueryService.class.getDeclaredMethod("buscarPorProtocolo", String.class);

            assertThat(method.getReturnType()).isEqualTo(Optional.class);
            assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
        }

        @Test
        @DisplayName("listar deve aceitar SinistroFilter e Pageable e retornar Page")
        void listarSignature() throws NoSuchMethodException {
            Method method = SinistroQueryService.class.getDeclaredMethod("listar", SinistroFilter.class, Pageable.class);

            assertThat(method.getReturnType()).isEqualTo(Page.class);
            assertThat(method.getParameterTypes()).containsExactly(SinistroFilter.class, Pageable.class);
        }

        @Test
        @DisplayName("buscarPorCpfSegurado deve aceitar String e Pageable e retornar Page")
        void buscarPorCpfSeguradoSignature() throws NoSuchMethodException {
            Method method = SinistroQueryService.class.getDeclaredMethod("buscarPorCpfSegurado", String.class, Pageable.class);

            assertThat(method.getReturnType()).isEqualTo(Page.class);
        }

        @Test
        @DisplayName("buscarPorPlaca deve aceitar String e Pageable e retornar Page")
        void buscarPorPlacaSignature() throws NoSuchMethodException {
            Method method = SinistroQueryService.class.getDeclaredMethod("buscarPorPlaca", String.class, Pageable.class);

            assertThat(method.getReturnType()).isEqualTo(Page.class);
        }

        @Test
        @DisplayName("buscarPorTexto deve aceitar String e Pageable e retornar Page")
        void buscarPorTextoSignature() throws NoSuchMethodException {
            Method method = SinistroQueryService.class.getDeclaredMethod("buscarPorTexto", String.class, Pageable.class);

            assertThat(method.getReturnType()).isEqualTo(Page.class);
        }

        @Test
        @DisplayName("buscarPorTag deve aceitar String e Pageable e retornar Page")
        void buscarPorTagSignature() throws NoSuchMethodException {
            Method method = SinistroQueryService.class.getDeclaredMethod("buscarPorTag", String.class, Pageable.class);

            assertThat(method.getReturnType()).isEqualTo(Page.class);
        }

        @Test
        @DisplayName("obterDashboard deve não ter parâmetros e retornar DashboardView")
        void obterDashboardSignature() throws NoSuchMethodException {
            Method method = SinistroQueryService.class.getDeclaredMethod("obterDashboard");

            assertThat(method.getReturnType()).isEqualTo(DashboardView.class);
            assertThat(method.getParameterCount()).isZero();
        }
    }

    @Nested
    @DisplayName("Implementabilidade")
    class Implementabilidade {

        @Test
        @DisplayName("Interface pode ser implementada anonimamente")
        void interfacePodeSerImplementada() {
            SinistroQueryService impl = new SinistroQueryService() {
                @Override public Optional<SinistroDetailView> buscarPorId(UUID id) { return Optional.empty(); }
                @Override public Optional<SinistroDetailView> buscarPorProtocolo(String protocolo) { return Optional.empty(); }
                @Override public Page<SinistroListView> listar(SinistroFilter filter, Pageable pageable) { return Page.empty(); }
                @Override public Page<SinistroListView> buscarPorCpfSegurado(String cpf, Pageable pageable) { return Page.empty(); }
                @Override public Page<SinistroListView> buscarPorPlaca(String placa, Pageable pageable) { return Page.empty(); }
                @Override public Page<SinistroListView> buscarPorTexto(String termo, Pageable pageable) { return Page.empty(); }
                @Override public Page<SinistroListView> buscarPorTag(String tag, Pageable pageable) { return Page.empty(); }
                @Override public DashboardView obterDashboard() { return null; }
            };

            assertThat(impl).isNotNull();
            assertThat(impl).isInstanceOf(SinistroQueryService.class);
        }
    }
}
