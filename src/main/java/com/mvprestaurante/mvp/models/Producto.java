package com.mvprestaurante.mvp.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "nombre", "empresa_id" }))
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    private String descripcion;

    private Double precioCompra;
    private Double precioVenta;

    private Boolean estaActivo;
    
    @Column(nullable = false)
    private Boolean tieneReceta = false;

    @OneToOne(mappedBy = "producto")
    private Receta receta; // Puede ser null si el producto no tiene receta

    private Double stock; // solo si no tiene receta

    // ADD THIS - Relationship with Empresa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
}