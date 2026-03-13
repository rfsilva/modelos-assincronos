package com.seguradora.hibrida.domain.segurado.controller;

import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoListView;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoQueryModel;
import com.seguradora.hibrida.domain.segurado.query.service.SeguradoQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link SeguradoQueryController}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SeguradoQueryController - Testes Unitários")
class SeguradoQueryControllerTest {

    @Mock
    private SeguradoQueryService queryService;

    @InjectMocks
    private SeguradoQueryController controller;

    @Test
    @DisplayName("Deve buscar segurado por ID com sucesso")
    void shouldFindByIdSuccessfully() {
        // Given
        String id = "SEG-001";
        SeguradoQueryModel segurado = SeguradoQueryModel.builder()
                .id(id)
                .nome("João Silva")
                .build();

        when(queryService.findById(id)).thenReturn(Optional.of(segurado));

        // When
        ResponseEntity<SeguradoQueryModel> response = controller.buscarPorId(id);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(id);
        verify(queryService).findById(id);
    }

    @Test
    @DisplayName("Deve retornar 404 quando segurado não encontrado por ID")
    void shouldReturn404WhenSeguradoNotFoundById() {
        // Given
        String id = "SEG-999";
        when(queryService.findById(id)).thenReturn(Optional.empty());

        // When
        ResponseEntity<SeguradoQueryModel> response = controller.buscarPorId(id);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(queryService).findById(id);
    }

    @Test
    @DisplayName("Deve buscar segurado por CPF com sucesso")
    void shouldFindByCpfSuccessfully() {
        // Given
        String cpf = "12345678909";
        SeguradoQueryModel segurado = SeguradoQueryModel.builder()
                .cpf(cpf)
                .nome("João Silva")
                .build();

        when(queryService.findByCpf(cpf)).thenReturn(Optional.of(segurado));

        // When
        ResponseEntity<SeguradoQueryModel> response = controller.buscarPorCpf(cpf);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(queryService).findByCpf(cpf);
    }

    @Test
    @DisplayName("Deve buscar segurado por email com sucesso")
    void shouldFindByEmailSuccessfully() {
        // Given
        String email = "joao@example.com";
        SeguradoQueryModel segurado = SeguradoQueryModel.builder()
                .email(email)
                .nome("João Silva")
                .build();

        when(queryService.findByEmail(email)).thenReturn(Optional.of(segurado));

        // When
        ResponseEntity<SeguradoQueryModel> response = controller.buscarPorEmail(email);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(queryService).findByEmail(email);
    }

    @Test
    @DisplayName("Deve listar todos os segurados com paginação")
    void shouldListAllSeguradosWithPagination() {
        // Given
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoListView> page = new PageImpl<>(List.of(new SeguradoListView()));

        when(queryService.findAll(pageable)).thenReturn(page);

        // When
        ResponseEntity<Page<SeguradoListView>> response = controller.listarTodos(pageable);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isNotEmpty();
        verify(queryService).findAll(pageable);
    }

    @Test
    @DisplayName("Deve listar segurados por status")
    void shouldListByStatus() {
        // Given
        StatusSegurado status = StatusSegurado.ATIVO;
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoListView> page = new PageImpl<>(List.of(new SeguradoListView()));

        when(queryService.findByStatus(status, pageable)).thenReturn(page);

        // When
        ResponseEntity<Page<SeguradoListView>> response = controller.listarPorStatus(status, pageable);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(queryService).findByStatus(status, pageable);
    }

    @Test
    @DisplayName("Deve buscar segurados por nome")
    void shouldFindByNome() {
        // Given
        String nome = "João";
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoListView> page = new PageImpl<>(List.of(new SeguradoListView()));

        when(queryService.findByNome(nome, pageable)).thenReturn(page);

        // When
        ResponseEntity<Page<SeguradoListView>> response = controller.buscarPorNome(nome, pageable);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(queryService).findByNome(nome, pageable);
    }

    @Test
    @DisplayName("Deve buscar segurados por cidade")
    void shouldFindByCidade() {
        // Given
        String cidade = "São Paulo";
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoListView> page = new PageImpl<>(List.of(new SeguradoListView()));

        when(queryService.findByCidade(cidade, pageable)).thenReturn(page);

        // When
        ResponseEntity<Page<SeguradoListView>> response = controller.buscarPorCidade(cidade, pageable);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(queryService).findByCidade(cidade, pageable);
    }

    @Test
    @DisplayName("Deve buscar segurados por estado")
    void shouldFindByEstado() {
        // Given
        String estado = "SP";
        PageRequest pageable = PageRequest.of(0, 20);
        Page<SeguradoListView> page = new PageImpl<>(List.of(new SeguradoListView()));

        when(queryService.findByEstado(estado, pageable)).thenReturn(page);

        // When
        ResponseEntity<Page<SeguradoListView>> response = controller.buscarPorEstado(estado, pageable);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(queryService).findByEstado(estado, pageable);
    }

    @Test
    @DisplayName("Deve verificar se CPF existe")
    void shouldCheckIfCpfExists() {
        // Given
        String cpf = "12345678909";
        when(queryService.existsByCpf(cpf)).thenReturn(true);

        // When
        ResponseEntity<Boolean> response = controller.existePorCpf(cpf);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
        verify(queryService).existsByCpf(cpf);
    }

    @Test
    @DisplayName("Deve verificar se email existe")
    void shouldCheckIfEmailExists() {
        // Given
        String email = "joao@example.com";
        when(queryService.existsByEmail(email)).thenReturn(false);

        // When
        ResponseEntity<Boolean> response = controller.existePorEmail(email);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isFalse();
        verify(queryService).existsByEmail(email);
    }

    @Test
    @DisplayName("Deve contar segurados por status")
    void shouldCountByStatus() {
        // Given
        StatusSegurado status = StatusSegurado.ATIVO;
        when(queryService.countByStatus(status)).thenReturn(100L);

        // When
        ResponseEntity<Long> response = controller.contarPorStatus(status);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(100L);
        verify(queryService).countByStatus(status);
    }
}
