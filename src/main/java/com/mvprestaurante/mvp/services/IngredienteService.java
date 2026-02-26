package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.repositories.IngredienteRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IngredienteService {

    private final IngredienteRepository ingredienteRepository;

    @Transactional(readOnly = true)
    public Page<Ingrediente> listarActivos(Pageable pageable) {
        return ingredienteRepository.findByEstaActivoTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Ingrediente> buscarPorNombre(String nombre, Pageable pageable) {
        return ingredienteRepository.findByNombreContainingIgnoreCaseAndEstaActivoTrue(nombre, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Ingrediente> obtenerPorId(Long id) {
        return ingredienteRepository.findById(id);
    }

    @Transactional
    public Ingrediente guardar(Ingrediente ingrediente) {
        ingrediente.setEstaActivo(true);
        return ingredienteRepository.save(ingrediente);
    }

    @Transactional
    public Optional<Ingrediente> actualizar(Long id, Ingrediente ingredienteActualizado) {
        return ingredienteRepository.findById(id)
                .map(ingrediente -> {
                    ingrediente.setNombre(ingredienteActualizado.getNombre());
                    ingrediente.setStockDisponible(ingredienteActualizado.getStockDisponible());
                    ingrediente.setPrecioCompra(ingredienteActualizado.getPrecioCompra());
                    ingrediente.setUnidadMedida(ingredienteActualizado.getUnidadMedida());
                    return ingredienteRepository.save(ingrediente);
                });
    }

    @Transactional
    public boolean eliminarLogico(Long id) {
        return ingredienteRepository.findById(id)
                .map(ingrediente -> {
                    ingrediente.setEstaActivo(false);
                    ingredienteRepository.save(ingrediente);
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        return ingredienteRepository.existsByNombreAndEstaActivoTrue(nombre);
    }
}