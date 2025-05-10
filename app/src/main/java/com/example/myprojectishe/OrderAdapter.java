package com.example.myprojectishe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<com.example.myprojectishe.Order> orders;

    public OrderAdapter(List<com.example.myprojectishe.Order> orders) {
        this.orders = orders;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        com.example.myprojectishe.Order order = orders.get(position);
        holder.text1.setText("Заказ #" + order.getId());
        holder.text2.setText("Сумма: " + order.getTotalAmount() + " | Статус: " + order.getStatus());
    }

    @Override
    public int getItemCount() { return orders.size(); }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        OrderViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}