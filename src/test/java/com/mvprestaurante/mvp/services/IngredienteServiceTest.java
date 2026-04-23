package com.mvprestaurante.mvp.services;

import static com.mvprestaurante.mvp.testdata.DataProviderIngrediente.*;
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

import com.mvprestaurante.mvp.DTO.IngredienteDTO;
import com.mvprestaurante.mvp.enums.UnidadMedida;
import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.mapper.IngredienteMapper;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.DetalleRecetaRepository;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;
import com.mvprestaurante.mvp.repositories.IngredienteRepository;
import com.mvprestaurante.mvp.repositories.RecetaRepository;
import com.mvprestaurante.mvp.utils.AuditLogger;

@ExtendWith(MockitoExtension.class)
class IngredienteServiceTest {

    @Mock
    private IngredienteRepository ingredienteRepository;

    @Mock
    private EmpresaRepositorio empresaRepositorio;

    @Mock
    private RecetaRepository recetaRepository;

    @Mock
    private DetalleRecetaRepository detalleRecetaRepository;

    @Mock
    private RecetaService recetaService;

    @Mock
    private IngredienteMapper ingredienteMapper;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private IngredienteService ingredienteService;

    private static final Long EMPRESA_ID = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(EMPRESA_ID);
    }

    @Nested
    @DisplayName("listarActivos")
    class ListarActivosTests {

        @Test
        @DisplayName("debe retornar lista de ingredientes activos")
        void debeRetornarListaDeIngredientesActivos() {
            List<Ingrediente> ingredientes = listaIngredientes();
            List<IngredienteDTO> ingredientesDTO = listaIngredientesDTO();
            Page<Ingrediente> ingredientePage = new PageImpl<>(ingredientes);

            when(ingredienteRepository.findByEstaActivoTrue(anyLong(), any(Pageable.class))).thenReturn(ingredientePage);
            when(ingredienteMapper.toDTO(any(Ingrediente.class))).thenReturn(ingredientesDTO.get(0));

            Page<IngredienteDTO> resultado = ingredienteService.listarActivos(Pageable.ofSize(10));

            assertNotNull(resultado);
            assertEquals(2, resultado.getContent().size());
            verify(ingredienteRepository).findByEstaActivoTrue(anyLong(), any(Pageable.class));
            verify(auditLogger).logListar("Ingrediente", 2);
        }

        @Test
        @DisplayName("debe retornar lista vacia cuando no hay ingredientes")
        void debeRetornarListaVaciaCuandoNoHayIngredientes() {
            Page<Ingrediente> ingredientePage = new PageImpl<>(List.of());

            when(ingredienteRepository.findByEstaActivoTrue(anyLong(), any(Pageable.class))).thenReturn(ingredientePage);

            Page<IngredienteDTO> resultado = ingredienteService.listarActivos(Pageable.ofSize(10));

            assertTrue(resultado.getContent().isEmpty());
            verify(auditLogger).logListar("Ingrediente", 0);
        }
    }

    @Nested
    @DisplayName("buscarPorNombre")
    class BuscarPorNombreTests {

        @Test
        @DisplayName("debe buscar ingredientes por nombre")
        void debeBuscarIngredientesPorNombre() {
            List<Ingrediente> ingredientes = listaIngredientes();
            Page<Ingrediente> ingredientePage = new PageImpl<>(ingredientes);

            when(ingredienteRepository.findByNombreContainingIgnoreCaseAndEstaActivoTrue(anyLong(), anyString(), any(Pageable.class)))
                    .thenReturn(ingredientePage);
            when(ingredienteMapper.toDTO(any(Ingrediente.class))).thenReturn(unIngredienteDTO());

            Page<IngredienteDTO> resultado = ingredienteService.buscarPorNombre("carne", Pageable.ofSize(10));

            assertNotNull(resultado);
            verify(ingredienteRepository).findByNombreContainingIgnoreCaseAndEstaActivoTrue(anyLong(), anyString(), any(Pageable.class));
            verify(auditLogger).logBuscar("Ingrediente", "Nombre: carne");
        }
    }

    @Nested
    @DisplayName("obtenerPorId")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("debe retornar ingrediente cuando existe")
        void debeRetornarIngredienteCuandoExiste() {
            Ingrediente ingrediente = unIngredienteConId();
            IngredienteDTO ingredienteDTO = unIngredienteDTOConId();

            when(ingredienteRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(ingrediente));
            when(ingredienteMapper.toDTO(ingrediente)).thenReturn(ingredienteDTO);

            Optional<IngredienteDTO> resultado = ingredienteService.obtenerPorId(1L);

            assertTrue(resultado.isPresent());
            assertEquals("Queso", resultado.get().getNombre());
            verify(ingredienteRepository).findByIdAndEmpresaId(1L, EMPRESA_ID);
            verify(auditLogger).logBuscar("Ingrediente", "1");
        }

        @Test
        @DisplayName("debe retornar vacio cuando ingrediente no existe")
        void debeRetornarVacioCuandoIngredienteNoExiste() {
            when(ingredienteRepository.findByIdAndEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.empty());

            Optional<IngredienteDTO> resultado = ingredienteService.obtenerPorId(99L);

            assertFalse(resultado.isPresent());
        }
    }

    @Nested
    @DisplayName("guardar")
    class GuardarTests {

        @Test
        @DisplayName("debe guardar ingrediente exitosamente")
        void debeGuardarIngredienteExitosamente() {
            IngredienteDTO dto = unIngredienteDTO();
            Ingrediente ingrediente = unIngrediente();
            Empresa empresa = Empresa.builder().id(EMPRESA_ID).build();

            when(ingredienteRepository.existsByNombreAndEstaActivoTrue(anyLong(), anyString())).thenReturn(false);
            when(empresaRepositorio.findById(EMPRESA_ID)).thenReturn(Optional.of(empresa));
            when(ingredienteMapper.toEntity(dto)).thenReturn(ingrediente);
            when(ingredienteRepository.save(any(Ingrediente.class))).thenReturn(ingrediente);
            when(ingredienteMapper.toDTO(ingrediente)).thenReturn(dto);

            IngredienteDTO resultado = ingredienteService.guardar(dto);

            assertNotNull(resultado);
            verify(ingredienteRepository).save(any(Ingrediente.class));
            verify(auditLogger).logCrear("Ingrediente", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando nombre ya existe")
        void debeLanzarExcepcionCuandoNombreYaExiste() {
            IngredienteDTO dto = unIngredienteDTO();

            when(ingredienteRepository.existsByNombreAndEstaActivoTrue(anyLong(), anyString())).thenReturn(true);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> ingredienteService.guardar(dto)
            );

            assertEquals("Ya existe un ingrediente con ese nombre", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("actualizar")
    class ActualizarTests {

        @Test
        @DisplayName("debe actualizar ingrediente exitosamente")
        void debeActualizarIngredienteExitosamente() {
            Ingrediente ingredienteExistente = unIngredienteConId();
            IngredienteDTO dto = unIngredienteDTOConId();

            when(ingredienteRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(ingredienteExistente));
            when(ingredienteRepository.existsByNombreAndEstaActivoTrue(anyLong(), anyString())).thenReturn(false);
            when(ingredienteRepository.save(any(Ingrediente.class))).thenReturn(ingredienteExistente);
            when(ingredienteMapper.toDTO(ingredienteExistente)).thenReturn(dto);

            IngredienteDTO resultado = ingredienteService.actualizar(1L, dto);

            assertNotNull(resultado);
            verify(ingredienteRepository).save(any(Ingrediente.class));
            verify(auditLogger).logActualizar("Ingrediente", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando ingrediente no existe")
        void debeLanzarExcepcionCuandoIngredienteNoExiste() {
            IngredienteDTO dto = unIngredienteDTOConId();

            when(ingredienteRepository.findByIdAndEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> ingredienteService.actualizar(99L, dto)
            );

            assertEquals("Ingrediente no encontrado", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("eliminar")
    class EliminarTests {

        @Test
        @DisplayName("debe eliminar ingrediente cambiando estaActivo a false")
        void debeEliminarIngredienteCambiandoEstaActivoAFalse() {
            Ingrediente ingrediente = unIngredienteConId();

            when(ingredienteRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(ingrediente));
            when(ingredienteRepository.existsByIngredienteEnReceta(anyLong(), anyLong())).thenReturn(false);
            when(ingredienteRepository.save(any(Ingrediente.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ingredienteService.eliminar(1L);

            verify(ingredienteRepository).save(argThat(i -> i.getEstaActivo() == false));
            verify(auditLogger).logDesactivar("Ingrediente", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando ingrediente no existe")
        void debeLanzarExcepcionCuandoIngredienteNoExiste() {
            when(ingredienteRepository.findByIdAndEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> ingredienteService.eliminar(99L)
            );

            assertEquals("Ingrediente no encontrado", exception.getMessage());
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando ingrediente esta en receta")
        void debeLanzarExcepcionCuandoIngredienteEstaEnReceta() {
            Ingrediente ingrediente = unIngredienteConId();

            when(ingredienteRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(ingrediente));
            when(ingredienteRepository.existsByIngredienteEnReceta(anyLong(), anyLong())).thenReturn(true);

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> ingredienteService.eliminar(1L)
            );

            assertEquals("No se puede eliminar el ingrediente porque está siendo usado en una o más recetas", exception.getMessage());
        }
    }
}