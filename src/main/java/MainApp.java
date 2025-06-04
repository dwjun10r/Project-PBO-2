import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public class MainApp {
    public static void main(String[] args) {
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
            System.err.println("Terjadi kesalahan: " + e.getMessage());
        }
    }
}
