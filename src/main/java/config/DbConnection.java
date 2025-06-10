package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

    private static final String URL = "jdbc:sqlite:src/main/resources/database/villa.db";

    public static Connection connect() {
        Connection conn = null;
        try {

            conn = DriverManager.getConnection(URL);
            System.out.println("Koneksi ke SQLite berhasil!");
        } catch (SQLException e) {
            System.out.println("Gagal koneksi: " + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }
}