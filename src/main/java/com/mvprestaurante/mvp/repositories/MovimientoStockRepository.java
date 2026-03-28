package com.mvprestaurante.mvp.repositories;

import com.mvprestaurante.mvp.models.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    @Query("SELECT COALESCE(SUM(m.cantidad), 0) FROM MovimientoStock m " +
            "WHERE m.empresa.id = :empresaId " +
            "AND m.tipoItem = :tipoItem " +
            "AND ((m.ingrediente.id = :itemId) OR (m.producto.id = :itemId)) " +
            "AND m.tipoMovimiento = 'SALIDA' " +
            "AND m.origen = 'VENTA' " +
            "AND m.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin")
    Integer sumConsumoByItem(
            @Param("empresaId") Long empresaId,
            @Param("tipoItem") String tipoItem,
            @Param("itemId") Long itemId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT COALESCE(SUM(m.cantidad), 0) FROM MovimientoStock m " +
            "WHERE m.empresa.id = :empresaId " +
            "AND m.tipoItem = :tipoItem " +
            "AND ((m.ingrediente.id = :itemId) OR (m.producto.id = :itemId)) " +
            "AND m.tipoMovimiento = 'ENTRADA' " +
            "AND m.origen = 'COMPRA' " +
            "AND m.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin")
    Integer sumComprasByItem(
            @Param("empresaId") Long empresaId,
            @Param("tipoItem") String tipoItem,
            @Param("itemId") Long itemId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
}
