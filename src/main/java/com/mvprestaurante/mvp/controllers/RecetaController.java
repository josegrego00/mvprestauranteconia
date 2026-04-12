package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.IngredienteDTO;
import com.mvprestaurante.mvp.DTO.RecetaDTO;
import com.mvprestaurante.mvp.mapper.IngredienteMapper;
import com.mvprestaurante.mvp.mapper.RecetaMapper;
import com.mvprestaurante.mvp.services.IngredienteService;
import com.mvprestaurante.mvp.services.ProductoService;
import com.mvprestaurante.mvp.services.RecetaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/recetas")
@RequiredArgsConstructor
public class RecetaController {

    private final RecetaService recetaService;
    private final RecetaMapper recetaMapper;
    private final IngredienteService ingredienteService;
    private final IngredienteMapper ingredienteMapper;
    private final ProductoService productoService;

    @GetMapping
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
    public String nuevaReceta(@RequestParam(required = false) Long productoId, Model model) {
        RecetaDTO receta = new RecetaDTO();
        model.addAttribute("receta", receta);
        model.addAttribute("productoId", productoId);

        Page<IngredienteDTO> ingredientesPage = ingredienteService.listarActivos(PageRequest.of(0, 100));
        model.addAttribute("ingredientes", ingredientesPage.getContent());

        return "recetas/formulario";
    }

    @GetMapping("/editar/{id}")
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
    public String guardar(@ModelAttribute @Valid RecetaDTO receta,
            @RequestParam(required = false) Long[] ingredientesIds,
            @RequestParam(required = false) Double[] cantidades,
            @RequestParam(required = false) Long productoId,
            RedirectAttributes redirectAttributes) {
        try {
            RecetaDTO recetaGuardada;

            if (receta.getId() != null) {
                recetaGuardada = recetaService.actualizar(receta.getId(), receta, ingredientesIds, cantidades);
                redirectAttributes.addFlashAttribute("success", "Receta actualizada exitosamente");
            } else {
                recetaGuardada = recetaService.crear(receta, ingredientesIds, cantidades);
                redirectAttributes.addFlashAttribute("success", "Receta guardada exitosamente");
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
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean eliminado = recetaService.eliminar(id);
            if (eliminado) {
                redirectAttributes.addFlashAttribute("success", "Receta eliminada correctamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "Error al eliminar la receta");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/recetas";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            RecetaDTO receta = recetaService.obtenerPorId(id);
            model.addAttribute("receta", receta);
            return "recetas/ver";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Receta no encontrada");
            return "redirect:/recetas";
        }
    }

    @GetMapping("/stock/{id}")
    @ResponseBody
    public Double obtenerStock(@PathVariable Long id) {
        return recetaService.calcularStockDisponible(id);
    }
}