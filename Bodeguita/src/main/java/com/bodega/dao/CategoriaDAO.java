package com.bodega.dao;

import com.bodega.model.Categoria;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** DAO para consultas de categorias. */
public class CategoriaDAO extends JdbcDaoSupport {

    public List<Categoria> listarActivas() throws SQLException {
        return consultarCategorias("""
                WHERE activo = TRUE
                ORDER BY nombre
                """, null);
    }

    public List<Categoria> listarTodas() throws SQLException {
        return consultarCategorias(" ORDER BY nombre", null);
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
