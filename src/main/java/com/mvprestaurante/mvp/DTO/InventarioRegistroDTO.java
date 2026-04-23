package com.mvprestaurante.mvp.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioRegistroDTO {

    private Long id;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    private LocalDateTime fechaCreacion;
    private Long empresaId;
    private Long usuarioId;
    private String nombreUsuario;

    @Positive(message = "Debe incluir al menos un detalle")
    private Integer cantidadDetalles;
}