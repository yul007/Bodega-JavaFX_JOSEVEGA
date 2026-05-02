package com.bodega.dao;

import com.bodega.model.Proveedor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO para operaciones CRUD de proveedores. */
public class ProveedorDAO {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bodega_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "holacomo";

    public int crear(Proveedor proveedor) throws SQLException {
        String sql = """
                INSERT INTO proveedor (ruc, nombre, contacto, telefono, email, direccion, activo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, proveedor.getRuc());
            statement.setString(2, proveedor.getNombre());
            statement.setString(3, proveedor.getContacto());
            statement.setString(4, proveedor.getTelefono());
            statement.setString(5, proveedor.getEmail());
            statement.setString(6, proveedor.getDireccion());
            statement.setBoolean(7, proveedor.isActivo());
            statement.executeUpdate();
            return leerIdGenerado(statement);
        }
    }

    public List<Proveedor> listarActivos() throws SQLException {
        String sql = """
                SELECT id_proveedor, ruc, nombre, contacto, telefono, email, direccion, activo
                FROM proveedor
                WHERE activo = TRUE
                ORDER BY nombre
                """;
        return consultarLista(sql);
    }

    public List<Proveedor> listarTodos() throws SQLException {
        String sql = """
                SELECT id_proveedor, ruc, nombre, contacto, telefono, email, direccion, activo
                FROM proveedor
                ORDER BY nombre
                """;
        return consultarLista(sql);
    }

    public Optional<Proveedor> buscarPorId(int idProveedor) throws SQLException {
        String sql = """
                SELECT id_proveedor, ruc, nombre, contacto, telefono, email, direccion, activo
                FROM proveedor
                WHERE id_proveedor = ?
                """;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idProveedor);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapearProveedor(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<Proveedor> buscarPorNombreORuc(String texto) throws SQLException {
        String sql = """
                SELECT id_proveedor, ruc, nombre, contacto, telefono, email, direccion, activo
                FROM proveedor
                WHERE nombre LIKE ? OR ruc LIKE ?
                ORDER BY nombre
                """;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            String filtro = "%" + texto + "%";
            statement.setString(1, filtro);
            statement.setString(2, filtro);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Proveedor> proveedores = new ArrayList<>();
                while (resultSet.next()) {
                    proveedores.add(mapearProveedor(resultSet));
                }
                return proveedores;
            }
        }
    }

    public boolean actualizar(Proveedor proveedor) throws SQLException {
        String sql = """
                UPDATE proveedor
                SET ruc = ?, nombre = ?, contacto = ?, telefono = ?, email = ?, direccion = ?, activo = ?
                WHERE id_proveedor = ?
                """;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, proveedor.getRuc());
            statement.setString(2, proveedor.getNombre());
            statement.setString(3, proveedor.getContacto());
            statement.setString(4, proveedor.getTelefono());
            statement.setString(5, proveedor.getEmail());
            statement.setString(6, proveedor.getDireccion());
            statement.setBoolean(7, proveedor.isActivo());
            statement.setInt(8, proveedor.getIdProveedor());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean inactivar(int idProveedor) throws SQLException {
        String sql = "UPDATE proveedor SET activo = FALSE WHERE id_proveedor = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idProveedor);
            return statement.executeUpdate() > 0;
        }
    }

    private List<Proveedor> consultarLista(String sql) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            List<Proveedor> proveedores = new ArrayList<>();
            while (resultSet.next()) {
                proveedores.add(mapearProveedor(resultSet));
            }
            return proveedores;
        }
    }

    private Proveedor mapearProveedor(ResultSet resultSet) throws SQLException {
        return new Proveedor(
                resultSet.getInt("id_proveedor"),
                resultSet.getString("ruc"),
                resultSet.getString("nombre"),
                resultSet.getString("contacto"),
                resultSet.getString("telefono"),
                resultSet.getString("email"),
                resultSet.getString("direccion"),
                resultSet.getBoolean("activo"));
    }

    private int leerIdGenerado(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        throw new SQLException("No se pudo obtener el ID generado para el proveedor.");
    }
}
