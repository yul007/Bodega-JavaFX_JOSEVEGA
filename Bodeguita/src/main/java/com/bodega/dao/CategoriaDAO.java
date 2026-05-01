package com.bodega.dao;

import com.bodega.db.DatabaseConnection;
import com.bodega.model.Categoria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO para operaciones CRUD de categorias. */
public class CategoriaDAO {

    private final DatabaseConnection databaseConnection;

    public CategoriaDAO() {
        this.databaseConnection = DatabaseConnection.getInstance();
    }

    public int crear(Categoria categoria) throws SQLException {
        String sql = """
                INSERT INTO categoria (nombre, descripcion, activo)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, categoria.getNombre());
            statement.setString(2, categoria.getDescripcion());
            statement.setBoolean(3, categoria.isActivo());
            statement.executeUpdate();
            return leerIdGenerado(statement);
        }
    }

    public List<Categoria> listarActivas() throws SQLException {
        String sql = """
                SELECT id_categoria, nombre, descripcion, activo
                FROM categoria
                WHERE activo = TRUE
                ORDER BY nombre
                """;
        return consultarLista(sql);
    }

    public List<Categoria> listarTodas() throws SQLException {
        String sql = """
                SELECT id_categoria, nombre, descripcion, activo
                FROM categoria
                ORDER BY nombre
                """;
        return consultarLista(sql);
    }

    public Optional<Categoria> buscarPorId(int idCategoria) throws SQLException {
        String sql = """
                SELECT id_categoria, nombre, descripcion, activo
                FROM categoria
                WHERE id_categoria = ?
                """;

        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idCategoria);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapearCategoria(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<Categoria> buscarPorNombre(String texto) throws SQLException {
        String sql = """
                SELECT id_categoria, nombre, descripcion, activo
                FROM categoria
                WHERE nombre LIKE ?
                ORDER BY nombre
                """;

        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + texto + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Categoria> categorias = new ArrayList<>();
                while (resultSet.next()) {
                    categorias.add(mapearCategoria(resultSet));
                }
                return categorias;
            }
        }
    }

    public boolean actualizar(Categoria categoria) throws SQLException {
        String sql = """
                UPDATE categoria
                SET nombre = ?, descripcion = ?, activo = ?
                WHERE id_categoria = ?
                """;

        try (Connection connection = databaseConnection.getConnection();
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

        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idCategoria);
            return statement.executeUpdate() > 0;
        }
    }

    private List<Categoria> consultarLista(String sql) throws SQLException {
        try (Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            List<Categoria> categorias = new ArrayList<>();
            while (resultSet.next()) {
                categorias.add(mapearCategoria(resultSet));
            }
            return categorias;
        }
    }

    private Categoria mapearCategoria(ResultSet resultSet) throws SQLException {
        return new Categoria(
                resultSet.getInt("id_categoria"),
                resultSet.getString("nombre"),
                resultSet.getString("descripcion"),
                resultSet.getBoolean("activo"));
    }

    private int leerIdGenerado(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        throw new SQLException("No se pudo obtener el ID generado para la categoria.");
    }
}
