package com.mvprestaurante.mvp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventarioReporteDTO {
    private String nombre;
    private String tipo;
    private String unidadMedida;
    private LocalDate fechaInicial;
    private Double inventarioInicial;
    private LocalDate fechaFinal;
    private Double inventarioFinal;
    private Double consumo;
    private Double diferencia;
    private Double diferenciaDinero;
}
