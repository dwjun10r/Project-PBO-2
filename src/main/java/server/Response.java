package server; // Pastikan ini package yang benar

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper; // Tambahkan import ini

import java.io.IOException;
import java.io.OutputStream; // Ganti PrintStream dengan OutputStream
import java.nio.charset.StandardCharsets;
import java.util.HashMap; // Tambahkan import ini
import java.util.Map; // Tambahkan import ini

public class Response {

    private HttpExchange httpExchange;
    private StringBuilder stringBuilder;
    private boolean isSent;
    private static final ObjectMapper objectMapper = new ObjectMapper(); // Static ObjectMapper

    public Response(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
        this.stringBuilder = new StringBuilder();
        this.isSent = false;
    }

    public void setBody(String string) {
        stringBuilder.setLength(0);
        stringBuilder.append(string);
    }

    // Metode baru untuk mengirim JSON langsung
    public void sendJson(int status, Object data) throws IOException {
        this.httpExchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        String jsonBody = objectMapper.writeValueAsString(data);
        this.httpExchange.sendResponseHeaders(status, jsonBody.length()); // Gunakan panjang byte JSON

        try (OutputStream os = this.httpExchange.getResponseBody()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        } finally {
            this.httpExchange.close();
        }
        this.isSent = true;
    }

    // Modifikasi metode send yang ada
    public void send(int status) {
        try {
            this.httpExchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8"); // Default JSON
            String body = stringBuilder.toString();
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            this.httpExchange.sendResponseHeaders(status, bytes.length); // Gunakan panjang byte

            try (OutputStream out = this.httpExchange.getResponseBody()) { // Gunakan OutputStream
                out.write(bytes);
            }
        } catch (IOException ioe) {
            System.err.println("Problem encountered when sending response.");
            ioe.printStackTrace();
        } finally {
            this.httpExchange.close();
        }
        this.isSent = true;
    }

    public boolean isSent() {
        if (this.httpExchange.getResponseCode() != -1)
            this.isSent = true;
        return isSent;
    }

    // Utility method untuk mengirim pesan error JSON
    public void sendError(int statusCode, String errorMessage) throws IOException {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", errorMessage);
        sendJson(statusCode, errorResponse);
    }

    // Utility method untuk mengirim pesan sukses JSON
    public void sendSuccess(int statusCode, String message) throws IOException {
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", message);
        sendJson(statusCode, successResponse);
    }
}