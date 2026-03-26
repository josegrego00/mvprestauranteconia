package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.exceptions.DuplicateResourceException;
import com.mvprestaurante.mvp.models.DetalleReceta;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.models.Producto;
import com.mvprestaurante.mvp.models.Receta;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.DetalleRecetaRepository;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;
import com.mvprestaurante.mvp.repositories.IngredienteRepository;
import com.mvprestaurante.mvp.repositories.RecetaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final EmpresaRepositorio empresaRepositorio;
    private final IngredienteRepository ingredienteRepository;

    private void validarTenant() {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new BusinessException("No se ha identificado la empresa");
        }
    }

    @Transactional(readOnly = true)
    public Page<Receta> listarActivas(Pageable pageable) {
        validarTenant();
        return recetaRepository.findByEstaActivaTrue(TenantContext.getTenantId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Receta> listarSinProducto(Pageable pageable) {
        validarTenant();
        return recetaRepository.findBySinProducto(TenantContext.getTenantId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Receta> listarDisponiblesParaProducto(Long productoId, Pageable pageable) {
        validarTenant();
        return recetaRepository.findDisponiblesParaProducto(TenantContext.getTenantId(), productoId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Receta> buscarPorNombre(String nombre, Pageable pageable) {
        validarTenant();
        return recetaRepository.findByNombreContainingIgnoreCaseAndEstaActivaTrue(TenantContext.getTenantId(), nombre,
                pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Receta> obtenerPorId(Long id) {
        validarTenant();
        return recetaRepository.findById(id)
                .filter(receta -> receta.getEmpresa().getId().equals(TenantContext.getTenantId()));
    }

    @Transactional
    public Receta crearRecetaConIngredientes(Receta receta, Long[] ingredientesIds, Double[] cantidades) {
        validarTenant();

        if (receta.getNombre() == null || receta.getNombre().trim().isEmpty()) {
            throw new BusinessException("El nombre de la receta es obligatorio");
        }

        if (ingredientesIds == null || cantidades == null || ingredientesIds.length == 0) {
            throw new BusinessException("La receta debe tener al menos un ingrediente");
        }

        validarIngredientesEnFormulario(ingredientesIds, cantidades);

        if (receta.getId() == null && existePorNombre(receta.getNombre())) {
            throw new DuplicateResourceException("Receta", receta.getNombre());
        }

        Empresa empresa = empresaRepositorio.findById(TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));

        receta.setEmpresa(empresa);
        receta.setEstaActiva(true);
        receta.setNombre(receta.getNombre().trim());

        if (receta.getDescripcion() != null) {
            receta.setDescripcion(receta.getDescripcion().trim());
        }

        List<DetalleReceta> detalles = new ArrayList<>();
        for (int i = 0; i < ingredientesIds.length; i++) {
            Long idIngrediente = ingredientesIds[i];
            Double cantidadIngrediente = cantidades[i];

            if (idIngrediente != null && cantidadIngrediente != null && cantidadIngrediente > 0) {
                Ingrediente ingrediente = ingredienteRepository.findById(idIngrediente)
                        .filter(ing -> ing.getEmpresa().getId().equals(TenantContext.getTenantId()))
                        .orElseThrow(() -> new BusinessException("Ingrediente no encontrado: " + idIngrediente));

                DetalleReceta detalle = DetalleReceta.builder()
                        .receta(receta)
                        .ingrediente(ingrediente)
                        .cantidadIngrediente(cantidadIngrediente)
                        .build();
                detalles.add(detalle);
            }
        }

        if (detalles.isEmpty()) {
            throw new BusinessException("La receta debe tener al menos un ingrediente");
        }

        receta.setListaIngredientes(detalles);

        calcularPrecioBruto(receta);

        return recetaRepository.save(receta);
    }

    private void validarIngredientesEnFormulario(Long[] ingredientesIds, Double[] cantidades) {
        if (ingredientesIds == null || cantidades == null || ingredientesIds.length != cantidades.length) {
            throw new BusinessException("Los datos de ingredientes no son válidos");
        }
    }

    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        validarTenant();
        return recetaRepository.existsByNombreAndEstaActivaTrue(TenantContext.getTenantId(), nombre);
    }

    @Transactional(readOnly = true)
    public Page<DetalleReceta> listarIngredientesDeReceta(Long recetaId, Pageable pageable) {
        validarTenant();

        Optional<Receta> receta = recetaRepository.findById(recetaId);
        if (receta.isEmpty() || !receta.get().getEmpresa().getId().equals(TenantContext.getTenantId())) {
            throw new BusinessException("Receta no encontrada o no pertenece a su empresa");
        }

        return detalleRecetaRepository.findByRecetaId(TenantContext.getTenantId(), recetaId, pageable);
    }

    public void calcularPrecioBruto(Receta receta) {
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
            if (d.getIngrediente() == null) {
                throw new BusinessException("Todos los ingredientes deben estar seleccionados");
            }
            Long id = d.getIngrediente().getId();

            if (!ids.add(id)) {
                throw new BusinessException("Ingrediente duplicado: " + d.getIngrediente().getNombre());
            }
            if (d.getCantidadIngrediente() == null || d.getCantidadIngrediente() <= 0) {
                throw new BusinessException("La cantidad del ingrediente debe ser mayor a 0");
            }
        }
    }

    @Transactional
    public Receta actualizarConIngredientes(Long id, Receta recetaActualizada, Long[] ingredientesIds,
            Double[] cantidades) {
        validarTenant();

        Receta recetaExistente = recetaRepository.findById(id)
                .filter(r -> r.getEmpresa().getId().equals(TenantContext.getTenantId()))
                .orElseThrow(() -> new BusinessException("Receta no encontrada"));

        if (recetaActualizada.getNombre() == null || recetaActualizada.getNombre().trim().isEmpty()) {
            throw new BusinessException("El nombre de la receta es obligatorio");
        }

        if (!recetaExistente.getNombre().equalsIgnoreCase(recetaActualizada.getNombre())
                && existePorNombre(recetaActualizada.getNombre())) {
            throw new DuplicateResourceException("Receta", recetaActualizada.getNombre());
        }

        if (ingredientesIds == null || cantidades == null || ingredientesIds.length == 0) {
            throw new BusinessException("La receta debe tener al menos un ingrediente");
        }

        validarIngredientesEnFormulario(ingredientesIds, cantidades);

        recetaExistente.setNombre(recetaActualizada.getNombre().trim());
        if (recetaActualizada.getDescripcion() != null) {
            recetaExistente.setDescripcion(recetaActualizada.getDescripcion().trim());
        }
        recetaExistente.setPrecioVenta(recetaActualizada.getPrecioVenta());

        List<DetalleReceta> nuevosDetalles = new ArrayList<>();
        for (int i = 0; i < ingredientesIds.length; i++) {
            Long idIngrediente = ingredientesIds[i];
            Double cantidadIngrediente = cantidades[i];

            if (idIngrediente != null && cantidadIngrediente != null && cantidadIngrediente > 0) {
                Ingrediente ingrediente = ingredienteRepository.findById(idIngrediente)
                        .filter(ing -> ing.getEmpresa().getId().equals(TenantContext.getTenantId()))
                        .orElseThrow(() -> new BusinessException("Ingrediente no encontrado: " + idIngrediente));

                DetalleReceta detalle = DetalleReceta.builder()
                        .receta(recetaExistente)
                        .ingrediente(ingrediente)
                        .cantidadIngrediente(cantidadIngrediente)
                        .build();
                nuevosDetalles.add(detalle);
            }
        }

        if (nuevosDetalles.isEmpty()) {
            throw new BusinessException("La receta debe tener al menos un ingrediente");
        }

        recetaExistente.getListaIngredientes().clear();
        recetaExistente.getListaIngredientes().addAll(nuevosDetalles);

        calcularPrecioBruto(recetaExistente);

        return recetaRepository.save(recetaExistente);
    }

    @Transactional(readOnly = true)
    public Double calcularStockDisponible(Long recetaId) {
        validarTenant();

        Receta receta = recetaRepository.findById(recetaId)
                .filter(r -> r.getEmpresa().getId().equals(TenantContext.getTenantId()))
                .orElseThrow(() -> new BusinessException("Receta no encontrada"));

        if (receta.getListaIngredientes() == null || receta.getListaIngredientes().isEmpty()) {
            return 0.0;
        }

        return receta.getListaIngredientes().stream()
                .mapToDouble(detalle -> {
                    Double stock = detalle.getIngrediente().getStockDisponible();
                    Double cantidad = detalle.getCantidadIngrediente();
                    if (stock == null || cantidad == null || cantidad == 0) {
                        return Double.MAX_VALUE;
                    }
                    return stock / cantidad;
                })
                .min()
                .orElse(0.0);
    }

    @Transactional
    public boolean eliminarLogico(Long id) {
        validarTenant();

        return recetaRepository.findById(id)
                .filter(receta -> receta.getEmpresa().getId().equals(TenantContext.getTenantId()))
                .map(receta -> {
                    if (receta.getProducto() != null) {
                        throw new BusinessException(
                                "No se puede eliminar la receta porque está asociada a un producto");
                    }
                    receta.setEstaActiva(false);
                    recetaRepository.save(receta);
                    return true;
                })
                .orElse(false);
    }
}