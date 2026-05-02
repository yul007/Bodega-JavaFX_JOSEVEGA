package com.bodega.service;

import com.bodega.dao.KardexDAO;
import com.bodega.dao.NotaSalidaDAO;
import com.bodega.model.DetalleFIFO;
import com.bodega.model.DetalleSalida;
import com.bodega.model.MovimientoKardex;
import com.bodega.model.NotaSalida;
import com.bodega.model.ResultadoFIFO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Registra ventas completas con detalle, FIFO, stock y Kardex en una transaccion. */
public class NotaSalidaService {

    private static final int MONEY_SCALE = 2;
    private static final BigDecimal IVA_RATE = new BigDecimal("0.12");

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bodega_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "holacomo";
    private final NotaSalidaDAO notaSalidaDAO;
    private final KardexDAO kardexDAO;
    private final FIFOInventoryService fifoInventoryService;

    public NotaSalidaService() {
        this.notaSalidaDAO = new NotaSalidaDAO();
        this.kardexDAO = new KardexDAO();
        this.fifoInventoryService = new FIFOInventoryService();
    }

    public NotaSalida crearNotaSalida(NotaSalida notaSalida, List<DetalleSalida> detalles) throws SQLException {
        validarNotaSalida(notaSalida, detalles);

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            boolean autoCommitOriginal = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                NotaSalida notaGuardada = crearNotaSalida(connection, notaSalida, detalles);
                connection.commit();
                return notaGuardada;
            } catch (SQLException | RuntimeException exception) {
                rollback(connection);
                throw exception;
            } finally {
                connection.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    public NotaSalida crearNotaSalida(Connection connection, NotaSalida notaSalida,
            List<DetalleSalida> detalles) throws SQLException {
        validarNotaSalida(notaSalida, detalles);

        BigDecimal subtotalVenta = calcularSubtotalVenta(detalles);
        BigDecimal iva = subtotalVenta.multiply(IVA_RATE).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal total = subtotalVenta.add(iva).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        notaSalida.setFechaEmision(notaSalida.getFechaEmision() == null ? LocalDate.now() : notaSalida.getFechaEmision());
        notaSalida.setSubtotal(subtotalVenta);
        notaSalida.setIva(iva);
        notaSalida.setTotal(total);
        notaSalida.setCostoTotalFifo(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        notaSalida.setUtilidad(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        notaSalida.setEstado("completada");

        int idSalida = notaSalidaDAO.crear(connection, notaSalida);
        notaSalida.setIdSalida(idSalida);

        BigDecimal costoTotalFifo = BigDecimal.ZERO;

        for (DetalleSalida detalleSolicitado : detalles) {
            validarDetalle(detalleSolicitado);

            BigDecimal cantidad = valorSeguro(detalleSolicitado.getCantidad());
            BigDecimal precioUnitario = valorSeguro(detalleSolicitado.getPrecioUnitario())
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            int idProducto = detalleSolicitado.getProducto().getIdProducto();

            ResultadoFIFO resultadoFIFO = fifoInventoryService.aplicarSalidaFIFO(connection, idProducto, cantidad);
            costoTotalFifo = costoTotalFifo.add(resultadoFIFO.getCostoTotal());

            registrarDetallesYKardex(connection, notaSalida, resultadoFIFO, precioUnitario);
        }

        costoTotalFifo = costoTotalFifo.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        notaSalida.setCostoTotalFifo(costoTotalFifo);
        notaSalida.setUtilidad(subtotalVenta.subtract(costoTotalFifo).setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        notaSalidaDAO.actualizarTotales(connection, notaSalida);

        return notaSalida;
    }

    private void registrarDetallesYKardex(Connection connection, NotaSalida notaSalida,
            ResultadoFIFO resultadoFIFO, BigDecimal precioUnitario) throws SQLException {
        BigDecimal valorInventarioActualProducto =
                kardexDAO.calcularValorInventarioProducto(connection, resultadoFIFO.getProducto().getIdProducto());

        for (DetalleFIFO detalleFIFO : resultadoFIFO.getDetalles()) {
            BigDecimal cantidadConsumida = detalleFIFO.getCantidadConsumida();
            BigDecimal subtotalLinea = cantidadConsumida.multiply(precioUnitario)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            BigDecimal costoLinea = detalleFIFO.getCostoTotal().setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            BigDecimal utilidadLinea = subtotalLinea.subtract(costoLinea).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            DetalleSalida detalleSalida = new DetalleSalida(
                    0,
                    notaSalida,
                    resultadoFIFO.getProducto(),
                    detalleFIFO.getLote(),
                    cantidadConsumida,
                    precioUnitario,
                    subtotalLinea,
                    detalleFIFO.getCostoUnitario(),
                    costoLinea,
                    utilidadLinea);

            int idDetalle = notaSalidaDAO.crearDetalle(connection, detalleSalida);
            detalleSalida.setIdDetalleSalida(idDetalle);

            registrarMovimientoSalida(connection, notaSalida, resultadoFIFO, detalleFIFO,
                    valorInventarioActualProducto);
        }
    }

    private void registrarMovimientoSalida(Connection connection, NotaSalida notaSalida,
            ResultadoFIFO resultadoFIFO, DetalleFIFO detalleFIFO,
            BigDecimal valorInventarioActualProducto) throws SQLException {
        MovimientoKardex saldoAnterior = obtenerSaldoAnterior(connection, resultadoFIFO,
                valorInventarioActualProducto);

        BigDecimal saldoCantidad = valorSeguro(saldoAnterior.getSaldoCantidad())
                .subtract(detalleFIFO.getCantidadConsumida());
        BigDecimal saldoValor = valorSeguro(saldoAnterior.getSaldoValor())
                .subtract(detalleFIFO.getCostoTotal())
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        if (saldoCantidad.compareTo(BigDecimal.ZERO) < 0 || saldoValor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("El movimiento de salida generaria saldos negativos en Kardex.");
        }

        MovimientoKardex movimiento = new MovimientoKardex(
                0,
                resultadoFIFO.getProducto(),
                detalleFIFO.getLote(),
                notaSalida,
                notaSalida.getFechaEmision(),
                "SALIDA",
                notaSalida.getNumeroFactura(),
                BigDecimal.ZERO,
                null,
                BigDecimal.ZERO,
                detalleFIFO.getCantidadConsumida(),
                detalleFIFO.getCostoUnitario(),
                detalleFIFO.getCostoTotal(),
                saldoCantidad,
                saldoValor,
                "Salida de mercancia por venta FIFO");

        kardexDAO.crear(connection, movimiento);
    }

    private MovimientoKardex obtenerSaldoAnterior(Connection connection, ResultadoFIFO resultadoFIFO,
            BigDecimal valorInventarioActualProducto) throws SQLException {
        Optional<MovimientoKardex> ultimoMovimiento =
                kardexDAO.obtenerUltimoMovimiento(connection, resultadoFIFO.getProducto().getIdProducto());
        if (ultimoMovimiento.isPresent()) {
            return ultimoMovimiento.get();
        }

        MovimientoKardex saldoInicial = new MovimientoKardex();
        saldoInicial.setSaldoCantidad(resultadoFIFO.getStockAnterior());
        saldoInicial.setSaldoValor(valorInventarioActualProducto.add(resultadoFIFO.getCostoTotal())
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        return saldoInicial;
    }

    private BigDecimal calcularSubtotalVenta(List<DetalleSalida> detalles) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleSalida detalle : detalles) {
            validarDetalle(detalle);
            BigDecimal subtotalLinea = valorSeguro(detalle.getCantidad())
                    .multiply(valorSeguro(detalle.getPrecioUnitario()))
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            subtotal = subtotal.add(subtotalLinea);
        }
        return subtotal.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private void validarNotaSalida(NotaSalida notaSalida, List<DetalleSalida> detalles) {
        if (notaSalida == null) {
            throw new IllegalArgumentException("La nota de salida no puede ser nula.");
        }
        if (notaSalida.getCliente() == null || notaSalida.getCliente().getIdCliente() <= 0) {
            throw new IllegalArgumentException("La nota de salida debe tener un cliente valido.");
        }
        if (estaVacio(notaSalida.getNumeroFactura())) {
            throw new IllegalArgumentException("El numero de factura es obligatorio.");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La nota de salida debe tener al menos un detalle.");
        }
    }

    private void validarDetalle(DetalleSalida detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle de venta no puede ser nulo.");
        }
        if (detalle.getProducto() == null || detalle.getProducto().getIdProducto() <= 0) {
            throw new IllegalArgumentException("Cada detalle debe tener un producto valido.");
        }
        if (valorSeguro(detalle.getCantidad()).compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad vendida debe ser mayor que cero.");
        }
        if (valorSeguro(detalle.getPrecioUnitario()).compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo.");
        }
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Se conserva la excepcion original de la transaccion.
        }
    }
}
