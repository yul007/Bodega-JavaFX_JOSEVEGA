package com.bodega.dao;

import com.bodega.model.Cliente;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO para operaciones CRUD de clientes. */
public class ClienteDAO {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bodega_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "holacomo";

    public int crear(Cliente cliente) throws SQLException {
        String sql = """
                INSERT INTO cliente (identificacion, nombre, telefono, email, direccion, activo)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, cliente.getIdentificacion());
            statement.setString(2, cliente.getNombre());
            statement.setString(3, cliente.getTelefono());
            statement.setString(4, cliente.getEmail());
            statement.setString(5, cliente.getDireccion());
            statement.setBoolean(6, cliente.isActivo());
            statement.executeUpdate();
            return leerIdGenerado(statement);
        }
    }

    public List<Cliente> listarActivos() throws SQLException {
        String sql = """
                SELECT id_cliente, identificacion, nombre, telefono, email, direccion, activo
                FROM cliente
                WHERE activo = TRUE
                ORDER BY nombre
                """;
        return consultarLista(sql);
    }

    public List<Cliente> listarTodos() throws SQLException {
        String sql = """
                SELECT id_cliente, identificacion, nombre, telefono, email, direccion, activo
                FROM cliente
                ORDER BY nombre
                """;
        return consultarLista(sql);
    }

    public Optional<Cliente> buscarPorId(int idCliente) throws SQLException {
        String sql = """
                SELECT id_cliente, identificacion, nombre, telefono, email, direccion, activo
                FROM cliente
                WHERE id_cliente = ?
                """;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idCliente);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapearCliente(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<Cliente> buscarPorNombreOIdentificacion(String texto) throws SQLException {
        String sql = """
                SELECT id_cliente, identificacion, nombre, telefono, email, direccion, activo
                FROM cliente
                WHERE nombre LIKE ? OR identificacion LIKE ?
                ORDER BY nombre
                """;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            String filtro = "%" + texto + "%";
            statement.setString(1, filtro);
            statement.setString(2, filtro);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Cliente> clientes = new ArrayList<>();
                while (resultSet.next()) {
                    clientes.add(mapearCliente(resultSet));
                }
                return clientes;
            }
        }
    }

    public boolean actualizar(Cliente cliente) throws SQLException {
        String sql = """
                UPDATE cliente
                SET identificacion = ?, nombre = ?, telefono = ?, email = ?, direccion = ?, activo = ?
                WHERE id_cliente = ?
                """;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cliente.getIdentificacion());
            statement.setString(2, cliente.getNombre());
            statement.setString(3, cliente.getTelefono());
            statement.setString(4, cliente.getEmail());
            statement.setString(5, cliente.getDireccion());
            statement.setBoolean(6, cliente.isActivo());
            statement.setInt(7, cliente.getIdCliente());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean inactivar(int idCliente) throws SQLException {
        String sql = "UPDATE cliente SET activo = FALSE WHERE id_cliente = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idCliente);
            return statement.executeUpdate() > 0;
        }
    }

    private List<Cliente> consultarLista(String sql) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            List<Cliente> clientes = new ArrayList<>();
            while (resultSet.next()) {
                clientes.add(mapearCliente(resultSet));
            }
            return clientes;
        }
    }

    private Cliente mapearCliente(ResultSet resultSet) throws SQLException {
        return new Cliente(
                resultSet.getInt("id_cliente"),
                resultSet.getString("identificacion"),
                resultSet.getString("nombre"),
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
        throw new SQLException("No se pudo obtener el ID generado para el cliente.");
    }
}
