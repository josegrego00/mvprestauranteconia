package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.models.Producto;
import com.mvprestaurante.mvp.services.ProductoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/ajuste-precios")
public class AjustePreciosController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "30") Double porcentajeGanancia,
            @RequestParam(required = false, defaultValue = "all") String tipoProducto,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<Producto> productosPage;

        if (search != null && !search.isEmpty()) {
            productosPage = productoService.buscarPorNombre(search, pageable);
            model.addAttribute("search", search);
        } else {
            switch (tipoProducto) {
                case "conReceta" -> productosPage = productoService.listarProductosConReceta(pageable);
                case "sinReceta" -> productosPage = productoService.listarProductosSinReceta(pageable);
                default -> productosPage = productoService.listarActivos(pageable);
            }
        }

        model.addAttribute("productos", productosPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("porcentajeGanancia", porcentajeGanancia);
        model.addAttribute("tipoProducto", tipoProducto);

        return "ajuste-precios/lista";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam MultiValueMap<String, String> formParams,
                         @RequestParam(required = false, defaultValue = "0") Double porcentajeGanancia,
                         RedirectAttributes ra) {

        int actualizados = 0;

        for (String key : formParams.keySet()) {
            if (key.startsWith("precios[")) {
                String value = formParams.getFirst(key);
                if (value != null && !value.isEmpty()) {
                    try {
                        Long productoId = Long.parseLong(key.replace("precios[", "").replace("]", ""));
                        Double nuevoPrecio = Double.parseDouble(value);

                        productoService.actualizarPrecioVenta(productoId, nuevoPrecio);
                        actualizados++;
                    } catch (NumberFormatException e) {
                        // Ignorar valores inválidos
                    }
                }
            }
        }

        if (actualizados > 0) {
            ra.addFlashAttribute("success", "Se actualizaron " + actualizados + " precios correctamente");
        } else {
            ra.addFlashAttribute("error", "No se pudieron actualizar los precios");
        }

        return "redirect:/ajuste-precios" + (porcentajeGanancia != null && porcentajeGanancia > 0 ? "?porcentajeGanancia=" + porcentajeGanancia : "");
    }

    @PostMapping("/actualizar-precio/{id}")
    @ResponseBody
    public Map<String, Object> actualizarPrecio(@PathVariable Long id, @RequestParam Double precio) {
        try {
            productoService.actualizarPrecioVenta(id, precio);
            return Map.of("success", true, "precio", precio);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
}
