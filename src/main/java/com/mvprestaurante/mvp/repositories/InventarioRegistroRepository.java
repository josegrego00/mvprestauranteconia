package com.mvprestaurante.mvp.repositories;

import com.mvprestaurante.mvp.models.InventarioRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface InventarioRegistroRepository extends JpaRepository<InventarioRegistro, Long> {

    @Query("SELECT ir FROM InventarioRegistro ir WHERE ir.empresa.id = :tenantId AND ir.fecha = :fecha")
    Optional<InventarioRegistro> findByFechaAndTenantId(@Param("tenantId") Long tenantId, @Param("fecha") LocalDate fecha);

    @Query("SELECT ir FROM InventarioRegistro ir JOIN FETCH ir.detalles " +
            "WHERE ir.empresa.id = :tenantId AND ir.fecha = :fecha")
    Optional<InventarioRegistro> findByFechaAndTenantIdWithDetalles(@Param("tenantId") Long tenantId, @Param("fecha") LocalDate fecha);

    @Query("SELECT ir FROM InventarioRegistro ir JOIN FETCH ir.detalles " +
            "WHERE ir.empresa.id = :tenantId AND ir.fecha BETWEEN :fechaInicio AND :fechaFin " +
            "ORDER BY ir.fecha")
    java.util.List<InventarioRegistro> findByFechaBetweenAndTenantId(
            @Param("tenantId") Long tenantId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);
}
