package com.bodega.controller;

import com.bodega.dao.CategoriaDAO;
import com.bodega.model.Categoria;
import com.bodega.model.Producto;
import com.bodega.util.ValidationUtils;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/** Controlador del formulario modal de productos. */
public class ProductoFormController {

    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private TextField txtSku;
    @FXML private TextField txtCodigoBarras;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtStockMinimo;
    @FXML private TextField txtStockActual;
    @FXML private TextField txtPrecioVenta;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private Stage stage;
    private Producto productoOriginal;
    private Producto resultado;

    @FXML
    private void initialize() {
        cargarCategorias();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setProducto(Producto producto) {
        this.productoOriginal = producto;
        if (producto == null) {
            limpiarCampos();
            return;
        }

        seleccionarCategoria(producto.getCategoria());
        txtSku.setText(valorSeguro(producto.getSku()));
        txtCodigoBarras.setText(valorSeguro(producto.getCodigoBarras()));
        txtNombre.setText(valorSeguro(producto.getNombre()));
        txtDescripcion.setText(valorSeguro(producto.getDescripcion()));
        txtStockMinimo.setText(valorSeguro(producto.getStockMinimo()).toPlainString());
        txtStockActual.setText(valorSeguro(producto.getStockActual()).toPlainString());
        txtPrecioVenta.setText(valorSeguro(producto.getPrecioVenta()).toPlainString());
    }

    public Optional<Producto> getResultado() {
        return Optional.ofNullable(resultado);
    }

    @FXML
    private void onGuardar() {
        try {
            Categoria categoria = cmbCategoria.getValue();
            String sku = ValidationUtils.requerido(txtSku.getText(), "SKU");
            String nombre = ValidationUtils.requerido(txtNombre.getText(), "nombre");
            String codigoBarras = ValidationUtils.opcional(txtCodigoBarras.getText());
            String descripcion = ValidationUtils.opcional(txtDescripcion.getText());
            BigDecimal stockMinimo = parseBigDecimal(txtStockMinimo.getText(), "stock minimo");
            BigDecimal stockActual = parseBigDecimal(txtStockActual.getText(), "stock actual");
            BigDecimal precioVenta = parseBigDecimal(txtPrecioVenta.getText(), "precio venta");

            validarProducto(categoria, stockMinimo, stockActual, precioVenta);

            Producto producto = productoOriginal == null
                    ? new Producto()
                    : copiarProducto(productoOriginal);

            producto.setCategoria(categoria);
            producto.setSku(sku);
            producto.setCodigoBarras(codigoBarras);
            producto.setNombre(nombre);
            producto.setDescripcion(descripcion);
            producto.setStockMinimo(stockMinimo);
            producto.setStockActual(stockActual);
            producto.setPrecioVenta(precioVenta);
            producto.setActivo(true);

            resultado = producto;
            cerrarVentana();
        } catch (IllegalArgumentException exception) {
            mostrarError("Datos inválidos", exception.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        resultado = null;
        cerrarVentana();
    }

    private void cargarCategorias() {
        try {
            cmbCategoria.setItems(FXCollections.observableArrayList(categoriaDAO.listarActivas()));
        } catch (SQLException exception) {
            mostrarError("No se pudo cargar categorias", exception.getMessage());
        }
    }

    private void seleccionarCategoria(Categoria categoria) {
        if (categoria == null) {
            return;
        }
        for (Categoria item : cmbCategoria.getItems()) {
            if (item.getIdCategoria() == categoria.getIdCategoria()) {
                cmbCategoria.getSelectionModel().select(item);
                return;
            }
        }
    }

    private void validarProducto(Categoria categoria, BigDecimal stockMinimo, BigDecimal stockActual, BigDecimal precioVenta) {
        if (categoria == null) {
            throw new IllegalArgumentException("Debe seleccionar una categoria.");
        }
        if (stockMinimo.compareTo(BigDecimal.ZERO) < 0
                || stockActual.compareTo(BigDecimal.ZERO) < 0
                || precioVenta.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Stock y precio no pueden ser negativos.");
        }
    }

    private Producto copiarProducto(Producto producto) {
        Producto copia = new Producto();
        copia.setIdProducto(producto.getIdProducto());
        copia.setCategoria(producto.getCategoria());
        copia.setSku(producto.getSku());
        copia.setCodigoBarras(producto.getCodigoBarras());
        copia.setNombre(producto.getNombre());
        copia.setDescripcion(producto.getDescripcion());
        copia.setStockMinimo(producto.getStockMinimo());
        copia.setStockActual(producto.getStockActual());
        copia.setPrecioVenta(producto.getPrecioVenta());
        copia.setActivo(producto.isActivo());
        return copia;
    }

    private void limpiarCampos() {
        cmbCategoria.getSelectionModel().clearSelection();
        txtSku.clear();
        txtCodigoBarras.clear();
        txtNombre.clear();
        txtDescripcion.clear();
        txtStockMinimo.setText("0");
        txtStockActual.setText("0");
        txtPrecioVenta.setText("0.00");
    }

    private BigDecimal parseBigDecimal(String texto, String campo) {
        try {
            return new BigDecimal(texto.trim().replace(",", "."));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser numerico.");
        }
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private String valorSeguro(String texto) {
        return texto == null ? "" : texto;
    }

    private void cerrarVentana() {
        if (stage != null) {
            stage.close();
        }
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Bodega Master");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
