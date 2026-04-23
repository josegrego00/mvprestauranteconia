package com.mvprestaurante.mvp.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;

    @Size(max = 100)
    private String apellido;

    @Size(max = 20)
    private String telefono;

    @Email(message = "El email no es válido")
    @Size(max = 100)
    private String email;

    @Size(max = 255)
    private String direccion;

    private String documentoIdentidad;
    private LocalDateTime fechaRegistro;
    private Boolean estaActivo;
}