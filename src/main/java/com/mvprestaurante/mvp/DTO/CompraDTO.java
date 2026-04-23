package com.mvprestaurante.mvp.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompraDTO {

    private Long id;

    @NotBlank(message = "El número de compra es obligatorio")
    private String numeroCompra;

    private LocalDateTime fechaCompra;
    private String proveedor;
    private String observaciones;
    private String estado;
    private BigDecimal subtotal;
    private BigDecimal impuesto;
    private BigDecimal total;
    private String nombreUsuario;
    private Long empresaId;

    @Valid
    private List<DetalleCompraDTO> detalles;
}