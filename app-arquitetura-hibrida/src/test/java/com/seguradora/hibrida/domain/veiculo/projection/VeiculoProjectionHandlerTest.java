package com.seguradora.hibrida.domain.veiculo.projection;

import com.seguradora.hibrida.domain.veiculo.event.*;
import com.seguradora.hibrida.domain.veiculo.model.*;
import com.seguradora.hibrida.domain.veiculo.query.model.VeiculoQueryModel;
import com.seguradora.hibrida.domain.veiculo.query.repository.VeiculoQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link VeiculoProjectionHandler}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VeiculoProjectionHandler - Testes Unitários")
class VeiculoProjectionHandlerTest {

    @Mock
    private VeiculoQueryRepository veiculoQueryRepository;

    @InjectMocks
    private VeiculoProjectionHandler handler;

    @Captor
    private ArgumentCaptor<VeiculoQueryModel> queryModelCaptor;

    private VeiculoCriadoEvent eventoCriacao;
    private VeiculoQueryModel queryModelExistente;

    @BeforeEach
    void setUp() {
        // Evento de criação
        eventoCriacao = new VeiculoCriadoEvent(
            "VEI-001",
            1L,
            "ABC1234",
            "12345678900",
            "1HGBH41J6MN109186",
            "Honda",
            "Civic",
            2023,
            2024,
            "Branco",
            "FLEX",
            "PASSEIO",
            1600,
            "12345678909",
            "João Silva",
            "FISICA",
            "OP-123"
        );

        // Query model existente para updates
        queryModelExistente = new VeiculoQueryModel();
        queryModelExistente.setId("VEI-001");
        queryModelExistente.setPlaca("ABC1234");
        queryModelExistente.setRenavam("12345678900");
        queryModelExistente.setChassi("1HGBH41J6MN109186");
        queryModelExistente.setMarca("Honda");
        queryModelExistente.setModelo("Civic");
        queryModelExistente.setAnoFabricacao(2023);
        queryModelExistente.setAnoModelo(2024);
        queryModelExistente.setCor("Branco");
        queryModelExistente.setTipoCombustivel("FLEX");
        queryModelExistente.setCategoria("PASSEIO");
        queryModelExistente.setCilindrada(1600);
        queryModelExistente.setProprietarioCpf("11144477735");
        queryModelExistente.setProprietarioNome("João Silva");
        queryModelExistente.setProprietarioTipo("FISICA");
        queryModelExistente.setStatus(StatusVeiculo.ATIVO);
        queryModelExistente.setApoliceAtiva(false);
        queryModelExistente.setVersion(1L);
    }

    @Nested
    @DisplayName("Testes de Configuração do Handler")
    class ConfiguracaoHandlerTests {

        @Test
        @DisplayName("Deve retornar tipo de evento correto")
        void deveRetornarTipoEventoCorreto() {
            assertThat(handler.getEventType()).isEqualTo(VeiculoCriadoEvent.class);
        }

        @Test
        @DisplayName("Deve retornar nome da projeção")
        void deveRetornarNomeProjecao() {
            assertThat(handler.getProjectionName()).isEqualTo("VeiculoProjection");
        }

        @Test
        @DisplayName("Deve ter ordem 100")
        void deveTermOrdem100() {
            assertThat(handler.getOrder()).isEqualTo(100);
        }

        @Test
        @DisplayName("Deve ser assíncrono")
        void deveSerAssincrono() {
            assertThat(handler.isAsync()).isTrue();
        }

        @Test
        @DisplayName("Deve ter timeout de 30 segundos")
        void deveTermTimeout30Segundos() {
            assertThat(handler.getTimeoutSeconds()).isEqualTo(30);
        }

        @Test
        @DisplayName("Deve ser retentável")
        void deveSerRetentavel() {
            assertThat(handler.isRetryable()).isTrue();
        }

