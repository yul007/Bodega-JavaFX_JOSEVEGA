package com.bodega.controller;

import com.bodega.dao.CategoriaDAO;
import com.bodega.model.Categoria;
import com.bodega.util.ValidationUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.Optional;

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
    private CategoriaDAO categoriaDAO;

    @FXML
    public void initialize() {
        categoriaDAO = new CategoriaDAO();
        categorias = FXCollections.observableArrayList();

        colId.setCellValueFactory(new PropertyValueFactory<>("idCategoria"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        categoriaTable.setItems(categorias);
        loadCategorias();
    }

    public void loadCategorias() {
        try {
            categorias.setAll(categoriaDAO.listarActivas());
        } catch (SQLException e) {
            showAlert("Error al cargar categorías: " + e.getMessage(), "Error", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void onNuevaCategoria() {
        Dialog<Categoria> dialog = new Dialog<>();
        dialog.setTitle("Nueva Categoría");
        dialog.setHeaderText("Crear nueva categoría");

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre");
        TextArea descripcionArea = new TextArea();
        descripcionArea.setPromptText("Descripción");
        descripcionArea.setPrefRowCount(3);

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descripcionArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Categoria categoria = new Categoria();
                categoria.setNombre(ValidationUtils.requerido(nombreField.getText(), "nombre"));
                categoria.setDescripcion(ValidationUtils.opcional(descripcionArea.getText()));
                categoria.setActivo(true);
                return categoria;
            }
            return null;
        });

        Optional<Categoria> result = dialog.showAndWait();
        result.ifPresent(categoria -> {
            try {
                int id = categoriaDAO.crear(categoria);
                categoria.setIdCategoria(id);
                categorias.add(categoria);
                showAlert("Categoría creada exitosamente", "Éxito", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Error al crear categoría: " + e.getMessage(), "Error", Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    public void onEditarCategoria() {
        Categoria selected = categoriaTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Seleccione una categoría para editar.", "Información", Alert.AlertType.INFORMATION);
            return;
        }

        Dialog<Categoria> dialog = new Dialog<>();
        dialog.setTitle("Editar Categoría");
        dialog.setHeaderText("Editar categoría: " + selected.getNombre());

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nombreField = new TextField(selected.getNombre());
        nombreField.setPromptText("Nombre");
        TextArea descripcionArea = new TextArea(selected.getDescripcion());
        descripcionArea.setPromptText("Descripción");
        descripcionArea.setPrefRowCount(3);

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descripcionArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selected.setNombre(ValidationUtils.requerido(nombreField.getText(), "nombre"));
                selected.setDescripcion(ValidationUtils.opcional(descripcionArea.getText()));
                return selected;
            }
            return null;
        });

        Optional<Categoria> result = dialog.showAndWait();
        result.ifPresent(categoria -> {
            try {
                if (categoriaDAO.actualizar(categoria)) {
                    categoriaTable.refresh();
                    showAlert("Categoría actualizada exitosamente", "Éxito", Alert.AlertType.INFORMATION);
                }
            } catch (SQLException e) {
                showAlert("Error al actualizar categoría: " + e.getMessage(), "Error", Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    public void onInactivarCategoria() {
        Categoria selected = categoriaTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Seleccione una categoría para inactivar.", "Información", Alert.AlertType.INFORMATION);
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, 
            "¿Está seguro de inactivar esta categoría?", 
            ButtonType.YES, 
            ButtonType.NO);
        confirmation.showAndWait();
        
        if (confirmation.getResult() == ButtonType.YES) {
            try {
                if (categoriaDAO.inactivar(selected.getIdCategoria())) {
                    categorias.remove(selected);
                    showAlert("Categoría inactivada exitosamente", "Éxito", Alert.AlertType.INFORMATION);
                }
            } catch (SQLException e) {
                showAlert("Error al inactivar categoría: " + e.getMessage(), "Error", Alert.AlertType.ERROR);
            }
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
