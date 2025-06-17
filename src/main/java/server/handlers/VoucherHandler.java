package server.handlers; // Sesuaikan dengan struktur package Anda

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.VoucherDAO; // Pastikan import ini benar
import models.Voucher;
import server.Request; // Pastikan import ini benar
import server.Response; // Pastikan import ini benar

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoucherHandler implements HttpHandler {
    private final VoucherDAO voucherDAO = new VoucherDAO();

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
                if (path.equals("/vouchers")) { // GET /vouchers
                    handleGetAllVouchers(res);
                } else if (path.matches("/vouchers/\\d+")) { // GET /vouchers/{id}
                    handleGetVoucherById(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for GET on Voucher");
                }
            } else if ("POST".equals(method)) {
                if (path.equals("/vouchers")) { // POST /vouchers
                    handleAddVoucher(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for POST on Voucher");
                }
            } else if ("PUT".equals(method)) {
                if (path.matches("/vouchers/\\d+")) { // PUT /vouchers/{id}
                    handleUpdateVoucher(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for PUT on Voucher");
                }
            } else if ("DELETE".equals(method)) {
                if (path.matches("/vouchers/\\d+")) { // DELETE /vouchers/{id}
                    handleDeleteVoucher(req, res);
                } else {
                    res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Endpoint not found for DELETE on Voucher");
                }
            } else {
                res.sendError(HttpURLConnection.HTTP_METHOD_NOT_ALLOWED, "Method Not Allowed");
            }
        } catch (Exception e) {
            System.err.println("Error in VoucherHandler: " + e.getMessage());
            e.printStackTrace();
            res.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGetAllVouchers(Response res) throws IOException {
        List<Voucher> vouchers = voucherDAO.getAllVouchers();
        res.sendJson(HttpURLConnection.HTTP_OK, vouchers);
    }

    private void handleGetVoucherById(Request req, Response res) throws IOException {
        int id = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/vouchers/(\\d+)");
        if (id == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Voucher ID");
            return;
        }
        Voucher voucher = voucherDAO.getVoucherById(id);
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

        if (code == null || code.isEmpty() || description == null || description.isEmpty() || discount == null ||
                startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty()) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Missing required fields for Voucher");
            return;
        }
        Pattern dateFormat = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
        if (!dateFormat.matcher(startDate).matches() || !dateFormat.matcher(endDate).matches()) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid date format. Use YYYY-MM-DD hh:mm:ss");
            return;
        }

        Voucher newVoucher = new Voucher(code, description, discount, startDate, endDate);
        if (voucherDAO.addVoucher(newVoucher)) {
            res.sendSuccess(HttpURLConnection.HTTP_CREATED, "Voucher added successfully");
        } else {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Failed to add voucher or invalid data");
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

        if (code == null || code.isEmpty() || description == null || description.isEmpty() || discount == null ||
                startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty()) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Missing required fields for Voucher update");
            return;
        }
        Pattern dateFormat = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
        if (!dateFormat.matcher(startDate).matches() || !dateFormat.matcher(endDate).matches()) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid date format. Use YYYY-MM-DD hh:mm:ss");
            return;
        }

        Voucher updatedVoucher = new Voucher(id, code, description, discount, startDate, endDate);
        if (voucherDAO.updateVoucher(updatedVoucher)) {
            res.sendSuccess(HttpURLConnection.HTTP_OK, "Voucher updated successfully");
        } else {
            res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Voucher not found or failed to update");
        }
    }

    private void handleDeleteVoucher(Request req, Response res) throws IOException {
        int id = extractIdFromPath(req.getHttpExchange().getRequestURI().getPath(), "/vouchers/(\\d+)");
        if (id == -1) {
            res.sendError(HttpURLConnection.HTTP_BAD_REQUEST, "Invalid Voucher ID");
            return;
        }
        if (voucherDAO.deleteVoucher(id)) {
            res.sendSuccess(HttpURLConnection.HTTP_OK, "Voucher deleted successfully");
        } else {
            res.sendError(HttpURLConnection.HTTP_NOT_FOUND, "Voucher not found or failed to delete");
        }
    }
}