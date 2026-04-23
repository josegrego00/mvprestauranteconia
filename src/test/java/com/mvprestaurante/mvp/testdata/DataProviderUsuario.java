package com.mvprestaurante.mvp.testdata;

import java.util.List;

import com.mvprestaurante.mvp.DTO.UsuarioRequestDTO;
import com.mvprestaurante.mvp.DTO.UsuarioResponseDTO;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Usuario;

public class DataProviderUsuario {

    private static final Empresa EMPRESA_FICTICIA = Empresa.builder()
            .id(1L)
            .subdominio("testempresa")
            .nombreEmpresa("Test Empresa")
            .email("test@empresa.com")
            .telefono("1234567890")
            .plan("BASICO")
            .activa(true)
            .build();

    public static Usuario unUsuario() {
        return Usuario.builder()
                .nombre("Usuario Test")
                .nombreUsuario("testuser")
                .contrasenna("password123")
                .rol("CAJERO")
                .email("test@usuario.com")
                .estaActivo(true)
                .esSuperadmin(false)
                .build();
    }

    public static Usuario unUsuarioConId() {
        return Usuario.builder()
                .id(1L)
                .nombre("Usuario Test")
                .nombreUsuario("testuser")
                .contrasenna("$2a$10$encodedpassword")
                .rol("CAJERO")
                .email("test@usuario.com")
                .estaActivo(true)
                .esSuperadmin(false)
                .empresa(EMPRESA_FICTICIA)
                .build();
    }

    public static Usuario unUsuarioActivo() {
        return Usuario.builder()
                .id(1L)
                .nombre("Usuario Activo")
                .nombreUsuario("usuarioactivo")
                .contrasenna("$2a$10$encodedpassword")
                .rol("ADMIN")
                .email("activo@usuario.com")
                .estaActivo(true)
                .esSuperadmin(false)
                .empresa(EMPRESA_FICTICIA)
                .build();
    }

    public static Usuario unUsuarioInactivo() {
        return Usuario.builder()
                .id(2L)
                .nombre("Usuario Inactivo")
                .nombreUsuario("usuarioinactivo")
                .contrasenna("$2a$10$encodedpassword")
                .rol("CAJERO")
                .email("inactivo@usuario.com")
                .estaActivo(false)
                .esSuperadmin(false)
                .empresa(EMPRESA_FICTICIA)
                .build();
    }

    public static UsuarioRequestDTO unUsuarioRequestDTO() {
        return UsuarioRequestDTO.builder()
                .nombre("Nuevo Usuario")
                .nombreUsuario("nuevousuario")
                .contrasenna("password456")
                .rol("CAJERO")
                .email("nuevo@usuario.com")
                .estaActivo(true)
                .build();
    }

    public static UsuarioRequestDTO unUsuarioRequestDTOConId() {
        return UsuarioRequestDTO.builder()
                .nombre("Usuario Actualizado")
                .nombreUsuario("usuarioactualizado")
                .contrasenna("newpassword789")
                .rol("ADMIN")
                .email("actualizado@usuario.com")
                .build();
    }

    public static UsuarioResponseDTO unUsuarioResponseDTO() {
        return UsuarioResponseDTO.builder()
                .id(1L)
                .nombre("Usuario Test")
                .nombreUsuario("testuser")
                .rol("CAJERO")
                .estaActivo(true)
                .email("test@usuario.com")
                .esSuperadmin(false)
                .empresaId(1L)
                .nombreEmpresa("Test Empresa")
                .build();
    }

    public static List<Usuario> listaUsuarios() {
        return List.of(
                Usuario.builder()
                        .id(1L)
                        .nombre("Usuario 1")
                        .nombreUsuario("user1")
                        .contrasenna("$2a$10$encoded1")
                        .rol("ADMIN")
                        .email("user1@test.com")
                        .estaActivo(true)
                        .esSuperadmin(false)
                        .empresa(EMPRESA_FICTICIA)
                        .build(),
                Usuario.builder()
                        .id(2L)
                        .nombre("Usuario 2")
                        .nombreUsuario("user2")
                        .contrasenna("$2a$10$encoded2")
                        .rol("CAJERO")
                        .email("user2@test.com")
                        .estaActivo(true)
                        .esSuperadmin(false)
                        .empresa(EMPRESA_FICTICIA)
                        .build()
        );
    }

    public static List<UsuarioResponseDTO> listaUsuariosDTO() {
        return List.of(
                UsuarioResponseDTO.builder()
                        .id(1L)
                        .nombre("Usuario 1")
                        .nombreUsuario("user1")
                        .rol("ADMIN")
                        .estaActivo(true)
                        .email("user1@test.com")
                        .esSuperadmin(false)
                        .empresaId(1L)
                        .nombreEmpresa("Test Empresa")
                        .build(),
                UsuarioResponseDTO.builder()
                        .id(2L)
                        .nombre("Usuario 2")
                        .nombreUsuario("user2")
                        .rol("CAJERO")
                        .estaActivo(true)
                        .email("user2@test.com")
                        .esSuperadmin(false)
                        .empresaId(1L)
                        .nombreEmpresa("Test Empresa")
                        .build()
        );
    }
}