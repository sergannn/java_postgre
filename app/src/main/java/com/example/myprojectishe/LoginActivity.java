package com.example.myprojectishe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myprojectishe.R;
import com.example.myprojectishe.RegisterActivity;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private EditText editUsername, editPassword;
    private Button buttonLogin;
    private TextView textRegister;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        buttonLogin = findViewById(R.id.button_login);
        textRegister = findViewById(R.id.text_register);

        // Ensure tables are created on app start
        executorService.execute(() -> {
            DatabaseHelper.createTables();
        });

        buttonLogin.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show();
                return;
            }

            String passwordHash = hashPassword(password);

            executorService.execute(() -> {
                try (Connection conn = DatabaseHelper.getConnection()) {
                    String selectUserSql = "SELECT user_id FROM users WHERE username = ? AND password_hash = ?";
                    try (PreparedStatement userStmt = conn.prepareStatement(selectUserSql)) {
                        userStmt.setString(1, username);
                        userStmt.setString(2, passwordHash);
                        try (ResultSet userRs = userStmt.executeQuery()) {
                            if (userRs.next()) {
                                int userId = userRs.getInt("user_id");

                                String selectRoleSql = "SELECT r.role_name FROM roles r " +
                                        "INNER JOIN user_roles ur ON r.role_id = ur.role_id " +
                                        "WHERE ur.user_id = ?";
                                try (PreparedStatement roleStmt = conn.prepareStatement(selectRoleSql)) {
                                    roleStmt.setInt(1, userId);
                                    try (ResultSet roleRs = roleStmt.executeQuery()) {
                                        if (roleRs.next()) {
                                            String role = roleRs.getString("role_name");
                                            runOnUiThread(() -> {
                                                Intent intent;
                                                switch (role) {
                                                    case "customer":
                                                        intent = new Intent(LoginActivity.this, com.example.myprojectishe.ProductsActivity.class);
                                                        break;
                                                    case "courier":
                                                        intent = new Intent(LoginActivity.this, com.example.myprojectishe.DeliveriesActivity.class);
                                                        break;
                                                    case "admin":
                                                        intent = new Intent(LoginActivity.this, com.example.myprojectishe.AdminActivity.class);
                                                        break;
                                                    default:
                                                        Toast.makeText(this, "Неизвестная роль", Toast.LENGTH_SHORT).show();
                                                        return;
                                                }
                                                startActivity(intent);
                                                finish();
                                            });
                                        } else {
                                            runOnUiThread(() -> Toast.makeText(this, "Роль не найдена", Toast.LENGTH_SHORT).show());
                                        }
                                    }
                                }
                            } else {
                                runOnUiThread(() -> Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show());
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Ошибка базы данных: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        });

        textRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
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
