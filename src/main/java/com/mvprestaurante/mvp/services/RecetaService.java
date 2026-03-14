package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.models.DetalleReceta;
import com.mvprestaurante.mvp.models.Empresa;
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

import java.util.List;
import java.util.Optional;

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
    public Receta guardar(Receta receta) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }

        // Validate unique name within tenant
        if (receta.getId() == null && existePorNombre(receta.getNombre())) {
            throw new RuntimeException("Ya existe una receta con este nombre en su empresa");
        }

        // Set the empresa
        Empresa empresa = empresaRepositorio.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        receta.setEmpresa(empresa);

        receta.setEstaActiva(true);

        // Calcular precio bruto basado en ingredientes si hay
        if (receta.getListaIngredientes() != null && !receta.getListaIngredientes().isEmpty()) {
            // Ensure each detalle has the receta reference
            receta.getListaIngredientes().forEach(detalle -> detalle.setReceta(receta));
            calcularPrecioBruto(receta);
        }

        Receta recetaGuardada = recetaRepository.save(receta);

        // Asociar la receta al producto y marcar que tiene receta
        if (recetaGuardada.getProducto() != null) {
            // Verify the producto belongs to the same tenant?
            // This depends on your ProductoService implementation
            productoService.actualizarFlagReceta(recetaGuardada.getProducto().getId(), true);
        }

        return recetaGuardada;
    }

    @Transactional
    public Optional<Receta> actualizar(Long id, Receta recetaActualizada) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }

        return recetaRepository.findById(id)
                .filter(receta -> receta.getEmpresa().getId().equals(empresaId))
                .map(receta -> {
                    // Validate unique name within tenant (excluding current recipe)
                    if (!receta.getNombre().equalsIgnoreCase(recetaActualizada.getNombre())
                            && existePorNombre(recetaActualizada.getNombre())) {
                        throw new RuntimeException("Ya existe una receta con este nombre en su empresa");
                    }

                    receta.setNombre(recetaActualizada.getNombre());
                    receta.setDescripcion(recetaActualizada.getDescripcion());
                    receta.setPrecioVenta(recetaActualizada.getPrecioVenta());

                    // Actualizar producto si cambió
                    if (recetaActualizada.getProducto() != null &&
                            (receta.getProducto() == null
                                    || !receta.getProducto().getId().equals(recetaActualizada.getProducto().getId()))) {

                        // Si tenía un producto anterior, actualizar su flag
                        if (receta.getProducto() != null) {
                            productoService.actualizarFlagReceta(receta.getProducto().getId(), false);
                        }

                        receta.setProducto(recetaActualizada.getProducto());
                        productoService.actualizarFlagReceta(recetaActualizada.getProducto().getId(), true);
                    }

                    // Actualizar ingredientes si vienen en la petición
                    if (recetaActualizada.getListaIngredientes() != null) {
                        // Eliminar ingredientes anteriores
                        detalleRecetaRepository.deleteByRecetaId(empresaId, receta.getId());

                        // Asignar la receta a cada detalle y guardarlos
                        List<DetalleReceta> nuevosDetalles = recetaActualizada.getListaIngredientes();
                        nuevosDetalles.forEach(detalle -> detalle.setReceta(receta));
                        receta.setListaIngredientes(nuevosDetalles);

                        // Recalcular precio bruto
                        calcularPrecioBruto(receta);
                    }

                    return recetaRepository.save(receta);
                });
    }

    @Transactional
    public boolean eliminarLogico(Long id) {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No tenant found in context");
        }

        return recetaRepository.findById(id)
                .filter(receta -> receta.getEmpresa().getId().equals(empresaId))
                .map(receta -> {
                    receta.setEstaActiva(false);

                    // Actualizar flag del producto asociado
                    if (receta.getProducto() != null) {
                        productoService.actualizarFlagReceta(receta.getProducto().getId(), false);
                    }

                    recetaRepository.save(receta);
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
}