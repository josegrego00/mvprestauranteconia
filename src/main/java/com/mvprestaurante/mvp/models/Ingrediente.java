package com.mvprestaurante.mvp.models;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private Double stockDisponible;

    private Double precioCompra;

    @Column(nullable = false)
    private String unidadMedida;

    private Boolean estaActivo;

    @OneToMany(mappedBy = "ingrediente")
    private List<DetalleReceta> listaDetalleRecetas;

}