        @Test
        @DisplayName("Deve ter máximo de 3 tentativas")
        void deveTermMaximo3Tentativas() {
            assertThat(handler.getMaxRetries()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Testes de Evento VeiculoCriadoEvent")
    class VeiculoCriadoEventTests {

        @Test
        @DisplayName("Deve criar projeção ao processar evento de criação")
        void deveCriarProjecaoAoProcessarEventoCriacao() {
            // Act
            handler.doHandle(eventoCriacao);

            // Assert
            verify(veiculoQueryRepository).save(queryModelCaptor.capture());

            VeiculoQueryModel saved = queryModelCaptor.getValue();
            assertThat(saved.getId()).isEqualTo("VEI-001");
            assertThat(saved.getPlaca()).isEqualTo("ABC1234");
            assertThat(saved.getRenavam()).isEqualTo("12345678900");
            assertThat(saved.getChassi()).isEqualTo("1HGBH41J6MN109186");
        }

        @Test
        @DisplayName("Deve mapear dados básicos do veículo")
        void deveMapeardadosBasicosVeiculo() {
            // Act
            handler.doHandle(eventoCriacao);

            // Assert
            verify(veiculoQueryRepository).save(queryModelCaptor.capture());

            VeiculoQueryModel saved = queryModelCaptor.getValue();
            assertThat(saved.getMarca()).isEqualTo("Honda");
            assertThat(saved.getModelo()).isEqualTo("Civic");
            assertThat(saved.getAnoFabricacao()).isEqualTo(2023);
            assertThat(saved.getAnoModelo()).isEqualTo(2024);
        }

        @Test
        @DisplayName("Deve mapear especificações")
        void deveMapeardspecificacoes() {
            // Act
            handler.doHandle(eventoCriacao);

            // Assert
            verify(veiculoQueryRepository).save(queryModelCaptor.capture());

            VeiculoQueryModel saved = queryModelCaptor.getValue();
            assertThat(saved.getCor()).isEqualTo("Branco");
            assertThat(saved.getTipoCombustivel()).isEqualTo("FLEX");
            assertThat(saved.getCategoria()).isEqualTo("PASSEIO");
            assertThat(saved.getCilindrada()).isEqualTo(1600);
        }

        @Test
        @DisplayName("Deve mapear dados do proprietário")
        void deveMapeardadosProprietario() {
            // Act
            handler.doHandle(eventoCriacao);

            // Assert
            verify(veiculoQueryRepository).save(queryModelCaptor.capture());

            VeiculoQueryModel saved = queryModelCaptor.getValue();
            assertThat(saved.getProprietarioCpf()).isEqualTo("12345678909");
            assertThat(saved.getProprietarioNome()).isEqualTo("João Silva");
            assertThat(saved.getProprietarioTipo()).isEqualTo("FISICA");
        }

        @Test
        @DisplayName("Deve definir status ATIVO para novo veículo")
        void deveDefinirStatusAtivoParaNovoVeiculo() {
            // Act
            handler.doHandle(eventoCriacao);

            // Assert
            verify(veiculoQueryRepository).save(queryModelCaptor.capture());

            VeiculoQueryModel saved = queryModelCaptor.getValue();
            assertThat(saved.getStatus()).isEqualTo(StatusVeiculo.ATIVO);
        }

        @Test
        @DisplayName("Deve definir apoliceAtiva como false para novo veículo")
        void deveDefinirApoliceAtivaFalseParaNovoVeiculo() {
            // Act
            handler.doHandle(eventoCriacao);

            // Assert
            verify(veiculoQueryRepository).save(queryModelCaptor.capture());

            VeiculoQueryModel saved = queryModelCaptor.getValue();
            assertThat(saved.getApoliceAtiva()).isFalse();
        }

        @Test
        @DisplayName("Deve definir versão do evento")
        void deveDefinirVersaoEvento() {
            // Act
            handler.doHandle(eventoCriacao);

            // Assert
            verify(veiculoQueryRepository).save(queryModelCaptor.capture());

            VeiculoQueryModel saved = queryModelCaptor.getValue();
            assertThat(saved.getVersion()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve definir lastEventId como 1")
        void deveDefinirLastEventIdComo1() {
            // Act
            handler.doHandle(eventoCriacao);

            // Assert
            verify(veiculoQueryRepository).save(queryModelCaptor.capture());

            VeiculoQueryModel saved = queryModelCaptor.getValue();
            assertThat(saved.getLastEventId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve suportar evento válido")
        void deveSuportarEventoValido() {
            assertThat(handler.supports(eventoCriacao)).isTrue();
        }

        @Test
        @DisplayName("Não deve suportar evento nulo")
        void naoDeveSuportarEventoNulo() {
            assertThat(handler.supports(null)).isFalse();
        }

        @Test
        @DisplayName("Não deve suportar evento sem aggregateId")
        void naoDeveSuportarEventoSemAggregateId() {
            VeiculoCriadoEvent eventoInvalido = new VeiculoCriadoEvent(
                null, 1L, "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2023, 2024, "Branco", "FLEX", "PASSEIO", 1600,
                "11144477735", "João Silva", "FISICA", "OP-123"
            );

            assertThat(handler.supports(eventoInvalido)).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Evento VeiculoAtualizadoEvent")
    class VeiculoAtualizadoEventTests {

        @Test
        @DisplayName("Deve atualizar projeção ao processar evento de atualização")
        void deveAtualizarProjecaoAoProcessarEventoAtualizacao() {
            // Arrange
            when(veiculoQueryRepository.findById("VEI-001")).thenReturn(Optional.of(queryModelExistente));

            Especificacao especAtual = Especificacao.of("Branco", TipoCombustivel.FLEX, CategoriaVeiculo.PASSEIO, 1600);
            Especificacao novaEspec = Especificacao.of("Preto", TipoCombustivel.GASOLINA,
                CategoriaVeiculo.PASSEIO, 2000);

            VeiculoAtualizadoEvent eventoAtualizacao = VeiculoAtualizadoEvent.create(
                "VEI-001", 2L, especAtual, novaEspec, "OP-456", null
            );

            // Act
            handler.on(eventoAtualizacao);

            // Assert
            verify(veiculoQueryRepository).save(queryModelExistente);
            assertThat(queryModelExistente.getCor()).isEqualTo("Preto");
            assertThat(queryModelExistente.getTipoCombustivel()).isEqualTo("GASOLINA");
            assertThat(queryModelExistente.getCilindrada()).isEqualTo(2000);
            assertThat(queryModelExistente.getVersion()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Não deve fazer nada se veículo não existir")
        void naoDeveFazerNadaSeVeiculoNaoExistir() {
            // Arrange
            when(veiculoQueryRepository.findById("VEI-999")).thenReturn(Optional.empty());

            VeiculoAtualizadoEvent eventoAtualizacao = VeiculoAtualizadoEvent.create(
                "VEI-999", 2L, Especificacao.exemplo(), Especificacao.exemplo(), "OP-456", null
            );

            // Act
            handler.on(eventoAtualizacao);

            // Assert
            verify(veiculoQueryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve chamar onUpdate ao atualizar")
        void deveChamarOnUpdateAoAtualizar() {
            // Arrange
            when(veiculoQueryRepository.findById("VEI-001")).thenReturn(Optional.of(queryModelExistente));

            VeiculoAtualizadoEvent eventoAtualizacao = VeiculoAtualizadoEvent.create(
                "VEI-001", 2L, Especificacao.exemplo(), Especificacao.exemplo(), "OP-456", null
            );

            // Act
            handler.on(eventoAtualizacao);

            // Assert
            verify(veiculoQueryRepository).save(queryModelExistente);
            assertThat(queryModelExistente.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Testes de Evento VeiculoAssociadoEvent")
    class VeiculoAssociadoEventTests {

        @Test
        @DisplayName("Deve marcar apólice como ativa ao processar associação")
        void deveMarcarApoliceAtivaAoProcessarAssociacao() {
            // Arrange
            when(veiculoQueryRepository.findById("VEI-001")).thenReturn(Optional.of(queryModelExistente));

            VeiculoAssociadoEvent eventoAssociacao = new VeiculoAssociadoEvent(
                "VEI-001", 2L, "APO-001", LocalDate.now(), "OP-456"
            );

            // Act
            handler.on(eventoAssociacao);

            // Assert
            verify(veiculoQueryRepository).save(queryModelExistente);
            assertThat(queryModelExistente.getApoliceAtiva()).isTrue();
            assertThat(queryModelExistente.getVersion()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Não deve fazer nada se veículo não existir na associação")
        void naoDeveFazerNadaSeVeiculoNaoExistirNaAssociacao() {
            // Arrange
            when(veiculoQueryRepository.findById("VEI-999")).thenReturn(Optional.empty());

            VeiculoAssociadoEvent eventoAssociacao = new VeiculoAssociadoEvent(
                "VEI-999", 2L, "APO-001", LocalDate.now(), "OP-456"
            );

            // Act
            handler.on(eventoAssociacao);

            // Assert
            verify(veiculoQueryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Evento VeiculoDesassociadoEvent")
    class VeiculoDesassociadoEventTests {

        @Test
        @DisplayName("Deve marcar apólice como inativa ao processar desassociação")
        void deveMarcarApoliceInativaAoProcessarDesassociacao() {
            // Arrange
            queryModelExistente.setApoliceAtiva(true);
            when(veiculoQueryRepository.findById("VEI-001")).thenReturn(Optional.of(queryModelExistente));

            VeiculoDesassociadoEvent eventoDesassociacao = new VeiculoDesassociadoEvent(
                "VEI-001", 2L, "APO-001", LocalDate.now(), "Cancelamento", "OP-456"
            );

            // Act
            handler.on(eventoDesassociacao);

            // Assert
            verify(veiculoQueryRepository).save(queryModelExistente);
            assertThat(queryModelExistente.getApoliceAtiva()).isFalse();
            assertThat(queryModelExistente.getVersion()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Não deve fazer nada se veículo não existir na desassociação")
        void naoDeveFazerNadaSeVeiculoNaoExistirNaDesassociacao() {
            // Arrange
            when(veiculoQueryRepository.findById("VEI-999")).thenReturn(Optional.empty());

            VeiculoDesassociadoEvent eventoDesassociacao = new VeiculoDesassociadoEvent(
                "VEI-999", 2L, "APO-001", LocalDate.now(), "Cancelamento", "OP-456"
            );

            // Act
            handler.on(eventoDesassociacao);

            // Assert
            verify(veiculoQueryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Evento PropriedadeTransferidaEvent")
    class PropriedadeTransferidaEventTests {

        @Test
        @DisplayName("Deve atualizar proprietário ao processar transferência")
        void deveAtualizarProprietarioAoProcessarTransferencia() {
            // Arrange
            when(veiculoQueryRepository.findById("VEI-001")).thenReturn(Optional.of(queryModelExistente));

            Proprietario novoProprietario = Proprietario.of("12345678909", "Maria Santos", TipoPessoa.FISICA);

            PropriedadeTransferidaEvent eventoTransferencia = new PropriedadeTransferidaEvent(
                "VEI-001", 2L, Proprietario.exemplo(), novoProprietario, LocalDate.now(),
                "OP-456", null
            );

            // Act
            handler.on(eventoTransferencia);

            // Assert
            verify(veiculoQueryRepository).save(queryModelExistente);
            assertThat(queryModelExistente.getProprietarioCpf()).isEqualTo("12345678909");
            assertThat(queryModelExistente.getProprietarioNome()).isEqualTo("Maria Santos");
            assertThat(queryModelExistente.getProprietarioTipo()).isEqualTo("FISICA");
            assertThat(queryModelExistente.getVersion()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Não deve fazer nada se veículo não existir na transferência")
        void naoDeveFazerNadaSeVeiculoNaoExistirNaTransferencia() {
            // Arrange
            when(veiculoQueryRepository.findById("VEI-999")).thenReturn(Optional.empty());

            PropriedadeTransferidaEvent eventoTransferencia = new PropriedadeTransferidaEvent(
                "VEI-999", 2L, Proprietario.exemplo(), Proprietario.exemplo(), LocalDate.now(),
                "OP-456", null
            );

            // Act
            handler.on(eventoTransferencia);

            // Assert
            verify(veiculoQueryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve chamar onUpdate ao transferir propriedade")
        void deveChamarOnUpdateAoTransferirPropriedade() {
            // Arrange
            when(veiculoQueryRepository.findById("VEI-001")).thenReturn(Optional.of(queryModelExistente));

            PropriedadeTransferidaEvent eventoTransferencia = new PropriedadeTransferidaEvent(
                "VEI-001", 2L, Proprietario.exemplo(), Proprietario.exemplo(), LocalDate.now(),
                "OP-456", null
            );

            // Act
            handler.on(eventoTransferencia);

            // Assert
            verify(veiculoQueryRepository).save(queryModelExistente);
            assertThat(queryModelExistente.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Testes de Fluxo Completo")
    class FluxoCompletoTests {

        @Test
        @DisplayName("Deve processar ciclo completo: criar, associar, desassociar")
        void deveProcessarCicloCompletoCrearAssociarDesassociar() {
            // 1. Criar
            handler.doHandle(eventoCriacao);
            verify(veiculoQueryRepository).save(any(VeiculoQueryModel.class));

            // 2. Associar
            when(veiculoQueryRepository.findById("VEI-001")).thenReturn(Optional.of(queryModelExistente));

            VeiculoAssociadoEvent eventoAssociacao = new VeiculoAssociadoEvent(
                "VEI-001", 2L, "APO-001", LocalDate.now(), "OP-456"
            );
            handler.on(eventoAssociacao);

            assertThat(queryModelExistente.getApoliceAtiva()).isTrue();

            // 3. Desassociar
            VeiculoDesassociadoEvent eventoDesassociacao = new VeiculoDesassociadoEvent(
                "VEI-001", 3L, "APO-001", LocalDate.now(), "Cancelamento", "OP-456"
            );
            handler.on(eventoDesassociacao);

            assertThat(queryModelExistente.getApoliceAtiva()).isFalse();
            assertThat(queryModelExistente.getVersion()).isEqualTo(3L);
        }

        @Test
        @DisplayName("Deve processar ciclo: criar, atualizar, transferir")
        void deveProcessarCicloCrearAtualizarTransferir() {
            // 1. Criar
            handler.doHandle(eventoCriacao);

            // 2. Atualizar especificação
            when(veiculoQueryRepository.findById("VEI-001")).thenReturn(Optional.of(queryModelExistente));

            Especificacao especAtual2 = Especificacao.of("Branco", TipoCombustivel.FLEX, CategoriaVeiculo.PASSEIO, 1600);
            Especificacao novaEspec = Especificacao.of("Azul", TipoCombustivel.GASOLINA,
                CategoriaVeiculo.PASSEIO, 1800);

            VeiculoAtualizadoEvent eventoAtualizacao = VeiculoAtualizadoEvent.create(
                "VEI-001", 2L, especAtual2, novaEspec, "OP-456", null
            );
            handler.on(eventoAtualizacao);

            assertThat(queryModelExistente.getCor()).isEqualTo("Azul");

            // 3. Transferir propriedade
            Proprietario novoProprietario = Proprietario.of("11222333000181", "Empresa XYZ", TipoPessoa.JURIDICA);

            PropriedadeTransferidaEvent eventoTransferencia = new PropriedadeTransferidaEvent(
                "VEI-001", 3L, Proprietario.exemplo(), novoProprietario, LocalDate.now(),
                "OP-456", null
            );
            handler.on(eventoTransferencia);

            assertThat(queryModelExistente.getProprietarioCpf()).isEqualTo("11222333000181");
            assertThat(queryModelExistente.getProprietarioTipo()).isEqualTo("JURIDICA");
        }
    }
}
