package com.mvprestaurante.mvp.services;

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
        
        // Validate unique name within tenant
        if (producto.getId() == null && existePorNombre(producto.getNombre())) {
            throw new RuntimeException("Ya existe un producto con este nombre en su empresa");
        }
        
        // Set the empresa
        Empresa empresa = empresaRepositorio.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        producto.setEmpresa(empresa);
        
        producto.setEstaActivo(true);
        producto.setTieneReceta(false); // Por defecto, hasta que se asocie una receta
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
                    // Validate unique name within tenant (excluding current product)
                    if (!producto.getNombre().equalsIgnoreCase(productoActualizado.getNombre()) 
                            && existePorNombre(productoActualizado.getNombre())) {
                        throw new RuntimeException("Ya existe un producto con este nombre en su empresa");
                    }
                    
                    producto.setNombre(productoActualizado.getNombre());
                    producto.setDescripcion(productoActualizado.getDescripcion());
                    producto.setPrecioCompra(productoActualizado.getPrecioCompra());
                    producto.setPrecioVenta(productoActualizado.getPrecioVenta());
                    // No actualizamos tieneReceta aquí, eso se maneja al asociar/desasociar receta
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
        return productoRepository.existsByNombreAndEstaActivoTrue(empresaId, nombre);
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