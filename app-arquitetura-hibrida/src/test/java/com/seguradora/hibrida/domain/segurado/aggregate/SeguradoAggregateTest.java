package com.seguradora.hibrida.domain.segurado.aggregate;

import com.seguradora.hibrida.aggregate.exception.BusinessRuleViolationException;
import com.seguradora.hibrida.domain.segurado.event.*;
import com.seguradora.hibrida.domain.segurado.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link SeguradoAggregate}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("SeguradoAggregate - Testes Unitários")
class SeguradoAggregateTest {

    // ==================== CRIAÇÃO ====================

    @Test
    @DisplayName("Deve criar novo segurado com dados válidos")
    void shouldCreateNewSeguradoWithValidData() {
        // Given
        String id = "SEG-001";
        String cpf = "12345678909";
        String nome = "João da Silva";
        String email = "joao@example.com";
        String telefone = "11987654321";
        LocalDate dataNascimento = LocalDate.now().minusYears(30);
        Endereco endereco = createEnderecoValido();

        // When
        SeguradoAggregate aggregate = new SeguradoAggregate(
            id, cpf, nome, email, telefone, dataNascimento, endereco
        );

        // Then
        assertThat(aggregate.getId()).isEqualTo(id);
        assertThat(aggregate.getCpf()).isEqualTo(cpf);
        assertThat(aggregate.getNome()).isEqualTo(nome);
        assertThat(aggregate.getEmail()).isEqualTo(email);
        assertThat(aggregate.isAtivo()).isTrue();
        assertThat(aggregate.getStatus()).isEqualTo(StatusSegurado.ATIVO);
        assertThat(aggregate.getUncommittedEvents()).hasSize(1);
        assertThat(aggregate.getUncommittedEvents().get(0)).isInstanceOf(SeguradoCriadoEvent.class);
    }

    @Test
    @DisplayName("Deve falhar ao criar segurado com CPF inválido")
    void shouldFailToCreateSeguradoWithInvalidCpf() {
        // Given
        String cpf = "12345";
        Endereco endereco = createEnderecoValido();

        // When/Then
        assertThatThrownBy(() -> new SeguradoAggregate(
            "SEG-001", cpf, "João da Silva", "joao@example.com",
            "11987654321", LocalDate.now().minusYears(30), endereco
        ))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("CPF inválido");
    }

    @Test
    @DisplayName("Deve falhar ao criar segurado com CPF de dígitos iguais")
    void shouldFailToCreateSeguradoWithSameDigitsCpf() {
        // Given
        String cpf = "11111111111";
        Endereco endereco = createEnderecoValido();

        // When/Then
        assertThatThrownBy(() -> new SeguradoAggregate(
            "SEG-001", cpf, "João da Silva", "joao@example.com",
            "11987654321", LocalDate.now().minusYears(30), endereco
        ))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("CPF");
    }

    @Test
    @DisplayName("Deve falhar ao criar segurado com nome muito curto")
    void shouldFailToCreateSeguradoWithShortName() {
        // Given
        String nome = "Jo";
        Endereco endereco = createEnderecoValido();

        // When/Then
        assertThatThrownBy(() -> new SeguradoAggregate(
            "SEG-001", "12345678909", nome, "joao@example.com",
            "11987654321", LocalDate.now().minusYears(30), endereco
        ))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("Nome inválido");
    }

    @Test
    @DisplayName("Deve falhar ao criar segurado com email inválido")
    void shouldFailToCreateSeguradoWithInvalidEmail() {
        // Given
        String email = "email-invalido";
        Endereco endereco = createEnderecoValido();

        // When/Then
        assertThatThrownBy(() -> new SeguradoAggregate(
            "SEG-001", "12345678909", "João da Silva", email,
            "11987654321", LocalDate.now().minusYears(30), endereco
        ))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("Email inválido");
    }

    @Test
    @DisplayName("Deve falhar ao criar segurado com telefone inválido")
    void shouldFailToCreateSeguradoWithInvalidTelefone() {
        // Given
        String telefone = "123";
        Endereco endereco = createEnderecoValido();

        // When/Then
        assertThatThrownBy(() -> new SeguradoAggregate(
            "SEG-001", "12345678909", "João da Silva", "joao@example.com",
            telefone, LocalDate.now().minusYears(30), endereco
        ))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("Telefone inválido");
    }

    @Test
    @DisplayName("Deve falhar ao criar segurado menor de 18 anos")
    void shouldFailToCreateSeguradoUnder18() {
        // Given
        LocalDate dataNascimento = LocalDate.now().minusYears(17);
        Endereco endereco = createEnderecoValido();

        // When/Then
        assertThatThrownBy(() -> new SeguradoAggregate(
            "SEG-001", "12345678909", "João da Silva", "joao@example.com",
            "11987654321", dataNascimento, endereco
        ))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("maior de idade");
    }

