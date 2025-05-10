package com.example.myprojectishe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myprojectishe.R;

public class OrderConfirmationActivity extends AppCompatActivity {
    private Button buttonBackToStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        buttonBackToStore = findViewById(R.id.button_back_to_store);

        buttonBackToStore.setOnClickListener(v -> {
            startActivity(new Intent(OrderConfirmationActivity.this, ProductsActivity.class));
            finish();
        });
    }
}