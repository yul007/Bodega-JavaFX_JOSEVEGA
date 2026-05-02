package com.bodega.controller;

import com.bodega.dao.KardexDAO;
import com.bodega.dao.ProductoDAO;
import com.bodega.model.MovimientoKardex;
import com.bodega.model.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Controlador del Kardex valorado con filtros por producto y fecha. */
public class KardexController {

    @FXML private ComboBox<Producto> comboProducto;
    @FXML private DatePicker dateDesde;
    @FXML private DatePicker dateHasta;
    @FXML private TableView<MovimientoKardex> kardexTable;
    @FXML private TableColumn<MovimientoKardex, LocalDate> colFecha;
    @FXML private TableColumn<MovimientoKardex, String> colTipo;
    @FXML private TableColumn<MovimientoKardex, BigDecimal> colCantidad;
    @FXML private TableColumn<MovimientoKardex, BigDecimal> colCostoUnitario;
    @FXML private TableColumn<MovimientoKardex, BigDecimal> colSaldoCantidad;
    @FXML private TableColumn<MovimientoKardex, BigDecimal> colSaldoValor;
    @FXML private TableColumn<MovimientoKardex, String> colReferencia;
    @FXML private Label lblResumenKardex;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final KardexDAO kardexDAO = new KardexDAO();
    private final ObservableList<MovimientoKardex> movimientos = FXCollections.observableArrayList();

    @FXML private void initialize() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colCantidad.setCellValueFactory(cell -> {
            MovimientoKardex movimiento = cell.getValue();
            BigDecimal valor = valorMovimiento(movimiento);
            return new javafx.beans.property.SimpleObjectProperty<>(valor);
        });
        colCostoUnitario.setCellValueFactory(cell -> {
            MovimientoKardex movimiento = cell.getValue();
            BigDecimal valor = movimiento.getCostoUnitarioEntrada() != null
                    && movimiento.getCostoUnitarioEntrada().compareTo(BigDecimal.ZERO) > 0
                    ? movimiento.getCostoUnitarioEntrada()
                    : movimiento.getCostoUnitarioSalida();
            return new javafx.beans.property.SimpleObjectProperty<>(valor);
        });
        colSaldoCantidad.setCellValueFactory(new PropertyValueFactory<>("saldoCantidad"));
        colSaldoValor.setCellValueFactory(new PropertyValueFactory<>("saldoValor"));
        colReferencia.setCellValueFactory(new PropertyValueFactory<>("referencia"));

        kardexTable.setItems(movimientos);
        cargarProductos();
        dateDesde.setValue(LocalDate.now().minusDays(30));
        dateHasta.setValue(LocalDate.now());
        cargarKardex();
    }

    @FXML private void onFiltrar() {
        cargarKardex();
    }

    @FXML private void onLimpiar() {
        comboProducto.setValue(null);
        dateDesde.setValue(LocalDate.now().minusDays(30));
        dateHasta.setValue(LocalDate.now());
        cargarKardex();
    }



    private void cargarProductos() {
        try {
            comboProducto.setItems(FXCollections.observableArrayList(productoDAO.listarActivos()));
        } catch (SQLException e) {
            mostrarError("No se pudieron cargar los productos: " + e.getMessage());
        }
    }

    private void cargarKardex() {
        try {
            Producto producto = comboProducto.getValue();
            if (producto == null) {
                movimientos.clear();
                lblResumenKardex.setText("Seleccione un producto para ver su Kardex.");
                return;
            }

            LocalDate desde = dateDesde.getValue();
            LocalDate hasta = dateHasta.getValue();
            List<MovimientoKardex> datos = (desde != null && hasta != null)
                    ? kardexDAO.listarPorProductoYFechas(producto.getIdProducto(), desde, hasta)
                    : kardexDAO.listarPorProducto(producto.getIdProducto());

            movimientos.setAll(datos);
            lblResumenKardex.setText("Movimientos: " + movimientos.size() + " | Producto: " + producto.getNombre());
        } catch (SQLException e) {
            mostrarError("No se pudo cargar el Kardex: " + e.getMessage());
        }
    }


    private BigDecimal valorMovimiento(MovimientoKardex movimiento) {
        if (movimiento.getCantidadEntrada() != null && movimiento.getCantidadEntrada().compareTo(BigDecimal.ZERO) > 0) {
            return movimiento.getCantidadEntrada();
        }
        return movimiento.getCantidadSalida() == null ? BigDecimal.ZERO : movimiento.getCantidadSalida();
    }

    private BigDecimal costoUnitario(MovimientoKardex movimiento) {
        if (movimiento.getCostoUnitarioEntrada() != null
                && movimiento.getCostoUnitarioEntrada().compareTo(BigDecimal.ZERO) > 0) {
            return movimiento.getCostoUnitarioEntrada();
        }
        return movimiento.getCostoUnitarioSalida() == null ? BigDecimal.ZERO : movimiento.getCostoUnitarioSalida();
    }

    private String valorTexto(BigDecimal valor) {
        return valor == null ? "0.00" : valor.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bodega Master");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Bodega Master");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
