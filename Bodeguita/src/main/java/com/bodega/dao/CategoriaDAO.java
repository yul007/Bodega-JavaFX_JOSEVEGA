package com.bodega.dao;

import com.bodega.model.Categoria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/** DAO para operaciones CRUD de categorias. */
public class CategoriaDAO extends JdbcDaoSupport {

    public int crear(Categoria categoria) throws SQLException {
        String sql = """
                INSERT INTO categoria (nombre, descripcion, activo)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = abrirConexion();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, categoria.getNombre());
            statement.setString(2, categoria.getDescripcion());
            statement.setBoolean(3, categoria.isActivo());
            statement.executeUpdate();
            return leerIdGenerado(statement, "No se pudo obtener el ID generado para la categoria.");
        }
    }

    public List<Categoria> listarActivas() throws SQLException {
        return consultarCategorias("""
                WHERE activo = TRUE
                ORDER BY nombre
                """, null);
    }

    public boolean actualizar(Categoria categoria) throws SQLException {
        String sql = """
                UPDATE categoria
                SET nombre = ?, descripcion = ?, activo = ?
                WHERE id_categoria = ?
                """;

        try (Connection connection = abrirConexion();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, categoria.getNombre());
            statement.setString(2, categoria.getDescripcion());
            statement.setBoolean(3, categoria.isActivo());
            statement.setInt(4, categoria.getIdCategoria());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean inactivar(int idCategoria) throws SQLException {
        String sql = "UPDATE categoria SET activo = FALSE WHERE id_categoria = ?";

        return ejecutarActualizacion(sql, statement -> statement.setInt(1, idCategoria));
    }

    private List<Categoria> consultarCategorias(String complementoSql, StatementConfigurer configurador)
            throws SQLException {
        return consultarLista("""
                SELECT id_categoria, nombre, descripcion, activo
                FROM categoria
                """
                + complementoSql, configurador, this::mapearCategoria);
    }

    private Categoria mapearCategoria(ResultSet resultSet) throws SQLException {
        return new Categoria(
                resultSet.getInt("id_categoria"),
                resultSet.getString("nombre"),
                resultSet.getString("descripcion"),
                resultSet.getBoolean("activo"));
    }
}
