package com.mvprestaurante.mvp.repositories;

import com.mvprestaurante.mvp.models.Ingrediente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {
    
    Page<Ingrediente> findByEstaActivoTrue(Pageable pageable);
    
    Page<Ingrediente> findByNombreContainingIgnoreCaseAndEstaActivoTrue(String nombre, Pageable pageable);
    
    boolean existsByNombreAndEstaActivoTrue(String nombre);
}