package com.seguradora.hibrida.domain.veiculo.service;

import com.seguradora.hibrida.domain.veiculo.query.repository.VeiculoQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link VeiculoValidationService}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VeiculoValidationService - Testes Unitários")
class VeiculoValidationServiceTest {

    @Mock
    private VeiculoQueryRepository veiculoQueryRepository;

    @InjectMocks
    private VeiculoValidationService validationService;

    private static final String PLACA_VALIDA = "ABC1234";
    private static final String RENAVAM_VALIDO = "12345678900";
    private static final String CHASSI_VALIDO = "1HGBH41J6MN109186";

    @BeforeEach
    void setUp() {
        // Configuração padrão: nenhum veículo existe
        lenient().when(veiculoQueryRepository.existsByPlaca(anyString())).thenReturn(false);
        lenient().when(veiculoQueryRepository.existsByRenavam(anyString())).thenReturn(false);
        lenient().when(veiculoQueryRepository.existsByChassi(anyString())).thenReturn(false);
    }

    @Nested
    @DisplayName("Testes de Validação de Unicidade - Sucesso")
    class ValidacaoUnicidadeSucessoTests {

        @Test
        @DisplayName("Deve validar com sucesso quando nenhum campo existe")
        void deveValidarComSucessoQuandoNenhumCampoExiste() {
            // Act & Assert
            assertThatCode(() -> validationService.validarUnicidade(
                PLACA_VALIDA, RENAVAM_VALIDO, CHASSI_VALIDO
            )).doesNotThrowAnyException();

            // Verify
            verify(veiculoQueryRepository).existsByPlaca(PLACA_VALIDA);
            verify(veiculoQueryRepository).existsByRenavam(RENAVAM_VALIDO);
            verify(veiculoQueryRepository).existsByChassi(CHASSI_VALIDO);
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Unicidade - Falha por Placa")
    class ValidacaoUnicidadeFalhaPlacaTests {

        @Test
        @DisplayName("Deve lançar exceção quando placa já existe")
        void deveLancarExcecaoQuandoPlacaJaExiste() {
            // Arrange
            when(veiculoQueryRepository.existsByPlaca(PLACA_VALIDA)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> validationService.validarUnicidade(
                PLACA_VALIDA, RENAVAM_VALIDO, CHASSI_VALIDO
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Placa já cadastrada no sistema")
                .hasMessageContaining(PLACA_VALIDA);

            // Verify - não deve verificar os demais campos após encontrar placa duplicada
            verify(veiculoQueryRepository).existsByPlaca(PLACA_VALIDA);
            verify(veiculoQueryRepository, never()).existsByRenavam(anyString());
            verify(veiculoQueryRepository, never()).existsByChassi(anyString());
        }

        @Test
        @DisplayName("Deve lançar exceção com placa formatada")
        void deveLancarExcecaoComPlacaFormatada() {
            // Arrange
            String placaFormatada = "ABC-1234";
            when(veiculoQueryRepository.existsByPlaca(placaFormatada)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> validationService.validarUnicidade(
                placaFormatada, RENAVAM_VALIDO, CHASSI_VALIDO
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(placaFormatada);
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Unicidade - Falha por RENAVAM")
    class ValidacaoUnicidadeFalhaRenavamTests {

        @Test
        @DisplayName("Deve lançar exceção quando RENAVAM já existe")
        void deveLancarExcecaoQuandoRenavamJaExiste() {
            // Arrange
            when(veiculoQueryRepository.existsByRenavam(RENAVAM_VALIDO)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> validationService.validarUnicidade(
                PLACA_VALIDA, RENAVAM_VALIDO, CHASSI_VALIDO
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RENAVAM já cadastrado no sistema")
                .hasMessageContaining(RENAVAM_VALIDO);

            // Verify - verifica placa primeiro, depois RENAVAM, mas não chassi
            verify(veiculoQueryRepository).existsByPlaca(PLACA_VALIDA);
            verify(veiculoQueryRepository).existsByRenavam(RENAVAM_VALIDO);
            verify(veiculoQueryRepository, never()).existsByChassi(anyString());
        }

        @Test
        @DisplayName("Deve lançar exceção com RENAVAM sem formatação")
        void deveLancarExcecaoComRenavamSemFormatacao() {
            // Arrange
            String renavamNumerico = "12345678900";
            when(veiculoQueryRepository.existsByRenavam(renavamNumerico)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> validationService.validarUnicidade(
                PLACA_VALIDA, renavamNumerico, CHASSI_VALIDO
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(renavamNumerico);
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Unicidade - Falha por Chassi")
    class ValidacaoUnicidadeFalhaChssiTests {

        @Test
        @DisplayName("Deve lançar exceção quando chassi já existe")
        void deveLancarExcecaoQuandoChassiJaExiste() {
            // Arrange
            when(veiculoQueryRepository.existsByChassi(CHASSI_VALIDO)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> validationService.validarUnicidade(
                PLACA_VALIDA, RENAVAM_VALIDO, CHASSI_VALIDO
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chassi já cadastrado no sistema")
                .hasMessageContaining(CHASSI_VALIDO);

            // Verify - verifica todos os campos antes
            verify(veiculoQueryRepository).existsByPlaca(PLACA_VALIDA);
            verify(veiculoQueryRepository).existsByRenavam(RENAVAM_VALIDO);
            verify(veiculoQueryRepository).existsByChassi(CHASSI_VALIDO);
        }

        @Test
        @DisplayName("Deve lançar exceção com chassi em uppercase")
        void deveLancarExcecaoComChassiEmUppercase() {
            // Arrange
            String chassiUppercase = "1HGBH41J6MN109186";
            when(veiculoQueryRepository.existsByChassi(chassiUppercase)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> validationService.validarUnicidade(
                PLACA_VALIDA, RENAVAM_VALIDO, chassiUppercase
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(chassiUppercase);
        }
    }

    @Nested
    @DisplayName("Testes de Cenários Múltiplos")
    class CenariosMultiplosTests {

        @Test
        @DisplayName("Deve lançar exceção para placa mesmo se RENAVAM e chassi também existem")
        void deveLancarExcecaoParaPlacaMesmoSeRenavamEChassiTambemExistem() {
            // Arrange - todos existem
            when(veiculoQueryRepository.existsByPlaca(PLACA_VALIDA)).thenReturn(true);
            lenient().when(veiculoQueryRepository.existsByRenavam(RENAVAM_VALIDO)).thenReturn(true);
            lenient().when(veiculoQueryRepository.existsByChassi(CHASSI_VALIDO)).thenReturn(true);

            // Act & Assert - deve lançar exceção para placa primeiro
            assertThatThrownBy(() -> validationService.validarUnicidade(
                PLACA_VALIDA, RENAVAM_VALIDO, CHASSI_VALIDO
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Placa");

            // Verify - para no primeiro erro
            verify(veiculoQueryRepository).existsByPlaca(PLACA_VALIDA);
            verify(veiculoQueryRepository, never()).existsByRenavam(anyString());
            verify(veiculoQueryRepository, never()).existsByChassi(anyString());
        }

        @Test
        @DisplayName("Deve validar diferentes combinações de valores")
        void deveValidarDiferentesCombinavoesDeValores() {
            // Cenário 1: Placa diferente, mas válida
            assertThatCode(() -> validationService.validarUnicidade(
                "XYZ9876", RENAVAM_VALIDO, CHASSI_VALIDO
            )).doesNotThrowAnyException();

            // Cenário 2: RENAVAM diferente, mas válido
            assertThatCode(() -> validationService.validarUnicidade(
                PLACA_VALIDA, "12345678909", CHASSI_VALIDO
            )).doesNotThrowAnyException();

            // Cenário 3: Chassi diferente, mas válido
            assertThatCode(() -> validationService.validarUnicidade(
                PLACA_VALIDA, RENAVAM_VALIDO, "9BWZZZ377VT004251"
            )).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de Interação com Repository")
    class InteracaoRepositoryTests {

        @Test
        @DisplayName("Deve chamar métodos do repository na ordem correta")
        void deveChamarMetodosDoRepositoryNaOrdemCorreta() {
            // Act
            validationService.validarUnicidade(PLACA_VALIDA, RENAVAM_VALIDO, CHASSI_VALIDO);

            // Verify - ordem de chamadas
            var inOrder = inOrder(veiculoQueryRepository);
            inOrder.verify(veiculoQueryRepository).existsByPlaca(PLACA_VALIDA);
            inOrder.verify(veiculoQueryRepository).existsByRenavam(RENAVAM_VALIDO);
            inOrder.verify(veiculoQueryRepository).existsByChassi(CHASSI_VALIDO);
        }

        @Test
        @DisplayName("Deve chamar cada método do repository exatamente uma vez em caso de sucesso")
        void deveChamarCadaMetodoDoRepositoryExatamenteUmaVezEmCasoDeSucesso() {
            // Act
            validationService.validarUnicidade(PLACA_VALIDA, RENAVAM_VALIDO, CHASSI_VALIDO);

            // Verify
            verify(veiculoQueryRepository, times(1)).existsByPlaca(PLACA_VALIDA);
            verify(veiculoQueryRepository, times(1)).existsByRenavam(RENAVAM_VALIDO);
            verify(veiculoQueryRepository, times(1)).existsByChassi(CHASSI_VALIDO);
        }

        @Test
        @DisplayName("Não deve chamar métodos subsequentes se encontrar duplicata")
        void naoDeveChamarMetodosSubsequenteSeEncontrarDuplicata() {
            // Arrange
            when(veiculoQueryRepository.existsByPlaca(PLACA_VALIDA)).thenReturn(true);

            // Act
            try {
                validationService.validarUnicidade(PLACA_VALIDA, RENAVAM_VALIDO, CHASSI_VALIDO);
            } catch (IllegalArgumentException e) {
                // Esperado
            }

            // Verify
            verify(veiculoQueryRepository, times(1)).existsByPlaca(PLACA_VALIDA);
            verify(veiculoQueryRepository, never()).existsByRenavam(anyString());
            verify(veiculoQueryRepository, never()).existsByChassi(anyString());
        }
    }
}
