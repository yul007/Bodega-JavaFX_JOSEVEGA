package com.bodega.controller;

import com.bodega.model.Producto;
import com.bodega.model.Cliente;
import com.bodega.model.DetalleSalida;
import com.bodega.model.NotaSalida;
import com.bodega.service.FIFOInventoryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SalidaController {

    @FXML
    private ComboBox<Cliente> comboCliente;

    @FXML
    private DatePicker dateFecha;

    @FXML
    private TextField txtNumeroFactura;

    @FXML
    private ComboBox<Producto> comboProducto;

    @FXML
    private TextField txtCantidad;

    @FXML
    private TextField txtPrecioUnitario;

    @FXML
    private TableView<DetalleSalida> detalleTable;

    @FXML
    private TableColumn<DetalleSalida, String> colProducto;

    @FXML
    private TableColumn<DetalleSalida, Integer> colCantidad;

    @FXML
    private TableColumn<DetalleSalida, BigDecimal> colPrecioUnitario;

    @FXML
    private TableColumn<DetalleSalida, BigDecimal> colSubtotal;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblIva;

    @FXML
    private Label lblTotal;

    private ObservableList<DetalleSalida> detalles;
    private ObservableList<Producto> productos;
    private ObservableList<Cliente> clientes;

    @FXML
    public void initialize() {
        detalles = FXCollections.observableArrayList();
        productos = FXCollections.observableArrayList(); // Replace with DAO call
        clientes = FXCollections.observableArrayList(); // Replace with DAO call

        colProducto.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        detalleTable.setItems(detalles);
        comboProducto.setItems(productos);
        comboCliente.setItems(clientes);

        actualizarTotales();
    }

    @FXML
    public void onAgregarDetalle() {
        try {
            Producto producto = comboProducto.getValue();
            int cantidad = Integer.parseInt(txtCantidad.getText());
            BigDecimal precioUnitario = new BigDecimal(txtPrecioUnitario.getText());
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));

            if (producto == null || cantidad <= 0 || precioUnitario.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Todos los campos deben completarse con valores válidos.");
            }

            DetalleSalida detalle = new DetalleSalida(producto, cantidad, precioUnitario, subtotal);
            detalles.add(detalle);

            limpiarFormularioDetalle();
            actualizarTotales();

        } catch (NumberFormatException e) {
            mostrarMensaje("Cantidad y precio unitario deben ser valores numéricos válidos.", "Error", Alert.AlertType.ERROR);
        } catch (IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), "Error", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onGenerarFactura() {
        try {
            Cliente cliente = comboCliente.getValue();
            LocalDate fecha = dateFecha.getValue();
            String numeroFactura = txtNumeroFactura.getText();

            if (cliente == null || fecha == null || numeroFactura.trim().isEmpty() || detalles.isEmpty()) {
                throw new IllegalArgumentException("Todos los campos obligatorios deben completarse correctamente.");
            }

            NotaSalida nuevaNotaSalida = new NotaSalida(cliente, fecha, numeroFactura, new BigDecimal(lblSubtotal.getText().replace("Subtotal: $", "")),
                    new BigDecimal(lblIva.getText().replace("IVA (12%): $", "")), new BigDecimal(lblTotal.getText().replace("Total: $", "")));

            // Llamar servicio FIFO para aplicar reglas de inventario y guardar la nota de salida
            FIFOInventoryService.aplicarFIFO(detalles);

            // Guardar en base de datos (nota de salida y sus detalles): se debe conectar con DAO

            limpiarFormularioCabecera();
            detalles.clear();
            actualizarTotales();

            mostrarMensaje("Factura generada exitosamente.", "Éxito", Alert.AlertType.INFORMATION);

        } catch (IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), "Error", Alert.AlertType.ERROR);
        }
    }

    private void actualizarTotales() {
        BigDecimal subtotal = detalles.stream().map(DetalleSalida::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal iva = subtotal.multiply(BigDecimal.valueOf(0.12));
        BigDecimal total = subtotal.add(iva);

        lblSubtotal.setText("Subtotal: $" + subtotal);
        lblIva.setText("IVA (12%): $" + iva);
        lblTotal.setText("Total: $" + total);
    }

    private void limpiarFormularioDetalle() {
        comboProducto.setValue(null);
        txtCantidad.clear();
        txtPrecioUnitario.clear();
    }

    private void limpiarFormularioCabecera() {
        comboCliente.setValue(null);
        dateFecha.setValue(null);
        txtNumeroFactura.clear();
    }

    private void mostrarMensaje(String mensaje, String titulo, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void onCancelar() {
        limpiarFormularioCabecera();
        detalles.clear();
        actualizarTotales();
    }
}