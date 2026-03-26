package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.ProductoVentaDTO;
import com.mvprestaurante.mvp.DTO.VentaDetalleDTO;
import com.mvprestaurante.mvp.mapper.ProductoVentaMapper;
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
    private ProductoVentaMapper productoVentaMapper;

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
        model.addAttribute("pageSize", size);

        return "ventas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("venta", new Venta());
        
        List<ProductoVentaDTO> productos = productoService.listarActivos(PageRequest.of(0, 100))
            .getContent()
            .stream()
            .map(producto -> {
                ProductoVentaDTO dto = productoVentaMapper.toVentaDTO(producto);
                dto.setTieneReceta(producto.getTieneReceta());
                
                if (Boolean.TRUE.equals(producto.getTieneReceta()) && producto.getReceta() != null) {
                    dto.setStockEstimado(ventaService.calcularStockDisponibleReceta(producto.getReceta().getId()));
                }
                
                return dto;
            })
            .collect(Collectors.toList());
        
        model.addAttribute("productos", productos);
        model.addAttribute("numeroVenta", ventaService.generarNumeroVenta());
        return "ventas/nueva";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Venta venta,
            @RequestParam Map<String, String> allParams,
            RedirectAttributes ra) {

        try {
            ventaService.guardarDesdeFormulario(venta, allParams);
            ra.addFlashAttribute("success", "Venta registrada exitosamente");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ventas/nueva";
        }

        return "redirect:/ventas";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return ventaService.obtenerPorId(id)
                .map(venta -> {
                    VentaDetalleDTO ventaDTO = ventaMapper.toDetalleDTO(venta);
                    model.addAttribute("venta", ventaDTO);
                    return "ventas/ver";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "Venta no encontrada");
                    return "redirect:/ventas";
                });
    }

    @PostMapping("/anular/{id}")
    public String anular(@PathVariable Long id, RedirectAttributes ra) {
        try {
            boolean anulado = ventaService.anular(id).isPresent();
            if (anulado) {
                ra.addFlashAttribute("success", "Venta anulada correctamente");
            } else {
                ra.addFlashAttribute("error", "No se pudo anular la venta");
            }
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas";
    }
}
