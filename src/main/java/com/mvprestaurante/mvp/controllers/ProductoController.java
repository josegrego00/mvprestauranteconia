package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.ProductoDTO;
import com.mvprestaurante.mvp.DTO.RecetaDTO;
import com.mvprestaurante.mvp.services.ProductoService;
import com.mvprestaurante.mvp.services.RecetaService;
import com.mvprestaurante.mvp.utils.AuditLogger;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Gestión de productos de la empresa")
public class ProductoController {

    private final ProductoService productoService;
    private final RecetaService recetaService;
    private final AuditLogger auditLogger;

    @GetMapping
    @Operation(summary = "Listar productos", description = "Lista todos los productos activos de la empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtained successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content)
    })
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
    @Operation(summary = "Formulario nuevo producto", description = "Muestra el formulario para crear un nuevo producto")
    public String nuevo(Model model) {
        model.addAttribute("producto", new ProductoDTO());
        Page<RecetaDTO> recetas = recetaService.listarSinProducto(PageRequest.of(0, 100));
        model.addAttribute("recetas", recetas.getContent());
        return "productos/formulario";
    }

    @PostMapping("/guardar")
    @Operation(summary = "Guardar producto", description = "Crea o actualiza un producto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Producto created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content)
    })
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
    @Operation(summary = "Formulario editar producto", description = "Muestra el formulario para editar un producto")
    public String editar(@Parameter(description = "ID del producto") @PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
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
    @Operation(summary = "Eliminar producto", description = "Desactiva un producto (eliminación lógica)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Producto deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Producto not found", content = @Content)
    })
    public String eliminar(@Parameter(description = "ID del producto") @PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Producto eliminado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }

    @GetMapping("/ver/{id}")
    @Operation(summary = "Ver producto", description = "Muestra los detalles de un producto")
    public String ver(@Parameter(description = "ID del producto") @PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
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
    @Operation(summary = "Stock estimado", description = "Retorna el stock estimado de un producto con receta")
    public Double obtenerEstimado(@Parameter(description = "ID del producto") @PathVariable Long id) {
        return productoService.calcularStockEstimado(id);
    }

    @GetMapping("/receta/{id}")
    @Operation(summary = "Gestionar receta", description = "Redirecciona a gestionar la receta de un producto")
    public String gestionarReceta(@Parameter(description = "ID del producto") @PathVariable Long id, RedirectAttributes redirectAttributes) {
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

    @GetMapping("/api/listar")
    @ResponseBody
    @Operation(summary = "API: Listar productos", description = "Lista productos activos para API REST")
    public ResponseEntity<Page<ProductoDTO>> listarApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<ProductoDTO> productos = productoService.listarActivos(pageable);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    @Operation(summary = "API: Buscar producto por ID", description = "Busca un producto por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto found"),
        @ApiResponse(responseCode = "404", description = "Producto not found", content = @Content)
    })
    public ResponseEntity<ProductoDTO> buscarPorIdApi(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        ProductoDTO producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(producto);
    }
}