package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.DTO.DetalleRecetaDTO;
import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.mapper.DetalleRecetaMapper;
import com.mvprestaurante.mvp.models.DetalleReceta;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.models.Receta;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.DetalleRecetaRepository;
import com.mvprestaurante.mvp.repositories.IngredienteRepository;
import com.mvprestaurante.mvp.repositories.RecetaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DetalleRecetaService {

    private final DetalleRecetaRepository detalleRecetaRepository;
    private final DetalleRecetaMapper detalleRecetaMapper;
    private final RecetaRepository recetaRepository;
    private final IngredienteRepository ingredienteRepository;

    private void validarTenant() {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new BusinessException("No se ha identificado la empresa");
        }
    }

    @Transactional(readOnly = true)
    public DetalleRecetaDTO obtenerPorId(Long id) {
        validarTenant();
        DetalleReceta detalle = detalleRecetaRepository.findByIdAndRecetaEmpresaId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Detalle de receta no encontrado"));
        return detalleRecetaMapper.toDTO(detalle);
    }

    @Transactional(readOnly = true)
    public Page<DetalleRecetaDTO> listarPorRecetaId(Long recetaId, Pageable pageable) {
        validarTenant();
        recetaRepository.findByIdAndEmpresaId(recetaId, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Receta no encontrada"));

        Page<DetalleReceta> detalles = detalleRecetaRepository.findByRecetaId(TenantContext.getTenantId(), recetaId, pageable);
        return detalles.map(detalleRecetaMapper::toDTO);
    }

    @Transactional
    public DetalleRecetaDTO guardar(DetalleRecetaDTO dto) {
        validarTenant();

        if (dto.getRecetaId() != null) {
            Receta receta = recetaRepository.findByIdAndEmpresaId(dto.getRecetaId(), TenantContext.getTenantId())
                    .orElseThrow(() -> new BusinessException("Receta no encontrada"));
        }

        if (dto.getIngredienteId() != null) {
            ingredienteRepository.findByIdAndEmpresaId(dto.getIngredienteId(), TenantContext.getTenantId())
                    .orElseThrow(() -> new BusinessException("Ingrediente no encontrado"));
        }

        DetalleReceta detalle = detalleRecetaMapper.toEntity(dto);
        DetalleReceta guardado = detalleRecetaRepository.save(detalle);
        return detalleRecetaMapper.toDTO(guardado);
    }

    @Transactional
    public void eliminarPorRecetaId(Long recetaId) {
        validarTenant();
        recetaRepository.findByIdAndEmpresaId(recetaId, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Receta no encontrada"));
        detalleRecetaRepository.deleteByRecetaId(TenantContext.getTenantId(), recetaId);
    }

    @Transactional
    public boolean eliminar(Long id) {
        validarTenant();

        DetalleReceta detalle = detalleRecetaRepository.findByIdAndRecetaEmpresaId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Detalle de receta no encontrado"));

        detalleRecetaRepository.delete(detalle);
        return true;
    }
}