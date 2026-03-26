package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.CompraDetalleDTO;
import com.mvprestaurante.mvp.DTO.DetalleCompraSimpleDTO;
import com.mvprestaurante.mvp.DTO.IngredienteSimpleDTO;
import com.mvprestaurante.mvp.DTO.ProductoSimpleDTO;
import com.mvprestaurante.mvp.models.Compra;
import com.mvprestaurante.mvp.models.DetalleCompra;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.models.Producto;
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
import java.util.ArrayList;
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

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCompra").descending());
        Page<Compra> comprasPage;

        if (search != null && !search.isEmpty()) {
            comprasPage = compraService.buscarPorNumero(search, pageable);
            model.addAttribute("search", search);
        } else if (fechaInicio != null && !fechaInicio.isEmpty() && fechaFin != null && !fechaFin.isEmpty()) {
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio + "T00:00:00");
            LocalDateTime fin = LocalDateTime.parse(fechaFin + "T23:59:59");
            comprasPage = compraService.filtrarPorFecha(inicio, fin, pageable);
            model.addAttribute("fechaInicio", fechaInicio);
            model.addAttribute("fechaFin", fechaFin);
        } else {
            comprasPage = compraService.listarActivos(pageable);
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
        
        List<IngredienteSimpleDTO> ingredientesDTO = ingredienteService.listarActivos(PageRequest.of(0, 100))
            .getContent()
            .stream()
            .map(i -> IngredienteSimpleDTO.builder()
                .id(i.getId())
                .nombre(i.getNombre())
                .unidadMedida(i.getUnidadMedida())
                .build())
            .collect(Collectors.toList());
        
        List<ProductoSimpleDTO> productosDTO = productoService.listarProductosSinReceta(PageRequest.of(0, 100))
            .getContent()
            .stream()
            .map(p -> ProductoSimpleDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .build())
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
            List<DetalleCompra> detalles = new ArrayList<>();

            for (String key : allParams.keySet()) {
                if (key.startsWith("itemId[")) {
                    String index = key.substring(7, key.length() - 1);
                    String itemValue = allParams.get(key);
                    String cantidadParam = "cantidad[" + index + "]";
                    String precioParam = "precio[" + index + "]";
                    
                    if (itemValue != null && !itemValue.isEmpty() && 
                        allParams.containsKey(cantidadParam) && 
                        allParams.containsKey(precioParam)) {
                        
                        String[] parts = itemValue.split("\\|");
                        if (parts.length != 2) continue;
                        
                        Long itemId = Long.parseLong(parts[0]);
                        String tipoItem = parts[1];
                        Integer cantidad = Integer.parseInt(allParams.get(cantidadParam));
                        Double precio = Double.parseDouble(allParams.get(precioParam));
                        
                        DetalleCompra detalle = new DetalleCompra();
                        
                        if ("INGREDIENTE".equals(tipoItem)) {
                            Ingrediente ing = new Ingrediente();
                            ing.setId(itemId);
                            detalle.setIngrediente(ing);
                        } else if ("PRODUCTO".equals(tipoItem)) {
                            Producto prod = new Producto();
                            prod.setId(itemId);
                            detalle.setProducto(prod);
                        }
                        
                        detalle.setTipoItem(tipoItem);
                        detalle.setCantidad(cantidad);
                        detalle.setPrecioUnitarioCompra(precio);
                        detalles.add(detalle);
                    }
                }
            }

            compraService.guardar(compra, detalles);
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
                    List<DetalleCompraSimpleDTO> detallesDTO = new ArrayList<>();
                    for (DetalleCompra detalle : compra.getDetallesCompra()) {
                        String nombreItem = "";
                        if ("INGREDIENTE".equals(detalle.getTipoItem()) && detalle.getIngrediente() != null) {
                            nombreItem = detalle.getIngrediente().getNombre();
                        } else if ("PRODUCTO".equals(detalle.getTipoItem()) && detalle.getProducto() != null) {
                            nombreItem = detalle.getProducto().getNombre();
                        }
                        detallesDTO.add(DetalleCompraSimpleDTO.builder()
                            .tipoItem(detalle.getTipoItem())
                            .nombreItem(nombreItem)
                            .cantidad(detalle.getCantidad())
                            .precioUnitarioCompra(detalle.getPrecioUnitarioCompra())
                            .subtotal(detalle.getSubtotal())
                            .build());
                    }
                    
                    CompraDetalleDTO compraDTO = CompraDetalleDTO.builder()
                        .id(compra.getId())
                        .numeroCompra(compra.getNumeroCompra())
                        .fechaCompra(compra.getFechaCompra())
                        .proveedor(compra.getProveedor())
                        .observaciones(compra.getObservaciones())
                        .estado(compra.getEstado())
                        .subtotal(compra.getSubtotal())
                        .impuesto(compra.getImpuesto())
                        .total(compra.getTotal())
                        .nombreUsuario(compra.getUsuario().getNombre())
                        .detalles(detallesDTO)
                        .build();
                    
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
