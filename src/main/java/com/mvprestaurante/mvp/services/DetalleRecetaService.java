package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.models.DetalleReceta;
import com.mvprestaurante.mvp.models.Receta;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.DetalleRecetaRepository;
import com.mvprestaurante.mvp.repositories.RecetaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DetalleRecetaService {

    private final DetalleRecetaRepository detalleRecetaRepository;
    private final RecetaRepository recetaRepository; // ADD THIS to verify receta ownership

    @Transactional
    public DetalleReceta guardar(DetalleReceta detalleReceta) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
        
        // Verify the recipe belongs to this tenant
        if (detalleReceta.getReceta() != null) {
            Receta receta = recetaRepository.findById(detalleReceta.getReceta().getId())
                    .orElseThrow(() -> new RuntimeException("Receta no encontrada"));
            
            if (!receta.getEmpresa().getId().equals(empresaId)) {
                throw new RuntimeException("No tiene permiso para modificar esta receta");
            }
            
            detalleReceta.setReceta(receta);
        }
        
        // Verify the ingredient belongs to this tenant (if needed)
        if (detalleReceta.getIngrediente() != null && 
            !detalleReceta.getIngrediente().getEmpresa().getId().equals(empresaId)) {
            throw new RuntimeException("No tiene permiso para usar este ingrediente");
        }
        
        return detalleRecetaRepository.save(detalleReceta);
    }

    @Transactional
    public List<DetalleReceta> guardarTodos(List<DetalleReceta> detalles) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
        
        // Verify all details belong to the same tenant
        for (DetalleReceta detalle : detalles) {
            if (detalle.getReceta() != null) {
                Receta receta = recetaRepository.findById(detalle.getReceta().getId())
                        .orElseThrow(() -> new RuntimeException("Receta no encontrada"));
                
                if (!receta.getEmpresa().getId().equals(empresaId)) {
                    throw new RuntimeException("No tiene permiso para modificar esta receta");
                }
                
                detalle.setReceta(receta);
            }
            
            if (detalle.getIngrediente() != null && 
                !detalle.getIngrediente().getEmpresa().getId().equals(empresaId)) {
                throw new RuntimeException("No tiene permiso para usar este ingrediente");
            }
        }
        
        return detalleRecetaRepository.saveAll(detalles);
    }

    @Transactional(readOnly = true)
    public Optional<DetalleReceta> obtenerPorId(Long id) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
        
        return detalleRecetaRepository.findById(id)
                .filter(detalle -> detalle.getReceta().getEmpresa().getId().equals(empresaId));
    }

    @Transactional
    public void eliminarPorRecetaId(Long recetaId) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
        
        // Verify the recipe belongs to this tenant
        Receta receta = recetaRepository.findById(recetaId)
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));
        
        if (!receta.getEmpresa().getId().equals(empresaId)) {
            throw new RuntimeException("No tiene permiso para eliminar detalles de esta receta");
        }
        
        detalleRecetaRepository.deleteByRecetaId(empresaId, recetaId);
    }

    @Transactional
    public boolean eliminar(Long id) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
        
        return detalleRecetaRepository.findById(id)
                .filter(detalle -> detalle.getReceta().getEmpresa().getId().equals(empresaId))
                .map(detalle -> {
                    detalleRecetaRepository.delete(detalle);
                    return true;
                })
                .orElse(false);
    }
}