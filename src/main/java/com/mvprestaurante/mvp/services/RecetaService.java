package com.mvprestaurante.mvp.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

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
import com.mvprestaurante.mvp.utils.AuditLogger;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecetaService {

    private final RecetaRepository recetaRepository;
    private final DetalleRecetaRepository detalleRecetaRepository;
    private final RecetaMapper recetaMapper;
    private final DetalleRecetaMapper detalleRecetaMapper;
    private final EmpresaRepositorio empresaRepositorio;
    private final IngredienteRepository ingredienteRepository;
    private final AuditLogger auditLogger;

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
        auditLogger.logListar("Receta", recetas.getContent().size());
        return recetas.map(recetaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<RecetaDTO> listarSinProducto(Pageable pageable) {
        validarTenant();
        Page<Receta> recetas = recetaRepository.findBySinProducto(TenantContext.getTenantId(), pageable);
        auditLogger.logListar("Receta", recetas.getContent().size());
        return recetas.map(recetaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<RecetaDTO> listarDisponiblesParaProducto(Long productoId, Pageable pageable) {
        validarTenant();
        Page<Receta> recetas = recetaRepository.findDisponiblesParaProducto(TenantContext.getTenantId(), productoId, pageable);
        auditLogger.logListar("Receta", recetas.getContent().size());
        return recetas.map(recetaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<RecetaDTO> buscarPorNombre(String nombre, Pageable pageable) {
        validarTenant();
        Page<Receta> recetas = recetaRepository.findByNombreContainingIgnoreCaseAndEstaActivaTrue(
                TenantContext.getTenantId(), nombre, pageable);
        auditLogger.logListar("Receta", recetas.getContent().size());
        return recetas.map(recetaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public RecetaDTO obtenerPorId(Long id) {
        validarTenant();
        Receta receta = recetaRepository.findByIdAndEmpresaId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Receta no encontrada"));
        auditLogger.logBuscar("Receta", String.valueOf(id));
        return recetaMapper.toDTO(receta);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public RecetaDTO crear(RecetaDTO dto, Long[] ingredientesIds, BigDecimal[] cantidades) {
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
        auditLogger.logCrear("Receta", String.valueOf(guardada.getId()));
        return recetaMapper.toDTO(guardada);
    }

    private void validarIngredientesEnFormulario(Long[] ingredientesIds, BigDecimal[] cantidades) {
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

        Page<DetalleReceta> detalles = detalleRecetaRepository.findByRecetaId(TenantContext.getTenantId(), recetaId, pageable);
        auditLogger.logListar("DetalleReceta", detalles.getContent().size());
        return detalles.map(detalleRecetaMapper::toDTO);
    }

    public void calcularPrecioBruto(Receta receta) {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleReceta detalle : receta.getListaIngredientes()) {
            if (detalle.getIngrediente() != null && detalle.getIngrediente().getPrecioCompra() != null) {
                BigDecimal precio = detalle.getIngrediente().getPrecioCompra();
                BigDecimal cantidad = detalle.getCantidadIngrediente();
                if (precio != null && cantidad != null) {
                    total = total.add(precio.multiply(cantidad));
                }
            }
        }
        receta.setPrecioBruto(total);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public RecetaDTO actualizar(Long id, RecetaDTO dto, Long[] ingredientesIds, BigDecimal[] cantidades) {
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
        auditLogger.logActualizar("Receta", String.valueOf(id));
        return recetaMapper.toDTO(guardada);
    }

    private List<DetalleReceta> construirDetalles(Receta receta, Long[] ingredientesIds, BigDecimal[] cantidades) {
        List<DetalleReceta> detalles = new ArrayList<>();
        for (int i = 0; i < ingredientesIds.length; i++) {
            Long idIngrediente = ingredientesIds[i];
            BigDecimal cantidadIngrediente = cantidades[i];

            if (idIngrediente != null && cantidadIngrediente != null && cantidadIngrediente.compareTo(BigDecimal.ZERO) > 0) {
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
    public BigDecimal calcularStockDisponible(Long recetaId) {
        validarTenant();

        Receta receta = recetaRepository.findByIdAndEmpresaId(recetaId, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Receta no encontrada"));

        if (receta.getListaIngredientes() == null || receta.getListaIngredientes().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal stockMinimo = BigDecimal.valueOf(Double.MAX_VALUE);

        for (DetalleReceta detalle : receta.getListaIngredientes()) {
            BigDecimal stock = BigDecimal.valueOf(detalle.getIngrediente().getStockDisponible());
            BigDecimal cantidad = detalle.getCantidadIngrediente();

            if (stock == null || cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                return BigDecimal.ZERO;
            }

            BigDecimal disponibles = stock.divide(cantidad, 2, RoundingMode.HALF_DOWN);
            if (disponibles.compareTo(stockMinimo) < 0) {
                stockMinimo = disponibles;
            }
        }

        return stockMinimo;
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
        auditLogger.logDesactivar("Receta", String.valueOf(id));
        return true;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public boolean activar(Long id) {
        validarTenant();

        Receta receta = recetaRepository.findByIdAndEmpresaId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Receta no encontrada"));

        receta.setEstaActiva(true);
        recetaRepository.save(receta);
        auditLogger.logActivar("Receta", String.valueOf(id));
        return true;
    }

    @Transactional(readOnly = true)
    public Page<RecetaDTO> listar(Pageable pageable) {
        validarTenant();
        Page<Receta> recetas = recetaRepository.findByEstaActivaTrue(TenantContext.getTenantId(), pageable);
        auditLogger.logListar("Receta", recetas.getContent().size());
        return recetas.map(recetaMapper::toDTO);
    }
}