package dao;

import models.Booking;
import config.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    // Metode yang sudah ada: addBooking, getBookingById

    // -------------- Endpoint baru: Daftar semua booking pada suatu vila (GET /villas/{id}/bookings) --------------
    public List<Booking> getBookingsByVillaId(int villaId) {

        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.* FROM bookings b JOIN room_types rt ON b.room_type = rt.id WHERE rt.villa = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, villaId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                bookings.add(new Booking(
                        rs.getInt("id"),
                        rs.getInt("customer"),
                        rs.getInt("room_type"),
                        rs.getString("checkin_date"),
                        rs.getString("checkout_date"),
                        rs.getInt("price"),
rs.getString("voucher"),
//                        rs.getObject("voucher", Integer.class), // Handle nullable voucher
                        rs.getInt("final_price"),
                        rs.getString("payment_status"),
                        rs.getInt("has_checkedin"),
                        rs.getInt("has_checkedout")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error getting bookings by villa ID: " + e.getMessage());
        }
        return bookings;
    }

    // -------------- Endpoint baru: Daftar booking yang telah dilakukan oleh seorang customer (GET /customers/{id}/bookings) --------------
    public List<Booking> getBookingsByCustomerId(int customerId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE customer = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                bookings.add(new Booking(
                        rs.getInt("id"),
                        rs.getInt("customer"),
                        rs.getInt("room_type"),
                        rs.getString("checkin_date"),
                        rs.getString("checkout_date"),
                        rs.getInt("price"),
                        rs.getString("voucher"),
//                        rs.getObject("voucher", Integer.class), // Handle nullable voucher
                        rs.getInt("final_price"),
                        rs.getString("payment_status"),
                        rs.getInt("has_checkedin"),
                        rs.getInt("has_checkedout")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting bookings by customer ID: " + e.getMessage());
        }
        return bookings;
    }

    // -------------- Metode yang sudah ada (disertakan kembali untuk kelengkapan) --------------
    public boolean addBooking(Booking booking) { /* ... kode Anda ... */
        String sql = "INSERT INTO bookings(customer, room_type, checkin_date, checkout_date, " +
                "price, voucher, final_price, payment_status, has_checkedin, has_checkedout) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, booking.getCustomerId());
            pstmt.setInt(2, booking.getRoomTypeId());
            pstmt.setString(3, booking.getCheckinDate());
            pstmt.setString(4, booking.getCheckoutDate());
            pstmt.setInt(5, booking.getPrice());
            if (booking.getVoucherId() != null) {
                pstmt.setString(6, booking.getVoucherId());
            } else {
                pstmt.setNull(6, java.sql.Types.INTEGER);
            }
            pstmt.setInt(7, booking.getFinalPrice());
            pstmt.setString(8, booking.getPaymentStatus());
            pstmt.setInt(9, booking.isHasCheckedIn() ? 1 : 0);
            pstmt.setInt(10, booking.isHasCheckedOut() ? 1 : 0);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding booking: " + e.getMessage());
            return false;
        }
    }

    public Booking getBookingById(int id) {
        String sql = "SELECT * FROM bookings WHERE id = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Booking(
                        rs.getInt("id"),
                        rs.getInt("customer"),
                        rs.getInt("room_type"),
                        rs.getString("checkin_date"),
                        rs.getString("checkout_date"),
                        rs.getInt("price"),
                        rs.getString("voucher"),
//                        rs.getObject("voucher", Integer.class),
                        rs.getInt("final_price"),
                        rs.getString("payment_status"),
                        rs.getInt("has_checkedin"),
                        rs.getInt("has_checkedout")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting booking by ID: " + e.getMessage());
        }
        return null;
    }
}