package com.bodega.controller;

import com.bodega.model.Lote;
import com.bodega.model.Producto;
import com.bodega.model.Proveedor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoteController {

    @FXML
    private TableView<Lote> loteTable;

    @FXML
    private TableColumn<Lote, String> colProducto;

    @FXML
    private TableColumn<Lote, String> colProveedor;

    @FXML
    private TableColumn<Lote, Integer> colCantidad;

    @FXML
    private TableColumn<Lote, BigDecimal> colCostoUnitario;

    @FXML
    private TableColumn<Lote, LocalDate> colFechaIngreso;

    @FXML
    private ComboBox<Producto> comboProducto;

    @FXML
    private ComboBox<Proveedor> comboProveedor;

    @FXML
    private TextField txtCantidad;

    @FXML
    private TextField txtCostoUnitario;

    @FXML
    private DatePicker dateFechaIngreso;

    @FXML
    private DatePicker dateFechaVencimiento;

    @FXML
    private TextField txtFacturaReferencia;

    private ObservableList<Lote> lotes;
    private ObservableList<Producto> productos;
    private ObservableList<Proveedor> proveedores;

    @FXML
    public void initialize() {
        lotes = FXCollections.observableArrayList();
        productos = FXCollections.observableArrayList(); // Load from ProductoDAO
        proveedores = FXCollections.observableArrayList(); // Load from ProveedorDAO

        colProducto.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedorNombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCostoUnitario.setCellValueFactory(new PropertyValueFactory<>("costoUnitario"));
        colFechaIngreso.setCellValueFactory(new PropertyValueFactory<>("fechaIngreso"));

        loteTable.setItems(lotes);
        comboProducto.setItems(productos);
        comboProveedor.setItems(proveedores);
    }

    @FXML
    public void onGuardar() {
        try {
            Producto producto = comboProducto.getValue();
            Proveedor proveedor = comboProveedor.getValue();
            int cantidad = Integer.parseInt(txtCantidad.getText());
            BigDecimal costoUnitario = new BigDecimal(txtCostoUnitario.getText());
            LocalDate fechaIngreso = dateFechaIngreso.getValue();
            LocalDate fechaVencimiento = dateFechaVencimiento.getValue();
            String facturaReferencia = txtFacturaReferencia.getText();

            if (producto == null || proveedor == null || cantidad <= 0 || costoUnitario.compareTo(BigDecimal.ZERO) <= 0 || fechaIngreso == null) {
                throw new IllegalArgumentException("Todos los campos obligatorios deben completarse correctamente.");
            }

            Lote nuevoLote = new Lote(producto, proveedor, cantidad, costoUnitario, fechaIngreso, fechaVencimiento, facturaReferencia);
            // Simulate saving to DB
            lotes.add(nuevoLote);

            // Update stock and Kardex
            actualizarStockKardex(producto, cantidad, costoUnitario);

            limpiarFormulario();
            mostrarMensaje("Lote guardado exitosamente.", "Éxito", Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            mostrarMensaje("Cantidad y costo unitario deben ser valores numéricos válidos.", "Error", Alert.AlertType.ERROR);
        } catch (IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), "Error", Alert.AlertType.ERROR);
        }
    }

    private void actualizarStockKardex(Producto producto, int cantidad, BigDecimal costoUnitario) {
        // Logic to update stock and register in Kardex
        // Example: productoDAO.updateStock(producto, cantidad);
        // kardexService.registrarEntrada(producto, cantidad, costoUnitario);
    }

    private void limpiarFormulario() {
        comboProducto.setValue(null);
        comboProveedor.setValue(null);
        txtCantidad.clear();
        txtCostoUnitario.clear();
        dateFechaIngreso.setValue(null);
        dateFechaVencimiento.setValue(null);
        txtFacturaReferencia.clear();
    }

    private void mostrarMensaje(String mensaje, String titulo, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void onCancelar() {
        limpiarFormulario();
    }

    @FXML
    public void onTableClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Lote selectedLote = loteTable.getSelectionModel().getSelectedItem();
            if (selectedLote != null) {
                cargarDatosLote(selectedLote);
            }
        }
    }

    private void cargarDatosLote(Lote lote) {
        comboProducto.setValue(lote.getProducto());
        comboProveedor.setValue(lote.getProveedor());
        txtCantidad.setText(String.valueOf(lote.getCantidad()));
        txtCostoUnitario.setText(String.valueOf(lote.getCostoUnitario()));
        dateFechaIngreso.setValue(lote.getFechaIngreso());
        dateFechaVencimiento.setValue(lote.getFechaVencimiento());
        txtFacturaReferencia.setText(lote.getFacturaReferencia());
    }
}