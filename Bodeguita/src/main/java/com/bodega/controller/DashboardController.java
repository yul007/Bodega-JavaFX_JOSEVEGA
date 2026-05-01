package com.bodega.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardController {

    @FXML
    private Label lblValorInventario;

    @FXML
    private TableView<Map<String, Object>> tablaStockBajo;

    @FXML
    private TableColumn<Map<String, Object>, String> colProductoStock;

    @FXML
    private TableColumn<Map<String, Object>, Integer> colStockActual;

    @FXML
    private TableColumn<Map<String, Object>, Integer> colStockMinimo;

    @FXML
    private PieChart chartTopVendidos;

    @FXML
    private LineChart<String, Number> chartVentas30Dias;

    private ObservableList<Map<String, Object>> stockBajoData;

    @FXML
    public void initialize() {
        // Initialize columns for the Stock Bajo table
        colProductoStock.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colStockActual.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colStockMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        stockBajoData = FXCollections.observableArrayList();
        tablaStockBajo.setItems(stockBajoData);

        actualizarDatos();
    }

    @FXML
    public void onActualizar() {
        actualizarDatos();
    }

    @FXML
    public void onExportarReporte() {
        // Export complete dashboard data to a report (CSV/TXT)
        // Connect with ReporteService for full report export
    }

    private void actualizarDatos() {
        actualizarValorInventario();
        actualizarStockBajo();
        actualizarTopVendidos();
        actualizarVentas30Dias();
    }

    private void actualizarValorInventario() {
        // Simulate value, replace with actual calculation
        BigDecimal valorInventario = new BigDecimal("150000.00");
        lblValorInventario.setText("Valor Total de Inventario: $" + valorInventario);
    }

    private void actualizarStockBajo() {
        // Replace with actual database fetching logic
        stockBajoData.setAll(
            List.of(
                Map.of("producto", "Producto A", "stockActual", 3, "stockMinimo", 10),
                Map.of("producto", "Producto B", "stockActual", 2, "stockMinimo", 5),
                Map.of("producto", "Producto C", "stockActual", 7, "stockMinimo", 8)
            )
        );
    }

    private void actualizarTopVendidos() {
        // Replace with actual data fetching logic, simulate for now
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Producto A", 300),
            new PieChart.Data("Producto B", 250),
            new PieChart.Data("Producto C", 200),
            new PieChart.Data("Producto D", 150),
            new PieChart.Data("Producto E", 100)
        );
        chartTopVendidos.setData(pieChartData);
    }

    private void actualizarVentas30Dias() {
        // Replace with actual data, simulate LineChart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventas Últimos 30 Días");

        series.getData().add(new XYChart.Data<>("1", 1000));
        series.getData().add(new XYChart.Data<>("2", 2000));
        series.getData().add(new XYChart.Data<>("3", 1500));
        // Add more data points here...

        chartVentas30Dias.getData().clear();
        chartVentas30Dias.getData().add(series);
    }
}