package com.mvprestaurante.mvp.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioDetalleDTO {

    private Long id;

    private Long registroId;

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    private Long ingredienteId;
    private Long productoId;
    private String nombre;

    @NotBlank(message = "La unidad de medida es obligatoria")
    private String unidadMedida;

    @NotNull(message = "El stock es obligatorio")
    private Double stock;
}