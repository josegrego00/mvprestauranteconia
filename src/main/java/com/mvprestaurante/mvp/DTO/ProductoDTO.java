package com.mvprestaurante.mvp.DTO;

import jakarta.validation.constraints.NotBlank;
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
public class ProductoDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @PositiveOrZero(message = "El precio de compra no puede ser negativo")
    private Double precioCompra;

    @PositiveOrZero(message = "El precio de venta no puede ser negativo")
    private Double precioVenta;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estaActivo;

    @NotNull(message = "Debe especificar si tiene receta")
    private Boolean tieneReceta;

    private Long recetaId;

    private String recetaNombre;

    private Double precioBruto;

    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Double stock;

    private Double stockEstimado;

    private Long empresaId;
}