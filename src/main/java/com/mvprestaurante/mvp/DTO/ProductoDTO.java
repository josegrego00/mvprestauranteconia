package com.mvprestaurante.mvp.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    private BigDecimal precioCompra;

    private BigDecimal precioVenta;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estaActivo;

    @NotNull(message = "Debe especificar si tiene receta")
    private Boolean tieneReceta;

    private Long recetaId;
    private String recetaNombre;
    private BigDecimal precioBruto;
    private Double stock;
    private Double stockEstimado;
    private Long empresaId;
}