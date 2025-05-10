package com.example.myprojectishe;

public class Order {
    private int id;
    private double totalAmount;
    private String status;

    public Order(int id, double totalAmount, String status) {
        this.id = id;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getId() { return id; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
}