package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.DTO.DetalleVentaDTO;
import com.mvprestaurante.mvp.DTO.ProductoDTO;
import com.mvprestaurante.mvp.DTO.ReporteCierreDTO;
import com.mvprestaurante.mvp.DTO.VentaDTO;
import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.mapper.VentaMapper;
import com.mvprestaurante.mvp.models.Cliente;
import com.mvprestaurante.mvp.models.DetalleReceta;
import com.mvprestaurante.mvp.models.DetalleVenta;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.models.Producto;
import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.models.Venta;
import com.mvprestaurante.mvp.models.CierreDia;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.CierreDiaRepository;
import com.mvprestaurante.mvp.repositories.ClienteRepositorio;
import com.mvprestaurante.mvp.repositories.CompraRepository;
import com.mvprestaurante.mvp.repositories.DetalleVentaRepository;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;
import com.mvprestaurante.mvp.repositories.IngredienteRepository;
import com.mvprestaurante.mvp.repositories.InventarioRegistroRepository;
import com.mvprestaurante.mvp.repositories.ProductoRepository;
import com.mvprestaurante.mvp.repositories.RecetaRepository;
import com.mvprestaurante.mvp.repositories.UsuarioRepositorio;
import com.mvprestaurante.mvp.repositories.VentaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private final CompraRepository compraRepository;
    private final CierreDiaRepository cierreDiaRepository;
    private final InventarioRegistroRepository inventarioRegistroRepository;
    private final VentaMapper ventaMapper;

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

    private Double nullSafe(Double value, Double defaultVal) {
        return value != null ? value : defaultVal;
    }

    private Integer nullSafe(Integer value, Integer defaultVal) {
        return value != null ? value : defaultVal;
    }

    @Transactional(readOnly = true)
    public Page<VentaDTO> buscar(String search, String fechaInicio, String fechaFin, Pageable pageable) {
        validarTenant();

        Page<Venta> ventasPage;
        if (search != null && !search.isEmpty()) {
            ventasPage = ventaRepository.findByNumeroContainingIgnoreCase(TenantContext.getTenantId(), search, pageable);
        } else if (fechaInicio != null && !fechaInicio.isEmpty() && fechaFin != null && !fechaFin.isEmpty()) {
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio + "T00:00:00");
            LocalDateTime fin = LocalDateTime.parse(fechaFin + "T23:59:59");
            ventasPage = ventaRepository.findByFechaBetween(TenantContext.getTenantId(), inicio, fin, pageable);
        } else {
            ventasPage = ventaRepository.findAllByTenantId(TenantContext.getTenantId(), pageable);
        }

        return ventasPage.map(ventaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    public Optional<VentaDTO> obtenerPorId(Long id) {
        validarTenant();
        Long tenantId = TenantContext.getTenantId();
        return ventaRepository.findByIdWithDetails(id, tenantId)
                .map(ventaMapper::toDTO);
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
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    public VentaDTO guardar(VentaDTO ventaDTO, Map<String, String> allParams) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        if (ventaDTO.getDetalles() == null || ventaDTO.getDetalles().isEmpty()) {
            throw new BusinessException("La venta debe tener al menos un producto");
        }

        Usuario usuario = getUsuarioActual();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));

        Cliente cliente = obtenerOClienteDefault(allParams.get("clienteId"), empresaId);
        String metodoPago = allParams.get("metodoPago");

        Double efectivo = parseDoubleSafe(allParams.get("pagoEfectivo"));
        Double tarjeta = parseDoubleSafe(allParams.get("pagoTarjeta"));
        Double transferencia = parseDoubleSafe(allParams.get("pagoTransferencia"));
        Double cantidadPagada = efectivo + tarjeta + transferencia;

        List<DetalleVenta> detalles = new ArrayList<>();
        for (DetalleVentaDTO detalleDTO : ventaDTO.getDetalles()) {
            Optional<Producto> productoOpt = productoRepository.findById(detalleDTO.getProductoId());
            if (productoOpt.isEmpty()) {
                throw new BusinessException("Producto no encontrado");
            }

            Producto producto = productoOpt.get();
            validarStock(producto, detalleDTO.getCantidad());

            Double precioUnitario = producto.getPrecioVenta();
            Double subtotal = detalleDTO.getCantidad() * precioUnitario;

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);
            detalles.add(detalle);
        }

        Double total = detalles.stream().mapToDouble(DetalleVenta::getSubtotal).sum();
        Double cambio = cantidadPagada - total;

        if (cantidadPagada < total) {
            throw new BusinessException("La cantidad pagada debe ser mayor o igual al total");
        }

        Venta venta = new Venta();
        venta.setNumeroVenta(generarNumeroVenta());
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
        venta.setMetodoPago(metodoPago);
        venta.setPagoEfectivo(efectivo);
        venta.setPagoTarjeta(tarjeta);
        venta.setPagoTransferencia(transferencia);

        Venta ventaGuardada = ventaRepository.save(venta);

        for (DetalleVenta detalle : detalles) {
            detalle.setVenta(ventaGuardada);
            detalleVentaRepository.save(detalle);
        }

        descontarStock(ventaGuardada, detalles, usuario);

        return ventaMapper.toDTO(ventaGuardada);
    }

    private void validarStock(Producto producto, Integer cantidad) {
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
            throw new BusinessException("Stock insuficiente para '" + producto.getNombre() + "'. "
                    + tipoStock + " actual: " + stockEstimado.intValue());
        }
    }

    private void actualizarStock(Venta venta, List<DetalleVenta> detalles, Usuario usuario, boolean esEntrada) {
        String tipoMovimiento = esEntrada ? "ENTRADA" : "SALIDA";
        String motivo = esEntrada ? "ANULACION_VENTA" : "VENTA";
        Double factor = esEntrada ? 1.0 : -1.0;

        for (DetalleVenta detalle : detalles) {
            Producto producto = detalle.getProducto();

            if (Boolean.TRUE.equals(producto.getTieneReceta())) {
                var receta = producto.getReceta();
                if (receta != null && receta.getListaIngredientes() != null) {
                    for (DetalleReceta ingReceta : receta.getListaIngredientes()) {
                        Ingrediente ingrediente = ingReceta.getIngrediente();
                        Double cantidad = ingReceta.getCantidadIngrediente() * detalle.getCantidad();
                        Double stockAnterior = ingrediente.getStockDisponible() != null
                                ? ingrediente.getStockDisponible()
                                : 0.0;
                        Double nuevoStock = stockAnterior + (cantidad * factor);
                        if (nuevoStock < 0) nuevoStock = 0.0;
                        ingrediente.setStockDisponible(nuevoStock);
                        ingredienteRepository.save(ingrediente);

                        movimientoStockService.registrarMovimiento(
                                ingrediente, stockAnterior, cantidad * factor, tipoMovimiento, motivo, venta,
                                venta.getEmpresa(), usuario);
                    }
                }
            } else {
                Double stockAnterior = producto.getStock() != null ? producto.getStock() : 0.0;
                Double nuevoStock = stockAnterior + (detalle.getCantidad() * factor);
                if (nuevoStock < 0) nuevoStock = 0.0;
                producto.setStock(nuevoStock);
                productoRepository.save(producto);

                movimientoStockService.registrarMovimiento(
                        producto, stockAnterior, detalle.getCantidad() * factor, tipoMovimiento, motivo, venta,
                        venta.getEmpresa(), usuario);
            }
        }
    }

    private void descontarStock(Venta venta, List<DetalleVenta> detalles, Usuario usuario) {
        actualizarStock(venta, detalles, usuario, false);
    }

    private void devolverStock(Venta venta, Usuario usuario) {
        actualizarStock(venta, venta.getDetallesVenta(), usuario, true);
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
        Optional<Cliente> existente = clienteRepository.findByNombreContainingIgnoreCaseAndEmpresaId("Consumidor Final", empresaId);
        if (existente.isPresent()) {
            return existente.get();
        }
        Cliente cliente = new Cliente();
        cliente.setNombre("Consumidor Final");
        cliente.setEstaActivo(true);
        Empresa empresa = new Empresa();
        empresa.setId(empresaId);
        cliente.setEmpresa(empresa);
        return clienteRepository.save(cliente);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Optional<VentaDTO> anular(Long id) {
        validarTenant();
        Long tenantId = TenantContext.getTenantId();
        Usuario usuario = getUsuarioActual();

        return ventaRepository.findByIdWithDetails(id, tenantId)
                .filter(venta -> "COMPLETADA".equals(venta.getEstado()))
                .map(venta -> {
                    devolverStock(venta, usuario);
                    venta.setEstado("ANULADA");
                    Venta ventaAnulada = ventaRepository.save(venta);
                    return ventaMapper.toDTO(ventaAnulada);
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

    @Transactional(readOnly = true)
    public ReporteCierreDTO generarReporteCierreX(LocalDateTime fecha) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        LocalDateTime inicioDia = fecha.toLocalDate().atStartOfDay();
        LocalDateTime finDia = fecha.toLocalDate().atTime(23, 59, 59);

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));

        String nombreEmpresa = empresa.getNombreEmpresa() != null ? empresa.getNombreEmpresa() : empresa.getSubdominio();

        Double totalVentas = nullSafe(ventaRepository.sumTotalByFechaBetween(empresaId, inicioDia, finDia), 0.0);
        Integer cantidadVentas = nullSafe(ventaRepository.countByFechaBetween(empresaId, inicioDia, finDia), 0);
        Double totalEfectivo = nullSafe(ventaRepository.sumEfectivoByFechaBetween(empresaId, inicioDia, finDia), 0.0);
        Double totalTarjeta = nullSafe(ventaRepository.sumTarjetaByFechaBetween(empresaId, inicioDia, finDia), 0.0);
        Double totalTransferencia = nullSafe(ventaRepository.sumTransferenciaByFechaBetween(empresaId, inicioDia, finDia), 0.0);
        Integer ventasAnuladas = nullSafe(ventaRepository.countAnuladasByFechaBetween(empresaId, inicioDia, finDia), 0);
        Double totalCompras = nullSafe(compraRepository.sumTotalByFechaBetween(empresaId, inicioDia, finDia), 0.0);
        Integer cantidadCompras = nullSafe(compraRepository.countByFechaBetween(empresaId, inicioDia, finDia), 0);

        List<Venta> ventasDelDia = ventaRepository.findVentasDelDia(empresaId, inicioDia, finDia);

        Map<Long, ReporteProductoAcumulado> productosMap = new LinkedHashMap<>();

        for (Venta venta : ventasDelDia) {
            for (DetalleVenta detalle : venta.getDetallesVenta()) {
                Long productoId = detalle.getProducto().getId();
                String productoNombre = detalle.getProducto().getNombre();

                if (productosMap.containsKey(productoId)) {
                    ReporteProductoAcumulado ac = productosMap.get(productoId);
                    ac.cantidadTotal += detalle.getCantidad();
                    ac.montoTotal += detalle.getSubtotal();
                } else {
                    ReporteProductoAcumulado nuevo = new ReporteProductoAcumulado();
                    nuevo.productoId = productoId;
                    nuevo.productoNombre = productoNombre;
                    nuevo.cantidadTotal = detalle.getCantidad();
                    nuevo.montoTotal = detalle.getSubtotal();
                    productosMap.put(productoId, nuevo);
                }
            }
        }

        List<ProductoDTO> productosVendidos = new ArrayList<>();
        for (ReporteProductoAcumulado ac : productosMap.values()) {
            productosVendidos.add(ProductoDTO.builder()
                    .id(ac.productoId)
                    .nombre(ac.productoNombre)
                    .stock(ac.cantidadTotal.doubleValue())
                    .precioVenta(ac.montoTotal)
                    .build());
        }

        productosVendidos.sort((a, b) -> Double.compare(
                b.getPrecioVenta() != null ? b.getPrecioVenta() : 0.0,
                a.getPrecioVenta() != null ? a.getPrecioVenta() : 0.0));

        return ReporteCierreDTO.builder()
                .fecha(fecha.toLocalDate())
                .nombreEmpresa(nombreEmpresa)
                .totalVentas(totalVentas)
                .cantidadVentas(cantidadVentas)
                .totalEfectivo(totalEfectivo)
                .totalTarjeta(totalTarjeta)
                .totalTransferencia(totalTransferencia)
                .ventasAnuladas(ventasAnuladas)
                .totalAnulado(0.0)
                .totalCompras(totalCompras)
                .cantidadCompras(cantidadCompras)
                .balance(totalVentas - totalCompras)
                .productosVendidos(productosVendidos)
                .build();
    }

    @Transactional
    public ReporteCierreDTO generarReporteCierreZ(LocalDateTime fecha) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));

        LocalDate hoy = LocalDate.now();

        boolean tieneInventarioFisico = inventarioRegistroRepository.findByFechaAndTenantId(empresaId, hoy).isPresent();
        if (!tieneInventarioFisico) {
            throw new BusinessException("Debe cargar el inventario físico antes de generar el Reporte Z.");
        }

        if (cierreDiaRepository.existsByEmpresaIdAndFechaAndTipo(empresaId, hoy, "Z")) {
            throw new BusinessException("El día ya ha sido cerrado. No se puede generar otro Reporte Z.");
        }

        ReporteCierreDTO reporte = generarReporteCierreX(fecha);

        CierreDia cierreDia = CierreDia.builder()
                .empresa(empresa)
                .fecha(hoy)
                .tipo("Z")
                .totalVentas(reporte.getTotalVentas())
                .totalCompras(reporte.getTotalCompras())
                .balance(reporte.getBalance())
                .fechaCierre(LocalDateTime.now())
                .build();
        cierreDiaRepository.save(cierreDia);

        return reporte;
    }

    @Transactional(readOnly = true)
    public boolean isDiaCerrado() {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        LocalDate hoy = LocalDate.now();
        return cierreDiaRepository.existsByEmpresaIdAndFechaAndTipo(empresaId, hoy, "Z");
    }

    private static class ReporteProductoAcumulado {
        Long productoId;
        String productoNombre;
        Integer cantidadTotal = 0;
        Double montoTotal = 0.0;
    }
}