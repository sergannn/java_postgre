package com.example.myprojectishe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myprojectishe.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class CartActivity extends AppCompatActivity {
    private RecyclerView recyclerCart;
    private Button buttonSubmit, buttonCancel;
    private ProductAdapter productAdapter;
    private List<Product> cartItems;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize views
        recyclerCart = findViewById(R.id.recycler_cart);
        buttonSubmit = findViewById(R.id.button_submit);
        buttonCancel = findViewById(R.id.button_cancel);

        // Setup RecyclerView
        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        cartItems = CartManager.getInstance().getCartItems();
        productAdapter = new ProductAdapter(cartItems, product -> {});
        recyclerCart.setAdapter(productAdapter);

        buttonSubmit.setOnClickListener(v -> submitOrder());
        buttonCancel.setOnClickListener(v -> cancelOrder());
    }

    private void submitOrder() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Корзина пуста", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            Connection conn = null;
            try {
                conn = DatabaseHelper.getConnection();
                if (conn == null) {
                    throw new SQLException("Failed to establish database connection");
                }

                conn.setAutoCommit(false); // Start transaction

                // 1. Calculate total
                double totalAmount = 0;
                for (Product product : cartItems) {
                    totalAmount += product.getPrice();
                }

                // 2. Insert order
                String insertOrderSql = "INSERT INTO orders (customer_id, total_amount, status) VALUES (?, ?, ?)";
                long orderId = -1;

                try (PreparedStatement pstmt = conn.prepareStatement(
                        insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {

                    pstmt.setInt(1, getCurrentCustomerId());
                    pstmt.setDouble(2, totalAmount);
                    pstmt.setString(3, "processing");

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating order failed, no rows affected");
                    }

                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            orderId = generatedKeys.getLong(1);
                        } else {
                            throw new SQLException("Creating order failed, no ID obtained");
                        }
                    }
                }

                // 3. Insert order items
                String insertItemsSql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";

                try (PreparedStatement pstmt = conn.prepareStatement(insertItemsSql)) {
                    for (Product product : cartItems) {
                        pstmt.setLong(1, orderId);
                        pstmt.setInt(2, product.getId());
                        pstmt.setInt(3, 1); // Default quantity = 1
                        pstmt.setDouble(4, product.getPrice());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }

                conn.commit(); // Commit transaction if all succeeds

                runOnUiThread(() -> {
                    CartManager.getInstance().clearCart();
                    startActivity(new Intent(CartActivity.this, OrderConfirmationActivity.class));
                    finish();
                });

            } catch (SQLException e) {
                runOnUiThread(() -> {
                    Toast.makeText(CartActivity.this,
                            "Ошибка базы данных: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                });
                Log.e("CartActivity", "Database error", e);

                try {
                    if (conn != null) {
                        conn.rollback(); // Rollback on error
                    }
                } catch (SQLException ex) {
                    Log.e("CartActivity", "Rollback failed", ex);
                }
            } finally {
                try {
                    if (conn != null) {
                        conn.close(); // Always close connection
                    }
                } catch (SQLException e) {
                    Log.e("CartActivity", "Error closing connection", e);
                }
            }
        });
    }

    private void cancelOrder() {
        CartManager.getInstance().clearCart();
        startActivity(new Intent(CartActivity.this, ProductsActivity.class));
        finish();
    }

    private int getCurrentCustomerId() {
        // TODO: Replace with actual user session management
        return 1; // Temporary hardcoded value
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}