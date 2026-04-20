package com.solab.appdesktop.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Esta clase es una clase utilitaria
 * encargada de la creacion de las tablas de las base de datos
 * y de exponer solo un unico objeto de conexion
 * haciendo uso del patron de diseño singleton
 */
public class SQLiteUtil {
    private static Connection connection;

    // Constructor privado para evitar instanciación
    private SQLiteUtil() {}

    /**
     * Este metodo devuelve solo una unica instancia de la conexion
     * haciendo uso del patron de diseño singleton.
     * @return Connection: conexion a la base de datos SQLite
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = "jdbc:sqlite:gestion_procesos.db";
            connection = DriverManager.getConnection(url);
        }
        return connection;
    }

    // Método para crear las tablas si no existen
    public static void crearTablas() throws SQLException {
        String sqlCatalogo = "CREATE TABLE IF NOT EXISTS catalogo ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "numero INTEGER NOT NULL,"
                + "nombre TEXT NOT NULL,"
                + "fecha TEXT NOT NULL"
                + ");";
        String sqlProceso = "CREATE TABLE IF NOT EXISTS proceso ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "catalogo_id INTEGER,"
                + "pid INTEGER,"
                + "nombre TEXT,"
                + "usuario TEXT,"
                + "descripcion TEXT,"
                + "prioridad INTEGER,"
                + "FOREIGN KEY (catalogo_id) REFERENCES catalogo(id)"
                + ");";
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sqlCatalogo);
            stmt.execute(sqlProceso);
        }
    }
}
