package com.bodega.controller;

import com.bodega.model.Cliente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ClienteController {

    @FXML
    private TableView<Cliente> clienteTable;

    @FXML
    private TableColumn<Cliente, String> colIdentificacion;

    @FXML
    private TableColumn<Cliente, String> colNombre;

    @FXML
    private TableColumn<Cliente, String> colTelefono;

    @FXML
    private TableColumn<Cliente, String> colEmail;

    @FXML
    private TableColumn<Cliente, String> colDireccion;

    private ObservableList<Cliente> clientes;

    @FXML
    public void initialize() {
        clientes = FXCollections.observableArrayList(); // Replace this with DAO call for database interaction

        colIdentificacion.setCellValueFactory(new PropertyValueFactory<>("identificacion"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));

        clienteTable.setItems(clientes);
        loadClientes(); // Optionally load clients at the start
    }

    public void loadClientes() {
        // Load clients here using database service or DAO
        // Example: clientes.setAll(clienteDAO.findAll());
    }

    @FXML
    public void onNuevoCliente() {
        // Open a modal to add new client
    }

    @FXML
    public void onEditarCliente() {
        Cliente selected = clienteTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Por favor selecciona un cliente para editar.", "Información", Alert.AlertType.INFORMATION);
            return;
        }

        // Open a modal passing the selected client for editing
    }

    @FXML
    public void onBuscarCliente() {
        // Open a modal to search for a client based on criteria
    }

    @FXML
    public void onInactivarCliente() {
        Cliente selected = clienteTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Por favor selecciona un cliente para inactivar.", "Información", Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "¿Estás seguro que deseas inactivar el cliente?", ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait();
        if (confirmation.getResult() == ButtonType.YES) {
            // Perform inactivation with DAO call: clienteDAO.inactivate(selected);
            clientes.remove(selected);
        }
    }

    @FXML
    public void onRefrescar() {
        loadClientes();
    }

    private void showAlert(String message, String title, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}