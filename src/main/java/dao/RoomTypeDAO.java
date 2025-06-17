package dao;

import models.RoomType;
import config.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomTypeDAO {

    // Metode yang sudah ada: getRoomTypesByVillaId, getRoomTypeById, addRoomType

    // -------------- Endpoint baru: Mengubah informasi kamar suatu vila (PUT /villas/{id}/rooms/{id}) --------------
    public boolean updateRoomType(RoomType roomType) {
        String sql = "UPDATE room_types SET villa = ?, name = ?, quantity = ?, capacity = ?, price = ?, bed_size = ?, " +
                "has_desk = ?, has_ac = ?, has_tv = ?, has_wifi = ?, has_shower = ?, has_hotwater = ?, has_fridge = ? WHERE id = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomType.getVillaId());
            pstmt.setString(2, roomType.getName());
            pstmt.setInt(3, roomType.getQuantity());
            pstmt.setInt(4, roomType.getCapacity());
            pstmt.setInt(5, roomType.getPrice());
            pstmt.setString(6, roomType.getBedSize());
            pstmt.setInt(7, roomType.isHasDesk() ? 1 : 0);
            pstmt.setInt(8, roomType.isHasAc() ? 1 : 0);
            pstmt.setInt(9, roomType.isHasTv() ? 1 : 0);
            pstmt.setInt(10, roomType.isHasWifi() ? 1 : 0);
            pstmt.setInt(11, roomType.isHasShower() ? 1 : 0);
            pstmt.setInt(12, roomType.isHasHotwater() ? 1 : 0);
            pstmt.setInt(13, roomType.isHasFridge() ? 1 : 0);
            pstmt.setInt(14, roomType.getId()); // ID untuk WHERE clause
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating room type: " + e.getMessage());
            return false;
        }
    }

    // -------------- Endpoint baru: Menghapus kamar suatu vila (DELETE /villas/{id}/rooms/{id}) --------------
    public boolean deleteRoomType(int id) {
        String sql = "DELETE FROM room_types WHERE id = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting room type: " + e.getMessage());
            return false;
        }
    }

    // -------------- Metode yang sudah ada (disertakan kembali untuk kelengkapan) --------------
    public List<RoomType> getRoomTypesByVillaId(int villaId) {
        List<RoomType> roomTypes = new ArrayList<>();
        String sql = "SELECT * FROM room_types WHERE villa = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, villaId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                roomTypes.add(new RoomType(
                        rs.getInt("id"),
                        rs.getInt("villa"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getInt("capacity"),
                        rs.getInt("price"),
                        rs.getString("bed_size"),
                        rs.getInt("has_desk"),
                        rs.getInt("has_ac"),
                        rs.getInt("has_tv"),
                        rs.getInt("has_wifi"),
                        rs.getInt("has_shower"),
                        rs.getInt("has_hotwater"),
                        rs.getInt("has_fridge")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting room types by villa ID: " + e.getMessage());
        }
        return roomTypes;
    }

    public RoomType getRoomTypeById(int id) {
        String sql = "SELECT * FROM room_types WHERE id = ?";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new RoomType(
                        rs.getInt("id"),
                        rs.getInt("villa"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getInt("capacity"),
                        rs.getInt("price"),
                        rs.getString("bed_size"),
                        rs.getInt("has_desk"),
                        rs.getInt("has_ac"),
                        rs.getInt("has_tv"),
                        rs.getInt("has_wifi"),
                        rs.getInt("has_shower"),
                        rs.getInt("has_hotwater"),
                        rs.getInt("has_fridge")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting room type by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean addRoomType(RoomType roomType) {
        String sql = "INSERT INTO room_types(villa, name, quantity, capacity, price, bed_size, " +
                "has_desk, has_ac, has_tv, has_wifi, has_shower, has_hotwater, has_fridge) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomType.getVillaId());
            pstmt.setString(2, roomType.getName());
            pstmt.setInt(3, roomType.getQuantity());
            pstmt.setInt(4, roomType.getCapacity());
            pstmt.setInt(5, roomType.getPrice());
            pstmt.setString(6, roomType.getBedSize());
            pstmt.setInt(7, roomType.isHasDesk() ? 1 : 0);
            pstmt.setInt(8, roomType.isHasAc() ? 1 : 0);
            pstmt.setInt(9, roomType.isHasTv() ? 1 : 0);
            pstmt.setInt(10, roomType.isHasWifi() ? 1 : 0);
            pstmt.setInt(11, roomType.isHasShower() ? 1 : 0);
            pstmt.setInt(12, roomType.isHasHotwater() ? 1 : 0);
            pstmt.setInt(13, roomType.isHasFridge() ? 1 : 0);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding room type: " + e.getMessage());
            return false;
        }
    }
}