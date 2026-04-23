package com.mvprestaurante.mvp.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mvprestaurante.mvp.enums.OrigenMovimiento;
import com.mvprestaurante.mvp.enums.TipoItem;
import com.mvprestaurante.mvp.enums.TipoMovimiento;
import com.mvprestaurante.mvp.models.Compra;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.models.MovimientoStock;
import com.mvprestaurante.mvp.models.Producto;
import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.models.Venta;
import com.mvprestaurante.mvp.repositories.MovimientoStockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoStockRepository;

    @Transactional
    public void registrarMovimiento(
            Object item,
            BigDecimal stockAnterior,
            BigDecimal cantidad,
            TipoMovimiento tipoMovimiento,
            OrigenMovimiento origen,
            Object referencia,
            Empresa empresa,
            Usuario usuario) {

        MovimientoStock.MovimientoStockBuilder builder = MovimientoStock.builder()
                .fechaMovimiento(LocalDateTime.now())
                .empresa(empresa)
                .tipoMovimiento(tipoMovimiento)
                .cantidad(cantidad != null ? cantidad : BigDecimal.ZERO)
                .stockAnterior(stockAnterior != null ? stockAnterior : BigDecimal.ZERO)
                .stockNuevo(stockAnterior != null && cantidad != null ? stockAnterior.add(cantidad) : BigDecimal.ZERO)
                .origen(origen)
                .usuario(usuario);

        if (item instanceof Ingrediente) {
            builder.ingrediente((Ingrediente) item)
                    .tipoItem(TipoItem.INGREDIENTE);
        } else if (item instanceof Producto) {
            builder.producto((Producto) item)
                    .tipoItem(TipoItem.PRODUCTO);
        }

        if (referencia instanceof Venta) {
            builder.venta((Venta) referencia);
        } else if (referencia instanceof Compra) {
            builder.compra((Compra) referencia);
        }

        movimientoStockRepository.save(builder.build());
    }
}