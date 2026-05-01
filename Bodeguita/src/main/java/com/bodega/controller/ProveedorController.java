package com.bodega.controller;

import com.bodega.model.Proveedor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ProveedorController {

    @FXML
    private TableView<Proveedor> proveedorTable;

    @FXML
    private TableColumn<Proveedor, String> colRuc;

    @FXML
    private TableColumn<Proveedor, String> colNombre;

    @FXML
    private TableColumn<Proveedor, String> colContacto;

    @FXML
    private TableColumn<Proveedor, String> colTelefono;

    @FXML
    private TableColumn<Proveedor, String> colEmail;

    private ObservableList<Proveedor> proveedores;

    @FXML
    public void initialize() {
        proveedores = FXCollections.observableArrayList(); // Replace with DAO or database call

        colRuc.setCellValueFactory(cellData -> cellData.getValue().rucProperty());
        colNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colContacto.setCellValueFactory(cellData -> cellData.getValue().contactoProperty());
        colTelefono.setCellValueFactory(cellData -> cellData.getValue().telefonoProperty());
        colEmail.setCellValueFactory(cellData -> cellData.getValue().emailProperty());

        proveedorTable.setItems(proveedores);
        loadProveedores();
    }

    public void loadProveedores() {
        // Load Proveedores e.g. proveedores.setAll(proveedorDAO.findAll());
    }

    @FXML
    public void onNuevoProveedor() {
        // Opens modal to create new Proveedor
    }

    @FXML
    public void onEditarProveedor() {
        Proveedor selected = proveedorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Seleccione un proveedor para editar.", "Información", Alert.AlertType.INFORMATION);
            return;
        }

        // Open modal to edit selected Proveedor
    }

    @FXML
    public void onInactivarProveedor() {
        Proveedor selected = proveedorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Seleccione un proveedor para inactivar.", "Información", Alert.AlertType.INFORMATION);
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de inactivar este proveedor?", ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait();
        if (confirmation.getResult() == ButtonType.YES) {
            // Use DAO to inactivate the Proveedor
            proveedores.remove(selected);
        }
    }

    @FXML
    public void onRefrescar() {
        loadProveedores();
    }

    private void showAlert(String message, String title, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}