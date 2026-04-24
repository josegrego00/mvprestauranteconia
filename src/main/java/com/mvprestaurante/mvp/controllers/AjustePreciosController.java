package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.ProductoDTO;
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

import java.math.BigDecimal;
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
            @RequestParam(required = false, defaultValue = "30") BigDecimal porcentajeGanancia,
            @RequestParam(required = false, defaultValue = "all") String tipoProducto,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<ProductoDTO> productosPage;

        if (search != null && !search.isEmpty()) {
            productosPage = productoService.buscarPorNombre(search, pageable);
            model.addAttribute("search", search);
        } else {
            switch (tipoProducto) {
                case "conReceta" -> productosPage = productoService.listarProductosConReceta(pageable);
                case "sinReceta" -> productosPage = productoService.listarSinProducto(pageable);
                default -> productosPage = productoService.listarActivos(pageable);
            }
        }

        model.addAttribute("productos", productosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productosPage.getTotalPages());
        model.addAttribute("totalItems", productosPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("porcentajeGanancia", porcentajeGanancia);
        model.addAttribute("tipoProducto", tipoProducto);

        return "ajuste-precios/lista";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam MultiValueMap<String, String> formParams,
                         @RequestParam(required = false, defaultValue = "0") BigDecimal porcentajeGanancia,
                         RedirectAttributes ra) {

        try {
            for (Map.Entry<String, java.util.List<String>> entry : formParams.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("precio_")) {
                    Long productoId = Long.parseLong(key.replace("precio_", ""));
                    String valor = entry.getValue().get(0);
                    if (valor != null && !valor.isEmpty()) {
                        BigDecimal nuevoPrecio = new BigDecimal(valor);
                        productoService.actualizarPrecioVenta(productoId, nuevoPrecio);
                    }
                }
            }
            ra.addFlashAttribute("success", "Precios actualizados correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/ajuste-precios";
    }
}