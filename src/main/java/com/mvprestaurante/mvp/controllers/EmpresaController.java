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

import com.mvprestaurante.mvp.DTO.EmpresaDTO;
import com.mvprestaurante.mvp.services.EmpresaService;
import com.mvprestaurante.mvp.utils.AuditLogger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/empresas")
@RequiredArgsConstructor
@Tag(name = "Empresas", description = "Gestión de empresas del sistema")
public class EmpresaController {

    private final EmpresaService empresaService;
    private final AuditLogger auditLogger;

    @Operation(summary = "Listar todas las empresas", description = "Retorna una lista de todas las empresas registradas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de empresas obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<EmpresaDTO>> listarTodas() {
        List<EmpresaDTO> empresas = empresaService.listarTodas();
        auditLogger.logListar("Empresa", empresas.size());
        return ResponseEntity.ok(empresas);
    }

    @Operation(summary = "Buscar empresa por ID", description = "Retorna una empresa específica por su identificador")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmpresaDTO> buscarPorId(
            @Parameter(description = "ID de la empresa") @PathVariable Long id) {
        EmpresaDTO empresa = empresaService.buscarPorId(id);
        auditLogger.logBuscar("Empresa", String.valueOf(id));
        return ResponseEntity.ok(empresa);
    }

    @Operation(summary = "Buscar empresa por subdominio", description = "Retorna una empresa específica por su subdominio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
        @ApiResponse(responseCode = "404", description = "Subdominio no encontrado", content = @Content)
    })
    @GetMapping("/subdominio/{subdominio}")
    public ResponseEntity<EmpresaDTO> buscarPorSubdominio(
            @Parameter(description = "Subdominio de la empresa") @PathVariable String subdominio) {
        EmpresaDTO empresa = empresaService.buscarPorSubdominio(subdominio);
        auditLogger.logBuscar("Empresa", "Subdominio: " + subdominio);
        return ResponseEntity.ok(empresa);
    }

    @Operation(summary = "Registrar nueva empresa", description = "Crea una nueva empresa en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Empresa creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o subdominio duplicado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EmpresaDTO> guardar(@Valid @RequestBody EmpresaDTO empresaDTO) {
        String subdominio = empresaDTO.getSubdominio().toLowerCase().trim();
        empresaDTO.setSubdominio(subdominio);
        EmpresaDTO empresa = empresaService.guardar(empresaDTO);
        auditLogger.logCrear("Empresa", "Subdominio: " + subdominio);
        return ResponseEntity.status(HttpStatus.CREATED).body(empresa);
    }

    @Operation(summary = "Actualizar empresa", description = "Actualiza los datos de una empresa existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<EmpresaDTO> actualizar(
            @Parameter(description = "ID de la empresa") @PathVariable Long id,
            @Valid @RequestBody EmpresaDTO empresaDTO) {
        EmpresaDTO empresa = empresaService.actualizar(id, empresaDTO);
        auditLogger.logActualizar("Empresa", String.valueOf(id));
        return ResponseEntity.ok(empresa);
    }

    @Operation(summary = "Eliminar empresa", description = "Desactiva una empresa (eliminación lógica)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Empresa eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la empresa") @PathVariable Long id) {
        empresaService.eliminar(id);
        auditLogger.logEliminar("Empresa", String.valueOf(id));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activar empresa", description = "Activa una empresa previamente desactivada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa activada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada", content = @Content)
    })
    @PostMapping("/{id}/activar")
    public ResponseEntity<EmpresaDTO> activar(
            @Parameter(description = "ID de la empresa") @PathVariable Long id) {
        EmpresaDTO empresa = empresaService.actualizarEstadoActivo(id, true);
        auditLogger.logActivar("Empresa", String.valueOf(id));
        return ResponseEntity.ok(empresa);
    }

    @Operation(summary = "Desactivar empresa", description = "Desactiva una empresa activa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa desactivada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada", content = @Content)
    })
    @PostMapping("/{id}/desactivar")
    public ResponseEntity<EmpresaDTO> desactivar(
            @Parameter(description = "ID de la empresa") @PathVariable Long id) {
        EmpresaDTO empresa = empresaService.actualizarEstadoActivo(id, false);
        auditLogger.logDesactivar("Empresa", String.valueOf(id));
        return ResponseEntity.ok(empresa);
    }
}