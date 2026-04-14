package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.DTO.MovimientoStockDTO;
import com.mvprestaurante.mvp.models.Compra;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.models.MovimientoStock;
import com.mvprestaurante.mvp.models.Producto;
import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.models.Venta;
import com.mvprestaurante.mvp.repositories.MovimientoStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoStockRepository;

    @Transactional
    public void registrarMovimiento(
            Object item,
            Double stockAnterior,
            Double cantidad,
            String tipoMovimiento,
            String origen,
            Object referencia,
            Empresa empresa,
            Usuario usuario) {

        MovimientoStock.MovimientoStockBuilder builder = MovimientoStock.builder()
                .fechaMovimiento(LocalDateTime.now())
                .empresa(empresa)
                .tipoMovimiento(tipoMovimiento)
                .cantidad(cantidad != null ? cantidad.intValue() : 0)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockAnterior + cantidad)
                .origen(origen)
                .usuario(usuario);

        if (item instanceof Ingrediente) {
            builder.ingrediente((Ingrediente) item)
                    .tipoItem("INGREDIENTE");
        } else if (item instanceof Producto) {
            builder.producto((Producto) item)
                    .tipoItem("PRODUCTO");
        }

        if (referencia instanceof Venta) {
            builder.venta((Venta) referencia);
        } else if (referencia instanceof Compra) {
            builder.compra((Compra) referencia);
        }

        movimientoStockRepository.save(builder.build());
    }
}