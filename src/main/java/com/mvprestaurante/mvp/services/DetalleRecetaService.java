package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.models.DetalleReceta;
import com.mvprestaurante.mvp.repositories.DetalleRecetaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DetalleRecetaService {

    private final DetalleRecetaRepository detalleRecetaRepository;

    @Transactional
    public DetalleReceta guardar(DetalleReceta detalleReceta) {
        return detalleRecetaRepository.save(detalleReceta);
    }

    @Transactional
    public List<DetalleReceta> guardarTodos(List<DetalleReceta> detalles) {
        return detalleRecetaRepository.saveAll(detalles);
    }

    @Transactional(readOnly = true)
    public Optional<DetalleReceta> obtenerPorId(Long id) {
        return detalleRecetaRepository.findById(id);
    }

    @Transactional
    public void eliminarPorRecetaId(Long recetaId) {
        detalleRecetaRepository.deleteByRecetaId(recetaId);
    }

    @Transactional
    public boolean eliminar(Long id) {
        return detalleRecetaRepository.findById(id)
                .map(detalle -> {
                    detalleRecetaRepository.delete(detalle);
                    return true;
                })
                .orElse(false);
    }
}