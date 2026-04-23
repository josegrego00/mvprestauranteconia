package com.mvprestaurante.mvp.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mvprestaurante.mvp.enums.TipoCierre;
import com.mvprestaurante.mvp.models.CierreDia;

@Repository
public interface CierreDiaRepository extends JpaRepository<CierreDia, Long> {

    boolean existsByEmpresaIdAndFechaAndTipo(Long empresaId, LocalDate fecha, TipoCierre tipo);

    Optional<CierreDia> findByEmpresaIdAndFechaAndTipo(Long empresaId, LocalDate fecha, TipoCierre tipo);

    List<CierreDia> findByEmpresaIdOrderByFechaDesc(Long empresaId);
}