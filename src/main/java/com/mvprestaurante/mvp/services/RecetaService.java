package com.mvprestaurante.mvp.services;

import java.math.BigDecimal;

import com.mvprestaurante.mvp.DTO.DetalleRecetaDTO;
import com.mvprestaurante.mvp.DTO.RecetaDTO;
import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.mapper.DetalleRecetaMapper;
import com.mvprestaurante.mvp.mapper.RecetaMapper;
import com.mvprestaurante.mvp.models.DetalleReceta;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.models.Receta;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.DetalleRecetaRepository;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;
import com.mvprestaurante.mvp.repositories.IngredienteRepository;
import com.mvprestaurante.mvp.repositories.RecetaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecetaService {

    private final RecetaRepository recetaRepository;
    private final DetalleRecetaRepository detalleRecetaRepository;
    private final RecetaMapper recetaMapper;
    private final DetalleRecetaMapper detalleRecetaMapper;
    private final EmpresaRepositorio empresaRepositorio;
    private final IngredienteRepository ingredienteRepository;

    private void validarTenant() {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new BusinessException("No se ha identificado la empresa");
        }
    }

    @Transactional(readOnly = true)
    public Page<RecetaDTO> listarActivas(Pageable pageable) {
        validarTenant();
        Page<Receta> recetas = recetaRepository.findByEstaActivaTrue(TenantContext.getTenantId(), pageable);
        return recetas.map(recetaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<RecetaDTO> listarSinProducto(Pageable pageable) {
        validarTenant();
        Page<Receta> recetas = recetaRepository.findBySinProducto(TenantContext.getTenantId(), pageable);
        return recetas.map(recetaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<RecetaDTO> listarDisponiblesParaProducto(Long productoId, Pageable pageable) {
        validarTenant();
        Page<Receta> recetas = recetaRepository.findDisponiblesParaProducto(TenantContext.getTenantId(), productoId,
                pageable);
        return recetas.map(recetaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<RecetaDTO> buscarPorNombre(String nombre, Pageable pageable) {
        validarTenant();
        Page<Receta> recetas = recetaRepository.findByNombreContainingIgnoreCaseAndEstaActivaTrue(
                TenantContext.getTenantId(), nombre, pageable);
        return recetas.map(recetaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public RecetaDTO obtenerPorId(Long id) {
        validarTenant();
        Receta receta = recetaRepository.findByIdAndEmpresaId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Receta no encontrada"));
        return recetaMapper.toDTO(receta);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public RecetaDTO crear(RecetaDTO dto, Long[] ingredientesIds, Double[] cantidades) {
        validarTenant();

        if (ingredientesIds == null || cantidades == null || ingredientesIds.length == 0) {
            throw new BusinessException("La receta debe tener al menos un ingrediente");
        }

        validarIngredientesEnFormulario(ingredientesIds, cantidades);

        if (existePorNombre(dto.getNombre())) {
            throw new BusinessException("Ya existe una receta con ese nombre");
        }

        Empresa empresa = empresaRepositorio.findById(TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));

        Receta receta = recetaMapper.toEntity(dto);
        receta.setEmpresa(empresa);
        receta.setEstaActiva(true);
        receta.setNombre(dto.getNombre().trim());

        if (receta.getDescripcion() != null) {
            receta.setDescripcion(receta.getDescripcion().trim());
        }

        List<DetalleReceta> detalles = construirDetalles(receta, ingredientesIds, cantidades);

        if (detalles.isEmpty()) {
            throw new BusinessException("La receta debe tener al menos un ingrediente");
        }

        receta.setListaIngredientes(detalles);
        calcularPrecioBruto(receta);

        Receta guardada = recetaRepository.save(receta);
        return recetaMapper.toDTO(guardada);
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
    public Page<DetalleRecetaDTO> listarIngredientesDeReceta(Long recetaId, Pageable pageable) {
        validarTenant();

        recetaRepository.findByIdAndEmpresaId(recetaId, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Receta no encontrada"));

        Page<DetalleReceta> detalles = detalleRecetaRepository.findByRecetaId(TenantContext.getTenantId(), recetaId,
                pageable);
        return detalles.map(detalleRecetaMapper::toDTO);
    }

    public void calcularPrecioBruto(Receta receta) {
        BigDecimal total = receta.getListaIngredientes().stream()
                .mapToDouble(detalle -> {
                    if (detalle.getIngrediente() != null && detalle.getIngrediente().getPrecioCompra() != null) {
                        return detalle.getIngrediente().getPrecioCompra().doubleValue() * detalle.getCantidadIngrediente();
                    }
                    return 0.0;
                })
                .mapToObj(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        receta.setPrecioBruto(total);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public RecetaDTO actualizar(Long id, RecetaDTO dto, Long[] ingredientesIds, Double[] cantidades) {
        validarTenant();

        Receta recetaExistente = recetaRepository.findByIdAndEmpresaId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Receta no encontrada"));

        if (!recetaExistente.getNombre().equalsIgnoreCase(dto.getNombre()) && existePorNombre(dto.getNombre())) {
            throw new BusinessException("Ya existe una receta con ese nombre");
        }

        if (ingredientesIds == null || cantidades == null || ingredientesIds.length == 0) {
            throw new BusinessException("La receta debe tener al menos un ingrediente");
        }

        validarIngredientesEnFormulario(ingredientesIds, cantidades);

        recetaExistente.setNombre(dto.getNombre().trim());
        if (dto.getDescripcion() != null) {
            recetaExistente.setDescripcion(dto.getDescripcion().trim());
        }
        recetaExistente.setPrecioVenta(dto.getPrecioVenta());

        List<DetalleReceta> nuevosDetalles = construirDetalles(recetaExistente, ingredientesIds, cantidades);

        if (nuevosDetalles.isEmpty()) {
            throw new BusinessException("La receta debe tener al menos un ingrediente");
        }

        recetaExistente.getListaIngredientes().clear();
        recetaExistente.getListaIngredientes().addAll(nuevosDetalles);

        calcularPrecioBruto(recetaExistente);

        Receta guardada = recetaRepository.save(recetaExistente);
        return recetaMapper.toDTO(guardada);
    }

    private List<DetalleReceta> construirDetalles(Receta receta, Long[] ingredientesIds, Double[] cantidades) {
        List<DetalleReceta> detalles = new ArrayList<>();
        for (int i = 0; i < ingredientesIds.length; i++) {
            Long idIngrediente = ingredientesIds[i];
            Double cantidadIngrediente = cantidades[i];

            if (idIngrediente != null && cantidadIngrediente != null && cantidadIngrediente > 0) {
                Ingrediente ingrediente = ingredienteRepository
                        .findByIdAndEmpresaId(idIngrediente, TenantContext.getTenantId())
                        .orElseThrow(() -> new BusinessException("Ingrediente no encontrado: " + idIngrediente));

                DetalleReceta detalle = DetalleReceta.builder()
                        .receta(receta)
                        .ingrediente(ingrediente)
                        .cantidadIngrediente(cantidadIngrediente)
                        .build();
                detalles.add(detalle);
            }
        }
        return detalles;
    }

    @Transactional(readOnly = true)
    public Double calcularStockDisponible(Long recetaId) {
        validarTenant();

        Receta receta = recetaRepository.findByIdAndEmpresaId(recetaId, TenantContext.getTenantId())
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
    @PreAuthorize("hasRole('ADMIN')")
    public boolean eliminar(Long id) {
        validarTenant();

        Receta receta = recetaRepository.findByIdAndEmpresaId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Receta no encontrada"));

        if (receta.getProducto() != null) {
            throw new BusinessException("No se puede eliminar la receta porque está asociada a un producto");
        }

        receta.setEstaActiva(false);
        recetaRepository.save(receta);
        return true;
    }
}