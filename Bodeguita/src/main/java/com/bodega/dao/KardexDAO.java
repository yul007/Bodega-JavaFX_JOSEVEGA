package com.bodega.dao;

import com.bodega.dao.JdbcDaoSupport.StatementConfigurer;
import com.bodega.model.Categoria;
import com.bodega.model.Lote;
import com.bodega.model.MovimientoKardex;
import com.bodega.model.NotaSalida;
import com.bodega.model.Producto;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** DAO para movimientos de Kardex valorado y consultas de inventario. */
public class KardexDAO extends JdbcDaoSupport {

    private static final String SELECT_KARDEX_RELACIONADO = """
            SELECT mk.id_movimiento, mk.id_producto, mk.id_lote, mk.id_salida, mk.fecha,
                   mk.tipo, mk.referencia, mk.cantidad_entrada, mk.costo_unitario_entrada,
                   mk.valor_entrada, mk.cantidad_salida, mk.costo_unitario_salida,
                   mk.valor_salida, mk.saldo_cantidad, mk.saldo_valor, mk.observacion,
                   p.id_categoria, p.sku, p.codigo_barras, p.nombre AS producto_nombre,
                   p.descripcion AS producto_descripcion, p.stock_minimo,
                   p.stock_actual, p.precio_venta, p.activo AS producto_activo,
                   c.nombre AS categoria_nombre, c.descripcion AS categoria_descripcion,
                   c.activo AS categoria_activo,
                   l.codigo_lote,
                   ns.numero_factura
            FROM movimiento_kardex mk
            JOIN producto p ON p.id_producto = mk.id_producto
            JOIN categoria c ON c.id_categoria = p.id_categoria
            LEFT JOIN lote l ON l.id_lote = mk.id_lote
            LEFT JOIN nota_salida ns ON ns.id_salida = mk.id_salida
            """;

    public int crear(MovimientoKardex movimiento) throws SQLException {
        try (Connection connection = abrirConexion()) {
            return crear(connection, movimiento);
        }
    }

    public int crear(Connection connection, MovimientoKardex movimiento) throws SQLException {
        String sql = """
                INSERT INTO movimiento_kardex (
                  id_producto, id_lote, id_salida, fecha, tipo, referencia,
                  cantidad_entrada, costo_unitario_entrada, valor_entrada,
                  cantidad_salida, costo_unitario_salida, valor_salida,
                  saldo_cantidad, saldo_valor, observacion
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, movimiento.getProducto().getIdProducto());
            setNullableInt(statement, 2, movimiento.getLote() == null ? 0 : movimiento.getLote().getIdLote());
            setNullableInt(statement, 3, movimiento.getNotaSalida() == null ? 0 : movimiento.getNotaSalida().getIdSalida());
            statement.setTimestamp(4, Timestamp.valueOf(toDateTime(movimiento.getFecha())));
            statement.setString(5, movimiento.getTipo());
            statement.setString(6, movimiento.getReferencia());
            statement.setBigDecimal(7, movimiento.getCantidadEntrada());
            statement.setBigDecimal(8, movimiento.getCostoUnitarioEntrada());
            statement.setBigDecimal(9, movimiento.getValorEntrada());
            statement.setBigDecimal(10, movimiento.getCantidadSalida());
            statement.setBigDecimal(11, movimiento.getCostoUnitarioSalida());
            statement.setBigDecimal(12, movimiento.getValorSalida());
            statement.setBigDecimal(13, movimiento.getSaldoCantidad());
            statement.setBigDecimal(14, movimiento.getSaldoValor());
            statement.setString(15, movimiento.getObservacion());
            statement.executeUpdate();
            return leerIdGenerado(statement, "No se pudo obtener el ID generado para el movimiento Kardex.");
        }
    }

    public List<MovimientoKardex> listarPorProducto(int idProducto) throws SQLException { //esto devuelve una lista de movimientos de kardex para el producto especificado por idProducto, luego convierte cada movimiento en un arreglo de String con los datos relevantes para el reporte, y finalmente recopila todos esos arreglos en una lista que se devuelve como resultado del método.
        return consultarMovimientos(" WHERE mk.id_producto = ? ORDER BY mk.fecha, mk.id_movimiento",
                statement -> statement.setInt(1, idProducto)); //.setInt hace que el primer parámetro (1) se reemplace por el valor de idProducto en la consulta SQL, lo que permite obtener los movimientos de Kardex relacionados con ese producto específico.
    }

    public List<MovimientoKardex> listarPorProductoYFechas(int idProducto,
            LocalDate desde, LocalDate hasta) throws SQLException {
        return consultarMovimientos("""
                WHERE mk.id_producto = ?
                  AND DATE(mk.fecha) BETWEEN ? AND ?
                ORDER BY mk.fecha, mk.id_movimiento
                """, statement -> {
            statement.setInt(1, idProducto);
            statement.setDate(2, java.sql.Date.valueOf(desde));
            statement.setDate(3, java.sql.Date.valueOf(hasta));
        });
    }

    public Optional<MovimientoKardex> obtenerUltimoMovimiento(Connection connection, int idProducto) throws SQLException {
        String sql = """
                SELECT id_movimiento, saldo_cantidad, saldo_valor
                FROM movimiento_kardex
                WHERE id_producto = ?
                ORDER BY fecha DESC, id_movimiento DESC
                LIMIT 1
                FOR UPDATE
                """;

        return consultarUno(connection, sql, statement -> statement.setInt(1, idProducto),
                resultSet -> {
                    MovimientoKardex movimiento = new MovimientoKardex();
                    movimiento.setIdMovimiento(resultSet.getInt("id_movimiento"));
                    movimiento.setSaldoCantidad(resultSet.getBigDecimal("saldo_cantidad"));
                    movimiento.setSaldoValor(resultSet.getBigDecimal("saldo_valor"));
                    return movimiento;
                });
    }

    public BigDecimal calcularValorInventarioActual() throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(cantidad_disponible * costo_unitario), 0) AS valor_inventario
                FROM lote
                WHERE activo = TRUE
                  AND cantidad_disponible > 0
                """;

        try (Connection connection = abrirConexion();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getBigDecimal("valor_inventario");
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calcularValorInventarioProducto(Connection connection, int idProducto) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(cantidad_disponible * costo_unitario), 0) AS valor_inventario
                FROM lote
                WHERE id_producto = ?
                  AND activo = TRUE
                  AND cantidad_disponible > 0
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idProducto);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal("valor_inventario");
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private List<MovimientoKardex> consultarMovimientos(String complementoSql, StatementConfigurer configurador)
            throws SQLException {
        return consultarLista(SELECT_KARDEX_RELACIONADO + complementoSql, configurador, this::mapearMovimiento);
    }

