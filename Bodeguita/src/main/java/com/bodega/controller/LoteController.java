package com.bodega.controller;

import com.bodega.dao.LoteDAO;
import com.bodega.dao.ProductoDAO;
import com.bodega.dao.ProveedorDAO;
import com.bodega.model.Lote;
import com.bodega.model.Producto;
import com.bodega.model.Proveedor;
import com.bodega.service.LoteCompraService;
import com.bodega.util.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoteController {

    @FXML private TableView<Lote> loteTable;
    @FXML private TableColumn<Lote, String> colProducto;
    @FXML private TableColumn<Lote, String> colProveedor;
    @FXML private TableColumn<Lote, Integer> colCantidad;
    @FXML private TableColumn<Lote, BigDecimal> colCostoUnitario;
    @FXML private TableColumn<Lote, LocalDate> colFechaIngreso;
    @FXML private ComboBox<Producto> comboProducto;
    @FXML private ComboBox<Proveedor> comboProveedor;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtCostoUnitario;
    @FXML private DatePicker dateFechaIngreso;
    @FXML private TextField txtFacturaReferencia;

    private ObservableList<Lote> lotes;
    private ObservableList<Producto> productos;
    private ObservableList<Proveedor> proveedores;
    private final LoteDAO loteDAO = new LoteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final LoteCompraService loteCompraService = new LoteCompraService();

    @FXML public void initialize() {
        lotes = FXCollections.observableArrayList();
        productos = FXCollections.observableArrayList();
        proveedores = FXCollections.observableArrayList();

        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedor"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCostoUnitario.setCellValueFactory(new PropertyValueFactory<>("costoUnitario"));
        colFechaIngreso.setCellValueFactory(new PropertyValueFactory<>("fechaIngreso"));

        loteTable.setItems(lotes);
        comboProducto.setItems(productos);
        comboProveedor.setItems(proveedores);
        cargarDatosIniciales();
    }

    @FXML public void onGuardar() {
        try {
            Producto producto = comboProducto.getValue();
            Proveedor proveedor = comboProveedor.getValue();
            BigDecimal cantidad = parseBigDecimal(txtCantidad.getText(), "cantidad");
            BigDecimal costoUnitario = parseBigDecimal(txtCostoUnitario.getText(), "costo unitario");
            LocalDate fechaIngreso = dateFechaIngreso.getValue();
            String facturaReferencia = ValidationUtils.opcional(txtFacturaReferencia.getText());

            if (producto == null) {
                throw new IllegalArgumentException("Debe seleccionar un producto.");
            }
            if (proveedor == null) {
                throw new IllegalArgumentException("Debe seleccionar un proveedor.");
            }
            ValidationUtils.requeridoPositivo(cantidad, "cantidad");
            ValidationUtils.requeridoPositivo(costoUnitario, "costo unitario");
            if (fechaIngreso == null) {
                throw new IllegalArgumentException("La fecha de ingreso es obligatoria.");
            }
            validarNombreProducto(producto);
            validarProveedor(proveedor);

            String codigoLote = "LOT-" + System.currentTimeMillis(); // Código temporal
            BigDecimal cantidadDisponible = cantidad;

            Lote nuevoLote = new Lote(
                0, // idLote (0 para nuevo, la BD generará el ID)
                producto,
                proveedor,
                codigoLote,
                cantidad,
                cantidadDisponible,
                costoUnitario,
                fechaIngreso,
                facturaReferencia,
                true
            );

            Lote loteGuardado = loteCompraService.registrarLoteCompra(nuevoLote);
            lotes.add(0, loteGuardado);

            limpiarFormulario();
            mostrarMensaje("Lote guardado exitosamente. Stock actualizado correctamente.", "Éxito", Alert.AlertType.INFORMATION);
        } catch (IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), "Error", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarMensaje("No se pudo guardar el lote.\n" + mensajeAmigable(e), "Error", Alert.AlertType.ERROR);
        }
    }

    private BigDecimal parseBigDecimal(String texto, String campo) {
        try {
            BigDecimal valor = new BigDecimal(texto.trim().replace(",", "."));
            if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El campo " + campo + " debe ser mayor que cero.");
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser numerico.");
        }
    }

    private void cargarDatosIniciales() {
        try {
            productos.setAll(productoDAO.listarActivos());
            proveedores.setAll(proveedorDAO.listarActivos());
            lotes.setAll(loteDAO.listarTodos());
        } catch (Exception e) {
            mostrarMensaje("No se pudieron cargar los datos iniciales.\n" + mensajeAmigable(e), "Error", Alert.AlertType.ERROR);
        }
    }

    private void limpiarFormulario() {
        comboProducto.setValue(null);
        comboProveedor.setValue(null);
        txtCantidad.clear();
        txtCostoUnitario.clear();
        dateFechaIngreso.setValue(null);
        txtFacturaReferencia.clear();
    }

    private void mostrarMensaje(String mensaje, String titulo, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void validarNombreProducto(Producto producto) {
        if (producto == null || ValidationUtils.estaVacio(producto.getNombre())) {
            throw new IllegalArgumentException("El producto debe tener un nombre valido.");
        }
    }

    private void validarProveedor(Proveedor proveedor) {
        if (proveedor == null || proveedor.getIdProveedor() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor valido.");
        }
    }

    private String mensajeAmigable(Exception exception) {
        String mensaje = exception.getMessage() == null ? "" : exception.getMessage().toLowerCase();
        if (mensaje.contains("connect") || mensaje.contains("timeout") || mensaje.contains("communications link failure")) {
            return "No hay conexion con MySQL. Verifica el servidor y la configuracion.";
        }
        return exception.getMessage();
    }

    @FXML public void onCancelar() {
        limpiarFormulario();
    }

    @FXML public void onTableClick(MouseEvent event) {
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
        txtFacturaReferencia.setText(lote.getFacturaReferencia());
    }
}
