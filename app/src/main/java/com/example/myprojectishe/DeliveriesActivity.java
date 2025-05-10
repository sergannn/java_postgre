package com.example.myprojectishe;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myprojectishe.R;

import java.util.ArrayList;
import java.util.List;

public class DeliveriesActivity extends AppCompatActivity {
    private RecyclerView recyclerDeliveries;
    private com.example.myprojectishe.DatabaseHelper dbHelper;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliveries);

        dbHelper = new com.example.myprojectishe.DatabaseHelper(this);
        recyclerDeliveries = findViewById(R.id.recycler_deliveries);

        recyclerDeliveries.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList);
        recyclerDeliveries.setAdapter(orderAdapter);

        loadOrders();
    }

    private void loadOrders() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("orders", new String[]{"order_id", "total_amount", "status"},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            double totalAmount = cursor.getDouble(1);
            String status = cursor.getString(2);
            orderList.add(new Order(id, totalAmount, status));
        }
        cursor.close();
        orderAdapter.notifyDataSetChanged();
    }
}