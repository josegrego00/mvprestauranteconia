package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.models.DetalleReceta;
import com.mvprestaurante.mvp.models.Receta;
import com.mvprestaurante.mvp.repositories.DetalleRecetaRepository;
import com.mvprestaurante.mvp.repositories.RecetaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecetaService {

    private final RecetaRepository recetaRepository;

    private final DetalleRecetaRepository detalleRecetaRepository;

    private final ProductoService productoService;

    @Transactional(readOnly = true)
    public Page<Receta> listarActivas(Pageable pageable) {
        return recetaRepository.findByEstaActivaTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Receta> buscarPorNombre(String nombre, Pageable pageable) {
        return recetaRepository.findByNombreContainingIgnoreCaseAndEstaActivaTrue(nombre, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Receta> obtenerPorId(Long id) {
        return recetaRepository.findById(id);
    }

    @Transactional
    public Receta guardar(Receta receta) {
        receta.setEstaActiva(true);

        // Calcular precio bruto basado en ingredientes si hay
        if (receta.getListaIngredientes() != null && !receta.getListaIngredientes().isEmpty()) {
            calcularPrecioBruto(receta);
        }

        Receta recetaGuardada = recetaRepository.save(receta);

        // Asociar la receta al producto y marcar que tiene receta
        if (recetaGuardada.getProducto() != null) {
            productoService.actualizarFlagReceta(recetaGuardada.getProducto().getId(), true);
        }

        return recetaGuardada;
    }

    @Transactional
    public Optional<Receta> actualizar(Long id, Receta recetaActualizada) {
        return recetaRepository.findById(id)
                .map(receta -> {
                    receta.setNombre(recetaActualizada.getNombre());
                    receta.setDescripcion(recetaActualizada.getDescripcion());
                    receta.setPrecioVenta(recetaActualizada.getPrecioVenta());

                    // Actualizar producto si cambió
                    if (recetaActualizada.getProducto() != null &&
                            (receta.getProducto() == null
                                    || !receta.getProducto().getId().equals(recetaActualizada.getProducto().getId()))) {

                        // Si tenía un producto anterior, actualizar su flag
                        if (receta.getProducto() != null) {
                            productoService.actualizarFlagReceta(receta.getProducto().getId(), false);
                        }

                        receta.setProducto(recetaActualizada.getProducto());
                        productoService.actualizarFlagReceta(recetaActualizada.getProducto().getId(), true);
                    }

                    // Actualizar ingredientes si vienen en la petición
                    if (recetaActualizada.getListaIngredientes() != null) {
                        // Eliminar ingredientes anteriores
                        detalleRecetaRepository.deleteByRecetaId(receta.getId());

                        // Asignar la receta a cada detalle y guardarlos
                        List<DetalleReceta> nuevosDetalles = recetaActualizada.getListaIngredientes();
                        nuevosDetalles.forEach(detalle -> detalle.setReceta(receta));
                        receta.setListaIngredientes(nuevosDetalles);

                        // Recalcular precio bruto
                        calcularPrecioBruto(receta);
                    }

                    return recetaRepository.save(receta);
                });
    }

    @Transactional
    public boolean eliminarLogico(Long id) {
        return recetaRepository.findById(id)
                .map(receta -> {
                    receta.setEstaActiva(false);

                    // Actualizar flag del producto asociado
                    if (receta.getProducto() != null) {
                        productoService.actualizarFlagReceta(receta.getProducto().getId(), false);
                    }

                    recetaRepository.save(receta);
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        return recetaRepository.existsByNombreAndEstaActivaTrue(nombre);
    }

    @Transactional(readOnly = true)
    public Page<DetalleReceta> listarIngredientesDeReceta(Long recetaId, Pageable pageable) {
        return detalleRecetaRepository.findByRecetaId(recetaId, pageable);
    }

    private void calcularPrecioBruto(Receta receta) {
        double total = receta.getListaIngredientes().stream()
                .mapToDouble(detalle -> {
                    if (detalle.getIngrediente() != null && detalle.getIngrediente().getPrecioCompra() != null) {
                        return detalle.getIngrediente().getPrecioCompra() * detalle.getCantidadIngrediente();
                    }
                    return 0.0;
                })
                .sum();
        receta.setPrecioBruto(total);
    }
}