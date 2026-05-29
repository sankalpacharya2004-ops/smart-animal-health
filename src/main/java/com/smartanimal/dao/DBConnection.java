package com.smartanimal.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static String dbUrl = "jdbc:mysql://localhost:3306/animal_health_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String dbUser = "root";
    private static String dbPassword = ""; // Default local MySQL has no password

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found in classpath!");
            e.printStackTrace();
        }
    }

    // Allow overriding connection parameters from context listeners or configuration
    public static void initialize(String url, String user, String password) {
        if (url != null && !url.trim().isEmpty()) {
            dbUrl = url;
        }
        if (user != null) {
            dbUser = user;
        }
        if (password != null) {
            dbPassword = password;
        }
    }

    public static Connection getConnection() throws SQLException {
        // Fallback check for env parameters
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");

        String activeUrl = (envUrl != null) ? envUrl : dbUrl;
        String activeUser = (envUser != null) ? envUser : dbUser;
        String activePass = (envPass != null) ? envPass : dbPassword;

        return DriverManager.getConnection(activeUrl, activeUser, activePass);
    }
}
