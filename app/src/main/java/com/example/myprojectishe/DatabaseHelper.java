package com.example.myprojectishe;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {

    private static final String DB_ENGINE = "postgresql";
    private static final String DB_USERNAME = "alex";
    private static final String DB_PASS = "Alex1987!";
    private static final String DB_HOST = "79.174.88.242";
    private static final String DB_PORT = "18781";
    private static final String DB_NAME = "java";

    private static final String DB_URL = "jdbc:" + DB_ENGINE + "://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASS);
    }

    public static void createTables() {
        String createRolesTable = "CREATE TABLE IF NOT EXISTS roles (" +
                "role_id SERIAL PRIMARY KEY, " +
                "role_name VARCHAR(255) NOT NULL UNIQUE, " +
                "description TEXT)";

        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "user_id SERIAL PRIMARY KEY, " +
                "username VARCHAR(255) NOT NULL UNIQUE, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255) NOT NULL UNIQUE, " +
                "phone VARCHAR(255), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_active BOOLEAN DEFAULT TRUE)";

        String createUserRolesTable = "CREATE TABLE IF NOT EXISTS user_roles (" +
                "user_id INTEGER, " +
                "role_id INTEGER, " +
                "PRIMARY KEY (user_id, role_id), " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id), " +
                "FOREIGN KEY (role_id) REFERENCES roles(role_id))";

        String createProductsTable = "CREATE TABLE IF NOT EXISTS products (" +
                "product_id SERIAL PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "description TEXT, " +
                "price DECIMAL(10, 2) NOT NULL, " +
                "stock_quantity INTEGER NOT NULL DEFAULT 0, " +
                "created_by INTEGER, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_active BOOLEAN DEFAULT TRUE, " +
                "FOREIGN KEY (created_by) REFERENCES users(user_id))";

        String insertRoles = "INSERT INTO roles (role_name, description) VALUES " +
                "('admin', 'Полный доступ'), " +
                "('customer', 'Покупатель'), " +
                "('courier', 'Курьер') " +
                "ON CONFLICT (role_name) DO NOTHING"; // Prevent duplicate inserts on subsequent runs

        try (Connection conn = getConnection()) {
            conn.createStatement().execute(createRolesTable);
            conn.createStatement().execute(createUsersTable);
            conn.createStatement().execute(createUserRolesTable);
            conn.createStatement().execute(createProductsTable);
            conn.createStatement().execute(insertRoles);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
