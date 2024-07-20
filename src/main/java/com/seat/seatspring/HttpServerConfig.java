package com.seat.seatspring;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;

public class HttpServerConfig {
    public static void startServer(Connection conn) {
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
}