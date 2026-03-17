package com.mvprestaurante.mvp.models;

import java.util.List;

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
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    private Double precioBruto;

    private Double precioVenta;

    private Boolean estaActiva;

    @OneToOne
    @JoinColumn(name = "producto_id", unique = true)
    private Producto producto;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleReceta> listaIngredientes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

}