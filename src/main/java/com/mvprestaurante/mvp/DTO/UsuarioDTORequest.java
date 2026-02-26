package com.mvprestaurante.mvp.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioDTORequest {

    @NotEmpty
    @Size(min = 3, max = 50)
    private String nombre;

    @NotEmpty
    @Size(min = 3, max = 50)
    private String nombreUsuario;

    @NotEmpty
    @Size(min = 6, max = 15)
    private String contrasenna;

    @NotEmpty
    private String rol;

    @NotNull
    private Boolean estaActivo;

    @NotEmpty
    @Email  
    private String email;

}
