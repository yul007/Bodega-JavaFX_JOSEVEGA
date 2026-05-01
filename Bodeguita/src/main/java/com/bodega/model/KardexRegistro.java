package com.bodega.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Fila lista para mostrar el Kardex valorado de un producto. */
public class KardexRegistro {

    private LocalDate fecha;
    private String tipo;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal valorMovimiento;
    private BigDecimal saldoCantidad;
    private BigDecimal saldoValor;
    private String referencia;
    private String lote;
    private String observacion;

    public KardexRegistro() {
        this.cantidad = BigDecimal.ZERO;
        this.costoUnitario = BigDecimal.ZERO;
        this.valorMovimiento = BigDecimal.ZERO;
        this.saldoCantidad = BigDecimal.ZERO;
        this.saldoValor = BigDecimal.ZERO;
    }

    public KardexRegistro(LocalDate fecha, String tipo, BigDecimal cantidad,
            BigDecimal costoUnitario, BigDecimal valorMovimiento, BigDecimal saldoCantidad,
            BigDecimal saldoValor, String referencia, String lote, String observacion) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.valorMovimiento = valorMovimiento;
        this.saldoCantidad = saldoCantidad;
        this.saldoValor = saldoValor;
        this.referencia = referencia;
        this.lote = lote;
        this.observacion = observacion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public BigDecimal getValorMovimiento() {
        return valorMovimiento;
    }

    public void setValorMovimiento(BigDecimal valorMovimiento) {
        this.valorMovimiento = valorMovimiento;
    }

    public BigDecimal getSaldoCantidad() {
        return saldoCantidad;
    }

    public void setSaldoCantidad(BigDecimal saldoCantidad) {
        this.saldoCantidad = saldoCantidad;
    }

    public BigDecimal getSaldoValor() {
        return saldoValor;
    }

    public void setSaldoValor(BigDecimal saldoValor) {
        this.saldoValor = saldoValor;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
