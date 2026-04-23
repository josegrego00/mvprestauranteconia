package com.mvprestaurante.mvp.controllers;

import static com.mvprestaurante.mvp.testdata.DataProviderProducto.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import com.mvprestaurante.mvp.DTO.ProductoDTO;
import com.mvprestaurante.mvp.services.ProductoService;
import com.mvprestaurante.mvp.services.RecetaService;
import com.mvprestaurante.mvp.utils.AuditLogger;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    @Mock
    private RecetaService recetaService;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private ProductoController productoController;

    @Nested
    @DisplayName("GET /productos")
    class ListarTests {

        @Test
        @DisplayName("debe retornar vista de lista con productos")
        void debeRetornarVistaDeListaConProductos() {
            List<ProductoDTO> productos = listaProductosDTO();
            Page<ProductoDTO> productoPage = new PageImpl<>(productos);

            when(productoService.listarActivos(any(Pageable.class))).thenReturn(productoPage);

            String vista = productoController.listar(0, 10, null, false, new org.springframework.ui.Model() {
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

            assertEquals("productos/lista", vista);
        }

        @Test
        @DisplayName("debe retornar vista de lista vacia")
        void debeRetornarVistaDeListaVacia() {
            Page<ProductoDTO> productoPage = new PageImpl<>(List.of());

            when(productoService.listarActivos(any(Pageable.class))).thenReturn(productoPage);

            String vista = productoController.listar(0, 10, null, false, new org.springframework.ui.Model() {
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

            assertEquals("productos/lista", vista);
        }
    }

    @Nested
    @DisplayName("GET /productos/{id}")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("debe retornar vista de producto")
        void debeRetornarVistaDeProducto() {
            ProductoDTO producto = unProductoDTOConId();
            Model model = new org.springframework.ui.Model() {
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
            };

            when(productoService.obtenerPorId(1L)).thenReturn(producto);

            String vista = productoController.ver(1L, model, null);

            assertEquals("productos/ver", vista);
        }

        @Test
        @DisplayName("debe redireccionar cuando producto no existe")
        void debeRedireccionarCuandoProductoNoExiste() {
            when(productoService.obtenerPorId(99L)).thenThrow(new com.mvprestaurante.mvp.exceptions.BusinessException("Producto no encontrado"));

            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes = mock(org.springframework.web.servlet.mvc.support.RedirectAttributes.class);
            String vista = productoController.ver(99L, null, redirectAttributes);

            assertEquals("redirect:/productos", vista);
        }
    }

    @Nested
    @DisplayName("GET /productos/estimado/{id}")
    class CalcularStockEstimadoTests {

        @Test
        @DisplayName("debe retornar stock estimado")
        void debeRetornarStockEstimado() {
            when(productoService.calcularStockEstimado(1L)).thenReturn(25.0);

            Double resultado = productoController.obtenerEstimado(1L);

            assertEquals(25.0, resultado);
        }
    }

    @Nested
    @DisplayName("API Endpoints")
    class ApiTests {

        @Test
        @DisplayName("GET /productos/api/listar")
        void debeListarProductosApi() {
            List<ProductoDTO> productos = listaProductosDTO();
            Page<ProductoDTO> productoPage = new PageImpl<>(productos);

            when(productoService.listarActivos(any(Pageable.class))).thenReturn(productoPage);

            ResponseEntity<Page<ProductoDTO>> response = productoController.listarApi(0, 10);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }

        @Test
        @DisplayName("GET /productos/api/{id}")
        void debeBuscarProductoApi() {
            ProductoDTO producto = unProductoDTOConId();

            when(productoService.obtenerPorId(1L)).thenReturn(producto);

            ResponseEntity<ProductoDTO> response = productoController.buscarPorIdApi(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Nested
    @DisplayName("POST /productos/guardar")
    class GuardarTests {

        @Test
        @DisplayName("debe guardar nuevo producto")
        void debeGuardarNuevoProducto() {
            ProductoDTO dto = unProductoDTO();
            doNothing().when(productoService).guardar(any(), any());
            when(productoService.guardar(any(), any())).thenReturn(dto);

            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes = mock(org.springframework.web.servlet.mvc.support.RedirectAttributes.class);

            String vista = productoController.guardar(dto, null, redirectAttributes);

            assertEquals("redirect:/productos", vista);
            verify(productoService).guardar(any(), any());
        }

        @Test
        @DisplayName("debe actualizar producto existente")
        void debeActualizarProductoExistente() {
            ProductoDTO dto = unProductoDTOConId();
            dto.setId(1L);

            when(productoService.actualizar(anyLong(), any(), any())).thenReturn(dto);

            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes = mock(org.springframework.web.servlet.mvc.support.RedirectAttributes.class);

            String vista = productoController.guardar(dto, null, redirectAttributes);

            assertEquals("redirect:/productos", vista);
            verify(productoService).actualizar(anyLong(), any(), any());
        }
    }

    @Nested
    @DisplayName("GET /productos/eliminar/{id}")
    class EliminarTests {

        @Test
        @DisplayName("debe eliminar producto")
        void debeEliminarProducto() {
            doNothing().when(productoService).eliminar(anyLong());

            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes = mock(org.springframework.web.servlet.mvc.support.RedirectAttributes.class);

            String vista = productoController.eliminar(1L, redirectAttributes);

            assertEquals("redirect:/productos", vista);
            verify(productoService).eliminar(1L);
        }
    }
}