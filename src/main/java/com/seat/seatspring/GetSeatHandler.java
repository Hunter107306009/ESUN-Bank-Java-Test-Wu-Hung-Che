package com.seat.seatspring;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetSeatHandler implements HttpHandler {
    Connection conn;

    public GetSeatHandler(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        List<Map<String, Object>> seatInfoList = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT e.EMP_ID, e.FLOOR_SEAT_SEQ, s.FLOOR_NO, s.SEAT_NO " +
                             "FROM Employee e " +
                             "JOIN SeatingChart s ON e.FLOOR_SEAT_SEQ = s.FLOOR_SEAT_SEQ")) {
            while (rs.next()) {
                Map<String, Object> seatInfo = new HashMap<>();
                seatInfo.put("EMP_ID", rs.getInt("EMP_ID"));
                seatInfo.put("FLOOR_SEAT_SEQ", rs.getInt("FLOOR_SEAT_SEQ"));
                seatInfo.put("FLOOR_NO", rs.getInt("FLOOR_NO"));
                seatInfo.put("SEAT_NO", rs.getInt("SEAT_NO"));
                seatInfoList.add(seatInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String json = new Gson().toJson(seatInfoList);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, json.getBytes().length);

        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes());
        os.close();
    }

    private void handleOptions(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        exchange.sendResponseHeaders(204, -1);
    }
}