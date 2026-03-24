package com.mvprestaurante.mvp.repositories;

import com.mvprestaurante.mvp.models.Ingrediente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {

    // Find active ingredients for a specific company (tenant)
    @Query("SELECT i FROM Ingrediente i WHERE i.empresa.id = :tenantId AND i.estaActivo = true")
    Page<Ingrediente> findByEstaActivoTrue(@Param("tenantId") Long tenantId, Pageable pageable);

    // Search by name for a specific company
    @Query("SELECT i FROM Ingrediente i WHERE i.empresa.id = :tenantId " +
            "AND LOWER(i.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND i.estaActivo = true")
    Page<Ingrediente> findByNombreContainingIgnoreCaseAndEstaActivoTrue(@Param("tenantId") Long tenantId,
            @Param("nombre") String nombre,
            Pageable pageable);

    @Query("SELECT COUNT(i) > 0 FROM Ingrediente i " +
            "WHERE i.empresa.id = :tenantId AND LOWER(i.nombre) = LOWER(:nombre) AND i.estaActivo = true")
    boolean existsByNombreAndEstaActivoTrue(
            @Param("tenantId") Long tenantId,
            @Param("nombre") String nombre);

    @Query("SELECT COUNT(d) > 0 FROM DetalleReceta d WHERE d.ingrediente.id = :ingredienteId")
    boolean existsByIngredienteEnReceta(@Param("ingredienteId") Long ingredienteId);
}