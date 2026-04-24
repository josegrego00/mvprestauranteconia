package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.IngredienteDTO;
import com.mvprestaurante.mvp.services.IngredienteService;
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


import java.util.Optional;

@RestController
@RequestMapping("/api/v1/ingredientes")
@RequiredArgsConstructor
@Tag(name = "Ingredientes", description = "Gestión de ingredientes de la empresa - API REST")
public class IngredienteController {

    private final IngredienteService ingredienteService;
    private final AuditLogger auditLogger;

    @GetMapping
    @Operation(summary = "API: Listar ingredientes", description = "Lista todos los ingredientes activos de la empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content)
    })
    public ResponseEntity<Page<IngredienteDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<IngredienteDTO> ingredientesPage;

        if (search != null && !search.isEmpty()) {
            ingredientesPage = ingredienteService.buscarPorNombre(search, pageable);
        } else {
            ingredientesPage = ingredienteService.listarActivos(pageable);
        }

        auditLogger.logListar("Ingrediente", ingredientesPage.getContent().size());
        return ResponseEntity.ok(ingredientesPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "API: Buscar ingrediente por ID", description = "Busca un ingrediente por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ingrediente encontrado"),
        @ApiResponse(responseCode = "404", description = "Ingrediente no encontrado", content = @Content)
    })
    public ResponseEntity<IngredienteDTO> buscarPorId(
            @Parameter(description = "ID del ingrediente") @PathVariable Long id) {
        Optional<IngredienteDTO> ingrediente = ingredienteService.obtenerPorId(id);
        return ingrediente.map(i -> {
                auditLogger.logBuscar("Ingrediente", id.toString());
                return ResponseEntity.ok(i);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "API: Guardar ingrediente", description = "Crea un nuevo ingrediente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ingrediente creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    public ResponseEntity<IngredienteDTO> guardar(@Valid @RequestBody IngredienteDTO ingredienteDTO) {
        IngredienteDTO guardado = ingredienteService.guardar(ingredienteDTO);
        auditLogger.logCrear("Ingrediente", guardado.getId().toString());
        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "API: Actualizar ingrediente", description = "Actualiza un ingrediente existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ingrediente actualizado"),
        @ApiResponse(responseCode = "404", description = "Ingrediente no encontrado", content = @Content)
    })
    public ResponseEntity<IngredienteDTO> actualizar(
            @Parameter(description = "ID del ingrediente") @PathVariable Long id,
            @Valid @RequestBody IngredienteDTO ingredienteDTO) {
        IngredienteDTO actualizado = ingredienteService.actualizar(id, ingredienteDTO);
        if (actualizado != null) {
            auditLogger.logActualizar("Ingrediente", id.toString());
            return ResponseEntity.ok(actualizado);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "API: Eliminar ingrediente", description = "Desactiva un ingrediente (eliminación lógica)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Ingrediente desactivado"),
        @ApiResponse(responseCode = "404", description = "Ingrediente no encontrado", content = @Content)
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del ingrediente") @PathVariable Long id) {
        ingredienteService.eliminar(id);
        auditLogger.logDesactivar("Ingrediente", id.toString());
        return ResponseEntity.noContent().build();
    }
}