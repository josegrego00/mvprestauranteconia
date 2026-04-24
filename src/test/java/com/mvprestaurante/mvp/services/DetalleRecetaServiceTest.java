package com.mvprestaurante.mvp.services;

import static com.mvprestaurante.mvp.testdata.DataProviderDetalleReceta.*;
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

import com.mvprestaurante.mvp.DTO.DetalleRecetaDTO;
import com.mvprestaurante.mvp.enums.UnidadMedida;
import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.mapper.DetalleRecetaMapper;
import com.mvprestaurante.mvp.models.DetalleReceta;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.models.Receta;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.DetalleRecetaRepository;
import com.mvprestaurante.mvp.repositories.IngredienteRepository;
import com.mvprestaurante.mvp.repositories.RecetaRepository;
import com.mvprestaurante.mvp.utils.AuditLogger;

@ExtendWith(MockitoExtension.class)
class DetalleRecetaServiceTest {

    @Mock
    private DetalleRecetaRepository detalleRecetaRepository;

    @Mock
    private DetalleRecetaMapper detalleRecetaMapper;

    @Mock
    private RecetaRepository recetaRepository;

    @Mock
    private IngredienteRepository ingredienteRepository;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private DetalleRecetaService detalleRecetaService;

    private static final Long EMPRESA_ID = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(EMPRESA_ID);
    }

    @Nested
    @DisplayName("listar")
    class ListarTests {

        @Test
        @DisplayName("debe retornar lista de detalles de receta")
        void debeRetornarListaDeDetalles() {
            List<DetalleReceta> detalles = listaDetallesReceta();
            List<DetalleRecetaDTO> detallesDTO = listaDetallesRecetaDTO();
            Page<DetalleReceta> detallePage = new PageImpl<>(detalles);

            when(detalleRecetaRepository.findByRecetaEmpresaId(anyLong(), any(Pageable.class)))
                    .thenReturn(detallePage);
            when(detalleRecetaMapper.toDTO(any(DetalleReceta.class)))
                    .thenReturn(detallesDTO.get(0));

            Page<DetalleRecetaDTO> resultado = detalleRecetaService.listar(Pageable.ofSize(10));

            assertNotNull(resultado);
            assertEquals(2, resultado.getContent().size());
            verify(detalleRecetaRepository).findByRecetaEmpresaId(anyLong(), any(Pageable.class));
            verify(auditLogger).logListar("DetalleReceta", 2);
        }

        @Test
        @DisplayName("debe retornar lista vacia cuando no hay detalles")
        void debeRetornarListaVacia() {
            Page<DetalleReceta> detallePage = new PageImpl<>(List.of());

            when(detalleRecetaRepository.findByRecetaEmpresaId(anyLong(), any(Pageable.class)))
                    .thenReturn(detallePage);

            Page<DetalleRecetaDTO> resultado = detalleRecetaService.listar(Pageable.ofSize(10));

            assertNotNull(resultado);
            assertTrue(resultado.getContent().isEmpty());
            verify(auditLogger).logListar("DetalleReceta", 0);
        }
    }

    @Nested
    @DisplayName("obtenerPorId")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("debe retornar detalle por id")
        void debeRetornarDetallePorId() {
            DetalleReceta detalle = unDetalleRecetaCompleto();
            DetalleRecetaDTO detalleDTO = unDetalleRecetaDTOConId();

            when(detalleRecetaRepository.findByIdAndRecetaEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(detalle));
            when(detalleRecetaMapper.toDTO(detalle)).thenReturn(detalleDTO);

            DetalleRecetaDTO resultado = detalleRecetaService.obtenerPorId(1L);

            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
            verify(detalleRecetaRepository).findByIdAndRecetaEmpresaId(1L, EMPRESA_ID);
            verify(auditLogger).logBuscar("DetalleReceta", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando no existe")
        void debeLanzarExcepcionCuandoNoExiste() {
            when(detalleRecetaRepository.findByIdAndRecetaEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () -> detalleRecetaService.obtenerPorId(1L));
        }
    }

    @Nested
    @DisplayName("listarPorRecetaId")
    class ListarPorRecetaIdTests {

        @Test
        @DisplayName("debe retornar detalles por receta id")
        void debeRetornarDetallesPorRecetaId() {
            List<DetalleReceta> detalles = listaDetallesReceta();
            List<DetalleRecetaDTO> detallesDTO = listaDetallesRecetaDTO();
            Page<DetalleReceta> detallePage = new PageImpl<>(detalles);
            Receta receta = Receta.builder().id(1L).build();

            when(recetaRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(receta));
            when(detalleRecetaRepository.findByRecetaId(anyLong(), anyLong(), any(Pageable.class)))
                    .thenReturn(detallePage);
            when(detalleRecetaMapper.toDTO(any(DetalleReceta.class)))
                    .thenReturn(detallesDTO.get(0));

            Page<DetalleRecetaDTO> resultado = detalleRecetaService.listarPorRecetaId(1L, Pageable.ofSize(10));

            assertNotNull(resultado);
            assertEquals(2, resultado.getContent().size());
            verify(detalleRecetaRepository).findByRecetaId(EMPRESA_ID, 1L, Pageable.ofSize(10));
            verify(auditLogger).logListar("DetalleReceta", 2);
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando receta no existe")
        void debeLanzarExcepcionCuandoRecetaNoExiste() {
            when(recetaRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () -> 
                    detalleRecetaService.listarPorRecetaId(1L, Pageable.ofSize(10)));
        }
    }

    @Nested
    @DisplayName("guardar")
    class GuardarTests {

        @Test
        @DisplayName("debe guardar detalle de receta")
        void debeGuardarDetalle() {
            DetalleRecetaDTO dto = unDetalleRecetaDTO();
            Receta receta = Receta.builder().id(1L).build();
            Ingrediente ingrediente = Ingrediente.builder().id(1L).build();
            DetalleReceta detalle = unDetalleRecetaCompleto();
            DetalleRecetaDTO detalleDTO = unDetalleRecetaDTOConId();

            when(recetaRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(receta));
            when(ingredienteRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(ingrediente));
            when(detalleRecetaRepository.existsByRecetaAndIngrediente(anyLong(), anyLong(), anyLong()))
                    .thenReturn(false);
            when(detalleRecetaRepository.save(any(DetalleReceta.class))).thenReturn(detalle);
            when(detalleRecetaMapper.toDTO(detalle)).thenReturn(detalleDTO);

            DetalleRecetaDTO resultado = detalleRecetaService.guardar(dto);

            assertNotNull(resultado);
            verify(detalleRecetaRepository).save(any(DetalleReceta.class));
            verify(auditLogger).logCrear("DetalleReceta", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando receta no existe")
        void debeLanzarExcepcionCuandoRecetaNoExiste() {
            DetalleRecetaDTO dto = unDetalleRecetaDTO();

            when(recetaRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () -> detalleRecetaService.guardar(dto));
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando ingrediente no existe")
        void debeLanzarExcepcionCuandoIngredienteNoExiste() {
            DetalleRecetaDTO dto = unDetalleRecetaDTO();
            Receta receta = Receta.builder().id(1L).build();

            when(recetaRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(receta));
            when(ingredienteRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () -> detalleRecetaService.guardar(dto));
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando ingrediente ya existe en receta")
        void debeLanzarExcepcionCuandoIngredienteYaExiste() {
            DetalleRecetaDTO dto = unDetalleRecetaDTO();
            Receta receta = Receta.builder().id(1L).build();
            Ingrediente ingrediente = Ingrediente.builder().id(1L).build();

            when(recetaRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(receta));
            when(ingredienteRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(ingrediente));
            when(detalleRecetaRepository.existsByRecetaAndIngrediente(anyLong(), anyLong(), anyLong()))
                    .thenReturn(true);

            assertThrows(BusinessException.class, () -> detalleRecetaService.guardar(dto));
        }
    }

    @Nested
    @DisplayName("actualizar")
    class ActualizarTests {

        @Test
        @DisplayName("debe actualizar detalle de receta")
        void debeActualizarDetalle() {
            DetalleRecetaDTO dto = unDetalleRecetaDTO();
            dto.setCantidadIngrediente(BigDecimal.valueOf(0.5));
            DetalleReceta detalle = unDetalleRecetaCompleto();
            DetalleRecetaDTO detalleDTO = unDetalleRecetaDTOConId();
            detalleDTO.setCantidadIngrediente(BigDecimal.valueOf(0.5));

            when(detalleRecetaRepository.findByIdAndRecetaEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(detalle));
            when(detalleRecetaRepository.save(any(DetalleReceta.class))).thenReturn(detalle);
            when(detalleRecetaMapper.toDTO(detalle)).thenReturn(detalleDTO);

            DetalleRecetaDTO resultado = detalleRecetaService.actualizar(1L, dto);

            assertNotNull(resultado);
            verify(detalleRecetaRepository).save(detalle);
            verify(auditLogger).logActualizar("DetalleReceta", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando detalle no existe")
        void debeLanzarExcepcionCuandoNoExiste() {
            DetalleRecetaDTO dto = unDetalleRecetaDTO();

            when(detalleRecetaRepository.findByIdAndRecetaEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () -> detalleRecetaService.actualizar(1L, dto));
        }
    }

    @Nested
    @DisplayName("eliminar")
    class EliminarTests {

        @Test
        @DisplayName("debe eliminar detalle de receta")
        void debeEliminarDetalle() {
            DetalleReceta detalle = unDetalleRecetaCompleto();

            when(detalleRecetaRepository.findByIdAndRecetaEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(detalle));

            boolean resultado = detalleRecetaService.eliminar(1L);

            assertTrue(resultado);
            verify(detalleRecetaRepository).delete(detalle);
            verify(auditLogger).logEliminar("DetalleReceta", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando detalle no existe")
        void debeLanzarExcepcionCuandoNoExiste() {
            when(detalleRecetaRepository.findByIdAndRecetaEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () -> detalleRecetaService.eliminar(1L));
        }
    }

    @Nested
    @DisplayName("calcularStockMinimo")
    class CalcularStockMinimoTests {

        @Test
        @DisplayName("debe calcular stock minimo correctamente")
        void debeCalcularStockMinimo() {
            Receta receta = Receta.builder().id(1L).build();
            DetalleReceta detalle = DetalleReceta.builder()
                    .ingrediente(Ingrediente.builder()
                            .stockDisponible(50.0)
                            .build())
                    .cantidadIngrediente(BigDecimal.valueOf(0.25))
                    .build();
            Page<DetalleReceta> detallePage = new PageImpl<>(List.of(detalle));

            when(recetaRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(receta));
            when(detalleRecetaRepository.findByRecetaId(anyLong(), anyLong(), any(Pageable.class)))
                    .thenReturn(detallePage);

            BigDecimal resultado = detalleRecetaService.calcularStockMinimo(1L);

            assertNotNull(resultado);
            assertEquals(200, resultado.intValue());
        }

        @Test
        @DisplayName("debe retornar cero cuando cantidad es cero")
        void debeRetornarCeroCuandoCantidadEsCero() {
            Receta receta = Receta.builder().id(1L).build();
            DetalleReceta detalle = DetalleReceta.builder()
                    .ingrediente(Ingrediente.builder()
                            .stockDisponible(50.0)
                            .build())
                    .cantidadIngrediente(BigDecimal.ZERO)
                    .build();
            Page<DetalleReceta> detallePage = new PageImpl<>(List.of(detalle));

            when(recetaRepository.findByIdAndEmpresaId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(receta));
            when(detalleRecetaRepository.findByRecetaId(anyLong(), anyLong(), any(Pageable.class)))
                    .thenReturn(detallePage);

            BigDecimal resultado = detalleRecetaService.calcularStockMinimo(1L);

            assertEquals(BigDecimal.ZERO, resultado);
        }
    }
}