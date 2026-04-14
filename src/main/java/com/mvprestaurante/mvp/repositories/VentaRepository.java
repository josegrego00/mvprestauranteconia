package com.mvprestaurante.mvp.repositories;

import com.mvprestaurante.mvp.models.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("SELECT v FROM Venta v JOIN FETCH v.empresa JOIN FETCH v.usuario JOIN FETCH v.cliente " +
            "WHERE v.id = :id AND v.empresa.id = :tenantId")
    Optional<Venta> findByIdWithDetails(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Query("SELECT v FROM Venta v JOIN FETCH v.empresa WHERE v.empresa.id = :tenantId ORDER BY v.fechaVenta DESC")
    Page<Venta> findAllByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT v FROM Venta v JOIN FETCH v.empresa WHERE v.empresa.id = :tenantId " +
            "AND LOWER(v.numeroVenta) LIKE LOWER(CONCAT('%', :numero, '%')) ORDER BY v.fechaVenta DESC")
    Page<Venta> findByNumeroContainingIgnoreCase(@Param("tenantId") Long tenantId, @Param("numero") String numero, Pageable pageable);

    @Query("SELECT v FROM Venta v JOIN FETCH v.empresa WHERE v.empresa.id = :tenantId " +
            "AND v.estado = :estado ORDER BY v.fechaVenta DESC")
    Page<Venta> findByEstado(@Param("tenantId") Long tenantId, @Param("estado") String estado, Pageable pageable);

    @Query("SELECT v FROM Venta v JOIN FETCH v.empresa WHERE v.empresa.id = :tenantId " +
            "AND v.fechaVenta BETWEEN :fechaInicio AND :fechaFin ORDER BY v.fechaVenta DESC")
    Page<Venta> findByFechaBetween(@Param("tenantId") Long tenantId, 
            @Param("fechaInicio") LocalDateTime fechaInicio, 
            @Param("fechaFin") LocalDateTime fechaFin, Pageable pageable);

    @Query("SELECT MAX(v.numeroVenta) FROM Venta v WHERE v.empresa.id = :tenantId AND v.numeroVenta LIKE :prefix%")
    Optional<String> findMaxNumeroVentaByPrefix(@Param("tenantId") Long tenantId, @Param("prefix") String prefix);

    boolean existsByNumeroVentaAndEmpresaId(String numeroVenta, Long empresaId);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.empresa.id = :tenantId AND v.estado = 'COMPLETADA'")
    Double sumTotalByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.empresa.id = :tenantId AND v.estado = 'COMPLETADA' " +
            "AND v.fechaVenta BETWEEN :fechaInicio AND :fechaFin")
    Double sumTotalByFechaBetween(@Param("tenantId") Long tenantId, @Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.empresa.id = :tenantId AND v.estado = 'COMPLETADA' " +
            "AND v.fechaVenta BETWEEN :fechaInicio AND :fechaFin")
    Integer countByFechaBetween(@Param("tenantId") Long tenantId, @Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT v FROM Venta v JOIN FETCH v.detallesVenta dv JOIN FETCH dv.producto " +
            "WHERE v.empresa.id = :tenantId AND v.estado = 'COMPLETADA' " +
            "AND v.fechaVenta BETWEEN :fechaInicio AND :fechaFin ORDER BY v.fechaVenta ASC")
    List<Venta> findVentasDelDia(@Param("tenantId") Long tenantId, @Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT COALESCE(SUM(v.pagoEfectivo), 0) FROM Venta v WHERE v.empresa.id = :tenantId AND v.estado = 'COMPLETADA' " +
            "AND v.fechaVenta BETWEEN :fechaInicio AND :fechaFin")
    Double sumEfectivoByFechaBetween(@Param("tenantId") Long tenantId, @Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT COALESCE(SUM(v.pagoTarjeta), 0) FROM Venta v WHERE v.empresa.id = :tenantId AND v.estado = 'COMPLETADA' " +
            "AND v.fechaVenta BETWEEN :fechaInicio AND :fechaFin")
    Double sumTarjetaByFechaBetween(@Param("tenantId") Long tenantId, @Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT COALESCE(SUM(v.pagoTransferencia), 0) FROM Venta v WHERE v.empresa.id = :tenantId AND v.estado = 'COMPLETADA' " +
            "AND v.fechaVenta BETWEEN :fechaInicio AND :fechaFin")
    Double sumTransferenciaByFechaBetween(@Param("tenantId") Long tenantId, @Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.empresa.id = :tenantId AND v.estado = 'ANULADA' " +
            "AND v.fechaVenta BETWEEN :fechaInicio AND :fechaFin")
    Integer countAnuladasByFechaBetween(@Param("tenantId") Long tenantId, @Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);
}
