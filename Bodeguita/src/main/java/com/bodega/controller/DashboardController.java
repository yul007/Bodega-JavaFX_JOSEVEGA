package com.bodega.controller;

import com.bodega.dao.KardexDAO;
import com.bodega.dao.NotaSalidaDAO;
import com.bodega.dao.ProductoDAO;
import com.bodega.service.ReporteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final KardexDAO kardexDAO = new KardexDAO();
    private final NotaSalidaDAO notaSalidaDAO = new NotaSalidaDAO();

    @FXML
    public void initialize() {
        colProductoStock.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("producto"))));
        colStockActual.setCellValueFactory(data ->
                new SimpleIntegerProperty((Integer) data.getValue().get("stockActual")).asObject());
        colStockMinimo.setCellValueFactory(data ->
                new SimpleIntegerProperty((Integer) data.getValue().get("stockMinimo")).asObject());

        stockBajoData = FXCollections.observableArrayList();
        tablaStockBajo.setItems(stockBajoData);

        actualizarDatos();
    }

    @FXML
    public void onActualizar() {
        actualizarDatos();
    }


    private void actualizarValorInventario() {
        try {
            BigDecimal valorInventario = kardexDAO.calcularValorInventarioActual();
            lblValorInventario.setText("Valor Total de Inventario: $" + valorInventario.setScale(2, java.math.RoundingMode.HALF_UP));
        } catch (SQLException e) {
            lblValorInventario.setText("Valor Total de Inventario: no disponible");
        }
    }

    private void actualizarStockBajo() {
        try {
            stockBajoData.setAll(
                    productoDAO.listarStockBajo().stream()
                            .map(producto -> Map.<String, Object>of(
                                    "producto", producto.getNombre(),
                                    "stockActual", valorEntero(producto.getStockActual()),
                                    "stockMinimo", valorEntero(producto.getStockMinimo())))
                            .toList());
        } catch (SQLException e) {
            stockBajoData.clear();
        }
    }

    private void actualizarTopVendidos() {
        try {
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            for (Object[] fila : notaSalidaDAO.listarTopProductosVendidos(5)) {
                pieChartData.add(new PieChart.Data(
                        String.valueOf(fila[0]),
                        ((BigDecimal) fila[1]).doubleValue()));
            }
            chartTopVendidos.setData(pieChartData);
        } catch (SQLException e) {
            chartTopVendidos.setData(FXCollections.observableArrayList());
        }
    }

    private void actualizarVentas30Dias() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventas Últimos 30 Días");
        try {
            for (Object[] fila : notaSalidaDAO.listarVentasPorDiaUltimosDias(30)) {
                LocalDate fecha = (LocalDate) fila[0];
                BigDecimal total = (BigDecimal) fila[1];
                series.getData().add(new XYChart.Data<>(fecha.format(DateTimeFormatter.ISO_LOCAL_DATE), total));
            }
            chartVentas30Dias.getData().clear();
            chartVentas30Dias.getData().add(series);
        } catch (SQLException e) {
            chartVentas30Dias.getData().clear();
        }
    }

    private void actualizarDatos() {
        actualizarValorInventario();
        actualizarStockBajo();
        actualizarTopVendidos();
        actualizarVentas30Dias();
    }

    private int valorEntero(BigDecimal valor) {
        return valor == null ? 0 : valor.intValue();
    }
}
