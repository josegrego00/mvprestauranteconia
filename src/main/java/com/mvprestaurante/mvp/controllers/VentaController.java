package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.ProductoDTO;
import com.mvprestaurante.mvp.DTO.ReporteCierreDTO;
import com.mvprestaurante.mvp.DTO.VentaDTO;
import com.mvprestaurante.mvp.mapper.ProductoMapper;
import com.mvprestaurante.mvp.mapper.VentaMapper;
import com.mvprestaurante.mvp.models.Venta;
import com.mvprestaurante.mvp.services.ProductoService;
import com.mvprestaurante.mvp.services.VentaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private VentaMapper ventaMapper;

    @Autowired
    private ProductoMapper productoMapper;

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaVenta").descending());
        Page<Venta> ventasPage = ventaService.buscar(search, fechaInicio, fechaFin, pageable);

        if (search != null && !search.isEmpty()) {
            model.addAttribute("search", search);
        }
        if (fechaInicio != null && !fechaInicio.isEmpty()) {
            model.addAttribute("fechaInicio", fechaInicio);
        }
        if (fechaFin != null && !fechaFin.isEmpty()) {
            model.addAttribute("fechaFin", fechaFin);
        }

        Double totalVentas = ventaService.obtenerTotalVentasPorFecha(fechaInicio, fechaFin);
        model.addAttribute("totalVentas", totalVentas != null ? totalVentas : 0.0);

        model.addAttribute("ventas", ventasPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ventasPage.getTotalPages());
        model.addAttribute("totalItems", ventasPage.getTotalElements());

        return "ventas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        List<ProductoDTO> productos = productoService.listarActivos(PageRequest.of(0, 100)).getContent();

        model.addAttribute("productos", productos);
        model.addAttribute("venta", new VentaDTO());
        model.addAttribute("numeroVenta", ventaService.generarNumeroVenta());

        return "ventas/nueva";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return ventaService.obtenerPorId(id)
                .map(venta -> {
                    model.addAttribute("venta", venta);
                    return "ventas/ver";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Venta no encontrada");
                    return "redirect:/ventas";
                });
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute VentaDTO ventaDTO,
            @RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes) {

        try {
            ventaService.guardar(ventaDTO, params);
            redirectAttributes.addFlashAttribute("success", "Venta registrada correctamente");
            return "redirect:/ventas";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ventas/nueva";
        }
    }

    @GetMapping("/buscar")
    @ResponseBody
    public Page<Venta> buscar(@RequestParam(required = false) String search,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaVenta").descending());
        return ventaService.buscar(search, fechaInicio, fechaFin, pageable);
    }
}