package com.mvprestaurante.mvp.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecetaDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    private BigDecimal precioBruto;

    private BigDecimal precioVenta;

    @NotNull(message = "El estado es obligatorio")
    private Boolean estaActiva;

    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    private String productoNombre;

    @NotEmpty(message = "Debe incluir al menos un ingrediente")
    @Valid
    private List<DetalleRecetaDTO> listaIngredientes;

    private Long empresaId;
}