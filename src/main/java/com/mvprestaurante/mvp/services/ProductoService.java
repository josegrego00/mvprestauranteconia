package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.models.Producto;
import com.mvprestaurante.mvp.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public Page<Producto> listarActivos(Pageable pageable) {
        return productoRepository.findByEstaActivoTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Producto> buscarPorNombre(String nombre, Pageable pageable) {
        return productoRepository.findByNombreContainingIgnoreCaseAndEstaActivoTrue(nombre, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Producto> listarProductosConReceta(Pageable pageable) {
        return productoRepository.findByTieneRecetaTrueAndEstaActivoTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Producto> obtenerPorId(Long id) {
        return productoRepository.findById(id);
    }

    @Transactional
    public Producto guardar(Producto producto) {
        producto.setEstaActivo(true);
        producto.setTieneReceta(false); // Por defecto, hasta que se asocie una receta
        return productoRepository.save(producto);
    }

    @Transactional
    public Optional<Producto> actualizar(Long id, Producto productoActualizado) {
        return productoRepository.findById(id)
                .map(producto -> {
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
        return productoRepository.findById(id)
                .map(producto -> {
                    producto.setEstaActivo(false);
                    productoRepository.save(producto);
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        return productoRepository.existsByNombreAndEstaActivoTrue(nombre);
    }

    @Transactional
    public void actualizarFlagReceta(Long productoId, boolean tieneReceta) {
        productoRepository.findById(productoId).ifPresent(producto -> {
            producto.setTieneReceta(tieneReceta);
            productoRepository.save(producto);
        });
    }
}