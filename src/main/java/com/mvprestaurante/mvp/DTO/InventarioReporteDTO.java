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
    private LocalDate fecha;
    private String nombre;
    private String tipo;
    private String unidadMedida;
    private Double inventarioInicial;
    private Double consumo;
    private Double inventarioFinal;
    private Double diferencia;
    private Double diferenciaDinero;
}
