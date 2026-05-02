package com.bodega.controller;

import com.bodega.dao.CategoriaDAO;
import com.bodega.dao.ProductoDAO;
import com.bodega.model.Categoria;
import com.bodega.model.Producto;
import com.bodega.service.QrCodeService;
import com.bodega.util.ValidationUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// Estas son vitales para que tus columnas funcionen:
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;

/** Controlador de productos: busqueda, CRUD y alertas visuales de stock. */
public class ProductoController {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final QrCodeService qrCodeService = new QrCodeService();
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    @FXML
    private TextField buscadorField;

    @FXML
    private Label stockAlertaLabel;

    @FXML
    private TableView<Producto> productosTable;

    @FXML
    private TableColumn<Producto, Integer> idColumn;

    @FXML     private TableColumn<Producto, String> nombreColumn;

    @FXML
    private TableColumn<Producto, String> categoriaColumn;

    @FXML
    private TableColumn<Producto, BigDecimal> stockColumn;

    @FXML
    private TableColumn<Producto, BigDecimal> stockMinimoColumn;

    @FXML
    private ImageView qrImageView;

    @FXML
    private void initialize() {
        configurarTabla();
        configurarSeleccionQr();
        cargarProductos();
    }

    @FXML
    private void nuevoProducto() {
        abrirDialogoProducto(null).ifPresent(producto -> {
            try {
                productoDAO.crear(producto);
                cargarProductos();
                mostrarInformacion("Producto creado", "El producto se guardo correctamente.");
            } catch (SQLException exception) {
                mostrarError("No se pudo crear el producto", exception.getMessage());
            }
        });
    }

    @FXML
    private void buscarProducto() {
        String texto = buscadorField.getText() == null ? "" : buscadorField.getText().trim();
        try {
            List<Producto> resultado = texto.isEmpty()
                    ? productoDAO.listarActivos()
                    : productoDAO.buscarPorNombreOCodigo(texto);
            productos.setAll(resultado);
            actualizarResumenStock();
        } catch (SQLException exception) {
            mostrarError("No se pudo buscar productos", exception.getMessage());
        }
    }

    @FXML
    private void editarProducto() {
        Producto seleccionado = obtenerSeleccionado();
        if (seleccionado == null) {
            return;
        }

        abrirDialogoProducto(seleccionado).ifPresent(producto -> {
            try {
                productoDAO.actualizar(producto);
                cargarProductos();
                mostrarInformacion("Producto actualizado", "Los cambios se guardaron correctamente.");
            } catch (SQLException exception) {
                mostrarError("No se pudo actualizar el producto", exception.getMessage());
            }
        });
    }

    @FXML
    private void inactivarProducto() {
        Producto seleccionado = obtenerSeleccionado();
        if (seleccionado == null) {
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Bodega Master");
        confirmacion.setHeaderText("Inactivar producto");
        confirmacion.setContentText("Se inactivara " + seleccionado.getNombre() + ".");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            productoDAO.inactivar(seleccionado.getIdProducto());
            cargarProductos();
            mostrarInformacion("Producto inactivado", "El producto ya no aparece como activo.");
        } catch (SQLException exception) {
            mostrarError("No se pudo inactivar el producto", exception.getMessage());
        }
    }

    @FXML
    private void verLotes() {
        if (obtenerSeleccionado() != null) {
            navegarA("/view/lotes.fxml", "Lotes");
        }
    }

    @FXML
    private void nuevaSalida() {
        if (obtenerSeleccionado() != null) {
            navegarA("/view/salidas.fxml", "Salidas");
        }
    }

    // @FXML
    // private void mostrarQrSeleccionado() {
    //     Producto seleccionado = obtenerSeleccionado();
    //     if (seleccionado == null) {
    //         Platform.runLater(() -> qrImageView.setImage(null));
    //         return;
    //     }
    //     generarQrEnBackground(seleccionado);
    // }

    //     @FXML
    // private void mostrarQrSeleccionado() {
    //     Producto seleccionado = obtenerSeleccionado();
    //     if (seleccionado == null) {
    //         qrImageView.setImage(null);
    //         return;
    //     }
        
