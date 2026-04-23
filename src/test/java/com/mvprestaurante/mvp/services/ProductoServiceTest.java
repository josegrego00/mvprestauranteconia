package com.mvprestaurante.mvp.services;

import static com.mvprestaurante.mvp.testdata.DataProviderProducto.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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

import com.mvprestaurante.mvp.DTO.ProductoDTO;
import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.mapper.ProductoMapper;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Producto;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;
import com.mvprestaurante.mvp.repositories.ProductoRepository;
import com.mvprestaurante.mvp.repositories.RecetaRepository;
import com.mvprestaurante.mvp.utils.AuditLogger;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProductoMapper productoMapper;

    @Mock
    private EmpresaRepositorio empresaRepositorio;

    @Mock
    private RecetaRepository recetaRepository;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private ProductoService productoService;

    private static final Long EMPRESA_ID = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(EMPRESA_ID);
    }

    @Nested
    @DisplayName("listarActivos")
    class ListarActivosTests {

        @Test
        @DisplayName("debe retornar lista de productos activos")
        void debeRetornarListaDeProductosActivos() {
            List<Producto> productos = listaProductos();
            List<ProductoDTO> productosDTO = listaProductosDTO();
            Page<Producto> productoPage = new PageImpl<>(productos);
            Page<ProductoDTO> productoDTOPage = new PageImpl<>(productosDTO);

            when(productoRepository.findByEstaActivoTrue(anyLong(), any(Pageable.class))).thenReturn(productoPage);
            when(productoMapper.toDTO(any(Producto.class))).thenReturn(productosDTO.get(0));

            Page<ProductoDTO> resultado = productoService.listarActivos(Pageable.ofSize(10));

            assertNotNull(resultado);
            assertEquals(2, resultado.getContent().size());
            verify(productoRepository).findByEstaActivoTrue(anyLong(), any(Pageable.class));
            verify(auditLogger).logListar("Producto", 2);
        }

        @Test
        @DisplayName("debe retornar lista vacia cuando no hay productos")
        void debeRetornarListaVaciaCuandoNoHayProductos() {
            Page<Producto> productoPage = new PageImpl<>(List.of());

            when(productoRepository.findByEstaActivoTrue(anyLong(), any(Pageable.class))).thenReturn(productoPage);

            Page<ProductoDTO> resultado = productoService.listarActivos(Pageable.ofSize(10));

            assertTrue(resultado.getContent().isEmpty());
            verify(auditLogger).logListar("Producto", 0);
        }
    }

    @Nested
    @DisplayName("buscarPorNombre")
    class BuscarPorNombreTests {

        @Test
        @DisplayName("debe buscar productos por nombre")
        void debeBuscarProductosPorNombre() {
            List<Producto> productos = listaProductos();
            Page<Producto> productoPage = new PageImpl<>(productos);

            when(productoRepository.findByNombreContainingIgnoreCaseAndEstaActivoTrue(anyLong(), anyString(), any(Pageable.class)))
                    .thenReturn(productoPage);
            when(productoMapper.toDTO(any(Producto.class))).thenReturn(unProductoDTO());

            Page<ProductoDTO> resultado = productoService.buscarPorNombre("pizza", Pageable.ofSize(10));

            assertNotNull(resultado);
            verify(productoRepository).findByNombreContainingIgnoreCaseAndEstaActivoTrue(anyLong(), anyString(), any(Pageable.class));
            verify(auditLogger).logBuscar("Producto", "Nombre: pizza");
        }
    }

    @Nested
    @DisplayName("obtenerPorId")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("debe retornar producto cuando existe")
        void debeRetornarProductoCuandoExiste() {
            Producto producto = unProductoConId();
            ProductoDTO productoDTO = unProductoDTOConId();

            when(productoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(producto));
            when(productoMapper.toDTO(producto)).thenReturn(productoDTO);

            ProductoDTO resultado = productoService.obtenerPorId(1L);

            assertNotNull(resultado);
            assertEquals("Hamburguesa clásica", resultado.getNombre());
            verify(productoRepository).findByIdAndEmpresaId(1L, EMPRESA_ID);
            verify(auditLogger).logBuscar("Producto", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando producto no existe")
        void debeLanzarExcepcionCuandoProductoNoExiste() {
            when(productoRepository.findByIdAndEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> productoService.obtenerPorId(99L)
            );

            assertEquals("Producto no encontrado", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("guardar")
    class GuardarTests {

        @Test
        @DisplayName("debe guardar producto exitosamente")
        void debeGuardarProductoExitosamente() {
            ProductoDTO dto = unProductoDTO();
            Producto producto = unProducto();
            Empresa empresa = Empresa.builder().id(EMPRESA_ID).build();

            when(productoRepository.existsByNombreIgnoreCaseAndEmpresaIdAndEstaActivoTrue(anyString(), anyLong())).thenReturn(false);
            when(empresaRepositorio.findById(EMPRESA_ID)).thenReturn(Optional.of(empresa));
            when(productoMapper.toEntity(dto)).thenReturn(producto);
            when(productoRepository.save(any(Producto.class))).thenReturn(producto);
            when(productoMapper.toDTO(producto)).thenReturn(dto);

            ProductoDTO resultado = productoService.guardar(dto, null);

            assertNotNull(resultado);
            verify(productoRepository).save(any(Producto.class));
            verify(auditLogger).logCrear("Producto", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando nombre ya existe")
        void debeLanzarExcepcionCuandoNombreYaExiste() {
            ProductoDTO dto = unProductoDTO();

            when(productoRepository.existsByNombreIgnoreCaseAndEmpresaIdAndEstaActivoTrue(anyString(), anyLong())).thenReturn(true);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> productoService.guardar(dto, null)
            );

            assertEquals("Ya existe un producto con ese nombre", exception.getMessage());
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando nombre es vacio")
        void debeLanzarExcepcionCuandoNombreEsVacio() {
            ProductoDTO dto = unProductoDTO();
            dto.setNombre("");

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> productoService.guardar(dto, null)
            );

            assertEquals("El nombre del producto es obligatorio", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("actualizar")
    class ActualizarTests {

        @Test
        @DisplayName("debe actualizar producto exitosamente")
        void debeActualizarProductoExitosamente() {
            Producto productoExistente = unProductoConId();
            ProductoDTO dto = unProductoDTOConId();
            Empresa empresa = Empresa.builder().id(EMPRESA_ID).build();

            when(productoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(productoExistente));
            when(productoRepository.existsByNombreIgnoreCaseAndEmpresaIdAndEstaActivoTrue(anyString(), anyLong())).thenReturn(false);
            when(productoRepository.save(any(Producto.class))).thenReturn(productoExistente);
            when(productoMapper.toDTO(productoExistente)).thenReturn(dto);

            ProductoDTO resultado = productoService.actualizar(1L, dto, null);

            assertNotNull(resultado);
            verify(productoRepository).save(any(Producto.class));
            verify(auditLogger).logActualizar("Producto", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando producto no existe")
        void debeLanzarExcepcionCuandoProductoNoExiste() {
            ProductoDTO dto = unProductoDTOConId();

            when(productoRepository.findByIdAndEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> productoService.actualizar(99L, dto, null)
            );

            assertEquals("Producto no encontrado", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("eliminar")
    class EliminarTests {

        @Test
        @DisplayName("debe eliminar producto cambiando estaActivo a false")
        void debeEliminarProductoCambiandoEstaActivoAFalse() {
            Producto producto = unProductoConId();

            when(productoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(producto));
            when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

            productoService.eliminar(1L);

            verify(productoRepository).save(argThat(p -> p.getEstaActivo() == false));
            verify(auditLogger).logDesactivar("Producto", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando producto no existe")
        void debeLanzarExcepcionCuandoProductoNoExiste() {
            when(productoRepository.findByIdAndEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> productoService.eliminar(99L)
            );

            assertEquals("Producto no encontrado", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("listarProductosConReceta")
    class ListarProductosConRecetaTests {

        @Test
        @DisplayName("debe retornar productos con receta")
        void debeRetornarProductosConReceta() {
            List<Producto> productos = listaProductos();
            Page<Producto> productoPage = new PageImpl<>(productos);

            when(productoRepository.findByTieneRecetaTrueAndEstaActivoTrue(anyLong(), any(Pageable.class)))
                    .thenReturn(productoPage);

            Page<ProductoDTO> resultado = productoService.listarProductosConReceta(Pageable.ofSize(10));

            assertNotNull(resultado);
            verify(auditLogger).logListar("Producto", productos.size());
        }
    }

    @Nested
    @DisplayName("listarSinProducto")
    class ListarSinProductoTests {

        @Test
        @DisplayName("debe retornar productos sin receta")
        void debeRetornarProductosSinReceta() {
            List<Producto> productos = listaProductos();
            Page<Producto> productoPage = new PageImpl<>(productos);

            when(productoRepository.findByTieneRecetaFalseAndEstaActivoTrue(anyLong(), any(Pageable.class)))
                    .thenReturn(productoPage);

            Page<ProductoDTO> resultado = productoService.listarSinProducto(Pageable.ofSize(10));

            assertNotNull(resultado);
            verify(auditLogger).logListar("Producto", productos.size());
        }
    }
}