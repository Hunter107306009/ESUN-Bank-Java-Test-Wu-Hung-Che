package com.seat.seatspring;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
public class ConnectDB {
    private final static String db = "jdbc:postgresql://localhost:5432/postgres";
    private final static String user = "postgres";
    private final static String password = "410684";

    private static Connection conn = null;

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        System.out.println("Connecting to PostgreSQL database...");
        try {
            conn = DriverManager.getConnection(db, user, password);
            if (conn != null) {
                System.out.println("Connected to the database!");
                initializeDatabase();
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        }

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(7070), 0);
            server.createContext("/getId", new GetIdHandler(conn));
            server.createContext("/getSeat", new GetSeatHandler(conn));
            server.createContext("/updateSeat", new UpdateSeatHandler(conn));
            server.setExecutor(null);
            server.start();
            System.out.println("Server started on port 8080");
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }

    private void initializeDatabase() {
        try (Statement stmt = conn.createStatement()) {
            String sql = "DROP TABLE IF EXISTS Employee CASCADE;" +
                    "DROP TABLE IF EXISTS SeatingChart CASCADE;" +
                    "CREATE TABLE SeatingChart (" +
                    "    FLOOR_SEAT_SEQ INT PRIMARY KEY," +
                    "    FLOOR_NO INT NOT NULL," +
                    "    SEAT_NO INT NOT NULL" +
                    ");" +
                    "CREATE TABLE Employee (" +
                    "    EMP_ID INT PRIMARY KEY," +
                    "    NAME VARCHAR(100) NOT NULL," +
                    "    EMAIL VARCHAR(100) NOT NULL," +
                    "    FLOOR_SEAT_SEQ INT," +
                    "    FOREIGN KEY (FLOOR_SEAT_SEQ) REFERENCES SeatingChart(FLOOR_SEAT_SEQ)" +
                    ");" +
                    "INSERT INTO SeatingChart (FLOOR_SEAT_SEQ, FLOOR_NO, SEAT_NO) VALUES" +
                    "    (101, 1, 1)," +
                    "    (102, 1, 2)," +
                    "    (103, 1, 3)," +
                    "    (104, 1, 4)," +
                    "    (201, 2, 1)," +
                    "    (202, 2, 2)," +
                    "    (203, 2, 3)," +
                    "    (204, 2, 4)," +
                    "    (301, 3, 1)," +
                    "    (302, 3, 2)," +
                    "    (303, 3, 3)," +
                    "    (304, 3, 4)," +
                    "    (401, 4, 1)," +
                    "    (402, 4, 2)," +
                    "    (403, 4, 3)," +
                    "    (404, 4, 4);" +
                    "INSERT INTO Employee (EMP_ID, NAME, EMAIL, FLOOR_SEAT_SEQ) VALUES" +
                    "    (108307001, '小王', '108307001@gmail.com', NULL)," +
                    "    (108307002, '小林', '108307002@gmail.com', NULL)," +
                    "    (108307003, '小吳', '108307003@gmail.com', NULL)," +
                    "    (108307004, '小周', '108307004@gmail.com', NULL)," +
                    "    (108307005, '小華', '108307005@gmail.com', 303)," +
                    "    (108307006, '小米', '108307006@gmail.com', 102);";
            stmt.execute(sql);
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class GetIdHandler implements HttpHandler {
    Connection conn;
    Statement stmt = null;
    ResultSet rs = null;

    public GetIdHandler(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        List<Integer> empIds = new ArrayList<>();
        try {
            stmt = conn.createStatement();
            String sql = "SELECT EMP_ID FROM Employee WHERE FLOOR_SEAT_SEQ IS NULL";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                empIds.add(rs.getInt("EMP_ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        String json = new Gson().toJson(empIds);

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

class GetSeatHandler implements HttpHandler {
    Connection conn;
    Statement stmt = null;
    ResultSet rs = null;

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
        try {
            stmt = conn.createStatement();
            String sql = "SELECT e.EMP_ID, e.FLOOR_SEAT_SEQ, s.FLOOR_NO, s.SEAT_NO " +
                    "FROM Employee e " +
                    "JOIN SeatingChart s ON e.FLOOR_SEAT_SEQ = s.FLOOR_SEAT_SEQ";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Map<String, Object> seatInfo = new HashMap<>();
                seatInfo.put("EMP_ID", rs.getInt("EMP_ID"));
                seatInfo.put("FLOOR_SEAT_SEQ", rs.getInt("FLOOR_SEAT_SEQ"));
                seatInfo.put("FLOOR_NO", rs.getString("FLOOR_NO"));
                seatInfo.put("SEAT_NO", rs.getString("SEAT_NO"));
                seatInfoList.add(seatInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

class UpdateSeatHandler implements HttpHandler {
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
        } catch (SQLException e) {
            e.printStackTrace();
            response = "Error updating seat: " + e.getMessage();
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
