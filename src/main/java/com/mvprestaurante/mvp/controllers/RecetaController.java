package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.IngredienteDTO;
import com.mvprestaurante.mvp.DTO.RecetaDTO;
import com.mvprestaurante.mvp.mapper.IngredienteMapper;
import com.mvprestaurante.mvp.mapper.RecetaMapper;
import com.mvprestaurante.mvp.services.IngredienteService;
import com.mvprestaurante.mvp.services.ProductoService;
import com.mvprestaurante.mvp.services.RecetaService;
import com.mvprestaurante.mvp.utils.AuditLogger;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;

@Controller
@RequestMapping("/recetas")
@RequiredArgsConstructor
@Tag(name = "Recetas", description = "Gestión de recetas de la empresa")
public class RecetaController {

    private final RecetaService recetaService;
    private final RecetaMapper recetaMapper;
    private final IngredienteService ingredienteService;
    private final IngredienteMapper ingredienteMapper;
    private final ProductoService productoService;
    private final AuditLogger auditLogger;

    @GetMapping
    @Operation(summary = "Listar recetas", description = "Lista todas las recetas activas de la empresa")
    public String listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<RecetaDTO> recetasPage;

        if (search != null && !search.isEmpty()) {
            recetasPage = recetaService.buscarPorNombre(search, pageable);
            model.addAttribute("search", search);
        } else {
            recetasPage = recetaService.listarActivas(pageable);
        }

