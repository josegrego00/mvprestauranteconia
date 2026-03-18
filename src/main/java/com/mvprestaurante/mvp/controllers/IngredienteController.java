package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.services.IngredienteService;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ingredientes")
public class IngredienteController {

    @Autowired
    private IngredienteService ingredienteService;

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<Ingrediente> ingredientesPage;

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
    public String nuevo(Model model) {
        model.addAttribute("ingrediente", new Ingrediente());
        model.addAttribute("unidades", new String[] { "kg", "g", "l", "ml", "unidad", "docena" });
        return "ingredientes/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Ingrediente ingrediente,
            RedirectAttributes redirectAttributes) {

        // 🔥 SIN try-catch
        // 🔥 SIN validación duplicada

        if (ingrediente.getId() != null) {
            Optional<Ingrediente> actualizado = ingredienteService.actualizar(ingrediente.getId(), ingrediente);
            if (actualizado.isPresent()) {
                redirectAttributes.addFlashAttribute("success", "Ingrediente actualizado exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo actualizar el ingrediente");
                return "redirect:/ingredientes/editar/" + ingrediente.getId();
            }
        } else {
            ingredienteService.guardar(ingrediente);
            redirectAttributes.addFlashAttribute("success", "Ingrediente guardado exitosamente");
        }

        return "redirect:/ingredientes";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return ingredienteService.obtenerPorId(id)
                .map(ingrediente -> {
                    model.addAttribute("ingrediente", ingrediente);
                    model.addAttribute("unidades", new String[] { "kg", "g", "l", "ml", "unidad", "docena" });
                    return "ingredientes/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Ingrediente no encontrado");
                    return "redirect:/ingredientes";
                });
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (ingredienteService.eliminarLogico(id)) {
            redirectAttributes.addFlashAttribute("success", "Ingrediente eliminado correctamente");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el ingrediente");
        }
        return "redirect:/ingredientes";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return ingredienteService.obtenerPorId(id)
                .map(ingrediente -> {
                    model.addAttribute("ingrediente", ingrediente);
                    return "ingredientes/ver";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Ingrediente no encontrado");
                    return "redirect:/ingredientes";
                });
    }
}