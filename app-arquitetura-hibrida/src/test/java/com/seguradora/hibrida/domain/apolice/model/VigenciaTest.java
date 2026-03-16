package com.seguradora.hibrida.domain.apolice.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para a classe Vigencia.
 */
@DisplayName("Vigencia - Testes Unitários")
class VigenciaTest {

    @Test
    @DisplayName("Deve criar vigência válida com datas específicas")
    void deveCriarVigenciaValidaComDatasEspecificas() {
        // Arrange
        LocalDate inicio = LocalDate.now();
        LocalDate fim = LocalDate.now().plusMonths(6);

        // Act
        Vigencia vigencia = Vigencia.of(inicio, fim);

        // Assert
        assertThat(vigencia).isNotNull();
        assertThat(vigencia.getInicio()).isEqualTo(inicio);
        assertThat(vigencia.getFim()).isEqualTo(fim);
    }

    @Test
    @DisplayName("Deve criar vigência anual a partir de data específica")
    void deveCriarVigenciaAnualComDataEspecifica() {
        // Arrange
        LocalDate inicio = LocalDate.now();

        // Act
        Vigencia vigencia = Vigencia.anual(inicio);

        // Assert
        assertThat(vigencia.getInicio()).isEqualTo(inicio);
        assertThat(vigencia.getFim()).isEqualTo(inicio.plusYears(1).minusDays(1));
        assertThat(vigencia.getDiasVigencia()).isGreaterThanOrEqualTo(365); // Mínimo 365 dias
    }

    @Test
    @DisplayName("Deve criar vigência anual a partir de hoje")
    void deveCriarVigenciaAnualAPartirDeHoje() {
        // Arrange
        LocalDate hoje = LocalDate.now();

        // Act
        Vigencia vigencia = Vigencia.anualAPartirDeHoje();

        // Assert
        assertThat(vigencia.getInicio()).isEqualTo(hoje);
        assertThat(vigencia.getFim()).isEqualTo(hoje.plusYears(1).minusDays(1));
    }

