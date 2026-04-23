package com.mvprestaurante.mvp.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaDTO {

    private Long id;
    private String numeroVenta;
    private LocalDateTime fechaVenta;

    private Long clienteId;
    private String nombreCliente;

    private Long usuarioId;
    private String nombreUsuario;

    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;

    private BigDecimal cantidadPagada;
    private BigDecimal cantidadCambio;
    private BigDecimal subtotal;
    private BigDecimal impuesto;
    private BigDecimal total;

    private String estado;
    private String observaciones;
    private BigDecimal pagoEfectivo;
    private BigDecimal pagoTarjeta;
    private BigDecimal pagoTransferencia;

    @NotEmpty(message = "Debe incluir al menos un producto")
    @Valid
    private List<DetalleVentaDTO> detalles;
}