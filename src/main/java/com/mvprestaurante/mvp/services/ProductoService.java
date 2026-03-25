package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.exceptions.DuplicateResourceException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final EmpresaRepositorio empresaRepositorio;
    private final RecetaRepository recetaRepository;

    private void validarTenant() {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new BusinessException("No se ha identificado la empresa");
        }
    }

    @Transactional(readOnly = true)
    public Page<Producto> listarActivos(Pageable pageable) {
        validarTenant();
        return productoRepository.findByEstaActivoTrue(TenantContext.getTenantId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Producto> buscarPorNombre(String nombre, Pageable pageable) {
        validarTenant();
        return productoRepository.findByNombreContainingIgnoreCaseAndEstaActivoTrue(TenantContext.getTenantId(), nombre, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Producto> listarProductosConReceta(Pageable pageable) {
        validarTenant();
        return productoRepository.findByTieneRecetaTrueAndEstaActivoTrue(TenantContext.getTenantId(), pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Producto> obtenerPorId(Long id) {
        validarTenant();
        return productoRepository.findById(id)
                .filter(producto -> producto.getEmpresa().getId().equals(TenantContext.getTenantId()));
    }

    @Transactional
    public Producto guardar(Producto producto, Long recetaId) {
        validarTenant();

        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new BusinessException("El nombre del producto es obligatorio");
        }
        if (producto.getPrecioVenta() == null || producto.getPrecioVenta() < 0) {
            throw new BusinessException("El precio de venta debe ser mayor o igual a 0");
        }

        if (producto.getId() == null &&
                productoRepository.existsByNombreIgnoreCaseAndEmpresaIdAndEstaActivoTrue(
                        producto.getNombre(), TenantContext.getTenantId())) {
            throw new DuplicateResourceException("Producto", producto.getNombre());
        }

        if (producto.getTieneReceta() == null) {
            producto.setTieneReceta(false);
        }
        
        if (Boolean.TRUE.equals(producto.getTieneReceta())) {
            producto.setStock(null);
            if (recetaId != null) {
                Receta receta = recetaRepository.findById(recetaId)
                        .orElseThrow(() -> new BusinessException("Receta no encontrada"));
                if (receta.getProducto() != null) {
                    throw new BusinessException("Esta receta ya está asignada a otro producto: " + receta.getProducto().getNombre());
                }
                producto.setReceta(receta);
                receta.setProducto(producto);
                recetaRepository.save(receta);
            } else {
                producto.setReceta(null);
            }
        } else {
            producto.setReceta(null);
            if ((producto.getStock() == null) || (producto.getStock() < 0)) {
                producto.setStock(0.0);
            }
        }

        Empresa empresa = empresaRepositorio.findById(TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));

        producto.setEmpresa(empresa);
        producto.setEstaActivo(true);
        producto.setNombre(producto.getNombre().trim());

        return productoRepository.save(producto);
    }

    @Transactional
    public Optional<Producto> actualizar(Long id, Producto productoActualizado, Long recetaId) {
        validarTenant();

        return productoRepository.findById(id)
                .filter(producto -> producto.getEmpresa().getId().equals(TenantContext.getTenantId()))
                .map(producto -> {
                    if (productoActualizado.getNombre() == null || productoActualizado.getNombre().trim().isEmpty()) {
                        throw new BusinessException("El nombre del producto es obligatorio");
                    }
                    if (productoActualizado.getPrecioVenta() == null || productoActualizado.getPrecioVenta() < 0) {
                        throw new BusinessException("El precio de venta debe ser mayor o igual a 0");
                    }

                    if (!producto.getNombre().equalsIgnoreCase(productoActualizado.getNombre()) &&
                            productoRepository.existsByNombreIgnoreCaseAndEmpresaIdAndEstaActivoTrue(
                                    productoActualizado.getNombre(), TenantContext.getTenantId())) {
                        throw new DuplicateResourceException("Producto", productoActualizado.getNombre());
                    }

                    Boolean tieneRecetaActual = Boolean.TRUE.equals(producto.getTieneReceta());
                    Boolean tieneRecetaNuevo = Boolean.TRUE.equals(productoActualizado.getTieneReceta());

                    if (tieneRecetaActual != tieneRecetaNuevo) {
                        throw new BusinessException("No puedes cambiar el tipo de producto (con/sin receta)");
                    }

                    producto.setNombre(productoActualizado.getNombre().trim());
                    if (productoActualizado.getDescripcion() != null) {
                        producto.setDescripcion(productoActualizado.getDescripcion().trim());
                    }
                    producto.setPrecioCompra(productoActualizado.getPrecioCompra());
                    producto.setPrecioVenta(productoActualizado.getPrecioVenta() != null ? productoActualizado.getPrecioVenta() : 0.0);

                    if (Boolean.TRUE.equals(producto.getTieneReceta())) {
                        producto.setStock(null);
                        if (recetaId != null) {
                            Receta recetaNueva = recetaRepository.findById(recetaId)
                                    .orElseThrow(() -> new BusinessException("Receta no encontrada"));
                            
                            if (recetaNueva.getProducto() != null && 
                                !recetaNueva.getProducto().getId().equals(producto.getId())) {
                                throw new BusinessException("Esta receta ya está asignada a otro producto: " + recetaNueva.getProducto().getNombre());
                            }
                            
                            Receta recetaAnterior = producto.getReceta();
                            if (recetaAnterior != null && !recetaAnterior.getId().equals(recetaId)) {
                                recetaAnterior.setProducto(null);
                                recetaRepository.save(recetaAnterior);
                            }
                            
                            producto.setReceta(recetaNueva);
                            recetaNueva.setProducto(producto);
                            recetaRepository.save(recetaNueva);
                        }
                    } else {
                        if ((productoActualizado.getStock() == null) || (productoActualizado.getStock() < 0)) {
                            producto.setStock(0.0);
                        } else {
                            producto.setStock(productoActualizado.getStock());
                        }
                    }

                    return productoRepository.save(producto);
                });
    }

    @Transactional
    public boolean eliminarLogico(Long id) {
        validarTenant();

        return productoRepository.findById(id)
                .filter(producto -> producto.getEmpresa().getId().equals(TenantContext.getTenantId()))
                .map(producto -> {
                    producto.setEstaActivo(false);
                    productoRepository.save(producto);
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        validarTenant();
        return productoRepository.existsByNombreIgnoreCaseAndEmpresaIdAndEstaActivoTrue(nombre, TenantContext.getTenantId());
    }

    @Transactional
    public void actualizarFlagReceta(Long productoId, boolean tieneReceta) {
        validarTenant();

        productoRepository.findById(productoId)
                .filter(producto -> producto.getEmpresa().getId().equals(TenantContext.getTenantId()))
                .ifPresent(producto -> {
                    producto.setTieneReceta(tieneReceta);
                    productoRepository.save(producto);
                });
    }

    @Transactional
    public void asociarReceta(Long productoId, Long recetaId) {
        validarTenant();

        Producto producto = productoRepository.findById(productoId)
                .filter(p -> p.getEmpresa().getId().equals(TenantContext.getTenantId()))
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));

        Receta receta = recetaRepository.findById(recetaId)
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

        Producto producto = productoRepository.findById(productoId)
                .filter(p -> p.getEmpresa().getId().equals(TenantContext.getTenantId()))
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
        
        productoRepository.findById(productoId)
                .filter(producto -> producto.getEmpresa().getId().equals(TenantContext.getTenantId()))
                .ifPresent(producto -> {
                    producto.setPrecioVenta(nuevoPrecio);
                    productoRepository.save(producto);
                });
    }
}