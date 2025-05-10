package com.example.myprojectishe;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myprojectishe.R;
import com.example.myprojectishe.CartManager;

import java.util.ArrayList;
import java.util.List;

public class ProductsActivity extends AppCompatActivity {
    private RecyclerView recyclerProducts;
    private Button buttonCart;
    private com.example.myprojectishe.DatabaseHelper dbHelper;
    private com.example.myprojectishe.ProductAdapter productAdapter;
    private List<Product> productList;
    private com.example.myprojectishe.CartManager CartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new com.example.myprojectishe.DatabaseHelper(this);
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