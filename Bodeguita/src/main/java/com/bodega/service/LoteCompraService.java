package com.bodega.service;

import com.bodega.dao.KardexDAO;
import com.bodega.dao.LoteDAO;
import com.bodega.dao.ProductoDAO;
import com.bodega.model.Lote;
import com.bodega.model.MovimientoKardex;
import com.bodega.model.Producto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

/** Registra entradas de mercancia y mantiene stock, lote y Kardex sincronizados. */
public class LoteCompraService {

    private static final int MONEY_SCALE = 2;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bodega_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "holacomo";
    private final ProductoDAO productoDAO;
    private final LoteDAO loteDAO;
    private final KardexDAO kardexDAO;

    public LoteCompraService() {
        this.productoDAO = new ProductoDAO();
        this.loteDAO = new LoteDAO();
        this.kardexDAO = new KardexDAO();
    }

    public Lote registrarLoteCompra(Lote lote) throws SQLException {
        validarLote(lote);

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            boolean autoCommitOriginal = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                Lote loteGuardado = registrarLoteCompra(connection, lote);
                connection.commit();
                return loteGuardado;
            } catch (SQLException | RuntimeException exception) {
                rollback(connection);
                throw exception;
            } finally {
                connection.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    public Lote registrarLoteCompra(Connection connection, Lote lote) throws SQLException {
        validarLote(lote);

        int idProducto = lote.getProducto().getIdProducto();
        Producto producto = productoDAO.buscarPorIdParaActualizar(connection, idProducto)
                .orElseThrow(() -> new IllegalArgumentException("No existe el producto con ID " + idProducto + "."));

        if (!producto.isActivo()) {
            throw new IllegalStateException("No se puede registrar compra para un producto inactivo: "
                    + producto.getNombre());
        }

        BigDecimal cantidad = valorSeguro(lote.getCantidad());
        BigDecimal costoUnitario = valorSeguro(lote.getCostoUnitario()).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal stockAnterior = valorSeguro(producto.getStockActual());
        BigDecimal stockNuevo = stockAnterior.add(cantidad);

        lote.setProducto(producto);
        lote.setCantidad(cantidad);
        lote.setCantidadDisponible(cantidad);
        lote.setCostoUnitario(costoUnitario);
        lote.setActivo(true);
        if (lote.getFechaIngreso() == null) {
            lote.setFechaIngreso(LocalDate.now());
        }

        int idLote = loteDAO.crear(connection, lote);
        lote.setIdLote(idLote);

        productoDAO.actualizarStock(connection, idProducto, stockNuevo);
        producto.setStockActual(stockNuevo);

        MovimientoKardex ultimoMovimiento = obtenerUltimoMovimiento(producto, stockAnterior, connection);
        BigDecimal valorEntrada = cantidad.multiply(costoUnitario).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal saldoCantidad = valorSeguro(ultimoMovimiento.getSaldoCantidad()).add(cantidad);
        BigDecimal saldoValor = valorSeguro(ultimoMovimiento.getSaldoValor()).add(valorEntrada)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        MovimientoKardex entrada = new MovimientoKardex(
                0,
                producto,
                lote,
                null,
                lote.getFechaIngreso(),
                "ENTRADA",
                obtenerReferencia(lote),
                cantidad,
                costoUnitario,
                valorEntrada,
                BigDecimal.ZERO,
                null,
                BigDecimal.ZERO,
                saldoCantidad,
                saldoValor,
                "Entrada de mercancia por compra");

        int idMovimiento = registrarMovimientoKardexEntrada(connection, entrada);
        entrada.setIdMovimiento(idMovimiento);

        return lote;
    }

    private MovimientoKardex obtenerUltimoMovimiento(Producto producto, BigDecimal stockAnterior,
            Connection connection) throws SQLException {
        Optional<MovimientoKardex> ultimoMovimiento =
                kardexDAO.obtenerUltimoMovimiento(connection, producto.getIdProducto());
        if (ultimoMovimiento.isPresent()) {
            return ultimoMovimiento.get();
        }

        MovimientoKardex movimientoInicial = new MovimientoKardex();
        movimientoInicial.setSaldoCantidad(stockAnterior);
        movimientoInicial.setSaldoValor(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        return movimientoInicial;
    }

    private void validarLote(Lote lote) {
        if (lote == null) {
            throw new IllegalArgumentException("El lote no puede ser nulo.");
        }
        if (lote.getProducto() == null || lote.getProducto().getIdProducto() <= 0) {
            throw new IllegalArgumentException("El lote debe tener un producto valido.");
        }
        if (lote.getProveedor() == null || lote.getProveedor().getIdProveedor() <= 0) {
            throw new IllegalArgumentException("El lote debe tener un proveedor valido.");
        }
        if (estaVacio(lote.getCodigoLote())) {
            throw new IllegalArgumentException("El codigo de lote es obligatorio.");
        }
        if (valorSeguro(lote.getCantidad()).compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad del lote debe ser mayor que cero.");
        }
        if (valorSeguro(lote.getCostoUnitario()).compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El costo unitario debe ser mayor que cero.");
        }
    }

    private String obtenerReferencia(Lote lote) {
        if (!estaVacio(lote.getFacturaReferencia())) {
            return lote.getFacturaReferencia();
        }
        return lote.getCodigoLote();
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private int registrarMovimientoKardexEntrada(Connection connection, MovimientoKardex movimiento)
            throws SQLException {
        return kardexDAO.crear(connection, movimiento);
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Se conserva la excepcion original de la operacion.
        }
    }
}
