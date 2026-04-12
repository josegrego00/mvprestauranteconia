package com.mvprestaurante.mvp.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecetaDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @PositiveOrZero(message = "El precio bruto no puede ser negativo")
    private Double precioBruto;

    @PositiveOrZero(message = "El precio de venta no puede ser negativo")
    private Double precioVenta;

    private Boolean estaActiva;

    private Long productoId;

    private String productoNombre;

    private List<DetalleRecetaDTO> listaIngredientes;

    private Long empresaId;
}