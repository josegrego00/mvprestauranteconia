package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.exceptions.DuplicateResourceException;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.models.Receta;
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

    @Transactional(readOnly = true)
    public Page<Ingrediente> listarActivos(Pageable pageable) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
        return ingredienteRepository.findByEstaActivoTrue(empresaId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Ingrediente> buscarPorNombre(String nombre, Pageable pageable) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
        return ingredienteRepository.findByNombreContainingIgnoreCaseAndEstaActivoTrue(empresaId, nombre, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Ingrediente> obtenerPorId(Long id) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }

        return ingredienteRepository.findById(id)
                .filter(ingrediente -> ingrediente.getEmpresa().getId().equals(empresaId));
    }

    @Transactional
    public Ingrediente guardar(Ingrediente ingrediente) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }

        // Validate unique name within tenant
        validarNombreDuplicado(ingrediente.getNombre());

        Empresa empresa = empresaRepositorio.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        ingrediente.setId(null); // asegura que siempre sea creación
        ingrediente.setEstaActivo(true);
        ingrediente.setEmpresa(empresa);
        return ingredienteRepository.save(ingrediente);
    }

    @Transactional
    public Optional<Ingrediente> actualizar(Long id, Ingrediente ingredienteActualizado) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }

        return ingredienteRepository.findById(id)
                .filter(ingrediente -> ingrediente.getEmpresa().getId().equals(empresaId))
                .map(ingrediente -> {
                    // Validate unique name within tenant (excluding current ingredient)
                    if (!ingrediente.getNombre().equalsIgnoreCase(ingredienteActualizado.getNombre())
                            && existePorNombre(ingredienteActualizado.getNombre())) {
                        throw new RuntimeException("Ya existe un ingrediente con este nombre en su empresa");
                    }

                    ingrediente.setNombre(ingredienteActualizado.getNombre());
                    ingrediente.setStockDisponible(ingredienteActualizado.getStockDisponible());
                    ingrediente.setPrecioCompra(ingredienteActualizado.getPrecioCompra());
                    ingrediente.setUnidadMedida(ingredienteActualizado.getUnidadMedida());
                    return ingredienteRepository.save(ingrediente);
                });
    }

    @Transactional
    public boolean eliminarLogico(Long id) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }

        return ingredienteRepository.findById(id)
                .filter(ingrediente -> ingrediente.getEmpresa().getId().equals(empresaId))
                .map(ingrediente -> {
                    ingrediente.setEstaActivo(false);
                    ingredienteRepository.save(ingrediente);
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
        return ingredienteRepository.existsByNombreAndEstaActivoTrue(empresaId, nombre);
    }

    private void validarNombreDuplicado(String nombre) {
        String nombreNormalizado = nombre.trim().toLowerCase();
        if (existePorNombre(nombreNormalizado)) {
            throw new DuplicateResourceException(
                    "Ingrediente",
                    "Ya existe un ingrediente con el nombre: " + nombre);
        }
    }
}