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

    @OneToMany(mappedBy = "empresa")
    private List<Usuario> listaUsuario;

    @OneToMany(mappedBy = "empresa")
    private List<Ingrediente> listaIngredientes;
    
    @OneToMany(mappedBy = "empresa")
    private List<Receta> listaRecetas;
    
    // ADD THIS
    @OneToMany(mappedBy = "empresa")
    private List<Producto> listaProductos;
}