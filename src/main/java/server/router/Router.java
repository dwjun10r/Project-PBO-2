
package server.router;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.BookingDAO; // Import semua DAO
import dao.CustomerDAO;
import dao.ReviewDAO;
import dao.RoomTypeDAO;
import dao.VillaDAO;
import dao.VoucherDAO;
import server.Request;
import server.Response;
import server.handlers.CustomerHandler;
import server.handlers.VillaHandler;
import server.handlers.VoucherHandler;
import services.CustomerService; // Import semua Service
import services.VillaService;
import services.VoucherService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Router implements HttpHandler {

    private static final String API_KEY = "PBO123";

    private final BookingDAO bookingDAO = new BookingDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final RoomTypeDAO roomTypeDAO = new RoomTypeDAO();
    private final VillaDAO villaDAO = new VillaDAO();
    private final VoucherDAO voucherDAO = new VoucherDAO();

    private final CustomerService customerService = new CustomerService(customerDAO, bookingDAO, reviewDAO);
    private final VillaService villaService = new VillaService(villaDAO, roomTypeDAO, bookingDAO, reviewDAO);
    private final VoucherService voucherService = new VoucherService(voucherDAO);

    private final VillaHandler villaHandler = new VillaHandler(villaService);
    private final CustomerHandler customerHandler = new CustomerHandler(customerService);
    private final VoucherHandler voucherHandler = new VoucherHandler(voucherService);

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Request req = new Request(httpExchange);
        Response res = new Response(httpExchange);
        URI uri = httpExchange.getRequestURI();
        String path = uri.getPath();
        String method = req.getRequestMethod();
        String query = uri.getQuery();
        System.out.printf("Received %s request for path: %s, query: %s\n", method, path, query);

        try {
            String providedApiKey = req.getHttpExchange().getRequestHeaders().getFirst("X-API-Key");
            if (providedApiKey == null || !providedApiKey.equals(API_KEY)) {
                res.sendError(HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized: Invalid API Key");
                return;
            }
            if (path.startsWith("/villas")) {
                villaHandler.handle(httpExchange);
            } else if (path.startsWith("/customers")) {
                customerHandler.handle(httpExchange);
            } else if (path.startsWith("/vouchers")) {
                voucherHandler.handle(httpExchange);
            } else if (path.equals("/")) { // Handle root path
                res.sendJson(HttpURLConnection.HTTP_OK, Map.of("message", "Welcome to the Villa Booking API! Available endpoints: /villas, /customers, /vouchers"));
            } else {
                res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            System.err.println("Error processing request: " + e.getMessage());
            e.printStackTrace();
            res.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal Server Error: " + e.getMessage());
        } finally {
            if (!res.isSent()) {
                httpExchange.close();
            }
        }
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

    private Map<String, String> parseQueryParams(URI uri) {
        String query = uri.getQuery();
        if (query == null || query.isEmpty()) {
            return Map.of();
        }
        return java.util.Arrays.stream(query.split("&"))
                .map(s -> s.split("="))
                .filter(a -> a.length == 2)
                .collect(java.util.stream.Collectors.toMap(a -> a[0], a -> a[1]));
    }
}
