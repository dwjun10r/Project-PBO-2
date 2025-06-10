// Tidak perlu package declaration jika langsung di src/main/java

import server.SimpleHttpServer;  // Import SimpleHttpServer dari package server
import config.DbConnection; // Import DbConnection dari package config
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class MainApp {
    public static void main(String[] args) {
        // Optional: Kode untuk cek koneksi atau tabel seperti yang Anda buat sebelumnya
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
            // Tidak perlu e.printStackTrace() jika Anda ingin output lebih bersih di konsol
        }

        // Jalankan server HTTP
        try {
            new SimpleHttpServer().startServer();
        } catch (IOException e) {
            System.err.println("Gagal memulai HTTP server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}