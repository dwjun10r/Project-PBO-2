package dao;

import models.Villa;
import config.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VillaDAO {

    // Metode yang sudah ada: getAllVillas, getVillaById, addVilla, updateVilla, deleteVilla

    // -------------- Endpoint baru: Pencarian ketersediaan vila (GET /villas?ci_date={checkin_date}&co_date={checkout_date}) --------------
    public List<Villa> searchVillasByAvailability(String checkinDate, String checkoutDate) {
        List<Villa> availableVillas = new ArrayList<>();
        // Query untuk mencari villa yang memiliki setidaknya satu room_type yang tersedia
        // dalam rentang tanggal yang diberikan.
        // Ini adalah query yang cukup kompleks karena melibatkan beberapa JOIN dan subquery/NOT EXISTS.
        String sql = "SELECT DISTINCT v.* FROM villas v " +
                "JOIN room_types rt ON v.id = rt.villa " +
                "WHERE NOT EXISTS (" +
                "    SELECT 1 FROM bookings b " +
                "    WHERE b.room_type = rt.id " +
                "    AND (" +
                "        (b.checkin_date < ? AND b.checkout_date > ?) OR " + // Tumpang tindih sebagian (booking berakhir setelah checkin baru)
                "        (b.checkin_date < ? AND b.checkout_date > ?) OR " + // Tumpang tindih sebagian (booking mulai sebelum checkout baru)
                "        (b.checkin_date >= ? AND b.checkout_date <= ?)    " + // Booking penuh di antara tanggal baru
                "    )" +
                ")";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set parameter untuk tanggal
            pstmt.setString(1, checkoutDate); // Booking berakhir setelah tanggal checkin baru
            pstmt.setString(2, checkinDate);  // Booking mulai sebelum tanggal checkout baru
            pstmt.setString(3, checkoutDate);
            pstmt.setString(4, checkinDate);
            pstmt.setString(5, checkinDate);
            pstmt.setString(6, checkoutDate);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                availableVillas.add(new Villa(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error searching villas by availability: " + e.getMessage());
        }
        return availableVillas;
    }

    // -------------- Metode yang sudah ada (disertakan kembali untuk kelengkapan) --------------
    public List<Villa> getAllVillas() {
        List<Villa> villas = new ArrayList<>();
        String sql = "SELECT * FROM villas";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                villas.add(new Villa(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all villas: " + e.getMessage());
        }
        return villas;
    }

    public Villa getVillaById(int id) {
        String sql = "SELECT * FROM villas WHERE id = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Villa(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("address")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting villa by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean addVilla(Villa villa) {
        String sql = "INSERT INTO villas(name, description, address) VALUES(?,?,?)";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, villa.getName());
            pstmt.setString(2, villa.getDescription());
            pstmt.setString(3, villa.getAddress());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding villa: " + e.getMessage());
            return false;
        }
    }

    public boolean updateVilla(Villa villa) {
        String sql = "UPDATE villas SET name = ?, description = ?, address = ? WHERE id = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, villa.getName());
            pstmt.setString(2, villa.getDescription());
            pstmt.setString(3, villa.getAddress());
            pstmt.setInt(4, villa.getId());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating villa: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteVilla(int id) {
        String sql = "DELETE FROM villas WHERE id = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting villa: " + e.getMessage());
            return false;
        }
    }
}