package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Import DAO dan Model Anda
import config.DbConnection; // Pastikan ini benar
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

public class Server {
    private HttpServer server;

    // ObjectMapper untuk Jackson (digunakan untuk konversi JSON)
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Instansiasi DAO (karena processHttpExchange statis, DAO harus statis atau di-passing)
    // Paling mudah adalah instansiasi di sini sebagai statis
    private static VillaDAO villaDAO = new VillaDAO();
    private static RoomTypeDAO roomTypeDAO = new RoomTypeDAO();
    private static CustomerDAO customerDAO = new CustomerDAO();
    private static BookingDAO bookingDAO = new BookingDAO();
    private static ReviewDAO reviewDAO = new ReviewDAO();
    private static VoucherDAO voucherDAO = new VoucherDAO();


    private class RequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) {
            Server.processHttpExchange(httpExchange);
        }
    }

    public Server(int port) throws Exception {
        server = HttpServer.create(new InetSocketAddress(port), 128);
        server.createContext("/", new RequestHandler()); // Semua request akan ke RequestHandler ini
        server.start();
        System.out.println("Server started on port " + port);
    }

    public static void processHttpExchange(HttpExchange httpExchange) {
        Request req = new Request(httpExchange);
        Response res = new Response(httpExchange);

        URI uri = httpExchange.getRequestURI();
        String path = uri.getPath();
        String method = req.getRequestMethod();
        String query = uri.getQuery();

        System.out.printf("Received %s request for path: %s, query: %s\n", method, path, query);

        try {
            // --- Logic Routing Anda dimulai di sini ---
            // Endpoint untuk Villa
            if ("GET".equals(method) && path.equals("/villas")) {
                if (query != null) { // GET /villas?ci_date=...&co_date=...
                    Map<String, String> params = parseQueryParams(uri);
                    String checkinDate = params.get("ci_date");
                    String checkoutDate = params.get("co_date");

                    if (checkinDate == null || checkoutDate == null || checkinDate.isEmpty() || checkoutDate.isEmpty()) {
                        sendErrorResponse(res, HttpURLConnection.HTTP_BAD_REQUEST, "Missing ci_date or co_date query parameters");
                        return;
                    }
                    List<Villa> villas = villaDAO.searchVillasByAvailability(checkinDate, checkoutDate);
                    res.setBody(objectMapper.writeValueAsString(villas));
                    res.send(HttpURLConnection.HTTP_OK);
                } else { // GET /villas
                    List<Villa> villas = villaDAO.getAllVillas();
                    res.setBody(objectMapper.writeValueAsString(villas));
                    res.send(HttpURLConnection.HTTP_OK);
                }
            } else if ("GET".equals(method) && path.matches("/villas/\\d+")) { // GET /villas/{id}
                int id = extractIdFromPath(path, "/villas/(\\d+)");
                if (id == -1) { sendErrorResponse(res, HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Villa ID"); return; }
                Villa villa = villaDAO.getVillaById(id);
                if (villa != null) {
                    res.setBody(objectMapper.writeValueAsString(villa));
                    res.send(HttpURLConnection.HTTP_OK);
                } else {
                    sendErrorResponse(res, HttpURLConnection.HTTP_NOT_FOUND, "Villa not found");
                }
            } else if ("POST".equals(method) && path.equals("/villas")) { // POST /villas
                Map<String, Object> reqJsonMap = req.getJSON();
                Villa newVilla = new Villa(0, (String) reqJsonMap.get("name"), (String) reqJsonMap.get("description"), (String) reqJsonMap.get("address"));
                if (villaDAO.addVilla(newVilla)) {
                    sendSuccessMessage(res, HttpURLConnection.HTTP_CREATED, "Villa added successfully");
                } else {
                    sendErrorResponse(res, HttpURLConnection.HTTP_BAD_REQUEST, "Failed to add villa or invalid data");
                }
            }
            // ... Tambahkan endpoint Villa lainnya (PUT, DELETE, /villas/{id}/rooms, bookings, reviews)
            else if ("DELETE".equals(method) && path.matches("/villas/\\d+")) { // DELETE /villas/{id}
                int id = extractIdFromPath(path, "/villas/(\\d+)");
                if (id == -1) { sendErrorResponse(res, HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Villa ID"); return; }
                if (villaDAO.deleteVilla(id)) {
                    sendSuccessMessage(res, HttpURLConnection.HTTP_OK, "Villa deleted successfully");
                } else {
                    sendErrorResponse(res, HttpURLConnection.HTTP_NOT_FOUND, "Villa not found or failed to delete");
                }
            }
            // --- Endpoint untuk Customer ---
            else if ("POST".equals(method) && path.equals("/customers")) { // POST /customers
                Map<String, Object> reqJsonMap = req.getJSON();
                Customer newCustomer = new Customer(0, (String) reqJsonMap.get("name"), (String) reqJsonMap.get("email"), (String) reqJsonMap.get("phone"));
                if (customerDAO.addCustomer(newCustomer)) {
                    sendSuccessMessage(res, HttpURLConnection.HTTP_CREATED, "Customer added successfully");
                } else {
                    sendErrorResponse(res, HttpURLConnection.HTTP_BAD_REQUEST, "Failed to add customer or invalid data");
                }
            } else if ("GET".equals(method) && path.matches("/customers/\\d+")) { // GET /customers/{id}
                int id = extractIdFromPath(path, "/customers/(\\d+)");
                if (id == -1) { sendErrorResponse(res, HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Customer ID"); return; }
                Customer customer = customerDAO.getCustomerById(id);
                if (customer != null) {
                    res.setBody(objectMapper.writeValueAsString(customer));
                    res.send(HttpURLConnection.HTTP_OK);
                } else {
                    sendErrorResponse(res, HttpURLConnection.HTTP_NOT_FOUND, "Customer not found");
                }
            }
            // ... Tambahkan endpoint Customer lainnya ...
            // --- Endpoint untuk Booking ---
            else if ("POST".equals(method) && path.matches("/customers/\\d+/bookings")) { // POST /customers/{id}/bookings
                Pattern pattern = Pattern.compile("/customers/(\\d+)/bookings");
                Matcher matcher = pattern.matcher(path);
                int customerId = -1;
                if (matcher.find() && matcher.groupCount() == 1) {
                    customerId = Integer.parseInt(matcher.group(1));
                }
                if (customerId == -1) { sendErrorResponse(res, HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Customer ID in path"); return; }

                Map<String, Object> reqJsonMap = req.getJSON();
                // Pastikan semua field yang dibutuhkan ada dan tipenya benar
                Booking newBooking = new Booking(
                        0, // ID otomatis
                        customerId, // Dari path
                        (Integer) reqJsonMap.get("roomTypeId"),
                        (String) reqJsonMap.get("checkinDate"),
                        (String) reqJsonMap.get("checkoutDate"),
                        (Integer) reqJsonMap.get("price"),
                        (Integer) reqJsonMap.get("voucherId"), // Bisa null
                        (Integer) reqJsonMap.get("finalPrice"),
                        (String) reqJsonMap.get("paymentStatus"),
                        (Boolean) reqJsonMap.get("hasCheckedIn") ? 1 : 0,
                        (Boolean) reqJsonMap.get("hasCheckedOut") ? 1 : 0
                );

                if (bookingDAO.addBooking(newBooking)) {
                    sendSuccessMessage(res, HttpURLConnection.HTTP_CREATED, "Booking added successfully for customer " + customerId);
                } else {
                    sendErrorResponse(res, HttpURLConnection.HTTP_BAD_REQUEST, "Failed to add booking or invalid data");
                }
            }
            // --- Endpoint untuk Review ---
            else if ("POST".equals(method) && path.matches("/customers/\\d+/bookings/\\d+/reviews")) { // POST /customers/{id}/bookings/{id}/reviews
                Pattern pattern = Pattern.compile("/customers/\\d+/bookings/(\\d+)/reviews");
                Matcher matcher = pattern.matcher(path);
                int bookingId = -1;
                if (matcher.find() && matcher.groupCount() == 1) {
                    bookingId = Integer.parseInt(matcher.group(1));
                }
                if (bookingId == -1) { sendErrorResponse(res, HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Booking ID in path"); return; }

                Map<String, Object> reqJsonMap = req.getJSON();
                Review newReview = new Review(
                        bookingId, // Dari path
                        (Integer) reqJsonMap.get("star"),
                        (String) reqJsonMap.get("title"),
                        (String) reqJsonMap.get("content")
                );

                if (reviewDAO.addReview(newReview)) {
                    sendSuccessMessage(res, HttpURLConnection.HTTP_CREATED, "Review added successfully for booking " + bookingId);
                } else {
                    sendErrorResponse(res, HttpURLConnection.HTTP_BAD_REQUEST, "Failed to add review or invalid data");
                }
            }
            // --- Endpoint default jika tidak ada yang cocok ---
            else {
                sendErrorResponse(res, HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            System.err.println("Error processing request: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(res, HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal Server Error: " + e.getMessage());
        }

        // Pastikan respons selalu dikirim
        if (!res.isSent()) {
            httpExchange.close(); // Tutup koneksi jika respons belum dikirim
        }
    }

    // --- Utility methods untuk parseQueryParams dan extractIdFromPath (dipindahkan dari SimpleHttpServer lama) ---
    private static Map<String, String> parseQueryParams(URI uri) {
        String query = uri.getQuery();
        if (query == null || query.isEmpty()) {
            return Map.of();
        }
        return java.util.Arrays.stream(query.split("&"))
                .map(s -> s.split("="))
                .filter(a -> a.length == 2)
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));
    }

    private static int extractIdFromPath(String path, String regexPattern) {
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

    // --- Utility methods untuk mengirim pesan sukses/error ---
    private static void sendSuccessMessage(Response res, int statusCode, String message) throws JsonProcessingException {
        Map<String, Object> resJsonMap = new HashMap<>();
        resJsonMap.put("message", message);
        res.setBody(objectMapper.writeValueAsString(resJsonMap));
        res.send(statusCode);
    }

    private static void sendErrorResponse(Response res, int statusCode, String errorMessage) throws JsonProcessingException {
        Map<String, Object> resJsonMap = new HashMap<>();
        resJsonMap.put("error", errorMessage);
        res.setBody(objectMapper.writeValueAsString(resJsonMap));
        res.send(statusCode);
    }
}