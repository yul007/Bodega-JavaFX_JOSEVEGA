package com.bodega.controller;

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
import java.util.ArrayList;
import java.util.List;

public class ReporteController {

    @FXML
    private ComboBox<String> comboTipoReporte;

    @FXML
    private ComboBox<Producto> comboProducto;

    @FXML
    private ComboBox<Proveedor> comboProveedor;

    @FXML
    private DatePicker dateDesde;

    @FXML
    private DatePicker dateHasta;

    @FXML
    public void initialize() {
        comboTipoReporte.setItems(FXCollections.observableArrayList(
                "Kardex por Producto",
                "Notas de Salida por Periodo",
                "Compras por Proveedor",
                "Productos con Stock Bajo",
                "Valor Actual de Inventario"
        ));
    }

    @FXML
    public void onExportarCSV() {
        generarReporte("csv");
    }

    @FXML
    public void onExportarTXT() {
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
                    ReporteService.generarReporteKardexPorProducto(producto, obtenerDatosDummy());
                    break;
                case "Notas de Salida por Periodo":
                    LocalDate desde = dateDesde.getValue();
                    LocalDate hasta = dateHasta.getValue();
                    if (desde == null || hasta == null) throw new IllegalArgumentException("Debe seleccionar un rango de fechas válido.");
                    ReporteService.generarReporteNotasPorPeriodo(desde, hasta, obtenerDatosDummy());
                    break;
                case "Compras por Proveedor":
                    Proveedor proveedor = comboProveedor.getValue();
                    if (proveedor == null) throw new IllegalArgumentException("Debe seleccionar un proveedor.");
                    ReporteService.generarReporteComprasPorProveedor(proveedor, obtenerDatosDummy());
                    break;
                case "Productos con Stock Bajo":
                    ReporteService.generarReporteProductosConStockBajo(obtenerDatosDummy());
                    break;
                case "Valor Actual de Inventario":
                    ReporteService.generarReporteValorInventario(new BigDecimal("12345.67"), obtenerDatosDummy());
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de reporte no reconocido.");
            }

            mostrarMensaje("Reporte generado exitosamente.", "Éxito", AlertType.INFORMATION);
        } catch (IllegalArgumentException | IOException e) {
            mostrarMensaje(e.getMessage(), "Error", AlertType.ERROR);
        }
    }

    private List<String[]> obtenerDatosDummy() {
        // Método temporal para simular datos
        List<String[]> datos = new ArrayList<>();
        datos.add(new String[]{"2026-05-01", "ENTRADA", "10", "1.50", "100", "150.00"});
        datos.add(new String[]{"2026-05-02", "SALIDA", "5", "1.50", "95", "142.50"});
        return datos;
    }

    private void mostrarMensaje(String mensaje, String titulo, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}