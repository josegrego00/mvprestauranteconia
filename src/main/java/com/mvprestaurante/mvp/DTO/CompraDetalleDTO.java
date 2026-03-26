package com.mvprestaurante.mvp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompraDetalleDTO {
    private Long id;
    private String numeroCompra;
    private LocalDateTime fechaCompra;
    private String proveedor;
    private String observaciones;
    private String estado;
    private Double subtotal;
    private Double impuesto;
    private Double total;
    private String nombreUsuario;
    private List<DetalleCompraSimpleDTO> detalles;
}
