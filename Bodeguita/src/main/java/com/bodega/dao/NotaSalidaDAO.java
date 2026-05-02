package com.bodega.dao;

import com.bodega.db.DatabaseConnection;
import com.bodega.model.Cliente;
import com.bodega.model.DetalleSalida;
import com.bodega.model.Lote;
import com.bodega.model.NotaSalida;
import com.bodega.model.Producto;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO para notas de salida, ventas y detalles asociados. */
public class NotaSalidaDAO {

    private static final String SELECT_NOTA_CON_CLIENTE = """
            SELECT ns.id_salida, ns.numero_factura, ns.fecha_emision, ns.subtotal,
                   ns.iva, ns.total, ns.costo_total_fifo, ns.utilidad, ns.estado,
                   ns.observaciones,
                   c.id_cliente, c.identificacion, c.nombre AS cliente_nombre,
                   c.telefono, c.email, c.direccion, c.activo AS cliente_activo
            FROM nota_salida ns
            JOIN cliente c ON c.id_cliente = ns.id_cliente
            """;

    private final DatabaseConnection databaseConnection;

    public NotaSalidaDAO() {
        this.databaseConnection = DatabaseConnection.getInstance();
    }

    public int crear(NotaSalida notaSalida) throws SQLException {
        try (Connection connection = databaseConnection.getConnection()) {
            return crear(connection, notaSalida);
        }
    }

