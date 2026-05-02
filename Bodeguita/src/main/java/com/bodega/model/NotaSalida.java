package com.bodega.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Representa la cabecera de una nota de salida o venta. */
public class NotaSalida {
    private int idSalida;
    private Cliente cliente;
    private String numeroFactura;
    private LocalDate fechaEmision;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;
    private BigDecimal costoTotalFifo;
    private BigDecimal utilidad;
    private String estado;
    private String observaciones;

    public NotaSalida() {
        this.fechaEmision = LocalDate.now();
        this.subtotal = BigDecimal.ZERO;
        this.iva = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.costoTotalFifo = BigDecimal.ZERO;
        this.utilidad = BigDecimal.ZERO;
        this.estado = "completada";
    }

    public NotaSalida(int idSalida, Cliente cliente, String numeroFactura, LocalDate fechaEmision,
            BigDecimal subtotal, BigDecimal iva, BigDecimal total, BigDecimal costoTotalFifo,
            BigDecimal utilidad, String estado, String observaciones) {
        this.idSalida = idSalida;
        this.cliente = cliente;
        this.numeroFactura = numeroFactura;
        this.fechaEmision = fechaEmision;
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
        this.costoTotalFifo = costoTotalFifo;
        this.utilidad = utilidad;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    public int getIdSalida() { return idSalida; }
    public void setIdSalida(int idSalida) { this.idSalida = idSalida; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getIva() { return iva; }
    public void setIva(BigDecimal iva) { this.iva = iva; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public BigDecimal getCostoTotalFifo() { return costoTotalFifo; }
    public void setCostoTotalFifo(BigDecimal costoTotalFifo) { this.costoTotalFifo = costoTotalFifo; }
    public BigDecimal getUtilidad() { return utilidad; }
    public void setUtilidad(BigDecimal utilidad) { this.utilidad = utilidad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    @Override
    public String toString() { return numeroFactura; }
}