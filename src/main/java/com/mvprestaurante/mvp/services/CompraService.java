package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.models.Compra;
import com.mvprestaurante.mvp.models.DetalleCompra;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.models.Producto;
import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.CompraRepository;
import com.mvprestaurante.mvp.repositories.DetalleCompraRepository;
import com.mvprestaurante.mvp.repositories.IngredienteRepository;
import com.mvprestaurante.mvp.repositories.ProductoRepository;
import com.mvprestaurante.mvp.repositories.UsuarioRepositorio;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final IngredienteRepository ingredienteRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepositorio usuarioRepository;

    private void validarTenant() {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new BusinessException("No se ha identificado la empresa");
        }
    }

    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BusinessException("No hay usuario autenticado");
        }
        String nombreUsuario = auth.getName();
        Long empresaId = TenantContext.getTenantId();
        return usuarioRepository.findByNombreUsuarioAndEmpresa_Id(nombreUsuario, empresaId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<Compra> listarActivos(Pageable pageable) {
        validarTenant();
        return compraRepository.findAllByTenantId(TenantContext.getTenantId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Compra> buscarPorNumero(String numero, Pageable pageable) {
        validarTenant();
        return compraRepository.findByNumeroContainingIgnoreCase(TenantContext.getTenantId(), numero, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Compra> filtrarPorEstado(String estado, Pageable pageable) {
        validarTenant();
        return compraRepository.findByEstado(TenantContext.getTenantId(), estado, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Compra> filtrarPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable) {
        validarTenant();
        return compraRepository.findByFechaBetween(TenantContext.getTenantId(), fechaInicio, fechaFin, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Compra> obtenerPorId(Long id) {
        validarTenant();
        return compraRepository.findById(id)
                .filter(compra -> compra.getUsuario().getEmpresa().getId().equals(TenantContext.getTenantId()));
    }

    @Transactional
    public Compra guardar(Compra compra, List<DetalleCompra> detalles) {
        validarTenant();

        if (detalles == null || detalles.isEmpty()) {
            throw new BusinessException("La compra debe tener al menos un item");
        }

        Usuario usuario = getUsuarioActual();

        compra.setFechaCompra(LocalDateTime.now());
        compra.setUsuario(usuario);
        compra.setEstado("COMPLETADA");

        double subtotal = 0;
        for (DetalleCompra detalle : detalles) {
            double itemSubtotal = detalle.getCantidad() * detalle.getPrecioUnitarioCompra();
            detalle.setSubtotal(itemSubtotal);
            subtotal += itemSubtotal;
        }

        compra.setSubtotal(subtotal);
        compra.setImpuesto(0.0);
        compra.setTotal(subtotal);

        Compra compraGuardada = compraRepository.save(compra);

        for (DetalleCompra detalle : detalles) {
            detalle.setCompra(compraGuardada);
            detalleCompraRepository.save(detalle);

            if ("INGREDIENTE".equals(detalle.getTipoItem()) && detalle.getIngrediente() != null) {
                Long ingredienteId = detalle.getIngrediente().getId();
                Optional<Ingrediente> ingredienteOpt = ingredienteRepository.findById(ingredienteId);
                if (ingredienteOpt.isPresent()) {
                    Ingrediente ingrediente = ingredienteOpt.get();
                    Double nuevoStock = (ingrediente.getStockDisponible() != null ? ingrediente.getStockDisponible() : 0) 
                            + detalle.getCantidad();
                    ingrediente.setStockDisponible(nuevoStock);
                    ingrediente.setPrecioCompra(detalle.getPrecioUnitarioCompra());
                    ingredienteRepository.save(ingrediente);
                }
            } else if ("PRODUCTO".equals(detalle.getTipoItem()) && detalle.getProducto() != null) {
                Long productoId = detalle.getProducto().getId();
                Optional<Producto> productoOpt = productoRepository.findById(productoId);
                if (productoOpt.isPresent()) {
                    Producto producto = productoOpt.get();
                    Double nuevoStock = (producto.getStock() != null ? producto.getStock() : 0) 
                            + detalle.getCantidad();
                    producto.setStock(nuevoStock);
                    producto.setPrecioCompra(detalle.getPrecioUnitarioCompra());
                    productoRepository.save(producto);
                }
            }
        }

        return compraGuardada;
    }

    @Transactional
    public Optional<Compra> anular(Long id) {
        validarTenant();

        return compraRepository.findById(id)
                .filter(compra -> compra.getUsuario().getEmpresa().getId().equals(TenantContext.getTenantId()))
                .filter(compra -> "COMPLETADA".equals(compra.getEstado()))
                .map(compra -> {
                    for (DetalleCompra detalle : compra.getDetallesCompra()) {
                        if ("INGREDIENTE".equals(detalle.getTipoItem()) && detalle.getIngrediente() != null) {
                            Ingrediente ingrediente = detalle.getIngrediente();
                            Double nuevoStock = (ingrediente.getStockDisponible() != null ? ingrediente.getStockDisponible() : 0) 
                                    - detalle.getCantidad();
                            if (nuevoStock < 0) nuevoStock = 0.0;
                            ingrediente.setStockDisponible(nuevoStock);
                            ingredienteRepository.save(ingrediente);
                        } else if ("PRODUCTO".equals(detalle.getTipoItem()) && detalle.getProducto() != null) {
                            Producto producto = detalle.getProducto();
                            Double nuevoStock = (producto.getStock() != null ? producto.getStock() : 0) 
                                    - detalle.getCantidad();
                            if (nuevoStock < 0) nuevoStock = 0.0;
                            producto.setStock(nuevoStock);
                            productoRepository.save(producto);
                        }
                    }

                    compra.setEstado("ANULADA");
                    return compraRepository.save(compra);
                });
    }

    public String generarNumeroCompra() {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "CMP-" + fecha + "-";
        
        Optional<String> maxNumero = compraRepository.findMaxNumeroCompraByPrefix(empresaId, prefix);
        
        int siguienteNumero = 1;
        if (maxNumero.isPresent()) {
            String numeroActual = maxNumero.get();
            String parteNumerica = numeroActual.substring(numeroActual.lastIndexOf("-") + 1);
            try {
                siguienteNumero = Integer.parseInt(parteNumerica) + 1;
            } catch (NumberFormatException e) {
                siguienteNumero = 1;
            }
        }
        
        return prefix + String.format("%04d", siguienteNumero);
    }
}
