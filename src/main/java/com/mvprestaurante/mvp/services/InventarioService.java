package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.DTO.InventarioItemDTO;
import com.mvprestaurante.mvp.DTO.InventarioReporteDTO;
import com.mvprestaurante.mvp.models.*;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final IngredienteRepository ingredienteRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRegistroRepository inventarioRegistroRepository;
    private final UsuarioRepositorio usuarioRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    private void validarTenant() {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No se ha identificado la empresa");
        }
    }

    private Usuario getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("No hay usuario autenticado");
        }
        String nombreUsuario = auth.getName();
        Long empresaId = TenantContext.getTenantId();
        return usuarioRepository.findByNombreUsuarioAndEmpresa_Id(nombreUsuario, empresaId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<InventarioItemDTO> obtenerItemsInventario() {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        List<InventarioItemDTO> items = new ArrayList<>();

        Page<Ingrediente> ingredientes = ingredienteRepository.findByEstaActivoTrue(empresaId, Pageable.unpaged());
        for (Ingrediente ing : ingredientes.getContent()) {
            InventarioItemDTO dto = InventarioItemDTO.builder()
                    .tipo("INGREDIENTE")
                    .itemId(ing.getId())
                    .nombre(ing.getNombre())
                    .unidadMedida(ing.getUnidadMedida())
                    .stockSistema(ing.getStockDisponible() != null ? ing.getStockDisponible() : 0.0)
                    .stockFisico(0.0)
                    .diferenciaUnidad(0.0)
                    .diferenciaDinero(0.0)
                    .precioUnitario(ing.getPrecioCompra() != null ? ing.getPrecioCompra() : 0.0)
                    .build();
            items.add(dto);
        }

        Page<Producto> productos = productoRepository.findByTieneRecetaFalseAndEstaActivoTrue(empresaId,
                Pageable.unpaged());
        for (Producto prod : productos.getContent()) {
            InventarioItemDTO dto = InventarioItemDTO.builder()
                    .tipo("PRODUCTO")
                    .itemId(prod.getId())
                    .nombre(prod.getNombre())
                    .unidadMedida("UND")
                    .stockSistema(prod.getStock() != null ? prod.getStock() : 0.0)
                    .stockFisico(0.0)
                    .diferenciaUnidad(0.0)
                    .diferenciaDinero(0.0)
                    .precioUnitario(prod.getPrecioCompra() != null ? prod.getPrecioCompra() : 0.0)
                    .build();
            items.add(dto);
        }

        return items;
    }

    @Transactional
    public void guardarInventario(Map<String, String> params) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        Usuario usuario = getCurrentUser();

        LocalDate fechaInventario = LocalDate.now();

        Optional<InventarioRegistro> existente = inventarioRegistroRepository.findByFechaAndTenantId(empresaId,
                fechaInventario);
        InventarioRegistro registro;

        if (existente.isPresent()) {
            registro = existente.get();
            registro.getDetalles().clear();
        } else {
            registro = InventarioRegistro.builder()
                    .fecha(fechaInventario)
                    .empresa(Empresa.builder().id(empresaId).build())
                    .usuario(usuario)
                    .fechaCreacion(LocalDateTime.now())
                    .build();
        }

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (!key.startsWith("stock_")) {
                continue;
            }

            String[] parts = key.split("_");
            if (parts.length != 3) {
                continue;
            }

            String tipo = parts[1];
            String idStr = parts[2];

            try {
                Double stockFisico = Double.parseDouble(value);

                if (tipo.equals("INGREDIENTE")) {
                    Long id = Long.parseLong(idStr);
                    Optional<Ingrediente> ingOpt = ingredienteRepository.findById(id);
                    if (ingOpt.isPresent()) {
                        Ingrediente ing = ingOpt.get();
                        InventarioDetalle detalle = InventarioDetalle.builder()
                                .registro(registro)
                                .tipo("INGREDIENTE")
                                .ingrediente(ing)
                                .nombre(ing.getNombre())
                                .unidadMedida(ing.getUnidadMedida())
                                .stock(stockFisico)
                                .precioUnitario(ing.getPrecioCompra() != null ? ing.getPrecioCompra() : 0.0)
                                .build();
                        registro.getDetalles().add(detalle);

                        ing.setStockDisponible(stockFisico);
                        ingredienteRepository.save(ing);
                    }
                } else if (tipo.equals("PRODUCTO")) {
                    Long id = Long.parseLong(idStr);
                    Optional<Producto> prodOpt = productoRepository.findById(id);
                    if (prodOpt.isPresent()) {
                        Producto prod = prodOpt.get();
                        InventarioDetalle detalle = InventarioDetalle.builder()
                                .registro(registro)
                                .tipo("PRODUCTO")
                                .producto(prod)
                                .nombre(prod.getNombre())
                                .unidadMedida("UND")
                                .stock(stockFisico)
                                .precioUnitario(prod.getPrecioCompra() != null ? prod.getPrecioCompra() : 0.0)
                                .build();
                        registro.getDetalles().add(detalle);

                        prod.setStock(stockFisico);
                        productoRepository.save(prod);
                    }
                }
            } catch (NumberFormatException e) {
            }
        }

        inventarioRegistroRepository.save(registro);
    }

    @Transactional(readOnly = true)
    public Page<InventarioReporteDTO> obtenerReporte(LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        List<InventarioRegistro> registros = inventarioRegistroRepository.findByFechaBetweenAndTenantId(
                empresaId, fechaDesde, fechaHasta);

        Map<LocalDate, Map<String, InventarioDetalle>> mapaPorFecha = new LinkedHashMap<>();
        for (InventarioRegistro reg : registros) {
            Map<String, InventarioDetalle> mapaItems = new HashMap<>();
            for (InventarioDetalle det : reg.getDetalles()) {
                String key = det.getTipo() + "_"
                        + (det.getIngrediente() != null ? det.getIngrediente().getId() : det.getProducto().getId());
                mapaItems.put(key, det);
            }
            mapaPorFecha.put(reg.getFecha(), mapaItems);
        }

        Set<String> todosLosItems = new LinkedHashSet<>();
        for (Map<String, InventarioDetalle> mapa : mapaPorFecha.values()) {
            todosLosItems.addAll(mapa.keySet());
        }

        List<InventarioReporteDTO> resultados = new ArrayList<>();

        for (String key : todosLosItems) {
            InventarioDetalle primerDetalle = null;
            InventarioDetalle ultimoDetalle = null;
            LocalDate fechaPrimerRegistro = null;
            LocalDate fechaUltimoRegistro = null;

            for (Map.Entry<LocalDate, Map<String, InventarioDetalle>> entry : mapaPorFecha.entrySet()) {
                InventarioDetalle det = entry.getValue().get(key);
                if (det != null) {
                    if (primerDetalle == null || entry.getKey().isBefore(fechaPrimerRegistro)) {
                        primerDetalle = det;
                        fechaPrimerRegistro = entry.getKey();
                    }
                    if (ultimoDetalle == null || entry.getKey().isAfter(fechaUltimoRegistro)) {
                        ultimoDetalle = det;
                        fechaUltimoRegistro = entry.getKey();
                    }
                }
            }

            if (primerDetalle != null && ultimoDetalle != null) {
                double stockInicial = primerDetalle.getStock() != null ? primerDetalle.getStock() : 0.0;
                double stockFinal = ultimoDetalle.getStock() != null ? ultimoDetalle.getStock() : 0.0;
                double diferencia = stockFinal - stockInicial;
                double precio = primerDetalle.getPrecioUnitario() != null ? primerDetalle.getPrecioUnitario() : 0.0;

                Long itemId = primerDetalle.getIngrediente() != null ? primerDetalle.getIngrediente().getId()
                        : primerDetalle.getProducto().getId();
                String tipoItem = primerDetalle.getTipo();

                LocalDateTime fechaInicioDateTime = fechaPrimerRegistro.atStartOfDay();
                LocalDateTime fechaFinDateTime = fechaUltimoRegistro.atTime(LocalTime.MAX);

                Integer consumoFromVentas = movimientoStockRepository.sumConsumoByItem(
                        empresaId, tipoItem, itemId, fechaInicioDateTime, fechaFinDateTime);
                double consumo = consumoFromVentas != null ? consumoFromVentas : 0.0;

                Integer comprasFromProveedor = movimientoStockRepository.sumComprasByItem(
                        empresaId, tipoItem, itemId, fechaInicioDateTime, fechaFinDateTime);
                double compras = comprasFromProveedor != null ? comprasFromProveedor : 0.0;

                double inventarioEstimado = stockInicial + compras + consumo;

                InventarioReporteDTO dto = InventarioReporteDTO.builder()
                        .nombre(primerDetalle.getNombre())
                        .tipo(primerDetalle.getTipo())
                        .unidadMedida(primerDetalle.getUnidadMedida())
                        .fechaInicial(fechaPrimerRegistro)
                        .inventarioInicial(stockInicial)
                        .compras(compras)
                        .inventarioEstimado(inventarioEstimado)
                        .fechaFinal(fechaUltimoRegistro)
                        .inventarioFinal(stockFinal)
                        .consumo(consumo)
                        .diferencia(diferencia)
                        .diferenciaDinero(diferencia * precio)
                        .build();

                resultados.add(dto);
            }
        }

        int start = page * size;
        int end = Math.min(start + size, resultados.size());

        if (start > resultados.size()) {
            return Page.empty(PageRequest.of(page, size));
        }

        List<InventarioReporteDTO> pagina = resultados.subList(start, end);
        return new PageImpl<>(pagina, PageRequest.of(page, size), resultados.size());
    }
}

class PageImpl<T> extends org.springframework.data.domain.PageImpl<T> {
    public PageImpl(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }
}
