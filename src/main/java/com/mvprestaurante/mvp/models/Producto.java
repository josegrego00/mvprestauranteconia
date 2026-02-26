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
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    private Double precioCompra;
    private Double precioVenta;

    private Boolean estaActivo;
    private Boolean tieneReceta;

    @OneToOne(mappedBy = "producto")
    private Receta receta;

}
