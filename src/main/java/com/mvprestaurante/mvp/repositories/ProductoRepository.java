package com.mvprestaurante.mvp.repositories;

import com.mvprestaurante.mvp.models.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    Page<Producto> findByEstaActivoTrue(Pageable pageable);
    
    Page<Producto> findByNombreContainingIgnoreCaseAndEstaActivoTrue(String nombre, Pageable pageable);
    
    Page<Producto> findByTieneRecetaTrueAndEstaActivoTrue(Pageable pageable);
    
    boolean existsByNombreAndEstaActivoTrue(String nombre);
}