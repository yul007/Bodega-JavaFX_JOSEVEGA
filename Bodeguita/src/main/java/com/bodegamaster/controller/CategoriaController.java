package com.bodega.controller;

import com.bodegamaster.model.Categoria;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CategoriaController {

    @FXML
    private TableView<Categoria> categoriaTable;

    @FXML
    private TableColumn<Categoria, String> colId;

    @FXML
    private TableColumn<Categoria, String> colNombre;

    @FXML
    private TableColumn<Categoria, String> colDescripcion;

    private ObservableList<Categoria> categorias;

    @FXML
    public void initialize() {
        categorias = FXCollections.observableArrayList(); // Fetch from database via DAO

        colId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asString());
        colNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colDescripcion.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());

        categoriaTable.setItems(categorias);
        loadCategorias();
    }

    public void loadCategorias() {
        // Fetch records e.g. categorias.setAll(categoriaDAO.findAll());
    }

    @FXML
    public void onNuevaCategoria() {
        // Show modal to create a new Categoria
    }

    @FXML
    public void onEditarCategoria() {
        Categoria selected = categoriaTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Seleccione una categoría para editar.", "Información", Alert.AlertType.INFORMATION);
            return;
        }

        // Opens edit modal for the selected Categoria
    }

    @FXML
    public void onInactivarCategoria() {
        Categoria selected = categoriaTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Seleccione una categoría para inactivar.", "Información", Alert.AlertType.INFORMATION);
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de inactivar esta categoría?", ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait();
        if (confirmation.getResult() == ButtonType.YES) {
            // Call DAO to mark Categoria as inactive
            categorias.remove(selected);
        }
    }

    @FXML
    public void onRefrescar() {
        loadCategorias();
    }

    private void showAlert(String message, String title, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}