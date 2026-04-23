package com.mvprestaurante.mvp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {

    private Long id;
    private String nombre;
    private String nombreUsuario;
    private String rol;
    private Boolean estaActivo;
    private String email;
    private Boolean esSuperadmin;
    private Long empresaId;
    private String nombreEmpresa;
}