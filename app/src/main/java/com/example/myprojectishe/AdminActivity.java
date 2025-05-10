package com.example.myprojectishe;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myprojectishe.R;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private RecyclerView recyclerAdmin;
    private Button buttonCreate, buttonUpdate, buttonDelete;
    private com.example.myprojectishe.DatabaseHelper dbHelper;
    private com.example.myprojectishe.ProductAdapter productAdapter;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        dbHelper = new DatabaseHelper(this);
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
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", "Новый товар");
            values.put("price", 999.99);
            values.put("created_by", 1);
            long productId = db.insert("products", null, values);
            if (productId != -1) {
                loadProducts();
            } else {
                Toast.makeText(this, "Ошибка при создании товара", Toast.LENGTH_SHORT).show();
            }
        });

        buttonUpdate.setOnClickListener(v -> {
            if (productList.isEmpty()) {
                Toast.makeText(this, "Список товаров пуст", Toast.LENGTH_SHORT).show();
                return;
            }
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", "Обновленный товар");
            db.update("products", values, "product_id = ?", new String[]{String.valueOf(productList.get(0).getId())});
            loadProducts();
        });

        buttonDelete.setOnClickListener(v -> {
            if (productList.isEmpty()) {
                Toast.makeText(this, "Список товаров пуст", Toast.LENGTH_SHORT).show();
                return;
            }
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete("products", "product_id = ?", new String[]{String.valueOf(productList.get(0).getId())});
            loadProducts();
        });
    }

    private void loadProducts() {
        productList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("products", new String[]{"product_id", "name", "price"},
                "is_active = 1", null, null, null, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            double price = cursor.getDouble(2);
            productList.add(new Product(id, name, price));
        }
        cursor.close();
        productAdapter.notifyDataSetChanged();
    }
}