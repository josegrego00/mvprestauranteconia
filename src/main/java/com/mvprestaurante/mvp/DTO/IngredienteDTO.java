package com.mvprestaurante.mvp.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IngredienteDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @PositiveOrZero(message = "El stock disponible no puede ser negativo")
    private Double stockDisponible;

    @PositiveOrZero(message = "El precio de compra no puede ser negativo")
    private Double precioCompra;

    @NotBlank(message = "La unidad de medida es obligatoria")
    private String unidadMedida;

    private Boolean estaActivo;

    private Long empresaId;
}