package com.seguradora.hibrida.domain.veiculo.controller;

import com.seguradora.hibrida.command.CommandBus;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.veiculo.command.*;
import com.seguradora.hibrida.domain.veiculo.controller.dto.*;
import com.seguradora.hibrida.domain.veiculo.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link VeiculoCommandController}.
 *
 * <p>Testa todos os endpoints REST de comandos de veículos,
 * incluindo cenários de sucesso e falha.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VeiculoCommandController - Testes Unitários")
class VeiculoCommandControllerTest {

    @Mock
    private CommandBus commandBus;

    @InjectMocks
    private VeiculoCommandController controller;

    // === TESTES DE CRIAÇÃO DE VEÍCULO ===

    @Nested
    @DisplayName("Criar Veículo - POST /")
    class CriarVeiculoTests {

        @Test
        @DisplayName("Deve criar veículo com sucesso")
        void shouldCreateVeiculoSuccessfully() {
            // Given
            CriarVeiculoRequestDTO request = criarRequestValido();

            String veiculoId = "VEI-12345";
            CommandResult commandResult = CommandResult.builder()
                .success(true)
                .data(veiculoId)
                .executedAt(Instant.now())
                .metadata(Map.of("action", "created"))
                .build();

            when(commandBus.send(any(CriarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.criarVeiculo(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().aggregateId()).isEqualTo(veiculoId);
            assertThat(response.getBody().message()).contains("sucesso");
            assertThat(response.getBody().timestamp()).isNotNull();

            verify(commandBus).send(any(CriarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve retornar erro quando placa já existe")
        void shouldReturnErrorWhenPlacaAlreadyExists() {
            // Given
            CriarVeiculoRequestDTO request = criarRequestValido();

            CommandResult commandResult = CommandResult.builder()
                .success(false)
                .errorMessage("Placa ABC1234 já está cadastrada")
                .executedAt(Instant.now())
                .build();

            when(commandBus.send(any(CriarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.criarVeiculo(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().aggregateId()).isNull();
            assertThat(response.getBody().message()).contains("Falha ao criar veículo");
            assertThat(response.getBody().message()).contains("Placa ABC1234 já está cadastrada");

            verify(commandBus).send(any(CriarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve retornar erro quando RENAVAM já existe")
        void shouldReturnErrorWhenRenavamAlreadyExists() {
            // Given
            CriarVeiculoRequestDTO request = criarRequestValido();

            CommandResult commandResult = CommandResult.builder()
                .success(false)
                .errorMessage("RENAVAM 12345678901 já está cadastrado")
                .executedAt(Instant.now())
                .build();

            when(commandBus.send(any(CriarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.criarVeiculo(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).contains("RENAVAM 12345678901 já está cadastrado");

            verify(commandBus).send(any(CriarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve retornar erro quando chassi já existe")
        void shouldReturnErrorWhenChassiAlreadyExists() {
            // Given
            CriarVeiculoRequestDTO request = criarRequestValido();

            CommandResult commandResult = CommandResult.builder()
                .success(false)
                .errorMessage("Chassi 1HGBH41JXMN109186 já está cadastrado")
                .executedAt(Instant.now())
                .build();

            when(commandBus.send(any(CriarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.criarVeiculo(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).contains("Chassi 1HGBH41JXMN109186 já está cadastrado");

            verify(commandBus).send(any(CriarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve retornar erro interno quando ocorre exceção")
        void shouldReturnInternalErrorWhenExceptionOccurs() {
            // Given
            CriarVeiculoRequestDTO request = criarRequestValido();

            when(commandBus.send(any(CriarVeiculoCommand.class)))
                .thenThrow(new RuntimeException("Erro inesperado no comando"));

            // When
            ResponseEntity<CommandResponseDTO> response = controller.criarVeiculo(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().aggregateId()).isNull();
            assertThat(response.getBody().message()).contains("Erro interno ao criar veículo");
            assertThat(response.getBody().message()).contains("Erro inesperado no comando");
            assertThat(response.getBody().details()).containsKey("exception");
            assertThat(response.getBody().details().get("exception")).isEqualTo("RuntimeException");

            verify(commandBus).send(any(CriarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve incluir metadados na resposta de sucesso")
        void shouldIncludeMetadataInSuccessResponse() {
            // Given
            CriarVeiculoRequestDTO request = criarRequestValido();

            Map<String, Object> metadata = Map.of(
                "action", "created",
                "source", "api",
                "version", "1.0"
            );

            CommandResult commandResult = CommandResult.builder()
                .success(true)
                .data("VEI-12345")
                .executedAt(Instant.now())
                .metadata(metadata)
                .build();

            when(commandBus.send(any(CriarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.criarVeiculo(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().details()).isEqualTo(metadata);

            verify(commandBus).send(any(CriarVeiculoCommand.class));
        }

        private CriarVeiculoRequestDTO criarRequestValido() {
            Especificacao especificacao = Especificacao.of(
                "Branco",
                TipoCombustivel.FLEX,
                CategoriaVeiculo.PASSEIO,
                1600
            );

            Proprietario proprietario = Proprietario.of(
                "11144477735",
                "João da Silva",
                TipoPessoa.FISICA
            );

            return new CriarVeiculoRequestDTO(
                "ABC1234",
                "12345678901",
                "1HGBH41JXMN109186",
                "Honda",
                "Civic",
                2020,
                2021,
                especificacao,
                proprietario,
                "operador123"
            );
        }
    }

    // === TESTES DE ATUALIZAÇÃO DE VEÍCULO ===

    @Nested
    @DisplayName("Atualizar Veículo - PUT /{id}")
    class AtualizarVeiculoTests {

        @Test
        @DisplayName("Deve atualizar veículo com sucesso")
        void shouldUpdateVeiculoSuccessfully() {
            // Given
            String veiculoId = "VEI-12345";
            AtualizarVeiculoRequestDTO request = criarRequestAtualizacao();

            CommandResult commandResult = CommandResult.builder()
                .success(true)
                .data(veiculoId)
                .executedAt(Instant.now())
                .metadata(Map.of("action", "updated"))
                .build();

            when(commandBus.send(any(AtualizarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.atualizarVeiculo(veiculoId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().aggregateId()).isEqualTo(veiculoId);
            assertThat(response.getBody().message()).contains("sucesso");

            verify(commandBus).send(any(AtualizarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve retornar erro quando veículo não existe")
        void shouldReturnErrorWhenVeiculoNotFound() {
            // Given
            String veiculoId = "VEI-99999";
            AtualizarVeiculoRequestDTO request = criarRequestAtualizacao();

            CommandResult commandResult = CommandResult.builder()
                .success(false)
                .errorMessage("Veículo VEI-99999 não encontrado")
                .executedAt(Instant.now())
                .build();

            when(commandBus.send(any(AtualizarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.atualizarVeiculo(veiculoId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).contains("Falha ao atualizar veículo");
            assertThat(response.getBody().message()).contains("não encontrado");

            verify(commandBus).send(any(AtualizarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve retornar erro quando há conflito de versão")
        void shouldReturnErrorWhenVersionConflictOccurs() {
            // Given
            String veiculoId = "VEI-12345";
            AtualizarVeiculoRequestDTO request = criarRequestAtualizacao();

            CommandResult commandResult = CommandResult.builder()
                .success(false)
                .errorMessage("Conflito de versão: esperado 3, atual 5")
                .executedAt(Instant.now())
                .build();

            when(commandBus.send(any(AtualizarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.atualizarVeiculo(veiculoId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).contains("Conflito de versão");

            verify(commandBus).send(any(AtualizarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve retornar erro interno quando ocorre exceção")
        void shouldReturnInternalErrorWhenExceptionOccurs() {
            // Given
            String veiculoId = "VEI-12345";
            AtualizarVeiculoRequestDTO request = criarRequestAtualizacao();

            when(commandBus.send(any(AtualizarVeiculoCommand.class)))
                .thenThrow(new RuntimeException("Erro de processamento"));

            // When
            ResponseEntity<CommandResponseDTO> response = controller.atualizarVeiculo(veiculoId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).contains("Erro interno ao atualizar veículo");
            assertThat(response.getBody().details()).containsKey("exception");

            verify(commandBus).send(any(AtualizarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve atualizar com controle de versão otimista")
        void shouldUpdateWithOptimisticLocking() {
            // Given
            String veiculoId = "VEI-12345";
            AtualizarVeiculoRequestDTO request = new AtualizarVeiculoRequestDTO(
                Especificacao.of("Vermelho", TipoCombustivel.GASOLINA, CategoriaVeiculo.PASSEIO, 2000),
                "operador123",
                5L,
                "Mudança de cor"
            );

            CommandResult commandResult = CommandResult.builder()
                .success(true)
                .data(veiculoId)
                .executedAt(Instant.now())
                .build();

            when(commandBus.send(any(AtualizarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.atualizarVeiculo(veiculoId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            verify(commandBus).send(argThat(cmd -> {
                if (cmd instanceof AtualizarVeiculoCommand) {
                    AtualizarVeiculoCommand atualizar = (AtualizarVeiculoCommand) cmd;
                    return atualizar.getVersaoEsperada().equals(5L);
                }
                return false;
            }));
        }

        private AtualizarVeiculoRequestDTO criarRequestAtualizacao() {
            Especificacao novaEspecificacao = Especificacao.of(
                "Vermelho",
                TipoCombustivel.FLEX,
                CategoriaVeiculo.PASSEIO,
                1600
            );

            return new AtualizarVeiculoRequestDTO(
                novaEspecificacao,
                "operador123",
                3L,
                "Atualização de cor"
            );
        }
    }

    // === TESTES DE ASSOCIAÇÃO DE APÓLICE ===

    @Nested
    @DisplayName("Associar Apólice - POST /{id}/associar-apolice")
    class AssociarApoliceTests {

        @Test
        @DisplayName("Deve associar veículo à apólice com sucesso")
        void shouldAssociateVeiculoToApoliceSuccessfully() {
            // Given
            String veiculoId = "VEI-12345";
            String apoliceId = "POL-2024-001234";
            AssociarVeiculoRequestDTO request = new AssociarVeiculoRequestDTO(
                apoliceId,
                LocalDate.now(),
                "operador123"
            );

            CommandResult commandResult = CommandResult.builder()
                .success(true)
                .data(veiculoId)
                .executedAt(Instant.now())
                .metadata(Map.of("apoliceId", apoliceId))
                .build();

            when(commandBus.send(any(AssociarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.associarApolice(veiculoId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().aggregateId()).isEqualTo(veiculoId);
            assertThat(response.getBody().message()).contains("associado à apólice com sucesso");
            assertThat(response.getBody().details()).containsEntry("apoliceId", apoliceId);

            verify(commandBus).send(any(AssociarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve retornar erro quando veículo já está associado")
        void shouldReturnErrorWhenVeiculoAlreadyAssociated() {
            // Given
            String veiculoId = "VEI-12345";
            AssociarVeiculoRequestDTO request = new AssociarVeiculoRequestDTO(
                "POL-2024-001234",
                LocalDate.now(),
                "operador123"
            );

            CommandResult commandResult = CommandResult.builder()
                .success(false)
                .errorMessage("Veículo já está associado à apólice POL-2024-001234")
                .executedAt(Instant.now())
                .build();

            when(commandBus.send(any(AssociarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.associarApolice(veiculoId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).contains("Falha ao associar veículo");
            assertThat(response.getBody().message()).contains("já está associado");

            verify(commandBus).send(any(AssociarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve retornar erro quando veículo não existe")
        void shouldReturnErrorWhenVeiculoNotFound() {
            // Given
            String veiculoId = "VEI-99999";
            AssociarVeiculoRequestDTO request = new AssociarVeiculoRequestDTO(
                "POL-2024-001234",
                LocalDate.now(),
                "operador123"
            );

            CommandResult commandResult = CommandResult.builder()
                .success(false)
                .errorMessage("Veículo VEI-99999 não encontrado")
                .executedAt(Instant.now())
                .build();

            when(commandBus.send(any(AssociarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.associarApolice(veiculoId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).contains("não encontrado");

            verify(commandBus).send(any(AssociarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve retornar erro quando apólice não existe")
        void shouldReturnErrorWhenApoliceNotFound() {
            // Given
            String veiculoId = "VEI-12345";
            AssociarVeiculoRequestDTO request = new AssociarVeiculoRequestDTO(
                "POL-INVALIDA",
                LocalDate.now(),
                "operador123"
            );

            CommandResult commandResult = CommandResult.builder()
                .success(false)
                .errorMessage("Apólice POL-INVALIDA não encontrada")
                .executedAt(Instant.now())
                .build();

            when(commandBus.send(any(AssociarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.associarApolice(veiculoId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).contains("Apólice POL-INVALIDA não encontrada");

            verify(commandBus).send(any(AssociarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve retornar erro interno quando ocorre exceção")
        void shouldReturnInternalErrorWhenExceptionOccurs() {
            // Given
            String veiculoId = "VEI-12345";
            AssociarVeiculoRequestDTO request = new AssociarVeiculoRequestDTO(
                "POL-2024-001234",
                LocalDate.now(),
                "operador123"
            );

            when(commandBus.send(any(AssociarVeiculoCommand.class)))
                .thenThrow(new IllegalStateException("Estado inválido"));

            // When
            ResponseEntity<CommandResponseDTO> response = controller.associarApolice(veiculoId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).contains("Erro interno ao associar veículo");
            assertThat(response.getBody().details()).containsEntry("exception", "IllegalStateException");

            verify(commandBus).send(any(AssociarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve criar comando com data de início futura")
        void shouldCreateCommandWithFutureStartDate() {
            // Given
            String veiculoId = "VEI-12345";
            LocalDate dataFutura = LocalDate.now().plusDays(30);
            AssociarVeiculoRequestDTO request = new AssociarVeiculoRequestDTO(
                "POL-2024-001234",
                dataFutura,
                "operador123"
            );

            CommandResult commandResult = CommandResult.builder()
                .success(true)
                .data(veiculoId)
                .executedAt(Instant.now())
                .build();

            when(commandBus.send(any(AssociarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.associarApolice(veiculoId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(request.isDataInicioFutura()).isTrue();

            verify(commandBus).send(any(AssociarVeiculoCommand.class));
        }
    }

    // === TESTES DE DESASSOCIAÇÃO DE APÓLICE ===

    @Nested
    @DisplayName("Desassociar Apólice - POST /{id}/desassociar-apolice")
    class DesassociarApoliceTests {

        @Test
        @DisplayName("Deve desassociar veículo da apólice com sucesso")
        void shouldDisassociateVeiculoFromApoliceSuccessfully() {
            // Given
            String veiculoId = "VEI-12345";
            String apoliceId = "POL-2024-001234";
            String motivo = "Cancelamento da apólice por solicitação do cliente";

            DesassociarVeiculoRequestDTO request = new DesassociarVeiculoRequestDTO(
                apoliceId,
                LocalDate.now(),
                motivo,
                "operador123"
            );

            CommandResult commandResult = CommandResult.builder()
                .success(true)
                .data(veiculoId)
                .executedAt(Instant.now())
                .metadata(Map.of("apoliceId", apoliceId, "motivo", motivo))
                .build();

            when(commandBus.send(any(DesassociarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.desassociarApolice(veiculoId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().aggregateId()).isEqualTo(veiculoId);
            assertThat(response.getBody().message()).contains("desassociado da apólice com sucesso");
            assertThat(response.getBody().details()).containsEntry("apoliceId", apoliceId);
            assertThat(response.getBody().details()).containsEntry("motivo", motivo);

            verify(commandBus).send(any(DesassociarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve retornar erro quando veículo não está associado")
        void shouldReturnErrorWhenVeiculoNotAssociated() {
            // Given
            String veiculoId = "VEI-12345";
            DesassociarVeiculoRequestDTO request = new DesassociarVeiculoRequestDTO(
                "POL-2024-001234",
                LocalDate.now(),
                "Cancelamento da apólice por solicitação do cliente",
                "operador123"
            );

            CommandResult commandResult = CommandResult.builder()
                .success(false)
                .errorMessage("Veículo não está associado à apólice POL-2024-001234")
                .executedAt(Instant.now())
                .build();

            when(commandBus.send(any(DesassociarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.desassociarApolice(veiculoId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).contains("Falha ao desassociar veículo");
            assertThat(response.getBody().message()).contains("não está associado");

            verify(commandBus).send(any(DesassociarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve retornar erro quando veículo não existe")
        void shouldReturnErrorWhenVeiculoNotFound() {
            // Given
            String veiculoId = "VEI-99999";
            DesassociarVeiculoRequestDTO request = new DesassociarVeiculoRequestDTO(
                "POL-2024-001234",
                LocalDate.now(),
                "Cancelamento da apólice por solicitação do cliente",
                "operador123"
            );

            CommandResult commandResult = CommandResult.builder()
                .success(false)
                .errorMessage("Veículo VEI-99999 não encontrado")
                .executedAt(Instant.now())
                .build();

            when(commandBus.send(any(DesassociarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.desassociarApolice(veiculoId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).contains("não encontrado");

            verify(commandBus).send(any(DesassociarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve retornar erro interno quando ocorre exceção")
        void shouldReturnInternalErrorWhenExceptionOccurs() {
            // Given
            String veiculoId = "VEI-12345";
            DesassociarVeiculoRequestDTO request = new DesassociarVeiculoRequestDTO(
                "POL-2024-001234",
                LocalDate.now(),
                "Cancelamento da apólice por solicitação do cliente",
                "operador123"
            );

            when(commandBus.send(any(DesassociarVeiculoCommand.class)))
                .thenThrow(new NullPointerException("Referência nula"));

            // When
            ResponseEntity<CommandResponseDTO> response = controller.desassociarApolice(veiculoId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).contains("Erro interno ao desassociar veículo");
            assertThat(response.getBody().details()).containsEntry("exception", "NullPointerException");

            verify(commandBus).send(any(DesassociarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve categorizar motivo de cancelamento")
        void shouldCategorizeMotivoCancelamento() {
            // Given
            String veiculoId = "VEI-12345";
            DesassociarVeiculoRequestDTO request = new DesassociarVeiculoRequestDTO(
                "POL-2024-001234",
                LocalDate.now(),
                "Cancelamento da apólice por solicitação",
                "operador123"
            );

            CommandResult commandResult = CommandResult.builder()
                .success(true)
                .data(veiculoId)
                .executedAt(Instant.now())
                .build();

            when(commandBus.send(any(DesassociarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.desassociarApolice(veiculoId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(request.categorizarMotivo()).isEqualTo("CANCELAMENTO");

            verify(commandBus).send(any(DesassociarVeiculoCommand.class));
        }

        @Test
        @DisplayName("Deve categorizar motivo de venda")
        void shouldCategorizeMotivoVenda() {
            // Given
            DesassociarVeiculoRequestDTO request = new DesassociarVeiculoRequestDTO(
                "POL-2024-001234",
                LocalDate.now(),
                "Veículo foi vendido para terceiro",
                "operador123"
            );

            // Then
            assertThat(request.categorizarMotivo()).isEqualTo("VENDA_VEICULO");
        }

        @Test
        @DisplayName("Deve categorizar motivo de sinistro")
        void shouldCategorizeMotivoSinistro() {
            // Given
            DesassociarVeiculoRequestDTO request = new DesassociarVeiculoRequestDTO(
                "POL-2024-001234",
                LocalDate.now(),
                "Sinistro total - perda total do veículo",
                "operador123"
            );

            // Then
            assertThat(request.categorizarMotivo()).isEqualTo("SINISTRO_TOTAL");
        }

        @Test
        @DisplayName("Deve validar data de fim não muito no passado")
        void shouldValidateDataFimNotTooOld() {
            // Given
            LocalDate dataRecente = LocalDate.now().minusDays(3);
            DesassociarVeiculoRequestDTO request = new DesassociarVeiculoRequestDTO(
                "POL-2024-001234",
                dataRecente,
                "Cancelamento da apólice por solicitação do cliente",
                "operador123"
            );

            // Then
            assertThat(request.isDataFimValida()).isTrue();
        }

        @Test
        @DisplayName("Deve identificar data de fim inválida (muito antiga)")
        void shouldIdentifyInvalidDataFim() {
            // Given
            LocalDate dataAntiga = LocalDate.now().minusDays(30);
            DesassociarVeiculoRequestDTO request = new DesassociarVeiculoRequestDTO(
                "POL-2024-001234",
                dataAntiga,
                "Cancelamento da apólice por solicitação do cliente",
                "operador123"
            );

            // Then
            assertThat(request.isDataFimValida()).isFalse();
        }
    }

    // === TESTES DE HEALTH CHECK ===

    @Nested
    @DisplayName("Health Check - GET /commands/health")
    class HealthCheckTests {

        @Test
        @DisplayName("Deve retornar health check com status UP")
        void shouldReturnHealthCheckWithStatusUp() {
            // When
            ResponseEntity<Map<String, Object>> response = controller.healthCheck();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("status")).isEqualTo("UP");
            assertThat(response.getBody().get("service")).isEqualTo("VeiculoCommandController");
            assertThat(response.getBody().get("timestamp")).isInstanceOf(Instant.class);
            assertThat(response.getBody().get("commandBus")).isEqualTo("AVAILABLE");
        }

        @Test
        @DisplayName("Deve incluir timestamp no health check")
        void shouldIncludeTimestampInHealthCheck() {
            // When
            ResponseEntity<Map<String, Object>> response = controller.healthCheck();

            // Then
            assertThat(response.getBody()).isNotNull();

            Instant timestamp = (Instant) response.getBody().get("timestamp");
            assertThat(timestamp).isNotNull();
            assertThat(timestamp).isBeforeOrEqualTo(Instant.now());
        }

        @Test
        @DisplayName("Deve indicar status do CommandBus")
        void shouldIndicateCommandBusStatus() {
            // When
            ResponseEntity<Map<String, Object>> response = controller.healthCheck();

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("commandBus")).isEqualTo("AVAILABLE");
        }

        @Test
        @DisplayName("Deve verificar disponibilidade do CommandBus")
        void shouldVerifyCommandBusAvailability() {
            // When
            ResponseEntity<Map<String, Object>> response = controller.healthCheck();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsKeys("status", "service", "timestamp", "commandBus");
        }

        @Test
        @DisplayName("Deve retornar informações do serviço no health check")
        void shouldReturnServiceInformationInHealthCheck() {
            // When
            ResponseEntity<Map<String, Object>> response = controller.healthCheck();

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("service")).isEqualTo("VeiculoCommandController");
            assertThat(response.getBody().get("status")).isEqualTo("UP");
        }

        @Test
        @DisplayName("Deve ter todas as chaves esperadas no health check")
        void shouldHaveAllExpectedKeysInHealthCheck() {
            // When
            ResponseEntity<Map<String, Object>> response = controller.healthCheck();

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsKeys(
                "status",
                "service",
                "timestamp",
                "commandBus"
            );
        }
    }

    // === TESTES DE INTEGRAÇÃO ENTRE MÉTODOS ===

    @Nested
    @DisplayName("Testes de Integração")
    class IntegrationTests {

        @Test
        @DisplayName("Deve processar comando com correlationId único")
        void shouldProcessCommandWithUniqueCorrelationId() {
            // Given
            CriarVeiculoRequestDTO request = criarRequestCompleto();

            CommandResult commandResult = CommandResult.builder()
                .success(true)
                .data("VEI-12345")
                .executedAt(Instant.now())
                .build();

            when(commandBus.send(any(CriarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.criarVeiculo(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            verify(commandBus).send(argThat(cmd -> {
                if (cmd instanceof CriarVeiculoCommand) {
                    CriarVeiculoCommand criar = (CriarVeiculoCommand) cmd;
                    return criar.getCorrelationId() != null;
                }
                return false;
            }));
        }

        @Test
        @DisplayName("Deve incluir userId em todos os comandos")
        void shouldIncludeUserIdInAllCommands() {
            // Given
            CriarVeiculoRequestDTO request = criarRequestCompleto();
            String operadorId = "operador123";

            CommandResult commandResult = CommandResult.builder()
                .success(true)
                .data("VEI-12345")
                .executedAt(Instant.now())
                .build();

            when(commandBus.send(any(CriarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            controller.criarVeiculo(request);

            // Then
            verify(commandBus).send(argThat(cmd -> {
                if (cmd instanceof CriarVeiculoCommand) {
                    CriarVeiculoCommand criar = (CriarVeiculoCommand) cmd;
                    return criar.getUserId() != null && criar.getUserId().equals(operadorId);
                }
                return false;
            }));
        }

        @Test
        @DisplayName("Deve manter consistência de timestamps nas respostas")
        void shouldMaintainConsistentTimestampsInResponses() {
            // Given
            CriarVeiculoRequestDTO request = criarRequestCompleto();

            Instant commandTime = Instant.now();
            CommandResult commandResult = CommandResult.builder()
                .success(true)
                .data("VEI-12345")
                .executedAt(commandTime)
                .build();

            when(commandBus.send(any(CriarVeiculoCommand.class))).thenReturn(commandResult);

            // When
            ResponseEntity<CommandResponseDTO> response = controller.criarVeiculo(request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().timestamp()).isEqualTo(commandTime);
        }

        private CriarVeiculoRequestDTO criarRequestCompleto() {
            Especificacao especificacao = Especificacao.of(
                "Preto",
                TipoCombustivel.DIESEL,
                CategoriaVeiculo.CAMINHAO,
                3000
            );

            Proprietario proprietario = Proprietario.of(
                "11222333000181",
                "Empresa de Transporte LTDA",
                TipoPessoa.JURIDICA
            );

            return new CriarVeiculoRequestDTO(
                "XYZ9876",
                "98765432109",
                "9BWZZZ377VT004251",
                "Mercedes",
                "Actros",
                2023,
                2024,
                especificacao,
                proprietario,
                "operador123"
            );
        }
    }
}
