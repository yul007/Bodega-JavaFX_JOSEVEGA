package com.bodega.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Representa un movimiento valorado de entrada o salida en el Kardex. */
public class MovimientoKardex {
    private int idMovimiento;
    private Producto producto;
    private Lote lote;
    private NotaSalida notaSalida;
    private LocalDate fecha;
    private String tipo;
    private String referencia;
    private BigDecimal cantidadEntrada;
    private BigDecimal costoUnitarioEntrada;
    private BigDecimal valorEntrada;
    private BigDecimal cantidadSalida;
    private BigDecimal costoUnitarioSalida;
    private BigDecimal valorSalida;
    private BigDecimal saldoCantidad;
    private BigDecimal saldoValor;
    private String observacion;

    public MovimientoKardex() {
        this.fecha = LocalDate.now();
        this.cantidadEntrada = BigDecimal.ZERO;
        this.valorEntrada = BigDecimal.ZERO;
        this.cantidadSalida = BigDecimal.ZERO;
        this.valorSalida = BigDecimal.ZERO;
        this.saldoCantidad = BigDecimal.ZERO;
        this.saldoValor = BigDecimal.ZERO;
    }

    public MovimientoKardex(int idMovimiento, Producto producto, Lote lote,
            NotaSalida notaSalida, LocalDate fecha, String tipo, String referencia,
            BigDecimal cantidadEntrada, BigDecimal costoUnitarioEntrada,
            BigDecimal valorEntrada, BigDecimal cantidadSalida,
            BigDecimal costoUnitarioSalida, BigDecimal valorSalida,
            BigDecimal saldoCantidad, BigDecimal saldoValor, String observacion) {
        this.idMovimiento = idMovimiento;
        this.producto = producto;
        this.lote = lote;
        this.notaSalida = notaSalida;
        this.fecha = fecha;
        this.tipo = tipo;
        this.referencia = referencia;
        this.cantidadEntrada = cantidadEntrada;
        this.costoUnitarioEntrada = costoUnitarioEntrada;
        this.valorEntrada = valorEntrada;
        this.cantidadSalida = cantidadSalida;
        this.costoUnitarioSalida = costoUnitarioSalida;
        this.valorSalida = valorSalida;
        this.saldoCantidad = saldoCantidad;
        this.saldoValor = saldoValor;
        this.observacion = observacion;
    }

    public int getIdMovimiento() { return idMovimiento; }
    public void setIdMovimiento(int idMovimiento) { this.idMovimiento = idMovimiento; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }
    public NotaSalida getNotaSalida() { return notaSalida; }
    public void setNotaSalida(NotaSalida notaSalida) { this.notaSalida = notaSalida; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public BigDecimal getCantidadEntrada() { return cantidadEntrada; }
    public void setCantidadEntrada(BigDecimal cantidadEntrada) { this.cantidadEntrada = cantidadEntrada; }
    public BigDecimal getCostoUnitarioEntrada() { return costoUnitarioEntrada; }
    public void setCostoUnitarioEntrada(BigDecimal costoUnitarioEntrada) { this.costoUnitarioEntrada = costoUnitarioEntrada; }
    public BigDecimal getValorEntrada() { return valorEntrada; }
    public void setValorEntrada(BigDecimal valorEntrada) { this.valorEntrada = valorEntrada; }
    public BigDecimal getCantidadSalida() { return cantidadSalida; }
    public void setCantidadSalida(BigDecimal cantidadSalida) { this.cantidadSalida = cantidadSalida; }
    public BigDecimal getCostoUnitarioSalida() { return costoUnitarioSalida; }
    public void setCostoUnitarioSalida(BigDecimal costoUnitarioSalida) { this.costoUnitarioSalida = costoUnitarioSalida; }
    public BigDecimal getValorSalida() { return valorSalida; }
    public void setValorSalida(BigDecimal valorSalida) { this.valorSalida = valorSalida; }
    public BigDecimal getSaldoCantidad() { return saldoCantidad; }
    public void setSaldoCantidad(BigDecimal saldoCantidad) { this.saldoCantidad = saldoCantidad; }
    public BigDecimal getSaldoValor() { return saldoValor; }
    public void setSaldoValor(BigDecimal saldoValor) { this.saldoValor = saldoValor; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}