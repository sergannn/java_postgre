package com.example.myprojectishe;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myprojectishe.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeliveriesActivity extends AppCompatActivity {
    private RecyclerView recyclerDeliveries;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliveries);

        recyclerDeliveries = findViewById(R.id.recycler_deliveries);

        recyclerDeliveries.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList);
        recyclerDeliveries.setAdapter(orderAdapter);

        loadOrders();
    }

    private void loadOrders() {
        orderList.clear();
        String selectOrdersSql = "SELECT order_id, total_amount, status FROM orders";
        executorService.execute(() -> {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(selectOrdersSql);
                 ResultSet rs = pstmt.executeQuery()) {

                List<Order> loadedOrders = new ArrayList<>();
                while (rs.next()) {
                    int id = rs.getInt("order_id");
                    double totalAmount = rs.getDouble("total_amount");
                    String status = rs.getString("status");
                    loadedOrders.add(new Order(id, totalAmount, status));
                }

                runOnUiThread(() -> {
                    orderList.addAll(loadedOrders);
                    orderAdapter.notifyDataSetChanged();
                });

            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Ошибка загрузки заказов: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
