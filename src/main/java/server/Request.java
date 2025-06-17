package server; // Pastikan ini package yang benar

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Request {
    private final HttpExchange httpExchange;
    private Headers headers;
    private String rawBody;
    private Map<String, Object> jsonBodyMap; // Menggunakan Map langsung untuk JSON

    private static final ObjectMapper objectMapper = new ObjectMapper(); // Static ObjectMapper

    public Request(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
        this.headers = httpExchange.getRequestHeaders();
    }

    public String getBody() {
        if (this.rawBody == null) {
            this.rawBody = new BufferedReader(
                    new InputStreamReader(httpExchange.getRequestBody(), StandardCharsets.UTF_8)
            )
                    .lines()
                    .collect(Collectors.joining("\n"));
        }
        return this.rawBody;
    }

    public String getRequestMethod() {
        return httpExchange.getRequestMethod();
    }

    public String getContentType() {
        return headers.getFirst("Content-Type");
    }

    public Map<String, Object> getJSON() throws JsonProcessingException {
        // Hati-hati dengan !getContentType().equalsIgnoreCase("application/json")
        // Jika body kosong atau tidak ada Content-Type, ini bisa mengembalikan null
        // Anda mungkin ingin melempar exception atau mengembalikan Map kosong
        if (this.jsonBodyMap == null) {
            if (getContentType() != null && getContentType().toLowerCase().contains("application/json")) {
                String body = getBody();
                if (body != null && !body.isEmpty()) {
                    this.jsonBodyMap = objectMapper.readValue(body, new TypeReference<Map<String, Object>>(){});
                } else {
                    this.jsonBodyMap = new HashMap<>(); // Body JSON kosong
                }
            } else {
                this.jsonBodyMap = new HashMap<>(); // Bukan JSON, kembalikan Map kosong
            }
        }
        return this.jsonBodyMap;
    }
}