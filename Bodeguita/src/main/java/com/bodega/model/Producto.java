package com.bodega.model;

import java.math.BigDecimal;

/** Representa un producto del catalogo y su stock actual. */
public class Producto {

    private int idProducto;
    private Categoria categoria;
    private String sku;
    private String codigoBarras;
    private String nombre;
    private String descripcion;
    private String unidadMedida;
    private BigDecimal stockMinimo;
    private BigDecimal stockActual;
    private BigDecimal precioVenta;
    private boolean activo;

    public Producto() {
        this.stockMinimo = BigDecimal.ZERO;
        this.stockActual = BigDecimal.ZERO;
        this.precioVenta = BigDecimal.ZERO;
        this.unidadMedida = "unidad";
        this.activo = true;
    }

    public Producto(int idProducto, Categoria categoria, String sku, String codigoBarras,
            String nombre, String descripcion, String unidadMedida, BigDecimal stockMinimo,
            BigDecimal stockActual, BigDecimal precioVenta, boolean activo) {
        this.idProducto = idProducto;
        this.categoria = categoria;
        this.sku = sku;
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.unidadMedida = unidadMedida;
        this.stockMinimo = stockMinimo;
        this.stockActual = stockActual;
        this.precioVenta = precioVenta;
        this.activo = activo;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public BigDecimal getStockActual() {
        return stockActual;
    }

    public void setStockActual(BigDecimal stockActual) {
        this.stockActual = stockActual;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
