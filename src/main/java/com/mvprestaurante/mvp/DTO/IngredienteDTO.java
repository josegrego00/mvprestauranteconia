package com.mvprestaurante.mvp.DTO;

import com.mvprestaurante.mvp.enums.UnidadMedida;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredienteDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private Double stockDisponible;

    private BigDecimal precioCompra;

    private UnidadMedida unidadMedida;

    private Boolean estaActivo;

    private Long empresaId;
}