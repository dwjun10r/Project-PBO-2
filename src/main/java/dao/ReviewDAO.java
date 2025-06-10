package dao;

import models.Review;
import config.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    // Metode yang sudah ada: addReview, getReviewsByVillaId, getReviewsByCustomerId

    // -------------- Metode baru: Mengambil detail review berdasarkan booking ID (GET /reviews/{bookingId}) --------------
    // Karena 'booking' adalah PRIMARY KEY di tabel reviews, kita bisa langsung mencarinya berdasarkan booking ID
    public Review getReviewById(int bookingId) {
        String sql = "SELECT * FROM reviews WHERE booking = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Review(
                        rs.getInt("booking"),
                        rs.getInt("star"),
                        rs.getString("title"),
                        rs.getString("content")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting review by booking ID: " + e.getMessage());
        }
        return null; // Mengembalikan null jika review tidak ditemukan
    }

    // --- Metode yang sudah ada (disertakan kembali untuk kelengkapan) ---
    public boolean addReview(Review review) { /* ... kode Anda ... */
        String sql = "INSERT INTO reviews(booking, star, title, content) VALUES(?,?,?,?)";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, review.getBookingId());
            pstmt.setInt(2, review.getStar());
            pstmt.setString(3, review.getTitle());
            pstmt.setString(4, review.getContent());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding review: " + e.getMessage());
            return false;
        }
    }

    public List<Review> getReviewsByVillaId(int villaId) { /* ... kode Anda ... */
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.* FROM reviews r " +
                "JOIN bookings b ON r.booking = b.id " +
                "JOIN room_types rt ON b.room_type = rt.id " +
                "WHERE rt.villa = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, villaId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reviews.add(new Review(
                        rs.getInt("booking"),
                        rs.getInt("star"),
                        rs.getString("title"),
                        rs.getString("content")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting reviews by villa ID: " + e.getMessage());
        }
        return reviews;
    }

    public List<Review> getReviewsByCustomerId(int customerId) { /* ... kode Anda ... */
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.* FROM reviews r JOIN bookings b ON r.booking = b.id WHERE b.customer = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reviews.add(new Review(
                        rs.getInt("booking"),
                        rs.getInt("star"),
                        rs.getString("title"),
                        rs.getString("content")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting reviews by customer ID: " + e.getMessage());
        }
        return reviews;
    }
}