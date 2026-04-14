package com.mvprestaurante.mvp.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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

    @PositiveOrZero(message = "La cantidad pagada no puede ser negativa")
    private Double cantidadPagada;

    @PositiveOrZero(message = "El cambio no puede ser negativo")
    private Double cantidadCambio;

    @PositiveOrZero(message = "El subtotal no puede ser negativo")
    private Double subtotal;

    @PositiveOrZero(message = "El impuesto no puede ser negativo")
    private Double impuesto;

    @PositiveOrZero(message = "El total no puede ser negativo")
    private Double total;

    private String estado;

    private String observaciones;

    @PositiveOrZero(message = "El pago en efectivo no puede ser negativo")
    private Double pagoEfectivo;

    @PositiveOrZero(message = "El pago con tarjeta no puede ser negativo")
    private Double pagoTarjeta;

    @PositiveOrZero(message = "El pago por transferencia no puede ser negativo")
    private Double pagoTransferencia;

    @NotEmpty(message = "Debe incluir al menos un producto")
    @Valid
    private List<DetalleVentaDTO> detalles;
}