package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.RecetaDTO;
import com.mvprestaurante.mvp.services.ProductoService;
import com.mvprestaurante.mvp.services.RecetaService;
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
@RequestMapping("/api/v1/recetas")
@RequiredArgsConstructor
@Tag(name = "Recetas", description = "Gestión de recetas de la empresa - API REST")
public class RecetaController {

    private final RecetaService recetaService;
    private final ProductoService productoService;
    private final AuditLogger auditLogger;

    @GetMapping
    @Operation(summary = "API: Listar recetas", description = "Lista todas las recetas activas de la empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content)
    })
    public ResponseEntity<Page<RecetaDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<RecetaDTO> recetasPage;

        if (search != null && !search.isEmpty()) {
            recetasPage = recetaService.buscarPorNombre(search, pageable);
        } else {
            recetasPage = recetaService.listarActivas(pageable);
        }

        auditLogger.logListar("Receta", recetasPage.getContent().size());
        return ResponseEntity.ok(recetasPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "API: Buscar receta por ID", description = "Busca una receta por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Receta encontrada"),
        @ApiResponse(responseCode = "404", description = "Receta no encontrada", content = @Content)
    })
    public ResponseEntity<RecetaDTO> buscarPorId(@PathVariable Long id) {
        RecetaDTO receta = recetaService.obtenerPorId(id);
        auditLogger.logBuscar("Receta", String.valueOf(id));
        return ResponseEntity.ok(receta);
    }

    @PostMapping
    @Operation(summary = "API: Guardar receta", description = "Crea una nueva receta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Receta creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    public ResponseEntity<RecetaDTO> guardar(
            @Valid @RequestBody RecetaDTO receta,
            @RequestParam(required = false) Long[] ingredientesIds,
            @RequestParam(required = false) BigDecimal[] cantidades) {
        RecetaDTO recetaGuardada = recetaService.crear(receta, ingredientesIds, cantidades);
        auditLogger.logCrear("Receta", String.valueOf(recetaGuardada.getId()));
        return ResponseEntity.ok(recetaGuardada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "API: Actualizar receta", description = "Actualiza una receta existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Receta actualizada"),
        @ApiResponse(responseCode = "404", description = "Receta no encontrada", content = @Content)
    })
    public ResponseEntity<RecetaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody RecetaDTO receta,
            @RequestParam(required = false) Long[] ingredientesIds,
            @RequestParam(required = false) BigDecimal[] cantidades) {
        RecetaDTO recetaActualizada = recetaService.actualizar(id, receta, ingredientesIds, cantidades);
        auditLogger.logActualizar("Receta", String.valueOf(id));
        return ResponseEntity.ok(recetaActualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "API: Eliminar receta", description = "Desactiva una receta (eliminación lógica)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Receta desactivada"),
        @ApiResponse(responseCode = "404", description = "Receta no encontrada", content = @Content)
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            recetaService.eliminar(id);
            auditLogger.logDesactivar("Receta", String.valueOf(id));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/activar")
    @Operation(summary = "API: Activar receta", description = "Activa una receta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Receta activada"),
        @ApiResponse(responseCode = "404", description = "Receta no encontrada", content = @Content)
    })
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        try {
            recetaService.activar(id);
            auditLogger.logActivar("Receta", String.valueOf(id));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/stock")
    @Operation(summary = "API: Stock disponible", description = "Retorna el stock disponible de una receta")
    public ResponseEntity<BigDecimal> obtenerStock(@PathVariable Long id) {
        BigDecimal stock = recetaService.calcularStockDisponible(id);
        return ResponseEntity.ok(stock);
    }
}