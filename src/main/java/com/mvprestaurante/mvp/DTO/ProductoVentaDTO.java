package com.mvprestaurante.mvp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductoVentaDTO {
    private Long id;
    private String nombre;
    private Double precioVenta;
    private Double stock;
    private Double stockEstimado;
    private Boolean tieneReceta;
}
