package com.example.myprojectishe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myprojectishe.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> products;
    private OnProductClickListener listener;

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create your own item layout or use a proper layout with a Button
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false); // Create this layout file
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.button.setText(product.getName() + " - $" + product.getPrice());
        holder.button.setOnClickListener(v -> listener.onProductClick(product));
    }

    @Override
    public int getItemCount() { return products.size(); }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        Button button;

        ProductViewHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.product_button); // Match this ID in your layout
        }
    }

    interface OnProductClickListener {
        void onProductClick(Product product);
    }
}