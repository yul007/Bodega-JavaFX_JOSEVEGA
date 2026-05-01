package com.bodega.service;

import com.bodega.dao.LoteDAO;
import com.bodega.dao.ProductoDAO;
import com.bodega.db.DatabaseConnection;
import com.bodega.model.DetalleFIFO;
import com.bodega.model.Lote;
import com.bodega.model.Producto;
import com.bodega.model.ResultadoFIFO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Aplica salidas de inventario usando FIFO con transacciones JDBC. */
public class FIFOInventoryService {

    private static final int MONEY_SCALE = 2;

    private final DatabaseConnection databaseConnection;
    private final ProductoDAO productoDAO;
    private final LoteDAO loteDAO;

    public FIFOInventoryService() {
        this.databaseConnection = DatabaseConnection.getInstance();
        this.productoDAO = new ProductoDAO();
        this.loteDAO = new LoteDAO();
    }

    public ResultadoFIFO aplicarSalidaFIFO(int idProducto, BigDecimal cantidadSolicitada) throws SQLException {
        validarCantidad(cantidadSolicitada);

        try (Connection connection = databaseConnection.getConnection()) {
            boolean autoCommitOriginal = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                ResultadoFIFO resultado = aplicarSalidaFIFO(connection, idProducto, cantidadSolicitada);
                connection.commit();
                return resultado;
            } catch (SQLException | RuntimeException exception) {
                rollback(connection);
                throw exception;
            } finally {
                connection.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    public ResultadoFIFO aplicarSalidaFIFO(Connection connection, int idProducto,
            BigDecimal cantidadSolicitada) throws SQLException {
        validarCantidad(cantidadSolicitada);

        Producto producto = productoDAO.buscarPorIdParaActualizar(connection, idProducto)
                .orElseThrow(() -> new IllegalArgumentException("No existe el producto con ID " + idProducto + "."));

        if (!producto.isActivo()) {
            throw new IllegalStateException("El producto esta inactivo y no puede venderse: " + producto.getNombre());
        }

        BigDecimal stockAnterior = valorSeguro(producto.getStockActual());
        if (stockAnterior.compareTo(cantidadSolicitada) < 0) {
            throw new IllegalStateException("Stock insuficiente para " + producto.getNombre()
                    + ". Disponible: " + stockAnterior + ", solicitado: " + cantidadSolicitada + ".");
        }

        List<Lote> lotesDisponibles = loteDAO.listarDisponiblesFIFO(connection, idProducto);
        BigDecimal cantidadPendiente = cantidadSolicitada;
        BigDecimal costoTotal = BigDecimal.ZERO;
        List<DetalleFIFO> detalles = new ArrayList<>();

        for (Lote lote : lotesDisponibles) {
            if (cantidadPendiente.compareTo(BigDecimal.ZERO) == 0) {
                break;
            }

            BigDecimal disponible = valorSeguro(lote.getCantidadDisponible());
            BigDecimal cantidadConsumida = menor(cantidadPendiente, disponible);
            BigDecimal nuevaCantidadDisponible = disponible.subtract(cantidadConsumida);
            BigDecimal costoLinea = calcularCostoLinea(cantidadConsumida, lote.getCostoUnitario());

            loteDAO.actualizarCantidadDisponible(connection, lote.getIdLote(), nuevaCantidadDisponible);

            detalles.add(new DetalleFIFO(
                    lote,
                    cantidadConsumida,
                    lote.getCostoUnitario(),
                    costoLinea,
                    nuevaCantidadDisponible));

            costoTotal = costoTotal.add(costoLinea);
            cantidadPendiente = cantidadPendiente.subtract(cantidadConsumida);
        }

        if (cantidadPendiente.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Los lotes disponibles no cubren la cantidad solicitada. "
                    + "Faltan " + cantidadPendiente + " unidades.");
        }

        BigDecimal stockNuevo = stockAnterior.subtract(cantidadSolicitada);
        productoDAO.actualizarStock(connection, idProducto, stockNuevo);

        producto.setStockActual(stockNuevo);
        return new ResultadoFIFO(
                producto,
                cantidadSolicitada,
                costoTotal.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                stockAnterior,
                stockNuevo,
                detalles);
    }

    private void validarCantidad(BigDecimal cantidadSolicitada) {
        if (cantidadSolicitada == null || cantidadSolicitada.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad solicitada debe ser mayor que cero.");
        }
    }

    private BigDecimal calcularCostoLinea(BigDecimal cantidad, BigDecimal costoUnitario) {
        return cantidad.multiply(valorSeguro(costoUnitario)).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal menor(BigDecimal primero, BigDecimal segundo) {
        return primero.compareTo(segundo) <= 0 ? primero : segundo;
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Si rollback falla, se conserva la excepcion original.
        }
    }
}
