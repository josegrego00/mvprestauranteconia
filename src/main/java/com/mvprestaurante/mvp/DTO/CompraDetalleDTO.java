package com.mvprestaurante.mvp.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompraDetalleDTO {

    private Long id;

    @NotBlank(message = "El tipo de item es obligatorio")
    private String tipoItem;

    @NotNull(message = "El item es obligatorio")
    private Long itemId;

    private String itemNombre;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a cero")
    private Double precioUnitarioCompra;

    private Double subtotal;

    private Long compraId;
}