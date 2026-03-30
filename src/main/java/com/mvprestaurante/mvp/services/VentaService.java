package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.models.Cliente;
import com.mvprestaurante.mvp.models.DetalleReceta;
import com.mvprestaurante.mvp.models.DetalleVenta;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.models.Producto;
import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.models.Venta;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.ClienteRepositorio;
import com.mvprestaurante.mvp.repositories.DetalleVentaRepository;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;
import com.mvprestaurante.mvp.repositories.IngredienteRepository;
import com.mvprestaurante.mvp.repositories.ProductoRepository;
import com.mvprestaurante.mvp.repositories.RecetaRepository;
import com.mvprestaurante.mvp.repositories.UsuarioRepositorio;
import com.mvprestaurante.mvp.repositories.VentaRepository;

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
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepositorio usuarioRepository;
    private final ClienteRepositorio clienteRepository;
    private final EmpresaRepositorio empresaRepository;
    private final RecetaRepository recetaRepository;
    private final IngredienteRepository ingredienteRepository;
    private final MovimientoStockService movimientoStockService;

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

    private Double parseDoubleSafe(String value) {
        if (value == null || value.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    @Transactional(readOnly = true)
    public Page<Venta> buscar(String search, String fechaInicio, String fechaFin, Pageable pageable) {
        validarTenant();
        
        if (search != null && !search.isEmpty()) {
            return ventaRepository.findByNumeroContainingIgnoreCase(TenantContext.getTenantId(), search, pageable);
        } else if (fechaInicio != null && !fechaInicio.isEmpty() && fechaFin != null && !fechaFin.isEmpty()) {
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio + "T00:00:00");
            LocalDateTime fin = LocalDateTime.parse(fechaFin + "T23:59:59");
            return ventaRepository.findByFechaBetween(TenantContext.getTenantId(), inicio, fin, pageable);
        } else {
            return ventaRepository.findAllByTenantId(TenantContext.getTenantId(), pageable);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Venta> obtenerPorId(Long id) {
        validarTenant();
        return ventaRepository.findByIdWithDetails(id)
                .filter(venta -> venta.getEmpresa().getId().equals(TenantContext.getTenantId()));
    }

    @Transactional
    public Venta guardarDesdeFormulario(Venta venta, Map<String, String> allParams) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        List<DetalleVenta> detalles = new ArrayList<>();

        for (String key : allParams.keySet()) {
            if (key.startsWith("producto[")) {
                String index = key.substring(9, key.length() - 1);
                String productoValue = allParams.get(key);
                String cantidadParam = "cantidad[" + index + "]";
                
                if (productoValue != null && !productoValue.isEmpty() && 
                    allParams.containsKey(cantidadParam)) {
                    
                    Long productoId = Long.parseLong(productoValue);
                    Integer cantidad = Integer.parseInt(allParams.get(cantidadParam));
                    
                    if (cantidad == null || cantidad <= 0) continue;

                    Optional<Producto> productoOpt = productoRepository.findById(productoId);
                    if (productoOpt.isEmpty()) {
                        throw new BusinessException("Producto no encontrado");
                    }

                    Producto producto = productoOpt.get();
                    
                    Double stockEstimado = 0.0;
                    
                    if (Boolean.TRUE.equals(producto.getTieneReceta())) {
                        var recetaOpt = producto.getReceta();
                        if (recetaOpt != null) {
                            stockEstimado = calcularStockDisponibleReceta(recetaOpt.getId());
                        }
                    } else {
                        stockEstimado = producto.getStock() != null ? producto.getStock() : 0.0;
                    }
                    
                    if (stockEstimado < cantidad) {
                        String tipoStock = Boolean.TRUE.equals(producto.getTieneReceta()) ? "Stock estimado" : "Stock";
                        throw new BusinessException("Stock insuficiente para '" + producto.getNombre() + "'. " + tipoStock + " actual: " + stockEstimado.intValue());
                    }

                    DetalleVenta detalle = new DetalleVenta();
                    detalle.setProducto(producto);
                    detalle.setCantidad(cantidad);
                    detalle.setPrecioUnitario(producto.getPrecioVenta());
                    detalle.setSubtotal(cantidad * producto.getPrecioVenta());
                    detalles.add(detalle);
                }
            }
        }

        return guardar(venta, detalles, allParams);
    }

    @Transactional(readOnly = true)
    public Double calcularStockDisponibleReceta(Long recetaId) {
        var receta = recetaRepository.findById(recetaId)
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
    public Venta guardar(Venta venta, List<DetalleVenta> detalles, Map<String, String> allParams) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        if (detalles == null || detalles.isEmpty()) {
            throw new BusinessException("La venta debe tener al menos un producto");
        }

        Usuario usuario = getUsuarioActual();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));

        Cliente cliente = obtenerOClienteDefault(allParams.get("clienteId"), empresaId);

        String metodoPago = allParams.get("metodoPago");
        Double cantidadPagada = 0.0;
        
        if ("MIXTO".equals(metodoPago)) {
            Double efectivo = parseDoubleSafe(allParams.get("pagoEfectivo"));
            Double tarjeta = parseDoubleSafe(allParams.get("pagoTarjeta"));
            Double transferencia = parseDoubleSafe(allParams.get("pagoTransferencia"));
            cantidadPagada = efectivo + tarjeta + transferencia;
            
            venta.setPagoEfectivo(efectivo);
            venta.setPagoTarjeta(tarjeta);
            venta.setPagoTransferencia(transferencia);
        } else if (allParams.containsKey("cantidadPagada") && allParams.get("cantidadPagada") != null) {
            cantidadPagada = Double.parseDouble(allParams.get("cantidadPagada"));
        }

        Double total = detalles.stream().mapToDouble(DetalleVenta::getSubtotal).sum();
        Double cambio = cantidadPagada - total;
        
        if (cambio < 0) {
            throw new BusinessException("La cantidad pagada debe ser mayor o igual al total");
        }

        venta.setFechaVenta(LocalDateTime.now());
        venta.setUsuario(usuario);
        venta.setEmpresa(empresa);
        venta.setCliente(cliente);
        venta.setCantidadPagada(cantidadPagada);
        venta.setCantidadCambio(cambio);
        venta.setSubtotal(total);
        venta.setImpuesto(0.0);
        venta.setTotal(total);
        venta.setEstado("COMPLETADA");

        if (allParams.containsKey("metodoPago")) {
            venta.setMetodoPago(allParams.get("metodoPago"));
        }

        Venta ventaGuardada = ventaRepository.save(venta);

        for (DetalleVenta detalle : detalles) {
            detalle.setVenta(ventaGuardada);
            detalleVentaRepository.save(detalle);

            Producto producto = detalle.getProducto();
            
            if (Boolean.TRUE.equals(producto.getTieneReceta())) {
                var receta = producto.getReceta();
                if (receta != null && receta.getListaIngredientes() != null) {
                    for (DetalleReceta ingReceta : receta.getListaIngredientes()) {
                        Ingrediente ingrediente = ingReceta.getIngrediente();
                        Double cantidadDescontar = ingReceta.getCantidadIngrediente() * detalle.getCantidad();
                        Double stockAnterior = ingrediente.getStockDisponible() != null ? ingrediente.getStockDisponible() : 0.0;
                        Double nuevoStock = stockAnterior - cantidadDescontar;
                        if (nuevoStock < 0) nuevoStock = 0.0;
                        ingrediente.setStockDisponible(nuevoStock);
                        ingredienteRepository.save(ingrediente);
                        
                        movimientoStockService.registrarMovimiento(
                                ingrediente, stockAnterior, -cantidadDescontar, "SALIDA", "VENTA", ventaGuardada, ventaGuardada.getEmpresa(), usuario);
                    }
                }
            } else {
                Double stockAnterior = producto.getStock() != null ? producto.getStock() : 0.0;
                Double nuevoStock = stockAnterior - detalle.getCantidad();
                if (nuevoStock < 0) nuevoStock = 0.0;
                producto.setStock(nuevoStock);
                productoRepository.save(producto);
                
                movimientoStockService.registrarMovimiento(
                        producto, stockAnterior, -detalle.getCantidad().doubleValue(), "SALIDA", "VENTA", ventaGuardada, ventaGuardada.getEmpresa(), usuario);
            }
        }

        return ventaGuardada;
    }

    private Cliente obtenerOClienteDefault(String clienteIdStr, Long empresaId) {
        if (clienteIdStr != null && !clienteIdStr.isEmpty()) {
            Long clienteId = Long.parseLong(clienteIdStr);
            return clienteRepository.findById(clienteId)
                    .orElseGet(() -> crearClienteDefault(empresaId));
        }
        return crearClienteDefault(empresaId);
    }

    private Cliente crearClienteDefault(Long empresaId) {
        Optional<Cliente> existente = clienteRepository.findByNombreContainingIgnoreCase("Consumidor Final");
        if (existente.isPresent()) {
            return existente.get();
        }
        Cliente cliente = new Cliente();
        cliente.setNombre("Consumidor Final");
        cliente.setEstaActivo(true);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Optional<Venta> anular(Long id) {
        validarTenant();
        Usuario usuario = getUsuarioActual();

        return ventaRepository.findByIdWithDetails(id)
                .filter(venta -> venta.getEmpresa().getId().equals(TenantContext.getTenantId()))
                .filter(venta -> "COMPLETADA".equals(venta.getEstado()))
                .map(venta -> {
                    for (DetalleVenta detalle : venta.getDetallesVenta()) {
                        Producto producto = detalle.getProducto();
                        
                        if (Boolean.TRUE.equals(producto.getTieneReceta())) {
                            var receta = producto.getReceta();
                            if (receta != null && receta.getListaIngredientes() != null) {
                                for (DetalleReceta ingReceta : receta.getListaIngredientes()) {
                                    Ingrediente ingrediente = ingReceta.getIngrediente();
                                    Double cantidadDevolver = ingReceta.getCantidadIngrediente() * detalle.getCantidad();
                                    Double stockAnterior = ingrediente.getStockDisponible() != null ? ingrediente.getStockDisponible() : 0.0;
                                    Double nuevoStock = stockAnterior + cantidadDevolver;
                                    ingrediente.setStockDisponible(nuevoStock);
                                    ingredienteRepository.save(ingrediente);
                                    
                                    movimientoStockService.registrarMovimiento(
                                            ingrediente, stockAnterior, cantidadDevolver, "ENTRADA", "ANULACION_VENTA", venta, venta.getEmpresa(), usuario);
                                }
                            }
                        } else {
                            Double stockAnterior = producto.getStock() != null ? producto.getStock() : 0.0;
                            Double nuevoStock = stockAnterior + detalle.getCantidad();
                            producto.setStock(nuevoStock);
                            productoRepository.save(producto);
                            
                            movimientoStockService.registrarMovimiento(
                                    producto, stockAnterior, detalle.getCantidad().doubleValue(), "ENTRADA", "ANULACION_VENTA", venta, venta.getEmpresa(), usuario);
                        }
                    }

                    venta.setEstado("ANULADA");
                    return ventaRepository.save(venta);
                });
    }

    public String generarNumeroVenta() {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "VT-" + fecha + "-";
        
        Optional<String> maxNumero = ventaRepository.findMaxNumeroVentaByPrefix(empresaId, prefix);
        
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

    @Transactional(readOnly = true)
    public Double obtenerTotalVentas() {
        validarTenant();
        return ventaRepository.sumTotalByTenantId(TenantContext.getTenantId());
    }

    @Transactional(readOnly = true)
    public Double obtenerTotalVentasPorFecha(String fechaInicio, String fechaFin) {
        validarTenant();
        if (fechaInicio != null && !fechaInicio.isEmpty() && fechaFin != null && !fechaFin.isEmpty()) {
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio + "T00:00:00");
            LocalDateTime fin = LocalDateTime.parse(fechaFin + "T23:59:59");
            return ventaRepository.sumTotalByFechaBetween(TenantContext.getTenantId(), inicio, fin);
        }
        return obtenerTotalVentas();
    }
}
