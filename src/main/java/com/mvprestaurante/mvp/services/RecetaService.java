package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.models.DetalleReceta;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Producto;
import com.mvprestaurante.mvp.models.Receta;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.DetalleRecetaRepository;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;
import com.mvprestaurante.mvp.repositories.RecetaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecetaService {

    private final RecetaRepository recetaRepository;
    private final DetalleRecetaRepository detalleRecetaRepository;
    private final ProductoService productoService;
    private final EmpresaRepositorio empresaRepositorio; // ADD THIS

    @Transactional(readOnly = true)
    public Page<Receta> listarActivas(Pageable pageable) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
        return recetaRepository.findByEstaActivaTrue(empresaId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Receta> buscarPorNombre(String nombre, Pageable pageable) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
        return recetaRepository.findByNombreContainingIgnoreCaseAndEstaActivaTrue(empresaId, nombre, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Receta> obtenerPorId(Long id) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }

        return recetaRepository.findById(id)
                .filter(receta -> receta.getEmpresa().getId().equals(empresaId));
    }

    @Transactional
    public Receta crearReceta(Receta receta) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
        validarIngredientesUnicos(receta);

        // Validate unique name within tenant
        if (receta.getId() == null && existePorNombre(receta.getNombre())) {
            throw new RuntimeException("Ya existe una receta con este nombre en su empresa");
        }

        // Set the empresa
        Empresa empresa = empresaRepositorio.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        receta.setEmpresa(empresa);

        receta.setEstaActiva(true);

        if (receta.getListaIngredientes() == null || receta.getListaIngredientes().isEmpty()) {
            throw new RuntimeException("La receta debe tener al menos un ingrediente");
        }

        if (receta.getListaIngredientes() != null) {
            for (DetalleReceta detalle : receta.getListaIngredientes()) {
                detalle.setReceta(receta);
            }
        }
        if (receta.getListaIngredientes() == null) {
            receta.setPrecioBruto(0.0);
        } else {
            calcularPrecioBruto(receta);
        }

        Receta recetaGuardada = recetaRepository.save(receta);

        return recetaGuardada;
    }

    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }
        return recetaRepository.existsByNombreAndEstaActivaTrue(empresaId, nombre);
    }

    @Transactional(readOnly = true)
    public Page<DetalleReceta> listarIngredientesDeReceta(Long recetaId, Pageable pageable) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }

        // First verify the receta belongs to this tenant
        Optional<Receta> receta = recetaRepository.findById(recetaId);
        if (receta.isEmpty() || !receta.get().getEmpresa().getId().equals(empresaId)) {
            throw new RuntimeException("Receta no encontrada o no pertenece a su empresa");
        }

        return detalleRecetaRepository.findByRecetaId(empresaId, recetaId, pageable);
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

    private void validarIngredientesUnicos(Receta receta) {
        Set<Long> ids = new HashSet<>();

        for (DetalleReceta d : receta.getListaIngredientes()) {
            Long id = d.getIngrediente().getId();

            if (!ids.add(id)) {
                throw new RuntimeException("Ingrediente duplicado: " + d.getIngrediente().getNombre());
            }
        }
    }
}