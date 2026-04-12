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

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final EmpresaRepositorio empresaRepositorio;
    private final RecetaRepository recetaRepository;

    private void validarTenant() {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new BusinessException("No se ha identificado la empresa");
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarActivos(Pageable pageable) {
        validarTenant();
        Page<Producto> productos = productoRepository.findByEstaActivoTrue(TenantContext.getTenantId(), pageable);
        return productos.map(productoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> buscarPorNombre(String nombre, Pageable pageable) {
        validarTenant();
        Page<Producto> productos = productoRepository.findByNombreContainingIgnoreCaseAndEstaActivoTrue(TenantContext.getTenantId(), nombre, pageable);
        return productos.map(productoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarProductosConReceta(Pageable pageable) {
        validarTenant();
        Page<Producto> productos = productoRepository.findByTieneRecetaTrueAndEstaActivoTrue(TenantContext.getTenantId(), pageable);
        return productos.map(productoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarSinProducto(Pageable pageable) {
        validarTenant();
        Page<Producto> productos = productoRepository.findByTieneRecetaFalseAndEstaActivoTrue(TenantContext.getTenantId(), pageable);
        return productos.map(productoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarDisponiblesParaProducto(Long productoId, Pageable pageable) {
        validarTenant();
        Page<Producto> productos = productoRepository.findByTieneRecetaTrueAndEstaActivoTrue(TenantContext.getTenantId(), pageable);
        return productos.map(productoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Long id) {
        validarTenant();
        Producto producto = productoRepository.findByIdAndEmpresaId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));
        return productoMapper.toDTO(producto);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ProductoDTO guardar(ProductoDTO dto, Long recetaId) {
        validarTenant();

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
                Receta receta = recetaRepository.findByIdAndEmpresaId(recetaId, TenantContext.getTenantId())
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

        Empresa empresa = empresaRepositorio.findById(TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));

        Producto producto = productoMapper.toEntity(dto);
        producto.setEmpresa(empresa);
        producto.setEstaActivo(true);
        producto.setNombre(dto.getNombre().trim());

        if (producto.getDescripcion() != null) {
            producto.setDescripcion(producto.getDescripcion().trim());
        }

        if (Boolean.TRUE.equals(dto.getTieneReceta()) && recetaId != null) {
            Receta receta = recetaRepository.findByIdAndEmpresaId(recetaId, TenantContext.getTenantId())
                    .orElseThrow(() -> new BusinessException("Receta no encontrada"));
            producto.setReceta(receta);
            receta.setProducto(producto);
            recetaRepository.save(receta);
        }

        Producto guardado = productoRepository.save(producto);
        return productoMapper.toDTO(guardado);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ProductoDTO actualizar(Long id, ProductoDTO dto, Long recetaId) {
        validarTenant();

        Producto productoExistente = productoRepository.findByIdAndEmpresaId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Producto no ditemukan"));

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
        productoExistente.setPrecioVenta(dto.getPrecioVenta() != null ? dto.getPrecioVenta() : 0.0);

        if (Boolean.TRUE.equals(dto.getTieneReceta())) {
            productoExistente.setStock(null);
            if (recetaId != null) {
                Receta recetaNueva = recetaRepository.findByIdAndEmpresaId(recetaId, TenantContext.getTenantId())
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
        return productoMapper.toDTO(guardado);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public boolean eliminar(Long id) {
        validarTenant();

        Producto producto = productoRepository.findByIdAndEmpresaId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));

        producto.setEstaActivo(false);
        productoRepository.save(producto);
        return true;
    }

    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        validarTenant();
        return productoRepository.existsByNombreIgnoreCaseAndEmpresaIdAndEstaActivoTrue(nombre, TenantContext.getTenantId());
    }

    @Transactional
    public void asociarReceta(Long productoId, Long recetaId) {
        validarTenant();

        Producto producto = productoRepository.findByIdAndEmpresaId(productoId, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));

        Receta receta = recetaRepository.findByIdAndEmpresaId(recetaId, TenantContext.getTenantId())
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

        Producto producto = productoRepository.findByIdAndEmpresaId(productoId, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));

        if (producto.getReceta() == null) {
            return 0.0;
        }

        return producto.getReceta().getListaIngredientes().stream()
                .mapToDouble(detalle -> {
                    Double stock = detalle.getIngrediente().getStockDisponible();
                    Double cantidad = detalle.getCantidadIngrediente();
                    if (stock == null || cantidad == null || cantidad == 0) {
                        return Double.MAX_VALUE;
                    }
                    return stock / cantidad;
                })
                .min()
                .orElse(0.0);
    }

    @Transactional
    public void actualizarPrecioVenta(Long productoId, Double nuevoPrecio) {
        validarTenant();

        Producto producto = productoRepository.findByIdAndEmpresaId(productoId, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));

        producto.setPrecioVenta(nuevoPrecio);
        productoRepository.save(producto);
    }
}