    @Test
    @DisplayName("Deve falhar ao criar segurado com data de nascimento futura")
    void shouldFailToCreateSeguradoWithFutureBirthDate() {
        // Given
        LocalDate dataNascimento = LocalDate.now().plusDays(1);
        Endereco endereco = createEnderecoValido();

        // When/Then
        assertThatThrownBy(() -> new SeguradoAggregate(
            "SEG-001", "12345678909", "João da Silva", "joao@example.com",
            "11987654321", dataNascimento, endereco
        ))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("Data de nascimento inválida");
    }

    @Test
    @DisplayName("Deve falhar ao criar segurado sem endereço")
    void shouldFailToCreateSeguradoWithoutEndereco() {
        // When/Then
        assertThatThrownBy(() -> new SeguradoAggregate(
            "SEG-001", "12345678909", "João da Silva", "joao@example.com",
            "11987654321", LocalDate.now().minusYears(30), null
        ))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("Endereço");
    }

    @Test
    @DisplayName("Deve falhar ao criar segurado com CEP inválido")
    void shouldFailToCreateSeguradoWithInvalidCep() {
        // Given
        Endereco endereco = new Endereco(
            "Rua Teste", "100", null, "Centro", "São Paulo", "SP", "123"
        );

        // When/Then
        assertThatThrownBy(() -> new SeguradoAggregate(
            "SEG-001", "12345678909", "João da Silva", "joao@example.com",
            "11987654321", LocalDate.now().minusYears(30), endereco
        ))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("CEP");
    }

    // ==================== ATUALIZAÇÃO ====================

    @Test
    @DisplayName("Deve atualizar dados do segurado ativo")
    void shouldUpdateActiveSeguradoData() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();
        aggregate.markEventsAsCommitted();

        String novoNome = "João Santos Silva";
        String novoEmail = "joao.santos@example.com";
        String novoTelefone = "11998877665";
        LocalDate novaDataNascimento = LocalDate.now().minusYears(35);
        Endereco novoEndereco = createEnderecoValido();

        // When
        aggregate.atualizarDados(novoNome, novoEmail, novoTelefone, novaDataNascimento, novoEndereco);

