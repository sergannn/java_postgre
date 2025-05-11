package com.example.myprojectishe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myprojectishe.R;
import com.example.myprojectishe.CartManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductsActivity extends AppCompatActivity {
    private RecyclerView recyclerProducts;
    private Button buttonCart;
    private com.example.myprojectishe.ProductAdapter productAdapter;
    private List<Product> productList;
    private com.example.myprojectishe.CartManager CartManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        recyclerProducts = findViewById(R.id.recycler_products);
        buttonCart = findViewById(R.id.button_cart);

        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));
        productList = new ArrayList<>();
        productAdapter = new com.example.myprojectishe.ProductAdapter(productList, product -> {
            CartManager.getInstance().addProduct(product);
        });
        recyclerProducts.setAdapter(productAdapter);

        loadProducts();

        buttonCart.setOnClickListener(v -> {
            startActivity(new Intent(ProductsActivity.this, com.example.myprojectishe.CartActivity.class));
        });
    }

    private void loadProducts() {
        productList.clear();
        String selectProductsSql = "SELECT product_id, name, price FROM products WHERE is_active = TRUE";
        Log.d("ser",selectProductsSql);
        executorService.execute(() -> {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(selectProductsSql);
                 ResultSet rs = pstmt.executeQuery()) {

                List<Product> loadedProducts = new ArrayList<>();
//                loadedProducts.add(new Product(2, "hanuta", 100));
                Log.d("ser",rs.toString());
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                StringBuilder columns = new StringBuilder("Columns: ");
                for (int i = 1; i <= columnCount; i++) {
                    columns.append(metaData.getColumnName(i)).append(", ");
                }
                Log.d("DB_RESULTS", columns.toString());

                while (rs.next()) {
                    int id = rs.getInt("product_id");

                    String name = rs.getString("name");
                    Log.d("DB_RESULTS",name);
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
