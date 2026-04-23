package com.mvprestaurante.mvp.services;

import static com.mvprestaurante.mvp.testdata.DataProviderUsuario.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mvprestaurante.mvp.DTO.UsuarioRequestDTO;
import com.mvprestaurante.mvp.DTO.UsuarioResponseDTO;
import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.mapper.UsuarioMapper;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;
import com.mvprestaurante.mvp.repositories.UsuarioRepositorio;
import com.mvprestaurante.mvp.utils.AuditLogger;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private EmpresaRepositorio empresaRepositorio;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private UsuarioService usuarioService;

    private static final Long EMPRESA_ID = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(EMPRESA_ID);
    }

    @Nested
    @DisplayName("listarUsuarios")
    class ListarUsuariosTests {

        @Test
        @DisplayName("debe retornar lista de usuarios DTO cuando existen usuarios")
        void debeRetornarListaDeUsuariosDTOCuandoExistenUsuarios() {
            List<Usuario> usuarios = listaUsuarios();
            List<UsuarioResponseDTO> usuariosDTO = listaUsuariosDTO();

            when(usuarioRepositorio.findByEmpresaId(EMPRESA_ID)).thenReturn(usuarios);
            when(usuarioMapper.toResponse(usuarios.get(0))).thenReturn(usuariosDTO.get(0));
            when(usuarioMapper.toResponse(usuarios.get(1))).thenReturn(usuariosDTO.get(1));

            List<UsuarioResponseDTO> resultado = usuarioService.listarUsuarios();

            assertEquals(2, resultado.size());
            assertEquals(usuariosDTO, resultado);
            verify(usuarioRepositorio).findByEmpresaId(EMPRESA_ID);
            verify(auditLogger).logListar("Usuario", 2);
        }

        @Test
        @DisplayName("debe retornar lista vacia cuando no existen usuarios")
        void debeRetornarListaVaciaCuandoNoExistenUsuarios() {
            when(usuarioRepositorio.findByEmpresaId(EMPRESA_ID)).thenReturn(List.of());

            List<UsuarioResponseDTO> resultado = usuarioService.listarUsuarios();

            assertTrue(resultado.isEmpty());
            verify(usuarioRepositorio).findByEmpresaId(EMPRESA_ID);
            verify(auditLogger).logListar("Usuario", 0);
        }
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorIdTests {

        @Test
        @DisplayName("debe retornar usuario DTO cuando existe")
        void debeRetornarUsuarioDTOCuandoExiste() {
            Usuario usuario = unUsuarioConId();
            UsuarioResponseDTO usuarioDTO = unUsuarioResponseDTO();

            when(usuarioRepositorio.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(usuario));
            when(usuarioMapper.toResponse(usuario)).thenReturn(usuarioDTO);

            UsuarioResponseDTO resultado = usuarioService.buscarPorId(1L);

            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
            verify(usuarioRepositorio).findByIdAndEmpresaId(1L, EMPRESA_ID);
            verify(auditLogger).logBuscar("Usuario", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando no existe usuario")
        void debeLanzarExcepcionCuandoNoExisteUsuario() {
            when(usuarioRepositorio.findByIdAndEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> usuarioService.buscarPorId(99L)
            );

            assertEquals("Usuario no encontrado", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("guardarUsuario")
    class GuardarUsuarioTests {

        @Test
        @DisplayName("debe guardar usuario exitosamente")
        void debeGuardarUsuarioExitosamente() {
            UsuarioRequestDTO dto = unUsuarioRequestDTO();
            Usuario usuario = unUsuario();
            Usuario usuarioGuardado = unUsuarioConId();
            Empresa empresa = Empresa.builder().id(EMPRESA_ID).nombreEmpresa("Test").build();

            when(usuarioRepositorio.findByNombreUsuarioAndEmpresa_Id(anyString(), anyLong()))
                    .thenReturn(Optional.empty());
            when(empresaRepositorio.findById(EMPRESA_ID)).thenReturn(Optional.of(empresa));
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");
            when(usuarioMapper.toEntity(dto)).thenReturn(usuario);
            when(usuarioRepositorio.save(any(Usuario.class))).thenReturn(usuarioGuardado);
            when(usuarioMapper.toResponse(usuarioGuardado)).thenReturn(unUsuarioResponseDTO());

            UsuarioResponseDTO resultado = usuarioService.guardarUsuario(dto);

            assertNotNull(resultado);
            verify(usuarioRepositorio).findByNombreUsuarioAndEmpresa_Id(anyString(), anyLong());
            verify(empresaRepositorio).findById(EMPRESA_ID);
            verify(usuarioRepositorio).save(any(Usuario.class));
            verify(auditLogger).logCrear("Usuario", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando rol es invalido")
        void debeLanzarExcepcionCuandoRolEsInvalido() {
            UsuarioRequestDTO dto = unUsuarioRequestDTO();
            dto.setRol("ROL_INVALIDO");

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> usuarioService.guardarUsuario(dto)
            );

            assertEquals("Rol inválido. Solo se permiten ADMIN o CAJERO", exception.getMessage());
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando nombre de usuario ya existe")
        void debeLanzarExcepcionCuandoNombreDeUsuarioYaExiste() {
            UsuarioRequestDTO dto = unUsuarioRequestDTO();

            when(usuarioRepositorio.findByNombreUsuarioAndEmpresa_Id(anyString(), anyLong()))
                    .thenReturn(Optional.of(unUsuarioConId()));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> usuarioService.guardarUsuario(dto)
            );

            assertEquals("El nombre de usuario ya existe en esta empresa", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("actualizarUsuario")
    class ActualizarUsuarioTests {

        @Test
        @DisplayName("debe actualizar usuario exitosamente")
        void debeActualizarUsuarioExitosamente() {
            Usuario usuarioExistente = unUsuarioConId();
            UsuarioRequestDTO dto = unUsuarioRequestDTOConId();
            Usuario usuarioActualizado = unUsuarioConId();

            when(usuarioRepositorio.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(usuarioExistente));
            when(usuarioRepositorio.save(any(Usuario.class))).thenReturn(usuarioExistente);
            when(usuarioMapper.toResponse(usuarioExistente)).thenReturn(unUsuarioResponseDTO());

            UsuarioResponseDTO resultado = usuarioService.actualizarUsuario(1L, dto);

            assertNotNull(resultado);
            verify(usuarioRepositorio).findByIdAndEmpresaId(1L, EMPRESA_ID);
            verify(usuarioRepositorio).save(any(Usuario.class));
            verify(auditLogger).logActualizar("Usuario", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando usuario no existe")
        void debeLanzarExcepcionCuandoUsuarioNoExiste() {
            UsuarioRequestDTO dto = unUsuarioRequestDTOConId();

            when(usuarioRepositorio.findByIdAndEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> usuarioService.actualizarUsuario(99L, dto)
            );

            assertEquals("Usuario no encontrado", exception.getMessage());
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando nuevo nombre de usuario ya existe")
        void debeLanzarExcepcionCuandoNuevoNombreDeUsuarioYaExiste() {
            Usuario usuarioExistente = unUsuarioConId();
            UsuarioRequestDTO dto = unUsuarioRequestDTOConId();
            dto.setNombreUsuario("nuevousuario");

            when(usuarioRepositorio.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(usuarioExistente));
            when(usuarioRepositorio.findByNombreUsuarioAndEmpresa_Id(anyString(), anyLong()))
                    .thenReturn(Optional.of(unUsuario()));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> usuarioService.actualizarUsuario(1L, dto)
            );

            assertEquals("El nombre de usuario ya existe en esta empresa", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("eliminarUsuario")
    class EliminarUsuarioTests {

        @Test
        @DisplayName("debe eliminar usuario cambiando estaActivo a false")
        void debeEliminarUsuarioCambiandoEstaActivoAFalse() {
            Usuario usuario = unUsuarioActivo();

            when(usuarioRepositorio.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(usuario));
            when(usuarioRepositorio.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(usuarioMapper.toResponse(any(Usuario.class))).thenReturn(unUsuarioResponseDTO());

            UsuarioResponseDTO resultado = usuarioService.eliminarUsuario(1L);

            assertNotNull(resultado);
            verify(usuarioRepositorio).save(argThat(u -> u.getEstaActivo() == false));
            verify(auditLogger).logDesactivar("Usuario", "1");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando usuario no existe")
        void debeLanzarExcepcionCuandoUsuarioNoExiste() {
            when(usuarioRepositorio.findByIdAndEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> usuarioService.eliminarUsuario(99L)
            );

            assertEquals("Usuario no encontrado", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("activarUsuario")
    class ActivarUsuarioTests {

        @Test
        @DisplayName("debe activar usuario exitosamente")
        void debeActivarUsuarioExitosamente() {
            Usuario usuario = unUsuarioInactivo();

            when(usuarioRepositorio.findByIdAndEmpresaId(2L, EMPRESA_ID)).thenReturn(Optional.of(usuario));
            when(usuarioRepositorio.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(usuarioMapper.toResponse(any(Usuario.class))).thenReturn(unUsuarioResponseDTO());

            UsuarioResponseDTO resultado = usuarioService.activarUsuario(2L);

            assertNotNull(resultado);
            verify(usuarioRepositorio).save(argThat(u -> u.getEstaActivo() == true));
            verify(auditLogger).logActivar("Usuario", "2");
        }

        @Test
        @DisplayName("debe lanzar excepcion cuando usuario no existe")
        void debeLanzarExcepcionCuandoUsuarioNoExiste() {
            when(usuarioRepositorio.findByIdAndEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> usuarioService.activarUsuario(99L)
            );

            assertEquals("Usuario no encontrado", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("crearUsuarioAdmin")
    class CrearUsuarioAdminTests {

        @Test
        @DisplayName("debe crear usuario admin exitosamente")
        void debeCrearUsuarioAdminExitosamente() {
            Empresa empresa = Empresa.builder()
                    .id(EMPRESA_ID)
                    .subdominio("testempresa")
                    .nombreEmpresa("Test Empresa")
                    .build();

            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");

            usuarioService.crearUsuarioAdmin(empresa);

            verify(usuarioRepositorio).save(any(Usuario.class));
        }
    }
}