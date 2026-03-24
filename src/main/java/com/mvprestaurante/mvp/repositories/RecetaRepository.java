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

    @Query("SELECT r FROM Receta r WHERE r.empresa.id = :tenantId AND r.estaActiva = true")
    Page<Receta> findByEstaActivaTrue(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT r FROM Receta r WHERE r.empresa.id = :tenantId " +
            "AND LOWER(r.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND r.estaActiva = true")
    Page<Receta> findByNombreContainingIgnoreCaseAndEstaActivaTrue(
            @Param("tenantId") Long tenantId,
            @Param("nombre") String nombre,
            Pageable pageable);

    @Query("SELECT r FROM Receta r WHERE r.empresa.id = :tenantId " +
            "AND r.producto.id = :productoId AND r.estaActiva = true")
    Page<Receta> findByProductoId(
            @Param("tenantId") Long tenantId,
            @Param("productoId") Long productoId,
            Pageable pageable);

    @Query("SELECT COUNT(r) > 0 FROM Receta r " +
            "WHERE r.empresa.id = :tenantId AND LOWER(r.nombre) = LOWER(:nombre) AND r.estaActiva = true")
    boolean existsByNombreAndEstaActivaTrue(
            @Param("tenantId") Long tenantId,
            @Param("nombre") String nombre);

    @Query("SELECT r FROM Receta r WHERE r.empresa.id = :tenantId AND r.estaActiva = true AND r.producto IS NULL")
    Page<Receta> findBySinProducto(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT r FROM Receta r WHERE r.empresa.id = :tenantId AND r.estaActiva = true AND (r.producto IS NULL OR r.producto.id = :productoId)")
    Page<Receta> findDisponiblesParaProducto(@Param("tenantId") Long tenantId, @Param("productoId") Long productoId, Pageable pageable);
}