    @Test
    @DisplayName("Deve criar vigência com duração específica em meses")
    void deveCriarVigenciaComDuracaoEmMeses() {
        // Arrange
        LocalDate inicio = LocalDate.now();
        int meses = 6;

        // Act
        Vigencia vigencia = Vigencia.comDuracaoMeses(inicio, meses);

        // Assert
        assertThat(vigencia.getInicio()).isEqualTo(inicio);
        assertThat(vigencia.getFim()).isEqualTo(inicio.plusMonths(meses).minusDays(1));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar vigência com data início nula")
    void deveLancarExcecaoAoCriarComDataInicioNula() {
        // Arrange
        LocalDate fim = LocalDate.now().plusMonths(6);

        // Act & Assert
        assertThatThrownBy(() -> Vigencia.of(null, fim))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de início não pode ser nula");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar vigência com data fim nula")
    void deveLancarExcecaoAoCriarComDataFimNula() {
        // Arrange
        LocalDate inicio = LocalDate.now();

        // Act & Assert
        assertThatThrownBy(() -> Vigencia.of(inicio, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de fim não pode ser nula");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar vigência com data fim anterior à data início")
    void deveLancarExcecaoComDataFimAnteriorDataInicio() {
        // Arrange
        LocalDate fim = LocalDate.now();
        LocalDate inicio = fim.plusDays(1);

        // Act & Assert
        assertThatThrownBy(() -> Vigencia.of(inicio, fim))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de fim não pode ser anterior à data de início");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar vigência com data início muito anterior")
    void deveLancarExcecaoComDataInicioMuitoAnterior() {
        // Arrange
        LocalDate inicioMuitoAnterior = LocalDate.now().minusDays(31);
        LocalDate fim = inicioMuitoAnterior.plusYears(1);

        // Act & Assert
        assertThatThrownBy(() -> Vigencia.of(inicioMuitoAnterior, fim))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de início não pode ser muito anterior à data atual");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar vigência com duração menor que 30 dias")
    void deveLancarExcecaoComDuracaoMenorQue30Dias() {
        // Arrange
        LocalDate inicio = LocalDate.now();
        LocalDate fim = inicio.plusDays(28); // 29 dias total (inclusivo), menos que o mínimo de 30

        // Act & Assert
        assertThatThrownBy(() -> Vigencia.of(inicio, fim))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Vigência mínima é de 30 dias");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar vigência com duração maior que 5 anos")
    void deveLancarExcecaoComDuracaoMaiorQue5Anos() {
        // Arrange
        LocalDate inicio = LocalDate.now();
        LocalDate fim = inicio.plusYears(5).plusDays(1);

        // Act & Assert
        assertThatThrownBy(() -> Vigencia.of(inicio, fim))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Vigência máxima é de 5 anos");
    }

    @Test
    @DisplayName("Deve aceitar vigência com exatamente 30 dias")
    void deveAceitarVigenciaComExatamente30Dias() {
        // Arrange
        LocalDate inicio = LocalDate.now();
        LocalDate fim = inicio.plusDays(29); // 30 dias total (inclusivo)

        // Act & Assert
        assertThatNoException().isThrownBy(() -> Vigencia.of(inicio, fim));
    }

    @Test
    @DisplayName("Deve aceitar vigência com exatamente 5 anos")
    void deveAceitarVigenciaComExatamente5Anos() {
        // Arrange
        LocalDate inicio = LocalDate.now();
        LocalDate fim = inicio.plusYears(5).minusDays(1);

        // Act & Assert
        assertThatNoException().isThrownBy(() -> Vigencia.of(inicio, fim));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar com duração menor que 1 mês")
    void deveLancarExcecaoAoCriarComDuracaoMenorQue1Mes() {
        // Arrange
        LocalDate inicio = LocalDate.now();

        // Act & Assert
        assertThatThrownBy(() -> Vigencia.comDuracaoMeses(inicio, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duração deve estar entre 1 e 60 meses");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar com duração maior que 60 meses")
    void deveLancarExcecaoAoCriarComDuracaoMaiorQue60Meses() {
        // Arrange
        LocalDate inicio = LocalDate.now();

        // Act & Assert
        assertThatThrownBy(() -> Vigencia.comDuracaoMeses(inicio, 61))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duração deve estar entre 1 e 60 meses");
    }

    @Test
    @DisplayName("Deve verificar se vigência está ativa em data específica")
    void deveVerificarSeVigenciaEstaAtivaEmDataEspecifica() {
        // Arrange
        LocalDate inicio = LocalDate.now().minusDays(10);
        LocalDate fim = LocalDate.now().plusMonths(6);
        Vigencia vigencia = Vigencia.of(inicio, fim);

        LocalDate dentroVigencia = LocalDate.now().plusDays(15);
        LocalDate antesInicio = inicio.minusDays(1);
        LocalDate depoisFim = fim.plusDays(1);

        // Act & Assert
        assertThat(vigencia.estaVigenteEm(dentroVigencia)).isTrue();
        assertThat(vigencia.estaVigenteEm(inicio)).isTrue();
        assertThat(vigencia.estaVigenteEm(fim)).isTrue();
        assertThat(vigencia.estaVigenteEm(antesInicio)).isFalse();
        assertThat(vigencia.estaVigenteEm(depoisFim)).isFalse();
    }

    @Test
    @DisplayName("Deve retornar falso ao verificar vigência com data nula")
    void deveRetornarFalsoAoVerificarVigenciaComDataNula() {
        // Arrange
        Vigencia vigencia = Vigencia.anual(LocalDate.now());

        // Act & Assert
        assertThat(vigencia.estaVigenteEm(null)).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se vigência está ativa hoje")
    void deveVerificarSeVigenciaEstaAtivaHoje() {
        // Arrange
        LocalDate hoje = LocalDate.now();
        Vigencia vigenciaAtiva = Vigencia.of(hoje.minusDays(10), hoje.plusMonths(2));
        Vigencia vigenciaExpirada = Vigencia.of(hoje.minusDays(30), hoje.minusDays(1));

        // Act & Assert
        assertThat(vigenciaAtiva.estaVigenteHoje()).isTrue();
        assertThat(vigenciaExpirada.estaVigenteHoje()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se vigência já expirou")
    void deveVerificarSeVigenciaJaExpirou() {
        // Arrange
        LocalDate hoje = LocalDate.now();
        Vigencia expirada = Vigencia.of(hoje.minusDays(30), hoje.minusDays(1));
        Vigencia ativa = Vigencia.of(hoje.minusDays(10), hoje.plusMonths(2));

        // Act & Assert
        assertThat(expirada.jaExpirou()).isTrue();
        assertThat(ativa.jaExpirou()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se vigência ainda não começou")
    void deveVerificarSeVigenciaAindaNaoComecou() {
        // Arrange
        LocalDate hoje = LocalDate.now();
        Vigencia futura = Vigencia.of(hoje.plusDays(10), hoje.plusMonths(2));
        Vigencia ativa = Vigencia.of(hoje.minusDays(10), hoje.plusMonths(2));

        // Act & Assert
        assertThat(futura.aindaNaoComecou()).isTrue();
        assertThat(ativa.aindaNaoComecou()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar número de dias da vigência")
    void deveRetornarNumeroDiasVigencia() {
        // Arrange
        LocalDate inicio = LocalDate.now();
        LocalDate fim = LocalDate.now().plusDays(30);
        Vigencia vigencia = Vigencia.of(inicio, fim);

        // Act
        long dias = vigencia.getDiasVigencia();

        // Assert
        assertThat(dias).isEqualTo(31); // 31 dias (inclusivo)
    }

    @Test
    @DisplayName("Deve retornar número de dias restantes da vigência")
    void deveRetornarNumeroDiasRestantes() {
        // Arrange
        LocalDate hoje = LocalDate.now();
        Vigencia vigencia = Vigencia.of(hoje, hoje.plusDays(29));

        // Act
        long diasRestantes = vigencia.getDiasRestantes();

        // Assert
        assertThat(diasRestantes).isEqualTo(30);
    }

    @Test
    @DisplayName("Deve retornar zero dias restantes para vigência expirada")
    void deveRetornarZeroDiasRestantesParaVigenciaExpirada() {
        // Arrange
        LocalDate hoje = LocalDate.now();
        Vigencia expirada = Vigencia.of(hoje.minusDays(30), hoje.minusDays(1));

        // Act
        long diasRestantes = expirada.getDiasRestantes();

        // Assert
        assertThat(diasRestantes).isZero();
    }

    @Test
    @DisplayName("Deve retornar número de dias decorridos desde início")
    void deveRetornarNumeroDiasDecorridos() {
        // Arrange
        LocalDate hoje = LocalDate.now();
        Vigencia vigencia = Vigencia.of(hoje.minusDays(9), hoje.plusDays(20));

        // Act
        long diasDecorridos = vigencia.getDiasDecorridos();

        // Assert
        assertThat(diasDecorridos).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve retornar zero dias decorridos para vigência futura")
    void deveRetornarZeroDiasDecorridosParaVigenciaFutura() {
        // Arrange
        LocalDate hoje = LocalDate.now();
        Vigencia futura = Vigencia.of(hoje.plusDays(10), hoje.plusDays(40));

        // Act
        long diasDecorridos = futura.getDiasDecorridos();

        // Assert
        assertThat(diasDecorridos).isZero();
    }

    @Test
    @DisplayName("Deve retornar período da vigência")
    void deveRetornarPeriodoDaVigencia() {
        // Arrange
        LocalDate inicio = LocalDate.now();
        LocalDate fim = LocalDate.now().plusYears(1);
        Vigencia vigencia = Vigencia.of(inicio, fim);

        // Act
        var periodo = vigencia.getPeriodo();

        // Assert
        assertThat(periodo).isNotNull();
        assertThat(periodo.getYears()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Deve verificar se vence nos próximos N dias")
    void deveVerificarSeVenceNosProximosNDias() {
        // Arrange
        LocalDate hoje = LocalDate.now();
        Vigencia vigencia = Vigencia.of(hoje, hoje.plusDays(35)); // Mínimo 30 dias + 5 para o teste

        // Act & Assert
        assertThat(vigencia.venceEm(40)).isTrue(); // Vence em 35 dias, 40 é maior
        assertThat(vigencia.venceEm(35)).isTrue(); // Vence exatamente em 35 dias
        assertThat(vigencia.venceEm(30)).isFalse(); // Vence depois de 30 dias
    }

    @Test
    @DisplayName("Deve renovar vigência criando nova vigência anual")
    void deveRenovarVigenciaCriandoNovaVigenciaAnual() {
        // Arrange
        LocalDate inicio = LocalDate.now();
        LocalDate fim = LocalDate.now().plusYears(1).minusDays(1);
        Vigencia vigenciaOriginal = Vigencia.of(inicio, fim);

        // Act
        Vigencia vigenciaRenovada = vigenciaOriginal.renovar();

        // Assert
        assertThat(vigenciaRenovada.getInicio()).isEqualTo(fim.plusDays(1));
        assertThat(vigenciaRenovada.getFim()).isEqualTo(fim.plusYears(1));
    }

    @Test
    @DisplayName("Deve renovar vigência com duração específica em meses")
    void deveRenovarVigenciaComDuracaoEspecifica() {
        // Arrange
        LocalDate inicio = LocalDate.now();
        LocalDate fim = LocalDate.now().plusYears(1).minusDays(1);
        Vigencia vigenciaOriginal = Vigencia.of(inicio, fim);

        // Act
        Vigencia vigenciaRenovada = vigenciaOriginal.renovar(6);

        // Assert
        assertThat(vigenciaRenovada.getInicio()).isEqualTo(fim.plusDays(1));
        assertThat(vigenciaRenovada.getDiasVigencia()).isGreaterThanOrEqualTo(180); // Aproximadamente 6 meses
    }

    @Test
    @DisplayName("Deve retornar vigência formatada")
    void deveRetornarVigenciaFormatada() {
        // Arrange
        LocalDate inicio = LocalDate.now();
        LocalDate fim = LocalDate.now().plusMonths(6);
        Vigencia vigencia = Vigencia.of(inicio, fim);

        // Act
        String formatado = vigencia.getFormatado();

        // Assert
        assertThat(formatado).contains(inicio.toString());
        assertThat(formatado).contains(fim.toString());
        assertThat(formatado).contains("a");
    }

    @Test
    @DisplayName("Deve implementar equals corretamente")
    void deveImplementarEqualsCorretamente() {
        // Arrange
        LocalDate inicio = LocalDate.now();
        LocalDate fim = LocalDate.now().plusMonths(6);
        Vigencia v1 = Vigencia.of(inicio, fim);
        Vigencia v2 = Vigencia.of(inicio, fim);
        Vigencia v3 = Vigencia.of(inicio, fim.plusDays(1));

        // Act & Assert
        assertThat(v1).isEqualTo(v2);
        assertThat(v1).isNotEqualTo(v3);
        assertThat(v1.equals(v1)).isTrue();
        assertThat(v1.equals(null)).isFalse();
        assertThat(v1.equals("string")).isFalse();
    }

    @Test
    @DisplayName("Deve implementar hashCode corretamente")
    void deveImplementarHashCodeCorretamente() {
        // Arrange
        LocalDate inicio = LocalDate.now();
        LocalDate fim = LocalDate.now().plusMonths(6);
        Vigencia v1 = Vigencia.of(inicio, fim);
        Vigencia v2 = Vigencia.of(inicio, fim);

        // Act & Assert
        assertThat(v1.hashCode()).isEqualTo(v2.hashCode());
    }

    @Test
    @DisplayName("Deve implementar toString corretamente")
    void deveImplementarToStringCorretamente() {
        // Arrange
        LocalDate inicio = LocalDate.now();
        LocalDate fim = LocalDate.now().plusMonths(6);
        Vigencia vigencia = Vigencia.of(inicio, fim);

        // Act
        String toString = vigencia.toString();

        // Assert
        assertThat(toString).isEqualTo(vigencia.getFormatado());
    }

    @Test
    @DisplayName("Deve aceitar vigência com início até 30 dias no passado")
    void deveAceitarVigenciaComInicioAte30DiasNoPassado() {
        // Arrange
        LocalDate inicio = LocalDate.now().minusDays(30);
        LocalDate fim = inicio.plusMonths(12);

        // Act & Assert
        assertThatNoException().isThrownBy(() -> Vigencia.of(inicio, fim));
    }
}
