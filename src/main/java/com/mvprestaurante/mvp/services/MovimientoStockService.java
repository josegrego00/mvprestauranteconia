package com.mvprestaurante.mvp.services;

import com.mvprestaurante.mvp.models.*;
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
    public void registrarMovimiento(Ingrediente ingrediente, Double stockAnterior, Double cantidad, String tipoMovimiento, String origen, Empresa empresa, Usuario usuario) {
        MovimientoStock movimiento = MovimientoStock.builder()
                .fechaMovimiento(LocalDateTime.now())
                .empresa(empresa)
                .ingrediente(ingrediente)
                .tipoItem("INGREDIENTE")
                .tipoMovimiento(tipoMovimiento)
                .cantidad(cantidad != null ? cantidad.intValue() : 0)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockAnterior + cantidad)
                .origen(origen)
                .usuario(usuario)
                .build();
        movimientoStockRepository.save(movimiento);
    }

    @Transactional
    public void registrarMovimiento(Producto producto, Double stockAnterior, Double cantidad, String tipoMovimiento, String origen, Empresa empresa, Usuario usuario) {
        MovimientoStock movimiento = MovimientoStock.builder()
                .fechaMovimiento(LocalDateTime.now())
                .empresa(empresa)
                .producto(producto)
                .tipoItem("PRODUCTO")
                .tipoMovimiento(tipoMovimiento)
                .cantidad(cantidad != null ? cantidad.intValue() : 0)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockAnterior + cantidad)
                .origen(origen)
                .usuario(usuario)
                .build();
        movimientoStockRepository.save(movimiento);
    }

    @Transactional
    public void registrarMovimiento(Ingrediente ingrediente, Double stockAnterior, Double cantidad, String tipoMovimiento, String origen, Venta venta, Empresa empresa, Usuario usuario) {
        MovimientoStock movimiento = MovimientoStock.builder()
                .fechaMovimiento(LocalDateTime.now())
                .empresa(empresa)
                .ingrediente(ingrediente)
                .tipoItem("INGREDIENTE")
                .tipoMovimiento(tipoMovimiento)
                .cantidad(cantidad != null ? cantidad.intValue() : 0)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockAnterior + cantidad)
                .origen(origen)
                .venta(venta)
                .usuario(usuario)
                .build();
        movimientoStockRepository.save(movimiento);
    }

    @Transactional
    public void registrarMovimiento(Producto producto, Double stockAnterior, Double cantidad, String tipoMovimiento, String origen, Venta venta, Empresa empresa, Usuario usuario) {
        MovimientoStock movimiento = MovimientoStock.builder()
                .fechaMovimiento(LocalDateTime.now())
                .empresa(empresa)
                .producto(producto)
                .tipoItem("PRODUCTO")
                .tipoMovimiento(tipoMovimiento)
                .cantidad(cantidad != null ? cantidad.intValue() : 0)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockAnterior + cantidad)
                .origen(origen)
                .venta(venta)
                .usuario(usuario)
                .build();
        movimientoStockRepository.save(movimiento);
    }

    @Transactional
    public void registrarMovimiento(Ingrediente ingrediente, Double stockAnterior, Double cantidad, String tipoMovimiento, String origen, Compra compra, Empresa empresa, Usuario usuario) {
        MovimientoStock movimiento = MovimientoStock.builder()
                .fechaMovimiento(LocalDateTime.now())
                .empresa(empresa)
                .ingrediente(ingrediente)
                .tipoItem("INGREDIENTE")
                .tipoMovimiento(tipoMovimiento)
                .cantidad(cantidad != null ? cantidad.intValue() : 0)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockAnterior + cantidad)
                .origen(origen)
                .compra(compra)
                .usuario(usuario)
                .build();
        movimientoStockRepository.save(movimiento);
    }

    @Transactional
    public void registrarMovimiento(Producto producto, Double stockAnterior, Double cantidad, String tipoMovimiento, String origen, Compra compra, Empresa empresa, Usuario usuario) {
        MovimientoStock movimiento = MovimientoStock.builder()
                .fechaMovimiento(LocalDateTime.now())
                .empresa(empresa)
                .producto(producto)
                .tipoItem("PRODUCTO")
                .tipoMovimiento(tipoMovimiento)
                .cantidad(cantidad != null ? cantidad.intValue() : 0)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockAnterior + cantidad)
                .origen(origen)
                .compra(compra)
                .usuario(usuario)
                .build();
        movimientoStockRepository.save(movimiento);
    }
}
