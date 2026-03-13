package com.mvprestaurante.mvp.DTO;

import lombok.Data;

@Data
public class EmpresaDTO {
    private Long id;
    private String subdominio;
    private String nombreEmpresa;
    private String email;
    private String telefono;
    private String plan;
    private Boolean activa;
}