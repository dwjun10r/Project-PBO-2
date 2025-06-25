package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.BookingDAO;
import dao.CustomerDAO;
import dao.ReviewDAO;
import models.Booking;
import models.Customer;
import models.Review;
import server.Request;
import server.Response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomerHandler implements HttpHandler {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();

    private int extractIdFromPath(String path, String regexPattern) {
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(path);

        if (matcher.find() && matcher.groupCount() >= 1) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Request req = new Request(httpExchange);
        Response res = new Response(httpExchange);
        String path = httpExchange.getRequestURI().getPath();
        String method = req.getRequestMethod();

        try {
            if ("GET".equals(method)) {
                if (path.equals("/customers")) { // GET /customers
                    handleGetAllCustomers(res);
                } else if (path.matches("/customers/\\d+")) { // GET /customers/{id}
                    handleGetCustomerById(req, res);
                } else if (path.matches("/customers/\\d+/bookings")) { // GET /customers/{id}/bookings
                    handleGetBookingsByCustomerId(req, res);
                } else if (path.matches("/customers/\\d+/reviews")) { // GET /customers/{id}/reviews
                    handleGetReviewsByCustomerId(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for GET on Customer");
                }
            } else if ("POST".equals(method)) {
                if (path.equals("/customers")) { // POST /customers
                    handleAddCustomer(req, res);
                } else if (path.matches("/customers/\\d+/bookings")) { // POST /customers/{id}/bookings
                    handleAddBookingForCustomer(req, res);
                } else if (path.matches("/customers/\\d+/bookings/\\d+/reviews")) { // POST /customers/{id}/bookings/{id}/reviews
                    handleAddReviewForBooking(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for POST on Customer");
                }
            } else if ("PUT".equals(method)) {
                if (path.matches("/customers/\\d+")) { // PUT /customers/{id}
                    handleUpdateCustomer(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for PUT on Customer");
                }
            } else if ("DELETE".equals(method)) {
                if (path.matches("/customers/\\d+")) { // DELETE /customers/{id}
                    handleDeleteCustomer(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for DELETE on Customer");
                }
            } else {
                res.sendError(405, "Method Not Allowed");
            }
        } catch (Exception e) {
            System.err.println("Error in CustomerHandler: " + e.getMessage());
            e.printStackTrace();
            res.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGetAllCustomers(Response res) throws IOException {
        List<Customer> customers = customerDAO.getAllCustomers();
        res.sendJson(HttpURLConnection.HTTP_OK, customers);
    }

    private void handleGetCustomerById(Request req, Response res) throws IOException {
        int id = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/customers/(\\d+)");
        if (id == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Customer ID");
            return;
        }
        Customer customer = customerDAO.getCustomerById(id);
        if (customer != null) {
            res.sendJson(HttpURLConnection.HTTP_OK, customer);
        } else {
            res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Customer not found");
        }
    }

    private void handleGetBookingsByCustomerId(Request req, Response res) throws IOException {
        int customerId = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/customers/(\\d+)/bookings");
        if (customerId == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Customer ID");
            return;
        }
        List<Booking> bookings = bookingDAO.getBookingsByCustomerId(customerId);
        res.sendJson(HttpURLConnection.HTTP_OK, bookings);
    }

    private void handleGetReviewsByCustomerId(Request req, Response res) throws IOException {
        int customerId = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/customers/(\\d+)/reviews");
        if (customerId == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Customer ID");
            return;
        }
        List<Review> reviews = reviewDAO.getReviewsByCustomerId(customerId);
        res.sendJson(HttpURLConnection.HTTP_OK, reviews);
    }

    private void handleAddCustomer(Request req, Response res) throws IOException {
        Map<String, Object> reqJsonMap = req.getJSON();
        String name = (String) reqJsonMap.get("name");
        String email = (String) reqJsonMap.get("email");
        String phone = (String) reqJsonMap.get("phone");

        if (name == null || name.isEmpty() || email == null || email.isEmpty() || phone == null || phone.isEmpty()) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Missing required fields (name, email, phone)");
            return;
        }

        // Basic email validation
        if (!email.matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid email format");
            return;
        }

        // Basic phone number validation (e.g., starts with +, then digits)
        if (!phone.matches("^\\+?[0-9\\s\\-]+$")) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid phone number format");
            return;
        }
        Customer newCustomer = new Customer(0, name, email, phone);

        if (customerDAO.addCustomer(newCustomer)) {
            res.sendSuccess(HttpURLConnection.HTTP_CREATED, "Customer added successfully");
        } else {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Failed to add customer or invalid data");
        }
    }

    private void handleAddBookingForCustomer(Request req, Response res) throws IOException {
        int customerId = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/customers/(\\d+)/bookings");
        if (customerId == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Customer ID");
            return;
        }
        Map<String, Object> reqJsonMap = req.getJSON();
        Integer roomTypeId = (Integer) reqJsonMap.get("roomTypeId");
        String checkinDate = (String) reqJsonMap.get("checkinDate");
        String checkoutDate = (String) reqJsonMap.get("checkoutDate");
        Integer price = (Integer) reqJsonMap.get("price");
        Integer voucherId = (Integer) reqJsonMap.get("voucherId");
        Integer finalPrice = (Integer) reqJsonMap.get("finalPrice");
        String paymentStatus = (String) reqJsonMap.get("paymentStatus");
        Boolean hasCheckedIn = (Boolean) reqJsonMap.get("hasCheckedIn");
        Boolean hasCheckedOut = (Boolean) reqJsonMap.get("hasCheckedOut");

        if (roomTypeId == null || checkinDate == null || checkinDate.isEmpty() || checkoutDate == null || checkoutDate.isEmpty() ||
                price == null || finalPrice == null || paymentStatus == null || paymentStatus.isEmpty() || hasCheckedIn == null || hasCheckedOut == null) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Missing required fields for Booking");
            return;
        }
        Booking newBooking = new Booking(
                0, customerId, roomTypeId, checkinDate, checkoutDate,
                price, voucherId, finalPrice, paymentStatus,
                hasCheckedIn ? 1 : 0, hasCheckedOut ? 1 : 0
        );

        if (bookingDAO.addBooking(newBooking)) {
            res.sendSuccess(HttpURLConnection.HTTP_CREATED, "Booking added successfully for customer " + customerId);
        } else {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Failed to add booking or invalid data");
        }
    }

    private void handleAddReviewForBooking(Request req, Response res) throws IOException {
        Pattern pattern = Pattern.compile("/customers/\\d+/bookings/(\\d+)/reviews");
        Matcher matcher = pattern.matcher(req.getHttpExchange().getRequestURI().getPath());

        int bookingId = -1;
        if (matcher.find() && matcher.groupCount() == 1) {
            bookingId = Integer.parseInt(matcher.group(1));
        }

        if (bookingId == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Booking ID in path");
            return;
        }
        Map<String, Object> reqJsonMap = req.getJSON();
        Integer star = (Integer) reqJsonMap.get("star");
        String title = (String) reqJsonMap.get("title");
        String content = (String) reqJsonMap.get("content");

        if (star == null || title == null || title.isEmpty() || content == null || content.isEmpty()) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Missing required fields for Review");
            return;
        }

        if (star < 1 || star > 5) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Star rating must be between 1 and 5");
            return;
        }
        Review newReview = new Review(bookingId, star, title, content);

        if (reviewDAO.addReview(newReview)) {
            res.sendSuccess(HttpURLConnection.HTTP_CREATED, "Review added successfully for booking " + bookingId);
        } else {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Failed to add review or invalid data");
        }
    }

    private void handleUpdateCustomer(Request req, Response res) throws IOException {
        int id = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/customers/(\\d+)");
        if (id == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Customer ID");
            return;
        }
        Map<String, Object> reqJsonMap = req.getJSON();
        String name = (String) reqJsonMap.get("name");
        String email = (String) reqJsonMap.get("email");
        String phone = (String) reqJsonMap.get("phone");

        if (name == null || name.isEmpty() || email == null || email.isEmpty() || phone == null || phone.isEmpty()) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Missing required fields for Customer update");
            return;
        }

        if (!email.matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid email format");
            return;
        }

        if (!phone.matches("^\\+?[0-9\\s\\-]+$")) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid phone number format");
            return;
        }
        Customer updatedCustomer = new Customer(id, name, email, phone);

        if (customerDAO.updateCustomer(updatedCustomer)) {
            res.sendSuccess(HttpURLConnection.HTTP_OK, "Customer updated successfully");
        } else {
            res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Customer not found or failed to update");
        }
    }

    private void handleDeleteCustomer(Request req, Response res) throws IOException {
        int id = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/customers/(\\d+)");

        if (id == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Customer ID");
            return;
        }

        if (customerDAO.deleteCustomer(id)) {
            res.sendSuccess(HttpURLConnection.HTTP_OK, "Customer deleted successfully");
        } else {
            res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Customer not found or failed to delete");
        }
    }
}