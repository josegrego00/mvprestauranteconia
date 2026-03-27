package com.mvprestaurante.mvp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReporteDashboardDTO {
    private Double ventasHoy;
    private Double ventasSemana;
    private Double ventasMes;
    private Integer ventasCountHoy;
    private Integer ventasCountSemana;
    private Integer ventasCountMes;
    private List<ProductoVendidoDTO> topProductos;
    private List<InventarioDTO> inventario;
    private Map<String, Double> ventasPorDia;
}
