package com.mvprestaurante.mvp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReporteCierreDTO {
    private LocalDate fecha;
    private String nombreEmpresa;
    private Double totalVentas;
    private Integer cantidadVentas;
    private Double totalEfectivo;
    private Double totalTarjeta;
    private Double totalTransferencia;
    private Integer ventasAnuladas;
    private Double totalAnulado;
    private Double totalCompras;
    private Integer cantidadCompras;
    private Double balance;
    private List<ProductoVendidoDTO> productosVendidos;
}
