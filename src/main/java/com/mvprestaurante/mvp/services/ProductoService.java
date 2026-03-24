package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.exceptions.DuplicateResourceException;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Producto;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;
import com.mvprestaurante.mvp.repositories.ProductoRepository;

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

    private Long getTenantId() {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
        return empresaId;
    }

    private void validarTenant(Long empresaId) {
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
    }

    @Transactional(readOnly = true)
    public Page<Producto> listarActivos(Pageable pageable) {
        return productoRepository.findByEstaActivoTrue(getTenantId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Producto> buscarPorNombre(String nombre, Pageable pageable) {
        return productoRepository.findByNombreContainingIgnoreCaseAndEstaActivoTrue(getTenantId(), nombre, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Producto> listarProductosConReceta(Pageable pageable) {
        return productoRepository.findByTieneRecetaTrueAndEstaActivoTrue(getTenantId(), pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Producto> obtenerPorId(Long id) {
        Long empresaId = getTenantId();
        return productoRepository.findById(id)
                .filter(producto -> producto.getEmpresa().getId().equals(empresaId));
    }

    @Transactional
    public Producto guardar(Producto producto) {
        Long empresaId = getTenantId();

        if (producto.getId() == null &&
                productoRepository.existsByNombreIgnoreCaseAndEmpresaIdAndEstaActivoTrue(
                        producto.getNombre(), empresaId)) {
            throw new DuplicateResourceException("Producto", producto.getNombre());
        }

        if (producto.getTieneReceta() == null) {
            producto.setTieneReceta(false);
        }
        if (Boolean.TRUE.equals(producto.getTieneReceta())) {
            producto.setStock(null);
        } else {
            producto.setReceta(null);
            if ((producto.getStock() == null) || (producto.getStock() < 0)) {
                producto.setStock(0.0);
            }
        }

        Empresa empresa = empresaRepositorio.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        producto.setEmpresa(empresa);
        producto.setEstaActivo(true);

        return productoRepository.save(producto);
    }

    @Transactional
    public Optional<Producto> actualizar(Long id, Producto productoActualizado) {
        Long empresaId = getTenantId();

        return productoRepository.findById(id)
                .filter(producto -> producto.getEmpresa().getId().equals(empresaId))
                .map(producto -> {
                    if (!producto.getNombre().equalsIgnoreCase(productoActualizado.getNombre()) &&
                            productoRepository.existsByNombreIgnoreCaseAndEmpresaIdAndEstaActivoTrue(
                                    productoActualizado.getNombre(), empresaId)) {
                        throw new DuplicateResourceException("Producto", productoActualizado.getNombre());
                    }

                    Boolean tieneRecetaActual = Boolean.TRUE.equals(producto.getTieneReceta());
                    Boolean tieneRecetaNuevo = Boolean.TRUE.equals(productoActualizado.getTieneReceta());

                    if (tieneRecetaActual != tieneRecetaNuevo) {
                        throw new BusinessException("No puedes cambiar el tipo de producto (con/sin receta)");
                    }

                    producto.setNombre(productoActualizado.getNombre().trim());
                    producto.setDescripcion(productoActualizado.getDescripcion());
                    producto.setPrecioCompra(productoActualizado.getPrecioCompra());
                    if ((productoActualizado.getPrecioVenta() == null) || (productoActualizado.getPrecioVenta() < 0)) {
                        producto.setPrecioVenta(0.0);
                    } else {
                        producto.setPrecioVenta(productoActualizado.getPrecioVenta());
                    }

                    if (producto.getTieneReceta()) {
                        producto.setStock(null);
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
        Long empresaId = getTenantId();

        return productoRepository.findById(id)
                .filter(producto -> producto.getEmpresa().getId().equals(empresaId))
                .map(producto -> {
                    producto.setEstaActivo(false);
                    productoRepository.save(producto);
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        return productoRepository.existsByNombreIgnoreCaseAndEmpresaIdAndEstaActivoTrue(nombre, getTenantId());
    }

    @Transactional
    public void actualizarFlagReceta(Long productoId, boolean tieneReceta) {
        Long empresaId = getTenantId();

        productoRepository.findById(productoId)
                .filter(producto -> producto.getEmpresa().getId().equals(empresaId))
                .ifPresent(producto -> {
                    producto.setTieneReceta(tieneReceta);
                    productoRepository.save(producto);
                });
    }
}