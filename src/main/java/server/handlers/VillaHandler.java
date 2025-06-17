package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.Booking;
import models.RoomType;
import models.Villa;
import models.Review;
import server.Request;
import server.Response;
import services.VillaService; // Import service baru

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VillaHandler implements HttpHandler {

    private final VillaService villaService;

    public VillaHandler(VillaService villaService) {
        this.villaService = villaService;
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

    private Map<String, String> parseQueryParams(java.net.URI uri) {
        String query = uri.getQuery();
        if (query == null || query.isEmpty()) {
            return Map.of();
        }
        return java.util.Arrays.stream(query.split("&"))
                .map(s -> s.split("="))
                .filter(a -> a.length == 2)
                .collect(java.util.stream.Collectors.toMap(a -> a[0], a -> a[1]));
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Request req = new Request(httpExchange);
        Response res = new Response(httpExchange);
        String path = httpExchange.getRequestURI().getPath();
        String method = req.getRequestMethod();
        String query = httpExchange.getRequestURI().getQuery();

        try {
            if ("GET".equals(method)) {
                if (path.equals("/villas")) {
                    if (query != null && query.contains("ci_date") && query.contains("co_date")) {
                        handleSearchVillasByAvailability(req, res);
                    } else {
                        handleGetAllVillas(res);
                    }
                } else if (path.matches("/villas/\\d+")) {
                    handleGetVillaById(req, res);
                } else if (path.matches("/villas/\\d+/rooms")) {
                    handleGetRoomsByVillaId(req, res);
                } else if (path.matches("/villas/\\d+/bookings")) {
                    handleGetBookingsByVillaId(req, res);
                } else if (path.matches("/villas/\\d+/reviews")) {
                    handleGetReviewsByVillaId(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for GET on Villa");
                }
            } else if ("POST".equals(method)) {
                if (path.equals("/villas")) {
                    handleAddVilla(req, res);
                } else if (path.matches("/villas/\\d+/rooms")) {
                    handleAddRoomTypeToVilla(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for POST on Villa");
                }
            } else if ("PUT".equals(method)) {
                if (path.matches("/villas/\\d+")) {
                    handleUpdateVilla(req, res);
                } else if (path.matches("/villas/\\d+/rooms/\\d+")) {
                    handleUpdateRoomTypeInVilla(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for PUT on Villa");
                }
            } else if ("DELETE".equals(method)) {
                if (path.matches("/villas/\\d+/rooms/\\d+")) {
                    handleDeleteRoomTypeInVilla(req, res);
                } else if (path.matches("/villas/\\d+")) {
                    handleDeleteVilla(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for DELETE on Villa");
                }
            } else {
                res.sendError(405, "Method Not Allowed");
            }
        } catch (Exception e) {
            System.err.println("Error in VillaHandler: " + e.getMessage());
            e.printStackTrace();
            res.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGetAllVillas(Response res) throws IOException {
        List<Villa> villas = villaService.getAllVillas();
        res.sendJson(HttpURLConnection.HTTP_OK, villas);
    }

    private void handleGetVillaById(Request req, Response res) throws IOException {
        int id = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/villas/(\\d+)");
        if (id == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Villa ID");
            return;
        }
        Villa villa = villaService.getVillaById(id);
        if (villa != null) {
            res.sendJson(HttpURLConnection.HTTP_OK, villa);
        } else {
            res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Villa not found");
        }
    }

    private void handleGetRoomsByVillaId(Request req, Response res) throws IOException {
        int villaId = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/villas/(\\d+)/rooms");
        if (villaId == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Villa ID");
            return;
        }
        List<RoomType> roomTypes = villaService.getRoomTypesByVillaId(villaId);
        res.sendJson(HttpURLConnection.HTTP_OK, roomTypes);
    }

    private void handleGetBookingsByVillaId(Request req, Response res) throws IOException {
        int villaId = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/villas/(\\d+)/bookings");
        if (villaId == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Villa ID");
            return;
        }
        List<Booking> bookings = villaService.getBookingsByVillaId(villaId);
        res.sendJson(HttpURLConnection.HTTP_OK, bookings);
    }

    private void handleGetReviewsByVillaId(Request req, Response res) throws IOException {
        int villaId = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/villas/(\\d+)/reviews");
        if (villaId == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Villa ID");
            return;
        }
        List<Review> reviews = villaService.getReviewsByVillaId(villaId);
        res.sendJson(HttpURLConnection.HTTP_OK, reviews);
    }

    private void handleSearchVillasByAvailability(Request req, Response res) throws IOException {
        Map<String, String> params = parseQueryParams(req.getHttpExchange().getRequestURI());
        String checkinDate = params.get("ci_date");
        String checkoutDate = params.get("co_date");

        List<Villa> availableVillas = villaService.searchVillasByAvailability(checkinDate, checkoutDate);

        if (availableVillas != null) {
            res.sendJson(HttpURLConnection.HTTP_OK, availableVillas);
        } else {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Missing or invalid date format for ci_date or co_date query parameters. Use YYYY-MM-DD hh:mm:ss.");
        }
    }

    private void handleAddVilla(Request req, Response res) throws IOException {
        Map<String, Object> reqJsonMap = req.getJSON();
        String name = (String) reqJsonMap.get("name");
        String description = (String) reqJsonMap.get("description");
        String address = (String) reqJsonMap.get("address");

        Villa newVilla = new Villa(0, name, description, address);

        if (villaService.addVilla(newVilla)) {
            res.sendSuccess(HttpURLConnection.HTTP_CREATED, "Villa added successfully");
        } else {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Failed to add villa. Missing required fields (name, description, address) or invalid data.");
        }
    }

    private void handleAddRoomTypeToVilla(Request req, Response res) throws IOException {
        int villaId = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/villas/(\\d+)/rooms");
        if (villaId == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Villa ID");
            return;
        }
        Map<String, Object> reqJsonMap = req.getJSON();
        String name = (String) reqJsonMap.get("name");
        Integer quantity = (Integer) reqJsonMap.get("quantity");
        Integer capacity = (Integer) reqJsonMap.get("capacity");
        Integer price = (Integer) reqJsonMap.get("price");
        String bedSize = (String) reqJsonMap.get("bedSize");
        Boolean hasDesk = (Boolean) reqJsonMap.get("hasDesk");
        Boolean hasAc = (Boolean) reqJsonMap.get("hasAc");
        Boolean hasTv = (Boolean) reqJsonMap.get("hasTv");
        Boolean hasWifi = (Boolean) reqJsonMap.get("hasWifi");
        Boolean hasShower = (Boolean) reqJsonMap.get("hasShower");
        Boolean hasHotwater = (Boolean) reqJsonMap.get("hasHotwater");
        Boolean hasFridge = (Boolean) reqJsonMap.get("hasFridge");

        RoomType newRoomType = new RoomType(
                0, villaId, name, quantity, capacity, price, bedSize,
                hasDesk != null && hasDesk ? 1 : 0,
                hasAc != null && hasAc ? 1 : 0,
                hasTv != null && hasTv ? 1 : 0,
                hasWifi != null && hasWifi ? 1 : 0,
                hasShower != null && hasShower ? 1 : 0,
                hasHotwater != null && hasHotwater ? 1 : 0,
                hasFridge != null && hasFridge ? 1 : 0
        );

        if (villaService.addRoomTypeToVilla(villaId, newRoomType)) {
            res.sendSuccess(HttpURLConnection.HTTP_CREATED, "Room Type added successfully to villa " + villaId);
        } else {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Failed to add room type. Missing required fields or invalid data.");
        }
    }

    private void handleUpdateVilla(Request req, Response res) throws IOException {
        int id = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/villas/(\\d+)");
        if (id == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Villa ID");
            return;
        }
        Map<String, Object> reqJsonMap = req.getJSON();
        String name = (String) reqJsonMap.get("name");
        String description = (String) reqJsonMap.get("description");
        String address = (String) reqJsonMap.get("address");

        Villa updatedVilla = new Villa(id, name, description, address);

        if (villaService.updateVilla(updatedVilla)) {
            res.sendSuccess(HttpURLConnection.HTTP_OK, "Villa updated successfully");
        } else {
            if (villaService.getVillaById(id) == null) {
                res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Villa not found");
            } else {
                res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Failed to update villa. Check provided data.");
            }
        }
    }

    private void handleUpdateRoomTypeInVilla(Request req, Response res) throws IOException {
        Pattern pattern = Pattern.compile("/villas/(\\d+)/rooms/(\\d+)");
        Matcher matcher = pattern.matcher(req.getHttpExchange().getRequestURI().getPath());

        int villaId = -1;
        int roomTypeId = -1;
        if (matcher.find() && matcher.groupCount() == 2) {
            try {
                villaId = Integer.parseInt(matcher.group(1));
                roomTypeId = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException e) { /* handled by -1 defaults */ }
        }

        if (villaId == -1 || roomTypeId == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Villa ID or Room ID in path");
            return;
        }


        final int finalRoomTypeId = roomTypeId;
        final int finalVillaId = villaId;


        Map<String, Object> reqJsonMap = req.getJSON();
        String name = (String) reqJsonMap.get("name");
        Integer quantity = (Integer) reqJsonMap.get("quantity");
        Integer capacity = (Integer) reqJsonMap.get("capacity");
        Integer price = (Integer) reqJsonMap.get("price");
        String bedSize = (String) reqJsonMap.get("bedSize");
        Boolean hasDesk = (Boolean) reqJsonMap.get("hasDesk");
        Boolean hasAc = (Boolean) reqJsonMap.get("hasAc");
        Boolean hasTv = (Boolean) reqJsonMap.get("hasTv");
        Boolean hasWifi = (Boolean) reqJsonMap.get("hasWifi");
        Boolean hasShower = (Boolean) reqJsonMap.get("hasShower");
        Boolean hasHotwater = (Boolean) reqJsonMap.get("hasHotwater");
        Boolean hasFridge = (Boolean) reqJsonMap.get("hasFridge");

        RoomType updatedRoomType = new RoomType(
                finalRoomTypeId, finalVillaId, name, quantity, capacity, price, bedSize,
                hasDesk != null && hasDesk ? 1 : 0,
                hasAc != null && hasAc ? 1 : 0,
                hasTv != null && hasTv ? 1 : 0,
                hasWifi != null && hasWifi ? 1 : 0,
                hasShower != null && hasShower ? 1 : 0,
                hasHotwater != null && hasHotwater ? 1 : 0,
                hasFridge != null && hasFridge ? 1 : 0
        );

        if (villaService.updateRoomTypeInVilla(updatedRoomType)) {
            res.sendSuccess(HttpURLConnection.HTTP_OK, "Room Type updated successfully");
        } else {
            if (villaService.getRoomTypesByVillaId(finalVillaId).stream().noneMatch(rt -> rt.getId() == finalRoomTypeId)) {
                res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Room Type not found in specified Villa");
            } else {
                res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Failed to update room type. Check provided data.");
            }
        }
    }

    private void handleDeleteRoomTypeInVilla(Request req, Response res) throws IOException {
        int roomTypeId = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/villas/\\d+/rooms/(\\d+)");
        if (roomTypeId == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Room ID");
            return;
        }

        if (villaService.deleteRoomType(roomTypeId)) {
            res.sendSuccess(HttpURLConnection.HTTP_OK, "Room Type deleted successfully");
        } else {
            res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Room Type not found or failed to delete");
        }
    }

    private void handleDeleteVilla(Request req, Response res) throws IOException {
        int id = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/villas/(\\d+)");
        if (id == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Villa ID");
            return;
        }

        if (villaService.deleteVilla(id)) {
            res.sendSuccess(HttpURLConnection.HTTP_OK, "Villa deleted successfully");
        } else {
            res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Villa not found or failed to delete");
        }
    }
}

