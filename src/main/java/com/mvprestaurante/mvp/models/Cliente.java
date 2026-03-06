package com.mvprestaurante.mvp.models;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.TenantId;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String apellido;
    private String telefono;
    private String email;
    private String direccion;

    private String documentoIdentidad; // RUT, DNI, etc.

    private LocalDateTime fechaRegistro;

    private Boolean estaActivo;

    @OneToMany(mappedBy = "cliente")
    private List<Venta> ventas;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private String empresaId;

    @ManyToOne
    @JoinColumn(name = "empresa_id", insertable = false, updatable = false)
    private Empresa empresa;
}