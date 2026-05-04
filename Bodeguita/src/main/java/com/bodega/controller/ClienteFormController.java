//JAAN
package com.bodega.controller;

import com.bodega.model.Cliente;
import com.bodega.util.ValidationUtils;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/** Controlador del formulario modal de clientes. */
public class ClienteFormController {

    @FXML private TextField identificacionField;
    @FXML private TextField nombreField;
    @FXML private TextField telefonoField;
    @FXML private TextField emailField;
    @FXML private TextField direccionField;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private Stage stage;
    private Cliente clienteOriginal;
    private Cliente resultado;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCliente(Cliente cliente) {
        this.clienteOriginal = cliente;
        if (cliente == null) {
            limpiarCampos();
            return;
        }

        identificacionField.setText(valorSeguro(cliente.getIdentificacion()));
        nombreField.setText(valorSeguro(cliente.getNombre()));
        telefonoField.setText(valorSeguro(cliente.getTelefono()));
        emailField.setText(valorSeguro(cliente.getEmail()));
        direccionField.setText(valorSeguro(cliente.getDireccion()));
    }

    public Optional<Cliente> getResultado() {
        return Optional.ofNullable(resultado);
    }

    @FXML
    private void onGuardar() {
        try {
            Cliente cliente = clienteOriginal == null ? new Cliente() : copiarCliente(clienteOriginal);
            cliente.setIdentificacion(ValidationUtils.requerido(identificacionField.getText(), "identificacion"));
            cliente.setNombre(ValidationUtils.requerido(nombreField.getText(), "nombre"));
            cliente.setTelefono(ValidationUtils.opcional(telefonoField.getText()));
            cliente.setEmail(ValidationUtils.opcional(emailField.getText()));
            cliente.setDireccion(ValidationUtils.opcional(direccionField.getText()));
            cliente.setActivo(true);
            validarCliente(cliente);

            resultado = cliente;
            cerrarVentana();
        } catch (IllegalArgumentException exception) {
            mostrarError("Datos invalidos", exception.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        resultado = null;
        cerrarVentana();
    }

    private Cliente copiarCliente(Cliente cliente) {
        Cliente copia = new Cliente();
        copia.setIdCliente(cliente.getIdCliente());
        copia.setIdentificacion(cliente.getIdentificacion());
        copia.setNombre(cliente.getNombre());
        copia.setTelefono(cliente.getTelefono());
        copia.setEmail(cliente.getEmail());
        copia.setDireccion(cliente.getDireccion());
        copia.setActivo(cliente.isActivo());
        return copia;
    }

    private void validarCliente(Cliente cliente) {
        ValidationUtils.requerido(cliente.getIdentificacion(), "identificacion");
        ValidationUtils.requerido(cliente.getNombre(), "nombre");
        if (!ValidationUtils.estaVacio(cliente.getEmail()) && !cliente.getEmail().contains("@")) {
            throw new IllegalArgumentException("El email debe tener un formato basico valido.");
        }
    }

    private void limpiarCampos() {
        identificacionField.clear();
        nombreField.clear();
        telefonoField.clear();
        emailField.clear();
        direccionField.clear();
    }

    private String valorSeguro(String texto) {
        return texto == null ? "" : texto;
    }

    private void cerrarVentana() {
        if (stage != null) {
            stage.close();
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
