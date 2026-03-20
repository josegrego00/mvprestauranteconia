package com.mvprestaurante.mvp.repositories;

import com.mvprestaurante.mvp.models.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

       @Query("SELECT p FROM Producto p WHERE p.empresa.id = :tenantId AND p.estaActivo = true")
       Page<Producto> findByEstaActivoTrue(@Param("tenantId") Long tenantId, Pageable pageable);

       @Query("SELECT p FROM Producto p WHERE p.empresa.id = :tenantId " +
                     "AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND p.estaActivo = true")
       Page<Producto> findByNombreContainingIgnoreCaseAndEstaActivoTrue(
                     @Param("tenantId") Long tenantId,
                     @Param("nombre") String nombre,
                     Pageable pageable);

       @Query("SELECT p FROM Producto p WHERE p.empresa.id = :tenantId " +
                     "AND p.tieneReceta = true AND p.estaActivo = true")
       Page<Producto> findByTieneRecetaTrueAndEstaActivoTrue(
                     @Param("tenantId") Long tenantId,
                     Pageable pageable);

       // 🔥 ESTE ES EL IMPORTANTE
       boolean existsByNombreIgnoreCaseAndEmpresaIdAndEstaActivoTrue(
                     String nombre,
                     Long empresaId);
}