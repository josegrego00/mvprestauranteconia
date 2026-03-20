package com.mvprestaurante.mvp.services;

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
    private final EmpresaRepositorio empresaRepositorio; // ADD THIS

    @Transactional(readOnly = true)
    public Page<Producto> listarActivos(Pageable pageable) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
        return productoRepository.findByEstaActivoTrue(empresaId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Producto> buscarPorNombre(String nombre, Pageable pageable) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
        return productoRepository.findByNombreContainingIgnoreCaseAndEstaActivoTrue(empresaId, nombre, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Producto> listarProductosConReceta(Pageable pageable) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
        return productoRepository.findByTieneRecetaTrueAndEstaActivoTrue(empresaId, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Producto> obtenerPorId(Long id) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }

        return productoRepository.findById(id)
                .filter(producto -> producto.getEmpresa().getId().equals(empresaId));
    }

    @Transactional
    public Producto guardar(Producto producto) {

        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }

        // 🔥 VALIDACIÓN DUPLICADO
        if (producto.getId() == null &&
                productoRepository.existsByNombreIgnoreCaseAndEmpresaIdAndEstaActivoTrue(
                        producto.getNombre(), empresaId)) {

            throw new DuplicateResourceException("Producto", producto.getNombre());
        }

        // 🔥 REGLA DE NEGOCIO: RECETA vs STOCK
        if (Boolean.TRUE.equals(producto.getTieneReceta())) {
            producto.setStock(null); // producto preparado → no maneja stock directo
        } else {
            if (producto.getStock() == null) {
                producto.setStock(0.0); // producto de reventa → necesita stock
            }
        }

        // 🔥 ASIGNAR EMPRESA
        Empresa empresa = empresaRepositorio.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        producto.setEmpresa(empresa);
        producto.setEstaActivo(true);

        return productoRepository.save(producto);
    }

    @Transactional
    public Optional<Producto> actualizar(Long id, Producto productoActualizado) {

        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }

        return productoRepository.findById(id)
                .filter(producto -> producto.getEmpresa().getId().equals(empresaId))
                .map(producto -> {

                    // 🔥 VALIDACIÓN DUPLICADO (EXCLUYENDO EL MISMO)
                    if (!producto.getNombre().equalsIgnoreCase(productoActualizado.getNombre()) &&
                            productoRepository.existsByNombreIgnoreCaseAndEmpresaIdAndEstaActivoTrue(
                                    productoActualizado.getNombre(), empresaId)) {

                        throw new DuplicateResourceException("Producto", productoActualizado.getNombre());
                    }

                    // 🔥 ACTUALIZACIÓN DE CAMPOS
                    producto.setNombre(productoActualizado.getNombre());
                    producto.setDescripcion(productoActualizado.getDescripcion());
                    producto.setPrecioCompra(productoActualizado.getPrecioCompra());
                    producto.setPrecioVenta(productoActualizado.getPrecioVenta());

                    // 🔥 REGLA DE NEGOCIO (IMPORTANTE)
                    if (Boolean.TRUE.equals(producto.getTieneReceta())) {
                        producto.setStock(null);
                    } else {
                        if (productoActualizado.getStock() != null) {
                            producto.setStock(productoActualizado.getStock());
                        }
                    }

                    return productoRepository.save(producto);
                });
    }

    @Transactional
    public boolean eliminarLogico(Long id) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }

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
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
        return productoRepository.existsByNombreIgnoreCaseAndEmpresaIdAndEstaActivoTrue(nombre, empresaId);
    }

    @Transactional
    public void actualizarFlagReceta(Long productoId, boolean tieneReceta) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }

        productoRepository.findById(productoId)
                .filter(producto -> producto.getEmpresa().getId().equals(empresaId))
                .ifPresent(producto -> {
                    producto.setTieneReceta(tieneReceta);
                    productoRepository.save(producto);
                });
    }
}