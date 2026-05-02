package com.bodega.dao;

import com.bodega.db.DatabaseConnection;
import com.bodega.model.Categoria;
import com.bodega.model.Lote;
import com.bodega.model.Producto;
import com.bodega.model.Proveedor;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.bodega.util.ValidationUtils;

/** DAO para lotes de compra y consultas FIFO. */
public class LoteDAO {

    private static final String SELECT_LOTE_RELACIONADO = """
            SELECT l.id_lote, l.codigo_lote, l.cantidad, l.cantidad_disponible,
                   l.costo_unitario, l.fecha_ingreso,
                   l.factura_referencia, l.activo,
                   p.id_producto, p.id_categoria, p.sku, p.codigo_barras, p.nombre AS producto_nombre,
                   p.descripcion AS producto_descripcion, p.stock_minimo,
                   p.stock_actual, p.precio_venta, p.activo AS producto_activo,
                   c.nombre AS categoria_nombre, c.descripcion AS categoria_descripcion,
                   c.activo AS categoria_activo,
                   pr.id_proveedor, pr.ruc, pr.nombre AS proveedor_nombre, pr.contacto,
                   pr.telefono, pr.email, pr.direccion, pr.activo AS proveedor_activo
            FROM lote l
            JOIN producto p ON p.id_producto = l.id_producto
            JOIN categoria c ON c.id_categoria = p.id_categoria
            JOIN proveedor pr ON pr.id_proveedor = l.id_proveedor
            """;

    private final DatabaseConnection databaseConnection;

    public LoteDAO() {
        this.databaseConnection = DatabaseConnection.getInstance();
    }

    public int crear(Lote lote) throws SQLException {
        validarLote(lote);
        try (Connection connection = databaseConnection.getConnection()) {
            return crear(connection, lote);
        }
    }

