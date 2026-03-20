package org.lapaloma.mapamundi.gestores;

import java.sql.Connection;
import java.sql.DriverManager;

public class GestorConexionJDBC {

    // Evita que pueda construirse un objeto de la clase.
    private GestorConexionJDBC() {
    }

    public static Connection getConexionSGDB() throws Exception {
        Connection conexionSGDB = null;

        // Datos URL
        String urlBBDD = GestorFicheroConfiguracion.obtenerValor("jdbc.url");

        String usuario = System.getenv("DB_USER");
        String contrasenya = System.getenv("DB_PASSWORD");

        String claseDriver = GestorFicheroConfiguracion.obtenerValor("jdbc.driver");
        Class.forName(claseDriver);

        conexionSGDB = DriverManager.getConnection(urlBBDD, usuario, contrasenya);

        return conexionSGDB;
    }

}
