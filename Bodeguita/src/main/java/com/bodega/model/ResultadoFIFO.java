package com.bodega.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Resultado calculado despues de aplicar FIFO a una salida de inventario. */
public class ResultadoFIFO {
    private Producto producto;
    private BigDecimal cantidadSolicitada;
    private BigDecimal costoTotal;
    private BigDecimal stockAnterior;
    private BigDecimal stockNuevo;
    private List<DetalleFIFO> detalles;

    public ResultadoFIFO() {
        this.cantidadSolicitada = BigDecimal.ZERO;
        this.costoTotal = BigDecimal.ZERO;
        this.stockAnterior = BigDecimal.ZERO;
        this.stockNuevo = BigDecimal.ZERO;
        this.detalles = new ArrayList<>();
    }

    public ResultadoFIFO(Producto producto, BigDecimal cantidadSolicitada, BigDecimal costoTotal,
            BigDecimal stockAnterior, BigDecimal stockNuevo, List<DetalleFIFO> detalles) {
        this.producto = producto;
        this.cantidadSolicitada = cantidadSolicitada;
        this.costoTotal = costoTotal;
        this.stockAnterior = stockAnterior;
        this.stockNuevo = stockNuevo;
        this.detalles = new ArrayList<>(detalles);
    }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public BigDecimal getCantidadSolicitada() { return cantidadSolicitada; }
    public void setCantidadSolicitada(BigDecimal cantidadSolicitada) { this.cantidadSolicitada = cantidadSolicitada; }
    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }
    public BigDecimal getStockAnterior() { return stockAnterior; }
    public void setStockAnterior(BigDecimal stockAnterior) { this.stockAnterior = stockAnterior; }
    public BigDecimal getStockNuevo() { return stockNuevo; }
    public void setStockNuevo(BigDecimal stockNuevo) { this.stockNuevo = stockNuevo; }
    public List<DetalleFIFO> getDetalles() { return Collections.unmodifiableList(detalles); }
    public void setDetalles(List<DetalleFIFO> detalles) { this.detalles = new ArrayList<>(detalles); }
}