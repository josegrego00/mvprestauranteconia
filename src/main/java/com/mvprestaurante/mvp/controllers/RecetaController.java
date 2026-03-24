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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    public String nuevaReceta(@RequestParam(required = false) Long productoId, Model model) {
        Receta receta = new Receta();
        receta.setListaIngredientes(new ArrayList<>());

        model.addAttribute("receta", receta);
        model.addAttribute("productoId", productoId);
        model.addAttribute("ingredientes", ingredienteService.listarActivos(PageRequest.of(0, 100)).getContent());

        return "recetas/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editarReceta(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Receta receta = recetaService.obtenerPorId(id)
                    .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

            model.addAttribute("receta", receta);
            // model.addAttribute("productos",
            // productoService.listarActivos(PageRequest.of(0, 100)).getContent());
            model.addAttribute("ingredientes", ingredienteService.listarActivos(PageRequest.of(0, 100)).getContent());

            return "recetas/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar la receta: " + e.getMessage());
            return "redirect:/recetas";
        }
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Receta receta,
            @RequestParam(required = false) Long[] ingredientesIds,
            @RequestParam(required = false) Double[] cantidades,
            @RequestParam(required = false) Long productoId,
            RedirectAttributes redirectAttributes) {
        // 🔴 NUEVO LOG - PON ESTO AL INICIO
        System.out.println("===== DATOS RECIBIDOS =====");
        System.out.println("Nombre: " + receta.getNombre());
        System.out.println("Descripción: " + receta.getDescripcion());
        System.out
                .println("Ingredientes IDs: " + (ingredientesIds != null ? Arrays.toString(ingredientesIds) : "null"));
        System.out.println("Cantidades: " + (cantidades != null ? Arrays.toString(cantidades) : "null"));
        try {
            // Validar nombre único (solo si es nueva receta o cambió el nombre)
            if (receta.getId() == null) {
                if (recetaService.existePorNombre(receta.getNombre())) {
                    redirectAttributes.addFlashAttribute("error", "Ya existe una receta con ese nombre");
                    return "redirect:/recetas/nueva";
                }
            }

            // Construir lista de detalles de receta
            List<DetalleReceta> detalles = new ArrayList<>();

            if (ingredientesIds != null && cantidades != null && ingredientesIds.length > 0) {
                for (int i = 0; i < ingredientesIds.length; i++) {
                    if (ingredientesIds[i] != null && cantidades[i] != null && cantidades[i] > 0) {
                        Long idIngrediente = ingredientesIds[i];
                        Double cantidadIngrediente = cantidades[i];

                        Optional<Ingrediente> ingredienteOpt = ingredienteService.obtenerPorId(idIngrediente);

                        if (!ingredienteOpt.isPresent()) {
                            throw new RuntimeException("Ingrediente no encontrado: " + idIngrediente);
                        }

                        Ingrediente ingrediente = ingredienteOpt.get();

                        DetalleReceta detalle = DetalleReceta.builder()
                                .receta(receta)
                                .ingrediente(ingrediente)
                                .cantidadIngrediente(cantidadIngrediente)
                                .build();
                        detalles.add(detalle);
                    }
                }
            }

            // Validar que tenga al menos un ingrediente
            if (detalles.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "La receta debe tener al menos un ingrediente");
                return "redirect:/recetas/nueva";
            }

            receta.setListaIngredientes(detalles);

            Receta recetaGuardada;
            if (receta.getId() != null) {
                recetaGuardada = recetaService.actualizar(receta.getId(), receta);
                redirectAttributes.addFlashAttribute("success", "Receta actualizada exitosamente");
            } else {
                recetaGuardada = recetaService.crearReceta(receta);
                redirectAttributes.addFlashAttribute("success", "Receta guardada exitosamente");
            }

            if (productoId != null) {
                productoService.asociarReceta(productoId, recetaGuardada.getId());
                redirectAttributes.addFlashAttribute("success", "Receta creada y asociada al producto");
                return "redirect:/productos/editar/" + productoId;
            }

            // LOGS PARA VERIFICAR (AHORA SÍ DESPUÉS DE CONSTRUIR)
            System.out.println("===== VERIFICANDO INGREDIENTES =====");
            System.out.println("Receta nombre: " + receta.getNombre());
            System.out.println("Receta ID (antes de guardar): " + receta.getId());
            System.out.println("Número de ingredientes: " + receta.getListaIngredientes().size());

            for (int i = 0; i < receta.getListaIngredientes().size(); i++) {
                DetalleReceta d = receta.getListaIngredientes().get(i);
                System.out.println("Ingrediente " + i + ":");
                System.out.println(
                        "  - ID Ingrediente: " + (d.getIngrediente() != null ? d.getIngrediente().getId() : "null"));
                System.out.println("  - Cantidad: " + d.getCantidadIngrediente());
                System.out.println(
                        "  - Receta en detalle: " + (d.getReceta() != null ? d.getReceta().getNombre() : "null"));
            }

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            if (receta.getId() != null) {
                return "redirect:/recetas/editar/" + receta.getId();
            }
            return "redirect:/recetas/nueva";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al guardar la receta: " + e.getMessage());
            if (receta.getId() != null) {
                return "redirect:/recetas/editar/" + receta.getId();
            }
            return "redirect:/recetas/nueva";
        }

        // Redirigir a la lista de recetas
        return "redirect:/recetas";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (recetaService.eliminarLogico(id)) {
                redirectAttributes.addFlashAttribute("success", "Receta eliminada correctamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "Error al eliminar la receta");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
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

    @GetMapping("/stock/{id}")
    @ResponseBody
    public Double obtenerStock(@PathVariable Long id) {
        return recetaService.calcularStockDisponible(id);
    }
}