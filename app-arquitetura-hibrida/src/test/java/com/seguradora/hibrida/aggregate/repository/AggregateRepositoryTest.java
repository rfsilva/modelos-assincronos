package com.seguradora.hibrida.aggregate.repository;

import com.seguradora.hibrida.aggregate.AggregateRoot;
import com.seguradora.hibrida.aggregate.example.ExampleAggregate;
import com.seguradora.hibrida.aggregate.exception.AggregateNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para a interface {@link AggregateRepository}.
 *
 * <p>Valida o comportamento do método default {@code getById} usando um stub.
 */
@DisplayName("AggregateRepository Tests")
class AggregateRepositoryTest {

    private AggregateRepository<ExampleAggregate> repository;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        repository = mock(AggregateRepository.class, CALLS_REAL_METHODS);
    }

    // =========================================================================
    // Método default getById
    // =========================================================================

    @Nested
    @DisplayName("getById (método default)")
    class GetById {

        @Test
        @DisplayName("Deve retornar aggregate quando encontrado via findById")
        void shouldReturnAggregateWhenFoundViaFindById() {
            // Given
            String id = UUID.randomUUID().toString();
            ExampleAggregate expected = new ExampleAggregate(id);
            when(repository.findById(id)).thenReturn(Optional.of(expected));
            when(repository.getAggregateType()).thenReturn(ExampleAggregate.class);

            // When
            ExampleAggregate result = repository.getById(id);

            // Then
            assertThat(result).isSameAs(expected);
        }

        @Test
        @DisplayName("Deve lançar AggregateNotFoundException quando findById retorna empty")
        void shouldThrowAggregateNotFoundExceptionWhenFindByIdReturnsEmpty() {
            // Given
            String id = UUID.randomUUID().toString();
            when(repository.findById(id)).thenReturn(Optional.empty());
            when(repository.getAggregateType()).thenReturn(ExampleAggregate.class);

            // When / Then
            assertThatThrownBy(() -> repository.getById(id))
                    .isInstanceOf(AggregateNotFoundException.class);
        }

        @Test
        @DisplayName("Deve chamar findById com o ID correto")
        void shouldCallFindByIdWithCorrectId() {
            // Given
            String id = "agg-xyz";
            ExampleAggregate agg = new ExampleAggregate(id);
            when(repository.findById(id)).thenReturn(Optional.of(agg));
            when(repository.getAggregateType()).thenReturn(ExampleAggregate.class);

            // When
            repository.getById(id);

            // Then
            verify(repository).findById(id);
        }
    }

    // =========================================================================
    // Contrato da interface
    // =========================================================================

    @Nested
    @DisplayName("Contrato da interface")
    class ContratoInterface {

        @Test
        @DisplayName("Interface deve declarar método save")
        void shouldDeclareSaveMethod() throws NoSuchMethodException {
            assertThat(AggregateRepository.class.getMethod("save", AggregateRoot.class))
                    .isNotNull();
        }

        @Test
        @DisplayName("Interface deve declarar método findById")
        void shouldDeclareFindByIdMethod() throws NoSuchMethodException {
            assertThat(AggregateRepository.class.getMethod("findById", String.class))
                    .isNotNull();
        }

        @Test
        @DisplayName("Interface deve declarar método exists")
        void shouldDeclareExistsMethod() throws NoSuchMethodException {
            assertThat(AggregateRepository.class.getMethod("exists", String.class))
                    .isNotNull();
        }

        @Test
        @DisplayName("Interface deve declarar método delete")
        void shouldDeclareDeleteMethod() throws NoSuchMethodException {
            assertThat(AggregateRepository.class.getMethod("delete", String.class))
                    .isNotNull();
        }

        @Test
        @DisplayName("Interface deve declarar método getAggregateType")
        void shouldDeclareGetAggregateTypeMethod() throws NoSuchMethodException {
            assertThat(AggregateRepository.class.getMethod("getAggregateType"))
                    .isNotNull();
        }

        @Test
        @DisplayName("getById deve ser método default")
        void getByIdShouldBeDefaultMethod() throws NoSuchMethodException {
            assertThat(AggregateRepository.class.getMethod("getById", String.class).isDefault())
                    .isTrue();
        }
    }
}
