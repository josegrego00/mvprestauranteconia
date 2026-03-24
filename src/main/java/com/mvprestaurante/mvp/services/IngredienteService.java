package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.exceptions.DuplicateResourceException;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;
import com.mvprestaurante.mvp.repositories.IngredienteRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IngredienteService {

    private final IngredienteRepository ingredienteRepository;
    private final EmpresaRepositorio empresaRepositorio;

    private void validarTenant() {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new BusinessException("No se ha identificado la empresa");
        }
    }

    @Transactional(readOnly = true)
    public Page<Ingrediente> listarActivos(Pageable pageable) {
        validarTenant();
        return ingredienteRepository.findByEstaActivoTrue(TenantContext.getTenantId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Ingrediente> buscarPorNombre(String nombre, Pageable pageable) {
        validarTenant();
        return ingredienteRepository.findByNombreContainingIgnoreCaseAndEstaActivoTrue(TenantContext.getTenantId(), nombre, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Ingrediente> obtenerPorId(Long id) {
        validarTenant();
        return ingredienteRepository.findById(id)
                .filter(ingrediente -> ingrediente.getEmpresa().getId().equals(TenantContext.getTenantId()));
    }

    @Transactional
    public Ingrediente guardar(Ingrediente ingrediente) {
        validarTenant();
        
        if (ingrediente.getNombre() == null || ingrediente.getNombre().trim().isEmpty()) {
            throw new BusinessException("El nombre del ingrediente es obligatorio");
        }
        if (ingrediente.getUnidadMedida() == null || ingrediente.getUnidadMedida().trim().isEmpty()) {
            throw new BusinessException("La unidad de medida es obligatoria");
        }

        validarNombreDuplicado(ingrediente.getNombre());

        Empresa empresa = empresaRepositorio.findById(TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));
        ingrediente.setId(null);
        ingrediente.setEstaActivo(true);
        ingrediente.setNombre(ingrediente.getNombre().trim());
        if (ingrediente.getStockDisponible() == null) {
            ingrediente.setStockDisponible(0.0);
        }
        if (ingrediente.getPrecioCompra() == null) {
            ingrediente.setPrecioCompra(0.0);
        }
        ingrediente.setEmpresa(empresa);
        return ingredienteRepository.save(ingrediente);
    }

    @Transactional
    public Optional<Ingrediente> actualizar(Long id, Ingrediente ingredienteActualizado) {
        validarTenant();

        return ingredienteRepository.findById(id)
                .filter(ingrediente -> ingrediente.getEmpresa().getId().equals(TenantContext.getTenantId()))
                .map(ingrediente -> {
                    if (ingredienteActualizado.getNombre() == null || ingredienteActualizado.getNombre().trim().isEmpty()) {
                        throw new BusinessException("El nombre del ingrediente es obligatorio");
                    }
                    if (ingredienteActualizado.getUnidadMedida() == null || ingredienteActualizado.getUnidadMedida().trim().isEmpty()) {
                        throw new BusinessException("La unidad de medida es obligatoria");
                    }

                    if (!ingrediente.getNombre().equalsIgnoreCase(ingredienteActualizado.getNombre())
                            && existePorNombre(ingredienteActualizado.getNombre())) {
                        throw new DuplicateResourceException("Ingrediente", ingredienteActualizado.getNombre());
                    }

                    ingrediente.setNombre(ingredienteActualizado.getNombre().trim());
                    ingrediente.setStockDisponible(ingredienteActualizado.getStockDisponible() != null ? ingredienteActualizado.getStockDisponible() : 0.0);
                    ingrediente.setPrecioCompra(ingredienteActualizado.getPrecioCompra() != null ? ingredienteActualizado.getPrecioCompra() : 0.0);
                    ingrediente.setUnidadMedida(ingredienteActualizado.getUnidadMedida().trim());
                    return ingredienteRepository.save(ingrediente);
                });
    }

    @Transactional
    public boolean eliminarLogico(Long id) {
        validarTenant();

        return ingredienteRepository.findById(id)
                .filter(ingrediente -> ingrediente.getEmpresa().getId().equals(TenantContext.getTenantId()))
                .map(ingrediente -> {
                    if (ingredienteRepository.existsByIngredienteEnReceta(id)) {
                        throw new BusinessException("No se puede desactivar el ingrediente porque está siendo usado en una o más recetas");
                    }
                    ingrediente.setEstaActivo(false);
                    ingredienteRepository.save(ingrediente);
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        validarTenant();
        return ingredienteRepository.existsByNombreAndEstaActivoTrue(TenantContext.getTenantId(), nombre);
    }

    private void validarNombreDuplicado(String nombre) {
        String nombreNormalizado = nombre.trim().toLowerCase();
        if (existePorNombre(nombreNormalizado)) {
            throw new DuplicateResourceException("Ingrediente", nombre);
        }
    }
}