    public int crear(Connection connection, NotaSalida notaSalida) throws SQLException {
        String sql = """
                INSERT INTO nota_salida (
                  id_cliente, numero_factura, fecha_emision, subtotal, iva, total,
                  costo_total_fifo, utilidad, estado, observaciones
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, notaSalida.getCliente().getIdCliente());
            statement.setString(2, notaSalida.getNumeroFactura());
            statement.setDate(3, Date.valueOf(notaSalida.getFechaEmision()));
            statement.setBigDecimal(4, notaSalida.getSubtotal());
            statement.setBigDecimal(5, notaSalida.getIva());
            statement.setBigDecimal(6, notaSalida.getTotal());
            statement.setBigDecimal(7, notaSalida.getCostoTotalFifo());
            statement.setBigDecimal(8, notaSalida.getUtilidad());
            statement.setString(9, notaSalida.getEstado());
            statement.setString(10, notaSalida.getObservaciones());
            statement.executeUpdate();
            return leerIdGenerado(statement);
        }
    }

    public int crearDetalle(Connection connection, DetalleSalida detalle) throws SQLException {
        String sql = """
                INSERT INTO detalle_salida (
                  id_salida, id_producto, id_lote, cantidad, precio_unitario, subtotal,
                  costo_unitario_fifo, costo_total_fifo, utilidad
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, detalle.getNotaSalida().getIdSalida());
            statement.setInt(2, detalle.getProducto().getIdProducto());
            if (detalle.getLote() == null || detalle.getLote().getIdLote() <= 0) {
                statement.setNull(3, Types.INTEGER);
            } else {
                statement.setInt(3, detalle.getLote().getIdLote());
            }
            statement.setBigDecimal(4, detalle.getCantidad());
            statement.setBigDecimal(5, detalle.getPrecioUnitario());
            statement.setBigDecimal(6, detalle.getSubtotal());
            statement.setBigDecimal(7, detalle.getCostoUnitarioFifo());
            statement.setBigDecimal(8, detalle.getCostoTotalFifo());
            statement.setBigDecimal(9, detalle.getUtilidad());
            statement.executeUpdate();
            return leerIdGenerado(statement);
        }
    }

    public List<NotaSalida> listarTodas() throws SQLException {
        String sql = SELECT_NOTA_CON_CLIENTE + " ORDER BY ns.fecha_emision DESC, ns.id_salida DESC";
        return consultarNotas(sql);
    }

    public Optional<NotaSalida> buscarPorId(int idSalida) throws SQLException {
        String sql = SELECT_NOTA_CON_CLIENTE + " WHERE ns.id_salida = ?";

        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idSalida);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapearNotaSalida(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<NotaSalida> buscarPorNumeroFactura(String numeroFactura) throws SQLException {
        String sql = SELECT_NOTA_CON_CLIENTE + " WHERE ns.numero_factura = ?";

        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, numeroFactura);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapearNotaSalida(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<NotaSalida> listarPorPeriodo(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = SELECT_NOTA_CON_CLIENTE
                + """
                  WHERE ns.fecha_emision BETWEEN ? AND ?
                  ORDER BY ns.fecha_emision DESC, ns.id_salida DESC
                  """;

        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(desde));
            statement.setDate(2, Date.valueOf(hasta));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<NotaSalida> notas = new ArrayList<>();
                while (resultSet.next()) {
                    notas.add(mapearNotaSalida(resultSet));
                }
                return notas;
            }
        }
    }

    public List<DetalleSalida> listarDetallesPorSalida(int idSalida) throws SQLException {
        String sql = """
                SELECT ds.id_detalle_salida, ds.cantidad, ds.precio_unitario, ds.subtotal,
                       ds.costo_unitario_fifo, ds.costo_total_fifo, ds.utilidad,
                       p.id_producto, p.nombre AS producto_nombre, p.sku, p.codigo_barras,
                       l.id_lote, l.codigo_lote
                FROM detalle_salida ds
                JOIN producto p ON p.id_producto = ds.id_producto
                LEFT JOIN lote l ON l.id_lote = ds.id_lote
                WHERE ds.id_salida = ?
                ORDER BY ds.id_detalle_salida
                """;

        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idSalida);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<DetalleSalida> detalles = new ArrayList<>();
                while (resultSet.next()) {
                    detalles.add(mapearDetalleSalida(resultSet, idSalida));
                }
                return detalles;
            }
        }
    }

    public boolean actualizarEstado(int idSalida, String estado) throws SQLException {
        String sql = "UPDATE nota_salida SET estado = ? WHERE id_salida = ?";

        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, estado);
            statement.setInt(2, idSalida);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean actualizarTotales(Connection connection, NotaSalida notaSalida) throws SQLException {
        String sql = """
                UPDATE nota_salida
                SET subtotal = ?, iva = ?, total = ?, costo_total_fifo = ?, utilidad = ?, estado = ?
                WHERE id_salida = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, notaSalida.getSubtotal());
            statement.setBigDecimal(2, notaSalida.getIva());
            statement.setBigDecimal(3, notaSalida.getTotal());
            statement.setBigDecimal(4, notaSalida.getCostoTotalFifo());
            statement.setBigDecimal(5, notaSalida.getUtilidad());
            statement.setString(6, notaSalida.getEstado());
            statement.setInt(7, notaSalida.getIdSalida());
            return statement.executeUpdate() > 0;
        }
    }

    public List<Object[]> listarTopProductosVendidos(int limite) throws SQLException {
        String sql = """
                SELECT p.nombre AS producto_nombre, SUM(ds.cantidad) AS cantidad_total
                FROM detalle_salida ds
                JOIN producto p ON p.id_producto = ds.id_producto
                GROUP BY p.id_producto, p.nombre
                ORDER BY cantidad_total DESC, p.nombre
                LIMIT ?
                """;

        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limite);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Object[]> filas = new ArrayList<>();
                while (resultSet.next()) {
                    filas.add(new Object[] {
                            resultSet.getString("producto_nombre"),
                            resultSet.getBigDecimal("cantidad_total")
                    });
                }
                return filas;
            }
        }
    }

    public List<Object[]> listarVentasPorDiaUltimosDias(int dias) throws SQLException {
        String sql = """
                SELECT DATE(ns.fecha_emision) AS fecha_dia, COALESCE(SUM(ns.total), 0) AS total_dia
                FROM nota_salida ns
                WHERE ns.fecha_emision >= (CURDATE() - INTERVAL ? DAY)
                GROUP BY DATE(ns.fecha_emision)
                ORDER BY fecha_dia
                """;

        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, dias);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Object[]> filas = new ArrayList<>();
                while (resultSet.next()) {
                    filas.add(new Object[] {
                            resultSet.getDate("fecha_dia").toLocalDate(),
                            resultSet.getBigDecimal("total_dia")
                    });
                }
                return filas;
            }
        }
    }

    private List<NotaSalida> consultarNotas(String sql) throws SQLException {
        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            List<NotaSalida> notas = new ArrayList<>();
            while (resultSet.next()) {
                notas.add(mapearNotaSalida(resultSet));
            }
            return notas;
        }
    }

    private NotaSalida mapearNotaSalida(ResultSet resultSet) throws SQLException {
        Cliente cliente = new Cliente(
                resultSet.getInt("id_cliente"),
                resultSet.getString("identificacion"),
                resultSet.getString("cliente_nombre"),
                resultSet.getString("telefono"),
                resultSet.getString("email"),
                resultSet.getString("direccion"),
                resultSet.getBoolean("cliente_activo"));

        return new NotaSalida(
                resultSet.getInt("id_salida"),
                cliente,
                resultSet.getString("numero_factura"),
                resultSet.getDate("fecha_emision").toLocalDate(),
                resultSet.getBigDecimal("subtotal"),
                resultSet.getBigDecimal("iva"),
                resultSet.getBigDecimal("total"),
                resultSet.getBigDecimal("costo_total_fifo"),
                resultSet.getBigDecimal("utilidad"),
                resultSet.getString("estado"),
                resultSet.getString("observaciones"));
    }

    private DetalleSalida mapearDetalleSalida(ResultSet resultSet, int idSalida) throws SQLException {
        NotaSalida notaSalida = new NotaSalida();
        notaSalida.setIdSalida(idSalida);

        Producto producto = new Producto();
        producto.setIdProducto(resultSet.getInt("id_producto"));
        producto.setNombre(resultSet.getString("producto_nombre"));
        producto.setSku(resultSet.getString("sku"));
        producto.setCodigoBarras(resultSet.getString("codigo_barras"));

        Lote lote = null;
        int idLote = resultSet.getInt("id_lote");
        if (!resultSet.wasNull()) {
            lote = new Lote();
            lote.setIdLote(idLote);
            lote.setCodigoLote(resultSet.getString("codigo_lote"));
        }

        return new DetalleSalida(
                resultSet.getInt("id_detalle_salida"),
                notaSalida,
                producto,
                lote,
                resultSet.getBigDecimal("cantidad"),
                resultSet.getBigDecimal("precio_unitario"),
                resultSet.getBigDecimal("subtotal"),
                resultSet.getBigDecimal("costo_unitario_fifo"),
                resultSet.getBigDecimal("costo_total_fifo"),
                resultSet.getBigDecimal("utilidad"));
    }

    private int leerIdGenerado(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        throw new SQLException("No se pudo obtener el ID generado para la nota de salida.");
    }
}