        model.addAttribute("recetas", recetasPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", recetasPage.getTotalPages());
        model.addAttribute("totalItems", recetasPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "recetas/lista";
    }

    @GetMapping("/nueva")
    @Operation(summary = "Formulario nueva receta", description = "Muestra el formulario para crear una nueva receta")
    public String nuevaReceta(@RequestParam(required = false) Long productoId, Model model) {
        RecetaDTO receta = new RecetaDTO();
        model.addAttribute("receta", receta);
        model.addAttribute("productoId", productoId);

        Page<IngredienteDTO> ingredientesPage = ingredienteService.listarActivos(PageRequest.of(0, 100));
        model.addAttribute("ingredientes", ingredientesPage.getContent());

        return "recetas/formulario";
    }

    @GetMapping("/editar/{id}")
    @Operation(summary = "Formulario editar receta", description = "Muestra el formulario para editar una receta")
    public String editarReceta(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            RecetaDTO receta = recetaService.obtenerPorId(id);
            model.addAttribute("receta", receta);

            Page<IngredienteDTO> ingredientesPage = ingredienteService.listarActivos(PageRequest.of(0, 100));
            model.addAttribute("ingredientes", ingredientesPage.getContent());

            return "recetas/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar la receta: " + e.getMessage());
            return "redirect:/recetas";
        }
    }

    @PostMapping("/guardar")
    @Operation(summary = "Guardar receta", description = "Crea o actualiza una receta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Receta guardada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    public String guardar(@ModelAttribute @Valid RecetaDTO receta,
            @RequestParam(required = false) Long[] ingredientesIds,
            @RequestParam(required = false) BigDecimal[] cantidades,
            @RequestParam(required = false) Long productoId,
            RedirectAttributes redirectAttributes) {
        try {
            RecetaDTO recetaGuardada;

            if (receta.getId() != null) {
                recetaGuardada = recetaService.actualizar(receta.getId(), receta, ingredientesIds, cantidades);
                redirectAttributes.addFlashAttribute("success", "Receta actualizada exitosamente");
                auditLogger.logActualizar("Receta", String.valueOf(receta.getId()));
            } else {
                recetaGuardada = recetaService.crear(receta, ingredientesIds, cantidades);
                redirectAttributes.addFlashAttribute("success", "Receta guardada exitosamente");
                auditLogger.logCrear("Receta", String.valueOf(recetaGuardada.getId()));
            }

            if (productoId != null) {
                productoService.asociarReceta(productoId, recetaGuardada.getId());
                redirectAttributes.addFlashAttribute("success", "Receta creada y asociada al producto");
                return "redirect:/productos/editar/" + productoId;
            }

            return "redirect:/recetas";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            if (receta.getId() != null) {
                return "redirect:/recetas/editar/" + receta.getId();
            }
            return "redirect:/recetas/nueva";
        }
    }

    @GetMapping("/eliminar/{id}")
    @Operation(summary = "Eliminar receta", description = "Desactiva una receta (eliminación lógica)")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean eliminado = recetaService.eliminar(id);
            if (eliminado) {
                redirectAttributes.addFlashAttribute("success", "Receta eliminada correctamente");
                auditLogger.logDesactivar("Receta", String.valueOf(id));
            } else {
                redirectAttributes.addFlashAttribute("error", "Error al eliminar la receta");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/recetas";
    }

    @GetMapping("/ver/{id}")
    @Operation(summary = "Ver receta", description = "Muestra los detalles de una receta")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            RecetaDTO receta = recetaService.obtenerPorId(id);
            model.addAttribute("receta", receta);
            auditLogger.logBuscar("Receta", String.valueOf(id));
            return "recetas/ver";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Receta no encontrada");
            return "redirect:/recetas";
        }
    }

    @GetMapping("/stock/{id}")
    @ResponseBody
    @Operation(summary = "Stock disponible", description = "Retorna el stock disponible de una receta")
    public BigDecimal obtenerStock(@PathVariable Long id) {
        return recetaService.calcularStockDisponible(id);
    }

    @GetMapping("/api/listar")
    @ResponseBody
    @Operation(summary = "API: Listar recetas", description = "Lista recetas activas para API REST")
    public ResponseEntity<Page<RecetaDTO>> listarApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<RecetaDTO> recetas = recetaService.listarActivas(pageable);
        auditLogger.logListar("Receta", recetas.getContent().size());
        return ResponseEntity.ok(recetas);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    @Operation(summary = "API: Buscar receta por ID", description = "Busca una receta por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Receta encontrada"),
        @ApiResponse(responseCode = "404", description = "Receta no encontrada", content = @Content)
    })
    public ResponseEntity<RecetaDTO> buscarPorIdApi(@PathVariable Long id) {
        RecetaDTO receta = recetaService.obtenerPorId(id);
        auditLogger.logBuscar("Receta", String.valueOf(id));
        return ResponseEntity.ok(receta);
    }

    @PostMapping("/api/guardar")
    @ResponseBody
    @Operation(summary = "API: Guardar receta", description = "Crea una nueva receta via API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Receta creada"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    public ResponseEntity<RecetaDTO> guardarApi(@RequestBody RecetaDTO receta) {
        RecetaDTO recetaGuardada = recetaService.crear(receta, null, null);
        auditLogger.logCrear("Receta", String.valueOf(recetaGuardada.getId()));
        return ResponseEntity.ok(recetaGuardada);
    }

    @PutMapping("/api/actualizar/{id}")
    @ResponseBody
    @Operation(summary = "API: Actualizar receta", description = "Actualiza una receta vía API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Receta actualizada"),
        @ApiResponse(responseCode = "404", description = "Receta no encontrada", content = @Content)
    })
    public ResponseEntity<RecetaDTO> actualizarApi(
            @PathVariable Long id,
            @RequestBody RecetaDTO receta) {
        RecetaDTO recetaActualizada = recetaService.actualizar(id, receta, null, null);
        auditLogger.logActualizar("Receta", String.valueOf(id));
        return ResponseEntity.ok(recetaActualizada);
    }

    @GetMapping("/api/eliminar/{id}")
    @ResponseBody
    @Operation(summary = "API: Eliminar receta", description = "Desactiva una receta vía API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Receta desactivada"),
        @ApiResponse(responseCode = "404", description = "Receta no encontrada", content = @Content)
    })
    public ResponseEntity<Void> eliminarApi(@PathVariable Long id) {
        recetaService.eliminar(id);
        auditLogger.logDesactivar("Receta", String.valueOf(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/activar/{id}")
    @ResponseBody
    @Operation(summary = "API: Activar receta", description = "Activa una receta vía API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Receta activada"),
        @ApiResponse(responseCode = "404", description = "Receta no encontrada", content = @Content)
    })
    public ResponseEntity<Void> activarApi(@PathVariable Long id) {
        recetaService.activar(id);
        auditLogger.logActivar("Receta", String.valueOf(id));
        return ResponseEntity.ok().build();
    }
}