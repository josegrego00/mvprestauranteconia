package com.mvprestaurante.mvp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioItemDTO {

    private String tipo;
    private Long itemId;
    private String nombre;
    private String unidadMedida;
    private Double stockSistema;
    private Double stockFisico;
    private Double diferenciaUnidad;
    private Double diferenciaDinero;
    private Double precioUnitario;
}