package dao;

import models.Customer;
import config.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {



    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all customers: " + e.getMessage());
        }
        return customers;
    }


    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ? WHERE id = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getPhone());
            pstmt.setInt(4, customer.getId());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating customer: " + e.getMessage());
            return false;
        }
    }


    public boolean addCustomer(Customer customer) {
        String sql = "INSERT INTO customers(name, email, phone) VALUES(?,?,?)";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getPhone());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding customer: " + e.getMessage());
            return false;
        }
    }

    public Customer getCustomerById(int id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting customer by ID: " + e.getMessage());
        }
        return null;
    }


    public boolean deleteCustomer(int id) {
        Connection conn = null;
        try {
            conn = DbConnection.connect();
            conn.setAutoCommit(false);


            String deleteReviewsSql = "DELETE FROM reviews WHERE booking IN (SELECT id FROM bookings WHERE customer = ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteReviewsSql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }


            String deleteBookingsSql = "DELETE FROM bookings WHERE customer = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteBookingsSql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }


            String deleteCustomerSql = "DELETE FROM customers WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteCustomerSql)) {
                pstmt.setInt(1, id);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error during rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
}