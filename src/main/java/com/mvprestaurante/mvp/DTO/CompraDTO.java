package com.mvprestaurante.mvp.DTO;

import jakarta.validation.constraints.NotBlank;
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
public class CompraDTO {

    private Long id;

    @NotBlank(message = "El número de compra es obligatorio")
    private String numeroCompra;

    private LocalDateTime fechaCompra;

    private String proveedor;

    private String observaciones;

    private String estado;

    private Double subtotal;

    private Double impuesto;

    private Double total;

    private String nombreUsuario;

    private Long empresaId;

    private List<CompraDetalleDTO> detalles;
}