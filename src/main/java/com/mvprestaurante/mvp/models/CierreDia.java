package com.mvprestaurante.mvp.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mvprestaurante.mvp.enums.TipoCierre;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "empresa_id", "fecha", "tipo" }))
public class CierreDia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(length = 500)
    private String observaciones; // Para anotar incidencias del cierre o Cualquier comentario relevante

    @Column(nullable = false)
    private LocalDate fechaCierreDia;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoCierre tipo;

    private BigDecimal totalVentas;
    private BigDecimal totalCompras;
    private BigDecimal balance;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaEjecucionCierre;
}
