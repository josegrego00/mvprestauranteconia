package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.DetalleRecetaDTO;
import com.mvprestaurante.mvp.DTO.IngredienteDTO;
import com.mvprestaurante.mvp.services.DetalleRecetaService;
import com.mvprestaurante.mvp.services.IngredienteService;
import com.mvprestaurante.mvp.services.RecetaService;
import com.mvprestaurante.mvp.utils.AuditLogger;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Controller
@RequestMapping("/recetas/{recetaId}/ingredientes")
@RequiredArgsConstructor
@Tag(name = "DetalleReceta", description = "Gestión de ingredientes en recetas")
public class DetalleRecetaController {

    private final DetalleRecetaService detalleRecetaService;
    private final RecetaService recetaService;
    private final IngredienteService ingredienteService;
    private final AuditLogger auditLogger;

    @GetMapping
    @Operation(summary = "Listar ingredientes de receta", description = "Lista todos los ingredientes de una receta")
    public String listar(@Parameter(description = "ID de la receta") @PathVariable Long recetaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
            Page<DetalleRecetaDTO> detalles = detalleRecetaService.listarPorRecetaId(recetaId, pageable);

            model.addAttribute("detalles", detalles.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", detalles.getTotalPages());
            model.addAttribute("totalItems", detalles.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("recetaId", recetaId);

            auditLogger.logListar("DetalleReceta", detalles.getContent().size());
            return "recetas/ingredientes/lista";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/recetas";
        }
    }

    @GetMapping("/nuevo")
    @Operation(summary = "Formulario nuevo ingrediente", description = "Muestra el formulario para agregar un ingrediente a la receta")
    public String nuevo(@Parameter(description = "ID de la receta") @PathVariable Long recetaId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            recetaService.obtenerPorId(recetaId);

            Page<IngredienteDTO> ingredientes = ingredienteService.listarActivos(PageRequest.of(0, 100));

            DetalleRecetaDTO detalle = DetalleRecetaDTO.builder()
                    .recetaId(recetaId)
                    .build();
            model.addAttribute("detalle", detalle);
            model.addAttribute("ingredientes", ingredientes.getContent());
            model.addAttribute("recetaId", recetaId);

            return "recetas/ingredientes/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/recetas/" + recetaId + "/ingredientes";
        }
    }

    @PostMapping("/guardar")
    @Operation(summary = "Guardar ingrediente", description = "Agrega un ingrediente a la receta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ingrediente agregado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    public String guardar(@PathVariable Long recetaId,
            @ModelAttribute @Valid DetalleRecetaDTO detalle,
            RedirectAttributes redirectAttributes) {
        try {
            detalle.setRecetaId(recetaId);
            DetalleRecetaDTO guardado = detalleRecetaService.guardar(detalle);
            redirectAttributes.addFlashAttribute("success", "Ingrediente agregado correctamente");
            auditLogger.logCrear("DetalleReceta", String.valueOf(guardado.getId()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/recetas/" + recetaId + "/ingredientes/nuevo";
        }
        return "redirect:/recetas/" + recetaId + "/ingredientes";
    }

    @GetMapping("/editar/{id}")
    @Operation(summary = "Formulario editar ingrediente", description = "Muestra el formulario para editar un ingrediente de la receta")
    public String editar(@PathVariable Long recetaId,
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            DetalleRecetaDTO detalle = detalleRecetaService.obtenerPorId(id);
            model.addAttribute("detalle", detalle);

            Page<IngredienteDTO> ingredientes = ingredienteService.listarActivos(PageRequest.of(0, 100));
            model.addAttribute("ingredientes", ingredientes.getContent());
            model.addAttribute("recetaId", recetaId);

            return "recetas/ingredientes/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/recetas/" + recetaId + "/ingredientes";
        }
    }

    @PostMapping("/actualizar/{id}")
    @Operation(summary = "Actualizar ingrediente", description = "Actualiza un ingrediente de la receta")
    public String actualizar(@PathVariable Long recetaId,
            @PathVariable Long id,
            @ModelAttribute @Valid DetalleRecetaDTO detalle,
            RedirectAttributes redirectAttributes) {
        try {
            detalleRecetaService.actualizar(id, detalle);
            redirectAttributes.addFlashAttribute("success", "Ingrediente actualizado correctamente");
            auditLogger.logActualizar("DetalleReceta", String.valueOf(id));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/recetas/" + recetaId + "/ingredientes";
    }

    @GetMapping("/eliminar/{id}")
    @Operation(summary = "Eliminar ingrediente", description = "Elimina un ingrediente de la receta")
    public String eliminar(@PathVariable Long recetaId,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            detalleRecetaService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Ingrediente eliminado correctamente");
            auditLogger.logEliminar("DetalleReceta", String.valueOf(id));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/recetas/" + recetaId + "/ingredientes";
    }

    @GetMapping("/api/listar")
    @ResponseBody
    @Operation(summary = "API: Listar ingredientes", description = "Lista ingredientes de una receta para API REST")
    public ResponseEntity<Page<DetalleRecetaDTO>> listarApi(
            @PathVariable Long recetaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<DetalleRecetaDTO> detalles = detalleRecetaService.listarPorRecetaId(recetaId, pageable);
        auditLogger.logListar("DetalleReceta", detalles.getContent().size());
        return ResponseEntity.ok(detalles);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    @Operation(summary = "API: Buscar ingrediente por ID", description = "Busca un ingrediente de receta por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle encontrado"),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content)
    })
    public ResponseEntity<DetalleRecetaDTO> buscarPorIdApi(
            @PathVariable Long recetaId,
            @PathVariable Long id) {
        DetalleRecetaDTO detalle = detalleRecetaService.obtenerPorId(id);
        auditLogger.logBuscar("DetalleReceta", String.valueOf(id));
        return ResponseEntity.ok(detalle);
    }

    @PostMapping("/api/guardar")
    @ResponseBody
    @Operation(summary = "API: Guardar ingrediente", description = "Agrega un ingrediente a la receta via API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ingrediente agregado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    public ResponseEntity<DetalleRecetaDTO> guardarApi(
            @PathVariable Long recetaId,
            @RequestBody DetalleRecetaDTO detalle) {
        detalle.setRecetaId(recetaId);
        DetalleRecetaDTO guardado = detalleRecetaService.guardar(detalle);
        auditLogger.logCrear("DetalleReceta", String.valueOf(guardado.getId()));
        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/api/actualizar/{id}")
    @ResponseBody
    @Operation(summary = "API: Actualizar ingrediente", description = "Actualiza un ingrediente de la receta via API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ingrediente actualizado"),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content)
    })
    public ResponseEntity<DetalleRecetaDTO> actualizarApi(
            @PathVariable Long recetaId,
            @PathVariable Long id,
            @RequestBody DetalleRecetaDTO detalle) {
        DetalleRecetaDTO actualizado = detalleRecetaService.actualizar(id, detalle);
        auditLogger.logActualizar("DetalleReceta", String.valueOf(id));
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/api/eliminar/{id}")
    @ResponseBody
    @Operation(summary = "API: Eliminar ingrediente", description = "Elimina un ingrediente de la receta via API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Ingrediente eliminado"),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content)
    })
    public ResponseEntity<Void> eliminarApi(
            @PathVariable Long recetaId,
            @PathVariable Long id) {
        detalleRecetaService.eliminar(id);
        auditLogger.logEliminar("DetalleReceta", String.valueOf(id));
        return ResponseEntity.noContent().build();
    }
}