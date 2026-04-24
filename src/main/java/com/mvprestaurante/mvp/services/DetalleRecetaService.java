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
import com.mvprestaurante.mvp.utils.AuditLogger;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class DetalleRecetaService {

    private final DetalleRecetaRepository detalleRecetaRepository;
    private final DetalleRecetaMapper detalleRecetaMapper;
    private final RecetaRepository recetaRepository;
    private final IngredienteRepository ingredienteRepository;
    private final AuditLogger auditLogger;

    private void validarTenant() {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new BusinessException("No se ha identificado la empresa");
        }
    }

    @Transactional(readOnly = true)
    public Page<DetalleRecetaDTO> listar(Pageable pageable) {
        validarTenant();
        Page<DetalleReceta> detalles = detalleRecetaRepository.findByRecetaEmpresaId(
                TenantContext.getTenantId(), pageable);
        auditLogger.logListar("DetalleReceta", detalles.getContent().size());
        return detalles.map(detalleRecetaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public DetalleRecetaDTO obtenerPorId(Long id) {
        validarTenant();
        DetalleReceta detalle = detalleRecetaRepository.findByIdAndRecetaEmpresaId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Detalle de receta no encontrado"));
        auditLogger.logBuscar("DetalleReceta", String.valueOf(id));
        return detalleRecetaMapper.toDTO(detalle);
    }

    @Transactional(readOnly = true)
    public Page<DetalleRecetaDTO> listarPorRecetaId(Long recetaId, Pageable pageable) {
        validarTenant();
        recetaRepository.findByIdAndEmpresaId(recetaId, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Receta no encontrada"));

        Page<DetalleReceta> detalles = detalleRecetaRepository.findByRecetaId(TenantContext.getTenantId(), recetaId, pageable);
        auditLogger.logListar("DetalleReceta", detalles.getContent().size());
        return detalles.map(detalleRecetaMapper::toDTO);
    }

    @Transactional
    public DetalleRecetaDTO guardar(DetalleRecetaDTO dto) {
        validarTenant();

        Receta receta = recetaRepository.findByIdAndEmpresaId(dto.getRecetaId(), TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Receta no encontrada"));

        Ingrediente ingrediente = ingredienteRepository.findByIdAndEmpresaId(dto.getIngredienteId(), TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Ingrediente no encontrado"));

        if (detalleRecetaRepository.existsByRecetaAndIngrediente(
                TenantContext.getTenantId(), dto.getRecetaId(), dto.getIngredienteId())) {
            throw new BusinessException("El ingrediente ya está en la receta");
        }

        DetalleReceta detalle = DetalleReceta.builder()
                .nombre(dto.getNombre())
                .receta(receta)
                .ingrediente(ingrediente)
                .cantidadIngrediente(dto.getCantidadIngrediente())
                .build();

        DetalleReceta guardado = detalleRecetaRepository.save(detalle);
        auditLogger.logCrear("DetalleReceta", String.valueOf(guardado.getId()));
        return detalleRecetaMapper.toDTO(guardado);
    }

    @Transactional
    public DetalleRecetaDTO actualizar(Long id, DetalleRecetaDTO dto) {
        validarTenant();

        DetalleReceta detalle = detalleRecetaRepository.findByIdAndRecetaEmpresaId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Detalle de receta no encontrado"));

        if (dto.getIngredienteId() != null && !dto.getIngredienteId().equals(detalle.getIngrediente().getId())) {
            if (detalleRecetaRepository.existsByRecetaAndIngrediente(
                    TenantContext.getTenantId(), detalle.getReceta().getId(), dto.getIngredienteId())) {
                throw new BusinessException("El ingrediente ya está en la receta");
            }
            Ingrediente ingrediente = ingredienteRepository.findByIdAndEmpresaId(dto.getIngredienteId(), TenantContext.getTenantId())
                    .orElseThrow(() -> new BusinessException("Ingrediente no encontrado"));
            detalle.setIngrediente(ingrediente);
        }

        if (dto.getCantidadIngrediente() != null) {
            detalle.setCantidadIngrediente(dto.getCantidadIngrediente());
        }
        if (dto.getNombre() != null) {
            detalle.setNombre(dto.getNombre());
        }

        DetalleReceta actualizado = detalleRecetaRepository.save(detalle);
        auditLogger.logActualizar("DetalleReceta", String.valueOf(id));
        return detalleRecetaMapper.toDTO(actualizado);
    }

    @Transactional
    public boolean eliminar(Long id) {
        validarTenant();

        DetalleReceta detalle = detalleRecetaRepository.findByIdAndRecetaEmpresaId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Detalle de receta no encontrado"));

        detalleRecetaRepository.delete(detalle);
        auditLogger.logEliminar("DetalleReceta", String.valueOf(id));
        return true;
    }

    @Transactional
    public void eliminarPorRecetaId(Long recetaId) {
        validarTenant();
        recetaRepository.findByIdAndEmpresaId(recetaId, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Receta no encontrada"));
        detalleRecetaRepository.deleteByRecetaId(TenantContext.getTenantId(), recetaId);
        auditLogger.logEliminar("DetalleReceta", "receta-" + recetaId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularStockMinimo(Long recetaId) {
        validarTenant();
        Receta receta = recetaRepository.findByIdAndEmpresaId(recetaId, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Receta no encontrada"));

        Page<DetalleReceta> detalles = detalleRecetaRepository.findByRecetaId(
                TenantContext.getTenantId(), recetaId, Pageable.unpaged());

        BigDecimal stockMinimo = BigDecimal.valueOf(Double.MAX_VALUE);

        for (DetalleReceta detalle : detalles.getContent()) {
            BigDecimal stock = BigDecimal.valueOf(detalle.getIngrediente().getStockDisponible());
            BigDecimal cantidad = detalle.getCantidadIngrediente();

            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                return BigDecimal.ZERO;
            }

            BigDecimal disponibles = stock.divide(cantidad, 2, RoundingMode.HALF_DOWN);
            if (disponibles.compareTo(stockMinimo) < 0) {
                stockMinimo = disponibles;
            }
        }

        return stockMinimo;
    }
}