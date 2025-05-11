package com.example.myprojectishe;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myprojectishe.R;
import com.google.android.material.button.MaterialButton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminActivity extends AppCompatActivity {
    private RecyclerView recyclerAdmin;
    private MaterialButton buttonCreate, buttonUpdate, buttonDelete;
    private com.example.myprojectishe.ProductAdapter productAdapter;
    private List<Product> productList;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        recyclerAdmin = findViewById(R.id.recycler_admin);
        buttonCreate = findViewById(R.id.button_create);
        buttonUpdate = findViewById(R.id.button_update);
        buttonDelete = findViewById(R.id.button_delete);

        recyclerAdmin.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, product -> {});
        recyclerAdmin.setAdapter(productAdapter);

        loadProducts();

        buttonCreate.setOnClickListener(v -> {
            String insertSql = "INSERT INTO products (name, price, created_by, is_active) VALUES (?, ?, ?, ?)";
            executorService.execute(() -> {
                try (Connection conn = DatabaseHelper.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, "Новый товар");
                    pstmt.setDouble(2, 999.99);
                    pstmt.setInt(3, 1); // Assuming user with ID 1 exists and is the admin
                    pstmt.setBoolean(4, true);
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        runOnUiThread(this::loadProducts); // Reload products after creation
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Ошибка при создании товара", Toast.LENGTH_SHORT).show());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Ошибка базы данных: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        });

        buttonUpdate.setOnClickListener(v -> {
            if (productList.isEmpty()) {
                Toast.makeText(this, "Список товаров пуст", Toast.LENGTH_SHORT).show();
                return;
            }
            String updateSql = "UPDATE products SET name = ? WHERE product_id = ?";
            executorService.execute(() -> {
                try (Connection conn = DatabaseHelper.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setString(1, "Обновленный товар");
                    pstmt.setInt(2, productList.get(0).getId());
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        runOnUiThread(this::loadProducts); // Reload products after update
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Ошибка при обновлении товара", Toast.LENGTH_SHORT).show());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Ошибка базы данных: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        });

        buttonDelete.setOnClickListener(v -> {
            if (productList.isEmpty()) {
                Toast.makeText(this, "Список товаров пуст", Toast.LENGTH_SHORT).show();
                return;
            }
            String deleteSql = "DELETE FROM products WHERE product_id = ?";
            executorService.execute(() -> {
                try (Connection conn = DatabaseHelper.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                    pstmt.setInt(1, productList.get(0).getId());
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        runOnUiThread(this::loadProducts); // Reload products after deletion
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Ошибка при удалении товара", Toast.LENGTH_SHORT).show());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Ошибка базы данных: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        });
    }

    private void loadProducts() {
        productList.clear();
        String selectProductsSql = "SELECT product_id, name, price FROM products WHERE is_active = TRUE";
        executorService.execute(() -> {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(selectProductsSql);
                 ResultSet rs = pstmt.executeQuery()) {

                List<Product> loadedProducts = new ArrayList<>();
                while (rs.next()) {
                    int id = rs.getInt("product_id");
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");
                    loadedProducts.add(new Product(id, name, price));
                }

                runOnUiThread(() -> {
                    productList.addAll(loadedProducts);
                    productAdapter.notifyDataSetChanged();
                });

            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Ошибка загрузки товаров: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
