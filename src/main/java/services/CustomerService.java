package services;

import dao.BookingDAO;
import dao.CustomerDAO;
import dao.ReviewDAO;
import models.Booking;
import models.Customer;
import models.Review;

import java.sql.SQLException;
import java.util.List;

public class CustomerService {
    private final CustomerDAO customerDAO;
    private final BookingDAO bookingDAO;
    private final ReviewDAO reviewDAO;

    public CustomerService(CustomerDAO customerDAO, BookingDAO bookingDAO, ReviewDAO reviewDAO) {
        this.customerDAO = customerDAO;
        this.bookingDAO = bookingDAO;
        this.reviewDAO = reviewDAO;
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.getAllCustomers();
    }

    public Customer getCustomerById(int id) {
        return customerDAO.getCustomerById(id);
    }

    public List<Booking> getBookingsByCustomerId(int customerId) {
        return bookingDAO.getBookingsByCustomerId(customerId);
    }

    public List<Review> getReviewsByCustomerId(int customerId) {
        return reviewDAO.getReviewsByCustomerId(customerId);
    }

    public boolean addCustomer(Customer customer) {
        if (customer.getName() == null || customer.getName().isEmpty() ||
                customer.getEmail() == null || customer.getEmail().isEmpty() ||
                customer.getPhone() == null || customer.getPhone().isEmpty()) {
            System.err.println("Validation Error: Missing required fields (name, email, phone)");
            return false;
        }
        if (!customer.getEmail().matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) {
            System.err.println("Validation Error: Invalid email format");
            return false;
        }
        if (!customer.getPhone().matches("^\\+?[0-9\\s\\-]+$")) {
            System.err.println("Validation Error: Invalid phone number format");
            return false;
        }
        return customerDAO.addCustomer(customer);
    }

    public boolean updateCustomer(Customer customer) {
        if (customer.getName() == null || customer.getName().isEmpty() ||
                customer.getEmail() == null || customer.getEmail().isEmpty() ||
                customer.getPhone() == null || customer.getPhone().isEmpty()) {
            System.err.println("Validation Error: Missing required fields for Customer update");
            return false;
        }
        if (!customer.getEmail().matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) {
            System.err.println("Validation Error: Invalid email format");
            return false;
        }
        if (!customer.getPhone().matches("^\\+?[0-9\\s\\-]+$")) {
            System.err.println("Validation Error: Invalid phone number format");
            return false;
        }
        return customerDAO.updateCustomer(customer);
    }

    public boolean deleteCustomer(int id) {
        return customerDAO.deleteCustomer(id);
    }

    public boolean addBookingForCustomer(int customerId, Booking booking) {
        if (booking.getCheckinDate() == null || booking.getCheckinDate().isEmpty() ||
                booking.getCheckoutDate() == null || booking.getCheckoutDate().isEmpty() ||
                booking.getPaymentStatus() == null || booking.getPaymentStatus().isEmpty()) {
            System.err.println("Validation Error: Missing required string fields for Booking");
            return false;
        }
        if (booking.getRoomTypeId() <= 0 || booking.getPrice() <= 0 || booking.getFinalPrice() <= 0) {
            System.err.println("Validation Error: roomTypeId, price, or finalPrice must be positive for Booking");
            return false;
        }

        booking.setCustomerId(customerId);
        return bookingDAO.addBooking(booking);
    }

    public boolean addReviewForBooking(int bookingId, Review review) {
        if (review.getTitle() == null || review.getTitle().isEmpty() ||
                review.getContent() == null || review.getContent().isEmpty()) {
            System.err.println("Validation Error: Missing required string fields for Review");
            return false;
        }
        if (review.getStar() < 1 || review.getStar() > 5) {
            System.err.println("Validation Error: Star rating must be between 1 and 5");
            return false;
        }
        review.setBookingId(bookingId);
        return reviewDAO.addReview(review);
    }
}

