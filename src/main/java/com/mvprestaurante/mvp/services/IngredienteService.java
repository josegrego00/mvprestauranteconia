package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.DTO.IngredienteDTO;
import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.mapper.IngredienteMapper;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.models.Receta;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.DetalleRecetaRepository;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;
import com.mvprestaurante.mvp.repositories.IngredienteRepository;
import com.mvprestaurante.mvp.repositories.RecetaRepository;
import com.mvprestaurante.mvp.utils.AuditLogger;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IngredienteService {

    private final IngredienteRepository ingredienteRepository;
    private final EmpresaRepositorio empresaRepositorio;
    private final RecetaRepository recetaRepository;
    private final DetalleRecetaRepository detalleRecetaRepository;
    private final RecetaService recetaService;
    private final IngredienteMapper ingredienteMapper;
    private final AuditLogger auditLogger;

    private void validarTenant() {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new BusinessException("No se ha identificado la empresa");
        }
    }

    @Transactional(readOnly = true)
    public Page<IngredienteDTO> listarActivos(Pageable pageable) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        Page<Ingrediente> ingredientes = ingredienteRepository.findByEstaActivoTrue(empresaId, pageable);
        auditLogger.logListar("Ingrediente", ingredientes.getContent().size());
        return ingredientes.map(ingredienteMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<IngredienteDTO> buscarPorNombre(String nombre, Pageable pageable) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        Page<Ingrediente> ingredientes = ingredienteRepository
                .findByNombreContainingIgnoreCaseAndEstaActivoTrue(empresaId, nombre, pageable);
        auditLogger.logBuscar("Ingrediente", "Nombre: " + nombre);
        return ingredientes.map(ingredienteMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<IngredienteDTO> obtenerPorId(Long id) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        Optional<IngredienteDTO> resultado = ingredienteRepository.findByIdAndEmpresaId(id, empresaId)
                .map(ingrediente -> {
                    auditLogger.logBuscar("Ingrediente", id.toString());
                    return ingredienteMapper.toDTO(ingrediente);
                });
        return resultado;
    }

    @Transactional
    public IngredienteDTO guardar(IngredienteDTO ingredienteDTO) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        validarNombreDuplicado(ingredienteDTO.getNombre());

        Empresa empresa = empresaRepositorio.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));

        Ingrediente ingrediente = ingredienteMapper.toEntity(ingredienteDTO);

        ingrediente.setEstaActivo(true);
        ingrediente.setNombre(ingredienteDTO.getNombre().trim());
        if (ingrediente.getStockDisponible() == null) {
            ingrediente.setStockDisponible(0.0);
        }
        if (ingrediente.getPrecioCompra() == null) {
            ingrediente.setPrecioCompra(BigDecimal.ZERO);
        }
        ingrediente.setEmpresa(empresa);

        Ingrediente guardado = ingredienteRepository.save(ingrediente);
        auditLogger.logCrear("Ingrediente", guardado.getId().toString());
        return ingredienteMapper.toDTO(guardado);
    }

    @Transactional
    public IngredienteDTO actualizar(Long id, IngredienteDTO ingredienteDTO) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        Ingrediente ingrediente = ingredienteRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new BusinessException("Ingrediente no encontrado"));

        if (!ingrediente.getNombre().equalsIgnoreCase(ingredienteDTO.getNombre())
                && existePorNombre(ingredienteDTO.getNombre())) {
            throw new BusinessException("Ya existe un ingrediente con ese nombre");
        }

        boolean precioCambio = !Objects.equals(
                ingrediente.getPrecioCompra(),
                ingredienteDTO.getPrecioCompra() != null ? ingredienteDTO.getPrecioCompra() : BigDecimal.ZERO);

        ingrediente.setNombre(ingredienteDTO.getNombre().trim());
        ingrediente.setStockDisponible(
                ingredienteDTO.getStockDisponible() != null ? ingredienteDTO.getStockDisponible() : 0.0);
        ingrediente.setPrecioCompra(
                ingredienteDTO.getPrecioCompra() != null ? ingredienteDTO.getPrecioCompra() : BigDecimal.ZERO);
        ingrediente.setUnidadMedida(ingredienteDTO.getUnidadMedida());

        Ingrediente ingredienteGuardado = ingredienteRepository.save(ingrediente);

        if (precioCambio) {
            recalcularRecetasQueUsenIngrediente(ingredienteGuardado);
        }

        auditLogger.logActualizar("Ingrediente", id.toString());
        return ingredienteMapper.toDTO(ingredienteGuardado);
    }

    private void recalcularRecetasQueUsenIngrediente(Ingrediente ingrediente) {
        List<Receta> recetas = detalleRecetaRepository.findRecetasByIngredienteId(
                TenantContext.getTenantId(), ingrediente.getId());

        for (Receta receta : recetas) {
            recetaService.calcularPrecioBruto(receta);
            recetaRepository.save(receta);
        }
    }

    @Transactional
    public void eliminar(Long id) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        Ingrediente ingrediente = ingredienteRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new BusinessException("Ingrediente no encontrado"));

        if (ingredienteRepository.existsByIngredienteEnReceta(empresaId, id)) {
            throw new BusinessException("No se puede eliminar el ingrediente porque está siendo usado en una o más recetas");
        }

        ingrediente.setEstaActivo(false);
        ingredienteRepository.save(ingrediente);
        auditLogger.logDesactivar("Ingrediente", id.toString());
    }

    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        return ingredienteRepository.existsByNombreAndEstaActivoTrue(empresaId, nombre);
    }

    private void validarNombreDuplicado(String nombre) {
        String nombreNormalizado = nombre.trim().toLowerCase();
        if (existePorNombre(nombreNormalizado)) {
            throw new BusinessException("Ya existe un ingrediente con ese nombre");
        }
    }
}