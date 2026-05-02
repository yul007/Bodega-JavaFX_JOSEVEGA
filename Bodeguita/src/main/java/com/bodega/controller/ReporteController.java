package com.bodega.controller;

import com.bodega.dao.KardexDAO;
import com.bodega.dao.LoteDAO;
import com.bodega.dao.NotaSalidaDAO;
import com.bodega.dao.ProductoDAO;
import com.bodega.dao.ProveedorDAO;
import com.bodega.model.Producto;
import com.bodega.model.Proveedor;
import com.bodega.service.ReporteService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReporteController {

    @FXML private ComboBox<String> comboTipoReporte;
    @FXML private ComboBox<Producto> comboProducto;
    @FXML private ComboBox<Proveedor> comboProveedor;
    @FXML private DatePicker dateDesde;
    @FXML private DatePicker dateHasta;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final KardexDAO kardexDAO = new KardexDAO();
    private final LoteDAO loteDAO = new LoteDAO();
    private final NotaSalidaDAO notaSalidaDAO = new NotaSalidaDAO();

    @FXML public void initialize() {
        comboTipoReporte.setItems(FXCollections.observableArrayList(
                "Kardex por Producto",
                "Notas de Salida por Periodo",
                "Compras por Proveedor",
                "Productos con Stock Bajo"//,
                //"Valor Actual de Inventario"
        ));

        cargarFiltros();
    }

    @FXML public void onExportarCSV() {
        generarReporte("csv");
    }

    @FXML public void onExportarTXT() {
        generarReporte("txt");
    }

    private void generarReporte(String formato) {
        String tipo = comboTipoReporte.getValue();
        try {
            if (tipo == null) {
                throw new IllegalArgumentException("Debe seleccionar un tipo de reporte.");
            }

            switch (tipo) {
                case "Kardex por Producto":
                    Producto producto = comboProducto.getValue();
                    if (producto == null) throw new IllegalArgumentException("Debe seleccionar un producto.");
                    ReporteService.generarReporteKardexPorProducto(producto, construirKardex(producto.getIdProducto()));
                    break;
                case "Notas de Salida por Periodo":
                    LocalDate desde = dateDesde.getValue();
                    LocalDate hasta = dateHasta.getValue();
                    if (desde == null || hasta == null) throw new IllegalArgumentException("Debe seleccionar un rango de fechas válido.");
                    if (hasta.isBefore(desde)) throw new IllegalArgumentException("La fecha hasta no puede ser menor que la fecha desde.");
                    ReporteService.generarReporteNotasPorPeriodo(desde, hasta, construirNotasPorPeriodo(desde, hasta));
                    break;
                case "Compras por Proveedor":
                    Proveedor proveedor = comboProveedor.getValue();
                    if (proveedor == null) throw new IllegalArgumentException("Debe seleccionar un proveedor.");
                    ReporteService.generarReporteComprasPorProveedor(proveedor, construirComprasProveedor(proveedor.getIdProveedor()));
                    break;
                case "Productos con Stock Bajo":
                    ReporteService.generarReporteProductosConStockBajo(construirStockBajo());
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de reporte no reconocido.");
            }

            mostrarMensaje("Reporte generado exitosamente.", "Éxito", AlertType.INFORMATION);
        } catch (IllegalArgumentException | IOException e) {
            mostrarMensaje(e.getMessage(), "Error", AlertType.ERROR);
        }
    }

    private void cargarFiltros() {
        try {
            comboProducto.setItems(FXCollections.observableArrayList(productoDAO.listarActivos()));
            comboProveedor.setItems(FXCollections.observableArrayList(proveedorDAO.listarActivos()));
        } catch (Exception e) {
            mostrarMensaje("No se pudieron cargar los filtros: " + e.getMessage(), "Error", AlertType.ERROR);
        }
    }

    private List<String[]> construirKardex(int idProducto) throws IOException {
        try {
                    return kardexDAO.listarPorProducto(idProducto).stream()
                    .map(movimiento -> new String[] {
                            String.valueOf(movimiento.getFecha()),
                            movimiento.getTipo(),
                            movimiento.getProducto().getNombre(),
                            valorTexto(positivoO(movimiento.getCantidadEntrada(), movimiento.getCantidadSalida())),
                            valorTexto(positivoO(movimiento.getCostoUnitarioEntrada(), movimiento.getCostoUnitarioSalida())),
                            valorTexto(movimiento.getSaldoCantidad()),
                            valorTexto(movimiento.getSaldoValor()),
                            movimiento.getReferencia()
                    })
                    .toList();
        } catch (Exception e) {
            throw new IOException("No se pudo construir el Kardex: " + e.getMessage(), e);
        }
    }

    private List<String[]> construirNotasPorPeriodo(LocalDate desde, LocalDate hasta) throws IOException {
        try {
            return notaSalidaDAO.listarPorPeriodo(desde, hasta).stream()
                    .map(nota -> new String[] {
                            String.valueOf(nota.getIdSalida()),
                            nota.getCliente() == null ? "" : nota.getCliente().getNombre(),
                            String.valueOf(nota.getFechaEmision()),
                            valorTexto(nota.getSubtotal()),
                            valorTexto(nota.getIva()),
                            valorTexto(nota.getTotal())
                    })
                    .toList();
        } catch (Exception e) {
            throw new IOException("No se pudieron construir las notas del periodo: " + e.getMessage(), e);
        }
    }

    private List<String[]> construirComprasProveedor(int idProveedor) throws IOException {
        try {
            return loteDAO.listarPorProveedor(idProveedor).stream()
                    .map(lote -> new String[] {
                            String.valueOf(lote.getFechaIngreso()),
                            lote.getProducto().getNombre(),
                            valorTexto(lote.getCantidad()),
                            valorTexto(lote.getCostoUnitario())
                    })
                    .toList();
        } catch (Exception e) {
            throw new IOException("No se pudieron construir las compras del proveedor: " + e.getMessage(), e);
        }
    }

    private List<String[]> construirStockBajo() throws IOException {
        try {
            return productoDAO.listarStockBajo().stream()
                    .map(producto -> new String[] {
                            producto.getNombre(),
                            valorTexto(producto.getStockActual()),
                            valorTexto(producto.getStockMinimo())
                    })
                    .toList();
        } catch (Exception e) {
            throw new IOException("No se pudo construir el reporte de stock bajo: " + e.getMessage(), e);
        }
    }

    private String valorTexto(BigDecimal valor) {
        return valor == null ? "0.00" : valor.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private BigDecimal positivoO(BigDecimal primero, BigDecimal segundo) {
        if (primero != null && primero.compareTo(BigDecimal.ZERO) > 0) {
            return primero;
        }
        return segundo == null ? BigDecimal.ZERO : segundo;
    }

    private void mostrarMensaje(String mensaje, String titulo, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
