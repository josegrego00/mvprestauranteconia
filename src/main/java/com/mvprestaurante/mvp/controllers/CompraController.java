package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.CompraDetalleDTO;
import com.mvprestaurante.mvp.DTO.IngredienteDTO;
import com.mvprestaurante.mvp.DTO.ProductoDTO;
import com.mvprestaurante.mvp.mapper.CompraMapper;
import com.mvprestaurante.mvp.mapper.IngredienteMapper;
import com.mvprestaurante.mvp.mapper.ProductoMapper;
import com.mvprestaurante.mvp.models.Compra;
import com.mvprestaurante.mvp.services.CompraService;
import com.mvprestaurante.mvp.services.IngredienteService;
import com.mvprestaurante.mvp.services.ProductoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/compras")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @Autowired
    private IngredienteService ingredienteService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private IngredienteMapper ingredienteMapper;

    @Autowired
    private ProductoMapper productoMapper;

    @Autowired
    private CompraMapper compraMapper;

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCompra").descending());
        Page<Compra> comprasPage = compraService.buscar(search, fechaInicio, fechaFin, pageable);

        if (search != null && !search.isEmpty()) {
            model.addAttribute("search", search);
        }
        if (fechaInicio != null && !fechaInicio.isEmpty()) {
            model.addAttribute("fechaInicio", fechaInicio);
        }
        if (fechaFin != null && !fechaFin.isEmpty()) {
            model.addAttribute("fechaFin", fechaFin);
        }

        model.addAttribute("compras", comprasPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", comprasPage.getTotalPages());
        model.addAttribute("totalItems", comprasPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "compras/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("compra", new Compra());
        
        List<IngredienteDTO> ingredientesDTO = ingredienteService.listarActivos(PageRequest.of(0, 100))
            .getContent()
            .stream()
            .map(ingredienteMapper::toSimpleDTO)
            .collect(Collectors.toList());
        
        List<ProductoDTO> productosDTO = productoService.listarProductosSinReceta(PageRequest.of(0, 100))
            .getContent()
            .stream()
            .map(productoMapper::toSimpleDTO)
            .collect(Collectors.toList());
        
        model.addAttribute("ingredientes", ingredientesDTO);
        model.addAttribute("productos", productosDTO);
        model.addAttribute("numeroCompra", compraService.generarNumeroCompra());
        return "compras/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Compra compra,
            @RequestParam Map<String, String> allParams,
            RedirectAttributes ra) {

        try {
            compraService.guardarDesdeFormulario(compra, allParams);
            ra.addFlashAttribute("success", "Compra registrada exitosamente");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/compras/nueva";
        }

        return "redirect:/compras";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return compraService.obtenerPorId(id)
                .map(compra -> {
                    CompraDetalleDTO compraDTO = compraMapper.toDetalleDTO(compra);
                    model.addAttribute("compra", compraDTO);
                    return "compras/ver";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "Compra no encontrada");
                    return "redirect:/compras";
                });
    }

    @PostMapping("/anular/{id}")
    public String anular(@PathVariable Long id, RedirectAttributes ra) {
        try {
            boolean anulado = compraService.anular(id).isPresent();
            if (anulado) {
                ra.addFlashAttribute("success", "Compra anulada correctamente");
            } else {
                ra.addFlashAttribute("error", "No se pudo anular la compra");
            }
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/compras";
    }
}