    //     // Llamada directa sin hilos ni Platform.runLater
    //     qrCodeService.generarQRCode(seleccionado, qrImageView);
    // }

@FXML
private void mostrarQrSeleccionado() {
    Producto seleccionado = obtenerSeleccionado();
    if (seleccionado == null) {
        qrImageView.setImage(null);
        return;
    }
    // Llamada directa al servicio que ya tienes
    qrCodeService.generarQRCode(seleccionado, qrImageView);
}

    private void configurarTabla() {
        idColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getIdProducto()));
        nombreColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        categoriaColumn.setCellValueFactory(data -> {
            Categoria categoria = data.getValue().getCategoria();
            return new SimpleStringProperty(categoria == null ? "" : categoria.getNombre());
        });
        stockColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStockActual()));
        stockMinimoColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStockMinimo()));

        productosTable.setItems(productos);
        productosTable.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(Producto producto, boolean empty) {
                super.updateItem(producto, empty);
                getStyleClass().removeAll("stock-low-row", "stock-warning-row");
                if (empty || producto == null) {
                    return;
                }

                int comparacion = valorSeguro(producto.getStockActual()).compareTo(valorSeguro(producto.getStockMinimo()));
                if (comparacion < 0) {
                    getStyleClass().add("stock-low-row");
                } else if (comparacion == 0) {
                    getStyleClass().add("stock-warning-row");
                }
            }
        });
    }

    private void configurarSeleccionQr() {
        productosTable.getSelectionModel().selectedItemProperty().addListener((observable, anterior, actual) -> {
            if (actual == null) {
                qrImageView.setImage(null);
            } else {
                qrCodeService.generarQRCode(actual, qrImageView);
            }
        });
    }


    private void cargarProductos() {
        try {
            productos.setAll(productoDAO.listarActivos());
            actualizarResumenStock();
            if (!productos.isEmpty()) {
                productosTable.getSelectionModel().selectFirst();
                qrCodeService.generarQRCode(productosTable.getSelectionModel().getSelectedItem(), qrImageView);
            } else {
                qrImageView.setImage(null);
            }
        } catch (SQLException exception) {
            mostrarError("No se pudo cargar productos", exception.getMessage());
        }
    }

    private Optional<Producto> abrirDialogoProducto(Producto existente) {
        Dialog<Producto> dialog = new Dialog<>();
        dialog.setTitle(existente == null ? "Nuevo producto" : "Editar producto");
        dialog.setHeaderText(existente == null ? "Registrar producto" : "Actualizar producto");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<Categoria> categoriaCombo = new ComboBox<>();
        TextField skuField = new TextField();
        TextField codigoField = new TextField();
        TextField nombreField = new TextField();
        TextField descripcionField = new TextField();
        TextField unidadField = new TextField("unidad");
        TextField stockMinimoField = new TextField("0");
        TextField stockActualField = new TextField("0");
        TextField precioField = new TextField("0.00");

        try {
            categoriaCombo.setItems(FXCollections.observableArrayList(categoriaDAO.listarActivas()));
        } catch (SQLException exception) {
            mostrarError("No se pudo cargar categorias", exception.getMessage());
        }

        if (existente != null) {
            skuField.setText(existente.getSku());
            codigoField.setText(existente.getCodigoBarras());
            nombreField.setText(existente.getNombre());
            descripcionField.setText(existente.getDescripcion());
            unidadField.setText(existente.getUnidadMedida());
            stockMinimoField.setText(valorSeguro(existente.getStockMinimo()).toPlainString());
            stockActualField.setText(valorSeguro(existente.getStockActual()).toPlainString());
            precioField.setText(valorSeguro(existente.getPrecioVenta()).toPlainString());
            seleccionarCategoria(categoriaCombo, existente.getCategoria());
        }

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(12));
        form.addRow(0, new Label("Categoria"), categoriaCombo);
        form.addRow(1, new Label("SKU"), skuField);
        form.addRow(2, new Label("Codigo barras"), codigoField);
        form.addRow(3, new Label("Nombre"), nombreField);
        form.addRow(4, new Label("Descripcion"), descripcionField);
        form.addRow(5, new Label("Unidad"), unidadField);
        form.addRow(6, new Label("Stock minimo"), stockMinimoField);
        form.addRow(7, new Label("Stock actual"), stockActualField);
        form.addRow(8, new Label("Precio venta"), precioField);
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }

            Producto producto = existente == null ? new Producto() : existente;
            producto.setCategoria(categoriaCombo.getValue());
            producto.setSku(ValidationUtils.requerido(skuField.getText(), "SKU"));
            producto.setCodigoBarras(ValidationUtils.opcional(codigoField.getText()));
            producto.setNombre(ValidationUtils.requerido(nombreField.getText(), "nombre"));
            producto.setDescripcion(ValidationUtils.opcional(descripcionField.getText()));
            producto.setUnidadMedida(ValidationUtils.requerido(unidadField.getText(), "unidad de medida"));
            producto.setStockMinimo(parseNoNegativo(stockMinimoField.getText(), "stock minimo"));
            producto.setStockActual(parseNoNegativo(stockActualField.getText(), "stock actual"));
            producto.setPrecioVenta(parseNoNegativo(precioField.getText(), "precio venta"));
            producto.setActivo(true);
            validarProducto(producto);
            return producto;
        });

        try {
            return dialog.showAndWait();
        } catch (IllegalArgumentException exception) {
            mostrarError("Datos invalidos", exception.getMessage());
            return Optional.empty();
        }
    }

    private void seleccionarCategoria(ComboBox<Categoria> categoriaCombo, Categoria categoria) {
        if (categoria == null) {
            return;
        }
        for (Categoria item : categoriaCombo.getItems()) {
            if (item.getIdCategoria() == categoria.getIdCategoria()) {
                categoriaCombo.getSelectionModel().select(item);
                return;
            }
        }
    }

    private void validarProducto(Producto producto) {
        if (producto.getCategoria() == null) {
            throw new IllegalArgumentException("Debe seleccionar una categoria.");
        }
        ValidationUtils.requerido(producto.getSku(), "SKU");
        ValidationUtils.requerido(producto.getNombre(), "nombre");
        ValidationUtils.requerido(producto.getUnidadMedida(), "unidad de medida");
        ValidationUtils.requeridoNoNegativo(producto.getStockMinimo(), "stock minimo");
        ValidationUtils.requeridoNoNegativo(producto.getStockActual(), "stock actual");
        ValidationUtils.requeridoNoNegativo(producto.getPrecioVenta(), "precio venta");
    }

    private Producto obtenerSeleccionado() {
        Producto seleccionado = productosTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Seleccion requerida", "Selecciona un producto de la tabla.");
        }
        return seleccionado;
    }

    private void actualizarResumenStock() {
        long stockBajo = productos.stream()
                .filter(producto -> valorSeguro(producto.getStockActual()).compareTo(valorSeguro(producto.getStockMinimo())) <= 0)
                .count();
        stockAlertaLabel.setText(stockBajo == 0
                ? "Sin productos con stock bajo"
                : "Productos con stock bajo o al minimo: " + stockBajo);
    }

    private void navegarA(String rutaFxml, String tituloModulo) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(rutaFxml));
            Scene scene = new Scene(root, 1000, 650);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) productosTable.getScene().getWindow();
            stage.setTitle("Bodega Master - " + tituloModulo);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException | RuntimeException exception) {
            mostrarError("No se pudo abrir " + tituloModulo, "Revisa que la pantalla exista y cargue correctamente.");
        }
    }

    private BigDecimal parseNoNegativo(String texto, String campo) {
        try {
            BigDecimal valor = new BigDecimal(texto.trim().replace(",", "."));
            if (valor.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El campo " + campo + " no puede ser negativo.");
            }
            return valor;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser numerico.");
        }
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bodega Master");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Bodega Master");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
