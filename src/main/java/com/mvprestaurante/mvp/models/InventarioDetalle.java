package com.mvprestaurante.mvp.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class InventarioDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "registro_id", nullable = false)
    private InventarioRegistro registro;

    private String tipo; // "INGREDIENTE" o "PRODUCTO"

    @ManyToOne
    @JoinColumn(name = "ingrediente_id")
    private Ingrediente ingrediente;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private String nombre;
    private String unidadMedida;
    private Double stock;
    private Double precioUnitario;
}
