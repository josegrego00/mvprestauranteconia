package com.mvprestaurante.mvp.controllers;

import static com.mvprestaurante.mvp.testdata.DataProviderEmpresa.*;
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

import com.mvprestaurante.mvp.DTO.EmpresaDTO;
import com.mvprestaurante.mvp.services.EmpresaService;

@ExtendWith(MockitoExtension.class)
class EmpresaControllerTest {

    @Mock
    private EmpresaService empresaService;

    @InjectMocks
    private EmpresaController empresaController;

    @Nested
    @DisplayName("GET /api/v1/empresas")
    class ListarTodasTests {

        @Test
        @DisplayName("debe retornar 200 OK con lista de empresas")
        void debeRetornar200OkConListaDeEmpresas() {
            List<EmpresaDTO> empresas = listaEmpresasDTO();
            when(empresaService.listarTodas()).thenReturn(empresas);

            ResponseEntity<List<EmpresaDTO>> response = empresaController.listarTodas();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
        }

        @Test
        @DisplayName("debe retornar 200 OK con lista vacia")
        void debeRetornar200OkConListaVacia() {
            when(empresaService.listarTodas()).thenReturn(List.of());

            ResponseEntity<List<EmpresaDTO>> response = empresaController.listarTodas();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/empresas/{id}")
    class BuscarPorIdTests {

        @Test
        @DisplayName("debe retornar 200 OK cuando empresa existe")
        void debeRetornar200OkCuandoEmpresaExiste() {
            EmpresaDTO empresaDTO = unEmpresaDTOConId();
            when(empresaService.buscarPorId(1L)).thenReturn(empresaDTO);

            ResponseEntity<EmpresaDTO> response = empresaController.buscarPorId(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1L, response.getBody().getId());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/empresas/subdominio/{subdominio}")
    class BuscarPorSubdominioTests {

        @Test
        @DisplayName("debe retornar 200 OK cuando subdominio existe")
        void debeRetornar200OkCuandoSubdominioExiste() {
            EmpresaDTO empresaDTO = unEmpresaDTOConId();
            when(empresaService.buscarPorSubdominio("testempresa")).thenReturn(empresaDTO);

            ResponseEntity<EmpresaDTO> response = empresaController.buscarPorSubdominio("testempresa");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/empresas")
    class GuardarTests {

        @Test
        @DisplayName("debe retornar 201 Created cuando empresa se guarda exitosamente")
        void debeRetornar201CreatedCuandoEmpresaSeGuardaExitosamente() {
            EmpresaDTO empresaDTO = unEmpresaDTO();
            EmpresaDTO empresaGuardada = unEmpresaDTOConId();
            when(empresaService.guardar(any(EmpresaDTO.class))).thenReturn(empresaGuardada);

            ResponseEntity<EmpresaDTO> response = empresaController.guardar(empresaDTO);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/empresas/{id}")
    class ActualizarTests {

        @Test
        @DisplayName("debe retornar 200 OK cuando empresa se actualiza exitosamente")
        void debeRetornar200OkCuandoEmpresaSeActualizaExitosamente() {
            EmpresaDTO empresaDTO = unEmpresaDTOConId();
            when(empresaService.actualizar(1L, empresaDTO)).thenReturn(empresaDTO);

            ResponseEntity<EmpresaDTO> response = empresaController.actualizar(1L, empresaDTO);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/empresas/{id}")
    class EliminarTests {

        @Test
        @DisplayName("debe retornar 204 No Content cuando empresa se elimina exitosamente")
        void debeRetornar204NoContentCuandoEmpresaSeEliminaExitosamente() {
            when(empresaService.eliminar(1L)).thenReturn(unEmpresaDTOConId());

            ResponseEntity<Void> response = empresaController.eliminar(1L);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/empresas/{id}/activar")
    class ActivarTests {

        @Test
        @DisplayName("debe retornar 200 OK cuando empresa se activa exitosamente")
        void debeRetornar200OkCuandoEmpresaSeActivaExitosamente() {
            EmpresaDTO empresaDTO = unEmpresaDTOConId();
            empresaDTO.setActiva(true);
            when(empresaService.actualizarEstadoActivo(1L, true)).thenReturn(empresaDTO);

            ResponseEntity<EmpresaDTO> response = empresaController.activar(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().getActiva());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/empresas/{id}/desactivar")
    class DesactivarTests {

        @Test
        @DisplayName("debe retornar 200 OK cuando empresa se desactiva exitosamente")
        void debeRetornar200OkCuandoEmpresaSeDesactivaExitosamente() {
            EmpresaDTO empresaDTO = unEmpresaDTOConId();
            empresaDTO.setActiva(false);
            when(empresaService.actualizarEstadoActivo(1L, false)).thenReturn(empresaDTO);

            ResponseEntity<EmpresaDTO> response = empresaController.desactivar(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().getActiva());
        }
    }
}