package com.mvprestaurante.mvp.models;

import java.time.LocalDateTime;
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
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaMovimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    // Qué ítem se movió (puede ser ingrediente O producto)
    @ManyToOne
    @JoinColumn(name = "ingrediente_id")
    private Ingrediente ingrediente;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false)
    private String tipoItem; // "INGREDIENTE" o "PRODUCTO"

    @Column(nullable = false)
    private String tipoMovimiento; // "ENTRADA" o "SALIDA"

    @Column(nullable = false)
    private Integer cantidad;

    private Double stockAnterior;
    private Double stockNuevo;

    // Origen del movimiento
    private String origen; // COMPRA, VENTA, AJUSTE, PERDIDA, etc.

    @ManyToOne
    @JoinColumn(name = "compra_id")
    private Compra compra; // Si viene de una compra

    @ManyToOne
    @JoinColumn(name = "venta_id")
    private Venta venta; // Si viene de una venta

    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario; // Quién hizo el movimiento
    


}