    public int crear(Connection connection, Lote lote) throws SQLException {
        validarLote(lote);
        String sql = """
                INSERT INTO lote (
                  id_producto, id_proveedor, codigo_lote, cantidad, cantidad_disponible,
                  costo_unitario, fecha_ingreso, factura_referencia, activo
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            prepararInsertUpdate(statement, lote);
            statement.executeUpdate();
            return leerIdGenerado(statement);
        }
    }

    public List<Lote> listarTodos() throws SQLException {
        String sql = SELECT_LOTE_RELACIONADO + " ORDER BY l.fecha_ingreso DESC, l.id_lote DESC";
        return consultarLista(sql);
    }

    public Optional<Lote> buscarPorId(int idLote) throws SQLException {
        String sql = SELECT_LOTE_RELACIONADO + " WHERE l.id_lote = ?";

        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idLote);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapearLote(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<Lote> listarPorProducto(int idProducto) throws SQLException {
        String sql = SELECT_LOTE_RELACIONADO
                + " WHERE l.id_producto = ? ORDER BY l.fecha_ingreso, l.id_lote";

        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idProducto);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Lote> lotes = new ArrayList<>();
                while (resultSet.next()) {
                    lotes.add(mapearLote(resultSet));
                }
                return lotes;
            }
        }
    }

    public List<Lote> listarPorProveedor(int idProveedor) throws SQLException {
        String sql = SELECT_LOTE_RELACIONADO
                + " WHERE l.id_proveedor = ? ORDER BY l.fecha_ingreso DESC, l.id_lote DESC";

        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idProveedor);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Lote> lotes = new ArrayList<>();
                while (resultSet.next()) {
                    lotes.add(mapearLote(resultSet));
                }
                return lotes;
            }
        }
    }

    public List<Lote> listarDisponiblesFIFO(Connection connection, int idProducto) throws SQLException {
        String sql = SELECT_LOTE_RELACIONADO
                + """
                  WHERE l.id_producto = ?
                    AND l.cantidad_disponible > 0
                    AND l.activo = TRUE
                  ORDER BY l.fecha_ingreso ASC, l.id_lote ASC
                  FOR UPDATE
                  """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idProducto);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Lote> lotes = new ArrayList<>();
                while (resultSet.next()) {
                    lotes.add(mapearLote(resultSet));
                }
                return lotes;
            }
        }
    }

    public boolean actualizar(Lote lote) throws SQLException {
        validarLote(lote);
        String sql = """
                UPDATE lote
                SET id_producto = ?, id_proveedor = ?, codigo_lote = ?, cantidad = ?,
                    cantidad_disponible = ?, costo_unitario = ?, fecha_ingreso = ?,
                    factura_referencia = ?, activo = ?
                WHERE id_lote = ?
                """;

        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            prepararInsertUpdate(statement, lote);
            statement.setInt(11, lote.getIdLote());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean actualizarCantidadDisponible(Connection connection, int idLote,
            BigDecimal cantidadDisponible) throws SQLException {
        String sql = "UPDATE lote SET cantidad_disponible = ? WHERE id_lote = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, cantidadDisponible);
            statement.setInt(2, idLote);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean inactivar(int idLote) throws SQLException {
        String sql = "UPDATE lote SET activo = FALSE WHERE id_lote = ?";

        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idLote);
            return statement.executeUpdate() > 0;
        }
    }

    private void prepararInsertUpdate(PreparedStatement statement, Lote lote) throws SQLException {
        statement.setInt(1, lote.getProducto().getIdProducto());
        statement.setInt(2, lote.getProveedor().getIdProveedor());
        statement.setString(3, lote.getCodigoLote());
        statement.setBigDecimal(4, lote.getCantidad());
        statement.setBigDecimal(5, lote.getCantidadDisponible());
        statement.setBigDecimal(6, lote.getCostoUnitario());
        statement.setDate(7, Date.valueOf(lote.getFechaIngreso()));
        statement.setString(8, lote.getFacturaReferencia());
        statement.setBoolean(9, lote.isActivo());
    }

    private void validarLote(Lote lote) {
        if (lote == null) {
            throw new IllegalArgumentException("El lote es obligatorio.");
        }
        if (lote.getProducto() == null) {
            throw new IllegalArgumentException("Debe seleccionar un producto.");
        }
        if (lote.getProveedor() == null) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor.");
        }
        ValidationUtils.requerido(lote.getCodigoLote(), "codigo de lote");
        ValidationUtils.requeridoPositivo(lote.getCantidad(), "cantidad");
        ValidationUtils.requeridoPositivo(lote.getCantidadDisponible(), "cantidad disponible");
        ValidationUtils.requeridoPositivo(lote.getCostoUnitario(), "costo unitario");
        if (lote.getFechaIngreso() == null) {
            throw new IllegalArgumentException("La fecha de ingreso es obligatoria.");
        }
    }

    private List<Lote> consultarLista(String sql) throws SQLException {
        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            List<Lote> lotes = new ArrayList<>();
            while (resultSet.next()) {
                lotes.add(mapearLote(resultSet));
            }
            return lotes;
        }
    }

    private Lote mapearLote(ResultSet resultSet) throws SQLException {
        Categoria categoria = new Categoria(
                resultSet.getInt("id_categoria"),
                resultSet.getString("categoria_nombre"),
                resultSet.getString("categoria_descripcion"),
                resultSet.getBoolean("categoria_activo"));

        Producto producto = new Producto(
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

        Proveedor proveedor = new Proveedor(
                resultSet.getInt("id_proveedor"),
                resultSet.getString("ruc"),
                resultSet.getString("proveedor_nombre"),
                resultSet.getString("contacto"),
                resultSet.getString("telefono"),
                resultSet.getString("email"),
                resultSet.getString("direccion"),
                resultSet.getBoolean("proveedor_activo"));

        return new Lote(
                resultSet.getInt("id_lote"),
                producto,
                proveedor,
                resultSet.getString("codigo_lote"),
                resultSet.getBigDecimal("cantidad"),
                resultSet.getBigDecimal("cantidad_disponible"),
                resultSet.getBigDecimal("costo_unitario"),
                resultSet.getDate("fecha_ingreso").toLocalDate(),
                resultSet.getString("factura_referencia"),
                resultSet.getBoolean("activo"));
    }

    private int leerIdGenerado(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        throw new SQLException("No se pudo obtener el ID generado para el lote.");
    }
}
