package com.mvprestaurante.mvp.services;

import static com.mvprestaurante.mvp.testdata.DataProviderReceta.*;
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

import com.mvprestaurante.mvp.DTO.RecetaDTO;
import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.mapper.DetalleRecetaMapper;
import com.mvprestaurante.mvp.mapper.RecetaMapper;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Receta;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.DetalleRecetaRepository;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;
import com.mvprestaurante.mvp.repositories.IngredienteRepository;
import com.mvprestaurante.mvp.repositories.RecetaRepository;
import com.mvprestaurante.mvp.utils.AuditLogger;

@ExtendWith(MockitoExtension.class)
class RecetaServiceTest {

    @Mock
    private RecetaRepository recetaRepository;

    @Mock
    private DetalleRecetaRepository detalleRecetaRepository;

    @Mock
    private RecetaMapper recetaMapper;

    @Mock
    private DetalleRecetaMapper detalleRecetaMapper;

    @Mock
    private EmpresaRepositorio empresaRepositorio;

    @Mock
    private IngredienteRepository ingredienteRepository;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private RecetaService recetaService;

    private static final Long EMPRESA_ID = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(EMPRESA_ID);
    }

    @Nested
    @DisplayName("listarActivas")
    class ListarActivasTests {

        @Test
        @DisplayName("debe retornar lista de recetas activas")
        void debeRetornarListaDeRecetasActivas() {
            List<Receta> recetas = listaRecetas();
            List<RecetaDTO> recetasDTO = listaRecetasDTO();
            Page<Receta> recetaPage = new PageImpl<>(recetas);

            when(recetaRepository.findByEstaActivaTrue(anyLong(), any(Pageable.class)))
                    .thenReturn(recetaPage);
            when(recetaMapper.toDTO(any(Receta.class)))
                    .thenReturn(recetasDTO.get(0));

            Page<RecetaDTO> resultado = recetaService.listarActivas(Pageable.ofSize(10));

            assertNotNull(resultado);
            assertEquals(2, resultado.getContent().size());
            verify(recetaRepository).findByEstaActivaTrue(anyLong(), any(Pageable.class));
            verify(auditLogger).logListar("Receta", 2);
        }

        @Test
        @DisplayName("debe retornar lista vacia cuando no hay recetas")
        void debeRetornarListaVacia() {
            Page<Receta> recetaPage = new PageImpl<>(List.of());

            when(recetaRepository.findByEstaActivaTrue(anyLong(), any(Pageable.class)))
                    .thenReturn(recetaPage);

            Page<RecetaDTO> resultado = recetaService.listarActivas(Pageable.ofSize(10));

            assertNotNull(resultado);
            assertTrue(resultado.getContent().isEmpty());
            verify(auditLogger).logListar("Receta", 0);
        }
    }

    @Nested
    @DisplayName("obtenerPorId")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("debe retornar receta por id")
        void debeRetornarRecetaPorId() {
            Receta receta = unaRecetaCompleta();
            RecetaDTO recetaDTO = unaRecetaDTOConId();

            when(recetaRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(receta));
            when(recetaMapper.toDTO(receta)).thenReturn(recetaDTO);

            RecetaDTO resultado = recetaService.obtenerPorId(1L);

            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
            verify(recetaRepository).findByIdAndEmpresaId(1L, EMPRESA_ID);
            verify(auditLogger).logBuscar("Receta", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando no existe")
        void debeLanzarExcepcionCuandoNoExiste() {
            when(recetaRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () -> recetaService.obtenerPorId(1L));
        }
    }

    @Nested
    @DisplayName("crear")
    class CrearTests {

        @Test
        @DisplayName("debe crear receta")
        void debeCrearReceta() {
            RecetaDTO dto = unaRecetaDTO();
            Receta receta = unaRecetaConId();
            Empresa empresa = Empresa.builder().id(1L).build();

            when(empresaRepositorio.findById(anyLong())).thenReturn(Optional.of(empresa));
            when(recetaMapper.toEntity(dto)).thenReturn(receta);
            when(recetaRepository.existsByNombreAndEstaActivaTrue(anyLong(), anyString())).thenReturn(false);
            when(recetaRepository.save(any(Receta.class))).thenReturn(receta);
            when(recetaMapper.toDTO(receta)).thenReturn(unaRecetaDTOConId());

            RecetaDTO resultado = recetaService.crear(dto, new Long[]{1L}, new BigDecimal[]{BigDecimal.valueOf(0.5)});

            assertNotNull(resultado);
            verify(recetaRepository).save(any(Receta.class));
            verify(auditLogger).logCrear("Receta", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando nombre ya existe")
        void debeLanzarExcepcionCuandoNombreYaExiste() {
            RecetaDTO dto = unaRecetaDTO();

            when(recetaRepository.existsByNombreAndEstaActivaTrue(anyLong(), anyString()))
                    .thenReturn(true);

            assertThrows(BusinessException.class, () -> 
                    recetaService.crear(dto, new Long[]{1L}, new BigDecimal[]{BigDecimal.valueOf(0.5)}));
        }
    }

    @Nested
    @DisplayName("actualizar")
    class ActualizarTests {

        @Test
        @DisplayName("debe actualizar receta")
        void debeActualizarReceta() {
            RecetaDTO dto = unaRecetaDTO();
            Receta receta = unaRecetaConId();
            dto.setNombre("Hamburguesa actualizada");

            when(recetaRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(receta));
            when(recetaRepository.existsByNombreAndEstaActivaTrue(anyLong(), anyString()))
                    .thenReturn(false);
            when(recetaRepository.save(any(Receta.class))).thenReturn(receta);
            when(recetaMapper.toDTO(receta)).thenReturn(unaRecetaDTOConId());

            RecetaDTO resultado = recetaService.actualizar(1L, dto, new Long[]{1L}, new BigDecimal[]{BigDecimal.valueOf(0.5)});

            assertNotNull(resultado);
            verify(recetaRepository).save(receta);
            verify(auditLogger).logActualizar("Receta", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando receta no existe")
        void debeLanzarExcepcionCuandoNoExiste() {
            RecetaDTO dto = unaRecetaDTO();

            when(recetaRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () -> 
                    recetaService.actualizar(1L, dto, new Long[]{1L}, new BigDecimal[]{BigDecimal.valueOf(0.5)}));
        }
    }

    @Nested
    @DisplayName("eliminar")
    class EliminarTests {

        @Test
        @DisplayName("debe eliminar receta")
        void debeEliminarReceta() {
            Receta receta = unaRecetaConId();

            when(recetaRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(receta));

            boolean resultado = recetaService.eliminar(1L);

            assertTrue(resultado);
            verify(recetaRepository).save(receta);
            verify(auditLogger).logDesactivar("Receta", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando tiene producto asociado")
        void debeLanzarExcepcionCuandoTieneProducto() {
            Receta receta = unaRecetaConId();
            receta.setProducto(new com.mvprestaurante.mvp.models.Producto());

            when(recetaRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(receta));

            assertThrows(BusinessException.class, () -> recetaService.eliminar(1L));
        }
    }

    @Nested
    @DisplayName("activar")
    class ActivarTests {

        @Test
        @DisplayName("debe activar receta")
        void debeActivarReceta() {
            Receta receta = unaRecetaConId();
            receta.setEstaActiva(false);

            when(recetaRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(receta));

            boolean resultado = recetaService.activar(1L);

            assertTrue(resultado);
            verify(recetaRepository).save(receta);
            verify(auditLogger).logActivar("Receta", "1");
        }
    }

    @Nested
    @DisplayName("calcularStockDisponible")
    class CalcularStockDisponibleTests {

        @Test
        @DisplayName("debe calcular stock disponible")
        void debeCalcularStockDisponible() {
            Receta receta = unaRecetaConId();
            receta.setListaIngredientes(List.of());

            when(recetaRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(receta));

            BigDecimal resultado = recetaService.calcularStockDisponible(1L);

            assertNotNull(resultado);
        }
    }

    @Nested
    @DisplayName("existePorNombre")
    class ExistePorNombreTests {

        @Test
        @DisplayName("debe retornar true cuando existe")
        void debeRetornarTrueCuandoExiste() {
            when(recetaRepository.existsByNombreAndEstaActivaTrue(anyLong(), anyString()))
                    .thenReturn(true);

            boolean resultado = recetaService.existePorNombre("Hamburguesa");

            assertTrue(resultado);
        }

        @Test
        @DisplayName("debe retornar false cuando no existe")
        void debeRetornarFalseCuandoNoExiste() {
            when(recetaRepository.existsByNombreAndEstaActivaTrue(anyLong(), anyString()))
                    .thenReturn(false);

            boolean resultado = recetaService.existePorNombre("No existe");

            assertFalse(resultado);
        }
    }
}