package com.mvprestaurante.mvp.models;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Entity
@Builder
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String subdominio;
    
    private Boolean activa;
    private String nombreEmpresa;
    private String email;
    private String telefono;
    private String plan;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    private List<Usuario> listaUsuario;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    private List<Ingrediente> listaIngredientes;
    
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY )
    private List<Receta> listaRecetas;
    
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    private List<Producto> listaProductos;
}