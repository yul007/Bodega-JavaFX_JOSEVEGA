package com.bodega.controller;

import com.bodega.model.Producto;
import com.bodega.model.Cliente;
import com.bodega.model.DetalleSalida;
import com.bodega.model.NotaSalida;
import com.bodega.model.ResultadoFIFO;
import com.bodega.service.FIFOInventoryService;
import com.bodega.dao.ProductoDAO;
import com.bodega.dao.ClienteDAO;
import com.bodega.dao.NotaSalidaDAO;
import com.bodega.db.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.SQLException;

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
    
    private ProductoDAO productoDAO;
    private ClienteDAO clienteDAO;
    private NotaSalidaDAO notaSalidaDAO;
    private FIFOInventoryService fifoService;

    @FXML
    public void initialize() {
        productoDAO = new ProductoDAO();
        clienteDAO = new ClienteDAO();
        notaSalidaDAO = new NotaSalidaDAO();
        fifoService = new FIFOInventoryService();
        
        detalles = FXCollections.observableArrayList();
        productos = FXCollections.observableArrayList();
        clientes = FXCollections.observableArrayList();

        colProducto.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProducto().getNombre()));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        detalleTable.setItems(detalles);
        
        cargarProductos();
        cargarClientes();
        
        dateFecha.setValue(LocalDate.now());
        
        actualizarTotales();
    }
    
    private void cargarProductos() {
        try {
            productos.setAll(productoDAO.listarActivos());
            comboProducto.setItems(productos);
        } catch (SQLException e) {
            mostrarMensaje("Error al cargar productos: " + e.getMessage(), "Error", Alert.AlertType.ERROR);
        }
    }
    
    private void cargarClientes() {
        try {
            clientes.setAll(clienteDAO.listarActivos());
            comboCliente.setItems(clientes);
        } catch (SQLException e) {
            mostrarMensaje("Error al cargar clientes: " + e.getMessage(), "Error", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onAgregarDetalle() {
        try {
            Producto producto = comboProducto.getValue();
            BigDecimal cantidad = new BigDecimal(txtCantidad.getText());
            BigDecimal precioUnitario = new BigDecimal(txtPrecioUnitario.getText());
            BigDecimal subtotal = precioUnitario.multiply(cantidad).setScale(2, RoundingMode.HALF_UP);

            if (producto == null || cantidad.compareTo(BigDecimal.ZERO) <= 0 || 
                precioUnitario.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Todos los campos deben completarse con valores válidos.");
            }
            
            if (producto.getStockActual().compareTo(cantidad) < 0) {
                throw new IllegalArgumentException("Stock insuficiente. Stock actual: " + 
                    producto.getStockActual() + ", solicitado: " + cantidad);
            }

            DetalleSalida detalle = new DetalleSalida();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);
            
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
    public void onEliminarDetalle() {
        DetalleSalida selected = detalleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarMensaje("Seleccione un detalle para eliminar.", "Información", Alert.AlertType.INFORMATION);
            return;
        }
        
        detalles.remove(selected);
        actualizarTotales();
    }

    @FXML
    public void onGenerarFactura() {
        Connection connection = null;
        try {
            Cliente cliente = comboCliente.getValue();
            LocalDate fecha = dateFecha.getValue();
            String numeroFactura = txtNumeroFactura.getText();

            if (cliente == null || fecha == null || numeroFactura.trim().isEmpty() || detalles.isEmpty()) {
                throw new IllegalArgumentException("Todos los campos obligatorios deben completarse correctamente.");
            }
            
            // Obtener el texto de los labels correctamente
            String subtotalText = lblSubtotal.getText().replace("Subtotal: $", "");
            String ivaText = lblIva.getText().replace("IVA (12%): $", "");
            String totalText = lblTotal.getText().replace("Total: $", "");
            
            BigDecimal subtotal = new BigDecimal(subtotalText);
            BigDecimal iva = new BigDecimal(ivaText);
            BigDecimal total = new BigDecimal(totalText);
            
            // Aplicar FIFO para cada detalle y calcular costos
            BigDecimal costoTotalGeneral = BigDecimal.ZERO;
            BigDecimal utilidadTotal = BigDecimal.ZERO;
            
            for (DetalleSalida detalle : detalles) {
                ResultadoFIFO resultado = fifoService.aplicarSalidaFIFO(
                    detalle.getProducto().getIdProducto(), 
                    detalle.getCantidad()
                );
                
                detalle.setCostoTotalFifo(resultado.getCostoTotal());
                detalle.setCostoUnitarioFifo(
                    resultado.getCostoTotal().divide(detalle.getCantidad(), 2, RoundingMode.HALF_UP)
                );
                detalle.setUtilidad(
                    detalle.getSubtotal().subtract(resultado.getCostoTotal())
                );
                
                costoTotalGeneral = costoTotalGeneral.add(resultado.getCostoTotal());
                utilidadTotal = utilidadTotal.add(detalle.getUtilidad());
                detalle.getProducto().setStockActual(resultado.getStockNuevo());
            }
            
            // Crear la nota de salida (sin ID aún)
            NotaSalida nuevaNotaSalida = new NotaSalida(
                0,                          // idSalida temporal
                cliente,
                numeroFactura,
                fecha,
                subtotal,
                iva,
                total,
                costoTotalGeneral,
                utilidadTotal,
                "completada",
                ""
            );
            
            // Obtener una conexión manual para manejar la transacción
            connection = DatabaseConnection.getInstance().getConnection();
            connection.setAutoCommit(false);
            
            // Guardar la cabecera de la nota
            int idSalida = notaSalidaDAO.crear(connection, nuevaNotaSalida);
            nuevaNotaSalida.setIdSalida(idSalida);
            
            // Guardar cada detalle usando la misma conexión
            for (DetalleSalida detalle : detalles) {
                detalle.setNotaSalida(nuevaNotaSalida);
                notaSalidaDAO.crearDetalle(connection, detalle);
            }
            
            // Confirmar la transacción
            connection.commit();
            
            limpiarFormularioCabecera();
            detalles.clear();
            actualizarTotales();

            mostrarMensaje("Factura generada exitosamente.\nNúmero: " + numeroFactura, 
                "Éxito", Alert.AlertType.INFORMATION);

        } catch (IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), "Error", Alert.AlertType.ERROR);
            if (connection != null) try { connection.rollback(); } catch (SQLException ignored) {}
        } catch (SQLException e) {
            mostrarMensaje("Error al guardar la factura: " + e.getMessage(), "Error", Alert.AlertType.ERROR);
            if (connection != null) try { connection.rollback(); } catch (SQLException ignored) {}
        } catch (Exception e) {
            mostrarMensaje("Error al procesar inventario FIFO: " + e.getMessage(), "Error", Alert.AlertType.ERROR);
            if (connection != null) try { connection.rollback(); } catch (SQLException ignored) {}
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    private void actualizarTotales() {
        BigDecimal subtotal = detalles.stream()
            .map(DetalleSalida::getSubtotal)
            .filter(s -> s != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal iva = subtotal.multiply(BigDecimal.valueOf(0.12)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(iva).setScale(2, RoundingMode.HALF_UP);

        lblSubtotal.setText("Subtotal: $" + subtotal.setScale(2, RoundingMode.HALF_UP));
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
        dateFecha.setValue(LocalDate.now());
        txtNumeroFactura.clear();
    }

    private void mostrarMensaje(String mensaje, String titulo, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
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