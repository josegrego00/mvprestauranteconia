package com.mvprestaurante.mvp.repositories;

import com.mvprestaurante.mvp.models.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    @Query("SELECT dv.producto.id, dv.producto.nombre, SUM(dv.cantidad), SUM(dv.subtotal) " +
            "FROM DetalleVenta dv " +
            "JOIN dv.venta v " +
            "WHERE v.empresa.id = :tenantId AND v.estado = 'COMPLETADA' " +
            "AND v.fechaVenta BETWEEN :fechaInicio AND :fechaFin " +
            "GROUP BY dv.producto.id, dv.producto.nombre " +
            "ORDER BY SUM(dv.cantidad) DESC")
    List<Object[]> findTopProductosByVentasBetween(
            @Param("tenantId") Long tenantId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT dv.producto.id, dv.producto.nombre, SUM(dv.cantidad), SUM(dv.subtotal) " +
            "FROM DetalleVenta dv " +
            "JOIN dv.venta v " +
            "WHERE v.empresa.id = :tenantId AND v.estado = 'COMPLETADA' " +
            "GROUP BY dv.producto.id, dv.producto.nombre " +
            "ORDER BY SUM(dv.cantidad) DESC")
    List<Object[]> findTopProductosAll(@Param("tenantId") Long tenantId);
}
