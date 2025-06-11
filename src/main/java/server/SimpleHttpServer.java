package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.net.URI;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.format.DateTimeParseException;

// Import DAO dan Model
import config.DbConnection;
import dao.VillaDAO;
import dao.RoomTypeDAO;
import dao.CustomerDAO;
import dao.BookingDAO;
import dao.ReviewDAO;
import dao.VoucherDAO;
import models.Villa;
import models.RoomType;
import models.Customer;
import models.Booking;
import models.Review;
import models.Voucher;

public class SimpleHttpServer {

    private static final int PORT = 8080;

    // Instansiasi DAO untuk digunakan di semua handler
    private VillaDAO villaDAO = new VillaDAO();
    private RoomTypeDAO roomTypeDAO = new RoomTypeDAO();
    private CustomerDAO customerDAO = new CustomerDAO();
    private BookingDAO bookingDAO = new BookingDAO();
    private ReviewDAO reviewDAO = new ReviewDAO();
    private VoucherDAO voucherDAO = new VoucherDAO();

    public static void main(String[] args) throws IOException {
        new SimpleHttpServer().startServer();
    }

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Contexts untuk setiap entitas utama
        server.createContext("/villas", new VillaHandler());
        server.createContext("/customers", new CustomerHandler());
        server.createContext("/vouchers", new VoucherHandler());

