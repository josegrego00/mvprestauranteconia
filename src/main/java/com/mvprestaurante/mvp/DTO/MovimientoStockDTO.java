package com.mvprestaurante.mvp.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoStockDTO {

    private Long id;
    private LocalDateTime fechaMovimiento;

    @NotNull(message = "La empresa es obligatoria")
    private Long empresaId;

    private Long ingredienteId;
    private String nombreIngrediente;
    private Long productoId;
    private String nombreProducto;

    @NotBlank(message = "El tipo de item es obligatorio")
    private String tipoItem;

    @NotBlank(message = "El tipo de movimiento es obligatorio")
    private String tipoMovimiento;

    @NotNull(message = "La cantidad es obligatoria")
    private BigDecimal cantidad;

    private BigDecimal stockAnterior;
    private BigDecimal stockNuevo;

    @NotBlank(message = "El origen es obligatorio")
    private String origen;

    private Long ventaId;
    private Long compraId;
    private Long cierreDiaId;
    private String observaciones;

    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;
    private String nombreUsuario;
}