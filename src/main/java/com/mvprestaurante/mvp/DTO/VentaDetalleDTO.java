package com.mvprestaurante.mvp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VentaDetalleDTO {
    private Long id;
    private String numeroVenta;
    private LocalDateTime fechaVenta;
    private String nombreCliente;
    private String nombreUsuario;
    private String metodoPago;
    private Double cantidadPagada;
    private Double cantidadCambio;
    private Double subtotal;
    private Double impuesto;
    private Double total;
    private String estado;
    private String observaciones;
    private Double pagoEfectivo;
    private Double pagoTarjeta;
    private Double pagoTransferencia;
    private List<DetalleVentaDTO> detalles;
}
