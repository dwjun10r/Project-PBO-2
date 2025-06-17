package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.Booking;
import models.Customer;
import models.Review;
import server.Request;
import server.Response;
import services.CustomerService; // Import service baru

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomerHandler implements HttpHandler {

    private final CustomerService customerService;

    public CustomerHandler(CustomerService customerService) {
        this.customerService = customerService;
    }

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
                if (path.equals("/customers")) {
                    handleGetAllCustomers(res);
                } else if (path.matches("/customers/\\d+")) {
                    handleGetCustomerById(req, res);
                } else if (path.matches("/customers/\\d+/bookings")) {
                    handleGetBookingsByCustomerId(req, res);
                } else if (path.matches("/customers/\\d+/reviews")) {
                    handleGetReviewsByCustomerId(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for GET on Customer");
                }
            } else if ("POST".equals(method)) {
                if (path.equals("/customers")) {
                    handleAddCustomer(req, res);
                } else if (path.matches("/customers/\\d+/bookings")) {
                    handleAddBookingForCustomer(req, res);
                } else if (path.matches("/customers/\\d+/bookings/\\d+/reviews")) {
                    handleAddReviewForBooking(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for POST on Customer");
                }
            } else if ("PUT".equals(method)) {
                if (path.matches("/customers/\\d+")) {
                    handleUpdateCustomer(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for PUT on Customer");
                }
            } else if ("DELETE".equals(method)) {
                if (path.matches("/customers/\\d+")) {
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
        List<Customer> customers = customerService.getAllCustomers();
        res.sendJson(HttpURLConnection.HTTP_OK, customers);
    }

    private void handleGetCustomerById(Request req, Response res) throws IOException {
        int id = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/customers/(\\d+)");
        if (id == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Customer ID");
            return;
        }
        Customer customer = customerService.getCustomerById(id);
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
        List<Booking> bookings = customerService.getBookingsByCustomerId(customerId);
        res.sendJson(HttpURLConnection.HTTP_OK, bookings);
    }

    private void handleGetReviewsByCustomerId(Request req, Response res) throws IOException {
        int customerId = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/customers/(\\d+)/reviews");
        if (customerId == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Customer ID");
            return;
        }
        List<Review> reviews = customerService.getReviewsByCustomerId(customerId);
        res.sendJson(HttpURLConnection.HTTP_OK, reviews);
    }

    private void handleAddCustomer(Request req, Response res) throws IOException {
        Map<String, Object> reqJsonMap = req.getJSON();
        String name = (String) reqJsonMap.get("name");
        String email = (String) reqJsonMap.get("email");
        String phone = (String) reqJsonMap.get("phone");

        Customer newCustomer = new Customer(0, name, email, phone);

        if (customerService.addCustomer(newCustomer)) { // Memanggil service
            res.sendSuccess(HttpURLConnection.HTTP_CREATED, "Customer added successfully");
        } else {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Failed to add customer. Check provided data.");
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

        Booking newBooking = new Booking(
                0, customerId, roomTypeId, checkinDate, checkoutDate,
                price, voucherId, finalPrice, paymentStatus,
                hasCheckedIn != null && hasCheckedIn ? 1 : 0, hasCheckedOut != null && hasCheckedOut ? 1 : 0
        );

        if (customerService.addBookingForCustomer(customerId, newBooking)) {
            res.sendSuccess(HttpURLConnection.HTTP_CREATED, "Booking added successfully for customer " + customerId);
        } else {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Failed to add booking or invalid data. Check required fields.");
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

        Review newReview = new Review(bookingId, star, title, content);

        if (customerService.addReviewForBooking(bookingId, newReview)) {
            res.sendSuccess(HttpURLConnection.HTTP_CREATED, "Review added successfully for booking " + bookingId);
        } else {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Failed to add review or invalid data. Check required fields and star rating.");
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

        Customer updatedCustomer = new Customer(id, name, email, phone);

        if (customerService.updateCustomer(updatedCustomer)) {
            res.sendSuccess(HttpURLConnection.HTTP_OK, "Customer updated successfully");
        } else {
            if (customerService.getCustomerById(id) == null) {
                res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Customer not found");
            } else {
                res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Failed to update customer. Check provided data.");
            }
        }
    }

    private void handleDeleteCustomer(Request req, Response res) throws IOException {
        int id = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/customers/(\\d+)");
        if (id == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Customer ID");
            return;
        }

        if (customerService.deleteCustomer(id)) {
            res.sendSuccess(HttpURLConnection.HTTP_OK, "Customer deleted successfully");
        } else {
            res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Customer not found or failed to delete");
        }
    }
}

