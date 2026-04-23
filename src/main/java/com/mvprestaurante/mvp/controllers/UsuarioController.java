package com.mvprestaurante.mvp.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mvprestaurante.mvp.DTO.UsuarioRequestDTO;
import com.mvprestaurante.mvp.DTO.UsuarioResponseDTO;
import com.mvprestaurante.mvp.services.UsuarioService;
import com.mvprestaurante.mvp.utils.AuditLogger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios por empresa")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuditLogger auditLogger;

    @Operation(summary = "Listar usuarios", description = "Retorna una lista de todos los usuarios de la empresa actual")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        List<UsuarioResponseDTO> usuarios = usuarioService.listarUsuarios();
        auditLogger.logListar("Usuario", usuarios.size());
        return ResponseEntity.ok(usuarios);
    }

    @Operation(summary = "Buscar usuario por ID", description = "Retorna un usuario específico por su identificador")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(id);
        auditLogger.logBuscar("Usuario", String.valueOf(id));
        return ResponseEntity.ok(usuario);
    }

    @Operation(summary = "Registrar nuevo usuario", description = "Crea un nuevo usuario en la empresa actual")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o nombre de usuario duplicado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> guardar(
            @Valid @RequestBody UsuarioRequestDTO usuarioDTO) {
        UsuarioResponseDTO usuario = usuarioService.guardarUsuario(usuarioDTO);
        auditLogger.logCrear("Usuario", usuario.getId().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO usuarioDTO) {
        UsuarioResponseDTO usuario = usuarioService.actualizarUsuario(id, usuarioDTO);
        auditLogger.logActualizar("Usuario", String.valueOf(id));
        return ResponseEntity.ok(usuario);
    }

    @Operation(summary = "Eliminar usuario", description = "Desactiva un usuario (eliminación lógica)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        auditLogger.logEliminar("Usuario", String.valueOf(id));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activar usuario", description = "Activa un usuario previamente desactivado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario activado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @PostMapping("/{id}/activar")
    public ResponseEntity<UsuarioResponseDTO> activar(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        UsuarioResponseDTO usuario = usuarioService.activarUsuario(id);
        auditLogger.logActivar("Usuario", String.valueOf(id));
        return ResponseEntity.ok(usuario);
    }
}