package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.DetalleRecetaDTO;
import com.mvprestaurante.mvp.services.DetalleRecetaService;
import com.mvprestaurante.mvp.utils.AuditLogger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/recetas/{recetaId}/ingredientes")
@RequiredArgsConstructor
@Tag(name = "DetalleReceta", description = "Gestión de ingredientes en recetas - API REST")
public class DetalleRecetaController {

    private final DetalleRecetaService detalleRecetaService;
    private final AuditLogger auditLogger;

    @GetMapping
    @Operation(summary = "API: Listar ingredientes", description = "Lista todos los ingredientes de una receta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content)
    })
    public ResponseEntity<Page<DetalleRecetaDTO>> listar(
            @Parameter(description = "ID de la receta") @PathVariable Long recetaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<DetalleRecetaDTO> detalles = detalleRecetaService.listarPorRecetaId(recetaId, pageable);
        auditLogger.logListar("DetalleReceta", detalles.getContent().size());
        return ResponseEntity.ok(detalles);
    }

    @GetMapping("/{id}")
    @Operation(summary = "API: Buscar ingrediente por ID", description = "Busca un ingrediente de receta por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle encontrado"),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content)
    })
    public ResponseEntity<DetalleRecetaDTO> buscarPorId(
            @PathVariable Long recetaId,
            @Parameter(description = "ID del detalle") @PathVariable Long id) {
        DetalleRecetaDTO detalle = detalleRecetaService.obtenerPorId(id);
        auditLogger.logBuscar("DetalleReceta", String.valueOf(id));
        return ResponseEntity.ok(detalle);
    }

    @PostMapping
    @Operation(summary = "API: Guardar ingrediente", description = "Agrega un ingrediente a la receta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ingrediente agregado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    public ResponseEntity<DetalleRecetaDTO> guardar(
            @PathVariable Long recetaId,
            @Valid @RequestBody DetalleRecetaDTO detalle) {
        detalle.setRecetaId(recetaId);
        DetalleRecetaDTO guardado = detalleRecetaService.guardar(detalle);
        auditLogger.logCrear("DetalleReceta", String.valueOf(guardado.getId()));
        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "API: Actualizar ingrediente", description = "Actualiza un ingrediente de la receta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ingrediente actualizado"),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content)
    })
    public ResponseEntity<DetalleRecetaDTO> actualizar(
            @PathVariable Long recetaId,
            @Parameter(description = "ID del detalle") @PathVariable Long id,
            @Valid @RequestBody DetalleRecetaDTO detalle) {
        DetalleRecetaDTO actualizado = detalleRecetaService.actualizar(id, detalle);
        auditLogger.logActualizar("DetalleReceta", String.valueOf(id));
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "API: Eliminar ingrediente", description = "Elimina un ingrediente de la receta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Ingrediente eliminado"),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content)
    })
    public ResponseEntity<Void> eliminar(
            @PathVariable Long recetaId,
            @Parameter(description = "ID del detalle") @PathVariable Long id) {
        detalleRecetaService.eliminar(id);
        auditLogger.logEliminar("DetalleReceta", String.valueOf(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stock")
    @Operation(summary = "API: Stock mínimo", description = "Retorna el stock mínimo de la receta basado en ingredientes")
    public ResponseEntity<BigDecimal> obtenerStockMinimo(@PathVariable Long recetaId) {
        BigDecimal stockMinimo = detalleRecetaService.calcularStockMinimo(recetaId);
        return ResponseEntity.ok(stockMinimo);
    }
}