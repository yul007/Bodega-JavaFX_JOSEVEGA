package com.bodega.controller;

import com.bodega.dao.ProveedorDAO;
import com.bodega.model.Proveedor;
import com.bodega.util.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.Optional;

public class ProveedorController {

    @FXML private TableView<Proveedor> proveedorTable;
    @FXML private TableColumn<Proveedor, String> colRuc;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colContacto;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colEmail;

    private ObservableList<Proveedor> proveedores;
    private ProveedorDAO proveedorDAO;

    @FXML public void initialize() {
        proveedorDAO = new ProveedorDAO();
        proveedores = FXCollections.observableArrayList();

        colRuc.setCellValueFactory(new PropertyValueFactory<>("ruc"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colContacto.setCellValueFactory(new PropertyValueFactory<>("contacto"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        proveedorTable.setItems(proveedores);
        loadProveedores();
    }

    public void loadProveedores() {
        try {
            proveedores.setAll(proveedorDAO.listarActivos());
        } catch (SQLException e) {
            showAlert("Error al cargar proveedores: " + e.getMessage(), "Error", Alert.AlertType.ERROR);
        }
    }

    @FXML public void onNuevoProveedor() {
        Dialog<Proveedor> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Proveedor");
        dialog.setHeaderText("Crear nuevo proveedor");

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField rucField = new TextField();
        rucField.setPromptText("RUC");
        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre");
        TextField contactoField = new TextField();
        contactoField.setPromptText("Contacto");
        TextField telefonoField = new TextField();
        telefonoField.setPromptText("Teléfono");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField direccionField = new TextField();
        direccionField.setPromptText("Dirección");

        grid.add(new Label("RUC:"), 0, 0);
        grid.add(rucField, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(nombreField, 1, 1);
        grid.add(new Label("Contacto:"), 0, 2);
        grid.add(contactoField, 1, 2);
        grid.add(new Label("Teléfono:"), 0, 3);
        grid.add(telefonoField, 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(new Label("Dirección:"), 0, 5);
        grid.add(direccionField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Proveedor proveedor = new Proveedor();
                proveedor.setRuc(ValidationUtils.requerido(rucField.getText(), "RUC"));
                proveedor.setNombre(ValidationUtils.requerido(nombreField.getText(), "nombre"));
                proveedor.setContacto(ValidationUtils.opcional(contactoField.getText()));
                proveedor.setTelefono(ValidationUtils.opcional(telefonoField.getText()));
                proveedor.setEmail(ValidationUtils.opcional(emailField.getText()));
                proveedor.setDireccion(ValidationUtils.opcional(direccionField.getText()));
                proveedor.setActivo(true);
                validarProveedor(proveedor);
                return proveedor;
            }
            return null;
        });

        Optional<Proveedor> result = dialog.showAndWait();
        result.ifPresent(proveedor -> {
            try {
                int id = proveedorDAO.crear(proveedor);
                proveedor.setIdProveedor(id);
                proveedores.add(proveedor);
                showAlert("Proveedor creado exitosamente", "Éxito", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Error al crear proveedor: " + e.getMessage(), "Error", Alert.AlertType.ERROR);
            }
        });
    }

    @FXML public void onEditarProveedor() {
        Proveedor selected = proveedorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Seleccione un proveedor para editar.", "Información", Alert.AlertType.INFORMATION);
            return;
        }

        Dialog<Proveedor> dialog = new Dialog<>();
        dialog.setTitle("Editar Proveedor");
        dialog.setHeaderText("Editar proveedor: " + selected.getNombre());

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField rucField = new TextField(selected.getRuc());
        rucField.setPromptText("RUC");
        TextField nombreField = new TextField(selected.getNombre());
        nombreField.setPromptText("Nombre");
        TextField contactoField = new TextField(selected.getContacto());
        contactoField.setPromptText("Contacto");
        TextField telefonoField = new TextField(selected.getTelefono());
        telefonoField.setPromptText("Teléfono");
        TextField emailField = new TextField(selected.getEmail());
        emailField.setPromptText("Email");
        TextField direccionField = new TextField(selected.getDireccion());
        direccionField.setPromptText("Dirección");

        grid.add(new Label("RUC:"), 0, 0);
        grid.add(rucField, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(nombreField, 1, 1);
        grid.add(new Label("Contacto:"), 0, 2);
        grid.add(contactoField, 1, 2);
        grid.add(new Label("Teléfono:"), 0, 3);
        grid.add(telefonoField, 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(new Label("Dirección:"), 0, 5);
        grid.add(direccionField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selected.setRuc(ValidationUtils.requerido(rucField.getText(), "RUC"));
                selected.setNombre(ValidationUtils.requerido(nombreField.getText(), "nombre"));
                selected.setContacto(ValidationUtils.opcional(contactoField.getText()));
                selected.setTelefono(ValidationUtils.opcional(telefonoField.getText()));
                selected.setEmail(ValidationUtils.opcional(emailField.getText()));
                selected.setDireccion(ValidationUtils.opcional(direccionField.getText()));
                validarProveedor(selected);
                return selected;
            }
            return null;
        });

        Optional<Proveedor> result = dialog.showAndWait();
        result.ifPresent(proveedor -> {
            try {
                if (proveedorDAO.actualizar(proveedor)) {
                    proveedorTable.refresh();
                    showAlert("Proveedor actualizado exitosamente", "Éxito", Alert.AlertType.INFORMATION);
                }
            } catch (SQLException e) {
                showAlert("Error al actualizar proveedor: " + e.getMessage(), "Error", Alert.AlertType.ERROR);
            }
        });
    }

    @FXML public void onInactivarProveedor() {
        Proveedor selected = proveedorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Seleccione un proveedor para inactivar.", "Información", Alert.AlertType.INFORMATION);
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, 
            "¿Está seguro de inactivar este proveedor?", 
            ButtonType.YES, 
            ButtonType.NO);
        confirmation.showAndWait();
        
        if (confirmation.getResult() == ButtonType.YES) {
            try {
                if (proveedorDAO.inactivar(selected.getIdProveedor())) {
                    proveedores.remove(selected);
                    showAlert("Proveedor inactivado exitosamente", "Éxito", Alert.AlertType.INFORMATION);
                }
            } catch (SQLException e) {
                showAlert("Error al inactivar proveedor: " + e.getMessage(), "Error", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML public void onRefrescar() {
        loadProveedores();
    }

    private void showAlert(String message, String title, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void validarProveedor(Proveedor proveedor) {
        ValidationUtils.requerido(proveedor.getRuc(), "RUC");
        ValidationUtils.requerido(proveedor.getNombre(), "nombre");
    }
}