        // Then
        assertThat(aggregate.getNome()).isEqualTo(novoNome);
        assertThat(aggregate.getEmail()).isEqualTo(novoEmail);
        assertThat(aggregate.getUncommittedEvents()).hasSize(1);
        assertThat(aggregate.getUncommittedEvents().get(0)).isInstanceOf(SeguradoAtualizadoEvent.class);
    }

    @Test
    @DisplayName("Deve falhar ao atualizar segurado inativo")
    void shouldFailToUpdateInactiveSegurado() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();
        aggregate.desativar("Teste");
        aggregate.markEventsAsCommitted();

        // When/Then
        assertThatThrownBy(() -> aggregate.atualizarDados(
            "Novo Nome", "novo@example.com", "11987654321",
            LocalDate.now().minusYears(30), createEnderecoValido()
        ))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("inativo");
    }

    @Test
    @DisplayName("Deve atualizar apenas endereço do segurado")
    void shouldUpdateOnlyEndereco() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();
        aggregate.markEventsAsCommitted();

        Endereco novoEndereco = new Endereco(
            "Avenida Brasil", "500", "Apto 202", "Jardins", "São Paulo", "SP", "01310100"
        );

        // When
        aggregate.atualizarEndereco(novoEndereco);

        // Then
        assertThat(aggregate.getUncommittedEvents()).hasSize(1);
        assertThat(aggregate.getUncommittedEvents().get(0)).isInstanceOf(EnderecoAtualizadoEvent.class);
    }

    // ==================== CONTATOS ====================

    @Test
    @DisplayName("Deve adicionar contato ao segurado")
    void shouldAddContatoToSegurado() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();
        aggregate.markEventsAsCommitted();

        // When
        aggregate.adicionarContato(TipoContato.CELULAR, "11999887766", true);

        // Then
        assertThat(aggregate.getContatos()).hasSize(1);
        assertThat(aggregate.getUncommittedEvents()).hasSize(1);
        assertThat(aggregate.getUncommittedEvents().get(0)).isInstanceOf(ContatoAdicionadoEvent.class);
    }

    @Test
    @DisplayName("Deve falhar ao adicionar contato duplicado")
    void shouldFailToAddDuplicateContato() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();
        aggregate.adicionarContato(TipoContato.EMAIL, "joao@example.com", true);
        aggregate.markEventsAsCommitted();

        // When/Then
        assertThatThrownBy(() -> aggregate.adicionarContato(TipoContato.EMAIL, "joao@example.com", false))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("já existe");
    }

    @Test
    @DisplayName("Deve falhar ao adicionar contato a segurado inativo")
    void shouldFailToAddContatoToInactiveSegurado() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();
        aggregate.desativar("Teste");
        aggregate.markEventsAsCommitted();

        // When/Then
        assertThatThrownBy(() -> aggregate.adicionarContato(TipoContato.CELULAR, "11999887766", true))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("inativo");
    }

    @Test
    @DisplayName("Deve remover contato do segurado")
    void shouldRemoveContatoFromSegurado() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();
        aggregate.adicionarContato(TipoContato.CELULAR, "11999887766", true);
        aggregate.markEventsAsCommitted();

        // When
        aggregate.removerContato(TipoContato.CELULAR, "11999887766");

        // Then
        assertThat(aggregate.getContatos()).isEmpty();
        assertThat(aggregate.getUncommittedEvents()).hasSize(1);
        assertThat(aggregate.getUncommittedEvents().get(0)).isInstanceOf(ContatoRemovidoEvent.class);
    }

    @Test
    @DisplayName("Deve falhar ao remover contato inexistente")
    void shouldFailToRemoveNonExistentContato() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();
        aggregate.markEventsAsCommitted();

        // When/Then
        assertThatThrownBy(() -> aggregate.removerContato(TipoContato.CELULAR, "11999887766"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("não encontrado");
    }

    // ==================== DESATIVAÇÃO/REATIVAÇÃO ====================

    @Test
    @DisplayName("Deve desativar segurado ativo")
    void shouldDeactivateActiveSegurado() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();
        aggregate.markEventsAsCommitted();
        String motivo = "Cancelamento solicitado pelo cliente";

        // When
        aggregate.desativar(motivo);

        // Then
        assertThat(aggregate.isAtivo()).isFalse();
        assertThat(aggregate.getStatus()).isEqualTo(StatusSegurado.INATIVO);
        assertThat(aggregate.getUncommittedEvents()).hasSize(1);
        assertThat(aggregate.getUncommittedEvents().get(0)).isInstanceOf(SeguradoDesativadoEvent.class);
    }

    @Test
    @DisplayName("Deve falhar ao desativar segurado já inativo")
    void shouldFailToDeactivateAlreadyInactiveSegurado() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();
        aggregate.desativar("Primeira desativação");
        aggregate.markEventsAsCommitted();

        // When/Then
        assertThatThrownBy(() -> aggregate.desativar("Segunda desativação"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("já está inativo");
    }

    @Test
    @DisplayName("Deve falhar ao desativar sem motivo")
    void shouldFailToDeactivateWithoutMotivo() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();

        // When/Then
        assertThatThrownBy(() -> aggregate.desativar(null))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Motivo");

        assertThatThrownBy(() -> aggregate.desativar(""))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Motivo");
    }

    @Test
    @DisplayName("Deve reativar segurado inativo")
    void shouldReactivateInactiveSegurado() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();
        aggregate.desativar("Cancelamento temporário");
        aggregate.markEventsAsCommitted();
        String motivo = "Cliente solicitou reativação";

        // When
        aggregate.reativar(motivo);

        // Then
        assertThat(aggregate.isAtivo()).isTrue();
        assertThat(aggregate.getStatus()).isEqualTo(StatusSegurado.ATIVO);
        assertThat(aggregate.getUncommittedEvents()).hasSize(1);
        assertThat(aggregate.getUncommittedEvents().get(0)).isInstanceOf(SeguradoReativadoEvent.class);
    }

    @Test
    @DisplayName("Deve falhar ao reativar segurado já ativo")
    void shouldFailToReactivateAlreadyActiveSegurado() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();
        aggregate.markEventsAsCommitted();

        // When/Then
        assertThatThrownBy(() -> aggregate.reativar("Reativação"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("já está ativo");
    }

    @Test
    @DisplayName("Deve falhar ao reativar sem motivo")
    void shouldFailToReactivateWithoutMotivo() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();
        aggregate.desativar("Teste");

        // When/Then
        assertThatThrownBy(() -> aggregate.reativar(null))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Motivo");
    }

    // ==================== SNAPSHOT ====================

    @Test
    @DisplayName("Deve criar snapshot do aggregate")
    void shouldCreateSnapshot() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();

        // When
        Object snapshot = aggregate.createSnapshot();

        // Then
        assertThat(snapshot).isNotNull();
        assertThat(snapshot).isInstanceOf(Segurado.class);

        Segurado segurado = (Segurado) snapshot;
        assertThat(segurado.getCpf()).isEqualTo(aggregate.getCpf());
        assertThat(segurado.getNome()).isEqualTo(aggregate.getNome());
    }

    @Test
    @DisplayName("Deve restaurar aggregate a partir de snapshot")
    void shouldRestoreFromSnapshot() {
        // Given
        SeguradoAggregate originalAggregate = createSeguradoValido();
        Object snapshot = originalAggregate.createSnapshot();

        SeguradoAggregate restoredAggregate = new SeguradoAggregate();

        // When
        restoredAggregate.restoreFromSnapshot(snapshot);

        // Then
        assertThat(restoredAggregate.getCpf()).isEqualTo(originalAggregate.getCpf());
        assertThat(restoredAggregate.getNome()).isEqualTo(originalAggregate.getNome());
        assertThat(restoredAggregate.getEmail()).isEqualTo(originalAggregate.getEmail());
    }

    @Test
    @DisplayName("Deve limpar estado do aggregate")
    void shouldClearState() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();
        aggregate.adicionarContato(TipoContato.CELULAR, "11999887766", true);

        // When
        aggregate.clearState();

        // Then
        assertThat(aggregate.getSegurado()).isNull();
        assertThat(aggregate.getContatos()).isEmpty();
    }

    // ==================== EVENT SOURCING HANDLERS ====================

    @Test
    @DisplayName("Deve aplicar SeguradoCriadoEvent corretamente")
    void shouldApplySeguradoCriadoEvent() {
        // Given
        SeguradoAggregate aggregate = new SeguradoAggregate();
        SeguradoCriadoEvent event = new SeguradoCriadoEvent(
            "SEG-001", "12345678909", "João da Silva", "joao@example.com",
            "11987654321", LocalDate.now().minusYears(30), createEnderecoValido()
        );

        // When
        aggregate.on(event);

        // Then
        assertThat(aggregate.getSegurado()).isNotNull();
        assertThat(aggregate.getCpf()).isEqualTo("12345678909");
        assertThat(aggregate.getNome()).isEqualTo("João da Silva");
        assertThat(aggregate.isAtivo()).isTrue();
    }

    @Test
    @DisplayName("Deve aplicar SeguradoAtualizadoEvent corretamente")
    void shouldApplySeguradoAtualizadoEvent() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();
        String novoNome = "João Santos";
        SeguradoAtualizadoEvent event = new SeguradoAtualizadoEvent(
            "SEG-001", novoNome, "novo@example.com", "11999888777",
            LocalDate.now().minusYears(35), createEnderecoValido()
        );

        // When
        aggregate.on(event);

        // Then
        assertThat(aggregate.getNome()).isEqualTo(novoNome);
        assertThat(aggregate.getEmail()).isEqualTo("novo@example.com");
    }

    @Test
    @DisplayName("Deve aplicar SeguradoDesativadoEvent corretamente")
    void shouldApplySeguradoDesativadoEvent() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();
        SeguradoDesativadoEvent event = new SeguradoDesativadoEvent("SEG-001", "Motivo teste");

        // When
        aggregate.on(event);

        // Then
        assertThat(aggregate.isAtivo()).isFalse();
        assertThat(aggregate.getStatus()).isEqualTo(StatusSegurado.INATIVO);
    }

    @Test
    @DisplayName("Deve aplicar SeguradoReativadoEvent corretamente")
    void shouldApplySeguradoReativadoEvent() {
        // Given
        SeguradoAggregate aggregate = createSeguradoValido();
        aggregate.desativar("Teste");
        SeguradoReativadoEvent event = new SeguradoReativadoEvent("SEG-001", "Reativação");

        // When
        aggregate.on(event);

        // Then
        assertThat(aggregate.isAtivo()).isTrue();
        assertThat(aggregate.getStatus()).isEqualTo(StatusSegurado.ATIVO);
    }

    // ==================== HELPERS ====================

    private SeguradoAggregate createSeguradoValido() {
        return new SeguradoAggregate(
            "SEG-001",
            "12345678909",
            "João da Silva",
            "joao@example.com",
            "11987654321",
            LocalDate.now().minusYears(30),
            createEnderecoValido()
        );
    }

    private Endereco createEnderecoValido() {
        return new Endereco(
            "Rua Teste",
            "100",
            "Apto 101",
            "Centro",
            "São Paulo",
            "SP",
            "01310100"
        );
    }
}
