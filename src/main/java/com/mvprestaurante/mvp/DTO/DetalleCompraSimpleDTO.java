package com.mvprestaurante.mvp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetalleCompraSimpleDTO {
    private String tipoItem;
    private String nombreItem;
    private Integer cantidad;
    private Double precioUnitarioCompra;
    private Double subtotal;
}
