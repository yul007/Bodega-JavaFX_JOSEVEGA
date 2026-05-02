package com.bodega.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Representa un lote de compra con costo historico y stock disponible. */
public class Lote {
    private int idLote;
    private Producto producto;
    private Proveedor proveedor;
    private String codigoLote;
    private BigDecimal cantidad;
    private BigDecimal cantidadDisponible;
    private BigDecimal costoUnitario;
    private LocalDate fechaIngreso;
    private String facturaReferencia;
    private boolean activo;

    public Lote() {
        this.cantidad = BigDecimal.ZERO;
        this.cantidadDisponible = BigDecimal.ZERO;
        this.costoUnitario = BigDecimal.ZERO;
        this.activo = true;
    }

    public Lote(int idLote, Producto producto, Proveedor proveedor, String codigoLote,
            BigDecimal cantidad, BigDecimal cantidadDisponible, BigDecimal costoUnitario,
            LocalDate fechaIngreso, String facturaReferencia, boolean activo) {
        this.idLote = idLote;
        this.producto = producto;
        this.proveedor = proveedor;
        this.codigoLote = codigoLote;
        this.cantidad = cantidad;
        this.cantidadDisponible = cantidadDisponible;
        this.costoUnitario = costoUnitario;
        this.fechaIngreso = fechaIngreso;
        this.facturaReferencia = facturaReferencia;
        this.activo = activo;
    }

    public int getIdLote() { return idLote; }
    public void setIdLote(int idLote) { this.idLote = idLote; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }
    public String getCodigoLote() { return codigoLote; }
    public void setCodigoLote(String codigoLote) { this.codigoLote = codigoLote; }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
    public BigDecimal getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(BigDecimal cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }
    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public String getFacturaReferencia() { return facturaReferencia; }
    public void setFacturaReferencia(String facturaReferencia) { this.facturaReferencia = facturaReferencia; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() { return codigoLote; }
}