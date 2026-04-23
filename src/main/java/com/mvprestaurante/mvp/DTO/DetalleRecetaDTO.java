package com.mvprestaurante.mvp.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleRecetaDTO {

    private Long id;

    private String nombre;

    @NotNull(message = "El ingrediente es obligatorio")
    private Long ingredienteId;

    private String ingredienteNombre;
    private String ingredienteUnidadMedida;
    private Double ingredienteStockDisponible;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Double cantidadIngrediente;

    private Long recetaId;
}