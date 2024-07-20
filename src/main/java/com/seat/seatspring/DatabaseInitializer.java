package com.seat.seatspring;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initialize(Connection conn) {
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