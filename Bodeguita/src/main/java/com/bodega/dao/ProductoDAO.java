package com.bodega.dao;

import com.bodega.model.Categoria;
import com.bodega.model.Producto;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.bodega.util.ValidationUtils;

/** DAO para productos y consultas de stock. */
public class ProductoDAO {

    private static final String SELECT_PRODUCTO_CON_CATEGORIA = """
            SELECT p.id_producto, p.id_categoria, p.sku, p.codigo_barras, p.nombre,
                   p.descripcion, p.stock_minimo, p.stock_actual,
                   p.precio_venta, p.activo,
                   c.nombre AS categoria_nombre, c.descripcion AS categoria_descripcion,
                   c.activo AS categoria_activo
            FROM producto p
            JOIN categoria c ON c.id_categoria = p.id_categoria
            """;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bodega_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "holacomo";

    public int crear(Producto producto) throws SQLException {
        validarProducto(producto);
        String sql = """
                INSERT INTO producto (
                  id_categoria, sku, codigo_barras, nombre, descripcion,
                  stock_minimo, stock_actual, precio_venta, activo
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            prepararInsertUpdate(statement, producto);
            statement.executeUpdate();
            return leerIdGenerado(statement);
        }
    }

    public List<Producto> listarActivos() throws SQLException {
        String sql = SELECT_PRODUCTO_CON_CATEGORIA
                + " WHERE p.activo = TRUE ORDER BY p.nombre";
        return consultarLista(sql);
    }

    public List<Producto> listarTodos() throws SQLException {
        String sql = SELECT_PRODUCTO_CON_CATEGORIA
                + " ORDER BY p.nombre";
        return consultarLista(sql);
    }

    public Optional<Producto> buscarPorId(int idProducto) throws SQLException {
        String sql = SELECT_PRODUCTO_CON_CATEGORIA
                + " WHERE p.id_producto = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idProducto);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapearProducto(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Producto> buscarPorIdParaActualizar(Connection connection, int idProducto) throws SQLException {
        String sql = SELECT_PRODUCTO_CON_CATEGORIA
                + " WHERE p.id_producto = ? FOR UPDATE";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idProducto);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapearProducto(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Producto> buscarPorCodigo(String codigo) throws SQLException {
        String sql = SELECT_PRODUCTO_CON_CATEGORIA
                + " WHERE p.sku = ? OR p.codigo_barras = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, codigo);
            statement.setString(2, codigo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapearProducto(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<Producto> buscarPorNombre(String texto) throws SQLException {
        String sql = SELECT_PRODUCTO_CON_CATEGORIA
                + """
                  WHERE p.nombre LIKE ?
                  ORDER BY p.nombre
                  """;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            String filtro = "%" + texto + "%";
            statement.setString(1, filtro);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Producto> productos = new ArrayList<>();
                while (resultSet.next()) {
                    productos.add(mapearProducto(resultSet));
                }
                return productos;
            }
        }
    }

    public List<Producto> listarStockBajo() throws SQLException {
        String sql = SELECT_PRODUCTO_CON_CATEGORIA
                + """
                  WHERE p.activo = TRUE
                    AND p.stock_actual <= p.stock_minimo
                  ORDER BY p.stock_actual ASC, p.nombre
                  """;
        return consultarLista(sql);
    }

    public boolean actualizar(Producto producto) throws SQLException {
        validarProducto(producto);
        String sql = """
                UPDATE producto
                SET id_categoria = ?, sku = ?, codigo_barras = ?, nombre = ?, descripcion = ?,
                    stock_minimo = ?, stock_actual = ?, precio_venta = ?, activo = ?
                WHERE id_producto = ?
                """;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            prepararInsertUpdate(statement, producto);
            statement.setInt(11, producto.getIdProducto());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean actualizarStock(Connection connection, int idProducto, BigDecimal nuevoStock) throws SQLException {
        if (nuevoStock == null || nuevoStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        String sql = "UPDATE producto SET stock_actual = ? WHERE id_producto = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, nuevoStock);
            statement.setInt(2, idProducto);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean inactivar(int idProducto) throws SQLException {
        String sql = "UPDATE producto SET activo = FALSE WHERE id_producto = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idProducto);
            return statement.executeUpdate() > 0;
        }
    }

    private void prepararInsertUpdate(PreparedStatement statement, Producto producto) throws SQLException {
        statement.setInt(1, producto.getCategoria().getIdCategoria());
        statement.setString(2, producto.getSku());
        statement.setString(3, producto.getCodigoBarras());
        statement.setString(4, producto.getNombre());
        statement.setString(5, producto.getDescripcion());
        statement.setBigDecimal(6, producto.getStockMinimo());
        statement.setBigDecimal(7, producto.getStockActual());
        statement.setBigDecimal(8, producto.getPrecioVenta());
        statement.setBoolean(9, producto.isActivo());
    }

    private void validarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto es obligatorio.");
        }
        ValidationUtils.requerido(producto.getSku(), "SKU");
        ValidationUtils.requerido(producto.getNombre(), "nombre");
        ValidationUtils.requeridoNoNegativo(producto.getStockMinimo(), "stock minimo");
        ValidationUtils.requeridoNoNegativo(producto.getStockActual(), "stock actual");
        ValidationUtils.requeridoNoNegativo(producto.getPrecioVenta(), "precio venta");
        if (producto.getCategoria() == null) {
            throw new IllegalArgumentException("Debe seleccionar una categoria.");
        }
    }

    private List<Producto> consultarLista(String sql) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            List<Producto> productos = new ArrayList<>();
            while (resultSet.next()) {
                productos.add(mapearProducto(resultSet));
            }
            return productos;
        }
    }

    private Producto mapearProducto(ResultSet resultSet) throws SQLException {
        Categoria categoria = new Categoria(
                resultSet.getInt("id_categoria"),
                resultSet.getString("categoria_nombre"),
                resultSet.getString("categoria_descripcion"),
                resultSet.getBoolean("categoria_activo"));

        return new Producto(
                resultSet.getInt("id_producto"),
                categoria,
                resultSet.getString("sku"),
                resultSet.getString("codigo_barras"),
                resultSet.getString("nombre"),
                resultSet.getString("descripcion"),
                resultSet.getBigDecimal("stock_minimo"),
                resultSet.getBigDecimal("stock_actual"),
                resultSet.getBigDecimal("precio_venta"),
                resultSet.getBoolean("activo"));
    }

    private int leerIdGenerado(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        throw new SQLException("No se pudo obtener el ID generado para el producto.");
    }
}
