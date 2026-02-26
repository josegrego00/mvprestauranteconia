package com.mvprestaurante.mvp.repositories;

import com.mvprestaurante.mvp.models.Receta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecetaRepository extends JpaRepository<Receta, Long> {
    
    Page<Receta> findByEstaActivaTrue(Pageable pageable);
    
    Page<Receta> findByNombreContainingIgnoreCaseAndEstaActivaTrue(String nombre, Pageable pageable);
    
    @Query("SELECT r FROM Receta r WHERE r.producto.id = :productoId AND r.estaActiva = true")
    Page<Receta> findByProductoId(@Param("productoId") Long productoId, Pageable pageable);
    
    boolean existsByNombreAndEstaActivaTrue(String nombre);
}