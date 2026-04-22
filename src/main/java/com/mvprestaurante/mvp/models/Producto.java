package com.mvprestaurante.mvp.models;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "nombre", "empresa_id" }))
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String descripcion;

    private BigDecimal precioCompra;
    private BigDecimal precioVenta;

    private Boolean estaActivo;

    @Column(nullable = false)
    private Boolean tieneReceta = false;

    @OneToOne(mappedBy = "producto")
    private Receta receta; // Puede ser null si el producto no tiene receta

    private Double stock; // solo si no tiene receta

    // ADD THIS - Relationship with Empresa
    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
}