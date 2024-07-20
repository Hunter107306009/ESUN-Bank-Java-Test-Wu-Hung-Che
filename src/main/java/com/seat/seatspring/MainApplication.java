package com.seat.seatspring;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class MainApplication {
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
                DatabaseInitializer.initialize(conn);
                HttpServerConfig.startServer(conn);
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        }
    }
}