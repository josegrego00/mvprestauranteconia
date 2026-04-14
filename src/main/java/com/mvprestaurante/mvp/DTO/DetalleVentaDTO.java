package com.mvprestaurante.mvp.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetalleVentaDTO {

    private Long id;

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    private String nombreProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @PositiveOrZero(message = "El precio unitario no puede ser negativo")
    private Double precioUnitario;

    @PositiveOrZero(message = "El subtotal no puede ser negativo")
    private Double subtotal;
}