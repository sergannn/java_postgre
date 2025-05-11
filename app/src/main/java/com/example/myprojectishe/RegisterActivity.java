package com.example.myprojectishe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myprojectishe.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myprojectishe.R;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {
    private EditText editUsername, editPassword, editConfirmPassword;
    private Button buttonRegister;
    private TextView textLogin;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        editConfirmPassword = findViewById(R.id.edit_confirm_password);
        buttonRegister = findViewById(R.id.button_register);
        textLogin = findViewById(R.id.text_login);

        buttonRegister.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            String confirmPassword = editConfirmPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                return;
            }

            String passwordHash = hashPassword(password);

            executorService.execute(() -> {
                try (Connection conn = DatabaseHelper.getConnection()) {
                    // Check if username already exists
                    String checkUserSql = "SELECT user_id FROM users WHERE username = ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkUserSql)) {
                        checkStmt.setString(1, username);
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next()) {
                            runOnUiThread(() -> Toast.makeText(this, "Пользователь с таким именем уже существует", Toast.LENGTH_SHORT).show());
                            return;
                        }
                    }

                    // Insert new user
                    String insertUserSql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";
                    long userId = -1;
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setString(1, username);
                        insertStmt.setString(2, passwordHash);
                        insertStmt.setString(3, username + "@example.com");
                        int affectedRows = insertStmt.executeUpdate();
                        System.out.println("INSERT INTO users affected rows: " + affectedRows);

                        if (affectedRows > 0) {
                            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    userId = generatedKeys.getLong(1);
                                    System.out.println("Generated user_id: " + userId);
                                } else {
                                    System.out.println("No generated keys returned for INSERT INTO users.");
                                }
                            }
                        } else {
                            System.out.println("INSERT INTO users did not affect any rows.");
                        }
                    }

                    if (userId != -1) {
                        // Assign customer role
                        String insertUserRoleSql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
                        try (PreparedStatement insertRoleStmt = conn.prepareStatement(insertUserRoleSql)) {
                            insertRoleStmt.setLong(1, userId);
                            insertRoleStmt.setInt(2, 2); // customer role
                            int userRoleAffectedRows = insertRoleStmt.executeUpdate();
                            System.out.println("INSERT INTO user_roles affected rows: " + userRoleAffectedRows);
                        }

                        long finalUserId = userId;
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Ошибка базы данных: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        });

        textLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
