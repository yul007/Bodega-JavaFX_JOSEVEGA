//JAAN
package com.bodega.controller;

import com.bodega.dao.ProductoDAO;
import com.bodega.model.Producto;
import com.bodega.service.QrCodeService;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

/** Controlador de productos: busqueda, CRUD y alertas visuales de stock. */
public class ProductoController {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final QrCodeService qrCodeService = new QrCodeService();
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    @FXML private TextField buscadorField;
    @FXML private TableView<Producto> productosTable;
    @FXML private TableColumn<Producto, Integer> idColumn;
    @FXML private TableColumn<Producto, String> nombreColumn;
    @FXML private TableColumn<Producto, String> categoriaColumn;
    @FXML private TableColumn<Producto, BigDecimal> stockColumn;
    @FXML private TableColumn<Producto, BigDecimal> stockMinimoColumn;
    @FXML private ImageView qrImageView;

    @FXML
    private void initialize() {
        configurarTabla();
        configurarSeleccionQr();
        cargarProductos();
    }

    @FXML
    private void nuevoProducto() {
        abrirFormularioProducto(null).ifPresent(producto -> {
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
                    : productoDAO.buscarPorNombre(texto);
            productos.setAll(resultado);
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

        abrirFormularioProducto(seleccionado).ifPresent(producto -> {
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

    private void configurarTabla() {
        idColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getIdProducto()));
        nombreColumn.setCellValueFactory(data -> new SimpleStringProperty(valorSeguro(data.getValue().getNombre())));
        categoriaColumn.setCellValueFactory(data -> {
            String nombreCategoria = data.getValue().getCategoria() == null
                    ? ""
                    : valorSeguro(data.getValue().getCategoria().getNombre());
            return new SimpleStringProperty(nombreCategoria);
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
                Platform.runLater(() -> qrImageView.setImage(null));
            } else {
                Platform.runLater(() -> qrCodeService.generarQRCode(actual, qrImageView));
            }
        });
    }

    private void cargarProductos() {
        try {
            productos.setAll(productoDAO.listarActivos());
            if (!productos.isEmpty()) {
                productosTable.getSelectionModel().selectFirst();
                Producto seleccionado = productosTable.getSelectionModel().getSelectedItem();
                Platform.runLater(() -> qrCodeService.generarQRCode(seleccionado, qrImageView));
            } else {
                Platform.runLater(() -> qrImageView.setImage(null));
            }
        } catch (SQLException exception) {
            mostrarError("No se pudo cargar productos", exception.getMessage());
        }
    }

    private Optional<Producto> abrirFormularioProducto(Producto existente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/producto_form.fxml"));
            Parent root = loader.load();

            ProductoFormController controller = loader.getController();
            controller.setProducto(existente);

            Stage stage = new Stage();
            stage.setTitle(existente == null ? "Nuevo producto" : "Editar producto");
            stage.initModality(Modality.APPLICATION_MODAL);
            if (productosTable.getScene() != null && productosTable.getScene().getWindow() instanceof Stage owner) {
                stage.initOwner(owner);
            }

            Scene scene = new Scene(root);
            if (getClass().getResource("/css/styles.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            }

            stage.setScene(scene);
            stage.setResizable(false);
            controller.setStage(stage);
            stage.showAndWait();
            return controller.getResultado();
        } catch (IOException exception) {
            mostrarError("No se pudo abrir el formulario de producto", exception.getMessage());
            return Optional.empty();
        }
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

    private Producto obtenerSeleccionado() {
        Producto seleccionado = productosTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Seleccion requerida", "Selecciona un producto de la tabla.");
        }
        return seleccionado;
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private String valorSeguro(String texto) {
        return texto == null ? "" : texto;
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
