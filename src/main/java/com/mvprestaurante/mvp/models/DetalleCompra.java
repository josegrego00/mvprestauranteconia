package com.mvprestaurante.mvp.models;

import java.math.BigDecimal;

import jakarta.persistence.*;

import com.mvprestaurante.mvp.enums.TipoItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "ingrediente_id", nullable = true)
    private Ingrediente ingrediente;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = true)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoItem tipoItem;

    private Double cantidad;
    private BigDecimal precioUnitarioCompra;
    private BigDecimal subtotal;

}