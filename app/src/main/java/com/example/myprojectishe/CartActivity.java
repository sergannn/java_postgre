package com.example.myprojectishe;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myprojectishe.R;

import java.util.List;

public class CartActivity extends AppCompatActivity {
    private RecyclerView recyclerCart;
    private Button buttonSubmit, buttonCancel;
    private DatabaseHelper dbHelper;
    private ProductAdapter productAdapter;
    private List<Product> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        dbHelper = new DatabaseHelper(this);
        recyclerCart = findViewById(R.id.recycler_cart);
        buttonSubmit = findViewById(R.id.button_submit);
        buttonCancel = findViewById(R.id.button_cancel);

        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        cartItems = CartManager.getInstance().getCartItems();
        productAdapter = new ProductAdapter(cartItems, product -> {});
        recyclerCart.setAdapter(productAdapter);

        buttonSubmit.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Корзина пуста", Toast.LENGTH_SHORT).show();
                return;
            }

            double totalAmount = 0;
            for (Product product : cartItems) {
                totalAmount += product.getPrice();
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("customer_id", 1);
            values.put("total_amount", totalAmount);
            values.put("delivery_address", "ул. Ленина, д.10, кв.5");
            values.put("delivery_latitude", 55.752023);
            values.put("delivery_longitude", 37.617499);
            values.put("status", "processing");
            long orderId = db.insert("orders", null, values);

            if (orderId != -1) {
                CartManager.getInstance().clearCart();
                startActivity(new Intent(CartActivity.this, OrderConfirmationActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Ошибка при оформлении заказа", Toast.LENGTH_SHORT).show();
            }
        });

        buttonCancel.setOnClickListener(v -> {
            CartManager.getInstance().clearCart();
            startActivity(new Intent(CartActivity.this, ProductsActivity.class));
            finish();
        });
    }
}