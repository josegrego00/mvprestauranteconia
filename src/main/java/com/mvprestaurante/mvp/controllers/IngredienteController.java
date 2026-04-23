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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/ingredientes")
@RequiredArgsConstructor
@Tag(name = "Ingredientes", description = "Gestión de ingredientes de la empresa")
public class IngredienteController {

    private final IngredienteService ingredienteService;
    private final AuditLogger auditLogger;

    @GetMapping
    @Operation(summary = "Listar ingredientes", description = "Lista todos los ingredientes activos de la empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtained successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content)
    })
    public String listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<IngredienteDTO> ingredientesPage;

        if (search != null && !search.isEmpty()) {
            ingredientesPage = ingredienteService.buscarPorNombre(search, pageable);
            model.addAttribute("search", search);
        } else {
            ingredientesPage = ingredienteService.listarActivos(pageable);
        }

        model.addAttribute("ingredientes", ingredientesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ingredientesPage.getTotalPages());
        model.addAttribute("totalItems", ingredientesPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "ingredientes/lista";
    }

    @GetMapping("/nuevo")
    @Operation(summary = "Formulario nuevo ingrediente", description = "Muestra el formulario para crear un nuevo ingrediente")
    public String nuevo(Model model) {
        model.addAttribute("ingrediente", new IngredienteDTO());
        model.addAttribute("unidades", new String[] { "kg", "g", "l", "ml", "unidad", "docena" });
        return "ingredientes/formulario";
    }

    @PostMapping("/guardar")
    @Operation(summary = "Guardar ingrediente", description = "Crea o actualiza un ingrediente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ingrediente created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content)
    })
    public String guardar(@ModelAttribute @Valid IngredienteDTO ingredienteDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("ingrediente", ingredienteDTO);
            model.addAttribute("unidades", new String[] { "kg", "g", "l", "ml", "unidad", "docena" });
            return "ingredientes/formulario";
        }

        if (ingredienteDTO.getId() != null) {
            IngredienteDTO actualizado = ingredienteService.actualizar(ingredienteDTO.getId(), ingredienteDTO);
            if (actualizado != null) {
                redirectAttributes.addFlashAttribute("success", "Ingrediente actualizado exitosamente");
                auditLogger.logActualizar("Ingrediente", ingredienteDTO.getId().toString());
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo actualizar el ingrediente");
                return "redirect:/ingredientes/editar/" + ingredienteDTO.getId();
            }
        } else {
            IngredienteDTO guardado = ingredienteService.guardar(ingredienteDTO);
            redirectAttributes.addFlashAttribute("success", "Ingrediente guardado exitosamente");
            auditLogger.logCrear("Ingrediente", guardado.getId().toString());
        }

        return "redirect:/ingredientes";
    }

    @GetMapping("/editar/{id}")
    @Operation(summary = "Formulario editar ingrediente", description = "Muestra el formulario para editar un ingrediente")
    public String editar(@Parameter(description = "ID del ingrediente") @PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<IngredienteDTO> ingrediente = ingredienteService.obtenerPorId(id);
        
        return ingrediente.map(i -> {
            model.addAttribute("ingrediente", i);
            model.addAttribute("unidades", new String[] { "kg", "g", "l", "ml", "unidad", "docena" });
            return "ingredientes/formulario";
        })
        .orElseGet(() -> {
            redirectAttributes.addFlashAttribute("error", "Ingrediente no encontrado");
            return "redirect:/ingredientes";
        });
    }

    @GetMapping("/eliminar/{id}")
    @Operation(summary = "Eliminar ingrediente", description = "Desactiva un ingrediente (eliminación lógica)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Ingrediente deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Ingrediente not found", content = @Content)
    })
    public String eliminar(@Parameter(description = "ID del ingrediente") @PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ingredienteService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Ingrediente eliminado correctamente");
            auditLogger.logDesactivar("Ingrediente", id.toString());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ingredientes";
    }

    @GetMapping("/ver/{id}")
    @Operation(summary = "Ver ingrediente", description = "Muestra los detalles de un ingrediente")
    public String ver(@Parameter(description = "ID del ingrediente") @PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<IngredienteDTO> ingrediente = ingredienteService.obtenerPorId(id);
        
        return ingrediente.map(i -> {
            model.addAttribute("ingrediente", i);
            return "ingredientes/ver";
        })
        .orElseGet(() -> {
            redirectAttributes.addFlashAttribute("error", "Ingrediente no encontrado");
            return "redirect:/ingredientes";
        });
    }

    @GetMapping("/api/listar")
    @ResponseBody
    @Operation(summary = "API: Listar ingredientes", description = "Lista ingredientes activos para API REST")
    public ResponseEntity<Page<IngredienteDTO>> listarApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<IngredienteDTO> ingredientes = ingredienteService.listarActivos(pageable);
        return ResponseEntity.ok(ingredientes);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    @Operation(summary = "API: Buscar ingrediente por ID", description = "Busca un ingrediente por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ingrediente found"),
        @ApiResponse(responseCode = "404", description = "Ingrediente not found", content = @Content)
    })
    public ResponseEntity<IngredienteDTO> buscarPorIdApi(
            @Parameter(description = "ID del ingrediente") @PathVariable Long id) {
        Optional<IngredienteDTO> ingrediente = ingredienteService.obtenerPorId(id);
        return ingrediente.map(i -> {
                auditLogger.logBuscar("Ingrediente", id.toString());
                return ResponseEntity.ok(i);
            })
            .orElse(ResponseEntity.notFound().build());
    }
}