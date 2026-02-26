package com.mvprestaurante.mvp.repositories;

import com.mvprestaurante.mvp.models.DetalleReceta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleRecetaRepository extends JpaRepository<DetalleReceta, Long> {
    
    Page<DetalleReceta> findByRecetaId(Long recetaId, Pageable pageable);
    
    void deleteByRecetaId(Long recetaId);
}