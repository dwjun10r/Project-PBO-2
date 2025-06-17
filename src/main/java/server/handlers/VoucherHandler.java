package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.Voucher;
import server.Request;
import server.Response;
import services.VoucherService; // Import service baru

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoucherHandler implements HttpHandler {

    private final VoucherService voucherService;

    public VoucherHandler(VoucherService voucherService) {
        this.voucherService = voucherService;
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
                if (path.equals("/vouchers")) {
                    handleGetAllVouchers(res);
                } else if (path.matches("/vouchers/\\d+")) {
                    handleGetVoucherById(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for GET on Voucher");
                }
            } else if ("POST".equals(method)) {
                if (path.equals("/vouchers")) {
                    handleAddVoucher(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for POST on Voucher");
                }
            } else if ("PUT".equals(method)) {
                if (path.matches("/vouchers/\\d+")) {
                    handleUpdateVoucher(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for PUT on Voucher");
                }
            } else if ("DELETE".equals(method)) {
                if (path.matches("/vouchers/\\d+")) {
                    handleDeleteVoucher(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for DELETE on Voucher");
                }
            } else {
                res.sendError(405, "Method Not Allowed");
            }
        } catch (Exception e) {
            System.err.println("Error in VoucherHandler: " + e.getMessage());
            e.printStackTrace();
            res.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGetAllVouchers(Response res) throws IOException {
        List<Voucher> vouchers = voucherService.getAllVouchers();
        res.sendJson(HttpURLConnection.HTTP_OK, vouchers);
    }

    private void handleGetVoucherById(Request req, Response res) throws IOException {
        int id = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/vouchers/(\\d+)");
        if (id == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Voucher ID");
            return;
        }
        Voucher voucher = voucherService.getVoucherById(id);
        if (voucher != null) {
            res.sendJson(HttpURLConnection.HTTP_OK, voucher);
        } else {
            res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Voucher not found");
        }
    }

    private void handleAddVoucher(Request req, Response res) throws IOException {
        Map<String, Object> reqJsonMap = req.getJSON();
        String code = (String) reqJsonMap.get("code");
        String description = (String) reqJsonMap.get("description");
        Double discount = (Double) reqJsonMap.get("discount");
        String startDate = (String) reqJsonMap.get("startDate");
        String endDate = (String) reqJsonMap.get("endDate");

        Voucher newVoucher = new Voucher(code, description, discount, startDate, endDate);

        if (voucherService.addVoucher(newVoucher)) {
            res.sendSuccess(HttpURLConnection.HTTP_CREATED, "Voucher added successfully");
        } else {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Failed to add voucher. Check provided data and date format.");
        }
    }

    private void handleUpdateVoucher(Request req, Response res) throws IOException {
        int id = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/vouchers/(\\d+)");
        if (id == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Voucher ID");
            return;
        }
        Map<String, Object> reqJsonMap = req.getJSON();
        String code = (String) reqJsonMap.get("code");
        String description = (String) reqJsonMap.get("description");
        Double discount = (Double) reqJsonMap.get("discount");
        String startDate = (String) reqJsonMap.get("startDate");
        String endDate = (String) reqJsonMap.get("endDate");

        Voucher updatedVoucher = new Voucher(id, code, description, discount, startDate, endDate);

        if (voucherService.updateVoucher(updatedVoucher)) {
            res.sendSuccess(HttpURLConnection.HTTP_OK, "Voucher updated successfully");
        } else {
            if (voucherService.getVoucherById(id) == null) {
                res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Voucher not found");
            } else {
                res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Failed to update voucher. Check provided data and date format.");
            }
        }
    }

    private void handleDeleteVoucher(Request req, Response res) throws IOException {
        int id = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/vouchers/(\\d+)");
        if (id == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Voucher ID");
            return;
        }

        if (voucherService.deleteVoucher(id)) {
            res.sendSuccess(HttpURLConnection.HTTP_OK, "Voucher deleted successfully");
        } else {
            res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Voucher not found or failed to delete");
        }
    }
}

