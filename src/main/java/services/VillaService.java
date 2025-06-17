package services;

import dao.BookingDAO;
import dao.ReviewDAO;
import dao.RoomTypeDAO;
import dao.VillaDAO;
import models.Booking;
import models.Review;
import models.RoomType;
import models.Villa;

import java.util.List;

public class VillaService {
    private final VillaDAO villaDAO;
    private final RoomTypeDAO roomTypeDAO;
    private final BookingDAO bookingDAO;
    private final ReviewDAO reviewDAO;

    public VillaService(VillaDAO villaDAO, RoomTypeDAO roomTypeDAO, BookingDAO bookingDAO, ReviewDAO reviewDAO) {
        this.villaDAO = villaDAO;
        this.roomTypeDAO = roomTypeDAO;
        this.bookingDAO = bookingDAO;
        this.reviewDAO = reviewDAO;
    }

    public List<Villa> getAllVillas() {
        return villaDAO.getAllVillas();
    }

    public Villa getVillaById(int id) {
        return villaDAO.getVillaById(id);
    }

    public List<RoomType> getRoomTypesByVillaId(int villaId) {
        return roomTypeDAO.getRoomTypesByVillaId(villaId);
    }

    public List<Booking> getBookingsByVillaId(int villaId) {
        return bookingDAO.getBookingsByVillaId(villaId);
    }

    public List<Review> getReviewsByVillaId(int villaId) {
        return reviewDAO.getReviewsByVillaId(villaId);
    }

    public List<Villa> searchVillasByAvailability(String checkinDate, String checkoutDate) {
        if (checkinDate == null || checkinDate.isEmpty() || checkoutDate == null || checkoutDate.isEmpty()) {
            System.err.println("Validation Error: Missing checkinDate or checkoutDate for availability search");
            return null;
        }
        return villaDAO.searchVillasByAvailability(checkinDate, checkoutDate);
    }

    public boolean addVilla(Villa villa) {
        if (villa.getName() == null || villa.getName().isEmpty() ||
                villa.getDescription() == null || villa.getDescription().isEmpty() ||
                villa.getAddress() == null || villa.getAddress().isEmpty()) {
            System.err.println("Validation Error: Missing required fields for Villa (name, description, address)");
            return false;
        }
        return villaDAO.addVilla(villa);
    }

    public boolean addRoomTypeToVilla(int villaId, RoomType roomType) {
        if (roomType.getName() == null || roomType.getName().isEmpty() ||
                roomType.getBedSize() == null || roomType.getBedSize().isEmpty() ||
                roomType.getQuantity() <= 0 || roomType.getCapacity() <= 0 || roomType.getPrice() <= 0) {
            System.err.println("Validation Error: Missing required string fields or invalid numeric values (<=0) for Room Type");
            return false;
        }
        roomType.setVillaId(villaId);
        return roomTypeDAO.addRoomType(roomType);
    }

    public boolean updateVilla(Villa villa) {
        if (villa.getName() == null || villa.getName().isEmpty() ||
                villa.getDescription() == null || villa.getDescription().isEmpty() ||
                villa.getAddress() == null || villa.getAddress().isEmpty()) {
            System.err.println("Validation Error: Missing required fields for Villa update");
            return false;
        }
        return villaDAO.updateVilla(villa);
    }

    public boolean updateRoomTypeInVilla(RoomType roomType) {
        if (roomType.getId() == -1 || roomType.getVillaId() == -1) {
            System.err.println("Validation Error: Invalid Room ID or Villa ID for update");
            return false;
        }
        if (roomType.getName() == null || roomType.getName().isEmpty() ||
                roomType.getBedSize() == null || roomType.getBedSize().isEmpty() ||
                roomType.getQuantity() <= 0 || roomType.getCapacity() <= 0 || roomType.getPrice() <= 0) {
            System.err.println("Validation Error: Missing required string fields or invalid numeric values (<=0) for Room Type update");
            return false;
        }
        return roomTypeDAO.updateRoomType(roomType);
    }

    public boolean deleteRoomType(int roomTypeId) {
        return roomTypeDAO.deleteRoomType(roomTypeId);
    }

    public boolean deleteVilla(int id) {
        return villaDAO.deleteVilla(id);
    }
}

}

