package com.bodega.controller;

import com.bodega.util.MusicPlayer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import java.io.IOException;

/** Controlador de login para autenticar al usuario de demostracion. */
public class LoginController {

    private static final String DEMO_USER = "admin";
    private static final String DEMO_PASSWORD = "admin";
    private static final String LOGIN_SOUND = "/audio/login.mp3";

    //private final MusicPlayer loginSoundPlayer = MusicPlayer.cargarDesdeResources(LOGIN_SOUND);
    private final MusicPlayer loginSoundPlayer = new MusicPlayer(LOGIN_SOUND, false);


    @FXML private TextField usuarioField;
    @FXML private PasswordField contrasenaField;
    @FXML private ToggleButton musicaToggle;

    @FXML private void ingresar() {
        String usuario = usuarioField.getText() == null ? "" : usuarioField.getText().trim();
        String contrasena = contrasenaField.getText() == null ? "" : contrasenaField.getText();

        if (DEMO_USER.equals(usuario) && DEMO_PASSWORD.equals(contrasena)) {
            loginSoundPlayer.play();
            abrirMenu();
            return;
        }

        mostrarAlerta("Credenciales incorrectas", "Usa admin/admin para la demostracion.");
        contrasenaField.clear();
        contrasenaField.requestFocus();
    }

    @FXML private void salir() {
        //MusicPlayer.detenerMusicaFondo();
        Platform.exit();
    }

    @FXML private void alternarMusica() {
        if (musicaToggle.isSelected()) {
            MusicPlayer.reanudarMusicaFondo();
            musicaToggle.setText("Musica ON");
        } else {
            MusicPlayer.pausarMusicaFondo();
            musicaToggle.setText("Musica OFF");
        }
    }

    private void abrirMenu() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/menu.fxml"));
            Scene scene = new Scene(root, 1000, 650);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) usuarioField.getScene().getWindow();
            stage.setTitle("Bodeguita - Menu principal");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException exception) {
            mostrarAlerta("No se pudo abrir el menu", "Revisa que menu.fxml exista y sea valido.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Bodeguita");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML public void initialize() {
        // Esto asegura que la música de fondo empiece apenas abre el login
        MusicPlayer.reproducirMusicaFondo();
    }
}
