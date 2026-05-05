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
        configurarTabla();       // 
        configurarSeleccionQr(); //se inizializa listener de QR
        cargarProductos();       //OBTENER LOS PRODUCTOS DESDE LA BASE DE DATOS Y MOSTRARLOS EN LA TABLA 
    }

    @FXML
    private void nuevoProducto() { // se abre el formulario y se lanza mensaje
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
    private void buscarProducto() { //
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

    private void configurarTabla() { // AQUI SE CONFIGURAN LAS COLUMNAS DE LA TABLA PARA MOSTRAR LOS DATOS CORRESPONDIENTES DE CADA PRODUCTO, SE ASIGNAN LOS VALORES DE CADA COLUMNA USANDO EXPRESIONES LAMBDA, Y SE CONFIGURA UNA FABRICA DE FILAS PARA APLICAR ESTILOS VISUALES SEGUN EL STOCK ACTUAL EN COMPARACION CON EL STOCK MINIMO.
                                     //, data.getValue() obtiene el producto de esa fila.
        idColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getIdProducto())); // new SimpleObjectProperty o SimpleStringProperty envuelven el valor para que la tabla pueda observarlo y actualizarlo automáticamente
        nombreColumn.setCellValueFactory(data -> new SimpleStringProperty(valorSeguro(data.getValue().getNombre())));
        categoriaColumn.setCellValueFactory(data -> { String nombreCategoria = data.getValue().getCategoria() == null ? "" : valorSeguro(data.getValue().getCategoria().getNombre()); return new SimpleStringProperty(nombreCategoria);
        });
        stockColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStockActual()));
        stockMinimoColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStockMinimo()));

        productosTable.setItems(productos); //  //productos es un ObservableList<Producto> que contiene todos los productos a mostrar. La tabla se actualizará automáticamente cuando esta lista cambie.
        productosTable.setRowFactory(table -> new TableRow<>() { //.setRowFactory produce rows personalizables
            @Override 
            protected void updateItem(Producto producto, boolean empty) { //se llama automaticamente
                super.updateItem(producto, empty);
                getStyleClass().removeAll("stock-low-row", "stock-warning-row"); //se remueven los estilos para que no se acumulen
                if (empty || producto == null) {
                    return;
                }
                int comparacion = valorSeguro(producto.getStockActual()).compareTo(valorSeguro(producto.getStockMinimo())); //se validan y comparan Se comparan stockActual y stockMinimo
                if (comparacion < 0) {
                    getStyleClass().add("stock-low-row");
                } else if (comparacion == 0) {
                    getStyleClass().add("stock-warning-row");
                }
            }
        });
    }

    private void configurarSeleccionQr() { // AQUI SE CONFIGURA UN LISTENER PARA DETECTAR CUANDO SE SELECCIONA UN PRODUCTO EN LA TABLA, Y SE GENERA UN CODIGO QR PARA EL PRODUCTO SELECCIONADO USANDO EL SERVICIO QrCodeService, MOSTRANDOLO EN EL ImageView CORRESPONDIENTE. SI NO HAY NINGUN PRODUCTO SELECCIONADO, SE LIMPIA LA IMAGEN DEL QR.
        productosTable.getSelectionModel().selectedItemProperty().addListener((observable, anterior, actual) -> { //.addListenes y observable, anterior, actual son parte de la sintaxis para agregar un listener a una propiedad observable en JavaFX. En este caso, se esta agregando un listener a la propiedad selectedItemProperty() de la tabla productosTable, que representa el producto actualmente seleccionado en la tabla. El listener se ejecuta cada vez que cambia el producto seleccionado, recibiendo como parametros el valor anterior y el valor actual del producto seleccionado.
            if (actual == null) {                                               //^ OB: Es la propiedad observable que está siendo monitoreada, en este caso es el, AN: Es el valor anterior que tenía la propiedad antes del cambio. Es decir, el producto que estaba seleccionado previamente (puede ser null si no había selección). ACT: Es el valor nuevo después del cambio. Es el obj actual seleccionado 
                Platform.runLater(() -> qrImageView.setImage(null)); // Si el producto seleccionado es null, se ejecuta en el hilo de la interfaz grafica para limpiar la imagen del QR estableciendo la imagen del ImageView a null. Esto asegura que no se muestre ningun QR cuando no hay un producto seleccionado.
            } else {
                Platform.runLater(() -> qrCodeService.generarQRCode(actual, qrImageView)); // Si hay un producto seleccionado, se ejecuta en el hilo de la interfaz grafica para generar un QR para el producto actual usando el servicio qrCodeService, y se muestra en el ImageView qrImageView. Esto permite que cada vez que el usuario seleccione un producto diferente en la tabla, se actualice automaticamente el QR mostrado para reflejar el producto seleccionado.
            }
        });
    }

    private void cargarProductos() { // AQUI SE CARGAN LOS PRODUCTOS DESDE LA BASE DE DATOS USANDO EL DAO, SE ACTUALIZA LA LISTA OBSERVABLE CON LOS PRODUCTOS OBTENIDOS, Y SI HAY PRODUCTOS EN LA LISTA, SE SELECCIONA EL PRIMERO Y SE GENERA SU QR. SI OCURRE ALGUNA EXCEPCION DURANTE LA CARGA DE LOS PRODUCTOS, SE MUESTRA UN MENSAJE DE ERROR AL USUARIO.
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
