package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.models.Producto;
import com.mvprestaurante.mvp.models.Receta;
import com.mvprestaurante.mvp.services.ProductoService;
import com.mvprestaurante.mvp.services.RecetaService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private RecetaService recetaService;

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) boolean soloConReceta,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<Producto> productosPage;

        if (soloConReceta) {
            productosPage = productoService.listarProductosConReceta(pageable);
            model.addAttribute("soloConReceta", true);
        } else if (search != null && !search.isEmpty()) {
            productosPage = productoService.buscarPorNombre(search, pageable);
            model.addAttribute("search", search);
        } else {
            productosPage = productoService.listarActivos(pageable);
        }

        model.addAttribute("productos", productosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productosPage.getTotalPages());
        model.addAttribute("totalItems", productosPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("recetas", recetaService.listarSinProducto(PageRequest.of(0, 100)).getContent());
        return "productos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute Producto producto, 
            @RequestParam(required = false) Long recetaId,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("recetas", recetaService.listarActivas(PageRequest.of(0, 100)).getContent());
            return "productos/formulario";
        }

        try {
            if (producto.getId() == null) {
                productoService.guardar(producto, recetaId);
                redirectAttributes.addFlashAttribute("success", "Producto creado exitosamente");
            } else {
                productoService.actualizar(producto.getId(), producto, recetaId);
                redirectAttributes.addFlashAttribute("success", "Producto actualizado correctamente");
            }
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            if (producto.getId() != null) {
                model.addAttribute("recetas", recetaService.listarDisponiblesParaProducto(producto.getId(), PageRequest.of(0, 100)).getContent());
            } else {
                model.addAttribute("recetas", recetaService.listarSinProducto(PageRequest.of(0, 100)).getContent());
            }
            return "productos/formulario";
        }

        return "redirect:/productos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return productoService.obtenerPorId(id)
                .map(producto -> {
                    model.addAttribute("producto", producto);
                    model.addAttribute("recetas", recetaService.listarDisponiblesParaProducto(id, PageRequest.of(0, 100)).getContent());
                    return "productos/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                    return "redirect:/productos";
                });
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (productoService.eliminarLogico(id)) {
            redirectAttributes.addFlashAttribute("success", "Producto eliminado correctamente");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el producto");
        }
        return "redirect:/productos";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return productoService.obtenerPorId(id)
                .map(producto -> {
                    model.addAttribute("producto", producto);
                    return "productos/ver";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                    return "redirect:/productos";
                });
    }

    @GetMapping("/estimado/{id}")
    @ResponseBody
    public Double obtenerEstimado(@PathVariable Long id) {
        return productoService.calcularStockEstimado(id);
    }

    @GetMapping("/receta/{id}")
    public String gestionarReceta(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return productoService.obtenerPorId(id)
                .map(producto -> {
                    if (!Boolean.TRUE.equals(producto.getTieneReceta())) {
                        redirectAttributes.addFlashAttribute("error", "Los productos sin receta no pueden tener receta");
                        return "redirect:/productos/ver/" + id;
                    }
                    if (producto.getReceta() != null) {
                        return "redirect:/recetas/ver/" + producto.getReceta().getId();
                    }
                    return "redirect:/recetas/nueva?productoId=" + id;
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                    return "redirect:/productos";
                });
    }
}