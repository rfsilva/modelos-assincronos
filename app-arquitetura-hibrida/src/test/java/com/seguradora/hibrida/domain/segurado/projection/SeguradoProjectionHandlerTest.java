package com.seguradora.hibrida.domain.segurado.projection;

import com.seguradora.hibrida.domain.segurado.event.*;
import com.seguradora.hibrida.domain.segurado.model.Endereco;
import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoQueryModel;
import com.seguradora.hibrida.domain.segurado.query.repository.SeguradoQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link SeguradoProjectionHandler}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SeguradoProjectionHandler - Testes Unitários")
class SeguradoProjectionHandlerTest {

    @Mock
    private SeguradoQueryRepository queryRepository;

    @InjectMocks
    private SeguradoProjectionHandler handler;

    @Test
    @DisplayName("Deve projetar evento de criação de segurado")
    void shouldProjectSeguradoCriadoEvent() {
        // Given
        Endereco endereco = new Endereco("Rua A", "100", null, "Bairro", "Cidade", "SP", "12345678");
        SeguradoCriadoEvent event = new SeguradoCriadoEvent(
                "SEG-001", "12345678909", "João Silva", "joao@example.com",
                "11987654321", LocalDate.of(1990, 1, 1), endereco
        );

        when(queryRepository.findById("SEG-001")).thenReturn(Optional.empty());
        when(queryRepository.save(any(SeguradoQueryModel.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        handler.on(event);

        // Then
        verify(queryRepository).findById("SEG-001");
        verify(queryRepository).save(any(SeguradoQueryModel.class));
    }

    @Test
    @DisplayName("Deve ignorar evento de criação já processado")
    void shouldIgnoreAlreadyProcessedCriadoEvent() {
        // Given
        SeguradoQueryModel existing = SeguradoQueryModel.builder()
                .id("SEG-001")
                .version(1L)
                .build();

        Endereco endereco = new Endereco("Rua A", "100", null, "Bairro", "Cidade", "SP", "12345678");
        SeguradoCriadoEvent event = new SeguradoCriadoEvent(
                "SEG-001", "12345678909", "João Silva", "joao@example.com",
                "11987654321", LocalDate.of(1990, 1, 1), endereco
        );
        event.setVersion(1L);

        when(queryRepository.findById("SEG-001")).thenReturn(Optional.of(existing));

        // When
        handler.on(event);

        // Then
        verify(queryRepository).findById("SEG-001");
        verify(queryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve projetar evento de atualização de segurado")
    void shouldProjectSeguradoAtualizadoEvent() {
        // Given
        SeguradoQueryModel existing = SeguradoQueryModel.builder()
                .id("SEG-001")
                .version(1L)
                .build();

        Endereco endereco = new Endereco("Rua A", "100", null, "Bairro", "Cidade", "SP", "12345678");
        SeguradoAtualizadoEvent event = new SeguradoAtualizadoEvent(
                "SEG-001", "João Silva Updated", "joao@example.com",
                "11987654321", LocalDate.of(1990, 1, 1), endereco
        );
        event.setVersion(2L);
        event.setTimestamp(Instant.now());

        when(queryRepository.findById("SEG-001")).thenReturn(Optional.of(existing));
        when(queryRepository.save(any(SeguradoQueryModel.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        handler.onSeguradoAtualizado(event);

        // Then
        verify(queryRepository).findById("SEG-001");
        verify(queryRepository).save(any(SeguradoQueryModel.class));
    }

    @Test
    @DisplayName("Deve ignorar evento de atualização fora de ordem")
    void shouldIgnoreOutOfOrderAtualizadoEvent() {
        // Given
        SeguradoQueryModel existing = SeguradoQueryModel.builder()
                .id("SEG-001")
                .version(5L)
                .build();

        Endereco endereco = new Endereco("Rua A", "100", null, "Bairro", "Cidade", "SP", "12345678");
        SeguradoAtualizadoEvent event = new SeguradoAtualizadoEvent(
                "SEG-001", "João Silva", "joao@example.com",
                "11987654321", LocalDate.of(1990, 1, 1), endereco
        );
        event.setVersion(3L); // Versão menor que a atual

        when(queryRepository.findById("SEG-001")).thenReturn(Optional.of(existing));

        // When
        handler.onSeguradoAtualizado(event);

        // Then
        verify(queryRepository).findById("SEG-001");
        verify(queryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve projetar evento de atualização de endereço")
    void shouldProjectEnderecoAtualizadoEvent() {
        // Given
        SeguradoQueryModel existing = SeguradoQueryModel.builder()
                .id("SEG-001")
                .version(1L)
                .build();

        Endereco enderecoAntigo = new Endereco("Rua A", "100", null, "Bairro", "Cidade", "SP", "12345678");
        Endereco enderecoNovo = new Endereco("Rua B", "200", null, "Bairro Novo", "Cidade Nova", "RJ", "87654321");

        EnderecoAtualizadoEvent event = new EnderecoAtualizadoEvent("SEG-001", enderecoAntigo, enderecoNovo);
        event.setVersion(2L);
        event.setTimestamp(Instant.now());

        when(queryRepository.findById("SEG-001")).thenReturn(Optional.of(existing));
        when(queryRepository.save(any(SeguradoQueryModel.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        handler.onEnderecoAtualizado(event);

        // Then
        verify(queryRepository).findById("SEG-001");
        verify(queryRepository).save(any(SeguradoQueryModel.class));
    }

    @Test
    @DisplayName("Deve projetar evento de desativação")
    void shouldProjectSeguradoDesativadoEvent() {
        // Given
        SeguradoQueryModel existing = SeguradoQueryModel.builder()
                .id("SEG-001")
                .version(1L)
                .status(StatusSegurado.ATIVO)
                .build();

        SeguradoDesativadoEvent event = new SeguradoDesativadoEvent("SEG-001", "Solicitação do cliente");
        event.setVersion(2L);
        event.setTimestamp(Instant.now());

        when(queryRepository.findById("SEG-001")).thenReturn(Optional.of(existing));
        when(queryRepository.save(any(SeguradoQueryModel.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        handler.onSeguradoDesativado(event);

        // Then
        verify(queryRepository).findById("SEG-001");
        verify(queryRepository).save(any(SeguradoQueryModel.class));
    }

    @Test
    @DisplayName("Deve projetar evento de reativação")
    void shouldProjectSeguradoReativadoEvent() {
        // Given
        SeguradoQueryModel existing = SeguradoQueryModel.builder()
                .id("SEG-001")
                .version(1L)
                .status(StatusSegurado.INATIVO)
                .build();

        SeguradoReativadoEvent event = new SeguradoReativadoEvent("SEG-001", "Regularização");
        event.setVersion(2L);
        event.setTimestamp(Instant.now());

        when(queryRepository.findById("SEG-001")).thenReturn(Optional.of(existing));
        when(queryRepository.save(any(SeguradoQueryModel.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        handler.onSeguradoReativado(event);

        // Then
        verify(queryRepository).findById("SEG-001");
        verify(queryRepository).save(any(SeguradoQueryModel.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando segurado não encontrado na atualização")
    void shouldThrowExceptionWhenSeguradoNotFoundOnUpdate() {
        // Given
        Endereco endereco = new Endereco("Rua A", "100", null, "Bairro", "Cidade", "SP", "12345678");
        SeguradoAtualizadoEvent event = new SeguradoAtualizadoEvent(
                "SEG-999", "João Silva", "joao@example.com",
                "11987654321", LocalDate.of(1990, 1, 1), endereco
        );

        when(queryRepository.findById("SEG-999")).thenReturn(Optional.empty());

        // When
        try {
            handler.onSeguradoAtualizado(event);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            // Then - A exceção é envolvida com mensagem de erro da projeção
            assertThat(e.getMessage()).contains("SeguradoAtualizadoEvent");
        }

        verify(queryRepository).findById("SEG-999");
        verify(queryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve tratar erro na projeção de criação")
    void shouldHandleErrorOnCriacaoProjection() {
        // Given
        Endereco endereco = new Endereco("Rua A", "100", null, "Bairro", "Cidade", "SP", "12345678");
        SeguradoCriadoEvent event = new SeguradoCriadoEvent(
                "SEG-001", "12345678909", "João Silva", "joao@example.com",
                "11987654321", LocalDate.of(1990, 1, 1), endereco
        );

        when(queryRepository.findById("SEG-001")).thenReturn(Optional.empty());
        when(queryRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        // When / Then
        assertThatThrownBy(() -> handler.on(event))
                .isInstanceOf(RuntimeException.class);

        verify(queryRepository).findById("SEG-001");
        verify(queryRepository).save(any());
    }
}
