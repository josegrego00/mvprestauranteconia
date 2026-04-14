package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.ProductoDTO;
import com.mvprestaurante.mvp.DTO.RecetaDTO;
import com.mvprestaurante.mvp.mapper.ProductoMapper;
import com.mvprestaurante.mvp.services.ProductoService;
import com.mvprestaurante.mvp.services.RecetaService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final RecetaService recetaService;

    public ProductoController(ProductoService productoService, RecetaService recetaService) {
        this.productoService = productoService;
        this.recetaService = recetaService;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean soloConReceta,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<ProductoDTO> productosPage;

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
        model.addAttribute("producto", new ProductoDTO());
        Page<RecetaDTO> recetas = recetaService.listarSinProducto(PageRequest.of(0, 100));
        model.addAttribute("recetas", recetas.getContent());
        return "productos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute @Valid ProductoDTO producto,
            @RequestParam(required = false) Long recetaId,
            RedirectAttributes redirectAttributes) {

        try {
            if (producto.getId() == null) {
                productoService.guardar(producto, recetaId);
                redirectAttributes.addFlashAttribute("success", "Producto creado exitosamente");
            } else {
                productoService.actualizar(producto.getId(), producto, recetaId);
                redirectAttributes.addFlashAttribute("success", "Producto actualizado correctamente");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            Page<RecetaDTO> recetas = recetaService.listarSinProducto(PageRequest.of(0, 100));
            redirectAttributes.addFlashAttribute("recetas", recetas.getContent());
            return "redirect:/productos/editar/" + producto.getId();
        }

        return "redirect:/productos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductoDTO producto = productoService.obtenerPorId(id);
            model.addAttribute("producto", producto);

            Page<RecetaDTO> recetas = recetaService.listarSinProducto(PageRequest.of(0, 100));
            model.addAttribute("recetas", recetas.getContent());

            return "productos/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/productos";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Producto eliminado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductoDTO producto = productoService.obtenerPorId(id);
            model.addAttribute("producto", producto);
            return "productos/ver";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/productos";
        }
    }

    @GetMapping("/estimado/{id}")
    @ResponseBody
    public Double obtenerEstimado(@PathVariable Long id) {
        return productoService.calcularStockEstimado(id);
    }

    @GetMapping("/receta/{id}")
    public String gestionarReceta(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ProductoDTO producto = productoService.obtenerPorId(id);
            if (!Boolean.TRUE.equals(producto.getTieneReceta())) {
                redirectAttributes.addFlashAttribute("error", "Los productos sin receta no pueden tener receta");
                return "redirect:/productos/ver/" + id;
            }
            if (producto.getRecetaId() != null) {
                return "redirect:/recetas/ver/" + producto.getRecetaId();
            }
            return "redirect:/recetas/nueva?productoId=" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/productos";
        }
    }
}