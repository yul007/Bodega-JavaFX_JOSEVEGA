package com.bodega.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.bodega.dao.JdbcDaoSupport.StatementConfigurer;

abstract class JdbcDaoSupport {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bodega_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "holacomo";

    @FunctionalInterface
    interface StatementConfigurer {
        void configurar(PreparedStatement statement) throws SQLException;
    }

    @FunctionalInterface
    interface RowMapper<T> {
        T mapear(ResultSet resultSet) throws SQLException;
    }

    protected Connection abrirConexion() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    protected <T> List<T> consultarLista(String sql, StatementConfigurer configurador, RowMapper<T> mapeador) throws SQLException {
        try (Connection connection = abrirConexion();
                PreparedStatement statement = connection.prepareStatement(sql)) {       // prepara la consulta SQL para su ejecución, permitiendo agregar parámetros de forma segura
            if (configurador != null) {     //null
                configurador.configurar(statement);     // esto permite configurar el statement antes de ejecutarlo, por ejemplo para agregar parámetros
            }
            try (ResultSet resultSet = statement.executeQuery()) {      // ejecuta la consulta SQL y devuelve un ResultSet que contiene los resultados de la consulta
                return mapearLista(resultSet, mapeador);    // toma el resultset y 
            }
        }
    }

    protected <T> List<T> consultarLista(Connection connection, String sql,
            StatementConfigurer configurador, RowMapper<T> mapeador) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (configurador != null) {
                configurador.configurar(statement);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapearLista(resultSet, mapeador);
            }
        }
    }

    protected <T> Optional<T> consultarUno(String sql, StatementConfigurer configurador,
            RowMapper<T> mapeador) throws SQLException {
        try (Connection connection = abrirConexion();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            if (configurador != null) {
                configurador.configurar(statement);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapeador.mapear(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    protected <T> Optional<T> consultarUno(Connection connection, String sql,
            StatementConfigurer configurador, RowMapper<T> mapeador) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (configurador != null) {
                configurador.configurar(statement);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapeador.mapear(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    protected boolean ejecutarActualizacion(String sql, StatementConfigurer configurador)
            throws SQLException {
        try (Connection connection = abrirConexion();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            if (configurador != null) {
                configurador.configurar(statement);
            }
            return statement.executeUpdate() > 0;
        }
    }

    protected boolean ejecutarActualizacion(Connection connection, String sql,
            StatementConfigurer configurador) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (configurador != null) {
                configurador.configurar(statement);
            }
            return statement.executeUpdate() > 0;
        }
    }

    protected int leerIdGenerado(PreparedStatement statement, String mensajeError) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        throw new SQLException(mensajeError);
    }

    private <T> List<T> mapearLista(ResultSet resultSet, RowMapper<T> mapeador) throws SQLException {
        List<T> valores = new ArrayList<>(); //crea una lista vacía para almacenar los objetos mapeados
        while (resultSet.next()) {
            valores.add(mapeador.mapear(resultSet)); /// .mapear hace el mapeo de cada fila del ResultSet a un objeto del tipo T utilizando el RowMapper proporcionado, y luego agrega ese objeto a la lista de valores
        }//mapeador es una instancia de RowMapper<T>    // mapeador.mapear()== mapearProducto()
        return valores;
    }
}
