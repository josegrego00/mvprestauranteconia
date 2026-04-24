package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.ProductoDTO;
import com.mvprestaurante.mvp.DTO.RecetaDTO;
import com.mvprestaurante.mvp.services.ProductoService;
import com.mvprestaurante.mvp.services.RecetaService;
import com.mvprestaurante.mvp.utils.AuditLogger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Gestión de productos de la empresa - API REST")
public class ProductoController {

    private final ProductoService productoService;
    private final RecetaService recetaService;
    private final AuditLogger auditLogger;

    @GetMapping
    @Operation(summary = "API: Listar productos", description = "Lista todos los productos activos de la empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content)
    })
    public ResponseEntity<Page<ProductoDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean soloConReceta) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<ProductoDTO> productosPage;

        if (soloConReceta) {
            productosPage = productoService.listarProductosConReceta(pageable);
        } else if (search != null && !search.isEmpty()) {
            productosPage = productoService.buscarPorNombre(search, pageable);
        } else {
            productosPage = productoService.listarActivos(pageable);
        }

        auditLogger.logListar("Producto", productosPage.getContent().size());
        return ResponseEntity.ok(productosPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "API: Buscar producto por ID", description = "Busca un producto por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto encontrado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    public ResponseEntity<ProductoDTO> buscarPorId(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        ProductoDTO producto = productoService.obtenerPorId(id);
        auditLogger.logBuscar("Producto", id.toString());
        return ResponseEntity.ok(producto);
    }

    @PostMapping
    @Operation(summary = "API: Guardar producto", description = "Crea un nuevo producto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    public ResponseEntity<ProductoDTO> guardar(
            @Valid @RequestBody ProductoDTO producto,
            @RequestParam(required = false) Long recetaId) {
        ProductoDTO guardado = productoService.guardar(producto, recetaId);
        auditLogger.logCrear("Producto", guardado.getId().toString());
        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "API: Actualizar producto", description = "Actualiza un producto existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto actualizado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    public ResponseEntity<ProductoDTO> actualizar(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Valid @RequestBody ProductoDTO producto,
            @RequestParam(required = false) Long recetaId) {
        ProductoDTO actualizado = productoService.actualizar(id, producto, recetaId);
        auditLogger.logActualizar("Producto", id.toString());
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "API: Eliminar producto", description = "Desactiva un producto (eliminación lógica)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Producto desactivado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        try {
            productoService.eliminar(id);
            auditLogger.logDesactivar("Producto", id.toString());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/estimado")
    @Operation(summary = "API: Stock estimado", description = "Retorna el stock estimado de un producto con receta")
    public ResponseEntity<Double> obtenerEstimado(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        Double estimado = productoService.calcularStockEstimado(id);
        return ResponseEntity.ok(estimado);
    }
}