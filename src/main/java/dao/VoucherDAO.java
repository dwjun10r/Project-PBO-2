package dao;

import models.Voucher;
import config.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VoucherDAO {

    // Mengambil daftar semua voucher (Endpoint: GET /vouchers)
    public List<Voucher> getAllVouchers() {
        List<Voucher> vouchers = new ArrayList<>();
        String sql = "SELECT * FROM vouchers";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                vouchers.add(new Voucher(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("description"),
                        rs.getDouble("discount"),
                        rs.getString("start_date"),
                        rs.getString("end_date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all vouchers: " + e.getMessage());
        }
        return vouchers;
    }

    // Mengambil detail voucher berdasarkan ID (Endpoint: GET /vouchers/{id})
    public Voucher getVoucherById(int id) {
        String sql = "SELECT * FROM vouchers WHERE id = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Voucher(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("description"),
                        rs.getDouble("discount"),
                        rs.getString("start_date"),
                        rs.getString("end_date")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting voucher by ID: " + e.getMessage());
        }
        return null;
    }

    // Membuat voucher baru (Endpoint: POST /vouchers)
    public boolean addVoucher(Voucher voucher) {
        String sql = "INSERT INTO vouchers(code, description, discount, start_date, end_date) VALUES(?,?,?,?,?)";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, voucher.getCode());
            pstmt.setString(2, voucher.getDescription());
            pstmt.setDouble(3, voucher.getDiscount());
            pstmt.setString(4, voucher.getStartDate());
            pstmt.setString(5, voucher.getEndDate());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding voucher: " + e.getMessage());
            return false;
        }
    }

    // Mengubah data suatu voucher (Endpoint: PUT /vouchers/{id})
    public boolean updateVoucher(Voucher voucher) {
        String sql = "UPDATE vouchers SET code = ?, description = ?, discount = ?, start_date = ?, end_date = ? WHERE id = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, voucher.getCode());
            pstmt.setString(2, voucher.getDescription());
            pstmt.setDouble(3, voucher.getDiscount());
            pstmt.setString(4, voucher.getStartDate());
            pstmt.setString(5, voucher.getEndDate());
            pstmt.setInt(6, voucher.getId());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating voucher: " + e.getMessage());
            return false;
        }
    }

    // Menghapus data suatu voucher (Endpoint: DELETE /vouchers/{id})
    public boolean deleteVoucher(int id) {
        String sql = "DELETE FROM vouchers WHERE id = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting voucher: " + e.getMessage());
            return false;
        }
    }
}