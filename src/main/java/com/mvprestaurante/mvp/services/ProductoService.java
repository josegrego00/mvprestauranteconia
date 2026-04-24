package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.DTO.ProductoDTO;
import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.mapper.ProductoMapper;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Producto;
import com.mvprestaurante.mvp.models.Receta;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;
import com.mvprestaurante.mvp.repositories.ProductoRepository;
import com.mvprestaurante.mvp.repositories.RecetaRepository;
import com.mvprestaurante.mvp.utils.AuditLogger;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final EmpresaRepositorio empresaRepositorio;
    private final RecetaRepository recetaRepository;
    private final AuditLogger auditLogger;

    private void validarTenant() {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new BusinessException("No se ha identificado la empresa");
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarActivos(Pageable pageable) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        Page<Producto> productos = productoRepository.findByEstaActivoTrue(empresaId, pageable);
        auditLogger.logListar("Producto", productos.getContent().size());
        return productos.map(productoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> buscarPorNombre(String nombre, Pageable pageable) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        Page<Producto> productos = productoRepository.findByNombreContainingIgnoreCaseAndEstaActivoTrue(empresaId, nombre, pageable);
        auditLogger.logBuscar("Producto", "Nombre: " + nombre);
        return productos.map(productoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarProductosConReceta(Pageable pageable) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        Page<Producto> productos = productoRepository.findByTieneRecetaTrueAndEstaActivoTrue(empresaId, pageable);
        auditLogger.logListar("Producto", productos.getContent().size());
        return productos.map(productoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarSinProducto(Pageable pageable) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        Page<Producto> productos = productoRepository.findByTieneRecetaFalseAndEstaActivoTrue(empresaId, pageable);
        auditLogger.logListar("Producto", productos.getContent().size());
        return productos.map(productoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarDisponiblesParaReceta(Pageable pageable) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        Page<Producto> productos = productoRepository.findByTieneRecetaTrueAndEstaActivoTrue(empresaId, pageable);
        return productos.map(productoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Long id) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        Producto producto = productoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));
        auditLogger.logBuscar("Producto", id.toString());
        return productoMapper.toDTO(producto);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ProductoDTO guardar(ProductoDTO dto, Long recetaId) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new BusinessException("El nombre del producto es obligatorio");
        }

        if (existePorNombre(dto.getNombre())) {
            throw new BusinessException("Ya existe un producto con ese nombre");
        }

        if (dto.getTieneReceta() == null) {
            dto.setTieneReceta(false);
        }

        if (Boolean.TRUE.equals(dto.getTieneReceta())) {
            dto.setStock(null);
            if (recetaId != null) {
                Receta receta = recetaRepository.findByIdAndEmpresaId(recetaId, empresaId)
                        .orElseThrow(() -> new BusinessException("Receta no encontrada"));
                if (receta.getProducto() != null) {
                    throw new BusinessException("Esta receta ya está asignada a otro producto");
                }
            }
        } else {
            if (dto.getStock() == null || dto.getStock() < 0) {
                dto.setStock(0.0);
            }
        }

        Empresa empresa = empresaRepositorio.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));

        Producto producto = productoMapper.toEntity(dto);
        producto.setEmpresa(empresa);
        producto.setEstaActivo(true);
        producto.setNombre(dto.getNombre().trim());

        if (producto.getDescripcion() != null) {
            producto.setDescripcion(producto.getDescripcion().trim());
        }

        if (Boolean.TRUE.equals(dto.getTieneReceta()) && recetaId != null) {
            Receta receta = recetaRepository.findByIdAndEmpresaId(recetaId, empresaId)
                    .orElseThrow(() -> new BusinessException("Receta no encontrada"));
            producto.setReceta(receta);
            receta.setProducto(producto);
            recetaRepository.save(receta);
        }

        Producto guardado = productoRepository.save(producto);
        auditLogger.logCrear("Producto", guardado.getId().toString());
        return productoMapper.toDTO(guardado);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ProductoDTO actualizar(Long id, ProductoDTO dto, Long recetaId) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        Producto productoExistente = productoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));

        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new BusinessException("El nombre del producto es obligatorio");
        }

        if (!productoExistente.getNombre().equalsIgnoreCase(dto.getNombre()) && existePorNombre(dto.getNombre())) {
            throw new BusinessException("Ya existe un producto con ese nombre");
        }

        Boolean tieneRecetaActual = Boolean.TRUE.equals(productoExistente.getTieneReceta());
        Boolean tieneRecetaNuevo = Boolean.TRUE.equals(dto.getTieneReceta());

        if (!tieneRecetaActual.equals(tieneRecetaNuevo)) {
            throw new BusinessException("No puedes cambiar el tipo de producto (con/sin receta)");
        }

        productoExistente.setNombre(dto.getNombre().trim());
        if (dto.getDescripcion() != null) {
            productoExistente.setDescripcion(dto.getDescripcion().trim());
        }
        productoExistente.setPrecioCompra(dto.getPrecioCompra());
        productoExistente.setPrecioVenta(dto.getPrecioVenta() != null ? dto.getPrecioVenta() : BigDecimal.ZERO);

        if (Boolean.TRUE.equals(dto.getTieneReceta())) {
            productoExistente.setStock(null);
            if (recetaId != null) {
                Receta recetaNueva = recetaRepository.findByIdAndEmpresaId(recetaId, empresaId)
                        .orElseThrow(() -> new BusinessException("Receta no encontrada"));

                if (recetaNueva.getProducto() != null && !recetaNueva.getProducto().getId().equals(id)) {
                    throw new BusinessException("Esta receta ya está asignada a otro producto");
                }

                Receta recetaAnterior = productoExistente.getReceta();
                if (recetaAnterior != null && !recetaAnterior.getId().equals(recetaId)) {
                    recetaAnterior.setProducto(null);
                    recetaRepository.save(recetaAnterior);
                }

                productoExistente.setReceta(recetaNueva);
                recetaNueva.setProducto(productoExistente);
                recetaRepository.save(recetaNueva);
            }
        } else {
            if (dto.getStock() == null || dto.getStock() < 0) {
                productoExistente.setStock(0.0);
            } else {
                productoExistente.setStock(dto.getStock());
            }
        }

        Producto guardado = productoRepository.save(productoExistente);
        auditLogger.logActualizar("Producto", id.toString());
        return productoMapper.toDTO(guardado);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(Long id) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        Producto producto = productoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));

        producto.setEstaActivo(false);
        productoRepository.save(producto);
        auditLogger.logDesactivar("Producto", id.toString());
    }

    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        return productoRepository.existsByNombreIgnoreCaseAndEmpresaIdAndEstaActivoTrue(nombre, empresaId);
    }

    @Transactional
    public void asociarReceta(Long productoId, Long recetaId) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        Producto producto = productoRepository.findByIdAndEmpresaId(productoId, empresaId)
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));

        Receta receta = recetaRepository.findByIdAndEmpresaId(recetaId, empresaId)
                .orElseThrow(() -> new BusinessException("Receta no encontrada"));

        if (receta.getProducto() != null && !receta.getProducto().getId().equals(productoId)) {
            throw new BusinessException("Esta receta ya está asignada a otro producto");
        }

        producto.setTieneReceta(true);
        producto.setReceta(receta);
        receta.setProducto(producto);

        productoRepository.save(producto);
        recetaRepository.save(receta);
    }

    @Transactional(readOnly = true)
    public Double calcularStockEstimado(Long productoId) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        Producto producto = productoRepository.findByIdAndEmpresaId(productoId, empresaId)
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));

        if (producto.getReceta() == null) {
            return 0.0;
        }

        return producto.getReceta().getListaIngredientes().stream()
                .mapToDouble(detalle -> {
                    Double stock = detalle.getIngrediente().getStockDisponible();
                    BigDecimal cantidad = detalle.getCantidadIngrediente();
                    if (stock == null || cantidad == null || cantidad.compareTo(BigDecimal.ZERO) == 0) {
                        return Double.MAX_VALUE;
                    }
                    return stock / cantidad.doubleValue();
                })
                .min()
                .orElse(0.0);
    }

    @Transactional
    public void actualizarPrecioVenta(Long productoId, BigDecimal nuevoPrecio) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        Producto producto = productoRepository.findByIdAndEmpresaId(productoId, empresaId)
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));

        producto.setPrecioVenta(nuevoPrecio);
        productoRepository.save(producto);
    }
}