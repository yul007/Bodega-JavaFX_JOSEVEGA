package com.bodega.dao;

import com.bodega.model.Proveedor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/** DAO para operaciones CRUD de proveedores. */
public class ProveedorDAO extends JdbcDaoSupport {

    public int crear(Proveedor proveedor) throws SQLException {
        String sql = """
                INSERT INTO proveedor (ruc, nombre, contacto, telefono, email, direccion, activo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = abrirConexion();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, proveedor.getRuc());
            statement.setString(2, proveedor.getNombre());
            statement.setString(3, proveedor.getContacto());
            statement.setString(4, proveedor.getTelefono());
            statement.setString(5, proveedor.getEmail());
            statement.setString(6, proveedor.getDireccion());
            statement.setBoolean(7, proveedor.isActivo());
            statement.executeUpdate();
            return leerIdGenerado(statement, "No se pudo obtener el ID generado para el proveedor.");
        }
    }

    public List<Proveedor> listarActivos() throws SQLException {
        return consultarProveedores("""
                WHERE activo = TRUE
                ORDER BY nombre
                """, null);
    }

    public boolean actualizar(Proveedor proveedor) throws SQLException {
        String sql = """
                UPDATE proveedor
                SET ruc = ?, nombre = ?, contacto = ?, telefono = ?, email = ?, direccion = ?, activo = ?
                WHERE id_proveedor = ?
                """;

        try (Connection connection = abrirConexion();
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

        return ejecutarActualizacion(sql, statement -> statement.setInt(1, idProveedor));
    }

    private List<Proveedor> consultarProveedores(String complementoSql, StatementConfigurer configurador)
            throws SQLException {
        return consultarLista("""
                SELECT id_proveedor, ruc, nombre, contacto, telefono, email, direccion, activo
                FROM proveedor
                """
                + complementoSql, configurador, this::mapearProveedor);
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
}
