package com.mvprestaurante.mvp.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String numeroCompra; // Ej: "CMP-20250216-0001"

    @Column(nullable = false)
    private LocalDateTime fechaCompra;

    private String proveedor;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCompra> detallesCompra = new ArrayList<>();

    private Double subtotal;
    private Double impuesto;
    private Double total;
    private String estado; // PENDIENTE, COMPLETADA, ANULADA

    private String observaciones;
}