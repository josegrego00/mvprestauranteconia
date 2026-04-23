package com.mvprestaurante.mvp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioDTO {

    private Long ingredienteId;
    private String ingredienteNombre;
    private String unidadMedida;
    private Double stockActual;
    private Double consumoPromedio;
    private Double diasRestantes;
    private Integer estado;
}