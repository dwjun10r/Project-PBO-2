import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException; // Penting: import SQLException

public class MainApp {
    public static void main(String[] args) {
        // Query yang ingin Anda jalankan untuk tabel room_types
        String query = "SELECT * FROM room_types;";

        try (Connection conn = DbConnection.connect()) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();

                // --- PENTING: Sesuaikan lebar kolom secara MANUAL di sini ---
                // Anda perlu mengukur panjang data terpanjang di setiap kolom
                // dan mengatur lebar yang sesuai.
                // Jika terlalu kecil, teks akan terpotong. Jika terlalu besar, banyak spasi kosong.
                int idWidth = 5;
                int villaIdWidth = 7;
                int nameWidth = 25; // Untuk nama room_type
                int quantityWidth = 10;
                int capacityWidth = 10;
                int priceWidth = 12;
                int bedSizeWidth = 12;
                int hasBoolWidth = 8; // Untuk kolom boolean seperti has_desk, has_ac, dll.

                // Array untuk menyimpan lebar setiap kolom berdasarkan urutan kolom di SELECT * FROM room_types
                // Ini harus sesuai dengan urutan kolom di tabel room_types Anda
                int[] columnWidths = {
                        idWidth, villaIdWidth, nameWidth, quantityWidth, capacityWidth, priceWidth,
                        bedSizeWidth, hasBoolWidth, hasBoolWidth, hasBoolWidth, hasBoolWidth,
                        hasBoolWidth, hasBoolWidth, hasBoolWidth
                };

                // --- Cetak Header ---
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rsmd.getColumnName(i);
                    int currentWidth = columnWidths[i - 1]; // Ambil lebar dari array
                    System.out.printf("%-" + currentWidth + "s", columnName);
                }
                System.out.println(); // Pindah baris setelah header

                // Garis pemisah
                for (int width : columnWidths) {
                    for (int i = 0; i < width; i++) {
                        System.out.print("-");
                    }
                    System.out.print(" "); // Spasi antar pemisah kolom
                }
                System.out.println(); // Pindah baris setelah garis pemisah

                // --- Cetak Data ---
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        String columnValue = rs.getString(i); // Mengambil nilai kolom berdasarkan indeks
                        if (columnValue == null) {
                            columnValue = "NULL"; // Menangani nilai null
                        }
                        int currentWidth = columnWidths[i - 1];
                        System.out.printf("%-" + currentWidth + "s", columnValue);
                    }
                    System.out.println(); // Pindah baris setelah setiap baris data
                }
            }
        } catch (SQLException e) { // Tangani SQLException secara spesifik
            System.err.println("Terjadi kesalahan SQL: " + e.getMessage());
            e.printStackTrace(); // Cetak stack trace untuk detail error
        } catch (Exception e) { // Tangani Exception umum lainnya
            System.err.println("Terjadi kesalahan umum: " + e.getMessage());
            e.printStackTrace(); // Cetak stack trace untuk detail error
        }
    }
}