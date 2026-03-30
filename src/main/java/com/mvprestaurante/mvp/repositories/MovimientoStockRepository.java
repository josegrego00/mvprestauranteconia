package com.mvprestaurante.mvp.repositories;

import com.mvprestaurante.mvp.models.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
}
