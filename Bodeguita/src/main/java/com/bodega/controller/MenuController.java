package com.bodega.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/** Controlador del menu principal y navegacion entre modulos. */
public class MenuController {

    @FXML
    private Label usuarioActivoLabel;

    @FXML
    private void initialize() {
        usuarioActivoLabel.setText("Usuario activo: admin");
    }

    @FXML
    private void abrirProductos() {
        navegarA("/view/productos.fxml", "Productos");
    }

    @FXML
    private void abrirLotes() {
        navegarA("/view/lotes.fxml", "Lotes");
    }

    @FXML
    private void abrirSalidas() {
        navegarA("/view/salidas.fxml", "Salidas");
    }

    @FXML
    private void abrirKardex() {
        navegarA("/view/kardex.fxml", "Kardex");
    }

    @FXML
    private void abrirClientes() {
        navegarA("/view/clientes.fxml", "Clientes");
    }

    @FXML
    private void abrirReportes() {
        navegarA("/view/reportes.fxml", "Reportes");
    }

    @FXML
    private void abrirDashboard() {
        navegarA("/view/dashboard.fxml", "Dashboard");
    }

    private void navegarA(String rutaFxml, String tituloModulo) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(rutaFxml));
            Scene scene = new Scene(root, 1000, 650);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) usuarioActivoLabel.getScene().getWindow();
            stage.setTitle("Bodega Master - " + tituloModulo);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException | RuntimeException exception) {
            mostrarError("No se pudo abrir " + tituloModulo, "Revisa que la pantalla exista y cargue correctamente.");
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
