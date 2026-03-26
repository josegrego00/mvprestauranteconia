package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.exceptions.BusinessException;
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
    private final RecetaRepository recetaRepository;

    private void validarTenant() {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new BusinessException("No se ha identificado la empresa");
        }
    }

    @Transactional
    public DetalleReceta guardar(DetalleReceta detalleReceta) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        if (detalleReceta.getReceta() != null) {
            Receta receta = recetaRepository.findById(detalleReceta.getReceta().getId())
                    .orElseThrow(() -> new BusinessException("Receta no encontrada"));

            if (!receta.getEmpresa().getId().equals(empresaId)) {
                throw new BusinessException("No tiene permiso para modificar esta receta");
            }

            detalleReceta.setReceta(receta);
        }

        if (detalleReceta.getIngrediente() != null &&
                !detalleReceta.getIngrediente().getEmpresa().getId().equals(empresaId)) {
            throw new BusinessException("No tiene permiso para usar este ingrediente");
        }
        
        return detalleRecetaRepository.save(detalleReceta);
    }

    @Transactional
    public List<DetalleReceta> guardarListaDetalleReceta(List<DetalleReceta> detalles) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        for (DetalleReceta detalle : detalles) {
            if (detalle.getReceta() != null) {
                Receta receta = recetaRepository.findById(detalle.getReceta().getId())
                        .orElseThrow(() -> new BusinessException("Receta no encontrada"));

                if (!receta.getEmpresa().getId().equals(empresaId)) {
                    throw new BusinessException("No tiene permiso para modificar esta receta");
                }

                detalle.setReceta(receta);
            }

            if (detalle.getIngrediente() != null &&
                    !detalle.getIngrediente().getEmpresa().getId().equals(empresaId)) {
                throw new BusinessException("No tiene permiso para usar este ingrediente");
            }
        }

        return detalleRecetaRepository.saveAll(detalles);
    }

    @Transactional(readOnly = true)
    public Optional<DetalleReceta> obtenerPorId(Long id) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        return detalleRecetaRepository.findById(id)
                .filter(detalle -> detalle.getReceta().getEmpresa().getId().equals(empresaId));
    }

    @Transactional
    public void eliminarPorRecetaId(Long recetaId) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        Receta receta = recetaRepository.findById(recetaId)
                .orElseThrow(() -> new BusinessException("Receta no encontrada"));

        if (!receta.getEmpresa().getId().equals(empresaId)) {
            throw new BusinessException("No tiene permiso para eliminar detalles de esta receta");
        }

        detalleRecetaRepository.deleteByRecetaId(empresaId, recetaId);
    }

    @Transactional
    public boolean eliminar(Long id) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        return detalleRecetaRepository.findById(id)
                .filter(detalle -> detalle.getReceta().getEmpresa().getId().equals(empresaId))
                .map(detalle -> {
                    detalleRecetaRepository.delete(detalle);
                    return true;
                })
                .orElse(false);
    }

}