    private MovimientoKardex mapearMovimiento(ResultSet resultSet) throws SQLException {
        Categoria categoria = new Categoria(
                resultSet.getInt("id_categoria"),
                resultSet.getString("categoria_nombre"),
                resultSet.getString("categoria_descripcion"),
                resultSet.getBoolean("categoria_activo"));

        Producto producto = new Producto( //devuelve un nuevo objeto Producto construido a partir de los datos obtenidos del ResultSet, incluyendo la categoría asociada
                resultSet.getInt("id_producto"),
                categoria,
                resultSet.getString("sku"),
                resultSet.getString("codigo_barras"),
                resultSet.getString("producto_nombre"),
                resultSet.getString("producto_descripcion"),
                resultSet.getBigDecimal("stock_minimo"),
                resultSet.getBigDecimal("stock_actual"),
                resultSet.getBigDecimal("precio_venta"),
                resultSet.getBoolean("producto_activo"));

        Lote lote = null;
        int idLote = resultSet.getInt("id_lote");
        if (!resultSet.wasNull()) {
            lote = new Lote();
            lote.setIdLote(idLote);
            lote.setCodigoLote(resultSet.getString("codigo_lote"));
        }

        NotaSalida notaSalida = null;
        int idSalida = resultSet.getInt("id_salida");
        if (!resultSet.wasNull()) {
            notaSalida = new NotaSalida();
            notaSalida.setIdSalida(idSalida);
            notaSalida.setNumeroFactura(resultSet.getString("numero_factura"));
        }

        return new MovimientoKardex(
                resultSet.getInt("id_movimiento"),
                producto,
                lote,
                notaSalida,
                resultSet.getTimestamp("fecha").toLocalDateTime().toLocalDate(),
                resultSet.getString("tipo"),
                resultSet.getString("referencia"),
                resultSet.getBigDecimal("cantidad_entrada"),
                resultSet.getBigDecimal("costo_unitario_entrada"),
                resultSet.getBigDecimal("valor_entrada"),
                resultSet.getBigDecimal("cantidad_salida"),
                resultSet.getBigDecimal("costo_unitario_salida"),
                resultSet.getBigDecimal("valor_salida"),
                resultSet.getBigDecimal("saldo_cantidad"),
                resultSet.getBigDecimal("saldo_valor"),
                resultSet.getString("observacion"));
    }

    private void setNullableInt(PreparedStatement statement, int index, int value) throws SQLException {
        if (value <= 0) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private LocalDateTime toDateTime(LocalDate fecha) {
        LocalDate safeDate = fecha == null ? LocalDate.now() : fecha;
        return safeDate.atStartOfDay();
    }
}