        // Optional: Context untuk halaman utama atau default
        server.createContext("/", this::handleRootRequest);

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Server started on port " + PORT);
    }

    private void handleRootRequest(HttpExchange exchange) throws IOException {
        String response = "Welcome to the Villa Booking API! Available endpoints: /villas, /customers, /vouchers";
        sendResponse(exchange, 200, response, "text/plain");
    }

    // --- Utility Methods for HTTP Responses and Parsing ---
    private void sendResponse(HttpExchange exchange, int statusCode, String response, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        isr.close();
        return sb.toString();
    }

    // Perbaiki extractIdFromPath agar lebih robust dengan regex
    // Regex Pattern contoh: "/villas/(\\d+)" akan menangkap ID setelah /villas/
    // Regex Pattern contoh: "/customers/(\\d+)/bookings/(\\d+)" akan menangkap kedua ID (group 1 & group 2)
    private int extractIdFromPath(String path, String regexPattern) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regexPattern);
        java.util.regex.Matcher matcher = pattern.matcher(path);
        if (matcher.find() && matcher.groupCount() >= 1) { // Pastikan ada setidaknya satu group yang ditangkap
            try {
                return Integer.parseInt(matcher.group(1)); // Group 1 adalah ID pertama yang ditangkap
            } catch (NumberFormatException e) {
                return -1; // Indicate error
            }
        }
        return -1; // No match found or no ID group
    }

    private Map<String, String> parseQueryParams(URI uri) {
        String query = uri.getQuery();
        if (query == null || query.isEmpty()) {
            return Map.of(); // Java 9+ for Map.of()
        }
        return java.util.Arrays.stream(query.split("&"))
                .map(s -> s.split("="))
                .filter(a -> a.length == 2)
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));
    }

    // --- JSON Conversion Utility (Manual - Sederhana) ---
    // PENTING: Implementasi parsing JSON manual ini sangat sederhana.
    // Jika Anda diperbolehkan menggunakan Gson/Jackson, itu akan sangat mempermudah ini.

    // Universal JSON value extractor (untuk semua parseJsonToX)
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return null;

        startIndex += searchKey.length();
        char firstChar = json.charAt(startIndex);

        if (firstChar == '"') { // String value
            int endIndex = json.indexOf("\"", startIndex + 1);
            if (endIndex == -1) return null;
            return json.substring(startIndex + 1, endIndex);
        } else { // Number, boolean, or null value
            int endIndex = startIndex;
            while (endIndex < json.length() &&
                    json.charAt(endIndex) != ',' && json.charAt(endIndex) != '}' &&
                    json.charAt(endIndex) != ']' && !Character.isWhitespace(json.charAt(endIndex))) {
                endIndex++;
            }
            return json.substring(startIndex, endIndex).trim();
        }
    }

    // Universal JSON escape for string values
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // --- Konversi JSON (Java Object to JSON String) ---
    private String convertVillaToJson(Villa villa) {
        if (villa == null) return "null";
        return String.format("{\"id\":%d,\"name\":\"%s\",\"description\":\"%s\",\"address\":\"%s\"}",
                villa.getId(), escapeJson(villa.getName()), escapeJson(villa.getDescription()), escapeJson(villa.getAddress()));
    }
    private String convertVillasToJson(List<Villa> villas) {
        return "[" + villas.stream().map(this::convertVillaToJson).collect(Collectors.joining(",")) + "]";
    }

    private String convertRoomTypeToJson(RoomType rt) {
        if (rt == null) return "null";
        return String.format("{\"id\":%d,\"villaId\":%d,\"name\":\"%s\",\"quantity\":%d,\"capacity\":%d," +
                        "\"price\":%d,\"bedSize\":\"%s\",\"hasDesk\":%b,\"hasAc\":%b,\"hasTv\":%b," +
                        "\"hasWifi\":%b,\"hasShower\":%b,\"hasHotwater\":%b,\"hasFridge\":%b}",
                rt.getId(), rt.getVillaId(), escapeJson(rt.getName()), rt.getQuantity(), rt.getCapacity(),
                rt.getPrice(), escapeJson(rt.getBedSize()), rt.isHasDesk(), rt.isHasAc(), rt.isHasTv(),
                rt.isHasWifi(), rt.isHasShower(), rt.isHasHotwater(), rt.isHasFridge());
    }
    private String convertRoomTypesToJson(List<RoomType> roomTypes) {
        return "[" + roomTypes.stream().map(this::convertRoomTypeToJson).collect(Collectors.joining(",")) + "]";
    }

    private String convertCustomerToJson(Customer customer) {
        if (customer == null) return "null";
        return String.format("{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\"}",
                customer.getId(), escapeJson(customer.getName()), escapeJson(customer.getEmail()), escapeJson(customer.getPhone()));
    }
    private String convertCustomersToJson(List<Customer> customers) {
        return "[" + customers.stream().map(this::convertCustomerToJson).collect(Collectors.joining(",")) + "]";
    }

    private String convertBookingToJson(Booking booking) {
        if (booking == null) return "null";
        return String.format("{\"id\":%d,\"customerId\":%d,\"roomTypeId\":%d,\"checkinDate\":\"%s\"," +
                        "\"checkoutDate\":\"%s\",\"price\":%d,\"voucherId\":%s,\"finalPrice\":%d," +
                        "\"paymentStatus\":\"%s\",\"hasCheckedIn\":%b,\"hasCheckedOut\":%b}",
                booking.getId(), booking.getCustomerId(), booking.getRoomTypeId(),
                escapeJson(booking.getCheckinDate()), escapeJson(booking.getCheckoutDate()), booking.getPrice(),
                booking.getVoucherId() != null ? booking.getVoucherId().toString() : "null", // Handle null voucherId
                booking.getFinalPrice(), escapeJson(booking.getPaymentStatus()),
                booking.isHasCheckedIn(), booking.isHasCheckedOut());
    }
    private String convertBookingsToJson(List<Booking> bookings) {
        return "[" + bookings.stream().map(this::convertBookingToJson).collect(Collectors.joining(",")) + "]";
    }

    private String convertReviewToJson(Review review) {
        if (review == null) return "null";
        return String.format("{\"bookingId\":%d,\"star\":%d,\"title\":\"%s\",\"content\":\"%s\"}",
                review.getBookingId(), review.getStar(), escapeJson(review.getTitle()), escapeJson(review.getContent()));
    }
    private String convertReviewsToJson(List<Review> reviews) {
        return "[" + reviews.stream().map(this::convertReviewToJson).collect(Collectors.joining(",")) + "]";
    }

    private String convertVoucherToJson(Voucher voucher) {
        if (voucher == null) return "null";
        return String.format("{\"id\":%d,\"code\":\"%s\",\"description\":\"%s\",\"discount\":%.2f,\"startDate\":\"%s\",\"endDate\":\"%s\"}",
                voucher.getId(), escapeJson(voucher.getCode()), escapeJson(voucher.getDescription()),
                voucher.getDiscount(), escapeJson(voucher.getStartDate()), escapeJson(voucher.getEndDate()));
    }
    private String convertVouchersToJson(List<Voucher> vouchers) {
        return "[" + vouchers.stream().map(this::convertVoucherToJson).collect(Collectors.joining(",")) + "]";
    }

    // --- Parsing JSON (JSON String to Java Object) ---
    private Villa parseJsonToVilla(String json) {
        try {
            String name = extractJsonValue(json, "name");
            String description = extractJsonValue(json, "description");
            String address = extractJsonValue(json, "address");
            return new Villa(0, name, description, address); // ID 0 karena akan di-generate DB
        } catch (Exception e) {
            System.err.println("Error parsing JSON to Villa: " + e.getMessage());
            return null;
        }
    }

    private RoomType parseJsonToRoomType(String json) {
        try {
            // villaId akan di-set dari path, jadi di sini 0 atau nilai default
            int villaId = 0;
            String name = extractJsonValue(json, "name");
            int quantity = Integer.parseInt(extractJsonValue(json, "quantity"));
            int capacity = Integer.parseInt(extractJsonValue(json, "capacity"));
            int price = Integer.parseInt(extractJsonValue(json, "price"));
            String bedSize = extractJsonValue(json, "bedSize");
            int hasDesk = Boolean.parseBoolean(extractJsonValue(json, "hasDesk")) ? 1 : 0;
            int hasAc = Boolean.parseBoolean(extractJsonValue(json, "hasAc")) ? 1 : 0;
            int hasTv = Boolean.parseBoolean(extractJsonValue(json, "hasTv")) ? 1 : 0;
            int hasWifi = Boolean.parseBoolean(extractJsonValue(json, "hasWifi")) ? 1 : 0;
            int hasShower = Boolean.parseBoolean(extractJsonValue(json, "hasShower")) ? 1 : 0;
            int hasHotwater = Boolean.parseBoolean(extractJsonValue(json, "hasHotwater")) ? 1 : 0;
            int hasFridge = Boolean.parseBoolean(extractJsonValue(json, "hasFridge")) ? 1 : 0;

            return new RoomType(0, villaId, name, quantity, capacity, price, bedSize,
                    hasDesk, hasAc, hasTv, hasWifi, hasShower, hasHotwater, hasFridge);
        } catch (Exception e) {
            System.err.println("Error parsing JSON to RoomType: " + e.getMessage());
            return null;
        }
    }

    private Customer parseJsonToCustomer(String json) {
        try {
            String name = extractJsonValue(json, "name");
            String email = extractJsonValue(json, "email");
            String phone = extractJsonValue(json, "phone");
            return new Customer(0, name, email, phone); // ID 0 karena akan di-generate DB
        } catch (Exception e) {
            System.err.println("Error parsing JSON to Customer: " + e.getMessage());
            return null;
        }
    }

    private Booking parseJsonToBooking(String json) {
        try {
            // customerId dan bookingId (jika POST bersarang) akan di-set dari path
            int customerId = 0;
            int roomTypeId = Integer.parseInt(extractJsonValue(json, "roomTypeId"));
            String checkinDate = extractJsonValue(json, "checkinDate");
            String checkoutDate = extractJsonValue(json, "checkoutDate");
            int price = Integer.parseInt(extractJsonValue(json, "price"));
            String voucherIdStr = extractJsonValue(json, "voucherId");
            Integer voucherId = (voucherIdStr != null && !voucherIdStr.equalsIgnoreCase("null")) ? Integer.parseInt(voucherIdStr) : null;
            int finalPrice = Integer.parseInt(extractJsonValue(json, "finalPrice"));
            String paymentStatus = extractJsonValue(json, "paymentStatus");
            int hasCheckedIn = Boolean.parseBoolean(extractJsonValue(json, "hasCheckedIn")) ? 1 : 0;
            int hasCheckedOut = Boolean.parseBoolean(extractJsonValue(json, "hasCheckedOut")) ? 1 : 0;

            return new Booking(0, customerId, roomTypeId, checkinDate, checkoutDate, price,
                    voucherId, finalPrice, paymentStatus, hasCheckedIn, hasCheckedOut);
        } catch (Exception e) {
            System.err.println("Error parsing JSON to Booking: " + e.getMessage());
            return null;
        }
    }

    private Review parseJsonToReview(String json) {
        try {
            // bookingId akan di-set dari path
            int bookingId = 0;
            int star = Integer.parseInt(extractJsonValue(json, "star"));
            String title = extractJsonValue(json, "title");
            String content = extractJsonValue(json, "content");
            return new Review(bookingId, star, title, content);
        } catch (Exception e) {
            System.err.println("Error parsing JSON to Review: " + e.getMessage());
            return null;
        }
    }

    private Voucher parseJsonToVoucher(String json) {
        try {
            String code = extractJsonValue(json, "code");
            String description = extractJsonValue(json, "description");
            double discount = Double.parseDouble(extractJsonValue(json, "discount"));
            String startDate = extractJsonValue(json, "startDate");
            String endDate = extractJsonValue(json, "endDate");
            return new Voucher(0, code, description, discount, startDate, endDate);
        } catch (Exception e) {
            System.err.println("Error parsing JSON to Voucher: " + e.getMessage());
            return null;
        }
    }


    // --- Handler Kelas untuk setiap Entitas ---

    class VillaHandler implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            System.out.println("Received " + method + " request for " + path);

            try {
                // Endpoint 1: GET /villas
                if ("GET".equals(method) && path.equals("/villas") && exchange.getRequestURI().getQuery() == null) {
                    handleGetAllVillas(exchange);
                }
                // Endpoint 6: GET /villas?ci_date={checkin_date}&co_date={checkout_date}
                else if ("GET".equals(method) && path.equals("/villas") && exchange.getRequestURI().getQuery() != null) {
                    handleSearchVillasByAvailability(exchange);
                }
                // Endpoint 2: GET /villas/{id}
                else if ("GET".equals(method) && path.matches("/villas/\\d+")) {
                    handleGetVillaById(exchange);
                }
                // Endpoint 3: GET /villas/{id}/rooms
                else if ("GET".equals(method) && path.matches("/villas/\\d+/rooms")) {
                    handleGetRoomsByVillaId(exchange);
                }
                // Endpoint 4: GET /villas/{id}/bookings
                else if ("GET".equals(method) && path.matches("/villas/\\d+/bookings")) {
                    handleGetBookingsByVillaId(exchange);
                }
                // Endpoint 5: GET /villas/{id}/reviews
                else if ("GET".equals(method) && path.matches("/villas/\\d+/reviews")) {
                    handleGetReviewsByVillaId(exchange);
                }
                // Endpoint 7: POST /villas
                else if ("POST".equals(method) && path.equals("/villas")) {
                    handleAddVilla(exchange);
                }
                // Endpoint 8: POST /villas/{id}/rooms
                else if ("POST".equals(method) && path.matches("/villas/\\d+/rooms")) {
                    handleAddRoomTypeToVilla(exchange);
                }
                // Endpoint 9: PUT /villas/{id}
                else if ("PUT".equals(method) && path.matches("/villas/\\d+")) {
                    handleUpdateVilla(exchange);
                }
                // Endpoint 10: PUT /villas/{id}/rooms/{id}
                else if ("PUT".equals(method) && path.matches("/villas/\\d+/rooms/\\d+")) {
                    handleUpdateRoomTypeInVilla(exchange);
                }
                // Endpoint 11: DELETE /villas/{id}/rooms/{id}
                else if ("DELETE".equals(method) && path.matches("/villas/\\d+/rooms/\\d+")) {
                    handleDeleteRoomTypeInVilla(exchange);
                }
                // Endpoint 12: DELETE /villas/{id}
                else if ("DELETE".equals(method) && path.matches("/villas/\\d+")) {
                    handleDeleteVilla(exchange);
                }
                else {
                    sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed or Invalid Path\"}", "application/json");
                }
            } catch (Exception e) {
                System.err.println("Error in VillaHandler: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"Internal Server Error\"}", "application/json");
            }
        }

        private void handleGetAllVillas(HttpExchange exchange) throws IOException {
            List<Villa> villas = villaDAO.getAllVillas();
            String jsonResponse = convertVillasToJson(villas);
            sendResponse(exchange, 200, jsonResponse, "application/json");
        }

        private void handleGetVillaById(HttpExchange exchange) throws IOException {
            int id = extractIdFromPath(exchange.getRequestURI().getPath(), "/villas/(\\d+)");
            if (id == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Villa ID\"}", "application/json"); return; }
            Villa villa = villaDAO.getVillaById(id);
            if (villa != null) {
                String jsonResponse = convertVillaToJson(villa);
                sendResponse(exchange, 200, jsonResponse, "application/json");
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Villa not found\"}", "application/json");
            }
        }

        private void handleGetRoomsByVillaId(HttpExchange exchange) throws IOException {
            int villaId = extractIdFromPath(exchange.getRequestURI().getPath(), "/villas/(\\d+)/rooms");
            if (villaId == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Villa ID\"}", "application/json"); return; }
            List<RoomType> roomTypes = roomTypeDAO.getRoomTypesByVillaId(villaId);
            String jsonResponse = convertRoomTypesToJson(roomTypes);
            sendResponse(exchange, 200, jsonResponse, "application/json");
        }

        private void handleGetBookingsByVillaId(HttpExchange exchange) throws IOException {
            int villaId = extractIdFromPath(exchange.getRequestURI().getPath(), "/villas/(\\d+)/bookings");
            if (villaId == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Villa ID\"}", "application/json"); return; }
            List<Booking> bookings = bookingDAO.getBookingsByVillaId(villaId);
            String jsonResponse = convertBookingsToJson(bookings);
            sendResponse(exchange, 200, jsonResponse, "application/json");
        }

        private void handleGetReviewsByVillaId(HttpExchange exchange) throws IOException {
            int villaId = extractIdFromPath(exchange.getRequestURI().getPath(), "/villas/(\\d+)/reviews");
            if (villaId == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Villa ID\"}", "application/json"); return; }
            List<Review> reviews = reviewDAO.getReviewsByVillaId(villaId);
            String jsonResponse = convertReviewsToJson(reviews);
            sendResponse(exchange, 200, jsonResponse, "application/json");
        }

        private void handleSearchVillasByAvailability(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI());
            String checkinDate = params.get("ci_date");
            String checkoutDate = params.get("co_date");

            if (checkinDate == null || checkinDate.isEmpty() || checkoutDate == null || checkoutDate.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\":\"Missing ci_date or co_date query parameters\"}", "application/json");
                return;
            }
            // Optional: Validasi format tanggal (YYYY-MM-DD hh:mm:ss)
            try {
                // Sederhana validasi format: Anda mungkin ingin menggunakan DateTimeFormatter yang lebih kuat
                if (!checkinDate.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}") ||
                        !checkoutDate.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                    sendResponse(exchange, 400, "{\"error\":\"Invalid date format. Use YYYY-MM-DD hh:mm:ss\"}", "application/json");
                    return;
                }
            } catch (Exception e) {
                // Ini catch generic, Anda bisa lebih spesifik dengan DateTimeParseException jika pakai java.time
                sendResponse(exchange, 400, "{\"error\":\"Invalid date format: " + e.getMessage() + "\"}", "application/json");
                return;
            }

            List<Villa> availableVillas = villaDAO.searchVillasByAvailability(checkinDate, checkoutDate);
            String jsonResponse = convertVillasToJson(availableVillas);
            sendResponse(exchange, 200, jsonResponse, "application/json");
        }

        private void handleAddVilla(HttpExchange exchange) throws IOException {
            String requestBody = readRequestBody(exchange);
            Villa newVilla = parseJsonToVilla(requestBody);
            if (newVilla != null && villaDAO.addVilla(newVilla)) {
                sendResponse(exchange, 201, "{\"message\":\"Villa added successfully\"}", "application/json");
            } else {
                sendResponse(exchange, 400, "{\"error\":\"Failed to add villa or invalid data\"}", "application/json");
            }
        }

        private void handleAddRoomTypeToVilla(HttpExchange exchange) throws IOException {
            int villaId = extractIdFromPath(exchange.getRequestURI().getPath(), "/villas/(\\d+)/rooms");
            if (villaId == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Villa ID\"}", "application/json"); return; }

            String requestBody = readRequestBody(exchange);
            RoomType newRoomType = parseJsonToRoomType(requestBody);
            if (newRoomType != null) {
                newRoomType.setVillaId(villaId); // Set villaId dari path
                if (roomTypeDAO.addRoomType(newRoomType)) {
                    sendResponse(exchange, 201, "{\"message\":\"Room Type added successfully to villa " + villaId + "\"}", "application/json");
                } else {
                    sendResponse(exchange, 400, "{\"error\":\"Failed to add room type or invalid data\"}", "application/json");
                }
            } else {
                sendResponse(exchange, 400, "{\"error\":\"Invalid data for room type\"}", "application/json");
            }
        }

        private void handleUpdateVilla(HttpExchange exchange) throws IOException {
            int id = extractIdFromPath(exchange.getRequestURI().getPath(), "/villas/(\\d+)");
            if (id == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Villa ID\"}", "application/json"); return; }
            String requestBody = readRequestBody(exchange);
            Villa updatedVilla = parseJsonToVilla(requestBody);
            if (updatedVilla != null) {
                updatedVilla.setId(id);
                if (villaDAO.updateVilla(updatedVilla)) {
                    sendResponse(exchange, 200, "{\"message\":\"Villa updated successfully\"}", "application/json");
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Villa not found or failed to update\"}", "application/json");
                }
            } else {
                sendResponse(exchange, 400, "{\"error\":\"Invalid data for update\"}", "application/json");
            }
        }

        private void handleUpdateRoomTypeInVilla(HttpExchange exchange) throws IOException {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("/villas/(\\d+)/rooms/(\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(exchange.getRequestURI().getPath());
            int villaId = -1;
            int roomTypeId = -1;
            if (matcher.find() && matcher.groupCount() == 2) {
                try {
                    villaId = Integer.parseInt(matcher.group(1));
                    roomTypeId = Integer.parseInt(matcher.group(2));
                } catch (NumberFormatException e) { /* handled by -1 defaults */ }
            }

            if (villaId == -1 || roomTypeId == -1) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid Villa ID or Room ID in path\"}", "application/json");
                return;
            }

            String requestBody = readRequestBody(exchange);
            RoomType updatedRoomType = parseJsonToRoomType(requestBody);
            if (updatedRoomType != null) {
                updatedRoomType.setId(roomTypeId); // Set ID kamar dari path
                updatedRoomType.setVillaId(villaId); // Pastikan villaId juga di-set dari path
                if (roomTypeDAO.updateRoomType(updatedRoomType)) {
                    sendResponse(exchange, 200, "{\"message\":\"Room Type updated successfully\"}", "application/json");
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Room Type not found or failed to update\"}", "application/json");
                }
            } else {
                sendResponse(exchange, 400, "{\"error\":\"Invalid data for room type update\"}", "application/json");
            }
        }

        private void handleDeleteRoomTypeInVilla(HttpExchange exchange) throws IOException {
            int roomTypeId = extractIdFromPath(exchange.getRequestURI().getPath(), "/villas/\\d+/rooms/(\\d+)");
            if (roomTypeId == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Room ID\"}", "application/json"); return; }

            if (roomTypeDAO.deleteRoomType(roomTypeId)) {
                sendResponse(exchange, 200, "{\"message\":\"Room Type deleted successfully\"}", "application/json");
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Room Type not found or failed to delete\"}", "application/json");
            }
        }

        private void handleDeleteVilla(HttpExchange exchange) throws IOException {
            int id = extractIdFromPath(exchange.getRequestURI().getPath(), "/villas/(\\d+)");
            if (id == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Villa ID\"}", "application/json"); return; }
            if (villaDAO.deleteVilla(id)) {
                sendResponse(exchange, 200, "{\"message\":\"Villa deleted successfully\"}", "application/json");
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Villa not found or failed to delete\"}", "application/json");
            }
        }
    }

    class CustomerHandler implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            System.out.println("Received " + method + " request for " + path);

            try {
                // Endpoint: GET /customers
                if ("GET".equals(method) && path.equals("/customers")) {
                    handleGetAllCustomers(exchange);
                }
                // Endpoint: GET /customers/{id}
                else if ("GET".equals(method) && path.matches("/customers/\\d+")) {
                    handleGetCustomerById(exchange);
                }
                // Endpoint: GET /customers/{id}/bookings
                else if ("GET".equals(method) && path.matches("/customers/\\d+/bookings")) {
                    handleGetBookingsByCustomerId(exchange);
                }
                // Endpoint: GET /customers/{id}/reviews
                else if ("GET".equals(method) && path.matches("/customers/\\d+/reviews")) {
                    handleGetReviewsByCustomerId(exchange);
                }
                // Endpoint: POST /customers
                else if ("POST".equals(method) && path.equals("/customers")) {
                    handleAddCustomer(exchange);
                }
                // Endpoint: POST /customers/{id}/bookings
                else if ("POST".equals(method) && path.matches("/customers/\\d+/bookings")) {
                    handleAddBookingForCustomer(exchange);
                }
                // Endpoint: POST /customers/{id}/bookings/{id}/reviews
                else if ("POST".equals(method) && path.matches("/customers/\\d+/bookings/\\d+/reviews")) {
                    handleAddReviewForBooking(exchange);
                }
                // Endpoint: PUT /customers/{id}
                else if ("PUT".equals(method) && path.matches("/customers/\\d+")) {
                    handleUpdateCustomer(exchange);
                }
                else {
                    sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed or Invalid Path\"}", "application/json");
                }
            } catch (Exception e) {
                System.err.println("Error in CustomerHandler: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"Internal Server Error\"}", "application/json");
            }
        }

        private void handleGetAllCustomers(HttpExchange exchange) throws IOException {
            List<Customer> customers = customerDAO.getAllCustomers(); // Anda perlu menambahkan ini ke CustomerDAO
            String jsonResponse = convertCustomersToJson(customers);
            sendResponse(exchange, 200, jsonResponse, "application/json");
        }

        private void handleGetCustomerById(HttpExchange exchange) throws IOException {
            int id = extractIdFromPath(exchange.getRequestURI().getPath(), "/customers/(\\d+)");
            if (id == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Customer ID\"}", "application/json"); return; }
            Customer customer = customerDAO.getCustomerById(id);
            if (customer != null) {
                String jsonResponse = convertCustomerToJson(customer);
                sendResponse(exchange, 200, jsonResponse, "application/json");
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Customer not found\"}", "application/json");
            }
        }

        private void handleGetBookingsByCustomerId(HttpExchange exchange) throws IOException {
            int customerId = extractIdFromPath(exchange.getRequestURI().getPath(), "/customers/(\\d+)/bookings");
            if (customerId == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Customer ID\"}", "application/json"); return; }
            List<Booking> bookings = bookingDAO.getBookingsByCustomerId(customerId);
            String jsonResponse = convertBookingsToJson(bookings);
            sendResponse(exchange, 200, jsonResponse, "application/json");
        }

        private void handleGetReviewsByCustomerId(HttpExchange exchange) throws IOException {
            int customerId = extractIdFromPath(exchange.getRequestURI().getPath(), "/customers/(\\d+)/reviews");
            if (customerId == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Customer ID\"}", "application/json"); return; }
            List<Review> reviews = reviewDAO.getReviewsByCustomerId(customerId);
            String jsonResponse = convertReviewsToJson(reviews);
            sendResponse(exchange, 200, jsonResponse, "application/json");
        }

        private void handleAddCustomer(HttpExchange exchange) throws IOException {
            String requestBody = readRequestBody(exchange);
            Customer newCustomer = parseJsonToCustomer(requestBody);
            if (newCustomer != null && customerDAO.addCustomer(newCustomer)) {
                sendResponse(exchange, 201, "{\"message\":\"Customer added successfully\"}", "application/json");
            } else {
                sendResponse(exchange, 400, "{\"error\":\"Failed to add customer or invalid data\"}", "application/json");
            }
        }

        private void handleAddBookingForCustomer(HttpExchange exchange) throws IOException {
            int customerId = extractIdFromPath(exchange.getRequestURI().getPath(), "/customers/(\\d+)/bookings");
            if (customerId == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Customer ID\"}", "application/json"); return; }

            String requestBody = readRequestBody(exchange);
            Booking newBooking = parseJsonToBooking(requestBody);
            if (newBooking != null) {
                newBooking.setCustomerId(customerId); // Set customerId dari path
                if (bookingDAO.addBooking(newBooking)) {
                    sendResponse(exchange, 201, "{\"message\":\"Booking added successfully for customer " + customerId + "\"}", "application/json");
                } else {
                    sendResponse(exchange, 400, "{\"error\":\"Failed to add booking or invalid data\"}", "application/json");
                }
            } else {
                sendResponse(exchange, 400, "{\"error\":\"Invalid data for booking\"}", "application/json");
            }
        }

        private void handleAddReviewForBooking(HttpExchange exchange) throws IOException {
            // Regex untuk menangkap customerId dan bookingId dari path
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("/customers/(\\d+)/bookings/(\\d+)/reviews");
            java.util.regex.Matcher matcher = pattern.matcher(exchange.getRequestURI().getPath());
            int customerId = -1; // unused, but kept for clarity if needed later
            int bookingId = -1;
            if (matcher.find() && matcher.groupCount() == 2) {
                try {
                    // Group 1: customerId, Group 2: bookingId
                    customerId = Integer.parseInt(matcher.group(1));
                    bookingId = Integer.parseInt(matcher.group(2));
                } catch (NumberFormatException e) { /* handled by -1 defaults */ }
            }

            if (customerId == -1 || bookingId == -1) { // Validate both IDs from path
                sendResponse(exchange, 400, "{\"error\":\"Invalid Customer ID or Booking ID in path\"}", "application/json");
                return;
            }

            String requestBody = readRequestBody(exchange);
            Review newReview = parseJsonToReview(requestBody);
            if (newReview != null) {
                newReview.setBookingId(bookingId); // Set bookingId dari path
                // Optional: validasi bahwa bookingId ini milik customerId yang benar
                // Anda mungkin perlu memanggil bookingDAO.getBookingById(bookingId)
                // dan memeriksa apakah customerId-nya cocok.
                if (reviewDAO.addReview(newReview)) {
                    sendResponse(exchange, 201, "{\"message\":\"Review added successfully for booking " + bookingId + "\"}", "application/json");
                } else {
                    sendResponse(exchange, 400, "{\"error\":\"Failed to add review or invalid data\"}", "application/json");
                }
            } else {
                sendResponse(exchange, 400, "{\"error\":\"Invalid data for review\"}", "application/json");
            }
        }

        private void handleUpdateCustomer(HttpExchange exchange) throws IOException {
            int id = extractIdFromPath(exchange.getRequestURI().getPath(), "/customers/(\\d+)");
            if (id == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Customer ID\"}", "application/json"); return; }
            String requestBody = readRequestBody(exchange);
            Customer updatedCustomer = parseJsonToCustomer(requestBody);
            if (updatedCustomer != null) {
                updatedCustomer.setId(id);
                if (customerDAO.updateCustomer(updatedCustomer)) {
                    sendResponse(exchange, 200, "{\"message\":\"Customer updated successfully\"}", "application/json");
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Customer not found or failed to update\"}", "application/json");
                }
            } else {
                sendResponse(exchange, 400, "{\"error\":\"Invalid data for update\"}", "application/json");
            }
        }
    }

    class BookingHandler implements com.sun.net.httpserver.HttpHandler {
        // PERHATIAN: Peran BookingHandler dan RoomTypeHandler dikurangi
        // karena sebagian besar logika mereka digabungkan ke VillaHandler dan CustomerHandler.
        // Jika Anda masih ingin endpoint seperti /bookings/{id} secara langsung, tetap pertahankan ini.
        // Namun, jika semua interaksi booking/room_type melalui /villas/{id}/... atau /customers/{id}/...
        // maka Anda bisa menghapus context("/bookings", new BookingHandler()) dari startServer
        // dan menghapus class BookingHandler ini.

        // Saya akan menyertakan kode minimal di sini jika Anda ingin mempertahankannya:

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            System.out.println("Received " + method + " request for " + path);

            try {
                // Endpoint GET /bookings/{id} (jika diperlukan terpisah dari /customers/{id}/bookings)
                if ("GET".equals(method) && path.matches("/bookings/\\d+")) {
                    handleGetBookingById(exchange);
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed or Invalid Path\"}", "application/json");
                }
            } catch (Exception e) {
                System.err.println("Error in BookingHandler: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"Internal Server Error\"}", "application/json");
            }
        }

        private void handleGetBookingById(HttpExchange exchange) throws IOException {
            int id = extractIdFromPath(exchange.getRequestURI().getPath(), "/bookings/(\\d+)");
            if (id == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Booking ID\"}", "application/json"); return; }
            Booking booking = bookingDAO.getBookingById(id);
            if (booking != null) {
                String jsonResponse = convertBookingToJson(booking);
                sendResponse(exchange, 200, jsonResponse, "application/json");
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Booking not found\"}", "application/json");
            }
        }
    }

    class ReviewHandler implements com.sun.net.httpserver.HttpHandler {
        // Mirip dengan BookingHandler, ReviewHandler ini hanya diperlukan
        // jika ada endpoint langsung ke /reviews/{id} atau /reviews
        // Jika semua review diakses melalui /villas/{id}/reviews atau /customers/{id}/reviews
        // maka Anda bisa menghapus context("/reviews", new ReviewHandler()) dari startServer
        // dan menghapus class ReviewHandler ini.

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            System.out.println("Received " + method + " request for " + path);

            try {
                // Contoh: GET /reviews/{bookingId}
                if ("GET".equals(method) && path.matches("/reviews/\\d+")) {
                    handleGetReviewById(exchange); // Perlu metode getReviewById di ReviewDAO
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed or Invalid Path\"}", "application/json");
                }
            } catch (Exception e) {
                System.err.println("Error in ReviewHandler: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"Internal Server Error\"}", "application/json");
            }
        }

        private void handleGetReviewById(HttpExchange exchange) throws IOException {
            int bookingId = extractIdFromPath(exchange.getRequestURI().getPath(), "/reviews/(\\d+)");
            if (bookingId == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Booking ID for Review\"}", "application/json"); return; }
            // Anda perlu menambahkan getReviewById(int bookingId) di ReviewDAO
            // public Review getReviewById(int bookingId) { ... }
            Review review = reviewDAO.getReviewById(bookingId); // Ini perlu diimplementasikan di ReviewDAO

            if (review != null) {
                String jsonResponse = convertReviewToJson(review);
                sendResponse(exchange, 200, jsonResponse, "application/json");
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Review not found\"}", "application/json");
            }
        }
    }

    class VoucherHandler implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            System.out.println("Received " + method + " request for " + path);

            try {
                // Endpoint: GET /vouchers
                if ("GET".equals(method) && path.equals("/vouchers")) {
                    handleGetAllVouchers(exchange);
                }
                // Endpoint: GET /vouchers/{id}
                else if ("GET".equals(method) && path.matches("/vouchers/\\d+")) {
                    handleGetVoucherById(exchange);
                }
                // Endpoint: POST /vouchers
                else if ("POST".equals(method) && path.equals("/vouchers")) {
                    handleAddVoucher(exchange);
                }
                // Endpoint: PUT /vouchers/{id}
                else if ("PUT".equals(method) && path.matches("/vouchers/\\d+")) {
                    handleUpdateVoucher(exchange);
                }
                // Endpoint: DELETE /vouchers/{id}
                else if ("DELETE".equals(method) && path.matches("/vouchers/\\d+")) {
                    handleDeleteVoucher(exchange);
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed or Invalid Path\"}", "application/json");
                }
            } catch (Exception e) {
                System.err.println("Error in VoucherHandler: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"Internal Server Error\"}", "application/json");
            }
        }

        private void handleGetAllVouchers(HttpExchange exchange) throws IOException {
            List<Voucher> vouchers = voucherDAO.getAllVouchers();
            String jsonResponse = convertVouchersToJson(vouchers);
            sendResponse(exchange, 200, jsonResponse, "application/json");
        }

        private void handleGetVoucherById(HttpExchange exchange) throws IOException {
            int id = extractIdFromPath(exchange.getRequestURI().getPath(), "/vouchers/(\\d+)");
            if (id == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Voucher ID\"}", "application/json"); return; }
            Voucher voucher = voucherDAO.getVoucherById(id);
            if (voucher != null) {
                String jsonResponse = convertVoucherToJson(voucher);
                sendResponse(exchange, 200, jsonResponse, "application/json");
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Voucher not found\"}", "application/json");
            }
        }

        private void handleAddVoucher(HttpExchange exchange) throws IOException {
            String requestBody = readRequestBody(exchange);
            Voucher newVoucher = parseJsonToVoucher(requestBody);
            if (newVoucher != null && voucherDAO.addVoucher(newVoucher)) {
                sendResponse(exchange, 201, "{\"message\":\"Voucher added successfully\"}", "application/json");
            } else {
                sendResponse(exchange, 400, "{\"error\":\"Failed to add voucher or invalid data\"}", "application/json");
            }
        }

        private void handleUpdateVoucher(HttpExchange exchange) throws IOException {
            int id = extractIdFromPath(exchange.getRequestURI().getPath(), "/vouchers/(\\d+)");
            if (id == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Voucher ID\"}", "application/json"); return; }
            String requestBody = readRequestBody(exchange);
            Voucher updatedVoucher = parseJsonToVoucher(requestBody);
            if (updatedVoucher != null) {
                updatedVoucher.setId(id);
                if (voucherDAO.updateVoucher(updatedVoucher)) {
                    sendResponse(exchange, 200, "{\"message\":\"Voucher updated successfully\"}", "application/json");
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Voucher not found or failed to update\"}", "application/json");
                }
            } else {
                sendResponse(exchange, 400, "{\"error\":\"Invalid data for update\"}", "application/json");
            }
        }

        private void handleDeleteVoucher(HttpExchange exchange) throws IOException {
            int id = extractIdFromPath(exchange.getRequestURI().getPath(), "/vouchers/(\\d+)");
            if (id == -1) { sendResponse(exchange, 400, "{\"error\":\"Invalid Voucher ID\"}", "application/json"); return; }
            if (voucherDAO.deleteVoucher(id)) {
                sendResponse(exchange, 200, "{\"message\":\"Voucher deleted successfully\"}", "application/json");
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Voucher not found or failed to delete\"}", "application/json");
            }
        }
    }
}

