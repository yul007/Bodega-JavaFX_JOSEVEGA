//JAAN
package com.bodega.controller;

import com.bodega.model.Proveedor;
import com.bodega.util.ValidationUtils;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/** Controlador del formulario modal de proveedores. */
public class ProveedorFormController {

    @FXML private TextField txtRuc;
    @FXML private TextField txtNombre;
    @FXML private TextField txtContacto;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtDireccion;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private Stage stage;
    private Proveedor proveedorOriginal;
    private Proveedor resultado;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedorOriginal = proveedor;
        if (proveedor == null) {
            limpiarCampos();
            return;
        }

        txtRuc.setText(valorSeguro(proveedor.getRuc()));
        txtNombre.setText(valorSeguro(proveedor.getNombre()));
        txtContacto.setText(valorSeguro(proveedor.getContacto()));
        txtTelefono.setText(valorSeguro(proveedor.getTelefono()));
        txtEmail.setText(valorSeguro(proveedor.getEmail()));
        txtDireccion.setText(valorSeguro(proveedor.getDireccion()));
    }

    public Optional<Proveedor> getResultado() {
        return Optional.ofNullable(resultado);
    }

    @FXML
    private void onGuardar() {
        try {
            String ruc = ValidationUtils.requerido(txtRuc.getText(), "RUC");
            String nombre = ValidationUtils.requerido(txtNombre.getText(), "nombre");
            String contacto = ValidationUtils.opcional(txtContacto.getText());
            String telefono = ValidationUtils.opcional(txtTelefono.getText());
            String email = ValidationUtils.opcional(txtEmail.getText());
            String direccion = ValidationUtils.opcional(txtDireccion.getText());

            validarEmail(email);

            Proveedor proveedor = proveedorOriginal == null
                    ? new Proveedor()
                    : copiarProveedor(proveedorOriginal);

            proveedor.setRuc(ruc);
            proveedor.setNombre(nombre);
            proveedor.setContacto(contacto);
            proveedor.setTelefono(telefono);
            proveedor.setEmail(email);
            proveedor.setDireccion(direccion);
            proveedor.setActivo(true);
            validarProveedor(proveedor);

            resultado = proveedor;
            cerrarVentana();
        } catch (IllegalArgumentException exception) {
            mostrarError("Datos inválidos", exception.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        resultado = null;
        cerrarVentana();
    }

    private void validarProveedor(Proveedor proveedor) {
        ValidationUtils.requerido(proveedor.getRuc(), "RUC");
        ValidationUtils.requerido(proveedor.getNombre(), "nombre");
    }

    private void validarEmail(String email) {
        if (!ValidationUtils.estaVacio(email) && !email.contains("@")) {
            throw new IllegalArgumentException("El email debe tener un formato basico valido.");
        }
    }

    private Proveedor copiarProveedor(Proveedor proveedor) {
        Proveedor copia = new Proveedor();
        copia.setIdProveedor(proveedor.getIdProveedor());
        copia.setRuc(proveedor.getRuc());
        copia.setNombre(proveedor.getNombre());
        copia.setContacto(proveedor.getContacto());
        copia.setTelefono(proveedor.getTelefono());
        copia.setEmail(proveedor.getEmail());
        copia.setDireccion(proveedor.getDireccion());
        copia.setActivo(proveedor.isActivo());
        return copia;
    }

    private void limpiarCampos() {
        txtRuc.clear();
        txtNombre.clear();
        txtContacto.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtDireccion.clear();
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
