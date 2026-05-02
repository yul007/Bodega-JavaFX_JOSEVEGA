package com.bodega.model;

import java.math.BigDecimal;

/** Representa una linea de venta asociada a producto y lote FIFO. */
public class DetalleSalida {
    private int idDetalleSalida;
    private NotaSalida notaSalida;
    private Producto producto;
    private Lote lote;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private BigDecimal costoUnitarioFifo;
    private BigDecimal costoTotalFifo;
    private BigDecimal utilidad;

    public DetalleSalida() {
        this.cantidad = BigDecimal.ZERO;
        this.precioUnitario = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
        this.costoUnitarioFifo = BigDecimal.ZERO;
        this.costoTotalFifo = BigDecimal.ZERO;
        this.utilidad = BigDecimal.ZERO;
    }

    public DetalleSalida(int idDetalleSalida, NotaSalida notaSalida, Producto producto,
            Lote lote, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal subtotal,
            BigDecimal costoUnitarioFifo, BigDecimal costoTotalFifo, BigDecimal utilidad) {
        this.idDetalleSalida = idDetalleSalida;
        this.notaSalida = notaSalida;
        this.producto = producto;
        this.lote = lote;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.costoUnitarioFifo = costoUnitarioFifo;
        this.costoTotalFifo = costoTotalFifo;
        this.utilidad = utilidad;
    }

    public int getIdDetalleSalida() { return idDetalleSalida; }
    public void setIdDetalleSalida(int idDetalleSalida) { this.idDetalleSalida = idDetalleSalida; }
    public NotaSalida getNotaSalida() { return notaSalida; }
    public void setNotaSalida(NotaSalida notaSalida) { this.notaSalida = notaSalida; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getCostoUnitarioFifo() { return costoUnitarioFifo; }
    public void setCostoUnitarioFifo(BigDecimal costoUnitarioFifo) { this.costoUnitarioFifo = costoUnitarioFifo; }
    public BigDecimal getCostoTotalFifo() { return costoTotalFifo; }
    public void setCostoTotalFifo(BigDecimal costoTotalFifo) { this.costoTotalFifo = costoTotalFifo; }
    public BigDecimal getUtilidad() { return utilidad; }
    public void setUtilidad(BigDecimal utilidad) { this.utilidad = utilidad; }
}