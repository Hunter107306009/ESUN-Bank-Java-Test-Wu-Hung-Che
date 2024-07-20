package com.seat.seatspring;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateSeatHandler implements HttpHandler {
    Connection conn;

    public UpdateSeatHandler(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        InputStream inputStream = exchange.getRequestBody();
        String requestBody = new String(inputStream.readAllBytes());
        JsonObject jsonObject = JsonParser.parseString(requestBody).getAsJsonObject();

        int empId = jsonObject.get("empId").getAsInt();
        int floorSeatSeq = jsonObject.get("floorSeatSeq").getAsInt();

        String response;
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE Employee SET FLOOR_SEAT_SEQ = NULL WHERE FLOOR_SEAT_SEQ = ?")) {
                pstmt.setInt(1, floorSeatSeq);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE Employee SET FLOOR_SEAT_SEQ = ? WHERE EMP_ID = ?")) {
                pstmt.setInt(1, floorSeatSeq);
                pstmt.setInt(2, empId);
                int updated = pstmt.executeUpdate();
                if (updated > 0) {
                    response = "Seat updated successfully";
                } else {
                    response = "Seat update failed";
                }
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            response = "Error updating seat: " + e.getMessage();
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, response.getBytes().length);

        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private void handleOptions(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        exchange.sendResponseHeaders(204, -1);
    }
}