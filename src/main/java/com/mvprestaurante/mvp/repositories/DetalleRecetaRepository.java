package com.mvprestaurante.mvp.repositories;

import com.mvprestaurante.mvp.models.DetalleReceta;
import com.mvprestaurante.mvp.models.Receta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleRecetaRepository extends JpaRepository<DetalleReceta, Long> {

    @Query("SELECT dr FROM DetalleReceta dr WHERE dr.receta.id = :recetaId " +
            "AND dr.receta.empresa.id = :tenantId")
    Page<DetalleReceta> findByRecetaId(@Param("tenantId") Long tenantId,
            @Param("recetaId") Long recetaId,
            Pageable pageable);

    @Modifying
    @Query("DELETE FROM DetalleReceta dr WHERE dr.receta.id = :recetaId " +
            "AND dr.receta.empresa.id = :tenantId")
    void deleteByRecetaId(@Param("tenantId") Long tenantId, @Param("recetaId") Long recetaId);

    @Query("SELECT COUNT(dr) > 0 FROM DetalleReceta dr " +
            "WHERE dr.receta.id = :recetaId AND dr.ingrediente.id = :ingredienteId " +
            "AND dr.receta.empresa.id = :tenantId")
    boolean existsByRecetaAndIngrediente(@Param("tenantId") Long tenantId,
            @Param("recetaId") Long recetaId,
            @Param("ingredienteId") Long ingredienteId);

    @Query("SELECT DISTINCT dr.receta FROM DetalleReceta dr " +
            "WHERE dr.ingrediente.id = :ingredienteId " +
            "AND dr.receta.empresa.id = :tenantId " +
            "AND dr.receta.estaActiva = true")
    List<Receta> findRecetasByIngredienteId(@Param("tenantId") Long tenantId,
            @Param("ingredienteId") Long ingredienteId);
}