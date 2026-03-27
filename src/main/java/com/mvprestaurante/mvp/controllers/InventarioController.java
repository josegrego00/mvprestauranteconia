package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.InventarioItemDTO;
import com.mvprestaurante.mvp.DTO.InventarioReporteDTO;
import com.mvprestaurante.mvp.services.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public String lista(Model model) {
        List<InventarioItemDTO> items = inventarioService.obtenerItemsInventario();
        model.addAttribute("items", items);
        return "inventario/lista";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam Map<String, String> allParams, RedirectAttributes ra) {
        try {
            inventarioService.guardarInventario(allParams);
            ra.addFlashAttribute("success", "Inventario guardado correctamente");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventario";
    }

    @GetMapping("/reporte")
    public String reporte(
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        if (fechaDesde == null) {
            fechaDesde = LocalDate.now();
        }
        if (fechaHasta == null) {
            fechaHasta = LocalDate.now();
        }

        Page<InventarioReporteDTO> reporte = inventarioService.obtenerReporte(fechaDesde, fechaHasta, page, 20);

        model.addAttribute("reporte", reporte.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reporte.getTotalPages());
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);

        return "inventario/reporte";
    }
}
