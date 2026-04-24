package com.mvprestaurante.mvp.controllers;

import static com.mvprestaurante.mvp.testdata.DataProviderDetalleReceta.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mvprestaurante.mvp.DTO.DetalleRecetaDTO;
import com.mvprestaurante.mvp.DTO.IngredienteDTO;
import com.mvprestaurante.mvp.services.DetalleRecetaService;
import com.mvprestaurante.mvp.services.IngredienteService;
import com.mvprestaurante.mvp.services.RecetaService;
import com.mvprestaurante.mvp.utils.AuditLogger;

@ExtendWith(MockitoExtension.class)
class DetalleRecetaControllerTest {

    @Mock
    private DetalleRecetaService detalleRecetaService;

    @Mock
    private RecetaService recetaService;

    @Mock
    private IngredienteService ingredienteService;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private DetalleRecetaController detalleRecetaController;

    private static final Long RECETA_ID = 1L;
    private static final Long DETALLE_ID = 1L;

    @Nested
    @DisplayName("GET /recetas/{recetaId}/ingredientes")
    class ListarTests {

        @Test
        @DisplayName("debe retornar vista de lista con ingredientes")
        void debeRetornarVistaDeLista() {
            List<DetalleRecetaDTO> detalles = listaDetallesRecetaDTO();
            Page<DetalleRecetaDTO> detallePage = new PageImpl<>(detalles);

            when(detalleRecetaService.listarPorRecetaId(anyLong(), any(Pageable.class)))
                    .thenReturn(detallePage);

            String vista = detalleRecetaController.listar(RECETA_ID, 0, 10, 
                    crearMockModel(), crearMockRedirectAttributes());

            assertEquals("recetas/ingredientes/lista", vista);
            verify(detalleRecetaService).listarPorRecetaId(eq(RECETA_ID), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /recetas/{recetaId}/ingredientes/nuevo")
    class NuevoTests {

        @Test
        @DisplayName("debe retornar formulario nuevo")
        void debeRetornarFormularioNuevo() {
            List<IngredienteDTO> ingredientes = List.of(
                    IngredienteDTO.builder()
                            .id(1L)
                            .nombre("Carne molida")
                            .build()
            );
            Page<IngredienteDTO> ingredientePage = new PageImpl<>(ingredientes);

            when(recetaService.obtenerPorId(anyLong())).thenReturn(null);
            when(ingredienteService.listarActivos(any(Pageable.class))).thenReturn(ingredientePage);

            String vista = detalleRecetaController.nuevo(RECETA_ID, 
                    crearMockModel(), crearMockRedirectAttributes());

            assertEquals("recetas/ingredientes/formulario", vista);
        }
    }

    @Nested
    @DisplayName("POST /recetas/{recetaId}/ingredientes/guardar")
    class GuardarTests {

        @Test
        @DisplayName("debe guardar ingrediente en receta")
        void debeGuardarIngrediente() {
            DetalleRecetaDTO dto = unDetalleRecetaDTO();
            RedirectAttributes redirectAttributes = crearMockRedirectAttributes();

            when(detalleRecetaService.guardar(any(DetalleRecetaDTO.class)))
                    .thenReturn(unDetalleRecetaDTOConId());

            String vista = detalleRecetaController.guardar(RECETA_ID, dto, redirectAttributes);

            assertEquals("redirect:/recetas/" + RECETA_ID + "/ingredientes", vista);
            verify(detalleRecetaService).guardar(any(DetalleRecetaDTO.class));
        }
    }

    @Nested
    @DisplayName("GET /recetas/{recetaId}/ingredientes/editar/{id}")
    class EditarTests {

        @Test
        @DisplayName("debe retornar formulario editar")
        void debeRetornarFormularioEditar() {
            DetalleRecetaDTO detalleDTO = unDetalleRecetaDTOConId();
            List<IngredienteDTO> ingredientes = List.of(
                    IngredienteDTO.builder()
                            .id(1L)
                            .nombre("Carne molida")
                            .build()
            );
            Page<IngredienteDTO> ingredientePage = new PageImpl<>(ingredientes);

            when(detalleRecetaService.obtenerPorId(anyLong())).thenReturn(detalleDTO);
            when(ingredienteService.listarActivos(any(Pageable.class))).thenReturn(ingredientePage);

            String vista = detalleRecetaController.editar(RECETA_ID, DETALLE_ID, 
                    crearMockModel(), crearMockRedirectAttributes());

            assertEquals("recetas/ingredientes/formulario", vista);
        }
    }

    @Nested
    @DisplayName("GET /recetas/{recetaId}/ingredientes/eliminar/{id}")
    class EliminarTests {

        @Test
        @DisplayName("debe eliminar ingrediente de receta")
        void debeEliminarIngrediente() {
            RedirectAttributes redirectAttributes = crearMockRedirectAttributes();

            when(detalleRecetaService.eliminar(anyLong())).thenReturn(true);

            String vista = detalleRecetaController.eliminar(RECETA_ID, DETALLE_ID, redirectAttributes);

            assertEquals("redirect:/recetas/" + RECETA_ID + "/ingredientes", vista);
            verify(detalleRecetaService).eliminar(DETALLE_ID);
        }
    }

    @Nested
    @DisplayName("API Endpoints")
    class ApiTests {

        @Test
        @DisplayName("API: debe listar ingredientes via API")
        void debeListarViaApi() {
            List<DetalleRecetaDTO> detalles = listaDetallesRecetaDTO();
            Page<DetalleRecetaDTO> detallePage = new PageImpl<>(detalles);

            when(detalleRecetaService.listarPorRecetaId(anyLong(), any(Pageable.class)))
                    .thenReturn(detallePage);

            ResponseEntity<Page<DetalleRecetaDTO>> respuesta = 
                    detalleRecetaController.listarApi(RECETA_ID, 0, 10);

            assertEquals(HttpStatus.OK, respuesta.getStatusCode());
            verify(detalleRecetaService).listarPorRecetaId(eq(RECETA_ID), any(Pageable.class));
        }

        @Test
        @DisplayName("API: debe buscar detalle por ID via API")
        void debeBuscarPorIdViaApi() {
            DetalleRecetaDTO detalleDTO = unDetalleRecetaDTOConId();

            when(detalleRecetaService.obtenerPorId(anyLong())).thenReturn(detalleDTO);

            ResponseEntity<DetalleRecetaDTO> respuesta = 
                    detalleRecetaController.buscarPorIdApi(RECETA_ID, DETALLE_ID);

            assertEquals(HttpStatus.OK, respuesta.getStatusCode());
            assertNotNull(respuesta.getBody());
        }

        @Test
        @DisplayName("API: debe guardar detalle via API")
        void debeGuardarViaApi() {
            DetalleRecetaDTO dto = unDetalleRecetaDTO();
            DetalleRecetaDTO detalleDTO = unDetalleRecetaDTOConId();

            when(detalleRecetaService.guardar(any(DetalleRecetaDTO.class))).thenReturn(detalleDTO);

            ResponseEntity<DetalleRecetaDTO> respuesta = 
                    detalleRecetaController.guardarApi(RECETA_ID, dto);

            assertEquals(HttpStatus.OK, respuesta.getStatusCode());
            verify(detalleRecetaService).guardar(any(DetalleRecetaDTO.class));
        }

        @Test
        @DisplayName("API: debe actualizar detalle via API")
        void debeActualizarViaApi() {
            DetalleRecetaDTO dto = unDetalleRecetaDTO();
            DetalleRecetaDTO detalleDTO = unDetalleRecetaDTOConId();

            when(detalleRecetaService.actualizar(anyLong(), any(DetalleRecetaDTO.class)))
                    .thenReturn(detalleDTO);

            ResponseEntity<DetalleRecetaDTO> respuesta = 
                    detalleRecetaController.actualizarApi(RECETA_ID, DETALLE_ID, dto);

            assertEquals(HttpStatus.OK, respuesta.getStatusCode());
            verify(detalleRecetaService).actualizar(eq(DETALLE_ID), any(DetalleRecetaDTO.class));
        }

        @Test
        @DisplayName("API: debe eliminar detalle via API")
        void debeEliminarViaApi() {
            when(detalleRecetaService.eliminar(anyLong())).thenReturn(true);

            ResponseEntity<Void> respuesta = 
                    detalleRecetaController.eliminarApi(RECETA_ID, DETALLE_ID);

            assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());
            verify(detalleRecetaService).eliminar(DETALLE_ID);
        }
    }

    private Model crearMockModel() {
        return new org.springframework.ui.Model() {
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

            @Override
            public org.springframework.ui.Model addAllAttributes(java.util.Collection<?> attributeValues) {
                return this;
            }

            @Override
            public org.springframework.ui.Model addAllAttributes(java.util.Map<String, ?> attributes) {
                return this;
            }

            @Override
            public boolean containsAttribute(String attributeName) {
                return false;
            }

            @Override
            public java.util.Map<String, Object> getAttributes() {
                return java.util.Collections.emptyMap();
            }
        };
    }

    private RedirectAttributes crearMockRedirectAttributes() {
        return new RedirectAttributes() {
            @Override
            public RedirectAttributes addFlashAttribute(String attributeName, Object attributeValue) {
                return this;
            }

            @Override
            public RedirectAttributes addFlashAttribute(Object attributeValue) {
                return this;
            }

            @Override
            public RedirectAttributes addAllFlashAttributes(java.util.Map<String, ?> attributes) {
                return this;
            }

            @Override
            public java.util.Map<String, ?> getFlashAttributes() {
                return java.util.Collections.emptyMap();
            }

            @Override
            public RedirectAttributes addAttribute(String attributeName, Object attributeValue) {
                return this;
            }

            @Override
            public RedirectAttributes addAttribute(Object attributeValue) {
                return this;
            }

            @Override
            public RedirectAttributes addAllAttributes(java.util.Collection<?> attributeValues) {
                return this;
            }

            @Override
            public RedirectAttributes addAllAttributes(java.util.Map<String, ?> attributes) {
                return this;
            }

            @Override
            public RedirectAttributes mergeAttributes(java.util.Map<String, ?> attributes) {
                return this;
            }

            @Override
            public RedirectAttributes removeAttribute(String attributeName) {
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
        };
    }
}