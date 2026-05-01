package com.bodega.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Administra la configuracion y creacion de conexiones JDBC a MySQL. */
public final class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String CONFIG_FILE = "database.properties";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/bodega_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static final DatabaseConnection INSTANCE = new DatabaseConnection();

    private final String url;
    private final String user;
    private final String password;

    private DatabaseConnection() {
        Properties properties = loadProperties();
        this.url = properties.getProperty("db.url", DEFAULT_URL).trim();
        this.user = properties.getProperty("db.user", DEFAULT_USER).trim();
        this.password = properties.getProperty("db.password", DEFAULT_PASSWORD);
        registerDriver();
    }

    public static DatabaseConnection getInstance() {
        return INSTANCE;
    }

    /**
     * Devuelve una conexion nueva. El codigo que la usa debe cerrarla con
     * try-with-resources o en un bloque finally.
     */
    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "No se pudo conectar a la base de datos bodega_db.", exception);
            throw new SQLException(buildConnectionErrorMessage(), exception);
        }
    }

    /** Permite validar la conexion desde controladores, DAOs o pruebas. */
    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection.isValid(3);
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "Prueba de conexion fallida.", exception);
            return false;
        }
    }

    private Properties loadProperties() {
        Properties properties = new Properties();

        try (InputStream input = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                LOGGER.warning("No se encontro database.properties. Se usara configuracion local por defecto.");
                return properties;
            }

            properties.load(input);
            return properties;
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "No se pudo leer database.properties. Se usara configuracion por defecto.", exception);
            return properties;
        }
    }

    private void registerDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            LOGGER.log(Level.SEVERE, "MySQL Connector/J no esta disponible en el classpath.", exception);
            throw new IllegalStateException("No se encontro el driver JDBC de MySQL. Revisa mysql-connector-j en pom.xml.", exception);
        }
    }

    private String buildConnectionErrorMessage() {
        return "No se pudo conectar a MySQL usando la base bodega_db. "
                + "Verifica que MySQL este encendido, que el script SQL se haya ejecutado "
                + "y que database.properties tenga usuario, clave y URL correctos.";
    }
}
