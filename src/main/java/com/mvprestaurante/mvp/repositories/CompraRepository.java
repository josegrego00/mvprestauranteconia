package com.mvprestaurante.mvp.repositories;

import com.mvprestaurante.mvp.models.Compra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

        @Query("SELECT c FROM Compra c JOIN FETCH c.empresa JOIN FETCH c.usuario WHERE c.id = :id AND c.empresa.id = :tenantId")
        Optional<Compra> findByIdAndEmpresaId(@Param("id") Long id, @Param("tenantId") Long tenantId);

        @Query("SELECT c FROM Compra c JOIN FETCH c.empresa JOIN FETCH c.usuario WHERE c.id = :id AND c.empresa.id = :tenantId AND c.estado = :estado")
        Optional<Compra> findByIdAndEmpresaIdAndEstado(@Param("id") Long id, @Param("tenantId") Long tenantId, @Param("estado") String estado);
        @Query("SELECT c FROM Compra c JOIN FETCH c.empresa WHERE c.empresa.id = :tenantId ORDER BY c.fechaCompra DESC")
        Page<Compra> findAllByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

        @Query("SELECT c FROM Compra c JOIN FETCH c.empresa WHERE c.empresa.id = :tenantId " +
                        "AND LOWER(c.numeroCompra) LIKE LOWER(CONCAT('%', :numero, '%')) ORDER BY c.fechaCompra DESC")
        Page<Compra> findByNumeroContainingIgnoreCase(@Param("tenantId") Long tenantId, @Param("numero") String numero,
                        Pageable pageable);

        @Query("SELECT c FROM Compra c JOIN FETCH c.empresa WHERE c.empresa.id = :tenantId " +
                        "AND c.estado = :estado ORDER BY c.fechaCompra DESC")
        Page<Compra> findByEstado(@Param("tenantId") Long tenantId, @Param("estado") String estado, Pageable pageable);

        @Query("SELECT c FROM Compra c JOIN FETCH c.empresa WHERE c.empresa.id = :tenantId " +
                        "AND c.fechaCompra BETWEEN :fechaInicio AND :fechaFin ORDER BY c.fechaCompra DESC")
        Page<Compra> findByFechaBetween(@Param("tenantId") Long tenantId,
                        @Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin, Pageable pageable);

        @Query("SELECT MAX(c.numeroCompra) FROM Compra c WHERE c.empresa.id = :tenantId AND c.numeroCompra LIKE :prefix%")
        Optional<String> findMaxNumeroCompraByPrefix(@Param("tenantId") Long tenantId, @Param("prefix") String prefix);

        boolean existsByNumeroCompraAndEmpresaId(String numeroCompra, Long empresaId);

        boolean existsByNumeroCompraAndEmpresaIdAndIdNot(String numeroCompra, Long empresaId, Long id);

        @Query("SELECT COALESCE(SUM(c.total), 0) FROM Compra c WHERE c.empresa.id = :tenantId AND c.estado = 'COMPLETADA' "
                        +
                        "AND c.fechaCompra BETWEEN :fechaInicio AND :fechaFin")
        Double sumTotalByFechaBetween(@Param("tenantId") Long tenantId, @Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin);

        @Query("SELECT COUNT(c) FROM Compra c WHERE c.empresa.id = :tenantId AND c.estado = 'COMPLETADA' " +
                        "AND c.fechaCompra BETWEEN :fechaInicio AND :fechaFin")
        Integer countByFechaBetween(@Param("tenantId") Long tenantId, @Param("fechaInicio") LocalDateTime fechaInicio,
                        @Param("fechaFin") LocalDateTime fechaFin);
}
