package com.mvprestaurante.mvp.services;

import static com.mvprestaurante.mvp.testdata.DataProviderEmpresa.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

import com.mvprestaurante.mvp.DTO.EmpresaDTO;
import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.mapper.EmpresaMapper;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepositorio empresaRepositorio;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private EmpresaMapper empresaMapper;

    @InjectMocks
    private EmpresaService empresaService;

    @Nested
    @DisplayName("listarTodas")
    class ListarTodasTests {

        @Test
        @DisplayName("debe retornar lista de empresas DTO cuando existen empresas")
        void debeRetornarListaDeEmpresasDTOCuandoExistenEmpresas() {
            List<Empresa> empresas = listaEmpresas();
            List<EmpresaDTO> empresasDTO = listaEmpresasDTO();

            when(empresaRepositorio.findAll()).thenReturn(empresas);
            when(empresaMapper.toResponse(empresas.get(0))).thenReturn(empresasDTO.get(0));
            when(empresaMapper.toResponse(empresas.get(1))).thenReturn(empresasDTO.get(1));

            List<EmpresaDTO> resultado = empresaService.listarTodas();

            assertEquals(2, resultado.size());
            assertEquals(empresasDTO, resultado);
            verify(empresaRepositorio).findAll();
        }

        @Test
        @DisplayName("debe retornar lista vacia cuando no existen empresas")
        void debeRetornarListaVaciaCuandoNoExistenEmpresas() {
            when(empresaRepositorio.findAll()).thenReturn(List.of());

            List<EmpresaDTO> resultado = empresaService.listarTodas();

            assertTrue(resultado.isEmpty());
            verify(empresaRepositorio).findAll();
        }
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorIdTests {

        @Test
        @DisplayName("debe retornar empresa DTO cuando existe")
        void debeRetornarEmpresaDTOCuandoExiste() {
            Empresa empresa = unaEmpresaConId();
            EmpresaDTO empresaDTO = unEmpresaDTOConId();

            when(empresaRepositorio.findById(1L)).thenReturn(Optional.of(empresa));
            when(empresaMapper.toResponse(empresa)).thenReturn(empresaDTO);

            EmpresaDTO resultado = empresaService.buscarPorId(1L);

            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
            verify(empresaRepositorio).findById(1L);
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando no existe empresa")
        void debeLanzarExcepcionCuandoNoExisteEmpresa() {
            when(empresaRepositorio.findById(99L)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> empresaService.buscarPorId(99L)
            );

            assertEquals("Empresa no encontrada", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("guardar")
    class GuardarTests {

        @Test
        @DisplayName("debe guardar empresa exitosamente")
        void debeGuardarEmpresaExitosamente() {
            EmpresaDTO dto = unEmpresaDTO();
            Empresa empresa = unaEmpresa();
            Empresa empresaGuardada = unaEmpresaConId();

            when(empresaRepositorio.findBySubdominio(anyString())).thenReturn(Optional.empty());
            when(empresaRepositorio.findByNombreEmpresa(anyString())).thenReturn(Optional.empty());
            when(empresaMapper.toEntity(dto)).thenReturn(empresa);
            when(empresaRepositorio.save(any(Empresa.class))).thenReturn(empresaGuardada);
            when(empresaMapper.toResponse(empresaGuardada)).thenReturn(unEmpresaDTOConId());

            EmpresaDTO resultado = empresaService.guardar(dto);

            assertNotNull(resultado);
            verify(empresaRepositorio).findBySubdominio(anyString());
            verify(empresaRepositorio).findByNombreEmpresa(anyString());
            verify(empresaRepositorio).save(any(Empresa.class));
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando subdominio ya existe")
        void debeLanzarExcepcionCuandoSubdominioYaExiste() {
            EmpresaDTO dto = unEmpresaDTO();

            when(empresaRepositorio.findBySubdominio(anyString())).thenReturn(Optional.of(unaEmpresa()));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> empresaService.guardar(dto)
            );

            assertTrue(exception.getMessage().contains("subdominio"));
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando nombre empresa ya existe")
        void debeLanzarExcepcionCuandoNombreEmpresaYaExiste() {
            EmpresaDTO dto = unEmpresaDTO();

            when(empresaRepositorio.findBySubdominio(anyString())).thenReturn(Optional.empty());
            when(empresaRepositorio.findByNombreEmpresa(anyString())).thenReturn(Optional.of(unaEmpresa()));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> empresaService.guardar(dto)
            );

            assertTrue(exception.getMessage().contains("Nombre"));
        }
    }

    @Nested
    @DisplayName("actualizar")
    class ActualizarTests {

        @Test
        @DisplayName("debe actualizar empresa exitosamente")
        void debeActualizarEmpresaExitosamente() {
            Empresa empresa = unaEmpresaConId();
            EmpresaDTO dto = unEmpresaDTOConId();

            when(empresaRepositorio.findById(1L)).thenReturn(Optional.of(empresa));
            when(empresaRepositorio.save(any(Empresa.class))).thenReturn(empresa);
            when(empresaMapper.toResponse(empresa)).thenReturn(dto);

            EmpresaDTO resultado = empresaService.actualizar(1L, dto);

            assertNotNull(resultado);
            verify(empresaRepositorio).findById(1L);
            verify(empresaRepositorio).save(any(Empresa.class));
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando empresa no existe")
        void debeLanzarExcepcionCuandoEmpresaNoExiste() {
            EmpresaDTO dto = unEmpresaDTOConId();

            when(empresaRepositorio.findById(99L)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> empresaService.actualizar(99L, dto)
            );

            assertEquals("Empresa no encontrada", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("eliminar")
    class EliminarTests {

        @Test
        @DisplayName("debe eliminar empresa cambiando activa a false")
        void debeEliminarEmpresaCambiandoActivaAFalse() {
            Empresa empresa = unaEmpresaActiva();

            when(empresaRepositorio.findById(1L)).thenReturn(Optional.of(empresa));
            when(empresaRepositorio.save(any(Empresa.class))).thenAnswer(invocation -> {
                Empresa saved = invocation.getArgument(0);
                return saved;
            });
            when(empresaMapper.toResponse(any(Empresa.class))).thenReturn(unEmpresaDTOConId());

            EmpresaDTO resultado = empresaService.eliminar(1L);

            assertNotNull(resultado);
            verify(empresaRepositorio).save(argThat(e -> e.getActiva() == false));
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando empresa no existe")
        void debeLanzarExcepcionCuandoEmpresaNoExiste() {
            when(empresaRepositorio.findById(99L)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> empresaService.eliminar(99L)
            );

            assertEquals("Empresa no encontrada", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("buscarPorSubdominio")
    class BuscarPorSubdominioTests {

        @Test
        @DisplayName("debe retornar empresa DTO cuando subdominio existe")
        void debeRetornarEmpresaDTOCuandoSubdominioExiste() {
            Empresa empresa = unaEmpresaConId();
            EmpresaDTO empresaDTO = unEmpresaDTOConId();

            when(empresaRepositorio.findBySubdominio("testempresa")).thenReturn(Optional.of(empresa));
            when(empresaMapper.toResponse(empresa)).thenReturn(empresaDTO);

            EmpresaDTO resultado = empresaService.buscarPorSubdominio("testempresa");

            assertNotNull(resultado);
            verify(empresaRepositorio).findBySubdominio("testempresa");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando subdominio no existe")
        void debeLanzarExcepcionCuandoSubdominioNoExiste() {
            when(empresaRepositorio.findBySubdominio("noexiste")).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> empresaService.buscarPorSubdominio("noexiste")
            );

            assertEquals("Subdominio no encontrado", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("existeNombreEmpresa")
    class ExisteNombreEmpresaTests {

        @Test
        @DisplayName("debe retornar true cuando nombre empresa existe")
        void debeRetornarTrueCuandoNombreEmpresaExiste() {
            when(empresaRepositorio.findByNombreEmpresa("Test")).thenReturn(Optional.of(unaEmpresa()));

            boolean resultado = empresaService.existeNombreEmpresa("Test");

            assertTrue(resultado);
        }

        @Test
        @DisplayName("debe retornar false cuando nombre empresa no existe")
        void debeRetornarFalseCuandoNombreEmpresaNoExiste() {
            when(empresaRepositorio.findByNombreEmpresa("NoExiste")).thenReturn(Optional.empty());

            boolean resultado = empresaService.existeNombreEmpresa("NoExiste");

            assertFalse(resultado);
        }
    }

    @Nested
    @DisplayName("actualizarEstadoActivo")
    class ActualizarEstadoActivoTests {

        @Test
        @DisplayName("debe activar empresa exitosamente")
        void debeActivarEmpresaExitosamente() {
            Empresa empresa = unaEmpresaInactiva();
            EmpresaDTO empresaDTO = unEmpresaDTOConId();
            empresaDTO.setActiva(true);

            when(empresaRepositorio.findById(1L)).thenReturn(Optional.of(empresa));
            when(empresaRepositorio.save(any(Empresa.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(empresaMapper.toResponse(any(Empresa.class))).thenReturn(empresaDTO);

            EmpresaDTO resultado = empresaService.actualizarEstadoActivo(1L, true);

            assertNotNull(resultado);
            assertTrue(resultado.getActiva());
            verify(empresaRepositorio).save(argThat(e -> e.getActiva() == true));
        }

        @Test
        @DisplayName("debe desactivar empresa exitosamente")
        void debeDesactivarEmpresaExitosamente() {
            Empresa empresa = unaEmpresaActiva();
            EmpresaDTO empresaDTO = unEmpresaDTOConId();
            empresaDTO.setActiva(false);

            when(empresaRepositorio.findById(1L)).thenReturn(Optional.of(empresa));
            when(empresaRepositorio.save(any(Empresa.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(empresaMapper.toResponse(any(Empresa.class))).thenReturn(empresaDTO);

            EmpresaDTO resultado = empresaService.actualizarEstadoActivo(1L, false);

            assertNotNull(resultado);
            assertFalse(resultado.getActiva());
            verify(empresaRepositorio).save(argThat(e -> e.getActiva() == false));
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando empresa no existe")
        void debeLanzarExcepcionCuandoEmpresaNoExiste() {
            when(empresaRepositorio.findById(99L)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> empresaService.actualizarEstadoActivo(99L, true)
            );

            assertEquals("Empresa no encontrada", exception.getMessage());
        }
    }
}