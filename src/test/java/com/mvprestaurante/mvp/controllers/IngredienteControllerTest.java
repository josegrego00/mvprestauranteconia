package com.mvprestaurante.mvp.controllers;

import static com.mvprestaurante.mvp.testdata.DataProviderIngrediente.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import com.mvprestaurante.mvp.DTO.IngredienteDTO;
import com.mvprestaurante.mvp.services.IngredienteService;
import com.mvprestaurante.mvp.utils.AuditLogger;

@ExtendWith(MockitoExtension.class)
class IngredienteControllerTest {

    @Mock
    private IngredienteService ingredienteService;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private IngredienteController ingredienteController;

    @Nested
    @DisplayName("GET /ingredientes")
    class ListarTests {

        @Test
        @DisplayName("debe retornar vista de lista con ingredientes")
        void debeRetornarVistaDeListaConIngredientes() {
            List<IngredienteDTO> ingredientes = listaIngredientesDTO();
            Page<IngredienteDTO> ingredientePage = new PageImpl<>(ingredientes);

            when(ingredienteService.listarActivos(any(Pageable.class))).thenReturn(ingredientePage);

            String vista = ingredienteController.listar(0, 10, null, new org.springframework.ui.Model() {
                @Override
                public org.springframework.ui.Model addAttribute(String attributeName, Object attributeValue) {
                    return this;
                }

                @Override
                public org.springframework.ui.Model addAttribute(Object attributeValue) {
                    return this;
                }

                @Override
                public org.springframework.ui.Model mergeAttributes(java.util.Map<String, ?> attributes) {
                    return this;
                }

                @Override
                public org.springframework.ui.Model removeAttribute(String attributeName) {
                    return this;
                }

                @Override
                public boolean hasAttributes() {
                    return false;
                }

                @Override
                public java.util.Map<String, Object> asMap() {
                    return java.util.Collections.emptyMap();
                }
            });

            assertEquals("ingredientes/lista", vista);
        }
    }

    @Nested
    @DisplayName("GET /ingredientes/{id}")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("debe retornar vista de ingrediente")
        void debeRetornarVistaDeIngrediente() {
            IngredienteDTO ingrediente = unIngredienteDTOConId();

            when(ingredienteService.obtenerPorId(1L)).thenReturn(Optional.of(ingrediente));

            String vista = ingredienteController.ver(1L, new org.springframework.ui.Model() {
                @Override
                public org.springframework.ui.Model addAttribute(String attributeName, Object attributeValue) {
                    return this;
                }

                @Override
                public org.springframework.ui.Model addAttribute(Object attributeValue) {
                    return this;
                }

                @Override
                public org.springframework.ui.Model mergeAttributes(java.util.Map<String, ?> attributes) {
                    return this;
                }

                @Override
                public org.springframework.ui.Model removeAttribute(String attributeName) {
                    return this;
                }

                @Override
                public boolean hasAttributes() {
                    return false;
                }

                @Override
                public java.util.Map<String, Object> asMap() {
                    return java.util.Collections.emptyMap();
                }
            }, null);

            assertEquals("ingredientes/ver", vista);
        }

        @Test
        @DisplayName("debe redireccionar cuando ingrediente no existe")
        void debeRedireccionarCuandoIngredienteNoExiste() {
            when(ingredienteService.obtenerPorId(99L)).thenReturn(Optional.empty());

            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes = mock(org.springframework.web.servlet.mvc.support.RedirectAttributes.class);
            String vista = ingredienteController.ver(99L, null, redirectAttributes);

            assertEquals("redirect:/ingredientes", vista);
        }
    }

    @Nested
    @DisplayName("API Endpoints")
    class ApiTests {

        @Test
        @DisplayName("GET /ingredientes/api/listar")
        void debeListarIngredientesApi() {
            List<IngredienteDTO> ingredientes = listaIngredientesDTO();
            Page<IngredienteDTO> ingredientePage = new PageImpl<>(ingredientes);

            when(ingredienteService.listarActivos(any(Pageable.class))).thenReturn(ingredientePage);

            ResponseEntity<Page<IngredienteDTO>> response = ingredienteController.listarApi(0, 10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }

        @Test
        @DisplayName("GET /ingredientes/api/{id}")
        void debeBuscarIngredienteApi() {
            IngredienteDTO ingrediente = unIngredienteDTOConId();

            when(ingredienteService.obtenerPorId(1L)).thenReturn(Optional.of(ingrediente));

            ResponseEntity<IngredienteDTO> response = ingredienteController.buscarPorIdApi(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }

        @Test
        @DisplayName("GET /ingredientes/api/{id} Not Found")
        void debeRetornarNotFoundCuandoNoExiste() {
            when(ingredienteService.obtenerPorId(99L)).thenReturn(Optional.empty());

            ResponseEntity<IngredienteDTO> response = ingredienteController.buscarPorIdApi(99L);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("POST /ingredientes/guardar")
    class GuardarTests {

        @Test
        @DisplayName("debe guardar nuevo ingrediente")
        void debeGuardarNuevoIngrediente() {
            IngredienteDTO dto = unIngredienteDTO();
            doNothing().when(ingredienteService).guardar(any());

            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes = mock(org.springframework.web.servlet.mvc.support.RedirectAttributes.class);

            String vista = ingredienteController.guardar(dto, null, new org.springframework.ui.Model() {
                @Override
                public org.springframework.ui.Model addAttribute(String attributeName, Object attributeValue) {
                    return this;
                }

                @Override
                public org.springframework.ui.Model addAttribute(Object attributeValue) {
                    return this;
                }

                @Override
                public org.springframework.ui.Model mergeAttributes(java.util.Map<String, ?> attributes) {
                    return this;
                }

                @Override
                public org.springframework.ui.Model removeAttribute(String attributeName) {
                    return this;
                }

                @Override
                public boolean hasAttributes() {
                    return false;
                }

                @Override
                public java.util.Map<String, Object> asMap() {
                    return java.util.Collections.emptyMap();
                }
            }, redirectAttributes);

            assertEquals("redirect:/ingredientes", vista);
            verify(ingredienteService).guardar(any());
        }
    }

    @Nested
    @DisplayName("GET /ingredientes/eliminar/{id}")
    class EliminarTests {

        @Test
        @DisplayName("debe eliminar ingrediente")
        void debeEliminarIngrediente() {
            doNothing().when(ingredienteService).eliminar(anyLong());

            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes = mock(org.springframework.web.servlet.mvc.support.RedirectAttributes.class);

            String vista = ingredienteController.eliminar(1L, redirectAttributes);

            assertEquals("redirect:/ingredientes", vista);
            verify(ingredienteService).eliminar(1L);
        }
    }
}