package server; // Pastikan package ini sesuai

import config.DbConnection; // Import DbConnection
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class MainApp {
    public static void main(String[] args) throws Exception {
        int port = 8080; // Default port
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        // Optional: Kode untuk cek koneksi atau tabel
        try (Connection conn = DbConnection.connect()) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table';");
                System.out.println("Daftar tabel di villa.db:");
                while (rs.next()) {
                    System.out.println(rs.getString("name"));
                }
            }
        } catch (Exception e) {
            System.err.println("Terjadi kesalahan saat memeriksa database: " + e.getMessage());
        }

        System.out.printf("Listening on port: %s...\n", port);
        new Server(port); // Memulai server dari kelas Server baru
    }
}