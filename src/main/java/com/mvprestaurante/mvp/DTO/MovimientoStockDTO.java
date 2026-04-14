package com.mvprestaurante.mvp.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    @PositiveOrZero(message = "La cantidad no puede ser negativa")
    private Integer cantidad;

    private Double stockAnterior;
    private Double stockNuevo;

    @NotBlank(message = "El origen es obligatorio")
    private String origen;

    private Long ventaId;
    private Long compraId;

    private String observaciones;

    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;
    private String nombreUsuario;
}