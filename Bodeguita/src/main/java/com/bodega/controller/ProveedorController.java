//JAAN
package com.bodega.controller;

import com.bodega.dao.ProveedorDAO;
import com.bodega.model.Proveedor;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

/** Controlador de proveedores: lista, alta, edición e inactivación. */
public class ProveedorController {

    @FXML private TableView<Proveedor> proveedorTable;
    @FXML private TableColumn<Proveedor, String> colRuc;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colContacto;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colEmail;

    private final ObservableList<Proveedor> proveedores = FXCollections.observableArrayList();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    @FXML
    public void initialize() {
        colRuc.setCellValueFactory(new PropertyValueFactory<>("ruc"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colContacto.setCellValueFactory(new PropertyValueFactory<>("contacto"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        proveedorTable.setItems(proveedores); // Vincula la lista observable de proveedores con la tabla para que se actualice automáticamente cuando cambie la lista
        loadProveedores();                    // Carga los proveedores activos desde la base de datos y los muestra en la tabla al inicializar el controlador
    }

    public void loadProveedores() {
        try {
            proveedores.setAll(proveedorDAO.listarActivos()); // Obtiene la lista de proveedores activos desde la base de datos utilizando el método listarActivos() del ProveedorDAO, y luego actualiza la lista observable "proveedores" con esos datos, lo que a su vez actualiza la tabla en la interfaz de usuario.
        } catch (SQLException exception) {
            showAlert("Error al cargar proveedores: " + exception.getMessage(), "Error", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onNuevoProveedor() {
        abrirFormularioProveedor(null).ifPresent(proveedor -> { 
            try {
                int idGenerado = proveedorDAO.crear(proveedor);
                proveedor.setIdProveedor(idGenerado);
                loadProveedores();
                showAlert("Proveedor creado exitosamente", "Éxito", Alert.AlertType.INFORMATION);
            } catch (SQLException exception) {
                showAlert("Error al crear proveedor: " + exception.getMessage(), "Error", Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    public void onEditarProveedor() {
        Proveedor seleccionado = proveedorTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            showAlert("Seleccione un proveedor para editar.", "Información", Alert.AlertType.INFORMATION);
            return;
        }

        abrirFormularioProveedor(seleccionado).ifPresent(proveedor -> {
            try {
                if (proveedorDAO.actualizar(proveedor)) {
                    loadProveedores();
                    showAlert("Proveedor actualizado exitosamente", "Éxito", Alert.AlertType.INFORMATION);
                }
            } catch (SQLException exception) {
                showAlert("Error al actualizar proveedor: " + exception.getMessage(), "Error", Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    public void onInactivarProveedor() {
        Proveedor seleccionado = proveedorTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            showAlert("Seleccione un proveedor para inactivar.", "Información", Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Está seguro de inactivar este proveedor?");
        confirmation.setTitle("Confirmación");
        confirmation.setHeaderText("Inactivar proveedor");

        confirmation.showAndWait().ifPresent(button -> {
            if (button == javafx.scene.control.ButtonType.OK) {
                try {
                    if (proveedorDAO.inactivar(seleccionado.getIdProveedor())) {
                        loadProveedores();
                        showAlert("Proveedor inactivado exitosamente", "Éxito", Alert.AlertType.INFORMATION);
                    }
                } catch (SQLException exception) {
                    showAlert("Error al inactivar proveedor: " + exception.getMessage(), "Error", Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    public void onRefrescar() {
        loadProveedores();
    }

    private Optional<Proveedor> abrirFormularioProveedor(Proveedor proveedor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/proveedor_form.fxml"));
            Parent root = loader.load();

            ProveedorFormController controller = loader.getController();
            controller.setProveedor(proveedor);

            Stage stage = new Stage();
            stage.setTitle(proveedor == null ? "Nuevo Proveedor" : "Editar Proveedor");
            stage.initModality(Modality.APPLICATION_MODAL);
            if (proveedorTable.getScene() != null && proveedorTable.getScene().getWindow() instanceof Stage owner) {
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
            showAlert("No se pudo abrir el formulario de proveedor.", "Error", Alert.AlertType.ERROR);
            return Optional.empty();
        }
    }

    private void showAlert(String message, String title, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
