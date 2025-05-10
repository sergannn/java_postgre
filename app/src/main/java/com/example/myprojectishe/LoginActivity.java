package com.example.myprojectishe;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myprojectishe.R;
import com.example.myprojectishe.RegisterActivity;

import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {
    private EditText editUsername, editPassword;
    private Button buttonLogin;
    private TextView textRegister;
    private com.example.myprojectishe.DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new com.example.myprojectishe.DatabaseHelper(this);
        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        buttonLogin = findViewById(R.id.button_login);
        textRegister = findViewById(R.id.text_register);

        buttonLogin.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show();
                return;
            }

            String passwordHash = hashPassword(password);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("users", new String[]{"user_id"},
                    "username = ? AND password_hash = ?", new String[]{username, passwordHash},
                    null, null, null);

            if (cursor.moveToFirst()) {
                int userId = cursor.getInt(0);
                Cursor roleCursor = db.rawQuery(
                        "SELECT r.role_name FROM roles r " +
                        "INNER JOIN user_roles ur ON r.role_id = ur.role_id " +
                        "WHERE ur.user_id = ?", new String[]{String.valueOf(userId)});
                
                if (roleCursor.moveToFirst()) {
                    String role = roleCursor.getString(0);
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
                } else {
                    Toast.makeText(this, "Роль не найдена", Toast.LENGTH_SHORT).show();
                }
                roleCursor.close();
            } else {
                Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
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
}