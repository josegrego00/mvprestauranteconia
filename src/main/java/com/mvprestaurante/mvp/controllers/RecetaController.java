package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.models.DetalleReceta;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.models.Producto;
import com.mvprestaurante.mvp.models.Receta;
import com.mvprestaurante.mvp.services.IngredienteService;
import com.mvprestaurante.mvp.services.ProductoService;
import com.mvprestaurante.mvp.services.RecetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/recetas")
public class RecetaController {

    @Autowired
    private RecetaService recetaService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private IngredienteService ingredienteService;

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<Receta> recetasPage;

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
    public String nueva(Model model) {
        Receta receta = new Receta();
        receta.setListaIngredientes(new ArrayList<>());

        model.addAttribute("receta", receta);
        model.addAttribute("productos", productoService.listarActivos(PageRequest.of(0, 100)).getContent());
        model.addAttribute("ingredientes", ingredienteService.listarActivos(PageRequest.of(0, 100)).getContent());

        return "recetas/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Receta receta,
            @RequestParam(required = false) Long[] ingredientesIds,
            @RequestParam(required = false) Double[] cantidades,
            RedirectAttributes redirectAttributes) {
        try {
            // Validar nombre único
            if (recetaService.existePorNombre(receta.getNombre())) {
                redirectAttributes.addFlashAttribute("error", "Ya existe una receta con ese nombre");
                return "redirect:/recetas/nueva";
            }

            // Validar que seleccionó un producto
            if (receta.getProducto() == null || receta.getProducto().getId() == null) {
                redirectAttributes.addFlashAttribute("error", "Debe seleccionar un producto");
                return "redirect:/recetas/nueva";
            }

            // Construir lista de detalles de receta
            if (ingredientesIds != null && cantidades != null) {
                List<DetalleReceta> detalles = new ArrayList<>();
                for (int i = 0; i < ingredientesIds.length; i++) {
                    if (ingredientesIds[i] != null && cantidades[i] > 0) {
                        final Long ingredienteId = ingredientesIds[i];
                        final Double cantidad = cantidades[i];

                        // Aquí podrías validar que el ingrediente existe
                        Ingrediente ingrediente = ingredienteService.obtenerPorId(ingredientesIds[i])
                                .orElseThrow(
                                        () -> new RuntimeException("Ingrediente no encontrado: " + ingredienteId));

                        DetalleReceta detalle = DetalleReceta.builder()
                                .ingrediente(ingrediente)
                                .cantidadIngrediente(cantidades[i])
                                .build();
                        detalles.add(detalle);
                    }
                }
                receta.setListaIngredientes(detalles);
            }

            recetaService.guardar(receta);
            redirectAttributes.addFlashAttribute("success", "Receta guardada exitosamente");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al guardar la receta: " + e.getMessage());
            return "redirect:/recetas/nueva";
        }
        return "redirect:/productos"; // 👈 Mejor redirigir a productos, no a recetas
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return recetaService.obtenerPorId(id)
                .map(receta -> {
                    model.addAttribute("receta", receta);
                    model.addAttribute("productos", productoService.listarActivos(PageRequest.of(0, 100)).getContent());
                    model.addAttribute("ingredientes",
                            ingredienteService.listarActivos(PageRequest.of(0, 100)).getContent());
                    return "recetas/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Receta no encontrada");
                    return "redirect:/recetas";
                });
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (recetaService.eliminarLogico(id)) {
            redirectAttributes.addFlashAttribute("success", "Receta eliminada correctamente");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la receta");
        }
        return "redirect:/recetas";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return recetaService.obtenerPorId(id)
                .map(receta -> {
                    model.addAttribute("receta", receta);
                    return "recetas/ver";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Receta no encontrada");
                    return "redirect:/recetas";
                });
    }

    @GetMapping("/{id}/ingredientes")
    public String verIngredientes(@PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            RedirectAttributes redirectAttributes) {
        return recetaService.obtenerPorId(id)
                .map(receta -> {
                    Pageable pageable = PageRequest.of(page, 10);
                    Page<DetalleReceta> ingredientesPage = recetaService.listarIngredientesDeReceta(id, pageable);

                    model.addAttribute("receta", receta);
                    model.addAttribute("ingredientes", ingredientesPage.getContent());
                    model.addAttribute("currentPage", page);
                    model.addAttribute("totalPages", ingredientesPage.getTotalPages());

                    return "recetas/ingredientes";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Receta no encontrada");
                    return "redirect:/recetas";
                });
    }
}