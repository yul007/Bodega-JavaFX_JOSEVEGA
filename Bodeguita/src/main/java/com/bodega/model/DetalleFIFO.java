package com.bodega.model;

import java.math.BigDecimal;

/** Detalla cuanto se consumio de un lote durante una salida FIFO. */
public class DetalleFIFO {
    private Lote lote;
    private BigDecimal cantidadConsumida;
    private BigDecimal costoUnitario;
    private BigDecimal costoTotal;
    private BigDecimal cantidadDisponibleRestante;

    public DetalleFIFO() {
        this.cantidadConsumida = BigDecimal.ZERO;
        this.costoUnitario = BigDecimal.ZERO;
        this.costoTotal = BigDecimal.ZERO;
        this.cantidadDisponibleRestante = BigDecimal.ZERO;
    }

    public DetalleFIFO(Lote lote, BigDecimal cantidadConsumida, BigDecimal costoUnitario,
            BigDecimal costoTotal, BigDecimal cantidadDisponibleRestante) {
        this.lote = lote;
        this.cantidadConsumida = cantidadConsumida;
        this.costoUnitario = costoUnitario;
        this.costoTotal = costoTotal;
        this.cantidadDisponibleRestante = cantidadDisponibleRestante;
    }

    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }
    public BigDecimal getCantidadConsumida() { return cantidadConsumida; }
    public void setCantidadConsumida(BigDecimal cantidadConsumida) { this.cantidadConsumida = cantidadConsumida; }
    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }
    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }
    public BigDecimal getCantidadDisponibleRestante() { return cantidadDisponibleRestante; }
    public void setCantidadDisponibleRestante(BigDecimal cantidadDisponibleRestante) { this.cantidadDisponibleRestante = cantidadDisponibleRestante; }
}