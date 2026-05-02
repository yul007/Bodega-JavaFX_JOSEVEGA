package com.bodega.controller;

import com.bodega.dao.ClienteDAO;
import com.bodega.model.Cliente;
import com.bodega.util.ValidationUtils;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/** Controlador de clientes: crear, listar, editar, buscar e inactivar. */
public class ClienteController {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ObservableList<Cliente> clientes = FXCollections.observableArrayList();

    @FXML private TextField buscadorField;
    @FXML private TableView<Cliente> clientesTable;
    @FXML private TableColumn<Cliente, Number> idColumn;
    @FXML private TableColumn<Cliente, String> identificacionColumn;
    @FXML private TableColumn<Cliente, String> nombreColumn;
    @FXML private TableColumn<Cliente, String> telefonoColumn;
    @FXML private TableColumn<Cliente, String> emailColumn;
    @FXML private TableColumn<Cliente, String> direccionColumn;
    @FXML private Label resumenLabel;
    
    @FXML private void initialize() {
        configurarTabla();
        cargarClientes();
    }

    @FXML private void nuevoCliente() {
        abrirDialogoCliente(null).ifPresent(cliente -> {
            try {
                clienteDAO.crear(cliente);
                cargarClientes();
                mostrarInformacion("Cliente creado", "El cliente se guardo correctamente.");
            } catch (SQLException exception) {
                mostrarError("No se pudo crear el cliente", exception.getMessage());
            }
        });
    }

    @FXML private void buscarCliente() {
        String texto = buscadorField.getText() == null ? "" : buscadorField.getText().trim();
        try {
            List<Cliente> resultado = texto.isEmpty()
                    ? clienteDAO.listarActivos()
                    : clienteDAO.buscarPorNombreOIdentificacion(texto);
            clientes.setAll(resultado);
            actualizarResumen();
        } catch (SQLException exception) {
            mostrarError("No se pudo buscar clientes", exception.getMessage());
        }
    }

    @FXML private void editarCliente() {
        Cliente seleccionado = obtenerSeleccionado();
        if (seleccionado == null) {
            return;
        }

        abrirDialogoCliente(seleccionado).ifPresent(cliente -> {
            try {
                clienteDAO.actualizar(cliente);
                cargarClientes();
                mostrarInformacion("Cliente actualizado", "Los cambios se guardaron correctamente.");
            } catch (SQLException exception) {
                mostrarError("No se pudo actualizar el cliente", exception.getMessage());
            }
        });
    }

    @FXML private void inactivarCliente() {
        Cliente seleccionado = obtenerSeleccionado();
        if (seleccionado == null) {
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Bodega Master");
        confirmacion.setHeaderText("Inactivar cliente");
        confirmacion.setContentText("Se inactivara " + seleccionado.getNombre() + ".");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            clienteDAO.inactivar(seleccionado.getIdCliente());
            cargarClientes();
            mostrarInformacion("Cliente inactivado", "El cliente ya no aparece como activo.");
        } catch (SQLException exception) {
            mostrarError("No se pudo inactivar el cliente", exception.getMessage());
        }
    }

    @FXML private void listarClientes() {
        buscadorField.clear();
        cargarClientes();
    }

    private void configurarTabla() {
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIdCliente()));
        identificacionColumn.setCellValueFactory(data -> new SimpleStringProperty(valorSeguro(data.getValue().getIdentificacion())));
        nombreColumn.setCellValueFactory(data -> new SimpleStringProperty(valorSeguro(data.getValue().getNombre())));
        telefonoColumn.setCellValueFactory(data -> new SimpleStringProperty(valorSeguro(data.getValue().getTelefono())));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(valorSeguro(data.getValue().getEmail())));
        direccionColumn.setCellValueFactory(data -> new SimpleStringProperty(valorSeguro(data.getValue().getDireccion())));
        clientesTable.setItems(clientes);
    }

    private void cargarClientes() {
        try {
            clientes.setAll(clienteDAO.listarActivos());
            actualizarResumen();
        } catch (SQLException exception) {
            mostrarError("No se pudo cargar clientes", exception.getMessage());
        }
    }

    private Optional<Cliente> abrirDialogoCliente(Cliente existente) {
        Dialog<Cliente> dialog = new Dialog<>();
        dialog.setTitle(existente == null ? "Nuevo cliente" : "Editar cliente");
        dialog.setHeaderText(existente == null ? "Registrar cliente" : "Actualizar cliente");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField identificacionField = new TextField();
        TextField nombreField = new TextField();
        TextField telefonoField = new TextField();
        TextField emailField = new TextField();
        TextField direccionField = new TextField();

        if (existente != null) {
            identificacionField.setText(existente.getIdentificacion());
            nombreField.setText(existente.getNombre());
            telefonoField.setText(existente.getTelefono());
            emailField.setText(existente.getEmail());
            direccionField.setText(existente.getDireccion());
        }

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(12));
        form.addRow(0, new Label("Identificacion"), identificacionField);
        form.addRow(1, new Label("Nombre"), nombreField);
        form.addRow(2, new Label("Telefono"), telefonoField);
        form.addRow(3, new Label("Email"), emailField);
        form.addRow(4, new Label("Direccion"), direccionField);
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }

            Cliente cliente = existente == null ? new Cliente() : existente;
            cliente.setIdentificacion(ValidationUtils.requerido(identificacionField.getText(), "identificacion"));
            cliente.setNombre(ValidationUtils.requerido(nombreField.getText(), "nombre"));
            cliente.setTelefono(ValidationUtils.opcional(telefonoField.getText()));
            cliente.setEmail(ValidationUtils.opcional(emailField.getText()));
            cliente.setDireccion(ValidationUtils.opcional(direccionField.getText()));
            cliente.setActivo(true);
            validarCliente(cliente);
            return cliente;
        });

        try {
            return dialog.showAndWait();
        } catch (IllegalArgumentException exception) {
            mostrarError("Datos invalidos", exception.getMessage());
            return Optional.empty();
        }
    }

    private Cliente obtenerSeleccionado() {
        Cliente seleccionado = clientesTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Seleccion requerida", "Selecciona un cliente de la tabla.");
        }
        return seleccionado;
    }

    private void validarCliente(Cliente cliente) {
        ValidationUtils.requerido(cliente.getIdentificacion(), "identificacion");
        ValidationUtils.requerido(cliente.getNombre(), "nombre");
        if (!ValidationUtils.estaVacio(cliente.getEmail()) && !cliente.getEmail().contains("@")) {
            throw new IllegalArgumentException("El email debe tener un formato basico valido.");
        }
    }

    private void actualizarResumen() {
        resumenLabel.setText("Clientes activos: " + clientes.size());
    }

    private String valorSeguro(String texto) {
        return texto == null ? "" : texto;
    }

    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bodega Master");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Bodega Master");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
