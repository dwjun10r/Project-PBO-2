
package server.router;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.Request;
import server.Response;
import server.handlers.CustomerHandler;
import server.handlers.VillaHandler;
import server.handlers.VoucherHandler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Router implements HttpHandler {

    // API Key yang di-hardcode (dari dosen Anda)
    private static final String API_KEY = "PBO123"; //

    // Instansiasi semua handler Anda di sini
    private final VillaHandler villaHandler = new VillaHandler();
    private final CustomerHandler customerHandler = new CustomerHandler();
    private final VoucherHandler voucherHandler = new VoucherHandler();
    // Jika Anda punya BookingHandler atau ReviewHandler terpisah (bukan nested di Villa/CustomerHandler),
    // instansiasi juga di sini. Contoh:
    // private final BookingHandler bookingHandler = new BookingHandler();
    // private final ReviewHandler reviewHandler = new ReviewHandler();

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
            // Autentikasi API Key
            String providedApiKey = req.getHttpExchange().getRequestHeaders()
                    .getFirst("X-API-Key");
            if (providedApiKey == null || !providedApiKey.equals(API_KEY)) {
                res.sendError(HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized: Invalid API Key");
                return;
            }

            // --- Logic Routing ---
            // Villa Endpoints
            if (path.startsWith("/villas")) {
                villaHandler.handle(httpExchange); // Delegasikan ke VillaHandler
            }
            // Customer Endpoints
            else if (path.startsWith("/customers")) {
                customerHandler.handle(httpExchange); // Delegasikan ke CustomerHandler
            }
            // Voucher Endpoints
            else if (path.startsWith("/vouchers")) {
                voucherHandler.handle(httpExchange); // Delegasikan ke VoucherHandler
            }
            // Handle root path
            else if (path.equals("/")) {
                res.sendJson(HttpURLConnection.HTTP_OK, Map.of("message", "Welcome to the Villa Booking API! Available endpoints: /villas, /customers, /vouchers"));
            }
            // Default handling for unmatched paths
            else {
                res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found");
            }

        } catch (Exception e) {
            System.err.println("Error processing request: " + e.getMessage());
            e.printStackTrace();
            res.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal Server Error: " + e.getMessage());
        } finally {
            // httpExchange.close() sudah dihandle di Response.send() dan sendError/sendJson
            // Pastikan tidak ada resource yang bocor jika respons belum terkirim karena suatu error
            if (!res.isSent()) {
                httpExchange.close();
            }
        }
    }

    // Utility methods (bisa dipindahkan ke Request/Response atau kelas util terpisah jika lebih sering dipakai)
    // Untuk saat ini, bisa diletakkan di sini atau di masing-masing handler jika spesifik
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