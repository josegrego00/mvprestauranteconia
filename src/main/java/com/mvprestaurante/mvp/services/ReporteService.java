package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.DTO.InventarioDTO;
import com.mvprestaurante.mvp.DTO.ProductoVendidoDTO;
import com.mvprestaurante.mvp.DTO.ReporteDashboardDTO;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.DetalleVentaRepository;
import com.mvprestaurante.mvp.repositories.IngredienteRepository;
import com.mvprestaurante.mvp.repositories.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final IngredienteRepository ingredienteRepository;

    private void validarTenant() {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new RuntimeException("No se ha identificado la empresa");
        }
    }

    @Transactional(readOnly = true)
    public ReporteDashboardDTO obtenerDashboard() {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(LocalTime.MAX);

        LocalDateTime inicioSemana = hoy.minusDays(7).atStartOfDay();
        LocalDateTime inicioMes = hoy.minusDays(30).atStartOfDay();

        Double ventasHoy = ventaRepository.sumTotalByFechaBetween(empresaId, inicioHoy, finHoy);
        Double ventasSemana = ventaRepository.sumTotalByFechaBetween(empresaId, inicioSemana, finHoy);
        Double ventasMes = ventaRepository.sumTotalByFechaBetween(empresaId, inicioMes, finHoy);

        Integer countHoy = ventaRepository.countByFechaBetween(empresaId, inicioHoy, finHoy);
        Integer countSemana = ventaRepository.countByFechaBetween(empresaId, inicioSemana, finHoy);
        Integer countMes = ventaRepository.countByFechaBetween(empresaId, inicioMes, finHoy);

        List<ProductoVendidoDTO> topProductos = obtenerTopProductos(empresaId, inicioMes, finHoy);

        List<InventarioDTO> inventario = obtenerInventario(empresaId);

        Map<String, Double> ventasPorDia = obtenerVentasPorDia(empresaId, inicioMes, finHoy);

        return ReporteDashboardDTO.builder()
                .ventasHoy(ventasHoy != null ? ventasHoy : 0.0)
                .ventasSemana(ventasSemana != null ? ventasSemana : 0.0)
                .ventasMes(ventasMes != null ? ventasMes : 0.0)
                .ventasCountHoy(countHoy != null ? countHoy : 0)
                .ventasCountSemana(countSemana != null ? countSemana : 0)
                .ventasCountMes(countMes != null ? countMes : 0)
                .topProductos(topProductos)
                .inventario(inventario)
                .ventasPorDia(ventasPorDia)
                .build();
    }

    private List<ProductoVendidoDTO> obtenerTopProductos(Long empresaId, LocalDateTime inicio, LocalDateTime fin) {
        List<Object[]> resultados = detalleVentaRepository.findTopProductosByVentasBetween(empresaId, inicio, fin);

        return resultados.stream()
                .limit(10)
                .map(row -> ProductoVendidoDTO.builder()
                        .productoId((Long) row[0])
                        .productoNombre((String) row[1])
                        .cantidadTotal(((Number) row[2]).intValue())
                        .montoTotal(((Number) row[3]).doubleValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<InventarioDTO> obtenerInventario(Long empresaId) {
        List<Ingrediente> ingredientes = ingredienteRepository.findActiveWithStock(empresaId);

        List<InventarioDTO> inventario = new ArrayList<>();

        for (Ingrediente ing : ingredientes) {
            InventarioDTO dto = InventarioDTO.builder()
                    .ingredienteId(ing.getId())
                    .ingredienteNombre(ing.getNombre())
                    .unidadMedida(ing.getUnidadMedida())
                    .stockActual(ing.getStockDisponible())
                    .build();

            double stock = ing.getStockDisponible() != null ? ing.getStockDisponible() : 0.0;
            if (stock <= 0) {
                dto.setDiasRestantes(0.0);
                dto.setEstado(0);
            } else if (stock < 5) {
                dto.setDiasRestantes(stock);
                dto.setEstado(0);
            } else if (stock < 15) {
                dto.setDiasRestantes(stock);
                dto.setEstado(1);
            } else {
                dto.setDiasRestantes(stock);
                dto.setEstado(2);
            }

            inventario.add(dto);
        }

        return inventario.stream()
                .sorted((a, b) -> Double.compare(a.getDiasRestantes(), b.getDiasRestantes()))
                .collect(Collectors.toList());
    }

    private Map<String, Double> obtenerVentasPorDia(Long empresaId, LocalDateTime inicio, LocalDateTime fin) {
        List<Object[]> resultados = detalleVentaRepository.findTopProductosByVentasBetween(empresaId, inicio, fin);

        Map<String, Double> ventasPorDia = new HashMap<>();

        LocalDate fecha = inicio.toLocalDate();
        while (!fecha.isAfter(fin.toLocalDate())) {
            ventasPorDia.put(fecha.toString(), 0.0);
            fecha = fecha.plusDays(1);
        }

        return ventasPorDia;
    }
}
