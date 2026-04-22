package com.mvprestaurante.mvp.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.mvprestaurante.mvp.enums.OrigenMovimiento;
import com.mvprestaurante.mvp.enums.TipoItem;
import com.mvprestaurante.mvp.enums.TipoMovimiento;

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
@Table(name = "movimiento_stock", indexes = {
    @Index(name = "idx_fecha_movimiento", columnList = "fechaMovimiento"),
    @Index(name = "idx_empresa_fecha", columnList = "empresa_id, fechaMovimiento"),
    @Index(name = "idx_ingrediente_fecha", columnList = "ingrediente_id, fechaMovimiento"),
    @Index(name = "idx_producto_fecha", columnList = "producto_id, fechaMovimiento"),
    @Index(name = "idx_origen", columnList = "origen")
})
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingrediente_id")
    private Ingrediente ingrediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoItem tipoItem; // "INGREDIENTE" o "PRODUCTO"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimiento tipoMovimiento; // "ENTRADA" o "SALIDA"

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(precision = 10, scale = 2)
    private BigDecimal stockAnterior;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal stockNuevo;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private OrigenMovimiento origen; // COMPRA, VENTA, AJUSTE, PERDIDA, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id")
    private Compra compra; // Si viene de una compra

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    private Venta venta; // Si viene de una venta

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cierre_dia_id")
    private CierreDia cierreDia; // Cierre que incluye este movimiento

    @Column(length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario; // Quién hizo el movimiento

    @PrePersist
    public void prePersist() {
        if (tipoItem == TipoItem.INGREDIENTE && ingrediente == null) {
            throw new IllegalStateException("Si tipoItem es INGREDIENTE, ingrediente no puede ser null");
        }
        if (tipoItem == TipoItem.PRODUCTO && producto == null) {
            throw new IllegalStateException("Si tipoItem es PRODUCTO, producto no puede ser null");
        }
        if (fechaMovimiento == null) {
            fechaMovimiento = LocalDateTime.now();
        }
    }
}