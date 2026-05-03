package com.bodega.dao;

import com.bodega.model.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/** DAO para operaciones CRUD de clientes. */
public class ClienteDAO extends JdbcDaoSupport {

    public int crear(Cliente cliente) throws SQLException {
        String sql = """
                INSERT INTO cliente (identificacion, nombre, telefono, email, direccion, activo)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = abrirConexion();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, cliente.getIdentificacion());
            statement.setString(2, cliente.getNombre());
            statement.setString(3, cliente.getTelefono());
            statement.setString(4, cliente.getEmail());
            statement.setString(5, cliente.getDireccion());
            statement.setBoolean(6, cliente.isActivo());
            statement.executeUpdate();
            return leerIdGenerado(statement, "No se pudo obtener el ID generado para el cliente.");
        }
    }

    public List<Cliente> listarActivos() throws SQLException {
        return consultarClientes("""
                WHERE activo = TRUE
                ORDER BY nombre
                """, null);
    }

    public List<Cliente> buscarPorNombreOIdentificacion(String texto) throws SQLException {
        return consultarClientes("""
                WHERE nombre LIKE ? OR identificacion LIKE ?
                ORDER BY nombre
                """, statement -> {
            String filtro = "%" + texto + "%";
            statement.setString(1, filtro);
            statement.setString(2, filtro);
        });
    }

    public boolean actualizar(Cliente cliente) throws SQLException {
        String sql = """
                UPDATE cliente
                SET identificacion = ?, nombre = ?, telefono = ?, email = ?, direccion = ?, activo = ?
                WHERE id_cliente = ?
                """;

        try (Connection connection = abrirConexion();
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

        return ejecutarActualizacion(sql, statement -> statement.setInt(1, idCliente));
    }

    private List<Cliente> consultarClientes(String complementoSql, StatementConfigurer configurador)
            throws SQLException {
        return consultarLista("""
                SELECT id_cliente, identificacion, nombre, telefono, email, direccion, activo
                FROM cliente
                """
                + complementoSql, configurador, this::mapearCliente);
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
}
