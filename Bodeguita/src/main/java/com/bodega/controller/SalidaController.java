package com.bodega.controller;

import com.bodega.model.Producto;
import com.bodega.model.Cliente;
import com.bodega.model.DetalleSalida;
import com.bodega.model.NotaSalida;
import com.bodega.service.NotaSalidaService;
import com.bodega.dao.ProductoDAO;
import com.bodega.dao.ClienteDAO;
import com.bodega.dao.NotaSalidaDAO;
import com.bodega.util.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.sql.SQLException;

public class SalidaController {

    @FXML private ComboBox<Cliente> comboCliente;
    @FXML private DatePicker dateFecha;
    @FXML private TextField txtNumeroFactura;
    @FXML private ComboBox<Producto> comboProducto;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtPrecioUnitario;
    @FXML private TableView<DetalleSalida> detalleTable;
    @FXML private TableColumn<DetalleSalida, String> colProducto;
    @FXML private TableColumn<DetalleSalida, Integer> colCantidad;
    @FXML private TableColumn<DetalleSalida, BigDecimal> colPrecioUnitario;
    @FXML private TableColumn<DetalleSalida, BigDecimal> colSubtotal;
    @FXML private Label lblSubtotal; 
    @FXML private Label lblIva;
    @FXML private Label lblTotal;

    private ObservableList<DetalleSalida> detalles;
    private ObservableList<Producto> productos;
    private ObservableList<Cliente> clientes;
    
    private ProductoDAO productoDAO;
    private ClienteDAO clienteDAO;
    private NotaSalidaDAO notaSalidaDAO;
    private NotaSalidaService notaSalidaService;

    @FXML public void initialize() {
        productoDAO = new ProductoDAO();
        clienteDAO = new ClienteDAO();
        notaSalidaDAO = new NotaSalidaDAO();
        notaSalidaService = new NotaSalidaService();
        
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
            mostrarMensaje("No se pudieron cargar los productos.\n" + mensajeAmigableBD(e),
                    "Error", Alert.AlertType.ERROR);
        } catch (RuntimeException e) {
            mostrarMensaje("No se pudieron cargar los productos.\n" + e.getMessage(),
                    "Error", Alert.AlertType.ERROR);
        }
    }
    
    private void cargarClientes() {
        try {
            clientes.setAll(clienteDAO.listarActivos());
            comboCliente.setItems(clientes);
        } catch (SQLException e) {
            mostrarMensaje("No se pudieron cargar los clientes.\n" + mensajeAmigableBD(e),
                    "Error", Alert.AlertType.ERROR);
        } catch (RuntimeException e) {
            mostrarMensaje("No se pudieron cargar los clientes.\n" + e.getMessage(),
                    "Error", Alert.AlertType.ERROR);
        }
    }

    @FXML public void onAgregarDetalle() {
        try {
            Producto producto = comboProducto.getValue();
            BigDecimal cantidad = parsePositive(txtCantidad.getText(), "cantidad");
            BigDecimal precioUnitario = parsePositive(txtPrecioUnitario.getText(), "precio unitario");
            BigDecimal subtotal = precioUnitario.multiply(cantidad).setScale(2, RoundingMode.HALF_UP);

            if (producto == null) {
                throw new IllegalArgumentException("Debe seleccionar un producto.");
            }
            ValidationUtils.requeridoPositivo(cantidad, "cantidad");
            ValidationUtils.requeridoPositivo(precioUnitario, "precio unitario");

            if (producto.getStockActual().compareTo(cantidad) < 0) {
                throw new IllegalArgumentException("Stock insuficiente. Stock actual: "
                        + producto.getStockActual() + ", solicitado: " + cantidad);
            }

            DetalleSalida detalle = new DetalleSalida();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);
            
            detalles.add(detalle);

            limpiarFormularioDetalle();
            actualizarTotales();
        } catch (IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), "Error", Alert.AlertType.ERROR);
        } catch (RuntimeException e) {
            mostrarMensaje("No se pudo agregar el detalle.\n" + e.getMessage(),
                    "Error", Alert.AlertType.ERROR);
        }
    }
    
    @FXML public void onEliminarDetalle() {
        DetalleSalida selected = detalleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarMensaje("Seleccione un detalle para eliminar.", "Información", Alert.AlertType.INFORMATION);
            return;
        }
        
        detalles.remove(selected);
        actualizarTotales();
    }

    @FXML public void onGenerarFactura() {
        try {
            Cliente cliente = comboCliente.getValue();
            LocalDate fecha = dateFecha.getValue();
            String numeroFactura = ValidationUtils.requerido(txtNumeroFactura.getText(), "numero de factura");

            if (cliente == null) {
                throw new IllegalArgumentException("Debe seleccionar un cliente antes de generar la factura.");
            }
            if (fecha == null) {
                throw new IllegalArgumentException("La fecha de la venta es obligatoria.");
            }
            if (detalles.isEmpty()) {
                throw new IllegalArgumentException("Debe agregar al menos un detalle antes de generar la factura.");
            }
            if (notaSalidaDAO.buscarPorNumeroFactura(numeroFactura).isPresent()) {
                throw new IllegalArgumentException("Ya existe una factura con ese numero.");
            }
            
            // Obtener el texto de los labels correctamente
            String subtotalText = lblSubtotal.getText().replace("Subtotal: $", "");
            String ivaText = lblIva.getText().replace("IVA (12%): $", "");
            String totalText = lblTotal.getText().replace("Total: $", "");
            
            BigDecimal subtotal = new BigDecimal(subtotalText);
            BigDecimal iva = new BigDecimal(ivaText);
            BigDecimal total = new BigDecimal(totalText);

            NotaSalida nuevaNotaSalida = new NotaSalida(
                0,
                cliente,
                numeroFactura,
                fecha,
                subtotal,
                iva,
                total,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "completada",
                ""
            );

            NotaSalida notaGuardada = notaSalidaService.crearNotaSalida(nuevaNotaSalida, detalles);

            limpiarFormularioCabecera();
            detalles.clear();
            actualizarTotales();

            mostrarMensaje("Factura generada exitosamente.\nNúmero: " + notaGuardada.getNumeroFactura(), 
                "Éxito", Alert.AlertType.INFORMATION);

        } catch (IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), "Error", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            mostrarMensaje("No se pudo guardar la factura.\n" + mensajeAmigableBD(e),
                    "Error", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarMensaje("No se pudo procesar la venta.\n" + e.getMessage(), "Error", Alert.AlertType.ERROR);
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

    private String mensajeAmigableBD(SQLException exception) {
        String causa = exception.getMessage() == null ? "" : exception.getMessage().toLowerCase();
        if (causa.contains("connect") || causa.contains("timeout") || causa.contains("communications link failure")) {
            return "No hay conexion con MySQL. Verifica que el servidor este encendido y accesible.";
        }
        return exception.getMessage();
    }

    private BigDecimal parsePositive(String texto, String campo) {
        try {
            return ValidationUtils.requeridoPositivo(new BigDecimal(texto.trim().replace(",", ".")), campo);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser numerico.");
        }
    }

    @FXML public void onCancelar() {
        limpiarFormularioCabecera();
        detalles.clear();
        actualizarTotales();
    }
}
