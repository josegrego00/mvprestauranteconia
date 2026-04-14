package com.mvprestaurante.mvp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioDTOResponse {

    private Long id;
    private String nombre;
    private String nombreUsuario;
    private String rol;
    private Boolean estaActivo;
    private String email;
    private Long empresaId;
    private String nombreEmpresa;
    private Boolean esSuperadmin;
}