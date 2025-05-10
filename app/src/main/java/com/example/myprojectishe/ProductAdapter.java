package com.example.myprojectishe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.button.setText(product.getName() + " - " + product.getPrice());
        holder.button.setOnClickListener(v -> listener.onProductClick(product));
    }

    @Override
    public int getItemCount() { return products.size(); }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        Button button;

        ProductViewHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(android.R.id.text1);
        }
    }

    interface OnProductClickListener {
        void onProductClick(Product product);
    }
}