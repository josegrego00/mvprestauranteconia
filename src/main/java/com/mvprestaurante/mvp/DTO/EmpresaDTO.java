package com.mvprestaurante.mvp.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de una empresa registrada en el sistema")
public class EmpresaDTO {

    @Schema(description = "ID único de la empresa", example = "1")
    private Long id;

    @NotBlank(message = "El subdominio es obligatorio")
    @Size(min = 3, max = 50, message = "El subdominio debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-z0-9]+$", message = "Solo letras minúsculas y números")
    @Schema(description = "Subdominio único para identificar la empresa", example = "restauranteluna")
    private String subdominio;

    @NotBlank(message = "El nombre de empresa es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre oficial de la empresa", example = "Restaurante Luna")
    private String nombreEmpresa;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Schema(description = "Correo electrónico de contacto", example = "contacto@restauranteluna.com")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 dígitos")
    @Schema(description = "Teléfono de contacto", example = "+573001234567")
    private String telefono;

    @NotBlank(message = "El plan es obligatorio")
    @Size(max = 50, message = "El plan no puede exceder 50 caracteres")
    @Schema(description = "Plan de suscripción", example = "BASICO")
    private String plan;

    @Schema(description = "Indica si la empresa está activa", example = "true")
    private Boolean activa;
}