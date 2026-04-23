package com.mvprestaurante.mvp.controllers;

import static com.mvprestaurante.mvp.testdata.DataProviderUsuario.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.mvprestaurante.mvp.DTO.UsuarioRequestDTO;
import com.mvprestaurante.mvp.DTO.UsuarioResponseDTO;
import com.mvprestaurante.mvp.services.UsuarioService;
import com.mvprestaurante.mvp.utils.AuditLogger;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private UsuarioController usuarioController;

    @Nested
    @DisplayName("GET /api/v1/usuarios")
    class ListarUsuariosTests {

        @Test
        @DisplayName("debe retornar 200 OK con lista de usuarios")
        void debeRetornar200OkConListaDeUsuarios() {
            List<UsuarioResponseDTO> usuarios = listaUsuariosDTO();
            when(usuarioService.listarUsuarios()).thenReturn(usuarios);

            ResponseEntity<List<UsuarioResponseDTO>> response = usuarioController.listarUsuarios();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
        }

        @Test
        @DisplayName("debe retornar 200 OK con lista vacia")
        void debeRetornar200OkConListaVacia() {
            when(usuarioService.listarUsuarios()).thenReturn(List.of());

            ResponseEntity<List<UsuarioResponseDTO>> response = usuarioController.listarUsuarios();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/usuarios/{id}")
    class BuscarPorIdTests {

        @Test
        @DisplayName("debe retornar 200 OK cuando usuario existe")
        void debeRetornar200OkCuandoUsuarioExiste() {
            UsuarioResponseDTO usuarioDTO = unUsuarioResponseDTO();
            when(usuarioService.buscarPorId(1L)).thenReturn(usuarioDTO);

            ResponseEntity<UsuarioResponseDTO> response = usuarioController.buscarPorId(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1L, response.getBody().getId());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/usuarios")
    class GuardarTests {

        @Test
        @DisplayName("debe retornar 201 Created cuando usuario se guarda exitosamente")
        void debeRetornar201CreatedCuandoUsuarioSeGuardaExitosamente() {
            UsuarioRequestDTO requestDTO = unUsuarioRequestDTO();
            UsuarioResponseDTO responseDTO = unUsuarioResponseDTO();
            when(usuarioService.guardarUsuario(any(UsuarioRequestDTO.class))).thenReturn(responseDTO);

            ResponseEntity<UsuarioResponseDTO> response = usuarioController.guardar(requestDTO);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/usuarios/{id}")
    class ActualizarTests {

        @Test
        @DisplayName("debe retornar 200 OK cuando usuario se actualiza exitosamente")
        void debeRetornar200OkCuandoUsuarioSeActualizaExitosamente() {
            UsuarioRequestDTO requestDTO = unUsuarioRequestDTOConId();
            UsuarioResponseDTO responseDTO = unUsuarioResponseDTO();
            when(usuarioService.actualizarUsuario(1L, requestDTO)).thenReturn(responseDTO);

            ResponseEntity<UsuarioResponseDTO> response = usuarioController.actualizar(1L, requestDTO);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/usuarios/{id}")
    class EliminarTests {

        @Test
        @DisplayName("debe retornar 204 No Content cuando usuario se elimina exitosamente")
        void debeRetornar204NoContentCuandoUsuarioSeEliminaExitosamente() {
            doNothing().when(usuarioService).eliminarUsuario(1L);

            ResponseEntity<Void> response = usuarioController.eliminar(1L);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/usuarios/{id}/activar")
    class ActivarTests {

        @Test
        @DisplayName("debe retornar 200 OK cuando usuario se activa exitosamente")
        void debeRetornar200OkCuandoUsuarioSeActivaExitosamente() {
            UsuarioResponseDTO responseDTO = unUsuarioResponseDTO();
            when(usuarioService.activarUsuario(1L)).thenReturn(responseDTO);

            ResponseEntity<UsuarioResponseDTO> response = usuarioController.activar(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }
}