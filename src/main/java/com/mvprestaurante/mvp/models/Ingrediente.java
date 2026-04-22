package com.mvprestaurante.mvp.models;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import com.mvprestaurante.mvp.enums.UnidadMedida;

import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table( // esto es para evitar la duplicidad en la misma empresa. es decir, 2 empresas
        // pueden tener el mismo producto.
        uniqueConstraints = @UniqueConstraint(columnNames = { "nombre", "empresa_id" }))
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private Double stockDisponible;

    private BigDecimal precioCompra;

    private UnidadMedida unidadMedida;

    private Boolean estaActivo;

    @OneToMany(mappedBy = "ingrediente", fetch = FetchType.LAZY)
    private List<DetalleReceta> listaDetalleRecetas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

}
